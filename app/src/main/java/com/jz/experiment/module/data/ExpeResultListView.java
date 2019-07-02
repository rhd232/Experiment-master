package com.jz.experiment.module.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.jz.experiment.R;

public class ExpeResultListView extends FrameLayout {
    public ExpeResultListView(@NonNull Context context) {
        this(context,null);
    }

    public ExpeResultListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ExpeResultListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    ListView lv_result;
    private void init() {
        inflate(getContext(), R.layout.layout_expe_dt_result,this);
        lv_result=findViewById(R.id.lv_result);


    }
}
