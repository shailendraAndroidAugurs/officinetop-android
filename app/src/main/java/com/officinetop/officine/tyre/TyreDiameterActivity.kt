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
import com.officinetop.officine.utils.convertToJsonString
import com.officinetop.officine.utils.genericAPICall
import com.officinetop.officine.utils.isOnline
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_tyre_diameter.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.intentFor

class TyreDiameterActivity : BaseActivity() {
    private var selectedTyreDetail: String = ""
    private var TyreSelectedId: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyre_diameter)



        getSelectedCar()?.let {
            title_tyre.text = "${resources.getString(R.string.select_car_measures)} \n${it.carMakeName} - ${it.carModelName} (${it.carModel.modelYear})"
        }
        if (intent.extras != null && intent.hasExtra("currentlySelectedMeasurement")) {
            selectedTyreDetail = intent.getStringExtra("currentlySelectedMeasurement")

        }
        if (intent.extras != null && intent.hasExtra("TyreSelectedId")) {
            TyreSelectedId = intent.getStringExtra("TyreSelectedId")

        }


        customize_measure_btn.setOnClickListener {
            if (!selectedTyreDetail.isNullOrBlank()) {
                startActivity(intentFor<TyreCustomizationActivity>().putExtra("currentlySelectedMeasurement", selectedTyreDetail))
                finish()
            } else {
                startActivity(intentFor<TyreCustomizationActivity>())
                finish()
            }


        }
        if (isOnline()) {
            getUserTyreDetailsApi()
        }else{
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }

    }

    private fun getUserTyreDetailsApi() {
        progressbar.visibility = View.VISIBLE
        try {
            RetrofitClient.client.getUserTyreDetails(
                    getUserId(),
                    getSelectedCar()?.carVersionModel?.idVehicle ?: "")
                    .genericAPICall { _, response ->
                        response?.let {
                            progressbar.visibility = View.GONE
                            val body = response.body() as UserTyreMeasurementResponse

                            if (response.isSuccessful) {
                                if (!isStatusCodeValid(body.toString())) {
                                    //bind tyre list recycler

                                    bindTyreRecycler(body)
                                }
                                if (body.statusCode == 0) {
                                    if (isLoggedIn()) {
                                        clearTyreDetail()
                                        startActivity(intentFor<TyreCustomizationActivity>())
                                        finish()
                                    } else {
                                        if (!selectedTyreDetail.isNullOrBlank()) {

                                            startActivity(intentFor<TyreCustomizationActivity>().putExtra("currentlySelectedMeasurement", selectedTyreDetail))
                                            finish()
                                        } else {
                                            startActivity(intentFor<TyreCustomizationActivity>())
                                            finish()
                                        }
                                    }


                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindTyreRecycler(body: UserTyreMeasurementResponse) {
        val tyreStringList: MutableList<MeasurementDataSetItem> = ArrayList()
        val tyreMeasurementDetailList: MutableList<MeasurementDataSetItem?>? = body.mDataSet
        tyreMeasurementDetailList?.let {
            tyreMeasurementDetailList.reverse()
            for (measurePosition in 0 until tyreMeasurementDetailList.size) {
                tyreMeasurementDetailList[measurePosition]?.let {
                    tyreStringList.add(it)


                }
            }

            if (tyreStringList.size > 0) {
                setSupportActionBar(toolbar)

                toolbar_title.text = resources.getString(R.string.select_diameter)

                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                parentViewOfSelectedDiameter.visibility = View.VISIBLE
                title_tyre.visibility = View.VISIBLE

            }

            recycler_view.adapter = SimpleTextListAdapter(this, tyreStringList, object : SimpleTextListAdapter.OnRecyclerItemClickListener {

                override fun onItemClick(view: View, title: MeasurementDataSetItem, position: Int, IsEdit: Boolean) {

                    Log.e("TYRE DETAILS==", "****************** 2 " + tyreMeasurementDetailList[position])
                    tyreMeasurementDetailList[position]?.let {

                        var tyre_type: String = ""
                        var aspect_ratio: String = ""
                        var speed_index: String = ""
                        var season: String = ""
                        var width: String = ""
                        var diameter: String = ""
                        var runFlat = false
                        var reinforced = false
                        tyre_type = if (it.vehicleType == null) " " else if (it.vehicleType.toString().isEmpty() || it.vehicleType.toString() == "null") "" else it.vehicleType.toString()
                        aspect_ratio = if (it.aspectRatio == null) "0 " else if (it.aspectRatio.toString().isEmpty() || it.aspectRatio.toString() == "null") "0" else it.aspectRatio.toString()
                        speed_index = if (it.speedindex == null) " " else if (it.speedindex.toString().isEmpty() || it.speedindex.toString() == "null") "" else it.speedindex.toString()
                        season = if (it.season == null) " " else if (it.season.toString().isEmpty() || it.season.toString() == "null") "" else it.season.toString()
                        width = if (it.width == null) "0 " else if (it.width.toString().isEmpty() || it.width.toString() == "null") "0" else it.width.toString()
                        diameter = if (it.rimDiameter == null) " 0 " else if (it.rimDiameter.toString().isEmpty() || it.rimDiameter.toString() == "null") "0" else it.rimDiameter.toString()
                        runFlat = if (it.runFlat == null) false else !(it.runFlat.toString().isNullOrBlank() || it.runFlat.toString() == "null" || it.runFlat.toString() == "0")
                        reinforced = if (it.reinforced == null) false else !(it.reinforced.toString().isNullOrBlank() || it.reinforced.toString() == "null" || it.reinforced.toString() == "0")
                        val speed_load_index = if (it.speed_load_index == null) " " else if (it.speed_load_index.toString().isEmpty() || it.speed_load_index.toString() == "null") "" else it.speed_load_index.toString()


                        val tyre =
                                Models.TyreDetail(
                                        id = it.id.toString(),
                                        vehicleType = tyre_type,
                                        aspectRatio = aspect_ratio,
                                        width = width.toFloat(),
                                        diameter = diameter.toFloat(),
                                        rimWidth = width.toFloat(),
                                        vehicleTypeName = if (it.vehicleTypeStatus == null) "" else it.vehicleTypeStatus,
                                        seasonName = if (it.seasonStatus == null) "" else it.seasonStatus,
                                        seasonId = season,
                                        speedIndexId = speed_index,
                                        speedIndexName = if (it.speedindexStatus == null) "" else it.speedindexStatus,
                                        runFlat = runFlat,
                                        reinforced = reinforced,
                                        speed_load_index = speed_load_index,
                                        cust_speedIndexId = speed_index,
                                        cust_seasonId = season,
                                        cust_speedLoad_indexId = speed_load_index,
                                        cust_runflat = runFlat,
                                        cust_reinforced = reinforced

                                )
                        if (!IsEdit) {
                            setTyreDetail(tyre)
                        } else {
                            if (TyreSelectedId.equals(tyre.id)) {
                                startActivityForResult(intentFor<TyreCustomizationActivity>().putExtra("currentlySelectedMeasurement", tyre.convertToJsonString()).putExtra("editId", it.id.toString()).putExtra("IsSavedInlocal", tyre.id), 130)

                            } else {
                                startActivityForResult(intentFor<TyreCustomizationActivity>().putExtra("currentlySelectedMeasurement", tyre.convertToJsonString()).putExtra("editId", it.id.toString()), 130)

                            }

                        }

                    }


                    if (!IsEdit) {

                        startActivity(intentFor<TyreListActivity>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        finish()
                    }
                }


                override fun onItemRemove() {
                    tyreStringList.clear()

                    getUserTyreDetailsApi()
                }
            })


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 130) {
            getUserTyreDetailsApi()
        }


        Log.d("TyreListActivity", "OnActivityResult")
    }
}
