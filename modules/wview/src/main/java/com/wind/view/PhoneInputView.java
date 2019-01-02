package com.wind.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.wind.toastlib.ToastUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wind on 2017/12/7.
 */

public class PhoneInputView extends FrameLayout {
    public PhoneInputView(@NonNull Context context) {
        super(context);
        init();
    }

    public PhoneInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhoneInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    EditText et_phone;
    View iv_phone_delete;

    private void init(){
        inflate(getContext(), R.layout.wd_layout_phone_input,this);
        et_phone=findViewById(R.id.et_phone);
        iv_phone_delete=findViewById(R.id.iv_phone_delete);
        iv_phone_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                et_phone.setText("");
            }
        });
        et_phone.addTextChangedListener(new EditChangedListener());

        et_phone.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    //校验是否是手机号
                    if (!isMobileNumber(et_phone.getText().toString())) {
                       // ToastUtil.showToast(getContext(),R.string.mobile_not_correct);
                      /*  if (mLinkageView!=null) {
                            mLinkageView.setEnabled(false);
                            mLinkageView.setClickable(false);
                        }*/
                    }else {
                     /*   if (mLinkageView!=null) {
                            mLinkageView.setEnabled(true);
                            mLinkageView.setClickable(true);
                        }*/
                    }
                }
            }
        });
    }

    /**
     * 验证手机号正则表达式
     */
    private static String MOBILE_PATTERN = "^((13[0-9])|(14[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$";

    /**
     * 验证是否是手机号
     *
     * @param mobiles
     * @return
     */
    public boolean isMobileNumber(String mobiles) {
        Pattern p = Pattern.compile(MOBILE_PATTERN);
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }
    private View mLinkageView;
    public void setLinkageView(View view){
        this.mLinkageView=view;
    }

    public CharSequence getText() {
        return et_phone.getText();
    }

    /**
     * 电话号码输入监听事件
     */
    class EditChangedListener implements TextWatcher {
        private CharSequence temp;// 监听前的文本
        private final int charMaxNum = 11;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before,
                                  int count) {
            if (count==1) {
                int length = charSequence.length();
                if (length == 3 || length == 8) {
                    et_phone.setText(charSequence.toString() + " ");
                    et_phone.setSelection(et_phone.getText().toString().length());
                }

                if (length==4){
                    //检查是否有空格
                    String phone=et_phone.getText().toString();
                    if (!phone.contains(" ")){
                        String newString=phone.substring(0,3)+" "+phone.substring(3,4);
                        et_phone.setText(newString);
                        try {
                            et_phone.setSelection(et_phone.getText().length());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                if (length==9){
                    String phone=et_phone.getText().toString();
                    //检查是否有空格
                    int spaceLastIndex=et_phone.getText().toString().lastIndexOf(" ");
                    if (spaceLastIndex!=8){
                        String newString=phone.substring(0,8)+" "+phone.substring(8,9);
                        et_phone.setText(newString);
                        try {
                            et_phone.setSelection(et_phone.getText().length());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
          /*  if (gettingVerificationCode) {
                return;
            }*/
            String trimStr=temp.toString().replace(" ","");
            if (trimStr.length() >= charMaxNum) {
                if (isMobileNumber(trimStr.toString())) {
                    /*if (mLinkageView!=null) {
                        mLinkageView.setClickable(true);
                        mLinkageView.setEnabled(true);
                    }*/
                } else {
                   /* if (mLinkageView!=null) {
                        mLinkageView.setEnabled(false);
                        mLinkageView.setClickable(false);
                    }*/
                   if (mAttchedFragment!=null){
                       ToastUtil.showToast(mAttchedFragment, R.string.mobile_not_correct);
                   }else {
                       ToastUtil.showToast((Activity) getContext(), R.string.mobile_not_correct);
                   }
                }

            } else {
              /*  if (mLinkageView!=null) {
                    mLinkageView.setClickable(false);
                    mLinkageView.setEnabled(false);
                }*/
            }




            if (s.toString().length()>0){
                iv_phone_delete.setVisibility(View.VISIBLE);
            }else {
                iv_phone_delete.setVisibility(View.GONE);
            }

            if (mOnInputListener!=null){
                mOnInputListener.onInputFinish(s.toString());
            }
        }
    }

    private Fragment mAttchedFragment;
    public void seAttchedFragment(Fragment attchedFragment){
        this.mAttchedFragment=attchedFragment;
    }


    private OnInputListener mOnInputListener;
    public void setOnInputListener(OnInputListener listener){
        this.mOnInputListener=listener;
    }
    public interface OnInputListener{
        void onInputFinish(String text);
    }
}
