package com.wind.base.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.wind.base.C;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    @SuppressLint({"NewApi"})
    public static String getPath(Context context, Uri uri) {
        if(uri.toString().startsWith("content://com.google.android.apps.photos.content")) {
            return null;
        } else {
            boolean isKitKat = Build.VERSION.SDK_INT >= 19;
            if(isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                String docId;
                String[] split;
                String type;
                if(isExternalStorageDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    split = docId.split(":");
                    type = split[0];
                    if("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else {
                    if(isDownloadsDocument(uri)) {
                        docId = DocumentsContract.getDocumentId(uri);
                        Uri split1 = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId).longValue());
                        return getDataColumn(context, split1, (String)null, (String[])null);
                    }

                    if(isMediaDocument(uri)) {
                        docId = DocumentsContract.getDocumentId(uri);
                        split = docId.split(":");
                        type = split[0];
                        Uri contentUri = null;

                        if("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{split[1]};
                        return getDataColumn(context, contentUri, "_id=?", selectionArgs);
                    }
                }
            } else {
                if("content".equalsIgnoreCase(uri.getScheme())) {
                    return getDataColumn(context, uri, (String)null, (String[])null);
                }

                if("file".equalsIgnoreCase(uri.getScheme())) {
                    return uri.getPath();
                }
            }

            return null;
        }
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{"_data"};

        String var9="";
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if(cursor == null || !cursor.moveToFirst()) {
                return null;
            }

            //int column_index = cursor.getColumnIndexOrThrow("_data");
            int column_index = cursor.getColumnIndex("_data");
            if (column_index!=-1){

                var9 = cursor.getString(column_index);
            }else {
                //不存在此column_index， android N报错
                String fileName = getFileName(uri);
                File dir=new File(C.Value.TEMP_FOLDER);
                if (!dir.exists()){
                    dir.mkdirs();
                }else {
                    for (File file:dir.listFiles()){
                        file.delete();
                    }
                }

                if (!TextUtils.isEmpty(fileName)) {
                    File copyFile = new File(dir, fileName);
                    copy(context, uri, copyFile);
                    var9= copyFile.getAbsolutePath();
                }
            }

        } finally {
            if(cursor != null) {
                cursor.close();
            }

        }

        return var9;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
