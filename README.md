[![](https://jitpack.io/v/xuqiqiang/EasyCamera2-QRCode.svg)](https://jitpack.io/#xuqiqiang/EasyCamera2-QRCode)

# EasyCamera2-QRCode
EasyCamera2的二维码、条形码识别插件

- 与EasyCamera2其他功能独立，可定制扫码界面
- 支持QR_CODE、ISBN13、UPC_A、EAN_13、CODE_128等格式二维码、条形码
- 支持预览界面显示二维码区域
- 支持预览界面自动缩放镜头以提高识别率

## Gradle dependency

```
allprojects {
        repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
	implementation 'com.github.xuqiqiang:EasyCamera2-QRCode:1.0.2'
}
```


## Usage

[Simple demo](https://github.com/xuqiqiang/EasyCamera2-QRCode/blob/master/demo/src/main/java/com/xuqiqiang/camera2/qrcode/demo/DemoActivity.java)
