package com.wind.data.expe.datastore;

import android.content.ContentValues;
import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqldelight.SqlDelightStatement;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.MeltingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ChannelInfoModel;
import com.wind.data.expe.bean.DtMode;
import com.wind.data.expe.bean.ExpeInfoModel;
import com.wind.data.expe.bean.ExpeSettingSecondInfo;
import com.wind.data.expe.bean.ExpeSettingsFirstInfo;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.MeltMode;
import com.wind.data.expe.bean.Mode;
import com.wind.data.expe.bean.Sample;
import com.wind.data.expe.bean.SampleInfoModel;
import com.wind.data.expe.bean.StageInfoModel;
import com.wind.data.expe.request.DelExpeRequest;
import com.wind.data.expe.request.FindExpeByIdResponse;
import com.wind.data.expe.request.FindExpeRequest;
import com.wind.data.expe.request.InsertExpeRequest;
import com.wind.data.expe.response.DelExpeResponse;
import com.wind.data.expe.response.FindExpeResponse;
import com.wind.data.expe.response.InsertExpeResponse;
import com.wind.data.expe.table.ChannelInfo;
import com.wind.data.expe.table.ExpeInfo;
import com.wind.data.expe.table.SampleInfo;
import com.wind.data.expe.table.StageInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class ExpeDataStore {

    private final BriteDatabase mBriteDb;

    private static ExpeDataStore sInstance = null;

    public static ExpeDataStore getInstance(BriteDatabase briteDb) {
        if (sInstance == null) {
            synchronized (ExpeDataStore.class) {
                if (sInstance == null) {
                    sInstance = new ExpeDataStore(briteDb);
                }
            }
        }

        return sInstance;
    }

    @Inject
    public ExpeDataStore(BriteDatabase briteDb) {
        this.mBriteDb = briteDb;
    }

    public Observable<InsertExpeResponse> insertExpeData(final InsertExpeRequest request) {
        return Observable.create(new Observable.OnSubscribe<InsertExpeResponse>() {
            @Override
            public void call(Subscriber<? super InsertExpeResponse> subscriber) {

            }
        });
    }


    public Observable<DelExpeResponse> delExpe(final DelExpeRequest request){
        return Observable.create(new Observable.OnSubscribe<DelExpeResponse>() {

            @Override
            public void call(Subscriber<? super DelExpeResponse> subscriber) {

                //删除channel表，sampleinfo表，stageinfo表，expe表
                long expeId=request.getId();
                DelExpeResponse response = new DelExpeResponse();
                response.setErr(-1);
                final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                try {

                    SqlDelightStatement channelDelStatement=ChannelInfo.FACTORY.del_by_expeid(expeId);
                    mBriteDb.execute(channelDelStatement.statement);
                    //int channelAffected=mBriteDb.getWritableDatabase().delete(ChannelInfo.TABLE_NAME,channelDelStatement.statement,channelDelStatement.args);

                    SqlDelightStatement sampleDelStatement=SampleInfo.FACTORY.del_by_expeid(expeId);
                    mBriteDb.execute(sampleDelStatement.statement);
                   // int sampleAffected=mBriteDb.getWritableDatabase().delete(SampleInfo.TABLE_NAME,sampleDelStatement.statement,sampleDelStatement.args);

                    SqlDelightStatement stageDelStatement=StageInfo.FACTORY.del_by_expeid(expeId);
                    mBriteDb.execute(stageDelStatement.statement);
                    //int stageAffected=mBriteDb.getWritableDatabase().delete(StageInfo.TABLE_NAME, stageDelStatement.statement,stageDelStatement.args);


                    SqlDelightStatement expeDelStatement=ExpeInfo.FACTORY.del_by_expeid(expeId);
                    mBriteDb.execute(expeDelStatement.statement);
                    //int expeAffected=mBriteDb.getWritableDatabase().delete(ExpeInfo.TABLE_NAME, expeDelStatement.statement,expeDelStatement.args);

                    transaction.markSuccessful();
                    response.setErr(0);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                }catch (Exception e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }finally {
                    transaction.end();
                }



            }
        });
    }

    public Observable<InsertExpeResponse> insertExpe(final InsertExpeRequest request) {
        return Observable.create(new Observable.OnSubscribe<InsertExpeResponse>() {

            @Override
            public void call(Subscriber<? super InsertExpeResponse> subscriber) {
                InsertExpeResponse response = new InsertExpeResponse();
                response.setErr(-1);
                final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                try {

                    HistoryExperiment experiment = request.getExperiment();
                    List<Mode> modes = experiment.getSettingSecondInfo().getModes();
                    StringBuilder sBuilder = new StringBuilder(modes.get(0).getName());
                    if (modes.size() > 1) {
                        sBuilder.append("-").append(modes.get(1).getName());
                    }
                    //插入主表实验数据
                    ExpeInfoModel.Marshal marshal = ExpeInfo.FACTORY.marshal();

                    ContentValues values = marshal.name(experiment.getName())
                            .device(experiment.getDevice())
                            .millitime(experiment.getMillitime())
                            .status(experiment.getStatus().getStatus())
                            .status_desc(experiment.getStatus().getDesc())
                            .finish_millitime(experiment.getFinishMilliTime())
                            .during(experiment.getDuring())
                            .autoIntTime((long) experiment.getAutoIntegrationTime())
                            .mode(sBuilder.toString())
                            .asContentValues();
                    mBriteDb.insert(ExpeInfo.TABLE_NAME, values);
                   /* if (experiment.getId()>=0){
                        mBriteDb.update(ExpeInfo.TABLE_NAME,values,"_id = ?",new String[]{experiment.getId()+""});
                    }else {
                        mBriteDb.insert(ExpeInfo.TABLE_NAME, values);
                    }*/
                    // long rowId = mBriteDb.insert(User.TABLE_NAME, values);
                    //获取主键id
                    Cursor idCursor = mBriteDb.getReadableDatabase().rawQuery("select last_insert_rowid() from expe_info", null);
                    idCursor.moveToFirst();
                    int expe_id = idCursor.getInt(0);
                    idCursor.close();
                   /* int expe_id;
                    if (experiment.getId()==HistoryExperiment.ID_NONE) {
                        Cursor idCursor = mBriteDb.getReadableDatabase().rawQuery("select last_insert_rowid() from expe_info", null);
                        idCursor.moveToFirst();
                        expe_id = idCursor.getInt(0);
                        idCursor.close();
                    }else {
                        expe_id= (int) experiment.getId();
                    }*/

                    //插入channel表
                    ExpeSettingsFirstInfo firstInfo = experiment.getSettingsFirstInfo();
                    for (int i = 0; i < firstInfo.getChannels().size(); i++) {
                        Channel channel = firstInfo.getChannels().get(i);
                        long integration_time=(long) channel.getIntegrationTime();
                        ChannelInfoModel.Marshal channelMarshal = ChannelInfo.FACTORY.marshal();
                        ContentValues channelValues = channelMarshal.name(channel.getName())
                                .value(channel.getValue())
                                .integration_time(integration_time)
                                .remark(channel.getRemark())
                                .expe_id(expe_id).asContentValues();
                        mBriteDb.insert(ChannelInfo.TABLE_NAME, channelValues);
                        /*if (channel.getId()==HistoryExperiment.ID_NONE){
                            mBriteDb.insert(ChannelInfo.TABLE_NAME, channelValues);
                        }else {
                            mBriteDb.update(ChannelInfo.TABLE_NAME, channelValues,"_id = ?",new String[]{channel.getId()+""});
                        }*/

                    }
                    //插入samplesA
                    for (int i = 0; i < firstInfo.getSamplesA().size(); i++) {
                        Sample sample = firstInfo.getSamplesA().get(i);

                        SampleInfoModel.Marshal sampleMarshal = SampleInfo.FACTORY.marshal();
                        ContentValues sampleValues = sampleMarshal.expe_id(expe_id)
                                .name(sample.getName())
                                .type(Sample.TYPE_A)
                                .asContentValues();
                        mBriteDb.insert(SampleInfo.TABLE_NAME, sampleValues);
                     /*   if (sample.getId()==HistoryExperiment.ID_NONE){
                            mBriteDb.insert(SampleInfo.TABLE_NAME, sampleValues);
                        }else {
                            mBriteDb.update(SampleInfo.TABLE_NAME, sampleValues,"_id = ?",new String[]{sample.getId()+""});
                        }*/

                    }
                    //插入samplesB
                    for (int i = 0; i < firstInfo.getSamplesB().size(); i++) {
                        Sample sample = firstInfo.getSamplesB().get(i);

                        SampleInfoModel.Marshal sampleMarshal = SampleInfo.FACTORY.marshal();
                        ContentValues sampleValues = sampleMarshal.expe_id(expe_id)
                                .name(sample.getName())
                                .type(Sample.TYPE_B)
                                .asContentValues();
                        mBriteDb.insert(SampleInfo.TABLE_NAME, sampleValues);
                       /* if (sample.getId()==HistoryExperiment.ID_NONE){
                            mBriteDb.insert(SampleInfo.TABLE_NAME, sampleValues);
                        }else {
                            mBriteDb.update(SampleInfo.TABLE_NAME, sampleValues,"_id = ?",new String[]{sample.getId()+""});
                        }*/
                    }

                    //插入stage表

                    ExpeSettingSecondInfo secondInfo = experiment.getSettingSecondInfo();
                    List<Stage> stages = secondInfo.getSteps();
                    ContentValues stageValues;

                    for (int i = 0; i < stages.size(); i++) {
                        Stage stage = stages.get(i);
                        StageInfoModel.Marshal stageMarshal = StageInfo.FACTORY.marshal();
                        if (stage.getType() == Stage.TYPE_CYCLING) {
                            CyclingStage cyclingStage = (CyclingStage) stage;
                            stageValues = stageMarshal.expe_id(expe_id)
                                    .type((long) cyclingStage.getType())
                                    .cycling_count((long) cyclingStage.getCyclingCount())
                                    .serialNumber((long) cyclingStage.getSerialNumber())
                                    .asContentValues();

                            mBriteDb.insert(StageInfo.TABLE_NAME, stageValues);
                            //获取主键id
                            Cursor cyclingCursor = mBriteDb.getReadableDatabase().rawQuery("select last_insert_rowid() from stage_info", null);
                            cyclingCursor.moveToFirst();
                            long cycling_id = cyclingCursor.getInt(0);
                            cyclingCursor.close();
                            for (int j = 0; j < cyclingStage.getPartStageList().size(); j++) {
                                PartStage partStage = cyclingStage.getPartStageList().get(j);
                                long takePic = partStage.isTakePic() ? 1 : 0;
                                stageValues = stageMarshal.expe_id(expe_id)
                                        .type((long) partStage.getType())
                                        .startScale((double) partStage.getStartScale())
                                        .curScale((double) partStage.getCurScale())
                                        .serialNumber((long) partStage.getSerialNumber())
                                        .cycling_id(cycling_id)
                                        .during((long) partStage.getDuring())
                                        .part_takepic(takePic)
                                        .asContentValues();
                                mBriteDb.insert(StageInfo.TABLE_NAME, stageValues);
                            }

                        } else {
                            stageValues = stageMarshal.expe_id(expe_id)
                                    .type((long) stage.getType())
                                    .startScale((double) stage.getStartScale())
                                    .curScale((double) stage.getCurScale())
                                    .during((long) stage.getDuring())
                                    .asContentValues();
                            mBriteDb.insert(StageInfo.TABLE_NAME, stageValues);
                        }


                    }
                    transaction.markSuccessful();
                    response.setErr(0);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    transaction.end();
                }
            }
        });

    }
    public Observable<FindExpeResponse> findAllCompleted() {
        final FindExpeResponse response = new FindExpeResponse();
        response.setErr(-1);
        final SqlDelightStatement statement = ExpeInfo.FACTORY.find_all_completed();
        return mBriteDb.createQuery(ExpeInfo.TABLE_NAME,statement.statement)
                .mapToList(new Func1<Cursor, HistoryExperiment>() {
                    @Override
                    public HistoryExperiment call(Cursor cursor) {
                        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                        HistoryExperiment experiment = new HistoryExperiment();
                        try {
                            ExpeInfo expeInfo = ExpeInfo.FACTORY.find_all_completedMapper().map(cursor);
                            experiment.setName(expeInfo.name());
                            experiment.setId(expeInfo._id());
                            experiment.setDevice(expeInfo.device());
                            experiment.setMillitime(expeInfo.millitime());
                            experiment.setFinishMilliTime(expeInfo.finish_millitime());
                            experiment.setDuring(expeInfo.during());
                            if (expeInfo.autoIntTime()==null){
                                experiment.setAutoIntegrationTime(0);
                            }else {
                                experiment.setAutoIntegrationTime(expeInfo.autoIntTime().intValue());
                            }
                            ExperimentStatus status = new ExperimentStatus();
                            status.setStatus((int) expeInfo.status());
                            status.setDesc(expeInfo.status_desc());
                            experiment.setStatus(status);

                            String mode=expeInfo.mode();
                            String []modes=mode.split("-");
                            List<Mode> modeList=new ArrayList<>();
                            modeList.add(new DtMode(modes[0]));
                            if (modes.length>1){
                                modeList.add(new MeltMode(modes[1]));
                            }

                            ExpeSettingsFirstInfo expeSettingsFirstInfo = new ExpeSettingsFirstInfo();
                            experiment.setSettingsFirstInfo(expeSettingsFirstInfo);
                            //channel信息
                            List<Channel> channelList = new ArrayList<>();
                            SqlDelightStatement channelStatement = ChannelInfo.FACTORY.find_by_expeid(expeInfo._id());
                            Cursor channelCursor = mBriteDb.query(channelStatement.statement, channelStatement.args);
                            while (channelCursor.moveToNext()) {
                                ChannelInfo channelInfo = ChannelInfo.FACTORY.find_by_expeidMapper().map(channelCursor);
                                Channel channel = new Channel();
                                channel.setName(channelInfo.name());
                                channel.setValue(channelInfo.value());
                                channel.setRemark(channelInfo.remark());
                                channelList.add(channel);
                            }
                            expeSettingsFirstInfo.setChannels(channelList);
                            channelCursor.close();

                            //samples信息
                            List<Sample> sampleAList = new ArrayList<>();
                            List<Sample> sampleBList = new ArrayList<>();
                            SqlDelightStatement sampleStatement = SampleInfo.FACTORY.find_by_expeid(expeInfo._id());
                            Cursor sampleCursor = mBriteDb.query(sampleStatement.statement, sampleStatement.args);
                            while (sampleCursor.moveToNext()) {
                                SampleInfo sampleInfo = SampleInfo.FACTORY.find_by_expeidMapper().map(sampleCursor);
                                Sample sample = new Sample();
                                sample.setName(sampleInfo.name());
                                sample.setType((int) sampleInfo.type());
                                if (sampleInfo.type() == Sample.TYPE_A) {
                                    sampleAList.add(sample);
                                } else {
                                    sampleBList.add(sample);
                                }

                            }
                            sampleCursor.close();
                            expeSettingsFirstInfo.setSamplesA(sampleAList);
                            expeSettingsFirstInfo.setSamplesB(sampleBList);


                            ExpeSettingSecondInfo secondInfo = new ExpeSettingSecondInfo();
                            secondInfo.setModes(modeList);
                            experiment.setSettingSecondInfo(secondInfo);
                            SqlDelightStatement stageStatement = StageInfo.FACTORY.find_by_expeid(expeInfo._id());
                            Cursor stageCursor = mBriteDb.query(stageStatement.statement, stageStatement.args);

                            List<CyclingStage> cyclingStageList=new ArrayList<>();
                            List<PartStage> partStageList=new ArrayList<>();
                            List<Stage> stageList=new ArrayList<>();
                            EndStage endStage=new EndStage();
                            List<StartStage> startStages=new ArrayList<>();
                            //StartStage startStage=new StartStage();

                            List<MeltingStage> meltingStages=new ArrayList<>();
                            while (stageCursor.moveToNext()) {
                                StageInfo stageInfo = StageInfo.FACTORY.find_by_expeidMapper().map(stageCursor);
                                long type = stageInfo.type();

                                double startScale=0;
                                if (stageInfo.startScale()!=null){
                                    startScale=stageInfo.startScale();
                                }
                                double curScale=0;
                                if (stageInfo.curScale()!=null){
                                    curScale=stageInfo.curScale();
                                }
                                long serialNumber=0;
                                if (stageInfo.serialNumber()!=null){
                                    serialNumber=stageInfo.serialNumber();
                                }

                                long during=0;
                                if (stageInfo.during()!=null){
                                    during=stageInfo.during();
                                }
                                switch ((int) type) {
                                    case Stage.TYPE_MELTING:
                                        MeltingStage meltingStage=new MeltingStage();
                                        meltingStage.setStartScale((float) startScale);
                                        meltingStage.setCurScale((float) curScale);
                                        meltingStage.setDuring((short) during);
                                        meltingStages.add(meltingStage);
                                        break;
                                    case Stage.TYPE_END:
                                        endStage.setStartScale((float) startScale);
                                        endStage.setCurScale((float) curScale);
                                        endStage.setDuring((short) during);
                                        break;
                                    case Stage.TYPE_START:
                                        StartStage startStage=new StartStage();
                                        startStage.setStartScale((float) startScale);
                                        startStage.setCurScale((float) curScale);
                                        startStage.setDuring((short) during);

                                        startStages.add(startStage);
                                        break;
                                    case Stage.TYPE_CYCLING:
                                        long count=stageInfo.cycling_count();

                                        long id=stageInfo._id();
                                        CyclingStage cyclingStage=new CyclingStage();
                                        cyclingStage.setCyclingCount((int) count);
                                        cyclingStage.setSerialNumber((int) serialNumber);
                                        cyclingStage.setId((int) id);

                                        cyclingStageList.add(cyclingStage);
                                        break;
                                    case Stage.TYPE_PART:
                                        long cyclingId=stageInfo.cycling_id();
                                        long takepic=stageInfo.part_takepic();
                                        PartStage partStage=new PartStage();
                                        partStage.setStartScale((float) startScale);
                                        partStage.setCurScale((float) curScale);
                                        partStage.setSerialNumber((int) serialNumber);
                                        partStage.setCyclingId((int) cyclingId);
                                        partStage.setTakePic(takepic==1?true:false);
                                        partStage.setDuring((short) during);
                                        partStageList.add(partStage);
                                        break;
                                }
                            }
                            stageCursor.close();
                            Collections.sort(cyclingStageList, new Comparator<CyclingStage>() {
                                @Override
                                public int compare(CyclingStage o1, CyclingStage o2) {
                                    return o1.getSerialNumber()-o2.getSerialNumber();
                                }
                            });
                            for (int i=0;i<cyclingStageList.size();i++){
                                CyclingStage cyclingStage=cyclingStageList.get(i);
                                for (int j=0;j<partStageList.size();j++){
                                    int cyclingId=partStageList.get(j).getCyclingId();
                                    if (cyclingStage.getId()==cyclingId){
                                        cyclingStage.getPartStageList().add(partStageList.get(j));
                                    }
                                }
                                Collections.sort(cyclingStage.getPartStageList(), new Comparator<PartStage>() {
                                    @Override
                                    public int compare(PartStage o1, PartStage o2) {
                                        return o1.getSerialNumber()-o2.getSerialNumber();
                                    }
                                });
                            }
                            Collections.sort(cyclingStageList, new Comparator<CyclingStage>() {
                                @Override
                                public int compare(CyclingStage o1, CyclingStage o2) {
                                    return o1.getSerialNumber()-o2.getSerialNumber();
                                }
                            });

                            stageList.addAll(startStages);
                            for (int i=0;i<cyclingStageList.size();i++){
                                stageList.add(cyclingStageList.get(i));
                            }
                            stageList.add(endStage);

                            if (!meltingStages.isEmpty()) {
                                stageList.addAll(meltingStages);
                            }
                            secondInfo.setSteps(stageList);


                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            transaction.end();
                        }
                        return experiment;
                    }
                }).map(new Func1<List<HistoryExperiment>, FindExpeResponse>() {
                    @Override
                    public FindExpeResponse call(List<HistoryExperiment> historyExperiments) {
                        response.setItems(historyExperiments);
                        response.setErr(0);
                        return response;
                    }
                });
    }

    public Observable<FindExpeResponse> findAll() {
        final FindExpeResponse response = new FindExpeResponse();
        response.setErr(-1);
        final SqlDelightStatement statement = ExpeInfo.FACTORY.find_all();
        return mBriteDb.createQuery(ExpeInfo.TABLE_NAME,
                statement.statement)
                .mapToList(new Func1<Cursor, HistoryExperiment>() {
                    @Override
                    public HistoryExperiment call(Cursor cursor) {
                        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                        HistoryExperiment experiment = new HistoryExperiment();
                        try {

                            ExpeInfo expeInfo = ExpeInfo.FACTORY.find_allMapper().map(cursor);
                            experiment.setName(expeInfo.name());
                            experiment.setId(expeInfo._id());
                            experiment.setDevice(expeInfo.device());
                            experiment.setMillitime(expeInfo.millitime());
                            if (expeInfo.autoIntTime()==null){
                                experiment.setAutoIntegrationTime(0);
                            }else {
                                experiment.setAutoIntegrationTime(expeInfo.autoIntTime().intValue());
                            }

                            ExperimentStatus status = new ExperimentStatus();
                            status.setStatus((int) expeInfo.status());
                            status.setDesc(expeInfo.status_desc());
                            experiment.setStatus(status);

                            response.setErr(0);
                            transaction.markSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            transaction.end();
                        }
                        return experiment;
                    }
                }).map(new Func1<List<HistoryExperiment>, FindExpeResponse>() {
                    @Override
                    public FindExpeResponse call(List<HistoryExperiment> historyExperiments) {

                        response.setItems(historyExperiments);
                        return response;
                    }
                });
    }

    public Observable<FindExpeByIdResponse> findById(final FindExpeRequest request) {
        final FindExpeByIdResponse response = new FindExpeByIdResponse();
        response.setErr(-1);
        final SqlDelightStatement statement = ExpeInfo.FACTORY.find_by_id(request.getId());
        return mBriteDb.createQuery(ExpeInfo.TABLE_NAME, statement.statement,
                statement.args)

                .mapToOne(new Func1<Cursor, FindExpeByIdResponse>() {
                    @Override
                    public FindExpeByIdResponse call(Cursor cursor) {
                        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                        HistoryExperiment experiment = new HistoryExperiment();
                        try {
                            //实验基本信息
                            ExpeInfo expeInfo = ExpeInfo.FACTORY.find_by_idMapper().map(cursor);
                            experiment.setId(expeInfo._id());
                            experiment.setName(expeInfo.name());
                            experiment.setDevice(expeInfo.device());
                            experiment.setMillitime(expeInfo.millitime());
                            if (expeInfo.autoIntTime()==null){
                                experiment.setAutoIntegrationTime(0);
                            }else {
                                experiment.setAutoIntegrationTime(expeInfo.autoIntTime().intValue());
                            }
                            ExperimentStatus status = new ExperimentStatus();
                            status.setStatus((int) expeInfo.status());
                            status.setDesc(expeInfo.status_desc());
                            experiment.setStatus(status);

                            String mode=expeInfo.mode();
                            String []modes=mode.split("-");
                            List<Mode> modeList=new ArrayList<>();
                            modeList.add(new DtMode(modes[0]));
                            if (modes.length>1){
                                modeList.add(new MeltMode(modes[1]));
                            }

                            ExpeSettingsFirstInfo expeSettingsFirstInfo = new ExpeSettingsFirstInfo();
                            experiment.setSettingsFirstInfo(expeSettingsFirstInfo);
                            //channel信息
                            List<Channel> channelList = new ArrayList<>();
                            SqlDelightStatement channelStatement = ChannelInfo.FACTORY.find_by_expeid(expeInfo._id());
                            Cursor channelCursor = mBriteDb.query(channelStatement.statement, channelStatement.args);
                            while (channelCursor.moveToNext()) {
                                ChannelInfo channelInfo = ChannelInfo.FACTORY.find_by_expeidMapper().map(channelCursor);
                                Channel channel = new Channel();
                                channel.setName(channelInfo.name());
                                channel.setIntegrationTime(channelInfo.integration_time().intValue());
                                channel.setValue(channelInfo.value());
                                channel.setRemark(channelInfo.remark());
                                channelList.add(channel);
                            }
                            expeSettingsFirstInfo.setChannels(channelList);
                            channelCursor.close();

                            //samples信息
                            List<Sample> sampleAList = new ArrayList<>();
                            List<Sample> sampleBList = new ArrayList<>();
                            SqlDelightStatement sampleStatement = SampleInfo.FACTORY.find_by_expeid(expeInfo._id());
                            Cursor sampleCursor = mBriteDb.query(sampleStatement.statement, sampleStatement.args);
                            while (sampleCursor.moveToNext()) {
                                SampleInfo sampleInfo = SampleInfo.FACTORY.find_by_expeidMapper().map(sampleCursor);
                                Sample sample = new Sample();
                                sample.setName(sampleInfo.name());
                                sample.setType((int) sampleInfo.type());
                                if (sampleInfo.type() == Sample.TYPE_A) {
                                    sampleAList.add(sample);
                                } else {
                                    sampleBList.add(sample);
                                }

                            }
                            sampleCursor.close();
                            expeSettingsFirstInfo.setSamplesA(sampleAList);
                            expeSettingsFirstInfo.setSamplesB(sampleBList);


                            ExpeSettingSecondInfo secondInfo = new ExpeSettingSecondInfo();
                            secondInfo.setModes(modeList);
                            experiment.setSettingSecondInfo(secondInfo);
                            SqlDelightStatement stageStatement = StageInfo.FACTORY.find_by_expeid(expeInfo._id());
                            Cursor stageCursor = mBriteDb.query(stageStatement.statement, stageStatement.args);

                            List<CyclingStage> cyclingStageList=new ArrayList<>();
                            List<PartStage> partStageList=new ArrayList<>();
                            List<Stage> stageList=new ArrayList<>();
                            EndStage endStage=new EndStage();
                            List<StartStage> startStages=new ArrayList<>();
                          //  StartStage startStage=new StartStage();
                            List<MeltingStage> meltingStages=new ArrayList<>();
                            while (stageCursor.moveToNext()) {
                                StageInfo stageInfo = StageInfo.FACTORY.find_by_expeidMapper().map(stageCursor);
                                long type = stageInfo.type();

                                double startScale=0;
                                if (stageInfo.startScale()!=null){
                                    startScale=stageInfo.startScale();
                                }
                                double curScale=0;
                                if (stageInfo.curScale()!=null){
                                    curScale=stageInfo.curScale();
                                }
                                long serialNumber=0;
                                if (stageInfo.serialNumber()!=null){
                                     serialNumber=stageInfo.serialNumber();
                                }
                                long during=0;
                                if (stageInfo.during()!=null){
                                    during=stageInfo.during();
                                }
                                switch ((int) type) {
                                    case Stage.TYPE_MELTING:
                                        MeltingStage meltingStage=new MeltingStage();
                                        meltingStage.setStartScale((float) startScale);
                                        meltingStage.setCurScale((float) curScale);
                                        meltingStage.setDuring((short) during);
                                        meltingStages.add(meltingStage);
                                        break;
                                    case Stage.TYPE_END:
                                        endStage.setStartScale((float) startScale);
                                        endStage.setCurScale((float) curScale);
                                        endStage.setDuring((short) during);
                                        break;
                                    case Stage.TYPE_START:
                                        StartStage startStage=new StartStage();
                                        startStage.setStartScale((float) startScale);
                                        startStage.setCurScale((float) curScale);
                                        startStage.setDuring((short) during);
                                        startStages.add(startStage);
                                        break;
                                    case Stage.TYPE_CYCLING:
                                        long count=stageInfo.cycling_count();

                                        long id=stageInfo._id();
                                        CyclingStage cyclingStage=new CyclingStage();
                                        cyclingStage.setCyclingCount((int) count);
                                        cyclingStage.setSerialNumber((int) serialNumber);
                                        cyclingStage.setId((int) id);

                                        cyclingStageList.add(cyclingStage);
                                        break;
                                    case Stage.TYPE_PART:
                                        long cyclingId=stageInfo.cycling_id();
                                        long takepic=stageInfo.part_takepic();
                                        PartStage partStage=new PartStage();
                                        partStage.setStartScale((float) startScale);
                                        partStage.setCurScale((float) curScale);
                                        partStage.setSerialNumber((int) serialNumber);
                                        partStage.setCyclingId((int) cyclingId);
                                        partStage.setTakePic(takepic==1?true:false);
                                        partStage.setDuring((short) during);
                                        partStageList.add(partStage);
                                        break;
                                }
                            }
                            stageCursor.close();
                            Collections.sort(cyclingStageList, new Comparator<CyclingStage>() {
                                @Override
                                public int compare(CyclingStage o1, CyclingStage o2) {
                                    return o1.getSerialNumber()-o2.getSerialNumber();
                                }
                            });
                            for (int i=0;i<cyclingStageList.size();i++){
                                CyclingStage cyclingStage=cyclingStageList.get(i);
                                for (int j=0;j<partStageList.size();j++){
                                    int cyclingId=partStageList.get(j).getCyclingId();
                                    if (cyclingStage.getId()==cyclingId){
                                        cyclingStage.getPartStageList().add(partStageList.get(j));
                                    }
                                }
                                Collections.sort(cyclingStage.getPartStageList(), new Comparator<PartStage>() {
                                    @Override
                                    public int compare(PartStage o1, PartStage o2) {
                                        return o1.getSerialNumber()-o2.getSerialNumber();
                                    }
                                });
                            }
                            Collections.sort(cyclingStageList, new Comparator<CyclingStage>() {
                                @Override
                                public int compare(CyclingStage o1, CyclingStage o2) {
                                    return o1.getSerialNumber()-o2.getSerialNumber();
                                }
                            });

                            stageList.addAll(startStages);
                            for (int i=0;i<cyclingStageList.size();i++){
                                stageList.add(cyclingStageList.get(i));
                            }
                            stageList.add(endStage);

                            if (!meltingStages.isEmpty()){
                                stageList.addAll(meltingStages);
                            }

                            secondInfo.setSteps(stageList);
                            response.setErr(0);
                            response.setData(experiment);
                            transaction.markSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            transaction.end();
                        }

                        return response;
                    }
                });

    }
   /* public Observable<InsertUserResponse> insertExpe(final InsertUserRequest request) {
        return Observable.create(new Observable.OnSubscribe<InsertUserResponse>() {

            @Override
            public void call(Subscriber<? super InsertUserResponse> subscriber) {
                InsertUserResponse response=new InsertUserResponse();
                response.setErr(-1);
                final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                try {

                    UserModel.Marshal marshal = User.FACTORY.marshal();

                    long rowId=mBriteDb.insert(User.TABLE_NAME, marshal.username(request.getUsername())
                                    .password(request.getPwd()).asContentValues(),
                            SQLiteDatabase.CONFLICT_REPLACE);
                    transaction.markSuccessful();
                    response.setErr(0);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    transaction.end();
                }
            }
        });

    }*/


}
