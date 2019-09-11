package com.commaai.commons.service.request;

import java.util.HashMap;
import java.util.Map;

public class RequestParameters {

//    public RequestParameters newInstance() {
//        RequestParameters requestParameters = new RequestParameters();
//        return requestParameters;
//    }

    private String app_version = null;




    private String signKey = "sign";

    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        return map;
    }
}
