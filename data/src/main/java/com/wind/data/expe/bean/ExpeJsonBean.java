package com.wind.data.expe.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class ExpeJsonBean {


    private String name;
    private Device device;
    @JSONField(name = "create_millitime")
    private long createMillitime;
    @JSONField(name = "finish_millitime")
    private long finishMillitime;
    /**程序执行时长*/
    private long during;

    private ExpeMode pcr;
    private ExpeMode melting;

    private List<Channel> channels;

    private List<Sample> samplesA;
    private List<Sample> samplesB;

    private Stages stages;



    public static class Stages{
        @JSONField(name = "denaturation_stages")
        public List<DenaturationStage> denaturationStages;

        @JSONField(name = "cycling_stages")
        public List<CyclingStage> cyclingStages;

        @JSONField(name = "extension_stage")
        public ExtensionStage extensionStage;

        @JSONField(name = "melting_stages")
        public List<MeltingStage> meltingStages;
    }
    private static class BaseStage{
        public float temp;
        public int during;
        public String stepName;
    }

    public static class DenaturationStage extends BaseStage {
    }
    public static class PartStage extends BaseStage{
        /**是否执行拍照*/
        @JSONField(name = "take_pic")
        public boolean takePic;
    }
    public static class CyclingStage{
        @JSONField(name = "cycling_count")
        public int cyclingCount;
        @JSONField(name = "part_stages")
        public List<PartStage> partStages;
    }

    public static class ExtensionStage extends BaseStage{
    }
    public static class MeltingStage extends BaseStage{

    }
    public static class Sample{
        /**反应井编码 诸如A1 A2*/
        public String code;
        /**自定义的反应井名称 */
        public String name;
        /**当前反应井顺序号*/
        public int seq;
    }



    public static class ExpeMode{
        /**程序模式名称*/
        public String name;
        /**文件存放路径，请使用相对路径*/
        @JSONField(name = "data_file_name")
        public String dataFileName;
        /**是否自动积分*/
        @JSONField(name = "auto_int")
        public boolean autoInt;

        @JSONField(name = "ct_threshold")
        public float ctThreshold;
        @JSONField(name = "ct_min")
        public int ctMin;
    }
    public static class Device{
        @JSONField(name = "device_code")
        public String deviceCode;
        @JSONField(name = "device_name")
        public String deviceName;

    }

    public static class Channel{

        /**通道名称*/
        public String name;
        /**该通道使用的染料*/
        public String dye;
        /**通道备注*/
        public String remark;
        /**通道积分时间*/
        @JSONField(name = "integrtion_time")
        public int integrtionTime;

    }


    //=======================================
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public long getCreateMillitime() {
        return createMillitime;
    }

    public void setCreateMillitime(long createMillitime) {
        this.createMillitime = createMillitime;
    }

    public long getFinishMillitime() {
        return finishMillitime;
    }

    public void setFinishMillitime(long finishMillitime) {
        this.finishMillitime = finishMillitime;
    }

    public long getDuring() {
        return during;
    }

    public void setDuring(long during) {
        this.during = during;
    }

    public ExpeMode getPcr() {
        return pcr;
    }

    public void setPcr(ExpeMode pcr) {
        this.pcr = pcr;
    }

    public ExpeMode getMelting() {
        return melting;
    }

    public void setMelting(ExpeMode melting) {
        this.melting = melting;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Sample> getSamplesA() {
        return samplesA;
    }

    public void setSamplesA(List<Sample> samplesA) {
        this.samplesA = samplesA;
    }

    public List<Sample> getSamplesB() {
        return samplesB;
    }

    public void setSamplesB(List<Sample> samplesB) {
        this.samplesB = samplesB;
    }

    public Stages getStages() {
        return stages;
    }

    public void setStages(Stages stages) {
        this.stages = stages;
    }
}
