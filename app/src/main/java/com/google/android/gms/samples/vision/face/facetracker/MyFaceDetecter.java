package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
//import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.arcsoft.arcfacedemo.common.Util;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import javax.security.auth.callback.Callback;

import static com.arcsoft.arcfacedemo.common.Util.nv21ToBitmap;
import static com.arcsoft.arcfacedemo.common.Util.rotateBitmap;

public class MyFaceDetecter extends Detector<Face> {

    private FaceDetector detector;
    private Context context;

    public MyFaceDetecter(FaceDetector detector, Context context) {
        this.detector = detector;
        this.context = context;
    }

    @Override
    public SparseArray<Face> detect(Frame frame) {


//        ByteBuffer bb = frame.getGrayscaleImageData();
//        byte[] b = new byte[bb.remaining()];  //byte[] b = new byte[bb.capacity()]  is OK
//        bb.get(b, 0, b.length);


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

        Bitmap bitmap = nv21ToBitmap(frame.getGrayscaleImageData().array(), frame.getMetadata().getWidth(), frame.getMetadata().getHeight());

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
