package com.ipgou.coolsdk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lazen at 2019/10/15 12:58
 */
public class Coolpay {
    public static final int VERSION_1 = 1;
    public static final int VERSION_2 = 2;
    public static final String TAG = "CoolPay";
    public static boolean DEBUG = false;
    private Activity context;
    private int version;
    private static Coolpay instance;
    private ResultListener mPayListener = null;
    private QueryListener mQueryListener = null;

    public MyHandler mHandler = new MyHandler(this);

    public static void log(String msg) {
        Log.v(TAG, msg);
    }

    public Coolpay(Activity activity, int version) {
        this.context = activity;
        this.version = version;
    }

    public Coolpay(Activity activity) {
        this(activity, VERSION_2);
    }

    public static Coolpay getInstance(Activity context) {
        if (instance == null) {
            instance = new Coolpay(context);
        }
        return instance;
    }

    public interface ResultListener {
        /**
         * 支付成功
         *
         * @param coolResult
         */
        void onSuccess(CoolResult coolResult);

        /**
         * 支付等待中...
         *
         * @param coolResult
         */
        void onWaiting(CoolResult coolResult);

        /**
         * 支付取消
         *
         * @param coolResult
         */
        void onCancel(CoolResult coolResult);

        /**
         * 支付失败
         *
         * @param coolResult
         */
        void onFailure(CoolResult coolResult);
    }

    public interface QueryListener {
        void onQuerySuccess(CoolResult result);

        void onQueryFailure(CoolResult result);
    }

    /*
     *   检查参数有效性
     */
    public boolean check( int type ) {
        if( Config.mch_id.isEmpty()  || Config.api_key.isEmpty())
            return false;
        if( 0 == type ) {
            if( Config.pay_url.isEmpty() ||
                    Config.pay_channel.isEmpty() || Config.pay_currency.isEmpty() )
                return false;
        }
        else if( 1 == type ){ // query
            if( Config.query_url.isEmpty() ) {
                Log.e(TAG,"query_url is empty!!!");
                return false;
            }
        }
        else if( 2 == type ){ //
            if( Config.txn_url.isEmpty() ) {
                Log.e(TAG,"txn_url is empty!!!");
                return false;
            }
        }
        return true;
    }

    /**
     * add pay call back listener
     *
     * @param listener
     */
    public void setResultListener(ResultListener listener) {
        mPayListener = listener;
    }

    public void setQueryListener(QueryListener queryListener) {
        mQueryListener = queryListener;
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<Coolpay> myClassWeakReference;

        public MyHandler(Coolpay myClassInstance) {
            myClassWeakReference = new WeakReference<Coolpay>(myClassInstance);
        }
        @Override
        public void handleMessage(Message msg) {
            Coolpay myClass = myClassWeakReference.get();
            if ( null == myClass ) {
                Log.e(TAG,"myClass is null!!!");
                return;
            }
            switch(msg.what) {
                case Config.SDK_PAY: { // 处理支付结果消息
                    CoolResult payResult = new CoolResult(-1,"","","","");
                    if ( null == myClass.mPayListener ) {
                        Log.e(TAG,"no paylistener!!");
                        break;
                    }
                    Bundle bundle = msg.getData();
                    if( null == bundle ){
                        myClass.mPayListener.onFailure(payResult);
                        break;
                    }
                    int code = -1;
                    String txn_no = "",message="",time="";
                    if( bundle.containsKey("code") ) {
                        code = bundle.getInt("code");
                        message = bundle.getString("msg");
                        time = bundle.getString("time");
                        if (bundle.containsKey("txn_no"))
                            txn_no = bundle.getString("txn_no");
                    }
                    payResult.setCool(code,message,txn_no,time,"");

                    if (DEBUG) {
                        Coolpay.log("pay result:" + payResult);
                    }

                    if( 0 == code ) {
                        myClass.mPayListener.onSuccess(payResult);
                    }
                    else {
                        myClass.mPayListener.onFailure(payResult);
                    }
                    break;
                }
                case Config.SDK_QUERY:
                    myClass.deQuery(msg.getData());
                    break;
                default:
                    break;
            }
        }
    }


    /**
     *  解析查询结果
     * @param bundle
     * @return
     */
    private boolean deQuery( Bundle bundle ) {
        try {
            CoolResult result = new CoolResult(-1, "", "", "","");
            if ( null == mQueryListener ) {
                Log.e(TAG,"no QueryListener!!");
                return false;
            }
            if (null == bundle) {
                mQueryListener.onQueryFailure(result);
                return true;
            }
            int code = bundle.getInt("code");
            String message = bundle.getString("msg");
            String time = bundle.getString("time");
            String txn_no = "";
            String detail = "";
            if (bundle.containsKey("data")) {
                /* data...
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
                String data = bundle.getString("data");
                JSONObject json = new JSONObject(data);
                if( null != json && !json.isNull("txn_no") ) {
                    txn_no = json.getString("txn_no");
                    detail = data;
                }
            }
            result.setCool(code, message, txn_no, time,detail);
            if( 0 == code ) {
                mQueryListener.onQuerySuccess(result);
            }
            else {
                mQueryListener.onQueryFailure(result);
            }
        }
        catch ( Exception e ) {
            Log.e(TAG,"error msg "+e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     *
     * @param payInfo
     * @return
     */
    private Bundle doPay(  Map<String, String> payInfo,boolean req_txnno ) {
        Bundle bundle = null;
        try {
            if( !check(req_txnno?2:0) ){
                Log.e(TAG,"check pay param error!!!");
                return bundle;
            }
            Map<String, String> mapInfo = new HashMap<String, String>();
            // 内部默认项
            // 商户号
            mapInfo.put("mer_id", Config.mch_id);
            // 渠道号
            mapInfo.put("channel", Config.pay_channel);
            // 货币代码
            mapInfo.put("currency", Config.pay_currency);
            // 版本
            mapInfo.put("version", "1");
            // 合并
            Map<String, String> mapAll = new HashMap<String, String>();
            mapAll.putAll(mapInfo);
            mapAll.putAll(payInfo);
            /*
            {
                 "code": 0,
                 "msg": "短信已发送到您的手机。要结束购买， 将“ PAY”消息发送到75457之后发送3位数字",
                 "time": "1570774561",
                 "data": {
                     "txn_no": "20191011262508757899506526844462"
                 }
             }
             */
            String url = req_txnno?Config.txn_url:Config.pay_url;
            JSONObject json = doHttp(url,mapAll);
            if( null != json && !json.isNull("code" ) ) {
                bundle = new Bundle();
                int code = json.getInt("code");
                bundle.putInt("code",code);
                String msg = json.getString("msg");
                bundle.putString("msg",msg);
                int time = json.getInt("time");
                bundle.putInt("time",time);
                if( !json.isNull("data") ) {
                    JSONObject jsondata = json.getJSONObject("data");
                    String txn_no = jsondata.getString("txn_no");
                    bundle.putString("txn_no",txn_no);
                }
            }
        }
        catch ( Exception e ){
            Log.e(TAG,"error msg "+e.getMessage());
            e.printStackTrace();
        }
        return  bundle;
    }


    /**
     *  排序
     */
    class MapKeyComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

    /**
     *
     * @param map
     * @return
     */
    public Map<String, String> sortByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> sortMap = new TreeMap<String, String>(
                new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }


    /**
     *  发送http 请求并返回 jsonobj
     * @param mapInfo
     * @return
     */
    private JSONObject doHttp( String url,Map<String, String> mapInfo ) {
        try {
            String data = "";
            Map<String, String> resultMap = sortByKey(mapInfo);
            for (String key : resultMap.keySet()) {
                data += key + '=' + resultMap.get(key) + '&';
            }

            data += Config.api_key;
            data = Utils.urlEncode(data);
            resultMap.put("sign", Utils.signSha256(data));

            data = Utils.httpBuildQuery(resultMap);
            if (DEBUG)
                log(data);
            String ret = Utils.postData(url, data);
            return new JSONObject(ret);
        }
         catch ( Exception e ){
                Log.e(TAG,"error msg "+e.getMessage());
                e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询交易结果
     * @return
     */
    private Bundle doQuery( String trn_no ) {
        Bundle bundle = null;
        try {
            if( !check(1) ){
                Log.e(TAG,"check query param error!!!");
                return bundle;
            }
            Map<String, String> mapInfo = new HashMap<String, String>();
            // 内部默认项
            // 商户号
            mapInfo.put("mer_id", Config.mch_id);
            // 设置交易号
            mapInfo.put("txn_no", trn_no);
            JSONObject json = doHttp(Config.query_url,mapInfo);
            /*
             {
            "code": 0,
            "msg": "success",
            "time": "1570787733",
            "data": {
                "txn_no": "20191011693613926499662214146345",
                "mer_id": "b59c67bf196a4758191e42f76670ceba",
                "channel": "CODAPAY_01",
                "currency": "MMK",
                "msisdn": "085814727310",
                    "amount": "2000", // 订单金额，最小币种单位
                "status": "1", // 支付状态， 0：未支付，1：已支付
                "status_text": "已支付"
            }
            }
             */
            if( null != json && !json.isNull("code" ) ) {
                bundle = new Bundle();
                int code = json.getInt("code");
                bundle.putInt("code",code);
                String msg = json.getString("msg");
                bundle.putString("msg",msg);
                int time = json.getInt("time");
                bundle.putInt("time",time);
                if( !json.isNull("data") ) {
                    JSONObject jsondata = json.getJSONObject("data");
                    bundle.putString("data",jsondata.toString());
                }
            }
        }
        catch ( Exception e ){
            Log.e(TAG,"error msg "+e.getMessage());
            e.printStackTrace();
        }
        return  bundle;
    }

    /*
     * 客户端支付模式
     */
    public boolean payV1( Map<String, String> payInfo ) {
        if( !check(0) ||
                !payInfo.containsKey("amount") ||
                !payInfo.containsKey("msisdn") ||
                !payInfo.containsKey("order_no") ){
            Log.e(TAG,"pay error invalid param");
            return false;
        }
        return pay(payInfo,false);
    }

    /*
     * 客户端支付模式
     */
    public boolean payV2( Map<String, String> payInfo ) {
        if( !check(2) ||
                !payInfo.containsKey("amount") ||
                !payInfo.containsKey("order_no") ){
            Log.e(TAG,"pay error invalid param");
            return false;
        }
        return pay(payInfo,true);
    }

    /**
     *  内部支付
     * @param payInfo
     * @return
     */
    private boolean pay( final Map<String, String> payInfo,final boolean req_txnno ) {
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                Bundle bundle = doPay(payInfo,req_txnno);
                Message msg = new Message();
                msg.what = Config.SDK_PAY;
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        return true;
    }

    /**
     *  查询订单信息
     * @param trn_no
     */
    public void query( final String trn_no ) {
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                Bundle bundle = doQuery(trn_no);
                Message msg = new Message();
                msg.what = Config.SDK_QUERY;
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public static class Config {
        public static final int SDK_PAY     = 1;   // 支付
        public static final int SDK_QUERY   = 2;  // 查询
        public static final int SDK_AUTH    = 3;   // 认证
        /**
         *  appid
         */
        public static String app_id;
        /**
         * 商户号
         */
        public static String mch_id;
        /**
         * API密钥，在商户平台设置
         */
        public static String api_key;

        /**
         * 支付渠道号 CODAPAY_01
         */
        public static String pay_channel;

        /**
         * 支付货币代码 MMK
         */
        public static String pay_currency;
        /**
         * 支付路径
         */
        public static String pay_url;
        /**
         * 服务器查询面路径
         */
        public static String query_url;
        /**
         *  web模式获取订单号路径
         */
        public static String txn_url;

        /**
         *  web 模式支付url
         */
        public static String web_url;
        /**
         * 服务器通知url 通常后台预配置 sdk 暂不提供此接口
         */
        public static String notify_url;
    }

}
