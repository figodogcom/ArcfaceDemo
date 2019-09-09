package com.arcsoft.arcfacedemo.searcher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Part;

public class CommaTakeSercher extends YZWSearcher {



    public CommaTakeSercher(){

    }

    void search(Bitmap bitmap){
//        @Part("store_id") ResquestBody store_id,
//        @Part("door_id") ResquestBody doorId,
//        @Part("door_side") ResquestBody doorSide,
//        @Part("face_photo\";filename=\"face_photo.jpeg") ResquestBody facePhoto
        RequestBody requestApiKey = RequestBody.create(MediaType.parse("multipart/form-data"), store_id);
        RequestBody requestApiKey2 = RequestBody.create(MediaType.parse("multipart/form-data"), doorId);
        RequestBody requestApiKey3 = RequestBody.create(MediaType.parse("multipart/form-data"), doorSide);
// 创建RequestBody，传入参数："multipart/form-data"，File
        String path = "/face";
        File imgFile = new File(path);

        try {
            saveBitmapToJPG(bitmap,imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody requestImgFile = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);
// 创建MultipartBody.Part，用于封装文件数据
        MultipartBody.Part requestImgPart = MultipartBody.Part.createFormData("img_file", imgFile.getName(), requestImgFile);

    }


    public static void saveBitmapToJPG(Bitmap bitmap, File file) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(file);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }


}
