package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jz.experiment.R;

public class PrintTitleLayout extends FrameLayout {
    public PrintTitleLayout(@NonNull Context context) {
        this(context,null);
    }

    public PrintTitleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PrintTitleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }
    TextView tv_company_name;
    TextView tv_test_name;
    private void init() {

        inflate(getContext(), R.layout.layout_print_title,this);
        tv_company_name=findViewById(R.id.tv_company_name);
        tv_test_name=findViewById(R.id.tv_test_name);
    }


    public void setTitle(String companyName,String testName){
        tv_company_name.setText(companyName);
        tv_test_name.setText(testName);
    }
}
