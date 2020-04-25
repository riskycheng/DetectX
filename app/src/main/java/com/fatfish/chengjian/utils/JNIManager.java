package com.fatfish.chengjian.utils;

import android.util.Log;

public class JNIManager {
    private final String TAG = JNIManager.class.getSimpleName();

    static {
        System.loadLibrary("native-lib");
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
    public void setupTensorFlowModels(String modelPath, String weightPath) {
        DNN_initTensorFlowNet(modelPath, weightPath);
    }

    public static JNIManager getInstance() {
        if (mJNIManager == null)
            mJNIManager = new JNIManager();
        return mJNIManager;
    }





    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // bridges to native functions
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private static native void DNN_initTensorFlowNet(String prototxtPath, String caffePath);

}
