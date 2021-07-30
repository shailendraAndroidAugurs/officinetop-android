package com.officinetop.rim

import android.app.ProgressDialog
import android.nfc.tech.MifareUltralight
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.adapter.PaginationListener
import com.officinetop.adapter.RimProductlistAdapter
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.Constant
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.showInfoDialog
import com.officinetop.virtual_garage.AddVehicleActivity
import kotlinx.android.synthetic.main.activity_maintenance.*
import kotlinx.android.synthetic.main.activity_rim.*
import kotlinx.android.synthetic.main.activity_rim_part.*
import kotlinx.android.synthetic.main.activity_tyre_list.*
import kotlinx.android.synthetic.main.activity_tyre_list.recycler_view
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RimPartActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    val rimProductlist: MutableList<Models.rimProductDetails> = ArrayList()
    var layoutmanager = LinearLayoutManager(this)
    var adapter = RimProductlistAdapter(this,rimProductlist)
    private var isListLoading = false
    private val PAGESTART = 0
    private var currentPage = PAGESTART
    private var isLastPageOfList = false
    private var isFirstTimeLoading = true


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

        loadProductWithModel(false,"2","4","4","4","4",currentPage)
        setPaginationScroll()

    }


    private fun setPaginationScroll() {

        val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = layoutmanager.childCount
                val totalItemCount: Int = layoutmanager.itemCount
                val firstVisibleItemPosition: Int = layoutmanager.findFirstVisibleItemPosition()
                if (!isListLoading && !isLastPageOfList) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= MifareUltralight.PAGE_SIZE) {
                        currentPage += 5
                        isListLoading = true
/*
                        progress_bar_bottom.visibility = View.VISIBLE
*/
                        loadProductWithModel(true,"2","4","4","4","4",currentPage)

                    }
                }
            }
        }
        rv_product_list.addOnScrollListener(recyclerViewOnScrollListener)
    }


    fun loadProductWithModel(ispagination : Boolean,carTypeId: String, frontDiameterId: String, rearDiameterId: String, frontWidthId: String, rearWidthId: String,limit : Int) {
        progressDialog = getProgressDialog()
        progressDialog.show()
       RetrofitClient.client.getRimproductlist(carTypeId,frontDiameterId,rearDiameterId,frontWidthId,rearWidthId,getUserId(),limit).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                Log.d("rim_productl_list", "onResponse: models = "+body)
                isListLoading = false
                if (isStatusCodeValid(body)) {
                    val dataset = getDataSetArrayFromResponse(body)
                    progressDialog.dismiss()
                    for (i in 0 until dataset.length()) {
                        val data = Gson().fromJson<Models.rimProductDetails>(dataset.getJSONObject(i).toString(), Models.rimProductDetails::class.java)
                        rimProductlist.add(data)
                    }
                   if(ispagination){
                       adapter.notifyDataSetChanged()

                   }else{
                       adapter = RimProductlistAdapter(applicationContext,rimProductlist)
                       rv_product_list.layoutManager = layoutmanager
                       rv_product_list.adapter = adapter
                       progressDialog.dismiss()
                   }
                }
                else{
                    isLastPageOfList = true
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
                isLastPageOfList = true
                Log.d("pdetails_data",""+t.message)

            }

        })
    }


    private fun submitdata() {
        var samedata : Int
        if(cb_front_rear_same.isChecked)
            samedata = 1
        else
            samedata = 0

        RetrofitClient.client.rimsearch(et_front_width.text.toString()+".00", et_front_diameter.text.toString(), et_front_et.text.toString(), et_front_no_of_holes.text.toString(),et_front_distance_holes.text.toString(),samedata,et_rear_width.text.toString()+".00",et_rear_diameter.text.toString(),et_rear_et.text.toString(),et_rear_no_of_holes.text.toString(),et_rear_distamce_holes.text.toString(),5).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                progressDialog.dismiss()
                if (isStatusCodeValid(body)) {
                    startActivity(intentFor<RimPartActivity>().putExtra("json_response", body))
                }
                else{
                    val body = JSONObject(body)
                    if (!body.getString("message").isNullOrBlank()) {
                        showInfoDialog(body.getString("message"))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("check_submit_data", "error : = "+t.message)

            }

        })
    }


   fun bindDatainView(jsonresponse: String?) {
    /*   val dataset = getDataSetArrayFromResponse(jsonresponse)
       rimProductlist.clear()

       for (i in 0 until dataset.length()) {
           val data = Gson().fromJson<Models.rimProductDetails>(dataset.getJSONObject(i).toString(), Models.rimProductDetails::class.java)
           rimProductlist.add(data)
       }
*/

       adapter = RimProductlistAdapter(this,rimProductlist)
       rv_product_list.layoutManager = layoutmanager
       rv_product_list.adapter = adapter

   }

}