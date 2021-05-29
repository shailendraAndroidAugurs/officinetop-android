package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.adapter.CarAvailablemeasureListAdapter
import com.officinetop.adapter.CarVersionListAdapter
import com.officinetop.data.Models
import com.officinetop.data.getDataSetArrayFromResponse
import com.officinetop.data.isStatusCodeValid
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.getProgressDialog
import kotlinx.android.synthetic.main.activity_available_rim.*
import kotlinx.android.synthetic.main.activity_compatible_model.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AvailableRimActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    val rimcarcarlistype: MutableList<Models.Availablecartypelist> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_rim)
        toolbar_title.text = resources.getString(R.string.available_sizes)
        progressDialog = getProgressDialog()
        var id = intent.getStringExtra("id")
        loaddata(id)
    }

    private fun loaddata(id: String) {
        progressDialog.show()

        RetrofitClient.client.rimavailablelist(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()

                progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataSetArrayFromResponse(body)

                    Log.d("caravailablelist", "onResponse: models = "+body)
                    Log.d("caravailablelist", "onResponse: models = "+dataset)

                    rimcarcarlistype.clear()

                    for (i in 0 until dataset.length()) {
                        val data = Gson().fromJson<Models.Availablecartypelist>(dataset.getJSONObject(i).toString(), Models.Availablecartypelist::class.java)
                        rimcarcarlistype.add(data)
                    }

                    var adapter = CarAvailablemeasureListAdapter(this@AvailableRimActivity,rimcarcarlistype)
                    var layoutmanager = LinearLayoutManager(this@AvailableRimActivity)
                    car_measure_available.layoutManager = layoutmanager
                    car_measure_available.adapter = adapter

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("AddVehicleActivity", "onResponse: models = "+t.message)

            }

        })
    }
}