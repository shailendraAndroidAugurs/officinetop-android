package com.officinetop.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.R
import com.officinetop.binding_adapters.imageLoad
import com.officinetop.data.Models
import kotlinx.android.synthetic.main.item_faq.view.*
import kotlinx.android.synthetic.main.item_list_version_car.view.*

class CarVersionListAdapter(private val context: Context, private val carlist: List<Models.Carversionlist>) : RecyclerView.Adapter<CarVersionListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_version_car, parent, false))
    }

    override fun getItemCount(): Int = carlist.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv_car_type.setText(carlist.get(position).CarType)
        holder.tv_car_comment.setText(carlist.get(position).CarComment)
        holder.tv_car_type_desc.setText(carlist.get(position).CarType)
        holder.tv_manufacture_year.setText(carlist.get(position).ManufactureYear)
        imageLoad(holder.img_car_image,carlist.get(position).CarPic)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_type: TextView = view.tv_car_type
        val tv_car_comment: TextView = view.tv_car_comment
        val tv_car_type_desc: TextView = view.tv_car_type_desc
        val tv_manufacture_year: TextView = view.tv_manufacture_year
        val img_car_image: ImageView = view.img_car_image
    }


}