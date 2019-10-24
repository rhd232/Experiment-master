package com.aigestudio.wheelpicker.widget;

import java.util.ArrayList;
import java.util.List;

/**
 * 2级滚轮数据解析对象
 * Created by guodx on 16/6/4.
 */
public class WheelAAVo {
    private String unitA;           // 1级单位
    private String unitB;           // 2级单位
    private List<IWheelVo> list;    // 1级数据

    public List<IWheelVo> getList() {
        return list;
    }

    public void addList(IWheelVo item) {
        if (null == this.list) {
            this.list = new ArrayList<>();
        }
        this.list.add(item);
    }

    public String getUnitA() {
        return unitA;
    }

    public void setUnitA(String unitA) {
        this.unitA = unitA;
    }

    public String getUnitB() {
        return unitB;
    }

    public void setUnitB(String unitB) {
        this.unitB = unitB;
    }
}