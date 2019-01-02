package com.wind.coder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wind on 2018/6/3.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Api {
    HttpMethod httpMethod();

    String url();

    String packageName() default "";


    enum HttpMethod {
        GET("get"), POST("post"), PUT("put");

        private String name;

        HttpMethod(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}


