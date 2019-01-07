package com.jz.experiment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.login.LoginActivity;
import com.wind.base.BaseActivity;
import com.wind.base.C;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.data.DbOpenHelper;
import com.wind.data.base.datastore.UserDataStore;
import com.wind.data.base.request.FindUserRequest;
import com.wind.data.base.request.InsertUserRequest;
import com.wind.data.base.response.FindUserResponse;
import com.wind.data.base.response.InsertUserResponse;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.DtMode;
import com.wind.data.expe.bean.ExpeSettingSecondInfo;
import com.wind.data.expe.bean.ExpeSettingsFirstInfo;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;
import com.wind.data.expe.bean.Sample;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.FindExpeByIdResponse;
import com.wind.data.expe.request.FindExpeRequest;
import com.wind.data.expe.request.InsertExpeRequest;
import com.wind.data.expe.response.InsertExpeResponse;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class SplashActivity extends BaseActivity {

    @Override
    public int getStatusBarColor() {
        return Color.WHITE;
    }

    UserDataStore mUserDataStore;
    ExpeDataStore mExpeDataStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mUserDataStore = new UserDataStore(
                ProviderModule
                        .getInstance()
                        .provideBriteDb(DbOpenHelper.getInstance(getApplicationContext())));

        mExpeDataStore = new ExpeDataStore(
                ProviderModule
                        .getInstance()
                        .provideBriteDb(DbOpenHelper.getInstance(getApplicationContext())));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                insertAdminIfNeeded();
            }
        }, 500);

    }
    Subscription expeSubscription;
    public void testInsertExpe(){
        InsertExpeRequest insertExpeRequest=new InsertExpeRequest();
        HistoryExperiment experiment=new HistoryExperiment();
        experiment.setName("测试实验");
        experiment.setDevice("测试专用设备");
        experiment.setMillitime(System.currentTimeMillis());
        ExperimentStatus status=new ExperimentStatus();
        status.setStatus(0);
        status.setDesc("已完成");
        experiment.setStatus(status);


        ExpeSettingsFirstInfo firstInfo=new ExpeSettingsFirstInfo();
        List<Channel> channels=new ArrayList<>();
        for (int i=1;i<=4;i++){
            Channel channel=new Channel();
            channel.setName("通道"+i);
            channel.setValue("xaf");
            channel.setRemark("备注");
            channels.add(channel);
        }
        firstInfo.setChannels(channels);
        List<Sample> samplesA=new ArrayList<>();
        for (int i=1;i<=8;i++){
            Sample sample=new Sample();
            sample.setName("a"+i);
            samplesA.add(sample);
        }
        firstInfo.setSamplesA(samplesA);
        List<Sample> samplesB=new ArrayList<>();
        for (int i=1;i<=8;i++){
            Sample sample=new Sample();
            sample.setName("b"+i);
            samplesB.add(sample);
        }
        firstInfo.setSamplesB(samplesB);
        experiment.setSettingsFirstInfo(firstInfo);

        ExpeSettingSecondInfo secondInfo=new ExpeSettingSecondInfo();
        List<Stage> stages=new ArrayList<>();
        StartStage startStage=new StartStage();
        startStage.setStartScale(100);
        startStage.setCurScale(200);
        stages.add(startStage);
        for (int i=0;i<2;i++){
            CyclingStage stage=new CyclingStage();
            stage.setSerialNumber(i);
            stage.setCyclingCount(30);
            List<PartStage> partStages=stage.getPartStageList();
            for (int j=0;j<2;j++){
                PartStage partStage=new PartStage();
                partStage.setSerialNumber(j);
                partStage.setTakePic(j==0?true:false);
                partStage.setStepName("step "+(i+1));
                partStage.setStartScale(200);
                partStage.setCurScale(200);
                partStages.add(partStage);
            }
            stages.add(stage);
        }
        EndStage endStage=new EndStage();
        endStage.setStartScale(200);
        endStage.setCurScale(150);
        stages.add(endStage);
        secondInfo.setSteps(stages);

        List<Mode> modes=new ArrayList<>();
        modes.add(new DtMode("变温扩增"));
        secondInfo.setModes(modes);
        experiment.setSettingSecondInfo(secondInfo);
        insertExpeRequest.setExperiment(experiment);
        expeSubscription=mExpeDataStore.insertExpe(insertExpeRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<InsertExpeResponse>() {
                    @Override
                    public void call(InsertExpeResponse response) {
                        expeSubscription.unsubscribe();
                        if (response.getErrCode()==BaseResponse.CODE_SUCCESS){

                        }
                    }
                });
    }

    private Subscription mFindExpeSubscription;
    public void testFindExpeById(){
        final FindExpeRequest request=new FindExpeRequest();
        request.setId(1);
        mFindExpeSubscription=mExpeDataStore.findById(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FindExpeByIdResponse>() {
                    @Override
                    public void call(FindExpeByIdResponse response) {
                        mFindExpeSubscription.unsubscribe();
                        if (response.getErrCode()==BaseResponse.CODE_SUCCESS){

                        }

                    }
                });
    }

    Subscription findSubscription;
    Subscription insertSubscription;

    private void insertAdminIfNeeded() {
        testFindExpeById();
        //testInsertExpe();

        //LoadingDialogHelper.showOpLoading(getActivity());
        final FindUserRequest request = new FindUserRequest();
        request.setUsername(C.Config.DEFAULT_USERNAME);
        request.setPwd(C.Config.DEFAULT_PWD);
        findSubscription=mUserDataStore.findUserByUsername(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FindUserResponse>() {
                    @Override
                    public void call(FindUserResponse response) {
                        findSubscription.unsubscribe();
                        if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                            if (response.getUser() == null) {
                                inertDefaultUser();

                            } else {
                                next();
                            }
                        } else {
                            inertDefaultUser();
                        }
                    }
                });




    }

    private void inertDefaultUser() {

        //执行插入
        InsertUserRequest req = new InsertUserRequest();
        req.setUsername(C.Config.DEFAULT_USERNAME);
        req.setPwd(C.Config.DEFAULT_PWD);
        insertSubscription=mUserDataStore.insertUser(req)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<InsertUserResponse>() {
                    @Override
                    public void call(InsertUserResponse insertUserResponse) {
                        insertSubscription.unsubscribe();
                        next();
                    }
                });
    }

    private void next() {

        LoginActivity.start(getActivity());
        ActivityUtil.finish(getActivity());
    }
}
