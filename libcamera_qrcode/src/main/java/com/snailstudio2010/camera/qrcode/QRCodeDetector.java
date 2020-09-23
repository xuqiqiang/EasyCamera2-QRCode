package com.snailstudio2010.camera.qrcode;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Size;

import com.snailstudio2010.camera.qrcode.zxing.ZXingView;
import com.snailstudio2010.camera2.Config;
import com.snailstudio2010.camera2.module.SingleCameraModule;
import com.snailstudio2010.camera2.ui.ContainerView;

public class QRCodeDetector implements SingleCameraModule.PreviewCallback {
    private static final String TAG = Config.getTag(QRCodeDetector.class);

    private Context mContext;
    private long mLastPreviewFrameTime = 0;
    private ProcessDataTask mProcessDataTask;
    private boolean mSpotAble = true;
    private boolean isShowLocationPoint;
    //    private ContainerView mContainerView;
    private ZXingView.ZoomListener mZoomListener;
    private ZXingView.QRCodeListener mQRCodeListener;
    private SingleCameraModule mCameraModule;

    public QRCodeDetector(Context mContext) {
        this.mContext = mContext;
    }

    public QRCodeDetector setQRCodeListener(ZXingView.QRCodeListener listener) {
        this.mQRCodeListener = listener;
        return this;
    }

    public QRCodeDetector setZoomListener(ZXingView.ZoomListener listener) {
        this.mZoomListener = listener;
        return this;
    }

//    public QRCodeDetector setContainerView(ContainerView view) {
//        this.mContainerView = view;
//        return this;
//    }

    public QRCodeDetector isShowLocationPoint(boolean show) {
        this.isShowLocationPoint = show;
        return this;
    }

    /**
     * 开始识别
     */
    public void startSpot(SingleCameraModule cameraModule) {
        mCameraModule = cameraModule;
        mCameraModule.setPreviewCallback(this);
        mSpotAble = true;
    }

    /**
     * 停止识别
     */
    public void pauseSpot() {
        mSpotAble = false;

        if (mProcessDataTask != null) {
            mProcessDataTask.cancelTask();
            mProcessDataTask = null;
        }
    }

    public void stopSpot() {
        pauseSpot();
        if (mCameraModule != null) {
            mCameraModule.setPreviewCallback(null);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Size size) {
//        Log.d(TAG, "_test2_ onPreviewFrame");

        BGAQRCodeUtil.d(TAG, "_test3_ 两次 onPreviewFrame 时间间隔：" + (System.currentTimeMillis() - mLastPreviewFrameTime));
        mLastPreviewFrameTime = System.currentTimeMillis();

//        new ZXingView.QRCodeListener() {
//            @Override
//            public void onScanQRCodeSuccess(String result) {
//                if (!TextUtils.isEmpty(result)) {
//                    mSpotAble = false;
//                    new AlertDialog.Builder(mContext)
//                            .setTitle("二维码")
//                            .setMessage(result)
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                }
//                            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            mSpotAble = true;
//                        }
//                    }).create().show();
//                }
//            }
//
//            @Override
//            public void onCameraAmbientBrightnessChanged(boolean isDark) {
//                Log.d(TAG, "_test3_ onCameraAmbientBrightnessChanged:" + isDark);
//            }
//        }


//        new ZXingView.ZoomListener() {
//            @Override
//            public float getCameraZoom() {
//                return mCameraZoom;
//            }
//
//            @Override
//            public void setCameraZoom(float value) {
//                mCameraZoom = value;
//                ((SingleCameraModule) mCameraModule).setCameraZoom(mCameraZoom);
//                mCameraMenu.setSeekBarValue(CameraSettings.KEY_CAMERA_ZOOM, mCameraZoom);
//            }
//        }


//                    boolean mSpotAble = true;
        if (!mSpotAble || (mProcessDataTask != null && (mProcessDataTask.getStatus() == AsyncTask.Status.PENDING
                || mProcessDataTask.getStatus() == AsyncTask.Status.RUNNING))) {
            return;
        }
        BGAQRCodeUtil.d(TAG, "_test3_ new ProcessDataTask");
        ContainerView containerView = null;
        if (isShowLocationPoint)
            containerView = (ContainerView) mCameraModule.getUI().getRootView();
        mProcessDataTask = new ProcessDataTask(mContext, containerView, size,
                data, mZoomListener, mQRCodeListener).perform();
    }
}
