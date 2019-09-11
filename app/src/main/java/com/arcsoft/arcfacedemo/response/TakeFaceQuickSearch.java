package com.arcsoft.arcfacedemo.response;

import com.google.gson.annotations.SerializedName;

public class TakeFaceQuickSearch {

    @SerializedName("user_id")
    public int userID;

    @SerializedName("real_name")
    public int realName;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("comma_id")
    public String commaId;

    @SerializedName("user_level")
    public int userLevel;

    @SerializedName("identity_num")
    public String identityNum;

    @SerializedName("nick_name")
    public String nickName;

    @SerializedName("integral")
    public int integral;

    @SerializedName("opendoor_count")
    public int opendoorCount;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("wxpay_entrust")
    public int wxpayEntrust;

    @SerializedName("unpaid_order")
    public String unpaidOrder;

    @SerializedName("qrcode_url")
    public String qrcodeUrl;


    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getRealName() {
        return realName;
    }

    public void setRealName(int realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCommaId() {
        return commaId;
    }

    public void setCommaId(String commaId) {
        this.commaId = commaId;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public String getIdentityNum() {
        return identityNum;
    }

    public void setIdentityNum(String identityNum) {
        this.identityNum = identityNum;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getIntegral() {
        return integral;
    }

    public void setIntegral(int integral) {
        this.integral = integral;
    }

    public int getOpendoorCount() {
        return opendoorCount;
    }

    public void setOpendoorCount(int opendoorCount) {
        this.opendoorCount = opendoorCount;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getWxpayEntrust() {
        return wxpayEntrust;
    }

    public void setWxpayEntrust(int wxpayEntrust) {
        this.wxpayEntrust = wxpayEntrust;
    }

    public String getUnpaidOrder() {
        return unpaidOrder;
    }

    public void setUnpaidOrder(String unpaidOrder) {
        this.unpaidOrder = unpaidOrder;
    }

    public String getQrcodeUrl() {
        return qrcodeUrl;
    }

    public void setQrcodeUrl(String qrcodeUrl) {
        this.qrcodeUrl = qrcodeUrl;
    }
}
