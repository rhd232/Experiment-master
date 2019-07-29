package com.jz.experiment.module.report;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.jz.experiment.R;
import com.jz.experiment.module.report.bean.InputParams;
import com.wind.base.BaseActivity;
import com.wind.data.expe.bean.HistoryExperiment;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
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

    int REQUESTCODE_FROM_ACTIVITY = 1000;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle(getString(R.string.print_preview));
        mTitleBar.setRightText(getString(R.string.print));
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 f.print();
               /* UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(getActivity());
                String rootPath = "/storage/emulated/0";
                for (UsbMassStorageDevice device : devices) {
                    try {
                        // before interacting with a device you need to call init()!
                        device.init();
                        // Only uses the first partition on the device
                        FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
                        UsbFile usbFile = currentFs.getRootDirectory();
                        ToastUtil.showToast(getActivity(), "usbFile==null?" + (usbFile == null));
                        rootPath = "/mnt/udisk";
                        ToastUtil.showToast(getActivity(),
                                "UsbMassStorageDevice rootPath:" + rootPath);
                    } catch (Exception e) {
                        e.printStackTrace();

                        ToastUtil.showToast(getActivity(), "Exception");
                    }

                }
                new LFilePicker()
                        .withActivity(getActivity())
                        .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                        .withStartPath(rootPath)
                        .withChooseMode(false)
                        .start();*/

              /*  String path=getStoragePath(getActivity(),true);
                ToastUtil.showToast(getActivity(),"路径："+path);*/
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                //If it is a file selection mode, you need to get the path collection of all the files selected
                //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                List<String> list = data.getStringArrayListExtra("paths");
                Toast.makeText(getApplicationContext(), "selected " + list.size(), Toast.LENGTH_SHORT).show();
                //If it is a folder selection mode, you need to get the folder path of your choice
                String path = data.getStringExtra("path");
                Toast.makeText(getApplicationContext(), "The selected path is:" + path, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private static String getStoragePath(Context mContext, boolean is_removale) {
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


    public class StorageInfo{
        public String path;
        public String state;
        public boolean isRemoveable;
        public StorageInfo(String path){
            this.path=path;
        }
        public boolean isMounted(){
            return"mounted".equals(state);
        }
    }

   /* public  List<StorageInfo> listAvaliableStorage(Context context) {
        ArrayList<StorageInfo> storagges = new ArrayList<StorageInfo>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            getVolumeList.setAccessible(true);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
            if (invokes != null) {
                ToastUtil.showToast(getActivity(),"invokes.length:"+invokes.length);
                StorageInfo info = null;
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath", new Class[0]);
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    info = new StorageInfo(path);
                    File file = new File(info.path);
                    if ((file.exists()) && (file.isDirectory()) && (file.canWrite())) {
                        Method isRemovable = obj.getClass().getMethod("isRemovable", new Class[0]);
                        String state = null;
                        try {
                            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                            state = (String) getVolumeState.invoke(storageManager, info.path);
                            info.state = state;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        if (info.isMounted()) {
                            info.isRemoveable = ((Boolean) isRemovable.invoke(obj, new Object[0])).booleanValue();
                            storagges.add(info);
                        }
                    }
                }
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        storagges.trimToSize();


        return storagges;


    }*/

}
