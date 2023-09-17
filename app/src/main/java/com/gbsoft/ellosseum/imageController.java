package com.gbsoft.ellosseum;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class imageController {

    private imageController() {
    }

    private static class LazyHolder {
        public static final imageController INSTANCE = new imageController();
    }

    public static imageController getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getRealPathFromURI(ContentResolver contentResolver, Uri uri){
        String buildname = Build.MANUFACTURER;
        if(buildname.equals("Xiaomi")){
            return uri.getPath();
        }

        int columnIndex = 0;
        String[] proj = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(uri, proj, null, null, null);
        if(cursor.moveToFirst()){
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(columnIndex);
    }


    /**
     * 실제 경로
     */
    public String getPath(Context context, Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
            return null;
        // 파일 경로 만듦
        String filePath = context.getApplicationInfo().dataDir + File.separator + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            // 매개변수로 받은 uri를 통해 이미지에 필요한 데이터를 불러들인다
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            // 이미지 데이터를 다시 내보내면서 file 객체에 만들었던 경로를 이용한다.
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException ignore) {
            return null;
        }
        return file.getAbsolutePath();
    }

    /**
     * 이미지 회전1 ( 원본 이미지가 회전되어있는지 확인 후 정상적으로 보이게 회전)
     */
    public Bitmap getRotateBitmap(Bitmap bitmap, String path) throws IOException {
        Bitmap result = null;
        if (bitmap != null) {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    result = getRotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    result = getRotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    result = getRotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    result = getRotateImage(bitmap, 0);
            }
        }
//        bitmap.recycle();

        return compressBitmap(result, 50);
    }

    /**
     * 이미지 회전2 ( 받아온 degree 만큼 이미지를 회전시킨다)
     */
    public Bitmap getRotateImage(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * bitmap 압축
     */
    public Bitmap compressBitmap(Bitmap bitmap, int rate) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, rate, stream);
        byte[] byteArray = stream.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        bitmap.recycle();
        return compressedBitmap;
    }


    /**
     * Bitmap을 File로 변경
     */
    public File bitmapToFile(Bitmap bitmap, String path) {
        // create a file to write bitmap data
        File file = null;
        try {
            file = new File(path);
            file.createNewFile();

            // Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);  // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            // write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }



}
