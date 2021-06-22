package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.adapter.CarAvailablemeasureListAdapter
import com.officinetop.adapter.RimProductlistAdapter
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_available_rim.*
import kotlinx.android.synthetic.main.activity_rim_part.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RimPartActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    val rimProductlist: MutableList<Models.rimProductDetails> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim_part)

        if(intent.hasExtra("json_response")){
            bindDatainView(intent.getStringExtra("json_response"))
        }
    }

    fun loadRimProductDetails(){
        progressDialog.show()

        RetrofitClient.client.rimdetails("103311","103326",getLat(),getLong()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()

                progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataFromResponse(body)

                    Log.d("pdetails_data", "onResponse: models = "+body)
                    dataset.let { it1 ->


                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("pdetails_data",""+t.message)

            }

        })
    }


   fun bindDatainView(jsonresponse: String?) {
       val dataset = getDataSetArrayFromResponse(jsonresponse)
       rimProductlist.clear()

       for (i in 0 until dataset.length()) {
           val data = Gson().fromJson<Models.rimProductDetails>(dataset.getJSONObject(i).toString(), Models.rimProductDetails::class.java)
           rimProductlist.add(data)
       }

       var adapter = RimProductlistAdapter(this,rimProductlist)
       var layoutmanager = LinearLayoutManager(this)
       rv_product_list.layoutManager = layoutmanager
       rv_product_list.adapter = adapter
   }

}