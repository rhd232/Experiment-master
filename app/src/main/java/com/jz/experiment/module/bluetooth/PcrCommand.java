package com.jz.experiment.module.bluetooth;

import com.jz.experiment.util.ByteBufferUtil;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class PcrCommand {
    public static final String TAG = "PcrCommand";
    private ArrayList<Byte> commandList = new ArrayList<>();
    public static final int STEP_1_CMD=1;
    public static final int STEP_1_TYPE=36;

    public static final int STEP_2_CMD=16;
    public static final int STEP_2_TYPE=1;

    public static final int STEP_3_OR_4_CMD=19;
    public static final int STEP_3_TYPE=3;

    public static final int STEP_4_TYPE=4;

    public static final int STEP_5_CMD=21;
    public static final int STEP_5_TYPE=1;

    public static final int STEP_6_CMD=2;
    public static final int STEP_6_TYPE1=8;
    public static final int STEP_6_TYPE2=24;
    public static final int STEP_6_TYPE3=40;
    public static final int STEP_6_TYPE4=56;

    public static final int STEP_7_CMD=20;
    public static final int STEP_7_TYPE=21;

    public void reset() {
        commandList.clear();
    }

    public ArrayList<Byte> getCommandList() {
        return commandList;
    }

    /**
     * Length: 	          包含type以及有效数据data部分的长度
     *
     * 设置通道
     *              header command  length  type
     * 十六进制码    0xaa     0x1             0x24
     * 十进制码      170        1             36
     *
     * @param channelOp  数组长度为4  取值0|1   	0 关闭 1开启
     */
    public void step1(int[] channelOp) {

        int header=170;
        int command=1;
        int length=2;
        int type=36;
        //channel 0 在最右边
        int channel=channelOp[0] | (channelOp[1] << 1) | (channelOp[2] << 2) | (channelOp[3] << 3);

        byte[] cmd = new byte[8];
        cmd[0]= (byte)header;
        cmd[1]= (byte)command;
        cmd[2]= (byte)length;
        cmd[3]= (byte)type;

        cmd[4]= (byte)channel;

        cmd[5]=(byte)0;
        for(int i=1; i<5; i++)
            cmd[5] += cmd[i];//求校验和
        if(cmd[5] == (byte) 0x17)
            cmd[5] = 0x18;
        cmd[6] = 0x17;
        cmd[7] = 0x17;

        addCommand(cmd);
    }

    /**
     * sensor #1
     * 为热盖的序号。热盖的温度保持时间为Peltier关闭后延迟关闭时间。（默认为105，不延迟）
     * Sensor 2#为Peltier的。
     *              header command   length    type    data
     *                      0x10              0x01
     *                      16                   1
     *
     *        data字段构成
     *      Sensor index，     4 byte （float）目标温度， 2 byte （word）
     * 		 1 ---LID           LSB在前，MSB在后，         MSB 在前， LSB在后， 单位 S
     * 		 2 --  peltier                               如果0，无限执行，直到收到off命令
     * @param temperature
     * @param during
     */
    public void step2(float temperature, short during) {
        int length=8;
        int header=170;
        int command=16;
        int type=1;
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) header);
        bytes.add((byte) command);
        bytes.add((byte) length);
        bytes.add((byte) type);
        //sensor index
        bytes.add((byte)2);
        byte[] tempBytes = ByteBufferUtil.getBytes(temperature,ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < tempBytes.length; i++) {
            bytes.add(tempBytes[i]);
        }
        byte[] duringBytes = ByteBufferUtil.getBytes(during,ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < duringBytes.length; i++) {
            bytes.add(duringBytes[i]);
        }


        addCommonBytes(bytes);


        byte[] cmd = listToByteArray(bytes);
        addCommand(cmd);
    }

    private void addCommonBytes(List<Byte> bytes) {
        byte checksum=0;
        for (int i=1;i<bytes.size();i++){
            checksum+=bytes.get(i);
        }


        if(checksum == (byte) 0x17)
            checksum = 0x18;

        bytes.add(checksum);
        bytes.add((byte)0x17);
        bytes.add((byte)0x17);
    }


    /**
     * 循环参数设置
     *        command  length    type
     * *      0x13               0x3
     * *       19                 3
     *
     * @param cyclingCount 循环数
     * @param cur_cycling  当前第x循环
     * @param picStep      拍照阶段
     * @param steps        此步骤阶段数
     * @param combineList  每个阶段的温度和持续时间
     */
    public void step3(int picStep, int steps, List<TempDuringCombine> combineList) {
        int length = combineList.size()*(4+2)+2+1;
        int picAndSteps = picStep << 4 | steps;//[7-4]为拍照阶段，[3:0]为区间内阶段数
        //  byte [] cmd=new byte[]{19,length,3,cyclingCount,cur_cycling,picAndSteps};
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) 0xaa);
        bytes.add((byte) 19);
        bytes.add((byte) length);
        bytes.add((byte) 3);

        bytes.add((byte) 0);//固定为0
        bytes.add((byte) 1);//固定为1
        bytes.add((byte) steps);


        for (TempDuringCombine combine : combineList) {
            byte[] tempBytes = ByteBufferUtil.getBytes(combine.getTemp(),ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < tempBytes.length; i++) {
                bytes.add(tempBytes[i]);
            }
            byte[] duringBytes = ByteBufferUtil.getBytes(combine.getDuring(),ByteOrder.BIG_ENDIAN);
            for (int i = 0; i < duringBytes.length; i++) {
                bytes.add(duringBytes[i]);
            }
        }
        addCommonBytes(bytes);
        byte[] cmd = listToByteArray(bytes);
        addCommand(cmd);

    }


    /**
     *      循环启动/停止
     *               command  type
     *              0x13      0x4
     *               19        4
     * @param control                0stop,1start
     * @param cyclingCount           循环数
     * @param cmdMode
     * @param predenaturationCombine 预变性目标温度，保持时间
     * @param extendCombine          延伸目标温度，保持时间
     *                               <p>
     *                               cfg [7-4]cmd_mode  [3-0]initial_mode
     *                               0--normal mode (目前alpha的模式，一个预变性)		"13 04 命令格式就是目前的格式：
     *                               预变性温度 ，预变性时间，hold温度，hold保持时间"
     *                               1--mode  1 （两个预变性阶段）		"命令格式会增加一个预变性阶段：
     *                               预变性1温度 ，预变性1时间， 预变性2温度，预变性2时间，hold温度，hold保持时间"
     */
    public void step4(Control control, int cyclingCount, CmdMode cmdMode, TempDuringCombine predenaturationCombine,
                      TempDuringCombine extendCombine) {
        int length=3+(4+2)*2+1;
        int cfg = cmdMode.getValue() << 4;//initial_mode值为0，所以不用设置
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte)0xaa);
        bytes.add((byte)19);
        bytes.add((byte)length);
        bytes.add((byte)4);

        bytes.add((byte)control.getValue());
        bytes.add((byte)cyclingCount);
        bytes.add((byte)cfg);

        //设置预变性目标温度时间
        byte[] tempBytes = ByteBufferUtil.getBytes(predenaturationCombine.getTemp(),ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < tempBytes.length; i++) {
            bytes.add(tempBytes[i]);
        }
        byte[] duringBytes = ByteBufferUtil.getBytes(predenaturationCombine.getDuring(),ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < duringBytes.length; i++) {
            bytes.add(duringBytes[i]);
        }
        //设置延伸目标温度时间
        byte[] exTempBytes = ByteBufferUtil.getBytes(extendCombine.getTemp(),ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < exTempBytes.length; i++) {
            bytes.add(exTempBytes[i]);
        }
        byte[] exDuringBytes = ByteBufferUtil.getBytes(extendCombine.getDuring(),ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < exDuringBytes.length; i++) {
            bytes.add(exDuringBytes[i]);
        }
        addCommonBytes(bytes);
        byte[] cmd = listToByteArray(bytes);
        addCommand(cmd);
    }


    /**
     * 查询图像板是否准备好
     *  header    command   length    type   data
     *   0xaa       0x15       0x2      0x01    0x00
     *              21          2         1      0
     */
    public void step5(){
        int length=2;
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) 0xaa);
        bytes.add((byte) 21);
        bytes.add((byte) length);
        bytes.add((byte) 1);
        bytes.add((byte) 0);//data not care

        addCommonBytes(bytes);
        byte[] cmd = listToByteArray(bytes);
        addCommand(cmd);
    }

    /**
     * 获取图像板数据  ，图像默认格式是 12*12
     *        command  length  type  data
     *         0x2             0x8    0xff
     *                         0x18
     *                         0x28
     *                         0x38
     */
    public void step6(PCR_IMAGE pcr_image){
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) 0xaa);
        bytes.add((byte) 0x2);
        bytes.add((byte) 1);
        bytes.add(pcr_image.getValue());
        bytes.add((byte)0x0);
        addCommonBytes(bytes);

        addCommand(listToByteArray(bytes));
    }

    /**
     * 询问是否继续polling图像板
     *       command  length  type  data
     *        0x14             0x15
     *        20                21
     */
    public void step7(){
        int length=0;
        byte[] cmd=new byte[]{20,(byte) length,21,0};
        addCommand(cmd);
    }

    /**
     * 溶解曲线开启/停止
     * command	length	type	control byte	data
     * 0x13		         0xB	   1 byte	     float	float	float
     * 	19		          11      "0 -- stop
     *                            1 -- start"	起始温度	结束温度	速度 （暂时无效）
     * @param control    1开启，0结束
     * @param startTemp
     * @param endTemp
     */
    public void meltingCurve(Control control,float startTemp,float endTemp,float speed){
        List<Byte> bytes = new ArrayList<>();
        int length=4+4+4;
        bytes.add((byte)19);
        bytes.add((byte)length);
        bytes.add((byte)11);

        bytes.add((byte)control.getValue());

        byte[] startTempBytes = ByteBufferUtil.getBytes(startTemp);
        for (int i = 0; i < startTempBytes.length; i++) {
            bytes.add(startTempBytes[i]);
        }

        byte[] endTempBytes = ByteBufferUtil.getBytes(endTemp);
        for (int i = 0; i < endTempBytes.length; i++) {
            bytes.add(endTempBytes[i]);
        }
        byte[] speedBytes = ByteBufferUtil.getBytes(speed);
        for (int i = 0; i < speedBytes.length; i++) {
            bytes.add(speedBytes[i]);
        }
        byte[] cmd = listToByteArray(bytes);
        addCommand(cmd);
    }
    private byte[] listToByteArray(List<Byte> bytes){
        byte[] cmd = new byte[bytes.size()];
        for (int i=0;i<bytes.size();i++) {
            cmd[i]=bytes.get(i);
        }
        return cmd;
    }
    private void addCommand(byte[] command) {
        for (int i = 0; i < command.length; i++) {
            commandList.add(Byte.valueOf(command[i]));
        }
    }

    public static class TempDuringCombine {
        private float temp;
        private short during;

        public TempDuringCombine(float temp, short during) {
            this.temp = temp;
            this.during = during;
        }

        public float getTemp() {
            return temp;
        }

        public short getDuring() {
            return during;
        }
    }

    /**
     * 循环启动/停止命令
     */
    public static enum Control {
        START(1), STOP(0);
        private int value;

        Control(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 连续命令模式： 13 03 / 13 09 命令会有一个buffer。
     * 上位机可在循环结束前的任何时刻，下传另一组循环参数，而不会打断当前循环。
     * <p>
     * 下位机在当前循环结束前，收到新的参数，会保存起来。等到当前循环结束时，立即运行新的循环参数（中间不会重新运行预变性），
     * 无需上位机发送开始命令。
     * <p>
     * 如果在结束前没有新的参数下发，则正常结束循环。
     */
    public static enum CmdMode {
        NORMAL(0), CONTINU(1);
        private int value;

        CmdMode(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) value;
        }
    }

    /**
     * 24*24格式
     *  0x08    8
     *  0x18    24
     *  0x28    40
     *  0x38    56
     *
     *  12*12格式
     *        0x2    2
     *        0x12   18
     *        0x22    34
     *        0x32    50
     */
    public static enum PCR_IMAGE{
        PCR_24_CHANNEL_3(56),PCR_24_CHANNEL_2(40),PCR_24_CHANNEL_1(24) ,PCR_24_CHANNEL_0(8),
        PCR_12_CHANNEL_3(50), PCR_12_CHANNEL_2(34), PCR_12_CHANNEL_1(18),  PCR_12_CHANNEL_0(2);
        private int value;

        PCR_IMAGE(int value) {
            this.value = value;
        }


        public byte getValue() {
            return (byte) value;
        }
    }


    public static enum IMAGE_MODE{
        IMAGE_12(12),IMAGE_24(24);
        private int size;
        IMAGE_MODE(int size){
            this.size=size;
        }

        public int getSize() {
            return size;
        }
    }
}
