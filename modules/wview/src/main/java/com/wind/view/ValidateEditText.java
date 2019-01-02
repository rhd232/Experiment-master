package com.wind.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import com.wind.toastlib.ToastUtil;

/**
 * Created by wind on 2018/5/8.
 */

public class ValidateEditText extends EditText implements Validatable {


    public ValidateEditText(Context context) {
        super(context);
    }

    public ValidateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidateEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean validate(UnqualifiedCallback callback,boolean quiet) {
        String text=getText().toString();
        if (TextUtils.isEmpty(text)){
            if (callback!=null&& !quiet)
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
        return validate(new UnqualifiedCallback() {
            @Override
            public void unqualified() {
                ToastUtil.showToast((Activity) getContext(),"不能为空");
            }
        },false);

    }


    @Override
    public boolean validate(final String errMsg) {
       return validate(errMsg,false);
    }

    public boolean validate(final String errMsg,boolean quiet){

        return validate(new UnqualifiedCallback() {
            @Override
            public void unqualified() {
                ToastUtil.showToast((Activity) getContext(),errMsg);
            }
        },quiet);
    }


}
