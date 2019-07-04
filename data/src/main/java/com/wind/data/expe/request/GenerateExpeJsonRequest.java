package com.wind.data.expe.request;

import com.wind.base.request.BaseRequest;
import com.wind.data.expe.bean.ExpeJsonBean;

public class GenerateExpeJsonRequest extends BaseRequest {

    private ExpeJsonBean expeJsonBean;

    public ExpeJsonBean getExpeJsonBean() {
        return expeJsonBean;
    }

    public void setExpeJsonBean(ExpeJsonBean expeJsonBean) {
        this.expeJsonBean = expeJsonBean;
    }
}
