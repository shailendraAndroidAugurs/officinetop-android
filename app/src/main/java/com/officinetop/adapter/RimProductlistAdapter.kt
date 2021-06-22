package com.officinetop.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.R
import com.officinetop.binding_adapters.imageLoad
import com.officinetop.data.Models
import com.officinetop.rim.AvailableRimActivity
import kotlinx.android.synthetic.main.activity_available_rim.*
import kotlinx.android.synthetic.main.activity_rim_product_details.*
import kotlinx.android.synthetic.main.item_faq.view.*
import kotlinx.android.synthetic.main.item_list_car_availble_rim.view.*
import kotlinx.android.synthetic.main.item_list_rim_product.view.*
import kotlinx.android.synthetic.main.item_list_version_car.view.*

class RimProductlistAdapter(private val context: Context, private val productlist: List<Models.rimProductDetails>) : RecyclerView.Adapter<RimProductlistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_rim_product, parent, false))
    }

    override fun getItemCount(): Int = productlist.size


    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
      imageLoad(holder.prodcut_image,productlist.get(i).ImageUrl)
      holder.tv_type.text = productlist.get(i).Manufacturer
      holder.tv_type_name.text = productlist.get(i).TypeName
      holder.tv_type_color.text = productlist.get(i).TypeColor
      holder.tv_rim_anteriore.text = productlist.get(i).AlloyRimDescription
      holder.item_price.text = context.getString(R.string.prepend_euro_symbol_string,productlist.get(i).AlloyRimPrice)

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val prodcut_image: ImageView = view.prodcut_image
        val tv_type: TextView = view.tv_type
        val tv_type_name: TextView = view.tv_type_name
        val tv_type_color: TextView = view.tv_type_color
        val tv_rim_anteriore: TextView = view.tv_rim_anteriore
        val tv_rim_posteriore: TextView = view.tv_rim_posteriore
        val item_price: TextView = view.item_price


    }


}