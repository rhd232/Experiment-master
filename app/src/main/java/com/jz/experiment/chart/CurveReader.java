package com.jz.experiment.chart;

import com.anitoa.well.Well;
import com.jz.experiment.widget.CtParamInputLayout;

import java.util.ArrayList;
import java.util.List;

public class CurveReader {

    //public double[][][] m_zData = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL][ CCurveShow.MAX_CYCL];
    //public double[][] m_CTValue = new double[CCurveShow.MAX_CHAN][CCurveShow.MAX_WELL];
    public CurveReader(){}
   /* private static CurveReader INSTANCE=new CurveReader();
    public static CurveReader getInstance(){
        return INSTANCE;
    }*/
    public double[][] m_bData = new double[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_CYCL];
    public DtChart.DtData readCurve(/*double[][] factorValues,*/ CtParamInputLayout.CtParam ctParam, boolean norm){
        double[][][]  m_yData = new double[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL][CCurveShowPolyFit.MAX_CYCL];
        CCurveShowPolyFit cCurveShow =  new CCurveShowPolyFit();

        int minCt=CtParamInputLayout.DEFALUT_MIN_CT;
        int threshold=CtParamInputLayout.DEFALUT_THRESHHOLD_CT;
        if (ctParam!=null){
            minCt=ctParam.ctMin;
            threshold=ctParam.ctThreshhold;
            if (minCt < 5) minCt = 5;
            else if (minCt > 18) minCt = 25;

            if (threshold < 5) threshold = 5;
            else if (threshold > 50) threshold = 50;
        }
        cCurveShow.log_threshold[0] = (float)(threshold * 0.01);
        cCurveShow.log_threshold[1] = (float)(threshold * 0.01);
        cCurveShow.log_threshold[2] = (float)(threshold * 0.01);
        cCurveShow.log_threshold[3] = (float)(threshold * 0.01);
        cCurveShow.MIN_CT=minCt;
        cCurveShow.norm_top=norm;
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
            List<ChartData> cdlist=new ArrayList<>();
            for (int n = 0; n < kslist.size(); n++)
            {
                cdlist = CommData.GetChartData(tdlist.get(i), 0, kslist.get(n));//获取选点值

                for (int k = 0; k < cdlist.size(); k++)
                {
                   /* double factorValue=factorValues[GetChan(tdlist.get(i))][k];
                    m_yData[i][n][k] = cdlist.get(k).y /factorValue;*/
                    m_yData[i][n][k] = cdlist.get(k).y ;

                }
            }
            if (cdlist.isEmpty()){
                continue;
            }
            cdlist = CommData.GetChartData(tdlist.get(i), 0, "C0"); //dark pixels
            for (int k = 0; k < cdlist.size(); k++)
            {
                m_bData[i][k] = cdlist.get(k).y*4/11; //
            }
            if (CommData.diclist.size() > 0 && CommData.diclist.get(tdlist.get(i))!=null)
            {
                int size=CommData.diclist.get(tdlist.get(i)).size();
                cyclenum = ( size/ CommData.imgFrame);
                // plus 1 because the point at 0 and 1 is replicated.
            }
        }

        cCurveShow.m_yData = m_yData;
        cCurveShow.m_bData = m_bData;
        for (int i = 0; i < CCurveShowPolyFit.MAX_CHAN; i++) {
            cCurveShow.m_Size[i] = cyclenum+1;
        }

        //cCurveShow.ifactor = factorValues;
        cCurveShow.UpdateAllcurve();

      /*  m_zData = cCurveShow.m_zData;
        m_CTValue = cCurveShow.m_CTValue;*/
        DtChart.DtData dtData=new DtChart.DtData();
        dtData.m_CTValue= cCurveShow.m_CTValue;
        dtData.m_zData=cCurveShow.m_zData;
        dtData.m_falsePositive=cCurveShow.m_falsePositive;
        return dtData;
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
