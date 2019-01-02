package com.na.coder_compiler.part;

import com.na.coder_compiler.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.na.coder_compiler.Utils.getApiProxyClassName;
import static com.na.coder_compiler.Utils.getInjectClassName;
import static com.na.coder_compiler.Utils.getRequestClassName;
import static com.na.coder_compiler.Utils.getResponseClassName;

/**
 * Created by wind on 2018/9/5.
 */

public class RepositoryPart {
    public static final String PAGEREPOSITORY_SUFFIX="PageRepository";
    final String PACKAGE_SUFFITX=".api";
    private String parentPackageName;
    private String packageName;
    private String prefix;
    private ClassName requestClassName;
    private ClassName responseClassName;
    private ApiProxyPart apiProxyPart;
    private static final ClassName RX_OBSERVABLE_TYPENAME=
            ClassName.get("rx","Observable");
    private static final ClassName PSPAGE_TYPENAME=
            ClassName.get("com.wind.base.http.page","PsPage");
    private static final ClassName PAGEREPOSITORY_TYPENAME=
            ClassName.get("com.wind.base.repository","PageRepository");
    public RepositoryPart(TypeElement annotatedElement){

        String parentPackageName= Utils.getPackageElement(annotatedElement).getQualifiedName().toString();
        packageName=parentPackageName+PACKAGE_SUFFITX;
    }
    public void setParam(String prefix, String requestCanonicalName, String responseCanonicalName) {
        this.prefix=prefix;

        if (Utils.isEmpty(requestCanonicalName)){
            requestClassName=getRequestClassName(parentPackageName,prefix);
        }else {
            requestClassName= ClassName.bestGuess(requestCanonicalName);
        }

        if (Utils.isEmpty(responseCanonicalName)){
            responseClassName=getResponseClassName(parentPackageName,prefix);
        }else {
            responseClassName=ClassName.bestGuess(responseCanonicalName);
        }
    }
    public void brewJava(Filer filer) throws IOException {
        ClassName apiProxyClassName=getApiProxyClassName(apiProxyPart.getPackageName(),prefix);
        String simpleName=apiProxyClassName.simpleName();
        String paramName=simpleName.substring(0,1).toLowerCase()+simpleName.substring(1,simpleName.length());
        String fieldName="m"+simpleName;

        FieldSpec.Builder field=FieldSpec.builder(apiProxyClassName,fieldName,Modifier.PRIVATE);

        MethodSpec.Builder constructor= MethodSpec.constructorBuilder()
                .addParameter(apiProxyClassName,paramName)
                .addAnnotation(getInjectClassName())
                .addStatement("super("+paramName+")");

       /* ParameterizedTypeName returnTypeName=ParameterizedTypeName.get(RX_OBSERVABLE_TYPENAME,
                responseClassName);
        MethodSpec.Builder loadPageMethod=MethodSpec.methodBuilder("loadPage")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(requestClassName,"request")
                .addStatement("return "+fieldName+".get(request)")

                .returns(returnTypeName);*/



        String pageRepositorySimpleClassName=prefix+PAGEREPOSITORY_SUFFIX;

        ParameterizedTypeName parameterizedTypeName=ParameterizedTypeName.get(PSPAGE_TYPENAME,
                requestClassName,responseClassName);
        ParameterizedTypeName interfaceParameterizedTypeName=ParameterizedTypeName.get(PAGEREPOSITORY_TYPENAME,
                requestClassName,responseClassName);
        TypeSpec typeSpec=TypeSpec.classBuilder(pageRepositorySimpleClassName)
                .superclass(parameterizedTypeName)
                .addSuperinterface(interfaceParameterizedTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor.build())
                .addField(field.build())
                .build();
        JavaFile.builder(packageName,typeSpec).build().writeTo(filer);

    }
    public void setAssociatedProxyApi(ApiProxyPart apiProxyPart) {
        this.apiProxyPart = apiProxyPart;
    }

    public String getPackageName() {
        return packageName;
    }
    /*
     public class SquarePageRepository extends PsPage<SquareRequest, SquareResponse> {


    public SquarePageRepository(SquareApiProxy pageApi) {
        super(pageApi);
    }


}
     */
}
