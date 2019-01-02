package com.wind.base.mvp.view;

import com.wind.base.response.BaseResponse;

/**
 * Created by wind on 2018/6/3.
 */

public interface PureMvpView<RP extends BaseResponse> extends ErrorMvpView {
    void onSuccess(RP rp);
}
