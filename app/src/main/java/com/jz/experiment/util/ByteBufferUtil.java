package com.jz.experiment.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferUtil {

    public static byte[] getBytes(int data)
    {
        return ByteBuffer.allocate(4).putInt(data).array();

    }
    public static byte[] getBytes(short data)
    {
        return getBytes(data,ByteOrder.nativeOrder());

    }
    public static byte[] getBytes(short data,ByteOrder order)
    {
        return ByteBuffer.allocate(4).order(order).putShort(data).array();

    }

    public static byte[] getBytes(float data)
    {
        return getBytes(data,ByteOrder.nativeOrder());
    }
    public static byte[] getBytes(float data,ByteOrder order)
    {
        return ByteBuffer.allocate(4).order(order).putFloat(data).array();
    }
}
