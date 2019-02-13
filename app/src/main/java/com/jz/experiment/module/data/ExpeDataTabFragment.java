package com.jz.experiment.module.data;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.expe.event.SavedExpeDataEvent;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.wind.base.mvp.view.TabLayoutFragment;
import com.wind.base.response.BaseResponse;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.response.FindExpeResponse;
import com.wind.view.TitleBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import ezy.ui.layout.LoadingLayout;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ExpeDataTabFragment extends TabLayoutFragment {

    ExpeDataStore mExpeDataStore;
    LoadingLayout layout_loading;
    TitleBar mTitleBar;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        layout_loading = view.findViewById(R.id.layout_loading);
        layout_loading.setEmpty(R.layout.layout_expe_empty);
        layout_loading.setOnEmptyInflateListener(new LoadingLayout.OnInflateListener() {
            @Override
            public void onInflate(View inflated) {
                inflated.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new ToExpeSettingsEvent(null));

                    }
                });
            }
        });
        mTitleBar = view.findViewById(R.id.title_bar);
        initTitleBar();


        mExpeDataStore =
                ExpeDataStore
                        .getInstance(ProviderModule.getInstance()
                                .getBriteDb(getActivity().getApplicationContext()));

        //TODO 获取之前已经完成且保存的历史实验
        loadExpe();


    }


    private void initTitleBar() {
        mTitleBar.setTextColor(Color.WHITE);
        mTitleBar.setRightTextColor(Color.WHITE);
        mTitleBar.setLeftVisibility(View.GONE);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.color686868));
        mTitleBar.setTitle("数据");
        mTitleBar.setRightText("筛选");
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterActivity.start(getActivity());
            }
        });
    }

    private Subscription mFindSubscription;

    private void loadExpe() {
        layout_loading.showLoading();
        mFindSubscription = mExpeDataStore
                .findAllCompleted()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FindExpeResponse>() {
                    @Override
                    public void call(FindExpeResponse response) {
                        mFindSubscription.unsubscribe();
                        if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                            List<HistoryExperiment> experiments = response.getItems();
                            if (experiments == null || experiments.isEmpty()) {
                                layout_loading.showEmpty();
                            } else {
                                layout_loading.showContent();
                                List<Fragment> fragments = new ArrayList<>();
                                List<String> titles = new ArrayList<>();
                                for (int i = 0; i < experiments.size(); i++) {
                                    titles.add("历史实验");
                                    HistoryExperiment expe = experiments.get(i);
                                    ExpeDataFragment f = ExpeDataFragment.newInstance(expe);
                                    fragments.add(f);

                                   // titles.add(expe.getName());

                                }
                                mFragmentAdapter.setFragments(fragments);
                                mFragmentAdapter.setTitles(titles);
                                mFragmentAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
        ;
    }


    @Override
    public int getLayoutRes() {
        return R.layout.fragment_tab_expe_data;
    }


    @Override
    protected List<Fragment> getFragments() {
       return null;
    }



    public void setExpe(HistoryExperiment expe) {

        //增加一个tab显示本实验
        ExpeDataFragment f = ExpeDataFragment.newInstance(expe);
        List<Fragment> fragments = mFragmentAdapter.getFragments();
        List<String> titles=mFragmentAdapter.getTitles();
        if (fragments == null) {
            fragments = new ArrayList<>();
            titles=new ArrayList<>();
            mFragmentAdapter.setFragments(fragments);
            mFragmentAdapter.setTitles(titles);
        } else {
            int lastIndex=fragments.size()-1;
            ExpeDataFragment expeDataFragment= (ExpeDataFragment) fragments.get(lastIndex);
            if (!expeDataFragment.isSavedExpe()){
                //fragments.remove(lastIndex);
                //titles.remove(lastIndex);
                titles.remove(lastIndex);
                titles.add("历史实验");
            }


        }

        titles.add("本次实验");
        fragments.add(f);
        mFragmentAdapter.notifyDataSetChanged();
        view_pager.setCurrentItem(fragments.size()-1, false);


        if (fragments.size()>4){
            layout_tab.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        layout_loading.showContent();
    }

    @Subscribe
    public void onSavedExpeDataEvent(SavedExpeDataEvent event){
        loadExpe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}


