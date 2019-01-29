package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aigestudio.wheelpicker.utils.WheelPickerFactory;
import com.aigestudio.wheelpicker.widget.IWheelVo;
import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.expe.adapter.StageAdapter;
import com.jz.experiment.module.expe.event.AddCyclingStageEvent;
import com.jz.experiment.module.expe.event.DelCyclingStageEvent;
import com.jz.experiment.module.expe.event.RefreshStageAdapterEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.BaseActivity;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.Navigator;
import com.wind.data.DbOpenHelper;
import com.wind.data.expe.bean.DtMode;
import com.wind.data.expe.bean.ExpeSettingSecondInfo;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.InsertExpeRequest;
import com.wind.data.expe.response.InsertExpeResponse;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserSettingsStep2Activity extends BaseActivity {

    public static void start(Context context, HistoryExperiment experiment) {

        Navigator.navigate(context, UserSettingsStep2Activity.class, experiment);
    }

    RecyclerView rv;
    StageAdapter mStageAdapter;

    @BindView(R.id.tv_mode)
    TextView tv_mode;
    @BindView(R.id.layout_melt)
    LinearLayout layout_melt;
    @BindView(R.id.tv_start_temp)
    TextView tv_start_temp;
    @BindView(R.id.tv_end_temp)
    TextView tv_end_temp;

    private Handler handler = new Handler();
    private HistoryExperiment mHistoryExperiment;
    private ExpeDataStore mExpeDataStore;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("用户设置2");
        mTitleBar.setRightIcon(R.drawable.icon_program_save);

        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存实验设置
                LoadingDialogHelper.showOpLoading(getActivity());
                buildExperiment();
                final InsertExpeRequest request = new InsertExpeRequest();
                request.setExperiment(mHistoryExperiment);
                mExpeDataStore.insertExpe(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<InsertExpeResponse>() {
                            @Override
                            public void call(InsertExpeResponse response) {
                                LoadingDialogHelper.hideOpLoading();
                                String msg;
                                if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                                    //保存成功
                                    msg = "保存成功";
                                } else {
                                    msg = "保存失败";
                                }
                                ToastUtil.showToast(getActivity(), msg);
                            }
                        });
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step2);
        ButterKnife.bind(this);
        mHistoryExperiment = Navigator.getParcelableExtra(getActivity());

        mExpeDataStore = new ExpeDataStore(
                ProviderModule
                        .getInstance()
                        .provideBriteDb(DbOpenHelper.getInstance(getApplicationContext())));

        EventBus.getDefault().register(this);
        rv = findViewById(R.id.rv);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(manager);
        rv.setNestedScrollingEnabled(false);
        mStageAdapter = new StageAdapter(getActivity());
        rv.setAdapter(mStageAdapter);

        ExpeSettingSecondInfo expeSettingSecondInfo = mHistoryExperiment.getSettingSecondInfo();
        if (expeSettingSecondInfo == null) {
            List<DisplayItem> list = new ArrayList<>();
            list.add(new StartStage());
            list.add(new CyclingStage());
            list.add(new EndStage());
            mStageAdapter.addAll(list);
            mModes = new ArrayList<>();
            mModes.add(new DtMode("变温扩增"));
        } else {
            mModes = expeSettingSecondInfo.getModes();
            buildModeShowName();
            List<DisplayItem> list = new ArrayList<>();
            List<Stage> stageList=expeSettingSecondInfo.getSteps();
            list.addAll(stageList);
            mStageAdapter.replace(list);

        }

        rv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStageAdapter.buildLink();
            }
        }, 500);


    }


    @Subscribe
    public void onRefreshStageAdapterEvent(RefreshStageAdapterEvent event) {
        mStageAdapter.notifyDataSetChanged();
        buildLink();
        Log.i("ChangeStage", "onRefreshStageAdapterEvent");
    }

    @Subscribe
    public void onAddCyclingStage(AddCyclingStageEvent event) {
        mStageAdapter.add(event.getPosition(), new CyclingStage());

        buildLink();

        Log.i("ChangeStage", "onAddCyclingStage");
    }

    @Subscribe
    public void onDelCyclingStageEvent(DelCyclingStageEvent event) {
        mStageAdapter.remove(event.getPosition());
        buildLink();
        Log.i("ChangeStage", "onDelCyclingStageEvent");
    }

    private void buildLink() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStageAdapter.buildLink();
                mStageAdapter.notifyDataSetChanged();
            }
        }, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void buildExperiment() {
        ExpeSettingSecondInfo secondInfo = mHistoryExperiment.getSettingSecondInfo();
        mHistoryExperiment.setDevice("未知设备");
        ExperimentStatus status = new ExperimentStatus();
        status.setStatus(ExperimentStatus.STATUS_NOT_START);
        status.setDesc("未启动");
        mHistoryExperiment.setStatus(status);
        if (secondInfo == null) {
            secondInfo = new ExpeSettingSecondInfo();
            mHistoryExperiment.setSettingSecondInfo(secondInfo);
        }
        String startT=tv_start_temp.getText().toString().trim();
        String endT=tv_end_temp.getText().toString().trim();
        secondInfo.setStartTemperature(startT);
        secondInfo.setEndTemperature(endT);
        secondInfo.setModes(mModes);
        List<Stage> stageList = new ArrayList<>();
        for (int i = 0; i < mStageAdapter.getItemCount(); i++) {
            Stage stage = (Stage) mStageAdapter.getItem(i);
            stageList.add(stage);
        }
        secondInfo.setSteps(stageList);
    }

    private List<Mode> mModes;

    @OnClick({R.id.rl_mode_sel, R.id.tv_next,R.id.tv_start_temp,R.id.tv_end_temp})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_end_temp:
            case R.id.tv_start_temp:
                WheelPickerFactory.showWheelNumberAAPicker(tv_start_temp, new WheelPickerFactory.OnWheelClickListener() {
                    @Override
                    public void onResult(View v, IWheelVo[] result, int[] indexs, String[] unit) {

                        tv_start_temp.setText(result[0].getLabel());
                        tv_end_temp.setText(result[1].getLabel());
                    }
                },0,100,"","",false);

                break;
            case R.id.tv_next:
                if (validate()) {
                    buildExperiment();
                   //ExpeRunningActivityBackup.start(getActivity(), mHistoryExperiment);
                    ExpeRunningActivity.start(getActivity(), mHistoryExperiment);

                }

                break;
            case R.id.rl_mode_sel:
                if (mModes == null) {
                    mModes = new ArrayList<>();
                    mModes.add(new DtMode("变温扩增"));
                }
                AppDialogHelper.showModeSelectDialog(getActivity(), mModes, new AppDialogHelper.OnModeSelectListener() {
                    @Override
                    public void onModeSelected(List<Mode> modes) {
                        mModes = modes;
                        buildModeShowName();
                    }
                });
                break;
        }
    }

    public void buildModeShowName() {
        StringBuilder sBuilder = new StringBuilder(mModes.get(0).getName());
        if (mModes.size() == 2) {
            sBuilder.append("+")
                    .append(mModes.get(1).getName());
            layout_melt.setVisibility(View.VISIBLE);
        } else {
            layout_melt.setVisibility(View.GONE);
        }
        tv_mode.setText(sBuilder.toString());
    }

    private boolean validate() {
        if (mModes == null) {
            ToastUtil.showToast(getActivity(), "请选择程序模式");
            return false;
        }

        return true;
    }

}
