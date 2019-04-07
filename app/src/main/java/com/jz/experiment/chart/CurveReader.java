package com.jz.experiment.chart;

import com.jz.experiment.device.Well;

import java.util.ArrayList;
import java.util.List;

public class CurveReader {

    public double[][][] m_zData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][ CCurveShow.MAX_CYCL];
    public double[][] m_CTValue = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL];
    private CurveReader(){}
    private static CurveReader INSTANCE=new CurveReader();
    public static CurveReader getInstance(){
        return INSTANCE;
    }
    public void readCurve(/*List<String> tdlist,List<String> kslist*/double[][] factorValues){
        double[][][]  m_yData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][CCurveShow.MAX_CYCL];
        CCurveShow cCurveShow =  CCurveShow.getInstance();
        cCurveShow.InitData();
        //List<String> kslist = new ArrayList<>();//定义孔数
        List<String> kslist=Well.getWell().getKsList();

        List<String> tdlist = new ArrayList<>();//定义通道
        tdlist.add("Chip#1");
        tdlist.add("Chip#2");
        tdlist.add("Chip#3");
        tdlist.add("Chip#4");

        //File file= new File(C.Value.IMAGE_DATA,"ydata_no_factor.txt");
        int cyclenum = 0;
        for (int i = 0; i < tdlist.size(); i++)
        {
            for (int n = 0; n < kslist.size(); n++)
            {
                List<ChartData> cdlist = CommData.GetChartData(tdlist.get(i), 0, kslist.get(n));//获取选点值
                for (int k = 0; k < cdlist.size(); k++)
                {


                    double factorValue=factorValues[GetChan(tdlist.get(i))][k];
                    m_yData[i][n][k] = cdlist.get(k).y /factorValue;
                   // m_yData[i][n][k] = cdlist.get(k).y ;
                    //String t=i+"-"+n+"-"+k+"="+ cdlist.get(k).y;
                    //AppendToFile(t,file);
                }
            }
            if (CommData.diclist.size() > 0 && CommData.diclist.get(tdlist.get(i))!=null)
            {
                cyclenum = (CommData.diclist.get(tdlist.get(i)).size() / CommData.imgFrame);
            }
        }

        cCurveShow.m_yData = m_yData;
        cCurveShow.m_Size = cyclenum+1;
        cCurveShow.ifactor = factorValues;
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




}
