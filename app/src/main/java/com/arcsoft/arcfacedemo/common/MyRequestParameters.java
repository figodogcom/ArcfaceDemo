package com.arcsoft.arcfacedemo.common;

import com.commaai.commons.service.request.RequestParameters;

import java.util.Map;

public class MyRequestParameters extends RequestParameters {



    /**
     * csp_id
     */
    private String cspId;

    private String cspKey;



    /**
     * utm_medium
     */
    private String utmMedium;

    /**
     * utm_source
     */
    private String utmSource;


    public String getCspKey() {
        return cspKey;
    }

    public void setCspKey(String cspKey) {
        this.cspKey = cspKey;
    }


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
        map.put("csp_key",cspKey);
//        map.put("device_id", deviceId);
//        map.put("system_version", systemVersion);
        map.put("utm_medium", utmMedium);
        map.put("utm_source", utmSource);


        return map;
    }
}
