package com.wind.base.request;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.wind.base.utils.LogUtil;

/**
 * Created by wind on 2017/3/8.
 * 负责给body加密
 */
public class WrapperRequest {


    @JSONField(name = "body")
    private String  body;
    @JSONField(name = "sign")
    private String sign="";

    public WrapperRequest(String body){
        this.body=body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }


    public static class Builder{

        private String body;

        public Builder(BaseRequest request){
            this.body= JSONObject.toJSONString(request);
            LogUtil.e("JSON",body);
        }

        public WrapperRequest build(){
            WrapperRequest wrapperRequest=new WrapperRequest(body);
            return wrapperRequest;
        }
    }
}
