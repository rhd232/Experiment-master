package com.jz.experiment.chart;

import com.jz.experiment.util.DataFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataParser {

    public static Map<Integer,List<List<String>>> parseDtData(File dataFile){
        Map<Integer,List<List<String>>> chanMap=new LinkedHashMap<>();

        List<String> data = DataFileUtil.covertToList(dataFile);
        Map<String, List<String>> dataMap = new HashMap<>();
        dataMap.put("data", data);
        JSONObject jsonObject = new JSONObject(dataMap);
        String url="http://114.215.195.137:55500/data";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , jsonObject.toString());
        Request request = new Request.Builder()
                .url(url)//请求的url
                .post(requestBody)
                .build();
        okhttp3.Call call = okHttpClient.newCall(request);
        try {
            Response response =call.execute();
            //获取服务器转换好的图形数据
            String vals=response.body().toString();
            //TODO 转换


            try {
                JSONArray jsonArray=new JSONArray(vals);
                int length=jsonArray.length();
                for (int i=0;i<length;i++){
                    List<List<String>> listList=new ArrayList<>();
                    JSONArray subJSONArray=jsonArray.getJSONArray(i);
                    for (int j=0;j<subJSONArray.length();j++){
                        JSONArray subSubJSONArray=subJSONArray.getJSONArray(j);
                        List<String> yVals=new ArrayList<>();
                        for (int k=0;k<subSubJSONArray.length();k++){
                            String y=subSubJSONArray.getString(k);
                            yVals.add(y);
                        }
                        listList.add(yVals);
                    }
                    chanMap.put(i,listList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return chanMap;
    }
}
