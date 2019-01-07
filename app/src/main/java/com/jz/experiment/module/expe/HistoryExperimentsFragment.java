package com.jz.experiment.module.expe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.expe.activity.DeviceListActivity;
import com.jz.experiment.module.expe.activity.UserSettingsStep1Activity;
import com.jz.experiment.module.expe.adapter.HistoryExperimentAdapter;
import com.jz.experiment.module.settings.UserSettingsActivity;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.recyclerview.decoration.VerticalSpacesItemDecoration;
import com.wind.base.response.BaseResponse;
import com.wind.coder.annotations.Api;
import com.wind.coder.annotations.Heros;
import com.wind.coder.annotations.Param;
import com.wind.data.expe.bean.AddExperiment;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.response.FindExpeResponse;
import com.wind.view.DisplayUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezy.ui.layout.LoadingLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@Heros(
        param = @Param(
                viewCanonicalName = "com.jz.experiment.module.expe.mvp.HistoryExperimentsView",
                responseCanonicalName = "com.wind.data.expe.HistoryExperimentsResponse",
                requestCanonicalName = "com.wind.data.expe.HistoryExperimentsRequest"
        ),
        api = @Api(httpMethod = Api.HttpMethod.POST, url = "/login")
)
public class HistoryExperimentsFragment extends BaseFragment {

    @BindView(R.id.rv)
    RecyclerView rv;
    HistoryExperimentAdapter mAdapter;
    ExpeDataStore mExpeDataStore;
    @BindView(R.id.layout_loading)
    LoadingLayout layout_loading;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_hostory_experiments;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        layout_loading.setEmpty(R.layout.layout_expe_empty);
        layout_loading.setOnEmptyInflateListener(new LoadingLayout.OnInflateListener() {
            @Override
            public void onInflate(View inflated) {
                inflated.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserSettingsStep1Activity.start(getActivity());
                    }
                });
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        mAdapter = new HistoryExperimentAdapter(getActivity());
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new VerticalSpacesItemDecoration(DisplayUtil.dip2px(getActivity(), 8)));
        mExpeDataStore = new ExpeDataStore(ProviderModule.getInstance()
                        .getBriteDb(getActivity()
                        .getApplicationContext()));
        loadData();
    }



    private void loadData() {

        mExpeDataStore
                .findAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FindExpeResponse>() {
                    @Override
                    public void call(FindExpeResponse response) {
                        if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                            List<HistoryExperiment> experiments = response.getItems();
                            if (experiments == null || experiments.isEmpty()) {
                                layout_loading.showEmpty();
                            } else {
                                mAdapter.getItems().clear();
                                mAdapter.getItems().add(new AddExperiment());
                                mAdapter.getItems().addAll(experiments);
                                mAdapter.notifyDataSetChanged();
                                layout_loading.showContent();
                            }

                        } else {
                            //显示空白页
                            layout_loading.showEmpty();
                        }
                    }
                });

    }

    @OnClick({R.id.tv_dev_list, R.id.tv_user})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_user:
                UserSettingsActivity.start(getActivity());
                break;
            case R.id.tv_dev_list:
                DeviceListActivity.start(getActivity());
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

       /* if (isVisibleToUser){
            SystemUiUtil.setStatusBarColor(getActivity(), getResources().getColor(R.color.color686868));
        }*/
    }
}
