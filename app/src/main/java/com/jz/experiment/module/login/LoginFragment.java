package com.jz.experiment.module.login;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anitoa.Anitoa;
import com.anitoa.bean.Data;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.listener.AnitoaConnectionListener;
import com.anitoa.service.CommunicationService;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.StatusChecker;
import com.jz.experiment.util.UsbManagerHelper;
import com.wind.base.C;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.AppUtil;
import com.wind.data.DbOpenHelper;
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


public class LoginFragment extends BaseFragment implements AnitoaConnectionListener {


    @BindView(R.id.iv_pwd_toggle)
    ImageView iv_pwd_toggle;

    @BindView(R.id.et_pwd)
    ValidateEditText et_pwd;
    @BindView(R.id.et_username)
    ValidateEditText et_username;

    @BindView(R.id.tv_msg)
    TextView tv_msg;
    @BindView(R.id.tv_app_version)
    TextView tv_app_version;

    @BindView(R.id.tv_lower_computer_host_version)
    TextView tv_lower_computer_host_version;

    @BindView(R.id.tv_lower_computer_img_version)
    TextView tv_lower_computer_img_version;

    @BindView(R.id.tv_lower_computer_temp_version)
    TextView tv_lower_computer_temp_version;

    UserDataStore mUserDataStore;
    Subscription findSubscription;
    private Anitoa sAnitoa;
    private Handler mHandler = new Handler();
    CommunicationService mCommunicationService;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_login;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        //读取第一个用户，自动填充到文本框
        mUserDataStore = new UserDataStore(
                ProviderModule
                        .getInstance()
                        .provideBriteDb(DbOpenHelper.getInstance(getActivity().getApplicationContext())));


        final FindUserRequest request = new FindUserRequest();
        request.setUsername(C.Config.DEFAULT_USERNAME);
        request.setPwd(C.Config.DEFAULT_PWD);
        findSubscription = mUserDataStore.findUserByUsername(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FindUserResponse>() {
                    @Override
                    public void call(FindUserResponse response) {
                        findSubscription.unsubscribe();
                        if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                            if (response.getUser() != null) {
                                String username = response.getUser().username();
                                String password = response.getUser().password();
                                et_username.setText(username);
                                et_pwd.setText(password);

                            }
                        }
                    }
                });
        //绑定service
        sAnitoa = Anitoa
                .getInstance(getActivity());


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ActivityUtil.isFinish(getActivity())) {
                    mCommunicationService = sAnitoa.getCommunicationService();

                    if (mCommunicationService != null) {
                        setNofity(LoginFragment.this);
                        UsbManagerHelper.connectUsbDevice(getActivity());
                    }
                }

            }
        }, 500);

        String appVersion = AppUtil.getAppVersionName(getActivity());
        tv_app_version.setText("App：" + appVersion);
        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CommunicationService service=sAnitoa.getCommunicationService();
                if (service!=null){
                    service.setNotify(LoginFragment.this);
                    service.sendPcrCommand(PcrCommand.ofVersionCmd());
                }
            }
        },1500);*/

    }

    private void doPdfPrint(String filePath) {

        PrintManager printManager = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);
        PdfPrintAdapter myPrintAdapter = new PdfPrintAdapter(filePath);
        printManager.print("print_pdf", myPrintAdapter, null);

    }

    Subscription loginSubscription;

    @OnClick({R.id.iv_pwd_toggle, R.id.tv_login})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
               // ReportPreviewActivity.start(getActivity());
                login();
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

    private boolean mNeedStopService = true;

    private void login() {
        if (validate()) {
            setNofity(null);
            LoadingDialogHelper.showOpLoading(getActivity());
            String username = et_username.getText().toString().trim();
            String pwd = et_pwd.getText().toString().trim();
            final FindUserRequest request = new FindUserRequest();
            request.setUsername(username);
            request.setPwd(pwd);
            loginSubscription = UserDataStore
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
                            if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                                //登录成功
                                MainActivity.start(getActivity());
                                mNeedStopService = false;
                                ActivityUtil.finish(getActivity());
                            } else {
                                String tips = getString(R.string.login_error_tips);
                                tv_msg.setText(tips);
                            }
                        }
                    });
        }
    }

    private boolean validate() {
        if (!et_username.validate()) {
            String nameTips = getString(R.string.login_input_name);
            tv_msg.setText(nameTips);
            return false;
        }
        if (!et_pwd.validate()) {
            String pwdTips = getString(R.string.login_input_pwd);
            tv_msg.setText(pwdTips);
            return false;
        }
        return true;
    }


    public void testServer() {
        String filePath = C.Value.IMAGE_DATA + "2019_01_29_03_22_23_source.txt";
        File file = new File(filePath);
        List<String> data = DataFileUtil.covertToList(file);
        Map<String, List<String>> dataMap = new HashMap<>();
        dataMap.put("data", data);
        JSONObject jsonObject = new JSONObject(dataMap);
        String url = "http://114.215.195.137:55500/data";
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
                String ret = response.body().string();
                System.out.println("返回数据：" + ret);
            }
        });
    }

    public void testJson() {
        String json = "[[\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                "[40.54448441, 81.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                "]\n" +
                ",[\n" +
                "[30.54448441, 31.0617958, -16.75278455, 0.0, 0.0,0.0],\n" +
                "[20.54448441, 61.0617958, -16.75278455, 0.0, 0.0,0.0]\n" +
                "]\n" +
                "]";
        Map<Integer, List<List<Double>>> chanMap = new LinkedHashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                List<List<Double>> listList = new ArrayList<>();
                JSONArray subJSONArray = jsonArray.getJSONArray(i);
                for (int j = 0; j < subJSONArray.length(); j++) {
                    JSONArray subSubJSONArray = subJSONArray.getJSONArray(j);
                    List<Double> yVals = new ArrayList<>();
                    for (int k = 0; k < subSubJSONArray.length(); k++) {
                        double y = subSubJSONArray.getDouble(k);
                        yVals.add(y);
                    }
                    listList.add(yVals);
                }
                chanMap.put(i, listList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int size = chanMap.size();
        for (int i = 0; i < size; i++) {
            List<List<Double>> listList = chanMap.get(i);
            for (int j = 0; j < listList.size(); j++) {
                List<Double> vals = listList.get(j);
                for (int k = 0; k < vals.size(); k++) {
                    System.out.print(vals.get(k) + " ");
                }
                System.out.println();
            }


        }
    }


    private void setNofity(AnitoaConnectionListener listener) {
        if (mCommunicationService != null) {
            mCommunicationService.setNotify(listener);
        }
    }

    @Override
    public void onConnectSuccess() {
        if (sAnitoa != null) {
            if (mCommunicationService != null) {
                mCommunicationService.sendPcrCommand(PcrCommand.ofVersionCmd());
            }
          /*  CommunicationService service = sAnitoa.getCommunicationService();
            if (service != null)
                service.sendPcrCommand(PcrCommand.ofVersionCmd());*/
        }
    }

    @Override
    public void onConnectCancel() {

    }

    @Override
    public void onDoThing() {

    }

    @Override
    public void onReceivedData(Data data) {
        byte[] buffer = data.getBuffer();

        int statusIndex = 1;
        int status = buffer[statusIndex];
        int dataIndex = 5;
        //TODO 检查返回的包是否正确
        boolean succ = StatusChecker.checkStatus(status);
        if (succ) {


            //host型号
            int model = buffer[dataIndex++];
            int majorVersion = buffer[dataIndex++];
            int minorVersion = buffer[dataIndex++];
            dataIndex++;
            dataIndex++;
            dataIndex++;
            //IMG型号
            int imgModel = buffer[dataIndex++];
            int imgMajorVersion = buffer[dataIndex++];
            int imgMinorVersion = buffer[dataIndex++];
            dataIndex++;
            dataIndex++;
            dataIndex++;
            //TEMP型号
            int tempModel = buffer[dataIndex++];
            int tempMajorVersion = buffer[dataIndex++];
            int tempMinorVersion = buffer[dataIndex++];
            //host1.35-temp1.29-img1.42
            tv_lower_computer_host_version.setText("HOST：" + majorVersion + "." + minorVersion);
            tv_lower_computer_img_version.setText("TEMP：" + imgMajorVersion + "." + imgMinorVersion);
            tv_lower_computer_temp_version.setText("IMG：" + tempMajorVersion + "." + tempMinorVersion);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setNofity(null);
        if (mNeedStopService) {
            Anitoa.getInstance(getActivity().getApplicationContext()).unbindService(getActivity().getApplicationContext());
        }
        System.out.println("LoginFragment onDestroyView mNeedStopService"+mNeedStopService);
    }


}
