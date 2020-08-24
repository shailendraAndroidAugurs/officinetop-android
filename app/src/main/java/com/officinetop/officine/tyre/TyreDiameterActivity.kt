package  com.officinetop.officine.tyre

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.SimpleTextListAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.genericAPICall
import kotlinx.android.synthetic.main.activity_tyre_customization.*
import kotlinx.android.synthetic.main.activity_tyre_diameter.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor

class TyreDiameterActivity : BaseActivity() {
     var selectedTyreDetail: String=""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.officinetop.officine.R.layout.activity_tyre_diameter)

        setSupportActionBar(toolbar)

        toolbar_title.text = resources.getString(R.string.select_diameter)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getSelectedCar()?.let {
            title_tyre.text = "${resources.getString(R.string.select_car_measures)} \n${it.carMakeName} - ${it.carModelName} (${it.carModel.modelYear})"
        }
        if(intent.extras!=null && intent.hasExtra("currentlySelectedMeasurement") ){
             selectedTyreDetail    =intent.getStringExtra("currentlySelectedMeasurement")

        }


        customize_measure_btn.setOnClickListener {
            if(!selectedTyreDetail.isNullOrBlank()){
                startActivityForResult(intentFor<TyreCustomizationActivity>().putExtra("currentlySelectedMeasurement",selectedTyreDetail), 200)
            }else{
                startActivityForResult(intentFor<TyreCustomizationActivity>(), 200)
            }


        }

        getUserTyreDetailsApi()
    }

    private fun getUserTyreDetailsApi() {
        try {
            RetrofitClient.client.getUserTyreDetails(
                    getUserId(),
                    getSelectedCar()?.carVersionModel?.idVehicle ?: "")
                    .genericAPICall { _, response ->
                        response?.let {
                            val body = response.body() as UserTyreMeasurementResponse

                            if (response.isSuccessful) {
                                if (!isStatusCodeValid(body.toString())) {
                                    //bind tyre list recycler
                                    bindTyreRecycler(body)
                                }
                                if (body.statusCode == 0) {
                                    alert {
                                        message = getString(R.string.costimizemessage)
                                        positiveButton(getString(R.string.yes)) {
                                            startActivity(intentFor<TyreCustomizationActivity>())
                                            finish()
                                        }
                                        negativeButton(getString(R.string.no)) {
                                            //finish()
                                        }
                                    }.show()


                                    /*customize_measure_btn.isEnabled = false*/
                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        /*RetrofitClient.client.getUserTyreDetails(
                getUserId()!!,
                getSelectedCar()?.carVersionModel?.idVehicle ?: "")
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val body : String = response.body().toString()
                        if (response.isSuccessful){
                            if (!isStatusCodeValid(body)){
                                 //bind tyre list recycler
                                bindTyreRecycler(body)
                            }
                        }
                    }
                }) */
    }

    private fun bindTyreRecycler(body: UserTyreMeasurementResponse) {
        val tyreStringList: MutableList<MeasurementDataSetItem> = ArrayList()
//        val tyres:MutableList<Models.TyreDetails> = ArrayList()
//        val gson = Gson()
//        val jsonString = gson.toJson(body)
//        val listType = object : TypeToken<List<Models.TyreDetails>>() { }.type
//        val tyreDetails  = gson.fromJson<List<Models.TyreDetails>>(jsonString, listType)

        val tyreMeasurementDetailList: MutableList<MeasurementDataSetItem?>? = body.mDataSet
        tyreMeasurementDetailList?.let {
            tyreMeasurementDetailList.reverse()
            for (measurePosition in 0 until tyreMeasurementDetailList.size) {
                //                val tyre:
//                val tyre = Gson().fromJson<Models.TyreDetails>(body, Models.TyreDetails::class.java)
                tyreMeasurementDetailList[measurePosition]?.let {
                    /*tyreStringList.add("${it.vehicleName} with Diameter: ${it.rimDiameter} , Width: ${it.width}, Speed Index : ${it.speedindex}" +
                            ", Aspect Ratio : ${it.aspectRatio}")*/
                    tyreStringList.add(it)
//                tyres.add(tyre)

                }
            }//${items.max_width}${items.max_aspect_ratio} R${items.max_diameter}"/

            recycler_view.adapter = SimpleTextListAdapter(this, tyreStringList, object : SimpleTextListAdapter.OnRecyclerItemClickListener {

                override fun onItemClick(view: View, title: MeasurementDataSetItem, position: Int) {

                    Log.e("TYRE DETAILS==", "****************** 2 " + tyreMeasurementDetailList[position])
                    tyreMeasurementDetailList[position]?.let {

                        var tyre_type: String = ""
                        var tyre_type_name: String = ""
                        var aspect_ratio: String = ""
                        var speed_index: String = ""
                        var season: String = ""
                        var season_type_name: String = ""
                        var width: String = ""
                        var diameter: String = ""
                        var speed_indexId: String = ""
                        var runFlat = false
                        var reinforced = false
                        tyre_type = if (it.vehicleType == null) " " else if (it.vehicleType.toString().isEmpty() || it.vehicleType.toString().equals("null")) "" else it.vehicleType.toString()
                        aspect_ratio = if (it.aspectRatio == null) "0 " else if (it.aspectRatio.toString().isEmpty() || it.aspectRatio.toString().equals("null")) "0" else it.aspectRatio.toString()
                        speed_index = if (it.speedindex == null) " " else if (it.speedindex.toString().isEmpty() || it.speedindex.toString().equals("null")) "" else it.speedindex.toString()
                        season = if (it.season == null) " " else if (it.season.toString().isEmpty() || it.season.toString().equals("null")) "" else it.season.toString()
                        width = if (it.width == null) "0 " else if (it.width.toString().isEmpty() || it.width.toString().equals("null")) "0" else it.width.toString()
                        diameter = if (it.rimDiameter == null) " 0 " else if (it.rimDiameter.toString().isEmpty() || it.rimDiameter.toString().equals("null")) "0" else it.rimDiameter.toString()
                        runFlat = if (it.runFlat == null) false else if (it.runFlat.toString().isNullOrBlank() || it.runFlat.toString().equals("null") || it.runFlat.toString().equals("0")) false else true
                        reinforced = if (it.reinforced == null) false else if (it.reinforced.toString().isNullOrBlank() || it.reinforced.toString().equals("null") || it.reinforced.toString().equals("0")) false else true
                    val    speed_load_index = if (it.speed_load_index == null) " " else if (it.speed_load_index.toString().isEmpty() || it.speed_load_index.toString().equals("null")) "" else it.speed_load_index.toString()


                        val tyre =
                                Models.TyreDetail(
                                        vehicleType = tyre_type,
                                        aspectRatio = aspect_ratio,
                                        width = width.toFloat(),
                                        diameter = diameter.toFloat(),
                                        rimWidth = width.toFloat(),
                                        vehicleTypeName = if (it.vehicleTypeStatus == null) "" else it.vehicleTypeStatus,
                                        seasonTypeName = if (it.seasonStatus == null) "" else it.seasonStatus,
                                        filter_SeasonTypeId = season,
                                        filter_SpeedIndexId = speed_index,
                                        speedIndex = if (it.speedindexStatus == null) "" else it.speedindexStatus,
                                        runFlat = runFlat,
                                        reinforced = reinforced,
                                        SeasonId = season,
                                        SpeedindexId = speed_index,
                                        selected_runFlat=runFlat,
                                        selected_reinforced=reinforced,
                                        speed_load_index = speed_load_index,
                                        Selected_speed_load_index=speed_load_index

                                )

                        setTyreDetail(tyre)
                    }
                    startActivity(intentFor<TyreListActivity>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
//                        toast(convertToJson(tyreMeasurementDetailList[position]))
                }


                override fun onItemRemove() {
                    tyreStringList.clear()

                    getUserTyreDetailsApi()
                }
            })

//        for (item in 0 until tyreDetails.size) {
//            val tyre = Gson().fromJson<Models.TyreDetails>(body, Models.TyreDetails::class.java)
//            tyreStringList.add("Diameter: ${tyreDetails.get(item).width} , Width: ${tyreDetails.get(item).width}, Rim Width : ${tyreDetails.get(item).vehicle_type}")
//            tyres.add(tyre)
//        }
            /*getStoredTyres().forEach {
                val tyre = Gson().fromJson(it.value.toString(), Models.TyreDetail::class.java )
                tyreStringList.add("Diameter: ${tyre.diameter} , Width: ${tyre.width}, Rim Width : ${tyre.rimWidth}")
                tyres.add(tyre)
            }*/

        }
    }


    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                getTyreDetail()?.let {
                    startActivity(intentFor<TyreListActivity>())
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
