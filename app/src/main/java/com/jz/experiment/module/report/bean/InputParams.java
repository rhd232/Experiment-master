package com.jz.experiment.module.report.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.jz.experiment.widget.CtParamInputLayout;

import java.util.List;

public class InputParams implements Parcelable {
    public static final int EXPE_PCR=1;
    public static final int EXPE_MELTING=2;
    private CtParamInputLayout.CtParam ctParam;
    private int expeType;
    private String sourceDataPath;

    private List<String> ChanList;
    private List<String> KsList;

    public InputParams(){

    }


    protected InputParams(Parcel in) {
        ctParam = in.readParcelable(CtParamInputLayout.CtParam.class.getClassLoader());
        expeType = in.readInt();
        sourceDataPath = in.readString();
        ChanList = in.createStringArrayList();
        KsList = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(ctParam, flags);
        dest.writeInt(expeType);
        dest.writeString(sourceDataPath);
        dest.writeStringList(ChanList);
        dest.writeStringList(KsList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InputParams> CREATOR = new Creator<InputParams>() {
        @Override
        public InputParams createFromParcel(Parcel in) {
            return new InputParams(in);
        }

        @Override
        public InputParams[] newArray(int size) {
            return new InputParams[size];
        }
    };

    public CtParamInputLayout.CtParam getCtParam() {
        return ctParam;
    }

    public void setCtParam(CtParamInputLayout.CtParam ctParam) {
        this.ctParam = ctParam;
    }

    public int getExpeType() {
        return expeType;
    }

    public void setExpeType(int expeType) {
        this.expeType = expeType;
    }

    public String getSourceDataPath() {
        return sourceDataPath;
    }

    public void setSourceDataPath(String sourceDataPath) {
        this.sourceDataPath = sourceDataPath;
    }

    public List<String> getChanList() {
        return ChanList;
    }

    public void setChanList(List<String> chanList) {
        ChanList = chanList;
    }

    public List<String> getKsList() {
        return KsList;
    }

    public void setKsList(List<String> ksList) {
        KsList = ksList;
    }
}
