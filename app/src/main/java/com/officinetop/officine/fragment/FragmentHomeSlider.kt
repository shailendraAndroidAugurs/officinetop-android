package com.officinetop.officine.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.officinetop.officine.R
import com.officinetop.officine.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_grid_home_btn.view.*
import kotlinx.android.synthetic.main.item_grid_home_btn.view.item_title as item_title1


class FragmentHomeSlider : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.home_slider_layout, container, false)
        return rootView
    }


}