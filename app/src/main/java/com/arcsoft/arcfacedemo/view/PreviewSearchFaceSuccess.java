package com.arcsoft.arcfacedemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.bumptech.glide.Glide;

import java.io.File;

public class PreviewSearchFaceSuccess extends ConstraintLayout {
    ImageView registerImage;
    TextView preiviewRegisterName;
    TextView previewWelcome;
    Context context;
    TextView tvTime;
    ImageView backtoPreview;
    SearchSuccessTimeDelayer searchSuccessTimeDelayer;

    public PreviewSearchFaceSuccess(Context context) {
        super(context);
        init(context);
    }



    public PreviewSearchFaceSuccess(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PreviewSearchFaceSuccess(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void show(){
        searchSuccessTimeDelayer.start();
        this.setVisibility(VISIBLE);
    }

    public void hide(){
        searchSuccessTimeDelayer.cancel();
        this.setVisibility(INVISIBLE);

    }

    void init(Context context){

        final View rootView = inflate(context,R.layout.preview_suceess,this);
        registerImage = findViewById(R.id.img_preview_register_face);
        preiviewRegisterName = findViewById(R.id.tv_preview_search_register_name);
        previewWelcome = findViewById(R.id.tv_preview_searching_message);
        tvTime = findViewById(R.id.tv_preview_success_timer);
        backtoPreview = findViewById(R.id.img_preview_success_back);

        this.context = context;


        backtoPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickBack();
            }
        });


        searchSuccessTimeDelayer = new SearchSuccessTimeDelayer(10 * 1000, 500);



    }

    public void bindData(Bitmap bitmap, String name){
//            final File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResult.getUserName() + FaceServer.IMG_SUFFIX);
//            Glide.with(registerImage)
//                    .load(imgFile)
//                    .into(registerImage);

        registerImage.setImageBitmap(bitmap);
        preiviewRegisterName.setText(name);
        previewWelcome.setText("欢迎您");

    }

    public interface Callback{
        void onClickBack();
    }

    private Callback callback;

    public void setCallback(Callback callback){
        this.callback = callback;
    }

    class SearchSuccessTimeDelayer extends CountDownTimer {
        public SearchSuccessTimeDelayer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            tvTime.setText((int)((l+1000)/1000) + "秒");

        }

        @Override
        public void onFinish() {
            callback.onClickBack();
        }
    }


}
