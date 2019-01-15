package com.jz.experiment.util;

import java.nio.ByteBuffer;

public class ByteBufferUtil {

    public static byte[] getBytes(int data)
    {
        return ByteBuffer.allocate(4).putInt(data).array();

    }
    public static byte[] getBytes(short data)
    {
        return ByteBuffer.allocate(4).putShort(data).array();

    }

    public static byte[] getBytes(float data)
    {
        return ByteBuffer.allocate(4).putFloat(data).array();
    }
}
