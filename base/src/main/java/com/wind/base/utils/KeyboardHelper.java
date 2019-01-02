package com.wind.base.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by wind on 2017/3/10.
 */

public class KeyboardHelper {
    public static void showOrHideKeyBoard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
    public static void showKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);
    }

    public static void hideKeyBoard(Activity context) {
        if (context.getCurrentFocus()!=null) {
            ((InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),0 /*InputMethodManager.HIDE_NOT_ALWAYS*/);
        }
    }
    public static void hideKeyBoard(View currentFocusView) {
        if (currentFocusView!=null) {
            ((InputMethodManager) currentFocusView.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
        }
    }
    public static boolean isKeyBoardShowing(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();//isOpen若返回true，则表示输入法打开
    }
}
