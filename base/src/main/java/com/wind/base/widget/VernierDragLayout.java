package com.wind.base.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.wind.base.R;
import com.wind.base.bean.Stage;
import com.wind.base.dialog.AlertDialogUtil;
import com.wind.base.utils.KeyboardHelper;
import com.wind.base.utils.ViewHelper;
import com.wind.toastlib.ToastUtil;
import com.wind.view.DisplayUtil;

import java.text.DecimalFormat;

import rx.functions.Action1;

public class VernierDragLayout extends FrameLayout implements VernierView.OnViewPositionChangedListener {


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

    EditText et_time;
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
        et_time=findViewById(R.id.et_time);


        setEditTextAttr(et_time);
     //   setEditTextAttr(tv_temperature);

        RxTextView.textChanges(et_time).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                  if (mLink!=null){
                      short during=10;
                      String s=charSequence.toString();
                      if (TextUtils.isEmpty(s)){
                          mLink.setDuring(during);
                      }else {

                          try {
                              during=Short.parseShort(s);
                          }catch (Exception e){
                              e.printStackTrace();
                          }
                          mLink.setDuring(during);
                      }

                  }
            }
        });
        RxView.clicks(tv_temperature).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {

                showInputDialog(getContext(),null);

            }
        });
       /* RxTextView.textChanges(tv_temperature).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {


            }
        });*/


    }

    public void manualTemperature(int temperature){
        if (mLink!=null){
            System.out.println("temperature textChanges:"+mLink);
            float temp=50;
            String s=temperature+"";
            if (TextUtils.isEmpty(s)){
                mLink.setTemp(temp);
            }else {

                try {
                    temp=Float.parseFloat(s);
                }catch (Exception e){
                    e.printStackTrace();
                }
                mLink.setTemp(temp);
            }
            float percent=temp/(float)100;
            float curScale=(1-percent)* vernier_view.getDragHeight()+vernier_view.getSpaceTopHeight();
            //vernier_view.setScale(curScale);
            mLink.setCurScale(curScale);
            setCurScale(curScale);
            Stage next=mLink.getNext();
            if (next!=null){
                VernierDragLayout nextLayout=next.getLayout();
                if (nextLayout!=null)
                    nextLayout.setStartScale(curScale);
            }

        }
    }
    private void setEditTextAttr(final EditText et){
        et.setLongClickable(false);
        et.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardHelper.showKeyBoard(et);
            }
        });
        et.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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

        });
    }
    public void setTimeLayoutVisibility(int visibility){
        layout_time.setVisibility(visibility);
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
       // Log.i("DragCallback","left:"+mCurPos.x+"-top:"+mCurPos.y);
        //刻度0-100
        float percent=vernier_view.heightPercent(mCurPos.y);
       // System.out.println("percent:"+percent);
        float t=(1-percent)*100;
        String temp=decimalFormat.format(t);
        tv_temperature.setText(temp);
        mLink.setTemp(Float.parseFloat(temp));
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
        if (stage!=null) {
            et_time.setText(stage.getDuring() + "");
            tv_temperature.setText(stage.getTemp() + "");
        }
       /* if (mLink!=null)
            mLink.setStartScale(vernier_view.getStartScale());*/
    }


    public void resetStartScale() {
        vernier_view.resetStartScale();
    }

    public void resetCurScale() {
        vernier_view.resetCurScale();
    }


    public interface DialogConfirmCallback{

    }
    public void showInputDialog(Context context, final DialogConfirmCallback callback) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context,
                R.layout.dialog_input_temperature, true);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        final EditText et_temp=alertDialog.findViewById(R.id.et_temp);

        alertDialog.findViewById(R.id.tv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp=et_temp.getText().toString().trim();
                if (!TextUtils.isEmpty(temp)){
                   int temperature=Integer.parseInt(temp);
                   if (temperature<40 || temperature>100){
                       ToastUtil.showToast((Activity) getContext(),R.string.temperature_ranges);
                   }else {
                       //合法
                       alertDialog.dismiss();
                       manualTemperature(temperature);
                   }
                }
            }
        });

    }
}
