package com.officinetop.tyre

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.isapanah.awesomespinner.AwesomeSpinner
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.SpinnerAdapter
import com.officinetop.adapter.SpinnerAdapterForSeason
import com.officinetop.adapter.SpinnerAdapterForVehicalType
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.loadImage
import com.officinetop.utils.makeRound
import com.officinetop.utils.showInfoDialog
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
    private var speedLoadIndexDesc: String = ""
    private var speedLoadIndexName: String = ""

    private var tyreAlertImage: String = ""
    private var tyreAlertDescription: String = ""
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
    lateinit var selectedTyreDetail: Models.TyreDetail
    private var isPreviousSelectedMeasurement = false
    private var IsMeaurementEditId = ""
    private var IsSavedInlocal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyre_customization)

        setSupportActionBar(toolbar)

        toolbar_title.text = resources.getString(R.string.customize_measures)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        info_btn.setOnClickListener {
            showDialog()
        }
        if (intent.extras != null && intent.hasExtra("currentlySelectedMeasurement")) {
            selectedTyreDetail = Gson().fromJson(intent.getStringExtra("currentlySelectedMeasurement"), Models.TyreDetail::class.java)
            isPreviousSelectedMeasurement = true
        }
        if (intent.extras != null && intent.hasExtra("editId")) {
            IsMeaurementEditId = intent.getStringExtra("editId")
        }
        if (intent.extras != null && intent.hasExtra("IsSavedInlocal")) {
            IsSavedInlocal = intent.getStringExtra("IsSavedInlocal")

        }

        getTyreSpecificationApi()

        submit.setOnClickListener {

            if (spinner_width.selectedItemPosition == 0) {
                Toast.makeText(this, getString(R.string.please_select_width), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (spinner_aspect_ratio.selectedItemPosition == 0) {
                Toast.makeText(this, getString(R.string.please_select_aspect), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (spinner_diameter.selectedItemPosition == 0) {
                Toast.makeText(this, getString(R.string.please_select_diameter), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val specification: String = spinner_vehicle_type.selectedItem.toString() + spinner_width.selectedItem.toString() +
                    spinner_aspect_ratio.selectedItem.toString() + spinner_speed_limit.selectedItem.toString()

            val tyre = Models.TyreDetail(
                    id = IsSavedInlocal,
                    vehicleType = vehicleType,
                    seasonName = seasonTypeName,
                    aspectRatio = aspectRatioName,
                    width = widthName.toFloat(),
                    diameter = diameterName.toFloat(),
                    rimWidth = widthName.toFloat(),
                    runFlat = checkbox_run_flat.isChecked,
                    reinforced = checkbox_reinforced.isChecked,
                    speedIndexName = speedIndexName,
                    vehicleTypeName = vehicleTypeName,
                    speedIndexId = speedIndex,
                    seasonId = seasonType,
                    speed_load_index = speedLoadIndexName,
                    speed_load_indexDesc = speedLoadIndexDesc,
                    cust_speedIndexId = speedIndex,
                    cust_seasonId = seasonType,
                    cust_speedLoad_indexName = speedLoadIndexName,
                    cust_runflat = checkbox_run_flat.isChecked,
                    cust_reinforced = checkbox_reinforced.isChecked,
                    cust_seasonName = seasonTypeName,
                    cust_speed_indexName = speedIndexName,
                    cust_speedLoad_indexDesc = speedLoadIndexDesc
            )


            val run_flat = if (checkbox_run_flat.isChecked) 1 else 0
            val reinforced = if (checkbox_reinforced.isChecked) 1 else 0
            val progressDialog = getProgressDialog(true)
            if (!seasonType.isNullOrBlank() && seasonType == "0") {
                seasonType = ""
            }
            if (!speedIndex.isNullOrBlank() && speedIndex == "0") {
                speedIndex = ""
            }

            if (!speedLoadIndexDesc.isNullOrBlank() && speedLoadIndexDesc == "0") {
                speedLoadIndexDesc = ""
            }
            if (getUserId().isNullOrBlank()) {
                setTyreDetail(tyre)//save tyre details to local(shared preferences)
                progressDialog.dismiss()
                val intent = Intent(applicationContext, TyreListActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                onNewIntent(intent)
                finish()


            } else if (!IsMeaurementEditId.isNullOrBlank()) {
                RetrofitClient.client.editUserTyreDetails(
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
                        getSelectedCar()?.carVersionModel?.idVehicle
                                ?: "", speedLoadIndexName, IsMeaurementEditId)
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
                                        //Toast.makeText(this@TyreCustomizationActivity, jsonObject.optString("message"), Toast.LENGTH_SHORT).show()
                                        setTyreDetail(tyre)
                                        val intent = Intent(this@TyreCustomizationActivity, TyreListActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                        intent.putExtra("result", "edit")
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                }
                            }
                        })


            } else {
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
                        getSelectedCar()?.carVersionModel?.idVehicle ?: "", speedLoadIndexName)
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

                                        if (jsonObject.has("data") && jsonObject.get("data") != null) {
                                            val data = jsonObject.getJSONObject("data") as JSONObject

                                            val TyreMeasurementResponse = Gson().fromJson<MeasurementDataSetItem>(data.toString(), MeasurementDataSetItem::class.java)
                                            tyre.id = TyreMeasurementResponse.id.toString()
                                        }

                                        setTyreDetail(tyre)//save tyre details to local(shared preferences)
                                        Handler().postDelayed({

                                            val intent = Intent(this@TyreCustomizationActivity, TyreListActivity::class.java)
                                            if (isPreviousSelectedMeasurement) {
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                                intent.putExtra("result", "edit")
                                                setResult(RESULT_OK, intent)
                                                //  onNewIntent(intent)

                                            } else {

                                                startActivity(intent)
                                            }
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
    }

    private fun getTyreSpecificationApi() {
        RetrofitClient.client.getTyreSpecification(getSavedSelectedVehicleID(), "", getUserId())
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
                                val speedLoadIndexList: ArrayList<Models.TypeSpecification> = ArrayList<Models.TypeSpecification>()
                                val data = jsonObject.getJSONObject("data") as JSONObject
                                val width = data.getJSONArray("width") as JSONArray
                                val aspect_ratio = data.getJSONArray("aspect_ratio") as JSONArray
                                val diameter = data.getJSONArray("diameter") as JSONArray
                                val speed_index = data.getJSONArray("speed_index") as JSONArray
                                val tyre_type = data.getJSONArray("tyre_type") as JSONArray
                                val season_tyre_type = data.getJSONArray("season_tyre_type") as JSONArray
                                val speed_load_index = data.getJSONArray("load_index") as JSONArray

                                widthList.clear()
                                aspectRatioList.clear()
                                diameterList.clear()



                                widthList.add(0, Models.TypeSpecification(getString(R.string.select), "0"))
                                for (widthobj in 0 until width.length()) {
                                    val widthobjdata: JSONObject = width.get(widthobj) as JSONObject
                                    widthList.add(widthobj + 1, Models.TypeSpecification(widthobjdata.optString("value"), widthobjdata.optString("id")))
                                }
                                aspectRatioList.add(0, Models.TypeSpecification(getString(R.string.select), "0"))
                                for (aspectratio in 0 until aspect_ratio.length()) {
                                    val aspectratioObj: JSONObject = aspect_ratio.get(aspectratio) as JSONObject


                                    aspectRatioList.add(aspectratio + 1, Models.TypeSpecification(aspectratioObj.optString("value"), aspectratioObj.optString("id")))
                                }
                                diameterList.add(0, Models.TypeSpecification(getString(R.string.select), "0"))
                                for (diameterobj in 0 until diameter.length()) {
                                    val diameterobjdata: JSONObject = diameter.get(diameterobj) as JSONObject
                                    diameterList.add(diameterobj + 1, Models.TypeSpecification(diameterobjdata.optString("value"), diameterobjdata.optString("id")))
                                }
                                speedIndexList.clear()
                                speedIndexList.add(0, Models.TypeSpecification(getString(R.string.all), "0"))
                                for (speedindex in 0 until speed_index.length()) {
                                    val speedindexObj: JSONObject = speed_index.get(speedindex) as JSONObject
                                    speedIndexList.add(speedindex + 1, Models.TypeSpecification(speedindexObj.optString("description"), speedindexObj.optString("id")))
                                }


                                speedLoadIndexList.clear()
                                speedLoadIndexList.add(0, Models.TypeSpecification(getString(R.string.all), "0"))
                                for (loadindex in 0 until speed_load_index.length()) {
                                    val speedindexObj: JSONObject = speed_load_index.get(loadindex) as JSONObject
                                    if (speedindexObj != null)
                                        speedLoadIndexList.add(loadindex + 1, Models.TypeSpecification(speedindexObj.optString("description"), speedindexObj.optString("load_speed_index")))
                                }

                                for (tyretype in 0 until tyre_type.length()) {
                                    val vehicaltypeObject: JSONObject = tyre_type.get(tyretype) as JSONObject
                                    val tyretypeObj = Gson().fromJson(vehicaltypeObject.toString(), Models.TypeSpecificationForVehicalType::class.java)
                                    if (tyretypeObj != null) {
                                        tyreTypeList.add(Models.TypeSpecificationForVehicalType(tyretypeObj.name, tyretypeObj.id, tyretypeObj.vhicle_type_arr!!))
                                    }


                                }

                                tyreSeasonList.add(0, Models.TypeSpecificationForSeason(getString(R.string.all), "0", ""))
                                for (season in 0 until season_tyre_type.length()) {
                                    val seasonObj: JSONObject = season_tyre_type.get(season) as JSONObject
                                    tyreSeasonList.add(season + 1, Models.TypeSpecificationForSeason(seasonObj.optString("name"), seasonObj.optString("id"), seasonObj.optString("code2")))
                                }

                                spinner_width.adapter = SpinnerAdapter(applicationContext, widthList.distinct())
                                spinner_diameter.adapter = SpinnerAdapter(applicationContext, diameterList.distinct())
                                spinner_aspect_ratio.adapter = SpinnerAdapter(applicationContext, aspectRatioList.distinct())
                                spinner_vehicle_type.adapter = SpinnerAdapterForVehicalType(applicationContext, tyreTypeList.distinct())
                                spinner_season_type.adapter = SpinnerAdapterForSeason(applicationContext, tyreSeasonList.distinct())
                                spinner_speed_limit.adapter = SpinnerAdapter(applicationContext, speedIndexList.distinct())
                                spinner_speed_load_index.adapter = SpinnerAdapter(applicationContext, speedLoadIndexList.distinct())
                                if (isPreviousSelectedMeasurement) {
                                    if (selectedTyreDetail != null) {

                                        if (!selectedTyreDetail.width.equals("0.0")) {
                                            val tyreWidth = widthList.find { it.name == selectedTyreDetail.width.toInt().toString() }
                                            val selectedWidthPotion = widthList.indexOf(tyreWidth)
                                            spinner_width.setSelection(selectedWidthPotion)
                                        }
                                        if (!selectedTyreDetail.diameter.equals("0.0")) {
                                            val tyreDiameter = diameterList.find { it.name == selectedTyreDetail.diameter.toInt().toString() }
                                            val selectedWidthPotion = diameterList.indexOf(tyreDiameter)
                                            spinner_diameter.setSelection(selectedWidthPotion)
                                        }
                                        if (selectedTyreDetail.aspectRatio != "") {
                                            val tyre_aspectRatio = aspectRatioList.find { it.name == selectedTyreDetail.aspectRatio.toInt().toString() }
                                            val selected_aspectRatio = aspectRatioList.indexOf(tyre_aspectRatio)
                                            spinner_aspect_ratio.setSelection(selected_aspectRatio)
                                        }
                                        if (selectedTyreDetail.vehicleType != "") {
                                            val tyreVehicalType = tyreTypeList.find { it.id == selectedTyreDetail.vehicleType.toString() }
                                            val selectedVehicalTypePotion = tyreTypeList.indexOf(tyreVehicalType)
                                            spinner_vehicle_type.setSelection(selectedVehicalTypePotion)
                                        }

                                        /* if (!selectedTyreDetail.seasonName.isNullOrBlank() && selectedTyreDetail.seasonName != getString(R.string.All) && selectedTyreDetail.seasonName != getString(R.string.all_in_italin)) {

                                             val tyre_Season = tyreSeasonList.find { it.id == selectedTyreDetail.seasonId }
                                             if (tyre_Season != null) {
                                                 val selected_Season = tyreSeasonList.indexOf(tyre_Season)
                                                 spinner_season_type.setSelection(selected_Season)
                                             } else {
                                                 val tyre_seasonname = tyreSeasonList.find { it.name == selectedTyreDetail.seasonName.toString() }
                                                 if (tyre_seasonname != null) {
                                                     val selected_Season = tyreSeasonList.indexOf(tyre_seasonname)
                                                     spinner_season_type.setSelection(selected_Season)
                                                 }
                                             }

                                         }
                                         if (!selectedTyreDetail.speedIndexName.isNullOrBlank() && selectedTyreDetail.speedIndexName != getString(R.string.All) && selectedTyreDetail.speedIndexName != getString(R.string.all_in_italin)) {
                                             val tyreSpeedIndex = speedIndexList.find { it.name == selectedTyreDetail.speedIndexName.toString() }

                                             if (tyreSpeedIndex != null) {
                                                 val selectedSpeedIndex = speedIndexList.indexOf(tyreSpeedIndex)
                                                 spinner_speed_limit.setSelection(selectedSpeedIndex)
                                             } else {
                                                 val tyreSpeedIndexName = speedIndexList.find { it.code == selectedTyreDetail.speedIndexId.toString() }

                                                 if (tyreSpeedIndexName != null) {
                                                     val selectedSpeedIndex = speedIndexList.indexOf(tyreSpeedIndexName)
                                                     spinner_speed_limit.setSelection(selectedSpeedIndex)
                                                 }
                                             }

                                         }
                                         if (!selectedTyreDetail.speed_load_index.isNullOrBlank() && selectedTyreDetail.speed_load_index != getString(R.string.All) && selectedTyreDetail.speed_load_index != getString(R.string.all_in_italin)) {
                                             val tyreSpeedloadIndex = speedLoadIndexList.find { it.name == selectedTyreDetail.speed_load_index.toString() }
                                             if (tyreSpeedloadIndex != null) {
                                                 val selectedSpeedloadIndex = speedLoadIndexList.indexOf(tyreSpeedloadIndex)
                                                 spinner_speed_load_index.setSelection(selectedSpeedloadIndex)
                                             }

                                         }
 */


                                        if (!selectedTyreDetail.cust_seasonId.isNullOrBlank()) {

                                            val tyre_Season = tyreSeasonList.find { it.id == selectedTyreDetail.cust_seasonId }
                                            if (tyre_Season != null) {
                                                val selected_Season = tyreSeasonList.indexOf(tyre_Season)
                                                spinner_season_type.setSelection(selected_Season)
                                            }

                                        }
                                        if (!selectedTyreDetail.cust_speedIndexId.isNullOrBlank()) {
                                            val tyreSpeedIndex = speedIndexList.find { it.code == selectedTyreDetail.cust_speedIndexId.toString() }
                                            if (tyreSpeedIndex != null) {
                                                val selectedSpeedIndex = speedIndexList.indexOf(tyreSpeedIndex)
                                                spinner_speed_limit.setSelection(selectedSpeedIndex)
                                            }

                                        }
                                        if (!selectedTyreDetail.cust_speedLoad_indexName.isNullOrBlank()) {
                                            val tyreSpeedloadIndex = speedLoadIndexList.find { it.code == selectedTyreDetail.cust_speedLoad_indexName.toString() }
                                            if (tyreSpeedloadIndex != null) {
                                                val selectedSpeedloadIndex = speedLoadIndexList.indexOf(tyreSpeedloadIndex)
                                                spinner_speed_load_index.setSelection(selectedSpeedloadIndex)
                                            }

                                        }



                                        if (selectedTyreDetail.cust_runflat) {
                                            checkbox_run_flat.isChecked = true
                                        }
                                        if (selectedTyreDetail.cust_reinforced) {
                                            checkbox_reinforced.isChecked = true
                                        }


                                    }
                                }
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
                                spinner_speed_load_index.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        val items: Models.TypeSpecification = p0?.getItemAtPosition(p2) as Models.TypeSpecification
                                        speedLoadIndexDesc = items.name
                                        speedLoadIndexName = items.code
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

    private fun bindSpinner(spinner: AwesomeSpinner, titles: List<String>) {


        spinner.clearSelection()

        if (spinner.id == R.id.spinner_version) {
            spinner.setSpinnerHint(if (titles.isEmpty()) "No Car for this type" else getString(R.string.sel_version))
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titles)

        spinner.setOnSpinnerItemClickListener { _, _ -> }

        spinner.setAdapter(adapter)


    }
}
