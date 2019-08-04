package com.jz.experiment.module.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShowPolyFit;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.CurveReader;
import com.jz.experiment.chart.DataFileReader;
import com.jz.experiment.chart.StandardCurveChart;
import com.jz.experiment.module.data.adapter.SampleStatefulAdapter;
import com.jz.experiment.module.data.adapter.SeqAdapter;
import com.jz.experiment.module.data.adapter.TableAdapter;
import com.jz.experiment.module.data.adapter.TableUnknowAdapter;
import com.jz.experiment.module.data.bean.SampleRow;
import com.jz.experiment.module.report.PcrPrintPreviewFragment;
import com.jz.experiment.module.report.StandardCurvePrintPreviewActivity;
import com.jz.experiment.module.report.bean.InputParams;
import com.jz.experiment.module.report.bean.StdLineData;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.Utils;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Sample;
import com.wind.toastlib.ToastUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class StandardCurveFragment extends BaseFragment {
    @BindView(R.id.gv_a)
    GridView gv_a;
    @BindView(R.id.gv_b)
    GridView gv_b;
    @BindView(R.id.gv_seq)
    GridView gv_seq;
    @BindView(R.id.rg_point)
    RadioGroup rg_point;
    @BindView(R.id.rg_channel)
    RadioGroup rg_channel;
    @BindView(R.id.lv_standard)
    ListView lv_standard;
    @BindView(R.id.lv_unknow)
    ListView lv_unknow;
    HistoryExperiment mExeperiment;
    TableAdapter standardAdapter;
    TableUnknowAdapter unknowAdapter;

    @BindView(R.id.chart_standard)
    CombinedChart chart_standard;
    //LineChart chart_standard;

    @BindView(R.id.tv_equation)
    TextView tv_equation;
    @BindView(R.id.tv_r2)
    TextView tv_r2;

    @BindView(R.id.tv_y_desc)
    TextView tv_y_desc;
    @BindView(R.id.tv_x_desc)
    TextView tv_x_desc;
    public static StandardCurveFragment newInstance(HistoryExperiment experiment, InputParams inputParams) {
        StandardCurveFragment f = new StandardCurveFragment();
        Bundle args = new Bundle();
        args.putParcelable(ExpeDataFragment.ARGS_KEY_EXPE, experiment);
        args.putParcelable(PcrPrintPreviewFragment.ARG_KEY_CT_PARAM, inputParams);
        f.setArguments(args);
        return f;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_standard_curve;
    }

    StandardCurveChart mStandardChart;
    InputParams mInputParams;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        // mStandardChart = new StandardChart(chart_standard);
        mStandardChart = new StandardCurveChart(chart_standard);
        mExeperiment = getArguments().getParcelable(ExpeDataFragment.ARGS_KEY_EXPE);
        mInputParams = getArguments().getParcelable(PcrPrintPreviewFragment.ARG_KEY_CT_PARAM);

        List<Sample> samplesA = mExeperiment.getSettingsFirstInfo().getSamplesA();
        List<Sample> samplesB = mExeperiment.getSettingsFirstInfo().getSamplesB();
        for (int i = 0; i < samplesA.size(); i++) {
            samplesA.get(i).setSeq(i + 1);
        }
        for (int i = 0; i < samplesB.size(); i++) {
            samplesB.get(i).setSeq(i + 1);
        }

        rg_channel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                List<Sample> samplesA = mSampleStatefulAdapterA.getData();
                for (Sample sample : samplesA) {
                    sample.setStatus(Sample.CODE_DEFAULT);
                }
                List<Sample> samplesB = mSampleStatefulAdapterB.getData();
                for (Sample sample : samplesB) {
                    sample.setStatus(Sample.CODE_DEFAULT);
                }
                mSampleStatefulAdapterA.notifyDataSetChanged();
                mSampleStatefulAdapterB.notifyDataSetChanged();
                standardAdapter.clear();
                standardAdapter.add(new SampleRow());
                unknowAdapter.clear();
                unknowAdapter.add(new SampleRow());

                //清除标准曲线
                mStandardChart.clear();
                tv_equation.setText("");
                tv_r2.setText("");
            }
        });


        initSamples();


        initStandardTable();
        initUnknowTable();

        chart_standard.post(new Runnable() {
            @Override
            public void run() {
                LoadingDialogHelper.showOpLoading(getActivity());
                loadDtFile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                LoadingDialogHelper.hideOpLoading();
                            }
                        });
            }
        });

        String conc=getActivity().getString(R.string.concentration);

        tv_y_desc.setText("log("+conc+")");
        tv_y_desc.setVisibility(View.GONE);

        tv_x_desc.setText("Ct");
        tv_x_desc.setVisibility(View.GONE);


    }

    private Observable<Boolean> loadDtFile() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                //重新读取生成ct值
                InputStream ips = null;
                try {
                    ips = new FileInputStream(DataFileUtil.getDtImageDataFile(mExeperiment));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DataFileReader.getInstance().ReadFileData(ips, false);
                CommData.m_factorData = DataFileReader.getInstance().factorValue;
                CurveReader.getInstance().readCurve(CommData.m_factorData, mInputParams.getCtParam(), false);
                subscriber.onNext(true);
            }
        });
    }

    private void initUnknowTable() {
        unknowAdapter = new TableUnknowAdapter(getContext(), R.layout.item_unknow_sample_table);
        lv_unknow.setAdapter(unknowAdapter);
        SampleRow titleRow = new SampleRow();
        List<SampleRow> rows = new ArrayList<>();
        rows.add(titleRow);
       /* for (int i=0;i<=5;i++) {
            SampleRow row=new SampleRow();
            //row.setType(getString(R.string.standard_point));
            rows.add(row);
        }*/
        unknowAdapter.replace(rows);
    }

    private void initStandardTable() {
        standardAdapter = new TableAdapter(getContext(), R.layout.item_sample_table);
        lv_standard.setAdapter(standardAdapter);
        SampleRow titleRow = new SampleRow();
        List<SampleRow> rows = new ArrayList<>();
        rows.add(titleRow);
        /*for (int i=0;i<=5;i++) {
            SampleRow row=new SampleRow();
            //row.setType(getString(R.string.standard_point));
            rows.add(row);
        }*/
        standardAdapter.replace(rows);

    }

    private SampleStatefulAdapter mSampleStatefulAdapterA;
    private SampleStatefulAdapter mSampleStatefulAdapterB;

    private void initSamples() {
        SeqAdapter seqAdapter = new SeqAdapter(getActivity(), R.layout.item_sample_seq);
        gv_seq.setAdapter(seqAdapter);
        List<String> seqList = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            seqList.add(i + "");
        }
        seqAdapter.replace(seqList);

        mSampleStatefulAdapterA = new SampleStatefulAdapter(getActivity(), R.layout.item_standard_sample);
        gv_a.setAdapter(mSampleStatefulAdapterA);
        mSampleStatefulAdapterA.replace(mExeperiment.getSettingsFirstInfo().getSamplesA());
        gv_a.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Sample sample = mSampleStatefulAdapterA.getItem(position);

                changeSampleStatus(sample, mSampleStatefulAdapterA);


            }
        });

        mSampleStatefulAdapterB = new SampleStatefulAdapter(getActivity(), R.layout.item_standard_sample);
        gv_b.setAdapter(mSampleStatefulAdapterB);
        mSampleStatefulAdapterB.replace(mExeperiment.getSettingsFirstInfo().getSamplesB());
        gv_b.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sample sample = mSampleStatefulAdapterB.getItem(position);
                changeSampleStatus(sample, mSampleStatefulAdapterB);
            }
        });
    }

    private void changeSampleStatus(Sample sample, SampleStatefulAdapter adapter) {
        int checkedId = rg_point.getCheckedRadioButtonId();

        switch (checkedId) {
            case R.id.rb_standard:

                if (sample.getStatus() == Sample.CODE_STANDARD) {
                    sample.setStatus(Sample.CODE_DEFAULT);
                    //从表格中删除
                    SampleRow row = findSampleRow(sample, standardAdapter.getData());
                    standardAdapter.remove(row);
                } else {
                    if (sample.getStatus() == Sample.CODE_DEFAULT) {

                        //添加到表格

                        SampleRow sampleRow = buildSampleRow(sample);
                        //获取ct值
                        double[][] ctValues = CCurveShowPolyFit.getInstance().m_CTValue;
                        boolean[][] falsePositive = CCurveShowPolyFit.getInstance().m_falsePositive;
                        String chan = getChannelName();
                        try {
                            //TODO 获取对应的ctValue
                            String ctVal = Utils.getCtValue(getActivity(), chan, sample.getSeqName(), ctValues, falsePositive);
                            sampleRow.setCtValue(ctVal);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        sample.setStatus(Sample.CODE_STANDARD);
                        standardAdapter.add(sampleRow);
                    }

                }


                break;
            case R.id.rb_unknow:
                if (sample.getStatus() == Sample.CODE_UNKWON) {
                    sample.setStatus(Sample.CODE_DEFAULT);
                    //从表格中删除
                    SampleRow row = findSampleRow(sample, unknowAdapter.getData());
                    unknowAdapter.remove(row);
                } else {
                    if (sample.getStatus() == Sample.CODE_DEFAULT) {

                        //添加到表格
                        SampleRow sampleRow = buildSampleRow(sample);
                        //获取ct值
                        double[][] ctValues = CCurveShowPolyFit.getInstance().m_CTValue;
                        boolean[][] falsePositive = CCurveShowPolyFit.getInstance().m_falsePositive;
                        String chan = getChannelName();
                        try {
                            //TODO 获取对应的ctValue
                            String ctVal = Utils.getCtValue(getActivity(), chan, sample.getSeqName(), ctValues, falsePositive);
                            sampleRow.setCtValue(ctVal);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }


                        sample.setStatus(Sample.CODE_UNKWON);
                        unknowAdapter.add(sampleRow);
                    }
                }
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private String getChannelName() {
        int channelCheckedId = rg_channel.getCheckedRadioButtonId();
        String chan;
        switch (channelCheckedId) {
            case R.id.rb_channel_1:
                chan = "Chip#1";
                break;
            case R.id.rb_channel_2:
                chan = "Chip#2";
                break;
            case R.id.rb_channel_3:
                chan = "Chip#3";
                break;
            case R.id.rb_channel_4:
                chan = "Chip#4";
                break;
            default:
                chan = "Chip#1";
                break;
        }
        return chan;
    }

    private SampleRow buildSampleRow(Sample sample) {
        SampleRow sampleRow = new SampleRow();
        sampleRow.setName(sample.getSeqName());
        sampleRow.setConcentration("");
        return sampleRow;
    }

   /* private String getSampleName(Sample sample){
        String name=sample.getName();
        if (TextUtils.isEmpty(name)){
            String type=sample.getType()==Sample.TYPE_A?"A":"B";
            sample.getSeq()
        }
    }*/

    private SampleRow findSampleRow(Sample sample, List<SampleRow> rows) {
        for (int i = 0; i < rows.size(); i++) {
            SampleRow row = rows.get(i);
            if (sample.getSeqName().equals(row.getName())) {
                return row;
            }
        }
        return null;
    }


    @OnClick({R.id.tv_draw_std, R.id.tv_find_unknow})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_draw_std:
                if (standardAdapter.getCount()<=1){
                    return;
                }
                boolean empty=false;
                for (int i=1;i<standardAdapter.getCount();i++){
                    String conc=standardAdapter.getItem(i).getConcentration();
                    if (TextUtils.isEmpty(conc)){
                        empty=true;
                        break;
                    }
                }
                if (empty){
                    ToastUtil.showToast(getActivity(),R.string.standard_input_concentration);
                    return;
                }
                drawStdCurve();
                break;
            case R.id.tv_find_unknow:

                if (unknowAdapter.getCount()<=1){
                    return;
                }
                List<SampleRow> unknow = new ArrayList<>(unknowAdapter.getData());
                unknow.remove(0);


                //获取直线方程，计算浓度
                //重新直线方程
                double[] coefficients = polyFitStdCurve();
                if (coefficients == null) {
                    return;
                }
                for (int i = 0; i < unknow.size(); i++) {
                    double ct = 0;
                    SampleRow sampleRow = unknow.get(i);
                    try {
                        ct = Double.parseDouble(sampleRow.getCtValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(getActivity(), R.string.standard_illegal);
                        return;
                    }


                    double conc = Utils.getPolyY(coefficients, ct);
                    DecimalFormat format = new DecimalFormat("#.##");
                    sampleRow.setConcentration(format.format(Math.pow(10, conc)));
                }
                unknowAdapter.notifyDataSetChanged();
                break;
        }
    }


    private double[] polyFitStdCurve() {
        List<SampleRow> stdRows = new ArrayList<>(standardAdapter.getData());
        stdRows.remove(0);

        int size = stdRows.size();
        double[] yy = new double[size];
        double[] xx = new double[size];

        for (int i = 0; i < size; i++) {
            try {
                xx[i] = Double.parseDouble(stdRows.get(i).getCtValue());
                double conc = Double.parseDouble(stdRows.get(i).getConcentration());
                yy[i] = Math.log10(conc);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.showToast(getActivity(), R.string.standard_illegal);
                return null;
            }

        }


        double[] coefficients = Utils.getPolyCoefficients(1, xx, yy);
        return coefficients;

    }

    private StdLineData mStdLineData;

    private void drawStdCurve() {


        List<SampleRow> stdRows = new ArrayList<>(standardAdapter.getData());
        stdRows.remove(0);

        int size = stdRows.size();
        double[] yy = new double[size];
        double[] xx = new double[size];


        double maxX = 0;
        for (int i = 0; i < size; i++) {

            try {
                xx[i] = Double.parseDouble(stdRows.get(i).getCtValue());
                if (xx[i] > maxX) {
                    maxX = xx[i];
                }
                double conc = Double.parseDouble(stdRows.get(i).getConcentration());
                yy[i] = Math.log10(conc);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.showToast(getActivity(), R.string.standard_illegal);
                return;
            }

        }
       /*
        for (int i = 0; i < 20; i++) {
            fitxx[i] = (double) (20 + i);
        }*/
        //生成的标准曲线的点数
        int intMax = (int) (maxX + 10);
        double[] fitxx = new double[intMax];
        for (int i = 0; i < intMax; i++) {
            fitxx[i] = (double) (i);
        }


        double[] fityy = Utils.getPolyfit(1, xx, yy, fitxx);

        double[] coefficients = Utils.getPolyCoefficients(1, xx, yy);

        StringBuilder equation = new StringBuilder();
        equation.append("y=");

        DecimalFormat format = new DecimalFormat("#.##");
        String xCoefficient = format.format(coefficients[1]);

        equation.append(xCoefficient + "x");

        String constCoefficient = format.format(coefficients[0]);
        if (coefficients[0] >= 0) {
            equation.append("+");
        }
        equation.append(constCoefficient);
        tv_equation.setText(equation.toString());
        //TODO 计算相关系数
        double R2 = caculateR(xx, yy);
        format = new DecimalFormat("#.######");
        String nR2 = "R<sup>2</sup>:" + format.format(R2);
        tv_r2.setText(Html.fromHtml(nR2));


        //计算未知点
        List<SampleRow> unknownRows = new ArrayList<>(unknowAdapter.getData());
        unknownRows.remove(0);
        size = unknownRows.size();
        double[] unknownYY = new double[size];
        double[] unknownXX = new double[size];


        for (int i = 0; i < unknownRows.size(); i++) {
            double ct = 0;
            SampleRow sampleRow = unknownRows.get(i);
            try {
                ct = Double.parseDouble(sampleRow.getCtValue());
                unknownXX[i] = ct;
                double conc = Utils.getPolyY(coefficients, ct);
                sampleRow.setConcentration(format.format(Math.pow(10, conc)));
                unknownYY[i] = conc;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mStdLineData = new StdLineData(fitxx, fityy, xx, yy, unknownXX, unknownYY, equation.toString(), format.format(R2));
        //

        mStandardChart.addPoints(fitxx, fityy, xx, yy, unknownXX, unknownYY);



        tv_y_desc.setVisibility(View.VISIBLE);
        tv_x_desc.setVisibility(View.VISIBLE);
    }

    private double caculateR(double[] xx, double[] yy) {

        double avgX = sumArray(xx) / xx.length;
        double avgY = sumArray(yy) / yy.length;

        double sumAvg = 0;
        for (int i = 0; i < xx.length; i++) {
            sumAvg += (xx[i] - avgX) * (yy[i] - avgY);
        }

        double sumX2 = 0;
        for (int i = 0; i < xx.length; i++) {
            sumX2 += (xx[i] - avgX) * (xx[i] - avgX);
        }
        double sumY2 = 0;
        for (int i = 0; i < xx.length; i++) {
            sumY2 += (yy[i] - avgY) * (yy[i] - avgY);
        }

        double v = Math.sqrt(sumX2) * Math.sqrt(sumY2);
        //double v=sumX2*sumY2;
        double R2 = Math.pow(sumAvg / v, 2);
        return R2;
    }

    public double sumArray(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public void toPrintPreview() {

        if (mStdLineData != null) {
            List<SampleRow> stdRows = new ArrayList<>(standardAdapter.getData());
            List<SampleRow> unknownRows = new ArrayList<>(unknowAdapter.getData());


            mStdLineData.setStdRows(stdRows);
            mStdLineData.setUnknownRows(unknownRows);

            StandardCurvePrintPreviewActivity.start(getActivity(), mStdLineData);
        }


    }
}
