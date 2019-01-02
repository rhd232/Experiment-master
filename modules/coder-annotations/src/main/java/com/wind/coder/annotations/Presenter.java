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
public @interface Presenter {
    /**
     * 是否需要生成Presenter类，true 需要生成，false不生成
     * @return
     */
    boolean value() default true;
    String packageName() default "";
}
