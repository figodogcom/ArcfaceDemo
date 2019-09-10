package com.commaai.commons.service;

import com.commaai.commons.service.request.RequestParameters;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class BaseService {

    private String baseUrl;
    private RequestParameters requestParameters;

    public RequestParameters getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public BaseService() {

    }

    protected Retrofit getRetrofit() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        Retrofit2Interceptor networkInterceptor = new Retrofit2Interceptor();
        networkInterceptor.requestParameters = requestParameters;
        builder.addInterceptor(networkInterceptor);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(httpLoggingInterceptor);

//        Retrofit2Interceptor networkInterceptor = new Retrofit2Interceptor();
//        networkInterceptor.requestParameters = requestParameters;
//        builder.addNetworkInterceptor(networkInterceptor);

        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory()
                .build();
    }

}
