package com.aigestudio.wheelpicker.utils;

import android.app.AlertDialog;
import android.support.annotation.XmlRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aigestudio.wheelpicker.R;
import com.aigestudio.wheelpicker.core.AbstractWheelPicker;
import com.aigestudio.wheelpicker.view.WheelCurvedPicker;
import com.aigestudio.wheelpicker.widget.IWheelVo;
import com.aigestudio.wheelpicker.widget.WheelAAVo;
import com.aigestudio.wheelpicker.widget.WheelAVo;
import com.aigestudio.wheelpicker.widget.WheelSimpleVo;
import com.aigestudio.wheelpicker.widget.WheelSimpleVo2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 滚轮工具
 * Created by guodx on 16/6/3.
 */
public class WheelPickerFactory {
    public interface High {
        int HIGH_DEFAULT_WOMAN = 160;
        int HIGH_DEFAULT_MAN = 170;
    }

    public interface Birthday {
        int BIRTHDAY_MIN_AGE = 18;
        int BIRTHDAY_MAX_AGE = 100;
        int BIRTHDAY_DEFAULT_AGE = 26;
    }

    /**
     * 弹窗确认点击事件
     */
    public interface OnWheelClickListener {
        void onResult(View v, IWheelVo[] result, int[] indexs, String[] unit);
    }

    /**
     * 显示弹窗选择，单轮
     *
     * @param view         点击的View对象，处理结果
     * @param listener
     * @param xmlId        数据xml资源id
     * @param defaultValue 默认选中项
     */
    public static void showWheelAPicker(View view, final OnWheelClickListener listener,
                                        @XmlRes int xmlId, String defaultValue) {
        showWheelAPicker(view, listener, xmlId, defaultValue, false);
    }

    /**
     * 显示弹窗选择，单轮
     *
     * @param view         点击的View对象，处理结果
     * @param listener
     * @param xmlId        数据xml资源id
     * @param defaultValue 默认选中项
     * @param isHasAll     是否显示请选择/全部选项，如果显示，请配置对应数据id为-1，其他数据id不能为-1
     */
    public static void showWheelAPicker(View view, final OnWheelClickListener listener,
                                        @XmlRes int xmlId, String defaultValue, boolean isHasAll) {
        // 数据
        WheelAVo wheelAVo = WheelXmlParser.parserWheelAXML(view.getContext(), xmlId, isHasAll);
        int defaultIndex = 0;
        if (null != defaultValue && !"".equals(defaultValue)) {
            // 去除单位
            if (null != wheelAVo.getUnit() && !"".equals(wheelAVo.getUnit())) {
                defaultValue = defaultValue.replace(wheelAVo.getUnit(), "");
            }
            for (int i = 0; i < wheelAVo.getList().size(); i++) {
                IWheelVo vo = wheelAVo.getList().get(i);
                if (vo.getLabel().equals(defaultValue)) {
                    defaultIndex = i;
                    break;
                }
            }
        }
        showWheelAPicker(view, listener, wheelAVo.getList(), wheelAVo.getUnit(), defaultIndex);
    }

    /**
     * 显示单滚轮
     *
     * @param view         点击的View对象，处理结果
     * @param listener
     * @param list         数据
     * @param unit         单位
     * @param defaultIndex 默认选中项下标
     */
    public static void showWheelAPicker(final View view, final OnWheelClickListener listener,
                                         List<IWheelVo> list, final String unit, int defaultIndex) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(view.getContext(),
                R.layout.cnlib_dialog_wheelpicker_layout_a, true,
                true, true, 0);
        final Button mOkBtn = (Button) alertDialog.findViewById(R.id.btn_ok);
        Button mCancelBtn = (Button) alertDialog.findViewById(R.id.btn_cancel);
        final WheelCurvedPicker mApicker = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_a);
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // 确认
                if (null != listener) {
                    listener.onResult(view, new IWheelVo[]{mApicker.getSelectedData()},
                            new int[]{mApicker.getSelectedIndex()}, new String[]{unit});
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                alertDialog.dismiss();
            }
        });
        mApicker.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {

            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });

        // 默认选中
        mApicker.setData(list, defaultIndex);

        // 单位
        if (null != unit && !"".equals(unit)) {
            TextView mUnitTv = (TextView) alertDialog.findViewById(R.id.tv_unit_a);
            mUnitTv.setText(unit);
            mUnitTv.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 显示双滚轮
     *
     * @param view          点击的View对象，处理结果
     * @param listener
     * @param xmlId         数据xml资源id
     * @param defaultAValue 1级默认选中项
     * @param defaultBValue 2级默认选中项
     */
    public static void showWheelAAPicker(View view, final OnWheelClickListener listener,
                                         @XmlRes int xmlId, String defaultAValue, String defaultBValue) {
        showWheelAAPicker(view, listener, xmlId, defaultAValue, defaultBValue, false);
    }

    /**
     * 显示双滚轮
     *
     * @param view          点击的View对象，处理结果
     * @param listener
     * @param xmlId         数据xml资源id
     * @param defaultAValue 1级默认选中项
     * @param defaultBValue 2级默认选中项
     * @param isHasAll      是否显示请选择/全部选项，如果显示，请配置对应数据id为-1，其他数据id不能为-1
     */
    public static void showWheelAAPicker(View view, final OnWheelClickListener listener,
                                         @XmlRes int xmlId, String defaultAValue,
                                         String defaultBValue, boolean isHasAll) {
        long startTime=System.currentTimeMillis();
        // 数据
        WheelAAVo wheelVo = WheelXmlParser.parserWheelAAXML(view.getContext(), xmlId, isHasAll);
        long during=System.currentTimeMillis()-startTime;
        Log.e("Xml",during+"");
        int defaultAIndex = 0;
        int defaultBIndex = 0;
        if (null != defaultAValue && !"".equals(defaultAValue)) {
            // 去除单位
            if (null != wheelVo.getUnitA() && !"".equals(wheelVo.getUnitA())) {
                defaultAValue = defaultAValue.replace(wheelVo.getUnitA(), "");
            }

            for (int i = 0; i < wheelVo.getList().size(); i++) {
                IWheelVo vo = wheelVo.getList().get(i);
                if (vo.getLabel().equals(defaultAValue)) {
                    defaultAIndex = i;
                    // 子数据不为空，且有数据
                    if (null != vo.getChildren() && null != defaultBValue && !"".equals(defaultBValue)) {
                        // 去除单位
                        if (null != wheelVo.getUnitB() && !"".equals(wheelVo.getUnitB())) {
                            defaultBValue = defaultBValue.replace(wheelVo.getUnitB(), "");
                        }

                        for (int j = 0; j < vo.getChildren().size(); j++) {
                            IWheelVo child = vo.getChildren().get(j);
                            if (child.getLabel().equals(defaultBValue)) {
                                defaultBIndex = j;
                                break;
                            }
                        }
                    }

                    break;
                }
            }
        }
        showWheelAAPicker(view, listener, wheelVo.getList(), wheelVo.getUnitA(), wheelVo.getUnitB(),
                defaultAIndex, defaultBIndex);
    }

    /**
     * 显示双滚轮
     *
     * @param view          点击的View对象，处理结果
     * @param listener
     * @param list          数据
     * @param unitA         1级单位
     * @param unitB         2级单位
     * @param defaultAIndex 1级默认选中项下标
     * @param defaultBIndex 2级默认选中项下标
     */
    private static void showWheelAAPicker(final View view, final OnWheelClickListener listener,
                                          List<IWheelVo> list, final String unitA, final String unitB,
                                          final int defaultAIndex, final int defaultBIndex) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(view.getContext(),
                R.layout.cnlib_dialog_wheelpicker_layout_a_a, true,
                true, true, 0);
        final Button mOkBtn = (Button) alertDialog.findViewById(R.id.btn_ok);
        Button mCancelBtn = (Button) alertDialog.findViewById(R.id.btn_cancel);
        // 1级滚轮
        final WheelCurvedPicker mApickerA = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_a);
        // 2级滚轮
        final WheelCurvedPicker mApickerB = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_b);
        // 1级单位
        final TextView mUnitATv = (TextView) alertDialog.findViewById(R.id.tv_unit_a);
        // 2级单位
        final TextView mUnitBTv = (TextView) alertDialog.findViewById(R.id.tv_unit_b);


        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // 确认
                if (null != listener) {
                    listener.onResult(view, new IWheelVo[]{mApickerA.getSelectedData(), mApickerB.getSelectedData()},
                            new int[]{mApickerA.getSelectedIndex(), mApickerB.getSelectedIndex()},
                            new String[]{unitA, unitB});
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                alertDialog.dismiss();
            }
        });
        mApickerA.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {
                // 默认选中
                int defaultindex = defaultBIndex;
                if (index != defaultAIndex) {
                    // 不是默认选中项 则默认选中第一项
                    defaultindex = 0;
                }
                if (null == data.getChildren() || data.getChildren().size() == 0 || defaultindex < 0) {
                    defaultindex = 0;
                } else if (defaultindex >= data.getChildren().size()) {
                    defaultindex = data.getChildren().size() - 1;
                }

                mApickerB.setData(data.getChildren(), defaultindex);
                if (null != unitA && !"".equals(unitA)) {
                    if (-1 == (int) data.getValue()) {
                        mUnitATv.setVisibility(View.INVISIBLE);
                    } else {
                        mUnitATv.setVisibility(View.VISIBLE);
                    }

                    if (null != unitB && !"".equals(unitB)) {
                        if (null == data.getChildren() || data.getChildren().size() == 0
                                || -1 == (int) data.getChildren().get(defaultindex).getValue()) {
                            mUnitBTv.setVisibility(View.INVISIBLE);
                        } else {
                            mUnitBTv.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });
        mApickerB.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {
                if (null != unitB && !"".equals(unitB)) {
                    if (-1 == (int) data.getValue()) {
                        mUnitBTv.setVisibility(View.INVISIBLE);
                    } else {
                        mUnitBTv.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });

        // 默认选中
        mApickerA.setData(list, defaultAIndex);

        // 单位
        if (null != unitA && !"".equals(unitA)) {
            mUnitATv.setText(unitA);
        }
        if (null != unitB && !"".equals(unitB)) {
            mUnitBTv.setText(unitB);
        }
    }

    public static void showWheelNumberAAPicker(final View view, final OnWheelClickListener listener, int startNumber, final int endNumber, String defaultA, String defaultB, boolean hasAll){
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(view.getContext(),
                R.layout.cnlib_dialog_wheelpicker_layout_a_a, true,
                true, true, 0);
        final Button mOkBtn = (Button) alertDialog.findViewById(R.id.btn_ok);
        Button mCancelBtn = (Button) alertDialog.findViewById(R.id.btn_cancel);
        // 1级滚轮
        final WheelCurvedPicker mApickerA = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_a);
        // 2级滚轮
        final WheelCurvedPicker mApickerB = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_b);
        // 1级单位
        final TextView mUnitATv = (TextView) alertDialog.findViewById(R.id.tv_unit_a);
        // 2级单位
        final TextView mUnitBTv = (TextView) alertDialog.findViewById(R.id.tv_unit_b);
        final String unitA="";
        final String unitB="";

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // 确认
                if (null != listener) {
                    listener.onResult(view, new IWheelVo[]{mApickerA.getSelectedData(), mApickerB.getSelectedData()},
                            new int[]{mApickerA.getSelectedIndex(), mApickerB.getSelectedIndex()},
                            new String[]{unitA, unitB});
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                alertDialog.dismiss();
            }
        });
        mApickerA.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {
                // 默认选中
               /* int defaultindex = defaultBIndex;
                if (index != defaultAIndex) {
                    // 不是默认选中项 则默认选中第一项
                    defaultindex = 0;
                }
                if (null == data.getChildren() || data.getChildren().size() == 0 || defaultindex < 0) {
                    defaultindex = 0;
                } else if (defaultindex >= data.getChildren().size()) {
                    defaultindex = data.getChildren().size() - 1;
                }*/
                String label=data.getLabel();
                if ("不限".equals(label)){
                    List<IWheelVo> empty=new ArrayList<>();
                    mApickerB.setData(empty);
                    return;
                }
                int startValue=Integer.parseInt(data.getLabel());
                mApickerB.setData(buildData(startValue,endNumber), 0);
                if (null != unitA && !"".equals(unitA)) {
                    if (-1 == (int) data.getValue()) {
                        mUnitATv.setVisibility(View.INVISIBLE);
                    } else {
                        mUnitATv.setVisibility(View.VISIBLE);
                    }

                    /*if (null != unitB && !"".equals(unitB)) {
                        if (null == data.getChildren() || data.getChildren().size() == 0
                                || -1 == (int) data.getChildren().get(defaultindex).getValue()) {
                            mUnitBTv.setVisibility(View.INVISIBLE);
                        } else {
                            mUnitBTv.setVisibility(View.VISIBLE);
                        }
                    }*/
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });
        mApickerB.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {
                if (null != unitB && !"".equals(unitB)) {
                    if (-1 == (int) data.getValue()) {
                        mUnitBTv.setVisibility(View.INVISIBLE);
                    } else {
                        mUnitBTv.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });

        int startValue=startNumber;
        int endValue=endNumber;
        List<IWheelVo> list=buildData(startValue,endValue);
        if (hasAll){
            list.add(0,new WheelSimpleVo(-1,"不限"));
        }

        int defaultAIndex=0;
        for (int i=0;i<list.size();i++){
           if (defaultA.equals(list.get(i).getLabel())){
               defaultAIndex=i;
               break;
           }
        }
        // 默认选中
        mApickerA.setData(list, defaultAIndex);

        // 单位
        if (null != unitA && !"".equals(unitA)) {
            mUnitATv.setText(unitA);
        }
        if (null != unitB && !"".equals(unitB)) {
            mUnitBTv.setText(unitB);
        }
    }

    private static List<IWheelVo> buildData(int startValue, int endValue) {
        List<IWheelVo> data=new ArrayList<>();
        for (int i=startValue;i<=endValue;i++){
            data.add(new WheelSimpleVo(i,i+""));
        }

        return data;
    }

    /**
     * 显示3滚轮，出生日期
     *
     * @param view
     * @param listener
     * @param defalutyear  默认选中年份
     * @param defalutmonth 默认选中月份
     * @param defalutday   默认选中日期
     */
    public static void showWheelBirthdayPicker(final View view, final OnWheelClickListener listener,
                                               int defalutyear, int defalutmonth, int defalutday) {
        if (defalutyear == 0) {
            defalutyear = new Date().getYear() + 1900 - WheelPickerFactory.Birthday.BIRTHDAY_DEFAULT_AGE;
        }
        if (defalutmonth == 0) {
            defalutmonth = 1;
        }
        if (defalutday == 0) {
            defalutday = 1;
        }

        final int year = defalutyear;
        final int month = defalutmonth;
        final int day = defalutday;

        Date date = new Date();
        final int thisyear = date.getYear() + 1900;


        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(view.getContext(),
                R.layout.cnlib_dialog_wheelpicker_layout_a_a_a, true,
                true, true, 0);
        final Button mOkBtn = (Button) alertDialog.findViewById(R.id.btn_ok);
        Button mCancelBtn = (Button) alertDialog.findViewById(R.id.btn_cancel);
        // 1级滚轮
        final WheelCurvedPicker mApickerA = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_a);
        // 2级滚轮
        final WheelCurvedPicker mApickerB = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_b);
        // 3级滚轮
        final WheelCurvedPicker mApickerC = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_c);
        // 1级单位
        final TextView mUnitATv = (TextView) alertDialog.findViewById(R.id.tv_unit_a);
        // 2级单位
        final TextView mUnitBTv = (TextView) alertDialog.findViewById(R.id.tv_unit_b);
        // 3级单位
        final TextView mUnitCTv = (TextView) alertDialog.findViewById(R.id.tv_unit_c);
        final String unitA = "年";
        final String unitB = "月";
        final String unitC = "日";


        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // 确认
                if (null != listener) {
                    listener.onResult(view, new IWheelVo[]{mApickerA.getSelectedData(), mApickerB.getSelectedData(), mApickerC.getSelectedData()},
                            new int[]{mApickerA.getSelectedIndex(), mApickerB.getSelectedIndex(), mApickerC.getSelectedIndex()},
                            new String[]{unitA, unitB, unitC});
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                alertDialog.dismiss();
            }
        });
        mApickerA.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {
                // 当前选中的年份
                int selectyear = (int) data.getValue();


                // 默认选中
                int defaultMonthIndex = month - 1;
                if (selectyear != year || defaultMonthIndex < 0) {
                    // 不是默认选中项 则默认选中第一项
                    defaultMonthIndex = 0;
                } else if (defaultMonthIndex > 11) {
                    defaultMonthIndex = 11;
                }

                // day数据
                List<IWheelVo> l3 = getDays(selectyear, defaultMonthIndex + 1);
                int defaultDayIndex = day - 1;
                if (selectyear != year || (defaultMonthIndex + 1) != month || defaultDayIndex < 0) {
                    // 不是默认选中项 则默认选中第一项
                    defaultDayIndex = 0;
                } else if (defaultDayIndex >= l3.size()) {
                    defaultDayIndex = l3.size() - 1;
                }

                mApickerB.setItemIndex(defaultMonthIndex);
                mApickerC.setData(l3, defaultDayIndex);
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });
        mApickerB.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {
                // 当前选中的年份
                int selectyear = (int) mApickerA.getSelectedData().getValue();
                int selectmonth = (int) data.getValue();

                // day数据
                List<IWheelVo> l3 = getDays(selectyear, selectmonth);

                // 默认选中
                int defaultDayIndex = day - 1;
                if (selectyear != year || selectmonth != month || defaultDayIndex < 0) {
                    // 不是默认选中项 则默认选中第一项
                    defaultDayIndex = 0;
                } else if (defaultDayIndex >= l3.size()) {
                    defaultDayIndex = l3.size() - 1;
                }
                mApickerC.setData(l3, defaultDayIndex);
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });
        mApickerC.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {

            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });

        // 默认选中
        mApickerA.setData(getBirthdayYears(thisyear), year - thisyear + Birthday.BIRTHDAY_MAX_AGE);
        mApickerB.setData(getMonths(), month);

        // 单位
        if (null != unitA && !"".equals(unitA)) {
            mUnitATv.setText(unitA);
            mUnitATv.setVisibility(View.VISIBLE);
        }
        if (null != unitB && !"".equals(unitB)) {
            mUnitBTv.setText(unitB);
            mUnitBTv.setVisibility(View.VISIBLE);
        }
        if (null != unitC && !"".equals(unitC)) {
            mUnitCTv.setText(unitC);
            mUnitCTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获得日期显示年份
     *
     * @param thisyear 当前年份
     * @return
     */
    private static List<IWheelVo> getBirthdayYears(int thisyear) {
        List<IWheelVo> l1 = new ArrayList<>();
        for (int i = thisyear - Birthday.BIRTHDAY_MAX_AGE; i <= thisyear - Birthday.BIRTHDAY_MIN_AGE; i++) {
            l1.add(new WheelSimpleVo(i, i + ""));
        }
        return l1;
    }

    /**
     * 获得月份
     *
     * @return
     */
    private static List<IWheelVo> getMonths() {
        List<IWheelVo> l2 = new ArrayList<>();
        for (int j = 1; j <= 12; j++) {
            if (j < 10) {
                l2.add(new WheelSimpleVo(j, "0" + j));
            } else {
                l2.add(new WheelSimpleVo(j, "" + j));
            }
        }
        return l2;
    }

    /**
     * 所有类型day数据，28天，29天，30天，31天
     */
    private static Map<Integer, List<IWheelVo>> months = null;

    /**
     * 按年月获取对应天数
     *
     * @param year
     * @param month
     * @return
     */
    private static List<IWheelVo> getDays(int year, int month) {
        int maxday = 31;
        if (month == 2) {
            //  2月份
            if (year % 4 == 0) {
                maxday = 29;
            } else {
                maxday = 28;
            }
        } else if (month < 8 && month % 2 == 0) {
            // 偶数月份
            maxday = 30;
        } else if (month >= 8 && month % 2 == 1) {
            maxday = 30;
        }
        if (null != months && months.containsKey(maxday)) {
            return months.get(maxday);
        }

        List<IWheelVo> l3 = new ArrayList<>();
        for (int k = 1; k <= maxday; k++) {
            if (k < 10) {
                l3.add(new WheelSimpleVo(k, "0" + k));
            } else {
                l3.add(new WheelSimpleVo(k, "" + k));
            }

        }

        if (null == months) {
            months = new HashMap<>();
        }
        months.put(maxday, l3);

        return l3;
    }

    /**
     * 显示3滚轮，活动时间
     *
     * @param view
     * @param listener
     * @param date     默认选中日期 1999-09-29
     * @param hour     默认选中小时
     * @param minute   默认选中分
     */
    public static void showWheelPlanTimePicker(final View view, final OnWheelClickListener listener,
                                               final String date, final int hour, final int minute) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(view.getContext(),
                R.layout.cnlib_dialog_wheelpicker_layout_a_a_a, true,
                true, true, 0);
        final Button mOkBtn = (Button) alertDialog.findViewById(R.id.btn_ok);
        Button mCancelBtn = (Button) alertDialog.findViewById(R.id.btn_cancel);
        // 1级滚轮
        final WheelCurvedPicker mApickerA = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_a);
        // 2级滚轮
        final WheelCurvedPicker mApickerB = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_b);
        // 3级滚轮
        final WheelCurvedPicker mApickerC = (WheelCurvedPicker) alertDialog.findViewById(R.id.wcp_c);
        // 2级单位
        final TextView mUnitBTv = (TextView) alertDialog.findViewById(R.id.tv_unit_b);
        // 3级单位
        final TextView mUnitCTv = (TextView) alertDialog.findViewById(R.id.tv_unit_c);
        final String unitB = "时";
        final String unitC = "分";


        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // 确认
                if (null != listener) {
                    listener.onResult(view, new IWheelVo[]{mApickerA.getSelectedData(), mApickerB.getSelectedData(), mApickerC.getSelectedData()},
                            new int[]{mApickerA.getSelectedIndex(), mApickerB.getSelectedIndex(), mApickerC.getSelectedIndex()},
                            new String[]{"", unitB, unitC});
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                alertDialog.dismiss();
            }
        });
        mApickerA.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {

            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });
        mApickerB.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {

            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });
        mApickerC.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {

            }

            @Override
            public void onWheelSelected(int index, IWheelVo data) {

            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (state != AbstractWheelPicker.SCROLL_STATE_IDLE) {
                    mOkBtn.setClickable(false);
                } else {
                    mOkBtn.setClickable(true);
                }
            }
        });

        // 默认选中 当前日期3小时候
        List<IWheelVo> list = getFuture30Days();
        int defaultday = 0;
        if (null != date && !"".equals(date)) {
            for (int i = 0; i < list.size(); i++) {
                if (date.equals(list.get(i).getValue().toString())) {
                    defaultday = i;
                    break;
                }
            }
        }
        mApickerA.setData(list, defaultday);
        mApickerB.setData(getHoursOrMinutes(24), hour);
        mApickerC.setData(getHoursOrMinutes(60), minute);

        // 单位
        if (null != unitB && !"".equals(unitB)) {
            mUnitBTv.setText(unitB);
            mUnitBTv.setVisibility(View.VISIBLE);
        }
        if (null != unitC && !"".equals(unitC)) {
            mUnitCTv.setText(unitC);
            mUnitCTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 拿到将来30天的数据，并且存储一份对应关系
     */
    private static List<IWheelVo> getFuture30Days() {
        // 真实值
        List<IWheelVo> future30Days = new ArrayList<>();

        Calendar theCa = Calendar.getInstance();
        Date today = new Date();
        // 今天
        future30Days.add(new WheelSimpleVo2(DateUtil.formatYYYYMMDD(today), "今天"));

        theCa.setTime(today);
        for (int i = 1; i < 30; i++) {
            theCa.add(theCa.DATE, 1);
            Date date = theCa.getTime();
            String label = null;
            if (i == 1) {
                label = "明天";
            } else if (i == 2) {
                label = "后天";
            } else {
                label = DateUtil.formatMMDDE(date);
            }
            future30Days.add(new WheelSimpleVo2(DateUtil.formatYYYYMMDD(date), label));
        }
        return future30Days;
    }


    /**
     * 所有类型hour，minute数据，24小时，60分
     */
    private static Map<Integer, List<IWheelVo>> hoursOrMinutes = null;

    /**
     * 拿到小时数 或者 分数
     *
     * @return
     */
    private static List<IWheelVo> getHoursOrMinutes(int max) {
        if (null != hoursOrMinutes && hoursOrMinutes.containsKey(max)) {
            return hoursOrMinutes.get(max);
        }
        List<IWheelVo> list = new ArrayList<IWheelVo>();
        for (int i = 0; i < max; i++) {
            if (i < 10) {
                list.add(new WheelSimpleVo2(i + "", "0" + i));
            } else {
                list.add(new WheelSimpleVo2(i + "", i + ""));
            }
        }
        if (null == hoursOrMinutes) {
            hoursOrMinutes = new HashMap<>();
        }
        hoursOrMinutes.put(max, list);
        return list;
    }
}
