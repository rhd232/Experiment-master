package com.wind.base.di;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by wind on 16/5/18.
 */
public abstract class BaseDIFragment<C extends DaggerComponent> extends Fragment {
    private C mComponent;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependencies();
    }

    private void injectDependencies() {
        mComponent = ((HasComponent<C>) getActivity()).getComponent();
        inject();
    }

    protected abstract void inject();

    protected C getComponent() {
        return mComponent;
    }
}
