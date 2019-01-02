package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.adapter.ChannelAdapter;
import com.jz.experiment.module.expe.adapter.SampleAdapter;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ChannelMaterial;
import com.wind.data.expe.bean.Sample;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.BaseActivity;
import com.wind.base.utils.DateUtil;
import com.wind.base.utils.Navigator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserSettingsStep1Activity extends BaseActivity {

    public static void start(Context context) {
        Navigator.navigate(context, UserSettingsStep1Activity.class);
    }

    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.lv_channel)
    ListView lv_channel;

    @BindView(R.id.gv_sample_a)
    GridView gv_sample_a;
    @BindView(R.id.gv_sample_b)
    GridView gv_sample_b;
    SampleAdapter mSampleAdapterA;
    SampleAdapter mSampleAdapterB;
    ChannelAdapter mChannelAdapter;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("用户设置1");
        mTitleBar.setRightIcon(R.drawable.icon_history_data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step1);
        ButterKnife.bind(this);
        mChannelAdapter = new ChannelAdapter(getActivity(), R.layout.item_channel);
        for (int i = 0; i < 4; i++) {
            Channel channel = new Channel();
            channel.setName("通道" + (i + 1));
            mChannelAdapter.add(channel);
        }

        lv_channel.setAdapter(mChannelAdapter);
        lv_channel.setOnItemClickListener(mOnItemClickListener);
        mSampleAdapterA = new SampleAdapter(getActivity(), R.layout.item_sample);
        for (int i = 0; i < 8; i++) {
            Sample sample = new Sample();
            mSampleAdapterA.add(sample);
        }
        gv_sample_a.setAdapter(mSampleAdapterA);

        mSampleAdapterB = new SampleAdapter(getActivity(), R.layout.item_sample);
        for (int i = 0; i < 8; i++) {
            Sample sample = new Sample();
            mSampleAdapterB.add(sample);
        }
        gv_sample_b.setAdapter(mSampleAdapterB);

        String today=DateUtil.get();
        tv_time.setText(today);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AppDialogHelper.showChannelSelectDialog(getActivity(), (position + 1), new AppDialogHelper.OnChannelSelectListener() {
                @Override
                public void onChannelSelected(int position,ChannelMaterial material) {
                    mChannelAdapter.getItem(position).setValue(material.getName());
                    mChannelAdapter.getItem(position).setRemark(material.getRemark());
                    mChannelAdapter.notifyDataSetChanged();

                }
            });
        }
    };


    @OnClick(R.id.tv_next)
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.tv_next:
                List<Sample> sampleAs=mSampleAdapterA.getData();
                UserSettingsStep2Activity.start(getActivity());
                break;
        }

    }
}
