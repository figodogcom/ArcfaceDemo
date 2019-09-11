package com.arcsoft.arcfacedemo.common;

import com.commaai.commons.service.request.RequestParameters;

import java.util.Map;

public class MyRequestParameters extends RequestParameters {



    /**
     * csp_id
     */
    private String cspId;





    /**
     * utm_medium
     */
    private String utmMedium;

    /**
     * utm_source
     */
    private String utmSource;



    private String app_version = "1";

    private String api_ver = "1.0";


    private String api_token = "1";

    private String device_id = "1";

    private String system_version = "1";

    private String client_time = "1561632211";




    public String getCspId() {
        return cspId;
    }

    public void setCspId(String cspId) {
        this.cspId = cspId;
    }



    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }


    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApi_ver() {
        return api_ver;
    }

    public void setApi_ver(String api_ver) {
        this.api_ver = api_ver;
    }

    public String getApi_token() {
        return api_token;
    }

    public void setApi_token(String api_token) {
        this.api_token = api_token;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getSystem_version() {
        return system_version;
    }

    public void setSystem_version(String system_version) {
        this.system_version = system_version;
    }


    /**
     * client_time
     * @return
     */
    public String getClientTime() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    @Override
    public Map<String, String> toMap() {


        Map<String, String> map = super.toMap();

//        map.put("api_ver", apiVer);
//        map.put("app_version", appVersion);
//        map.put("cart_no",cartNo);
//        map.put("client_time", getClientTime());
        map.put("csp_id", cspId);
//        map.put("device_id", deviceId);
//        map.put("system_version", systemVersion);
        map.put("utm_medium", utmMedium);
        map.put("utm_source", utmSource);





        map.put("app_version",app_version);
        map.put("api_ver",api_ver);
        map.put("api_token",api_token);
        map.put("device_id",device_id);
        map.put("system_version",system_version);
        map.put("client_time",client_time);

        return map;
    }
}
