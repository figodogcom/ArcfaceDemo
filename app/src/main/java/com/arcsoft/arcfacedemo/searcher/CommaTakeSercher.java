package com.arcsoft.arcfacedemo.searcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import com.arcsoft.arcfacedemo.common.MyApi;
import com.arcsoft.arcfacedemo.common.MyApiService;
import com.arcsoft.arcfacedemo.common.MyApplication;
import com.arcsoft.arcfacedemo.response.TakeFaceQuickSearch;
import com.commaai.commons.service.Body;
import com.commaai.commons.service.request.RequestParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
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

    Context context;

    public CommaTakeSercher(Context context){
        this.context = context;
    }


    @Override
    public void search(final Bitmap bitmap) {
        super.search(bitmap);

        MyApi service = getService();


        Call<Body<TakeFaceQuickSearch>> call = service.takeFaceQuickSearch(
                RequestBody.create(MediaType.parse("text/plain"), store_id),
                RequestBody.create(MediaType.parse("text/plain"), doorId),
                RequestBody.create(MediaType.parse("text/plain"), doorSide),
                new BitmapRequestBody(bitmap)
        );




        call.enqueue(new retrofit2.Callback<Body<TakeFaceQuickSearch>>() {
            @Override
            public void onResponse(Call<Body<TakeFaceQuickSearch>> call, Response<Body<TakeFaceQuickSearch>> response) {
                if(response.isSuccessful()){
                    Body<TakeFaceQuickSearch> body = response.body();
                    if(body.code == 200){
                        Bitmap bitmap = returnBitmap(body.data.getAvatarUrl());

                        callback.onSearchSuccessCallback(bitmap,body.data.nickName);
                    }else {
                        callback.onSearchFailCallback();
                    }




                }else {
                    callback.onSearchFailCallback();
                }

            }

            @Override
            public void onFailure(Call<Body<TakeFaceQuickSearch>> call, Throwable t) {
                callback.onSearchFailCallback();
                Log.i(TAG, "onFailure: " + t.getMessage());
            }
        });


    }



    private MyApi getService() {
        MyApi service = ((MyApplication)  ((Activity)context).getApplication()).getService();
        return service;
    }






    @Override
    public void setCallback(YZWSearcher.Callback callback) {
        super.setCallback(callback);
    }



    private Bitmap returnBitmap(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;

    }



}
