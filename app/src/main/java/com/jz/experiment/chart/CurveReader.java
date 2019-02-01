package com.jz.experiment.chart;

import com.wind.base.C;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CurveReader {

    public double[][][] m_zData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][ CCurveShow.MAX_CYCL];
    public double[][] m_CTValue = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL];

    private static CurveReader INSTANCE=new CurveReader();
    public static CurveReader getInstance(){
        return INSTANCE;
    }
    public void readCurve(/*List<String> tdlist,List<String> kslist*/){
        double[][][]  m_yData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][CCurveShow.MAX_CYCL];
        CCurveShow cCurveShow =  CCurveShow.getInstance();
        cCurveShow.InitData();
        List<String> kslist = new ArrayList<>();//定义孔数
        kslist.add("A1");
        kslist.add("A2");
        kslist.add("A3");
        kslist.add("A4");
        kslist.add("B1");
        kslist.add("B2");
        kslist.add("B3");
        kslist.add("B4");

        List<String> tdlist = new ArrayList<>();//定义通道
        tdlist.add("Chip#1");
        tdlist.add("Chip#2");
        tdlist.add("Chip#3");
        tdlist.add("Chip#4");

        File file= new File(C.Value.IMAGE_DATA,"ydata_no_factor.txt");
        int cyclenum = 0;
        for (int i = 0; i < tdlist.size(); i++)
        {
            for (int n = 0; n < kslist.size(); n++)
            {
                List<ChartData> cdlist = CommData.GetChartData(tdlist.get(i), 0, kslist.get(n));//获取选点值
                for (int k = 0; k < cdlist.size(); k++)
                {
                    double [][] factorValues=DataFileReader.getInstance().factorValue;
                    double factorValue=factorValues[GetChan(tdlist.get(i))][k];
                    m_yData[i][n][k] = cdlist.get(k).y /factorValue;
                    String t=i+"-"+n+"-"+k+"="+ cdlist.get(k).y;
                    AppendToFile(t,file);
                }
            }
            if (CommData.diclist.size() > 0)
            {
                cyclenum = (CommData.diclist.get(tdlist.get(i)).size() / CommData.imgFrame);
            }
        }

        cCurveShow.m_yData = m_yData;
        cCurveShow.m_Size = cyclenum+1;
        cCurveShow.ifactor = DataFileReader.getInstance().factorValue;
        cCurveShow.UpdateAllcurve();

        m_zData = cCurveShow.m_zData;
        m_CTValue = cCurveShow.m_CTValue;
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


    private void AppendToFile(String txt, File file) {

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!file.exists()) {
                boolean hasFile = file.createNewFile();
               /* if (hasFile) {
                    System.out.println("file not exists, create new file");
                }*/
                fos = new FileOutputStream(file);
            } else {
                // System.out.println("file exists");
                fos = new FileOutputStream(file, true);
            }

            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(txt); //写入内容
            osw.write("\r\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        } finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
