package com.arcsoft.arcfacedemo.preview;

import android.content.Context;
import android.graphics.Bitmap;

import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;

public abstract class YZWPreview {
    private Context context;


    SettingPreference settingPreference;

    boolean livenessDetect;
    int previewPercent;
    int squarePercent;

    public void init() {

    }

    public void start() {

    }

    public void stop() {

    }


    public void onResume() {

    }

    public void onStop() {

    }

    public interface Callback {
        void imageOneAndTwo(Bitmap bitmap, Bitmap bitmap2);

        void imageThreeAndFour(Bitmap bitmap3, Bitmap bitmap4);

        void imageFiveAndSix(Bitmap bitmap5,Bitmap bitmap6);

        void tvDescribeAppend(String string);

        void tvDescribeSet(String string);

        void buttonText(String String);

        void tvSearchFaceSet(String string);

        void tvSearchFaceAppend(String string);

        void tvSearchFacesuccess(CompareResult compareResult);

        void tvSearchFaceFail(Bitmap bitmap6,String string);
    }

    protected Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
