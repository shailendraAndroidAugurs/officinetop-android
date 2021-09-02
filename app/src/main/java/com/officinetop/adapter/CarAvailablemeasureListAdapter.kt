package com.officinetop.adapter

import android.app.ProgressDialog
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
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.binding_adapters.imageLoad
import com.officinetop.data.Models
import com.officinetop.data.getDataSetArrayFromResponse
import com.officinetop.data.isStatusCodeValid
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.rim.AvailableRimActivity
import com.officinetop.utils.Constant
import com.officinetop.utils.getProgressDialog
import kotlinx.android.synthetic.main.activity_available_rim.*
import kotlinx.android.synthetic.main.item_faq.view.*
import kotlinx.android.synthetic.main.item_list_car_availble_rim.view.*
import kotlinx.android.synthetic.main.item_list_version_car.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarAvailablemeasureListAdapter(private val context: Context, private val carlist: List<Models.Availablecartypelist>) : RecyclerView.Adapter<CarAvailablemeasureListAdapter.ViewHolder>() {

    lateinit var progressDialog: ProgressDialog

    var diameter_rear_id = ""
    var car_type_id = ""
    var diameter_front_id = ""
    val rimcarcarlistype: MutableList<Models.Availablecartypelist> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        progressDialog = getProgressDialog()
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_car_availble_rim, parent, false))
    }

    override fun getItemCount(): Int = carlist.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv_daimeter.setText(context.resources.getString(R.string.diameter)+": "+carlist.get(position).front_diameter+"\"")
        holder.tv_quantitiy.setText("("+carlist.get(position).total_quantity+")")
//        Log.d("cheeck_measure_data",""+carlist.get(position).measures)
//        var adapter = CarmeasureExpandableAdapter(context,carlist.get(position).measures)
//        var layoutmanager = LinearLayoutManager(context)
//        holder.car_measure_list.layoutManager = layoutmanager
//        holder.car_measure_list.adapter = adapter
        var clicked = false
        holder.rv_container_parent.setOnClickListener{
            if(clicked){
                holder.car_measure_list.visibility = View.GONE
                clicked = false
            }
            else{

//                progressDialog.show()

                RetrofitClient.client.rimavailablesublist(carlist.get(position).IdRearDiameter,carlist.get(position).IdCarType,carlist.get(position).IdFrontDiameter).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val body = response.body()?.string()
//                        progressDialog.dismiss()

                        if (isStatusCodeValid(body)) {
//                            Toast.makeText(context,"this is toast message",Toast.LENGTH_SHORT).show()
                            val dataset = getDataSetArrayFromResponse(body)
//
                            Log.d("caravailablelist", "onResponse: models = "+body)
                            Log.d("caravailablelist", "onResponse: models = "+dataset)

                            rimcarcarlistype.clear()

                            for (i in 0 until dataset.length()) {
                                val data = Gson().fromJson<Models.Availablecartypelist>(dataset.getJSONObject(i).toString(), Models.Availablecartypelist::class.java)
                                rimcarcarlistype.add(data)
                            }

                            var adapter = CarmeasureExpandableAdapter(context,rimcarcarlistype)
                            var layoutmanager = LinearLayoutManager(context)
                            holder.car_measure_list.layoutManager = layoutmanager
                            holder.car_measure_list.adapter = adapter

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                        Log.d("AddVehicleActivity", "onResponse: models = "+t.message)

                    }

                })

                holder.car_measure_list.visibility = View.VISIBLE
                clicked = true
            }

        }
    }
    inline fun getDataSetArrayFromResponse(body: String?): JSONArray {
        try {
            body?.let { return JSONObject(body).getJSONArray(Constant.Fields.dataset) }
        } catch (e: Exception) {
        }
        return JSONArray()

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_daimeter: TextView = view.tv_daimeter
        val tv_quantitiy: TextView = view.tv_quantitiy
        val car_measure_list: RecyclerView = view.car_measure_list
        val rv_container_parent: RelativeLayout = view.rv_container_parent

    }


}