package com.arcsoft.arcfacedemo.common;

import com.arcsoft.arcfacedemo.response.Body;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MyApi {
    @FormUrlEncoded
    @POST("take/face/quick_search")
    Call<Body<>> takeFaceQuickSearch(//接口参数
                                           @Part("store_id") ResquestBody store_id,
                                           @Part("door_id") ResquestBody doorId,
                                           @Part("door_side") ResquestBody doorSide,
                                           @Part("face_photo\";filename=\"face_photo.jpeg") ResquestBody facePhoto
    );



}
