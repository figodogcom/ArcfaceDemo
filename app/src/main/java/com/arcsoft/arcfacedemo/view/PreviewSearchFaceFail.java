package com.arcsoft.arcfacedemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.arcsoft.arcfacedemo.R;

public class PreviewSearchFaceFail extends ConstraintLayout {
    ImageView imgSearchFailFace;
    TextView tvSearchFailMessage;

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
    }

    public void bindData(Bitmap bitmap){
        imgSearchFailFace.setImageBitmap(bitmap);


    }
}
