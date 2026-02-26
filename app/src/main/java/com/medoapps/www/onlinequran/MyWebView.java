package com.medoapps.www.onlinequran;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MyWebView extends AppCompatActivity {

    WebView webView;

    String url = "https://online-quran-3b07c.web.app/";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_web_view);

        Bundle b=getIntent().getExtras();
        assert b != null;
        url=b.getString("url");

        webView = (WebView) findViewById(R.id.myWebView);

        webView.setWebViewClient(new MyBrowser());

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        progressDialog = new ProgressDialog(MyWebView.this);
//        progressDialog.setMessage("Loading...");
        progressDialog.show();
        webView.loadUrl(url);





    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            Toast.makeText(ContestActivity.this, "Error:" + description, Toast.LENGTH_SHORT).show();

        }
    }
}