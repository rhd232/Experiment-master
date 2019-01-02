package com.wind.data.base.response;

import com.wind.base.response.BaseResponse;
import com.wind.data.base.bean.User;

public class FindUserResponse extends BaseResponse {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
