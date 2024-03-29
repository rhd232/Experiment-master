package com.wind.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wind.base.R;
import com.wind.view.DisplayUtil;

public class VernierView extends View {

    private final int LINE_OFFSET = DisplayUtil.dip2px(getContext(), 200);

    public VernierView(Context context) {
        super(context);
        init();
    }

    public VernierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VernierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint mPaint;

    private boolean initialized;
    private float spaceTopHeight;
    public float getSpaceTopHeight(){
        return spaceTopHeight;
    }
    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(DisplayUtil.dip2px(getContext(), 4));
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(Color.parseColor("#F2BC00"));

        spaceTopHeight = getResources().getDimensionPixelSize(R.dimen.space_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /*System.out.println("onMeasure:" + getMeasuredHeight());
        if (startScale == -1) {
            float startScale = getDragHeight() / 2f + spaceTopHeight;

            System.out.println("onMeasure->startScale:" + startScale);
            System.out.println("onMeasure->heightPercent:" + heightPercent(startScale));
            setStartScale(startScale);
            setScale(startScale);
        }*/
    }

    private boolean needRecaculateScale;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    //    System.out.println("onSizeChanged->h:" +h+"-w:"+w+"  needRecaculateScale:"+needRecaculateScale+" oldh:"+oldh);
        if (startScale == -1 || needRecaculateScale) {
            needRecaculateScale=true;
            startScale = (h - spaceTopHeight) / 2f + spaceTopHeight;
           // System.out.println("onSizeChanged->startScale:" + startScale);
            //System.out.println("onSizeChanged->heightPercent:" + heightPercent(startScale));
            setStartScale(startScale);
            setScale(startScale);
        }else {
            needRecaculateScale=false;
        }
    }

    public float heightPercent(float y) {
        float percent = (y - spaceTopHeight) / getDragHeight();
        return percent;
    }

    public float getDragHeight() {
        return getMeasuredHeight() - spaceTopHeight;//上方留出空白
    }

    RectF mBoldLineRectF;
    PointF mPos = new PointF();

    @Override
    protected void onDraw(Canvas canvas) {
        //  float height =getDragHeight();
        int width = getMeasuredWidth();
        System.out.println("onDraw->height:" + getMeasuredHeight());
        float lineWidth = width / 2f;

        mPaint.setStrokeWidth(DisplayUtil.dip2px(getContext(), 1));
        canvas.drawLine(0, startScale, lineWidth, mScale, mPaint);


        mPaint.setStrokeWidth(DisplayUtil.dip2px(getContext(), 4));
        canvas.drawLine(lineWidth, mScale, width, mScale, mPaint);

        mPos.x = lineWidth;
        mPos.y = mScale;
        float top = mScale - LINE_OFFSET;
        // top=top<spaceTopHeight?spaceTopHeight:top;
        float bottom = mScale + LINE_OFFSET;
        //  bottom=bottom>getHeight()-spaceHeight?getHeight()-spaceHeight:bottom;
        mBoldLineRectF = new RectF(lineWidth, top, width, bottom);
        if (listener != null)
            listener.onViewPositionChanged(mPos);
    }

    private float startScale;

    public void setStartScale(float startScale) {
        System.out.println("setStartScale:" + startScale);
        if (startScale == -1 && getMeasuredHeight() != 0) {
            startScale = getDragHeight() / 2f + spaceTopHeight;
            setStartScale(startScale);
            return;
        }

        this.startScale = startScale;
        invalidate();
        if (listener != null) {
            listener.onStartScaleChanged(startScale);
        }
    }

    public float getStartScale() {
        return startScale;
    }

    private float mScale;

    public void setScale(float scale) {
        if (scale == -1 && getMeasuredHeight() != 0) {
            scale = getDragHeight() / 2f + spaceTopHeight;
            setScale(scale);
            return;
        }
        this.mScale = scale;
        invalidate();
    }

    boolean canDrag = false;


    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return touchDragArea(event);
        //return touchDragBar(event);
    }


    public boolean tryCaptureView() {
        return canDrag;
    }

    public PointF getCurrentPosition() {
        return mPos;
    }

    private OnViewPositionChangedListener listener;

    public void setOnViewPositionChangedListener(OnViewPositionChangedListener listener) {
        this.listener = listener;
    }

    public void resetStartScale() {
        float startScale = getDragHeight() / 2f + spaceTopHeight;
        setStartScale(startScale);
    }

    public void resetCurScale() {
        float curScale = getDragHeight() / 2f + spaceTopHeight;
        setScale(curScale);
    }

    public interface OnViewPositionChangedListener {
        void onViewPositionChanged(PointF pos);

        void onStartScaleChanged(float startScale);
    }

    float downY;

    private boolean touchDragArea(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                //判断手指是否落在粗线范围内，若在可以拖动，否则不能拖动
                if (mBoldLineRectF.contains(x, y)) {
                    canDrag = true;
                    // getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    canDrag = false;
                    //  getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (canDrag) {
                    //  getParent().requestDisallowInterceptTouchEvent(true);
                    /*if (y>getHeight()){
                        y=getHeight();
                    }
                    if (y<spaceTopHeight){
                        y=spaceTopHeight;
                    }*/
                    float deltaY = y - downY;
                    //System.out.println("deltaY:"+deltaY);
                    float temp = mScale + deltaY;

                    if (heightPercent(temp) > 0.6f) {
                        temp = 0.6f * getDragHeight() + spaceTopHeight;
                    }

                   /* if (temp>getHeight()){
                        temp=getHeight();
                    }*/
                    if (temp < spaceTopHeight) {
                        temp = spaceTopHeight;
                    }
                    setScale(temp);
                    downY = y;
                } else {
                    // getParent().requestDisallowInterceptTouchEvent(false);
                }
                downY = y;
                break;

            case MotionEvent.ACTION_UP:
                canDrag = false;
                // getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return canDrag;
    }

    private boolean touchDragBar(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断手指是否落在粗线范围内，若在可以拖动，否则不能拖动
                if (mBoldLineRectF.contains(x, y)) {
                    canDrag = true;
                } else {
                    canDrag = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (canDrag) {
                    if (y > getHeight()) {
                        y = getHeight();
                    }
                    if (y < spaceTopHeight) {
                        y = spaceTopHeight;
                    }
                    setScale(y);
                }
                break;

            case MotionEvent.ACTION_UP:
                canDrag = false;
                break;
        }
        return true;
    }
}
