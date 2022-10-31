package com.example.technewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class webActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        try {
            WebView webView = findViewById(R.id.webView);

            Intent intent = getIntent();
            String link = intent.getStringExtra("link");

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(link);
        }
        catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }
}