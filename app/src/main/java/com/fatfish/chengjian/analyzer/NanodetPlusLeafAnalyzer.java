package com.fatfish.chengjian.analyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.fatfish.chengjian.detectx.App;
import com.fatfish.chengjian.utils.JNIManager;
import com.fatfish.chengjian.utils.LocalUtils;

public class NanodetPlusLeafAnalyzer implements ImageAnalysis.Analyzer {
    private final static String TAG = NanodetPlusLeafAnalyzer.class.getSimpleName();
    private Context mContext;
    private UpdateUICallback mUpdateUICallback;
    private JNIManager mJNIManager;

    public void setUpdateUICallback(UpdateUICallback updateUICallback) {
        mUpdateUICallback = updateUICallback;
    }

    public NanodetPlusLeafAnalyzer(@NonNull Context context) {
        mContext = context;
        mJNIManager = JNIManager.getInstance();
        mJNIManager.nanoDetLeaf_Init(App.NANODET_PLUS_LEAF_PARAM_PATH,
                App.NANODET_PLUS_LEAF_BIN_PATH);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Bitmap bitmap = null;
        Log.d(TAG, "dim : " + image.getWidth() + "," + image.getHeight() + " @ "
                + " image-format : " + image.getFormat());
        //todo reuse the bitmap
        bitmap = LocalUtils.YUV_420_888_toRGB(mContext, image, image.getWidth(), image.getHeight());

        //need to close it
        image.close();

        bitmap = LocalUtils.rotateBitmap(bitmap, 90);

        mJNIManager.nanoDetLeaf_Detect(bitmap);

        mUpdateUICallback.onAnalysisDone(bitmap);
    }
}