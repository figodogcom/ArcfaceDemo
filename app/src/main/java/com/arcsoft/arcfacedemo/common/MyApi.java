package com.arcsoft.arcfacedemo.common;

import com.arcsoft.arcfacedemo.response.TakeFaceQuickSearch;
import com.commaai.commons.service.Body;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MyApi {
    @Multipart
    @POST("take/face/quick_search")
    Call<Body<TakeFaceQuickSearch>> takeFaceQuickSearch(//接口参数
                                                        @Part("store_id") RequestBody store_id,
                                                        @Part("door_id") RequestBody doorId,
                                                        @Part("door_side") RequestBody doorSide,
                                                        @Part("face_photo\";filename=\"face_photo.jpeg") RequestBody facePhoto
    );



}
