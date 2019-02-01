package com.jz.experiment.chart;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class DataFileReader {
    public static final int MAX_CHAN = 4;
    private static int MAX_CYCL = 61;
    boolean[] m_dynIntTime = new boolean[MAX_CHAN];
    float[] m_factorIntTime = new float[MAX_CHAN];
    int[] m_maxPixVal = new int[MAX_CHAN];
    double[][] m_factorData = new double[MAX_CHAN][100];
    public double[][] factorValue = new double[MAX_CHAN][MAX_CYCL];
    private static DataFileReader INSTANCE=new DataFileReader();
    public static DataFileReader getInstance(){
        return INSTANCE;
    }

    private DataFileReader(){
       // SetInitData();
    }




    public void ReadFileData(InputStream ips) {
        try {
            CommData.diclist.clear();
          //  InputStream ips = context.getAssets().open("fluorescence_data.txt");
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
            for (String item:CommData.diclist.keySet()){
                if (CommData.diclist.get(item).size() == 0) continue;
                CommData.Cycle = CommData.diclist.get(item).size() / CommData.imgFrame;

                int chan = GetChan(item);
                for (int i = 1; i <= CommData.Cycle; i++)
                {
                    //   int index=i-1;
                    int k = (i * 12) - 1;
                    String[] strs = CommData.diclist.get(item).get(k).split(" ");
                    int v=Integer.parseInt(strs[11]);
                    factorValue[chan][ i] = CommData.GetFactor(v);
                    if (i == 1) factorValue[chan][0] = factorValue[chan][i];
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    int xhindex = 1;

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


    public String GetFactValueByXS(int n)
    {
        int chan = n - 1;
        int currCycle = xhindex - 1;
        double value = 5000 + m_factorData[chan][currCycle] * 10000;
        int v= (int) value;
        return v+"";
    }


    public int GetChan(String chan)
    {
        int currChan = -1;

        switch (chan)
        {
            case "Chip#1":
                currChan = 0;
                break;
            case "Chip#2":
                currChan = 1;
                break;
            case "Chip#3":
                currChan = 2;
                break;
            case "Chip#4":
                currChan = 3;
                break;
        }
        return currChan;
    }

}
