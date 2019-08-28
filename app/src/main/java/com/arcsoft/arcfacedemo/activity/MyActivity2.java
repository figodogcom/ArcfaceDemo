package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.getCameraInfo;

public class MyActivity2 extends PreviewActivity {
    private static final String TAG = "PreviewActivity";
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int afCode = -1;
    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    private FrameLayout frameView;
    private ImageView imageView;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private TextView tvDecribe;

    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    private FaceRectView faceRectView;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Log.i(TAG, "onCreate: 22222");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        previewView = findViewById(R.id.texture_preview);
        faceRectView = findViewById(R.id.face_rect_view);
        frameView = findViewById(R.id.frame_view);
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        imageView = findViewById(R.id.imageview);
        imageView2 = findViewById(R.id.imageview2);
        imageView3 = findViewById(R.id.imageview3);
        imageView4 = findViewById(R.id.imageview4);
        tvDecribe = findViewById(R.id.tv_describe);

    }

    protected void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraListener cameraListener = new CameraListener() {
            android.hardware.Camera.CameraInfo info;


            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: " + cameraId + "  " + displayOrientation + " " + isMirror);
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror);

                info = new android.hardware.Camera.CameraInfo();

            }


            @Override
            public void onPreview(byte[] nv21, Camera camera) {

//                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                getCameraInfo(cameraID, info);

                Bitmap bitmap = nv21ToBitmap(nv21, previewSize.width, previewSize.height);
                Bitmap bitmap2 = rotateBitmap(bitmap, info.orientation);
                Bitmap bitmap3 = null;
                tvDecribe.setText("");
                tvDecribe.append("预览原图宽高及像素：" + previewSize.width + "   " + previewSize.height + "   " + previewSize.width * previewSize.height + "\n");
                tvDecribe.append("预览正方形宽高及像素：" + frameView.getWidth()  + "   " + frameView.getHeight() + "    " + frameView.getWidth() * frameView.getHeight() +"\n");
//                tvDecribe.append("预览正方形像素：" + l.width  + "   " + l.height + "\n");


                imageView.setImageBitmap(bitmap);
                imageView2.setImageBitmap(bitmap2);


                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
//
//
//                if (faceInfoList != null && faceInfoList.size() > 0) {
//
//                    FaceInfo facemax = null;
//                    for (FaceInfo faceInfo : faceInfoList) {
//                        if (facemax == null) {
//                            facemax = faceInfo;
//                            continue;
//                        }
//
//                        if (faceInfo.getRect().width() * faceInfo.getRect().height() >= facemax.getRect().height() * faceInfo.getRect().width())
//                            facemax = faceInfo;
//
//                    }
//
//                    Rect rect = facemax.getRect();
//
//
//                    if (rect.left >= 0
//                            && rect.top >= 0
//                            && rect.bottom <= bitmap2.getHeight()
//                            && rect.right <= bitmap2.getWidth()
//                    ) {
//                        Log.i(TAG, "ccccc:bitmap2 " + bitmap.getWidth() + "   " + bitmap.getHeight());
//                        Log.i(TAG, "ccccc:preview " + previewSize.width + "    " + previewSize.height);
//                        bitmap3 = rotateBitmap(nv21ToFace(nv21, previewSize.width, previewSize.height, rect), info.orientation);
//                        Log.i(TAG, "xxxxx: " + info.orientation);
//                        imageView3.setImageBitmap(bitmap3);
//                        imageView4.setImageBitmap(fanZhuanBitmap(bitmap3));
//                        tvDecribe.append("头像宽高及面积：" + rect.width() + "    " + rect.height() + "    " + rect.width() * rect.height() + "\n");
//                        Log.i(TAG, "xxxxx " + (drawHelper == null) + "   " + (drawHelper.getRealrect() == null));
//
//                        if (drawHelper != null && drawHelper.getRealrect() != null) {
//                            tvDecribe.append("预览原图头像中心坐标：" + drawHelper.getRealrect().centerX() + "   " + drawHelper.getRealrect().centerY() + "\n");
//
//                        }
//                    }
//                } else {
//                    imageView3.setImageBitmap(null);
//                    imageView4.setImageBitmap(null);
//                }


                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                    if (code != ErrorInfo.MOK) {
                        return;
                    }
                } else {
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
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }




    private static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
//            if(image == null){
//                Log.i()
//            }
//
//            if(image == null)
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap == null) {
            Log.i("nnnnn", "bitmap is null");
        }
        return bitmap;
    }

    private static Bitmap nv21ToFace(byte[] nv21, int width, int height, Rect rect) {
        Bitmap bitmap = null;
        YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        Log.i(TAG, "xxxxx: " + rect.left + "    " + rect.top + "    " + rect.right + "   " + rect.bottom);
        Log.i(TAG, "xxxxx: " + image.getWidth() + "    " + image.getHeight());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compressToJpeg(rect, 100, stream);

        bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

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


    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {

        if (bitmap != null) {

            Matrix m = new Matrix();

            m.postRotate(degress);


            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

            return bitmap;

        }

        return bitmap;

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
