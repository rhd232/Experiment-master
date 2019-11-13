package com.jz.experiment.module.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.wind.base.BaseActivity;
import com.wind.base.C;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;
import com.wind.data.base.datastore.UserDataStore;
import com.wind.data.base.request.FindUserRequest;
import com.wind.data.base.request.UpdateUserRequest;
import com.wind.data.base.response.FindUserResponse;
import com.wind.data.base.response.UpdateUserResponse;
import com.wind.toastlib.ToastUtil;
import com.wind.view.ValidateEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class EditPwdActivity extends BaseActivity {
    @Override
    public int getStatusBarColor() {
        return Color.WHITE;
    }

    public static void start(Context context){
        Navigator.navigate(context,EditPwdActivity.class);
    }
    @BindView(R.id.change_pwd_view)
    LinearLayout change_pwd_view;
    @BindView(R.id.et_old_pwd)
    ValidateEditText et_old_pwd;
    @BindView(R.id.et_new_pwd)
    ValidateEditText et_new_pwd;
    @BindView(R.id.et_confirm_pwd)
    ValidateEditText et_confirm_pwd;
    @BindView(R.id.tv_msg)
    TextView tv_msg;
    @BindView(R.id.tv_edit)
    TextView tv_edit;

    UserDataStore mUserDataStore;

    @Override
    protected void setTitle() {
        String title=getActivity().getString(R.string.title_pwd);
        mTitleBar.setTitle(title);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_edit_pwd);
        ButterKnife.bind(this);

        mUserDataStore=UserDataStore
                .getInstance(ProviderModule.getInstance().getBriteDb(getApplicationContext()));
        autoScrollView(change_pwd_view,tv_msg);
    }

    private Subscription mFindSubscription;
    @OnClick(R.id.tv_edit)
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.tv_edit:
                if (validate()){
                    //检查旧密码是否正确

                    final FindUserRequest request=new FindUserRequest();
                    request.setUsername(C.Config.DEFAULT_USERNAME);
                    request.setPwd(et_old_pwd.getText().toString());
                    mFindSubscription=mUserDataStore.findUserByUsernameAndPwd(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<FindUserResponse>() {
                                @Override
                                public void call(FindUserResponse response) {
                                    mFindSubscription.unsubscribe();
                                    if (response.getErrCode()==BaseResponse.CODE_SUCCESS){
                                        //找到，继续修改密码
                                        updatePwd();
                                    }else {

                                        tv_msg.setText(getString(R.string.pwd_input_error));
                                    }

                                }
                            });



                }
                break;
        }
    }

    private Subscription mUpdateSubscription;
    private void updatePwd() {
        final UpdateUserRequest request=new UpdateUserRequest();
        request.setUsername(C.Config.DEFAULT_USERNAME);
        request.setPwd(et_new_pwd.getText().toString());
        mUpdateSubscription=mUserDataStore.updateUserByUsername(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UpdateUserResponse>() {
                    @Override
                    public void call(UpdateUserResponse response) {
                        mUpdateSubscription.unsubscribe();
                        if (response.getErrCode()==BaseResponse.CODE_SUCCESS){
                            ToastUtil.showToast(getActivity(),R.string.pwd_pwd_edit_succ);
                            //跳转
                            ActivityUtil.finish(getActivity());
                        }else {
                            ToastUtil.showToast(getActivity(),R.string.pwd_pwd_edit_error);
                        }
                    }
                });
    }

    private boolean validate() {
        if (!et_old_pwd.validate(true)){

            tv_msg.setText(getString(R.string.pwd_enter_old));
            return false;
        }
        if (!et_new_pwd.validate(true)){
            tv_msg.setText(getString(R.string.pwd_enter_new));
            return false;
        }
        if (!et_confirm_pwd.validate(true)){
            tv_msg.setText(getString(R.string.pwd_enter_confirm));
            return false;
        }
        String newPwd=et_new_pwd.getText().toString();
        boolean eq=newPwd.equals(et_confirm_pwd.getText().toString());
        if (!eq){

            tv_msg.setText(getString(R.string.pwd_not_equal_pwd));
            return false;
        }
        //检查新密码是否符合规则
        if (!isLetterDigit(newPwd)){
            tv_msg.setText( getString(R.string.pwd_not_pwd_rule));
            return false;
        }
        return true;
    }

    /**
     * 包含大小写字母及数字且在6-12位
     * 是否包含
     *
     * @param str
     * @return
     */
    public static boolean isLetterDigit(String str) {
        boolean isDigit = false;//定义一个boolean值，用来表示是否包含数字
        boolean isLetter = false;//定义一个boolean值，用来表示是否包含字母
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {   //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            } else if (Character.isLetter(str.charAt(i))) {  //用char包装类中的判断字母的方法判断每一个字符
                isLetter = true;
            }
        }
        String regex = "^[a-zA-Z0-9]{6,12}$";
        boolean isRight = isDigit && isLetter && str.matches(regex);
        return isRight;
    }

    /**
     * @param root 最外层的View
     * @param scrollToView 不想被遮挡的View,会移动到这个Veiw的可见位置
     */
    private int scrollToPosition=0;
    private void autoScrollView(final View root, final View scrollToView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();

                        //获取root在窗体的可视区域
                        root.getWindowVisibleDisplayFrame(rect);

                        //获取root在窗体的不可视区域高度(被遮挡的高度)
                        int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;

                        //若不可视区域高度大于150，则键盘显示
                        if (rootInvisibleHeight > 150) {

                            //获取scrollToView在窗体的坐标,location[0]为x坐标，location[1]为y坐标
                            int[] location = new int[2];
                            scrollToView.getLocationInWindow(location);

                            //计算root滚动高度，使scrollToView在可见区域的底部
                            int scrollHeight = (location[1] + scrollToView.getHeight()) - rect.bottom;

                            //注意，scrollHeight是一个相对移动距离，而scrollToPosition是一个绝对移动距离
                            scrollToPosition += scrollHeight;

                        } else {
                            //键盘隐藏
                            scrollToPosition = 0;
                        }
                        root.scrollTo(0, scrollToPosition);

                    }
                });
    }
}
