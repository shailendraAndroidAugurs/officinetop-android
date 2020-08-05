package com.officinetop.officine.tyre

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.SpinnerAdapter
import com.officinetop.officine.adapter.SpinnerAdapterForSeason
import com.officinetop.officine.adapter.SpinnerAdapterForVehicalType
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.getProgressDialog
import com.officinetop.officine.utils.loadImage
import com.officinetop.officine.utils.makeRound
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_tyre_customization.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TyreCustomizationActivity : BaseActivity() {
    private var vehicleType: String = ""
    private var vehicleTypeName: String = ""
    private var seasonType: String = ""
    private var seasonTypeName: String = ""
    private var speedIndex: String = ""
    private var tyreAlertImage: String = ""
    private var tyreAlertDescription: String = "Refer below measurements to enter correct tyre data."
    private var widthId: String = ""
    private var aspectRatioId: String = ""
    private var diameterId: String = ""
    private var widthName: String = ""
    private var aspectRatioName: String = ""
    private var diameterName: String = ""
    private var speedIndexName: String = ""
    private val widthList: ArrayList<Models.TypeSpecification> = ArrayList<Models.TypeSpecification>()
    private val diameterList: ArrayList<Models.TypeSpecification> = ArrayList<Models.TypeSpecification>()
    private val aspectRatioList: ArrayList<Models.TypeSpecification> = ArrayList<Models.TypeSpecification>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyre_customization)

        setSupportActionBar(toolbar)

        toolbar_title.text = resources.getString(R.string.customize_measures)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        info_btn.setOnClickListener {
            showDialog()
        }

        getTyreSpecificationApi()

        submit.setOnClickListener {
            var specification: String = spinner_vehicle_type.selectedItem.toString() + spinner_width.selectedItem.toString() +
                    spinner_aspect_ratio.selectedItem.toString() + spinner_speed_limit.selectedItem.toString()
            Log.d("tryeSpecification: ", "${specification}")

            val tyre = Models.TyreDetail(
                    vehicleType = vehicleType,
                    seasonType = seasonTypeName,
                    aspectRatio = aspectRatioName,
                    width = widthName.toFloat(),
                    diameter = diameterName.toFloat(),
                    rimWidth = widthName.toFloat(),
                    runFlat = checkbox_run_flat.isChecked,
                    reinforced = checkbox_reinforced.isChecked,
                    speedIndex = speedIndexName, vehicleTypeName = vehicleTypeName,
                    seasonTypeName = seasonTypeName,
                    filter_SeasonTypeId = seasonType,
                    filter_SpeedIndexId = speedIndex

            )


            var run_flat = if (checkbox_run_flat.isChecked) 1 else 0
            var reinforced = if (checkbox_reinforced.isChecked) 1 else 0
            var progressDialog = getProgressDialog(true)
            RetrofitClient.client.saveUserTyreDetails(
                    getUserId(),
                    vehicleType,
                    seasonType,
                    widthName.toInt(),
                    speedIndex,
                    run_flat,
                    reinforced,
                    1,
                    aspectRatioName,
                    diameterName,
                    getSelectedCar()?.carVersionModel?.idVehicle ?: "")
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progressDialog.dismiss()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val body = response.body()?.string()
                            progressDialog.dismiss()
                            Log.d("SAVE TYRE DETAILS", "API CALL SAVE TYRE DETAILS${getUserId()}${body}")
                            if (response.isSuccessful) {
                                try {
                                    val jsonObject = JSONObject(body)
                                    Toast.makeText(applicationContext, jsonObject.optString("message"), Toast.LENGTH_SHORT).show()
                                    setTyreDetail(tyre)//save tyre details to local(shared preferences)
                                    Handler().postDelayed({
                                        val intent = Intent(applicationContext, TyreListActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }, 500)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                        }
                    })


        }


    }

    private fun getTyreSpecificationApi() {
        RetrofitClient.client.getTyreSpecification(getSavedSelectedVehicleID(),"")
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val body = response.body()?.string()
                        //Log.d("TyreCustomizationActiv:","API CALL TYRE SPECIFICATION${body}")
                        if (response.isSuccessful) {
                            val jsonObject = JSONObject(body)
                            if (isStatusCodeValid(body)) {

                                val tyreTypeList: ArrayList<Models.TypeSpecificationForVehicalType> = ArrayList<Models.TypeSpecificationForVehicalType>()
                                val tyreSeasonList: ArrayList<Models.TypeSpecificationForSeason> = ArrayList<Models.TypeSpecificationForSeason>()
                                val speedIndexList: ArrayList<Models.TypeSpecification> = ArrayList<Models.TypeSpecification>()

                                val data = jsonObject.getJSONObject("data") as JSONObject
                                val width = data.getJSONArray("width") as JSONArray
                                val aspect_ratio = data.getJSONArray("aspect_ratio") as JSONArray
                                val diameter = data.getJSONArray("diameter") as JSONArray
                                val speed_index = data.getJSONArray("speed_index") as JSONArray
                                val tyre_type = data.getJSONArray("tyre_type") as JSONArray
                                val season_tyre_type = data.getJSONArray("season_tyre_type") as JSONArray
                                for (widthobj in 0 until width.length()) {
                                    val widthobjdata: JSONObject = width.get(widthobj) as JSONObject
                                    widthList.add(Models.TypeSpecification(widthobjdata.optString("value"), widthobjdata.optString("id")))
                                }
                                for (aspectratio in 0 until aspect_ratio.length()) {
                                    val aspectratioObj: JSONObject = aspect_ratio.get(aspectratio) as JSONObject
                                    aspectRatioList.add(Models.TypeSpecification(aspectratioObj.optString("value"), aspectratioObj.optString("id")))
                                }
                                for (diameterobj in 0 until diameter.length()) {
                                    val diameterobjdata: JSONObject = diameter.get(diameterobj) as JSONObject
                                    diameterList.add(Models.TypeSpecification(diameterobjdata.optString("value"), diameterobjdata.optString("id")))
                                }
                                for (speedindex in 0 until speed_index.length()) {
                                    val speedindexObj: JSONObject = speed_index.get(speedindex) as JSONObject
                                    speedIndexList.add(Models.TypeSpecification(speedindexObj.optString("name"), speedindexObj.optString("id")))
                                }

                                for (tyretype in 0 until tyre_type.length()) {
                                    val vehicaltypeObject: JSONObject = tyre_type.get(tyretype) as JSONObject
                                    val tyretypeObj = Gson().fromJson(vehicaltypeObject.toString(), Models.TypeSpecificationForVehicalType::class.java)
                                    if (tyretypeObj != null) {
                                        tyreTypeList.add(Models.TypeSpecificationForVehicalType(tyretypeObj.name!!, tyretypeObj.id!!, tyretypeObj.vhicle_type_arr!!))
                                    }


                                }
                                for (season in 0 until season_tyre_type.length()) {
                                    val seasonObj: JSONObject = season_tyre_type.get(season) as JSONObject
                                    tyreSeasonList.add(Models.TypeSpecificationForSeason(seasonObj.optString("name"), seasonObj.optString("id"), seasonObj.optString("code2")))
                                }


                                spinner_width.adapter = SpinnerAdapter(applicationContext, widthList.distinct())
                                spinner_diameter.adapter = SpinnerAdapter(applicationContext, diameterList.distinct())
                                spinner_aspect_ratio.adapter = SpinnerAdapter(applicationContext, aspectRatioList.distinct())
                                spinner_vehicle_type.adapter = SpinnerAdapterForVehicalType(applicationContext, tyreTypeList.distinct())
                                spinner_season_type.adapter = SpinnerAdapterForSeason(applicationContext, tyreSeasonList.distinct())






                                spinner_speed_limit.adapter = SpinnerAdapter(applicationContext, speedIndexList.distinct())
                                spinner_width.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecification = p0?.getItemAtPosition(p2) as Models.TypeSpecification
                                        Log.d("selected vehi type: ", items.code)
                                        widthId = items.code
                                        widthName = items.name

                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }
                                }
                                spinner_diameter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecification = p0?.getItemAtPosition(p2) as Models.TypeSpecification
                                        Log.d("selected vehi type: ", items.code)
                                        diameterId = items.code
                                        diameterName = items.name

                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }
                                }
                                spinner_aspect_ratio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecification = p0?.getItemAtPosition(p2) as Models.TypeSpecification
                                        Log.d("selected vehi type: ", items.code)
                                        aspectRatioId = items.code
                                        aspectRatioName = items.name

                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }
                                }

                                spinner_vehicle_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecificationForVehicalType = p0?.getItemAtPosition(p2) as Models.TypeSpecificationForVehicalType
                                        Log.d("selectedVehicaltype: ", items.id)
                                        vehicleType = items.id
                                        vehicleTypeName = items.name

                                        /*val tyreSeasonList1: ArrayList<Models.TypeSpecificationForSeason> = ArrayList<Models.TypeSpecificationForSeason>()
                                        if (items.vhicle_type_arr != null) {
                                            for (code in items.vhicle_type_arr!!) {

                                                tyreSeasonList1.addAll(tyreSeasonList.filter { it.CodeName == code })
                                            }
                                        }
                                        if (tyreSeasonList1.size == 0) {
                                            seasonType = ""
                                            seasonTypeName = "items.name"
                                        }
                                        spinner_season_type.adapter = SpinnerAdapterForSeason(applicationContext, tyreSeasonList1.distinct())
*/
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }
                                }


                                spinner_season_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecificationForSeason = p0?.getItemAtPosition(p2) as Models.TypeSpecificationForSeason
                                        Log.d("selected vehie season: ", items.id)

                                        seasonType = items.id
                                        seasonTypeName = items.name
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                                }




                                spinner_speed_limit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecification = p0?.getItemAtPosition(p2) as Models.TypeSpecification
                                        Log.d("selected speed type: ", items.code)
                                        speedIndex = items.code
                                        speedIndexName = items.name
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }
                                }

                                tyreAlertImage = data.getString("image")
                                loadImage(tyreAlertImage, tyre_preview_image)

                                tyreAlertDescription = data.getString("discription")
                            } else {
                                if (jsonObject.has("message") && !jsonObject.isNull("message")) {
                                    showInfoDialog(jsonObject.get("message").toString()) {
                                        finish()
                                    }
                                }
                            }
                        }
                    }
                }
                )
    }


    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_title_with_image)

        val alertImage = dialog.findViewById<ImageView>(R.id.alert_tyre_image)
        val alertDescription = dialog.findViewById<TextView>(R.id.item_sub_title)
        val alertClosePopup = dialog.findViewById<TextView>(R.id.close_popup)

        dialog.makeRound(this)

        dialog.show()
        loadImage(tyreAlertImage, alertImage)
        alertDescription.text = tyreAlertDescription
        alertClosePopup.setOnClickListener { dialog.dismiss() }

    }
}
