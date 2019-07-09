package com.jz.experiment.util;

import android.text.TextUtils;

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
import com.wind.data.expe.bean.ExpeSettingSecondInfo;
import com.wind.data.expe.bean.ExpeSettingsFirstInfo;
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

    public HistoryExperiment expeJsonBeanToExperiment(ExpeJsonBean expeJsonBean) {
        HistoryExperiment experiment = new HistoryExperiment();
        experiment.setName(expeJsonBean.getName());
        experiment.setMillitime(expeJsonBean.getCreateMillitime());
        experiment.setDuring(expeJsonBean.getDuring());
        experiment.setFinishMilliTime(expeJsonBean.getFinishMillitime());
        ExpeSettingsFirstInfo firstInfo = new ExpeSettingsFirstInfo();
        //List<ExpeJsonBean.Channel> channels=expeJsonBean.getChannels();
        List<Channel> channels = new ArrayList<>();
        for (ExpeJsonBean.Channel channel : expeJsonBean.getChannels()) {
            Channel c = new Channel();
            c.setName(channel.name);
            c.setValue(channel.dye);
            c.setIntegrationTime(channel.integrtionTime);
            c.setRemark(channel.remark);
            channels.add(c);
        }
        firstInfo.setChannels(channels);

        List<Sample> samplesA = new ArrayList<>();
        int seq = 1;
        for (ExpeJsonBean.Sample sample : expeJsonBean.getSamplesA()) {
            Sample s = new Sample();
            if (!TextUtils.isEmpty(sample.name)) {
                s.setName(sample.name);
            }
            s.setSeq(seq);
            s.setType(Sample.TYPE_A);
            seq++;
            samplesA.add(s);
        }
        firstInfo.setSamplesA(samplesA);

        List<Sample> samplesB = new ArrayList<>();
        seq = 1;
        for (ExpeJsonBean.Sample sample : expeJsonBean.getSamplesB()) {
            Sample s = new Sample();
            if (!TextUtils.isEmpty(sample.name)) {
                s.setName(sample.name);
            }
            s.setSeq(seq);
            s.setType(Sample.TYPE_B);
            seq++;
            samplesB.add(s);
        }
        firstInfo.setSamplesB(samplesB);
        experiment.setSettingsFirstInfo(firstInfo);

        //=========ExpeSettingSecondInfo 开始=====================
        ExpeSettingSecondInfo secondInfo = new ExpeSettingSecondInfo();
        List<Mode> modes = new ArrayList<>();
        ExpeJsonBean.ExpeMode pcrMode = expeJsonBean.getPcr();
        Mode pcr = new Mode(pcrMode.name);
        if (pcrMode.ctThreshold==0){
            pcrMode.ctThreshold=10;
        }
        if (pcrMode.ctMin==0){
            pcrMode.ctMin=13;
        }
        pcr.setCtThreshold(pcrMode.ctThreshold);
        pcr.setCtMin(pcrMode.ctMin);
        modes.add(pcr);
        experiment.setAutoIntegrationTime(pcrMode.autoInt ? 1 : 0);

        ExpeJsonBean.ExpeMode meltingMode = expeJsonBean.getMelting();
        if (meltingMode!=null) {
            Mode melting = new Mode(meltingMode.name);
            if (meltingMode.ctMin==0){
                meltingMode.ctMin=13;
            }
            if (meltingMode.ctThreshold==0){
                meltingMode.ctThreshold=10;
            }
            melting.setCtMin(meltingMode.ctMin);
            melting.setCtThreshold(meltingMode.ctThreshold);
            modes.add(melting);
        }
        secondInfo.setModes(modes);

        List<Stage> stages = new ArrayList<>();
        List<StartStage> startStages = new ArrayList<>();
        for (ExpeJsonBean.DenaturationStage denaturationStage : expeJsonBean.getStages().denaturationStages) {
            StartStage startStage = new StartStage();
            startStage.setDuring((short) denaturationStage.during);
            startStage.setTemp(denaturationStage.temp);
            startStage.setStepName(denaturationStage.stepName);
            startStages.add(startStage);
        }
        stages.addAll(startStages);
        List<CyclingStage> cyclingStages=new ArrayList<>();
        for (ExpeJsonBean.CyclingStage cyclingStage : expeJsonBean.getStages().cyclingStages) {
            CyclingStage cs = new CyclingStage();
            cs.setCyclingCount(cyclingStage.cyclingCount);
            //List<PartStage> partStages=new ArrayList<>();
            int position=0;
            for (ExpeJsonBean.PartStage  partStage: cyclingStage.partStages){
                PartStage ps=new PartStage();
                ps.setDuring((short) partStage.during);
                ps.setTemp(partStage.temp);
                ps.setStepName(partStage.stepName);
                ps.setTakePic(partStage.takePic);
                //partStages.add(ps);
                cs.addChildStage(position,ps);
                position++;
            }
            cyclingStages.add(cs);
        }
        stages.addAll(cyclingStages);

        ExpeJsonBean.ExtensionStage extensionStage=expeJsonBean.getStages().extensionStage;
        EndStage endStage = new EndStage();
        endStage.setDuring((short) extensionStage.during);
        endStage.setTemp(extensionStage.temp);
        endStage.setStepName(extensionStage.stepName);
        stages.add(endStage);

        if (meltingMode != null) {
            List<MeltingStage> meltingStages = new ArrayList<>();
            for (ExpeJsonBean.MeltingStage meltingStage : expeJsonBean.getStages().meltingStages) {
                MeltingStage ms = new MeltingStage();
               // ms.setDuring((short) meltingStage.during);
                ms.setTemp(meltingStage.temp);
                ms.setStepName(meltingStage.stepName);
                meltingStages.add(ms);
            }
            stages.addAll(meltingStages);

            secondInfo.setStartTemperature(meltingStages.get(0).getTemp()+"");
            secondInfo.setEndTemperature(meltingStages.get(1).getTemp()+"");
        }

        secondInfo.setSteps(stages);
        experiment.setSettingSecondInfo(secondInfo);

        return experiment;
    }

    private ExpeJsonBean experimentToExpeJsonBean(HistoryExperiment experiment) {
        ExpeJsonBean expeJsonBean = new ExpeJsonBean();
        expeJsonBean.setName(experiment.getName());
        expeJsonBean.setCreateMillitime(experiment.getMillitime());
        expeJsonBean.setFinishMillitime(experiment.getFinishMilliTime());
        expeJsonBean.setDuring(experiment.getDuring());

        List<ExpeJsonBean.Channel> channels = new ArrayList<>();
        for (Channel channel : experiment.getSettingsFirstInfo().getChannels()) {
            ExpeJsonBean.Channel c = new ExpeJsonBean.Channel();
            c.name = channel.getName();
            c.dye = channel.getValue();
            c.remark = channel.getRemark();
            c.integrtionTime = channel.getIntegrationTime();
            channels.add(c);
        }
        expeJsonBean.setChannels(channels);

        List<ExpeJsonBean.Sample> samplesA = new ArrayList<>();
        int seq = 1;
        for (Sample sample : experiment.getSettingsFirstInfo().getSamplesA()) {
            sample.setType(Sample.TYPE_A);
            sample.setSeq(seq);
            ExpeJsonBean.Sample s = new ExpeJsonBean.Sample();
            s.code = sample.getSeqName();
            s.name = sample.getName();
            s.seq = seq;
            samplesA.add(s);
            seq++;
        }
        expeJsonBean.setSamplesA(samplesA);

        List<ExpeJsonBean.Sample> samplesB = new ArrayList<>();
        seq = 1;
        for (Sample sample : experiment.getSettingsFirstInfo().getSamplesB()) {
            sample.setType(Sample.TYPE_B);
            sample.setSeq(seq);
            ExpeJsonBean.Sample s = new ExpeJsonBean.Sample();
            s.code = sample.getSeqName();
            s.name = sample.getName();
            s.seq = seq;
            samplesB.add(s);
            seq++;
        }
        expeJsonBean.setSamplesB(samplesB);

        List<Mode> modes = experiment.getSettingSecondInfo().getModes();
        ExpeJsonBean.ExpeMode pcr = new ExpeJsonBean.ExpeMode();
        pcr.autoInt = experiment.isAutoIntegrationTime();
        pcr.name = modes.get(0).getName();
        pcr.ctMin=modes.get(0).getCtMin();
        pcr.ctThreshold=modes.get(0).getCtThreshold();
        pcr.dataFileName = DataFileUtil.getDtImageDataFileName(experiment);
        //TODO  数据库中需要存入ctMin和ctThreshold
        expeJsonBean.setPcr(pcr);

        if (modes.size() > 1) {
            ExpeJsonBean.ExpeMode melting = new ExpeJsonBean.ExpeMode();
            melting.ctMin=modes.get(1).getCtMin();
            melting.ctThreshold=modes.get(1).getCtThreshold();
            melting.autoInt = experiment.isAutoIntegrationTime();
            melting.name = modes.get(1).getName();
            melting.dataFileName = DataFileUtil.getMeltImageDataFileName(experiment);
            //TODO  数据库中需要存入ctMin和ctThreshold
            expeJsonBean.setMelting(melting);
        }

        ExpeJsonBean.Stages stages = new ExpeJsonBean.Stages();

        List<ExpeJsonBean.DenaturationStage> denaturationStages = new ArrayList<>();
        List<ExpeJsonBean.MeltingStage> meltingStages = new ArrayList<>();
        List<ExpeJsonBean.CyclingStage> cyclingStages = new ArrayList<>();
        //温度没有值
        for (Stage stage : experiment.getSettingSecondInfo().getSteps()) {

            if (stage instanceof StartStage) {
                StartStage startStage = (StartStage) stage;
                ExpeJsonBean.DenaturationStage denaturationStage = new ExpeJsonBean.DenaturationStage();
                denaturationStage.during = startStage.getDuring();
                denaturationStage.temp = startStage.getTemp();
                denaturationStage.stepName = startStage.getStepName();
                denaturationStages.add(denaturationStage);
            } else if (stage instanceof CyclingStage) {
                ExpeJsonBean.CyclingStage cyclingStage = new ExpeJsonBean.CyclingStage();
                CyclingStage cStage = (CyclingStage) stage;
                cyclingStage.cyclingCount = cStage.getCyclingCount();
                List<ExpeJsonBean.PartStage> partStages = new ArrayList<>();
                for (PartStage pStage : cStage.getChildStages()) {
                    ExpeJsonBean.PartStage partStage = new ExpeJsonBean.PartStage();
                    partStage.during = pStage.getDuring();
                    //TODO 注意stepName为空
                    partStage.stepName = pStage.getStepName();
                    partStage.takePic = pStage.isTakePic();
                    partStages.add(partStage);
                }
                cyclingStage.partStages = partStages;
                cyclingStages.add(cyclingStage);

            } else if (stage instanceof EndStage) {
                EndStage endStage = (EndStage) stage;
                ExpeJsonBean.ExtensionStage extensionStage = new ExpeJsonBean.ExtensionStage();
                extensionStage.during = endStage.getDuring();
                extensionStage.temp = endStage.getTemp();
                extensionStage.stepName = endStage.getStepName();
                stages.extensionStage = extensionStage;
            } else if (stage instanceof MeltingStage) {
                ExpeJsonBean.MeltingStage meltingStage = new ExpeJsonBean.MeltingStage();
                MeltingStage m = (MeltingStage) stage;
                meltingStage.during = m.getDuring();
                meltingStage.temp = m.getTemp();
                meltingStage.stepName = m.getStepName();
                meltingStages.add(meltingStage);
            }


        }
        stages.denaturationStages = denaturationStages;
        stages.cyclingStages = cyclingStages;
        stages.meltingStages = meltingStages;
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
                    ExpeJsonBean expeJsonBean = experimentToExpeJsonBean(request.getExperiment());

                    String json = JsonParser.object2Json(expeJsonBean);
                    //实验名+时间
                    String formatTime = DateUtil.get(expeJsonBean.getCreateMillitime(), "yyyy_MM_dd_HH_mm_ss");
                    String fileName = expeJsonBean.getName() + "__" + formatTime + ".json";

                    String dir = AnitoaLogUtil.IMAGE_DATA;
                    File file = new File(dir, fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileUtils.write(file, json, Charset.forName("utf-8"));
                    response.setErr(0);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);

                }

            }
        });

    }

}
