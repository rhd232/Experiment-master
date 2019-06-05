package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.anitoa.bean.FlashData;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShow;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.expe.adapter.ChannelAdapter;
import com.jz.experiment.module.expe.adapter.SampleAdapter;
import com.jz.experiment.module.expe.event.ExpeNormalFinishEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.TrimReader;
import com.wind.base.BaseActivity;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.DateUtil;
import com.wind.base.utils.DisplayUtil;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ChannelMaterial;
import com.wind.data.expe.bean.ExpeSettingsFirstInfo;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Sample;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.FindExpeByIdResponse;
import com.wind.data.expe.request.FindExpeRequest;
import com.wind.toastlib.ToastUtil;
import com.wind.view.ValidateEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserSettingsStep1ActivityBackup extends BaseActivity {

    public static void start(Context context) {
        Navigator.navigate(context, UserSettingsStep1ActivityBackup.class);
    }

    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, UserSettingsStep1ActivityBackup.class, experiment);
    }

    @BindView(R.id.et_expe_name)
    ValidateEditText et_expe_name;
    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.lv_channel)
    ListView lv_channel;

    @BindView(R.id.et_integration_time_1)
    ValidateEditText et_integration_time_1;
    @BindView(R.id.et_integration_time_2)
    ValidateEditText et_integration_time_2;
    @BindView(R.id.et_integration_time_3)
    ValidateEditText et_integration_time_3;
    @BindView(R.id.et_integration_time_4)
    ValidateEditText et_integration_time_4;

    @BindView(R.id.ll_integration_time_1)
    LinearLayout ll_integration_time_1;
    @BindView(R.id.ll_integration_time_2)
    LinearLayout ll_integration_time_2;
    @BindView(R.id.ll_integration_time_3)
    LinearLayout ll_integration_time_3;
    @BindView(R.id.ll_integration_time_4)
    LinearLayout ll_integration_time_4;


    @BindView(R.id.gv_sample_a)
    GridView gv_sample_a;
    @BindView(R.id.gv_sample_b)
    GridView gv_sample_b;

    @BindView(R.id.cb_int)
    CheckBox cb_int;
    @BindView(R.id.ll_int)
    View ll_int;

    SampleAdapter mSampleAdapterA;
    SampleAdapter mSampleAdapterB;
    ChannelAdapter mChannelAdapter;

    private HistoryExperiment mExperiment;
    Subscription findSubscription;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("用户设置1");
        mTitleBar.setRightIcon(R.drawable.icon_history_data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step1);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        cb_int.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int visibility=isChecked?View.GONE:View.VISIBLE;
                ll_int.setVisibility(visibility);
            }
        });
        mExperiment = Navigator.getParcelableExtra(this);
        if (mExperiment == null) {
            mExperiment = new HistoryExperiment();
            ExpeSettingsFirstInfo firstInfo = new ExpeSettingsFirstInfo();
            mExperiment.setSettingsFirstInfo(firstInfo);

            List<Channel> channels = new ArrayList<>();
            firstInfo.setChannels(channels);
            for (int i = 0; i < 4; i++) {
                Channel channel = new Channel();
                channel.setName("通道" + (i + 1));
                channel.setIntegrationTime(10);
                channels.add(channel);
            }

            List<Sample> samplesA = new ArrayList<>();
            firstInfo.setSamplesA(samplesA);
            for (int i = 0; i < 8; i++) {
                Sample sample = new Sample();
                samplesA.add(sample);

            }
            List<Sample> samplesB = new ArrayList<>();
            firstInfo.setSamplesB(samplesB);
            for (int i = 0; i < 8; i++) {
                Sample sample = new Sample();
                samplesB.add(sample);

            }
            if (FlashData.NUM_WELLS==4){
                //4孔机只有A，没有B
                for (int i=0;i<8;i++){
                    if (i<4) {
                        samplesA.get(i).setEnabled(true);
                    }else {
                        samplesA.get(i).setEnabled(false);
                    }
                    samplesB.get(i).setEnabled(false);
                }
            }else {
                enableSamples(samplesA);
                enableSamples(samplesB);
            }
          /*  enableSamples(samplesA);
            enableSamples(samplesB);*/
            inflateData();
        } else {
            //需要根据expe_id查询具体的Expe
            FindExpeRequest request = new FindExpeRequest();
            request.setId(mExperiment.getId());
            findSubscription = ExpeDataStore
                    .getInstance(ProviderModule.getInstance()
                            .getBriteDb(getActivity()
                                    .getApplicationContext()))
                    .findById(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<FindExpeByIdResponse>() {
                        @Override
                        public void call(FindExpeByIdResponse response) {
                            findSubscription.unsubscribe();
                            mExperiment = response.getData();
                            List<Sample> samplesA=mExperiment.getSettingsFirstInfo().getSamplesA();
                            List<Sample> samplesB=mExperiment.getSettingsFirstInfo().getSamplesB();
                            cb_int.setChecked(mExperiment.isAutoIntegrationTime());
                            if (FlashData.NUM_WELLS==4){
                                //4孔机只有A，没有B
                                for (int i=0;i<8;i++){
                                    if (i<4) {
                                        samplesA.get(i).setEnabled(true);
                                    }else {
                                        samplesA.get(i).setEnabled(false);
                                    }
                                    samplesB.get(i).setEnabled(false);
                                }
                            }else {
                                enableSamples(samplesA);
                                enableSamples(samplesB);
                            }
                            inflateData();
                          /*  enableSamples(samplesA);
                            enableSamples(samplesB);*/
                        }
                    });
        }


        CommData.ReadDatapositionFile(getActivity());
        TrimReader.getInstance().ReadTrimFile(getActivity());




    }


    private void enableSamples(List<Sample> samples){
        int wellNum=CommData.KsIndex;//反应井个数

        int halfWellNum = wellNum / 2;
        for (int i = 0; i < 8; i++) {
            Sample sample = samples.get(i);
            if (halfWellNum > (i)) {
                sample.setEnabled(true);
            } else {
                sample.setEnabled(false);
            }
        }

    }

    private void inflateData() {
        //TODO 根据下位机通道数和孔数进行设置，多余的通道和孔位需要置灰，不可点。
        mChannelAdapter = new ChannelAdapter(getActivity(), R.layout.item_channel, DisplayUtil.dip2px(getActivity(),44));
        List<Channel> channels=mExperiment.getSettingsFirstInfo().getChannels();
        int enabledChannels=FlashData.NUM_CHANNELS;

        View[] integrationTimeViews={ll_integration_time_1,
                ll_integration_time_2,
                ll_integration_time_3,
                ll_integration_time_4};
        for (int i=0;i<integrationTimeViews.length;i++){
            integrationTimeViews[i].setBackgroundColor(getResources().getColor(R.color.colorE8E8E8));
            integrationTimeViews[i].setEnabled(false);
        }
        View[] integrationTimeEts={et_integration_time_1,
                et_integration_time_2,
                et_integration_time_3,
                et_integration_time_4};
        /*for (int i=0;i<integrationTimeViews.length;i++){
            integrationTimeEts[i].setFocusable(false);
        }*/



        for (int i=0;i<enabledChannels;i++){
            channels.get(i).setEnabled(true);
            integrationTimeViews[i].setBackgroundColor(getResources().getColor(R.color.white));
            integrationTimeViews[i].setEnabled(true);
            //integrationTimeEts[i].setFocusable(true);
        }

        for (int i=enabledChannels;i< CCurveShow.MAX_CHAN;i++){
            integrationTimeViews[i].setEnabled(false);
            integrationTimeEts[i].setFocusable(false);
        }

        et_integration_time_1.setText(channels.get(0).getIntegrationTime()+"");
        et_integration_time_2.setText(channels.get(1).getIntegrationTime()+"");
        et_integration_time_3.setText(channels.get(2).getIntegrationTime()+"");
        et_integration_time_4.setText(channels.get(3).getIntegrationTime()+"");



        mChannelAdapter.replaceAll(channels);
        lv_channel.setAdapter(mChannelAdapter);
        lv_channel.setOnItemClickListener(mOnItemClickListener);


        mSampleAdapterA = new SampleAdapter(getActivity(), R.layout.item_sample);
        mSampleAdapterA.replaceAll(mExperiment.getSettingsFirstInfo().getSamplesA());
        gv_sample_a.setAdapter(mSampleAdapterA);

        mSampleAdapterB = new SampleAdapter(getActivity(), R.layout.item_sample);
        mSampleAdapterB.replaceAll(mExperiment.getSettingsFirstInfo().getSamplesB());
        gv_sample_b.setAdapter(mSampleAdapterB);

        et_expe_name.setText(mExperiment.getName());

        Date date = new Date();
        String today = DateUtil.get(date.getTime());
        //设置实验时间
        mExperiment.setMillitime(date.getTime());
        tv_time.setText(today);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (!mChannelAdapter.getItem(position).isEnabled()){
                ToastUtil.showToast(getActivity(),"当前机器该通道不可用");
                return;
            }

            AppDialogHelper.showChannelSelectDialog(getActivity(), position , new AppDialogHelper.OnChannelSelectListener() {
                @Override
                public void onChannelSelected(int position, ChannelMaterial material) {
                    String name="";
                    String remark="";
                    if (material!=null){
                        name=material.getName();
                        remark=material.getRemark();
                    }
                    mChannelAdapter.getItem(position).setValue(name);
                    mChannelAdapter.getItem(position).setRemark(remark);
                    mChannelAdapter.notifyDataSetChanged();

                }
            });
        }
    };



    private void setIntegrationTime(List<Channel> channels){

        EditText []et_integration_times={et_integration_time_1,
                et_integration_time_2,et_integration_time_3,
                et_integration_time_4};

        for (int i=0;i<channels.size();i++){
            String integrationTimeStr=et_integration_times[i].getText().toString().trim();
            int integrationTime=10;
            try {
                integrationTime=Integer.parseInt(integrationTimeStr);
            }catch (Exception e){
                e.printStackTrace();
            }
            channels.get(i).setIntegrationTime(integrationTime);
        }

    }

    @OnClick(R.id.tv_next)
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_next:
                if (true) {//validate()

/*
                    String integrationTime= buildIntegrationTime();

                    mExperiment.setIntegrationTime(integrationTime);*/

                    //获取实验名称，通道设置，样板设置
                    ExpeSettingsFirstInfo firstInfo = new ExpeSettingsFirstInfo();
                    //设置是否自动积分时间，如果选择了自动计算积分时间将不考虑程序设置的积分时间。
                    int auto=cb_int.isChecked()?HistoryExperiment.CODE_AUTO_YES:HistoryExperiment.CODE_AUTO_NO;
                    mExperiment.setAutoIntegrationTime(auto);

                    mExperiment.setName(et_expe_name.getText().toString());
                    List<Channel> channels = mChannelAdapter.getData();
                    firstInfo.setChannels(channels);
                    //设置通道积分时间
                    setIntegrationTime(channels);


                    List<Sample> samplesA = mSampleAdapterA.getData();
                    List<Sample> samplesB = mSampleAdapterB.getData();
                    firstInfo.setSamplesA(samplesA);
                    firstInfo.setSamplesB(samplesB);

                    mExperiment.setSettingsFirstInfo(firstInfo);
                    UserSettingsStep2Activity.start(getActivity(), mExperiment);
                }

                break;
        }

    }

    private boolean validate() {
        if (!et_expe_name.validate(true)) {
            ToastUtil.showToast(getActivity(), "请输入实验名称");
            return false;
        }
        boolean channelSetted = false;
        //检查通道
        List<Channel> channels = mChannelAdapter.getData();
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);
            String value = channel.getValue();
            if (!TextUtils.isEmpty(value)) {
                channelSetted = true;
            }
        }
        if (!channelSetted) {
            ToastUtil.showToast(getActivity(), "请设置通道染料");
            return false;
        }


        return true;
    }

    @Subscribe
    public void onExpeNormalFinishEvent(ExpeNormalFinishEvent event) {
        ActivityUtil.finish(getActivity());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (findSubscription!=null&& !findSubscription.isUnsubscribed()){
            findSubscription.unsubscribe();
        }
        findSubscription=null;
        EventBus.getDefault().unregister(this);
    }
}
