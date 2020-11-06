package com.officinetop.officine.News

import android.os.Bundle
import android.webkit.WebView
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R


class WebViewLoad : BaseActivity() {
    private var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_load)


        /*webView = findViewById(R.id.webview) as WebView

        val webSettings = webView!!.settings
        webSettings.javaScriptEnabled = true

        val webViewClient = WebViewClientImpl(this)
        webView!!.webViewClient = webViewClient

        webView!!.loadUrl("https://www.journaldev.com")*/
    }


    /* override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         if (keyCode == KeyEvent.KEYCODE_BACK && this.webView!!.canGoBack()) {
             this.webView?.goBack()
             return true
         }
         return super.onKeyDown(keyCode, event)
     }*/
}
