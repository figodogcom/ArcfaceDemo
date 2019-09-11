package com.arcsoft.arcfacedemo.searcher;

import android.graphics.Bitmap;
import android.hardware.Camera;

import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;

public abstract class YZWSearcher {
    FaceHelper faceHelper;
    FaceEngine faceEngine;
    Camera.Size previewSize;

    public void search(byte[] nv21){

    }

    public void search(Bitmap bitmap){

    }

    public void searchface(FaceFeature fr){

    }

    public void setFaceHelper(FaceEngine faceEngine, Camera.Size previewSize){

    }

    public void setFaceEngine(FaceEngine faceEngine){
        this.faceEngine = faceEngine;
    }

    public void setPreviewSize(Camera.Size previewSize){
        this.previewSize = previewSize;
    }


    public FaceHelper getFaceHelper() {
        return faceHelper;
    }

    public interface Callback {
        void onSearchSuccessCallback(Bitmap bitmap,String name);
        void onSearchFailCallback();
        void onSearchingCallback();

    }

    protected Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


}
