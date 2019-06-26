package com.jz.experiment.util;

import android.content.Context;

import com.anitoa.well.Well;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Utils {

    public static String getCtValue(Context context,String chan, String well, double[][] ctValues, boolean[][] falsePositive) {
        if (!CommData.diclist.keySet().contains(chan) || CommData.diclist.get(chan).size() == 0)
            return null;

        int currChan = 0;

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

        int ksindex=Well.getWell().getWellIndex(well);

        //还需要判断是否是假阳性
        String negative=context.getString(R.string.negative);
        String ctValue;
        if (falsePositive[currChan][ksindex]){
            //假阳性

            ctValue = "["+negative+"]";
        }else {
            double val = ctValues[currChan][ksindex];
            if (val>0) {
                DecimalFormat format = new DecimalFormat("#0.00");
                ctValue = format.format(val);
            }else {
                ctValue = negative;
            }

        }


        return ctValue;
    }

    public static double [] getPolyCoefficients(int degree,double [] xx,double [] yy){
        int size=xx.length;
        PolynomialCurveFitter polynomialCurveFitter = PolynomialCurveFitter.create(degree);
        ArrayList<WeightedObservedPoint> weightedObservedPoints = new ArrayList();
        for (int k = 0; k < size; k++) {

            WeightedObservedPoint weightedObservedPoint = new WeightedObservedPoint(1,
                    xx[k], yy[k]);
            weightedObservedPoints.add(weightedObservedPoint);
        }
        //多项式系数
        double[] coefficients = polynomialCurveFitter.fit(weightedObservedPoints);
        return coefficients;
    }
    public static double [] getPolyfit(int degree,double [] xx,double [] yy,double [] fitXX){

        //多项式系数
        double[] coefficients = getPolyCoefficients(degree,xx,yy);
        //获取拟合的y值
        double[] fitted = fitValue(coefficients, fitXX);
        return fitted;
    }

    private static double[] fitValue(double[] coefficients, double[] xx) {
        double yy[] = new double[xx.length];
        for (int i = 0; i < xx.length; i++) {
            yy[i] = getPolyY(coefficients, xx[i]);
        }

        return yy;
    }

    public static double getPolyY(double[] coefficients, double x) {
        int degree = coefficients.length - 1;
        double y = 0;
        for (int i = degree; i >= 0; i--) {
            y += coefficients[i] * Math.pow(x, i);
        }
        return y;
     /*   return coefficients[0] +
                coefficients[1] * x;*/
    }
}
