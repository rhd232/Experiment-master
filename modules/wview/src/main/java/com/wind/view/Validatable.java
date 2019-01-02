package com.wind.view;

/**
 * Created by wind on 2018/5/8.
 */

public interface Validatable {

    boolean validate(UnqualifiedCallback callback, boolean quiet);
    boolean validate();
    boolean validate(boolean quiet);
    boolean validate(String errMsg);
    boolean validate(String errMsg, boolean quiet);

    public static interface UnqualifiedCallback{
        void unqualified();
    }
}
