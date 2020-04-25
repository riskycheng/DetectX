package com.fatfish.chengjian.analyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.fatfish.chengjian.utils.LocalUtils;

public class TextAnalyzer implements ImageAnalysis.Analyzer {
    private final static String TAG = TextAnalyzer.class.getSimpleName();
    private Context mContext;
    private UpdateUICallback mUpdateUICallback;

    public void setUpdateUICallback(UpdateUICallback updateUICallback) {
        mUpdateUICallback = updateUICallback;
    }

    public TextAnalyzer(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Bitmap bitmap = null;
        assert image != null;
        Log.d(TAG, "dim : " + image.getWidth() + "," + image.getHeight() + " @ "
                + " image-format : " + image.getFormat());
        //todo reuse the bitmap
        bitmap = LocalUtils.YUV_420_888_toRGB(mContext, image, image.getWidth(), image.getHeight());

        //need to close it
        image.close();

        bitmap = LocalUtils.rotateBitmap(bitmap, 90);
        mUpdateUICallback.onAnalysisDone(bitmap);
    }
}
