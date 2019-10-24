package com.leon.lfilepickerlibrary.ui;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.leon.lfilepickerlibrary.R;
import com.leon.lfilepickerlibrary.adapter.Fat32PathAdapter;
import com.leon.lfilepickerlibrary.utils.StringUtils;
import com.leon.lfilepickerlibrary.utils.USBBroadCastReceiver;
import com.leon.lfilepickerlibrary.utils.USBMassStorageHelper;
import com.leon.lfilepickerlibrary.widget.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fat32LFilePickerActivity extends FragmentActivity {

    private final String TAG = "FilePickerLeon";
    private EmptyRecyclerView mRecylerView;
    private View mEmptyView;
    private TextView mTvPath;
    private TextView mTvBack;
    private Button mBtnAddBook;
    private String mPath;
    private List<UsbFile> mListFiles;
    private ArrayList<String> mListNumbers = new ArrayList<String>();//存放选中条目的数据地址
    private Fat32PathAdapter mPathAdapter;
    private boolean mIsAllSelected = false;
    private Menu mMenu;
    UsbFile usbRootFile;
    USBMassStorageHelper mUSBMassStorageHelper;

    public static void startForResult(Activity context,int requestCode){
        Intent intent=new Intent(context,Fat32LFilePickerActivity.class);
        context.startActivityForResult(intent,requestCode);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // mParamEntity = (ParamEntity) getIntent().getExtras().getSerializable("param");
        //setTheme(mParamEntity.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lfile_picker);
        initView();
        mUSBMassStorageHelper=new USBMassStorageHelper(this);

        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this);
        for(UsbMassStorageDevice device: devices) {
            try {
                // before interacting with a device you need to call init()!
                device.init();
                // Only uses the first partition on the device
                FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
                usbRootFile = currentFs.getRootDirectory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        updateAddButton();
        if (!checkSDState()) {
            Toast.makeText(this, R.string.lfile_NotFoundPath, Toast.LENGTH_SHORT).show();
            return;
        }
        mPath ="";
        if (StringUtils.isEmpty(mPath)) {
            //如果没有指定路径，则使用默认路径
            mPath = "/";
        }
        mTvPath.setText(mPath);
       // mFilter = new LFileFilter(mParamEntity.getFileTypes());
        mListFiles =mUSBMassStorageHelper.getUsbFolderFileList(usbRootFile);

        mPathAdapter = new Fat32PathAdapter(mListFiles,mUSBMassStorageHelper, this);
        mRecylerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
      //  mPathAdapter.setmIconStyle(mParamEntity.getIconStyle());
        mRecylerView.setAdapter(mPathAdapter);
        mRecylerView.setmEmptyView(mEmptyView);
        initListener();
    }

    private USBBroadCastReceiver.UsbListener mUsbListener=new USBBroadCastReceiver.UsbListener() {
        @Override
        public void insertUsb(UsbDevice device_add) {

        }

        @Override
        public void removeUsb(UsbDevice device_remove) {

        }

        @Override
        public void getReadUsbPermission(UsbDevice usbDevice) {

        }

        @Override
        public void failedReadUsb(UsbDevice usbDevice) {

        }
    };


    private void updateAddButton() {
        mBtnAddBook.setVisibility(View.GONE);
        mBtnAddBook.setVisibility(View.VISIBLE);
        mBtnAddBook.setText(getString(R.string.lfile_OK));

    }

    /**
     * 添加点击事件处理
     */
    private void initListener() {
        // 返回目录上一级
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String tempPath = new File(mPath).getParent();
                UsbFile parentFile=mUSBMassStorageHelper.getParentFolder();
                if (parentFile==null  ){
                    return;
                }
              /*  parentFile=parentFile.getParent();
                if (parentFile == null ) {
                    return;
                }*/
                if (parentFile.isRoot()){
                    mPath="/";
                }else {
                    mPath = parentFile.getAbsolutePath();
                }

                mListFiles = mUSBMassStorageHelper.getUsbFolderFileList(parentFile);
                mPathAdapter.setmListData(mListFiles);
                mPathAdapter.updateAllSelelcted(false);
                mIsAllSelected = false;
               // updateMenuTitle();
                mBtnAddBook.setText(getString(R.string.lfile_Selected));
                mRecylerView.scrollToPosition(0);
                setShowPath(mPath);
             //   Toast.makeText(Fat32LFilePickerActivity.this,"mPath->"+mPath,Toast.LENGTH_SHORT).show();

                //清除添加集合中数据
                mListNumbers.clear();
                mBtnAddBook.setText(R.string.lfile_Selected);
            }
        });
        mPathAdapter.setOnItemClickListener(new Fat32PathAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {

                    //单选模式直接返回
                    if (mListFiles.get(position).isDirectory()) {
                        chekInDirectory(position);
                        return;
                    }
                Toast.makeText(Fat32LFilePickerActivity.this, R.string.lfile_ChooseTip, Toast.LENGTH_SHORT).show();
                }

        });

        mBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDone();
            }
        });
    }


    /**
     * 点击进入目录
     *
     * @param position
     */
    private void chekInDirectory(int position) {
        mPath = mListFiles.get(position).getAbsolutePath();
        setShowPath(mPath);
        //更新数据源
        mListFiles = mUSBMassStorageHelper.getUsbFolderFileList(mListFiles.get(position));//FileUtils.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
        mPathAdapter.setmListData(mListFiles);
        mPathAdapter.notifyDataSetChanged();
        mRecylerView.scrollToPosition(0);
    }

    /**
     * 完成提交
     */
    private void chooseDone() {
        //判断是否数量符合要求
     /*   if (mParamEntity.isChooseMode()) {
            if (mParamEntity.getMaxNum() > 0 && mListNumbers.size() > mParamEntity.getMaxNum()) {
                Toast.makeText(Fat32LFilePickerActivity.this, R.string.lfile_OutSize, Toast.LENGTH_SHORT).show();
                return;
            }
        }*/
        Intent intent = new Intent();
        intent.putStringArrayListExtra("paths", mListNumbers);
        intent.putExtra("path", mTvPath.getText().toString().trim());
        setResult(RESULT_OK, intent);
        this.finish();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mRecylerView = (EmptyRecyclerView) findViewById(R.id.recylerview);
        mTvPath = (TextView) findViewById(R.id.tv_path);
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mBtnAddBook = (Button) findViewById(R.id.btn_addbook);
        mEmptyView = findViewById(R.id.empty_view);
       // mToolbar = (Toolbar) findViewById(R.id.toolbar);
       /* if (mParamEntity.getAddText() != null) {
            mBtnAddBook.setText(mParamEntity.getAddText());
        }*/
    }

    /**
     * 检测SD卡是否可用
     */
    private boolean checkSDState() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 显示顶部地址
     *
     * @param path
     */
    private void setShowPath(String path) {
        mTvPath.setText(path);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        this.mMenu = menu;
        updateOptionsMenu(menu);
        return true;
    }

    /**
     * 更新选项菜单展示，如果是单选模式，不显示全选操作
     *
     * @param menu
     */
    private void updateOptionsMenu(Menu menu) {
        mMenu.findItem(R.id.action_selecteall_cancel).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_selecteall_cancel) {
            //将当前目录下所有文件选中或者取消
            mPathAdapter.updateAllSelelcted(!mIsAllSelected);
            mIsAllSelected = !mIsAllSelected;
            if (mIsAllSelected) {
                for (UsbFile mListFile : mListFiles) {
                    //不包含再添加，避免重复添加
                    if (!mListFile.isDirectory() && !mListNumbers.contains(mListFile.getAbsolutePath())) {
                        mListNumbers.add(mListFile.getAbsolutePath());
                    }
                    mBtnAddBook.setText(getString(R.string.lfile_Selected) + "( " + mListNumbers.size() + " )");
                   /* if (mParamEntity.getAddText() != null) {
                        mBtnAddBook.setText(mParamEntity.getAddText() + "( " + mListNumbers.size() + " )");
                    } else {
                        mBtnAddBook.setText(getString(R.string.lfile_Selected) + "( " + mListNumbers.size() + " )");
                    }*/
                }
            } else {
                mListNumbers.clear();
                mBtnAddBook.setText(getString(R.string.lfile_Selected));
            }
            updateMenuTitle();
        }
        return true;
    }

    /**
     * 更新选项菜单文字
     */
    public void updateMenuTitle() {

        if (mIsAllSelected) {
            mMenu.getItem(0).setTitle(getString(R.string.lfile_Cancel));
        } else {
            mMenu.getItem(0).setTitle(getString(R.string.lfile_SelectAll));
        }
    }

}
