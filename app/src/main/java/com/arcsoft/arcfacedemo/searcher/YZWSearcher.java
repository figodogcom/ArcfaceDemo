package com.arcsoft.arcfacedemo.searcher;

import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;

public abstract class YZWSearcher {
    FaceHelper faceHelper;

    public void onPreview(byte[] nv21){

    }

    public void searchface(FaceFeature fr){

    }

    public void setFaceHelper(FaceEngine faceEngine){

    }


    public FaceHelper getFaceHelper() {
        return faceHelper;
    }

    public interface Callback {
        void onSearchSuccessCallback(CompareResult compareResult);
        void onSearchFailCallback();
        void onSearchingCallback();

    }

    protected Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


}
