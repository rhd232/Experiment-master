package com.jz.experiment.chart;

import android.text.TextUtils;

import com.anitoa.bean.FlashData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class DataFileReader {
    public static final int MAX_CHAN = 4;
    private static int MAX_CYCL = 400;// 61;

    public double[][] factorValue = new double[MAX_CHAN][MAX_CYCL];
    private static DataFileReader INSTANCE = new DataFileReader();

    public static DataFileReader getInstance() {
        return INSTANCE;
    }

    private DataFileReader() {
    }
    public void ReadFileData(InputStream ips, boolean running) {
        ReadFileData(ips,running,false);
    }

    public void ReadFileData(InputStream ips, boolean running,boolean ignoreDp) {
        try {
            CommData.diclist.clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ips));
            String line;
            String name = "";
            boolean dpheader = false;
            String dpstr = "";
            while ((line = reader.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) {
                    if (line.contains("Chipdp")) {
                        dpheader = true;
                    } else if (line.contains("Chip#")) {
                        name = line;
                        dpheader = false;
                        if (!CommData.diclist.keySet().contains(name)) {
                            CommData.diclist.put(name, new ArrayList<String>());
                        }
                    } else {
                        if (!dpheader) {
                            if (line.contains("Chip#")) continue;
                            CommData.diclist.get(name).add(line);
                        } else {
                            if (ignoreDp){
                                return;
                            }
                            //4.13 加入dpheader
                            if (dpheader) {
                                dpstr += line;

                                String[] s2 = dpstr.split(" ");
                                int len;
                                if (TextUtils.isEmpty(s2[s2.length-1])) {
                                    len = s2.length - 1;
                                } else {
                                    len = s2.length;
                                }


                                byte[] trim_buff = new byte[len];

                                for (int i = 0; i < len; i++) {

                                    //trim_buff[i] = Convert.ToByte(s2[i]);
                                    int b = Integer.parseInt(s2[i]);
                                    //TODO 需要验证这里的转化
                                    trim_buff[i] = (byte) (b & 0xFF);
                                }

                                List<Integer> rlist = new ArrayList<>();      // row index
                                List<Integer> clist = new ArrayList<>();      // col index

                                int k = 0;

                                char version = (char) trim_buff[k];
                                k++;
                                int sn1 = trim_buff[k];
                                k++;
                                int sn2 = trim_buff[k];
                                k++;

                                int num_channels = trim_buff[k];
                                k++;
                                int num_wells = trim_buff[k];
                                k++;
                                int num_pages = trim_buff[k];
                                k++;

                                // if (FlashData.row_index[0][0] == null && FlashData.col_index[0][0] == null)           // This means dp data not loaded yet from flash
                                //{
                                CommData.KsIndex = num_wells;
                                FlashData.flash_loaded = true;                                                   // treated the same as flash loaded.
                                FlashData.NUM_WELLS=num_wells;
                                FlashData.NUM_CHANNELS=num_channels;
                                for (int i = 0; i < num_channels; i++) {
                                    for (int j = 0; j < num_wells; j++) {
                                        int n = trim_buff[k];
                                        k++;
                                        rlist.clear();
                                        clist.clear();
                                        for (int l = 0; l < n; l++) {
                                            int row = trim_buff[k++]; // k++;
                                            int col = trim_buff[k];
                                            k++;

                                            rlist.add(row);
                                            clist.add(col);
                                        }
                                        FlashData.row_index[i][j] = new ArrayList<>(rlist);
                                        FlashData.col_index[i][j] = new ArrayList<>(clist);
                                    }
                                }
                                CommData.UpdateDarkMap();
                            }
                        }
                    }
                }

            }

           if (running) {
                return;
            }
            for (String item : CommData.diclist.keySet()) {
                if (CommData.diclist.get(item).size() == 0) continue;
                CommData.Cycle = CommData.diclist.get(item).size() / CommData.imgFrame;

                int chan = GetChan(item);
                for (int i = 1; i <= CommData.Cycle; i++) {
                    //   int index=i-1;
                    int k = (i * 12) - 1;
                    String[] strs = CommData.diclist.get(item).get(k).split(" ");
                    int v = Integer.parseInt(strs[11]);
                    factorValue[chan][i] = CommData.GetFactor(v);
                    if (i == 1) factorValue[chan][0] = factorValue[chan][i];
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public int GetChan(String chan) {
        int currChan = -1;

        switch (chan) {
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
