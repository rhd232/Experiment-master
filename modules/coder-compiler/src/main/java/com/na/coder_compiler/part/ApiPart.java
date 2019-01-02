package com.na.coder_compiler.part;

import com.na.coder_compiler.Utils;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wind.coder.annotations.Api;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

import static com.na.coder_compiler.Utils.getViewClassName;

/**
 * Created by wind on 2018/6/3.
 */

public class ApiPart {
     static final String API_SUFFIX="Api";
     static final String PACKAGE_SUFFITX=".api";
     static final String RX_OBSERVABLE_NAME="Observable";
     static final String RX="rx";
     static final String RESPONSE_SUFFIX="Response";
    private Api.HttpMethod method;
    private String url;
    private String packageName;
    private String parentPackageName;
    private String annotatedClassSimpleName;
    private String prefix;
    private ClassName responseClassName;
    public ApiPart(TypeElement annotatedElement,Api api){
        annotatedClassSimpleName=annotatedElement.getSimpleName().toString();
            //Api api=annotatedElement.getAnnotation(Api.class);
        method=api.httpMethod();
        url=api.url();
        packageName=api.packageName();
        parentPackageName= Utils.getPackageElement(annotatedElement).getQualifiedName().toString();
        if ("".equals(packageName)|| null==packageName){
            packageName=parentPackageName+PACKAGE_SUFFITX;
        }

    }

    public String getPackageName() {
        return packageName;
    }

    public Api.HttpMethod getMethod() {
        return method;
    }



    public String getUrl() {
        return url;
    }


    public void brewJava(Filer filer) throws IOException {
        String methodName="";
        Class methodAnnotation=null;
        boolean needFormUrlEncoded=false;
        switch (method){
            case GET:
                methodName="get";
                methodAnnotation= GET.class;
                needFormUrlEncoded=false;
                break;
            case POST:
                methodName="post";
                methodAnnotation= POST.class;
                needFormUrlEncoded=true;
                break;
            case PUT:
                methodName="put";
                methodAnnotation= PUT.class;
                needFormUrlEncoded=true;
                break;
        }

        String url=getUrl();
        StringBuilder urlBuilder=new StringBuilder();
        urlBuilder.append("\"");
        urlBuilder.append(url);
        urlBuilder.append("\"");
        AnnotationSpec annotationSpec=AnnotationSpec.builder(methodAnnotation)
                .addMember("value",urlBuilder.toString()).build();
        ClassName returnClassName= ClassName.get(RX,RX_OBSERVABLE_NAME);

       /* String responseSimpleName=annotatedClassSimpleName+RESPONSE_SUFFIX;
        ClassName responseClassName=ClassName.get(parentPackageName,responseSimpleName);*/
        ParameterizedTypeName returnTypeName=ParameterizedTypeName.get(returnClassName,responseClassName);
        MethodSpec.Builder method= MethodSpec
                .methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .addAnnotation(annotationSpec)
                .returns(returnTypeName);

        if (needFormUrlEncoded){
            method.addAnnotation(FormUrlEncoded.class);
        }

        ClassName paramClassName=ClassName.get("java.util","Map");
        TypeName stringTypeName= ClassName.get("java.lang","String");
        ParameterizedTypeName paramType=ParameterizedTypeName.get(paramClassName,stringTypeName,stringTypeName);
        ParameterSpec.Builder parameterSpec=ParameterSpec.builder(paramType,"paramsMap");
        if (needFormUrlEncoded){
            parameterSpec.addAnnotation(FieldMap.class);
        }else {
            parameterSpec.addAnnotation(QueryMap.class);
        }
        method.addParameter(parameterSpec.build());

        String interfaceName=prefix+API_SUFFIX;
        TypeSpec.Builder type=TypeSpec.interfaceBuilder(interfaceName)
                .addMethod(method.build())
                .addModifiers(Modifier.PUBLIC);
        JavaFile.builder(packageName,type.build()).build().writeTo(filer);

    }



    public void setParam(String prefix, String responseCanonicalName) {
        this.prefix=prefix;
        if (Utils.isEmpty(responseCanonicalName)){
            responseClassName=getViewClassName(parentPackageName,prefix);
        }else {
            responseClassName=ClassName.bestGuess(responseCanonicalName);
        }
    }
}
