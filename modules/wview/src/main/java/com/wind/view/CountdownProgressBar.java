package com.wind.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wind on 2017/12/11.
 */

public class CountdownProgressBar extends View {
    public CountdownProgressBar(Context context) {
        super(context);
        init();
    }

    public CountdownProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountdownProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private long mTotolTime;
    private static final long  INTERVAL=100;
    private static final long DEFAULT_TOTAL_TIME=3000;
    private static final int CIRCLE_DEGREE=360;
    private int mFraction;
    private long mLeftTime;
    private int mProgress;
    private int mMax;
    private Paint mPaint;
    private void init(){
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
       // mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(DisplayUtil.dip2px(getContext(),3));
        mPaint.setStyle(Paint.Style.STROKE);

        mTotolTime=DEFAULT_TOTAL_TIME;
        mLeftTime=mTotolTime;
        mFraction= (int) (mTotolTime/INTERVAL);
        this.setMax(mFraction);
        this.setProgress(mFraction);
    }

    public void setTotalTime(long totalTime){
        if (mTotolTime%INTERVAL!=0){
            throw new IllegalArgumentException("The time value must be integer multiples of "+INTERVAL);
        }

        this.mTotolTime=totalTime;
        this.mLeftTime=this.mTotolTime;
        mFraction= (int) (mTotolTime/(float)INTERVAL);

        setMax(mFraction);
        setProgress(mFraction);
        recursive();
    }

    private void recursive(){

        postDelayed(new Runnable() {
            @Override
            public void run() {
                int progress= (int) (getProgress()-getMax()/(float)mFraction);
                setProgress(progress);
                mLeftTime=mLeftTime-INTERVAL;
               // LogUtil.e("ProgressBar","progress:"+getProgress()+"-mLeftTime:"+mLeftTime);
                if (mLeftTime>0){
                    recursive();
                }else {
                    if (getProgress()!=0){
                        setProgress(0);
                    }
                    if (mListener!=null){
                        mListener.onProgressFinish();
                    }
                }

            }
        },INTERVAL);
    }


    public void setProgress(int progress){
        this.mProgress=progress;
        invalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setMax(int max) {
        this.mMax = max;
    }

    public int getMax() {
        return mMax;
    }



    @Override
    protected void onDraw(Canvas canvas) {

        RectF rectF=new RectF(getPaddingLeft(),
                getPaddingTop(),
                getWidth()-getPaddingRight(),
                getHeight()-getPaddingBottom());


        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#99000000"));
        canvas.drawCircle(getWidth()/2,getHeight()/2,rectF.width()/2,mPaint);

        //计算sweepAngle
        float sweepAngle=getProgress()/(float)getMax()*CIRCLE_DEGREE*-1;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(DisplayUtil.dip2px(getContext(),2));
        canvas.drawArc(rectF,-90,sweepAngle,false,mPaint);
    }


    private OnProgressFinishListner mListener;
    public void setOnProgressFinishListner(OnProgressFinishListner listner){
        this.mListener=listner;
    }
    public interface OnProgressFinishListner{
        void onProgressFinish();
    }

}


