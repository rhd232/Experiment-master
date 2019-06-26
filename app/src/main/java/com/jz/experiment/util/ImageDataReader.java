package com.jz.experiment.util;

import android.text.TextUtils;

import com.anitoa.bean.Data;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.listener.AnitoaConnectionListener;
import com.anitoa.listener.SimpleConnectionListener;
import com.anitoa.service.CommunicationService;
import com.anitoa.util.AnitoaLogUtil;
import com.anitoa.util.ByteUtil;
import com.anitoa.well.Well;
import com.jz.experiment.chart.CCurveShow;
import com.jz.experiment.chart.CCurveShowPolyFit;
import com.jz.experiment.chart.ChartData;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DataFileReader;
import com.jz.experiment.chart.FactUpdater;
import com.jz.experiment.module.expe.bean.ChannelImageStatus;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.HistoryExperiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import rx.Observable;
import rx.Subscriber;

import static com.anitoa.util.ThreadUtil.sleep;

public class ImageDataReader {

    private CommunicationService mCommunicationService;
    private HistoryExperiment mExperiment;
    PcrCommand.IMAGE_MODE mImageMode;
    private File mAutoIntFile;
    private FactUpdater mFactUpdater;

    float[] opt_int_time = new float[CCurveShow.MAX_CHAN];
    float[] max_read_list = new float[CCurveShow.MAX_CHAN];
    float[] inc_factor = new float[CCurveShow.MAX_CHAN];
    int[] max_read_0 = new int[CCurveShow.MAX_CHAN];
    public static final String FILE_NAME = "auth_int_time.txt";
    private ExecutorService mExecutorService;
    public ImageDataReader(
                           CommunicationService communicationService,
                           HistoryExperiment experiment, FactUpdater factUpdater,
                          ExecutorService executorService) {
        this.mCommunicationService = communicationService;
        this.mExperiment = experiment;
        mImageMode = PcrCommand.IMAGE_MODE.IMAGE_12;
        mFactUpdater = factUpdater;

        mExecutorService=executorService;

        jfindex=0;

        DataFileUtil.deleteFile(FILE_NAME);
        mAutoIntFile = DataFileUtil.getOrCreateFile(FILE_NAME);

        mCommunicationService.setNotify(mListener);
    }


    private List<ChannelImageStatus> mChannelStatusList;

    public Observable<Boolean> autoInt(){
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                for (int i = 0; i < CCurveShow.MAX_CHAN; i++)
                {
                    opt_int_time[i] = 1;
                    max_read_list[i] = 30;
                    inc_factor[i] = 0;
                    max_read_0[i] =20;
                    setSensorAndInTime(i,Math.round(opt_int_time[i]));
                }
                readAllImg();

                setOnAutoIntTimeListener(new OnAutoIntTimeListener() {
                    @Override
                    public void onAutoIntTimeFinished() {
                        subscriber.onNext(true);
                    }
                });
            }
        });

    }


    private void readAllImg() {

        AnitoaLogUtil.writeFileLog("第"+(jfindex+1)+"次读取图像版",mExecutorService);
        mChannelStatusList = new ArrayList<>();
        List<Channel> channels = mExperiment.getSettingsFirstInfo().getChannels();
        //防止读出其他通道数据(设置了chip2第一条可能读出chip3，下位机bug)
        boolean channel0Selected = !TextUtils.isEmpty(channels.get(0).getValue());
        boolean channel1Selected = !TextUtils.isEmpty(channels.get(1).getValue());
        boolean channel2Selected = !TextUtils.isEmpty(channels.get(2).getValue());
        boolean channel3Selected = !TextUtils.isEmpty(channels.get(3).getValue());
        ChannelImageStatus channel0ImageStatus = new ChannelImageStatus(0,
                mImageMode.getSize());
        channel0ImageStatus.setReadable(channel0Selected);
        mChannelStatusList.add(channel0ImageStatus);

        ChannelImageStatus channel1ImageStatus = new ChannelImageStatus(1, mImageMode.getSize());
        channel1ImageStatus.setReadable(channel1Selected);
        mChannelStatusList.add(channel1ImageStatus);

        ChannelImageStatus channel2ImageStatus = new ChannelImageStatus(2, mImageMode.getSize());
        channel2ImageStatus.setReadable(channel2Selected);
        mChannelStatusList.add(channel2ImageStatus);

        ChannelImageStatus channel3ImageStatus = new ChannelImageStatus(3, mImageMode.getSize());
        channel3ImageStatus.setReadable(channel3Selected);
        mChannelStatusList.add(channel3ImageStatus);
        int readableIndex = -1;
        for (int i = 0; i < mChannelStatusList.size(); i++) {
            if (mChannelStatusList.get(i).isReadable()) {
                readableIndex = i;
                break;
            }
        }
        if (readableIndex != -1) {
            //读取通道图像数据
            sendReadImgCmd(mChannelStatusList.get(readableIndex).getPctImageCmd());
        }


    }

    private void sendReadImgCmd(PcrCommand.PCR_IMAGE pcr_image) {
        PcrCommand command = new PcrCommand();
        command.step7(pcr_image);
        mCommunicationService.sendPcrCommand(command);
    }


    private Map<String, List<String>> mItemData = new LinkedHashMap<>();
    private AnitoaConnectionListener mListener = new SimpleConnectionListener() {
        @Override
        public void onReceivedData(Data data) {
            byte[] reveicedBytes = data.getBuffer();
            int statusIndex = 1;
            int status_ = reveicedBytes[statusIndex];
            //TODO 检查返回的包是否正确
            boolean succ = StatusChecker.checkStatus(status_);
            if (!succ) {
                return;
            }
            int cmdIndex = 2;
            int cmd = reveicedBytes[cmdIndex];

            int dataStartIndex = 5;
            int curRowIndex = reveicedBytes[dataStartIndex];//从0开始

            int typeIndex = 4;
            int type = (reveicedBytes[typeIndex]);
            if (cmd == PcrCommand.STEP_7_CMD) {

                boolean readed = false;//本通道数据是否已经读完
                int channelIndex = -1;
                String chip = "Chip#unknow";
                if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_0.getValue()) {
                    //通道1图像数据返回
                    mChannelStatusList.get(0).setCurReadRow((curRowIndex + 1));//
                    readed = mChannelStatusList.get(0).readed();
                    channelIndex = 1;
                    chip = "Chip#1";
                } else if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_1.getValue()) {
                    //通道2图像数据返回
                    mChannelStatusList.get(1).setCurReadRow((curRowIndex + 1));//
                    readed = mChannelStatusList.get(1).readed();
                    channelIndex = 2;
                    chip = "Chip#2";
                } else if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_2.getValue()) {
                    //通道3图像数据返回
                    mChannelStatusList.get(2).setCurReadRow((curRowIndex + 1));//
                    readed = mChannelStatusList.get(2).readed();
                    channelIndex = 3;
                    chip = "Chip#3";
                } else if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_3.getValue()) {
                    //通道4图像数据返回
                    mChannelStatusList.get(3).setCurReadRow((curRowIndex + 1));//
                    readed = mChannelStatusList.get(3).readed();
                    channelIndex = 4;
                    chip = "Chip#4";
                }


                String imageData = transferImageData(channelIndex, curRowIndex, reveicedBytes, true);

                if (mItemData.get(chip) == null) {
                    mItemData.put(chip, new ArrayList<String>());
                }
                mItemData.get(chip).add(imageData);

                //检查本通道是否已经读取完毕
                if (readed) {
                    //执行下一个通道的读取操作
                    int next = -1;
                    for (int i = 0; i < mChannelStatusList.size(); i++) {
                        ChannelImageStatus status = mChannelStatusList.get(i);
                        if (status.isReadable() && !status.readed()) {
                            next = i;
                            break;
                        }
                    }
                    if (next == -1) {
                        //全部通道已经全部读取完
                        //将数据保存到文件中
                        List<Channel> channels = mExperiment.getSettingsFirstInfo().getChannels();
                        boolean channel0Selected = !TextUtils.isEmpty(channels.get(0).getValue());
                        boolean channel1Selected = !TextUtils.isEmpty(channels.get(1).getValue());
                        boolean channel2Selected = !TextUtils.isEmpty(channels.get(2).getValue());
                        boolean channel3Selected = !TextUtils.isEmpty(channels.get(3).getValue());
                        boolean needSave = true;
                        if (channel0Selected) {
                            List<String> list = mItemData.get("Chip#1");
                            if (list == null || list.size() != 12) {
                                needSave = false;
                            }
                        }
                        if (channel1Selected) {
                            List<String> list = mItemData.get("Chip#2");
                            if (list == null || list.size() != 12) {
                                needSave = false;
                            }
                        }
                        if (channel2Selected) {
                            List<String> list = mItemData.get("Chip#3");
                            if (list == null || list.size() != 12) {
                                needSave = false;
                            }
                        }
                        if (channel3Selected) {
                            List<String> list = mItemData.get("Chip#4");
                            if (list == null || list.size() != 12) {
                                needSave = false;
                            }
                        }
                        if (needSave) {
                            Set<Map.Entry<String, List<String>>> entries = mItemData.entrySet();
                            for (Map.Entry<String, List<String>> entry : entries) {
                                String key = entry.getKey();
                                List<String> value = entry.getValue();
                                StringBuilder sBuilder = new StringBuilder();
                                sBuilder.append(key)
                                        .append("\n");
                                for (String item : value) {
                                    sBuilder.append(item)
                                            .append("\n");
                                }
                                //System.out.println("图像内容："+ sBuilder.toString());
                                AnitoaLogUtil.writeToFile(mAutoIntFile, sBuilder.toString());
                            }
                        }
                        mItemData.clear();

                        if (mOnImageDataPrepareListener != null) {
                            mOnImageDataPrepareListener.onImageDataPrepared();
                        }
                    } else {
                        sendReadImgCmd(mChannelStatusList.get(next).getPctImageCmd());
                    }
                }

            }
        }

    };


    private String transferImageData(int chan, int k, byte[] reveicedBytes, boolean inCycling) {
        int count;

        if (inCycling) {
            count = mImageMode.getSize() + 1;
        } else {
            count = mImageMode.getSize() + 2;
        }
        int[] txData = new int[count];

        //aa 00 02 1c 02 0b 07112b12001117115012bb1063118b0a6311741bb50e000ed07a0d1717
        for (int numData = 0; numData < mImageMode.getSize(); numData++) {
            byte high = reveicedBytes[numData * 2 + 7];
            byte low = reveicedBytes[numData * 2 + 6];

            int value = TrimReader.getInstance()
                    .tocalADCCorrection(numData, high, low,
                            mImageMode.getSize(), chan,
                            CommData.gain_mode, 0);

            txData[numData] = value;
        }
        //当前行号
        txData[mImageMode.getSize()] = reveicedBytes[5];
        if (!inCycling) {
            if (reveicedBytes[5] == 0) {
                //TODO 待验证
                byte[] buffers = new byte[4];
                buffers[0] = reveicedBytes[CommData.imgFrame * 2 + 6];
                buffers[1] = reveicedBytes[CommData.imgFrame * 2 + 7];
                buffers[2] = reveicedBytes[CommData.imgFrame * 2 + 8];
                buffers[3] = reveicedBytes[CommData.imgFrame * 2 + 9];
                //float t = BitConverter.ToSingle(buffers, 0);
                float t = ByteUtil.getFloat(buffers);
                txData[count - 1] = (int) t;
            }
        }
        String res = "";
        //   String newres = "";
        for (int i = 0; i < txData.length; i++) {
            if (i == 0) {
                res = txData[i] + "";
                //  newres = txData[i] + "";
            } else {
                if (i != 11 && i != 23) {
                    res += " " + txData[i];
                    //   newres += "    " + txData[i];
                } else {
                    if (k == 11 || k == 23) {
                        res += " " + mFactUpdater.GetFactValueByXS(chan);
                        //   newres += "    " + mFactUpdater.GetFactValueByXS(chan);
                    } else {
                        res += " " + txData[i];
                        //  newres += "    " + txData[i];
                    }

                }

            }

        }


        return res;
    }



    private OnImageDataPrepareListener mOnImageDataPrepareListener=new OnImageDataPrepareListener() {
        @Override
        public void onImageDataPrepared() {
            InputStream ips = null;
            try {
                ips = new FileInputStream(mAutoIntFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            DataFileReader.getInstance().ReadFileData(ips, false, true);

            AutocalibInt();

            jfindex++;


            if (jfindex>5){
                //设置积分时间和gain模式
                sleep(50);

                PcrCommand gainCmd = PcrCommand.ofGainMode(CommData.gain_mode);

                mCommunicationService.sendPcrCommandSync(gainCmd);

                mFactUpdater.int_time_1= (float) (opt_int_time[0]);
                mFactUpdater.int_time_2= (float) (opt_int_time[1]);
                mFactUpdater.int_time_3= (float) (opt_int_time[2]);
                mFactUpdater.int_time_4= (float) (opt_int_time[3]);

                sleep(50);
                setSensorAndInTime(0, mFactUpdater.int_time_1);
                setSensorAndInTime(1, mFactUpdater.int_time_2);
                setSensorAndInTime(2, mFactUpdater.int_time_3);
                setSensorAndInTime(3, mFactUpdater.int_time_4);
                StringBuilder sBuilder=new StringBuilder();
                sBuilder.append("通道1积分时间："+ mFactUpdater.int_time_1).append("\n");
                sBuilder.append("通道2积分时间："+ mFactUpdater.int_time_2).append("\n");
                sBuilder.append("通道3积分时间："+ mFactUpdater.int_time_3).append("\n");
                sBuilder.append("通道4积分时间："+ mFactUpdater.int_time_4).append("\n");
                //System.out.println(sBuilder.toString());
                AnitoaLogUtil.writeFileLog(sBuilder.toString(),mExecutorService);
                mOnAutoIntTimeListener.onAutoIntTimeFinished();
            }else {
                readAllImg();
            }
        }
    };




    private int jfindex;
    private final float AutoInt_Target = 1600;
    private void AutocalibInt() {
        int max_read;
        float inc;
        float i_factor = 0;

        for (int i = 0; i < CCurveShow.MAX_CHAN; i++) {
            max_read = GetMaxChanRead(i);
            max_read_list[i] = max_read;
            if (jfindex==0){
                max_read_0[i]=max_read;

            }else if (jfindex == 1) {
                max_read -= max_read_0[i];
                if (max_read < 20) max_read = 20;

                float top;

                switch(i)
                {
                    case 0:
                        top = 0.5f;
                        break;
                    case 1:
                        top = 0.6f;
                        break;
                    case 2:
                        top = 1.6f;
                        break;
                    case 3:
                        top = 1.0f;
                        break;
                    default:
                        top = 0;
                        break;
                }

                i_factor = top / (float)max_read;
                inc_factor[i] = i_factor;
            }


            //inc = (float)Math.Round(Convert.ToDouble((AutoInt_Target - max_read) * inc_factor[i]), 2);    // slowly approach the opt int time to avoid saturation
            double v=Math.round((AutoInt_Target - max_read) * inc_factor[i]);
            inc = Float.parseFloat(String.format("%.2f",v));
            if (inc<0)
                inc=0;

            if (jfindex == 0)
            {
                opt_int_time[i] = 2;
            }
            else
            {
                opt_int_time[i] += inc;
            }

            if (opt_int_time[i] > 600)
                opt_int_time[i] = 600;


            setSensorAndInTime(i,Math.round(opt_int_time[i]));
            opt_int_time[i] = (float)Math.round(opt_int_time[i]);
        }

        StringBuilder sBuilder=new StringBuilder();
        sBuilder.append("通道1：max_read:"+ max_read_list[0]+"   int_time:"+opt_int_time[0]).append("\n");
        sBuilder.append("通道2：max_read:"+ max_read_list[1]+"   int_time:"+opt_int_time[1]).append("\n");
        sBuilder.append("通道3：max_read:"+ max_read_list[2]+"   int_time:"+opt_int_time[2]).append("\n");
        sBuilder.append("通道4：max_read:"+ max_read_list[3]+"   int_time:"+opt_int_time[3]).append("\n");

        //System.out.println(sBuilder.toString());
        AnitoaLogUtil.writeFileLog(sBuilder.toString(),mExecutorService);
    }
    private void setSensorAndInTime(int c, float inTime) {
        sleep(50);
        PcrCommand sensorCmd = new PcrCommand();
        sensorCmd.setSensor(c);
        mCommunicationService.sendPcrCommandSync(sensorCmd);

        sleep(50);
        PcrCommand inTimeCmd =PcrCommand.ofIntergrationTime(inTime);
        mCommunicationService.sendPcrCommandSync(inTimeCmd);

    }
    private int GetMaxChanRead(int ch) {
        double[][][] m_yData = ReadCCurveShow();
        double[][] yData = new double[CCurveShow.MAX_WELL][CCurveShow.MAX_CYCL];
        double max = 0;
        for (int i = 0; i < CommData.KsIndex; i++) {
            for (int n = 0; n < CCurveShow.MAX_CYCL; n++) {
                yData[i][n] = m_yData[ch][i][n];
                if (m_yData[ch][i][n] > max) {
                    max = m_yData[ch][i][n];
                }
            }
        }
        return (int) max;
    }

    private double[][][] ReadCCurveShow() {
        double[][][] m_yData = new double[CCurveShow.MAX_CHAN][ CCurveShow.MAX_WELL][CCurveShow.MAX_CYCL];
        CCurveShowPolyFit cCurveShow =  CCurveShowPolyFit.getInstance();
        cCurveShow.InitData();

        List<String> kslist= Well.getWell().getKsList();
        List<String> tdlist = new ArrayList<>();//定义通道
        tdlist.add("Chip#1");
        tdlist.add("Chip#2");
        tdlist.add("Chip#3");
        tdlist.add("Chip#4");

        for (int i = 0; i < tdlist.size(); i++)
        {
            for (int n = 0; n < kslist.size(); n++)
            {
                //List<ChartData> cdlist = CommData.GetChartData(tdlist[i], 0, kslist[n]);//获取选点值
                List<ChartData> cdlist = CommData.GetMaxChartData(tdlist.get(i), 0, kslist.get(n));//获取选点值
                for (int k = 0; k < cdlist.size(); k++)
                {
                    m_yData[GetChan(tdlist.get(i))][n][ k] = cdlist.get(k).y;
                }
            }
        }

        return m_yData;
    }

    private int GetChan(String chan)
    {
        int currChan = -1;

        switch (chan)
        {
            case "Chip#1":
                currChan = 0;
                break;
            case "Chip#2":
                currChan = 1;
                break;
            case "Chip#3":
                currChan = 2;
                break;
            case "Chip#4":
                currChan = 3;
                break;
        }
        return currChan;
    }




    public interface OnImageDataPrepareListener {
        void onImageDataPrepared();
    }

    private OnAutoIntTimeListener mOnAutoIntTimeListener;
    public void setOnAutoIntTimeListener(OnAutoIntTimeListener listener) {
        mOnAutoIntTimeListener = listener;
    }

    public interface OnAutoIntTimeListener{
        void onAutoIntTimeFinished();
    }
}

