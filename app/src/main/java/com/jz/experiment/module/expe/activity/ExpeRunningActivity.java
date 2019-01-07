package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.wind.data.expe.bean.ChartData;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.widget.ChartMarkerView;
import com.jz.experiment.widget.DuringView;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.ColorfulEntry;
import com.wind.data.expe.bean.HistoryExperiment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExpeRunningActivity extends BaseActivity {
    public static final int WHAT_REFRESH_CHART=1;

    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, ExpeRunningActivity.class, experiment);
    }

    @BindView(R.id.chart)
    LineChart chart;
    ChartMarkerView mChartMarkerView;
    LineData mLineData;
    ArrayList<ILineDataSet> mDataSets;

    @BindView(R.id.tv_cur_mode)
    TextView tv_cur_mode;
    @BindView(R.id.tv_duration)
    DuringView tv_duration;
    private List<Integer> mLineColors;
    private HistoryExperiment mHistoryExperiment;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("运行");
        mTitleBar.setRightText("筛选");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expe_running);
        ButterKnife.bind(this);
        mHistoryExperiment = Navigator.getParcelableExtra(this);

        tv_cur_mode.setText("当前模式：变温扩增");
        tv_duration.start();
        mDataSets = new ArrayList<>();
        mLineColors=new ArrayList<>();
        for (int i = 1; i <= 4; i++) {//4组数据

            List<Entry> expeData = new ArrayList<>();

            for (int j = 0; j < 10; j++) {
                float y = 0;

                if (i == 1) {
                    y = j;
                } else if (i == 2) {
                    y = j * 10;
                } else if (i == 3) {
                    y = (float) Math.pow(j, 2);
                } else if (i == 4) {
                    y = j * 20;
                }
                Entry entry = new Entry(j, y);
                expeData.add(entry);
            }

            LineDataSet dataSet = new LineDataSet(expeData, "通道" + i);
            int color=-1;
            switch (i) {
                case 1:
                    color=Color.parseColor("#355ABB");
                    dataSet.setColor(color);
                    break;
                case 2:
                    dataSet.setColor(color=Color.parseColor("#1F994A"));
                    break;
                case 3:
                    dataSet.setColor(color=Color.parseColor("#DBAE11"));
                    break;
                case 4:
                    dataSet.setColor(color=Color.parseColor("#F13B3B"));
                    break;
            }
            dataSet.setDrawCircles(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);
            mLineColors.add(color);
            mDataSets.add(dataSet);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 设置X轴的位置
        xAxis.setDrawGridLines(false); // 效果如下图
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setDrawGridLines(false);


        Description description = new Description();
        description.setEnabled(false);
        chart.setDescription(description);
        Legend legend = chart.getLegend();
        // legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        mLineData = new LineData(mDataSets);

        chart.setMarker(mChartMarkerView=new ChartMarkerView(getActivity(), new ChartMarkerView.OnPointSelectedListener() {
            @Override
            public void onPointSelected(Entry e) {

                int index = -1;
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    index = lineDataSet.getEntryIndex(e);
                    if (index != -1) {
                        break;
                    }
                }
                if (index == -1) {
                    return;
                }
                List<ColorfulEntry> entries=new ArrayList<>();
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    Entry entry = lineDataSet.getEntryForIndex(index);
                    ColorfulEntry colorfulEntry=new ColorfulEntry();
                    colorfulEntry.setEntry(entry);
                    colorfulEntry.setColor(mLineColors.get(i));
                    entries.add(colorfulEntry);

                }

                mChartMarkerView.getAdapter().replaceAll(entries);

            }
        }));
        chart.setTouchEnabled(false);
        chart.setDrawBorders(false);
        chart.setData(mLineData);
        chart.invalidate(); // refresh


        Thread thread = new Thread(mRun);
        thread.start();
    }

    private int i = 10;
    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            while (i < 100) {
                i++;
                for (int j = 1; j <= 4; j++) {
                    float y = 0;
                    if (j == 1) {
                        y = i;
                    } else if (j == 2) {
                        y = i * 10;
                    } else if (j == 3) {
                        y = (float) Math.pow(i, 2);
                    } else if (j == 4) {
                        y = i * 20;
                    }
                    Entry entry = new Entry(i, y);
                    mDataSets.get(j - 1).addEntry(entry);
                }

                mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mLineData.notifyDataChanged();
            chart.notifyDataSetChanged(); // let the chart know it's data changed
            chart.invalidate(); // refresh
        }
    };


    @OnClick({R.id.tv_stop})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_stop:
                //下一步
                //数据
                //变增扩温曲线数据
                //TODO 溶解曲线曲线数据
                ChartData chartData = new ChartData();
                List<com.wind.data.expe.bean.LineData> lineDataList = new ArrayList<>();
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    List<Entry> entries = lineDataSet.getValues();
                    com.wind.data.expe.bean.LineData lineData = new com.wind.data.expe.bean.LineData();
                    lineData.setEntries(entries);
                    lineData.setColor(mLineColors.get(i));
                    lineDataList.add(lineData);

                }
                chartData.setLineDataList(lineDataList);


                mHistoryExperiment.setDtChartData(chartData);
                Tab tab = new Tab();
                tab.setIndex(MainActivity.TAB_INDEX_DATA);
                tab.setExtra(mHistoryExperiment);
                MainActivity.start(getActivity(), tab);
                break;
        }
    }

}
