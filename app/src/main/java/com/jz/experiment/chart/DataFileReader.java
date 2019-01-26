package com.jz.experiment.chart;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DataFileReader {
    public static final int MAX_CHAN = 4;
    boolean[] m_dynIntTime = new boolean[MAX_CHAN];
    float[] m_factorIntTime = new float[MAX_CHAN];
    int[] m_maxPixVal = new int[MAX_CHAN];
    double[][] m_factorData = new double[MAX_CHAN][100];

    private static DataFileReader INSTANCE=new DataFileReader();
    public static DataFileReader getInstance(){
        return INSTANCE;
    }

    public DataFileReader(){
        SetInitData();
    }
    public void SetInitData()
    {
        for (int i = 0; i < MAX_CHAN; i++)
        {
            m_dynIntTime[i] = false;
            m_factorIntTime[i] = (float)1.0;
            m_maxPixVal[i] = 100;

            for (int n = 0; n < 100; n++)
            {
                m_factorData[i][ n] = 1;
            }
        }


    }
    public void UpdatePCRCurve(int PCRNum, int pixelNum) {
        List<Integer> list = GetMaxValue(PCRNum);
        if (list.size() > 1) {
            int max = list.get(0);
            int last_max = list.get(1);
            if (max + (max - last_max) > 3300) {
                m_dynIntTime[PCRNum - 1] = true;
            }
        }

    }

    public List<Integer> GetMaxValue(int PCRNum) {
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
            int n = (CommData.diclist.get(chip).size() / CommData.imgFrame) - 1;
            int max = GetValue(chip, n);
            list.add(max);
            int last_max = m_maxPixVal[PCRNum - 1];
            list.add(last_max);

            m_maxPixVal[PCRNum - 1] = max;
        }
        return list;
    }

    public int GetValue(final String chip, final int n) {
        try {
            final List<Integer> listOne = new ArrayList<>();

          /*  List<String> strlist = Observable.from(CommData.diclist.get(chip))
                    .toList().skip(n * CommData.imgFrame)
                    .take(CommData.imgFrame).toBlocking().first();*/

            List<String> strlist =CommData.diclist.get(chip).subList(n * CommData.imgFrame,n * CommData.imgFrame+CommData.imgFrame);
            for (int k = 0; k < strlist.size(); k++) {
                String[] datalis = strlist.get(k).split(" ");
                for (int j = 0; j < datalis.length; j++) {
                    if (j == 11 || j == 23) continue;
                    if (TextUtils.isEmpty(datalis[j])) continue;
                    listOne.add(Integer.parseInt(datalis[j]));
                }
            }

            Collections.sort(listOne, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.intValue() - o2.intValue();
                }
            });
            int max = listOne.get(listOne.size() - 1);
            return max;

        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

    }

    public void ReadAssetsFileData(Context context) {
        try {
            InputStream ips = context.getAssets().open("fluorescence_data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(ips));
            String line;
            String name = "";
            while ((line = reader.readLine()) != null) {
                if (!TextUtils.isEmpty(line)){
                    if (line.contains("Chip#")) {
                        name = line;
                        if (!CommData.diclist.keySet().contains(name)) {
                            CommData.diclist.put(name, new ArrayList<String>());
                        }
                    } else {
                        if (line.contains("Chip#")) continue;
                        CommData.diclist.get(name).add(line);
                    }
                }

            }

            for (int i = 1; i <= MAX_CHAN; i++) {
                UpdatePCRCurve(i, 0);
            }

            DynamicUpdateIntTime();
            CommData.m_factorData = m_factorData;

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    int xhindex = 1;
    public void DynamicUpdateIntTime()
    {

        for (int i = 0; i < MAX_CHAN; i++)
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
    float int_time1 = 1;
    float int_time2 = 1;
    float int_time3 = 1;
    float int_time4 = 1;

    float int_time_1 = 1;
    float int_time_2 = 1;
    float int_time_3 = 1;
    float int_time_4 = 1;
    public float DynamicUpdateIntTime(float factor, int chan)
    {
        switch (chan)
        {
            case 0:
                int_time1 = int_time_1;
                int_time1 = (float)Math.round(int_time1 * factor);
              //  SetSensor(1, int_time1);
                return int_time1 /int_time_1;
            // break;

            case 1:
                int_time2 = int_time_2;
                int_time2 = (float)Math.round(int_time2 * factor);
                //SetSensor(2, int_time2);
                return int_time2 / int_time_2;
            //		break;

            case 2:
                int_time3 = int_time_3;
                int_time3 = (float)Math.round(int_time3 * factor);
              //  SetSensor(3, int_time3);
                return int_time3 / int_time_3;
            //		break;

            case 3:
                int_time4 = int_time_4;
                int_time4 = (float)Math.round(int_time4 * factor);
               // SetSensor(4, int_time4);
                return int_time4 / int_time_4;
            //		break;

            default:
                return 1;
        }

//        return 0;
    }

}
