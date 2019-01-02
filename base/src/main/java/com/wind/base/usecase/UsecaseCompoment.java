package com.wind.base.usecase;

import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import java.lang.reflect.ParameterizedType;

import rx.Observer;

/**
 * Created by wind on 16/9/11.
 */

public class UsecaseCompoment<Q extends BaseRequest ,R extends BaseResponse> {

    private Observer<R> subscriber;

    private Usecase<Q,R> usecase;
    private Class requestClass;

    public UsecaseCompoment(Observer<R> subscriber, Usecase<Q, R> usecase) {
        this.subscriber = subscriber;
        this.usecase = usecase;

        ParameterizedType parameterizedType= (ParameterizedType) usecase.getClass().getGenericSuperclass();
        requestClass= (Class) parameterizedType.getActualTypeArguments()[0];
    }


    public Class getRequestClass(){
        return requestClass;
    }

    public Observer<R> getObserver() {
        return subscriber;
    }

    public Usecase<Q, R> getUsecase() {
        return usecase;
    }
}
