package com.arcsoft.arcfacedemo.searcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.TrackUtil;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.android.gms.internal.zzs.TAG;

public class ArcSoftSearcher extends YZWSearcher {

    //    private FaceHelper faceHelper;
    private FaceListener faceListener;
    private static final float SIMILAR_THRESHOLD = 0.8F;
    private static final int MAX_DETECT_NUM = 10;
    boolean livenessDetect;
    Context context;
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();



    public ArcSoftSearcher(Context context) {
        this.context = context;
        setFaceListener();
        SettingPreference settingPreference = new SettingPreference(context);
        livenessDetect = settingPreference.getPreviewAlive();
    }


//    public FaceHelper getFaceHelper(){
//
//        return faceHelper;
//    }

    @Override
    public void setPreviewSize(Camera.Size previewSize) {
        //TODO
//        Camera.Size newpreviewSize;
//        newpreviewSize = newInstance(Camera.Size.class, 640,640);
        super.setPreviewSize(previewSize);

    }

    public boolean hasFaceInfo(byte[] nv21){
        List<FaceInfo> faceInfoList = new ArrayList<>();
        faceEngine.detectFaces(nv21,previewSize.width,previewSize.height,FaceEngine.CP_PAF_NV21,faceInfoList);
        if(faceInfoList.size() > 0){
            return true;
        }
        return false;
    }

//    @Override
//    public void setFaceHelper(FaceEngine faceEngine,Camera.Size previewSize) {
//        super.setFaceHelper(faceEngine,previewSize);
////        previewSize = newInstance(Camera.Size.class, 640,640);
//
//        this.previewSize = previewSize;
//
//
//    }
//
//
//    public static <T> T newInstance(Class<T> clazz, Object... initargs) {
//        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
//        Constructor constructor = constructors[0];
//
//        try {
//            return (T) constructor.newInstance(initargs);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    @Override
    public void search(byte[] nv21) {
        super.search(nv21);
        callback.onSearchingCallback();
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        livenessMap.clear();
        Log.i(TAG, "onPreview: first");

        List<FaceInfo> faceInfoList = new ArrayList<>();
//        if(faceEngine == null){
//            Log.i(TAG, "xxxxx faceEngine is null ");
//        }


//        int detectFacesCode = faceEngine.detectFaces(nv21,480,640,FaceEngine.CP_PAF_NV21,faceInfoList);
        int detectFacesCode = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
        Log.i(TAG, "search: nv21 = " + nv21.length);
        Log.i(TAG, "search: detectFacesCode = " + detectFacesCode);
        Log.i(TAG, "search: previewsize" + previewSize.width + "   " + previewSize.height);
        Log.i(TAG, "search: faceinfolist.size = " + faceInfoList.size());
        if (detectFacesCode != ErrorInfo.MOK || faceInfoList.size() == 0) {
            callback.onSearchFailCallback();
            return;
        }
        TrackUtil.keepMaxFace(faceInfoList);
//
        FaceFeature faceFeature = new FaceFeature();
        int faceFeatureCode = faceEngine.extractFaceFeature(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
        Log.i(TAG, "search: faceFeatureCode = " + faceFeatureCode);

        if (faceFeatureCode != ErrorInfo.MOK) {
            callback.onSearchFailCallback();
            return;
        }
//        Log.i(TAG, "faceFeature = " + faceFeature.getFeatureData());
        searchface(faceFeature);
    }


    @Override
    public void searchface(final FaceFeature frFace) {
        super.searchface(frFace);
        Log.i(TAG, "searchFace: sssss");
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);

                        final CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);

//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                        if (compareResult == null) {
                            Log.i(TAG, "subscribe: sssss1");
                            emitter.onError(null);
                        } else {
                            Log.i(TAG, "subscribe: sssss2");
                            emitter.onNext(compareResult);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final CompareResult compareResult) {
                        Log.i(TAG, "onNext: sssss");


//                        Log.i(TAG, "compareResult: " + new Gson().toJson(compareResult));
                        Log.i(TAG, "compareResult getUserName: " + compareResult.getUserName());
                        Log.i(TAG, "compareResult getTrackId: " + compareResult.getTrackId());
                        Log.i(TAG, "compareResult getSimilar: " + compareResult.getSimilar());


                        if (compareResult == null || compareResult.getUserName() == null) {


                            callback.onSearchFailCallback();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tvSearchFace.append("识别结果：人脸识别结果为空" + "\n");
//                                    button.setText("启动识别");
//                                    searching = false;
//                                }
//                            });
//                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                            faceHelper.addName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            Log.i(TAG, "onNext: ");


                            //
//                            final File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResult.getUserName() + FaceServer.IMG_SUFFIX);
                            Bitmap bitmap = BitmapFactory.decodeFile(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResult.getUserName() + FaceServer.IMG_SUFFIX);

                            callback.onSearchSuccessCallback(bitmap,compareResult.getUserName());


//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tvSearchFace.append("识别结果：" + compareResult.getUserName() + "\n");
//                                    tvSearchFace.append("识别分数：" + compareResult.getSimilar() + "\n");
//
//                                    button.setText("启动识别");
//                                    searching = false;
//
//                                }
//                            });


//                            boolean isAdded = false;
//                            if (compareResultList == null) {
//                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                                faceHelper.addName(requestId, "VISITOR " + requestId);
//                                return;
//                            }
//                            for (CompareResult compareResult1 : compareResultList) {
//                                if (compareResult1.getTrackId() == requestId) {
//                                    isAdded = true;
//                                    break;
//                                }
//                            }
//                            if (!isAdded) {
//                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
//                                if (compareResultList.size() >= MAX_DETECT_NUM) {
//                                    compareResultList.remove(0);
//                                    adapter.notifyItemRemoved(0);
//                                }
//                                //添加显示人员时，保存其trackId
//                                compareResult.setTrackId(requestId);
//                                compareResultList.add(compareResult);
//                                adapter.notifyItemInserted(compareResultList.size() - 1);
//                            }
//                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
//                            faceHelper.addName(requestId, compareResult.getUserName());

                        } else {


                            callback.onSearchFailCallback();


                        }


                    }

                    @Override
                    public void onError(Throwable e) {

                        callback.onSearchFailCallback();

                    }

                    @Override
                    public void onComplete() {

                    }
                });

//        cameraHelper.release();
//        cameraHelper = null;


    }


    void setFaceListener() {

    }


}
