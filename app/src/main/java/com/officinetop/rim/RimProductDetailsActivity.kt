package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.adapter.CarAvailablemeasureListAdapter
import com.officinetop.data.*
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
    var front_id = ""
    var rear_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim_product_details)
        progressDialog = getProgressDialog()
        if(intent.hasExtra("front_id")){
            front_id = intent.getStringExtra("front_id")
        }
        if(intent.hasExtra("rear_id")){
            rear_id = intent.getStringExtra("rear_id")
        }
        loadRimProductDetails()
    }

    fun loadRimProductDetails(){
        progressDialog.show()

        RetrofitClient.client.rimdetails(front_id,rear_id,getLat(),getLong()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()

                progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataFromResponse(body)

                    Log.d("pdetails_data", "onResponse: models = "+body)
                    dataset.let { it1 ->
                        productDetails = Gson().fromJson<Models.rimProductDetails>(it1.toString(), Models.rimProductDetails::class.java)

                        if (productDetails != null) {
                            loadImage(productDetails!!.ImageUrl, prodcut_image)
                            tv_type.text = productDetails!!.Manufacturer
                            tv_type_name.text = productDetails!!.TypeName
                            tv_type_color.text = productDetails!!.TypeColor
                            tv_rim_anteriore.text = productDetails!!.rim_anteriore
                            tv_rim_posteriore.text = productDetails!!.rim_posteriore

                            //binding product description data
                            tv_width.text = "Ant. "+productDetails!!.front_width + " - Post."+productDetails!!.rear_width
                            tv_diameter.text = productDetails!!.front_diameter
                            tv_etoffset.text = productDetails!!.AlloyRimET
                            tv_no_of_holes.text = productDetails!!.AlloyRimLochzahl
                            tv_distance_between_holes.text = productDetails!!.AlloyRimLochkreis
                            tv_winter_compatibilty.text = productDetails!!.winter
                            tv_color.text = productDetails!!.TypeColor

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