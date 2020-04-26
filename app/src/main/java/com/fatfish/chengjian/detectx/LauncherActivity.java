package com.fatfish.chengjian.detectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fatfish.chengjian.utils.GlobalConstants;
import com.fatfish.chengjian.utils.LocalUtils;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LauncherActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private final static int RC_CAMERA_AND_STORAGE = 0x01;
    private final static String TAG = GlobalConstants.JAVA_LOG_PREFIX + LauncherActivity.class.getSimpleName();
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        mTextView = findViewById(R.id.textViewStatus);
        methodRequiresTwoPermission();
    }

    @AfterPermissionGranted(RC_CAMERA_AND_STORAGE)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            new Handler().postDelayed(() -> {
                finish();
                startActivity(new Intent(LauncherActivity.this, MainActivity.class));
            }, 3000L);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.request_permissions),
                    RC_CAMERA_AND_STORAGE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Thread copyThread = new Thread(() -> {
            LocalUtils.CopyAssetsDir(LauncherActivity.this, "models", App.EXTERNAL_LOCATION_ROOT);
        });
        copyThread.start();
        try {
            copyThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "error >>> " + e.getMessage());
        }
        mTextView.setText(getString(R.string.finish_copying_models));
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
