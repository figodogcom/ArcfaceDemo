package com.arcsoft.arcfacedemo.common;


import android.app.Application;
import android.app.Service;
import android.os.Build;

import com.commaai.commons.service.request.RequestParameters;

public class MyApplication extends Application {

    private MyApi service;
//    private Antiapi service2;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    public MyApi getService() {
//        if (service == null) {
//            MyRequestParameters requestParameters = new MyRequestParameters();
//            requestParameters.setApiVer("2.0");
//            requestParameters.setCartNo("LX001");
//            requestParameters.setAppVersion("1.0.1"); // TODO 动态获取
//            requestParameters.setCspId("smartcart_0001");
//            requestParameters.setDeviceId("CBI9S1IWU2");
//            requestParameters.setSystemVersion(Build.VERSION.RELEASE);
//            requestParameters.setUtmMedium("smartcart");
//            requestParameters.setUtmSource("smartcart");
//
//            requestParameters.setSecret("4b111cc14a33b88e37e2e2934f493458");
//            requestParameters.setSignKey("api_sign");
//
//            MyApiService retrofit = new MyApiService();
//            retrofit.setBaseUrl("https://api-ssc.commaretail.com/");
//            retrofit.setRequestParameters(requestParameters);
//
//
//            service = retrofit.getService();
//        }
//        return service;
//    }

    public MyApi getService() {


        if (service == null) {
            MyRequestParameters requestParameters = new MyRequestParameters();

            requestParameters.setCspKey("8e09dc50daa309498411bfc0358d4391");
            requestParameters.setCspId("take_cm");
            requestParameters.setUtmSource("android");
            requestParameters.setUtmMedium("take");
            //signkey???
//            requestParameters.setSecret("4b111cc14a33b88e37e2e2934f493458");
//            requestParameters.setSignKey("api_sign");

            MyApiService retrofit = new MyApiService();
            retrofit.setBaseUrl("https://api-v2.commaai.cn");
            retrofit.setRequestParameters(requestParameters);
            service = retrofit.getService();


        }
        return service;
    }






}