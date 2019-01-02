package com.wind.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import static java.lang.System.currentTimeMillis;

/**
 * Created by wind on 16/2/28.
 */
public class DragRecycleView extends RecyclerView implements GestureDetector.OnGestureListener{

    private GestureDetector mGestureDetector;
    private boolean mAnimating;
    public DragRecycleView(Context context) {
        super(context);
        init(context);
    }

    public DragRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private int mFlingSlop,mTouchSlop;
    private void init(Context context) {

        //mGestureDetector=new GestureDetector(context,this);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mFlingSlop = viewConfiguration.getScaledMinimumFlingVelocity();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    float curX,curY,x,y;
    private int mActivePointerId = INVALID_POINTER_ID;
    public static final int INVALID_POINTER_ID = -1;
    private long mDownTime;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTopCard==null){
            return false;
        }
        //return mGestureDetector.onTouchEvent(event);
        final int pointerIndex;
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mDownTime= currentTimeMillis();
                pointerIndex = event.getActionIndex();
                x=event.getX();
                y=event.getY();
                curX=x;
                curY=y;
                mActivePointerId = event.getPointerId(pointerIndex);
                return mOnDragListener.onDown();
            case MotionEvent.ACTION_MOVE:
              //  pointerIndex = event.findPointerIndex(mActivePointerId);
                x=event.getX();
                y=event.getY();
                float dx=x-curX;
                float dy=y-curY;

                if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                }

                if(!mDragging) {
                    return true;
                }
                mTopCard.setTranslationX(mTopCard.getTranslationX()+dx);
                mTopCard.setTranslationY(mTopCard.getTranslationY()+dy);

                mOnDragListener.onDrag(mTopCard.getTranslationX(),mTopCard.getTranslationY());

                curX=x;
                curY=y;


                break;
            case MotionEvent.ACTION_UP:
                /*if (!mDragging) {
                    return true;
                }*/
                long upTime=System.currentTimeMillis();
                if (upTime-mDownTime<100&&Math.abs(mTopCard.getTranslationX())<100){
                    performResetAnimatior();
                    //click事件
                    mOnDragListener.onItemClick(mTopCard);
                    return true;
                }

                //当translationX超过一定值时触发remove topview,没有的话则回到最初位置
                boolean pass=mOnDragListener.onUp(mTopCard.getTranslationX(),mTopCard.getTranslationY());
                if (Math.abs(mTopCard.getTranslationX())>300 && pass){
                    //执行topview退出动画
                    performExitAnimation();
                }else {
                    performResetAnimatior();
                }
                break;
        }


        return true;
    }
    private void performResetAnimatior() {
        //topview回到原先位置
        ObjectAnimator translationXAnimator=ObjectAnimator
                .ofFloat(mTopCard,"translationX",mTopCard.getTranslationX(),0);
        translationXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value= (Float) animation.getAnimatedValue();
                mTopCard.setTranslationX(value);
                mOnDragListener.onDrag(value,mTopCard.getTranslationY());
            }
        });
        ObjectAnimator translationYAnimator=ObjectAnimator
                .ofFloat(mTopCard,"translationY",mTopCard.getTranslationY(),0);
        translationYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value= (Float) animation.getAnimatedValue();
                mTopCard.setTranslationY(value);
                mOnDragListener.onDrag(mTopCard.getTranslationX(),value);
            }
        });
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(translationXAnimator,translationYAnimator);
        animatorSet.setDuration(300);
        animatorSet.setTarget(mTopCard);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimating=false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating=true;
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
        animatorSet.start();
    }

    private void performExitAnimation() {
        final boolean left=mTopCard.getTranslationX()<0;
        Log.e("Dect","left:"+left);
        float finalX=mTopCard.getTranslationX()*5;
        float finalY=mTopCard.getTranslationY()*5;
        doAnimation(left,finalX,finalY);

    }

    private void doAnimation(final boolean left,float finalX,float finalY){
        mAnimating=true;
        mTopCard.animate()
                .setDuration(300)
                .setInterpolator(new LinearInterpolator())
                .translationX(finalX)
                .translationY(finalY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        mOnDragListener.dragRemove(left);
                        mAnimating=false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }
                });
    }
    //外部按钮操作
    public void onPerformLeftExitAnimation(){
        LayoutManager layoutManager=getLayoutManager();
        if (layoutManager instanceof CardLayoutManager){
            CardLayoutManager cardLayoutManager=(CardLayoutManager)layoutManager;
            if (cardLayoutManager.isAnimating()){
                return;
            }
        }
        if (mAnimating){
            return;
        }
        float finalX=-1000;
        float finalY=1000;
        doAnimation(true,finalX,finalY);
    }
    //外部按钮操作
    public void onPerformRightExitAnimation(){
        if (mAnimating){
            return;
        }
        float finalX=1000;
        float finalY=1000;
        doAnimation(false,finalX,finalY);
    }


    private OnDragListener mOnDragListener;
    public void setOnDragListener(OnDragListener onDragListener){
        this.mOnDragListener=onDragListener;
    }
    public interface OnDragListener{
        void onDrag(float translationX, float translationY);
        void onItemClick(View view);
        void dragRemove(boolean left);

        boolean onDown();

        boolean onUp(float translationX, float translationY);
    }
    private View mTopCard;
    boolean mDragging;





    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void setTopView(View top) {
        mTopCard=top;

        //mTopCard.animate().setListener(null);
    }
}
