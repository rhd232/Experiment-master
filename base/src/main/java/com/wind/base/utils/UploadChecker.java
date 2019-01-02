package com.wind.base.utils;

import android.text.TextUtils;

import com.wind.base.bean.UploadFile;

import java.util.List;

/**
 * Created by wind on 2017/3/9.
 */

public class UploadChecker {

    /**
     * 检查是否已经全部上传
     * @param datas
     * @return
     */
    public static boolean isAllUploaded(List<UploadFile> datas) {
        for (UploadFile statefulPhotoModel : datas) {
            if (TextUtils.isEmpty(statefulPhotoModel.getPath())){
                continue;
            }
            if (statefulPhotoModel.getUploadState() != UploadFile.STATE_UPLOAD_SUCCESS) {
                return false;
            }
        }
        return true;
    }

    /**
     * 传输是否结束
     *
     * @return
     */
    public static boolean isUploadFinish(List<UploadFile> datas) {
        for (UploadFile statefulPhotoModel : datas) {
            if (statefulPhotoModel.getUploadState() == UploadFile.STATE_UPLOADING || statefulPhotoModel.getUploadState() == UploadFile.STATE_NORMAL) {
                return false;
            }
        }
        return true;
    }
}
