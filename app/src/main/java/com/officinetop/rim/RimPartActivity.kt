package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.loadImage
import kotlinx.android.synthetic.main.activity_rim_product_details.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RimPartActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim_part)
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


}