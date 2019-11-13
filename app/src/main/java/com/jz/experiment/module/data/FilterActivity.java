package com.jz.experiment.module.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jz.experiment.R;
import com.jz.experiment.module.data.adapter.StringSelectableAdapter;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterActivity extends BaseActivity {


    public static void start(Context context) {
        Navigator.navigate(context, FilterActivity.class);
    }

    @BindView(R.id.gv_channel)
    GridView gv_channel;
    @BindView(R.id.gv_sample_a)
    GridView gv_sample_a;
    @BindView(R.id.gv_sample_b)
    GridView gv_sample_b;

    StringSelectableAdapter mChannelAdapter, mSampleAAdapter, mSampleBAdapter;

    @Override
    protected void setTitle() {
        mTitleBar.setLeftIcon(R.drawable.btn_selector_back);
        mTitleBar.setTitle(getString(R.string.running_filter));
    }
    private  String channel1;
    private  String channel2;
    private  String channel3;
    private  String channel4;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

        channel1=getString(R.string.channel_1_str);
        channel2=getString(R.string.channel_2_str);
        channel3=getString(R.string.channel_3_str);
        channel4=getString(R.string.channel_4_str);

        mChannelAdapter = new StringSelectableAdapter(getActivity(), R.layout.item_string);
        List<StringSelectable> channelList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            StringSelectable selectable = new StringSelectable();
            String c=getString(R.string.setup_channel);
            selectable.setVal(c + (i + 1));
            channelList.add(selectable);
        }
        mChannelAdapter.replaceAll(channelList);
        gv_channel.setAdapter(mChannelAdapter);
        gv_channel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StringSelectable selectable = mChannelAdapter.getItem(position);
                selectable.setSelected(!selectable.isSelected());
                mChannelAdapter.notifyDataSetChanged();
            }
        });

        mSampleAAdapter = new StringSelectableAdapter(getActivity(), R.layout.item_string);
        List<StringSelectable> sampleAList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            StringSelectable selectable = new StringSelectable();
            selectable.setVal("" + (i + 1));
            sampleAList.add(selectable);
        }
        mSampleAAdapter.replaceAll(sampleAList);
        gv_sample_a.setAdapter(mSampleAAdapter);
        gv_sample_a.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StringSelectable selectable = mSampleAAdapter.getItem(position);
                selectable.setSelected(!selectable.isSelected());
                mSampleAAdapter.notifyDataSetChanged();
            }
        });
        mSampleBAdapter = new StringSelectableAdapter(getActivity(), R.layout.item_string);
        List<StringSelectable> sampleBList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            StringSelectable selectable = new StringSelectable();
            selectable.setVal("" + (i + 1));
            sampleBList.add(selectable);
        }
        mSampleBAdapter.replaceAll(sampleBList);
        gv_sample_b.setAdapter(mSampleBAdapter);
        gv_sample_b.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StringSelectable selectable = mSampleBAdapter.getItem(position);
                selectable.setSelected(!selectable.isSelected());
                mSampleBAdapter.notifyDataSetChanged();
            }
        });
    }


    @OnClick(R.id.tv_confirm)
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                //获取选择的通道
                List<StringSelectable> selectedChannelList = mChannelAdapter.getSelectedList();
                List<StringSelectable> selectedSampleAList = mSampleAAdapter.getSelectedList();
                List<StringSelectable> selectedSampleBList = mSampleBAdapter.getSelectedList();
                List<String> ChanList = new ArrayList<>();

                for (StringSelectable selectable : selectedChannelList) {
                    if (selectable.getVal().equals(channel1)){
                        ChanList.add("Chip#1");
                    }else if (selectable.getVal().equals(channel2)){
                        ChanList.add("Chip#2");
                    }else if (selectable.getVal().equals(channel3)){
                        ChanList.add("Chip#3");
                    }else if (selectable.getVal().equals(channel4)){
                        ChanList.add("Chip#4");
                    }
                }

                if (ChanList.isEmpty()){
                    //ToastUtil.showToast(getActivity(),"最少选择一个通道");
                    return;
                }
                List<String> KSList = new ArrayList<String>();
                for (StringSelectable selectable : selectedSampleAList) {
                    KSList.add("A"+selectable.getVal());
                }
                for (StringSelectable selectable : selectedSampleBList) {
                    KSList.add("B"+selectable.getVal());
                }

                if (KSList.isEmpty()){
                    //ToastUtil.showToast(getActivity(),"最少选择一个样本");
                    return;
                }

                EventBus.getDefault().post(new FilterEvent(ChanList,KSList));
                finish();
                break;
        }
    }
}
