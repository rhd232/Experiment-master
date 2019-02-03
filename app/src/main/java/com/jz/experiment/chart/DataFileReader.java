package com.jz.experiment.chart;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class DataFileReader {
    public static final int MAX_CHAN = 4;
    private static int MAX_CYCL = 61;

    public double[][] factorValue = new double[MAX_CHAN][MAX_CYCL];
    private static DataFileReader INSTANCE=new DataFileReader();
    public static DataFileReader getInstance(){
        return INSTANCE;
    }

    private DataFileReader(){
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
