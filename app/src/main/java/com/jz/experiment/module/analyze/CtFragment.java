package com.jz.experiment.module.analyze;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;

import com.anitoa.exception.UnsupportedDeviceException;
import com.anitoa.well.SixteenWell;
import com.anitoa.well.Well;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.module.data.ExpeDataFragment;
import com.jz.experiment.module.data.adapter.ChannelDataAdapter;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ChannelData;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Sample;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class CtFragment extends BaseFragment {

    @BindView(R.id.gv_a)
    protected GridView gv_a;
    @BindView(R.id.gv_b)
    protected GridView gv_b;
    protected ChannelDataAdapter[] mChannelDataAdapters;
    protected HistoryExperiment mExeperiment;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        if (getArguments()!=null) {
            mExeperiment = getArguments().getParcelable(ExpeDataFragment.ARGS_KEY_EXPE);
        }
        mChannelDataAdapters = new ChannelDataAdapter[2];

        buildChannelData();


    }
    protected List<String> ChanList = new ArrayList<>();
    protected List<String> KSList = new ArrayList<>();
    protected void initChanAndKs(){
        ChanList.clear();
        ChanList.add("Chip#1");
        ChanList.add("Chip#2");
        ChanList.add("Chip#3");
        ChanList.add("Chip#4");
        try {
            KSList = Well.getWell().getKsList();
        } catch (UnsupportedDeviceException e) {
            //第一次安装，没有文件读取权限导致
            e.printStackTrace();
            KSList.clear();
            KSList=new SixteenWell().getKsList();
        }
    }
    protected void buildChannelData() {

        GridView[] gvs = new GridView[2];
        gvs[0] = gv_a;
        gvs[1] = gv_b;
        String[] titles = {"A", "B"};

        String [] channelAlias=new String[4];
        String [][] sampleAlias=new String[2][8];

        if (mExeperiment!=null){
            List<Channel> channels=mExeperiment.getSettingsFirstInfo().getChannels();
            for (int i=0;i<channels.size();i++){
                String val=channels.get(i).getValue();
                if (TextUtils.isEmpty(val)){
                    String fullName=channels.get(i).getName();
                    if (fullName.startsWith("Channel")){
                        val=channels.get(i).getName().replace("nel","");
                    }else {
                        val=channels.get(i).getName();
                    }

                }else {
                    val+=channels.get(i).getRemark();
                }
                channelAlias[i]=val;
            }

            List<Sample> sampleAs=mExeperiment.getSettingsFirstInfo().getSamplesA();
            for (int i=0;i<sampleAs.size();i++){
                String val=sampleAs.get(i).getName();
                if (TextUtils.isEmpty(val)){
                    val=(i+1)+"";
                }
                sampleAlias[0][i]=val;
            }
            List<Sample> sampleBs=mExeperiment.getSettingsFirstInfo().getSamplesB();
            for (int i=0;i<sampleBs.size();i++){
                String val=sampleBs.get(i).getName();
                if (TextUtils.isEmpty(val)){
                    val=(i+1)+"";
                }
                sampleAlias[1][i]=val;
            }
        }else {
            for (int i=0;i<4;i++){
                String channel=getString(R.string.setup_channel);
                if (channel.startsWith("Channel")){
                    channel=channel.replace("nel","");
                }
                channelAlias[i]=channel+(i+1);
            }
            for (int i=0;i<8;i++){
                String val=(i+1)+"";
                sampleAlias[0][i]=val;
            }
            for (int i=0;i<8;i++){
                String val=(i+1)+"";
                sampleAlias[1][i]=val;
            }
        }

        for (int k = 0; k < gvs.length; k++) {
            mChannelDataAdapters[k] = new ChannelDataAdapter(getActivity(), R.layout.item_channel_data);
            gvs[k].setAdapter(mChannelDataAdapters[k]);
            List<ChannelData> channelDataAList = new ArrayList<>();
            String [] sampleAlias_=sampleAlias[k];
            for (int j = 0; j < 5; j++) {
                for (int i = 0; i < 9; i++) {

                    String channelName;
                    if (j==0){
                        channelName = titles[k];
                    }else {
                        channelName = channelAlias[j-1];
                    }
                    /*switch (j) {
                        case 0:
                            channelName = titles[k];
                            break;
                        case 1:
                            channelName = channelAlias[0];
                            break;
                        case 2:
                            channelName = "通道2";
                            break;
                        case 3:
                            channelName = "通道3";
                            break;
                        case 4:
                            channelName = "通道4";
                            break;
                    }*/
                    String sAlias;
                    if (i==0){
                        sAlias="";
                    }else {
                        sAlias=sampleAlias_[i-1];
                    }
                    ChannelData channelData = new ChannelData(channelName, i, sAlias,"");
                    channelDataAList.add(channelData);
                }
            }
            mChannelDataAdapters[k].replaceAll(channelDataAList);
        }
    }


    protected void getCtValue(String chan, String currks, double[][] ctValues,boolean[][] falsePositive) {
        if (!CommData.diclist.keySet().contains(chan) || CommData.diclist.get(chan).size() == 0)
            return;

        int currChan = 0;


        int line = 1;
        switch (chan) {
            case "Chip#1":
                currChan = 0;

                line = 1;
                break;
            case "Chip#2":
                currChan = 1;
                line = 2;
                break;
            case "Chip#3":
                currChan = 2;
                line = 3;

                break;
            case "Chip#4":
                currChan = 3;
                line = 4;
                break;
        }


        int gvIndex;
        int ksIndexInAdapter;
        int ksindex;

        NumberVal numberVal=numberVal(currks,line);
        if (numberVal==null){
            return;
        }
        gvIndex=numberVal.gvIndex;
        ksindex=numberVal.ksindex;
        ksIndexInAdapter=numberVal.ksIndexInAdapter;
        //还需要判断是否是假阳性
        String negative=getString(R.string.negative);
        String ctValue;
        if (falsePositive[currChan][ksindex] && isPcrMode()){
            //假阳性

            ctValue = "["+negative+"]";
        }else {
            double val = ctValues[currChan][ksindex];
            if (val>0) {
                DecimalFormat format = new DecimalFormat("#0.00");
                ctValue = format.format(val);
            }else {
                if (isPcrMode()) {
                    ctValue = negative;
                }else {
                    DecimalFormat format = new DecimalFormat("#0.00");
                    ctValue = format.format(val);
                }
            }

        }



        mChannelDataAdapters[gvIndex].getItem(ksIndexInAdapter).setSampleVal(ctValue);
    }

    protected abstract boolean isPcrMode();


    protected void notifyCtChanged(){
        for (int i=0;i<mChannelDataAdapters.length;i++){
            mChannelDataAdapters[i].notifyDataSetChanged();
        }
    }

    private NumberVal numberVal(String currks,int line){

        switch (CommData.KsIndex){
            case 4:
                return fourWell(currks,line);
            case 8:
                return eightWell(currks,line);
            case 16:
                return sixteenWell(currks,line);
        }
        return null;

    }
    private NumberVal sixteenWell(String currks,int line){
        int gvIndex = 0;
        int ksIndexInAdapter = 0;
        int ksindex = -1;
        int lineCount = 9;//反应孔数+1
        switch (currks) {
            case "A1":
                gvIndex = 0;
                ksindex = 0;
                ksIndexInAdapter = lineCount * line + 1;
                break;
            case "A2":
                gvIndex = 0;
                ksindex = 1;

                ksIndexInAdapter = lineCount * line + 2;
                break;
            case "A3":
                gvIndex = 0;
                ksindex = 2;

                ksIndexInAdapter = lineCount * line + 3;
                break;
            case "A4":
                gvIndex = 0;
                ksindex = 3;
                ksIndexInAdapter = lineCount * line + 4;
                break;
            case "A5":
                gvIndex = 0;
                ksindex = 4;
                ksIndexInAdapter = lineCount * line + 5;
                break;
            case "A6":
                gvIndex = 0;
                ksindex = 5;
                ksIndexInAdapter = lineCount * line + 6;
                break;
            case "A7":
                gvIndex = 0;
                ksindex = 6;
                ksIndexInAdapter = lineCount * line + 7;
                break;
            case "A8":
                gvIndex = 0;
                ksindex = 7;
                ksIndexInAdapter = lineCount * line + 8;
                break;
            case "B1":
                gvIndex = 1;
                ksindex = 8;
                ksIndexInAdapter = lineCount * line + 1;
                break;
            case "B2":
                gvIndex = 1;
                ksindex = 9;

                ksIndexInAdapter = lineCount * line + 2;
                break;
            case "B3":
                gvIndex = 1;
                ksindex = 10;

                ksIndexInAdapter = lineCount * line + 3;
                break;
            case "B4":
                gvIndex = 1;
                ksindex = 11;

                ksIndexInAdapter = lineCount * line + 4;
                break;
            case "B5":
                gvIndex = 1;
                ksindex = 12;
                ksIndexInAdapter = lineCount * line + 5;
                break;
            case "B6":
                gvIndex = 1;
                ksindex = 13;
                ksIndexInAdapter = lineCount * line + 6;
                break;
            case "B7":
                gvIndex = 1;
                ksindex = 14;
                ksIndexInAdapter = lineCount * line + 7;
                break;
            case "B8":
                gvIndex = 1;
                ksindex = 15;
                ksIndexInAdapter = lineCount * line + 8;
                break;
        }
        NumberVal numberVal=new NumberVal();
        numberVal.gvIndex=gvIndex;
        numberVal.ksIndexInAdapter=ksIndexInAdapter;
        numberVal.ksindex=ksindex;
        return numberVal;
    }
    private NumberVal eightWell(String currks,int line){
        int gvIndex = 0;
        int ksIndexInAdapter = 0;
        int ksindex = -1;
        int lineCount = 9;//反应孔数+1
        switch (currks) {
            case "A1":
                gvIndex = 0;
                ksindex = 0;
                ksIndexInAdapter = lineCount * line + 1;
                break;
            case "A2":
                gvIndex = 0;
                ksindex = 1;

                ksIndexInAdapter = lineCount * line + 2;
                break;
            case "A3":
                gvIndex = 0;
                ksindex = 2;

                ksIndexInAdapter = lineCount * line + 3;
                break;
            case "A4":
                gvIndex = 0;
                ksindex = 3;

                ksIndexInAdapter = lineCount * line + 4;
                break;
            case "B1":
                gvIndex = 1;
                ksindex = 4;

                ksIndexInAdapter = lineCount * line + 1;
                break;
            case "B2":
                gvIndex = 1;
                ksindex = 5;

                ksIndexInAdapter = lineCount * line + 2;
                break;
            case "B3":
                gvIndex = 1;
                ksindex = 6;

                ksIndexInAdapter = lineCount * line + 3;
                break;
            case "B4":
                gvIndex = 1;
                ksindex = 7;

                ksIndexInAdapter = lineCount * line + 4;
                break;
        }

        NumberVal numberVal=new NumberVal();
        numberVal.gvIndex=gvIndex;
        numberVal.ksIndexInAdapter=ksIndexInAdapter;
        numberVal.ksindex=ksindex;

        return numberVal;
    }


    private NumberVal fourWell(String currks,int line){
        int gvIndex = 0;
        int ksIndexInAdapter = 0;
        int ksindex = -1;
        int lineCount = 9;//反应孔数+1
        switch (currks) {
            case "A1":
                gvIndex = 0;
                ksindex = 0;
                ksIndexInAdapter = lineCount * line + 1;
                break;
            case "A2":
                gvIndex = 0;
                ksindex = 1;

                ksIndexInAdapter = lineCount * line + 2;
                break;

            case "A3":
                gvIndex = 0;
                ksindex = 2;

                ksIndexInAdapter = lineCount * line + 3;
                break;
            case "A4":
                gvIndex = 0;
                ksindex = 3;

                ksIndexInAdapter = lineCount * line + 4;
                break;

        }

        NumberVal numberVal=new NumberVal();
        numberVal.gvIndex=gvIndex;
        numberVal.ksIndexInAdapter=ksIndexInAdapter;
        numberVal.ksindex=ksindex;

        return numberVal;
    }

    private static class NumberVal{
        public int gvIndex = 0;
        public int ksIndexInAdapter = 0;
        public int ksindex = -1;
    }


}
