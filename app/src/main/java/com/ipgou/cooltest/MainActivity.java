package com.ipgou.cooltest;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ipgou.coolsdk.CoolResult;
import com.ipgou.coolsdk.Coolpay;

import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG,"oncreate ok threadid " + Thread.currentThread().getId());
        // 允许主线程 启用网络
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        /*
        try {
            Process process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        final View root = findViewById(R.id.root);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                root.setFocusable(true);
                root.setFocusableInTouchMode(true);
                root.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
                return false;
            }
        });

    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        public MyHandler(MainActivity myClassInstance) {
            myClassWeakReference = new WeakReference<MainActivity>(myClassInstance);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity myClass = myClassWeakReference.get();
            if (myClass != null) {
                switch(msg.what) {
                    case 1: {   // 增加文本
                        myClass.test(msg.what);
                    }
                    break;
                    case 2: {   // query
                        Bundle bundle = msg.getData();
                        String txnno = bundle.getString("txn_no");
                        myClass.doQuery(txnno);
                    }
                    break;
                    case 5:{    // 退出
                        myClass.finish();
                    }
                    break;
                    case 10: {

                    }
                    default:
                        break;
                }
            }
        }
    }
    public MyHandler mHandler = new MyHandler(this);

    public void onClick(View view) {
        if( view.getId() ==  R.id.btn_test ) {
            delayCmd( 1 );
        }
    }

    public void delayCmd( final int what ) {
        mHandler.postDelayed(new Runnable(){
            public void run(){
                Message msg = Message.obtain();
                msg.what = what;
                mHandler.sendMessage(msg);
            }
        },500);
    }

    // 测试支付
    private void test( int what ) {
        final Activity activity = this;
        Coolpay pay =  Coolpay.getInstance(activity);
        pay.log("begin...");
        pay.DEBUG = true;
        Coolpay.Config.mch_id = "8f6f12d04ddd2377a05063b6c3eaf92d";
        Coolpay.Config.api_key = "bcbe3365e6ac95ea2c0343a2395834dd";
        // 可以自定义内部服务器
        Coolpay.Config.pay_url = "http://testcoolpay.ipgou.net/coolpay/pay";
        // 可以自定义内部服务器
        Coolpay.Config.query_url = "http://testcoolpay.ipgou.net/coolpay/query";
        // 支付渠道号 CODAPAY_01
        Coolpay.Config.pay_channel = "CODAPAY_01";
        // 支付货币代码 MMK
        Coolpay.Config.pay_currency = "MMK";

        // 监听支付结果
        pay.setResultListener(new Coolpay.ResultListener() {
            @Override
            public void onSuccess(CoolResult payResult) {
                showToast(activity, "支付完成");
                preQuery(payResult.getTransNo());
            }

            @Override
            public void onWaiting(CoolResult payResult) {
                showToast(activity, "支付结果确认中...");
            }

            @Override
            public void onCancel(CoolResult payResult) {
                showToast(activity, "您已取消支付");
            }

            @Override
            public void onFailure(CoolResult payResult) {
                showToast(activity, "支付失败\n" + payResult.getResult());
            }
        });
        // 客户端下单模式
        Map<String, String> payInfo = new HashMap<String, String>();
        String oderid = getOrderId("1");

        payInfo.put("order_no",oderid);
        payInfo.put("amount","20000");
        payInfo.put("subject","test");
        payInfo.put("user_id","1");
        // 支付手机号
        payInfo.put("msisdn","085814727310");
        if( !pay.payV1(payInfo) ){
            showToast(activity,"pay error");
            return;
        }

        // 服务端下单模式

    }

    /**
     * 执行查询
     * @param txn_no
     * @return
     */
    private boolean doQuery(  String txn_no )  {
        final Activity activity = this;
        Coolpay pay =  Coolpay.getInstance(this);

        // 订单查询回调
        pay.setQueryListener(new Coolpay.QueryListener() {
            @Override
            public void onQuerySuccess(CoolResult result) {
                /*
                 {
                    "txn_no": "20191011693613926499662214146345",
                    "mer_id": "b59c67bf196a4758191e42f76670ceba",
                    "channel": "CODAPAY_01",
                    "currency": "MMK",
                    "msisdn": "085814727310",
                        "amount": "2000", // 订单金额，最小币种单位
                    "status": "1", // 支付状态， 0：未支付，1：已支付
                    "status_text": "已支付"
                }
                 */
                String data = result.getDetail();
                showToast(activity,"pay  "+ data);
            }

            @Override
            public void onQueryFailure(CoolResult result) {
                showToast(activity,"pay error "+result.getResult());
            }
        });

        pay.query(txn_no);
        return true;
    }

    /**
     *
     * @param txn_no
     */
    public void preQuery( final String txn_no ) {
        mHandler.postDelayed(new Runnable(){
            public void run(){
                Message msg = Message.obtain();
                msg.what = 2;   //
                Bundle bundle = new Bundle();
                bundle.putString("txn_no",txn_no);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        },3000);
    }


    public static String getOrderId(String machineId) {

        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数
            hashCodeV = -hashCodeV;
        }
        // 0 代表前面补充0
        // 15 代表长度为15
        // d 代表参数为正数型
        String orderId=machineId + String.format("%015d", hashCodeV);
        System.out.println(orderId);
        return orderId;
    }

    private void showToast(Activity activity, String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
