package com.wind.coder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wind on 2018/6/3.
 * 用于生成Usecase类
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Usecase {
    String packageName() default "";
}
