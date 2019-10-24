package com.aigestudio.wheelpicker.widget;


import java.util.List;

/**
 * Created by guodx on 2016/4/28.
 */
public interface IWheelVo {
    /**
     * 显示内容
     *
     * @return
     */
    String getLabel();

    /**
     * 值
     *
     * @return
     */
    Object getValue();

    /**
     * 获得子级数据
     *
     * @return
     */
    List<IWheelVo> getChildren();

    /**
     * 增加子数据
     *
     * @param child
     */
    void addChid(IWheelVo child);
}
