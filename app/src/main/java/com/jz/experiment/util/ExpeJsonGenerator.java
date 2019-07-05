package com.jz.experiment.util;

import com.anitoa.util.AnitoaLogUtil;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.MeltingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.DateUtil;
import com.wind.base.utils.JsonParser;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ExpeJsonBean;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;
import com.wind.data.expe.bean.Sample;
import com.wind.data.expe.request.GenerateExpeJsonRequest;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class ExpeJsonGenerator {


    private static ExpeJsonGenerator sInstance = null;

    public static synchronized ExpeJsonGenerator getInstance() {
        if (sInstance == null) {
            synchronized (ExpeJsonGenerator.class) {
                if (sInstance == null) {
                    sInstance = new ExpeJsonGenerator();
                }
            }
        }

        return sInstance;
    }

    private ExpeJsonBean experimentToExpeJsonBean(HistoryExperiment experiment){
        ExpeJsonBean expeJsonBean=new ExpeJsonBean();
        expeJsonBean.setName(experiment.getName());
        expeJsonBean.setCreateMillitime(experiment.getMillitime());
        expeJsonBean.setFinishMillitime(experiment.getFinishMilliTime());
        expeJsonBean.setDuring(experiment.getDuring());

        List<ExpeJsonBean.Channel> channels=new ArrayList<>();
        for (Channel channel:experiment.getSettingsFirstInfo().getChannels()){
            ExpeJsonBean.Channel c=new ExpeJsonBean.Channel();
            c.name=channel.getName();
            c.dye=channel.getValue();
            c.remark=channel.getRemark();
            c.integrtionTime=channel.getIntegrationTime();
            channels.add(c);
        }
        expeJsonBean.setChannels(channels);

        List<ExpeJsonBean.Sample> samplesA=new ArrayList<>();
        int seq=1;
        for (Sample sample:experiment.getSettingsFirstInfo().getSamplesA()){
            sample.setType(Sample.TYPE_A);
            sample.setSeq(seq);
            ExpeJsonBean.Sample s=new ExpeJsonBean.Sample();
            s.code=sample.getSeqName();
            s.name=sample.getName();
            s.seq=seq;
            samplesA.add(s);
            seq++;
        }
        expeJsonBean.setSamplesA(samplesA);

        List<ExpeJsonBean.Sample> samplesB=new ArrayList<>();
        seq=1;
        for (Sample sample:experiment.getSettingsFirstInfo().getSamplesB()){
            sample.setType(Sample.TYPE_B);
            sample.setSeq(seq);
            ExpeJsonBean.Sample s=new ExpeJsonBean.Sample();
            s.code=sample.getSeqName();
            s.name=sample.getName();
            s.seq=seq;
            samplesB.add(s);
            seq++;
        }
        expeJsonBean.setSamplesB(samplesB);

        List<Mode> modes=experiment.getSettingSecondInfo().getModes();
        ExpeJsonBean.ExpeMode pcr=new ExpeJsonBean.ExpeMode();
        pcr.autoInt=experiment.isAutoIntegrationTime();
        pcr.name=modes.get(0).getName();
        pcr.dataFileName=DataFileUtil.getDtImageDataFileName(experiment);
        //TODO  数据库中需要存入ctMin和ctThreshold
        expeJsonBean.setPcr(pcr);

        if (modes.size()>1){
            ExpeJsonBean.ExpeMode melting=new ExpeJsonBean.ExpeMode();
            melting.autoInt=experiment.isAutoIntegrationTime();
            melting.name=modes.get(1).getName();
            melting.dataFileName=DataFileUtil.getMeltImageDataFileName(experiment);
            //TODO  数据库中需要存入ctMin和ctThreshold
            expeJsonBean.setMelting(melting);
        }

        ExpeJsonBean.Stages stages=new ExpeJsonBean.Stages();

        List<ExpeJsonBean.DenaturationStage> denaturationStages=new ArrayList<>();
        List<ExpeJsonBean.MeltingStage> meltingStages=new ArrayList<>();
        List<ExpeJsonBean.CyclingStage> cyclingStages=new ArrayList<>();
        //温度没有值
        for (Stage stage:experiment.getSettingSecondInfo().getSteps()){

            if (stage instanceof StartStage){
                StartStage startStage= (StartStage) stage;
                ExpeJsonBean.DenaturationStage denaturationStage=new ExpeJsonBean.DenaturationStage();
                denaturationStage.during=startStage.getDuring();
                denaturationStage.temp=startStage.getTemp();
                denaturationStage.stepName=startStage.getStepName();
                denaturationStages.add(denaturationStage);
            }else if (stage instanceof CyclingStage){
                ExpeJsonBean.CyclingStage  cyclingStage=new ExpeJsonBean.CyclingStage();
                CyclingStage cStage= (CyclingStage) stage;
                cyclingStage.cyclingCount=cStage.getCyclingCount();
                List<ExpeJsonBean.PartStage> partStages=new ArrayList<>();
                for (PartStage pStage:cStage.getChildStages()){
                    ExpeJsonBean.PartStage partStage=new ExpeJsonBean.PartStage();
                    partStage.during=pStage.getDuring();
                    //TODO 注意stepName为空
                    partStage.stepName=pStage.getStepName();
                    partStage.takePic=pStage.isTakePic();
                    partStages.add(partStage);
                }
                cyclingStage.partStages=partStages;
                cyclingStages.add(cyclingStage);

            }else if (stage instanceof EndStage){
                EndStage endStage= (EndStage) stage;
                ExpeJsonBean.ExtensionStage extensionStage=new ExpeJsonBean.ExtensionStage();
                extensionStage.during=endStage.getDuring();
                extensionStage.temp=endStage.getTemp();
                extensionStage.stepName=endStage.getStepName();
                stages.extensionStage=extensionStage;
            }else if (stage instanceof MeltingStage){
                ExpeJsonBean.MeltingStage meltingStage=new ExpeJsonBean.MeltingStage();
                MeltingStage m= (MeltingStage) stage;
                meltingStage.during=m.getDuring();
                meltingStage.temp=m.getTemp();
                meltingStage.stepName=m.getStepName();
                meltingStages.add(meltingStage);
            }


        }
        stages.denaturationStages=denaturationStages;
        stages.cyclingStages=cyclingStages;
        stages.meltingStages=meltingStages;
        expeJsonBean.setStages(stages);
        return expeJsonBean;
    }

    public Observable<BaseResponse> generateExpeJson(final GenerateExpeJsonRequest request) {
        return Observable.create(new Observable.OnSubscribe<BaseResponse>() {
            @Override
            public void call(Subscriber<? super BaseResponse> subscriber) {
                BaseResponse response = new BaseResponse();
                response.setErr(-1);
                try {
                    ExpeJsonBean expeJsonBean=experimentToExpeJsonBean(request.getExperiment());

                    String json=JsonParser.object2Json(expeJsonBean);
                    //实验名+时间
                    String formatTime = DateUtil.get(expeJsonBean.getCreateMillitime(), "yyyy_MM_dd_HH_mm_ss");
                    String fileName=expeJsonBean.getName()+"__"+formatTime+".json";

                    String dir=AnitoaLogUtil.IMAGE_DATA;
                    File file=new File(dir,fileName);
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    FileUtils.write(file,json, Charset.forName("utf-8"));
                    response.setErr(0);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                }catch (Exception e){
                    e.printStackTrace();
                    subscriber.onError(e);

                }

            }
        });

    }

}
