package com.jz.experiment.util;

import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.text.TextUtils;
import android.widget.ScrollView;

import com.anitoa.util.ThreadUtil;
import com.jz.experiment.module.report.OnPdfGenerateListener;
import com.jz.experiment.widget.A4PageLayout;
import com.wind.base.utils.A4Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PdfGenerator {


    public static void generatePdf(final A4PageLayout layout_a4,final String pdfName,
                                   final OnPdfGenerateListener listener){
        generatePdf(layout_a4,pdfName)
                .subscribeOn(Schedulers.io())
                //.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String path) {
                        if (!TextUtils.isEmpty(path)) {
                            listener.onGeneratePdfSuccess(path);
                        } else {
                            listener.onGeneratePdfError();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        generatePdf(layout_a4,pdfName,listener);

                    }
                });
    }

    public static Observable<String> generatePdf(final A4PageLayout layout_a4,final String pdfName
                                                 ) {
        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                ThreadUtil.sleep(500);
                PdfDocument document = new PdfDocument();
                int width = layout_a4.getWidth();// AppUtil.getScreenWidth(getActivity());
                int height = 0;// AppUtil.getScreenHeight(getActivity());
                height = A4Util.getA4Height(layout_a4.getContext());

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                        .Builder(width, height, 1)
                        .create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                ScrollView sv= (ScrollView) layout_a4.getChildAt(0);
                if (sv != null) {
                    sv.draw(canvas);
                }


                document.finishPage(page);
                File file = new File(DataFileUtil.getPdfFilePath(pdfName));

                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    document.writeTo(outputStream);
                    subscriber.onNext(file.getAbsolutePath());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                document.close();



            }
        });


    }
}
