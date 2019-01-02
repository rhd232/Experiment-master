package com.na.coder_compiler;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

import static javax.lang.model.element.ElementKind.PACKAGE;

/**
 * Created by wind on 2018/6/3.
 */

public class Utils {

    public static String ACTIVITY_SUFFIX="Activity";
    public static String FRAGMENT_SUFFIX="Fragment";
    public static String LAYOUT_SUFFIX="Layout";


    private static final String API_SUFFIX="Api";
    public static final String APIPROXY_SUFFIX="ApiProxy";
    private static final String REQUEST_SUFFIX="Request";
    private static final String RESPONSE_SUFFIX="Response";
    private static final String VIEW_SUFFIX="View";
    private static final String USECASE_SUFFIX="Usecase";
    private static final String SUBSCRIBER_SUFFIX="Subscriber";

    public static ClassName getApiClassName(String packageName,String simpleName) {
        String apiSimpleName=simpleName+ API_SUFFIX;
        return ClassName.get(packageName,apiSimpleName);
    }
    public static ClassName getApiProxyClassName(String packageName,String simpleName) {
        String apiSimpleName=simpleName+ APIPROXY_SUFFIX;
        return ClassName.get(packageName,apiSimpleName);
    }
    public static ClassName getRepositoryClassName(String packageName,String simpleName) {
        String apiSimpleName=simpleName+ "PageRepository";
        return ClassName.get(packageName,apiSimpleName);
    }
    public static ClassName getResponseClassName(String packageName,String simpleName){
        String responseSimpleName=simpleName+RESPONSE_SUFFIX;
        ClassName responseClassName=ClassName.get(packageName,responseSimpleName);
        return responseClassName;
    }
    public static ClassName getRequestClassName(String packageName,String simpleName){
        String requestSimpleName=simpleName+REQUEST_SUFFIX;
        ClassName requestClassName=ClassName.get(packageName,requestSimpleName);
        return requestClassName;
    }
    public static ClassName getViewClassName(String packageName,String simpleName){
        ClassName viewClassName=ClassName.get(packageName,simpleName+VIEW_SUFFIX);
        return viewClassName;
    }

    public static ClassName getSubscriberClassName(String packageName,String simpleName){
        ClassName subscriberClassName=ClassName.get(packageName,simpleName+SUBSCRIBER_SUFFIX);
        return subscriberClassName;
    }
    public static ClassName getUsecaseClassName(String packageName,String simpleName){
        ClassName viewClassName=ClassName.get(packageName,simpleName+USECASE_SUFFIX);
        return viewClassName;
    }

    public static ClassName getUsecaseComponentClassName(){
        ClassName className=ClassName.get(" com.wind.base.usecase",
                "UsecaseCompoment");
        return className;

    }

    public static ClassName getInjectClassName(){
        ClassName injectClassName=ClassName.get("javax.inject","Inject");
        return injectClassName;
    }

    public static ClassName getNextObservableClassName(){
        ClassName nextObservableClassName=ClassName.get("com.wind.base.subscriber",
                "NextObserver");
        return nextObservableClassName;
    }

    public static ClassName getExecutePresenterClassName(){
        ClassName executePresenterClassName=ClassName.get("com.wind.base.mvp.presenter",
                "ExecutePresenter");
        return executePresenterClassName;
    }


    public static PackageElement getPackageElement(Element element) {
        while (element.getKind() != PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    public static boolean isEmpty(String value){
        if (value ==null || "".equals(value)){
            return true;
        }
        return false;
    }
}
