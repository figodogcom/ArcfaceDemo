package com.arcsoft.arcfacedemo.searcher;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.arcfacedemo.util.face.RequestFeatureStatus;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.LivenessInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
    private Camera.Size previewSize;
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



    public FaceHelper getFaceHelper(){

        return faceHelper;
    }


    @Override
    public void setFaceHelper(FaceEngine faceEngine) {
        super.setFaceHelper(faceEngine);
        previewSize = newInstance(Camera.Size.class, 640,640);



        faceHelper = new FaceHelper.Builder()
                .faceEngine(faceEngine)
                .frThreadNum(MAX_DETECT_NUM)
                .previewSize(previewSize)
                .faceListener(faceListener)
                //类名换了
                .currentTrackId(ConfigUtil.getTrackId(context))
                .build();
    }


    public static <T> T newInstance(Class<T> clazz, Object... initargs) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor constructor = constructors[0];

        try {
            return (T) constructor.newInstance(initargs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onPreview(byte[] nv21) {
        super.onPreview(nv21);
        List<FacePreviewInfo> facePreviewInfoList = null;


        if (faceHelper != null) {
            facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
        }


        if (facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
            Log.i(TAG, "run: rrrrr");
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        if (livenessDetect) {
                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
                        }
                /**
                 * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
                 * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
                 */


//                        Log.i(TAG, "run: requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) = " + requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()));
//                        //关闭条件,使在屏幕中已识别的人脸可以再次识别
////                                                if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
////                                                        || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
//                        requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
//                        faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());


//                                                }


            }
        }
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


                            callback.onSearchSuccessCallback(compareResult);


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
        faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
                Log.i(TAG, "onFaceFeatureInfoGet: xxxxxx");

                if (faceFeature == null) {
                    Log.i(TAG, "wwwww: ");

                }

                callback.onSearchFailCallback();


                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.i(TAG, "wwwww0: " + faceFeature.getFeatureData());

                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);

                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        Log.i(TAG, "wwwww1");

                        searchface(faceFeature);
//                        searchFace(faceFeature, requestId);
                    }
                    //活体检测通过，搜索特征
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {

                        Log.i(TAG, "wwwww2");


//                        callback.tvSearchFaceAppend("识别结果：活体" + "\n");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：活体" + "\n");
//
//                            }
//                        });

                        searchface(faceFeature);

                    }
                    //活体检测未出结果，延迟100ms再执行该函数
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.UNKNOWN) {


//                        getFeatureDelayedDisposables.add(Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
//                                .subscribe(new Consumer<Long>() {
//                                    @Override
//                                    public void accept(Long aLong) {
                        Log.i(TAG, "wwwww3");
//                                        onFaceFeatureInfoGet(faceFeature, requestId);
//                                        searching = true;

//                        callback.onPreviewSearchFaceFail(bitmap6);

//                        callback.tvSearchFaceAppend("识别结果：活体未能识别" + "\n");
//                        callback.buttonText("启动识别");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：活体未能识别" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });

//                        searching = false;

//                                    }
//                                }));
                    }
                    //活体检测失败
                    else {

                        callback.onSearchFailCallback();
//                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);
//                        callback.onPreviewSearchFaceFail(bitmap6);

//                        callback.tvSearchFaceAppend("识别结果：非活体" + "\n");
//                        callback.buttonText("启动识别");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：非活体" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });

//                        searching = false;

                    }

                }
                //FR 失败
                else {
//                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                    callback.onSearchFailCallback();

//                    callback.tvSearchFaceAppend("识别结果：FR失败" + "\n");
//                    callback.buttonText("启动识别");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            tvSearchFace.append("识别结果：FR失败" + "\n");
//                            button.setText("启动识别");
//                        }
//                    });
//                    searching = false;

                }
            }

        };
    }


}
