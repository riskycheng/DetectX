package com.fatfish.chengjian.detectx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.fatfish.chengjian.analyzer.TextAnalyzer;
import com.fatfish.chengjian.analyzer.UpdateUICallback;
import com.fatfish.chengjian.utils.JNIManager;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements UpdateUICallback {
    private final static String TAG = MainActivity.class.getSimpleName();
    private ImageView mImageViewDisplay;
    private Camera mCamera;

    private TextAnalyzer mTextAnalyzer;

    private JNIManager mJNIManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageViewDisplay = findViewById(R.id.previewView_Frame);
        //build the analyzer
        mTextAnalyzer = new TextAnalyzer(this);
        mTextAnalyzer.setUpdateUICallback(this::onAnalysisDone);
        mJNIManager = JNIManager.getInstance();
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        mJNIManager.setupTensorFlowModels(rootPath + "/Jian_Models/MobileSSD_test/MobileNetSSD_deploy.prototxt",
                rootPath + "/Jian_Models/MobileSSD_test/MobileNetSSD_deploy.caffemodel");
        setupCamera(mTextAnalyzer);
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
