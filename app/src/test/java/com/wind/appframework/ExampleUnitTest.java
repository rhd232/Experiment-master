package com.wind.appframework;

import com.jz.experiment.chart.ChartData;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.util.ByteBufferUtil;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.CvtUtil;
import com.jz.experiment.util.TrimReader;
import com.wind.base.C;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    public void testTransfer() {



        try {
            InputStream ips;
            ips=ips();
            TrimReader.getInstance().ReadTrimFile(ips);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //CHIP#1
      //  String r = "aa00021c0201001f3621f61cca1d3d235225eb1fc81e4a208f1a53227326fd7ef31717";
        //CHIP#2
        String r = "aa00021c12013d10d20e2b12fa0e5c151310321014111a122112af0e1c10d07c321717";
        byte [] bytes=ByteUtil.hexStringToBytes(r,r.length()/2);

        String ret_1=transferImageData(2, 1, bytes);
        System.out.println(ret_1);


     /*   String ret_2=transferImageData(2, 1, bytes);
        System.out.println(ret_2);
        String ret_3=transferImageData(3, 1, bytes);
        System.out.println(ret_3);
        String ret_4=transferImageData(4, 1, bytes);
        System.out.println(ret_4);*/

    }

    public ByteArrayInputStream ips(){

       /* String s="DEF zd014 {\n" +
                "\n" +
                "Version {\n" +
                "\n" +
                "0x2\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Kb {\n" +
                "\n" +
                "0.036, -20.242, 0.072, -20.921, 2.421, -2.117,\n" +
                "\n" +
                "-0.006, 19.375, 0.055, 32.044, 2.753, -8.620,\n" +
                "\n" +
                "0.201, -37.231, 0.127, -31.317, 1.726, 4.155,\n" +
                "\n" +
                "0.131, 16.143, 0.190, 3.755, 2.662, 1.634,\n" +
                "\n" +
                "-0.165, 27.961, -0.127, 7.797, 2.660, -4.602,\n" +
                "\n" +
                "-0.019, 29.335, 0.041, 17.800, 2.574, -10.104,\n" +
                "\n" +
                "-0.240, 32.430, -0.200, 51.952, 2.194, -5.983,\n" +
                "\n" +
                "-0.030, 43.570, -0.040, 55.110, 2.416, 1.846,\n" +
                "\n" +
                "0.212, -39.385, 0.251, -56.470, 1.767, -1.320,\n" +
                "\n" +
                "-0.409, 57.102, -0.433, 46.092, 3.335, -4.709,\n" +
                "\n" +
                "-0.167, -17.732, -0.191, -13.024, 2.520, -4.909,\n" +
                "\n" +
                "-0.280, 20.104, -0.333, 38.764, 2.667, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_lg {\n" +
                "\n" +
                "339.028, 389.056, 301.778, 302.083, 403.750, 438.778, 356.944, 338.278, 366.278, 292.333, 393.417, 474.694\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_hg {\n" +
                "\n" +
                "356.250, 406.528, 319.556, 319.833, 420.639, 454.361, 373.611, 355.833, 383.194, 298.528, 410.528, 492.806\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_lg {\n" +
                "\n" +
                "0x8\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_hg {\n" +
                "\n" +
                "0xa\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Rampgen {\n" +
                "\n" +
                "0x8b\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Range {\n" +
                "\n" +
                "0xf\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV15 {\n" +
                "\n" +
                "0xd\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Temp_calib {\n" +
                "\n" +
                "29.500, -61.936, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "DEF zd050 {\n" +
                "\n" +
                "Version {\n" +
                "\n" +
                "0x2\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Kb {\n" +
                "\n" +
                "0.086, -47.044, 0.138, -71.580, 2.106, 3.855,\n" +
                "\n" +
                "0.029, 48.414, 0.087, 51.443, 4.146, 0.000,\n" +
                "\n" +
                "0.050, -21.930, 0.118, -41.877, 2.197, 5.709,\n" +
                "\n" +
                "-0.043, -35.598, 0.016, -52.576, 2.348, 5.689,\n" +
                "\n" +
                "-0.168, 9.137, -0.102, -2.038, 3.176, 0.000,\n" +
                "\n" +
                "0.098, 72.743, 0.142, 59.299, 2.670, -3.261,\n" +
                "\n" +
                "-0.221, 29.581, -0.247, 25.843, 3.155, 1.802,\n" +
                "\n" +
                "-0.331, 88.556, -0.240, 93.698, 2.212, -3.508,\n" +
                "\n" +
                "0.026, 7.936, 0.077, 9.921, 2.989, 0.000,\n" +
                "\n" +
                "-0.182, 73.027, -0.180, 68.798, 3.558, -6.550,\n" +
                "\n" +
                "0.020, -19.281, 0.129, -48.078, 2.729, -1.191,\n" +
                "\n" +
                "-0.107, 70.255, -0.015, 48.821, 2.988, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_lg {\n" +
                "\n" +
                "275.556, 466.472, 369.056, 267.833, 457.222, 421.444, 346.417, 368.250, 455.083, 420.472, 402.750, 425.833\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_hg {\n" +
                "\n" +
                "293.444, 485.972, 386.417, 288.389, 476.194, 440.500, 363.917, 386.611, 473.806, 439.194, 421.056, 445.111\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_lg {\n" +
                "\n" +
                "0x8\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_hg {\n" +
                "\n" +
                "0xa\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Rampgen {\n" +
                "\n" +
                "0x92\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Range {\n" +
                "\n" +
                "0xf\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV15 {\n" +
                "\n" +
                "0xd\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Temp_calib {\n" +
                "\n" +
                "29.500, -52.703, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "\n" +
                "DEF zd014 {\n" +
                "\n" +
                "Version {\n" +
                "\n" +
                "0x2\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Kb {\n" +
                "\n" +
                "0.036, -20.242, 0.072, -20.921, 2.421, -2.117,\n" +
                "\n" +
                "-0.006, 19.375, 0.055, 32.044, 2.753, -8.620,\n" +
                "\n" +
                "0.201, -37.231, 0.127, -31.317, 1.726, 4.155,\n" +
                "\n" +
                "0.131, 16.143, 0.190, 3.755, 2.662, 1.634,\n" +
                "\n" +
                "-0.165, 27.961, -0.127, 7.797, 2.660, -4.602,\n" +
                "\n" +
                "-0.019, 29.335, 0.041, 17.800, 2.574, -10.104,\n" +
                "\n" +
                "-0.240, 32.430, -0.200, 51.952, 2.194, -5.983,\n" +
                "\n" +
                "-0.030, 43.570, -0.040, 55.110, 2.416, 1.846,\n" +
                "\n" +
                "0.212, -39.385, 0.251, -56.470, 1.767, -1.320,\n" +
                "\n" +
                "-0.409, 57.102, -0.433, 46.092, 3.335, -4.709,\n" +
                "\n" +
                "-0.167, -17.732, -0.191, -13.024, 2.520, -4.909,\n" +
                "\n" +
                "-0.280, 20.104, -0.333, 38.764, 2.667, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_lg {\n" +
                "\n" +
                "339.028, 389.056, 301.778, 302.083, 403.750, 438.778, 356.944, 338.278, 366.278, 292.333, 393.417, 474.694\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_hg {\n" +
                "\n" +
                "356.250, 406.528, 319.556, 319.833, 420.639, 454.361, 373.611, 355.833, 383.194, 298.528, 410.528, 492.806\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_lg {\n" +
                "\n" +
                "0x8\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_hg {\n" +
                "\n" +
                "0xa\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Rampgen {\n" +
                "\n" +
                "0x8b\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Range {\n" +
                "\n" +
                "0xf\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV15 {\n" +
                "\n" +
                "0xd\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Temp_calib {\n" +
                "\n" +
                "29.500, -61.936, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "\n" +
                "DEF zd014 {\n" +
                "\n" +
                "Version {\n" +
                "\n" +
                "0x2\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Kb {\n" +
                "\n" +
                "0.036, -20.242, 0.072, -20.921, 2.421, -2.117,\n" +
                "\n" +
                "-0.006, 19.375, 0.055, 32.044, 2.753, -8.620,\n" +
                "\n" +
                "0.201, -37.231, 0.127, -31.317, 1.726, 4.155,\n" +
                "\n" +
                "0.131, 16.143, 0.190, 3.755, 2.662, 1.634,\n" +
                "\n" +
                "-0.165, 27.961, -0.127, 7.797, 2.660, -4.602,\n" +
                "\n" +
                "-0.019, 29.335, 0.041, 17.800, 2.574, -10.104,\n" +
                "\n" +
                "-0.240, 32.430, -0.200, 51.952, 2.194, -5.983,\n" +
                "\n" +
                "-0.030, 43.570, -0.040, 55.110, 2.416, 1.846,\n" +
                "\n" +
                "0.212, -39.385, 0.251, -56.470, 1.767, -1.320,\n" +
                "\n" +
                "-0.409, 57.102, -0.433, 46.092, 3.335, -4.709,\n" +
                "\n" +
                "-0.167, -17.732, -0.191, -13.024, 2.520, -4.909,\n" +
                "\n" +
                "-0.280, 20.104, -0.333, 38.764, 2.667, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_lg {\n" +
                "\n" +
                "339.028, 389.056, 301.778, 302.083, 403.750, 438.778, 356.944, 338.278, 366.278, 292.333, 393.417, 474.694\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Fpn_hg {\n" +
                "\n" +
                "356.250, 406.528, 319.556, 319.833, 420.639, 454.361, 373.611, 355.833, 383.194, 298.528, 410.528, 492.806\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_lg {\n" +
                "\n" +
                "0x8\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV20_hg {\n" +
                "\n" +
                "0xa\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Rampgen {\n" +
                "\n" +
                "0x8b\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Range {\n" +
                "\n" +
                "0xf\n" +
                "\n" +
                "}\n" +
                "\n" +
                "AutoV15 {\n" +
                "\n" +
                "0xd\n" +
                "\n" +
                "}\n" +
                "\n" +
                "Temp_calib {\n" +
                "\n" +
                "29.500, -61.936, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "\n";*/

       String s="\n" +
               "\n" +
               "DEF 18823042 {\n" +
               "\n" +
               "Version {\n" +
               "\n" +
               "0x2\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Kb {\n" +
               "\n" +
               "-0.059, 44.569, -0.035, 46.240, 3.171, -4.459,\n" +
               "\n" +
               "0.190, -3.700, 0.278, -23.431, 2.908, 3.126,\n" +
               "\n" +
               "-0.112, 45.185, -0.051, 37.745, 3.344, 6.081,\n" +
               "\n" +
               "0.051, 34.447, 0.106, -6.217, 3.031, 10.901,\n" +
               "\n" +
               "0.131, -24.011, 0.235, -50.942, 2.862, 7.064,\n" +
               "\n" +
               "-0.289, 48.031, -0.281, 38.295, 3.727, 3.408,\n" +
               "\n" +
               "0.125, -56.251, 0.254, -66.410, 3.112, 1.382,\n" +
               "\n" +
               "0.056, 16.163, 0.135, -19.653, 3.787, 1.188,\n" +
               "\n" +
               "0.209, -50.622, 0.362, -100.030, 2.438, 9.454,\n" +
               "\n" +
               "0.232, 39.337, 0.350, 26.390, 3.278, 0.000,\n" +
               "\n" +
               "-0.120, 25.270, -0.088, 15.886, 3.644, -0.771,\n" +
               "\n" +
               "0.176, -65.244, 0.292, -68.853, 3.015, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_lg {\n" +
               "\n" +
               "179.250, 197.389, 186.111, 180.083, 208.500, 168.056, 190.694, 74.028, 197.694, 338.250, 131.889, 132.472\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_hg {\n" +
               "\n" +
               "204.361, 221.833, 211.139, 205.083, 233.361, 193.417, 215.083, 99.694, 222.333, 362.472, 158.306, 158.500\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_lg {\n" +
               "\n" +
               "0x8\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_hg {\n" +
               "\n" +
               "0xa\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Rampgen {\n" +
               "\n" +
               "0x9d\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Range {\n" +
               "\n" +
               "0xf\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV15 {\n" +
               "\n" +
               "0x7\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Temp_calib {\n" +
               "\n" +
               "29.500, -52.739, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "}\n" +
               "\n" +
               "DEF 18823043 {\n" +
               "\n" +
               "Version {\n" +
               "\n" +
               "0x2\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Kb {\n" +
               "\n" +
               "0.049, -32.230, 0.140, -58.601, 4.135, 11.269,\n" +
               "\n" +
               "0.084, -23.638, 0.132, -43.285, 3.611, 10.230,\n" +
               "\n" +
               "0.097, 6.312, 0.119, -0.368, 3.072, 10.338,\n" +
               "\n" +
               "0.240, -51.580, 0.356, -53.175, 2.860, 4.473,\n" +
               "\n" +
               "0.240, -17.030, 0.355, -26.052, 3.018, 12.601,\n" +
               "\n" +
               "0.077, 14.595, 0.159, 6.729, 3.793, 7.186,\n" +
               "\n" +
               "0.123, -34.480, 0.285, -62.308, 3.450, 11.907,\n" +
               "\n" +
               "0.326, 16.640, 0.373, -0.467, 3.624, 5.424,\n" +
               "\n" +
               "0.121, 25.781, 0.211, -7.121, 3.590, 14.096,\n" +
               "\n" +
               "0.089, 17.843, 0.152, 12.390, 3.328, 8.248,\n" +
               "\n" +
               "-0.271, 4.571, -0.243, 17.746, 3.916, 12.794,\n" +
               "\n" +
               "-0.045, -2.338, 0.038, 27.670, 3.753, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_lg {\n" +
               "\n" +
               "110.333, 74.028, 152.306, 98.833, 190.111, 125.250, 109.167, 132.806, 147.111, 154.861, 68.583, 114.972\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_hg {\n" +
               "\n" +
               "133.806, 98.167, 176.000, 121.194, 211.694, 148.611, 134.056, 154.833, 172.028, 178.472, 90.750, 136.472\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_lg {\n" +
               "\n" +
               "0x8\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_hg {\n" +
               "\n" +
               "0xa\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Rampgen {\n" +
               "\n" +
               "0x83\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Range {\n" +
               "\n" +
               "0xf\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV15 {\n" +
               "\n" +
               "0x4\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Temp_calib {\n" +
               "\n" +
               "29.500, -52.655, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "}\n" +
               "\n" +
               "DEF 18823044 {\n" +
               "\n" +
               "Version {\n" +
               "\n" +
               "0x2\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Kb {\n" +
               "\n" +
               "0.171, -49.602, 0.244, -75.050, 2.188, 6.762,\n" +
               "\n" +
               "0.135, -3.397, 0.260, -9.620, 3.434, -10.386,\n" +
               "\n" +
               "0.174, -19.042, 0.269, -24.808, 1.855, 8.919,\n" +
               "\n" +
               "-0.162, 42.527, -0.157, 58.910, 3.064, 0.000,\n" +
               "\n" +
               "0.364, -72.134, 0.402, -90.001, 2.052, 10.806,\n" +
               "\n" +
               "-0.117, 40.164, -0.157, 28.306, 2.973, 0.000,\n" +
               "\n" +
               "-0.101, -24.530, -0.063, -17.951, 2.924, -1.637,\n" +
               "\n" +
               "-0.066, 21.192, -0.121, 26.979, 2.864, -4.878,\n" +
               "\n" +
               "-0.163, 18.357, -0.151, 30.301, 2.482, 1.592,\n" +
               "\n" +
               "-0.216, 66.434, -0.184, 45.109, 3.178, 0.000,\n" +
               "\n" +
               "-0.038, 7.774, 0.047, -6.125, 3.164, -2.023,\n" +
               "\n" +
               "-0.111, -13.218, -0.016, -30.331, 2.138, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_lg {\n" +
               "\n" +
               "257.861, 276.972, 278.917, 337.778, 145.389, 322.861, 304.111, 282.944, 305.333, 332.972, 288.139, 233.278\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_hg {\n" +
               "\n" +
               "277.694, 301.167, 299.889, 359.500, 166.944, 342.417, 324.722, 304.278, 326.778, 354.194, 308.361, 257.222\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_lg {\n" +
               "\n" +
               "0x8\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_hg {\n" +
               "\n" +
               "0xa\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Rampgen {\n" +
               "\n" +
               "0x90\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Range {\n" +
               "\n" +
               "0xf\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV15 {\n" +
               "\n" +
               "0xc\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Temp_calib {\n" +
               "\n" +
               "29.500, -54.881, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "}\n" +
               "\n" +
               "\n" +
               "DEF 18823046 {\n" +
               "\n" +
               "Version {\n" +
               "\n" +
               "0x2\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Kb {\n" +
               "\n" +
               "-0.169, 27.040, -0.136, 31.929, 2.447, 2.334,\n" +
               "\n" +
               "-0.005, -18.665, -0.043, -14.932, 1.535, 4.946,\n" +
               "\n" +
               "0.040, 27.984, 0.110, -0.657, 1.566, 5.801,\n" +
               "\n" +
               "-0.015, -15.588, 0.061, -61.225, 2.436, -3.177,\n" +
               "\n" +
               "-0.597, 83.825, -0.476, 87.088, 2.179, -1.316,\n" +
               "\n" +
               "-0.061, -20.828, -0.009, -55.386, 2.263, 10.527,\n" +
               "\n" +
               "0.095, -17.371, 0.151, -37.902, 1.147, 9.841,\n" +
               "\n" +
               "-0.141, 37.899, -0.064, 29.464, 2.017, 6.811,\n" +
               "\n" +
               "-0.053, 11.805, 0.004, -15.502, 2.469, 2.113,\n" +
               "\n" +
               "-0.161, 61.775, -0.154, 73.978, 1.766, 7.419,\n" +
               "\n" +
               "-0.369, 63.895, -0.357, 57.607, 2.878, 5.383,\n" +
               "\n" +
               "-0.063, -19.821, 0.010, -54.091, 2.255, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_lg {\n" +
               "\n" +
               "104.139, 132.722, 161.917, 115.556, 172.250, 60.556, 182.222, 178.444, 81.750, 142.417, 123.500, 77.611\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Fpn_hg {\n" +
               "\n" +
               "124.389, 155.833, 183.917, 137.167, 193.528, 84.056, 202.778, 200.667, 103.861, 165.528, 146.583, 99.528\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_lg {\n" +
               "\n" +
               "0x8\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV20_hg {\n" +
               "\n" +
               "0xa\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Rampgen {\n" +
               "\n" +
               "0x82\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Range {\n" +
               "\n" +
               "0xf\n" +
               "\n" +
               "}\n" +
               "\n" +
               "AutoV15 {\n" +
               "\n" +
               "0x6\n" +
               "\n" +
               "}\n" +
               "\n" +
               "Temp_calib {\n" +
               "\n" +
               "29.500, -52.727, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000\n" +
               "\n" +
               "}\n" +
               "\n" +
               "}\n" +
               "\n";

        ByteArrayInputStream ips=new ByteArrayInputStream(s.getBytes());
        return ips;
    }
    private String transferImageData(int chan, int k, byte[] reveicedBytes) {
        int count;
        PcrCommand.IMAGE_MODE mImageMode = PcrCommand.IMAGE_MODE.IMAGE_12;
        count = mImageMode.getSize() + 1;

        int[] txData = new int[count];

        //aa 00 02 1c 02 0b 07112b12001117115012bb1063118b0a6311741bb50e000ed07a0d1717
        for (int numData = 0; numData < mImageMode.getSize(); numData++) {
            byte high = reveicedBytes[numData * 2 + 7];
            byte low = reveicedBytes[numData * 2 + 6];

            int value = TrimReader.getInstance()
                    .tocalADCCorrection(numData, high, low,
                            mImageMode.getSize(), chan,
                            CommData.gain_mode, 0);

            txData[numData] = value;
        }
        //当前行号
        txData[mImageMode.getSize()] = reveicedBytes[5];

        String res = "";

        for (int i = 0; i < txData.length; i++) {
            if (i == 0) {
                res = txData[i] + "";

            } else {
                if (i != 11 && i != 23) {
                    res += " " + txData[i];

                } else {
                    if (k == 11 || k == 23) {
                        res += " " + 15000;

                    } else {
                        res += " " + txData[i];
                    }

                }

            }

        }


        return res;
    }

    @Test
    public void testCombineString() {
        Map<String, List<String>> mItemData = new LinkedHashMap<>();
        List<String> chip1 = new ArrayList<>();
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
        mItemData.put("Chip#1", chip1);

        List<String> chip2 = new ArrayList<>();
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
        mItemData.put("Chip#2", chip2);

        Set<Map.Entry<String, List<String>>> entries = mItemData.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append(key)
                    .append("\n");
            for (String item : value) {
                sBuilder.append(item)
                        .append("\n");
            }
            System.out.println(sBuilder.toString());
        }
    }

    @Test
    public void testDiv() {
        int mode = 59 % 12;
        System.out.println(mode);
        int n = (int) Math.ceil(59 / 12f);
        System.out.println(n);
    }

    @Test
    public void testByteToString() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0xaa;
        cmd[1] = (byte) 0x13;
        cmd[2] = (byte) 0x2;
        cmd[3] = (byte) 0x1;
        cmd[4] = (byte) 0x17;
        cmd[5] = (byte) 0x17;
        for (int i=0;i<cmd.length;i++){
            System.out.print(cmd[i]+" ");
        }
        System.out.println();

        String rev = ByteUtil.getHexStr(cmd, cmd.length);
        System.out.println(rev);
        byte[] result = ByteUtil.hexStringToBytes(rev, rev.length() / 2);
        for (int i=0;i<result.length;i++){
            System.out.print(result[i]+" ");
        }
        System.out.println();
        String revs = ByteUtil.getHexStr(result, result.length);
        //打印出转回的byte
        System.out.println(revs);

       // String revSource="aa00021c1200551236104a1455107716291251112e132f133d13cf0f3f12d77e211717";
        //byte[] bytes =ByteUtil.hexStringToBytes(revSource,revSource.length()/2);

    }

    @Test
    public void testLastIndexOf() {
        String rev = "aa0083587aa357353781717aa0037573771717";
        List<String> vals = new ArrayList<>();
        splitAndCombine(rev, vals);
       /* String revs[]={rev,"25421717"};

        for (String r:revs) {
            splitAndCombine(r, vals);
        }
*/
        // System.out.println("size:"+vals.size());
    }

    private void splitAndCombine(String rev, List<String> vals) {
        int indexOf = rev.indexOf("1717aa");
        if (indexOf > 0) {
            String part1 = rev.substring(0, indexOf + 4);
            if (part1.startsWith((C.Value.DATA_PREFIX))) {
                if (part1.endsWith(C.Value.DATA_SUFFIX)) {
                    vals.clear();
                    //part1是一组完整的数据了
                    System.out.println("完整数据:" + part1);
                } else {
                    vals.add(part1);
                }
            } else if (part1.endsWith(C.Value.DATA_SUFFIX)) {
                vals.add(part1);
                StringBuilder sb = new StringBuilder();
                for (String v : vals) {
                    sb.append(v);
                }
                vals.clear();
                if (sb.toString().lastIndexOf("1717aa") > 0) {
                    splitAndCombine(sb.toString(), vals);
                } else {
                    System.out.println("完整数据:" + sb.toString());
                }
            }
            String leftPart = rev.substring(indexOf + 4);
            splitAndCombine(leftPart, vals);

        } else if (indexOf == 0) {
            //数据以aa开头
            String leftPart = rev.substring(2);
            indexOf = leftPart.indexOf("1717aa");
            //测试是否还有aa
            if (indexOf > 0) {
                String part1 = rev.substring(0, indexOf + 2 + 4);
                if (part1.endsWith(C.Value.DATA_SUFFIX)) {
                    vals.clear();
                    //part1是一组完整的数据了
                    System.out.println("完整数据:" + part1);
                } else {
                    vals.add(part1);
                }
                String part2 = rev.substring(indexOf + 2 + 4);
                splitAndCombine(part2, vals);
            } else {
                //没有aa了
                if (rev.endsWith(C.Value.DATA_SUFFIX)) {
                    //一组完整的数据
                    vals.clear();
                    //part1是一组完整的数据了
                    System.out.println("完整数据:" + rev);
                } else {
                    //以aa开头但是不是以1717结尾，数据还不完整
                    vals.add(rev);
                }
            }

        } else {
            //不存在aa

            if (rev.endsWith(C.Value.DATA_SUFFIX)) {
                vals.add(rev);
                StringBuilder sb = new StringBuilder();
                for (String v : vals) {
                    sb.append(v);
                }
                vals.clear();
                if (sb.toString().lastIndexOf("1717aa") > 0) {
                    splitAndCombine(sb.toString(), vals);
                } else {
                    System.out.println("完整数据:" + sb.toString());
                }
            } else {
                vals.add(rev);
            }
        }
    }

    @Test
    public void sub1717() {
        String source = "aa00fsi843784fjskhfsgskhfhskghskfjutu381717ksg99948848433e400000000000000000000000";
        int index = source.indexOf("1717");
        source = source.substring(0, index + 4);
        System.out.println(source);//aa00fsi843784fjskhfsgskhfhskghskfjutu381717

        float int_time1 = 1f;
        float factor = 0.5f;
        int_time1 = Float.parseFloat(String.format("%.2f", int_time1 * factor));
        System.out.println(int_time1);
    }

    @Test
    public void testJsonArray() {
        String json = "[[\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                "]\n" +
                ",[\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                "]\n" +
                "]";
        Map<Integer, List<Double>> chanMap = new LinkedHashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONArray subJSONArray = jsonArray.getJSONArray(i);
                for (int j = 0; j < subJSONArray.length(); i++) {
                    JSONArray subSubJSONArray = subJSONArray.getJSONArray(j);
                    List<Double> yVals = new ArrayList<>();
                    for (int k = 0; k < subSubJSONArray.length(); k++) {
                        double y = subSubJSONArray.getDouble(k);
                        yVals.add(y);
                    }
                    chanMap.put(i, yVals);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < chanMap.size(); i++) {
            List<Double> yVals = chanMap.get(i);
            for (double y : yVals) {
                System.out.print(y + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testListMax() {
        double rate = 8e-8;
        System.out.println(rate);
        List<ChartData> cdlist = new ArrayList<>();

        ChartData c = new ChartData();
        c.y = 23;
        cdlist.add(c);

        ChartData c2 = new ChartData();
        c2.y = 24;
        cdlist.add(c2);


        ChartData c1 = new ChartData();
        c1.y = 22;
        cdlist.add(c1);

        ChartData c3 = new ChartData();
        c3.y = 21;
        cdlist.add(c3);
        double y_max_value = Collections.max(cdlist, new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {
                return o1.y - o2.y;
            }
        }).y;
        System.out.println(y_max_value);
    }

    @Test
    public void testSplit() {
        String s = "151 137 103 105 140 98 100 96 93 97 106 102  0";
        String[] parts = s.replace("  ", " ").split(" ");
        System.out.println(parts.length);
        for (String item : parts) {
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
        short t = 0;
        cmd2.step2(105f, t);
        toByteString(cmd2);

        PcrCommand cmd3 = new PcrCommand();
        float temp = 50;
        short tt = 10;
        PcrCommand.TempDuringCombine combine = new PcrCommand.TempDuringCombine(temp, tt);
        List<PcrCommand.TempDuringCombine> combines = new ArrayList<>();
        combines.add(combine);
        cmd3.step3(0, 1, 1, combines);
        toByteString(cmd3);


        PcrCommand cmd4 = new PcrCommand();
        int cyclingCount = 2;
        float preT = 95;
        short preD = 5;
        PcrCommand.TempDuringCombine predenaturationCombine = new PcrCommand.TempDuringCombine(preT, preD);
        float extendT = 50;
        short extD = 5;
        PcrCommand.TempDuringCombine extendCombine = new PcrCommand.TempDuringCombine(extendT, extD);
        cmd4.step4(PcrCommand.Control.START, cyclingCount, PcrCommand.CmdMode.NORMAL,
                predenaturationCombine, extendCombine);
        toByteString(cmd4);

        PcrCommand meltingCurveCmd = new PcrCommand();
        float startT = 50;
        float endT = 55;
        float speed = 1;
        meltingCurveCmd.meltingCurve(PcrCommand.Control.START, startT, endT, speed);
        toByteString(meltingCurveCmd);
    }

    private void toByteString(PcrCommand cmd) {
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
        byte[] shortByte = new byte[2];
        shortByte[0] = tempBytes[0];
        shortByte[1] = tempBytes[1];
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

        String h = "a";
        int a = Integer.parseInt(h, 16);
        System.out.println(a);
        int picStep = 1;
        int steps = 3;
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