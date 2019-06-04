package com.anitoa.io;

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
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.anitoa.C;
import com.anitoa.bean.Data;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.event.AnitoaConnectedEvent;
import com.anitoa.util.AnitoaLogUtil;
import com.anitoa.util.ByteUtil;
import com.anitoa.util.ThreadUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class AnitoaUsbPort extends AnitoaPort {
    private UsbManager mUsbManager;
    private PendingIntent mRequestPermissionPendingIntent;
    private Context mContext;
    public static final String ACTION_DEVICE_PERMISSION = "action_device_permission";

    private AnitoaUsbPort.ConnectThread mConnectThread = null;
    private AnitoaUsbPort.ConnectedThread mConnectedThread = null;
    
    public AnitoaUsbPort(Context context, int deviceId, String deviceName, Handler handler) {
        super(deviceId,deviceName,handler);
        mContext=context;
        mUsbManager= (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public synchronized void connect() {
        Log.d("UsbPortService", "connect to usb device ");
        //清除数据
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        //启动连接线程
        this.mConnectThread = new AnitoaUsbPort.ConnectThread(this.mDeviceName);
        this.mConnectThread.start();
    }

    private void registerReceiver(){
        //申请USB使用的权限
        mRequestPermissionPendingIntent = PendingIntent.getBroadcast(mContext,
                0, new Intent(ACTION_DEVICE_PERMISSION), 0);
        //注册接收申请权限结果的广播接收器
        IntentFilter permissionFilter = new IntentFilter(ACTION_DEVICE_PERMISSION);
        mContext.registerReceiver(mUsbPermissionReceiver, permissionFilter);
    }
    


    @Override
    public int sendPcrCommand(PcrCommand command) {
        return mConnectedThread==null?-1:mConnectedThread.sendPcrCommand(command);
    }

    @Override
    public byte[] sendPcrCommandSync(PcrCommand command) {
        return mConnectedThread==null?new byte[64]:mConnectedThread.sendPcrCommandSync(command);
    }

    public AnitoaUsbPort getUsbPort(){
        return this;
    }

    /**
     * 找到指定设备并且已经获取到UsbDeviceConnection和UsbInterface
     * @param connection
     * @param intf
     */
    private synchronized void connected(UsbDeviceConnection connection, UsbInterface intf) {
        Log.d("UsbPortService", "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectedThread = new AnitoaUsbPort.ConnectedThread(connection, intf);
        this.mConnectedThread.start();
       /* Message msg = this.mHandler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putInt("printer.id", this.mPrinterId);
        bundle.putString("device_name", "Gprinter");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        this.setState(3);*/
    }
    
    
    private class ConnectThread extends Thread {
        private UsbDevice mmUSBDevice = null;
        private String mmDeviceName = null;
        private UsbDeviceConnection mmConnection = null;
        private UsbInterface mmIntf;

        public ConnectThread(String devicename) {
            this.mmDeviceName = devicename;
            this.mmUSBDevice = null;
            this.mmConnection = null;
            this.mmIntf = null;
        }

        public void run() {
            Log.i("UsbPortService", "BEGIN mConnectThread");
            this.setName("ConnectThread");
            this.mmUSBDevice = null;
            HashMap<String, UsbDevice> usbDeviceList = getUsbPort().mUsbManager.getDeviceList();
            //找到对应的设备
            if (!this.mmDeviceName.equals("")) {
                Log.d("UsbPortService", "UsbDeviceName not empty. Trying to open it...");
                this.mmUSBDevice = (UsbDevice)usbDeviceList.get(this.mmDeviceName);
            }

            if (this.mmUSBDevice != null) {
                //检查是否有权限连接设备
                if (!getUsbPort().mUsbManager.hasPermission(this.mmUSBDevice)) {
                     registerReceiver();
                     getUsbPort().mUsbManager.requestPermission(mmUSBDevice, mRequestPermissionPendingIntent);
                    
                } else {
                    int count = this.mmUSBDevice.getInterfaceCount();
                    UsbInterface intf = null;
                    for(int i = 0; i < count; ++i) {
                        intf = this.mmUSBDevice.getInterface(i);
                        //寻找hid interface
                        if (intf.getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
                            break;
                        }
                    }

                    if (intf != null) {
                        this.mmIntf = intf;
                        this.mmConnection = null;
                        //打开连接
                        this.mmConnection = getUsbPort().mUsbManager.openDevice(this.mmUSBDevice);
                        if (this.mmConnection != null) {
                            synchronized(getUsbPort()) {
                                getUsbPort().mConnectThread = null;
                            }
                            getUsbPort().connected(this.mmConnection, this.mmIntf);
                        } else {
                            getUsbPort().stop();
                        }
                    } else {
                        getUsbPort().stop();
                    }
                }
            } else {
                Log.e("UsbPortService", "Cannot find usb device");
                getUsbPort().stop();
            }

        }

        public void cancel() {
            if (this.mmConnection != null) {
                this.mmConnection.releaseInterface(this.mmIntf);
                this.mmConnection.close();
            }

            this.mmConnection = null;
        }
    }

    /**
     * 获取输入输出端点，以便进行读取和发送数据
     */
    private class ConnectedThread extends Thread {
        UsbDeviceConnection mmConnection;
        UsbInterface mmIntf;
        private UsbEndpoint mmEndIn = null;
        private UsbEndpoint mmEndOut = null;
        private boolean mSync;
        public byte[] mSyncReceivedBytes;
        public ConnectedThread(UsbDeviceConnection Connection, UsbInterface Intf) {
            Log.d("UsbPortService", "create ConnectedThread");
            this.mmConnection = Connection;
            this.mmIntf = Intf;
            Log.i("UsbPortService", "BEGIN mConnectedThread");
            if (this.mmConnection.claimInterface(this.mmIntf, true)) {
               /* for(int i = 0; i < this.mmIntf.getEndpointCount(); ++i) {
                    UsbEndpoint ep = this.mmIntf.getEndpoint(i);

                    if (ep.getType() == 2) {
                        if (ep.getDirection() == 0) {
                            this.mmEndOut = ep;
                        } else {
                            this.mmEndIn = ep;
                        }
                    }
                }*/
                mmEndOut = mmIntf.getEndpoint(1);
                mmEndIn = mmIntf.getEndpoint(0);
            }

            String name = "HID";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                name = mConnectThread.mmUSBDevice.getProductName();
            }
            //发送设备连接成功通知
            AnitoaConnectedEvent event = new AnitoaConnectedEvent(name);
            EventBus.getDefault().post(event);
        }

        public void run() {
            if (this.mmEndOut != null && this.mmEndIn != null) {
               getUsbPort().mClosePort = false;

                while(!getUsbPort().mClosePort) {
                    try {
                        byte[] receiveData = new byte[64];
                        int bytes = this.mmConnection.bulkTransfer(this.mmEndIn, receiveData, receiveData.length, 1000);
                        if (bytes > 0) {
                            //TODO 读取到下位机返回的数据
                            if (mSync) {
                                mSyncReceivedBytes = receiveData;
                            } else {
                                mSyncReceivedBytes = null;
                                StringBuilder hex = new StringBuilder(bytes * 2);

                                for (int i = 0; i < bytes; i++) {
                                    byte b = receiveData[i];
                                    if ((b & 0xFF) < 0x10) hex.append("0");
                                    hex.append(Integer.toHexString(b & 0xFF));
                                }
                                AnitoaLogUtil.writeFileLog("接收到：" + hex.toString().toLowerCase());

                                Data data = new Data(receiveData, bytes);
                                Message msg = Message.obtain();
                                msg.what = C.WHAT.RECEIVED_DATA;
                                msg.obj = data;
                                mHandler.sendMessage(msg);
                            }
                        }

                        Thread.sleep(10);
                    } catch (InterruptedException var5) {
                        break;
                    }
                }

                Log.d("UsbPortService", "Closing Usb work");
            } else {
                getUsbPort().stop();

            }

        }

        public void cancel() {
            getUsbPort().mClosePort = true;
            this.mmConnection.releaseInterface(this.mmIntf);
            this.mmConnection.close();
            this.mmConnection = null;
        }

        /**
         * 同步读取
         * @param command
         * @return
         */
        public byte[] sendPcrCommandSync(PcrCommand command) {
            toByteString(command);
            ArrayList<Byte> bytes = command.getCommandList();
            byte[] data = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                data[i] = bytes.get(i).byteValue();
            }
            return sendSync(bytes);
        }

        /**
         * 异步读取
         * @param command
         * @return
         */
        public int sendPcrCommand(PcrCommand command) {
            int err = 0;
            try {
                mSync = false;
                toByteString(command);
                ArrayList<Byte> bytes = command.getCommandList();
                byte[] data = new byte[bytes.size()];
                for (int i = 0; i < bytes.size(); i++) {
                    data[i] = bytes.get(i).byteValue();
                }
                return send(bytes);
            } catch (Exception e) {
                e.printStackTrace();
                err = -1;
            }
            return err;

        }
        private int send(ArrayList<Byte> command) {
            if (command != null && !command.isEmpty()) {

                if (mmEndOut == null || mmConnection == null) {
                    return -1;
                }
                byte[] data = new byte[command.size()];
                for (int i = 0; i < command.size(); i++) {
                    data[i] = command.get(i).byteValue();
                }

                //超时时间需要设置的长一点，不然很可能打印卡住，返回-1。
                int ret = mmConnection.bulkTransfer(mmEndOut, data, data.length, 5000);
                if (ret >= 0) {
                    return 0;
                }

                return -1;
            } else {
                return -1;
            }

        }

        private byte[] sendSync(ArrayList<Byte> command) {
            if (command != null && !command.isEmpty()) {

                if (mmEndOut == null || mmConnection == null) {
                    return null;
                }
                byte[] data = new byte[command.size()];
                for (int i = 0; i < command.size(); i++) {
                    data[i] = command.get(i).byteValue();
                }
                mSync = true;
                //超时时间需要设置的长一点，不然很可能打印卡住，返回-1。
                int ret = mmConnection.bulkTransfer(mmEndOut, data, data.length, 5000);
                if (ret >= 0) {
                    //等待读取线程读取
                    ThreadUtil.sleep(100);

                    byte[] buffer = new byte[64];

                        int count = 0;
                        while ( mSyncReceivedBytes == null && count <= 3) {
                            ThreadUtil.sleep(100);
                            count++;
                        }
                        if (mSyncReceivedBytes != null) {
                            buffer =mSyncReceivedBytes;
                        }

                    //  int bytes = this.mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer, 64, 5000);


                    StringBuilder hex = new StringBuilder(buffer.length * 2);
                    for (int i = 0; i < 64; i++) {
                        byte b = buffer[i];
                        if ((b & 0xFF) < 0x10) hex.append("0");
                        hex.append(Integer.toHexString(b & 0xFF));
                    }
                    //System.out.println("同步接收到:" + hex.toString().toLowerCase());
                    AnitoaLogUtil.writeFileLog("同步接收到：" + hex.toString().toLowerCase());


                    return buffer;
                }

                return null;
            } else {
                return null;
            }
        }
    }
    private void toByteString(PcrCommand cmd) {
        ArrayList<Byte> bytes = cmd.getCommandList();
        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            data[i] = bytes.get(i).byteValue();
        }
        String hex = ByteUtil.getHexStr(data, data.length);

        AnitoaLogUtil.writeFileLog("发送：" + hex);
    }

    @Override
    public synchronized void stop() {
        Log.d("UsbPortService", "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
    }

    /**
     * 接收用户是否同意连接到usb设备
     */
    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_DEVICE_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (granted) {
                    //获得了usb使用权限
                    connect();

                }
                getUsbPort().mContext.unregisterReceiver(this);
            }
        }
    };
}
