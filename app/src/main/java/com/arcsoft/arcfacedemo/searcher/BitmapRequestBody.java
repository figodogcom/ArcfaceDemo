package com.arcsoft.arcfacedemo.searcher;

import android.graphics.Bitmap;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class BitmapRequestBody extends RequestBody {

    private Bitmap bitmap;
    private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

    public BitmapRequestBody(Bitmap bitmap){
        this.bitmap = bitmap;
    }


//    @javax.annotation.Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("image/jpeg");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        bitmap.compress(format,100,sink.outputStream());
    }
}



//class BitmapRequestBody(
//
//private val bitmap: Bitmap,
//private val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
//        ): RequestBody(){
//
//        override fun contentType(): MediaType? {
//        return MediaType.parse(when(format){
//        Bitmap.CompressFormat.WEBP -> "image/webp"
//        Bitmap.CompressFormat.PNG -> "image/png"
//        else -> "image/jpeg"
//        })
//        }
//
//        override fun writeTo(sink: BufferedSink) {
//        bitmap.compress(format,100,sink.outputStream())
//        }
//
//        }
