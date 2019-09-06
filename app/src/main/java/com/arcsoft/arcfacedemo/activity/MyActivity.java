package com.arcsoft.arcfacedemo.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.preview.ArcSoftPreview;
import com.arcsoft.arcfacedemo.preview.GooglePreview;
import com.arcsoft.arcfacedemo.preview.YZWPreview;
import com.arcsoft.arcfacedemo.searcher.ArcSoftSearcher;
import com.arcsoft.arcfacedemo.searcher.YZWSearcher;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraHelper;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.view.PreviewSearchFaceFail;
import com.arcsoft.arcfacedemo.view.PreviewSearchFaceSuccess;
import com.arcsoft.arcfacedemo.view.PreviewSearching;
import com.arcsoft.arcfacedemo.widget.ShowFaceInfoAdapter;
import com.arcsoft.face.FaceEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.disposables.CompositeDisposable;

public class MyActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private FaceEngine faceEngine;
    private int afCode = -1;
    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    private FrameLayout frameView;
    private ImageView imageView;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ImageView imageView5;
    private ImageView imageView6;
    private TextView tvDecribe;
    private Button button;
    private TextView tvSearchFace;

    private boolean livenessDetect = true;
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
    SharedPreferences sharedPreferences;
    //    YZWPreview yzwPreview;
    ViewGroup debug_image;
    ViewGroup debug_information;
    PreviewSearchFaceSuccess previewSearchFaceSuccess;
    PreviewSearchFaceFail previewSearchFaceFail;
    PreviewSearching previewSearching;



    //    Boolean alive;
    int previewPercent;
    int squarePercent;
    boolean isDebug;

    SettingPreference settingPreference;
//    private CameraSourcePreview mPreview;
//    private GraphicOverlay mGraphicOverlay;

    private YZWPreview mCurrentPreview;

    int maxFaceNum;

//    Boolean isFaceBiggerPercentPreview;
//    Boolean isFaceBiggerPercentSquare;


//    /**
//     * 相机预览显示的控件，可为SurfaceView或TextureView
//     */
//    private View previewView;
//    private FaceRectView faceRectView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }
        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


        frameView = findViewById(R.id.frame_view);

        imageView = findViewById(R.id.imageview);
        imageView2 = findViewById(R.id.imageview2);
        imageView3 = findViewById(R.id.imageview3);
        imageView4 = findViewById(R.id.imageview4);
        imageView5 = findViewById(R.id.imageview5);
        imageView6 = findViewById(R.id.imageview6);
        tvDecribe = findViewById(R.id.tv_describe);
        button = findViewById(R.id.btn_preview_register_start);
        compareResultList = new ArrayList<>();
        adapter = new ShowFaceInfoAdapter(compareResultList, this);
        tvSearchFace = findViewById(R.id.tv_search_face);
        debug_image = findViewById(R.id.preview_debug_image);
        debug_information = findViewById(R.id.preview_debug_information);


        final YZWPreview mArcSoftPreview = new ArcSoftPreview(findViewById(R.id.preview_arcsoft));
        YZWPreview mGooglePreview = new GooglePreview(findViewById(R.id.preview_google));


        previewSearchFaceSuccess = findViewById(R.id.preview_search_success_view);
        previewSearchFaceFail = findViewById(R.id.preview_search_fail_view);
        previewSearching = findViewById(R.id.preview_searching_view);


        settingPreference = new SettingPreference(this);

        livenessDetect = settingPreference.getPreviewAlive();
        previewPercent = Integer.parseInt(settingPreference.getPreviewPercent());
        squarePercent = Integer.parseInt(settingPreference.getPreviewSquarePercent());
        isDebug = settingPreference.getDebug();


        if (isDebug) {
            debug_image.setVisibility(View.VISIBLE);
            debug_information.setVisibility(View.VISIBLE);
        } else {
            debug_image.setVisibility(View.INVISIBLE);
            debug_information.setVisibility(View.INVISIBLE);
        }

//        Log.i(TAG, "xxxxx = " + previewPercent + "   " + squarePercent + "     " + livenessDetect);


        if (settingPreference.getEngine().equals("arcsoft")) {
//            yzwPreview = new ArcSoftPreview();

            mCurrentPreview = mArcSoftPreview;
            mArcSoftPreview.show();
            mGooglePreview.hide();
        } else {
//            mPreview.setVisibility(View.VISIBLE);
//            yzwPreview = new GooglePreview(this, mPreview,mGraphicOverlay);

            mCurrentPreview = mGooglePreview;
            mArcSoftPreview.hide();
            mGooglePreview.show();
        }

        // TODO start / stop / onCreate


        YZWSearcher searcher = new ArcSoftSearcher(this);
//        YZWSearcher searcher = new CommaTakeSercher();
        mCurrentPreview.setSearcher(searcher);

        mCurrentPreview.setCallback(new YZWPreview.Callback() {
            @Override
            public void imageOneAndTwo(final Bitmap bitmap, final Bitmap bitmap2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        imageView2.setImageBitmap(bitmap2);
                    }
                });

            }

            @Override
            public void imageThreeAndFour(final Bitmap bitmap3, final Bitmap bitmap4) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView3.setImageBitmap(bitmap3);
                        imageView4.setImageBitmap(bitmap4);
                    }
                });

            }

            @Override
            public void imageFiveAndSix(final Bitmap bitmap5, final Bitmap bitmap6) {
                mCurrentPreview.stop();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView5.setImageBitmap(bitmap5);
                        imageView6.setImageBitmap(bitmap6);
                    }
                });

            }

            @Override
            public void tvDescribeAppend(final String string) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDecribe.append(string);
                    }
                });
            }

            @Override
            public void tvDescribeSet(final String string) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDecribe.setText(string);

                    }
                });
            }

            @Override
            public void buttonText(final String string) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText(string);

                    }
                });
            }

            @Override
            public void tvSearchFaceSet(final String string) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvSearchFace.setText(string);

                    }
                });


            }

            @Override
            public void tvSearchFaceAppend(final String string) {
                Log.i(TAG, "xxxxx:tvSearchFaceAppend " + string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvSearchFace.append(string);

                    }
                });

            }

            @Override
            public void onPreviewSearchFacesuccess(final CompareResult compareResult) {
                Log.i(TAG, "OnPreviewSearchFacesuccess: ");
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        previewSearchFaceSuccess.bindData(compareResult);
                        previewSearchFaceSuccess.show();
                        previewSearchFaceFail.hide();
                        previewSearching.setVisibility(View.INVISIBLE);
                    }
                });

            }

            @Override
            public void onPreviewSearchFaceFail(final Bitmap bitmap6) {

                Log.i(TAG, "OnPreviewSearchFaceFail: ");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        previewSearchFaceFail.bindData(bitmap6);
                        previewSearchFaceFail.show();
                        previewSearchFaceSuccess.hide();
                        previewSearching.setVisibility(View.INVISIBLE);
                    }
                });
            }


            @Override
            public void onPreviewSearching(final Bitmap bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        previewSearching.bindData(bitmap);
                        previewSearching.setVisibility(View.VISIBLE);
                        previewSearchFaceSuccess.setVisibility(View.INVISIBLE);
                        previewSearchFaceFail.setVisibility(View.INVISIBLE);
                    }
                });
            }


        });

        mCurrentPreview.onCreate();
        mCurrentPreview.start();

        previewSearchFaceSuccess.setCallback(new PreviewSearchFaceSuccess.Callback() {
            @Override
            public void onClickBack() {
//                mCurrentPreview.stop();
                previewSearchFaceSuccess.hide();
                mCurrentPreview.onCreate();
                mCurrentPreview.start();
            }
        });

        previewSearchFaceFail.setCallback(new PreviewSearchFaceFail.Callback() {
            @Override
            public void onClickBack() {
//                mCurrentPreview.stop();
                previewSearchFaceFail.hide();
                mCurrentPreview.onCreate();
                mCurrentPreview.start();
            }
        });

//        yzwPreview = new ArcSoftPreview(this);
//        yzwPreview = new GooglePreview(this,mPreview,mGraphicOverlay);


//        yzwPreview.init();


//        yzwPreview.start();


//        WindowManager wm = getWindowManager();
//        Display d = wm.getDefaultDisplay();

//        ViewGroup.LayoutParams l = frameView.getLayoutParams();
//        l.width = d.getWidth() / 2;
//        l.height = d.getWidth() / 2;


    }


    @Override
    protected void onDestroy() {


//        searching = false;
//        thread.interrupt();
        // TODO
//        unInitEngine();
        super.onDestroy();

        mCurrentPreview.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ppppp5");
        mCurrentPreview.onResume();
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

//    private void initCamera() {
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        // TODO
//        final android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//        getCameraInfo(cameraID, info);
//
//        final FaceListener faceListener = new FaceListener() {
//            @Override
//            public void onFail(Exception e) {
//                Log.e(TAG, "onFail: " + e.getMessage());
//            }
//
//            //请求FR的回调
//            @Override
//            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
//                if (faceFeature == null) {
//                    Log.i(TAG, "wwwww: ");
//
//                }
////                Log.i(TAG, "wwwww0: " + faceFeature.getFeatureData());
//
//                //FR成功
//                if (faceFeature != null) {
////                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
//
//                    //不做活体检测的情况，直接搜索
//                    if (!livenessDetect) {
//                        Log.i(TAG, "wwwww1");
//
//                        //TODO 暂时关了
////                        searchFace(faceFeature, requestId);
//                    }
//                    //活体检测通过，搜索特征
//                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {
//
//                        Log.i(TAG, "wwwww2");
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：活体" + "\n");
//
//                            }
//                        });
//
//                        //TODO 暂时关了
////                        searchFace(faceFeature, requestId);
//                    }
//                    //活体检测未出结果，延迟100ms再执行该函数
//                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.UNKNOWN) {
//
//
////                        getFeatureDelayedDisposables.add(Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
////                                .subscribe(new Consumer<Long>() {
////                                    @Override
////                                    public void accept(Long aLong) {
//                        Log.i(TAG, "wwwww3");
////                                        onFaceFeatureInfoGet(faceFeature, requestId);
////                                        searching = true;
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：活体未能识别" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });
//
//                        searching = false;
//
////                                    }
////                                }));
//                    }
//                    //活体检测失败
//                    else {
//                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：非活体" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });
//
//                        searching = false;
//
//                    }
//
//                }
//                //FR 失败
//                else {
//                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            tvSearchFace.append("识别结果：FR失败" + "\n");
//                            button.setText("启动识别");
//                        }
//                    });
//                    searching = false;
//
//                }
//            }
//
//        };
//
//        CameraListener cameraListener = new CameraListener() {
//            List<AgeInfo> ageInfoList;
//            List<GenderInfo> genderInfoList;
//            List<Face3DAngle> face3DAngleList;
//            List<LivenessInfo> faceLivenessInfoList;
//            @Override
//            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
//
//                previewSize = camera.getParameters().getPreviewSize();
//                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
//                        , cameraId, isMirror);
//
//                faceHelper = new FaceHelper.Builder()
//                        .faceEngine(faceEngine)
//                        .frThreadNum(MAX_DETECT_NUM)
//                        .previewSize(previewSize)
//                        .faceListener(faceListener)
//                        //类名换了
//                        .currentTrackId(ConfigUtil.getTrackId(MyActivity.this.getApplicationContext()))
//                        .build();
//
//                ageInfoList = new ArrayList<>();
//                genderInfoList = new ArrayList<>();
//                face3DAngleList = new ArrayList<>();
//                faceLivenessInfoList = new ArrayList<>();
//
//
//            }
//
//
//            @Override
//            public void onPreview(final byte[] nv21, Camera camera) {
//                if (faceHelper != null) {
//                    facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
//
//                }
//
//
//                final Bitmap bitmap = nv21ToBitmap(nv21, previewSize.width, previewSize.height);
//                final Bitmap bitmap2 = rotateBitmap(bitmap, info.orientation);
//                Bitmap bitmap3 = null;
//                tvDecribe.setText("");
//                tvDecribe.append("预览原图宽高及像素：" + previewSize.width + "   " + previewSize.height + "   " + previewSize.width * previewSize.height + "\n");
//                tvDecribe.append("预览正方形宽高及像素：" + frameView.getWidth() + "   " + frameView.getHeight() + "    " + frameView.getWidth() * frameView.getHeight() + "\n");
////                tvDecribe.append("预览正方形像素：" + l.width  + "   " + l.height + "\n");
//
//
//                imageView.setImageBitmap(bitmap);
//                imageView2.setImageBitmap(bitmap2);
//                imageView3.setImageBitmap(null);
//                imageView4.setImageBitmap(null);
//
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (searching == false) {
//                            searching = true;
//                            button.setText("停止识别");
//                            tvSearchFace.setText("");
//                            imageView5.setImageBitmap(null);
//                            imageView6.setImageBitmap(null);
//                        } else if (searching == true) {
//                            searching = false;
//                            button.setText("启动识别");
//                            tvSearchFace.setText("");
////                            thread.stop();
//
//                            imageView5.setImageBitmap(null);
//                            imageView6.setImageBitmap(null);
//
//
//                        }
//
//
//                    }
//                });
//
////                Log.i(TAG, "onPreview: " + "xxxxxxx3");
//
//                if (faceRectView != null) {
//                    faceRectView.clearFaceInfo();
//                }
//                List<FaceInfo> faceInfoList = new ArrayList<>();
//                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
//
//
//                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
//                    code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
//                    if (code != ErrorInfo.MOK) {
//                        return;
//                    }
//                } else {
//                    return;
//                }
//
//                Log.i(TAG, "onPreview: " + "xxxxxxx2");
//
//                int ageCode = faceEngine.getAge(ageInfoList);
//                int genderCode = faceEngine.getGender(genderInfoList);
//                int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
//                int livenessCode = faceEngine.getLiveness(faceLivenessInfoList);
//
//                //有其中一个的错误码不为0，return
//                if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
//                    return;
//                }
//                if (faceRectView != null && drawHelper != null) {
//                    List<DrawInfo> drawInfoList = new ArrayList<>();
//                    for (int i = 0; i < faceInfoList.size(); i++) {
//                        drawInfoList.add(new DrawInfo(faceInfoList.get(i).getRect(), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), faceLivenessInfoList.get(i).getLiveness(), null));
//                    }
//                    drawHelper.draw(faceRectView, drawInfoList);
//                }
//
//                if (faceInfoList != null && faceInfoList.size() > 0) {
//
//                    Log.i(TAG, "xxxxxx1");
//
//                    // TODO extra method
//                    FaceInfo facemax = null;
////                    for (FaceInfo faceInfo : faceInfoList) {
////                        if (facemax == null) {
////                            facemax = faceInfo;
////                            continue;
////                        }
////
////                        if (faceInfo.getRect().width() * faceInfo.getRect().height() >= facemax.getRect().height() * faceInfo.getRect().width()){
////                            facemax = faceInfo;
////                        }
////
////                    }
//
//
//                    for (int i = 0; i < faceInfoList.size(); i++) {
//                        if (facemax == null) {
//                            facemax = faceInfoList.get(i);
//                            continue;
//                        }
//
//                        if (faceInfoList.get(i).getRect().width() * faceInfoList.get(i).getRect().height() >= facemax.getRect().height() * facemax.getRect().width()) {
//                            facemax = faceInfoList.get(i);
//                            maxFaceNum = i;
//                        }
//                    }
//
//
//                    final Rect rect = facemax.getRect();
//
//                    Log.e(TAG, "xxxxx: " + previewPercent + "    " + squarePercent);
//
////                    Log.e(TAG, "" + (previewPercent / 100));
//
//
//
//
//                    double p1 = ((double) (rect.height() * rect.width()) / (double) (previewView.getHeight() * previewView.getWidth())) / ((double) previewPercent / 100);
//
//
//                    boolean isFaceBiggerPercentPreview = rect.height() * rect.width() > (previewView.getHeight() * previewView.getWidth()) * previewPercent / 100.0;
//                    boolean isFaceBiggerPercentSquare = rect.height() * rect.width() > (frameView.getWidth() * frameView.getHeight()) * squarePercent / 100.0;
//
//
//                    Log.e(TAG, "xxxxx  p1: " + p1);
//                    Log.e(TAG, "xxxxx isFaceBiggerPercentPreview=" + isFaceBiggerPercentPreview);
//                    Log.e(TAG, "xxxxx isFaceBiggerPercentSquare= " + isFaceBiggerPercentSquare);
//
//
//                    if (isFaceInPreview(rect, previewSize)) {
//                        Log.i(TAG, "xxxxxx");
//
//
//                        // TODO 1 直接copy前面的图片   OR  2 直接把rotate和flip一次性做完matrix
//                        bitmap3 = rotateBitmap(nv21ToFace(nv21, previewSize.width, previewSize.height, rect), info.orientation);
//                        imageView3.setImageBitmap(bitmap3);
//                        imageView4.setImageBitmap(Util.fanZhuanBitmap(bitmap3));
//
//
//                        tvDecribe.append("头像宽高及面积：" + rect.width() + "    " + rect.height() + "    " + rect.width() * rect.height() + "\n");
//
////                        if (drawHelper != null && drawHelper.getRealrect() != null) {
//                        Rect previewRect = Util.adjustRect(
//                                rect,
//                                previewSize.width,
//                                previewSize.height,
//                                previewView.getWidth(),
//                                previewView.getHeight(),
//                                info.orientation,
//                                cameraID,
//                                false,
//                                false,
//                                false
//
//                        );
//
////                            tvDecribe.append("预览原图头像中心坐标：" + drawHelper.getRealrect().centerX() + "   " + drawHelper.getRealrect().centerY() + "\n");
//                        tvDecribe.append("预览原图头像中心坐标：" + previewRect.centerX() + "   " + previewRect.centerY() + "\n");
//
//
////                        }
//
//
//                        int changeX = rect.width() / 4;
//                        int changeY = rect.height() / 4;
//                        // TODO
//                        Rect smallrect = new Rect(rect);
//                        smallrect.intersect(changeX, changeY, -changeX, changeY);
//
////                        Rect smallrect = new Rect(rect.left + changeX, rect.top + changeY, rect.right - changeX, rect.bottom - changeY);
//
//
//                        final boolean ifcenter = smallrect.contains((int) (previewSize.width / 2), (int) (previewSize.height / 2));
//                        tvDecribe.append("是否靠近中心：" + ifcenter + "\n");
//
//
////                        if (!ifcenter && drawHelper != null) {
//                        if (previewSize.width / 2 > previewRect.centerX()) {
//                            tvDecribe.append("人脸偏移：左" + "\n");
//
//                        } else {
//                            tvDecribe.append("人脸偏移：右" + "\n");
//
//                        }
////                        }
//
//
//                        if (searching == true) {
//
//                            // TODO huoti
//                            if (ifcenter) {
//                                if (isFaceBiggerPercentPreview)
//                                    if (isFaceBiggerPercentSquare)
//                                        if ((livenessDetect && faceLivenessInfoList.get(maxFaceNum).getLiveness() == LivenessInfo.ALIVE) || !livenessDetect) {
//
//                                            searching = false;
//
//
//                                            final Bitmap finalBitmap = bitmap3;
//                                            Runnable runnable = new Runnable() {
//                                                @Override
//                                                public void run() {
//
////                                        if (searching == false) {
////                                            return;
////                                        }
//
//                                                    // byte[] nv21Clone = nv21.clone();
//
////                                        // TODO
////                                        final Bitmap bitmap5 = rotateBitmap(nv21ToBitmap(nv21, previewSize.width, previewSize.height), info.orientation);
////                                        final Bitmap bitmap6 = Util.fanZhuanBitmap(rotateBitmap(nv21ToFace(nv21, previewSize.width, previewSize.height, rect), info.orientation));
//
//                                                    runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            imageView5.setImageBitmap(bitmap2);
//                                                            imageView6.setImageBitmap(Util.fanZhuanBitmap(finalBitmap));
//                                                            Log.i(TAG, "run: imageview");
//                                                        }
//                                                    });
//
//                                                    // TODO
////                                        clearLeftFace(facePreviewInfoList);
//
//
//                                                    Log.i(TAG, "run:facePreviewInfoList.size()= " + facePreviewInfoList.size());
////
////                                        if (searching == false) {
////                                            return;
////                                        }
//
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
//
//
//                                                }
//                                            };
//
//                                            thread = new Thread(runnable);
//                                            thread.start();
//                                        } else {
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    tvSearchFace.append("识别结果：非活体");
//                                                    searching = false;
//                                                }
//                                            });
//                                        }
//
//                            }
//                        }
//
//
//                    }
//                } else {
//                    imageView3.setImageBitmap(null);
//                    imageView4.setImageBitmap(null);
//                }
//
//
////                clearLeftFace(facePreviewInfoList);
//
////                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
////
////                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
////                        if (livenessDetect) {
////                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
////                        }
////                        /**
////                         * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
////                         * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
////                         */
////                        if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
////                                || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
////                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
////                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//////                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());
////                        }
////                    }
////                }
//
//
//            }
//
//            private boolean isFaceInPreview(Rect rect, Camera.Size previewSize) {
//
//                if (rect.left >= 0
//                        && rect.top >= 0
//                        && rect.bottom <= previewSize.height
//                        && rect.right <= previewSize.width)
//                    return true;
//                else return false;
//            }
//
//            @Override
//            public void onCameraClosed() {
//                Log.i(TAG, "onCameraClosed: ");
//            }
//
//            @Override
//            public void onCameraError(Exception e) {
//                Log.i(TAG, "onCameraError: " + e.getMessage());
//            }
//
//            @Override
//            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
//                if (drawHelper != null) {
//                    drawHelper.setCameraDisplayOrientation(displayOrientation);
//                }
//                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
//            }
//        };
//        cameraHelper = new CameraHelper.Builder()
//                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
//                .rotation(getWindowManager().getDefaultDisplay().getRotation())
//                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
//                .isMirror(false)
//                .previewOn(previewView)
//                .cameraListener(cameraListener)
//                .build();
//        cameraHelper.init();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mCurrentPreview.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
//            Log.i(TAG, "onRequestPermissionsResult: ppppp");
//            boolean isAllGranted = true;
//            for (int grantResult : grantResults) {
//                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
//            }
//            if (isAllGranted) {
////                if (settingPreference.getEngine().equals("arcsoft")) {
////                    yzwPreview = new ArcSoftPreview(this);
////
////                } else {
////                    mPreview.setVisibility(View.VISIBLE);
////                    yzwPreview = new GooglePreview(this, mPreview, mGraphicOverlay);
////
////                }
////                setcallback();
//                Log.i(TAG, "onRequestPermissionsResult: ppppp2");
//
//                yzwPreview.init();
//                yzwPreview.start();
////                initEngine();
////                initCamera();
////                yzwPreview.start();
//
////                if (cameraHelper != null) {
////                    cameraHelper.start();
////                }
//            } else {
//                Toast.makeText(this.getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
//            }
//        }
    }




//    private void searchFace(final FaceFeature frFace, final Integer requestId) {
//
//        Observable
//                .create(new ObservableOnSubscribe<CompareResult>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
////                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
//
//                        final CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//
////                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
//                        if (compareResult == null) {
//
//                            emitter.onError(null);
//                        } else {
//                            emitter.onNext(compareResult);
//                        }
//                    }
//                })
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<CompareResult>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(final CompareResult compareResult) {
//
//                        if (compareResult == null || compareResult.getUserName() == null) {
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
//                            return;
//                        }
//
////                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
//                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
//
//
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
//
//
////                            boolean isAdded = false;
////                            if (compareResultList == null) {
////                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
////                                faceHelper.addName(requestId, "VISITOR " + requestId);
////                                return;
////                            }
////                            for (CompareResult compareResult1 : compareResultList) {
////                                if (compareResult1.getTrackId() == requestId) {
////                                    isAdded = true;
////                                    break;
////                                }
////                            }
////                            if (!isAdded) {
////                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
////                                if (compareResultList.size() >= MAX_DETECT_NUM) {
////                                    compareResultList.remove(0);
////                                    adapter.notifyItemRemoved(0);
////                                }
////                                //添加显示人员时，保存其trackId
////                                compareResult.setTrackId(requestId);
////                                compareResultList.add(compareResult);
////                                adapter.notifyItemInserted(compareResultList.size() - 1);
////                            }
////                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
////                            faceHelper.addName(requestId, compareResult.getUserName());
//
//                        } else {
//                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                            faceHelper.addName(requestId, "VISITOR " + requestId);
//
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tvSearchFace.append("识别结果：人脸未注册" + "\n");
//                                    tvSearchFace.append("识别分数：" + compareResult.getSimilar() + "\n");
//                                    button.setText("启动识别");
//                                    searching = false;
//                                }
//                            });
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                        searching = false;
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvSearchFace.append("识别结果：人脸未注册" + "\n");
//                                button.setText("启动识别");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }


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


//    private Face getMaxFace(SparseArray<Face> sparseArrayFace) {
//        Face faceMax = null;
//
//        for (int i = 0; i < sparseArrayFace.size(); i++) {
//            if (faceMax == null) {
//                faceMax = sparseArrayFace.valueAt(i);
//                continue;
//            }
//            Face face = sparseArrayFace.valueAt(i);
//            if (face.getWidth() * face.getHeight() > faceMax.getHeight() * faceMax.getWidth())
//                faceMax = sparseArrayFace.valueAt(i);
//        }
//
//        return faceMax;
//    }
//
//    private int getDegrees(int rotation) {
//        switch (rotation) {
//            case Frame.ROTATION_0:
//                return 0;
//            case 1:
//                return 90;
//            case 2:
//                return 180;
//            case 3:
//                return 270;
//        }
//        return 0;
//    }


}
