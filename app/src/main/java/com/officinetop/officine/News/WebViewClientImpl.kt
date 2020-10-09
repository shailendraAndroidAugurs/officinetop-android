package com.officinetop.officine.News

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.fragment_guide.*


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
       Log.d("Amazon",request.toString())
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        handler?.proceed()
    }

}