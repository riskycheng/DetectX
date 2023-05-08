package com.fatfish.chengjian.analyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.fatfish.chengjian.detectx.App;
import com.fatfish.chengjian.utils.BoxInfo;
import com.fatfish.chengjian.utils.DoorDetectionLogInstance;
import com.fatfish.chengjian.utils.JNIManager;
import com.fatfish.chengjian.utils.LocalUtils;

import java.util.Vector;

public class NanodetPlusDoorAnalyzer implements ImageAnalysis.Analyzer {
    private final static String TAG = NanodetPlusDoorAnalyzer.class.getSimpleName();
    private Context mContext;
    private UpdateUICallbackDoor mUpdateUICallback;
    private JNIManager mJNIManager;

    private Vector<DoorDetectionLogInstance> mDoorDetectionLogInstances;

    public void setUpdateUICallback(UpdateUICallbackDoor updateUICallback) {
        mUpdateUICallback = updateUICallback;
    }

    public NanodetPlusDoorAnalyzer(@NonNull Context context) {
        mContext = context;
        mJNIManager = JNIManager.getInstance();
        mJNIManager.nanoDetDoor_Init(App.NANODET_PLUS_DOOR_PARAM_PATH, App.NANODET_PLUS_DOOR_BIN_PATH);
        mDoorDetectionLogInstances = new Vector<>();
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Bitmap bitmap = null;
        Log.d(TAG, "dim : " + image.getWidth() + "," + image.getHeight() + " @ " + " image-format : " + image.getFormat());
        //todo reuse the bitmap
        bitmap = LocalUtils.YUV_420_888_toRGB(mContext, image, image.getWidth(), image.getHeight());

        //need to close it
        image.close();

//        bitmap = LocalUtils.rotateBitmap(bitmap, 90); // no need to perform rotation

        BoxInfo[] detectedBoxes = mJNIManager.nanoDetDoor_Detect(bitmap);
        boolean anyDoorOpen = false;
        if (detectedBoxes != null) {
            for (BoxInfo box : detectedBoxes) {
                if (box == null) continue;
                if (box.getLabel() == 1) {
                    anyDoorOpen = true;
                    break;
                }
            }

            // update the results into the instance for every frame
            DoorDetectionLogInstance doorDetectionLogInstance = new DoorDetectionLogInstance();
            doorDetectionLogInstance.setDoors(detectedBoxes);
            doorDetectionLogInstance.setAnyDoorOpen(anyDoorOpen);
            if (anyDoorOpen) {
                // save out the bitmap
                long timeStamp = System.currentTimeMillis();
                String imageName = "image_" + timeStamp + ".png";
                LocalUtils.saveOutBitmap(imageName, bitmap, 0.2f, 20);
                doorDetectionLogInstance.setImagePath(imageName);
                doorDetectionLogInstance.setTimeStamp(String.valueOf(timeStamp));
            }
            mDoorDetectionLogInstances.add(doorDetectionLogInstance);
            LocalUtils.saveLogs(App.LAUNCH_TIME_STAMP + ".txt", mDoorDetectionLogInstances);
            mDoorDetectionLogInstances.clear();
        }
        mUpdateUICallback.onAnalysisDone(bitmap);
        mUpdateUICallback.onPostAnyDoorOpen(anyDoorOpen);
    }
}
