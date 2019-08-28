package com.arcsoft.arcfacedemo.preview;

import android.content.Context;
import android.graphics.Bitmap;

import com.arcsoft.arcfacedemo.common.SettingPreference;

public abstract class YZWPreview {
    private Context context;


    SettingPreference settingPreference;

    boolean livenessDetect;
    int previewPercent;
    int squarePercent;

    public void init(Context context) {
        this.context = context;
        settingPreference = new SettingPreference(context);

        livenessDetect = settingPreference.getPreviewAlive();
        previewPercent = Integer.parseInt(settingPreference.getPreviewPercent());
        squarePercent = Integer.parseInt(settingPreference.getPreviewSquarePercent());
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

        void tvDescripeAppend(String string);

        void tvDescripeSet(String string);

        void buttonText(String String);
    }

    protected Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
