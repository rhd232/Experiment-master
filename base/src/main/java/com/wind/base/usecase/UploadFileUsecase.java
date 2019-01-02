package com.wind.base.usecase;

import android.text.TextUtils;

import com.wind.base.api.UploadApi;
import com.wind.base.bean.UploadFile;
import com.wind.base.request.UploadFileRequest;
import com.wind.base.response.UploadFileResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import retrofit2.Retrofit;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by wind on 2017/3/1.
 */

public class UploadFileUsecase extends Usecase<UploadFileRequest,UploadFileResponse> {

    private Retrofit mRetrofit;
    private AtomicInteger mFailed;
    private int mBackendCallsCount;
    @Inject
    public UploadFileUsecase(Retrofit retrofit){
        this.mRetrofit=retrofit;
        mFailed=new AtomicInteger(0);
    }
    @Override
    protected Observable<UploadFileResponse> buildUsecaseObservable(UploadFileRequest request) {
        mFailed.set(0);

        List<Observable<UploadFileResponse>> observables = new ArrayList<>();

        List<UploadFile> fileList= request.getUploadFileList();
        mBackendCallsCount=fileList.size();

        for (int i=0;i<fileList.size();i++){
            UploadApi api=new UploadApi();
            final UploadFile uploadFile=fileList.get(i);
            if (TextUtils.isEmpty(uploadFile.getPath())){
                mBackendCallsCount--;
                continue;
            }
            Observable<UploadFileResponse> observable=api
                    .upload(uploadFile)
                    .onErrorResumeNext(new Func1<Throwable, Observable<? extends UploadFileResponse>>() {
                        @Override
                        public Observable<? extends UploadFileResponse> call(Throwable throwable) {
                            int fails = mFailed.incrementAndGet();

                            if (fails == mBackendCallsCount) {
                                uploadFile.setUploadState(UploadFile.STATE_UPLOAD_ERROR);
                                return Observable.error(throwable); // All failed so emmit error
                            } else {
                                return Observable.empty(); // Not all failed, so ignore this error and emit nothing
                            }

                        }
                    });
            observables.add(observable);
        }
        // return the created Observable
        return Observable.merge(observables);
    }
}
