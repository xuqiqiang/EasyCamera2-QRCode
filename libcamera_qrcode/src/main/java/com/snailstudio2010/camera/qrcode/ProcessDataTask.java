package com.snailstudio2010.camera.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import com.snailstudio2010.camera.qrcode.zxing.ZXingView;
import com.snailstudio2010.camera2.ui.ContainerView;

import java.lang.ref.WeakReference;

public class ProcessDataTask extends AsyncTask<Void, Void, ScanResult> {
    // 环境亮度历史记录的数组，255 是代表亮度最大值
    private static final long[] AMBIENT_BRIGHTNESS_DARK_LIST = new long[]{255, 255, 255, 255};
    // 环境亮度扫描间隔
    private static final int AMBIENT_BRIGHTNESS_WAIT_SCAN_TIME = 150;
    // 亮度低的阀值
    private static final int AMBIENT_BRIGHTNESS_DARK = 60;
    private static long sLastStartTime = 0;

    private Size mSize;
    private byte[] mData;
    private boolean mIsPortrait;

    private WeakReference<ZXingView> mQRCodeViewRef;
    private ZXingView.QRCodeListener mQRCodeListener;
    // 上次环境亮度记录的时间戳
    private long mLastAmbientBrightnessRecordTime = System.currentTimeMillis();
    // 上次环境亮度记录的索引
    private int mAmbientBrightnessDarkIndex = 0;

    public ProcessDataTask(Context context, ContainerView container, Size size, byte[] data,
                           ZXingView.ZoomListener zoomListener, ZXingView.QRCodeListener listener) {
        mSize = size;
        mData = data;
        mQRCodeViewRef = new WeakReference<>(
                new ZXingView(context, mSize, container, zoomListener, listener));
        mIsPortrait = BGAQRCodeUtil.isPortrait(context);
        mQRCodeListener = listener;
    }

    public ProcessDataTask perform() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return this;
    }

    void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mQRCodeViewRef.clear();
//        mBitmap = null;
        mData = null;
    }

    private ScanResult processData(ZXingView qrCodeView) {
        if (mData == null) {
            return null;
        }

        int width = 0;
        int height = 0;
        byte[] data = mData;
        try {
//            Camera.Parameters parameters = mCamera.getParameters();
//            Camera.Size size = parameters.getPreviewSize();
            width = mSize.getWidth();
            height = mSize.getHeight();

            if (mIsPortrait) {
                data = new byte[mData.length];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data[x * height + height - y - 1] = mData[x + y * width];
                    }
                }
                int tmp = width;
                width = height;
                height = tmp;
            }

            return qrCodeView.processData(data, width, height, false);
        } catch (Exception e1) {
            e1.printStackTrace();
            try {
                if (width != 0 && height != 0) {
                    BGAQRCodeUtil.d("识别出错重试");
                    return qrCodeView.processData(data, width, height, true);
                } else {
                    return null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    @Override
    protected ScanResult doInBackground(Void... params) {
        ZXingView qrCodeView = mQRCodeViewRef.get();
        if (qrCodeView == null) {
            return null;
        }

        try {
            handleAmbientBrightness(mData);
        } catch (Exception e) {
            e.printStackTrace();
            BGAQRCodeUtil.e("handleAmbientBrightness error:" + e.toString());
        }

        // for test
//        if (mPicturePath != null) {
//            return qrCodeView.processBitmapData(BGAQRCodeUtil.getDecodeAbleBitmap(mPicturePath));
//        } else if (mBitmap != null) {
//            ScanResult result = qrCodeView.processBitmapData(mBitmap);
//            mBitmap = null;
//            return result;
//        } else {
        if (BGAQRCodeUtil.isDebug()) {
            BGAQRCodeUtil.d("两次任务执行的时间间隔：" + (System.currentTimeMillis() - sLastStartTime));
            sLastStartTime = System.currentTimeMillis();
        }
        long startTime = System.currentTimeMillis();

        ScanResult scanResult = processData(qrCodeView);

        if (BGAQRCodeUtil.isDebug()) {
            long time = System.currentTimeMillis() - startTime;
            if (scanResult != null && !TextUtils.isEmpty(scanResult.result)) {
                BGAQRCodeUtil.d("识别成功时间为：" + time);
            } else {
                BGAQRCodeUtil.e("识别出错时间为：" + time);
            }
        }

        return scanResult;
//        }
    }

    private void handleAmbientBrightness(byte[] data) {
//        if (mCameraPreview == null || !mCameraPreview.isPreviewing()) {
//            return;
//        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastAmbientBrightnessRecordTime < AMBIENT_BRIGHTNESS_WAIT_SCAN_TIME) {
            return;
        }
        mLastAmbientBrightnessRecordTime = currentTime;

        int width = mSize.getWidth();//camera.getParameters().getPreviewSize().width;
        int height = mSize.getHeight();//camera.getParameters().getPreviewSize().height;
        // 像素点的总亮度
        long pixelLightCount = 0L;
        // 像素点的总数
        long pixelCount = width * height;
        // 采集步长，因为没有必要每个像素点都采集，可以跨一段采集一个，减少计算负担，必须大于等于1。
        int step = 10;
        // data.length - allCount * 1.5f 的目的是判断图像格式是不是 YUV420 格式，只有是这种格式才相等
        //因为 int 整形与 float 浮点直接比较会出问题，所以这么比
        if (Math.abs(data.length - pixelCount * 1.5f) < 0.00001f) {
            for (int i = 0; i < pixelCount; i += step) {
                // 如果直接加是不行的，因为 data[i] 记录的是色值并不是数值，byte 的范围是 +127 到 —128，
                // 而亮度 FFFFFF 是 11111111 是 -127，所以这里需要先转为无符号 unsigned long 参考 Byte.toUnsignedLong()
                pixelLightCount += ((long) data[i]) & 0xffL;
            }
            // 平均亮度
            long cameraLight = pixelLightCount / (pixelCount / step);
            // 更新历史记录
            int lightSize = AMBIENT_BRIGHTNESS_DARK_LIST.length;
            AMBIENT_BRIGHTNESS_DARK_LIST[mAmbientBrightnessDarkIndex = mAmbientBrightnessDarkIndex % lightSize] = cameraLight;
            mAmbientBrightnessDarkIndex++;
            boolean isDarkEnv = true;
            // 判断在时间范围 AMBIENT_BRIGHTNESS_WAIT_SCAN_TIME * lightSize 内是不是亮度过暗
            for (long ambientBrightness : AMBIENT_BRIGHTNESS_DARK_LIST) {
                if (ambientBrightness > AMBIENT_BRIGHTNESS_DARK) {
                    isDarkEnv = false;
                    break;
                }
            }
            BGAQRCodeUtil.d("摄像头环境亮度为：" + cameraLight);
            if (mQRCodeListener != null) {
                mQRCodeListener.onCameraAmbientBrightnessChanged(isDarkEnv);
            }
        }
    }

    @Override
    protected void onPostExecute(ScanResult scanResult) {
//        ZXingView qrCodeView = mQRCodeViewRef.get();
//        if (qrCodeView == null) {
//            return;
//        }

//        if (mPicturePath != null || mBitmap != null) {
//            mBitmap = null;
//            qrCodeView.onPostParseBitmapOrPicture(result);
//        } else {
//            qrCodeView.onPostParseData(result);
//        }

        String result = scanResult == null ? null : scanResult.result;
        Log.d("_test3_", "result:" + result);
        if (mQRCodeListener != null) mQRCodeListener.onScanQRCodeSuccess(result);
    }
}
