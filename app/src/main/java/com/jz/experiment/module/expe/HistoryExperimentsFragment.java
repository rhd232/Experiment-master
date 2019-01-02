package com.jz.experiment.module.expe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.activity.DeviceListActivity;
import com.jz.experiment.module.expe.adapter.HistoryExperimentAdapter;
import com.jz.experiment.module.settings.UserSettingsActivity;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.recyclerview.decoration.VerticalSpacesItemDecoration;
import com.wind.coder.annotations.Api;
import com.wind.coder.annotations.Heros;
import com.wind.coder.annotations.Param;
import com.wind.data.expe.bean.AddExperiment;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.view.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Heros(
        param = @Param(
                viewCanonicalName = "com.jz.experiment.module.expe.mvp.HistoryExperimentsView",
                responseCanonicalName = "com.wind.data.expe.HistoryExperimentsResponse",
                requestCanonicalName = "com.wind.data.expe.HistoryExperimentsRequest"
        ),
        api = @Api(httpMethod = Api.HttpMethod.POST, url = "/login")
)
public class HistoryExperimentsFragment extends Fragment {

    @BindView(R.id.rv)
    RecyclerView rv;
    HistoryExperimentAdapter mAdapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hostory_experiments, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        mAdapter=new HistoryExperimentAdapter(getActivity());
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new VerticalSpacesItemDecoration(DisplayUtil.dip2px(getActivity(),8)));
        List<DisplayItem> list=new ArrayList<>();
        list.add(new AddExperiment());
        for (int i=0;i<3;i++){
            HistoryExperiment experiment=new HistoryExperiment();
            experiment.setName("实验名称1");
            experiment.setDevice("设备1");
            experiment.setTimestamp("2018-11-14 10:11:02");
            ExperimentStatus status=new ExperimentStatus();
            status.setStatus(0);
            status.setDesc("已完成");
            experiment.setStatus(status);
            list.add(experiment);
        }

        mAdapter.replace(list);

    }

    @OnClick({R.id.tv_dev_list,R.id.tv_user})
    public void onViewClick(View v){
        switch (v.getId()){
            case R.id.tv_user:
                UserSettingsActivity.start(getActivity());
                break;
            case R.id.tv_dev_list:
                DeviceListActivity.start(getActivity());
                break;
        }
    }
}
