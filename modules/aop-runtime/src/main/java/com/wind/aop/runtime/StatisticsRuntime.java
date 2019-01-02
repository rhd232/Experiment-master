package com.wind.aop.runtime;

import android.util.Log;

import com.wind.aop.annotations.StatisticsAnnotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by wind on 2018/6/4.
 */
@Aspect
public class StatisticsRuntime {

    @Before("execution(@com.wind.aop.annotations.StatisticsAnnotation * *(..)) && @annotation(ann)")
    public void statistics(JoinPoint joinPoint, StatisticsAnnotation ann){
        Log.e("StatisticsRuntime","aop 拦截成功:"+ann.value());

        return;
    }

}
