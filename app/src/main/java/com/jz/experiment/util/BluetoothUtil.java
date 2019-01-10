package com.jz.experiment.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothUtil {
    /**
     * 获取已经连接的蓝牙设备
     * @param adapter
     * @return
     */
    public static List<BluetoothDevice> getConnectedDevice(BluetoothAdapter adapter) {
        List<BluetoothDevice> connectedDeviceList=new ArrayList<>();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i("BLUETOOTH", "BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i("BLUETOOTH", "devices:" + devices.size());

                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        Log.i("BLUETOOTH", "connected:" + device.getName());
                        connectedDeviceList.add(device);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedDeviceList;
    }
}
