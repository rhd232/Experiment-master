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
import com.jz.experiment.util.DataFileUtil;
import com.wind.base.C;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.data.base.datastore.UserDataStore;
import com.wind.data.base.request.FindUserRequest;
import com.wind.data.base.response.FindUserResponse;
import com.wind.view.ValidateEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
                //testServer();

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


    public void testServer(){
        String filePath= C.Value.IMAGE_DATA+"2019_01_29_03_22_23_source.txt";
        File file=new File(filePath);
        List<String> data = DataFileUtil.covertToList(file);
        Map<String, List<String>> dataMap = new HashMap<>();
        dataMap.put("data", data);
        JSONObject jsonObject = new JSONObject(dataMap);
        String url="http://114.215.195.137:55500/data";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , jsonObject.toString());
        Request request = new Request.Builder()
                .url(url)//请求的url
                .post(requestBody)
                .build();
        okhttp3.Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ret=response.body().string();
                System.out.println("返回数据："+ret);
            }
        });
    }

    public void testJson(){
           String json="[[\n" +
                        "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                        "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                        "]\n" +
                        ",[\n" +
                        "[30.54448441, 31.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                        "[20.54448441, 61.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                        "]\n" +
                        "]";
                Map<Integer,List<List<Double>>> chanMap=new LinkedHashMap<>();
                try {
                    JSONArray jsonArray=new JSONArray(json);
                    int length=jsonArray.length();
                    for (int i=0;i<length;i++){
                        List<List<Double>> listList=new ArrayList<>();
                        JSONArray subJSONArray=jsonArray.getJSONArray(i);
                        for (int j=0;j<subJSONArray.length();j++){
                            JSONArray subSubJSONArray=subJSONArray.getJSONArray(j);
                            List<Double> yVals=new ArrayList<>();
                            for (int k=0;k<subSubJSONArray.length();k++){
                                double y=subSubJSONArray.getDouble(k);
                                yVals.add(y);
                            }
                            listList.add(yVals);
                        }
                        chanMap.put(i,listList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int size=chanMap.size();
                for (int i=0;i<size;i++){
                    List<List<Double>> listList=chanMap.get(i);
                    for (int j=0;j<listList.size();j++){
                        List<Double> vals=listList.get(j);
                        for (int k=0;k<vals.size();k++){
                            System.out.print(vals.get(k)+" ");
                        }
                        System.out.println();
                    }


                }
    }
}
