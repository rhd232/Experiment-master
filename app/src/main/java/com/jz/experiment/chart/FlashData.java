package com.jz.experiment.chart;

import java.util.ArrayList;
import java.util.List;

public class FlashData {
    public static List<Integer>[][] row_index = new ArrayList[4][16];
    public static List<Integer>[][] col_index = new ArrayList[4][16];
    public static double[][] chan1_kb = new double[12][ 6];
    public static double[][] chan1_fpn = new double[2][12];
    public static double[] chan1_tempcal = new double[12];
    public static int chan1_rampgen = 0;
    public static int[] chan1_auto_v20 = new int[2];
    public static int chan1_auto_v15 = 0;

    public static double[][] chan2_kb = new double[12][ 6];
    public static double[][] chan2_fpn = new double[2][ 12];
    public static double[] chan2_tempcal = new double[12];
    public static int chan2_rampgen = 0;
    public static int[] chan2_auto_v20 = new int[2];
    public static int chan2_auto_v15 = 0;

    public static double[][] chan3_kb = new double[12][ 6];
    public static double[][] chan3_fpn = new double[2][12];
    public static double[] chan3_tempcal = new double[12];
    public static int chan3_rampgen = 0;
    public static int[] chan3_auto_v20 = new int[2];
    public static int chan3_auto_v15 = 0;

    public static double[][] chan4_kb = new double[12][6];
    public static double[][] chan4_fpn = new double[2][12];
    public static double[] chan4_tempcal = new double[12];
    public static int chan4_rampgen = 0;
    public static int[] chan4_auto_v20 = new int[2];
    public static int chan4_auto_v15 = 0;

    public static int[][][] kbi = new int[4][ 12][ 6];
    public static int[][][] fpni = new int[4][ 2][12];
    public static int[] rampgen = new int[4];
    public static int[] range = new int[4];
    public static int[][] auto_v20 = new int[4][2];
    public static int[] auto_v15 = new int[4];
    /**
     * 标识处理数据是否用下位机的trim数据
     */
    public static boolean flash_loaded = false;

    /**
     * 下位机通道数
     */
    public static int NUM_CHANNELS;
    /**
     * 下位机反应孔数
     */
    public static int NUM_WELLS;

    /**
     * 下位机trim数据 字符串形式，需要写到实验数据文件中的开头部位
     */
    public static String DATA_DEVICE_TRIM;

    /**
     * 当连接到下位机后需要读取下位机的trim数据。断开连接后需要置为false,需要重新进行读取。
     */
    public static boolean flash_inited=false;

}
