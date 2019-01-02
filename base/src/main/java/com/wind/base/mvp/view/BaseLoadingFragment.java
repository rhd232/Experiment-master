package com.wind.base.mvp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.wind.base.R;
import com.wind.base.di.BaseMvpComponent;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.presenter.ExecutePresenter;
import com.wind.base.request.BaseRequest;
import com.wind.base.response.PageResponse;

/**
 * Created by wind on 2018/3/16.
 */

public abstract class BaseLoadingFragment<V extends MvpPageLoadingView,P extends ExecutePresenter<V>,C extends BaseMvpComponent<V,P>,T extends PageResponse>
        extends DaggerMvpFragment<V,P,C>
        implements
        MvpPageLoadingView<T> {


    protected ezy.ui.layout.LoadingLayout mLayoutManager;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLayoutManager = view.findViewById(R.id.layout_loading);
        mLayoutManager.setEmptyImage(R.drawable.empty_icon);
        mLayoutManager.setEmptyText("空空如也~");

        mLayoutManager.setRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutManager.showLoading();
                load();
            }
        });

        mLayoutManager.showLoading();
        load();
    }

    private void load(){
        presenter.execute(buildLoadingRequest());
    }
    @Override
    public void showEmptyView(boolean isShow) {
        mLayoutManager.showEmpty();
    }

    @Override
    public void showErrorView(boolean isShow) {
        mLayoutManager.showError();
    }
    /**********接口{@link com.wind.base.mvp.view.MvpPageOpView } function start*************************/
    public void showOpLoadingIndicator() {
        LoadingDialogHelper.showOpLoading(getActivity());
    }

    public void hideOpLoadingIndicator() {
        LoadingDialogHelper.hideOpLoading();
    }
    /**********接口{@link com.wind.base.mvp.view.MvpPageOpView } function end*************************/
    @Override
    public void showPageLoadingIndicator() {

    }

    @Override
    public void hidePageLoadingIndicator() {

    }


    @Override
    public P createPresenter() {
        return getComponent().presenter();
    }
    protected abstract BaseRequest buildLoadingRequest();
}
