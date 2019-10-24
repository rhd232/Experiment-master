package com.leon.lfilepickerlibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mjdev.libaums.fs.UsbFile;
import com.leon.lfilepickerlibrary.R;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.leon.lfilepickerlibrary.utils.FileUtils;
import com.leon.lfilepickerlibrary.utils.USBMassStorageHelper;

import java.util.List;

/**
 * 作者：Leon
 * 时间：2017/3/15 15:47
 */
public class Fat32PathAdapter extends RecyclerView.Adapter<Fat32PathAdapter.PathViewHolder> {
    public interface OnItemClickListener {
        void click(int position);
    }

    public interface OnCancelChoosedListener {
        void cancelChoosed(CheckBox checkBox);
    }

    private final String TAG = "FilePickerLeon";
    private List<UsbFile> mListData;
    private Context mContext;
    public OnItemClickListener onItemClickListener;
    private boolean[] mCheckedFlags;
    private int mIconStyle;
    private USBMassStorageHelper mUSBMassStorageHelper;
    public Fat32PathAdapter(List<UsbFile> mListData, USBMassStorageHelper usbMassStorageHelper,Context mContext
    ) {
        this.mUSBMassStorageHelper=usbMassStorageHelper;
        this.mListData = mListData;
        this.mContext = mContext;
        mCheckedFlags = new boolean[mListData.size()];
    }

    @Override
    public PathViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.lfile_listitem, null);
        PathViewHolder pathViewHolder = new PathViewHolder(view);
        return pathViewHolder;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    @Override
    public void onBindViewHolder(final PathViewHolder holder, final int position) {
        final UsbFile file = mListData.get(position);
        if (!file.isDirectory()) {
            updateFileIconStyle(holder.ivType);
            holder.tvName.setText(file.getName());
            holder.tvDetail.setText(mContext.getString(R.string.lfile_FileSize) + " " + FileUtils.getReadableFileSize(file.getLength()));
            holder.cbChoose.setVisibility(View.VISIBLE);
        } else {
            updateFloaderIconStyle(holder.ivType);
            holder.tvName.setText(file.getName());
            //文件大小过滤
            List files = mUSBMassStorageHelper.getUsbFolderFileList(file,false);//FileUtils.getFileList(file.getAbsolutePath(), mFileFilter, mIsGreater, mFileSize);

            if (files == null) {
                holder.tvDetail.setText("0 " + mContext.getString(R.string.lfile_LItem));
            } else {
                holder.tvDetail.setText(files.size() + " " + mContext.getString(R.string.lfile_LItem));
            }
            holder.cbChoose.setVisibility(View.GONE);
        }

        holder.cbChoose.setVisibility(View.GONE);

        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!file.isDirectory()) {
                    holder.cbChoose.setChecked(!holder.cbChoose.isChecked());
                }
                onItemClickListener.click(position);
            }
        });
        holder.cbChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同步复选框和外部布局点击的处理
                onItemClickListener.click(position);
            }
        });
        holder.cbChoose.setOnCheckedChangeListener(null);//先设置一次CheckBox的选中监听器，传入参数null
        holder.cbChoose.setChecked(mCheckedFlags[position]);//用数组中的值设置CheckBox的选中状态
        //再设置一次CheckBox的选中监听器，当CheckBox的选中状态发生改变时，把改变后的状态储存在数组中
        holder.cbChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCheckedFlags[position] = b;
            }
        });
    }

    private void updateFloaderIconStyle(ImageView imageView) {
        switch (mIconStyle) {
            case Constant.ICON_STYLE_BLUE:
                imageView.setBackgroundResource(R.mipmap.lfile_folder_style_blue);
                break;
            case Constant.ICON_STYLE_GREEN:
                imageView.setBackgroundResource(R.mipmap.lfile_folder_style_green);
                break;
            case Constant.ICON_STYLE_YELLOW:
                imageView.setBackgroundResource(R.mipmap.lfile_folder_style_yellow);
                break;
        }
    }

    private void updateFileIconStyle(ImageView imageView) {
        switch (mIconStyle) {
            case Constant.ICON_STYLE_BLUE:
                imageView.setBackgroundResource(R.mipmap.lfile_file_style_blue);
                break;
            case Constant.ICON_STYLE_GREEN:
                imageView.setBackgroundResource(R.mipmap.lfile_file_style_green);
                break;
            case Constant.ICON_STYLE_YELLOW:
                imageView.setBackgroundResource(R.mipmap.lfile_file_style_yellow);
                break;
        }
    }

    /**
     * 设置监听器
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置数据源
     *
     * @param mListData
     */
    public void setmListData(List<UsbFile> mListData) {
        this.mListData = mListData;
        mCheckedFlags = new boolean[mListData.size()];
    }

    public void setmIconStyle(int mIconStyle) {
        this.mIconStyle = mIconStyle;
    }

    /**
     * 设置是否全选
     *
     * @param isAllSelected
     */
    public void updateAllSelelcted(boolean isAllSelected) {

        for (int i = 0; i < mCheckedFlags.length; i++) {
            mCheckedFlags[i] = isAllSelected;
        }
        notifyDataSetChanged();
    }

    class PathViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout layoutRoot;
        private ImageView ivType;
        private TextView tvName;
        private TextView tvDetail;
        private CheckBox cbChoose;

        public PathViewHolder(View itemView) {
            super(itemView);
            ivType = (ImageView) itemView.findViewById(R.id.iv_type);
            layoutRoot = (RelativeLayout) itemView.findViewById(R.id.layout_item_root);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDetail = (TextView) itemView.findViewById(R.id.tv_detail);
            cbChoose = (CheckBox) itemView.findViewById(R.id.cb_choose);
        }
    }
}


