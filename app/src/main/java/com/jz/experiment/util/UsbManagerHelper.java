package com.jz.experiment.util;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.anitoa.Anitoa;
import com.anitoa.util.AnitoaLogUtil;
import com.jz.experiment.module.bluetooth.DeviceRepo;
import com.jz.experiment.module.expe.bean.UsbDeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class UsbManagerHelper {

    public static void connectUsbDevice(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        List<String> printerList = new ArrayList<>();
        int printerCount = 0;//打印机台数
        String[] strDev = new String[usbManager.getDeviceList().size()];
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface usbInterface = device.getInterface(i);
                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
                    printerCount++;
                    printerList.add(device.getDeviceName());
                }
            }
        }
        strDev = new String[printerCount];
        for (int i = 0; i < printerCount; i++) {
            strDev[i] = printerList.get(i);

        }

        if (printerCount == 1) {
            for (UsbDevice device : usbManager.getDeviceList().values()) {
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface usbInterface = device.getInterface(i);
                    int vendorId=device.getVendorId();
                    int productId=device.getProductId();
                   // System.out.println("vendorId:"+vendorId+"   productId:"+productId);
                    AnitoaLogUtil.writeFileLog("vendorId:"+vendorId+"   productId:"+productId);
                    UsbDeviceInfo info = new UsbDeviceInfo();
                    info.setDeviceName(device.getDeviceName());
                    DeviceRepo.getInstance().store(context, info);
                    Anitoa.getInstance(context).getUsbService().connect(device.getDeviceName());
                    break;

                }
            }
        }
    }
}
