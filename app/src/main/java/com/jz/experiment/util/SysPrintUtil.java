package com.jz.experiment.util;

import android.content.Context;
import android.print.PrintManager;

import com.jz.experiment.module.login.PdfPrintAdapter;

public class SysPrintUtil {

    public static void printPdf(Context context,String pdfPath) {

        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        PdfPrintAdapter myPrintAdapter = new PdfPrintAdapter(pdfPath);
        printManager.print("print_pdf", myPrintAdapter, null);

    }
}
