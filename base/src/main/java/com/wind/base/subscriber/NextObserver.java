package com.wind.base.subscriber;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.app.Fragment;
import android.view.View;

import com.wind.base.R;
import com.wind.base.mvp.view.ErrorMvpView;
import com.wind.base.mvp.view.MvpPageLoadingView;
import com.wind.base.mvp.view.PureMvpView;
import com.wind.base.response.BaseResponse;
import com.wind.base.response.PageResponse;

import rx.Observer;

/**
 * Created by wind on 2018/3/15.
 */

public abstract class NextObserver<V extends ErrorMvpView,RP extends BaseResponse> implements Observer<RP> {
    protected V mView;
    public NextObserver(V mvpView){
        this.mView=mvpView;
    }
    @Override
    public void onCompleted() {
    }

    @Override
    public void onNext(RP rp) {
        if (rp.getErrCode()!= BaseResponse.CODE_SUCCESS){
            mView.showError(rp.getErrCode(),rp.getErrMsg());
        }else {
            if (isPageResponse(rp)){
                MvpPageLoadingView pageView= (MvpPageLoadingView) mView;
                pageView.showPage(rp);
            }else
                onSuccess(rp);
        }
    }
    private boolean isPageResponse(RP rp){
        return rp instanceof PageResponse;
    }
    protected  void onSuccess(RP rp){
        if (mView instanceof PureMvpView){
            PureMvpView pureView= (PureMvpView) mView;
            pureView.onSuccess(rp);
        }
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        Activity activity=null;
        if (mView instanceof Fragment){
            activity=((Fragment)mView).getActivity();
        }else if (mView instanceof Activity){
            activity= (Activity) mView;
        }else if (mView instanceof View){
            Context context=((View) mView).getContext();
            if ( context instanceof Activity){
                activity= (Activity) context;
            }else if (context instanceof ContextWrapper){
                ContextWrapper wrapper= (ContextWrapper) context;
                activity= (Activity) wrapper.getBaseContext();
            }
        }

        if (activity!=null){
            mView.showError(BaseResponse.CODE_NETWORK_ERROR,activity.getString(R.string.network_error));
        }

    }


}
