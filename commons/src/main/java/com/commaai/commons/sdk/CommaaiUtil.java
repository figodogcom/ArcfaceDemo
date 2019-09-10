package com.commaai.commons.sdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.commaai.commons.sdk.CommaaiConstants.SignType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by liuxue on 2018/5/24.
 */

public class CommaaiUtil {

    private static final String KEY = "sign";

    public static boolean isSignatureValid(Map<String, String> data, String key) throws Exception {
        return isSignatureValid(data, key, SignType.SHA256);
    }

    public static boolean isSignatureValid(Map<String, String> data, String key, SignType signType) throws Exception {
        if (!data.containsKey(KEY)) {
            return false;
        } else {
            String sign = data.get(KEY);
            return generateSignature(data, key, signType).equals(sign);
        }
    }

    public static String generateSignature(Map<String, String> data, String key) throws Exception {
        return generateSignature(data, key, SignType.SHA256);
    }

    public static String generateSignature(Map<String, String> data, String key, SignType signType) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        String[] keys = keyArray;
        int length = keyArray.length;

//        for (int i = 0; i < length; ++i) {
//            String k = keys[i];
//            if (!k.equals(KEY) && ((String) data.get(k)).trim().length() > 0) {
//                sb.append(k).append("=").append(((String) data.get(k)).trim()).append("&");
//            }
//        }
//
//        sb.append("key=").append(key);
//        if(SignType.MD5.equals(signType)) {
//            return MD5(sb.toString()).toUpperCase();
//        } else if(SignType.HMACSHA256.equals(signType)) {
//            return HMACSHA256(sb.toString(), key);
//        } else {
//            throw new Exception(String.format("Invalid sign_type: %s", new Object[]{signType}));
//        }

        for (int i = 0; i < length; ++i) {
            String k = keys[i];
            if (!k.equals(KEY)) {
                sb.append(k).append("=").append(data.get(k)).append("&");
            }
        }

        sb.append(key);
        if (SignType.SHA256.equals(signType)) {
            System.out.println("input: " + sb.toString());
            return SHA256(sb.toString());
        } else {
            throw new Exception(String.format("Invalid sign_type: %s", signType));
        }
    }

    public static String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }

    public static String HMACSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        byte[] bytes = array;
        int length = array.length;

        for (int i = 0; i < length; ++i) {
            byte item = bytes[i];
            sb.append(Integer.toHexString(item & 255 | 256).substring(1, 3));
        }

        return sb.toString().toUpperCase();
    }

    //

    private static String SHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes());

            String hash = bytesToHexString(digest.digest());

            return hash;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getAppVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    api_ver=1.0&app_version=1.0.5&
// avatar_url=http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL9DraJTygicJCMS0FSynXq2XfOucYR232lxNUfNx2PUyRToVaoCzAYibFRz2XHd4cjpwUOxX3CHiaKA/132&
// client_time=1539329651&
// csp_id=app_0001&device_id=50A6E99B-8751-413A-85BC-F747A905E431&
// latitude=23.12675124856792&longitude=113.32317077874886&nick_name=s.kwong &'&open_id=o_7aI1VFh7V7WIVjv08mdrEGs_F8&open_type=1&system_version=12.0&
// union_id=o5_471mEtLWDix1LDDKDvL8xdZ78&utm_medium=app&utm_source=ios&api_sign=bdd8057c31d2382c90c12e1bc70f66063d53dba35f30584bf06976e63df5f44e

    public static void main(String[] args) {
//        System.out.println("ok");
//
//        String nickName = "&边小苗\uD83C\uDF40";
////        String nickName = "边小苗\\uD83C\\uDF40";
//        System.out.println(nickName);
//        System.out.println("encode: " + java.net.URLEncoder.encode(nickName));
//        System.out.println("decode: " + java.net.URLDecoder.decode(java.net.URLEncoder.encode(nickName)));
//
//        Map<String, String> map = new HashMap<>();
//
//        map.put("api_ver", "1.0");
//        map.put("app_version", "1.0.5");
//        map.put("avatar_url", "http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL9DraJTygicJCMS0FSynXq2XfOucYR232lxNUfNx2PUyRToVaoCzAYibFRz2XHd4cjpwUOxX3CHiaKA/132");
//        map.put("client_time", "1539329651");
//        map.put("csp_id", "app_0001");
//        map.put("device_id", "50A6E99B-8751-413A-85BC-F747A905E431");
//        map.put("latitude", "23.12675124856792");
//        map.put("longitude", "113.32317077874886");
//        map.put("nick_name", "s.kwong &'");
//        map.put("open_id", "o_7aI1VFh7V7WIVjv08mdrEGs_F8");
//        map.put("open_type", "1");
//        map.put("system_version", "12.0");
//        map.put("union_id", "o5_471mEtLWDix1LDDKDvL8xdZ78");
//        map.put("utm_medium", "app");
//        map.put("utm_source", "ios");
//
//        try {
//            String signature = generateSignature(map, "4b111cc14a33b88e37e2e2934f493458");
//            System.out.println(signature);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String input = "api_ver=2.0&app_version=1.0.1&buy_barcodes=5000357703055&cart_no=LX001&change_number=0&client_time=1561632211&csp_id=smartcart_0001&device_id=CBI9S1IWU2&store_id=2&system_version=7.0&utm_medium=smartcart&utm_source=smartcart&4b111cc14a33b88e37e2e2934f493458";
        String output = SHA256(input);
        System.out.println("output: " + output);
    }
}
