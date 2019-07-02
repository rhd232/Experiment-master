package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jz.experiment.R;

public class PrintTailLayout extends FrameLayout {
    public PrintTailLayout(@NonNull Context context) {
        this(context,null);
    }

    public PrintTailLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PrintTailLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    TextView tv_test_by;
    TextView tv_check_by;
    TextView tv_test_company;
    TextView tv_report_date;
    private void init() {

        inflate(getContext(), R.layout.layout_print_tail,this);
        tv_test_by=findViewById(R.id.tv_test_by);
        tv_check_by=findViewById(R.id.tv_check_by);
        tv_test_company=findViewById(R.id.tv_test_company);
        tv_report_date=findViewById(R.id.tv_report_date);
    }

    public void setTail(String testBy,String checkBy,String testCompany){
        String user=getContext().getString(R.string.print_user);
        String auditor=getContext().getString(R.string.print_auditor);
        String unit=getContext().getString(R.string.print_unit);
        tv_test_by.setText(user+": "+testBy);
        tv_check_by.setText(auditor+": "+checkBy);
        tv_test_company.setText(unit+": "+testCompany);
    }

    public void setReportDate(String date){
        tv_report_date.setText(date);
    }
}
