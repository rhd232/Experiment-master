package com.jz.experiment.di;


import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.wind.base.C;
import com.wind.base.request.UploadFileRequest;
import com.wind.base.response.UploadFileResponse;
import com.wind.base.usecase.UploadFileUsecase;
import com.wind.base.usecase.Usecase;
import com.wind.data.DbOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import rx.schedulers.Schedulers;

@Module
public class ProviderModule {
    private static ProviderModule instance=new ProviderModule();
    public static ProviderModule getInstance(){
        return instance;
    }
    @Singleton
    @Provides
    Retrofit provideRetrofit() {

        //网络请求日志
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
       // loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
       OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder
                .addInterceptor(loggingInterceptor);
               // .addInterceptor(new HeaderInterceptor());
        OkHttpClient client = httpClientBuilder.build();
        return new Retrofit.Builder().baseUrl(C.Api.getBaseUrl())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(
                        RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(client)
                .build();
    }

    public BriteDatabase getBriteDb(Context context){
        return provideBriteDb(DbOpenHelper.getInstance(context));
    }
    @Singleton
    @Provides
    public BriteDatabase provideBriteDb(@NonNull final DbOpenHelper dbOpenHelper) {
        final BriteDatabase briteDb =
                SqlBrite.create().wrapDatabaseHelper(dbOpenHelper, Schedulers.io());

        return briteDb;
    }


    @Singleton
    @Provides
    public Usecase<UploadFileRequest, UploadFileResponse> provideUploadFileUsecase(Retrofit retrofit) {
        return new UploadFileUsecase(retrofit);
    }

   /* @Provides
    @Singleton
    public LocationDataStore providLocationStore(){
        return new LocationDataStore(App.get());
    }
    @Singleton
    @Provides
    public Usecase<LocationRequest, LocationResponse> provideLocationUsecaseUsecase(LocationDataStore store) {
        return new LocationUsecase(store);
    }*/

}