package com.anitoa.util;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;

public class AnitoaLogUtil {

    public static boolean sDebug;
    public static final String APP_FOLDER= Environment
                                            .getExternalStorageDirectory()
                                            .getAbsolutePath()+"/anitoa/";

    public static final String IMAGE_DATA=APP_FOLDER+"imageData/";

    public static void writeFileLog(final String txt, ExecutorService executorService) {
        if (!sDebug){
            return;
        }
        System.out.println(txt);
        if (executorService==null) {
            File file = getLogFile();
            writeToFile(file, txt);
        }else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    File file = getLogFile();
                    writeToFile(file, txt);
                }
            });
        }

    }

    public static void writeToFile(File file, String txt) {

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!file.exists()) {
                file.createNewFile();
                fos = new FileOutputStream(file);
            } else {
                fos = new FileOutputStream(file, true);
            }
            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(txt); //写入内容
            osw.write("\r\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        } finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static void removeLogFile() {
        File file = getLogFile();
        if (file.exists()) {
            file.delete();
        }
    }

    public static File getLogFile() {
        String fileName = "communicate_log.txt";
        return getOrCreateFile(fileName);
    }
    public static void writeFileLog(String txt) {
        //拼上一个发送时间
        long milliTime=new Date().getTime();

        txt=getDateTime(milliTime)+" "+txt;
        writeFileLog(txt,null);

    }
    public static File getOrCreateFile(String filename) {
        File file = new File(IMAGE_DATA, filename);
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

    public static String getDateTime(long milliTime){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }
}
