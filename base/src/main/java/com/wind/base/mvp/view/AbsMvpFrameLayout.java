package com.wind.base.mvp.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;
import com.hannesdorfmann.mosby.mvp.layout.MvpFrameLayout;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.toastlib.ToastUtil;


/**
 * Created by wind on 2018/5/18.
 */

public abstract class AbsMvpFrameLayout<V extends MvpView, P extends MvpPresenter<V>> extends MvpFrameLayout<V,P> {
    public AbsMvpFrameLayout(Context context) {
        super(context);
        init();
    }

    public AbsMvpFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbsMvpFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected abstract void init();


    public void showError(String msg) {
        if (getContext() instanceof Activity)
            ToastUtil.showToast((Activity) getContext(),msg);
        hideOpLoadingIndicator();
    }

    public void showOpLoadingIndicator() {
        LoadingDialogHelper.showOpLoading(getContext());
    }

    public void hideOpLoadingIndicator() {
        LoadingDialogHelper.hideOpLoading();
    }

}
