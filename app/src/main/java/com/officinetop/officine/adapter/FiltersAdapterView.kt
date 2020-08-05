package com.officinetop.officine.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.officinetop.officine.view_models.ListItemViewModel

class FiltersAdapterView<T: ListItemViewModel>(val context: Activity, @LayoutRes val layoutId : Int) : BaseAdapter() {

    private val items = mutableListOf<T>()
    private val inflater : LayoutInflater? = null

    fun addItems(items: List<T>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItem(p0: Int): T {
        return items.get(p0)
    }

    override fun getCount(): Int = items.size

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val layoutInflater = inflater?: LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater,layoutId,p2,false)
        return binding.root
    }


    }