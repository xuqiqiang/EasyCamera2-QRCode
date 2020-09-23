package com.xuqiqiang.camera2.qrcode.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.snailstudio2010.camera.qrcode.QRCodeDetector;
import com.snailstudio2010.camera.qrcode.zxing.ZXingView;
import com.snailstudio2010.camera2.CameraView;
import com.snailstudio2010.camera2.Properties;
import com.snailstudio2010.camera2.module.CameraModule;
import com.snailstudio2010.camera2.module.PhotoModule;
import com.snailstudio2010.camera2.module.SingleCameraModule;
import com.xuqiqiang.camera2.qrcode.demo.utils.Permission;

/**
 * Created by xuqiqiang on 2020/07/12.
 */
public class DemoActivity extends BaseActivity {

    private CameraView mCameraView;
    private CameraModule mCameraModule;
    private QRCodeDetector mQRCodeDetector;
    private float mCameraZoom;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((SingleCameraModule) mCameraModule).onTouchToFocus(event.getX(), event.getY());
                }
                return true;
            }
        });

        mQRCodeDetector = new QRCodeDetector(this)
                .isShowLocationPoint(true)
                .setZoomListener(new ZXingView.ZoomListener() {
                    @Override
                    public float getCameraZoom() {
                        return mCameraZoom;
                    }

                    @Override
                    public void setCameraZoom(float value) {
                        mCameraZoom = value;
                        ((SingleCameraModule) mCameraModule).setCameraZoom(mCameraZoom);
                    }
                })
                .setQRCodeListener(new ZXingView.QRCodeListener() {
                    @Override
                    public void onScanQRCodeSuccess(String result) {
                        showQRCodeResult(result);
                    }

                    @Override
                    public void onCameraAmbientBrightnessChanged(boolean isDark) {
                    }
                });
    }

    private void showQRCodeResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            mQRCodeDetector.pauseSpot();
            AlertDialog dialog = new AlertDialog.Builder(DemoActivity.this)
                    .setTitle("二维码")
                    .setMessage(result)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mQRCodeDetector.startSpot((SingleCameraModule) mCameraModule);
                }
            });
            dialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permission.checkPermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Permission.isPermissionGranted(this) && mCameraModule == null) {
            mCameraModule = new PhotoModule(new Properties().debug(true));
            mQRCodeDetector.startSpot((SingleCameraModule) mCameraModule);
            mCameraView.setCameraModule(mCameraModule);
        }
        mCameraZoom = 0f;
        if (mCameraView != null)
            mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraView != null)
            mCameraView.onPause();
    }
}
