package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wind.data.expe.bean.DtMode;
import com.wind.data.expe.bean.Mode;
import com.jz.experiment.module.expe.event.AddCyclingStageEvent;
import com.wind.base.bean.CyclingStage;
import com.jz.experiment.module.expe.event.DelCyclingStageEvent;
import com.wind.base.bean.EndStage;
import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.RefreshStageAdapterEvent;
import com.jz.experiment.module.expe.adapter.StageAdapter;
import com.wind.base.bean.StartStage;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.BaseActivity;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.utils.Navigator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserSettingsStep2Activity extends BaseActivity {

    public static void start(Context context){
        Navigator.navigate(context,UserSettingsStep2Activity.class);
    }
    RecyclerView rv;
    StageAdapter mStageAdapter;

    @BindView(R.id.tv_mode)
    TextView tv_mode;
    @BindView(R.id.layout_melt)
    LinearLayout layout_melt;
    private Handler handler=new Handler();

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("用户设置2");
        mTitleBar.setRightIcon(R.drawable.icon_program_save);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step2);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        rv=findViewById(R.id.rv);
        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(manager);
        rv.setNestedScrollingEnabled(false);
        mStageAdapter=new StageAdapter(getActivity());
        rv.setAdapter(mStageAdapter);

        List<DisplayItem> list=new ArrayList<>();
        list.add(new StartStage());
        list.add(new CyclingStage());
        list.add(new EndStage());
        mStageAdapter.addAll(list);


        rv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStageAdapter.buildLink();
            }
        },500);


        findViewById(R.id.btn_build).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildLink();
            }
        });
    }



    @Subscribe
    public void onRefreshStageAdapterEvent(RefreshStageAdapterEvent event){
        mStageAdapter.notifyDataSetChanged();
        buildLink();
        Log.i("ChangeStage","onRefreshStageAdapterEvent");
    }
    @Subscribe
    public void onAddCyclingStage(AddCyclingStageEvent event){
        mStageAdapter.add(event.getPosition(),new CyclingStage());

        buildLink();

        Log.i("ChangeStage","onAddCyclingStage");
    }
    @Subscribe
    public void onDelCyclingStageEvent(DelCyclingStageEvent event){
        mStageAdapter.remove(event.getPosition());
        buildLink();
        Log.i("ChangeStage","onDelCyclingStageEvent");
    }

    private void buildLink(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStageAdapter.buildLink();
                mStageAdapter.notifyDataSetChanged();
            }
        },200);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private List<Mode> mModes;
    @OnClick({R.id.rl_mode_sel,R.id.tv_next})
    public void onViewClick(View v){
        switch (v.getId()){
            case R.id.tv_next:
                ExpeRunningActivity.start(getActivity());
                break;
            case R.id.rl_mode_sel:
                if (mModes==null){
                    mModes=new ArrayList<>();
                    mModes.add(new DtMode("变温扩增"));
                }
                AppDialogHelper.showModeSelectDialog(getActivity(), mModes, new AppDialogHelper.OnModeSelectListener() {
                    @Override
                    public void onModeSelected(List<Mode> modes) {
                        mModes=modes;
                        StringBuilder sBuilder=new StringBuilder(mModes.get(0).getName());
                        if (modes.size()==2){
                            sBuilder.append("+")
                            .append(mModes.get(1).getName());
                            layout_melt.setVisibility(View.VISIBLE);
                        }else {
                            layout_melt.setVisibility(View.GONE);
                        }
                        tv_mode.setText(sBuilder.toString());
                    }
                });
                break;
        }
    }

}
