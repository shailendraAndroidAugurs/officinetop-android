package com.officinetop.officine.News

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.officinetop.officine.R
import kotlinx.android.synthetic.main.fragment_guide.view.*


private const val target_url = "http://www.example.com"
private const val target_url_prefix = "www.example.com"
private val mContext: Context? = null
private val mWebview: WebView? = null
private var mWebviewPop: WebView? = null
private val mContainer: FrameLayout? = null
private const val mLastBackPressTime: Long = 0
private val mToast: Toast? = null

class Fragmentnews : Fragment() {


    lateinit var rootview: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootview = inflater.inflate(R.layout.fragment_guide, container, false)
        with(rootview) {
            val webSettings = webview!!.settings
            webview!!.loadUrl("https://www.officinetop.com/news/")
            //   webview!!.loadUrl("https://services.officinetop.com/public/amazon_pay_checkout_add?user_id=${activity?.getUserId()}&payble_amount=${1000}")

            webview.requestFocus()
            webSettings.javaScriptEnabled = true
            webSettings.loadWithOverviewMode = true
            webSettings.useWideViewPort = true
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            webview!!.setInitialScale(1)
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            /*  webview.setWebViewClient(UriWebViewClient())
              webview.setWebChromeClient(UriChromeClient())*/
            val webViewClient = WebViewClientImpl(activity, progress_bar_webview)
            webview!!.webViewClient = webViewClient
        }

        return rootview
    }


    /* override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         if (keyCode == KeyEvent.KEYCODE_BACK && this.webView!!.canGoBack()) {
             this.webView?.goBack()
             return true
         }
         return super.onKeyDown(keyCode, event)
     }*/

    private class UriWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val host: String = Uri.parse(url).host!!
            //Log.d("shouldOverrideUrlLoading", url);
            if (host == target_url_prefix) { // This is my web site, so do not override; let my WebView load
// the page
                if (mWebviewPop != null) {
                    mWebviewPop!!.visibility = View.GONE
                    mContainer?.removeView(mWebviewPop)
                    mWebviewPop = null
                }
                return false
            }
            if (host == "m.facebook.com") {
                return false
            }
            // Otherwise, the link is not for a page on my site, so launch
// another Activity that handles URLs

            return true
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler,
                                        error: SslError) {
            Log.d("onReceivedSslError", "onReceivedSslError")
            //super.onReceivedSslError(view, handler, error);
        }
    }

    internal class UriChromeClient : WebChromeClient() {
        override fun onCreateWindow(view: WebView?, isDialog: Boolean,
                                    isUserGesture: Boolean, resultMsg: Message): Boolean {
            mWebviewPop = WebView(mContext)
            mWebviewPop!!.isVerticalScrollBarEnabled = false
            mWebviewPop!!.isHorizontalScrollBarEnabled = false
            mWebviewPop!!.webViewClient = UriWebViewClient()
            mWebviewPop!!.settings.javaScriptEnabled = true
            mWebviewPop!!.settings.savePassword = false
            mWebviewPop!!.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            mContainer?.addView(mWebviewPop)
            val transport = resultMsg.obj as WebViewTransport
            transport.webView = mWebviewPop
            resultMsg.sendToTarget()
            return true
        }

        override fun onCloseWindow(window: WebView) {
            Log.d("onCloseWindow", "called")
        }
    }
}
