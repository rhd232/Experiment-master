package com.jz.experiment;

import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;

import com.jz.experiment.di.AppComponent;
import com.jz.experiment.di.DaggerAppComponent;
import com.jz.experiment.util.DataFileUtil;

import javax.inject.Inject;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class App extends MultiDexApplication implements HasSupportFragmentInjector {

    private static App sInstance;
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        CaocConfig.Builder.create()
                .errorActivity(DefaultErrorActivity.class)
                .apply();
        mAppComponent = createComponent();
        mAppComponent.inject(this);

        DataFileUtil.sDebug=true;

    }


    public static App get() {
        return sInstance;
    }

    private AppComponent createComponent() {
        return DaggerAppComponent
                .builder()
                //.appModule(new AppModule(this))
                //.providerModule(new ProviderModule())
                .build();
    }

    public AppComponent appComponent() {
        return mAppComponent;
    }

    @Inject
    DispatchingAndroidInjector<android.support.v4.app.Fragment> dispatchingAndroidInjector;


    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}
