package com.wind.coder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Heros {
    Param param();
    Api api();
    Usecase usecase() default @Usecase;
    Subscriber subscriber() default @Subscriber;
    Presenter presenter() default @Presenter(value = true);
}
