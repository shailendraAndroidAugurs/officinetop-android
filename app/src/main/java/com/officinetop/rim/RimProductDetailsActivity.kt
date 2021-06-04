package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.adapter.CarAvailablemeasureListAdapter
import com.officinetop.data.Models
import com.officinetop.data.getDataFromResponse
import com.officinetop.data.getDataSetArrayFromResponse
import com.officinetop.data.isStatusCodeValid
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.loadImage
import kotlinx.android.synthetic.main.activity_available_rim.*
import kotlinx.android.synthetic.main.activity_rim_product_details.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RimProductDetailsActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    private var productDetails: Models.rimProductDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim_product_details)
        progressDialog = getProgressDialog()
        loadRimProductDetails()
    }

    fun loadRimProductDetails(){
        progressDialog.show()

        RetrofitClient.client.rimdetails("103311","103326").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                progressDialog.dismiss()

                progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataFromResponse(body)

                    Log.d("pdetails_data", "onResponse: models = "+body)
                    dataset.let { it1 ->
                        productDetails = Gson().fromJson<Models.rimProductDetails>(it1.toString(), Models.rimProductDetails::class.java)

                        if (productDetails != null) {
                            loadImage(productDetails!!.ImageUrl, prodcut_image)
                            product_name.text = productDetails!!.Manufacturer
                            product_name.text = productDetails!!.Manufacturer

                        }

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