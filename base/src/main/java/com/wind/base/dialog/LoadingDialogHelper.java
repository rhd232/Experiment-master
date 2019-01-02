package com.wind.base.dialog;

import android.content.Context;


/**
 * Created by wind on 2017/3/8.
 */

public class LoadingDialogHelper {

    private static OpLoadingDialog mDialog;
    public static void showOpLoading(Context context) {
        if (mDialog!=null&&mDialog.isShowing()){
            return;
        }
        mDialog=new OpLoadingDialog(context);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void showOpLoadingCanCancel(Context context) {
        if (mDialog!=null&&mDialog.isShowing()){
            return;
        }
        mDialog=new OpLoadingDialog(context);
        mDialog.setCancelable(true);
        mDialog.show();
    }

    public static void hideOpLoading(){
        if(mDialog!=null&&mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

}
