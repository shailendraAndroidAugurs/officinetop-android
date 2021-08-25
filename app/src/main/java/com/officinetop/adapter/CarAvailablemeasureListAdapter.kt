package com.officinetop.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
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
import kotlinx.android.synthetic.main.item_faq.view.*
import kotlinx.android.synthetic.main.item_list_car_availble_rim.view.*
import kotlinx.android.synthetic.main.item_list_version_car.view.*

class CarAvailablemeasureListAdapter(private val context: Context, private val carlist: List<Models.Availablecartypelist>) : RecyclerView.Adapter<CarAvailablemeasureListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_car_availble_rim, parent, false))
    }

    override fun getItemCount(): Int = carlist.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv_daimeter.setText(context.resources.getString(R.string.diameter)+": "+carlist.get(position).front_diameter+"\"")
        holder.tv_quantitiy.setText("("+carlist.get(position).total_quantity+")")
//        Log.d("cheeck_measure_data",""+carlist.get(position).measures)
        var adapter = CarmeasureExpandableAdapter(context,carlist.get(position).measures)
//        var layoutmanager = LinearLayoutManager(context)
//        holder.car_measure_list.layoutManager = layoutmanager
//        holder.car_measure_list.adapter = adapter
//        var clicked = false
//        holder.rv_container_parent.setOnClickListener{
//            if(clicked){
//                holder.car_measure_list.visibility = View.GONE
//                clicked = false
//            }
//            else{
//                holder.car_measure_list.visibility = View.VISIBLE
//                clicked = true
//            }
//
//        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_daimeter: TextView = view.tv_daimeter
        val tv_quantitiy: TextView = view.tv_quantitiy
        val car_measure_list: RecyclerView = view.car_measure_list
        val rv_container_parent: RelativeLayout = view.rv_container_parent

    }


}