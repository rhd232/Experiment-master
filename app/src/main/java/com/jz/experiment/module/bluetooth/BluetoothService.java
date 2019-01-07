package com.jz.experiment.module.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    public static final String TAG="BluetoothService";

    //蓝牙串口服务
    public static String  SerialPortServiceClass_UUID = "00001101-0000-1000-8000-00805F9B34FB";

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
      /*  IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        registerReceiver(mReceiver,filter);*/
        return mBinder;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, byte[] buffer) {
        final Intent intent = new Intent(action);
        String str = new String(buffer);
        intent.putExtra(EXTRA_DATA, str);
        sendBroadcast(intent);
    }



    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_UUID.equals(action)) {
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                for (int i=0; uuidExtra!=null&&i<uuidExtra.length; i++) {
                    String uuid = uuidExtra[i].toString();
                    Log.e(TAG,"uuid:"+uuid);
                    if (SDP_AudioSinkServiceClass_UUID.equalsIgnoreCase(uuid)){
                        if (mDevice!=null){
                            ConnectThread connectThread=new ConnectThread(mDevice);
                            connectThread.start();
                        }

                    }
                }
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
    private BluetoothDevice mDevice;

    public void connect(BluetoothDevice device) {
        if (mBluetoothAdapter == null || device == null) {
            return;
        }
        //mDevice=device;
      /*  //首先获取uuid
        ParcelUuid[] uuids=device.getUuids();
        if (uuids==null|| uuids.length==0) {
            boolean flag = device.fetchUuidsWithSdp();
            Log.e(TAG,"connect:"+flag);
        }else {
            for (int i=0; uuids!=null&&i<uuids.length; i++) {
                String uuid = uuids[i].toString();
                Log.e(TAG,"uuid:"+uuid);
                if (SDP_AudioSinkServiceClass_UUID.equalsIgnoreCase(uuid)){
                    ConnectThread connectThread=new ConnectThread(mDevice);
                    connectThread.start();
                }
            }
        }*/


        ConnectThread connectThread=new ConnectThread(device);
        connectThread.start();

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

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
             /*   tmp =(BluetoothSocket) device.getClass()
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
        ConnectedThread connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            //可以通信了
            broadcastUpdate(ACTION_DEVICE_COMMUNICATION_ENABLED);
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    //有数据可读

                    broadcastUpdate(ACTION_DATA_AVAILABLE, buffer);
                  /*  new Handler().obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
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
        //unregisterReceiver(mReceiver);
        return super.onUnbind(intent);
    }
}
