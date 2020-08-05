package com.officinetop.officine.virtual_garage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_my_vehicle.*
import kotlinx.android.synthetic.main.item_my_cars.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.intentFor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyVehicleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_vehicle)


        add_car.setOnClickListener { startActivity(intentFor<AddVehicleActivity>()) }


//        loadMyCars()
    }



/*
    private fun loadMyCars(){
        getBearerToken()?.let { token ->
            RetrofitClient.client.myCars(token).enqueue(object: Callback<Models.MyCar>{
                override fun onFailure(call: Call<Models.MyCar>, t: Throwable) {}

                override fun onResponse(call: Call<Models.MyCar>, response: Response<Models.MyCar>) {
                    val responseBody = response.body()
                    val body = responseBody?.toString()
                    Log.d("MyVehicleActivity", "onResponse: code = ${response.code()}, body = $body")

                    responseBody?.let { MyCar -> setAdapter(MyCar.dataSet) }


                }

            })


        }

    }*/


    private fun setAdapter( carList:List<Models.MyCarDataSet>){

        class Holder(view:View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
            val title = itemView.title_car
        }

        recycler_view.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<Holder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Holder {
                return Holder(layoutInflater.inflate(R.layout.item_my_cars, p0, false))
            }

            override fun getItemCount(): Int = carList.size

            override fun onBindViewHolder(p0: Holder, p1: Int) {
                val car = carList[p1]
                p0.title.text = "${car.carMakeName}\n${car.carModelName}\n${car.carVersion}"

            }

        }
    }
}
