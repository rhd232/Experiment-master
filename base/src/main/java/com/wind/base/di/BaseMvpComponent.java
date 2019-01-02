package com.wind.base.di;


import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;

/**
 * Created by wind on 16/5/18.
 */
public interface BaseMvpComponent<V extends MvpView,P extends MvpPresenter<V>> extends DaggerComponent {

    P presenter();
}
