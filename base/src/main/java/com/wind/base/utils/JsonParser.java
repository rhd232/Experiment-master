package com.wind.base.utils;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

public class JsonParser {
	
	public static <T> List<T> parserArray(String jsonArrayString, Class<T> clazz)
			throws JSONException {
		return JSON.parseArray(jsonArrayString, clazz);
	}

	public static <T> T parserObject(String jsonString, Class<T> clazz) {
		return JSON.parseObject(jsonString, clazz);
	}

	public static  String object2Json(Object obj){
		return JSON.toJSONString(obj);
	}
	public static  String list2Json(List list){
		return JSON.toJSONString(list);
	}
	public static  String map2Json(Map map){
		return JSON.toJSONString(map);
	}
}
