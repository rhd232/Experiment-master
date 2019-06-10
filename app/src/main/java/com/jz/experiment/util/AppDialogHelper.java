package com.jz.experiment.util;


import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.adapter.ChannelMaterialAdapter;
import com.jz.experiment.module.expe.adapter.StepSelectAdapter;
import com.wind.data.expe.bean.ChannelMaterial;
import com.wind.data.expe.bean.MeltMode;
import com.wind.data.expe.bean.Mode;
import com.wind.base.bean.PartStage;
import com.wind.base.dialog.AlertDialogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wind on 2017/6/12.
 */

public class AppDialogHelper {



    public interface OnStepSelectListener {
        void onStepSelected(PartStage step);
    }

    public static void showStepSelectDialog(Context context, List<PartStage> partStages, final OnStepSelectListener listener) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context, R.layout.dialog_step_select,
                false);
        GridView gv = alertDialog.findViewById(R.id.gv);
        final StepSelectAdapter adapter = new StepSelectAdapter(context, R.layout.item_sel_channel_material);
        adapter.replaceAll(partStages);
        PartStage none = new PartStage();

        none.setStepName(context.getString(R.string.setup_nopic));
        boolean hasPic = false;
        for (PartStage pStage : partStages) {
            if (pStage.isTakePic()) {
                hasPic = true;
                break;
            }
        }
        if (!hasPic) {
            none.setTakePic(true);
        }
        adapter.add(none);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < adapter.getData().size(); i++) {
                    adapter.getData().get(i).setTakePic(false);
                }
                adapter.getData().get(position).setTakePic(true);
                adapter.notifyDataSetChanged();
            }
        });
        alertDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                alertDialog.dismiss();
            }
        });
        alertDialog.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < adapter.getData().size(); i++) {

                    PartStage partStage = adapter.getData().get(i);
                    if (partStage.isTakePic()) {
                        listener.onStepSelected(partStage);
                        break;
                    }
                }
                alertDialog.dismiss();

            }
        });
    }

    public static void showChannelSelectDialog(Context context, final int position, final OnChannelSelectListener listener) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context, R.layout.dialog_channel_select,
                false);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        TextView tv = alertDialog.findViewById(R.id.tv_channel_sel_title);
        final EditText et_remark = alertDialog.findViewById(R.id.et_remark);

        alertDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                alertDialog.dismiss();
            }
        });
        tv.setText(context.getString(R.string.channel_select_title, (position+1) + ""));
        GridView gv = alertDialog.findViewById(R.id.gv_ranliao);
        final ChannelMaterialAdapter adapter = new ChannelMaterialAdapter(context, R.layout.item_sel_channel_material);
        alertDialog.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelMaterial selectedChannelMaterial=null;
                //TODO 保存通道数据
                for (int i = 0; i < adapter.getData().size(); i++) {

                    ChannelMaterial channelMaterial = adapter.getData().get(i);
                    if (channelMaterial.isSelected()) {
                        String remark = et_remark.getText().toString().trim();
                        channelMaterial.setRemark(remark);
                        selectedChannelMaterial=channelMaterial;
                       // listener.onChannelSelected(position, channelMaterial);
                        break;
                    }
                }
                listener.onChannelSelected(position, selectedChannelMaterial);
                alertDialog.dismiss();

            }
        });
        gv.setAdapter(adapter);
        List<ChannelMaterial> list = new ArrayList<>();
        switch (position){
            case 0:
                list.add(new ChannelMaterial("FAM"));
                list.add(new ChannelMaterial("SYBR"));
                break;
            case 1:
                list.add(new ChannelMaterial("HEX"));
                list.add(new ChannelMaterial("VIC"));
                list.add(new ChannelMaterial("TET"));
                list.add(new ChannelMaterial("JOE"));
                break;
            case 2:
                list.add(new ChannelMaterial("ROX"));
                list.add(new ChannelMaterial("TexRed"));
                break;
            case 3:
                list.add(new ChannelMaterial("Cy5"));
                list.add(new ChannelMaterial("Quasar670"));
                break;
        }
        if (list.size()>3){
            gv.setNumColumns(3);
        }else {
            gv.setNumColumns(2);
        }
        adapter.replaceAll(list);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < adapter.getData().size(); i++) {
                    adapter.getData().get(i).setSelected(false);
                }
                adapter.getData().get(position).setSelected(true);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnChannelSelectListener {
        void onChannelSelected(int position, ChannelMaterial material);
    }

    public interface OnModeSelectListener {
        void onModeSelected(List<Mode> modes);
    }

    public static void showModeSelectDialog(final Context context, final List<Mode> modes, final OnModeSelectListener listener) {

        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context, R.layout.dialog_mode_select, false);
        View tv_mode1 = alertDialog.findViewById(R.id.tv_mode1);
        tv_mode1.setActivated(true);
        final View tv_mode2 = alertDialog.findViewById(R.id.tv_mode2);
        if (modes.size() == 2) {
            tv_mode2.setActivated(true);
        }
        alertDialog.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_mode2.isActivated()) {
                    if (modes.size() == 1) {
                        String melting=context.getString(R.string.setup_mode_melting);
                        modes.add(new MeltMode(melting));
                    }
                } else {
                    if (modes.size() == 2) {
                        modes.remove(1);
                    }
                }
                listener.onModeSelected(modes);
                alertDialog.dismiss();
            }
        });
        alertDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        tv_mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
            }
        });
    }

    public static void showNormalDialog(Context context, String msg, String leftBtnName, String rightBtnName, final DialogOperCallback callback) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context, R.layout.dialog_normal, false);
        TextView tv_left_btn = (TextView) alertDialog.findViewById(R.id.tv_left_btn);
        TextView tv_right_btn = (TextView) alertDialog.findViewById(R.id.tv_right_btn);
        TextView tv_msg = (TextView) alertDialog.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);
        tv_left_btn.setText(leftBtnName);
        tv_right_btn.setText(rightBtnName);
        alertDialog.findViewById(R.id.tv_left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                callback.onDialogCancelClick();
            }
        });
        alertDialog.findViewById(R.id.tv_right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                callback.onDialogConfirmClick();
            }
        });
    }

    public static void showSingleBtnDialog(Context context, final DialogOperCallback callback) {
        showSingleBtnDialog(context,context.getString(R.string.dialog_btn_ok),callback);
    }
    public static void showSingleBtnDialog(Context context, String msg, final DialogOperCallback callback) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context, R.layout.dialog_single, true);
        TextView tv_msg = alertDialog.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);

        alertDialog.findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                callback.onDialogConfirmClick();
            }
        });
    }

    public static void showNormalDialog(Context context, String msg, final DialogOperCallback callback) {
        showNormalDialog(context, msg, context.getString(R.string.dialog_btn_cancel), context.getString(R.string.dialog_btn_ok), callback);
    }


    /**
     * 照片选择对话框 回调
     */
    public interface DialogPhotoSelectCallback {
        void onDialogCameraClick();

        void onDialogAlbumClick();
    }

    public interface DialogItemsCallback {
        void onDialogItemClick(int position);
    }

    /**
     * 对话框取消，确定按钮回调
     */
    public static abstract class DialogOperCallback {
        public void onDialogCancelClick() {
        }

        public void onDialogNeutralClcik() {
        }

        ;

        public abstract void onDialogConfirmClick();
    }


}
