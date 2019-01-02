package com.na.coder_compiler.part;

import com.na.coder_compiler.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.wind.coder.annotations.Usecase;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import retrofit2.Retrofit;

import static com.na.coder_compiler.Utils.getApiClassName;
import static com.na.coder_compiler.Utils.getInjectClassName;
import static com.na.coder_compiler.Utils.getRequestClassName;
import static com.na.coder_compiler.Utils.getResponseClassName;


/**
 * Created by wind on 2018/6/3.
 */

public class UsecasePart {
    private static final String USECASE_SUFFIX="Usecase";
    private static final String PACKAGE_SUFFIX=".usecase";
    private static final ClassName SUPERCLASS_TYPENAME=
            ClassName.get("com.wind.base.usecase","RetrofitUsecase");
    private static final ClassName PAGEUSECASE_TYPENAME=
            ClassName.get("com.wind.base.usecase","PageUsecase");
    private static final ClassName RX_OBSERVABLE_TYPENAME=
            ClassName.get("rx","Observable");

    private boolean page;//
    private String annotatedClassSimpleName;
    private ApiPart apiPart;
    private String packageName;
    private String parentPackageName;
    private String prefix;
    private ClassName requestClassName;
    private ClassName responseClassName;
    private RepositoryPart repositoryPart;
    public UsecasePart(TypeElement annotatedElement,Usecase usecase){
        annotatedClassSimpleName=annotatedElement.getSimpleName().toString();
       // Usecase usecase=annotatedElement.getAnnotation(Usecase.class);
        packageName=usecase.packageName();
        parentPackageName= Utils.getPackageElement(annotatedElement).getQualifiedName().toString();
        if ("".equals(packageName)|| null==packageName){
            packageName= parentPackageName+PACKAGE_SUFFIX;
        }
    }

    public void brewJava(Filer filer) throws IOException {
        if (page){
            brewPageUsecase(filer);
        }else {
            brewRetrofitUsecase(filer);
        }

    }
    /*

        public class ListUsecase extends PageUsecase<ListRequest,ListResponse> {
            @Inject
            public ListUsecase(PageRepository<ListRequest,ListResponse> pageRepository) {
                super(pageRepository);
            }
        }
     */
    private void brewPageUsecase(Filer filer) throws IOException{
        ClassName repositoryClassName= Utils.getRepositoryClassName(repositoryPart.getPackageName(),prefix);
        ClassName injectClassName=getInjectClassName();
        MethodSpec.Builder constructor=MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(injectClassName)
                .addParameter(repositoryClassName,"pageRepository")
                .addStatement("super(pageRepository)");

        String className=prefix+ USECASE_SUFFIX;

        ParameterizedTypeName parameterizedTypeName=ParameterizedTypeName.get(PAGEUSECASE_TYPENAME,
                requestClassName,responseClassName);
        TypeSpec typeSpec=TypeSpec.classBuilder(className)
                .superclass(parameterizedTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor.build())
                .build();
        JavaFile.builder(packageName,typeSpec).build().writeTo(filer);
    }


    private void brewRetrofitUsecase(Filer filer) throws IOException{
        ParameterizedTypeName returnTypeName=ParameterizedTypeName.get(RX_OBSERVABLE_TYPENAME,
                responseClassName);

        MethodSpec.Builder method=MethodSpec.methodBuilder("buildUsecaseObservable")
                .addParameter(requestClassName,"request")
                .addModifiers(Modifier.PUBLIC)
                .returns(returnTypeName);
        ClassName apiClassName=getApiClassName(apiPart.getPackageName(),prefix);
        method.addStatement("$T api = mRetrofit.create("+apiClassName+".class)",apiClassName);
        String apiMethodName=apiPart.getMethod().getName();

        ClassName mapTransformerClassName=ClassName.get("com.wind.base.utils","MapTransformer");
        method.addStatement("return api."+apiMethodName+"($T.transformObject2Map(request)"+")",mapTransformerClassName);


        ClassName retrofitClassName=ClassName.get(Retrofit.class);
        ClassName injectClassName=getInjectClassName();
        MethodSpec.Builder constructor=MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(injectClassName)
                .addParameter(retrofitClassName,"retrofit")
                .addStatement(" super(retrofit)");



        String className=prefix+ USECASE_SUFFIX;

        ParameterizedTypeName parameterizedTypeName=ParameterizedTypeName.get(SUPERCLASS_TYPENAME,
                requestClassName,responseClassName);
        TypeSpec typeSpec=TypeSpec.classBuilder(className)
                .superclass(parameterizedTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method.build())
                .addMethod(constructor.build())
                .build();
        JavaFile.builder(packageName,typeSpec).build().writeTo(filer);
    }

    public void setAssociatedApi(ApiPart api){
        this.apiPart=api;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setAssociatedRepository(RepositoryPart repositoryPart){
        this.repositoryPart=repositoryPart;
    }

    public void setParam(String prefix, String requestCanonicalName, String responseCanonicalName,boolean page) {
        this.prefix=prefix;
        this.page=page;


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
}
