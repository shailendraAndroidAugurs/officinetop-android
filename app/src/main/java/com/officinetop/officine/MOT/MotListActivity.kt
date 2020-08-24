package com.officinetop.officine.MOT

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.mot_search_activity.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MotListActivity : BaseActivity() {

    private var mServicesList: MutableList<Models.MotServicesList> = ArrayList()

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
        motServive(getMotKm()!!, "0")
        btn_edit.setOnClickListener {
            ed_search_km.isEnabled = true
            if (btn_edit.text == getString(R.string.add_car)){
                motServive(ed_search_km.text.toString(), "1")

            }
            saveMotCarKM(ed_search_km.text.toString())
            btn_edit.text =  getString(R.string.add_car)



        }

    }


    private fun motServive(serviceKM: String, isEdit: String) {
        mServicesList.clear()
        progress_bar.visibility = View.VISIBLE
        RetrofitClient.client.getMOT(getSavedSelectedVehicleID(), serviceKM, isEdit, "en")
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
                                if(!body.getString("message").isNullOrBlank()){
                                    showInfoDialog(body.getString("message"))
                                }else{
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

}
