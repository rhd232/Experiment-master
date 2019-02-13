package com.jz.experiment.chart;

import android.text.TextUtils;

import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.module.bluetooth.UsbService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FactUpdater {


    boolean [] m_dynIntTime = new boolean[CCurveShow.MAX_CHAN];
    float[] m_factorIntTime = new float[CCurveShow.MAX_CHAN];
    int[] m_maxPixVal = new int[CCurveShow.MAX_CHAN];
    double[][] m_factorData = new double[CCurveShow.MAX_CHAN][ 100];
    private UsbService mUsbService;
    private static FactUpdater sInstance;
    private DecimalFormat mDecimalFormat;
    public static FactUpdater getInstance(UsbService service){
        if (sInstance==null){
            synchronized (FactUpdater.class){
                if (sInstance==null){
                    sInstance=new FactUpdater(service);
                }
            }
        }
        return sInstance;
    }
    private  FactUpdater(UsbService service){
        this.mUsbService=service;
        mDecimalFormat=new DecimalFormat("#.00");
    }


    public void updateFact(){

        for (int i = 1; i <= CCurveShow.MAX_CHAN; i++)
        {
            UpdatePCRCurve(i, 0);
        }

        DynamicUpdateIntTime();

        CommData.m_factorData = m_factorData;
    }
    /// <summary>
    /// 初始化值
    /// </summary>
    public void SetInitData()
    {
        for (int i = 0; i < CCurveShow.MAX_CHAN; i++)
        {
            m_dynIntTime[i] = false;
            m_factorIntTime[i] = (float)1.0;
            m_maxPixVal[i] = 100;

            for (int n = 0; n < 100; n++)
            {
                m_factorData[i][n] = 1;
            }
        }

        xhindex=1;
        int_time1 = 1;
        int_time2 = 1;
        int_time3 = 1;
        int_time4 = 1;

        int_time_1=20;//默认积分时间20ms
        int_time_2=20;
        int_time_3=20;
        int_time_4=20;
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
    private void DynamicUpdateIntTime()
    {

        for (int i = 0; i < CCurveShow.MAX_CHAN; i++)
        {

            //		if (m_factorInt[i].empty()) {
            //			m_factorInt[i].push_back(m_factorIntTime[i]);    // First time push twice
            //		}
            //		m_factorInt[i].push_back(m_factorIntTime[i]);

            if (m_dynIntTime[i] && m_factorIntTime[i] > 0.03)
            {
                m_factorIntTime[i] *= (float)0.5;

                // Call to update Int time
                float new_factor;
                new_factor = DynamicUpdateIntTime(m_factorIntTime[i], i);	// done here because we need to set int time before auto trigger happens.
                m_factorIntTime[i] = new_factor;
                m_dynIntTime[i] = false;
            }

            m_factorData[i][xhindex] = m_factorIntTime[i];
        }

        xhindex++;

    }


    private float DynamicUpdateIntTime(float factor, int chan)
    {
        switch (chan)
        {
            case 0:
                int_time1 = int_time_1;
                //int_time1 = (float)Math.round(int_time1 * factor);
                  int_time1 = Float.parseFloat(String.format("%.2f", int_time1 * factor));
                  SetSensor(0, int_time1);
                return int_time1 /int_time_1;
            // break;
            case 1:
                int_time2 = int_time_2;
              //  int_time2 = (float)Math.round(int_time2 * factor);
                int_time2 = Float.parseFloat(String.format("%.2f", int_time2 * factor));
                SetSensor(1, int_time2);
                return int_time2 / int_time_2;
            //		break;

            case 2:
                int_time3 = int_time_3;
               // int_time3 = (float)Math.round(int_time3 * factor);
                int_time3 = Float.parseFloat(String.format("%.2f", int_time3 * factor));
                SetSensor(2, int_time3);
                return int_time3 / int_time_3;
            //		break;

            case 3:
                int_time4 = int_time_4;
                //int_time4 = (float)Math.round(int_time4 * factor);
                int_time4 = Float.parseFloat(String.format("%.2f", int_time4 * factor));
                SetSensor(3, int_time4);
                return int_time4 / int_time_4;
            //		break;

            default:
                return 1;
        }

//        return 0;
    }
    public String GetFactValueByXS(int n)
    {
        int chan = n - 1;
        int currCycle = xhindex - 1;
        double value = 5000 + m_factorData[chan][currCycle] * 10000;
        int v= (int) value;
        return v+"";
    }
    private byte[] SetSensor(int c, float InTime)
    {

        PcrCommand cmd=new PcrCommand();
        cmd.setSensor(c);
        byte[] rev_bytes=mUsbService.sendPcrCommandSync(cmd);
        if (rev_bytes[0]>0)
        {
            SetIntergrationTime(InTime);
        }
        return rev_bytes;
    }
    private byte[] SetIntergrationTime(float InTime)
    {
        PcrCommand cmd=new PcrCommand();
        cmd.setIntergrationTime(InTime);

        return mUsbService.sendPcrCommandSync(cmd);
    }
    private void UpdatePCRCurve(int PCRNum, int pixelNum)
    {
        List<Integer> list = GetMaxValue(PCRNum);
        if (list.size() > 1)
        {
            int max = list.get(0);
            int last_max = list.get(1);
            if (max + (max - last_max) > 3300)
            {
                m_dynIntTime[PCRNum - 1] = true;
            }
        }

    }
    private List<Integer> GetMaxValue(int PCRNum)
    {
        List<Integer> list = new ArrayList<>();
        String chip = "";
        switch (PCRNum)
        {
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
        if (CommData.diclist.get(chip) != null && CommData.diclist.get(chip).size() > 0)
        {
            int n = CommData.diclist.get(chip).size() / CommData.imgFrame - 1;
            int max = GetValue(chip, n);
            list.add(max);
            int last_max = m_maxPixVal[PCRNum - 1];
            list.add(last_max);

            m_maxPixVal[PCRNum - 1] = max;
        }
        return list;
    }

    private int GetValue(String chip, int n)
    {
        try
        {
            List<Integer> listOne = new ArrayList<>();
          /*  List<String> strlist = CommData.diclist.get(chip)
                    .Skip(n * CommData.imgFrame).Take(CommData.imgFrame).ToList();*/
            List<String> strlist = CommData.diclist.get(chip).subList(n * CommData.imgFrame,n * CommData.imgFrame+CommData.imgFrame);
            for (int k = 0; k < strlist.size(); k++)
            {
                String[] datalis = strlist.get(k).split(" ");
                for (int j = 0; j < datalis.length; j++)
                {
                    if (j == 11 || j == 23) continue;
                    if (TextUtils.isEmpty(datalis[j])) continue;
                    listOne.add(Integer.parseInt(datalis[j]));
                }
            }
            int max =Collections.max(listOne, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.intValue()-o2.intValue();
                }
            });
            return max;
        }
        catch (Exception ex)
        {
            return 0;
        }

    }
}
