package com.officinetop.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.officinetop.News.WebViewClientImpl
import com.officinetop.R
import kotlinx.android.synthetic.main.fragment_guide.view.*


class VideosFragment : Fragment() {


    lateinit var rootview: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootview = inflater.inflate(R.layout.fragment_guide, container, false)
        with(rootview) {
            val webSettings = webview!!.settings
            /* val up: String = mUserName.toString() + ":" + mPassC
             val authEncoded = String(Base64.encodeBase64(up.toByteArray()))
             val authHeader = "Basic $authEncoded"
             val headers: MutableMap<String, String> = HashMap()
             headers["Authorization"] = authHeader*/
            webview!!.loadUrl("https://www.officinetop.com/video/"/*, headers*/)
            webview.requestFocus()
            webSettings.javaScriptEnabled = true
            webSettings.loadWithOverviewMode = true
            webSettings.useWideViewPort = true
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            webview!!.setInitialScale(1)
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            val webViewClient = WebViewClientImpl(activity, progress_bar_webview)
            webview!!.webViewClient = webViewClient
        }

        return rootview
    }


}
