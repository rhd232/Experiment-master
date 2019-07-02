package com.jz.experiment.chart;

public class CCurveShowMet {
    int numWells = 4;
    int MIN_CT = 12;//之前15 // minimal allowed CT
    int CT_TH_MULTI = 5;// 5 instead of 10, this is pre calc of Ct anyway


    //float log_threshold = 0.11f;
    float cheat_factor = 0.06f;
    float cheat_factor_org = 0.06f;
    boolean hide_org = true;

    public static int MAX_CHAN = 4;
    public static int MAX_WELL = 16;//之前是8;
    public static int MAX_CYCL = 301;//400
    public double[][][] m_yData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][][] m_zData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][][] m_zdData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
    public double[][] m_CTValue = new double[MAX_CHAN][MAX_WELL];
    public double[][] mtemp = new double[MAX_CHAN][MAX_CYCL];
    double[][] m_xAxis = new double[MAX_CHAN][MAX_CYCL];

    double[] x = new double[MAX_CYCL];
    double[] y = new double[MAX_CYCL];
    double[][] k = new double[MAX_WELL][4];
    double[][] r = new double[MAX_WELL][4];
    double[][] t = new double[MAX_WELL][4];
    double delta_k, delta_r, delta_t;
    int[][] fit_count = new int[MAX_WELL][4];
    public int[] m_Size = new int[MAX_CHAN];

    public double[][] ifactor = new double[MAX_CHAN][MAX_CYCL];


    float[] log_threshold = new float[]{0.11f, 0.11f, 0.11f, 0.11f};

    float[] ct_offset = new float[4];
    public float start_temp = 60;
    private static CCurveShowMet INSTANCE = new CCurveShowMet();

    public static CCurveShowMet getInstance() {
        return INSTANCE;
    }

    private CCurveShowMet() {
    }

    public void InitData() {
        numWells = CommData.KsIndex;
        int i, j;
        for (i = 0; i < MAX_CHAN; i++) {
            for (j = 0; j < numWells; j++) {
                m_CTValue[i][j] = 0;
            }
        }

        m_yData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_zData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_zdData = new double[MAX_CHAN][MAX_WELL][MAX_CYCL];
        m_CTValue = new double[MAX_CHAN][MAX_WELL];
        mtemp = new double[MAX_CHAN][MAX_CYCL];
        m_xAxis = new double[MAX_CHAN][MAX_CYCL];

        x = new double[MAX_CYCL];
        y = new double[MAX_CYCL];
        k = new double[MAX_WELL][4];
        r = new double[MAX_WELL][4];
        t = new double[MAX_WELL][4];
    }


    public void UpdateAllcurve() {
        for (int iy = 0; iy < MAX_CHAN; iy++) {
            int datasize = m_Size[iy];
            int i;
            int size = m_Size[iy];
            if (size ==0){
                continue;
            }
            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                double[] yData = new double[MAX_CYCL];

                for (i = 0; i < datasize; i++) {
                    yData[i] = m_yData[iy][frameindex][i];
                }

                double[] z = new double[MAX_CYCL];
                double[] y = new double[MAX_CYCL];

                double eta = 0.2;

                z[0] = yData[0];

                for (i = 1; i < size; i++) {
                    z[i] = z[i - 1] + eta * (yData[i] - z[i - 1]);
                }

                for (i = 0; i < size; i++) {
                    y[i] = z[i];
                }

                for (i = size - 2; i >= 0; i--) {
                    z[i] = z[i + 1] + eta * (y[i] - z[i + 1]);
                }

                for (i = 0; i < size; i++) {
                    m_zData[iy][frameindex][i] = z[i];
                }
            }

            for (i = 0; i < size; i++) {
                m_xAxis[iy][i] = mtemp[iy][i];
            }

            for (int frameindex = 0; frameindex < numWells; frameindex++) {
                DrawMeltCurve(iy, frameindex);
            }
        }
    }

    public void DrawMeltCurve(int iy, int frameindex) {

        double[] yData = new double[MAX_CYCL];
        double[] y = new double[MAX_CYCL];
        double[] mt = new double[MAX_CYCL];

        int size = m_Size[iy];
        int i;

        for (i = 0; i < size; i++) {
            yData[i] = m_yData[iy][frameindex][i];
        }

        //==========factor for int time=================

        //            for (int k = 0; k < size; k++)
        //           {
        //               yData[k] /= ifactor[iy, k];
        //            }

        for (i = 0; i < size; i++) {
            y[i] = yData[i];
            mt[i] = mtemp[iy][i];
        }

        // Melt process

        double[] z = new double[MAX_CYCL];
        double eta = 0.2;

        z[0] = y[0];

        for (i = 1; i < size; i++) {
            z[i] = z[i - 1] + eta * (y[i] - z[i - 1]);
        }

        for (i = 0; i < size; i++) {
            y[i] = z[i];
        }

        for (i = size - 2; i >= 0; i--) {
            z[i] = z[i + 1] + eta * (y[i] - z[i + 1]);
        }

        for (i = 0; i < size; i++) {
            y[i] = z[i];
        }

        z[0] = 5;

        for (i = 1; i < size; i++) {
            z[i] = -10 * (y[i] - y[i - 1]);
        }

        for (i = 0; i < size; i++) {
            y[i] = z[i] * 4;
        }

        //====================Filter the differential =================

        z[0] = y[0];

        for (i = 1; i < size; i++) {
            z[i] = z[i - 1] + eta * (y[i] - z[i - 1]);
        }

        for (i = 0; i < size; i++) {
            y[i] = z[i];
        }

        for (i = size - 2; i >= 0; i--) {
            z[i] = z[i + 1] + eta * (y[i] - z[i + 1]);
        }


        int maxi = 0;

        while (mt[maxi] < start_temp && maxi < size - 10)
        {
            maxi++;
        }


        double max = z[maxi];
        for (i = maxi+1; i < size; i++) {
            if (z[i] > max) {
                max = z[i];
                maxi = i;
            }
        }
        try {

            double mtemp_n = 0;

            if (maxi == 0)
            {
                mtemp_n = mt[0];
            }
                /*else if (maxi >= size - 1)
                {
                    mtemp_n = mt[maxi];
                }*/
            else if (maxi >= size - 2)          // This is because the last point is discarded in display
            {
                mtemp_n = mt[maxi - 1];
            }
            else {

                double left_slop = (z[maxi] - z[maxi - 1]) / (mt[maxi] - mt[maxi - 1]);
                double right_slop = (z[maxi + 1] - z[maxi]) / (mt[maxi + 1] - mt[maxi]);
                double percent = -left_slop / (right_slop - left_slop);
                 mtemp_n = mt[maxi - 1] + percent * (mt[maxi + 1] - mt[maxi]);
            }
            m_CTValue[iy][frameindex] = mtemp_n;
        } catch (Exception ex) {
            m_CTValue[iy][frameindex] = 0;
        }


        for (i = 0; i < size; i++) {
            m_zdData[iy][frameindex][i] = z[i];
        }
    }

}