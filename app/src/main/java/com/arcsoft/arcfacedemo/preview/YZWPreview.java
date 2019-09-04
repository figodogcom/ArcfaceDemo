package com.arcsoft.arcfacedemo.preview;

import android.content.Context;
import android.graphics.Bitmap;

import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.searcher.YZWSearcher;

public abstract class YZWPreview {
    private Context context;


    SettingPreference settingPreference;

    private YZWSearcher searcher;

    public void setSearcher(YZWSearcher searcher) {
        this.searcher = searcher;
    }

    public YZWSearcher getSearcher() {
        return searcher;
    }

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

    public void onDestroy() {

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    public void show() {
    }

    public void hide(){

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

        void tvSearchFaceSearchingOrFail(Bitmap bitmap6, String string);
    }

    protected Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
