package com.wind.base.mvp.presenter;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;
import com.wind.base.request.BaseRequest;
import com.wind.base.usecase.UsecaseManager;

/**
 * Created by wind on 16/9/11.
 */

public abstract class ExecutePresenter<V extends MvpView> extends MvpBasePresenter<V> {

    protected UsecaseManager manager;

    @Override
    public void attachView(V view) {
        super.attachView(view);
        manager=new UsecaseManager();
    }

    public void execute(BaseRequest request) {
        manager.execute(request);
    }


    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (!retainInstance) {
            cancelSubscription();
        }
    }

    private void cancelSubscription() {
        manager.cancelSubscription();
    }
}
