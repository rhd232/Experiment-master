package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.util.ReportRepo;

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

        //获取
        String company=ReportRepo.getInstance().get(getContext()).getCompany();
        if (!TextUtils.isEmpty(company)){
            tv_company_name.setText(company);
        }


    }


    public void setExpeName(String testName){
        tv_test_name.setText(testName);
    }


    public String getCompany(){
        return tv_company_name.getText().toString().trim();
    }

    public void save(){
        ReportRepo.Config config=new ReportRepo.Config();
        config.setCompany(getCompany());
        ReportRepo.getInstance().store(getContext(),config);
    }

}



