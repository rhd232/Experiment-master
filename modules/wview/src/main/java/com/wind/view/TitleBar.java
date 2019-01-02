package com.wind.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by wind on 2018/1/26.
 */

public class TitleBar extends FrameLayout {

    public TitleBar(@NonNull Context context) {
        super(context);
        init();
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private TextView mCenterView;
    private TextView mRightView;
    private ImageView mLeftView;
    private View mLine;
    private View tile_bar_layout;
    private void init() {
        inflate(getContext(), R.layout.title_bar, this);
        mCenterView = findViewById(R.id.tv_title);
        mLeftView = findViewById(R.id.iv_left);
        mRightView = findViewById(R.id.tv_right);
        mLine = findViewById(R.id.line_titlebar);
        tile_bar_layout=findViewById(R.id.tile_bar_layout);

        mLeftView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context=getContext();
                if (context instanceof Activity){
                    Activity activity= (Activity) context;
                    activity.onBackPressed();
                }
            }
        });
    }

    public TitleBar setLeftIcon(int resId){
        mLeftView.setImageResource(resId);
        return this;
    }
    public TitleBar setTitle(String title) {
        mCenterView.setText(title);
        return this;
    }

    public TitleBar setTextColor(int color){
        mCenterView.setTextColor(color);
        mRightView.setTextColor(color);
        return this;
    }

    public TextView getRightView(){
        return mRightView;
    }
    public TitleBar setRightText(String text){
        mRightView.setText(text);
        return this;
    }
    public TitleBar setRightTextColor(int color){
        mRightView.setTextColor(color);
        return this;
    }
    public TitleBar setRightIcon(int  resId){
        mRightView.setCompoundDrawablesWithIntrinsicBounds(resId,0,0,0);
        return this;
    }
    public void setLeftVisibility(int visibility){
        mLeftView.setVisibility(visibility);
    }
    public void setLineVisibility(int visibility) {
        mLine.setVisibility(visibility);
    }
    public void setLineColor(int color){
        mLine.setBackgroundColor(color);
    }
    public void setRightVisibility(int visibility) {
        mRightView.setVisibility(visibility);
    }

    public void setBackgroundColor(int color){
        tile_bar_layout.setBackgroundColor(color);
    }
}
