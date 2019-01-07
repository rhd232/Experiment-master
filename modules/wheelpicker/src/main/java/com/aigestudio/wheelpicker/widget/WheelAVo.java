package com.aigestudio.wheelpicker.widget;

import java.util.ArrayList;
import java.util.List;

/**
 * 1级滚轮数据解析对象
 * Created by guodx on 16/6/4.
 */
public class WheelAVo {
    private String unit;
    private List<IWheelVo> list;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<IWheelVo> getList() {
        return list;
    }

    public void addList(IWheelVo item) {
        if (null == this.list) {
            this.list = new ArrayList<>();
        }
        this.list.add(item);
    }
}
