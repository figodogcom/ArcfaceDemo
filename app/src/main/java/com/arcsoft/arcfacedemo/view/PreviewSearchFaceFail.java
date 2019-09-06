package com.arcsoft.arcfacedemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.arcsoft.arcfacedemo.R;

public class PreviewSearchFaceFail extends ConstraintLayout {
    ImageView imgSearchFailFace;
    TextView tvTime;
    ImageView backToHome;
    SearchFailTimeDelayer searchFailTimeDelayer;

    public PreviewSearchFaceFail(Context context) {
        super(context);
        init(context);
    }

    public PreviewSearchFaceFail(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public PreviewSearchFaceFail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    void init(Context context){
        inflate(context, R.layout.preview_search_fail,this);
        imgSearchFailFace = findViewById(R.id.img_preview_search_fail_face);
        tvTime = findViewById(R.id.tv_preview_fail_timer);
        backToHome = findViewById(R.id.img_preview_fail_back);
        backToHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickBack();
            }
        });

        searchFailTimeDelayer = new SearchFailTimeDelayer(5 * 1000 , 500);
    }

    public void bindData(Bitmap bitmap){
        imgSearchFailFace.setImageBitmap(bitmap);


    }

    public void show(){
        this.setVisibility(VISIBLE);
        searchFailTimeDelayer.start();
    }

    public void hide(){
        this.setVisibility(INVISIBLE);
        searchFailTimeDelayer.cancel();
    }



    public interface Callback{
        void onClickBack();
    }

    private Callback callback;

    public void setCallback(Callback callback){
        this.callback = callback;
    }



    class SearchFailTimeDelayer extends CountDownTimer {
        public SearchFailTimeDelayer(long millisInFuture, long countDownInterval) {
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
