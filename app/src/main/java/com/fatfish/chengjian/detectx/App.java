package com.fatfish.chengjian.detectx;

import android.app.Application;
import android.os.Environment;

public class App extends Application {
    //will be updated
    public static String EXTERNAL_LOCATION_ROOT = Environment.getExternalStorageDirectory().getPath() + "/DetectX";

    public static String LAUNCH_TIME_STAMP = "";

    public static String MOBILESSD_MODEL_PATH = "";
    public static String MOBILESSD_WEIGHT_PATH = "";
    public static String NANODET_PLUS_PARAM_PATH = "";
    public static String NANODET_PLUS_BIN_PATH = "";
    public static String NANODET_PLUS_LEAF_PARAM_PATH = "";
    public static String NANODET_PLUS_LEAF_BIN_PATH = "";

    public static String NANODET_PLUS_DOOR_PARAM_PATH = "";
    public static String NANODET_PLUS_DOOR_BIN_PATH = "";
    @Override
    public void onCreate() {
        super.onCreate();
        //init here even though it is ugly but we need the context to get the actual package data location
        EXTERNAL_LOCATION_ROOT = getApplicationInfo().dataDir;
        MOBILESSD_MODEL_PATH = EXTERNAL_LOCATION_ROOT + "/models/Mobile_SSD/MobileNetSSD_deploy_20200426.prototxt";
        MOBILESSD_WEIGHT_PATH = EXTERNAL_LOCATION_ROOT + "/models/Mobile_SSD/MobileNetSSD_deploy_20200426.caffemodel";

        // the model path for nanodet-plus
        NANODET_PLUS_PARAM_PATH = EXTERNAL_LOCATION_ROOT + "/models/NanodetPlus_m416/nanodet-plus-m_416.param";
        NANODET_PLUS_BIN_PATH = EXTERNAL_LOCATION_ROOT + "/models/NanodetPlus_m416/nanodet-plus-m_416.bin";

        // the model path for nanodet-plus-leaf
        NANODET_PLUS_LEAF_PARAM_PATH = EXTERNAL_LOCATION_ROOT + "/models/NanodetPlus_m416_leafDet/nanodet_leaf.param";
        NANODET_PLUS_LEAF_BIN_PATH = EXTERNAL_LOCATION_ROOT + "/models/NanodetPlus_m416_leafDet/nanodet_leaf.bin";

        // the model path for nanodet-plus-door
        NANODET_PLUS_DOOR_PARAM_PATH = EXTERNAL_LOCATION_ROOT + "/models/NanodetPlus_m416_doorDet/nanodet_door.param";
        NANODET_PLUS_DOOR_BIN_PATH = EXTERNAL_LOCATION_ROOT + "/models/NanodetPlus_m416_doorDet/nanodet_door.bin";

        LAUNCH_TIME_STAMP = String.valueOf(System.currentTimeMillis());
    }
}
