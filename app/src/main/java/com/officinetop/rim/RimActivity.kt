package com.officinetop.rim

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.HomeActivity
import com.officinetop.R
import com.officinetop.adapter.CarVersionListAdapter
import com.officinetop.data.Models
import com.officinetop.data.getDataSetArrayFromResponse
import com.officinetop.data.isStatusCodeValid
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_compatible_model.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_rim.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.DecimalFormat

class RimActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim)
        setSupportActionBar(toolbar)
        progressDialog = getProgressDialog()
        toolbar_title.text = resources.getString(R.string.find_the_cicle)
        btn_search_for.setOnClickListener {
            if (!isValid(et_front_width, et_front_diameter, et_front_et, et_front_no_of_holes,et_front_distance_holes,et_rear_width,et_rear_diameter,et_rear_et,et_rear_no_of_holes,et_rear_distamce_holes)) {
               return@setOnClickListener
          }
            progressDialog.show()
            submitdata()
        }

        cb_front_rear_same.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                  et_rear_width.text =  et_front_width.text
                  et_rear_diameter.text = et_front_diameter.text
                  et_rear_et.text = et_front_et.text
                  et_rear_no_of_holes.text = et_front_no_of_holes.text
                  et_rear_distamce_holes.text = et_front_distance_holes.text
            }
        }
    }

    private fun submitdata() {
         var samedata : Int
         if(cb_front_rear_same.isChecked)
             samedata = 1
        else
             samedata = 0

          RetrofitClient.client.rimsearch(et_front_width.text.toString()+".00", et_front_diameter.text.toString(), et_front_et.text.toString(), et_front_no_of_holes.text.toString(),et_front_distance_holes.text.toString(),samedata,et_rear_width.text.toString()+".00",et_rear_diameter.text.toString(),et_rear_et.text.toString(),et_rear_no_of_holes.text.toString(),et_rear_distamce_holes.text.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                progressDialog.dismiss()
                Log.d("check_submit_data", "onResponse: models = "+body)
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

    private fun isValid(vararg editText: EditText): Boolean {
        var valid = true
        editText.forEach {
            if (it.text.isEmpty()) {
                it.error = "Field required"
                valid = false
            }
        }

        if (!valid)
            return valid

        return valid
    }


}