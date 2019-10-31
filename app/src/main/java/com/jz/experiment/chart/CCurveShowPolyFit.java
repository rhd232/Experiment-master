package com.jz.experiment.chart;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CCurveShowPolyFit {
    boolean GD_MOMENTUM = true;
    boolean ALL_SEL = true;
    boolean NORMALIZE_SIG = false;
    boolean ALIGN_BASE = true;
    boolean POLYFIT_OUTLIER = true;
    boolean ENGLISH_VER = true;
    boolean DARK_CORRECT = true;
    boolean CHECK_DARK = false;
    boolean NEG_CLIP = false;
    boolean SHOW_RAW = false;
    boolean AVG_STDEV = false;
    boolean NO_CT = false;

    private final static int numChannels = 4;
    int numWells = 16;
    public int MIN_CT = 13;         //之前15 // minimal allowed CT
    int CT_TH_MULTI = 8;            // 8 instead of 10, this is pre calc of Ct anyway

    private final static double OUTLIER_THRESHOLD = 2.8;

    //float log_threshold = 0.11f;

    float cheat_factor = 0.1f;
    float cheat_factor2 = 0.5f;
    float cheat_factorNeg = 0.33f;             // not used

    boolean hide_org = true;
    public boolean norm_top = true;

    public static int MAX_CHAN = 4;
    public static int MAX_WELL = 16;
    public static int MAX_CYCL = 501;//400

    public double[][][] m_yData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][] m_bData = new double[MAX_CHAN][MAX_CYCL];
    public double[][] m_bFactor = new double[MAX_CHAN][MAX_CYCL];
    public double[][][] m_zData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][] m_CTValue = new double[MAX_CHAN][MAX_WELL];
    public double[][] m_mean = new double[MAX_CHAN][MAX_WELL];
    public boolean[][] m_falsePositive = new boolean[MAX_CHAN][MAX_WELL];
    public String[][] m_Confidence = new String[MAX_CHAN][MAX_WELL];

    public double[][] m_stdev = new double[MAX_CHAN][MAX_WELL];
    public double[][] m_slope = new double[MAX_CHAN][MAX_WELL];
    public double[][] m_intercept = new double[MAX_CHAN][MAX_WELL];

    public double[][][] m_zData2 = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];


    double[] x = new double[MAX_CYCL];
    double[] y = new double[MAX_CYCL];
    double[][] k = new double[MAX_WELL][MAX_CHAN];
    double[][] r = new double[MAX_WELL][MAX_CHAN];
    double[][] t = new double[MAX_WELL][MAX_CHAN];
    double delta_k, delta_r, delta_t;
    int[][] fit_count = new int[MAX_WELL][MAX_CHAN];
    public int[] m_Size = new int[MAX_CHAN];            // Zhimin: now per channel

    public double[][] ifactor = new double[MAX_CHAN][MAX_CYCL];

    public float[] log_threshold = new float[]{0.12f, 0.12f, 0.12f, 0.12f};
    //float[] log_threshold = new float[] { 11f, 11f, 11f, 11f };

    float[] ct_offset = new float[4];
    Random ran = new Random();


   /* private static CCurveShowPolyFit sINSTANCE = new CCurveShowPolyFit();

    public static CCurveShowPolyFit getInstance() {
        return sINSTANCE;
    }*/

    public CCurveShowPolyFit() {
    }

    public void InitData() {
        numWells = CommData.KsIndex;

        int i,j;
        for ( i = 0; i < MAX_CHAN; i++) {
            for ( j = 0; j < numWells; j++) {
                m_CTValue[i][j] = 0;
                m_mean[i][j] = 0;
                m_falsePositive[i][j] = false;
                m_stdev[i][j] = 0;
            }

            for ( j = 0; j < MAX_CYCL; j++) {
                m_bFactor[i][j] = 0;
            }
        }

        for ( i = 0; i < MAX_WELL; i++) {
            for ( j = 0; j < MAX_CHAN; j++) {
                k[i][j] = 15;
                r[i][j] = 0.3;
                t[i][j] = 25;
                fit_count[i][j] = 0;
            }
        }

        for ( i = 0; i < 4; i++) {
            ct_offset[i] = (float) Math.log(1 / log_threshold[i] - 1);
        }

        if(SHOW_RAW){
            cheat_factor = 1.0f;                  // Fitted curve added with some hint of raw data
            cheat_factor2 = 1.0f;                 // The fake "raw" data
            cheat_factorNeg = 1.0f;              // Suppress signal is judged negative
        }
/*        m_zData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_zData2 = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_yData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        ifactor = new double[MAX_CHAN][MAX_CYCL];*/

    }

    public void UpdateAllcurve() {
        CalculateCT();

        for (int iy = 0; iy < MAX_CHAN; iy++) {
            double ct=0;
            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                double[] yData = new double[MAX_CYCL];

                for (int i = 0; i < m_Size[iy]; i++) {
                    yData[i] = m_yData[iy][frameindex][i];
                }

                int size = m_Size[iy];

                if (size < 3) {
                    for (int i = 0; i < size; i++) {
                        m_zData[iy][frameindex][i] = yData[i];
                        m_zData2[iy][frameindex][i] = yData[i];
                    }

                    continue;
                }

                double currbase = 0;
                int k;

                if (size > MIN_CT) {
                    for (k = 0; k < size; k++)
                    {
                        if (iy == 0 && m_Size[1] == m_Size[0] && CommData.crossTalk21 > 0.01)
                        {
                            yData[k] -= CommData.crossTalk21 * m_yData[1][frameindex][k];
                        }

                        if (iy == 1 && m_Size[1] == m_Size[0] && CommData.crossTalk12 > 0.01)
                        {
                            yData[k] -= CommData.crossTalk12 * m_yData[0][frameindex][k];
                        }
                    }

                    for (k = 3; k < MIN_CT; k++) {
                        currbase += yData[k];
                    }

                    if (currbase > 0) currbase /= (MIN_CT - 3);

                    yData[0] += 0.5 * (currbase - yData[0]);    // Replace the first data at index 0 with half way to base value, so the curve look better.

                    if(!ALIGN_BASE)
                        currbase = 0;

                    for (k = 0; k < size; k++) {
                        if (DARK_CORRECT) {
                            yData[k] -= currbase + m_bFactor[iy][k];
                        } else {
                            yData[k] -= currbase;
                        }
                        if(CHECK_DARK){
                            if(frameindex == numWells - 1 ) yData[k] = m_bFactor[iy][k];
                        }

                        yData[k] *= cheat_factorNeg;               // So it is well below the threshold
                        if(NEG_CLIP)
                        {
                            if (yData[k] < -25)
                                yData[k] = -25;
                        }
                    }
                }

                ct = m_CTValue[iy][frameindex];

                if (hide_org && ct >= 5 && yData.length >= 20) continue;//if (hide_org && ct >= 5 && yData.size() >= 20) continue;

                for (int i = 0; i < size; i++) {
                    m_zData[iy][frameindex][i] = yData[i];
                    m_zData2[iy][frameindex][i] = yData[i];
                }
                m_mean[iy][frameindex] = currbase;
            }

            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                DrawSigCurve(iy, frameindex);
            }

            CheckFalsePositive(iy);

            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                int size = m_Size[iy];
                if (norm_top && !m_falsePositive[iy][frameindex] && m_CTValue[iy][ frameindex] >= 5) {

                    for (int i = 0; i < size; i++) {
                        m_zData[iy][frameindex][i] = m_zData[iy][frameindex][i] * 50 / k[frameindex][iy];
                        m_zData2[iy][frameindex][i] = m_zData2[iy][frameindex][i] * 50 / k[frameindex][iy];
                    }
                }else if(m_falsePositive[iy][frameindex] && m_CTValue[iy][frameindex] >= 5){
                    for (int i = 0; i < size; i++)
                    {
                        m_zData[iy][frameindex][i] *= cheat_factorNeg;
                        m_zData2[iy][frameindex][i] *= cheat_factor2;
                    }
                }
            }
        }
    }

    public void DrawSigCurve(int iy, int frameindex) {
        double[] val = new double[MAX_CYCL];
        double[] cyc = new double[MAX_CYCL];

        double[] val2 = new double[MAX_CYCL];

//            for (int iy = 0; iy < MAX_CHAN; iy++)
//            {

        double mean = 0;
        double ct = m_CTValue[iy][frameindex];
        double[] yData = new double[MAX_CYCL];
        int size = m_Size[iy];

        for (int i = 0; i < size; i++) {
            yData[i] = m_yData[iy][frameindex][i];
        }

        if (size < 20 || ct < 5) return;

//                for (int k = 0; k < size; k++)
//                {
//                    yData[k] /= ifactor[iy, k];
//                }

        for (int i = 3; i < MIN_CT; i++) {
            mean += yData[i];
        }

        mean /= MIN_CT - 3;

        yData[0] += 0.5 * (mean - yData[0]);                // Replace the first data at index 0 with half way to base value, so the curve look better.

        for (int i = 0; i < size; i++) {
            x[i] = (double) i;
            y[i] = yData[i] - mean;
            if(m_slope[iy][ frameindex] < -5)
            {
                y[i] = yData[i] - m_intercept[iy][frameindex] - x[i] * m_slope[iy][frameindex];
            }
        }

        if (fit_count[frameindex][iy] < 1) {//ct change
            t[frameindex][iy] = ct + 4;
            k[frameindex][iy] = y[size - 1] / 130;            // a little smaller to ensure better converge
            if (size - (int) ct < 4) {
                k[frameindex][iy] *= 1.5;
            }
            curvefit(frameindex, iy, 700 + 250, size);
        } else if (fit_count[frameindex][iy] < 900) {
            curvefit(frameindex, iy, 250, size);
        } else {
            curvefit(frameindex, iy, 1, size);
        }

        for (int i = 0; i < size; i++) {
            cyc[i] = x[i];
            val[i] = sigmoid(x[i], k[frameindex][iy], r[frameindex][iy], t[frameindex][iy]);
            val2[i] = val[i];

            if (m_slope[iy][frameindex] < -5)
            {
                yData[i] -= m_intercept[iy][frameindex] + x[i] * m_slope[iy][frameindex];
            }
            else
            {
                yData[i] -= mean;
            }
            val[i] += cheat_factor * (yData[i] - val[i]); //* k[frameindex][iy] / 8;        // Some cheating :)
            val2[i] += cheat_factor2 * (yData[i] - val2[i]);       // Some cheating :)
            if(!ALIGN_BASE)
            {
                val[i] += mean;
                val2[i] += mean;
            }
        }


        for (int i = 0; i < size; i++) {

            if (NORMALIZE_SIG) {

                if (norm_top) {
                    m_zData[iy][frameindex][i] = val[i] * 50 / k[frameindex][iy];
                } else {
                    m_zData[iy][frameindex][i] = val[i]; //  + mean;
                }
            } else {
                m_zData[iy][frameindex][i] = val[i]; //  + mean;
                m_zData2[iy][frameindex][i] = val2[i]; //  + mean;
            }
        }

        m_mean[iy][frameindex] = mean;
//            }
    }

    int RSIZE = 9;

    public int curvefit(int well, int color, int iter, int size) {
        double delta_ko = 0;
        double delta_ro = 0;
        double delta_to = 0;

        for (int j = 0; j < iter; j++) {

            delta_k = delta_r = delta_t = 0;

            if (ALL_SEL) {

                for (int i = 3; i < size; i++) {
                    jacob(x[i], y[i], k[well][color], r[well][color], t[well][color], y[size - 1], fit_count[well][color]);
                }
            } else {
                int rsize = RSIZE;        // reduced size;
                int[] rindx = new int[RSIZE];

                if (rsize > size) rsize = size;

                for (int i = 0; i < rsize; i++) {

                    //rindx[i] =rand() % (size - 3) + 3;

                    rindx[i] = ran.nextInt(size - 3) + 3;//maybe
                }

                for (int i = 0; i < rsize; i++) {
                    jacob(x[rindx[i]], y[rindx[i]], k[well][color], r[well][color], t[well][color], y[size - 1], fit_count[well][color]);
                }
            }

            if (GD_MOMENTUM) {
                delta_k += 0.8 * delta_ko;
                delta_r += 0.8 * delta_ro;
                delta_t += 0.8 * delta_to;
            }

            if ((k[well][color] > 300 && delta_k > 0) || (k[well][color] < 0 && delta_k < 0))
                delta_k = 0;

            if ((r[well][color] > 0.65 && delta_r > 0) || (r[well][color] < 0.2 && delta_r < 0))
                delta_r = 0;

            if ((t[well][color] > 50 && delta_t > 0) || (t[well][color] < 10 && delta_t < 0))
                delta_t = 0;

            k[well][color] += delta_k;
            r[well][color] += delta_r;
            t[well][color] += delta_t;

            if (r[well][color] > 0.7)
                r[well][color] = 0.7;
            else if (r[well][color] < 0.15)
                r[well][color] = 0.15;

            fit_count[well][color] += 1;

            delta_ko = delta_k;
            delta_ro = delta_r;
            delta_to = delta_t;
        }

        double ct = t[well][color] - ct_offset[color] / r[well][color];

        if (ct > MIN_CT && ct <= size) {
            m_CTValue[color][well] = ct;
        }
//            else
//            {
//                MessageBox.Show("Minimum Ct too high, please readjust");
//                MessageBox.Show("Ct下限太高， 请调整");
//            }

        return 0;
    }

    public int jacob(double x, double y, double k, double r, double t, double endy, int fit_count) {
        double e = Math.exp(-r * (x - t));

        double dydk = 100 / (1 + e);
        double dydr = 100 * k * e * (x - t) / ((1 + e) * (1 + e));
        double dydt = -100 * k * e * r / ((1 + e) * (1 + e));

        double yy = 100 * k / (1 + e);

        double rate = 8e-8;

        if (fit_count > 1000) {
            rate *= 0.1;
        } else if (fit_count > 400) {
            rate *= 0.3;
        }

        if (endy > 100)
            rate *= 500 / endy;

        delta_k += rate * (y - yy) * dydk;
        delta_r += 0.5 * rate * (y - yy) * dydr;
        delta_t += rate * (y - yy) * dydt;

        return 0;
    }


    public double sigmoid(double x, double k, double r, double t) {
        double y = 100 * k / (1 + Math.exp(-r * (x - t)));
        return y;
    }

    public double sumArray(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public double sumList(List<Double> list) {
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }

    public void CalculateCT() {
        for (int i = 0; i < numChannels; i++) {
            double[][] yData = new double[MAX_WELL][MAX_CYCL];

            //=============== Background correct============

            double[] bData = new double[MAX_CYCL];
            int bsize = m_Size[i];

            if (bsize < 1) continue;

            for (int n = 0; n < bsize; n++) {
                bData[n] = m_bData[i][n];
            }

            double bmean = 0;

            for (int n = 1; n < MIN_CT; n++) {
                bmean += bData[n];
            }
            bmean /= MIN_CT - 1;

            double[] y = new double[bsize-1];
            double[] x = new double[bsize-1];


            for (int k = 0; k < bsize-1; k++) {
                y[k] = bData[k+1];
                x[k] = (double) k+1;
            }
            PolynomialCurveFitter polynomialCurveFitter = PolynomialCurveFitter.create(3);
            ArrayList<WeightedObservedPoint> weightedObservedPoints = new ArrayList();
            for (int k = 0; k < bsize-1; k++) {
                WeightedObservedPoint weightedObservedPoint = new WeightedObservedPoint(1,
                        x[k], y[k]);
                weightedObservedPoints.add(weightedObservedPoint);
            }
            //多项式系数
            double[] coefficients;
            if(weightedObservedPoints.size() == 0)
                coefficients = new double[]{0};
            else
                coefficients = polynomialCurveFitter.fit(weightedObservedPoints);
            //获取拟合的y值
            double[] bfitted = fitValue(coefficients , x);

            double[] bFactor = new double[bsize];
            for (int k = 0; k < bsize-1; k++) {
                bFactor[k+1] = bfitted[k] - bmean;
            }

            bFactor[0] = 0;

            for (int k = 0; k < bsize; k++)
            {
                m_bFactor[i][k] = bFactor[k];
            }
            //===============

            for (int j = 0; j < numWells; j++) {
                int size = m_Size[i];

                for (int n = 0; n < size; n++) {
                    if (DARK_CORRECT)
                        yData[j][n] = m_yData[i][j][n] - bFactor[n];
                    else
                        yData[j][n] = m_yData[i][j][n];
                    if(CHECK_DARK)
                        if (j == numWells - 1) yData[j][n] = bFactor[n];
                    if (i == 0 && m_Size[1] == m_Size[0] && CommData.crossTalk21 > 0.01) {
                        yData[j][n] -= CommData.crossTalk21 * m_yData[1][j][ n];
                    }

                    if (i == 1 && m_Size[1] == m_Size[0] && CommData.crossTalk12 > 0.01) {
                        yData[j][n] -= CommData.crossTalk12 * m_yData[0][j][n];
                    }
                }

                if (size < MIN_CT)      //data has not enough to perform Ct calculation
                    continue;

                if (POLYFIT_OUTLIER) {

                    double[] yy = new double[size];
                    double[] xx = new double[size];
                    double[] diff = new double[size];

                    for (int k = 0; k < size; k++)
                    {
                        yy[k] = yData[j][k];
                        xx[k] = (double)k;
                    }

                    PolynomialCurveFitter polynomialCurveFitterOutlier = PolynomialCurveFitter.create(5);
                    ArrayList<WeightedObservedPoint> weightedObservedPointsOutlier = new ArrayList();
                    for (int k = 0; k < size; k++) {
                        WeightedObservedPoint weightedObservedPointOutlier = new WeightedObservedPoint(1,
                                xx[k], yy[k]);

                        weightedObservedPointsOutlier.add(weightedObservedPointOutlier);
                    }
                    //多项式系数
                    double[] coefficientsOutlier = polynomialCurveFitterOutlier.fit(weightedObservedPointsOutlier);
                    //获取拟合的y值
                    double[] fitted = fitValue(coefficientsOutlier, xx);

                    for (int k = 0; k < size; k++) {
                        diff[k] = yy[k] - fitted[k];
                    }
                    double sum = sumArray(diff);
                    double mean = sum / diff.length;

                    double accum = 0.0;

                    for (int k = 0; k < diff.length; k++) {
                        accum += (diff[k] - mean) * (diff[k] - mean);
                    }
                    double stdev = Math.sqrt(accum / diff.length);         //方差

                    for (int k = 0; k < size; k++) {
                        if (diff[k] > OUTLIER_THRESHOLD * stdev || diff[k] < -OUTLIER_THRESHOLD * stdev) {
                            yData[j][k] = fitted[k];
                        }
                    }


                }
                List<Double> tempData = new ArrayList<>();

                for (int n = 3; n < MIN_CT; n++) {
                    tempData.add(yData[j][n]);
                }
                double sum = sumList(tempData);
                //sum = tempData.sum();
                double mean = sum / tempData.size();

                double accum = 0.0;

                for (int t = 0; t < tempData.size(); t++) {
                    accum += (tempData.get(t) - mean) * (tempData.get(t) - mean);
                }
                double stdev = Math.sqrt(accum / tempData.size()); //方差

                m_stdev[i][j] = stdev;
                m_mean[i][j] = mean;

                //============ PIVOT ========

                int MIN_CT2 = MIN_CT + 10;

                double[] yyy = new double[MIN_CT2 - 3];
                double[] xxx = new double[MIN_CT2 - 3];
                double[] diff2 = new double[MIN_CT2 - 3];

                for (int k = 0; k < MIN_CT2-3; k++)
                {
                    yyy[k] = yData[j][k];
                    xxx[k] = (double)k + 3;//maybe
                }

                PolynomialCurveFitter polynomialCurveFitter2 = PolynomialCurveFitter.create(1);
                ArrayList<WeightedObservedPoint> weightedObservedPoints2 = new ArrayList();
                for (int k = 0; k < MIN_CT2-3; k++) {
                    WeightedObservedPoint weightedObservedPoint2 = new WeightedObservedPoint(1,
                            xxx[k], yyy[k]);

                    weightedObservedPoints2.add(weightedObservedPoint2);
                }
                //多项式系数
                double[] coefficients2 = polynomialCurveFitter2.fit(weightedObservedPoints2);
                //获取拟合的y值
                double[] fitted2 = fitValue(coefficients2, xxx);

                double pvt_slope = coefficients2[1];
/*                var polyfit2 = new PolyFit(xxx, yyy, 1);
                var fitted2 = polyfit2.Fit(xxx);*/
                if (pvt_slope > -5)
                    continue;

                for (int k = 0; k < MIN_CT2 - 3; k++)
                {
                    diff2[k] = yyy[k] - fitted2[k];
                }

                double accum2 = 0.0;

                for (int k = 0; k < diff2.length; k++)
                {
                    accum2 += diff2[k] * diff2[k];
                }
                double stdev2 = Math.sqrt(accum2 / diff2.length);         //方差

                for (int k = 0; k < size; k++)
                {
                    yData[j][k] -= coefficients2[0] + (double)k * coefficients2[1];
                    yData[j][k] += mean;
                }

                m_stdev[i][j] = stdev2;
                m_intercept[i][j] = coefficients2[0];
                m_slope[i][j] = coefficients2[1];

                //===============
            }

            double std_mean = 0;

            for (int j = 0; j < numWells; j++)
            {
                std_mean += m_stdev[i][ j];
            }

            std_mean /= numWells;

            for (int j = 0; j < numWells; j++)
            {
                int size = m_Size[i];

                if (size < MIN_CT)      //data has not enough to perform Ct calculation
                    continue;

                double mean = m_mean[i][ j];
                double stdev = m_stdev[i][j]; //  m_stdev[i, j];

                //===============================================

                //			double yvalue=stdev * CT_TH_MULTI + mean;

                double multiple = log_threshold[i] * CT_TH_MULTI * 10;

                if (multiple < 6)
                    multiple = 6;

                if (m_slope[i][j] < -5)
                    multiple *= 1.5;

                double yvalue = stdev * multiple + mean;
                double first = yData[j][ 2];

                if(NO_CT)
                    continue;

                int index = 0;

                for (int g = 3; g < size; g++)
                {
                    if (yvalue > first  && yvalue <= yData[j][g])
                    {
                        break;
                    }
                    else
                    {
                        first = yData[j][ g];
                        index++;
                    }
                }

                if (index == 0 || index == size - 3)
                {
                    m_CTValue[i][ j] = 0;
                }
                else
                {
                    index = index + 3;
                    while (yData[j][index] - yData[j][ index - 1] == 0) index++;
                    m_CTValue[i][j] = index - (yData[j][ index] - yvalue) / (yData[j][ index] - yData[j][ index - 1]);
                    m_CTValue[i][j] = m_CTValue[i][ j] > 0 ? m_CTValue[i][j] : 0;
                }
            }

        }
    }

    private double[] fitValue(double[] coefficients, double[] x) {
        double y[] = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = getY(coefficients, x[i]);
        }
        return y;
    }

    public double getY(double[] coefficients, double x) {
        int degree = coefficients.length - 1;
        double y = 0;
        for (int i = degree; i >= 0; i--) {
            y += coefficients[i] * Math.pow(x, i);
        }
        return y;
    }

    private final static double r_th = 0.21;

    public void CheckFalsePositive(int iy) {
        double max_k = 0;
        int max_i = 0;

        for (int frameindex = 0; frameindex < numWells; frameindex++) {
            if (m_CTValue[iy][frameindex] < 0.1)
                continue;

            if (max_k < k[frameindex][iy]) {
                max_k = k[frameindex][iy];
                max_i = frameindex;
            }
        }
        double[] eff = new double[numWells];
        double max_eff = 0;
        int max_ei = 0;
        for (int frameindex = 0; frameindex < numWells; frameindex++) {
            eff[frameindex] = Math.exp(r[frameindex][iy]) - 1;         // amplification efficiency

            if (max_eff < eff[frameindex]) {
                max_eff = eff[frameindex];
                max_ei = frameindex;
            }
        }

        for (int frameindex = 0; frameindex < numWells; frameindex++) {
            m_falsePositive[iy][frameindex] = false;
            if (m_CTValue[iy][frameindex] < 0.1) continue;

            double ratio = 0;
            double confi = 0;
            double ratio_eff = 0;

            if (max_k > 0) ratio = k[frameindex][iy] / max_k;
            if (max_eff > 0) ratio_eff = eff[frameindex] / max_eff;

            if (r[max_i][iy] < r_th) {
                ratio *= 0.3;    // ratio discounted. If the max curve have a very slow slope
            }

//                if (ratio < 0.15 || r[frameindex, iy] < r_th)
//                {
//                      m_Confidence[iy, frameindex] = "可信度: " + (confi * 100).ToString("0") + "%";       // full confidence is 100
//                }

            // confi = (ratio - 0.1) * 1.11 * 0.4 + (r[frameindex][iy] - 0.19) * 2.17 * 0.6; // Weighted average of 2 score, r generally range 0.19 to 0.65
            confi = (ratio - 0.1) * 1.11 * 0.5 + (ratio_eff - 0.1) * 1.11 * 0.5; // Weighted average of 2 score, r generally range 0.19 to 0.65

            if (confi < 0 || ratio < 0.03 || ratio_eff < 0.05)
                confi = 0;

            //                m_Confidence[iy, frameindex] = "阳性可信度:" + (confi * 100).ToString("0") + "% "
            //                                                    + "ratio: " + (ratio * 100).ToString("0") + "%  " + "r: " + r[frameindex, iy].ToString("0.00") + " k: " + k[frameindex, iy].ToString("0.00") + " stdev: " + m_stdev[ iy, frameindex].ToString("0.00");       // full confidence is 100

//            double myr = r[frameindex][iy];

            //    double eff = Math.exp(myr);         // amplification efficiency

//                m_Confidence[iy, frameindex] = "阳性可信度: "
//                                                    + "sat_ratio: " + (ratio * 100).ToString("0") + "%  " + "rise rate: " + r[frameindex, iy].ToString("0.00") + "(efficiency: " + (eff*100).ToString("0.0") + "%) " + " sat_multiple " + (k[frameindex, iy] * 100 / m_stdev[iy, frameindex]).ToString("0.00") + " (stdev: " + m_stdev[iy, frameindex].ToString("0.00") + ")";       // full confidence is 100

            if(ENGLISH_VER)
            {
                m_Confidence[iy][frameindex] = " -- Confidence: " + (ratio * 100) + "%  " + "Saturation ratio: "
                        + ((eff[frameindex] * 100) + "% " + " Amplification efficiency: "
                        + (k[frameindex][iy] * 100 / m_stdev[iy][frameindex])
                        + " (Base RMS noise: " + m_stdev[iy][frameindex] + ")");       // full confidence is 100
            }else{
                m_Confidence[iy][frameindex] = " -- 饱和值比例: " + (ratio * 100) + "%  " + "扩增效率: "
                        + ((eff[frameindex] * 100) + "% " + " 信噪比 "
                        + (k[frameindex][iy] * 100 / m_stdev[iy][frameindex])
                        + " (本底噪音: " + m_stdev[iy][frameindex] + ")");       // full confidence is 100
            }

            if (confi < 0.2) {//之前0.12
                m_falsePositive[iy][frameindex] = true;
            }

        }
    }
}