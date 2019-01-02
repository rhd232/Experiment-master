package com.wind.base.mvp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;
import com.wind.base.di.BaseMvpComponent;
import com.wind.base.di.HasComponent;

import icepick.Icepick;

/**
 * Created by wind on 16/9/12.
 */

public abstract class DaggerMvpDialogFragment<V extends MvpView, P extends MvpPresenter<V>, C extends BaseMvpComponent<V, P>>
        extends MvpDialogFragment<V,P>
        {


    private C mComponent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependencies();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }


    private void injectDependencies() {
        Fragment parentFragment=getParentFragment();
        if (parentFragment==null){
            if (getActivity() instanceof HasComponent) {
                mComponent = ((HasComponent<C>) getActivity()).getComponent();
            } else {
                mComponent = createComponent();
            }
        }else {
            mComponent = createComponent();
        }

        if (mComponent == null) {
            throw new NullPointerException("Component can not be null! you should override createComponent method");
        }
        inject();
    }

    protected abstract void inject();

    /**
     * 如果activity不是HasComponent类型，那么fragment需要自己createComponent();
     *
     * @return
     */
    protected C createComponent() {
        return null;
    }

    public C getComponent() {
        return mComponent;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
