package com.insa.thehunt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private static final String TAG = "MyActivity";
    private ProgressBar progressBar;
    double lat = 0;
    double log = 0;
    boolean GPS_FIRST_FIX = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GPSLocationManager gpsManager = GPSLocationManager.getInstances(MainActivity.this);
        class MyListener implements GPSLocationListener {

            @Override
            public void UpdateLocation(Location location) {
                if (location != null) {
                    log = location.getLongitude();
                    lat = location.getLatitude();
                    if (GPS_FIRST_FIX = true) {
                        gpsManager.start_TimerTask();
                        GPS_FIRST_FIX = false;
                    }
                    Log.e("gps==", "Longitude：" + location.getLongitude() + "\nLatitude：" + location.getLatitude());
                }
            }

            @Override
            public void UpdateStatus(String provider, int status, Bundle extras) {
                if ("gps" == provider) {
                    Log.e("UpdateStatus--gps", "定位类型：" + provider);
                }
            }

            @Override
            public void UpdateGPSProviderStatus(int gpsStatus) {
                switch (gpsStatus) {

                    case GPSProviderStatus.GPS_ENABLED:
                        //   Toast.makeText(MainActivity.this, "GPS开启", Toast.LENGTH_SHORT).show();
                        break;
                    case GPSProviderStatus.GPS_DISABLED:
                        //    Toast.makeText(MainActivity.this, "GPS关闭", Toast.LENGTH_SHORT).show();
                        break;
                    case GPSProviderStatus.GPS_OUT_OF_SERVICE:
                        //  Toast.makeText(MainActivity.this, "GPS不可用", Toast.LENGTH_SHORT).show();
                        break;
                    case GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE:
                        //  Toast.makeText(MainActivity.this, "当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
                        break;
                    case GPSProviderStatus.GPS_AVAILABLE:
                        //     Toast.makeText(MainActivity.this, "GPS可用啦", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        //开启定位
        gpsManager.start(new MyListener(), true);

        //以下为web页面显示
        progressBar= (ProgressBar)findViewById(R.id.progressbar);//This is a progress bar

        webView = (WebView) findViewById(R.id.webview);
        //webView.loadUrl("file:///android_asset/test.html");//Load html under asset folder
        webView.loadUrl("http://e2072003.ngrok.io/");//Load url

        //Display html code using webview
        //webView.loadDataWithBaseURL(null,"<html><head><title> WELCOME </title></head>" +
        //"<body><h2>Display html code using webview</h2></body></html>", "text/html" , "utf-8", null);

        webView.addJavascriptInterface(this,"android");//Add js monitoring so that html can call the client
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//Js allowed

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//Not using the network, only read locally cached data

        //Support screen zoom
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        //Do not show webview zoom button
        //webSettings.setDisplayZoomControls(false);




    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("ansen","Is there a previous page:"+webView.canGoBack());
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){ //When clicking the back button, determine whether there is a previous page
            webView.goBack(); // goBack() means return to the previous page of webView
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //release resources
        webView.destroy();
        webView=null;
    }



    //Mainly help WebView to handle various notifications and request events
    private WebViewClient webViewClient=new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {//Page loading completed
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//The page starts to load
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen","Intercept url:"+url);
            if(url.equals("http://www.google.com/")){
                Toast.makeText(MainActivity.this,"Can not find google,Intercept url",Toast.LENGTH_LONG).show();
                return true;//It means I have dealt with it
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

    };

    //WebChromeClient mainly assists WebView in handling Javascript dialog boxes, website icons, website titles, loading progress, etc.
    private WebChromeClient webChromeClient=new WebChromeClient(){
        //Alert popups that do not support js, you need to monitor yourself and then popup through dialog
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定",null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            //note:
            // This sentence code is required: result.confirm () means:
            // The processing result is a certain state and wakes up the WebCore thread
            // Otherwise you cannot continue to click the button
            result.confirm();
            return true;
        }

        //Get page title
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.i("ansen","Title:"+title);
        }

        //Loading progress callback
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }
    };


    /**
     * JS call android method
     * @param str
     * @return
     */
    @JavascriptInterface
    public void  getClient(String str){
        Log.i("ansen","html call the client:"+str);
    }
}