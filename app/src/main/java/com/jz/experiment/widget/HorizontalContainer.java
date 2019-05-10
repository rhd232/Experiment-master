package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class HorizontalContainer extends LinearLayout {
    public HorizontalContainer(Context context) {
        this(context,null);
    }

    public HorizontalContainer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HorizontalContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

    }

    public void setAdapter(RecyclerView.Adapter adapter){
        removeAllViews();
        for (int i=0;i<adapter.getItemCount();i++){
            RecyclerView.ViewHolder vh=adapter.onCreateViewHolder(this,i);
            adapter.onBindViewHolder(vh,i);

            addView(vh.itemView);
        }
    }
}
