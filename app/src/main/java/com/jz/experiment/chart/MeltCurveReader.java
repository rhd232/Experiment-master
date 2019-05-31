package com.jz.experiment.chart;

import com.anitoa.well.Well;

import java.util.ArrayList;
import java.util.List;

public class MeltCurveReader {

    public double[][][] m_zData = new double[CCurveShowMet.MAX_CHAN][CCurveShowMet.MAX_WELL][ CCurveShowMet.MAX_CYCL];
    public double[][] m_CTValue = new double[CCurveShowMet.MAX_CHAN][CCurveShowMet.MAX_WELL];
    public double[][][] m_zdData = new double[CCurveShowMet.MAX_CHAN][ CCurveShowMet.MAX_WELL][CCurveShowMet.MAX_CYCL];
    private MeltCurveReader(){}
    private static MeltCurveReader INSTANCE=new MeltCurveReader();
    public static MeltCurveReader getInstance(){
        return INSTANCE;
    }
    public void readCurve(double[][] factorValues){

        double [][][]m_yData = new double[CCurveShowMet.MAX_CHAN][CCurveShowMet.MAX_WELL][ CCurveShowMet.MAX_CYCL];
        CCurveShowMet cCurveShowMet = CCurveShowMet.getInstance();
        cCurveShowMet.InitData();
       // List<String> kslist;//定义孔数

        List<String> kslist=Well.getWell().getKsList();

        List<String> tdlist = new ArrayList<>();//定义通道
        tdlist.add("Chip#1");
        tdlist.add("Chip#2");
        tdlist.add("Chip#3");
        tdlist.add("Chip#4");

        double[][] mtemp = new double[CCurveShowMet.MAX_CHAN][ CCurveShowMet.MAX_CYCL];
        int cyclenum = 0;
        for (int i = 0; i < tdlist.size(); i++)
        {
            for (int n = 0; n < kslist.size(); n++)
            {


                List<MeltChartData> cdlist = CommData.GetChartDataByRJQX(tdlist.get(i), 0, kslist.get(n));

                for (int k = 0; k < cdlist.size(); k++)
                {
                    mtemp[i][ k] = Double.parseDouble(cdlist.get(k).x);

                    double factorValue=factorValues[GetChan(tdlist.get(i))][k];
                    m_yData[i][ n][ k] = Double.parseDouble(cdlist.get(k).y) /factorValue;
                }
            }
            if (CommData.diclist.size() > 0&& CommData.diclist.get(tdlist.get(i))!=null)
            {
                cyclenum = (CommData.diclist.get(tdlist.get(i)).size() / CommData.imgFrame);
            }
        }

        int[] m_Size = new int[CCurveShowMet.MAX_CHAN];
        for (int n = 0; n < tdlist.size(); n++)
        {
            m_Size[n] = cyclenum;
        }

        try
        {
            cCurveShowMet.m_yData = m_yData;
            cCurveShowMet.m_Size = m_Size;
            cCurveShowMet.mtemp = mtemp;
            cCurveShowMet.ifactor = factorValues;
            cCurveShowMet.UpdateAllcurve();
            m_zData = cCurveShowMet.m_zData;
            m_zdData = cCurveShowMet.m_zdData;
            m_CTValue = cCurveShowMet.m_CTValue;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
