package com.officinetop.News

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.officinetop.R
import kotlinx.android.synthetic.main.fragment_guide.view.*


class VideoFragment : Fragment() {


    lateinit var parentview: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        parentview = inflater.inflate(R.layout.fragment_guide, container, false)
        with(parentview) {
            val webSettings = webview!!.settings
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



        return parentview
    }



}
