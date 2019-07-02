package com.jz.experiment.util;

import android.content.Context;

import com.jz.experiment.R;
import com.wind.base.C;
import com.wind.base.utils.DateUtil;
import com.wind.data.expe.bean.HistoryExperiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataFileUtil {

    public static String getPdfFileName(Context context,HistoryExperiment experiment, boolean melt) {
        //String name = "变温扩增";
        String name=context.getString(R.string.setup_mode_dt);
        if (melt) {
             name=context.getString(R.string.setup_mode_melting);
        }
        return DateUtil.get(experiment.getMillitime(), "yyyy_MM_dd_HH_mm_ss") + name + ".pdf";
    }

    public static File getDtImageDataFile(HistoryExperiment experiment) {
        String fileName = DateUtil.get(experiment.getMillitime(), "yyyy_MM_dd_HH_mm_ss") + "_dt.txt";
        //String filePath = C.Value.IMAGE_DATA + fileName;
        return getOrCreateFile(fileName);
    }

    public static File getMeltImageDateFile(HistoryExperiment experiment) {
        String fileName = DateUtil.get(experiment.getMillitime(), "yyyy_MM_dd_HH_mm_ss") + "_melting.txt";
        //String filePath = C.Value.IMAGE_DATA + fileName;
        return getOrCreateFile(fileName);
    }

    public static File getDtImageDataSourceFile(HistoryExperiment experiment) {
        String fileName = DateUtil.get(experiment.getMillitime(), "yyyy_MM_dd_HH_mm_ss") + "_dtsource.txt";
        return getOrCreateFile(fileName);
    }

    public static File getMeltImageDataSourceFile(HistoryExperiment experiment) {
        String fileName = DateUtil.get(experiment.getMillitime(), "yyyy_MM_dd_HH_mm_ss") + "_meltsource.txt";
        return getOrCreateFile(fileName);
    }

    public static boolean deleteFile(String filename){
        File file = new File(C.Value.IMAGE_DATA, filename);
        if (file.exists()){
            return file.delete();
        }
        return true;
    }
    public static File getOrCreateFile(String filename) {
        File file = new File(C.Value.IMAGE_DATA, filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    public static String getPdfFilePath(String pdfName) {
        try {
            String dir = C.Value.REPORT_FOLDER;
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File pdfFile = new File(dir, pdfName);
            if (!pdfFile.exists()) {
                pdfFile.createNewFile();
            }
            return pdfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }


    public static List<String> covertToList(File file) {
        List<String> newList = new ArrayList<>();
        try {
            //File file = new File(dataFile);
            int count = 0;
            if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    if (!"".equals(lineTxt)) {
                        //   String reds = lineTxt.split("\\+")[0];
                        newList.add(count, lineTxt);
                        count++;
                    }
                }
                isr.close();
                br.close();
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    public static void createAppFolder() {
        String appDir = C.Value.APP_FOLDER;
        File dir = new File(appDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dirTrim = new File(C.Value.TRIM_FOLDER);
        if (!dirTrim.exists()) {
            dirTrim.mkdirs();
        }
    }





}
