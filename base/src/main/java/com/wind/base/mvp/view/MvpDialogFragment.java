package com.wind.base.mvp.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;
import com.hannesdorfmann.mosby.mvp.delegate.BaseMvpDelegateCallback;
import com.hannesdorfmann.mosby.mvp.delegate.FragmentMvpDelegate;
import com.hannesdorfmann.mosby.mvp.delegate.FragmentMvpDelegateImpl;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.toastlib.ToastUtil;

/**
 * Created by wind on 2018/3/14.
 */

public abstract class MvpDialogFragment<V extends MvpView,P extends MvpPresenter<V>>
        extends DialogFragment implements BaseMvpDelegateCallback<V,P>,MvpView{
    protected FragmentMvpDelegate<V, P> mvpDelegate;
    protected P presenter;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMvpDelegate().onCreate(savedInstanceState);
    }



    @NonNull
    protected FragmentMvpDelegate<V, P> getMvpDelegate() {
        if (mvpDelegate == null) {
            mvpDelegate = new FragmentMvpDelegateImpl<>(this);
        }

        return mvpDelegate;
    }


    @Override
    public P getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter=presenter;
    }

    @Override
    public V getMvpView() {
        return (V)this;
    }

    @Override
    public boolean isRetainInstance() {
        return  getRetainInstance();
    }

    @Override
    public boolean shouldInstanceBeRetained() {
        FragmentActivity activity = getActivity();
        boolean changingConfig = activity != null && activity.isChangingConfigurations();
        return getRetainInstance() && changingConfig;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMvpDelegate().onViewCreated(view, savedInstanceState);
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        getMvpDelegate().onDestroyView();
    }



    @Override public void onDestroy() {
        super.onDestroy();
        getMvpDelegate().onDestroy();
    }

    @Override public void onPause() {
        super.onPause();
        getMvpDelegate().onPause();
    }

    @Override public void onResume() {
        super.onResume();
        getMvpDelegate().onResume();
    }

    @Override public void onStart() {
        super.onStart();
        getMvpDelegate().onStart();
    }

    @Override public void onStop() {
        super.onStop();
        getMvpDelegate().onStop();
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMvpDelegate().onActivityCreated(savedInstanceState);
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        getMvpDelegate().onAttach(activity);
    }

    @Override public void onDetach() {
        super.onDetach();
        getMvpDelegate().onDetach();
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getMvpDelegate().onSaveInstanceState(outState);
    }


    public void showError(String msg) {
        ToastUtil.showToast(this,msg);
        hideOpLoadingIndicator();
    }

    public void showOpLoadingIndicator() {
        LoadingDialogHelper.showOpLoading(getActivity());
    }

    public void hideOpLoadingIndicator() {
        LoadingDialogHelper.hideOpLoading();
    }
}
