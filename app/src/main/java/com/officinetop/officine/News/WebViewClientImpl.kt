package com.officinetop.officine.News

import android.app.Activity
import android.net.http.SslError
import android.util.Log
import android.view.View
import android.webkit.ClientCertRequest
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.officinetop.officine.utils.showInfoDialog


class WebViewClientImpl(activity: Activity?, var view1: View) : WebViewClient() {


    private var activity: Activity? = null

    init {
        this.activity = activity
    }


    override fun shouldOverrideUrlLoading(webView: WebView?, url: String): Boolean {
        webView?.loadUrl(url)
        return true
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        activity?.showInfoDialog(description!!)
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        super.onReceivedClientCertRequest(view, request)
        Log.d("Amazon", request.toString())
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        handler?.proceed()
    }

}