package com.wind.base.mvp.view;

import com.hannesdorfmann.mosby.mvp.MvpView;

/**
 * Created by wind on 2017/3/16.
 */

public interface ErrorMvpView extends MvpView {

    /**
     *
     * @param err 错误码
     * @param msg 错误码对应的错误消息
     */
    void showError(int err,String msg);
}
