package com.na.coder_compiler.part;

import com.na.coder_compiler.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.wind.coder.annotations.Presenter;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.na.coder_compiler.Utils.getExecutePresenterClassName;
import static com.na.coder_compiler.Utils.getInjectClassName;
import static com.na.coder_compiler.Utils.getSubscriberClassName;
import static com.na.coder_compiler.Utils.getUsecaseClassName;
import static com.na.coder_compiler.Utils.getViewClassName;

/**
 * Created by wind on 2018/6/3.
 */

public class PresenterPart {
    public static final String PRESENTER_SUFFIX="Presenter";
    public static final String PACKAGENAME_SUFFIX=".presenter";

    private String annotatedClassSimpleName;
    private String packageName;
    private String parentPackageName;
    private UsecasePart associatedUsecase;
    private SubscriberPart associatedSubscriber;
    private String prefix;
    private ClassName viewClassName;
    public PresenterPart(TypeElement annotatedElement,Presenter presenter) {
        annotatedClassSimpleName=annotatedElement.getSimpleName().toString();


        //Presenter presenter=annotatedElement.getAnnotation(Presenter.class);
        packageName=presenter.packageName();
        parentPackageName= Utils.getPackageElement(annotatedElement).getQualifiedName().toString();
        if ("".equals(packageName)|| null==packageName){
            packageName= parentPackageName+PACKAGENAME_SUFFIX;
        }
    }


    public void brewJava(Filer filer) throws IOException {

        ClassName usecaseClassName=getUsecaseClassName(associatedUsecase.getPackageName(),prefix);
        String simpleName=usecaseClassName.simpleName();
        String fieldName="m"+simpleName;

        String paramName=simpleName.substring(0,1).toLowerCase()+simpleName.substring(1,simpleName.length());
        MethodSpec.Builder constructor=MethodSpec.constructorBuilder()
                .addAnnotation(getInjectClassName())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(usecaseClassName,paramName)
                .addStatement("this."+fieldName+"="+paramName);

        String subscriberSimpleClassName=prefix+PRESENTER_SUFFIX;
        ParameterizedTypeName parameterizedTypeName=ParameterizedTypeName.get(getExecutePresenterClassName(),
                viewClassName);


        MethodSpec.Builder attachViewMethod=MethodSpec.methodBuilder("attachView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(viewClassName,"view")
                .addStatement("super.attachView(view)")
                .addStatement("$T subscriber=new $T(view)",
                        getSubscriberClassName(associatedSubscriber.getPackageName(),prefix),
                        getSubscriberClassName(associatedSubscriber.getPackageName(),prefix))
                .addStatement("$T component=new $T(subscriber,"+fieldName+")", Utils.getUsecaseComponentClassName(),
                        Utils.getUsecaseComponentClassName())
                .addStatement("manager.addUsecaseCompoment(component)");



        FieldSpec.Builder field=FieldSpec.builder(usecaseClassName,fieldName,Modifier.PRIVATE);
        TypeSpec.Builder typeSpec=TypeSpec.classBuilder(subscriberSimpleClassName)
                .superclass(parameterizedTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor.build())
                .addMethod(attachViewMethod.build())
                .addField(field.build());



        JavaFile.builder(packageName,typeSpec.build()).build().writeTo(filer);
    }


    public void setAssociatedUsecase(UsecasePart associatedUsecase) {
        this.associatedUsecase = associatedUsecase;
    }

    public UsecasePart getAssociatedUsecase() {
        return associatedUsecase;
    }

    public void setAssociatedSubscriber(SubscriberPart associatedSubscriber) {
        this.associatedSubscriber = associatedSubscriber;
    }

    public SubscriberPart getAssociatedSubscriber() {
        return associatedSubscriber;
    }



    public void setParam(String prefix, String viewCanonicalName) {
        this.prefix=prefix;
        if (Utils.isEmpty(viewCanonicalName)){
            viewClassName=getViewClassName(parentPackageName,prefix);
        }else {
            viewClassName=ClassName.bestGuess(viewCanonicalName);
        }
    }
}
