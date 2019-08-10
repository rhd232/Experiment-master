package com.jz.experiment.module.report;

import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.jz.experiment.R;
import com.jz.experiment.chart.StandardCurveChart;
import com.jz.experiment.module.data.adapter.TableAdapter;
import com.jz.experiment.module.data.adapter.TableUnknowAdapter;
import com.jz.experiment.module.data.bean.SampleRow;
import com.jz.experiment.module.report.bean.StdLineData;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.SysPrintUtil;
import com.jz.experiment.widget.A4PageLayout;
import com.jz.experiment.widget.PrintTailLayout;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.utils.A4Util;
import com.wind.base.utils.DateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class StdCurvePrintPreviewFragment extends BaseFragment {

    public static final String ARG_KEY_DATA = "arg_key_data";
    @BindView(R.id.chart_std)
    CombinedChart chart_std;
    @BindView(R.id.tv_equation)
    TextView tv_equation;
    @BindView(R.id.tv_r2)
    TextView tv_r2;
    @BindView(R.id.lv_standard)
    ListView lv_standard;
    @BindView(R.id.lv_unknow)
    ListView lv_unknow;
    @BindView(R.id.sv)
    ScrollView sv;
    @BindView(R.id.layout_a4)
    A4PageLayout layout_a4;
    @BindView(R.id.view_placeholder)
    View view_placeholder;

    @BindView(R.id.layout_print_tail)
    PrintTailLayout layout_print_tail;
    @BindView(R.id.tv_y_desc)
    TextView tv_y_desc;
    @BindView(R.id.tv_x_desc)
    TextView tv_x_desc;
    StandardCurveChart mStandardChart;
    TableAdapter standardAdapter;
    TableUnknowAdapter unknowAdapter;

    StdLineData data;

    public static StdCurvePrintPreviewFragment newInstance(StdLineData data) {
        StdCurvePrintPreviewFragment f = new StdCurvePrintPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY_DATA, data);
        f.setArguments(args);
        return f;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_std_curve_print;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mStandardChart = new StandardCurveChart(chart_std);

        //获取参数
        data = (StdLineData) getArguments().getSerializable(ARG_KEY_DATA);
        String R2 = "R<sup>2</sup>:" + data.getR2();
        tv_equation.setText(data.getEquation());
        tv_r2.setText(Html.fromHtml(R2));
        //-->标准点，未知点
        mStandardChart.addPoints(data.getFitedXX(), data.getFitedYY(),
                data.getStdXX(), data.getStdYY(),
                data.getUnknowXX(), data.getUnknowYY());

        //-->

        initUnknowTable();
        initStandardTable();

       // sv.getLayoutParams().height= AppUtil.getScreenHeight(getActivity());


       /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("sv:" + sv.getWidth() + "-"+sv.getHeight());
                System.out.println("layout_a4:" + layout_a4.getWidth()+ "-"+layout_a4.getHeight());
            }
        }, 1000);*/
        String conc=getActivity().getString(R.string.concentration);
        tv_x_desc.setText("log("+conc+")");
        tv_x_desc.setVisibility(View.GONE);

        tv_y_desc.setText("Ct");
        tv_y_desc.setVisibility(View.GONE);
    }


    private void initUnknowTable() {
        unknowAdapter = new TableUnknowAdapter(getContext(), R.layout.item_unknow_sample_table);
        lv_unknow.setAdapter(unknowAdapter);

        List<SampleRow> rows = data.getUnknownRows();

        unknowAdapter.replace(rows);
    }

    private void initStandardTable() {
        standardAdapter = new TableAdapter(getContext(), R.layout.item_sample_table);
        lv_standard.setAdapter(standardAdapter);
        standardAdapter.setConcentrationNeedInput(false);
        List<SampleRow> rows = data.getStdRows();
        standardAdapter.replace(rows);

    }

    public void print() {
        LoadingDialogHelper.showOpLoading(getActivity());
        generatePdf(new OnPdfGenerateListener() {
            @Override
            public void onGeneratePdfSuccess(String path) {
                SysPrintUtil.printPdf(getActivity(),path);
                //TODO 执行打印
                LoadingDialogHelper.hideOpLoading();

            }

            @Override
            public void onGeneratePdfError() {
                LoadingDialogHelper.hideOpLoading();
            }
        });
    }



    private void generatePdf(final OnPdfGenerateListener listener) {
        long time=System.currentTimeMillis();
        String prefix=DateUtil.get(time,"yyyy_MM_dd_HH_mm_ss");
        String tag=getString(R.string.standard_curve);
        layout_print_tail.setReportDate(DateUtil.get(time,"yyyy-MM-dd HH:mm:ss"));
        String pdfName = prefix+tag+".pdf";
        generatePdf(pdfName)
                //.subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String path) {
                        if (!TextUtils.isEmpty(path)) {
                            // ToastUtil.showToast(getActivity(),"输出成功");
                            listener.onGeneratePdfSuccess(path);
                        } else {
                            listener.onGeneratePdfError();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.onGeneratePdfError();
                    }
                });

    }


    private Observable<String> generatePdf(final String pdfName) {
        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                //ThreadUtil.sleep(1000);
                PdfDocument document = new PdfDocument();
                int width = layout_a4.getWidth();// AppUtil.getScreenWidth(getActivity());
                int height = 0;// AppUtil.getScreenHeight(getActivity());
                height = A4Util.getA4Height(getContext());

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                        .Builder(width, height, 1)
                        .create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                if (sv != null) {
                    sv.draw(canvas);
                }


                document.finishPage(page);
                File file = new File(DataFileUtil.getPdfFilePath(pdfName));

                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    document.writeTo(outputStream);
                    subscriber.onNext(file.getAbsolutePath());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                document.close();

            }
        });


    }
}
