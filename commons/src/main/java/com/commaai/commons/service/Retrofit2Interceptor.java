package com.commaai.commons.service;

import android.util.Log;

import com.commaai.commons.sdk.CommaaiUtil;
import com.commaai.commons.service.request.RequestParameters;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Created by liuxue on 2017/11/6.
 */

public class Retrofit2Interceptor implements Interceptor {

    private static final String TAG = Retrofit2Interceptor.class.getCanonicalName();

//    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static String toStringPartBody(MultipartBody.Part part) throws IOException {
//        RequestBody body = part.body();
//
//        Buffer sink = new Buffer();
//        body.writeTo(sink);
//
//        Charset charset = UTF8;
//        return sink.readString(charset);

        RequestBody body = part.body();

        Buffer sink = new Buffer();
        body.writeTo(sink);

        return sink.readUtf8();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
//        Log.d(TAG, "intercept() chain: " + chain);

        Request request = chain.request();

//        Log.d(TAG, "intercept() request: " + request);
//        Log.d(TAG, "intercept() request.body(): " + request.body());
//        Log.d(TAG, "intercept() request.body().contentType(): " + request.body().contentType());

        RequestBody requestBody = request.body();

        Log.d(TAG, "intercept() requestBody: " + requestBody);

        if (requestBody instanceof MultipartBody) {

            MultipartBody multipartBody = (MultipartBody) requestBody;

            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder(multipartBody.boundary());
            multipartBodyBuilder.setType(MultipartBody.FORM);

            Log.d(TAG, "MultipartBody requestParameters: " + requestParameters);
            if (requestParameters != null) {
                Map<String, String> m = requestParameters.toMap();
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    multipartBodyBuilder.addFormDataPart(entry.getKey(), null, RequestBody.create(MediaType.parse("text/plain"), entry.getValue()));
                }
            }

            for (MultipartBody.Part part : multipartBody.parts()) {
                multipartBodyBuilder.addPart(part);
            }

            TreeMap<String, String> map = new TreeMap<>();
            multipartBody = multipartBodyBuilder.build();
            for (MultipartBody.Part part : multipartBody.parts()) {

                Headers headers = part.headers();
                RequestBody body = part.body();
                MediaType contentType = body.contentType();

                if (headers != null) {
                    String disposition = headers.get("Content-Disposition");

                    if (disposition != null) {
                        if (contentType == null || contentType.type().equals("text")) {
                            Pattern pattern = Pattern.compile("form-data; name=\"(.*)\"");
                            Matcher matcher = pattern.matcher(disposition);
                            if (matcher.find()) {
                                String name = matcher.group(1);
                                map.put(name, toStringPartBody(part));
                            }
                        }
                    }
                }
            }

            multipartBodyBuilder = new MultipartBody.Builder(multipartBody.boundary());
            multipartBodyBuilder.setType(MultipartBody.FORM);
            String sign = null;

            try {
                sign = CommaaiUtil.generateSignature(map, requestParameters.getSecret());
                Log.d(TAG, "MultipartBody sign: " + sign);
            } catch (Exception e) {
                e.printStackTrace();
            }
            multipartBodyBuilder.addFormDataPart(requestParameters.getSignKey(), null, RequestBody.create(MediaType.parse("text/plain"), sign));

            for (MultipartBody.Part part : multipartBody.parts()) {
                multipartBodyBuilder.addPart(part);
            }

            multipartBody = multipartBodyBuilder.build();

            request = request.newBuilder()
                    .method(request.method(), multipartBody)
                    .header("Content-Length", String.valueOf(multipartBody.contentLength()))
                    .build();

            return chain.proceed(request);
        } else if (requestBody instanceof FormBody) {
            FormBody formBody = (FormBody) requestBody;

            TreeMap<String, String> map = new TreeMap<>();

            Log.d(TAG, "FormBody requestParameters: " + requestParameters);
            if (requestParameters != null) {
                Map<String, String> m = requestParameters.toMap();
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }

            for (int i = 0; i < formBody.size(); i++) {
                map.put(formBody.name(i), formBody.value(i));
            }





            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
//                formBodyBuilder.addEncoded(entry.getKey(), entry.getValue());
            }
            String sign = null;

            try {
                sign = CommaaiUtil.generateSignature(map, requestParameters.getSecret());
                Log.d(TAG, "FormBody sign: " + sign);
            } catch (Exception e) {
                e.printStackTrace();
            }
            formBodyBuilder.add(requestParameters.getSignKey(), sign);





            formBody = formBodyBuilder.build();






            request = request.newBuilder()
                    .method(request.method(), formBody)
                    .header("Content-Length", String.valueOf(formBody.contentLength()))
                    .build();

            return chain.proceed(request);
        } else {
            FormBody formBody = null;

            TreeMap<String, String> map = new TreeMap<>();

            Log.d(TAG, "FormBody requestParameters: " + requestParameters);
            if (requestParameters != null) {
                Map<String, String> m = requestParameters.toMap();
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }

            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
//                formBodyBuilder.addEncoded(entry.getKey(), entry.getValue());
            }
            String sign = null;

            try {
                sign = CommaaiUtil.generateSignature(map, requestParameters.getSecret());
                Log.d(TAG, "FormBody sign: " + sign);
            } catch (Exception e) {
                e.printStackTrace();
            }
            formBodyBuilder.add(requestParameters.getSignKey(), sign);

            formBody = formBodyBuilder.build();

            request = request.newBuilder()
                    .method(request.method(), formBody)
                    .header("Content-Length", String.valueOf(formBody.contentLength()))
                    .build();

            return chain.proceed(request);
        }

//        return chain.proceed(request);
    }

    public RequestParameters requestParameters;
}
