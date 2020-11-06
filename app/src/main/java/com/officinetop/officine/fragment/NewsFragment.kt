package com.officinetop.officine.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.officinetop.officine.News.Fragmentnews
import com.officinetop.officine.News.GuideFragment
import com.officinetop.officine.R
import com.officinetop.officine.adapter.ViewPagerAdapter
import com.officinetop.officine.data.getLangLocale
import com.officinetop.officine.data.storeLangLocale
import com.officinetop.officine.utils.setAppLanguage
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
