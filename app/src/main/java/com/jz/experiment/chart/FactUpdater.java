package com.jz.experiment.chart;

import android.text.TextUtils;

import com.anitoa.bean.FlashData;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.service.CommunicationService;
import com.anitoa.util.AnitoaLogUtil;
import com.jz.experiment.util.DataFileUtil;
import com.anitoa.util.ThreadUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FactUpdater {


    boolean[] m_dynIntTime = new boolean[CCurveShow.MAX_CHAN];
    float[] m_factorIntTime = new float[CCurveShow.MAX_CHAN];
    int[] m_maxPixVal = new int[CCurveShow.MAX_CHAN];
    double[][] m_factorData = new double[CCurveShow.MAX_CHAN][400];
    private CommunicationService mCommunicationService;
    private static FactUpdater sInstance;
    private DecimalFormat mDecimalFormat;
    private boolean isPcr=true;

    public void setPcr(boolean pcr) {
        isPcr = pcr;
    }

    public static FactUpdater getInstance(CommunicationService service) {
        System.out.println("FactUpdater==null->"+(sInstance == null));
        if (sInstance == null) {
            synchronized (FactUpdater.class) {
                if (sInstance == null) {
                    sInstance = new FactUpdater(service);
                }
            }
        }
        /*
         mCommunicationService重新赋值重要  原因：https://blog.csdn.net/denghwen/article/details/51448290
         *
         *直接杀死一个应用：所有的内存都会被回收，重新启动应用程序时，需要重新调用Application的OnCreate方法，会调用onSaveInstanceState方法。
         *退出键退出程序：退出程序后，一些加载过的静态变量并没有被回收，重新启动也不需要调用Application的OnCreate方法。
         *
         */
        sInstance.mCommunicationService=service;
        return sInstance;
    }

    private FactUpdater(CommunicationService service) {
        this.mCommunicationService = service;
        mDecimalFormat = new DecimalFormat("#.00");
    }


    public void updateFact() {

        for (int i = 1; i <= CCurveShow.MAX_CHAN; i++) {
            UpdatePCRCurve(i, 0);
        }

        DynamicUpdateIntTime();
       // CommData.m_factorData = m_factorData;

        //需要在m_factorData前面插入1
      /*  double [][] temp=new double[CCurveShow.MAX_CHAN][400];
        for (int i = 0; i < CCurveShow.MAX_CHAN; i++) {
            for (int j = 0; j < xhindex; j++) {
                temp[i][j+1]=m_factorData[i][j];
            }
        }
        for (int i=0;i<CCurveShow.MAX_CHAN;i++){
            temp[i][0]=1;
        }

        CommData.m_factorData = temp;*/


    }

    /// <summary>
    /// 初始化值
    /// </summary>
    public void SetInitData() {
        for (int i = 0; i < CCurveShow.MAX_CHAN; i++) {
            m_dynIntTime[i] = false;
            m_factorIntTime[i] = (float) 1.0;
            if (isPcr){
                m_maxPixVal[i] = FlashData.AutoInt_Target_PCR;
            }else {
                m_maxPixVal[i] = FlashData.AutoInt_Target_MELTING;
            }


            for (int n = 0; n < 200; n++) {
                m_factorData[i][n] = 1;
            }
        }

        xhindex = 1;
        int_time1 = 1;
        int_time2 = 1;
        int_time3 = 1;
        int_time4 = 1;

      /*  int_time_1=20;//默认积分时间20ms
        int_time_2=20;
        int_time_3=20;
        int_time_4=20;*/
    }

    int xhindex = 1;

    float int_time1 = 1;
    float int_time2 = 1;
    float int_time3 = 1;
    float int_time4 = 1;

    public float int_time_1 = 1;
    public float int_time_2 = 1;
    public float int_time_3 = 1;
    public float int_time_4 = 1;

    private void DynamicUpdateIntTime() {

        for (int i = 0; i < CCurveShow.MAX_CHAN; i++) {

            if (m_dynIntTime[i] && m_factorIntTime[i] > 0.03) {
                m_factorIntTime[i] *= 0.5f;

                // Call to update Int time
                float new_factor;
                new_factor = DynamicUpdateIntTime(m_factorIntTime[i], i);    // done here because we need to set int time before auto trigger happens.
                m_factorIntTime[i] = new_factor;
                m_dynIntTime[i] = false;
            }
            //存储下个循环使用的积分因子
            m_factorData[i][xhindex] = m_factorIntTime[i];

        }

        AnitoaLogUtil.writeToFile(DataFileUtil.getOrCreateFile("factor_log.txt"), "xhindex:" + xhindex + "\n");
        xhindex++;

    }


    private float DynamicUpdateIntTime(float factor, int chan) {
        switch (chan) {
            case 0:
                int_time1 = int_time_1;
                //int_time1 = (float)Math.round(int_time1 * factor);

                int_time1 = Float.parseFloat(String.format("%.2f", Math.floor(int_time1 * factor)));
                SetSensor(0, int_time1);
                return int_time1 / int_time_1;
            // break;
            case 1:
                int_time2 = int_time_2;
                //  int_time2 = (float)Math.round(int_time2 * factor);
                int_time2 = Float.parseFloat(String.format("%.2f",  Math.floor(int_time2 * factor)));
                SetSensor(1, int_time2);
                return int_time2 / int_time_2;
            //		break;

            case 2:
                int_time3 = int_time_3;
                // int_time3 = (float)Math.round(int_time3 * factor);
                int_time3 = Float.parseFloat(String.format("%.2f",  Math.floor(int_time3 * factor)));
                SetSensor(2, int_time3);
                return int_time3 / int_time_3;
            //		break;

            case 3:
                int_time4 = int_time_4;
                //int_time4 = (float)Math.round(int_time4 * factor);
                int_time4 = Float.parseFloat(String.format("%.2f",  Math.floor(int_time4 * factor)));
                SetSensor(3, int_time4);
                return int_time4 / int_time_4;
            //		break;

            default:
                return 1;
        }

//        return 0;
    }

    public String GetFactValueByXS(int n) {
        int chan = n - 1;
        int currCycle = xhindex - 1;
        double value = 5000 + m_factorData[chan][currCycle] * 10000;
        int v = (int) value;
        return v + "";
    }

    private byte[] SetSensor(int c, float InTime) {
        if(InTime<1){
            InTime=1;
        }
        PcrCommand cmd = new PcrCommand();
        cmd.setSensor(c);
        byte[] rev_bytes = mCommunicationService.sendPcrCommandSync(cmd);
        /*if (rev_bytes[0]>0)
        {
            SetIntergrationTime(InTime);
        }*/

        ThreadUtil.sleep(50);
        SetIntergrationTime(InTime);
        ThreadUtil.sleep(50);
        String txt = "通道" + (c + 1) + "积分时间：" + InTime;
        AnitoaLogUtil.writeFileLog(txt);
        return rev_bytes;
    }

    private byte[] SetIntergrationTime(float InTime) {
        PcrCommand cmd =  PcrCommand.ofIntergrationTime(InTime);
        return mCommunicationService.sendPcrCommandSync(cmd);
    }

    private void UpdatePCRCurve(int PCRNum, int pixelNum) {
        List<Integer> list = GetMaxValue(PCRNum);
        if (list.size() > 1) {
            int max = list.get(0);
            int last_max = list.get(1);
            boolean big = max + (max - last_max) > 3300;
            AnitoaLogUtil.writeFileLog("max + (max - last_max)=" + (max + (max - last_max)) + " > 3300:" + big);
            if (big) {
                m_dynIntTime[PCRNum - 1] = true;
            }
        }

    }

    private List<Integer> GetMaxValue(int PCRNum) {
        List<Integer> list = new ArrayList<>();
        String chip = "";
        switch (PCRNum) {
            case 1:
                chip = "Chip#1";
                break;
            case 2:
                chip = "Chip#2";
                break;
            case 3:
                chip = "Chip#3";
                break;
            case 4:
                chip = "Chip#4";
                break;
        }
        if (CommData.diclist.get(chip) != null && CommData.diclist.get(chip).size() > 0) {
            int n = CommData.diclist.get(chip).size() / CommData.imgFrame - 1;
            int max = GetValue(chip, n);
            list.add(max);
            int last_max = m_maxPixVal[PCRNum - 1];
            list.add(last_max);

            m_maxPixVal[PCRNum - 1] = max;
        }
        return list;
    }

    private int GetValue(String chip, int n) {
        try {
            List<Integer> listOne = new ArrayList<>();
          /*  List<String> strlist = CommData.diclist.get(chip)
                    .Skip(n * CommData.imgFrame).Take(CommData.imgFrame).ToList();*/
            List<String> strlist = CommData.diclist.get(chip)
                    .subList(n * CommData.imgFrame, n * CommData.imgFrame + CommData.imgFrame);
            for (int k = 0; k < strlist.size(); k++) {
                String[] datalis = strlist.get(k).split(" ");
                for (int j = 0; j < datalis.length; j++) {
                    if (j == 11 || j == 23) continue;
                    if (TextUtils.isEmpty(datalis[j])) continue;
                    listOne.add(Integer.parseInt(datalis[j]));
                }
            }
            int max = Collections.max(listOne, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.intValue() - o2.intValue();
                }
            });
            return max;
        } catch (Exception ex) {
            ex.printStackTrace();
            AnitoaLogUtil.writeFileLog(ex.getMessage());
            return 0;
        }

    }
}
