package com.insa.thehunt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar= (ProgressBar)findViewById(R.id.progressbar);//This is a progress bar

        webView = (WebView) findViewById(R.id.webview);
//        webView.loadUrl("file:///android_asset/test.html");//Load html under asset folder
        webView.loadUrl("https://www.google.fr/");//Load url

        //Display html code using webview
//        webView.loadDataWithBaseURL(null,"<html><head><title> WELCOME </title></head>" +
//                "<body><h2>Display html code using webview</h2></body></html>", "text/html" , "utf-8", null);

        webView.addJavascriptInterface(this,"android");//Add js monitoring so that html can call the client
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//Js allowed

        /**
         * LOAD_CACHE_ONLY: Not using the network, only read locally cached data
         * LOAD_DEFAULT: (Default) Decide whether to fetch data from the network according to cache-control.
         * LOAD_NO_CACHE: Not using cache, only get data from the network.
         * LOAD_CACHE_ELSE_NETWORK As long as it is available locally, whether it expires or no-cache, the data in the cache is used.
         */
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//Not using the network, only read locally cached data

        //Support screen zoom
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        //Do not show webview zoom button
//        webSettings.setDisplayZoomControls(false);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("ansen","Is there a previous page:"+webView.canGoBack());
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){//When clicking the back button, determine whether there is a previous page
            webView.goBack(); // goBack() means return to the previous page of webView
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * JS call android method
     * @param str
     * @return
     */
    @JavascriptInterface
    public void  getClient(String str){
        Log.i("ansen","html call the client:"+str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //release resources
        webView.destroy();
        webView=null;
    }
}