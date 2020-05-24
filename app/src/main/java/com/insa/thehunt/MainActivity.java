package com.insa.thehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.view.KeyEvent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

        /*
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
        gpsManager.start(new MyListener(), true); //开启定位
        */



public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private LocationWebChromeClient mLocationWebChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mLocationWebChromeClient = new LocationWebChromeClient(this);
        mWebView.setWebChromeClient(mLocationWebChromeClient);
        setupWebViewEnvirment();
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.loadUrl("https://0a6f7c2c.ngrok.io");
        //mWebView.loadUrl("https://www.google.fr/maps/preview");
    }

    private void setupWebViewEnvirment() {
        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setAllowContentAccess(true);
        mWebSettings.setSupportMultipleWindows(false);
        mWebSettings.setGeolocationEnabled(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
//        mWebView.loadUrl("https://msdev.czb365.com/?platformType=92631345&platformCode=18610899775");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mLocationWebChromeClient != null && mLocationWebChromeClient.getLocationWebChromeClientListener() != null) {
            mLocationWebChromeClient.getLocationWebChromeClientListener().onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mLocationWebChromeClient != null && mLocationWebChromeClient.getLocationWebChromeClientListener() != null) {
            if (mLocationWebChromeClient.getLocationWebChromeClientListener().onReturnFromLocationSetting(requestCode)) {
                return;
            }
        }
    }

    //返回键监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //返回上一级
    public void goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }
}