package com.fatfish.chengjian.utils;

import android.graphics.Bitmap;
import android.util.Log;

public class JNIManager {
    private final String TAG = JNIManager.class.getSimpleName();

    static {
        System.loadLibrary("mobileSSD");
    }

    private static JNIManager mJNIManager = null;

    private JNIManager() {
        Log.d(TAG, "Building JNIManager");
    }

    /**
     * this function must be called since we need to load the models
     * @param modelPath
     * @param weightPath
     */
    public void setupCaffeModels(String modelPath, String weightPath) {
        DNN_initCaffeNet(modelPath, weightPath);
    }

    public static JNIManager getInstance() {
        if (mJNIManager == null)
            mJNIManager = new JNIManager();
        return mJNIManager;
    }





    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // bridges to native functions
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private native void DNN_initCaffeNet(String prototxtPath, String caffePath);

    public native void DNN_execute(Bitmap bitmap);

}
