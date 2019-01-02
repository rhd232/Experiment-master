package com.wind.base.utils;

import com.wind.base.annotation.FormProperty;
import com.wind.base.annotation.Ignore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wind on 2018/3/14.
 */

public class MapTransformer {
    public static Map<String,String> transformObject2Map(Object o) {
        return transformObject2Map(o,o.getClass(),false);
    }
    public static Map<String,String> transformObject2Map(Object o, Class<?> cl) {
        return transformObject2Map(o,cl,false);
    }
    /**
     *
     * @param o
     * @param cl
     * @param hasConsiderArray get请求 有数组参数 时 为true。
     * @return
     */
    public static Map<String,String> transformObject2Map(Object o, Class<?> cl, boolean hasConsiderArray) {
        Map<String,String>  data = new HashMap<>();
        for (Class<?> superCl = cl.getSuperclass(); superCl != null
                && !superCl.equals(Object.class); superCl = superCl
                .getSuperclass()) {
            data.putAll(transformObject2Map(o, superCl,hasConsiderArray));
        }
        for (Field f : cl.getDeclaredFields()) {
            FormProperty fp = f.getAnnotation(FormProperty.class);
            Ignore ig = f.getAnnotation(Ignore.class);
            boolean accessFlag = f.isAccessible();
            f.setAccessible(true);
            try {
                if (ig != null) {
                    if (ig.byValue()) {
                        if (f.getType().equals(int.class)) {
                            if (f.getInt(o) != ig.intValue()) {
                                data.put(fp == null ? f
                                        .getName() : fp.value(), f.get(o)
                                        .toString());
                            }
                        } else if (f.getType().equals(String.class)) {
                            if (f.get(o) != null
                                    && !f.get(o).equals(ig.stringValue())) {
                                data.put(fp == null ? f
                                        .getName() : fp.value(), f.get(o)
                                        .toString());
                            }
                        }
                    }
                    continue;
                }
                if (fp == null && f.get(o) != null) {
                    String value=f.get(o)
                            .toString();
                    if (hasConsiderArray){
                        if (value.startsWith("[") && value.endsWith("]")){
                            value=value.substring(1,value.length());
                            value=value.substring(0,value.length()-1);
                            String[] vArray=value.split(",");
                            for (String v:vArray){
                                data.put(f.getName()+"[]", v);//数组参数
                            }
                        }else {
                            data.put(f.getName(), value);
                        }
                    }else {

                        data.put(f.getName(), value);
                    }

                } else if (f.get(o) != null) {
                    String value=f.get(o)
                            .toString();
                    if (hasConsiderArray){
                        if (value.startsWith("[") && value.endsWith("]")){
                            value=value.substring(1,value.length());
                            value=value.substring(0,value.length()-1);
                            String[] vArray=value.split(",");
                            for (String v:vArray){
                                data.put(fp.value()+"[]", v);//数组参数
                            }
                        }else {
                            data.put(fp.value(), value);
                        }

                    }else {
                        data.put(fp.value(), value);
                    }


                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            f.setAccessible(accessFlag);
        }
        return data;
    }
}
