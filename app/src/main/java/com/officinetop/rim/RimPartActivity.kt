package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.adapter.RimProductlistAdapter
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.Constant
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.showInfoDialog
import com.officinetop.virtual_garage.AddVehicleActivity
import kotlinx.android.synthetic.main.activity_rim_part.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONObject
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

        if(!intent.hasExtra("call_from_model")){
//            var car_type_id = intent.getStringExtra("car_type_id")
//            var front_diameter_id = intent.getStringExtra("front_diameter_id")
//            var rear_diameter_id = intent.getStringExtra("rear_diameter_id")
//            var front_width_id = intent.getStringExtra("front_width_id")
//            var rear_width_id = intent.getStringExtra("rear_width_id")
        }

        loadProductWithModel("2","4","4","4","4")
    }

  fun loadProductWithModel(carTypeId: String, frontDiameterId: String, rearDiameterId: String, frontWidthId: String, rearWidthId: String) {
        progressDialog = getProgressDialog()
        progressDialog.show()
       RetrofitClient.client.getRimproductlist(carTypeId,frontDiameterId,rearDiameterId,frontWidthId,rearWidthId,getUserId()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                Log.d("rim_productl_list", "onResponse: models = "+body)

                if (isStatusCodeValid(body)) {
                    bindDatainView(body)
                }
                else{
                    progressDialog.dismiss()
                    val body = JSONObject(body)
                    if (!body.getString("message").isNullOrBlank()) {
                        showInfoDialog(body.getString("message"), true) {
                           onBackPressed()
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
       progressDialog.dismiss()
   }

}