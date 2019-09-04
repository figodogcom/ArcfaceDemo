package com.arcsoft.arcfacedemo.searcher;

import com.arcsoft.arcfacedemo.util.face.FaceHelper;

public class ArcSoftSearcher extends YZWSearcher {

    private FaceHelper faceHelper;

    @Override
    public void onPreview(byte[] nv21) {
        super.onPreview(nv21);

//        if (faceHelper != null) {
//            facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
//        }
    }
}
