package com.arcsoft.arcfacedemo.common;

import com.commaai.commons.service.BaseService;

import retrofit2.Retrofit;

public class MyApiService extends BaseService {
    public MyApi getService() {
        Retrofit retrofit = getRetrofit();
        return retrofit.create(MyApi.class);
    }
}