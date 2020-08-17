package com.officinetop.officine.News

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.officinetop.officine.R
import kotlinx.android.synthetic.main.fragment_guide.view.*
import java.util.*
import kotlin.collections.HashMap


class GuideFragment : Fragment() {


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
            webview!!.loadUrl("https://www.officinetop.com/guide/"/*, headers*/)
            webview.requestFocus();
            webSettings.javaScriptEnabled = true
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowContentAccess(true);
            webview!!.setInitialScale(1);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            val webViewClient = WebViewClientImpl(activity, progress_bar_webview)
            webview!!.webViewClient = webViewClient
        }

        return rootview
    }

}
