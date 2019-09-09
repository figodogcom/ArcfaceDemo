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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.arcsoft.arcfacedemo.searcher.YZWSearcher;
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
import static com.google.android.gms.internal.zzs.TAG;

public class ArcSoftPreview extends YZWPreview implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = ArcSoftPreview.class.getCanonicalName();

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    private FaceEngine faceEngine;
    private int afCode = -1;
    private Context context;
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean livenessDetect = true;
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;


    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private static final int WAIT_LIVENESS_INTERVAL = 50;
    private static final int MAX_DETECT_NUM = 10;
    private static final float SIMILAR_THRESHOLD = 0.8F;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    boolean searching = false;
    boolean ifGettingFace = false;
    Thread thread;

    private View previewView;
    //    private FrameLayout frameView;
    private FaceRectView faceRectView;
    Button button;
    Button backToHome;
    Bitmap bitmap6;
    FrameLayout arcsoftFrame;

    private Activity activity;

    private YZWSearcher searcher;

    @Override
    public void setSearcher(YZWSearcher searcher) {
        this.searcher = searcher;
    }


    public ArcSoftPreview(View view) {
        context = view.getContext();
//        context = activity.getApplicationContext();
        activity = (Activity) context;

        arcsoftFrame = view.findViewById(R.id.preview_arcsoft);
        previewView = view.findViewById(R.id.texture_preview);
//        frameView = view.findViewById(R.id.frame_view);
        faceRectView = view.findViewById(R.id.face_rect_view);
        button = view.findViewById(R.id.btn_preview_register_start);

        FaceServer.getInstance().init(context);

        settingPreference = new SettingPreference(context);
        livenessDetect = settingPreference.getPreviewAlive();
        previewPercent = Integer.parseInt(settingPreference.getPreviewPercent());
        squarePercent = Integer.parseInt(settingPreference.getPreviewSquarePercent());
//        context =  activity.getApplicationContext();

        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);


    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (cameraHelper != null) {
            cameraHelper.stop();
            cameraHelper.release();
        }
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

        if (cameraHelper != null) {
            cameraHelper.stop();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "init: ppppp6");

        initEngine();
        initCamera();
    }

    public void show() {
        arcsoftFrame.setVisibility(View.VISIBLE);

    }

    public void hide() {
        arcsoftFrame.setVisibility(View.INVISIBLE);
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

        CameraListener cameraListener = new CameraListener() {
//            List<AgeInfo> ageInfoList;
//            List<GenderInfo> genderInfoList;
//            List<Face3DAngle> face3DAngleList;
//            List<LivenessInfo> faceLivenessInfoList;

            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {

                Log.d(TAG, "onCameraOpened cameraId: " + cameraId);

                previewSize = camera.getParameters().getPreviewSize();

                Log.d(TAG, "onCameraOpened previewSize.width: " + previewSize.width);
                Log.d(TAG, "onCameraOpened previewSize.height: " + previewSize.height);

                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror);

                searcher.setFaceEngine(faceEngine);
                searcher.setPreviewSize(previewSize);

//                faceHelper = searcher.getFaceHelper();
            }

            @Override
            public void onPreview(final byte[] nv21, final Camera camera) {
                Log.d(TAG, "onPreview");

                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
                Log.d(TAG, "onPreview detectFaces code: " + code);
                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                    Log.d(TAG, "onPreview process code: " + code);
                    if (code != ErrorInfo.MOK) {
                        return;
                    }
                }else {
                    return;
                }

                List<AgeInfo> ageInfoList = new ArrayList<>();
                List<GenderInfo> genderInfoList = new ArrayList<>();
                List<Face3DAngle> face3DAngleList = new ArrayList<>();
                List<LivenessInfo> faceLivenessInfoList = new ArrayList<>();
                int ageCode = faceEngine.getAge(ageInfoList);
                int genderCode = faceEngine.getGender(genderInfoList);
                int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
                int livenessCode = faceEngine.getLiveness(faceLivenessInfoList);

                Log.d(TAG, "onPreview (ageCode | genderCode | face3DAngleCode | livenessCode): " + (ageCode | genderCode | face3DAngleCode | livenessCode));

                //有其中一个的错误码不为0，return
                if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
                    return;
                }
                if (faceRectView != null && drawHelper != null) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        drawInfoList.add(new DrawInfo(faceInfoList.get(i).getRect(), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), faceLivenessInfoList.get(i).getLiveness(), null));
                    }
                    Log.d(TAG, "onPreview drawHelper.draw()");
                    drawHelper.draw(faceRectView, drawInfoList);
                }






                final Bitmap bitmap = nv21ToBitmap(nv21, previewSize.width, previewSize.height);
                final Bitmap bitmap2 = rotateBitmap(bitmap, info.orientation);
                Bitmap bitmap3 = null;

                if (!ifGettingFace) {
                    callback.tvSearchFaceSet("");
                }
                callback.tvDescribeSet("");

                callback.tvDescribeAppend("预览原图宽高及像素：" + previewSize.width + "   " + previewSize.height + "   " + previewSize.width * previewSize.height + "\n");
                callback.tvDescribeAppend("预览正方形宽高及像素：" + arcsoftFrame.getWidth() + "   " + arcsoftFrame.getHeight() + "    " + arcsoftFrame.getWidth() * arcsoftFrame.getHeight() + "\n");

                callback.imageOneAndTwo(bitmap, bitmap2);
                callback.imageThreeAndFour(null, null);


                Log.i(TAG, "onPreview: faceInfoList.size()" + faceInfoList.size());

                if (faceInfoList.size() > 0) {

                    Log.i(TAG, "xxxxxx1");


                    int maxFaceNum = getMaxFaceInfoIndex(faceInfoList);
                    FaceInfo maxFaceInfo = faceInfoList.get(maxFaceNum);

                    final Rect rect = maxFaceInfo.getRect();


                    double p1 = ((double) (rect.height() * rect.width()) / (double) (previewView.getHeight() * previewView.getWidth())) / ((double) previewPercent / 100);


                    boolean isFaceBiggerPercentPreview = rect.height() * rect.width() > (previewView.getHeight() * previewView.getWidth()) * previewPercent / 100.0;
                    boolean isFaceBiggerPercentSquare = rect.height() * rect.width() > (arcsoftFrame.getWidth() * arcsoftFrame.getHeight()) * squarePercent / 100.0;
                    if (!isFaceBiggerPercentPreview || !isFaceBiggerPercentSquare) {
                        if (!ifGettingFace)
                            callback.tvSearchFaceAppend("人脸偏移：偏后" + "\n");
                    }


                    Log.e(TAG, "xxxxx  p1: " + p1);
                    Log.e(TAG, "xxxxx isFaceBiggerPercentPreview=" + isFaceBiggerPercentPreview);
                    Log.e(TAG, "xxxxx isFaceBiggerPercentSquare= " + isFaceBiggerPercentSquare);


                    if (isFaceInPreview(rect, previewSize)) {
                        Log.i(TAG, "xxxxxx");


                        // TODO 1 直接copy前面的图片   OR  2 直接把rotate和flip一次性做完matrix
                        bitmap3 = rotateBitmap(nv21ToFace(nv21, previewSize.width, previewSize.height, rect), info.orientation);


                        callback.imageThreeAndFour(bitmap3, Util.fanZhuanBitmap(bitmap3));
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

//                        tvDecribe.append("预览原图头像中心坐标：" + previewRect.centerX() + "   " + previewRect.centerY() + "\n");


//                        }


                        int changeX = rect.width() / 4;
                        int changeY = rect.height() / 4;
                        // TODO
//                        Rect smallrect = new Rect(rect);
//                        smallrect.intersect(changeX, changeY, -changeX, -changeY);

                        Rect smallrect = new Rect(rect.left + changeX, rect.top + changeY, rect.right - changeX, rect.bottom - changeY);


                        final boolean ifcenter = smallrect.contains((int) (previewSize.width / 2), (int) (previewSize.height / 2));


                        callback.tvDescribeAppend("头像宽高及面积：" + rect.width() + "    " + rect.height() + "    " + rect.width() * rect.height() + "\n");
                        callback.tvDescribeAppend("预览原图头像中心坐标：" + previewRect.centerX() + "   " + previewRect.centerY() + "\n");
                        callback.tvDescribeAppend("是否靠近中心：" + ifcenter + "\n");
//                        tvDecribe.append("是否靠近中心：" + ifcenter + "\n");
                        Log.i(TAG, "ccccc =" + previewSize.width + "    " + previewRect.centerX() + "    rect = " + rect.left + "        " + rect.top + "        " + rect.right + "        " + rect.bottom);
                        if (!ifcenter && !ifGettingFace) {
                            //TODO
                            if (previewSize.width / 2 < previewRect.centerX()) {

                                callback.tvSearchFaceAppend("人脸偏移：左" + "\n");
//                            tvDecribe.append("人脸偏移：左" + "\n");

                            } else {
                                callback.tvSearchFaceAppend("人脸偏移：右" + "\n");
//                            tvDecribe.append("人脸偏移：右" + "\n");

                            }
                        }


                        // TODO huoti
                        if (!searching)
                            if (ifcenter) {
                                if (isFaceBiggerPercentPreview)
                                    if (isFaceBiggerPercentSquare)
                                        if ((livenessDetect && faceLivenessInfoList.get(maxFaceNum).getLiveness() == LivenessInfo.ALIVE) || !livenessDetect) {
                                            searching = true;

                                            final Bitmap finalBitmap = bitmap3;

                                            final Bitmap bitmap5 = finalBitmap;
                                            // TODO
                                            bitmap6 = Util.fanZhuanBitmap(finalBitmap);


                                            //TODO 暂时关闭
                                            callback.imageFiveAndSix(bitmap5, bitmap6);


                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    Log.i(TAG, "run: here");
                                                    searcher.setCallback(new YZWSearcher.Callback() {
                                                        @Override
                                                        public void onSearchSuccessCallback(CompareResult compareResult) {
                                                            callback.onPreviewSearchFacesuccess(compareResult);
                                                            cameraHelper.stop();
                                                            searching = false;
                                                        }

                                                        @Override
                                                        public void onSearchFailCallback() {
                                                            callback.onPreviewSearchFaceFail(bitmap6);
                                                            cameraHelper.stop();
                                                            searching = false;


                                                        }

                                                        @Override
                                                        public void onSearchingCallback() {
                                                            callback.onPreviewSearching(bitmap6);
                                                            cameraHelper.stop();
                                                            searching = false;
                                                        }
                                                    });
                                                    searcher.search(nv21);


                                                    ///////////////////////////////////////////////////////////////


                                                    /////////////////////////////////////////////////////////////////
//                                                    if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
//                                                        Log.i(TAG, "run: rrrrr");
//                                                        for (int i = 0; i < facePreviewInfoList.size(); i++) {
//                                                            if (livenessDetect) {
//                                                                livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
//                                                            }
//                                                            /**
//                                                             * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
//                                                             * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
//                                                             */
//
//
//                                                            Log.i(TAG, "run: requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) = " + requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()));
//                                                            //关闭条件,使在屏幕中已识别的人脸可以再次识别
////                                                if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
////                                                        || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
//                                                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
//                                                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//                                                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());
////                                                }
//
//
//                                                        }
//                                                    }
                                                    ///////////////////////////////////////////////////////////////////

                                                }
                                            };

                                            thread = new Thread(runnable);
                                            thread.start();
                                            Log.i(TAG, "onPreview: thread run");
                                        }

                            }


                    }
                } else {
//                    callback.onFaceInfoList(null);
                    callback.imageThreeAndFour(null, null);
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

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(windowManager.getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }

    private static int getMaxFaceInfoIndex(List<FaceInfo> faceInfoList) {

        Log.d(TAG, "getMaxFaceNum faceInfoList: " + faceInfoList.size());

        FaceInfo maxFaceInfo = null;
        int maxFaceInfoIndex = -1;

        for (int i = 0; i < faceInfoList.size(); i++) {
            if (maxFaceInfo == null) {
                maxFaceInfo = faceInfoList.get(i);
                maxFaceInfoIndex = 0;
                continue;
            }

            if (faceInfoList.get(i).getRect().width() * faceInfoList.get(i).getRect().height() >= maxFaceInfo.getRect().height() * maxFaceInfo.getRect().width()) {
                maxFaceInfo = faceInfoList.get(i);
                maxFaceInfoIndex = i;
            }
        }
        return maxFaceInfoIndex;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initEngine();
                initCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }
            } else {
                Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
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
