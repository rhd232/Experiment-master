package com.jz.experiment.module.data;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.jz.experiment.R;
import com.wind.base.mvp.view.TabLayoutFragment;
import com.wind.view.TitleBar;

import java.util.ArrayList;
import java.util.List;

public class ExpeDataTabFragment extends TabLayoutFragment {


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar=view.findViewById(R.id.title_bar);
        titleBar.setTextColor(Color.WHITE);
        titleBar.setRightTextColor(Color.WHITE);
        titleBar.setBackgroundColor(getResources().getColor(R.color.color686868));
        titleBar.setTitle("数据");
        titleBar.setRightText("筛选");
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_tab_expe_data;
    }

    @Override
    public List<String> getTitles() {
        List<String> titles=new ArrayList<>();
        titles.add("基因实验");
        return titles;
    }

    @Override
    protected List<Fragment> getFragments() {
        List<Fragment> fragments=new ArrayList<>();
        fragments.add(new ExpeDataFragment());
        return fragments;
    }
}
