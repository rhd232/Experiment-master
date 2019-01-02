package com.wind.base.mvp.view;

import com.hannesdorfmann.mosby.mvp.MvpView;
import com.wind.base.response.BaseResponse;

/**
 * Created by wind on 2017/3/7.
 */

public interface MvpPageLoadingView<T extends BaseResponse> extends MvpView{

    void showPageLoadingIndicator();

    void hidePageLoadingIndicator();

    void showEmptyView(boolean isShow);
    void showErrorView(boolean isShow);


    void showPage(T reponse);
}
