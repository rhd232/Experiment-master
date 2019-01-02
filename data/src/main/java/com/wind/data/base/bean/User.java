package com.wind.data.base.bean;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class User implements UserModel{

    public static final UserModel.Factory<User> FACTORY=new UserModel.Factory<>(new UserModel.Creator<User>() {
        @Override
        public User create(long _id, @NonNull String username,String password) {
            return new AutoValue_User(_id,username,password);
        }
    });
}
