package com.jz.experiment.module.expe.bean;


import com.anitoa.cmd.PcrCommand;

public class ChannelImageStatus {
    private int channelIndex;

    private int totalRow;
    private int curReadRow;

    public ChannelImageStatus(int channelIndex,int totalRow){
        this.channelIndex=channelIndex;
        this.totalRow=totalRow;
    }
    /**是否有数据读*/
    private boolean readable;

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public int getTotalRow() {
        return totalRow;
    }

    public void setTotalRow(int totalRow) {
        this.totalRow = totalRow;
    }

    public int getCurReadRow() {
        return curReadRow;
    }

    public void setCurReadRow(int curReadRow) {
        this.curReadRow = curReadRow;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    /**
     * 是否已经读完
     * @return
     */
    public boolean readed(){
        return curReadRow==totalRow;
    }

    public PcrCommand.PCR_IMAGE getPctImageCmd(){
        PcrCommand.PCR_IMAGE cmd=null;
        switch (channelIndex){
            case 0:
                cmd=PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_0;
                break;
            case 1:
                cmd=PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_1;
                break;
            case 2:
                cmd=PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_2;
                break;
            case 3:
                cmd=PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_3;
                break;
        }
        return cmd;
    }
}
