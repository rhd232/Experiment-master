package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DuringView extends TextView {
    public DuringView(Context context) {
        super(context);
    }

    public DuringView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DuringView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Subscription mSubscription;
    public void start() {
        setText("00:00:00");
        mSubscription=Observable
                .interval(1000, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        // String time = DateUtil.get(aLong * 1000, "HH:mm:ss");

                        String hh = new DecimalFormat("00").format(aLong / 3600);
                        String mm = new DecimalFormat("00").format(aLong % 3600 / 60);
                        String ss = new DecimalFormat("00").format(aLong % 60);
                        String timeFormat = new String(hh + ":" + mm + ":" + ss);
                        setText(timeFormat);
                    }
                });
    }

    public void stop() {
        if (mSubscription!=null&&  !mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }
}
