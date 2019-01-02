package com.wind.base.mvp.view;

import com.hannesdorfmann.mosby.mvp.MvpView;
import com.wind.base.request.UploadFileRequest;

/**
 * Created by wind on 2017/3/9.
 */

public interface UploadFileView extends MvpView{



    void uploadFile(UploadFileRequest request);
    void onUploadError(String msg);
    void onUploadFileProgress();
    void onUploadFileReturn();

}
