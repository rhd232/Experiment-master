package com.jz.experiment.chart;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CCurveShowPolyFit {
    boolean DARK_CORRECT = true;
    boolean NORMALIZE_SIG = false;
    boolean ALL_SEL = true;
    boolean GD_MOMENTUM = true;
    boolean POLYFIT_OUTLIER = true;

    final int numChannels = 4;
    int numWells = 16;
    public int MIN_CT = 13;         //之前15 // minimal allowed CT
    int CT_TH_MULTI = 8;            // 8 instead of 10, this is pre calc of Ct anyway

    final double OUTLIER_THRESHOLD = 2.8;

    //float log_threshold = 0.11f;

    //    float cheat_factor = 0.06f;
//    float cheat_factor2 = 0.5f;
//    float cheat_factorNeg = 0.33f;             // not used
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
    Random ran;


   /* private static CCurveShowPolyFit sINSTANCE = new CCurveShowPolyFit();

    public static CCurveShowPolyFit getInstance() {
        return sINSTANCE;
    }*/

    public CCurveShowPolyFit() {
    }

    public void InitData() {
        numWells = CommData.KsIndex;

        for (int i = 0; i < MAX_CHAN; i++) {
            for (int j = 0; j < numWells; j++) {
                m_CTValue[i][j] = 0;
                m_mean[i][j] = 0;
                m_falsePositive[i][j] = false;
                m_stdev[i][j] = 0;
            }

            for (int k = 0; k < MAX_CYCL; k++) {
                m_bFactor[i][k] = 0;
            }
        }

        for (int i = 0; i < MAX_WELL; i++) {
            for (int j = 0; j < MAX_CHAN; j++) {
                k[i][j] = 15;
                r[i][j] = 0.3;
                t[i][j] = 25;
                fit_count[i][j] = 0;
            }
        }

        for (int i = 0; i < 4; i++) {
            ct_offset[i] = (float) Math.log(1 / log_threshold[i] - 1);
        }
        m_bData = new double[MAX_CHAN][MAX_CYCL];
        m_zData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_zData2 = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_yData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        ifactor = new double[MAX_CHAN][MAX_CYCL];

       /* x = new double[MAX_CYCL];
        y = new double[MAX_CYCL];*/

        ran = new Random();
/*#if DEBUG
            cheat_factor2 = 1.0f;
#endif*/
    }

    public void UpdateAllcurve() {
        CalculateCT();

        for (int iy = 0; iy < MAX_CHAN; iy++) {
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
                    for (k = 3; k < MIN_CT; k++) {
                        currbase += yData[k];
                    }

                    if (currbase > 0) currbase /= (MIN_CT - 3);

                    yData[0] += 0.5 * (currbase - yData[0]);    // Replace the first data at index 0 with half way to base value, so the curve look better.

                    for (k = 0; k < size; k++) {
                        if (DARK_CORRECT) {
                            yData[k] -= currbase + m_bFactor[iy][k];
                        } else {
                            yData[k] -= currbase;
                        }

                        //                            yData[k] *= cheat_factor_org;			// cheating to beautify the curve
                        yData[k] *= cheat_factorNeg;               // So it is well below the threshold

                        if (yData[k] < -15) {
                            yData[k] = -15;
                        }
                    }
                }

                double ct = m_CTValue[iy][frameindex];

                if (hide_org && ct >= 5 && yData.length >= 20)
                    continue;//if (hide_org && ct >= 5 && yData.size() >= 20) continue;

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
                if (norm_top && !m_falsePositive[iy][frameindex]) {
                    int size = m_Size[iy];

                    for (int i = 0; i < size; i++) {
                        m_zData[iy][frameindex][i] = m_zData[iy][frameindex][i] * 50 / k[frameindex][iy];
                        m_zData2[iy][frameindex][i] = m_zData2[iy][frameindex][i] * 50 / k[frameindex][iy];
                    }
                }
            }
        }
    }

    public void DrawSigCurve(int iy, int frameindex) {
        double[] val = new double[MAX_CYCL];
        double[] cyc = new double[MAX_CYCL];

        double[] val2 = new double[MAX_CYCL];

        double mean = 0;
        double ct;

//            for (int iy = 0; iy < MAX_CHAN; iy++)
//            {

        mean = 0;
        ct = m_CTValue[iy][frameindex];
        double[] yData = new double[MAX_CYCL];
        for (int i = 0; i < m_Size[iy]; i++) {
            yData[i] = m_yData[iy][frameindex][i];
        }

        int size = m_Size[iy];

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
        }

        if (fit_count[frameindex][iy] < 1) {
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

            yData[i] -= mean;
            val[i] += cheat_factor * (yData[i] - val[i]); //* k[frameindex][iy] / 8;        // Some cheating :)
            val2[i] += cheat_factor2 * (yData[i] - val2[i]);       // Some cheating :)
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

                    rindx[i] = ran.nextInt(size - 3) + 3;
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


            for (int n = 0; n < bsize; n++) {
                bData[n] = m_bData[i][n];
            }

            double bmean = 0;

            for (int n = 0; n < MIN_CT; n++) {
                bmean += bData[n];
            }
            bmean /= MIN_CT;

            double[] y = new double[bsize];
            double[] x = new double[bsize];
            double[] bFactor = new double[bsize];

            for (int k = 0; k < bsize; k++) {
                y[k] = bData[k];
                x[k] = (double) k;
            }
            PolynomialCurveFitter polynomialCurveFitter = PolynomialCurveFitter.create(3);
            ArrayList<WeightedObservedPoint> weightedObservedPoints = new ArrayList();
            for (int k = 0; k < bsize; k++) {
               /* yy[k] = yData[k];
                xx[k] = (double) k;*/

                WeightedObservedPoint weightedObservedPoint = new WeightedObservedPoint(1,
                        k, bData[k]);

                weightedObservedPoints.add(weightedObservedPoint);
            }
            //多项式系数
            double[] coefficients = polynomialCurveFitter.fit(weightedObservedPoints);
            //获取拟合的y值
            double[] bfitted = fitValue(coefficients, x);


            for (int k = 0; k < bsize; k++) {
                bFactor[k] = bfitted[k] - bmean;
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

                    PolynomialCurveFitter polynomialCurveFitterOutlier = PolynomialCurveFitter.create(5);
                    ArrayList<WeightedObservedPoint> weightedObservedPointsOutlier = new ArrayList();
                    for (int k = 0; k < size; k++) {
                        yy[k] = yData[j][k];
                        xx[k] = (double) k;

                        WeightedObservedPoint weightedObservedPointOutlier = new WeightedObservedPoint(1,
                                k, yy[k]);

                        weightedObservedPointsOutlier.add(weightedObservedPointOutlier);
                    }
                    //多项式系数
                    double[] coefficientsOutlier = polynomialCurveFitterOutlier.fit(weightedObservedPointsOutlier);
                    //获取拟合的y值
                    double[] fitted = fitValue(coefficientsOutlier, xx);
  /*  var polyfit = new PolyFit(xx, yy, 5);
    var fitted = polyfit.Fit(xx);*/

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

/*#if DEBUG
//                            String debuginfo = "yvalue = " + yy[k].ToString() + " found the " + k.ToString() + "position outlier data( " + i.ToString() + " " + j.ToString() + ")";
//                            MessageBox.Show(debuginfo);
#endif*/
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
                double stdev = std_mean; //  m_stdev[i, j];

                //===============================================

                //			double yvalue=stdev * CT_TH_MULTI + mean;

                double multiple = log_threshold[i] * CT_TH_MULTI * 10;

                if (multiple < 8) multiple = 8;

                double yvalue = stdev * multiple + mean;

                double first = yData[j][ 2];

                int index = 0;

                for (int g = 3; g < size; g++)
                {
                    if (yvalue > first && yvalue <= yData[j][g])
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
                    m_CTValue[i][ j] = index - (yData[j][ index] - yvalue) / (yData[j][ index] - yData[j][ index - 1]);
                    m_CTValue[i][j] = m_CTValue[i][ j] > 0 ? m_CTValue[i][ j] : 0;
                }
            }

        }
    }

    public void CalculateCT2() {
        //if(IsCT() > 0 && !m_bThChange) return;		//CT valus have been calculated,need not been calculated again.
        // No, always recalc initial Ct.

        for (int i = 0; i < numChannels; i++) {
            for (int j = 0; j < numWells; j++) {
                double[] yData = new double[MAX_CYCL];
                int size = m_Size[i];

                for (int n = 0; n < size; n++) {
                    yData[n] = m_yData[i][j][n];
                }

                //#if DIS_FACTOR
                //          int k;

                //			for (k = 0; k < size; k++) {
                //				yData[k] /= ifactor[i, k];
                //			}
                //#endif


                if (size < MIN_CT)      //data has not enough to perform Ct calculation
                    continue;

                if (POLYFIT_OUTLIER) {

                    double[] yy = new double[size];
                    double[] xx = new double[size];
                    double[] diff = new double[size];

                    PolynomialCurveFitter polynomialCurveFitter = PolynomialCurveFitter.create(5);
                    ArrayList<WeightedObservedPoint> weightedObservedPoints = new ArrayList();
                    for (int k = 0; k < size; k++) {
                        yy[k] = yData[k];
                        xx[k] = (double) k;

                        WeightedObservedPoint weightedObservedPoint = new WeightedObservedPoint(1,
                                k, yData[k]);

                        weightedObservedPoints.add(weightedObservedPoint);
                    }
                    //多项式系数
                    double[] coefficients = polynomialCurveFitter.fit(weightedObservedPoints);
                    //获取拟合的y值
                    double[] fitted = fitValue(coefficients, xx);
  /*  var polyfit = new PolyFit(xx, yy, 5);
    var fitted = polyfit.Fit(xx);*/

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
                            yData[k] = fitted[k];

/*#if DEBUG
//                            String debuginfo = "yvalue = " + yy[k].ToString() + " found the " + k.ToString() + "position outlier data( " + i.ToString() + " " + j.ToString() + ")";
//                            MessageBox.Show(debuginfo);
#endif*/
                        }
                    }


                } else {
                    List<Double> tempData = new ArrayList<>();
                    for (int n = 3; n < MIN_CT; n++) {
                        tempData.add(yData[n]);
                    }

                    double sum = sumList(tempData);//tempData.Sum();

                    //calculate CT value
                    //double sum = std::accumulate(std::begin(tempData), std::end(tempData), 0.0);
                    double mean = sum / tempData.size(); //mean

                    double accum = 0.0;
                    for (int m = 0; m < tempData.size(); m++) {
                        accum += (tempData.get(m) - mean) * (tempData.get(m) - mean);
                    }

                    //std::for_each (std::begin(tempData), std::end(tempData), [&](const double d) {
                    //    accum  += (d-mean)*(d-mean);
                    //});
                    double stdev = Math.sqrt(accum / tempData.size()); //方差

                    //========== outlier data remove=================
                    //std::for_each(std::begin(tempData), std::end(tempData), [&](double &d) {
                    //    if (d - mean > 2 * stdev || d - mean < -2 * stdev) d = mean;
                    //});


                    for (int v = 0; v < tempData.size(); v++) {
                        if (tempData.get(v) - mean > 2.5 * stdev || tempData.get(v) - mean < -2.5 * stdev) {
                            // tempData[v] = mean;
                            tempData.set(v, mean);
                            yData[v + 3] = mean;
                        }
                    }

                }
                List<Double> tempData = new ArrayList<>();

                for (int n = 3; n < MIN_CT; n++) {
                    tempData.add(yData[n]);
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

                //===============================================

                //			double yvalue=stdev * CT_TH_MULTI + mean;

                double multiple = log_threshold[i] * CT_TH_MULTI * 10;

                if (multiple < 8) multiple = 8;

                double yvalue = stdev * multiple + mean;

                double first = yData[2];
                int index = 0;

                for (int g = 3; g < yData.length; g++) {
                    if (yvalue > first && yvalue <= yData[g]) {
                        break;
                    } else {
                        first = yData[g];
                        index++;
                    }
                }

                if (index == 0 || index == yData.length - 3) {
                    m_CTValue[i][j] = 0;
                } else {
                    index = index + 3;
                    while (yData[index] - yData[index - 1] == 0) index++;
                    m_CTValue[i][j] = index - (yData[index] - yvalue) / (yData[index] - yData[index - 1]);
                    m_CTValue[i][j] = m_CTValue[i][j] > 0 ? m_CTValue[i][j] : 0;
                }
            }
        }
    }

    private double[] fitValue(double[] coefficients, double[] xx) {
        double yy[] = new double[xx.length];
        for (int i = 0; i < xx.length; i++) {
            yy[i] = getY(coefficients, xx[i]);
        }

        return yy;
    }

    public double getY(double[] coefficients, double x) {
        int degree = coefficients.length - 1;
        double y = 0;
        for (int i = degree; i >= 0; i--) {
            y += coefficients[i] * Math.pow(x, i);
        }
        return y;
       /* return coefficients[0] +
                coefficients[1] * x +
                coefficients[2] * Math.pow(x, 2) +
                coefficients[3] * Math.pow(x, 3) +
                coefficients[4] * Math.pow(x, 4) +
                coefficients[5] * Math.pow(x, 5);*/
    }


    final double r_th = 0.21;

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

            double myr = r[frameindex][iy];

            //    double eff = Math.exp(myr);         // amplification efficiency

//                m_Confidence[iy, frameindex] = "阳性可信度: "
//                                                    + "sat_ratio: " + (ratio * 100).ToString("0") + "%  " + "rise rate: " + r[frameindex, iy].ToString("0.00") + "(efficiency: " + (eff*100).ToString("0.0") + "%) " + " sat_multiple " + (k[frameindex, iy] * 100 / m_stdev[iy, frameindex]).ToString("0.00") + " (stdev: " + m_stdev[iy, frameindex].ToString("0.00") + ")";       // full confidence is 100

            m_Confidence[iy][frameindex] = " -- 饱和值比例: " + (ratio * 100) + "%  " + "扩增效率: "
                    + ((eff[frameindex] * 100) + "% " + " 信噪比 "
                    + (k[frameindex][iy] * 100 / m_stdev[iy][frameindex])
                    + " (本底噪音: " + m_stdev[iy][frameindex] + ")");       // full confidence is 100


            if (confi < 0.2) {//之前0.12
                m_falsePositive[iy][frameindex] = true;
            }

        }
    }
}