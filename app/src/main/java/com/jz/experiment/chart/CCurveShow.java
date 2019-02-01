package com.jz.experiment.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CCurveShow {
    public static boolean ALL_SEL = false;
    public static boolean GD_MOMENTUM = true;
    public static int MAX_CHAN = 4;
    public static int MAX_WELL = 8;
    public static int MAX_CYCL = 61;//400
    int numWells = 4;
    int MIN_CT = 12;//之前15 // minimal allowed CT
    int CT_TH_MULTI = 6;// 6 instead of 10, this is pre calc of Ct anyway

    //float log_threshold = 0.11f;
    float cheat_factor = 0.06f;
    float cheat_factor_org = 0.06f;
    boolean hide_org = true;

    public double[][][] m_yData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][][] m_zData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][] m_CTValue = new double[MAX_CHAN][MAX_WELL];

    double[] x = new double[MAX_CYCL];
    double[] y = new double[MAX_CYCL];
    double[][] k = new double[MAX_WELL][4];
    double[][] r = new double[MAX_WELL][4];
    double[][] t = new double[MAX_WELL][4];
    double delta_k, delta_r, delta_t;
    int[][] fit_count = new int[MAX_WELL][4];
    public int m_Size = 30;

    public double[][] ifactor = new double[MAX_CHAN][MAX_CYCL];


    float[] log_threshold = new float[]{0.11f, 0.11f, 0.11f, 0.11f};
    //float[] log_threshold = new float[] { 11f, 11f, 11f, 11f };

    float[] ct_offset = new float[4];
    Random ran;

    private static CCurveShow INSTANCE=new CCurveShow();

    public static CCurveShow getInstance(){
        return INSTANCE;
    }

    private CCurveShow(){
    }

    public void InitData() {
        numWells = CommData.KsIndex;//读取dataposition文件
        System.out.println("numWells:"+numWells);
        int i, j;
        for (i = 0; i < MAX_CHAN; i++) {
            for (j = 0; j < numWells; j++) {
                m_CTValue[i][j] = 0;
            }
        }

        for (i = 0; i < MAX_WELL; i++) {
            for (j = 0; j < 4; j++) {
                k[i][j] = 15;
                r[i][j] = 0.3;
                t[i][j] = 25;
                fit_count[i][j] = 0;
            }
        }

        for (i = 0; i < 4; i++) {
            ct_offset[i] = (float) Math.log(1 / log_threshold[i] - 1);
        }
        ran = new Random(1);
    }

    public void UpdateAllcurve() {
        CalculateCT();
        for (int iy = 0; iy < MAX_CHAN; iy++) {
            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                double[] yData = new double[MAX_CYCL];

                for (int i = 0; i < m_Size; i++) {
                    yData[i] = m_yData[iy][frameindex][i];
                }

                if (m_Size < 3) continue;

                double currbase = 0;
                int size = m_Size;
                int k;

                if (size > MIN_CT) {
                    for (k = 3; k < MIN_CT; k++) {
                        currbase += yData[k];
                    }

                    if (currbase > 0) currbase /= (MIN_CT - 3);

                    yData[0] += 0.5 * (currbase - yData[0]);    // Replace the first data at index 0 with half way to base value, so the curve look better.

                    for (k = 0; k < size; k++) {
                        yData[k] -= currbase;
                        //                            yData[k] *= cheat_factor_org;			// cheating to beautify the curve
                        yData[k] /= 2;               // So it is well below the threshold
                    }
                }

                double ct = m_CTValue[iy][frameindex];

                if (hide_org && ct >= 5 && yData.length >= 20)
                    continue;//if (hide_org && ct >= 5 && yData.size() >= 20) continue;

                for (int i = 0; i < size; i++) {
                    m_zData[iy][frameindex][i] = yData[i];
                }
            }

            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                DrawSigCurve(frameindex);
            }
        }
    }


    public void DrawSigCurve(int frameindex) {
        double[] val = new double[MAX_CYCL];
        double[] cyc = new double[MAX_CYCL];

        double mean = 0;
        double ct=0;

        for (int iy = 0; iy < MAX_CHAN; iy++) {

            mean = 0;
            ct = m_CTValue[iy][frameindex];
            double[] yData = new double[MAX_CYCL];
            for (int i = 0; i < m_Size; i++) {
                yData[i] = m_yData[iy][frameindex][i];
            }

            int size = m_Size;

            if (size < 20 || ct < 5) continue;

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
                k[frameindex][iy] =
                        y[size - 1] / 130;            // a little smaller to ensure better converge
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

                yData[i] -= mean;
                val[i] += cheat_factor * (yData[i] - val[i]) * k[frameindex][iy] / 8;        // Some cheating :)
            }

            for (int i = 0; i < size; i++) {
//                    m_zData[iy, frameindex, i] = val[i];
                m_zData[iy][frameindex][i] = val[i] * 50 / k[frameindex][iy];
            }
        }
    }

    int RSIZE = 9;

    public int curvefit(int well, int color, int iter, int size) {
        double delta_ko = 0;
        double delta_ro = 0;
        double delta_to = 0;

        for (int j = 0; j < iter; j++) {

            delta_k = delta_r = delta_t = 0;

//#if ALL_SEL
            if (ALL_SEL) {
               /* for (int i = 3; i < size; i++) {
                    jacob(x[i], y[i], k[well][color], r[well][color], t[well][color],
                            y[size - 1], rate);
                }*/
            } else {
//#else
                int rsize = RSIZE;        // reduced size;
                int[] rindx = new int[RSIZE];

                if (rsize > size) rsize = size;

                for (int i = 0; i < rsize; i++) {

                    //rindx[i] =rand() % (size - 3) + 3;

                    rindx[i] = ran.nextInt(size - 3) + 3;
                }

                for (int i = 0; i < rsize; i++) {
                    jacob(x[rindx[i]], y[rindx[i]], k[well][color], r[well][color], t[well][color],
                            y[size - 1], fit_count[well][color]);
                }
            }
//#endif

//#if GD_MOMENTUM
            if (GD_MOMENTUM) {
                delta_k += 0.8 * delta_ko;
                delta_r += 0.8 * delta_ro;
                delta_t += 0.8 * delta_to;
            }
//#endif

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

        if (ct > MIN_CT && ct <= size)
            m_CTValue[color][well] = ct;

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


    public void CalculateCT() {
        //if(IsCT() > 0 && !m_bThChange) return;		//CT valus have been calculated,need not been calculated again.
        // No, always recalc initial Ct.

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < numWells; j++) {
                double[] yData = new double[MAX_CYCL];
                for (int n = 0; n < m_Size; n++) {
                    yData[n] = m_yData[i][j][n];
                }

                int size = m_Size;
                int k;

//#if DIS_FACTOR

//			for (k = 0; k < size; k++) {
//				yData[k] /= ifactor[i, k];
//			}
//#endif

                List<Double> tempData = new ArrayList<>();
                if (size < MIN_CT)//data has not enough to perform Ct calculation
                    continue;
                for (int n = 3; n < MIN_CT; n++) {
                    tempData.add(yData[n]);
                }
                double sum = 0;
                for (int s = 0; s < tempData.size(); s++) {
                    sum += tempData.get(s);
                }


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
                    if (tempData.get(v) - mean > 2 * stdev || tempData.get(v) - mean < -2 * stdev)
                        tempData.set(v, mean);

                }

                //sum = std::accumulate(std::begin(tempData), std::end(tempData), 0.0);
                //mean = sum / tempData.size(); //mean

                sum = 0;
                for (int s = 0; s < tempData.size(); s++) {
                    sum += tempData.get(s);
                }
                mean = sum / tempData.size();

                accum = 0.0;

                //std::for_each(std::begin(tempData), std::end(tempData), [&](const double d) {
                //    accum += (d - mean)*(d - mean);
                //});
                //stdev = sqrt(accum / tempData.size()); //方差


                for (int t = 0; t < tempData.size(); t++) {
                    accum += (tempData.get(t) - mean) * (tempData.get(t) - mean);
                }
                stdev = Math.sqrt(accum / tempData.size()); //方差

                //===============================================

                //			double yvalue=stdev * CT_TH_MULTI + mean;

                double yvalue = stdev * log_threshold[i] * CT_TH_MULTI * 10 + mean;

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
                    m_CTValue[i][j] =
                            index - (yData[index] - yvalue) / (yData[index] - yData[index - 1]);
                    m_CTValue[i][j] = m_CTValue[i][j] > 0 ? m_CTValue[i][j] : 0;
                }
            }


        }
    }

}