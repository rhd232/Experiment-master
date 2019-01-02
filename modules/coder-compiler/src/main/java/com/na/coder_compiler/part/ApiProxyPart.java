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

import static com.na.coder_compiler.Utils.APIPROXY_SUFFIX;
import static com.na.coder_compiler.Utils.getApiClassName;
import static com.na.coder_compiler.Utils.getInjectClassName;
import static com.na.coder_compiler.Utils.getRequestClassName;
import static com.na.coder_compiler.Utils.getResponseClassName;

/**
 * Created by wind on 2018/9/5.
 */

public class ApiProxyPart {
    private String annotatedClassSimpleName;
    final String PACKAGE_SUFFITX=".api";
    private ApiPart apiPart;
    private String prefix;
    private String packageName;
    private ClassName requestClassName;
    private ClassName responseClassName;
    private String parentPackageName;
    private static final ClassName RX_OBSERVABLE_TYPENAME=
            ClassName.get("rx","Observable");
    private static final ClassName IPAGE_TYPENAME=
            ClassName.get("com.wind.base.api","IPageApi");
    public ApiProxyPart(TypeElement annotatedElement) {
        annotatedClassSimpleName = annotatedElement.getSimpleName().toString();
         parentPackageName= Utils.getPackageElement(annotatedElement).getQualifiedName().toString();
         packageName=parentPackageName+PACKAGE_SUFFITX;
    }

    /*
    public class LoginApiProxy implements IPageApi<LoginRequest,LoginResponse> {
 private LoginApi api;
 @Inject
  public LoginApiProxy(LoginApi api){
    this.api=api;
  }
  @Override
  public Observable<LoginResponse> get(LoginRequest request) {
    return api.post(MapTransformer.transformObject2Map(request));
  }

}
     */
    public void brewJava(Filer filer) throws IOException {
        ClassName apiClassName=getApiClassName(apiPart.getPackageName(),prefix);
        String simpleName=apiClassName.simpleName();
        String paramName=simpleName.substring(0,1).toLowerCase()+simpleName.substring(1,simpleName.length());
        String filedName="m"+simpleName;
        FieldSpec.Builder field=FieldSpec
                .builder(apiClassName,filedName,Modifier.PRIVATE);

        MethodSpec.Builder constructor= MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(apiClassName,paramName)
                .addStatement("this."+filedName+"="+paramName)
                .addAnnotation(getInjectClassName());
        ParameterizedTypeName returnTypeName=ParameterizedTypeName.get(RX_OBSERVABLE_TYPENAME,
                responseClassName);
        MethodSpec.Builder getMethod=MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(requestClassName,"request")
                .returns(returnTypeName);
        String apiMethodName=apiPart.getMethod().getName();

        ClassName mapTransformerClassName=ClassName.get("com.wind.base.utils","MapTransformer");
        getMethod.addStatement("return "+filedName+"."+apiMethodName+"($T.transformObject2Map(request)"+")",mapTransformerClassName);


        String apiProxySimpleClassName=prefix+APIPROXY_SUFFIX;

        ParameterizedTypeName parameterizedTypeName=ParameterizedTypeName.get(IPAGE_TYPENAME,
                requestClassName,responseClassName);
        TypeSpec typeSpec=TypeSpec.classBuilder(apiProxySimpleClassName)
                .addSuperinterface(parameterizedTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor.build())
                .addField(field.build())
                .addMethod(getMethod.build())
                .build();
        JavaFile.builder(packageName,typeSpec).build().writeTo(filer);

    }
    public void setAssociatedApi(ApiPart apiPart) {
        this.apiPart = apiPart;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setParam(String prefix, String requestCanonicalName, String responseCanonicalName) {
        this.prefix=prefix;


        if (Utils.isEmpty(requestCanonicalName)){
            requestClassName=getRequestClassName(parentPackageName,prefix);
        }else {
            requestClassName=ClassName.bestGuess(requestCanonicalName);
        }

        if (Utils.isEmpty(responseCanonicalName)){
            responseClassName=getResponseClassName(parentPackageName,prefix);
        }else {
            responseClassName=ClassName.bestGuess(responseCanonicalName);
        }

    }

    /**
     *
     * public class ListPageApiProxy implements IPageApi<ListRequest,ListResponse> {
     private ListPageApi api;
     @Inject public ListPageApiProxy(ListPageApi api){
     this.api=api;
     }
     @Override public Observable<ListResponse> get(ListRequest request) {
     return api.get(MapTransformer.transformObject2Map(request));
     }
     }
     */
}
