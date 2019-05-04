package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Channel implements Parcelable {
    private long id=HistoryExperiment.ID_NONE;
    private String name;
    private String value;
    private String remark;//备注
    private boolean enabled;//是否可用

    public Channel(){}

    protected Channel(Parcel in) {
        id = in.readLong();
        name = in.readString();
        value = in.readString();
        remark = in.readString();
        enabled = in.readByte() != 0;
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(remark);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }
}
