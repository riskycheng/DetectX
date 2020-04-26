package com.fatfish.chengjian.detectx;

import android.app.Application;
import android.os.Environment;

public class App extends Application {
    //will be updated
    public static String EXTERNAL_LOCATION_ROOT = Environment.getExternalStorageDirectory().getPath() + "/DetectX";
    public static String MOBILESSD_MODEL_PATH = "";
    public static String MOBILESSD_WEIGHT_PATH = "";

    @Override
    public void onCreate() {
        super.onCreate();
        //init here even though it is ugly but we need the context to get the actual package data location
        EXTERNAL_LOCATION_ROOT = getApplicationInfo().dataDir;
        MOBILESSD_MODEL_PATH = EXTERNAL_LOCATION_ROOT + "/models/Mobile_SSD/MobileNetSSD_deploy_20200426.prototxt";
        MOBILESSD_WEIGHT_PATH = EXTERNAL_LOCATION_ROOT + "/models/Mobile_SSD/MobileNetSSD_deploy_20200426.caffemodel";
    }
}
