package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
//import android.support.annotation.NonNull;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.SettingPreference;
import com.arcsoft.arcfacedemo.common.Util;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.searcher.YZWSearcher;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.ImageUtil;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.arcfacedemo.util.face.RequestFeatureStatus;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.arcsoft.arcfacedemo.common.Util.nv21ToBitmap;
import static com.arcsoft.arcfacedemo.common.Util.rotateBitmap;
import static com.google.android.gms.internal.zzs.TAG;

public class MyFaceDetecter extends Detector<Face> {

    private FaceDetector detector;
    private Context context;

    boolean livenessDetect;
    int previewPercent;
    int squarePercent;
    int width;
    boolean ifcenter;
    private int afCode = -1;


    private FaceEngine faceEngine;
    private static final int MAX_DETECT_NUM = 10;
    FaceListener faceListener;
    private FaceHelper faceHelper;
    List<FacePreviewInfo> facePreviewInfoList;

    private YZWSearcher searcher;

    public void setSearcher(YZWSearcher searcher) {
        this.searcher = searcher;
    }

    public MyFaceDetecter(FaceDetector detector, Context context) {
        this.detector = detector;
        this.context = context;
        SettingPreference settingPreference = new SettingPreference(context);
        livenessDetect = settingPreference.getPreviewAlive();
        previewPercent = Integer.parseInt(settingPreference.getPreviewPercent());
        squarePercent = Integer.parseInt(settingPreference.getPreviewSquarePercent());
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        FaceServer.getInstance().init(context);
        initEngine();
        initFaceLister();
    }

    private CameraSource cameraSource;

    ///////////////////
    public void setCameraSource(CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    private void initEngine() {
        faceEngine = new FaceEngine();
        //增加了FaceEngine.ASF_FACE_RECOGNITION
        afCode = faceEngine.init(context, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                16, 20, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
        if (afCode != ErrorInfo.MOK) {
            //TODO 暂时关闭
//            Toast.makeText(context, context.getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }
    }

    private void initFaceLister() {
        faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
                Log.i(TAG, "fffff: " + faceFeature.getFeatureData());
//                if (faceFeature == null) {
//                    Log.i(TAG, "wwwww: ");
//
//                }
//
//
//                callback.tvSearchFaceSet("正在识别");
//                callback.tvSearchFaceSearchingOrFail(bitmap6, "正在搜索");
//
//                try {
//                    Thread.sleep(3 * 1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
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
//                        searchFace(faceFeature, requestId);
//                    }
//                    //活体检测通过，搜索特征
//                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {
//
//                        Log.i(TAG, "wwwww2");
//
//                        callback.tvSearchFaceAppend("识别结果：活体" + "\n");
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                tvSearchFace.append("识别结果：活体" + "\n");
////
////                            }
////                        });
//
//
//                        searchFace(faceFeature, requestId);
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
//
//                        callback.tvSearchFaceSearchingOrFail(bitmap6, "识别失败");
//
////                        callback.tvSearchFaceAppend("识别结果：活体未能识别" + "\n");
////                        callback.buttonText("启动识别");
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                tvSearchFace.append("识别结果：活体未能识别" + "\n");
////                                button.setText("启动识别");
////                            }
////                        });
//
//                        searching = false;
//
////                                    }
////                                }));
//                    }
//                    //活体检测失败
//                    else {
//                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);
//                        callback.tvSearchFaceSearchingOrFail(bitmap6, "识别失败");
//
////                        callback.tvSearchFaceAppend("识别结果：非活体" + "\n");
////                        callback.buttonText("启动识别");
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                tvSearchFace.append("识别结果：非活体" + "\n");
////                                button.setText("启动识别");
////                            }
////                        });
//
//                        searching = false;
//
//                    }
//
//                }
//                //FR 失败
//                else {
//                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                    callback.tvSearchFaceSearchingOrFail(bitmap6, "识别失败");
//
////                    callback.tvSearchFaceAppend("识别结果：FR失败" + "\n");
////                    callback.buttonText("启动识别");
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
//////                            tvSearchFace.append("识别结果：FR失败" + "\n");
////                            button.setText("启动识别");
////                        }
////                    });
//                    searching = false;
//
//                }
            }

        };

    }

    private List<FaceInfo> faceInfoList = new ArrayList<>();

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
    public SparseArray<Face> detect(Frame frame) {
        //////////////////////////////////
        byte[] nv21 = frame.getGrayscaleImageData().array();
        Frame.Metadata metadata = frame.getMetadata();

        Log.d(TAG, "metadata.getWidth(): " + metadata.getWidth());
        Log.d(TAG, "metadata.getHeight(): " + metadata.getHeight());


//        if (faceHelper == null) {
////            Camera camera = cameraSource.getCamera();
////
////            if (camera != null) {
////                Camera.Size previewSize = camera.getParameters().getPreviewSize();
//
//            Camera.Size previewSize = newInstance(Camera.Size.class, metadata.getWidth(), metadata.getHeight());
//
//            Log.d(TAG, "previewSize: " + previewSize);
//            Log.d(TAG, "previewSize.width: " + previewSize.width);
//            Log.d(TAG, "previewSize.height: " + previewSize.height);
//
//            faceHelper = new FaceHelper.Builder()
//                    .faceEngine(faceEngine)
//                    .frThreadNum(MAX_DETECT_NUM)
//                    .previewSize(previewSize)
//                    .faceListener(faceListener)
//                    //类名换了
//                    .currentTrackId(ConfigUtil.getTrackId(context))
//                    .build();
////            }
//        }


        if (faceHelper != null) {
//            facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
//            Log.i(TAG, "detect facePreviewInfoList.size(): " + facePreviewInfoList.size());

//        faceInfoList.clear();
//        int code = faceEngine.detectFaces(nv21, metadata.getWidth(), metadata.getHeight(), FaceEngine.CP_PAF_NV21, faceInfoList);
//        Log.d(TAG, "detect code: " + code);
//
//            faceInfoList.clear();
//            int code = faceEngine.detectFaces(ImageUtil.bitmapToNv21(bitmap2, bitmap2.getWidth(), bitmap2.getHeight()), bitmap2.getWidth(), bitmap2.getHeight(), FaceEngine.CP_PAF_NV21, faceInfoList);
//            Log.d(TAG, "detect code: " + code);

            ////////////////////////////////////////////////TODO
//            if (searcher != null) {
//
////                searcher.setCallback();
//
//                byte[] nv21New = ImageUtil.bitmapToNv21(bitmap2, bitmap2.getWidth(), bitmap2.getHeight());
//                searcher.abc(nv21New);
//            }
//
//
//            facePreviewInfoList = faceHelper.onPreviewFrame(nv21New);
            Log.i(TAG, "detect facePreviewInfoList.size(): " + facePreviewInfoList.size());
            /////////////////////////////////////////////////////////
//            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
        }


//        Log.i(TAG, "ccccc:  " + camera.getParameters().getPreviewSize().height);
//        Camera.Size previewSize = camera.getParameters().getPreviewSize();
//
//        faceHelper = new FaceHelper.Builder()
//                .faceEngine(faceEngine)
//                .frThreadNum(MAX_DETECT_NUM)
//                .previewSize(previewSize)
//                .faceListener(faceListener)
//                //类名换了
//                .currentTrackId(ConfigUtil.getTrackId(context))
//                .build();
        //////////////////////////////////////


//        ByteBuffer bb = frame.getGrayscaleImageData();
//        byte[] b = new byte[bb.remaining()];  //byte[] b = new byte[bb.capacity()]  is OK
//        bb.get(b, 0, b.length);

//        callback.onPreviewTextSet("");
        int frameRealwidth;
        int frameRealheight;
        if (isPortraitMode()) {
            Log.i("ddddd", "true");
            frameRealwidth = frame.getMetadata().getWidth();
            frameRealheight = frame.getMetadata().getHeight();
        } else {

            Log.i("ddddd", "false");
            frameRealwidth = frame.getMetadata().getHeight();
            frameRealheight = frame.getMetadata().getWidth();
        }

        Bitmap bitmap = nv21ToBitmap(nv21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight());
        byte[] nv21xxx = ImageUtil.bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
        boolean equals = Arrays.equals(nv21, nv21xxx);

        Log.d(TAG, "nv21 a length: " + nv21.length);
        Log.d(TAG, "nv21 b length: " + nv21xxx.length);
        Log.d(TAG, "nv21 equals: " + equals);

        Bitmap bitmap2 = rotateBitmap(bitmap, getDegrees(frame.getMetadata().getRotation()));
        Bitmap bitmap3 = null;
        Bitmap bitmap4 = null;


        if (bitmap == null) {
            Log.i("ddddd", "bitmap is null");
        }


        SparseArray<Face> sparseArrayFace = detector.detect(frame);
        Face face = getMaxFace(sparseArrayFace);

        if (face != null
                && face.getPosition().x >= 0
                && face.getPosition().y >= 0
                && face.getPosition().x + face.getWidth() <= bitmap2.getWidth()
                && face.getPosition().y + face.getHeight() <= bitmap2.getHeight()
        ) {
            bitmap3 = Bitmap.createBitmap(bitmap2, (int) face.getPosition().x, (int) face.getPosition().y, (int) face.getWidth(), (int) face.getHeight());
            bitmap4 = Util.fanZhuanBitmap(bitmap3);

//            bitmap4 = fanZhuanBitmap(bitmap3);
        }

        if (face != null) {
            ifcenter = (bitmap2.getWidth() / 2 > (face.getPosition().x) + face.getWidth() * 0.25) && (bitmap2.getWidth() / 2 < (face.getPosition().x + face.getWidth() * 0.75));
            boolean isBiggerPreviewPercent = face.getWidth() * face.getHeight() > ((bitmap.getHeight() * bitmap.getWidth()) * previewPercent) / 100.0;
            boolean isBiggerSquarePercent = face.getWidth() * face.getHeight() > ((width / 2) * (width / 2) * squarePercent) / 100.0;

            Log.i(TAG, "xxxxx: " + ifcenter);

            if (!ifcenter) {
                if (bitmap2.getWidth() / 2 > (face.getPosition().x + face.getWidth() * 0.75)) {
                    callback.onPreviewSearchTextSet("头像已偏右" + "\n");
                    Log.i(TAG, "xxxxx1: ");

                }
                if (bitmap2.getWidth() / 2 < (face.getPosition().x + face.getWidth() * 0.25)) {
                    callback.onPreviewSearchTextSet("头像已偏左" + "\n");
                    Log.i(TAG, "xxxxx:2 ");
                }
            }


            if (!isBiggerPreviewPercent || !isBiggerSquarePercent) {
                callback.onPreviewSearchTextSet("人脸偏移：偏后" + "\n");
            }

            if (isBiggerPreviewPercent && isBiggerSquarePercent && ifcenter) {
                callback.onPreviewSearchTextSet("");
            }
            Log.i(TAG, "xxxxx: " + previewPercent + "    " + squarePercent);

            callback.onPreviewDiscribeSet("预览原图宽高及像素:" + frame.getMetadata().getWidth() + "    " + frame.getMetadata().getHeight() + "    " + frame.getMetadata().getWidth() * frame.getMetadata().getHeight() + "\n"
                    + "预览正方形宽高及像素：" + width / 2 + "    " + width / 2 + "    " + width / 2 * width / 2 + "\n"
                    + "头像宽高及面积:" + (int) face.getWidth() + "    " + (int) face.getHeight() + "    " + (int) (face.getWidth() * face.getHeight()) + "\n"
                    + "预览原图头像中心坐标:" + ((int) face.getPosition().x + (int) face.getWidth() / 2) + "    " + (int) (face.getPosition().y + face.getHeight() / 2) + "\n"
                    + "是否靠近中心:" + ifcenter


            );

            //////////////////////////////////
            if (ifcenter && isBiggerPreviewPercent && isBiggerSquarePercent) {

                byte[] nv21New = ImageUtil.bitmapToNv21(bitmap2, bitmap2.getWidth(), bitmap2.getHeight());

                searcher.search(nv21New);


            }


            //////////////////////////////////


        } else {
            callback.onPreviewSearchTextSet("");
            callback.onPreviewDiscribeSet("预览原图宽高及像素:" + frame.getMetadata().getWidth() + "    " + frame.getMetadata().getHeight() + "    " + frame.getMetadata().getWidth() * frame.getMetadata().getHeight() + "\n"
                    + "预览正方形宽高及像素：" + width / 2 + "    " + width / 2 + "    " + width / 2 * width / 2 + "\n"

            );
        }


//        if (sparseArrayFace.size() != 0 && sparseArrayFace != null ) {
//
//            Face face = sparseArrayFace.valueAt(0);
//
//            if (face != null
//                    && face.getPosition().x >= 0
//                    && face.getPosition().y >= 0
//                    && face.getPosition().x + face.getWidth() <= bitmap2.getWidth()
//                    && face.getPosition().y + face.getHeight() <= bitmap2.getHeight()
//            )
//
//                bitmap3 = Bitmap.createBitmap(bitmap2, (int) face.getPosition().x, (int) face.getPosition().y, (int) face.getWidth(), (int) face.getHeight());
//
//        }


//        switch(frame.getMetadata().getRotation()){
//            case Frame.ROTATION_0 :
//                bitmap2 = bitmap;
//                break;
//            case 1 :
//                bitmap2 = rotateBitmap(bitmap,90);
//                break;
//            case 2 :
//                bitmap2 = rotateBitmap(bitmap,180);
//                break;
//            case 3 :
//                bitmap2 = rotateBitmap(bitmap,270);
//                break;
//        }


//
//        byte[] byteArray = frame.getGrayscaleImageData().array();
//        String sendString;
//        try {
////将byte转为String
//            sendString = new String(byteArray, "UTF-8");
//            try {
////将String转回byte
//                byte[] data = sendString.getBytes("UTF-8");
//// 为UTF8编吗
//// 把二进制图片转成位图
//                YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, 200,
//                        200, null); // 20、20分别是图的宽度与高度
//                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//// 80--JPG图片的质量[0-100],100最高
//                yuvimage.compressToJpeg(new Rect(0, 0, 20, 20), 80, baos2);
//                byte[] jdata = baos2.toByteArray();
//                Bitmap bitmap2 = BitmapFactory.decodeByteArray(jdata, 0,
//                        jdata.length);
//                bit = bitmap2;
//            } catch (UnsupportedEncodingException e) {
//// TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        if (bitmap == null) {
            Log.i("nnnnn", "bitmap is null");
        }
        callback.onCallback(bitmap, bitmap2, bitmap3, bitmap4);

        return sparseArrayFace;
    }

    private Face getMaxFace(SparseArray<Face> sparseArrayFace) {
        Face faceMax = null;

        for (int i = 0; i < sparseArrayFace.size(); i++) {
            if (faceMax == null) {
                faceMax = sparseArrayFace.valueAt(i);
                continue;
            }
            Face face = sparseArrayFace.valueAt(i);
            if (face.getWidth() * face.getHeight() > faceMax.getHeight() * faceMax.getWidth())
                faceMax = sparseArrayFace.valueAt(i);
        }

        return faceMax;
    }

    private int getDegrees(int rotation) {
        switch (rotation) {
            case Frame.ROTATION_0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
        }
        return 0;
    }


//    private static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
//        Bitmap bitmap = null;
//        try {
//            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
//            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//            stream.close();
////            if(image == null){
////                Log.i()
////            }
////
////            if(image == null)
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (bitmap == null) {
//            Log.i("nnnnn", "bitmap is null");
//        }
//        return bitmap;
//    }


    private Callback callback;

    public interface Callback {
        void onCallback(Bitmap bitmap, Bitmap bitmap2, Bitmap bitmap3, Bitmap bitmap4);

        void onPreviewSearchTextSet(String string);

        void onPreviewSearchTextAppend(String string);

        void onPreviewDiscribeAppend(String string);

        void onPreviewDiscribeSet(String string);
    }

    public void setCallback(MyFaceDetecter.Callback callback) {
        this.callback = callback;
    }

//    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
//
//        if (bitmap != null) {
//
//            Matrix m = new Matrix();
//
//            m.postRotate(degress);
//
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
//
//            return bitmap;
//
//        }
//
//        return bitmap;
//
//    }


    private boolean isPortraitMode() {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        return false;
    }

    private Bitmap fanZhuanBitmap(Bitmap sourceBitmap) {
        Matrix m = new Matrix();

        m.setScale(-1, 1);//水平翻转
//            m.setScale(1, -1);//垂直翻转
        int w = sourceBitmap.getWidth();
        int h = sourceBitmap.getHeight();
        //生成的翻转后的bitmap
        Bitmap reversePic = Bitmap.createBitmap(sourceBitmap, 0, 0, w, h, m, true);
        return reversePic;

    }


}
