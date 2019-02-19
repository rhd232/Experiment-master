package com.jz.experiment.module.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.util.DataFileUtil;
import com.wind.base.C;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends CommunicationService {
    public static final String TAG = "BluetoothService";

    //蓝牙串口服务
    public static String SerialPortServiceClass_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static String SDP_AVRemoteControlServiceClass_UUID = "0000110E-0000-1000-8000-00805F9B34FB";
    public static String SDP_AudioSinkServiceClass_UUID = "0000110B-0000-1000-8000-00805F9B34FB";


    /**
     * 设备连接成功
     */
    public final static String ACTION_DEVICE_CONNECTED =
            "com.wind.bluetooth.ACTION_DEVICE_CONNECTED";
    /**
     * 设备连接失败
     */
    public final static String ACTION_DEVICE_CONNECT_FAILED =
            "com.wind.bluetooth.ACTION_DEVICE_CONNECT_FAILED";
    /**
     * 设备间可进行通信
     */
    public final static String ACTION_DEVICE_COMMUNICATION_ENABLED =
            "com.wind.bluetooth.ACTION_DEVICE_COMMUNICATION_ENABLED";

    /**
     * 有可读数据到来
     */
    public final static String ACTION_DATA_AVAILABLE =
            "com.wind.bluetooth.ACTION_DATA_AVAILABLE";

    public final static String EXTRA_DATA =
            "com.wind.bluetooth.EXTRA_DATA";

    public static final String MY_UUID = "";
    BluetoothAdapter mBluetoothAdapter;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder mBinder = new BluetoothService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initialize();
        registerACLReceiver();
        return mBinder;
    }

    private void registerACLReceiver() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        //蓝牙连接成功
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        //蓝牙请求断开连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        //蓝牙已断开连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, Data data) {
        try {
            final Intent intent = new Intent(action);
            //  String str = new String(buffer,"ISO-8859-1");
            intent.putExtra(EXTRA_DATA, data);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                if (mConnectedThread != null) {
                    mConnectedThread.setRun(false);
                }
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
                mConnectedThread = null;
                //发送蓝牙已断开连接通知
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                EventBus.getDefault().post(new BluetoothDisConnectedEvent(device.getName()));

            }
        }
    };

    public boolean initialize() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                //Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        return true;
    }


    /**
     * 连接线程
     */
    private ConnectThread mConnectThread;
    /**
     * 已连接线程
     */
    private ConnectedThread mConnectedThread;

    public void connect(BluetoothDevice device) {
        if (mBluetoothAdapter == null || device == null) {
            broadcastUpdate(ACTION_DEVICE_CONNECT_FAILED);
            return;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

    }

    /**
     * 获取已经连接的设备
     *
     * @return
     */
    public Device getConnectedDevice() {
        if (isConnected()) {

            BluetoothDevice bluetoothDevice = mConnectThread.getConnectedDevice();
            Device device = new Device(bluetoothDevice.getName(), bluetoothDevice.getAddress());
            return device;
        }
        return null;

    }

    public BluetoothDevice getConnectedBluetoothDevice() {
        if (isConnected()) {
            BluetoothDevice bluetoothDevice = mConnectThread.getConnectedDevice();
            return bluetoothDevice;
        }
        return null;
    }

    /**
     * 设备是否已连接
     *
     * @return
     */
    public boolean isConnected() {
        return mConnectThread == null ? false : mConnectThread.isConnected();
    }

    public void cancel() {
        if (mConnectedThread != null) {
            mConnectedThread.setRun(false);
        }
        if (mConnectThread != null && mConnectThread.isConnected()) {
            mConnectThread.cancel();
        }

    }

    private boolean mSync;

    public byte[] sendPcrCommandSync(PcrCommand command) {
        mSync = true;
        sendPcrCommand(command);
        try {
            //等待设备回复读取掉
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void toByteString(PcrCommand cmd) {
        ArrayList<Byte> bytes = cmd.getCommandList();
        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            data[i] = bytes.get(i).byteValue();
        }

        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        File file = DataFileUtil.getLogFile();
        appendToFile("发送：" + hex.toString().toLowerCase(), file);
        // System.out.println("发送：" + hex.toString().toLowerCase());
    }

    @Override
    public int sendPcrCommand(PcrCommand command) {
        mSync = false;
        int err = 0;
        if (isConnected()) {
            try {
                ArrayList<Byte> bytes = command.getCommandList();
                byte[] data = new byte[bytes.size()];
                for (int i = 0; i < bytes.size(); i++) {
                    data[i] = bytes.get(i).byteValue();
                }
                toByteString(command);
                mConnectedThread.write(data);
            } catch (Exception e) {
                e.printStackTrace();
                err = -1;
            }

        }
        return err;
    }


    private class ConnectThread extends Thread {
        /**
         * 已经连接的设备
         */
        private final BluetoothSocket mmSocket;
        /**
         * 已经连接的设备
         */
        private final BluetoothDevice mmDevice;

        public BluetoothDevice getConnectedDevice() {
            return mmDevice;
        }

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SerialPortServiceClass_UUID));
                //安卓系统4.2以后的蓝牙通信端口为 1 ，但是默认为 -1，所以只能通过反射修改，才能成功
            /*    tmp =(BluetoothSocket) device.getClass()
                        .getDeclaredMethod("createRfcommSocket",new Class[]{int.class})
                        .invoke(device,1);*/
            } catch (Exception e) {

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                //连接成功
                broadcastUpdate(ACTION_DEVICE_CONNECTED);
            } catch (IOException connectException) {
                //连接失败
                broadcastUpdate(ACTION_DEVICE_CONNECT_FAILED);
                // Unable to connect; close the socket and get out


                try {
                    mmSocket.close();

                } catch (IOException closeException) {

                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }


        /**
         * 设备是否已连接
         *
         * @return
         */
        public boolean isConnected() {
            return mmSocket == null ? false : mmSocket.isConnected();
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mRun;
        private List<Byte> mReceivedBytes;
        private List<String> mReceivedStr;

        public void setRun(boolean run) {
            this.mRun = run;
        }

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mRun = true;
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mReceivedBytes = new ArrayList<>();
            //可以通信了
            broadcastUpdate(ACTION_DEVICE_COMMUNICATION_ENABLED);
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (mRun) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity

                    StringBuilder hex = new StringBuilder(bytes * 2);
                    for (int i = 0; i < bytes; i++) {
                        byte b = buffer[i];
                        if ((b & 0xFF) < 0x10) hex.append("0");
                        hex.append(Integer.toHexString(b & 0xFF));
                    }
                    String rev = hex.toString().toLowerCase();

                    splitAndCombine(rev,mReceivedStr);

                } catch (IOException e) {
                    break;
                }
            }
        }

        private void broadcast(byte[] buffer, int bytes,String content) {
            if (!mSync) {
                DataFileUtil.writeFileLog("接收到：" + content);
                //有数据可读
                Data data = new Data(buffer, bytes);
                broadcastUpdate(ACTION_DATA_AVAILABLE, data);
            } else {
                DataFileUtil.writeFileLog("同步接收到：" + content);
            }
        }
        private void splitAndCombine(String rev, List<String> vals){
            int indexOf=rev.indexOf(C.Value.DATA_PREFIX);
            if (indexOf>0){
                String part1=rev.substring(0,indexOf);
                if (part1.startsWith((C.Value.DATA_PREFIX))){
                    if (part1.endsWith(C.Value.DATA_SUFFIX))
                    {
                        vals.clear();
                        //part1是一组完整的数据了
                        //System.out.println("完整数据:"+part1);
                        byte[] buffer=part1.getBytes();
                        broadcast(buffer,buffer.length,part1);
                    }else {
                        vals.add(part1);
                    }
                }else if (part1.endsWith(C.Value.DATA_SUFFIX)){
                    vals.add(part1);
                    StringBuilder sb=new StringBuilder();
                    for (String v:vals){
                        sb.append(v);
                    }
                    vals.clear();
                    if (sb.toString().lastIndexOf("aa")>0){
                        splitAndCombine(sb.toString(),vals);
                    }else {
                        //System.out.println("完整数据:" + sb.toString());
                        byte[] buffer=sb.toString().getBytes();
                        broadcast(buffer,buffer.length,sb.toString());
                    }
                }
                String leftPart=rev.substring(indexOf);
                splitAndCombine(leftPart,vals);

            }else if (indexOf==0){
                //数据以aa开头
                String leftPart=rev.substring(2);
                indexOf=leftPart.indexOf(C.Value.DATA_PREFIX);
                //测试是否还有aa
                if (indexOf>0){
                    String part1=rev.substring(0,indexOf+2);
                    if (part1.endsWith(C.Value.DATA_SUFFIX)){
                        vals.clear();
                        //part1是一组完整的数据了
                        byte[] buffer=part1.getBytes();
                        broadcast(buffer,buffer.length,part1);
                       // System.out.println("完整数据:"+part1);
                    }else {
                        vals.add(part1);
                    }
                    String part2=rev.substring(indexOf+2);
                    splitAndCombine(part2,vals);
                }else {
                    //没有aa了
                    if (rev.endsWith(C.Value.DATA_SUFFIX)){
                        //一组完整的数据
                        vals.clear();
                        //part1是一组完整的数据了
                        byte[] buffer=rev.getBytes();
                        broadcast(buffer,buffer.length,rev);
                        //System.out.println("完整数据:"+rev);
                    }else {
                        //以aa开头但是不是以1717结尾，数据还不完整
                        vals.add(rev);
                    }
                }

            }else {
                //不存在aa

                if (rev.endsWith(C.Value.DATA_SUFFIX)){
                    vals.add(rev);
                    StringBuilder sb=new StringBuilder();
                    for (String v:vals){
                        sb.append(v);
                    }
                    vals.clear();
                    if (sb.toString().lastIndexOf("aa")>0){
                        splitAndCombine(sb.toString(),vals);
                    }else {
                        //System.out.println("完整数据:" + sb.toString());
                        byte[] buffer=sb.toString().getBytes();
                        broadcast(buffer,buffer.length,sb.toString());
                    }
                }else {
                    vals.add(rev);
                }
            }
        }
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) throws IOException {
            mmOutStream.write(bytes);

        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(mReceiver);
        return super.onUnbind(intent);
    }

    private void appendToFile(String txt, File file) {
     /*   String fileName=DateUtil.get(mHistoryExperiment.getMillitime(),"yyyy_MM_dd_hh_mm_ss")+".txt";
        String filePath= C.Value.IMAGE_DATA+fileName;*/

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!file.exists()) {
                boolean hasFile = file.createNewFile();
               /* if (hasFile) {
                    System.out.println("file not exists, create new file");
                }*/
                fos = new FileOutputStream(file);
            } else {
                // System.out.println("file exists");
                fos = new FileOutputStream(file, true);
            }

            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(txt); //写入内容
            osw.write("\r\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        } finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
