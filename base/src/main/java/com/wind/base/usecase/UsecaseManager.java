package com.wind.base.usecase;

import android.support.annotation.NonNull;

import com.wind.base.request.BaseRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by wind on 16/9/11.
 */

public class UsecaseManager {

    private Map<Class,UsecaseCompoment> usecases=new HashMap<>();


    public UsecaseManager addUsecaseCompoment(@NonNull UsecaseCompoment component){
        if (component==null){
            throw new NullPointerException("component is null!");
        }

        UsecaseCompoment exist=usecases.get(component.getRequestClass());
        if (exist!=null){
            throw new IllegalArgumentException(
                    "An UsecaseCompoment is already registered for the requestType is "
                            + component.getRequestClass());

        }

       // Log.e("UsecaseManager","component.getRequestClass():"+component.getRequestClass());
        usecases.put(component.getRequestClass(),component);

        return this;
    }


    public void execute(BaseRequest request){

        UsecaseCompoment usecaseComponent=getUsecaseForRequestType(request);
        usecaseComponent.getUsecase().execute(request,usecaseComponent.getObserver());
    }

    private UsecaseCompoment getUsecaseForRequestType(BaseRequest request) {
        //Log.e("UsecaseManager","request.getClass():"+request.getClass());
        return usecases.get(request.getClass());
    }

    public void cancelSubscription() {

        Set<Map.Entry<Class,UsecaseCompoment>> entrySet=usecases.entrySet();
        Iterator<Map.Entry<Class,UsecaseCompoment>>  it=entrySet.iterator();
        while (it.hasNext()){
            Map.Entry<Class,UsecaseCompoment> entry=it.next();
            entry.getValue().getUsecase().unsubscribe();
        }

    }
}
