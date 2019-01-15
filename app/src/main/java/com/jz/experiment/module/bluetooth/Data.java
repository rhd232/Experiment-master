package com.jz.experiment.module.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

public class Data implements Parcelable {
    private byte[] buffer;
    private int size;

    public Data(byte[] buffer, int size) {
        this.buffer = buffer;
        this.size = size;
    }

    protected Data(Parcel in) {
        buffer = in.createByteArray();
        size = in.readInt();
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(buffer);
        dest.writeInt(size);
    }

    @Override
    public String toString() {
        try {
            return new String(buffer,"ISO-8859-1");
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }
}
