package com.wind.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.wind.base.R;
import com.wind.base.utils.DisplayUtil;

public class ScaleView extends View {
    public ScaleView(Context context) {
        super(context);
        init();
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private Paint mPaint;
    private float spaceTopHeight;
    private void init(){
        mPaint=new Paint();
        mPaint.setColor(Color.parseColor("#333333"));
        //mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(DisplayUtil.sp2px(getContext(),14));
        spaceTopHeight=getResources().getDimensionPixelSize(R.dimen.space_height);
    }

    public float getDrawHeight() {
        return getHeight()-spaceTopHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float partHeight=getDrawHeight()/4f;
        //String texts[]={"100","75","50","25","0"};
        // 将坐标原点移到控件中心
        canvas.translate(getWidth() / 2, getDrawHeight()/ 2+spaceTopHeight);
        // x轴
        //canvas.drawLine(-getWidth() / 2, 0, getWidth() / 2, 0, mPaint);
        // y轴
        //canvas.drawLine(0, -getHeight() / 2, 0, getHeight() / 2, mPaint);

        // 文字宽
        float textWidth = mPaint.measureText("50");
        // 文字baseline在y轴方向的位置
        float baseLineY = Math.abs(mPaint.ascent() + mPaint.descent()) / 2;
        canvas.drawText("50", -textWidth / 2, baseLineY, mPaint);


        // 文字宽
        textWidth = mPaint.measureText("75");
        // 文字baseline在y轴方向的位置
        baseLineY = Math.abs(mPaint.ascent() + mPaint.descent()) / 2;
        canvas.drawText("75", -textWidth / 2, baseLineY-partHeight, mPaint);

        // 文字宽
        textWidth = mPaint.measureText("100");
        // 文字baseline在y轴方向的位置
        baseLineY = Math.abs(mPaint.ascent() + mPaint.descent()) / 2;
        canvas.drawText("100", -textWidth / 2, baseLineY-partHeight*2, mPaint);


        // 文字宽
        textWidth = mPaint.measureText("25");
        // 文字baseline在y轴方向的位置
        baseLineY = Math.abs(mPaint.ascent() + mPaint.descent()) / 2;
        canvas.drawText("25", -textWidth / 2, baseLineY+partHeight, mPaint);

        // 文字宽
        textWidth = mPaint.measureText("0");
        // 文字baseline在y轴方向的位置
        baseLineY = Math.abs(mPaint.ascent() + mPaint.descent()) / 2;
       canvas.drawText("0", -textWidth / 2, baseLineY+partHeight*2, mPaint);

    }
}
