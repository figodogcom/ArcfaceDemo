package com.arcsoft.arcfacedemo.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

//import static androidx.constraintlayout.Constraints.TAG;

public class Util {
    //1
    public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
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

    //2
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {

        if (bitmap != null) {

            Matrix m = new Matrix();

            m.postRotate(degress);


            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

            return bitmap;

        }

        return bitmap;

    }

    //3
    public static Bitmap nv21ToFace(byte[] nv21, int width, int height, Rect rect) {
        Bitmap bitmap = null;
        YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);

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

    //4
    public static Bitmap fanZhuanBitmap(Bitmap sourceBitmap) {
        Matrix m = new Matrix();

        m.setScale(-1, 1);//水平翻转
//            m.setScale(1, -1);//垂直翻转
        int w = sourceBitmap.getWidth();
        int h = sourceBitmap.getHeight();
        //生成的翻转后的bitmap
        Bitmap reversePic = Bitmap.createBitmap(sourceBitmap, 0, 0, w, h, m, true);
        return reversePic;

    }

    public static Rect adjustRect(Rect ftRect, int previewWidth, int previewHeight, int canvasWidth, int canvasHeight, int cameraDisplayOrientation, int cameraId,
                            boolean isMirror, boolean mirrorHorizontal, boolean mirrorVertical) {

        if (ftRect == null) {
            return null;
        }
        Rect rect = new Rect(ftRect);

        float horizontalRatio;
        float verticalRatio;
        if (cameraDisplayOrientation % 180 == 0) {
            horizontalRatio = (float) canvasWidth / (float) previewWidth;
            verticalRatio = (float) canvasHeight / (float) previewHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) previewWidth;
            verticalRatio = (float) canvasWidth / (float) previewHeight;
        }
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;
        Rect newRect = new Rect();
        switch (cameraDisplayOrientation) {
            case 0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                } else {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                }
                newRect.top = rect.top;
                newRect.bottom = rect.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - rect.top;
                newRect.left = canvasWidth - rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                } else {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - rect.bottom;
                newRect.bottom = canvasHeight - rect.top;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                } else {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                }
                break;
            case 270:
                newRect.left = rect.top;
                newRect.right = rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                } else {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                }
                break;
            default:
                break;
        }

        /**
         * isMirror mirrorHorizontal finalIsMirrorHorizontal
         * true         true                false
         * false        false               false
         * true         false               true
         * false        true                true
         *
         * XOR
         */
        if (isMirror ^ mirrorHorizontal) {
            int left = newRect.left;
            int right = newRect.right;
            newRect.left = canvasWidth - right;
            newRect.right = canvasWidth - left;
        }
        if (mirrorVertical) {
            int top = newRect.top;
            int bottom = newRect.bottom;
            newRect.top = canvasHeight - bottom;
            newRect.bottom = canvasHeight - top;
        }
        return newRect;
    }
}
