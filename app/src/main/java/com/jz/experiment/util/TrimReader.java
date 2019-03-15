package com.jz.experiment.util;

import android.content.Context;

import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.FlashData;
import com.jz.experiment.module.bluetooth.CommunicationService;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.wind.base.C;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TrimReader {
    int TRIM_IMAGER_SIZE = 12;
    double[][] kb = new double[12][6];
    double[][] fpn = new double[2][12];
    double[] tempcal = new double[12];
    int rampgen = 0;
    int[] auto_v20 = new int[2];
    int auto_v15 = 0;
    String version = "";

    private static TrimReader sInstance;

    public static TrimReader getInstance() {
        if (sInstance == null) {
            synchronized (TrimReader.class) {
                if (sInstance == null) {
                    sInstance = new TrimReader();
                }
            }
        }
        return sInstance;
    }

    public TrimReader() {
    }

    /**
     * 从仪器中读取trim data
     */
    public void ReadTrimDataFromInstrument(CommunicationService service) {
        PcrCommand cmd = new PcrCommand();
        cmd.trimData();
        service.sendPcrCommand(cmd);

    }

    public void ReadTrimFile(Context context) {
        try {
            InputStream ips;
            File trimFile = new File(C.Value.TRIM_FOLDER, "trim.dat");
            if (trimFile.exists()) {
                ips = new FileInputStream(trimFile);
                ReadTrimFile(ips);
                CommData.sTrimFromFile=true;
            } else {
                CommData.sTrimFromFile=false;
            }
             /*else {
                ips = context.getAssets().open("trim.dat");
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * TODO 跟c#版本比对读取的数据是否一致
     *
     * @param ips
     */
    public void ReadTrimFile(InputStream ips) {
        //判断相应月份文件夹是否存在，没有则创建
       /* String path = AppDomain.CurrentDomain.BaseDirectory + "trim/trim.dat";
        if (!System.IO.File.Exists(path))
        {
            MessageBox.Show("trim文件不存在！");
        }*/
        try {


            BufferedReader reader = new BufferedReader(new InputStreamReader(ips));
            String line;
            List<String> ss = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (isEmpty(line)) {
                    continue;
                }
                ss.add(line);

            }
            ips.close();
            reader.close();

            int index = 0;
            for (int i = 0; i < ss.size(); i++) {
                if (ss.get(i).contains("Version")) {
                    version = ss.get(i + 1);

                }
                if (ss.get(i).contains("Kb")) {
                    kb = new double[12][6];
                    //List<String> kbs = ss.ToList().Skip(i + 1).Take(12).ToList();
                    List<String> kbs = ss.subList(i + 1, i + 1 + 12);

                    for (int n = 0; n < kbs.size(); n++) {
                        if (isEmpty(kbs.get(n))) continue;

                        String[] strs = kbs.get(n).split(",");
                        for (int j = 0; j < strs.length; j++) {
                            if (isEmpty(strs[j])) continue;

                            kb[n][j] = Double.parseDouble(strs[j].trim());
                        }
                    }
                }

                if (ss.get(i).contains("Fpn_lg")) {
                    fpn = new double[2][12];
                    //List<String> Fpn_lgs = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> Fpn_lgs = ss.subList(i + 1, i + 1 + 1);

                    String[] strs = Fpn_lgs.get(0).split(",");
                    for (int j = 0; j < strs.length; j++) {
                        if (isEmpty(strs[j])) continue;

                        fpn[0][j] = Double.parseDouble(strs[j].trim());
                    }
                }
                if (ss.get(i).contains("Fpn_hg")) {
                    //List<String> Fpn_hgs = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> Fpn_hgs = ss.subList(i + 1, i + 1 + 1);
                    String[] strs = Fpn_hgs.get(0).split(",");
                    for (int j = 0; j < strs.length; j++) {
                        if (isEmpty(strs[j])) continue;

                        fpn[1][j] = Double.parseDouble(strs[j].trim());
                    }
                }
                if (ss.get(i).contains("AutoV20_lg")) {
                    auto_v20 = new int[2];
                    //List<String> AutoV20_lgs = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> AutoV20_lgs = ss.subList(i + 1, i + 1 + 1);
                    // String mm = AutoV20_lgs.get(0).trim();
                    String mm = AutoV20_lgs.get(0).trim().replace("0x", "");
                    int a = Integer.parseInt(mm, 16);
                    auto_v20[0] = a;
               /* uint a = Convert.ToUInt32(mm, 16);
                auto_v20[0] = Convert.ToInt32(a);*/
                }

                if (ss.get(i).contains("AutoV20_hg")) {
                    // List<String> AutoV20_hgs = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> AutoV20_hgs = ss.subList(i + 1, i + 1 + 1);
                    String mm = AutoV20_hgs.get(0).trim().replace("0x", "");
                    int a = Integer.parseInt(mm, 16);
                    auto_v20[1] = a;
            /*    uint a = Convert.ToUInt32(mm, 16);
                auto_v20[1] = Convert.ToInt32(a);*/
                }

                if (ss.get(i).contains("AutoV15")) {
                    //List<String> AutoV15s = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> AutoV15s = ss.subList(i + 1, i + 1 + 1);
                    String mm = AutoV15s.get(0).trim().replace("0x", "");
                    int a = Integer.parseInt(mm, 16);
                    auto_v15 = a;
              /*  uint a = Convert.ToUInt32(mm, 16);
                auto_v15 = Convert.ToInt32(a);*/
                }

                if (ss.get(i).contains("Rampgen")) {
                    // List<String> Rampgens = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> Rampgens = ss.subList(i + 1, i + 1 + 1);
                    String mm = Rampgens.get(0).trim().replace("0x", "");
                    int a = Integer.parseInt(mm, 16);
                    rampgen = a;
              /*  uint a = Convert.ToUInt32(mm, 16);
                rampgen = Convert.ToInt32(a);*/
                }

                if (ss.get(i).contains("Temp_calib")) {
                    tempcal = new double[12];
                    // List<String> Temp_calibs = ss.ToList().Skip(i + 1).Take(1).ToList();
                    List<String> Temp_calibs = ss.subList(i + 1, i + 1 + 1);
                    String[] strs = Temp_calibs.get(0).split(",");
                    for (int j = 0; j < strs.length; j++) {
                        if (isEmpty(strs[j])) continue;
                        // if (String.IsNullOrEmpty(strs[j])) continue;

                        //tempcal[j] = Convert.ToDouble(strs[j].Trim());
                        tempcal[j] = Double.parseDouble(strs[j].trim());
                    }

                    switch (index) {
                        case 0:
                            CommData.chan1_kb = kb;
                            CommData.chan1_fpn = fpn;
                            CommData.chan1_tempcal = tempcal;
                            CommData.chan1_rampgen = rampgen;
                            CommData.chan1_auto_v20 = auto_v20;
                            CommData.chan1_auto_v15 = auto_v15;
                            break;
                        case 1:
                            CommData.chan2_kb = kb;
                            CommData.chan2_fpn = fpn;
                            CommData.chan2_tempcal = tempcal;
                            CommData.chan2_rampgen = rampgen;
                            CommData.chan2_auto_v20 = auto_v20;
                            CommData.chan2_auto_v15 = auto_v15;
                            break;
                        case 2:
                            CommData.chan3_kb = kb;
                            CommData.chan3_fpn = fpn;
                            CommData.chan3_tempcal = tempcal;
                            CommData.chan3_rampgen = rampgen;
                            CommData.chan3_auto_v20 = auto_v20;
                            CommData.chan3_auto_v15 = auto_v15;
                            break;
                        case 3:
                            CommData.chan4_kb = kb;
                            CommData.chan4_fpn = fpn;
                            CommData.chan4_tempcal = tempcal;
                            CommData.chan4_rampgen = rampgen;
                            CommData.chan4_auto_v20 = auto_v20;
                            CommData.chan4_auto_v15 = auto_v15;
                            break;
                    }
                    index++;
                }


            }
//            LogUtil.e("读取Trim成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("读取trim失败");
        }
    }


    public int tocalADCCorrection(int numData, byte highByte, Byte lowByte, int pixelNum, int PCRNum,
                                  int gain_mode, int flag) {

        if (FlashData.flash_loaded) {
            return ADCCorrectioni(numData, highByte, lowByte, pixelNum, PCRNum, gain_mode, flag);
        }

        if (version.equals("0x2")) {
            //  System.out.println("ADCCorrection");
            return ADCCorrection(numData, highByte, lowByte, pixelNum, PCRNum, gain_mode, flag);
        } else {
            //System.out.println("ADCCorrectionOld");
            return ADCCorrectionOld(numData, highByte, lowByte, pixelNum, PCRNum, gain_mode, flag);
        }
    }

    int DARK_LEVEL = 100;

    public int ADCCorrectioni(int NumData, byte HighByte, byte LowByte, int pixelNum, int PCRNum, int gain_mode,
                              int flag) {
        SetTrim2(PCRNum);

        int hb, lb, lbc, hbi;
        int hbln, lbp, hbhn;
        boolean oflow = false, uflow = false;

        int ioffset = 0;
        int result;

        int intmax = 32767;
        int intmax256 = 128;

       // hb = (int) HighByte;
        hb =  HighByte &0xff;
        hbln = hb % 16;     //
        hbhn = hb / 16;     //

        int nd = 0;
        if (pixelNum == 12) nd = NumData;
        else nd = NumData >> 1;

        int k, b, c, h;

        //	double shrink = 0.022;

        c = (int) (kbi[nd][4]);
        h = (int) (kbi[nd][5]);

        if (hb < 16) {
            k = (int) (kbi[nd][0]);
            b = (int) (kbi[nd][1]) + h / 2;         // 15 is just an empirical value, the first bump is raised higher. To do: what about reverse bump
            c = c + h / 10;
        } else if (hb < 128) {
            k = (int) (kbi[nd][0]);
            b = (int) (kbi[nd][1]);                 //
        } else {
            k = (int) (kbi[nd][2]);
            b = (int) (kbi[nd][3]);                 //
        }

        ioffset = k * hb / intmax + b / intmax256;

        //lb = (int) LowByte;
        lb =  LowByte  &0xff;
        lbc = lb + ioffset;

        if (hb > 128) {
            hbi = 128 + (hb - 128) / 2;
        } else {
            hbi = hb;
        }

        // Use lbc, not hbln to calculate sawtooth correction, as hbln tends to be a little jittery
        ioffset += (lbc - 128) * c * (300 - hbi) / (12 * 300 * intmax256);      // 12/19/2016 modification, shrinking sawtooth.
        lbc = lb + ioffset;                                             // re-calc lbc, 2 pass algorithm

        if (lbc > 255) lbc = 255;
        else if (lbc < 0) lbc = 0;

        lbp = hbln * 16 + 7;

        int lbpc = lbp - ioffset;               // lpb - ioffset: low byte predicted from the high byte low nibble BEFORE correction
        int qerr = lbp - lbc;                   // if the lbc is correct, this would be the quantization error. If it is too large, maybe lb was the saturated "stuck" version

        if (lbpc > 255 + 20) {                   // We allow some correction error, because hbln may have randomly flipped.
            oflow = true;
            flag = 1;
        } else if (lbpc > 255 && qerr > 28) {       // Again we allow some tolerance because hbln may have drifted, leading to fake error
            oflow = true;
            flag = 2;
        } else if (lbpc > 191 && qerr > 52) {
            oflow = true;
            flag = 3;
        } else if (qerr > 96) {
            oflow = true;
            flag = 4;
        } else if (lbpc < -20) {
            uflow = true;
            flag = 5;
        } else if (lbpc < 0 && qerr < -28) {
            uflow = true;
            flag = 6;
        } else if (lbpc < 64 && qerr < -52) {
            uflow = true;
            flag = 7;
        } else if (qerr < -96) {
            uflow = true;
            flag = 8;
        } else {
            flag = 0;
        }

        if (oflow || uflow) {
            result = hb * 16 + 7;
        } else {
            result = hbhn * 256 + lbc;
        }


        if (gain_mode == 0)
            result += -(int) (fpni[1][nd]) + DARK_LEVEL;        // high gain
        else
            result += -(int) (fpni[0][nd]) + DARK_LEVEL;        // low gain

        if (result < 0) result = 0;


        return result;
    }

    public int ADCCorrection(int NumData, Byte HighByte, Byte LowByte, int pixelNum, int PCRNum, int gain_mode, int flag) {

        SetTrim(PCRNum);

        int hb, lb, lbc, hbi;
        int hbln, lbp, hbhn;
        boolean oflow = false, uflow = false; //  qerr_big=false;

        //	CString strbuf;
        double ioffset = 0;
        int result;

        hb = (HighByte & 0xff);
        //hb = (int) (HighByte &0xff);
        hbln = hb % 16;        //
        hbhn = hb / 16;        //

        int nd = 0;
        if (pixelNum == 12) nd = NumData;
        else nd = NumData >> 1;

        double k, b, c, h;

        c = kb[nd][4];
        h = kb[nd][5];
        double shrink = c * 0.0033;

        if (hb < 16) {
            k = kb[nd][0];
            b = kb[nd][1] + h * 0.5;            // 15 is just an empirical value, the first bump is raised higher. To do: what about reverse bump
            c = c + 0.1 * h;
        } else if (hb < 128) {
            k = kb[nd][0];
            b = kb[nd][1];                    //
        } else {
            k = kb[nd][2];
            b = kb[nd][3];                    //
        }

        ioffset = k * (double) hb + b;

        //lb = (int) LowByte;
        lb = LowByte & 0xff;
        lbc = lb + (int) ioffset;

        if (hb > 128) {
            hbi = 128 + (hb - 128) / 2;
        } else {
            hbi = hb;
        }

        // Use lbc, not hbln to calculate sawtooth correction, as hbln tends to be a little jittery
        ioffset += ((double) lbc - 128) * (c - shrink * (double) hbi) / 12;        // 12/19/2016 modification, shrinking sawtooth.
        lbc = lb + (int) ioffset;                    // re-calc lbc, 2 pass algorithm

        if (lbc > 255) lbc = 255;
        else if (lbc < 0) lbc = 0;

        lbp = hbln * 16 + 7;

        int lbpc = lbp - (int) ioffset;                // lpb - ioffset: low byte predicted from the high byte low nibble BEFORE correction
        int qerr = lbp - lbc;                    // if the lbc is correct, this would be the quantization error. If it is too large, maybe lb was the saturated "stuck" version

        if (lbpc > 255 + 20) {                    // We allow some correction error, because hbln may have randomly flipped.
            oflow = true;
            flag = 1;
        } else if (lbpc > 255 && qerr > 28) {        // Again we allow some tolerance because hbln may have drifted, leading to fake error
            oflow = true;
            flag = 2;
        } else if (lbpc > 191 && qerr > 52) {
            oflow = true;
            flag = 3;
        } else if (qerr > 96) {
            oflow = true;
            flag = 4;
        } else if (lbpc < -20) {
            uflow = true;
            flag = 5;
        } else if (lbpc < 0 && qerr < -28) {
            uflow = true;
            flag = 6;
        } else if (lbpc < 64 && qerr < -52) {
            uflow = true;
            flag = 7;
        } else if (qerr < -96) {
            uflow = true;
            flag = 8;
        } else {
            flag = 0;
        }

        if (oflow || uflow) {
            result = hb * 16 + 7;
        } else {
            result = hbhn * 256 + lbc;
        }

        //if (calib2) return result;


        if (CommData.gain_mode == 0)
            result += -(int) (fpn[1][nd]) + 100;        // high gain
        else
            result += -(int) (fpn[0][nd]) + 100;        // low gain

        if (result < 0) result = 0;


        return result;
    }

    int[][] kbi = new int[12][6];
    int[][] fpni = new int[2][12];

    private void SetTrim2(int PcrNum) {
        int ci = PcrNum - 1;

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 6; j++) {
                kbi[i][j] = FlashData.kbi[ci][i][j];
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 12; j++) {
                fpni[i][j] = FlashData.fpni[ci][i][j];
            }
        }

        for (int i = 0; i < 2; i++) {
            auto_v20[i] = FlashData.auto_v20[ci][i];
        }

        rampgen = FlashData.rampgen[ci];
        auto_v15 = FlashData.auto_v15[ci];
    }

    private void SetTrim(int PcrNum) {
        switch (PcrNum) {
            case 1:
                kb = CommData.chan1_kb;
                fpn = CommData.chan1_fpn;
                tempcal = CommData.chan1_tempcal;
                rampgen = CommData.chan1_rampgen;
                auto_v20 = CommData.chan1_auto_v20;
                auto_v15 = CommData.chan1_auto_v15;
                break;
            case 2:
                kb = CommData.chan2_kb;
                fpn = CommData.chan2_fpn;
                tempcal = CommData.chan2_tempcal;
                rampgen = CommData.chan2_rampgen;
                auto_v20 = CommData.chan2_auto_v20;
                auto_v15 = CommData.chan2_auto_v15;
                break;
            case 3:
                kb = CommData.chan3_kb;
                fpn = CommData.chan3_fpn;
                tempcal = CommData.chan3_tempcal;
                rampgen = CommData.chan3_rampgen;
                auto_v20 = CommData.chan3_auto_v20;
                auto_v15 = CommData.chan3_auto_v15;
                break;
            case 4:
                kb = CommData.chan4_kb;
                fpn = CommData.chan4_fpn;
                tempcal = CommData.chan4_tempcal;
                rampgen = CommData.chan4_rampgen;
                auto_v20 = CommData.chan4_auto_v20;
                auto_v15 = CommData.chan4_auto_v15;
                break;
        }
    }

    public int ADCCorrectionOld(int NumData, Byte HighByte, Byte LowByte, int pixelNum, int PCRNum, int gain_mode, int flag) {

        SetTrim(PCRNum);

        int hb, lb, lbc;
        int hbln, lbp, hbhn;
        boolean oflow = false, uflow = false; //  qerr_big=false;

        String strbuf;
        double ioffset = 0;
        int result;

        // hb = (int) HighByte;
        hb = HighByte & 0xff;

        int nd = 0;
        if (pixelNum == 12) nd = NumData;
        else nd = NumData >> 1;

        ioffset = kb[nd][0] * (double) hb + kb[nd][1];


        if (hb >= 128) {
            ioffset += kb[nd][3];
        }


        hbln = hb % 16;        //

        hbhn = hb / 16;        //


        //		ioffset = (int)(Node[PCRNum-1].kb[nd][0]*hb + Node[PCRNum-1].kb[nd][1] + Node[PCRNum-1].kb[nd][2] *(hbln - 7));

        lb = LowByte & 0xff;

        lbc = lb + (int) ioffset;

        // Use lbc, not hbln to calculate sawtooth correction, as hbln tends to be a little unstable
        ioffset += kb[nd][2] *
                ((double) lbc - 127) * (1 - (double) hb / 400) / 16;        // 12/19/2016 modification, shrinking sawtooth.
        lbc = lb + (int) ioffset;                    // re-calc lbc, 2 pass algorithm


        lbp = hbln * 16 + 7;

        if (lbc > 255) lbc = 255;
        else if (lbc < 0) lbc = 0;

        int lbpc = lbp - (int) ioffset;                // lpb - ioffset: low byte predicted from the high byte low nibble BEFORE correction
        int qerr = lbp - lbc;                    // if the lbc is correct, this would be the quantization error. If it is too large, maybe lb was the saturated "stuck" version

        if (lbpc > 255 + 20) {                    // We allow some correction error, because hbln may have randomly flipped.
            oflow = true;
            flag = 1;
        } else if (lbpc > 255 && qerr > 28) {        // Again we allow some tolerance because hbln may have drifted, leading to fake error
            oflow = true;
            flag = 2;
        } else if (lbpc > 191 && qerr > 52) {
            oflow = true;
            flag = 3;
        } else if (qerr > 96) {
            oflow = true;
            flag = 4;
        } else if (lbpc < -20) {
            uflow = true;
            flag = 5;
        } else if (lbpc < 0 && qerr < -28) {
            uflow = true;
            flag = 6;
        } else if (lbpc < 64 && qerr < -52) {
            uflow = true;
            flag = 7;
        } else if (qerr < -96) {
            uflow = true;
            flag = 8;
        } else {
            flag = 0;
        }

        //	if(abs(qerr) > 84) qerr_big = true;

        if (oflow || uflow) {
            result = hb * 16 + 7;
        } else {
            result = hbhn * 256 + lbc;
        }

        //if (calib2) return result;


        if (CommData.gain_mode == 0)
            result += -(int) (fpn[1][nd]) + 100;        // high gain
        else
            result += -(int) (fpn[0][nd]) + 100;        // low gain

        if (result < 0) result = 0;


        return result;
    }
}
