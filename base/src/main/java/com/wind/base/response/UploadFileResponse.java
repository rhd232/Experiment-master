package com.wind.base.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.wind.base.bean.UploadFile;

import java.util.List;

/**
 * Created by wind on 2017/3/1.
 */

public class UploadFileResponse extends BaseResponse{

    @JSONField(name = "items")
    private List<String> uploadedUrls;

    private UploadFile uploadFile;

    public UploadFile getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(UploadFile uploadFile) {
        this.uploadFile = uploadFile;
    }

    public List<String> getUploadedUrls() {
        return uploadedUrls;
    }

    public void setUploadedUrls(List<String> uploadedUrls) {
        this.uploadedUrls = uploadedUrls;
    }
}
