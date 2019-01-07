package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

/**
 * 折线图上的一条折线
 */
public class LineData implements Parcelable {

    private List<Entry> entries;
    private int color;

    public LineData() {
    }

    protected LineData(Parcel in) {
        entries = in.createTypedArrayList(Entry.CREATOR);
        color = in.readInt();
    }

    public static final Creator<LineData> CREATOR = new Creator<LineData>() {
        @Override
        public LineData createFromParcel(Parcel in) {
            return new LineData(in);
        }

        @Override
        public LineData[] newArray(int size) {
            return new LineData[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(entries);
        dest.writeInt(color);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
