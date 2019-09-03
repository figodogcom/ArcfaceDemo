package com.arcsoft.arcfacedemo.view;

import android.content.Context;
import android.graphics.Bitmap;
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
import java.util.List;

public class PreviewSearchFace extends ConstraintLayout {
    ImageView registerImage;
    TextView preiviewRegisterName;
    TextView previewWelcome;
    Context context;

    public PreviewSearchFace(Context context) {
        super(context);
        init(context);
    }



    public PreviewSearchFace(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PreviewSearchFace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context){

        View rootView = inflate(context,R.layout.preview_suceess,this);
        registerImage = findViewById(R.id.tv_preview_register_image);
        preiviewRegisterName = findViewById(R.id.tv_preview_search_register_name);
        previewWelcome = findViewById(R.id.tv_preview_search_welcome);
        this.context = context;
    }

    public void bindSuccessData(CompareResult compareResult){
            final File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResult.getUserName() + FaceServer.IMG_SUFFIX);
            Glide.with(registerImage)
                    .load(imgFile)
                    .into(registerImage);



        preiviewRegisterName.setText(compareResult.getUserName());
        previewWelcome.setText("欢迎您");

    }

    public void bindFailData(Bitmap bitmap,String string){
        registerImage.setImageBitmap(bitmap);
        previewWelcome.setText(string);

        preiviewRegisterName.setText("");

    }





}
