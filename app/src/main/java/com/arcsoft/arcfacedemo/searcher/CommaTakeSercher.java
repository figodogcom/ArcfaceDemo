package com.arcsoft.arcfacedemo.searcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.arcsoft.arcfacedemo.common.MyApi;
import com.arcsoft.arcfacedemo.common.MyApiService;
import com.arcsoft.arcfacedemo.common.MyApplication;
import com.arcsoft.arcfacedemo.common.Util;
import com.arcsoft.arcfacedemo.response.TakeFaceQuickSearch;
import com.commaai.commons.service.Body;
import com.commaai.commons.service.request.RequestParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Part;

public class CommaTakeSercher extends YZWSearcher {
    private static final String TAG = CommaTakeSercher.class.getCanonicalName();
    //doorside 1进门2出门
    String store_id = "2";
    String doorId = "1";
    String doorSide = "1";
    byte[] bytes ;

    Context context;

    public CommaTakeSercher(Context context){
        this.context = context;
    }


    @Override
    public void search(Bitmap bitmap) {
        super.search(bitmap);

        //        @Part("store_id") ResquestBody store_id,
//        @Part("door_id") ResquestBody doorId,
//        @Part("door_side") ResquestBody doorSide,
//        @Part("face_photo\";filename=\"face_photo.jpeg") ResquestBody facePhoto

        MyApi service = getService();

//        File file = savePNG_After(bitmap,"/files/yzw.jpeg");

        Call<Body<TakeFaceQuickSearch>> call = service.takeFaceQuickSearch(
                RequestBody.create(MediaType.parse("text/plain"), store_id),
                RequestBody.create(MediaType.parse("text/plain"), doorId),
                RequestBody.create(MediaType.parse("text/plain"), doorSide),
                RequestBody.create(MediaType.parse("image/jpeg"), bitmap.)
//                RequestBody.create(MediaType.parse("image/jpeg"), bytes)
        );


        call.enqueue(new retrofit2.Callback<Body<TakeFaceQuickSearch>>() {
            @Override
            public void onResponse(Call<Body<TakeFaceQuickSearch>> call, Response<Body<TakeFaceQuickSearch>> response) {
                Log.i(TAG, "onResponse: ");
            }

            @Override
            public void onFailure(Call<Body<TakeFaceQuickSearch>> call, Throwable t) {
                Log.i(TAG, "onFailure: ");
            }
        });


    }




    public static File savePNG_After(Bitmap bitmap, String name) {
        File file = new File(name);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
                return file;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
//转jpg就把Bitmap.CompressFormat.PNG改成Bitmap.Compressformat.JPEG

    private MyApi getService() {
        MyApi service = ((MyApplication)  ((Activity)context).getApplication()).getService();
        return service;
    }


}
