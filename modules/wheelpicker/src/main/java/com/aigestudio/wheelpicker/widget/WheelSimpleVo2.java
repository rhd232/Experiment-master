package com.aigestudio.wheelpicker.widget;


import java.util.List;

/**
 * 时间对象
 * Created by guodx on 16/6/5.
 */
public class WheelSimpleVo2 implements IWheelVo {
    private String value;   // 真实日期
    private String label;   // 显示值

    public WheelSimpleVo2(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public List<IWheelVo> getChildren() {
        return null;
    }

    @Override
    public void addChid(IWheelVo child) {

    }
}
