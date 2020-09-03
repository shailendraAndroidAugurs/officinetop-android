package com.officinetop.officine.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.News.WebViewClientImpl

import com.officinetop.officine.R
import kotlinx.android.synthetic.main.fragment_guide.view.*
import kotlinx.android.synthetic.main.fragment_videos.*


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
            webview.requestFocus();
            webSettings.javaScriptEnabled = true
            webSettings.loadWithOverviewMode = true;
            webSettings.useWideViewPort = true;
            webSettings.allowFileAccess = true;
            webSettings.allowContentAccess = true;
            webview!!.setInitialScale(1);
            webSettings.javaScriptCanOpenWindowsAutomatically = true;
            val webViewClient = WebViewClientImpl(activity, progress_bar_webview)
            webview!!.webViewClient = webViewClient
        }

        return rootview
    }


    /* lateinit var rootView: View

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         return inflater.inflate(R.layout.fragment_videos, container, false)
     }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
         rootView = view


         context?.let {

             val buttonTitles = rootView.resources.getStringArray(R.array.home_icon_titles)

             videos_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

                 override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                 }

                 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                     val view = layoutInflater.inflate(R.layout.item_my_cars, parent, false)
                     return object : RecyclerView.ViewHolder(view){}
                 }

                 override fun getItemCount(): Int {
                     return buttonTitles.size
                 }

             }



         }

     }*/

}
