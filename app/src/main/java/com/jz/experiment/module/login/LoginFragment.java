package com.jz.experiment.module.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.data.base.datastore.UserDataStore;
import com.wind.data.base.request.FindUserRequest;
import com.wind.data.base.response.FindUserResponse;
import com.wind.view.ValidateEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class LoginFragment extends BaseFragment {



    @BindView(R.id.iv_pwd_toggle)
    ImageView iv_pwd_toggle;

    @BindView(R.id.et_pwd)
    ValidateEditText et_pwd;
    @BindView(R.id.et_username)
    ValidateEditText et_username;

    @BindView(R.id.tv_msg)
    TextView tv_msg;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_login;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
    }

    Subscription loginSubscription;
    @OnClick({R.id.iv_pwd_toggle, R.id.tv_login})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                if (validate()) {
                    LoadingDialogHelper.showOpLoading(getActivity());
                    String username = et_username.getText().toString().trim();
                    String pwd = et_pwd.getText().toString().trim();
                    final FindUserRequest request = new FindUserRequest();
                    request.setUsername(username);
                    request.setPwd(pwd);
                    loginSubscription=UserDataStore
                            .getInstance(ProviderModule.getInstance()
                                    .getBriteDb(getActivity().getApplicationContext()))
                            .findUserByUsernameAndPwd(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<FindUserResponse>() {
                                @Override
                                public void call(FindUserResponse response) {
                                    loginSubscription.unsubscribe();
                                    LoadingDialogHelper.hideOpLoading();
                                    if (response.getErrCode()==BaseResponse.CODE_SUCCESS){
                                        //登录成功
                                        MainActivity.start(getActivity());
                                        ActivityUtil.finish(getActivity());
                                    }else {
                                        tv_msg.setText("用户不存在或密码错误");
                                    }
                                }
                            });
                }
                break;
            case R.id.iv_pwd_toggle:
                if (iv_pwd_toggle.isActivated()) {
                    //设置为密文
                    et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_pwd_toggle.setActivated(false);
                } else {
                    //设置为明文
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_pwd_toggle.setActivated(true);
                }
                break;
        }
    }

    private boolean validate() {
        if (!et_username.validate()) {
            tv_msg.setText("输入用户名");
            return false;
        }
        if (!et_pwd.validate()) {
            tv_msg.setText("输入密码");
            return false;
        }
        return true;
    }

}
