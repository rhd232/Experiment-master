package com.wind.base.recyclerview.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;
    public VerticalSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {


        outRect.top = space;


    }
}