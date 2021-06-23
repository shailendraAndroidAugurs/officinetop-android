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
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.R
import com.officinetop.binding_adapters.imageLoad
import com.officinetop.data.Models
import com.officinetop.rim.AvailableRimActivity
import com.officinetop.rim.RimPartActivity
import kotlinx.android.synthetic.main.item_faq.view.*
import kotlinx.android.synthetic.main.item_list_car_availble_rim.view.*
import kotlinx.android.synthetic.main.item_list_version_car.view.*
import org.jetbrains.anko.intentFor

class CarmeasureExpandableAdapter(private val context: Context, private val carlist: List<Models.Measures>) : RecyclerView.Adapter<CarmeasureExpandableAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_car_availble_rim, parent, false))
    }

    override fun getItemCount(): Int = carlist.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(carlist.get(position).front_width.equals(carlist.get(position).rear_width) && carlist.get(position).front_diameter.equals(carlist.get(position).rear_diameter)){
            holder.tv_daimeter.setText(context.resources.getString(R.string.front)+" - "+context.resources.getString(R.string.rear)+": "+carlist.get(position).front_width+"X"+carlist.get(position).front_diameter)
        }
        else{
            holder.tv_daimeter.setText(context.resources.getString(R.string.front)+": "+carlist.get(position).front_width+"X"+carlist.get(position).front_diameter+"\""+"\n"+
                    context.resources.getString(R.string.rear)+": "+carlist.get(position).rear_width+"X"+carlist.get(position).rear_diameter+"\"")
        }
        holder.tv_quantitiy.setText("("+carlist.get(position).quantity+")")

       holder.rv_container_parent.setOnClickListener {
           context.startActivity(context.intentFor<RimPartActivity>().
           putExtra("car_type_id", carlist.get(position).IdCarType).
           putExtra("front_diameter_id", carlist.get(position).IdFrontDiameter).
           putExtra("rear_diameter_id", carlist.get(position).IdRearDiameter).
           putExtra("front_width_id", carlist.get(position).IdFrontWidth).
           putExtra("rear_width_id", carlist.get(position).IdRearWidth).
           putExtra("call_from_model",true)
           )
       }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_daimeter: TextView = view.tv_daimeter
        val tv_quantitiy: TextView = view.tv_quantitiy
        val rv_container_parent: RelativeLayout = view.rv_container_parent

    }


}