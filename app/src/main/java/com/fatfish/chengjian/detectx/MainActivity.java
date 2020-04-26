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
import android.widget.ImageView;

import com.fatfish.chengjian.analyzer.MobileSSDAnalyzer;
import com.fatfish.chengjian.analyzer.UpdateUICallback;
import com.fatfish.chengjian.utils.GlobalConstants;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements UpdateUICallback {
    private final static String TAG = GlobalConstants.JAVA_LOG_PREFIX + MainActivity.class.getSimpleName();
    private ImageView mImageViewDisplay;
    private Camera mCamera;

    private MobileSSDAnalyzer mobileSSDAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageViewDisplay = findViewById(R.id.previewView_Frame);
        //build the analyzer
        mobileSSDAnalyzer = new MobileSSDAnalyzer(this);
        mobileSSDAnalyzer.setUpdateUICallback(this::onAnalysisDone);
        setupCamera(mobileSSDAnalyzer);
    }


    public void setupCamera(ImageAnalysis.Analyzer analyzer) {
        final CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
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
        ImageAnalysis resAnalyzer = new ImageAnalysis.Builder().build();
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
}
