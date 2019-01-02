package com.na.coder_compiler;

import com.na.coder_compiler.part.ApiPart;
import com.na.coder_compiler.part.ApiProxyPart;
import com.na.coder_compiler.part.PresenterPart;
import com.na.coder_compiler.part.RepositoryPart;
import com.na.coder_compiler.part.SubscriberPart;
import com.na.coder_compiler.part.UsecasePart;

import java.io.IOException;

import javax.annotation.processing.Filer;

/**
 * Created by wind on 2018/6/3.
 */

public class CoderItem {



    private ApiPart api;
    private UsecasePart usecase;
    private String packageName;
    private SubscriberPart subscriber;
    private PresenterPart presenter;
    private ApiProxyPart apiProxy;
    private RepositoryPart repository;


    public void setApi(ApiPart api) {
        this.api = api;
    }

    public void brewJava(Filer filer) throws IOException {
        if (api!=null)
            api.brewJava(filer);
        if (apiProxy!=null)
            apiProxy.brewJava(filer);
        if (repository!=null)
            repository.brewJava(filer);
        if (usecase!=null)
            usecase.brewJava(filer);
        if (subscriber!=null){
            subscriber.brewJava(filer);
        }
        if (presenter!=null){
            presenter.brewJava(filer);
        }
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setUsecase(UsecasePart usecase) {
        this.usecase = usecase;
        this.usecase.setAssociatedApi(api);
    }

    public void setSubscriber(SubscriberPart subscriber) {
        this.subscriber = subscriber;
    }

    public void setPresenter(PresenterPart presenter) {
        this.presenter = presenter;
        this.presenter.setAssociatedUsecase(usecase);
        this.presenter.setAssociatedSubscriber(subscriber);
    }

    public void setApiProxy(ApiProxyPart apiProxy) {
        this.apiProxy = apiProxy;
    }

    public void setRepository(RepositoryPart repository) {
        this.repository = repository;
    }
}
