package com.officinetop.officine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import kotlinx.android.synthetic.main.spinner_item_layout.view.*

class SpinnerAdapterForVehicalType (ctx: Context, items: List<Models.TypeSpecificationForVehicalType>) : ArrayAdapter<Models.TypeSpecificationForVehicalType>(ctx, R.layout.spinner_item_layout, items) {

    override fun getView(position: Int, v: View?, parent: ViewGroup): View {

        return this.createView(position, v, parent)

    }

    override fun getDropDownView(position: Int, v: View?, parent: ViewGroup): View {

        return this.createView(position, v, parent)

    }

    private fun createView(position: Int, v: View?, parent: ViewGroup): View {

        val view = v
                ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_layout, parent, false)
        val item = getItem(position)


        view.name.text = item?.name

        return view

    }

}