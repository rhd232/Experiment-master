package com.aigestudio.wheelpicker.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.aigestudio.wheelpicker.widget.WheelAAVo;
import com.aigestudio.wheelpicker.widget.WheelAVo;
import com.aigestudio.wheelpicker.widget.WheelSimpleVo;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * 简单Xml解析工具
 * Created by guodx on 16/6/4.
 */
public class WheelXmlParser {
    /**
     * 单轮数据解析
     *
     * @param context
     * @param xmlID
     * @param isHasAll 是否包含请选择／全部类似选项
     * @return
     */
    public static WheelAVo parserWheelAXML(Context context, int xmlID, boolean isHasAll) {
        XmlResourceParser xmlParser = context.getResources().getXml(xmlID);
        WheelAVo wheel = new WheelAVo();
        try {
            int eventType = xmlParser.getEventType();
            // 判断是否到了文件的结尾
            while (eventType != XmlResourceParser.END_DOCUMENT) {

                //文件的内容的起始标签开始
                if (eventType == XmlResourceParser.START_TAG) {
                    String tagname = xmlParser.getName();
                    if (tagname.endsWith("data")) {
                        wheel.setUnit(xmlParser.getAttributeValue(null, "unit"));
                    } else if (tagname.endsWith("item")) {
                        int id = xmlParser.getAttributeIntValue(null, "id", 0);
                        if (isHasAll || id != -1) {
                            // 不是全部的数据肯定可以加入，id＝-1的只有在需要显示的时候才可以
                            wheel.addList(new WheelSimpleVo(id, xmlParser.getAttributeValue(null, "label")));
                        }
                    } else if (tagname.endsWith("range")) {
                        int start = xmlParser.getAttributeIntValue(null, "start", 0);
                        int end = xmlParser.getAttributeIntValue(null, "end", 0);
                        for (int i = start; i <= end; i++) {
                            wheel.addList(new WheelSimpleVo(i, i + ""));
                        }
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {

                } else if (eventType == XmlResourceParser.TEXT) {

                }
                eventType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != xmlParser) {
                xmlParser.close();
            }
        }
        return wheel;
    }

    /**
     * 双轮数据解析
     *
     * @param context
     * @param xmlID
     * @param isHasAll 是否包含请选择／全部类似选项
     * @return
     */
    public static WheelAAVo parserWheelAAXML(Context context, int xmlID, boolean isHasAll) {
        XmlResourceParser xmlParser = context.getResources().getXml(xmlID);
        WheelAAVo wheel = new WheelAAVo();
        WheelSimpleVo item = null;

        try {
            int eventType = xmlParser.getEventType();
            // 判断是否到了文件的结尾
            while (eventType != XmlResourceParser.END_DOCUMENT) {

                //文件的内容的起始标签开始
                if (eventType == XmlResourceParser.START_TAG) {
                    String tagname = xmlParser.getName();
                    if (tagname.endsWith("data")) {
                        // 单位
                        wheel.setUnitA(xmlParser.getAttributeValue(null, "unitA"));
                        wheel.setUnitB(xmlParser.getAttributeValue(null, "unitB"));
                    } else if (tagname.endsWith("item-a")) {
                        // 初始化1级单条数据
                        int id = xmlParser.getAttributeIntValue(null, "id", 0);
                        if (isHasAll || id != -1) {
                            // 不是全部的数据肯定可以加入，id＝-1的只有在需要显示的时候才可以
                            item = new WheelSimpleVo(id, xmlParser.getAttributeValue(null, "label"));
                        } else {
                            item = null;
                        }
                    } else if (tagname.endsWith("range")) {
                        int start = xmlParser.getAttributeIntValue(null, "start", 0);
                        int end = xmlParser.getAttributeIntValue(null, "end", 0);
                        for (int i = start; i <= end; i++) {
                            item = new WheelSimpleVo(i, i + "");
                            for (int j = i; j <= end; j++) {
                                item.addChid(new WheelSimpleVo(j, j + ""));
                            }
                            wheel.addList(item);
                        }
                    } else if (tagname.endsWith("item")) {
                        // 1级单条数据，增加子数据
                        int id = xmlParser.getAttributeIntValue(null, "id", 0);
                        if (isHasAll || id != -1) {
                            // 不是全部的数据肯定可以加入，id＝-1的只有在需要显示的时候才可以
                            if (null != item) {
                                item.addChid(new WheelSimpleVo(id, xmlParser.getAttributeValue(null, "label")));
                            }
                        }
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {
                    String tagname = xmlParser.getName();
                    if (tagname.endsWith("item-a") && null != item) {
                        // 增加1级数据
                        wheel.addList(item);
                    }
                } else if (eventType == XmlResourceParser.TEXT) {

                }
                eventType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != xmlParser) {
                xmlParser.close();
            }
        }
        return wheel;
    }
}
