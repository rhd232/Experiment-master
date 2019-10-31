package com.jz.experiment.chart;

import android.content.Context;
import android.text.TextUtils;

import com.anitoa.bean.FlashData;
import com.anitoa.util.ByteUtil;
import com.anitoa.well.Well;
import com.wind.base.C;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommData {
    private final static boolean TwoByFour = true;
    public static boolean sTrimFromFile;
    public static int cboChan1 = 0;
    public static int cboChan2 = 0;
    public static int cboChan3 = 0;
    public static int cboChan4 = 0;

    public static int int_time1, int_time2, int_time3, int_time4 = 0;
    public static boolean isComplet = false;//实验是否完成
    public static String F_Path = "";//当前文件路径
    public static boolean isImport = false;
    public static int currCycleNum = 0;//当前循环数
    public static int CurrPZJD = 3;
    public static int KsIndex = 0;//孔数

    public static int sn1 = 0;
    public static int sn2 = 0;

    /**
     * 界面上可由用户输入，默认是0
     ****/
    public static double crossTalk21 = 0;
    public static double crossTalk12 = 0;

    public static int Cycle = 0;
    public static int imgFrame = 12;
    public static Map<String, List<String>> diclist = new HashMap<>();

    public static int currCycleState = 0;

    public static String currGuid = "";
    public static Map<String, List<String>> positionlist = new HashMap<>();

    //static int xdvalue = Convert.ToInt32(ConfigurationManager.AppSettings["xdvalue"].ToString());

    public static int gain_mode = 0;//1低gain 0高gain


    public static double[][] chan1_kb = new double[12][6];
    public static double[][] chan1_fpn = new double[2][12];
    public static double[] chan1_tempcal = new double[12];
    public static int chan1_rampgen = 0;
    public static int chan1_range = 0;
    public static int[] chan1_auto_v20 = new int[2];
    public static int chan1_auto_v15 = 0;

    public static double[][] chan2_kb = new double[12][6];
    public static double[][] chan2_fpn = new double[2][12];
    public static double[] chan2_tempcal = new double[12];
    public static int chan2_rampgen = 0;
    public static int chan2_range = 0;
    public static int[] chan2_auto_v20 = new int[2];
    public static int chan2_auto_v15 = 0;

    public static double[][] chan3_kb = new double[12][6];
    public static double[][] chan3_fpn = new double[2][12];
    public static double[] chan3_tempcal = new double[12];
    public static int chan3_rampgen = 0;
    public static int chan3_range = 0;
    public static int[] chan3_auto_v20 = new int[2];
    public static int chan3_auto_v15 = 0;

    public static double[][] chan4_kb = new double[12][6];
    public static double[][] chan4_fpn = new double[2][12];
    public static double[] chan4_tempcal = new double[12];
    public static int chan4_rampgen = 0;
    public static int chan4_range = 0;
    public static int[] chan4_auto_v20 = new int[2];
    public static int chan4_auto_v15 = 0;
    // public static double[][] m_factorData = new double[4][100];
    public static double[][] m_factorData = new double[4][400];
    public static int IFMet = 0;//0普通实验1溶解曲线

    public static boolean dpinfo_loaded = false;

    //暗像素集合
    public static int[][][] dark_map = new int[4][12][12];

    public static void ReadDatapositionFile(Context context) {
        if (positionlist.get("Chip#1") != null && !positionlist.get("Chip#1").isEmpty()) {
            return;
        }
        InputStream ips = null;
        BufferedReader reader = null;
        try {

            //查看sd卡目录下anitoa/trim文件夹下是否存在dataposition文件，优先读取该目录下文件
            File dataPositionFile = new File(C.Value.TRIM_FOLDER, "dataposition.ini");
            if (dataPositionFile.exists()) {
                ips = new FileInputStream(dataPositionFile);
            } else {
                return;
            }


            /*else {
                ips = context.getAssets().open("dataposition.ini");
            }*/

            //判断相应月份文件夹是否存在，没有则创建
            reader = new BufferedReader(new InputStreamReader(ips));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                if (!line.contains("CHIP")) {
                    if (line.contains("NWELLS")) {
                        String[] strs = line.split("=");
                        KsIndex = Integer.parseInt(strs[1]);
                        FlashData.NUM_WELLS = KsIndex;
                    }
                    if (line.contains("NCHANNELS")) {
                        String[] strs = line.split("=");
                        int numChannel = Integer.parseInt(strs[1]);
                        FlashData.NUM_CHANNELS = numChannel;
                    }
                    continue;
                }
                int startIndex = line.indexOf("=");//开始位置
                int ssindex = startIndex + 1;
                String str = line.substring(ssindex, line.length());//从开始位置截取一个新的字符串

                String str1 = line.substring(0, startIndex);
                switch (str1) {
                    case "CHIP1":
                        positionlist.put("Chip#1", GetPList(str));
                        break;
                    case "CHIP2":
                        positionlist.put("Chip#2", GetPList(str));
                        break;
                    case "CHIP3":
                        positionlist.put("Chip#3", GetPList(str));
                        break;
                    case "CHIP4":
                        positionlist.put("Chip#4", GetPList(str));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ips != null) {
                try {
                    ips.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static List<String> GetPList(String ss) {
        List<String> strlist;
        ss = ss.replace("A", "0-").replace("B", "1-")
                .replace("C", "2-").replace("D", "3-")
                .replace("E", "4-").replace("F", "5-")
                .replace("G", "6-").replace("H", "7-")
                .replace("I", "8-").replace("J", "9-")
                .replace("K", "10-").replace("L", "11-")
                .replace("M", "12-").replace("N", "13-")
                .replace("O", "14-").replace("P", "15-")
                .replace("Q", "16-").replace("R", "17-")
                .replace("S", "18-").replace("T", "19-")
                .replace("U", "20-").replace("V", "21-")
                .replace("W", "22-").replace("X", "23-");

        String[] sts = ss.split(",");
        strlist = Arrays.asList(sts);
        return strlist;
    }


    public static List<MeltChartData> GetChartDataByRJQX(String chan, int ks, String currks) {
        List<MeltChartData> cdlist = new ArrayList<>();
        try {
            if (diclist.get(chan) == null) {
                return cdlist;
            }
            int size = diclist.get(chan).size();
            int mode = size % imgFrame;//mode!=0说明存在丢数据
            int n = size / imgFrame;

            int ksindex = Well.getWell().getWellIndex(currks);

            if (ksindex == -1) {
                return cdlist;
            }
            int skipIndex = -1;
            for (int i = 0; i < n; i++) {

                List<String> strlist = diclist.get(chan).subList(i * imgFrame, i * imgFrame + imgFrame);
                //检测第一条数据是否正确，不正确则跳过
                //int lineNumber=Integer.parseInt(strlist.get(0).split(" ")[13]);


                if (strlist.size() == 0) continue;
                MeltChartData cd = new MeltChartData();
                String[] strs = strlist.get(0).split(" ");

                //为方便调试暂时注释掉的
                if (strs.length < imgFrame + 5) {
                    cd.x = "0.0";
                } else {

                    byte[] buffers = new byte[4];

                    buffers[0] = Byte.valueOf(strs[imgFrame + 1]);
                    buffers[1] = Byte.valueOf(strs[imgFrame + 2]);
                    buffers[2] = Byte.valueOf(strs[imgFrame + 3]);
                    buffers[3] = Byte.valueOf(strs[imgFrame + 4]);
                    //float t = BitConverter.ToSingle(buffers, 0);
                    float t = ByteUtil.getFloat(buffers);
                    cd.x = t + "";//横坐标为温度
                }


                //cd.x = i+"";//测试用//横坐标为循环次数


                Map<Integer, List<String>> datalist = new HashMap<>();
                //            Dictionary<int, List<string>> datalist = new Dictionary<int, List<string>>();
                for (int k = 0; k < strlist.size(); k++) {
                    List<String> list = Arrays.asList(strlist.get(k).split(" "));
                    datalist.put(k, list);
                    // datalist[k] = strlist[k].Split(' ').ToList();
                }
                int value = 0;
                int cindex = GetChanIndex(chan);

                if (FlashData.flash_loaded) {
                    int npoint = FlashData.row_index[cindex][ksindex].size();

                    for (int j = 0; j < npoint; j++) {
                        int row = FlashData.row_index[cindex][ksindex].get(j);
                        int col = FlashData.col_index[cindex][ksindex].get(j);
                        int val = Integer.parseInt(datalist.get(row).get(col));
                        int v = val - 100;
                        value += v;
                    }
                } else {

                    String ss = positionlist.get(chan).get(ksindex);
                    String[] newstrs = ss.split("\\+");

                    for (String item : newstrs) {
                        String[] nstrs = item.split("-");

                        if (nstrs.length > 1) {
                            int j = Integer.parseInt(nstrs[0]);
                            int k = Integer.parseInt(nstrs[1]);
                            int val = Integer.parseInt(datalist.get(k).get(j));
                            int v = val - 100;
                            value += v;
                        }
                    }
                }
                cd.y = value + "";
                cdlist.add(cd);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return cdlist;
    }

    public static int GetChanIndex(String chan) {
        int cindex = -1;

        switch (chan) {
            case "Chip#1":
                cindex = 0;
                break;
            case "Chip#2":
                cindex = 1;
                break;
            case "Chip#3":
                cindex = 2;
                break;
            case "Chip#4":
                cindex = 3;
                break;
            default:
                break;
        }
        return cindex;
    }

/*    public static List<ChartData> GetChartData(final String chan, int ks, String currks) {
        final List<ChartData> cdlist = new ArrayList<>();
        try {
            if (diclist.get(chan)==null){
                return cdlist;
            }

            int n = (diclist.get(chan).size() / imgFrame);
            //Log.e("ChartData","n:"+n);

            int ksindex=-1;
            if ("C0".equals(currks)) { //dark pixels
                ksindex=16;
            }else {
                ksindex = Well.getWell().getWellIndex(currks);
            }
            if (ksindex == -1) {
                return cdlist;
            }

            for (int i = 0; i < n; i++) {
                final ChartData cd = new ChartData();
                cd.x = i;

                List<String> strlist =diclist.get(chan).subList(i * imgFrame,i * imgFrame+imgFrame);

                Map<Integer, List<String>> datalist = new HashMap<>();
                for (int k = 0; k < strlist.size(); k++) {
                    List<String> list =Arrays.asList(strlist.get(k).split(" "));
                    datalist.put(k, list);

                }
                int factori=Integer.parseInt(datalist.get(11).get(11));
                int value = 0;
                int cindex = GetChanIndex(chan);

                if (ksindex == 16)  // dark
                {
                    int npoint = 11;
                    for (int j = 0; j < npoint; j++)
                    {
                        value += Integer.parseInt(datalist.get(11).get(j)) - 100;
                    }
                    *//*int npoint = 11;
                    boolean dark = true;
                    value = 0;
                    for (int j = 1; j < npoint; j++)
                    {
                        value += Integer.parseInt(datalist.get(j).get(11)) - 100;    // last column
                        if (dark_map[cindex][ j][ 11] > 0) {
                            dark = false;
                        }
                    }

                    if(!dark)
                    {
                        npoint = 11;
                        dark = true;
                        value = 0;
                        for (int j = 1; j < npoint; j++)
                        {
                            value += Integer.parseInt(datalist.get(j).get(0)) - 100;  // first column
                            if (dark_map[cindex][ j][ 0] > 0)
                            dark = false;
                        }
                        //Debug.Assert(dark);
                    }

                    value = value * 4 / (npoint - 1);   // normalize to 4 pixels*//*

                }
                else if (FlashData.flash_loaded){
                    int npoint = FlashData.row_index[cindex][ ksindex].size();

                    for (int j = 0; j < npoint; j++)
                    {
                        int row = FlashData.row_index[cindex][ ksindex].get(j);
                        int col = FlashData.col_index[cindex][ ksindex].get(j);
                        int val=Integer.parseInt(datalist.get(row).get(col));
                        int v = val - 100;
                        value += v;
                    }
                }else {

                    String ss = positionlist.get(chan).get(ksindex);
                    String[] newstrs = ss.split("\\+");

                    for (String item : newstrs) {
                        String[] nstrs = item.split("-");

                        if (nstrs.length > 1) {
                            int j = Integer.parseInt(nstrs[0]);
                            int k = Integer.parseInt(nstrs[1]);
                            //  int v = Integer.parseInt(datalist.get(k).get(j)) - 100;
                            int val = Integer.parseInt(datalist.get(k).get(j));
                            int v = val - 100;
                            value += v;
                        }
                    }
                }

                double vf=(double) value;
                vf/=GetFactor(factori);
                value=(int) vf;
                cd.y = value;
                cdlist.add(cd);
                if (i == 0) {
                    cdlist.add(cd);
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return cdlist;
    }*/

    // Zhimin: This one is used by RunOne and RunTwo. I added divide factor here.
    public static List<ChartData> GetChartData(String chan, int ks, String currks) {
        List<ChartData> cdlist = new ArrayList<ChartData>();
        if (diclist.size() == 0)
            return cdlist;
        if (!diclist.containsKey(chan))
            return cdlist;
        try {
            int n = GetCycleNum(chan);
            int ksindex = -1;
            if (KsIndex < 16) {
                if (TwoByFour) {
                    switch (currks) {
                        case "A1":
                            ksindex = 0;
                            break;
                        case "A2":
                            ksindex = 1;
                            break;
                        case "A3":
                            ksindex = 2;
                            break;
                        case "A4":
                            ksindex = 3;
                            break;
                        case "B1":
                            ksindex = 4;
                            break;
                        case "B2":
                            ksindex = 5;
                            break;
                        case "B3":
                            ksindex = 6;
                            break;
                        case "B4":
                            ksindex = 7;
                            break;
                        case "C0":           // dark pixes
                            ksindex = 16;
                            break;
                    }
                } else {
                    switch (currks) {
                        case "A1":
                            ksindex = 0;
                            break;
                        case "A2":
                            ksindex = 1;
                            break;
                        case "A3":
                            ksindex = 2;
                            break;
                        case "A4":
                            ksindex = 3;
                            break;
                        case "A5":
                            ksindex = 4;
                            break;
                        case "A6":
                            ksindex = 5;
                            break;
                        case "A7":
                            ksindex = 6;
                            break;
                        case "A8":
                            ksindex = 7;
                            break;
                        case "C0":              // dark pixes
                            ksindex = 16;
                            break;
                    }
                }
            } else {
                switch (currks) {
                    case "A1":
                        ksindex = 0;
                        break;
                    case "A2":
                        ksindex = 1;
                        break;
                    case "A3":
                        ksindex = 2;
                        break;
                    case "A4":
                        ksindex = 3;
                        break;
                    case "A5":
                        ksindex = 4;
                        break;
                    case "A6":
                        ksindex = 5;
                        break;
                    case "A7":
                        ksindex = 6;
                        break;
                    case "A8":
                        ksindex = 7;
                        break;
                    case "B1":
                        ksindex = 8;
                        break;
                    case "B2":
                        ksindex = 9;
                        break;
                    case "B3":
                        ksindex = 10;
                        break;
                    case "B4":
                        ksindex = 11;
                        break;
                    case "B5":
                        ksindex = 12;
                        break;
                    case "B6":
                        ksindex = 13;
                        break;
                    case "B7":
                        ksindex = 14;
                        break;
                    case "B8":
                        ksindex = 15;
                        break;
                    case "C0":              // dark pixes
                        ksindex = 16;
                        break;
                }
            }
            if (ksindex == -1) {
                return cdlist;
            }
            int cindex = -1;
            switch (chan) {
                case "Chip#1":
                    cindex = 0;
                    break;
                case "Chip#2":
                    cindex = 1;
                    break;
                case "Chip#3":
                    cindex = 2;
                    break;
                case "Chip#4":
                    cindex = 3;
                    break;
                default:
                    break;
            }
            int skip = 0;
            for (int i = 0; i < n; i++) {      // n is number of image blocks
                ChartData cd = new ChartData();
                cd.x = i;

                int skip_boundary = i * imgFrame - skip;
                int take_count = imgFrame;

                if (skip_boundary + imgFrame >= diclist.get(chan).size()) {
                    take_count -= skip_boundary + imgFrame - diclist.get(chan).size();

                    if (take_count < 0) {
                        take_count = 0;
                        continue;
                    }
                }
//                    List<String> strlist = diclist[chan].Skip(skip_boundary).Take(imgFrame).ToList();
                List<String> strlist = diclist.get(chan).subList(i * imgFrame, i * imgFrame + imgFrame);
//                    Dictionary<int, List<String>> datalist = new Dictionary<int, List<String>>();
                Map<Integer, List<String>> datalist = new HashMap<>();
                char[] charSeparator = new char[]{' '};

                for (int k = 0; k < strlist.size(); k++) {
                    List<String> list = Arrays.asList(strlist.get(k).split(" "));
                    datalist.put(k, list);
//                        datalist[k] = strlist[k].Split(charSeparator, StringSplitOptions.RemoveEmptyEntries).ToList();
                }
                int rn = Integer.parseInt(datalist.get(11).get(12));
                int factori = Integer.parseInt(datalist.get(11).get(11));
                if (rn != 11 && rn < 12) {
                    skip += rn + 1;
                } else if (rn > 12) {
                    cd.y = 0;
                    cdlist.add(cd);
                    continue;
                }

                // Use flash version of data position index
                int value = 0;

                if (ksindex == 16)  // dark
                {
                    int npoint = 11;
                    boolean dark = true;
                    value = 0;
                    for (int j = 1; j < npoint; j++) {
                        value += Integer.parseInt(datalist.get(j).get(11)) - 100;    // last column
                        if (dark_map[cindex][j][11] > 0)
                            dark = false;
                    }

                    if (!dark) {
                        npoint = 11;
                        dark = true;
                        value = 0;
                        for (int j = 1; j < npoint; j++) {
                            value += Integer.parseInt(datalist.get(j).get(0)) - 100;  // first column
                            if (dark_map[cindex][j][0] > 0)
                                dark = false;
                        }
//                            Debug.Assert(dark);
                    }

                    value = value * 4 / (npoint - 1);   // normalize to 4 pixels

                } else if (FlashData.flash_loaded || dpinfo_loaded) {
                    int npoint = FlashData.row_index[cindex][ksindex].size();

                    for (int j = 0; j < npoint; j++) {
                        int row = FlashData.row_index[cindex][ksindex].get(j);
                        int col = FlashData.col_index[cindex][ksindex].get(j);
                        value += Integer.parseInt(datalist.get(row).get(col)) - 100;
                    }
                } else {// Use Data position file version
                    if (positionlist.size() == 0) {
                        return cdlist;
                    }
                    if (!positionlist.containsKey(chan)) {
                        return cdlist;
                    }
                    String ss = positionlist.get(chan).get(ksindex);
                    String[] newstrs = ss.split("\\+");
/*                        foreach (var item in newstrs)
                        {
                            String[] nstrs = item.Split('-');

                            if (nstrs.length > 1)
                            {
                                int j = Integer.parseInt(nstrs[0]);
                                int k = Integer.parseInt(nstrs[1]);
                                int v = Integer.parseInt(datalist.get(k).get(j)) - 100;
                                value += v;
                            }
                        }*/

                    for (String item : newstrs) {
                        String[] nstrs = item.split("-");

                        if (nstrs.length > 1) {
                            int j = Integer.parseInt(nstrs[0]);
                            int k = Integer.parseInt(nstrs[1]);
                            int v = Integer.parseInt(datalist.get(k).get(j)) - 100;
                            value += v;
                        }
                    }
                }

                double vf = (double) value;
                vf /= GetFactor(factori);
                value = (int) vf;

                cd.y = value;
                cdlist.add(cd);
                if (i == 0)
                    cdlist.add(cd); // Zhimin: index 0 take value from index 1
            }
        } catch (Exception ex) {
//            MessageBox.Show(ex.Message + "Get Chart Data CommData");
        }
        return cdlist;
    }

    public static int GetCycleNum(String td)        // "td" stands for TongDao, or Channel in English
    {

        if (diclist.size() == 0 || !diclist.containsKey(td))
            return 0;
        // cyclenum = Convert.ToInt32(CommData.diclist[tdlist[i]].Count / CommData.imgFrame);
        int l, m, n;
        l = diclist.get(td).size();
        m = imgFrame;

        if (l % m != 0) {
            n = l / m + 1; //  (int)Math.Round(Convert.ToDouble(l / m)) + 1;
        } else {
            n = l / m;
        }
        return n;
        //====================================================
    }

    public static double GetFactor(int value) {
        double factor;
        if (value < 5000) {
            factor = 1;
        } else {
            factor = (((double) value - 5000) / 10000d);
        }
        return factor;
    }


    public static List<ChartData> GetMaxChartData(String chan, int ks, String currks) {
        List<ChartData> cdlist = new ArrayList<>();
        try {
            int n = (diclist.get(chan).size() / imgFrame);
            //if (ks == 4)
            //{
            int ksindex = Well.getWell().getWellIndex(currks);
            if (ksindex == -1) {
                return cdlist;
            }

            int cindex = -1;

            switch (chan) {
                case "Chip#1":
                    cindex = 0;
                    break;
                case "Chip#2":
                    cindex = 1;
                    break;
                case "Chip#3":
                    cindex = 2;
                    break;
                case "Chip#4":
                    cindex = 3;
                    break;
                default:
                    break;
            }

            for (int i = 0; i < n; i++) {
                ChartData cd = new ChartData();
                cd.x = i;
                List<String> strlist = diclist.get(chan).subList(i * imgFrame, i * imgFrame + imgFrame);
                //List<String> strlist = diclist.get(chan).Skip(i * imgFrame).Take(imgFrame).ToList();
                Map<Integer, List<String>> datalist = new HashMap<>();
                for (int k = 0; k < strlist.size(); k++) {
                    List<String> list = Arrays.asList(strlist.get(k).split(" "));
                    datalist.put(k, list);

                }

                //===========Use flash version of data position index============
                int value = 0;

                if (FlashData.flash_loaded) {
                    int npoint = FlashData.row_index[cindex][ksindex].size();

                    for (int j = 0; j < npoint; j++) {
                        int row = FlashData.row_index[cindex][ksindex].get(j);
                        int col = FlashData.col_index[cindex][ksindex].get(j);
                        int val = Integer.parseInt(datalist.get(row).get(col));
                        int v = val - 100;
                        if (v < 0) {
                            v = 0;
                        }
                        value += v;
                    }
                } else {

                    String ss = positionlist.get(chan).get(ksindex);
                    String[] newstrs = ss.split("\\+");

                    for (String item : newstrs) {
                        String[] nstrs = item.split("-");

                        if (nstrs.length > 1) {
                            int j = Integer.parseInt(nstrs[0]);
                            int k = Integer.parseInt(nstrs[1]);
                            //  int v = Integer.parseInt(datalist.get(k).get(j)) - 100;
                            int val = Integer.parseInt(datalist.get(k).get(j));
                            int v = val - 100;
                            if (v < 0) {
                                v = 0;
                            }
                            value += v;
                        }
                    }
                }

                cd.y = value;
                cdlist.add(cd);
            }

        } catch (Exception ex) {

        }
        return cdlist;
    }


    public static void UpdateDarkMap() {
        for (int i = 0; i < FlashData.NUM_CHANNELS; i++) {
            for (int j = 0; j < 12; j++) {
                for (int k = 0; k < 12; k++) {
                    dark_map[i][j][k] = 0;
                }
            }
        }

        for (int i = 0; i < FlashData.NUM_CHANNELS; i++) {
            for (int j = 0; j < FlashData.NUM_WELLS; j++) {
                int npoint = FlashData.row_index[i][j].size();

                for (int k = 0; k < npoint; k++) {
                    int row = FlashData.row_index[i][j].get(k);
                    int col = FlashData.col_index[i][j].get(k);
                    dark_map[i][row][col] += 1;
                }
            }
        }
    }
}