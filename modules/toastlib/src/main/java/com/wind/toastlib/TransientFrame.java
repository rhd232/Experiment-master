package com.wind.toastlib;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by wind on 2018/1/19.
 */

public class TransientFrame {
   public static final String TAG=TransientFrame.class.getSimpleName();

    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT =-1;
    public static final int LENGTH_LONG = 0;

    static final int ANIMATION_DURATION = 250;
    private ViewGroup mParent;
    private AbsTransientLayout mLayout;
    private int mDuration;
    private Handler mHandler=new Handler(Looper.getMainLooper());
    private TransientFrame(ViewGroup parent, AbsTransientLayout layout) {
        this.mLayout = layout;
        this.mParent = parent;
    }


    /**
     * create a toastlayout
     * @param view
     * @param text
     * @param duration
     * @return
     */
    public static TransientFrame make(View view, String text, int duration) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }
        final TransientFrame frame = new TransientFrame(parent,
                new ToastTransientLayout(parent.getContext(), text));
        frame.setDuration(duration);
        return frame;
    }

    /**
     *
     */
    private TransientFrameManager.Callback mCallback=new TransientFrameManager.Callback() {
        @Override
        public void show() {
            if (mLayout.getParent()==null){
                //还未添加进viewtree
                mParent.addView(mLayout);
            }

            if (ViewCompat.isLaidOut(mLayout)){
                if (shouldAnimate()){
                    animateContentIn();
                }else {
                    onViewShown();
                }
            }else {
                mLayout.setOnLayoutChangeListener(new AbsTransientLayout.OnLayoutChangeListener() {

                    @Override
                    public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                        mLayout.setOnLayoutChangeListener(null);
                        if (shouldAnimate()){
                            animateContentIn();
                        }else {
                            onViewShown();
                        }
                    }
                });
            }

        }

        @Override
        public void dismiss() {

            if (shouldAnimate() && mLayout.getVisibility() == View.VISIBLE) {
                animateContentOut();
            } else {
                // If anims are disabled or the view isn't visible, just call back now
                onViewHidden();
            }
            final ViewParent parent = mLayout.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mLayout);
            }
        }
    };



    private void animateContentOut() {
        mLayout.animateContentIn(0,ANIMATION_DURATION);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onViewHidden();
            }
        },ANIMATION_DURATION);
    }

    private void animateContentIn() {
        mLayout.animateContentIn(0,ANIMATION_DURATION);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onViewShown();
            }
        },ANIMATION_DURATION);
    }

    private void onViewShown() {
        Log.e(TAG,"onViewShown");
        TransientFrameManager.getInstance().onShown(mCallback);
    }
    private void onViewHidden() {
        Log.e(TAG,"onViewHidden");
        TransientFrameManager.getInstance().onDismiss(mCallback);
    }
    private boolean shouldAnimate() {
        return true;
    }

    /**
     * 供外部调用
     */
    public  void show(){
        TransientFrameManager.getInstance().show(mDuration,mCallback);
    }
    private void setDuration(int duration) {
        mDuration=duration;
    }

    private static ViewGroup findSuitableParent(View view) {
        while (view!=null){
            if (view instanceof FrameLayout){
                if (view.getId()== R.id.toast_container){
                    return (ViewGroup) view;
                }
                if (view.getId()==android.R.id.content){
                    return (ViewGroup) view;
                }

            }
            ViewParent parent=view.getParent();
            if ( parent instanceof View){
                view= (View) parent;
            }else {
                view=null;
            }
        }

        return null;
    }
}
