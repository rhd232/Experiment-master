package com.jz.experiment.module.bluetooth;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jz.experiment.module.bluetooth.event.BluetoothConnectedEvent;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.DataFileUtil;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsbService extends CommunicationService {
    private PendingIntent mRequestPermissionPendingIntent;
    private UsbManager mUsbManager;
    public static final String ACTION_DEVICE_PERMISSION = "action_device_permission";

    public static final String ACTION_DEVICE_ATTACHED = "action_device_attached";
    public static final String ACTION_DEVICE_DETACHED = "action_device_detached";

    public class LocalBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    private final IBinder mBinder = new UsbService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initialize();

        return mBinder;
    }


    private UsbEndpoint mUsbEndpointIn;
    private UsbEndpoint mUsbEndpointOut;
    private UsbDeviceConnection mUsbDeviceConnection;
    private ExecutorService mExecutorService;

    private ReadThread mReadThread;

    @Override
    public boolean initialize() {
        mExecutorService = Executors.newSingleThreadExecutor();
        //申请USB使用的权限
        mRequestPermissionPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DEVICE_PERMISSION), 0);
        //注册接收申请权限结果的广播接收器
        IntentFilter permissionFilter = new IntentFilter(ACTION_DEVICE_PERMISSION);
        registerReceiver(mUsbPermissionReceiver, permissionFilter);

        IntentFilter usbEventFilter = new IntentFilter();
        usbEventFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbEventFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(mUsbEventReceiver, usbEventFilter);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        return true;


      /*  ProbeTable customTable = new ProbeTable();
        customTable.addProduct(1667, 22608, CdcAcmSerialDriver.class);

        UsbSerialProber prober = new UsbSerialProber(customTable);
        mAllAvailableDrivers = prober.findAllDrivers(mUsbManager);*/
    }


    private UsbDevice mTargetDevice;

    @Override
    public Device getConnectedDevice() {
        if (mTargetDevice == null) {
            return null;
        }
        String name = "HID";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            name = mTargetDevice.getProductName();
        }
        Device device = new Device(name, "");
        return device;
    }

    public void connect(String deviceName) {
        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceMap.values().iterator();
        UsbDevice targetDevice = null;
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            if (deviceName.equals(device.getDeviceName())) {
                targetDevice = device;
                break;
            }
        }
        mTargetDevice = targetDevice;
        if (targetDevice != null) {
            if (mUsbManager.hasPermission(targetDevice)) {
                usbDeviceInit(targetDevice);
            } else {
                //没有权限则申请权限
                mUsbManager.requestPermission(targetDevice, mRequestPermissionPendingIntent);
            }
        }
    }


    public boolean hasPermission() {
        if (mTargetDevice != null) {
            return mUsbManager.hasPermission(mTargetDevice);
        } else {
            return false;
        }
    }

    public void requestPermission() {
        if (mTargetDevice != null)
            mUsbManager.requestPermission(mTargetDevice, mRequestPermissionPendingIntent);
        else {
            ToastUtil.showSystemToast(getApplicationContext(), "请插入HID设备");
        }
    }

    @Override
    public boolean isConnected() {
        return hasPermission();
    }


    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_DEVICE_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (granted) {
                    //获得了usb使用权限
                    usbDeviceInit(device);

                }
            }
        }
    };

    private void usbDeviceInit(UsbDevice device) {
        int interfaceCount = device.getInterfaceCount();
        UsbInterface usbInterface = null;
        for (int i = 0; i < interfaceCount; i++) {
            usbInterface = device.getInterface(i);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
                break;
            }
        }
        if (usbInterface != null) {
            //mUsbInterfaceMap.put(device.getDeviceName(), usbInterface);
            UsbDeviceConnection connection = mUsbManager.openDevice(device);
            if (connection != null) {
                // mUsbDeviceConnectionMap.put(device.getDeviceName(), connection);
                mUsbDeviceConnection = connection;
                if (connection.claimInterface(usbInterface, true)) {
                    mUsbEndpointOut = usbInterface.getEndpoint(1);
                    mUsbEndpointIn = usbInterface.getEndpoint(0);
                   /* for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                        UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                        //类型为大块传输
                        if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                                //mUsbEndpointOutMap.put(device.getDeviceName(), endpoint);
                                mUsbEndpointOut = endpoint;
                            } else {
                                mUsbEndpointIn = endpoint;
                                //mUsbEndpointInMap.put(device.getDeviceName(), endpoint);
                            }
                        }

                    }*/
                }
                //开启查询打印机状态的线程
             /*   if (mReadThread != null) {
                    mReadThread.stopRun();
                }
                mReadThread = new ReadThread();
                mReadThread.start();*/
            }
            String name = "HID";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                name = device.getProductName();
            }
           /* byte[] bytes = name.getBytes();
            Data data = new Data(bytes, bytes.length);
            broadcastUpdate(ACTION_DEVICE_ATTACHED, data);*/
            //发送设备连接成功通知
            BluetoothConnectedEvent event = new BluetoothConnectedEvent(name);
            EventBus.getDefault().post(event);


            if(mListener!=null){
                mListener.onConnectSuccess();
            }
        }


       /* List<UsbSerialPort> result = new ArrayList<>();

        for (final UsbSerialDriver driver : mAllAvailableDrivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            result.addAll(ports);
        }
        UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(device);
        try {
            mUsbSerialPort = result.get(0);
            mUsbSerialPort.open(usbDeviceConnection);
            mUsbSerialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            onDeviceStateChange();
            UsbInterface anInterface = device.getInterface(0);
            if (anInterface == null) {
                Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < anInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = anInterface.getEndpoint(i);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                        mUsbEndpointIn = endpoint;
                    } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                        mUsbEndpointOut = endpoint;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /*private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }*/

    /*private void stopIoManager() {
        if (mSerialIoManager != null) {
            //Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mUsbSerialPort != null) {
           // Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mUsbSerialPort, mListener);
            mExecutorService.submit(mSerialIoManager);
        }
    }
*/
    private void toByteString(PcrCommand cmd) {
        ArrayList<Byte> bytes = cmd.getCommandList();
        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            data[i] = bytes.get(i).byteValue();
        }
        String hex = ByteUtil.getHexStr(data,data.length);
       /* StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }*/
        System.out.println("发送：" + hex);
        DataFileUtil.writeFileLog("发送：" + hex);
    }

    public int sendPcrCommand(PcrCommand command) {
        int err = 0;
        try {
            if (mReadThread == null || !mReadThread.mRun) {
                startReadThread();
                Thread.sleep(100);
            }
            toByteString(command);
            ArrayList<Byte> bytes = command.getCommandList();
            byte[] data = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                data[i] = bytes.get(i).byteValue();
            }
            return bulk(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            err = -1;
        }
        return err;

    }

    public byte[] sendPcrCommandSync(PcrCommand command) {
        //停止读取线程。
        stopReadThread();
        toByteString(command);
        ArrayList<Byte> bytes = command.getCommandList();
        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            data[i] = bytes.get(i).byteValue();
        }
        return bulkSync(bytes);
    }

    private byte[] bulkSync(ArrayList<Byte> command) {
        if (command != null && !command.isEmpty()) {

            if (mUsbEndpointOut == null || mUsbDeviceConnection == null) {
                return null;
            }
            byte[] data = new byte[command.size()];
            for (int i = 0; i < command.size(); i++) {
                data[i] = command.get(i).byteValue();
            }

            //超时时间需要设置的长一点，不然很可能打印卡住，返回-1。
            int ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, data, data.length, 5000);
            if (ret >= 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[64];
                int bytes = this.mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer, 64, 5000);

                if (bytes > 0) {
                    StringBuilder hex = new StringBuilder(buffer.length * 2);
                    for (int i = 0; i < bytes; i++) {
                        byte b = buffer[i];
                        if ((b & 0xFF) < 0x10) hex.append("0");
                        hex.append(Integer.toHexString(b & 0xFF));
                    }
                    System.out.println("同步接收到:" + hex.toString().toLowerCase());
                    DataFileUtil.writeFileLog("同步接收到：" + hex.toString().toLowerCase());
                    //Data d = new Data(buffer, bytes);
                    //broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, d);
                }
                return buffer;
            }

            return null;
        } else {
            return null;
        }
    }

    private int bulk(ArrayList<Byte> command) {
        if (command != null && !command.isEmpty()) {

            if (mUsbEndpointOut == null || mUsbDeviceConnection == null) {
                return -1;
            }
            byte[] data = new byte[command.size()];
            for (int i = 0; i < command.size(); i++) {
                data[i] = command.get(i).byteValue();
            }

            //超时时间需要设置的长一点，不然很可能打印卡住，返回-1。
            int ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, data, data.length, 5000);
            if (ret >= 0) {
               /* try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[1024];
                int bytes = this.mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer, 64, 5000);
                //System.out.println("mmEndIn:"+bytes);
                if (bytes > 0) {
                    StringBuilder hex = new StringBuilder(buffer.length * 2);
                    for (byte b : buffer) {
                        if ((b & 0xFF) < 0x10) hex.append("0");
                        hex.append(Integer.toHexString(b & 0xFF));
                    }
                    System.out.println("接收到:"+hex.toString().toLowerCase());
                    Data d = new Data(buffer, bytes);
                    broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, d);
                }*/
                return 0;
            }

            return -1;
        } else {
            return -1;
        }

    }

    private void broadcastUpdate(String action, Data data) {
        try {
            final Intent intent = new Intent(action);
            //  String str = new String(buffer,"ISO-8859-1");
            intent.putExtra(BluetoothService.EXTRA_DATA, data);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopReadThread() {
        if (mReadThread != null) {
            mReadThread.stopRun();
            mReadThread = null;
        }
    }

    public void startReadThread() {
        stopReadThread();
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    private class ReadThread extends Thread {
        //private String deviceName;
        UsbEndpoint mmEndIn;
        UsbEndpoint mmEndOut;
        UsbDeviceConnection mmConnection;
        boolean mRun;

        public ReadThread() {
            //this.deviceName = deviceName;
            this.mRun = true;
            mmEndOut = mUsbEndpointOut;
            mmEndIn = mUsbEndpointIn;
            mmConnection = mUsbDeviceConnection;
        }

        private void stopRun() {
            mRun = false;
        }

        @Override
        public void run() {

            if (mmEndOut != null && mmEndIn != null) {

                while (mRun) {
                    try {
                        byte[] buffer = new byte[64];
                        int bytes = this.mmConnection.bulkTransfer(mmEndIn, buffer, 64, 5000);
                        //System.out.println("mmEndIn:"+bytes);
                        if (bytes > 0) {
                            StringBuilder hex = new StringBuilder(bytes * 2);

                            for (int i = 0; i < bytes; i++) {
                                byte b = buffer[i];
                                if ((b & 0xFF) < 0x10) hex.append("0");
                                hex.append(Integer.toHexString(b & 0xFF));
                            }
                            System.out.println("接收到:" + hex.toString().toLowerCase());
                            DataFileUtil.writeFileLog("接收到：" + hex.toString().toLowerCase());

                            Data data = new Data(buffer, bytes);
                            Message msg = Message.obtain();
                            msg.what = 3;
                            msg.obj = data;
                            mHandler.sendMessage(msg);
                        }

                        Thread.sleep(10);
                    } catch (InterruptedException var5) {
                        // UsbPort.this.connectionLost();

                        break;
                    }
                }

                Log.d("UsbPortService", "Closing Usb work");
            } else {
              /*  UsbPort.this.stop();
                UsbPort.this.connectionLost();*/

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mUsbEventReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unregisterReceiver(mUsbPermissionReceiver);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private final BroadcastReceiver mUsbEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

                connect(device.getDeviceName());
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                mTargetDevice = null;
                String name = "HID";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    name = device.getProductName();
                }
                BluetoothDisConnectedEvent event = new BluetoothDisConnectedEvent(name);
                EventBus.getDefault().post(event);
            }
        }
    };
}
