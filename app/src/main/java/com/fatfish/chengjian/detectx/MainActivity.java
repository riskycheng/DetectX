package com.fatfish.chengjian.detectx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fatfish.chengjian.analyzer.MobileSSDAnalyzer;
import com.fatfish.chengjian.analyzer.NanodetPlusAnalyzer;
import com.fatfish.chengjian.analyzer.NanodetPlusDoorAnalyzer;
import com.fatfish.chengjian.analyzer.NanodetPlusLeafAnalyzer;
import com.fatfish.chengjian.analyzer.UpdateUICallback;
import com.fatfish.chengjian.analyzer.UpdateUICallbackDoor;
import com.fatfish.chengjian.utils.GlobalConstants;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements UpdateUICallback, UpdateUICallbackDoor {
    private final static String TAG = GlobalConstants.JAVA_LOG_PREFIX + MainActivity.class.getSimpleName();
    private ImageView mImageViewDisplay;
    private Camera mCamera;
    private MobileSSDAnalyzer mobileSSDAnalyzer;
    private NanodetPlusAnalyzer nanodetPlusAnalyzer;
    private NanodetPlusLeafAnalyzer nanodetPlusLeafAnalyzer;
    private NanodetPlusDoorAnalyzer nanodetPlusDoorAnalyzer;
    private LinearLayout mLinearLayoutWarningBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageViewDisplay = findViewById(R.id.previewView_Frame);
        mLinearLayoutWarningBox = findViewById(R.id.warningBox);
        //build the any of the following analyzer
        mobileSSDAnalyzer = new MobileSSDAnalyzer(this);
        mobileSSDAnalyzer.setUpdateUICallback(this);
//        nanodetPlusAnalyzer = new NanodetPlusAnalyzer(this);
//        nanodetPlusAnalyzer.setUpdateUICallback(this);
//        nanodetPlusLeafAnalyzer = new NanodetPlusLeafAnalyzer(this);
//        nanodetPlusLeafAnalyzer.setUpdateUICallback(this);

        nanodetPlusDoorAnalyzer = new NanodetPlusDoorAnalyzer(this);
        nanodetPlusDoorAnalyzer.setUpdateUICallback(this);
        setupCamera(nanodetPlusDoorAnalyzer);
    }


    public void setupCamera(ImageAnalysis.Analyzer analyzer) {
        final CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK) // specially for the Logi camera RK3588
                .build();
        final ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    //get camera provider
                    ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

                    //bind to use-cases before rebinding them
                    cameraProvider.unbindAll();
                    mCamera = cameraProvider.bindToLifecycle(
                            MainActivity.this,
                            cameraSelector,
                            buildAnalyzer(analyzer)
                    );
                } catch (Exception e) {
                    Log.e(TAG, "fail to open camera >>> " + e.getMessage());
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }


    public ImageAnalysis buildAnalyzer(ImageAnalysis.Analyzer analyzer) {
        ImageAnalysis resAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1920, 1080))
                .build();
        Executor imageAnalyzerExecutor = Executors.newSingleThreadExecutor();
        resAnalyzer.setAnalyzer(imageAnalyzerExecutor, analyzer);
        return resAnalyzer;
    }

    /**
     * when analyzing the bitmap is done
     *
     * @param resBitmap
     */
    @Override
    public void onAnalysisDone(Bitmap resBitmap) {
        runOnUiThread(() -> {
            Log.d(TAG, "got result from analyzer...");
            mImageViewDisplay.setImageBitmap(resBitmap);
        });
    }

    @Override
    public void onPostAnyDoorOpen(boolean anyDoorOpen) {
        runOnUiThread(() -> {
            Log.d(TAG, "got result from analyzer for onPostAnyDoorOpen:" + anyDoorOpen);
            if (anyDoorOpen) {
                mLinearLayoutWarningBox.setVisibility(View.VISIBLE);
            } else {
                mLinearLayoutWarningBox.setVisibility(View.INVISIBLE);
            }
        });
    }
}
