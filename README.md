# CoolPay(酷支付)
酷支付 android sdk
## What's CoolPay ?

**让支付更简单，一键支付功能，海外支付，有CoolPay就够了**

> CooLPay'官方技术交流群①：...`

#### 集成步骤

> Java 项目配置
 1. 在app目录下的build.gradle中添加依赖
```
dependencies {
    ...
    implementation (name:'coolsdk-release',ext:'aar')
    ...
}
```
 2. 下载 coolsdk-release.aar 放置到app目录libs下
 

### 添加权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 如何使用

 1. 导入
 ```
import com.ipgou.coolsdk.CoolResult;
import com.ipgou.coolsdk.Coolpay;
```
 2. 配置和发起支付
 ```
  Coolpay pay =  Coolpay.getInstance(activity);
  Coolpay.Config....
  // 监听支付结果
  pay.setResultListener(new Coolpay.ResultListener() {
      ...
   // 支付
   Map<String, String> payInfo = new HashMap<String, String>();
   payInfo.put("amount","20000");
   ...
   pay.payV1(payInfo)
   ....
  }
 ```
    具体详见CoolTest
### 版本更新
***
版本 |日期 |描述
------- | ------- | -------
V1.0.0 |2019-10-24 | 初始化释出
   
