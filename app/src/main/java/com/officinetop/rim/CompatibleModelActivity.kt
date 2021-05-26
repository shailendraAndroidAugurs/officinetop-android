package com.officinetop.rim

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.isapanah.awesomespinner.AwesomeSpinner
import com.officinetop.R
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.getProgressDialog
import kotlinx.android.synthetic.main.activity_add_vehicle.*
import kotlinx.android.synthetic.main.activity_compatible_model.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.design.snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompatibleModelActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    val rimcarBrand: MutableList<Models.RimBrandlist> = ArrayList()
    val rimcarmodel: MutableList<Models.RimCaModels> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compatible_model)
        setSupportActionBar(toolbar)
        toolbar_title.text = resources.getString(R.string.compatibility_with_model)
        progressDialog = getProgressDialog()
        loadCarManufacturer()
    }

    private fun loadCarManufacturer() {

        try {
            progressDialog.show()
        } catch (e: Exception) {
        }

        RetrofitClient.client.rimCarBrands().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("rimedata", "onFailure: onFailure", t)
                add_from_plate.snackbar(getString(R.string.ConnectionErrorPleaseretry))
                progressDialog.dismiss()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()

                progressDialog.dismiss()
                if (response.code() == 200) {
                    val dataset = getDataSetArrayFromResponse(body)
                    rimcarBrand.clear()
                    Log.d("rimedata", "onResponse: car maker ="+body)
                    Log.d("rimedata", "onResponse: car maker ="+dataset)

                    val brandTitle: MutableList<String> = ArrayList()

                    var selectedIndex = -1

                    for (i in 0 until dataset.length()) {
                        val data = Gson().fromJson<Models.RimBrandlist>(dataset.getJSONObject(i).toString(), Models.RimBrandlist::class.java)
                        rimcarBrand.add(data)
                        brandTitle.add(data.rimbrand)


/*
                        if (isForEdit || isForPlateno) {

                            if (data.brandID == myCar?.carMakeModel?.brandID)
                                selectedIndex = i
                        }
*/

                    }



                    bindSpinner(spinner_brand_name, brandTitle)
                    if (selectedIndex > -1) {
                        spinner_brand_name.setSelection(selectedIndex)
                    }

                    spinner_brand_name.setOnSpinnerItemClickListener { position, itemAtPosition ->

                        //clearSpinners()
                       /* spinner_model.setAdapter(getEmptyAdapter())*/
                        loadCarModel(rimcarBrand[position].tyrebrandid)
                        Toast.makeText(applicationContext,""+rimcarBrand[position].tyrebrandid,Toast.LENGTH_SHORT).show()

                    }


                }


                }


        })
    }


    private fun loadCarModel(brandID: String) {
        progressDialog.show()

        RetrofitClient.client.rimcarModels("23").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                Log.d("AddVehicleActivity", "onResponse: models = "+body)

                    progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataArrayFromResponse(body)


                    val modelTitle: MutableList<String> = ArrayList()
                    rimcarmodel.clear()

                    var selectedIndex = -1

                    for (i in 0 until dataset.length()) {
                        val data = Gson().fromJson<Models.RimCaModels>(dataset.getJSONObject(i).toString(), Models.RimCaModels::class.java)
                        rimcarmodel.add(data)
                        modelTitle.add(data.tyre24ModelId)

                       /* if (isForEdit) {
                            if (data.modelID == myCar?.carModel?.modelID && data.modelYear == myCar?.carModel?.modelYear) {
                                selectedIndex = i
                            }
                        }*/
                    }

                    bindSpinner(spinner_model_rim, modelTitle)

                   /* if (selectedIndex > -1)
                        spinner_model.setSelection(selectedIndex)*/

                    spinner_model_rim.setOnSpinnerItemClickListener { position, _ ->
                        /*spinner_fuel.setAdapter(getEmptyAdapter())
                        spinner_version.setAdapter(getEmptyAdapter())

                        loadCarVersion(model[position].modelID, model[position].modelYear)*/
                    }


                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("AddVehicleActivity", "onResponse: models = "+t.message)

            }

        })
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


    private fun clearSpinners() {
        spinner_brand_name.clearSelection()
    }
}