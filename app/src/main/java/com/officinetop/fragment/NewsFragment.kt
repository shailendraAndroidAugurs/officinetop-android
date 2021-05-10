package com.officinetop.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.officinetop.News.Fragmentnews
import com.officinetop.News.GuideFragment
import com.officinetop.R
import com.officinetop.adapter.ViewPagerAdapter
import com.officinetop.data.getLangLocale
import com.officinetop.data.storeLangLocale
import com.officinetop.utils.setAppLanguage
import kotlinx.android.synthetic.main.fragment_news.*


class NewsFragment : Fragment() {


    private lateinit var rootView: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        if (context?.getLangLocale() != null && !context?.getLangLocale().equals("")) {
            context?.setAppLanguage()
        } else {
            context?.storeLangLocale("it")
            context?.setAppLanguage()
        }

        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(Fragmentnews(), getString(R.string.news))
        adapter.addFragment(VideosFragment(), getString(R.string.videos))
        adapter.addFragment(GuideFragment(), getString(R.string.guides))
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }

}
