package com.aigestudio.wheelpicker.widget;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guodx on 16/6/3.
 */
public class WheelSimpleVo implements IWheelVo {
    private int id;
    private String label;
    private List<IWheelVo> children = null;

    public WheelSimpleVo(int id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Object getValue() {
        return id;
    }

    @Override
    public List<IWheelVo> getChildren() {
        return children;
    }

    @Override
    public void addChid(IWheelVo child) {
        if (null == this.children) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
}
