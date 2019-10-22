package com.ipgou.coolsdk;

import android.text.TextUtils;
import java.util.Map;

/**
 * Created by lazen at 2019/10/15 17:19
 */
public class CoolResult {
    private int mStatus;    // 状态
    private String mResult; // 结果消息
    private String mTransNo; // 交易号
    private String mTime;    // 时间
    private String mDetail;  // 附加详细信息字段

    public CoolResult( int status,String result,String trnno,String time,String detail ) {
        mStatus =  status;
        mResult = result;
        mTransNo = trnno;
        mTime = time;
        mDetail = detail;
    }

    public CoolResult(Map<String, String> rawResult) {
        if (rawResult == null) {
            return;
        }
        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, "status")) {
                mStatus = Integer.parseInt(rawResult.get(key));
            }
            else if (TextUtils.equals(key, "result")) {
                mResult = rawResult.get(key);
            }
            else if (TextUtils.equals(key, "trnno")) {
                mTransNo = rawResult.get(key);
            }
            else if (TextUtils.equals(key, "time")) {
                mTime = rawResult.get(key);
            }
            else if (TextUtils.equals(key, "detail")) {
                mDetail = rawResult.get(key);
            }
        }
    }

    /**
     * Version 1 pay result
     *
     * @param rawResult pay result
     */
    public CoolResult(String rawResult) {
        if (TextUtils.isEmpty(rawResult)) {
            return;
        }

        String[] resultParams = rawResult.split(";");
        for (String resultParam : resultParams) {
            if (resultParam.startsWith("status")) {
                mStatus = Integer.parseInt(gatValue(resultParam, "status"));
            }
            if (resultParam.startsWith("result")) {
                mResult = gatValue(resultParam, "result");
            }
            if (resultParam.startsWith("trnno")) {
                mTransNo = gatValue(resultParam, "trnno");
            }
            if (resultParam.startsWith("time")) {
                mTime = gatValue(resultParam, "time");
            }
            if (resultParam.startsWith("detail")) {
                mDetail = gatValue(resultParam, "detail");
            }
        }
    }

    private String gatValue(String content, String key) {
        String prefix = key + "={";
        return content.substring(content.indexOf(prefix) + prefix.length(), content.lastIndexOf("}"));
    }

    @Override
    public String toString() {
        return "status={" + mStatus + "};trn_no={" + mTransNo + "};result={" + mResult + "};time={" + mTime + "};detail={" + mDetail + "}";
    }

    /**
     *
     * @param status
     * @param result
     * @param trn_no
     * @param time
     * @param detail
     */
    public void setCool( int status,String result,String trn_no,String time,String detail ) {
        mStatus =  status;
        mResult = result;
        mTransNo = trn_no;
        mTime = time;
        mDetail = detail;
    }
    /**
     * @return the resultStatus
     */
    public int getStatus() {
        return mStatus;
    }

    /**
     * @return the info
     */
    public String getTransNo() {
        return mTransNo;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return mResult;
    }

    /**
     * @return time
     */
    public String getTime( ) {
        return mTime;
    }

    public  String getDetail( ) {
        return mDetail;
    }
}
