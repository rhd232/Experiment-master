package com.wind.toastlib;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by wind on 2018/1/12.
 */

public class ToastActivity extends FragmentActivity {

    private TextView mToastText;
    private View mToastView;
    private boolean mShow;
    private Handler mHandler=new Handler();
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToastView=getLayoutInflater().inflate(R.layout.layout_toast,null);
        mToastText=mToastView.findViewById(R.id.tv);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity= Gravity.CENTER;
        addContentView(mToastView,lp);
        mToastView.setVisibility(View.GONE);
    }


    public void showToast(String msg){
        if (mShow){
            return;
        }
        mShow=true;
        mToastText.setText(msg);
        mToastView.setAlpha(0f);
        mToastView.setVisibility(View.VISIBLE);
        mToastView.animate().alpha(1f).start();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideToast();
            }
        }, 2000);

    }

    private void hideToast() {
        if (ToastActivity.this==null || ToastActivity.this.isFinishing()){
            return;
        }
        mShow=false;
        mToastView.setVisibility(View.GONE);
    }


}
