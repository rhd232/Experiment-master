package com.leon.lfilepickerlibrary.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public class USBBroadCastReceiver extends BroadcastReceiver {

    private UsbListener usbListener;

    public static final String ACTION_USB_PERMISSION = "com.android.wind.USB_PERMISSION";

    private boolean massStorage(UsbDevice device){

        int interfaceCount = device.getInterfaceCount();
        UsbInterface usbInterface = null;
        for (int i = 0; i < interfaceCount; i++) {

            //获取interfaceClass为USB_CLASS_HID的 interface
            int interfaceClass = device.getInterface(i).getInterfaceClass();
            if (interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE) {
                usbInterface = device.getInterface(i);
                break;
            }
        }
        if (usbInterface==null){
            return false;
        }
        return true;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_USB_PERMISSION:
                //接受到自定义广播
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (!massStorage(device)){
                    return;
                }
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    //允许权限申请
                    if (device != null) {
                        //回调
                        if (usbListener != null) {
                            usbListener.getReadUsbPermission(device);
                        }
                    }
                } else {
                    if (usbListener != null) {
                        usbListener.failedReadUsb(device);
                    }
                }
                break;
            case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到存储设备插入广播
                UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (!massStorage(device_add)){
                    return;
                }
                if (device_add != null) {
                    if (usbListener != null) {
                        usbListener.insertUsb(device_add);
                    }
                }
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                //接收到存储设备拔出广播
                UsbDevice device_remove = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (!massStorage(device_remove)){
                    return;
                }
                if (device_remove != null) {
                    if (usbListener != null) {
                        usbListener.removeUsb(device_remove);
                    }
                }
                break;
        }
    }

    public void setUsbListener(UsbListener usbListener) {
        this.usbListener = usbListener;
    }

    /**
     * USB 操作监听
     */
    public interface UsbListener {
        //USB 插入
        void insertUsb(UsbDevice device_add);

        //USB 移除
        void removeUsb(UsbDevice device_remove);

        //获取读取USB权限
        void getReadUsbPermission(UsbDevice usbDevice);

        //读取USB信息失败
        void failedReadUsb(UsbDevice usbDevice);
    }
}
