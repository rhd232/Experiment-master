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
import com.jz.experiment.R;
import com.jz.experiment.widget.ChartMarkerView;
import com.jz.experiment.widget.DuringView;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExpeRunningActivity extends BaseActivity {

    public static void start(Context context){
        Navigator.navigate(context,ExpeRunningActivity.class);
    }

    @BindView(R.id.chart)
    LineChart chart;

    LineData mLineData;
    ArrayList<ILineDataSet> mDataSets;

    @BindView(R.id.tv_cur_mode)
    TextView tv_cur_mode;
    @BindView(R.id.tv_duration)
    DuringView tv_duration;
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
        tv_cur_mode.setText("当前模式：变温扩增");
        tv_duration.start();
        mDataSets = new ArrayList<>();
        for (int i=1;i<=4;i++){

            List<Entry> expeData=new ArrayList<>();

            for (int j=0;j<10;j++){
                float y=0;

                if (i==1){
                    y=j;
                }else if (i==2){
                    y=j*10;
                }else if (i==3){
                   y= (float) Math.pow(j,2);
                }else if (i==4){
                    y=j*20;
                }
                Entry entry=new Entry(j, y);
                expeData.add(entry);
            }

            LineDataSet dataSet = new LineDataSet(expeData, "通道"+i);
            switch (i){
                case 1:
                    dataSet.setColor(Color.parseColor("#355ABB"));
                    break;
                case 2:
                    dataSet.setColor(Color.parseColor("#1F994A"));
                    break;
                case 3:
                    dataSet.setColor(Color.parseColor("#DBAE11"));
                    break;
                case 4:
                    dataSet.setColor(Color.parseColor("#F13B3B"));
                    break;
            }
            dataSet.setDrawCircles(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            mDataSets.add(dataSet);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 设置X轴的位置
        xAxis.setDrawGridLines(false); // 效果如下图
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);

        YAxis yAxisRight=chart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft=chart.getAxisLeft();
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setDrawGridLines(false);


        Description description=new Description();
        description.setEnabled(false);
        chart.setDescription(description);
        Legend legend=chart.getLegend();
       // legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        mLineData = new LineData(mDataSets);
        chart.setMarker(new ChartMarkerView(getActivity()));
        chart.setDrawBorders(false);
        chart.setData(mLineData);
        chart.invalidate(); // refresh



        Thread thread=new Thread(mRun);
        thread.start();
    }

    private int i=10;
    private Runnable mRun=new Runnable() {
        @Override
        public void run() {
            while (i<100){
                i++;
                for (int j=1;j<=4;j++){
                    float y=0;
                    if (j==1){
                        y=i;
                    }else if (j==2){
                        y=i*10;
                    }else if (j==3){
                        y= (float) Math.pow(i,2);
                    }else if (j==4){
                        y= i*20;
                    }
                    Entry entry=new Entry(i, y);
                    mDataSets.get(j-1).addEntry(entry);
                }

                mHandler.sendEmptyMessage(1);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };


    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mLineData.notifyDataChanged();
            chart.notifyDataSetChanged(); // let the chart know it's data changed
            chart.invalidate(); // refresh
        }
    };


    @OnClick({R.id.tv_stop})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.tv_stop:

                break;
        }
    }

}
