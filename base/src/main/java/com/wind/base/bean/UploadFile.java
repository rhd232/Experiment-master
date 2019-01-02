package com.wind.base.bean;

/**
 * Created by wind on 2017/3/1.
 */

public class UploadFile {



    private String path;
    /**
     * 上传成功后返回的url
     */
    private String uploadedUrl;

    public UploadFile(){}

    public UploadFile(String path){
        this.path=path;
    }


    public String getUploadedUrl() {
        return uploadedUrl;
    }

    public void setUploadedUrl(String uploadedUrl) {
        this.uploadedUrl = uploadedUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public static final int STATE_NORMAL=0;
    public static final int STATE_UPLOADING=3;
    public static final int STATE_UPLOAD_SUCCESS=1;
    public static final int STATE_UPLOAD_ERROR=2;


    /**
     * 上传进度
     */
    private int progress;

    /**
     * 0  正常
     * 1，上传成功
     * 2，上传失败
     * 3，上传中
     */
    private int uploadState;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }
}
