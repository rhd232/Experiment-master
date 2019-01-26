package com.jz.experiment.chart;

import java.util.ArrayList;
import java.util.List;

public class CurveReader {

    public double[][][] m_zData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][ CCurveShow.MAX_CYCL];
    public double[][] m_CTValue = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL];

    private static CurveReader INSTANCE=new CurveReader();
    public static CurveReader getInstance(){
        return INSTANCE;
    }
    public void readCurve(){
        double[][][]  m_yData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][CCurveShow.MAX_CYCL];
        CCurveShow cCurveShow = new CCurveShow();
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
        if (CommData.cboChan1 == 1)
        {
            tdlist.add("Chip#1");
        }
        if (CommData.cboChan2 == 1)
        {
            tdlist.add("Chip#2");
        }
        if (CommData.cboChan3 == 1)
        {
            tdlist.add("Chip#3");
        }
        if (CommData.cboChan4 == 1)
        {
            tdlist.add("Chip#4");
        }


        int cyclenum = 0;
        for (int i = 0; i < tdlist.size(); i++)
        {
            for (int n = 0; n < kslist.size(); n++)
            {
                List<ChartData> cdlist = CommData.GetChartData(tdlist.get(i), 0, kslist.get(n));//获取选点值
                for (int k = 0; k < cdlist.size(); k++)
                {
                    m_yData[i][ n][ k] = cdlist.get(k).y / CommData.m_factorData[GetChan(tdlist.get(i))][k];
                }
            }
            if (CommData.diclist.size() > 0)
            {
                cyclenum = (CommData.diclist.get(tdlist.get(i)).size() / CommData.imgFrame);
            }
        }

        cCurveShow.m_yData = m_yData;
        cCurveShow.m_Size = cyclenum;
        cCurveShow.ifactor = CommData.m_factorData;
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
