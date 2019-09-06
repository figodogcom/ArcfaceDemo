package com.arcsoft.arcfacedemo.preview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.google.android.gms.samples.vision.face.facetracker.FaceGraphic;
import com.google.android.gms.samples.vision.face.facetracker.MyFaceDetecter;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class GooglePreview extends YZWPreview {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;
    private GraphicOverlay mGraphicOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    private CameraSourcePreview mPreview;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    FaceGraphic superFaceGraphic;
    FrameLayout googleFrame;
    MyFaceDetecter myFaceDetecter;

    Context context;

    public void onCreate() {
        int rc = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
        myFaceDetecter.setCameraSource(mCameraSource);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void start() {
        super.start();
        int rc = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
//        startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Face Tracker sample")
//                .setMessage(R.string.no_camera_permission)
                .setMessage(R.string.app_name)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    @Override
    public void show() {
        super.show();
        googleFrame.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        super.hide();
        googleFrame.setVisibility(View.INVISIBLE);
    }

    public GooglePreview(View view){
        this.context = view.getContext();
        this.mPreview = view.findViewById(R.id.preview);
        this.mGraphicOverlay = view.findViewById(R.id.faceOverlay);
        this.googleFrame = view.findViewById(R.id.preview_google);

    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions((Activity) context, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

//        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions((Activity) context, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

//        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,     导入失败？？？？？？？？？？？？？？？
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.ok, listener)
//                .show();
    }


    private void createCameraSource() {
//        com.google.android.gms.vision.Frame frame;
//        frame.getBitmap();
//        Frame.Metadata metadata = frame.getMetadata();


//        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        final MyFaceDetecter myFaceDetecter = new MyFaceDetecter(detector , context);

        this.myFaceDetecter = myFaceDetecter;

//        detector.setProcessor(
//                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
//                        .build());

        myFaceDetecter.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
        mCameraSource = new CameraSource.Builder(context, myFaceDetecter)
                .setRequestedPreviewSize(640, 480)
//                .setRequestedPreviewSize(640, 640)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        //TODO 在这里设置
        myFaceDetecter.setCameraSource(mCameraSource);

        myFaceDetecter.setCallback(new MyFaceDetecter.Callback() {
            @Override
            public void onCallback(final Bitmap bitmap, final Bitmap bitmap2, final Bitmap bitmap3, final Bitmap bitmap4) {
                callback.imageOneAndTwo(bitmap,bitmap2);
                callback.imageThreeAndFour(bitmap3,bitmap4);

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        Log.i("yyyyy", String.valueOf(myFaceDetecter.getBit()));
//
//                        Log.i("", "yyyyy bitmap: " + bitmap);
//
//                        imageView.setImageBitmap(bitmap);
//                        imageView2.setImageBitmap(bitmap2);
//                        imageView3.setImageBitmap(bitmap3);
//                        imageView4.setImageBitmap(bitmap4);
//                    }
//
//                });


            }

            @Override
            public void onPreviewSearchTextSet(String string) {
                callback.tvSearchFaceSet(string);
            }

            @Override
            public void onPreviewSearchTextAppend(String string) {
                callback.tvSearchFaceAppend(string);
            }

            @Override
            public void onPreviewDiscribeAppend(String string) {
                callback.tvDescribeAppend(string);
            }

            @Override
            public void onPreviewDiscribeSet(String string) {
                callback.tvDescribeSet(string);
            }

//            @Override
//            public void onPreviewSearchingOrFail(Bitmap bitmap, String string) {
//                callback.tvSearchFaceSearchingOrFail(bitmap,string);
//            }
//
//            @Override
//            public void onPreviewSearchSuccess(CompareResult compareResult) {
//                callback.tvSearchFacesuccess(compareResult);
//            }
        });
    }


    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        //提示的dialog，关闭
//        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
//                context);
//        if (code != ConnectionResult.SUCCESS) {
//            Dialog dlg =
//                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
//            dlg.show();
//        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }



    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            superFaceGraphic = mFaceGraphic;
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

}