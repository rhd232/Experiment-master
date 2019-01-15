package com.wind.appframework;

import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.util.ByteBufferUtil;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.CvtUtil;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void testHex(){
        int hexValue=24;
        int decimal=Integer.parseInt(hexValue+"", 16);

        System.out.println(decimal);//36
        System.out.println(0x24);//36
    }
    @Test
    public void testCmd(){
        PcrCommand cmd=new PcrCommand();
      //  cmd.setChannel(1);
       // String hexChannel=String.format("%02x", 16);
        //System.out.println(hexChannel);
        int pic=2;
        int n= pic<<4 | 2;
        String bi=Integer.toBinaryString(n);
        System.out.println(bi);


    }
    @Test
    public void testFloat2Bytes(){
        float f=1;
        byte[] bytes=CvtUtil.getBytes(f);//00-12863
        for (byte b:bytes) {
            System.out.print(b);
        }

        System.out.println();

        bytes=intToBytes2(Float.floatToIntBits(f));
        for (byte b:bytes) {
            System.out.print(b);//63-12800
        }
        System.out.println();
        bytes=cvt(f);
        for (byte b:bytes) {
            System.out.print(b);//00-12863
        }
        System.out.println();
        bytes=ByteUtil.getBytes(f);
        for (byte b:bytes) {
            System.out.print(b);//00-12863
        }
        System.out.println();
        System.out.println((byte)1);
        bytes=ByteBufferUtil.getBytes(1);
        for (byte b:bytes) {
            System.out.print(b);//00-12863
        }
        System.out.println();
        bytes=ByteBufferUtil.getBytes(1f);
        for (byte b:bytes) {
            System.out.print(b);//00-12863
        }
    }


    /**
     * 将int类型的数据转换为byte数组 原理：将int数据中的四个byte取出，分别存储
     *
     * @param n  int数据
     * @return 生成的byte数组
     */
    public static byte[] intToBytes2(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        return b;
    }
    public static byte[] cvt(float n) {


        ByteBuffer bbuf = ByteBuffer.allocate(4);
        bbuf.putFloat(n);
        bbuf.position(0);
        bbuf.order(ByteOrder.nativeOrder());
        byte[] bBuffer = bbuf.array();
        ArrayList<Byte> al = new ArrayList<Byte>();
        for (int i = bBuffer.length - 1; i >= 0; i--) {
            al.add(bBuffer[i]);
        }

        byte[] buffer = new byte[al.size()];
        for (int i = 0; i <= buffer.length - 1; i++) {
            buffer[i] = al.get(i);
        }
        return buffer;

    }
}