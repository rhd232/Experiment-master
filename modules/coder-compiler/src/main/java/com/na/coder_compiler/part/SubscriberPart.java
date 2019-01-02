package com.na.coder_compiler.part;

import com.na.coder_compiler.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.wind.coder.annotations.Subscriber;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.na.coder_compiler.Utils.getNextObservableClassName;
import static com.na.coder_compiler.Utils.getViewClassName;

/**
 * Created by wind on 2018/6/3.
 */

public class SubscriberPart {
    public static final String SUBSCRIBER_SUFFIX="Subscriber";
    public static final String PACKAGE_SUFFIX=".subscriber";

    private String annotatedClassSimpleName;
    private String packageName;
    private String parentPackageName;
    private String prefix;
    private Subscriber subscriber;
    private ClassName viewClassName;
    private ClassName responseClassName;
    public SubscriberPart(TypeElement annotatedElement,Subscriber subscriber) {
        annotatedClassSimpleName=annotatedElement.getSimpleName().toString();
        this.subscriber=subscriber;
        //Subscriber subscriber=annotatedElement.getAnnotation(Subscriber.class);
        packageName=subscriber.packageName();
        parentPackageName= Utils.getPackageElement(annotatedElement).getQualifiedName().toString();
        if ("".equals(packageName)|| null==packageName){
            packageName=parentPackageName+ PACKAGE_SUFFIX;
        }


    }


    public void brewJava(Filer filer) throws IOException {

        MethodSpec.Builder constructor=MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(viewClassName,"mvpView")
                .addStatement(" super(mvpView)");

        String subscriberSimpleClassName=prefix+SUBSCRIBER_SUFFIX;
        ParameterizedTypeName parameterizedTypeName=ParameterizedTypeName.get(getNextObservableClassName(),
                viewClassName,responseClassName);


        TypeSpec.Builder typeSpec=TypeSpec.classBuilder(subscriberSimpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(parameterizedTypeName)
                .addMethod(constructor.build());

        JavaFile.builder(packageName,typeSpec.build()).build().writeTo(filer);
    }


    public String getPackageName() {
        return packageName;
    }



    public void setParam(String prefix,String viewCanonicalName ,String responseCanonicalName) {
        this.prefix=prefix;
        if (Utils.isEmpty(viewCanonicalName)){
            viewClassName=getViewClassName(parentPackageName,prefix);
        }else {
            viewClassName=ClassName.bestGuess(viewCanonicalName);
        }
        if (Utils.isEmpty(responseCanonicalName)){
            responseClassName=getViewClassName(parentPackageName,prefix);
        }else {
            responseClassName=ClassName.bestGuess(responseCanonicalName);
        }
    }
}
