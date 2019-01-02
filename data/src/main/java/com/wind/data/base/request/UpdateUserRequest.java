package com.wind.data.base.request;

import com.wind.base.request.BaseRequest;

public class UpdateUserRequest extends BaseRequest {
    private String pwd;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
