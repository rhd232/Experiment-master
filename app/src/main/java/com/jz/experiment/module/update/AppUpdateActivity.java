package com.jz.experiment.module.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jz.experiment.R;
import com.jz.experiment.SplashActivity;
import com.jz.experiment.module.login.LoginActivity;
import com.jz.experiment.util.CommonUtils;
import com.wind.base.utils.Navigator;

import static android.support.v4.content.FileProvider.getUriForFile;
import java.io.File;

public class AppUpdateActivity extends Activity {
    public static int version, serverVersion;
    public static String versionName, serverVersionName, downloadResult;
    public static receiveVersionHandler handler;
    private TextView textView;
    private Button btn;
    private Button mExitBtn;
    private ProgressBar proBar;
    private UpdateManager manager = UpdateManager.getInstance();
    private boolean updateFlag = false;
    private boolean apkDownloadApk = true;

    public static void start(Context context){
        Navigator.navigate(context,LoginActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
/*        if(!internetOk())
            finish();*/
        setFinishOnTouchOutside(false);
        textView = findViewById(R.id.textview_id);
        btn = findViewById(R.id.button_id);
        mExitBtn = findViewById(R.id.exit_download_btn);
        proBar = findViewById(R.id.progressBar_id);
        getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        Context c = this;
        version = manager.getVersion(c);
        versionName = manager.getVersionName(c);
        textView.setText(getResources().getString(R.string.version_code_current)+ version + "\n" + getResources().getString(R.string.version_name_current) + versionName);
        handler = new receiveVersionHandler();
        manager.compareVersion(AppUpdateActivity.this);
        bindClickListener(R.id.exit_download_btn);
//        installApp();
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {
            switch (paramAnonymousView.getId()) {
                default:
                    return;
                case R.id.exit_download_btn:
                    apkDownloadApk = false;
                    finish();
                    break;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            if (updateFlag)
                finish();
            else
                Toast.makeText(getBaseContext(), R.string.connecting_to_network, Toast.LENGTH_LONG).show();
            return true;
        }
        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    private void bindClickListener(int paramInt) {
        bindClickListener(findViewById(paramInt));
    }

    private void bindClickListener(View paramView) {
        paramView.setOnClickListener(this.mClickListener);
    }

    protected void onDestroy() {
        Intent localIntent = new Intent();
        localIntent.setClass(this, SplashActivity.class);
        this.startActivity(localIntent);
        super.onDestroy();
    }

    public class receiveVersionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mExitBtn.setEnabled(true);
            if (msg.arg1 == 101) {
                finish();
                return;
            }
            proBar.setProgress(msg.arg1);
            textView.setText(getResources().getString(R.string.download_progress) + " " +msg.arg1 + "%");
            if (msg.arg1 == 100 && apkDownloadApk) {
                updateFlag = true;
                installApp();
            }
        }
    }

    private void installApp()
    {
        File apkfile = new File(Environment.getExternalStorageDirectory() + "/app-anito-release.apk");
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            Uri apkUri = getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", apkfile); //与manifest中定义的provider中的authorities="com.xinchuang.buynow.fileprovider"保持一致
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String path = Environment.getExternalStorageDirectory()+"/app-anito-release.apk";
//					i.setDataAndType(Uri.fromFile(new File(path)),"application/vnd.android.package-archive");
            intent.setDataAndType(Uri.parse("file://"+apkfile.toString()), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    private boolean internetOk(){
        boolean internetOk = false;
        if (CommonUtils.isNetworkConnected(AppUpdateActivity.this))//wifi或有线已连接
        {
            if (!CommonUtils.ping())
                internetOk = false;
            else//网络正常
                internetOk = true;
        } else
            internetOk = false;
        return internetOk;
    }
}
