package com.officinetop.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.officinetop.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BaseFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val content = inflater.inflate(R.layout.fragment_base, container, false)
        val fragmentHome = FragmentHome()
        childFragmentManager.beginTransaction()
                .replace(R.id.baseFragment, fragmentHome)
                .commit()
        return content
    }


}
