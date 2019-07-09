package com.jz.experiment.module.report;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShowMet;
import com.jz.experiment.chart.CCurveShowPolyFit;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.chart.WindChart;
import com.jz.experiment.module.analyze.CtFragment;
import com.jz.experiment.module.data.ExpeDataFragment;
import com.jz.experiment.module.report.bean.InputParams;
import com.jz.experiment.util.PdfGenerator;
import com.jz.experiment.util.SysPrintUtil;
import com.jz.experiment.widget.A4PageLayout;
import com.jz.experiment.widget.PrintTailLayout;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.utils.DateUtil;
import com.wind.data.expe.bean.HistoryExperiment;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PcrPrintPreviewFragment extends CtFragment {

   // public static final String ARG_KEY_EXPE = "arg_key_expe";
    public static final String ARG_KEY_CT_PARAM = "arg_key_ct_param";
    @BindView(R.id.layout_print_tail)
    PrintTailLayout layout_print_tail;
    @BindView(R.id.layout_a4)
    A4PageLayout layout_a4;
    @BindView(R.id.chart)
    LineChart chart;
    /* @BindView(R.id.rv)
     RecyclerView rv;
     StageAdapter mStageAdapter;*/
     @BindView(R.id.tv_expe_type)
    TextView tv_expe_type;
    WindChart windChart;
    HistoryExperiment mExperiment;

    InputParams mInputParams;

    public static PcrPrintPreviewFragment newInstance(HistoryExperiment experiment, InputParams params) {
        PcrPrintPreviewFragment f = new PcrPrintPreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ExpeDataFragment.ARGS_KEY_EXPE, experiment);
        args.putParcelable(ARG_KEY_CT_PARAM, params);
        f.setArguments(args);
        return f;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_pcr_print_preview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        //mExperiment = getArguments().getParcelable(ARG_KEY_EXPE);
        mInputParams = getArguments().getParcelable(ARG_KEY_CT_PARAM);

       /* LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(manager);
        rv.setNestedScrollingEnabled(false);

        //动态计算stage宽度
        boolean isLandscape = ActivityUtil.isLandscape(getActivity());
        int stageItemWidth;
        if (isLandscape) {
            stageItemWidth = AppUtil.getScreenWidth(getActivity()) / 4;
        } else {
            stageItemWidth = DisplayUtil.dip2px(getActivity(), 120);
        }

        mStageAdapter = new StageAdapter(getActivity(), stageItemWidth);
        rv.setAdapter(mStageAdapter);*/


        if (isPcrMode()){
            tv_expe_type.setText(getString(R.string.setup_mode_dt));
        }else {
            tv_expe_type.setText(getString(R.string.setup_mode_melting));
        }

        initChart();
      /*  if (mExperiment != null && mExperiment.getSettingSecondInfo() != null) {
            int size = mExperiment.getSettingSecondInfo().getModes().size();
            List<DisplayItem> list = new ArrayList<>();
            List<Stage> stageList = mExperiment.getSettingSecondInfo().getSteps();
            list.addAll(stageList);
            mStageAdapter.replace(list);
        }*/


    }


    @Override
    protected boolean isPcrMode() {
        return mInputParams.getExpeType() == InputParams.EXPE_PCR;
    }


    private void initChart() {
        int totalCyclingCount = 0;
        String sourcePath = mInputParams.getSourceDataPath();
        if (mExperiment != null && mExperiment.getSettingSecondInfo() != null) {
            List<Stage> cyclingSteps = mExperiment.getSettingSecondInfo().getCyclingSteps();
            for (int i = 0; i < cyclingSteps.size(); i++) {
                CyclingStage cyclingStage = (CyclingStage) cyclingSteps.get(i);
                boolean pic = false;
                for (PartStage partStage : cyclingStage.getPartStageList()) {
                    if (partStage.isTakePic()) {
                        pic = true;
                        break;
                    }
                }
                if (pic) {
                    totalCyclingCount += cyclingStage.getCyclingCount();
                }
            }
        } else {
            if (CommData.diclist != null) {
                for (int i = 1; i <= 4; i++) {
                    if (CommData.diclist.get("Chip#" + i) != null) {
                        totalCyclingCount = CommData.diclist.get("Chip#" + i).size() / 12;
                        if (totalCyclingCount > 0) {
                            break;
                        }
                    }
                }
            } else {
                totalCyclingCount = 60;
            }

        }
      //  initChanAndKs();
        KSList=mInputParams.getKsList();
        ChanList=mInputParams.getChanList();
        if (isPcrMode()) {
            windChart = new DtChart(chart, totalCyclingCount);
        } else {
            windChart = new MeltingChart(chart);
            ((MeltingChart) windChart).setStartTemp(40);
            ((MeltingChart) windChart).setAxisMinimum(40);
        }
        //文件读取之后孔数已经有值
        windChart.show(ChanList, KSList, new File(sourcePath),
                mInputParams.getCtParam());
        double[][] ctValues;
        boolean[][] falsePositive;
        if (!isPcrMode()) {
            ctValues = CCurveShowMet.getInstance().m_CTValue;
            falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        } else {
            ctValues = CCurveShowPolyFit.getInstance().m_CTValue;
            falsePositive = CCurveShowPolyFit.getInstance().m_falsePositive;
        }
        //孔数已经放在数据文件中，不在存放在/anitoa/trim目录下
       // KSList.clear();
        //KSList = Well.getWell().getKsList();

        //获取CT value
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks, ctValues, falsePositive);
            }
        }

        mChannelDataAdapters[0].notifyDataSetChanged();
        mChannelDataAdapters[1].notifyDataSetChanged();
    }


    public void print() {

       // ScrollView sv= (ScrollView) layout_a4.getChildAt(0);
        LoadingDialogHelper.showOpLoading(getActivity());
        long time = System.currentTimeMillis();
        String prefix = DateUtil.get(time, "yyyy_MM_dd_HH_mm_ss");
        String tag;
        if (isPcrMode()) {
            tag = getString(R.string.setup_mode_dt);
        } else {
            tag = getString(R.string.setup_mode_melting);
        }

        layout_print_tail.setReportDate(DateUtil.get(time, "yyyy-MM-dd HH:mm:ss"));
        String pdfName = prefix + tag + ".pdf";
        PdfGenerator.generatePdf(layout_a4, pdfName, new OnPdfGenerateListener() {
            @Override
            public void onGeneratePdfSuccess(String path) {
                SysPrintUtil.printPdf(getActivity(), path);
                //TODO 执行打印
                LoadingDialogHelper.hideOpLoading();

            }

            @Override
            public void onGeneratePdfError() {
                LoadingDialogHelper.hideOpLoading();
            }
        });
    }
}
