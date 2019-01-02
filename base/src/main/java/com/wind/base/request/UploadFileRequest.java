package com.wind.base.request;

import com.wind.base.bean.UploadFile;

import java.util.List;

/**
 * Created by wind on 2017/3/1.
 */

public class UploadFileRequest extends BaseRequest {

    private List<UploadFile> uploadFileList;


    public List<UploadFile> getUploadFileList() {
        return uploadFileList;
    }

    public void setUploadFileList(List<UploadFile> uploadFileList) {
        this.uploadFileList = uploadFileList;
    }
}
