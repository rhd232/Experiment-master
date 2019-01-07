package com.aigestudio.wheelpicker.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.aigestudio.wheelpicker.widget.IWheelVo;
import com.aigestudio.wheelpicker.R;
import java.util.ArrayList;
import java.util.List;
/**
 * @author AigeStudio 2015-12-12
 */
public abstract class AbstractWheelPicker extends View implements IWheelPicker {
    private static final int TOUCH_DISTANCE_MINIMUM = 8;
    private static final int VELOCITY_TRACKER_UNITS = 150;

    protected VelocityTracker mTracker;
    protected WheelScroller mScroller;
    protected TextPaint mTextPaint;
    protected Paint mPaint;
    protected Rect mTextBound;              // 文本框
    protected Rect mDrawBound;
    protected Handler mHandler;
    protected OnWheelChangeListener mListener;
    protected AbstractWheelDecor mWheelDecor;

    protected List<IWheelVo> data = new ArrayList<>();          // 数据
    private IWheelVo selectedData;
    private int selectedIndex;

    protected String curData;

    protected int state = SCROLL_STATE_IDLE;
    protected int itemCount;
    protected int itemIndex;
    protected int itemSpace;
    protected int textSize;
    protected int textColor;
    protected int curTextColor;
    protected int maxTextWidth, maxTextHeight;
    protected int wheelContentWidth, wheelContentHeight;
    protected int wheelCenterX, wheelCenterY, wheelCenterTextY;

    protected int lastX, lastY;
    protected int diSingleMoveX, diSingleMoveY;
    protected int disTotalMoveX, disTotalMoveY;

    protected boolean ignorePadding;
    protected boolean hasSameSize;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SCROLLING = 2;

    public interface OnWheelChangeListener {
        /**
         * 滑动
         *
         * @param deltaX
         * @param deltaY
         */
        void onWheelScrolling(float deltaX, float deltaY);

        /**
         * 选中某项
         *
         * @param index
         * @param data
         */
        void onWheelSelected(int index, IWheelVo data);

        /**
         * 滚轮状态改变
         *
         * @param state
         */
        void onWheelScrollStateChanged(int state);
    }

    public static class SimpleWheelChangeListener implements OnWheelChangeListener {
        @Override
        public void onWheelScrolling(float deltaX, float deltaY) {

        }

        @Override
        public void onWheelSelected(int index, IWheelVo data) {

        }

        @Override
        public void onWheelScrollStateChanged(int state) {

        }
    }

    public AbstractWheelPicker(Context context) {
        super(context);
        init(null);
    }

    public AbstractWheelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        obtainAttrs(attrs);
        instantiation();
        assignment();
        computeWheelSizes();
    }

    /**
     * 初始化参数值
     *
     * @param attrs
     */
    protected void obtainAttrs(AttributeSet attrs) {
        int defItemIndex = 0;
        int defItemCount = 7;
        int defItemSpace = 60;

        int defTextSize = getResources().getDimensionPixelOffset(R.dimen.wheel_default_text_size);
        int defTextColor = 0xFF000000;

        int defCurTextColor = 0xFF000000;
        if (null != attrs) {
            TypedArray a = getContext()
                    .obtainStyledAttributes(attrs, R.styleable.AbstractWheelPicker);
            // 默认选中第几项
            itemIndex = a.getInt(R.styleable.AbstractWheelPicker_wheel_item_index, defItemIndex);
            // 一页显示多少条目
            itemCount = a.getInt(R.styleable.AbstractWheelPicker_wheel_item_count, defItemCount);
            // 一条数据所占到高度
            itemSpace = a.getDimensionPixelOffset(
                    R.styleable.AbstractWheelPicker_wheel_item_space, defItemSpace);
            // 文字大小
            textSize = a.getDimensionPixelOffset(
                    R.styleable.AbstractWheelPicker_wheel_text_size, defTextSize);
            // 文字颜色
            textColor = a.getColor(R.styleable.AbstractWheelPicker_wheel_text_color, defTextColor);

            // 高亮／选中项 文字颜色
            curTextColor = a.getColor(
                    R.styleable.AbstractWheelPicker_wheel_text_color_current, defCurTextColor);

            // 是否每项显示相同的大小 区域
            hasSameSize = a.getBoolean(R.styleable.AbstractWheelPicker_wheel_item_same_size, false);

            a.recycle();
        } else {
            itemIndex = defItemIndex;
            itemCount = defItemCount;
            itemSpace = defItemSpace;

            textSize = defTextSize;

            curTextColor = defCurTextColor;
        }
    }

    /**
     * 初始化工具
     */
    protected void instantiation() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(textSize);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mTextBound = new Rect();
        mDrawBound = new Rect();

        mHandler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mScroller = new OverScrollerCompat(getContext(), new DecelerateInterpolator());
        } else {
            mScroller = new ScrollerCompat(getContext(), new DecelerateInterpolator());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mScroller.setFriction(ViewConfiguration.getScrollFriction() / 25);
        }
    }

    protected void assignment() {
        curData = "";
    }

    protected void computeWheelSizes() {
        disTotalMoveX = 0;
        disTotalMoveY = 0;
        maxTextWidth = 0;
        maxTextHeight = 0;
        if (null != data && data.size() > 0) {
            if (hasSameSize) {
                String text = data.get(0).getLabel();
                mTextPaint.getTextBounds(text, 0, text.length(), mTextBound);
                maxTextWidth = Math.max(maxTextWidth, mTextBound.width());
                maxTextHeight = Math.max(maxTextHeight, mTextBound.height());
            } else {
                for (IWheelVo vo : data) {
                    String text = vo.getLabel();
                    mTextPaint.getTextBounds(text, 0, text.length(), mTextBound);
                    maxTextWidth = Math.max(maxTextWidth, mTextBound.width());
                    maxTextHeight = Math.max(maxTextHeight, mTextBound.height());
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int resultWidth = wheelContentWidth, resultHeight = wheelContentHeight;
        resultWidth += (getPaddingLeft() + getPaddingRight());
        resultHeight += (getPaddingTop() + getPaddingBottom());

        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth);
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight);

        setMeasuredDimension(resultWidth, resultHeight);
    }

    private int measureSize(int mode, int sizeExpect, int sizeActual) {
        int realSize;
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect;
        } else {
            realSize = sizeActual;
            if (mode == MeasureSpec.AT_MOST) {
                realSize = Math.min(realSize, sizeExpect);
            }
        }
        return realSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (null != data && data.size() > 0) {
            onWheelSelected(itemIndex, data.get(itemIndex));
        }
        mDrawBound.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(),
                h - getPaddingBottom());

        wheelCenterX = mDrawBound.centerX();
        wheelCenterY = mDrawBound.centerY();
        wheelCenterTextY = (int) (wheelCenterY - (mTextPaint.ascent() + mTextPaint.descent()) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);

        canvas.save();
        canvas.clipRect(mDrawBound);
        drawItems(canvas);
        canvas.restore();

        drawForeground(canvas);
    }

    protected abstract void drawBackground(Canvas canvas);

    protected abstract void drawItems(Canvas canvas);

    protected abstract void drawForeground(Canvas canvas);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null == mTracker) {
            mTracker = VelocityTracker.obtain();
        }
        mTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (!mScroller.isFinished()) mScroller.abortAnimation();
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                diSingleMoveX += (event.getX() - lastX);
                diSingleMoveY += (event.getY() - lastY);
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                onTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                disTotalMoveX += diSingleMoveX;
                disTotalMoveY += diSingleMoveY;
                diSingleMoveX = 0;
                diSingleMoveY = 0;
                mTracker.computeCurrentVelocity(VELOCITY_TRACKER_UNITS);
                onTouchUp(event);
                getParent().requestDisallowInterceptTouchEvent(false);
                mTracker.recycle();
                mTracker = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                mScroller.abortAnimation();
                mTracker.recycle();
                mTracker = null;
                break;
        }
        return true;
    }

    protected abstract void onTouchDown(MotionEvent event);

    protected abstract void onTouchMove(MotionEvent event);

    protected abstract void onTouchUp(MotionEvent event);

    protected boolean isEventValid() {
        return isEventValidVer() || isEventValidHor();
    }

    protected boolean isEventValidHor() {
        return Math.abs(diSingleMoveX) > TOUCH_DISTANCE_MINIMUM;
    }

    protected boolean isEventValidVer() {
        return Math.abs(diSingleMoveY) > TOUCH_DISTANCE_MINIMUM;
    }

    protected void onWheelScrolling(float deltaX, float deltaY) {
        if (null != mListener) {
            mListener.onWheelScrolling(deltaX, deltaY);
        }
    }

    /**
     * 选中值变化
     *
     * @param index
     * @param data
     */
    protected void onWheelSelected(int index, IWheelVo data) {
        this.selectedData = data;
        this.selectedIndex = index;
        if (null != mListener) {
            mListener.onWheelSelected(index, data);
        }
    }

    protected void onWheelScrollStateChanged(int state) {
        if (this.state != state) {
            this.state = state;
            if (null != mListener) mListener.onWheelScrollStateChanged(state);
        }
    }

    @Override
    public void setData(List<IWheelVo> data) {
        setData(data, 0);
    }

    @Override
    public void setData(List<IWheelVo> data, int index) {
        if (null == data || data.size() == 0) {
            index = 0;

            this.selectedData = null;
            this.selectedIndex = 0;
        } else {
            if (index < 0) {
                index = 0;
            } else if (index >= data.size()) {
                index = data.size() - 1;
            }

            this.selectedData = data.get(index);
            this.selectedIndex = index;
        }

        this.itemIndex = index;
        this.data = data;
        computeWheelSizes();
//错误，不应该调用这个方法        requestLayout();
        // 需要重新绘制
        invalidate();
    }

    @Override
    public void setOnWheelChangeListener(OnWheelChangeListener listener) {
        this.mListener = listener;
    }

    @Override
    public IWheelVo getSelectedData() {
        return selectedData;
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void setItemIndex(int index) {
        if (null == data || data.size() == 0) {
            index = 0;

            this.selectedData = null;
            this.selectedIndex = 0;
        } else {
            if (index < 0) {
                index = 0;
            } else if (index >= data.size()) {
                index = data.size() - 1;
            }

            this.selectedData = data.get(index);
            this.selectedIndex = index;
        }

        this.itemIndex = index;
        computeWheelSizes();
        invalidate();
    }

    @Override
    public void setItemSpace(int space) {
        itemSpace = space;
        computeWheelSizes();
        requestLayout();
    }

    @Override
    public void setItemCount(int count) {
        itemCount = count;
        computeWheelSizes();
        requestLayout();
    }

    @Override
    public void setTextColor(int color) {
        textColor = color;
        invalidate();
    }

    @Override
    public void setTextSize(int size) {
        textSize = size;
        mTextPaint.setTextSize(size);
        computeWheelSizes();
        requestLayout();
    }

    @Override
    public void setCurrentTextColor(int color) {
        curTextColor = color;
        // May be you don't need to invalidate all of view area.
    }

    @Override
    public void setWheelDecor(boolean ignorePadding, AbstractWheelDecor decor) {
        this.ignorePadding = ignorePadding;
        mWheelDecor = decor;
        // May be you don't need to invalidate all of view area.
    }
}