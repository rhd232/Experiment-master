package com.wind.appframework;

import com.jz.experiment.chart.ChartData;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.util.ByteBufferUtil;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.CvtUtil;
import com.wind.base.C;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testCombineString(){
        Map<String,List<String>> mItemData=new LinkedHashMap<>();
        List<String> chip1=new ArrayList<>();
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip1.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        mItemData.put("Chip#1",chip1 );

        List<String> chip2=new ArrayList<>();
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        chip2.add("100 42 183 78 177 145 165 37 158 114 55 173 0");
        mItemData.put("Chip#2",chip2 );

        Set<Map.Entry<String,List<String>>> entries= mItemData.entrySet();
        for (Map.Entry<String,List<String>> entry:entries){
            String key=entry.getKey();
            List<String> value=entry.getValue();
            StringBuilder sBuilder=new StringBuilder();
            sBuilder.append(key)
                    .append("\n");
            for (String item:value){
                sBuilder.append(item)
                        .append("\n");
            }
           System.out.println(sBuilder.toString());
        }
    }

    @Test
    public void testDiv(){
        int mode=59 % 12;
        System.out.println(mode);
        int n= (int) Math.ceil(59/12f);
        System.out.println(n);
    }
    @Test
    public void testByteToString(){
       /* Integer a=null;
        init(a);
        Assert.assertNull(a);//a为null*/

        byte [] cmd=new byte[6];
        cmd[0]= (byte) 0xaa;
        cmd[1]= (byte) 0x13;
        cmd[2]= (byte) 0x2;
        cmd[3]= (byte) 0x1;
        cmd[4]= (byte) 0x17;
        cmd[5]= (byte) 0x17;

        String rev=ByteUtil.getHexStr(cmd,cmd.length);
       /* StringBuilder hex = new StringBuilder(cmd.length * 2);
        for (int i = 0; i < cmd.length; i++) {
            byte b = cmd[i];
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        String rev = hex.toString().toLowerCase();*/
        System.out.println(rev);

        byte [] result= ByteUtil.hexStringToBytes(rev,rev.length()/2);
       /* //string 转回byte
        byte [] result=new byte[cmd.length];
        int start=0;
        for (int i=0;i< cmd.length;i++){
            String frag=rev.substring(start,start+2);
            int s=Integer.parseInt(frag,16);
            byte ret=(byte)s;
            result[i]=ret;
            start+=2;
        }*/
        String revs=ByteUtil.getHexStr(result,result.length);
        //打印出转回的byte
      /*  StringBuilder hexs = new StringBuilder(result.length * 2);
        for (int i = 0; i < result.length; i++) {
            byte b = result[i];
            if ((b & 0xFF) < 0x10) hexs.append("0");
            hexs.append(Integer.toHexString(b & 0xFF));
        }
        String revs = hexs.toString().toLowerCase();*/
        System.out.println(revs);

    }
    private void init(Integer a){
        a=new Integer(2);
    }
    @Test
    public void testLastIndexOf(){
        String rev="aa0083587aa357353781717aa0037573771717";
        List<String> vals=new ArrayList<>();
        splitAndCombine(rev, vals);
       /* String revs[]={rev,"25421717"};

        for (String r:revs) {
            splitAndCombine(r, vals);
        }
*/
       // System.out.println("size:"+vals.size());
    }

    private void splitAndCombine(String rev, List<String> vals){
        int indexOf=rev.indexOf("1717aa");
        if (indexOf>0){
            String part1=rev.substring(0,indexOf+4);
            if (part1.startsWith((C.Value.DATA_PREFIX))){
                if (part1.endsWith(C.Value.DATA_SUFFIX))
                {
                    vals.clear();
                    //part1是一组完整的数据了
                    System.out.println("完整数据:"+part1);
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
                if (sb.toString().lastIndexOf("1717aa")>0){
                    splitAndCombine(sb.toString(),vals);
                }else {
                    System.out.println("完整数据:" + sb.toString());
                }
            }
            String leftPart=rev.substring(indexOf+4);
            splitAndCombine(leftPart,vals);

        }else if (indexOf==0){
            //数据以aa开头
            String leftPart=rev.substring(2);
            indexOf=leftPart.indexOf("1717aa");
            //测试是否还有aa
            if (indexOf>0){
                String part1=rev.substring(0,indexOf+2+4);
                if (part1.endsWith(C.Value.DATA_SUFFIX)){
                    vals.clear();
                    //part1是一组完整的数据了
                    System.out.println("完整数据:"+part1);
                }else {
                    vals.add(part1);
                }
                String part2=rev.substring(indexOf+2+4);
                splitAndCombine(part2,vals);
            }else {
                //没有aa了
                if (rev.endsWith(C.Value.DATA_SUFFIX)){
                    //一组完整的数据
                    vals.clear();
                    //part1是一组完整的数据了
                    System.out.println("完整数据:"+rev);
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
                if (sb.toString().lastIndexOf("1717aa")>0){
                    splitAndCombine(sb.toString(),vals);
                }else {
                    System.out.println("完整数据:" + sb.toString());
                }
            }else {
                vals.add(rev);
            }
        }
    }
    @Test
    public void sub1717(){
        String source="aa00fsi843784fjskhfsgskhfhskghskfjutu381717ksg99948848433e400000000000000000000000";
        int index=source.indexOf("1717");
        source=source.substring(0,index+4);
        System.out.println(source);//aa00fsi843784fjskhfsgskhfhskghskfjutu381717

        float int_time1=1f;
        float factor=0.5f;
        int_time1 = Float.parseFloat(String.format("%.2f",int_time1 * factor));
        System.out.println(int_time1);
    }

    @Test
    public void testJsonArray(){
        String json="[[\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                "]\n" +
                ",[\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                "]\n" +
                "]";
        Map<Integer,List<Double>> chanMap=new LinkedHashMap<>();
        try {
            JSONArray jsonArray=new JSONArray(json);
            for (int i=0;i<jsonArray.length();i++){

                JSONArray subJSONArray=jsonArray.getJSONArray(i);
                for (int j=0;j<subJSONArray.length();i++){
                    JSONArray subSubJSONArray=subJSONArray.getJSONArray(j);
                    List<Double> yVals=new ArrayList<>();
                    for (int k=0;k<subSubJSONArray.length();k++){
                        double y=subSubJSONArray.getDouble(k);
                        yVals.add(y);
                    }
                    chanMap.put(i,yVals);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i=0;i<chanMap.size();i++){
            List<Double> yVals=chanMap.get(i);
            for (double y:yVals){
                System.out.print(y+" ");
            }
            System.out.println();
        }
    }

    @Test
    public void testListMax(){
        double rate = 8e-8;
        System.out.println(rate);
        List<ChartData> cdlist=new ArrayList<>();

        ChartData c=new ChartData();
        c.y=23;
        cdlist.add(c);

        ChartData c2=new ChartData();
        c2.y=24;
        cdlist.add(c2);


        ChartData c1=new ChartData();
        c1.y=22;
        cdlist.add(c1);

        ChartData c3=new ChartData();
        c3.y=21;
        cdlist.add(c3);
        double y_max_value=Collections.max(cdlist, new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {
                return o1.y-o2.y;
            }
        }).y;
        System.out.println(y_max_value);
    }

    @Test
    public void testSplit(){
        String s="151 137 103 105 140 98 100 96 93 97 106 102  0";
        String [] parts=s.replace("  "," ").split(" ");
        System.out.println(parts.length);
        for (String item: parts){
            System.out.println(item);
        }
    }
    @Test
    public void cmd() throws Exception {
        PcrCommand cmd = new PcrCommand();
        int channel[] = {1, 1, 1, 1};
        cmd.step1(channel);
        toByteString(cmd);


        PcrCommand cmd2 = new PcrCommand();
        short t=0;
        cmd2.step2(105f,t);
        toByteString(cmd2);

        PcrCommand cmd3 = new PcrCommand();
        float temp=50;
        short tt=10;
        PcrCommand.TempDuringCombine combine=new PcrCommand.TempDuringCombine(temp,tt);
        List<PcrCommand.TempDuringCombine> combines=new ArrayList<>();
        combines.add(combine);
        cmd3.step3(0,1,1,combines);
        toByteString(cmd3);


        PcrCommand cmd4 = new PcrCommand();
        int cyclingCount=2;
        float preT=95;
        short preD=5;
        PcrCommand.TempDuringCombine predenaturationCombine = new PcrCommand.TempDuringCombine(preT, preD);
        float extendT=50;
        short extD=5;
        PcrCommand.TempDuringCombine extendCombine = new PcrCommand.TempDuringCombine(extendT, extD);
        cmd4.step4(PcrCommand.Control.START, cyclingCount, PcrCommand.CmdMode.NORMAL,
                predenaturationCombine, extendCombine);
        toByteString(cmd4);

        PcrCommand meltingCurveCmd = new PcrCommand();
        float startT=50;
        float endT=55;
        float speed=1;
        meltingCurveCmd.meltingCurve(PcrCommand.Control.START,startT,endT,speed);
        toByteString(meltingCurveCmd);
    }
    private void  toByteString(PcrCommand cmd){
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
        System.out.println(hex.toString().toLowerCase());
    }
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void bytesToHex() {
        PcrCommand command = new PcrCommand();
        int channel[] = {1, 1, 1, 1};
        command.step1(channel);

        ArrayList<Byte> bytes = command.getCommandList();
        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            data[i] = bytes.get(i).byteValue();
        }
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        String str = hex.toString().toLowerCase();
        System.out.println(str);
    }

    @Test
    public void testShortLSB_MSB() {
        short during = 5;
     /*   System.out.println(Float.floatToIntBits(temperature));
        System.out.println();*/
        byte[] tempBytes = ByteBufferUtil.getBytes(during, ByteOrder.LITTLE_ENDIAN);//LITTLE_ENDIAN 0011266  BIG_ENDIAN 6611200
       /* System.out.println("LSB");
        for (int i = 0; i < tempBytes.length; i++) {

            System.out.print(tempBytes[i]);
        }
        System.out.println();*/
        tempBytes = ByteBufferUtil.getBytes(during, ByteOrder.BIG_ENDIAN);//LITTLE_ENDIAN 0011266  BIG_ENDIAN 6611200
        System.out.println("MSB");
        byte [] shortByte=new byte[2];
        shortByte[0]=tempBytes[0];
        shortByte[1]=tempBytes[1];
        StringBuilder hex = new StringBuilder(shortByte.length * 2);
        for (byte b : shortByte) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        System.out.println(hex.toString().toLowerCase());
    }


    @Test
    public void testFloatLSB() {
        float temperature = 17.625f;
     /*   System.out.println(Float.floatToIntBits(temperature));
        System.out.println();*/
        byte[] tempBytes = ByteBufferUtil.getBytes(temperature, ByteOrder.LITTLE_ENDIAN);//LITTLE_ENDIAN 0011266  BIG_ENDIAN 6611200
        StringBuilder hex = new StringBuilder(tempBytes.length * 2);
        for (byte b : tempBytes) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        System.out.println(hex.toString().toLowerCase());
      /*  for (int i = 0; i < tempBytes.length; i++) {
           System.out.print(tempBytes[i]);
        }*/

    }

    @Test
    public void testBitOp() {
        int[] channelOp = {1, 1, 0, 1};
        int channel = channelOp[0] | (channelOp[1] << 1) | (channelOp[2] << 2) | (channelOp[3] << 3);
        //int channel=channelOp[0] << 3| channelOp[1] << 2 | channelOp[2] << 1|channelOp[3];
        System.out.println(Integer.toBinaryString(channel));

        int a = 1;
        int channel1 = a & 0x1;
        int channel2 = a >> 1 & 0x1;
        int channel3 = a >> 2 & 0x1;
        int channel4 = a >> 3 & 0x1;
        System.out.println("channel1:" + channel1);
        System.out.println("channel2:" + channel2);
        System.out.println("channel3:" + channel3);
        System.out.println("channel4:" + channel4);
    }

    @Test
    public void testHex() {
      /*  int hexValue = 24;
        int decimal = Integer.parseInt(hexValue + "", 16);

        System.out.println(decimal);//36
        System.out.println(0x24);//36*/

        String h="a";
        int a=Integer.parseInt(h,16);
        System.out.println(a);
        int picStep=1;
        int steps=3;
        int picAndSteps = picStep << 4 | steps;
        System.out.println(Integer.toBinaryString(picAndSteps));
    }

    @Test
    public void testCmd() {
        PcrCommand cmd = new PcrCommand();
        //  cmd.setChannel(1);
        // String hexChannel=String.format("%02x", 16);
        //System.out.println(hexChannel);
        int pic = 2;
        int n = pic << 4 | 2;
        String bi = Integer.toBinaryString(n);
        System.out.println(bi);


    }

    @Test
    public void testFloat2Bytes() {
        float f = 1;
        byte[] bytes = CvtUtil.getBytes(f);//00-12863
        for (byte b : bytes) {
            System.out.print(b);
        }

        System.out.println();

        bytes = intToBytes2(Float.floatToIntBits(f));
        for (byte b : bytes) {
            System.out.print(b);//63-12800
        }
        System.out.println();
        bytes = cvt(f);
        for (byte b : bytes) {
            System.out.print(b);//00-12863
        }
        System.out.println();
        bytes = ByteUtil.getBytes(f);
        for (byte b : bytes) {
            System.out.print(b);//00-12863
        }
        System.out.println();
        System.out.println((byte) 1);
        bytes = ByteBufferUtil.getBytes(1);
        for (byte b : bytes) {
            System.out.print(b);//00-12863
        }
        System.out.println();
        bytes = ByteBufferUtil.getBytes(1f);
        for (byte b : bytes) {
            System.out.print(b);//00-12863
        }
    }


    /**
     * 将int类型的数据转换为byte数组 原理：将int数据中的四个byte取出，分别存储
     *
     * @param n int数据
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