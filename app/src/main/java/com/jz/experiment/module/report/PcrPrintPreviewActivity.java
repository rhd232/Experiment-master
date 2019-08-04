package com.jz.experiment.module.report;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.anitoa.Anitoa;
import com.anitoa.service.UsbService;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.jz.experiment.R;
import com.jz.experiment.module.report.bean.InputParams;
import com.leon.lfilepickerlibrary.ui.Fat32LFilePickerActivity;
import com.wind.base.BaseActivity;
import com.wind.data.expe.bean.HistoryExperiment;

import java.util.List;

public class PcrPrintPreviewActivity extends BaseActivity {

    public static final String EXTRA_KEY_EXPE = "extra_key_expe";
    public static final String EXTRA_KEY_PARAMS = "extra_key_params";

    public static void start(Context context, HistoryExperiment experiment, InputParams params) {

        Intent intent = new Intent(context, PcrPrintPreviewActivity.class);
        intent.putExtra(EXTRA_KEY_EXPE, experiment);
        intent.putExtra(EXTRA_KEY_PARAMS, params);
        context.startActivity(intent);
    }

    PcrPrintPreviewFragment f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);
        HistoryExperiment experiment = getIntent().getParcelableExtra(EXTRA_KEY_EXPE);
        InputParams params = getIntent().getParcelableExtra(EXTRA_KEY_PARAMS);
        f = PcrPrintPreviewFragment.newInstance(experiment, params);
        replaceFragment(f);
    }

    int REQUESTCODE_SYS_STORAGE = 1000;
    int REQUESTCODE_USB_STORAGE = 1001;
    private  UsbMassStorageDevice[] devices;
    @Override
    protected void setTitle() {
        mTitleBar.setTitle(getString(R.string.print_preview));
        mTitleBar.setRightText(getString(R.string.print));
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                UsbService service=Anitoa.getInstance(getActivity()).getUsbService();
                UsbDevice storageDevice=service.getUsbStorageDevice();
                if (storageDevice!=null){
                    if (!service.requestPermissionIfNeed(storageDevice)){
                        //已经拥有全新
                        doPrint();
                    }
                }else {
                    //没有插入U盘
                    f.print("");
                }

            }
        });


    }


    private void doPrint(){
        devices = UsbMassStorageDevice.getMassStorageDevices(getActivity());
        String rootPath = "/storage/emulated/0";
        //TODO 检查usb storage是否有权限了
        if (devices == null || devices.length == 0) {
            f.print("");
        } else {
            UsbMassStorageDevice device = devices[0];
            try {
                device.init();
                Fat32LFilePickerActivity.startForResult(getActivity(),REQUESTCODE_USB_STORAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_SYS_STORAGE) {
                //If it is a file selection mode, you need to get the path collection of all the files selected
                //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                List<String> list = data.getStringArrayListExtra("paths");
                Toast.makeText(getApplicationContext(), "selected " + list.size(), Toast.LENGTH_SHORT).show();
                //If it is a folder selection mode, you need to get the folder path of your choice
                String path = data.getStringExtra("path");
               // Toast.makeText(getApplicationContext(), "The selected path is:" + path, Toast.LENGTH_SHORT).show();
            }else if( requestCode==REQUESTCODE_USB_STORAGE) {
                String path = data.getStringExtra("path");

                //Toast.makeText(getApplicationContext(), "The selected path is:" + path, Toast.LENGTH_SHORT).show();

                f.print(path);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (devices!=null){
            for (UsbMassStorageDevice device:devices){
                device.close();
            }
        }

    }
}
