package com.wind.base.recyclerview.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HorizontalSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public HorizontalSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {


        int position = parent.getChildAdapterPosition(view);

      /*  outRect.right = space;
        outRect.left = space;
        outRect.bottom = space*2;*/
        if (position > 0 ) {
            outRect.left = space;
        }
    }
}