package com.ipgou.cooltest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * Created by lazen at 2019/10/22 15:11
 */
public class WebActivity extends AppCompatActivity {
    private WebView mWebView;
    protected Toolbar mToolbar;
    private ProgressBar mProgressbar;
    protected FrameLayout mWebLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        mWebView = findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        //设置适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        //支持缩放
        settings.setSupportZoom(false);
        //隐藏原生的缩放控件
        settings.setDisplayZoomControls(false);
        //支持内容重新布局
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.supportMultipleWindows();
        settings.setSupportMultipleWindows(true);
        //设置缓存模式
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(mWebView.getContext().getCacheDir().getAbsolutePath());
        //设置可访问文件
        settings.setAllowFileAccess(true);
        //当webview调用requestFocus时为webview设置节点
        settings.setNeedInitialFocus(true);
        //支持自动加载图片
        if (Build.VERSION.SDK_INT >= 19) {
            settings.setLoadsImagesAutomatically(true);
        } else {
            settings.setLoadsImagesAutomatically(false);
        }


        mWebLayout = (FrameLayout) findViewById(com.ipgou.cooltest.R.id.webLayout);

        mProgressbar = (ProgressBar) findViewById(R.id.progress_bar);
        // LOCAL RESOURCE
        // mWebView.loadUrl("file:///android_asset/index.html");
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

        layoutViews();

        //
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        // REMOTE RESOURCE
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new MyWebViewClient());
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutViews();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutViews();
        }
    }

    protected void layoutViews() {
     /*   int toolbarHeight = 56;
        int screenHeight = DisplayUtil.getHeight();
        int webLayoutMinimumHeight = screenHeight - toolbarHeight;
       // int webLayoutMinimumWidth = DisplayUtil.getWidth();
        mWebLayout.setMinimumHeight(webLayoutMinimumHeight);
     //   mWebLayout.setMinimumWidth(webLayoutMinimumWidth);
     */
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        //是否在WebView内加载新页面，可以在这里判断URL做相应操作
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.toString());
            return true;
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              HttpAuthHandler handler,
                                              String host,
                                              String realm){
              //handler.useHttpAuthUsergnamePassword();
        }

        @Override
        public void onReceivedSslError(WebView view,
                                       SslErrorHandler handler,
                                       SslError error) {
            handler.proceed() ; // 接受所有网站的证书
        }

        //网络错误时回调的方法
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressbar.setVisibility(View.VISIBLE);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            getSupportActionBar().setTitle(view.getTitle());
            mProgressbar.setVisibility(View.GONE);
        }
    }
}
