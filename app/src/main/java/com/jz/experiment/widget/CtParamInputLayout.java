package com.jz.experiment.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.jz.experiment.R;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CtParamInputLayout extends FrameLayout {


    public static int DEFALUT_MIN_CT=13;
    public static int DEFALUT_THRESHHOLD_CT=10;

    public CtParamInputLayout(@NonNull Context context) {
        this(context, null);
    }

    public CtParamInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CtParamInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    EditText et_ct_min, et_ct_threshold;

    private void init() {
        inflate(getContext(), R.layout.layout_ct_param_input, this);
        et_ct_min = findViewById(R.id.et_ct_min);
        et_ct_threshold = findViewById(R.id.et_ct_threshold);

        View btn_refresh = findViewById(R.id.btn_refresh);
        RxView.clicks(btn_refresh)
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mListener!=null){
                            mListener.onCtParamChanged(getCtParam());
                        }
                    }
                });
    }

    private OnCtParamChangeListener mListener;
    public void setOnCtParamChangeListener(OnCtParamChangeListener listener){
        this.mListener=listener;
    }
    public interface OnCtParamChangeListener{
        void onCtParamChanged(CtParam ctParam);
    }

    public CtParam getCtParam() {
        CtParam param = new CtParam();
        String ctMin = et_ct_min.getText().toString().trim();
        String ctThreshold = et_ct_threshold.getText().toString().trim();

        if (TextUtils.isEmpty(ctMin)) {
            param.ctMin = DEFALUT_MIN_CT;
            et_ct_min.setText(DEFALUT_MIN_CT+"");
        } else {
            param.ctMin = Integer.parseInt(ctMin);
        }
        if (TextUtils.isEmpty(ctThreshold)) {
            param.ctThreshhold = DEFALUT_THRESHHOLD_CT;
            et_ct_threshold.setText(DEFALUT_THRESHHOLD_CT+"");
        } else {
            param.ctThreshhold = Integer.parseInt(ctThreshold);
        }

        return param;
    }

    public void set(int ctMin,int ctThreshhold){
        et_ct_min.setText(ctMin+"");
        et_ct_threshold.setText(ctThreshhold+"");
    }
    public static class CtParam implements Parcelable {
        public int ctMin;
        public int ctThreshhold;
        public CtParam(){}
        protected CtParam(Parcel in) {
            ctMin = in.readInt();
            ctThreshhold = in.readInt();
        }

        public static final Creator<CtParam> CREATOR = new Creator<CtParam>() {
            @Override
            public CtParam createFromParcel(Parcel in) {
                return new CtParam(in);
            }

            @Override
            public CtParam[] newArray(int size) {
                return new CtParam[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ctMin);
            dest.writeInt(ctThreshhold);
        }
    }

}
