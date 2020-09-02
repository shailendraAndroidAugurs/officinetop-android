package com.officinetop.officine.MOT

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.adapter.SpinnerAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_tyre_customization.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.mot_search_activity.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MotListActivity : BaseActivity() {

    private var mServicesList: MutableList<Models.MotServicesList> = ArrayList()
    private var mMotSchedule: MutableList<Models.MotSchedule> = ArrayList()
    private  var scheduleId=""
    private val mMotScheduleData: ArrayList<Models.TypeSpecification> = ArrayList<Models.TypeSpecification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mot_search_activity)
        initView()

    }


    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.MOT)
        ed_search_km.isEnabled = false
        ed_search_km.setText(getMotKm())
        // motServive(getMotKm()!!, "0")
        CallMotSchedule()
        btn_edit.setOnClickListener {
            ed_search_km.isEnabled = true
            if (btn_edit.text == getString(R.string.add_car)) {
                motServive(ed_search_km.text.toString(), "1")

            }
            saveMotCarKM(ed_search_km.text.toString())
            btn_edit.text = getString(R.string.add_car)


        }

    }


    private fun motServive(serviceKM: String, isEdit: String) {
        mServicesList.clear()
        progress_bar.visibility = View.VISIBLE
        RetrofitClient.client.getMOT(getSavedSelectedVehicleID(), serviceKM, isEdit, "en", getSelectedCar()?.carVersionModel?.idVehicle!!, scheduleId)
                .onCall { networkException, response ->
                    response?.let {
                        progress_bar.visibility = View.GONE
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            //  mServicesList.clear()
                            if (body.has("data_set") && !body.isNull("data_set")) {
                                val dataSet = body.get("data_set") as JSONArray
                                if (dataSet.length() != 0) {
                                    mServicesList.clear()
                                    for (i in 0 until dataSet.length()) {
                                        val itemData = dataSet.get(i) as JSONObject
                                        val itemsData = Gson().fromJson<Models.MotServicesList>(itemData.toString(), Models.MotServicesList::class.java)
                                        mServicesList.add(itemsData)
                                    }
                                    ed_search_km.isEnabled = false
                                    btn_edit.text = getString(R.string.Edit)
                                    saveMotCarKM(ed_search_km.text.toString())
                                    //getSelectedCar()!!.km_of_cars = ed_search_km.text.toString()
                                }
                                bindMotServices()
                            } else {
                                //  mServicesList.clear()
                                if (!body.getString("message").isNullOrBlank()) {
                                    showInfoDialog(body.getString("message"))
                                } else {
                                    showInfoDialog(getString(R.string.DatanotFound))
                                }

                            }
                        } else
                        //   mServicesList.clear()
                            showInfoDialog(response.message())
                    }
                }
    }

    private fun bindMotServices() {
        val genericAdapter = GenericAdapter<Models.MotServicesList>(this@MotListActivity, R.layout.item_motlist)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(this@MotListActivity, MotDetailActivity::class.java)
                intent.putExtra("motObject", Gson().toJson(mServicesList[position]))
                startActivity(intent)

            }

            override fun onItemClick(view: View, position: Int) {
            }
        })
        recycler_view_mot.adapter = genericAdapter
        genericAdapter.addItems(mServicesList)
    }


    private fun CallMotSchedule() {
        progress_bar.visibility = View.VISIBLE
        mMotSchedule.clear()
        RetrofitClient.client.getMotServiceSchedule(getSelectedCar()?.carVersionModel?.idVehicle!!)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                        response?.let {
                            progress_bar.visibility = View.GONE
                            if (response.isSuccessful) {
                                val body = JSONObject(response.body()?.string())
                                if (body.has("data_set") && !body.isNull("data_set")) {
                                    val dataSet = body.get("data_set") as JSONArray
                                    if (dataSet.length() != 0) {
                                        mMotScheduleData.clear()
                                        for (i in 0 until dataSet.length()) {
                                            val itemData = dataSet.get(i) as JSONObject
                                            val itemsData = Gson().fromJson<Models.MotSchedule>(itemData.toString(), Models.MotSchedule::class.java)
                                            mMotSchedule.add(itemsData)
                                            mMotScheduleData.add(Models.TypeSpecification(if(itemsData.service_schedule_description.isNullOrEmpty())itemsData.service_schedule_id else itemsData.service_schedule_description , itemsData.schedule_id))

                                        }
                                        if (mMotScheduleData.size > 1) {
                                            Log.d("MotList","mMotScheduleData  "+mMotScheduleData.toString())

                                            ll_carcondition.visibility = View.VISIBLE
                                            bindSpinner()
                                        }

                                        scheduleId=mMotSchedule[0].schedule_id
                                        motServive(getMotKm()!!, "0")
                                    } else {
                                        showInfoDialog(getString(R.string.DatanotFound))
                                    }

                                } else {

                                    if (!body.getString("message").isNullOrBlank()) {
                                        showInfoDialog(body.getString("message"))
                                    } else {
                                        showInfoDialog(getString(R.string.DatanotFound))
                                    }

                                }
                            } else

                                showInfoDialog(response.message())
                        }
                    }

                })
    }


    private fun bindSpinner(){
        spinner_schedule.adapter = SpinnerAdapter(applicationContext, mMotScheduleData)
        Log.d("MotList","bindingSpinner")
        spinner_schedule.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val items: Models.TypeSpecification = p0?.getItemAtPosition(p2) as Models.TypeSpecification
                Log.d("selected vehi type: ", items.code)
                scheduleId=items.code
                motServive(getMotKm()!!, "0")

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

}
