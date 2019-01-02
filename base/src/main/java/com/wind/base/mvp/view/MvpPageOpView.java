package com.wind.base.mvp.view;

/**
 * Created by wind on 2017/3/7.
 * 一个页面的操作
 */

public interface MvpPageOpView extends ErrorMvpView{
    /**
     * 显示操作等待loading
     */
    void showOpLoadingIndicator();

    void hideOpLoadingIndicator();

}
