package com.wind.base.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wind.base.C;
import com.wind.base.bean.CropBitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PictureUtil {
    public static final String TAG="PictureUtil";
    public static  Bitmap watermark;
    static {
        // watermark= BitmapFactory.decodeResource(MYApplication.getInstance().getResources(), R.drawable.photo_watermark);
    }


    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        boolean flag=true;
        while ( baos.toByteArray().length / 1024>500&&flag) {  //循环判断如果压缩后图片是否大于500kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            if (options<20){
                flag=false;
            }
        }
        Log.e(TAG,"options:"+options);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    /** 
     * 获取图片
     * @param srcPath  图片路径
     * @return 
     */  
    private static Bitmap getimage(String srcPath) {  
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;  
        Bitmap bitmap;
        BitmapFactory.decodeFile(srcPath,newOpts);
        newOpts.inSampleSize=computeSampleSize(newOpts,-1,1920*1080);
        Log.e(TAG,"inSampleSize:"+newOpts.inSampleSize);
        newOpts.inJustDecodeBounds = false;//解析图片
        bitmap=BitmapFactory.decodeFile(srcPath,newOpts);

        if (bitmap==null){
            return null;
        }
        //获取照片角度
        int degree=ImageUtil.getBitmapDegree(srcPath);
        if (degree!=0){
            //旋转照片,保证上传的照片
            bitmap=ImageUtil.rotateBitmapByDegree(bitmap,degree);
        }

        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
      
    /** 
     * �õ���ʱͼƬ·�� 
     * @param filePath 
     * @return  
     * @return 
     * @throws IOException  
     */  
    public static CropBitmap bitmapToPath(String filePath) throws IOException {
        CropBitmap cropBitmap=new CropBitmap();
        Bitmap bm = getimage(filePath);
        if (bm==null){
            return null;
        }



        //加入水印

        //bm=WatermarkMaker.createWaterMaskRightBottom(MYApplication.getInstance(),bm,watermark);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        cropBitmap.setWidth(bm.getWidth());
        cropBitmap.setHeight(bm.getHeight());
        String imgName=System.currentTimeMillis()+"";
        //String imgName=getfilepath(filePath);

        String sdPath= C.Value.TEMP_IMAGE_PATH;

        File parent =new File(sdPath);
        if(!parent.exists()){  
            parent.mkdirs();
        }

        File file =new File(parent,imgName);
        file.createNewFile();  
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(baos.toByteArray());  
        fos.flush();  
        fos.close();  
        baos.close();

        cropBitmap.setTempPath(sdPath+"/"+imgName);
       // degree=ImageUtil.getBitmapDegree(sdPath+"/"+imgName);
        return  cropBitmap;

    }  
  
      
    /** 
     *  
     * @param path 
     * @return 
     */  
    private static String getfilepath(String path){  
        return System.currentTimeMillis()+getExtensionName(path);  
    }  
      
      
    /* 
     * ��ȡ�ļ���չ�� 
     */  
   private static String getExtensionName(String filename) {  
        if ((filename != null) && (filename.length() > 0)) {  
            int dot = filename.lastIndexOf('.');  
            if ((dot > -1) && (dot < (filename.length() - 1))) {  
                return filename.substring(dot, filename.length());  
            }  
        }  
        return filename;  
    }  
  
     
   /** 
    * ɾ����ʱ�ļ� 
    * @param imgs 
    */  
   public static void deleteImgTmp(List<String> imgs){
         
       for (String string : imgs) {  
        File file=new File(string);  
        if(file.exists()){  
            file.delete();  
        }  
    }  
         
   }



    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

}  