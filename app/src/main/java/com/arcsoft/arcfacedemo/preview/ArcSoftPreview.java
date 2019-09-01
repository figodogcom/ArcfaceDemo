package com.arcsoft.arcfacedemo.preview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.common.Util;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.arcfacedemo.util.face.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.ShowFaceInfoAdapter;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.hardware.Camera.getCameraInfo;
import static com.arcsoft.arcfacedemo.common.Util.nv21ToBitmap;
import static com.arcsoft.arcfacedemo.common.Util.nv21ToFace;
import static com.arcsoft.arcfacedemo.common.Util.rotateBitmap;

public class ArcSoftPreview extends YZWPreview {
    private FaceEngine faceEngine;
    private int afCode = -1;
    private Activity activity;
    private Context context;
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private String TAG = getClass().getCanonicalName();
    private boolean livenessDetect = true;
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;


    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private static final int WAIT_LIVENESS_INTERVAL = 50;
    private FaceHelper faceHelper;
    private static final int MAX_DETECT_NUM = 10;
    private static final float SIMILAR_THRESHOLD = 0.8F;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    List<FacePreviewInfo> facePreviewInfoList;
    Boolean searching = false;
    Thread thread;

    private View previewView;
    private FrameLayout frameView;
    private FaceRectView faceRectView;
    int maxFaceNum;
    Button button;



    public ArcSoftPreview(Activity activity){
        this.activity = activity;
        context = activity.getApplicationContext();
        previewView = activity.findViewById(R.id.texture_preview);
        frameView = activity.findViewById(R.id.frame_view);
        faceRectView = activity.findViewById(R.id.face_rect_view);
        button = activity.findViewById(R.id.btn_preview_register_start);
        FaceServer.getInstance().init(context);

//        settingPreference = new SettingPreference(context);
//        livenessDetect = settingPreference.getPreviewAlive();
//        previewPercent = Integer.parseInt(settingPreference.getPreviewPercent());
//        squarePercent = Integer.parseInt(settingPreference.getPreviewSquarePercent());
//        context =  activity.getApplicationContext();

    }



    @Override
    public void start() {
        super.start();


        if (cameraHelper != null) {
            cameraHelper.start();
        }
    }

    @Override
    public void stop() {
        super.stop();


    }

    @Override
    public void init() {
        super.init();

        Log.i(TAG, "init: ppppp6");

        initEngine();
        initCamera();
    }

    private void initEngine() {
        faceEngine = new FaceEngine();
        //增加了FaceEngine.ASF_FACE_RECOGNITION
        afCode = faceEngine.init(context, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                16, 20, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
//        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
        if (afCode != ErrorInfo.MOK) {
            //TODO 暂时关闭
//            Toast.makeText(context, context.getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(context, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

//    private void requestCameraPermission() {
//        Log.w(TAG, "Camera permission is not granted. Requesting permission");
//
//        final String[] permissions = new String[]{Manifest.permission.CAMERA};
//
//        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                Manifest.permission.CAMERA)) {
//            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
//            return;
//        }
//
//
////        View.OnClickListener listener = new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                ActivityCompat.requestPermissions(activity, permissions,
////                        RC_HANDLE_CAMERA_PERM);
////            }
////        };
//
////        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,     导入失败？？？？？？？？？？？？？？？
////                Snackbar.LENGTH_INDEFINITE)
////                .setAction(R.string.ok, listener)
////                .show();
//    }




    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // TODO
        final android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        getCameraInfo(cameraID, info);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
                if (faceFeature == null) {
                    Log.i(TAG, "wwwww: ");

                }
//                Log.i(TAG, "wwwww0: " + faceFeature.getFeatureData());

                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);

                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        Log.i(TAG, "wwwww1");
                        searchFace(faceFeature, requestId);
                    }
                    //活体检测通过，搜索特征
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {

                        Log.i(TAG, "wwwww2");

                        callback.tvSearchFaceAppend("识别结果：活体" + "\n");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：活体" + "\n");
//
//                            }
//                        });


                        searchFace(faceFeature, requestId);
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


                        callback.tvSearchFaceAppend("识别结果：活体未能识别" + "\n");
                        callback.buttonText("启动识别");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：活体未能识别" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });

                        searching = false;

//                                    }
//                                }));
                    }
                    //活体检测失败
                    else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);

                        callback.tvSearchFaceAppend("识别结果：非活体" + "\n");
                        callback.buttonText("启动识别");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：非活体" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });

                        searching = false;

                    }

                }
                //FR 失败
                else {
                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);

                    callback.tvSearchFaceAppend("识别结果：FR失败" + "\n");
                    callback.buttonText("启动识别");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            tvSearchFace.append("识别结果：FR失败" + "\n");
//                            button.setText("启动识别");
//                        }
//                    });
                    searching = false;

                }
            }

        };

        CameraListener cameraListener = new CameraListener() {
            List<AgeInfo> ageInfoList;
            List<GenderInfo> genderInfoList;
            List<Face3DAngle> face3DAngleList;
            List<LivenessInfo> faceLivenessInfoList;
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {

                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror);

                faceHelper = new FaceHelper.Builder()
                        .faceEngine(faceEngine)
                        .frThreadNum(MAX_DETECT_NUM)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        //类名换了
                        .currentTrackId(ConfigUtil.getTrackId(context))
                        .build();

                ageInfoList = new ArrayList<>();
                genderInfoList = new ArrayList<>();
                face3DAngleList = new ArrayList<>();
                faceLivenessInfoList = new ArrayList<>();


            }


            @Override
            public void onPreview(final byte[] nv21, final Camera camera) {
                if (faceHelper != null) {
                    facePreviewInfoList = faceHelper.onPreviewFrame(nv21);

                }

//                new YuvImage()

                final Bitmap bitmap = nv21ToBitmap(nv21, previewSize.width, previewSize.height);
                final Bitmap bitmap2 = rotateBitmap(bitmap, info.orientation);
                Bitmap bitmap3 = null;


                callback.tvDescripeSet("");
                callback.tvDescripeAppend("预览原图宽高及像素：" + previewSize.width + "   " + previewSize.height + "   " + previewSize.width * previewSize.height + "\n");
                callback.tvDescripeAppend("预览正方形宽高及像素：" + frameView.getWidth() + "   " + frameView.getHeight() + "    " + frameView.getWidth() * frameView.getHeight() + "\n");

//                tvDecribe.setText("");
//                tvDecribe.append("预览原图宽高及像素：" + previewSize.width + "   " + previewSize.height + "   " + previewSize.width * previewSize.height + "\n");
//                tvDecribe.append("预览正方形宽高及像素：" + frameView.getWidth() + "   " + frameView.getHeight() + "    " + frameView.getWidth() * frameView.getHeight() + "\n");


                callback.imageOneAndTwo(bitmap,bitmap2);
                callback.imageThreeAndFour(null,null);
//                imageView.setImageBitmap(bitmap);
//                imageView2.setImageBitmap(bitmap2);
//                imageView3.setImageBitmap(null);
//                imageView4.setImageBitmap(null);


                //TODO
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (searching == false) {
                            searching = true;
                            callback.buttonText("停止识别");
                            callback.tvSearchFaceSet("");
                            callback.imageFiveAndSix(null,null);



//                            button.setText("停止识别");
//                            tvSearchFace.setText("");
//                            imageView5.setImageBitmap(null);
//                            imageView6.setImageBitmap(null);
                        } else if (searching == true) {
                            searching = false;

                            callback.buttonText("启动识别");
                            callback.tvSearchFaceSet("");
                            callback.imageFiveAndSix(null,null);


//                            button.setText("启动识别");
//                            tvSearchFace.setText("");
////                            thread.stop();
//                            imageView5.setImageBitmap(null);
//                            imageView6.setImageBitmap(null);


                        }


                    }
                });

//                Log.i(TAG, "onPreview: " + "xxxxxxx3");

                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
                Log.i(TAG, "xxxxx: code = " + code);

                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                    if (code != ErrorInfo.MOK) {
                        return;
                    }
                } else {
                    return;
                }

                Log.i(TAG, "onPreview: " + "xxxxxxx2");

                int ageCode = faceEngine.getAge(ageInfoList);
                int genderCode = faceEngine.getGender(genderInfoList);
                int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
                int livenessCode = faceEngine.getLiveness(faceLivenessInfoList);

                //有其中一个的错误码不为0，return
                if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
                    return;
                }
                if (faceRectView != null && drawHelper != null) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        drawInfoList.add(new DrawInfo(faceInfoList.get(i).getRect(), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), faceLivenessInfoList.get(i).getLiveness(), null));
                    }
                    drawHelper.draw(faceRectView, drawInfoList);
                }

                if (faceInfoList != null && faceInfoList.size() > 0) {

                    Log.i(TAG, "xxxxxx1");

                    // TODO extra method
                    FaceInfo facemax = null;
//                    for (FaceInfo faceInfo : faceInfoList) {
//                        if (facemax == null) {
//                            facemax = faceInfo;
//                            continue;
//                        }
//
//                        if (faceInfo.getRect().width() * faceInfo.getRect().height() >= facemax.getRect().height() * faceInfo.getRect().width()){
//                            facemax = faceInfo;
//                        }
//
//                    }


                    for (int i = 0; i < faceInfoList.size(); i++) {
                        if (facemax == null) {
                            facemax = faceInfoList.get(i);
                            continue;
                        }

                        if (faceInfoList.get(i).getRect().width() * faceInfoList.get(i).getRect().height() >= facemax.getRect().height() * facemax.getRect().width()) {
                            facemax = faceInfoList.get(i);
                            maxFaceNum = i;
                        }
                    }


                    final Rect rect = facemax.getRect();
                        //TODO
//                    Log.e(TAG, "xxxxx: " + previewPercent + "    " + squarePercent);

//                    Log.e(TAG, "" + (previewPercent / 100));




                    double p1 = ((double) (rect.height() * rect.width()) / (double) (previewView.getHeight() * previewView.getWidth())) / ((double) previewPercent / 100);


                    boolean isFaceBiggerPercentPreview = rect.height() * rect.width() > (previewView.getHeight() * previewView.getWidth()) * previewPercent / 100.0;
                    boolean isFaceBiggerPercentSquare = rect.height() * rect.width() > (frameView.getWidth() * frameView.getHeight()) * squarePercent / 100.0;
                    if(!isFaceBiggerPercentPreview || !isFaceBiggerPercentSquare){
                        callback.tvSearchFaceSet("人脸偏后" + "\n");
                    }



                    Log.e(TAG, "xxxxx  p1: " + p1);
                    Log.e(TAG, "xxxxx isFaceBiggerPercentPreview=" + isFaceBiggerPercentPreview);
                    Log.e(TAG, "xxxxx isFaceBiggerPercentSquare= " + isFaceBiggerPercentSquare);


                    if (isFaceInPreview(rect, previewSize)) {
                        Log.i(TAG, "xxxxxx");


                        // TODO 1 直接copy前面的图片   OR  2 直接把rotate和flip一次性做完matrix
                        bitmap3 = rotateBitmap(nv21ToFace(nv21, previewSize.width, previewSize.height, rect), info.orientation);


                        callback.imageThreeAndFour(bitmap3,Util.fanZhuanBitmap(bitmap3));
                        callback.tvDescripeAppend("头像宽高及面积：" + rect.width() + "    " + rect.height() + "    " + rect.width() * rect.height() + "\n");
//                        imageView3.setImageBitmap(bitmap3);
//                        imageView4.setImageBitmap(Util.fanZhuanBitmap(bitmap3));
//
//
//                        tvDecribe.append("头像宽高及面积：" + rect.width() + "    " + rect.height() + "    " + rect.width() * rect.height() + "\n");

//                        if (drawHelper != null && drawHelper.getRealrect() != null) {
                        Rect previewRect = Util.adjustRect(
                                rect,
                                previewSize.width,
                                previewSize.height,
                                previewView.getWidth(),
                                previewView.getHeight(),
                                info.orientation,
                                cameraID,
                                false,
                                false,
                                false

                        );

//                            tvDecribe.append("预览原图头像中心坐标：" + drawHelper.getRealrect().centerX() + "   " + drawHelper.getRealrect().centerY() + "\n");

                        callback.tvDescripeAppend("预览原图头像中心坐标：" + previewRect.centerX() + "   " + previewRect.centerY() + "\n");
//                        tvDecribe.append("预览原图头像中心坐标：" + previewRect.centerX() + "   " + previewRect.centerY() + "\n");


//                        }


                        int changeX = rect.width() / 4;
                        int changeY = rect.height() / 4;
                        // TODO
                        Rect smallrect = new Rect(rect);
                        smallrect.intersect(changeX, changeY, -changeX, changeY);

//                        Rect smallrect = new Rect(rect.left + changeX, rect.top + changeY, rect.right - changeX, rect.bottom - changeY);


                        final boolean ifcenter = smallrect.contains((int) (previewSize.width / 2), (int) (previewSize.height / 2));
                        callback.tvDescripeAppend("是否靠近中心：" + ifcenter + "\n");
//                        tvDecribe.append("是否靠近中心：" + ifcenter + "\n");
                        Log.i(TAG, "ccccc =" + previewSize.width + "    " + previewRect.centerX() + "    rect = " + rect.left +  "        " + rect.top +  "        " +rect.right +  "        " + rect.bottom) ;
//                        if (!ifcenter && drawHelper != null) {
                        if (previewSize.width / 2 > previewRect.centerX()) {

                            callback.tvSearchFaceSet("人脸偏移：左" + "\n");
//                            tvDecribe.append("人脸偏移：左" + "\n");

                        } else {
                            callback.tvSearchFaceSet("人脸偏移：右" + "\n");
//                            tvDecribe.append("人脸偏移：右" + "\n");

                        }
//                        }


                        if (searching == true) {

                            // TODO huoti
                            if (ifcenter) {
                                if (isFaceBiggerPercentPreview)
                                    if (isFaceBiggerPercentSquare)
                                        if ((livenessDetect && faceLivenessInfoList.get(maxFaceNum).getLiveness() == LivenessInfo.ALIVE) || !livenessDetect) {

                                            searching = false;


                                            final Bitmap finalBitmap = bitmap3;
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

//                                        if (searching == false) {
//                                            return;
//                                        }

                                                    // byte[] nv21Clone = nv21.clone();

//                                        // TODO
//                                        final Bitmap bitmap5 = rotateBitmap(nv21ToBitmap(nv21, previewSize.width, previewSize.height), info.orientation);
//                                        final Bitmap bitmap6 = Util.fanZhuanBitmap(rotateBitmap(nv21ToFace(nv21, previewSize.width, previewSize.height, rect), info.orientation));
                                       final Bitmap bitmap5 = finalBitmap;
                                       final Bitmap bitmap6 = Util.fanZhuanBitmap(finalBitmap);


                                                    //TODO 暂时关闭
                                                    callback.imageFiveAndSix(bitmap5,bitmap6);


//                                                    runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            imageView5.setImageBitmap(bitmap2);
//                                                            imageView6.setImageBitmap(Util.fanZhuanBitmap(finalBitmap));
//                                                            Log.i(TAG, "run: imageview");
//                                                        }
//                                                    });

                                                    // TODO
//                                        clearLeftFace(facePreviewInfoList);


                                                    Log.i(TAG, "run:facePreviewInfoList.size()= " + facePreviewInfoList.size());
//
//                                        if (searching == false) {
//                                            return;
//                                        }

                                                    if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                                                        Log.i(TAG, "run: rrrrr");
                                                        for (int i = 0; i < facePreviewInfoList.size(); i++) {
                                                            if (livenessDetect) {
                                                                livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
                                                            }
                                                            /**
                                                             * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
                                                             * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
                                                             */


                                                            Log.i(TAG, "run: requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) = " + requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()));
                                                            //关闭条件,使在屏幕中已识别的人脸可以再次识别
//                                                if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
//                                                        || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
                                                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                                                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                                                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());
//                                                }


                                                        }
                                                    }


                                                }
                                            };

                                            thread = new Thread(runnable);
                                            thread.start();
                                        } else {
                                            callback.tvSearchFaceAppend("识别结果：非活体");
                                            searching = false;
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    tvSearchFace.append("识别结果：非活体");
//                                                    searching = false;
//                                                }
//                                            });
                                        }

                            }
                        }


                    }
                } else {
                    callback.imageThreeAndFour(null,null);
//                    imageView3.setImageBitmap(null);
//                    imageView4.setImageBitmap(null);
                }


//                clearLeftFace(facePreviewInfoList);

//                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
//
//                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
//                        if (livenessDetect) {
//                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
//                        }
//                        /**
//                         * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
//                         * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
//                         */
//                        if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
//                                || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
//                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
//                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
////                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());
//                        }
//                    }
//                }


            }

            private boolean isFaceInPreview(Rect rect, Camera.Size previewSize) {

                if (rect.left >= 0
                        && rect.top >= 0
                        && rect.bottom <= previewSize.height
                        && rect.right <= previewSize.width)
                    return true;
                else return false;
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(activity.getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
//            boolean isAllGranted = true;
//            for (int grantResult : grantResults) {
//                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
//            }
//            if (isAllGranted) {
//                initEngine();
//                initCamera();
//                if (cameraHelper != null) {
//                    cameraHelper.start();
//                }
//            } else {
//                Toast.makeText(this.getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
//    @Override
//    public void onGlobalLayout() {
//        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//        if (!checkPermissions(NEEDED_PERMISSIONS)) {
//            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
//        } else {
//            initEngine();
//            initCamera();
//        }
//    }


    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Log.i(TAG, "searchFace: sssss" );
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
                        if (compareResult == null || compareResult.getUserName() == null) {
                            callback.tvDescripeAppend("识别结果：人脸识别结果为空" + "\n");
                            callback.buttonText("启动识别");
                            searching = false;

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tvSearchFace.append("识别结果：人脸识别结果为空" + "\n");
//                                    button.setText("启动识别");
//                                    searching = false;
//                                }
//                            });
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {

                            callback.tvSearchFaceAppend("识别结果：" + compareResult.getUserName() + "\n");
                            callback.tvSearchFaceAppend("识别分数：" + compareResult.getSimilar() + "\n");
                            callback.buttonText("启动识别");

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
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "VISITOR " + requestId);


                            callback.tvSearchFaceAppend("识别结果：人脸未注册" + "\n");
                            callback.tvSearchFaceAppend("识别分数：" + compareResult.getSimilar() + "\n");
                            callback.buttonText("启动识别");
                            searching = false;

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tvSearchFace.append("识别结果：人脸未注册" + "\n");
//                                    tvSearchFace.append("识别分数：" + compareResult.getSimilar() + "\n");
//                                    button.setText("启动识别");
//                                    searching = false;
//                                }
//                            });

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        searching = false;

                        callback.tvSearchFaceAppend("识别结果：人脸未注册" + "\n");
                        callback.buttonText("启动识别");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：人脸未注册" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void unInitEngine() {

        if (afCode == 0) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        unInitEngine();

    }


    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
//    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
//        Set<Integer> keySet = requestFeatureStatusMap.keySet();
//        if (compareResultList != null) {
//            for (int i = compareResultList.size() - 1; i >= 0; i--) {
//                if (!keySet.contains(compareResultList.get(i).getTrackId())) {
//                    compareResultList.remove(i);
//                    adapter.notifyItemRemoved(i);
//                }
//            }
//        }
//        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
//            requestFeatureStatusMap.clear();
//            livenessMap.clear();
//            return;
//        }
//
//        for (Integer integer : keySet) {
//            boolean contained = false;
//            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
//                if (facePreviewInfo.getTrackId() == integer) {
//                    contained = true;
//                    break;
//                }
//            }
//            if (!contained) {
//                requestFeatureStatusMap.remove(integer);
//                livenessMap.remove(integer);
//            }
//        }
//
//    }





}
