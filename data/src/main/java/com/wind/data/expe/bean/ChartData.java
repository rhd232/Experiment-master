package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ChartData implements Parcelable {

    private List<LineData> lineDataList;
    public ChartData(){}
    protected ChartData(Parcel in) {
        lineDataList = in.createTypedArrayList(LineData.CREATOR);
    }

    public static final Creator<ChartData> CREATOR = new Creator<ChartData>() {
        @Override
        public ChartData createFromParcel(Parcel in) {
            return new ChartData(in);
        }

        @Override
        public ChartData[] newArray(int size) {
            return new ChartData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(lineDataList);
    }

    public List<LineData> getLineDataList() {
        return lineDataList;
    }

    public void setLineDataList(List<LineData> lineDataList) {
        this.lineDataList = lineDataList;
    }
}
