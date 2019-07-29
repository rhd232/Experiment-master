package com.jz.experiment.util;

import android.content.Context;

import com.wind.base.C;
import com.wind.base.utils.PrefsUtil;


/**
 * Created by wind on 2017/11/30.
 */

public class ReportRepo {

    private ReportRepo(){}
    public static class LayzHolder {
        public static final ReportRepo INSTANCE = new ReportRepo();
    }

    public static ReportRepo getInstance() {
        return LayzHolder.INSTANCE;
    }

    public void store(Context context, Config config) {
        if (config == null) {
            PrefsUtil.setString(context, C.PREF_KEY.REPORT_COMPANY, "");
            PrefsUtil.setString(context, C.PREF_KEY.REPORT_EXPE_NAME, "");

        } else {
            PrefsUtil.setString(context, C.PREF_KEY.REPORT_COMPANY, config.getCompany());
            PrefsUtil.setString(context, C.PREF_KEY.REPORT_EXPE_NAME, config.getExpeName());

        }
    }

    public Config get(Context context) {
        Config config = new Config();
        String company = PrefsUtil.getString(context, C.PREF_KEY.REPORT_COMPANY, "");
        String expeName = PrefsUtil.getString(context, C.PREF_KEY.REPORT_EXPE_NAME, "");
        config.setCompany(company);
        config.setExpeName(expeName);
        return config;

    }


    public static class Config{

        private String company;
        private String expeName;

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getExpeName() {
            return expeName;
        }

        public void setExpeName(String expeName) {
            this.expeName = expeName;
        }
    }


}
