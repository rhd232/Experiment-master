package com.jz.experiment.module.report;

public interface OnPdfGenerateListener {
    void onGeneratePdfSuccess(String path);
    void onGeneratePdfError();
}
