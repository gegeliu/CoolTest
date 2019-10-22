package com.ipgou.coolsdk;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lazen at 2019/10/16 17:34
 */
public class Utils {
    public static final String TAG = "Utils";
    /*
     * 获取字符串
     */
    public static String getString(InputStream inputStream, int max ) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = inputStream.read(buffer)) != -1; ) {
                byteArrayOutputStream.write(buffer, 0, count);
                if (max > 0 && byteArrayOutputStream.size() >= max)
                    break;
            }
            /*
            try {
                inputStream.reset();
            }
            catch (Exception e ) {
                //reset exception
                Log.e(TAG, "err2 "+e.getMessage());
            }*/
            return new String(byteArrayOutputStream.toByteArray());
        }
        catch (Exception e ) {

        }
        return "";
    }

    /**
     *
     * @param str
     * @return
     */
    public static String urlEncode(String str ) {
        try {
            str = java.net.URLEncoder.encode(str,"utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //str = str.replace("%3D", "=").replace("%26", "&");
        return str;
    }

    /*
     * map 转 http query
     */
    public static String httpBuildQuery(Map<String ,String> map) {
        if(map==null||map.isEmpty()){
            return "null";
        }
        String str = "";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            str += key+"="+map.get(key)+"&";
        }
        str = str.substring(0, str.length()-1);
        //将得到的字符串进行处理得到目标格式的字符串
        try {
            str = java.net.URLEncoder.encode(str,"utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        str = str.replace("%3D", "=").replace("%26", "&");
        return str;
    }

    /*
     *
     */
    public static String signSha256(String s) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(s.getBytes(Charset.forName("UTF-8")));
            byte byteData[] = md.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            result = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean  isConnected( ) {
        try {
            String url = "http://www.baidu.com";
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            OkHttpClient client = new OkHttpClient.Builder().
                    connectTimeout(3, TimeUnit.SECONDS).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return true;
            }
        }
        catch (IOException e) {
            // e.printStackTrace();
            Log.e(TAG,"isNetworkAvailabe IOException");
        } catch (Exception e) {
            Log.e(TAG,"isNetworkAvailabe error");
        }
        return false;
    }

    /*
     *
     */
    public static String postData( String url,String postData )  {
        String str = "";
        try {
            Request.Builder builder = new Request.Builder()
                    .url(url.trim());
            // "application/json; charset=utf-8"  "multipart/form-data"
            MediaType media = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(media, postData);
            builder.post(body);

            Request request = builder.build();
            OkHttpClient client = new OkHttpClient.Builder().
                    connectTimeout(30, TimeUnit.SECONDS).build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                str = response.body().string();
                response.body().close();
            }
            else {
                int code = response.code();
                str = response.body().string();
                response.body().close();
                Log.e(TAG, "error code " + code);
            }
        }
        catch (Exception e) {
            Log.e(TAG,"postData " + e.getMessage());
        }

        return str;
    }

}
