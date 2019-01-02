package com.wind.base.api;

import android.util.Base64;
import android.util.Log;

import com.wind.base.C;
import com.wind.base.bean.UploadFile;
import com.wind.base.response.UploadFileResponse;
import com.wind.base.utils.AppUtil;
import com.wind.base.utils.JsonParser;
import com.wind.base.utils.LogUtil;
import com.wind.base.utils.Md5Util;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by wind on 2017/3/1.
 */

public class UploadApi {

    public Observable<UploadFileResponse> upload(final UploadFile uploadFile) {

        //获取mac地址
        String macAddress = AppUtil.getMacAddress(null);
        // String initMac=new String(macAddress);
        Log.e("macAddress", macAddress);
        //判断mac地址位数
        int macLength = macAddress.length();
        int initMacLength = macLength;
        StringBuilder sbMac = new StringBuilder(macAddress);
        while (macLength < 20) {
            sbMac.append("0");
            macLength = sbMac.length();
        }
        Date day = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today =sdf.format(day);
        //当前日期
        ////////计算mb////////////////
        String mb = "";
        try {
            String beforeBase64 = macAddress + "," + today;
            mb = Base64.encodeToString(beforeBase64.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ////////计算cb////////////////
        String beforMd5 = initMacLength + sbMac.toString() + today;
        //进行md5加密
        final String md5 = Md5Util.md5Hex(beforMd5);
        final String cb = md5.substring(7, 24);//取8-24位字符串
        final String finalMb=mb;
        final File file=new File(uploadFile.getPath());
        return Observable.create(new Observable.OnSubscribe<UploadFileResponse>() {
            @Override
            public void call(final Subscriber<? super UploadFileResponse> subscriber) {
                try {
                    OkHttpUtils.post()
                            .url(C.Api.IMAGE_SERVER_URL)
                            .addFile("img_", file.getName(), file)
                            .addHeader("channel","richpeople")
                            .addParams("_mb", finalMb)
                            .addParams("_cb", cb)
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    uploadFile.setUploadState(UploadFile.STATE_UPLOAD_ERROR);
                                    subscriber.onError(e);
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    try {
                                        UploadFileResponse resp = JsonParser.parserObject(response, UploadFileResponse.class);
                                        uploadFile.setUploadState(UploadFile.STATE_UPLOAD_SUCCESS);
                                        uploadFile.setUploadedUrl(resp.getUploadedUrls().get(0));
                                        LogUtil.e(uploadFile.getUploadedUrl());
                                        resp.setUploadFile(uploadFile);
                                        subscriber.onNext(resp);
                                        subscriber.onCompleted();
                                    } catch (Exception e) {

                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void inProgress(float progress, long total, int id) {
                                    uploadFile.setUploadState(UploadFile.STATE_UPLOADING);
                                    UploadFileResponse resp=new UploadFileResponse();
                                    resp.setUploadFile(uploadFile);
                                    //subscriber.onNext(resp);
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });


    }
}
