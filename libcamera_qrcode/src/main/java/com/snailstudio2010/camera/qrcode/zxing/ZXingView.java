package com.snailstudio2010.camera.qrcode.zxing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Size;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.snailstudio2010.camera.qrcode.BGAQRCodeUtil;
import com.snailstudio2010.camera.qrcode.BarcodeType;
import com.snailstudio2010.camera.qrcode.ScanResult;
import com.snailstudio2010.camera2.ui.ContainerView;

import java.util.Map;

public class ZXingView {//extends QRCodeView {
    private static ValueAnimator mAutoZoomAnimator;
    protected BarcodeType mBarcodeType = BarcodeType.HIGH_FREQUENCY;
    private Context mContext;
    private ContainerView mContainer;
    private MultiFormatReader mMultiFormatReader;
    private Map<DecodeHintType, Object> mHintMap;
    private Size mSize;
    private ZoomListener mZoomListener;
    private QRCodeListener mQRCodeListener;

    //    public ZXingView(Context context, AttributeSet attributeSet) {
//        this(context, attributeSet, 0);
//    }
//
//    public ZXingView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
    private long mLastAutoZoomTime = 0;

    public ZXingView(Context context, Size size, ContainerView container, ZoomListener zoomListener, ZXingView.QRCodeListener listener) {
        mContext = context;
        mSize = size;
        mContainer = container;
        mZoomListener = zoomListener;
        mQRCodeListener = listener;
        setupReader();
    }

//    @Override
//    protected ScanResult processBitmapData(Bitmap bitmap) {
//        return new ScanResult(QRCodeDecoder.syncDecodeQRCode(bitmap));
//    }

//    private Rect mFramingRect;
//
//    public Rect getScanBoxAreaRect(int previewHeight) {
//        if (mIsOnlyDecodeScanBoxArea && getVisibility() == View.VISIBLE) {
//            Rect rect = new Rect(mFramingRect);
//            float ratio = 1.0f * previewHeight / getMeasuredHeight();
//
//            float centerX = rect.exactCenterX() * ratio;
//            float centerY = rect.exactCenterY() * ratio;
//
//            float halfWidth = rect.width() / 2f;
//            float halfHeight = rect.height() / 2f;
//            float newHalfWidth = halfWidth * ratio;
//            float newHalfHeight = halfHeight * ratio;
//
//            rect.left = (int) (centerX - newHalfWidth);
//            rect.right = (int) (centerX + newHalfWidth);
//            rect.top = (int) (centerY - newHalfHeight);
//            rect.bottom = (int) (centerY + newHalfHeight);
//            return rect;
//        } else {
//            return null;
//        }
//    }

    //    @Override
    protected void setupReader() {
        mMultiFormatReader = new MultiFormatReader();

        if (mBarcodeType == BarcodeType.ONE_DIMENSION) {
            mMultiFormatReader.setHints(QRCodeDecoder.ONE_DIMENSION_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.TWO_DIMENSION) {
            mMultiFormatReader.setHints(QRCodeDecoder.TWO_DIMENSION_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.ONLY_QR_CODE) {
            mMultiFormatReader.setHints(QRCodeDecoder.QR_CODE_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.ONLY_CODE_128) {
            mMultiFormatReader.setHints(QRCodeDecoder.CODE_128_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.ONLY_EAN_13) {
            mMultiFormatReader.setHints(QRCodeDecoder.EAN_13_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.HIGH_FREQUENCY) {
            mMultiFormatReader.setHints(QRCodeDecoder.HIGH_FREQUENCY_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.CUSTOM) {
            mMultiFormatReader.setHints(mHintMap);
        } else {
            mMultiFormatReader.setHints(QRCodeDecoder.ALL_HINT_MAP);
        }
    }

    /**
     * 设置识别的格式
     *
     * @param barcodeType 识别的格式
     * @param hintMap     barcodeType 为 BarcodeType.CUSTOM 时，必须指定该值
     */
    public void setType(BarcodeType barcodeType, Map<DecodeHintType, Object> hintMap) {
        mBarcodeType = barcodeType;
        mHintMap = hintMap;

        if (mBarcodeType == BarcodeType.CUSTOM && (mHintMap == null || mHintMap.isEmpty())) {
            throw new RuntimeException("barcodeType 为 BarcodeType.CUSTOM 时 hintMap 不能为空");
        }
        setupReader();
    }

    //    @Override
    public ScanResult processData(byte[] data, int width, int height, boolean isRetry) {
        Result rawResult = null;
        Rect scanBoxAreaRect = null;

        try {
            PlanarYUVLuminanceSource source;
            scanBoxAreaRect = null;//mScanBoxView.getScanBoxAreaRect(height);
            if (scanBoxAreaRect != null) {
                source = new PlanarYUVLuminanceSource(data, width, height, scanBoxAreaRect.left, scanBoxAreaRect.top, scanBoxAreaRect.width(),
                        scanBoxAreaRect.height(), false);
            } else {
                source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
            }

            rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
            if (rawResult == null) {
                rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
                if (rawResult != null) {
                    BGAQRCodeUtil.d("GlobalHistogramBinarizer 没识别到，HybridBinarizer 能识别到");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mMultiFormatReader.reset();
        }

        if (rawResult == null) {
            return null;
        }

        String result = rawResult.getText();
        if (TextUtils.isEmpty(result)) {
            return null;
        }

        BarcodeFormat barcodeFormat = rawResult.getBarcodeFormat();
        BGAQRCodeUtil.d("格式为：" + barcodeFormat.name());

        // 处理自动缩放和定位点
        boolean isNeedAutoZoom = isNeedAutoZoom(barcodeFormat);
        if (isShowLocationPoint() || isNeedAutoZoom) {
            ResultPoint[] resultPoints = rawResult.getResultPoints();
            final PointF[] pointArr = new PointF[resultPoints.length];
            int pointIndex = 0;
            for (ResultPoint resultPoint : resultPoints) {
                pointArr[pointIndex] = new PointF(resultPoint.getX(), resultPoint.getY());
                pointIndex++;
            }

            if (transformToViewCoordinates(pointArr, scanBoxAreaRect, isNeedAutoZoom, result)) {
                return null;
            }
        }
        return new ScanResult(result);
    }

    private boolean isNeedAutoZoom(BarcodeFormat barcodeFormat) {
        return isAutoZoom() && barcodeFormat == BarcodeFormat.QR_CODE && mZoomListener != null;
    }

    /**
     * 是否显示定位点
     */
    protected boolean isShowLocationPoint() {
        return mContainer != null;
    }

    /**
     * 是否自动缩放
     */
    protected boolean isAutoZoom() {
        return true;
    }

    protected boolean transformToViewCoordinates(final PointF[] pointArr, final Rect scanBoxAreaRect, final boolean isNeedAutoZoom, final String result) {
        if (pointArr == null || pointArr.length == 0) {
            return false;
        }

        try {
            // 不管横屏还是竖屏，size.width 大于 size.height
//            Camera.Size size = mCamera.getParameters().getPreviewSize();
            boolean isMirrorPreview = false;//mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
            int statusBarHeight = 0;//BGAQRCodeUtil.getStatusBarHeight(mContainer.getContext());

            PointF[] transformedPoints = new PointF[pointArr.length];
            int index = 0;
            for (PointF qrPoint : pointArr) {
                transformedPoints[index] = transform(qrPoint.x, qrPoint.y, mSize.getWidth(), mSize.getHeight(), isMirrorPreview, statusBarHeight, scanBoxAreaRect);
                index++;
            }

            if (mContainer != null) {
                mContainer.setLocationPoints(transformedPoints);
                mContainer.postInvalidate();
//            mLocationPoints = transformedPoints;
//            postInvalidate();
            }

            if (isNeedAutoZoom) {
                return handleAutoZoom(transformedPoints, result);
            }
            return false;
        } catch (Exception e) {
//            mLocationPoints = null;
            if (mContainer != null) {
                mContainer.setLocationPoints(null);
            }
            e.printStackTrace();
            return false;
        }
    }

    private PointF transform(float originX, float originY, float cameraPreviewWidth, float cameraPreviewHeight, boolean isMirrorPreview, int statusBarHeight,
                             final Rect scanBoxAreaRect) {
        int viewWidth = mContainer != null ? mContainer.getWidth() : mSize.getWidth();
        int viewHeight = mContainer != null ? mContainer.getHeight() : mSize.getWidth();

        PointF result;
        float scaleX;
        float scaleY;

        if (BGAQRCodeUtil.isPortrait(mContext)) {
            scaleX = viewWidth / cameraPreviewHeight;
            scaleY = viewHeight / cameraPreviewWidth;
            result = new PointF((cameraPreviewHeight - originX) * scaleX, (cameraPreviewWidth - originY) * scaleY);
            result.y = viewHeight - result.y;
            result.x = viewWidth - result.x;

            if (scanBoxAreaRect == null) {
                result.y += statusBarHeight;
            }
        } else {
            scaleX = viewWidth / cameraPreviewWidth;
            scaleY = viewHeight / cameraPreviewHeight;
            result = new PointF(originX * scaleX, originY * scaleY);
            if (isMirrorPreview) {
                result.x = viewWidth - result.x;
            }
        }

        if (scanBoxAreaRect != null) {
            result.y += scanBoxAreaRect.top;
            result.x += scanBoxAreaRect.left;
        }

        return result;
    }

    private boolean handleAutoZoom(PointF[] locationPoints, final String result) {
//        if (mCamera == null || mScanBoxView == null) {
//            return false;
//        }
        if (locationPoints == null || locationPoints.length < 1) {
            return false;
        }
        if (mAutoZoomAnimator != null && mAutoZoomAnimator.isRunning()) {
            return true;
        }
        if (System.currentTimeMillis() - mLastAutoZoomTime < 1200) {
            return true;
        }
//        Camera.Parameters parameters = mCamera.getParameters();
//        if (!parameters.isZoomSupported()) {
//            return false;
//        }

        float point1X = locationPoints[0].x;
        float point1Y = locationPoints[0].y;
        float point2X = locationPoints[1].x;
        float point2Y = locationPoints[1].y;
        float xLen = Math.abs(point1X - point2X);
        float yLen = Math.abs(point1Y - point2Y);
        int len = (int) Math.sqrt(xLen * xLen + yLen * yLen);

        int scanBoxWidth = BGAQRCodeUtil.dp2px(mContext, 200);//mScanBoxView.getRectWidth();
        if (len > scanBoxWidth / 4) {
            return false;
        }
        // 二维码在扫描框中的宽度小于扫描框的 1/4，放大镜头
        final float maxZoom = 1f;//parameters.getMaxZoom();
        final float zoomStep = maxZoom / 4f;
        final float zoom = mZoomListener.getCameraZoom();//parameters.getZoom();
        BGAQRCodeUtil.d("_test3_ zoom:" + zoom + ",zoomStep:" + zoomStep);

        startAutoZoom(zoom, Math.min(zoom + zoomStep, maxZoom), result);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
//                startAutoZoom(zoom, Math.min(zoom + zoomStep, maxZoom), result);
                mAutoZoomAnimator.start();
            }
        });
        return true;
    }

    private void startAutoZoom(float oldZoom, float newZoom, final String result) {
        mAutoZoomAnimator = ValueAnimator.ofFloat(oldZoom, newZoom);
        mAutoZoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                if (mCameraPreview == null || !mCameraPreview.isPreviewing()) {
//                    return;
//                }
                float zoom = (float) animation.getAnimatedValue();
                BGAQRCodeUtil.d("_test3_ zoom:" + zoom);
                mZoomListener.setCameraZoom(zoom);
//                Camera.Parameters parameters = mCamera.getParameters();
//                parameters.setZoom(zoom);
//                mCamera.setParameters(parameters);
            }
        });
        mAutoZoomAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                onPostParseData(new ScanResult(result));
//                String result = scanResult == null ? null : scanResult.result;
                BGAQRCodeUtil.d("_test3_ result1:" + result);
                if (mQRCodeListener != null) mQRCodeListener.onScanQRCodeSuccess(result);
//                mAutoZoomAnimator = null;
            }

//            @Override
//            public void onAnimationCancel(Animator animation) {
////                mAutoZoomAnimator = null;
//            }
        });
        mAutoZoomAnimator.setDuration(600);
        mAutoZoomAnimator.setRepeatCount(0);
//        mAutoZoomAnimator.start();
        mLastAutoZoomTime = System.currentTimeMillis();
    }


    public interface ZoomListener {
        float getCameraZoom();

        void setCameraZoom(float value);
    }

    public interface QRCodeListener {
        /**
         * 处理扫描结果
         *
         * @param result 摄像头扫码时只要回调了该方法 result 就一定有值，不会为 null。解析本地图片或 Bitmap 时 result 可能为 null
         */
        void onScanQRCodeSuccess(String result);

        /**
         * 摄像头环境亮度发生变化
         *
         * @param isDark 是否变暗
         */
        void onCameraAmbientBrightnessChanged(boolean isDark);

//        /**
//         * 处理打开相机出错
//         */
//        void onScanQRCodeOpenCameraError();
    }
}