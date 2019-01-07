package com.wind.base.widget;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wind.base.R;
import com.wind.base.bean.Stage;
import com.wind.base.utils.ViewHelper;
import com.wind.view.DisplayUtil;

import java.text.DecimalFormat;

public class VernierDragLayout extends FrameLayout implements VernierView.OnViewPositionChangedListener {

    private ViewDragHelper mViewDragHelper;
    public VernierDragLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public VernierDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VernierDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public boolean consumeDrag(){
        return vernier_view.tryCaptureView();
    }

    View layout_temperature;
    View layout_time;
    VernierView vernier_view;
    PointF mCurPos;

    //EditText et_time;
    TextView tv_temperature;
    DecimalFormat decimalFormat;
    private void init() {
       // mViewDragHelper=ViewDragHelper.create(this,new DragCallback());
        decimalFormat=new DecimalFormat("#");
        inflate(getContext(),R.layout.layout_vernier_drag,this);
        tv_temperature=findViewById(R.id.tv_temperature);

        layout_temperature=findViewById(R.id.layout_temperature);
        vernier_view=findViewById(R.id.vernier_view);
        layout_time=findViewById(R.id.layout_time);
        vernier_view.setOnViewPositionChangedListener(this);

      /*  et_time=findViewById(R.id.et_time);
        et_time.setLongClickable(false);
        et_time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardHelper.showKeyBoard(et_time);
            }
        });
        et_time.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }

        });*/

    }

    private void translateView() {
        float temperatureTranslateY=mCurPos.y-layout_temperature.getHeight()-DisplayUtil.dip2px(getContext(),4);
        ViewHelper.setTranslationX(layout_temperature,mCurPos.x);
        ViewHelper.setTranslationY(layout_temperature,temperatureTranslateY);


        float timeTranslateY=mCurPos.y+DisplayUtil.dip2px(getContext(),6);
        ViewHelper.setTranslationX(layout_time,mCurPos.x);
        ViewHelper.setTranslationY(layout_time,timeTranslateY);
       // 联动下一级VernierDrag
        if (mLink!=null){
            Stage next=mLink.getNext();
            if (next!=null){
                VernierDragLayout nextLayout=next.getLayout();
                if (nextLayout!=null)
                    nextLayout.setStartScale(mCurPos.y);
            }
        }
    }

    public void setStartScale(float startScale){
        vernier_view.setStartScale(startScale);
    }
    public void setCurScale(float scale){
        vernier_view.setScale(scale);
    }
    @Override
    public void onViewPositionChanged(PointF pos) {
        mCurPos=pos;
        Log.i("DragCallback","left:"+mCurPos.x+"-top:"+mCurPos.y);
        //刻度0-100
        float percent=vernier_view.heightPercent(mCurPos.y);
        String temp=decimalFormat.format((1-percent)*100);
        tv_temperature.setText(temp);
        if (mLink!=null)
            mLink.setCurScale(mCurPos.y);
        translateView();
    }

    @Override
    public void onStartScaleChanged(float startScale) {
        if (mLink!=null)
            mLink.setStartScale(startScale);
    }

    private Stage mLink;
    public void setLink(Stage stage) {
        mLink=stage;
       /* if (mLink!=null)
            mLink.setStartScale(vernier_view.getStartScale());*/
    }



}
