package com.arcsoft.arcfacedemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.arcsoft.arcfacedemo.R;

public class PreviewSearching extends ConstraintLayout {
    ImageView imgSearchingFace;
    TextView tvSearchingMessage;

    public PreviewSearching(Context context) {
        super(context);
        init(context);
    }

    public PreviewSearching(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PreviewSearching(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context){
        inflate(context, R.layout.preview_searching,this);
        imgSearchingFace = findViewById(R.id.img_preview_register_face);
        tvSearchingMessage = findViewById(R.id.tv_preview_searching_message);
    }

    public void bindData(Bitmap bitmap){
        imgSearchingFace.setImageBitmap(bitmap);
    }


}
