package com.wind.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.wind.toastlib.ToastUtil;

/**
 * Created by wind on 2018/5/8.
 */

public class ValidateTextView extends TextView implements Validatable {


    public ValidateTextView(Context context) {
        super(context);
    }

    public ValidateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean validate(UnqualifiedCallback callback, boolean quiet) {
        String text = getText().toString();
        if (TextUtils.isEmpty(text)) {
            if (!quiet)
                callback.unqualified();
            return false;
        }
        return true;
    }
    @Override
    public boolean validate(boolean quiet) {
        if (quiet){
            String text=getText().toString();
            if (TextUtils.isEmpty(text)){
                return false;
            }
            return true;
        }else {
            return validate();
        }
    }
    @Override
    public boolean validate() {
        return validate("不能为空");
    }



    @Override
    public boolean validate(String errMsg) {
        return validate(errMsg,false);
    }


    @Override
    public boolean validate(final String errMsg, boolean quiet) {
        return validate(new UnqualifiedCallback() {
            @Override
            public void unqualified() {
                ToastUtil.showToast((Activity) getContext(), errMsg);
            }
        }, quiet);
    }
}
