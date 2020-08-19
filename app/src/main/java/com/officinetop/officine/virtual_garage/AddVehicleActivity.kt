package com.officinetop.officine.virtual_garage

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.isapanah.awesomespinner.AwesomeSpinner
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.authentication.LoginActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_vehicle.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_add_car_image.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.design.snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class AddVehicleActivity : BaseActivity() {

    lateinit var progressDialog: ProgressDialog
    var isLoaded = false

    val manufacturers: MutableList<Models.Manufacturer> = ArrayList()
    val model: MutableList<Models.CarModels> = ArrayList()
    var carVersions: MutableList<Models.CarVersion> = ArrayList()
    var finalCarVersion: MutableList<Models.CarVersion> = ArrayList()

    var myCar: Models.MyCarDataSet? = null

    var isForEdit = false
    var WRITE_EXTERNAL_STORAGE_RC = 10001
    private var carImageList = ArrayList<Models.CarImages>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_vehicle)

        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.add_car)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (shouldShowAddCarInfoDialog())
            shouldShowAddCarInfoDialog(false)

        spinner_model.setDownArrowTintColor(Color.LTGRAY)
        spinner_manufacturer.setDownArrowTintColor(Color.LTGRAY)
        spinner_version.setDownArrowTintColor(Color.LTGRAY)
        spinner_fuel.setDownArrowTintColor(Color.LTGRAY)

        spinner_fuel.setSpinnerHint("Select Fuel Type")

        myCar = intent.getSerializableExtra(Constant.Key.myCar) as Models.MyCarDataSet?

        isForEdit = myCar != null

        Log.d("AddVehicleActivity", "onCreate: is For Edit $isForEdit")

        if (isForEdit) {
            setEditMode()
        }

        if (!isForEdit)
            container_editable.visibility = View.GONE

        progressDialog = getProgressDialog()
        //bind car carManufacturer


        bindOnCreateValues()

        add_from_fields.setOnClickListener {

            if (!isOnline()) {
                showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
                return@setOnClickListener
            }


            if (isLoaded) {

                //Do you want to complete the car details to get personalized suggestions?

                if (isForEdit && isLoggedIn())
                    editCarFromFields()
                else
                    addCarFromFields()

//                showConfirmDialog("Do you want to ${if(isForEdit) "update" else "add"} this car${ if(isForEdit) "" else " to your Virtual Garage"}?") {}
            }
        }

        add_from_plate.setOnClickListener {

            hideKeyboard()
            if (!isOnline()) {
                showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
                return@setOnClickListener
            }

            if (plate_editText.text.length != 7) {
                showInfoDialog(getString(R.string.PleaseEnterValidLicensePlateNumber), true) {}
                return@setOnClickListener
            }

            if (!isAlphaNumeric(plate_editText.text.toString())) {
                showInfoDialog(getString(R.string.PleaseEnterValidLicensePlateNumber), true) {}
                return@setOnClickListener
            }

            if (isLoggedIn())
                addFromPlate()
            else
                searchFromPlate()
//            showConfirmDialog("Do you want to add this car to your Virtual Garage?"){}


        }


        //todo temporary
        title_text.setOnClickListener {
        }
    }


    private fun bindOnCreateValues() {

        getMerlinConnectionCallback().registerConnectable {
            if (isForEdit)
                bindEditables()


            loadCarManufacturer()
        }
    }

    private fun setEditMode() {
        Log.d("AddVehicleActivity", "onCreate:editable car model : $myCar")
        plate_layout.visibility = View.GONE
        add_from_fields.text = getString(R.string.modify_car)
        toolbar_title.text = getString(R.string.modify_car)

        setDatePickers(text_revision_date)
        container_editable.visibility = View.VISIBLE

        if (isLoggedIn()) {
            car_images_list.visibility = View.VISIBLE

            carImageList = ArrayList<Models.CarImages>()
            carImageList = myCar!!.carImages

            initCarImagesList()
        }
    }

    private fun clearSpinners() {
        spinner_model.clearSelection()
        spinner_version.clearSelection()
        spinner_fuel.clearSelection()
    }

    var carListImageAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

        var TYPE_ITEM = 0
        var TYPE_FOOTER = 1

        override fun getItemCount(): Int {

            return if (carImageList.size < 1)
                carImageList.size + 1
            else
                carImageList.size
        }

        override fun onBindViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {

            if (viewHolder is FooterHolderView) {
                var itemHolder: FooterHolderView = viewHolder
                itemHolder.itemView.image_picker.setOnClickListener {

                    val permission = ContextCompat.checkSelfPermission(this@AddVehicleActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this@AddVehicleActivity,
                                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_RC)
                    } else
                        pickImage()
                }
            } else if (viewHolder is CarImageHolderView) {

                try {
                    var itemHolder: CarImageHolderView = viewHolder

                    var carImage: ImageView = itemHolder.itemView.findViewById(R.id.car_image)
                    var deleteCarImage: ImageView = itemHolder.itemView.findViewById(R.id.delete_car_image)

                    if (position < carImageList.size) {
                        var carImageModel = myCar!!.carImages[position]
                        var carImageURL = Constant.imageBaseURL + carImageModel.imageName
                        Glide.with(this@AddVehicleActivity)
                                .setDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_car).error(R.drawable.ic_car))
                                .load(carImageURL)
                                .thumbnail(0.7f)
                                .into(carImage)

                        deleteCarImage.setOnClickListener {
                            hideKeyboard()
                            showConfirmDialog(getString(R.string.Deletedefaultimageforthiscar)) {
                                deleteCarImageFromServer(position)
                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
            var view: View
            if (viewType == TYPE_FOOTER) {
                view = LayoutInflater.from(this@AddVehicleActivity).inflate(R.layout.item_add_car_image, parent, false)
                return FooterHolderView(view)
            } else {
                view = LayoutInflater.from(this@AddVehicleActivity).inflate(R.layout.item_car_image, parent, false)
                return CarImageHolderView(view)
            }
        }

        override fun getItemViewType(position: Int): Int {

            if (carImageList.size < 5 && position == carImageList.size)
                return TYPE_FOOTER
            else
                return TYPE_ITEM
        }
    }


    private fun bindEditables() {
        myCar?.let {
            edit_text_km_car.setText(it.km_of_cars)

            edit_text_km_annual.setText(it.km_traveled_annually)
            text_revision_date.text = it.carRevisionDate
            edit_text_km_on_revision_date.setText(it.carRevisionDateOnKm)
            alloy.isChecked = it.alloy_wheels == "1"
        }
    }

    private fun initCarImagesList() {
        car_images_list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

        car_images_list.adapter = carListImageAdapter
    }

    class CarImageHolderView(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class FooterHolderView(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)


    private fun addCarFromFields() {

        //Save car details locally
        hideKeyboard()

        if (!isOnline()) {
            showOnlineSnack(null)
            return
        }


        if (spinner_version.selectedItemPosition < 0 || spinner_manufacturer.selectedItemPosition < 0
                || spinner_fuel.selectedItemPosition < 0 || spinner_model.selectedItemPosition < 0) {
            snackbar(add_from_fields, getString(R.string.AllFieldsRequired))
            return
        }else {
            val idVehicle = finalCarVersion[spinner_version.selectedItemPosition].idVehicle
            RetrofitClient.client.kromedaCall(idVehicle)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        }

                    })

        }

        if (!isLoggedIn()) {


            val carDetails = JsonObject()

            val manufacturerIndex = spinner_manufacturer.selectedItemPosition
            val versionIndex = spinner_version.selectedItemPosition
            val modelIndex = spinner_model.selectedItemPosition

            carDetails.addProperty("id", "")
            carDetails.addProperty("user_id", "")

            carDetails.addProperty("carMakeName", manufacturers[manufacturerIndex].brandID)
            carDetails.addProperty("carModelName", model[modelIndex].modelID + "/" + model[modelIndex].modelYear)
            carDetails.addProperty("carVersion", finalCarVersion[versionIndex].idVehicle)
            carDetails.addProperty("addedTo", "")
            carDetails.addProperty("created_at", "")
            carDetails.addProperty("updated_at", "")


            carDetails.addProperty("km_of_cars", edit_text_km_car.text.toString())
            carDetails.addProperty("km_traveled_annually", edit_text_km_annual.text.toString())
            carDetails.addProperty("revision_date_km", text_revision_date.text.toString())
            carDetails.addProperty("revesion_km", edit_text_km_on_revision_date.text.toString())
            carDetails.addProperty("alloy_wheels", if (alloy.isChecked) "1" else "0")


            // Add car make details
            val carMakeDetails: JsonObject = JsonObject()
            carMakeDetails.addProperty("idMarca", manufacturers[manufacturerIndex].brandID)
            carMakeDetails.addProperty("Marca", manufacturers[manufacturerIndex].brand)
            carMakeDetails.addProperty("CodiceListino", manufacturers[manufacturerIndex].codeList)


            carDetails.add("carMake", carMakeDetails)

            // Add car make details
            val carModelDetails: JsonObject = JsonObject()
            carModelDetails.addProperty("idModello", model[modelIndex].modelID)
            carModelDetails.addProperty("Modello", model[modelIndex].model)
            carModelDetails.addProperty("ModelloAnno", model[modelIndex].modelYear)

            carDetails.add("carModel", carModelDetails)


            // Add car version details
            val carVersionDetails: JsonObject = JsonObject()
            carVersionDetails.addProperty("Versione", finalCarVersion[versionIndex].version)
            carVersionDetails.addProperty("Dal", finalCarVersion[versionIndex].dal)
            carVersionDetails.addProperty("Cv", finalCarVersion[versionIndex].cv)
            carVersionDetails.addProperty("Kw", finalCarVersion[versionIndex].kw)
            carVersionDetails.addProperty("idVeicolo", finalCarVersion[versionIndex].idVehicle)
            carVersionDetails.addProperty("fueltype", spinner_fuel.selectedItem.toString())


            //modified to add car size as per
            carDetails.addProperty("car_size", finalCarVersion[spinner_version.selectedItemPosition].carSize.toString())


            carDetails.add("carVers", carVersionDetails)

            val json = carDetails.toString()

            saveLocalCarInJSON(json)
            val car = Gson().fromJson<Models.MyCarDataSet>(carDetails, Models.MyCarDataSet::class.java)
            val carIntent = Intent()
            carIntent.putExtra(Constant.Key.myCar, car as Serializable)

            if (isForEdit) {
                showInfoDialog(getString(R.string.CarDetailSavedSuccessfully), false) {
                    setResult(Activity.RESULT_OK, carIntent)
                    finish()
                }
            } else {
                alert {
                    message = getString(R.string.Doyouwanttocompletethecardetailspersonalizedsuggestions)
                    positiveButton(getString(R.string.yes)) {

                        myCar = car
                        isForEdit = true
                        setEditMode()
                    }

                    negativeButton(getString(R.string.Completelater)) {
                        setResult(Activity.RESULT_OK, carIntent)
                        finish()
                    }
                    isCancelable = false
                }.show()
            }


        } else {
            progressDialog.show()

            val idVehicle = finalCarVersion[spinner_version.selectedItemPosition].idVehicle



            RetrofitClient.client.addCar(manufacturers[spinner_manufacturer.selectedItemPosition].brandID,
                    model[spinner_model.selectedItemPosition].modelID + "/" + model[spinner_model.selectedItemPosition].modelYear,
                    idVehicle,
                    finalCarVersion[spinner_version.selectedItemPosition].body,
                    getBearerToken() ?: "")
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progressDialog.dismiss()
                            snackbar(add_from_fields, getString(R.string.ConnectionErrorPleaseretry))
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            progressDialog.dismiss()
                            val body = response.body()?.string() ?: ""
                            if (response.code() == 200) {
                                val messageString = JSONObject(body).getString("message")

                                if (!isForEdit) {

                                    handleAddCarResponse(body)

                                } else {
                                    showInfoDialog(if (messageString.isEmpty()) getString(R.string.Caraddedsuccessfully) else messageString, !isStatusCodeValid(body)) {
                                        if (isStatusCodeValid(body)) {
                                            Log.d("AddVehicleActivity", "onResponse: Detail updated")
                                            setResult(Activity.RESULT_OK, getLastCarIntent(body!!))
                                            finish()
                                        }
                                    }
                                }
                            } else if (!response.isSuccessful) {
                                showInfoDialog(getString(R.string.Failed_addcar))
                            }
                        }

                    })
        }
    }

    private fun editCarFromFields() {

        if (!isOnline()) {
            showOnlineSnack(null)
            return
        }
        var modelName = ""
        if (spinner_model.selectedItemPosition != null && spinner_model.selectedItemPosition != -1) {
            modelName = model[spinner_model.selectedItemPosition].modelID + "/" + model[spinner_model.selectedItemPosition].modelYear
        }

        var vehicleID = ""
        if (spinner_version.selectedItemPosition != null && spinner_version.selectedItemPosition != -1) {
            vehicleID = finalCarVersion[spinner_version.selectedItemPosition].idVehicle
        }





        if (vehicleID.isNullOrBlank()) {
            showInfoDialog(getString(R.string.PleaseselectCarVersion))
        } else {
            progressDialog.show()
            RetrofitClient.client.editCar(
                    myCar?.id!!,
                    manufacturers[spinner_manufacturer.selectedItemPosition].brandID,
                    modelName,
                    vehicleID,
                    edit_text_km_car.text.toString(),
                    edit_text_km_annual.text.toString(),
                    text_revision_date.text.toString(),
                    edit_text_km_on_revision_date.text.toString(),
                    if (alloy.isChecked) "1" else "0",
                    if (spinner_fuel.selectedItem != null) spinner_fuel.selectedItem else "",
                    if (spinner_version.selectedItemPosition != null && spinner_version.selectedItemPosition != -1) finalCarVersion[spinner_version.selectedItemPosition].body else "",
                    "Bearer ${getStoredToken()}").enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressDialog.dismiss()
                    snackbar(add_from_fields, getString(R.string.ConnectionErrorPleaseretry))
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    if (!isForEdit)
                        progressDialog.dismiss()

                    val body = response.body()?.string()!!
                    Log.d("AddVehicleActivity", "onResponse: edit car = $body")
                    if (isStatusCodeValid(body)) {
                        val messageString = JSONObject(body).getString("message")

                        showInfoDialog(if (messageString.isEmpty()) getString(R.string.Carmodifiedsuccessfully) else messageString, false) {
                            if (isStatusCodeValid(body)) {
                                setResult(Activity.RESULT_OK, getLastCarIntent(body))
                                progressDialog.dismiss()
                                finish()
                                saveMotCarKM(edit_text_km_car.text.toString())

                            }

                        }
                    } else {
                        showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again), true) {}
                        progressDialog.dismiss()
                    }
                }
            })

        }


    }

    private fun loadCarManufacturer() {

        try {
            progressDialog.show()
        } catch (e: Exception) {
        }

        RetrofitClient.client.carManufacturer().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("AddVehicleActivity", "onFailure: onFailure", t)
                add_from_plate.snackbar(getString(R.string.ConnectionErrorPleaseretry))
                progressDialog.dismiss()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                Log.d("AddVehicleActivity", "onResponse: car maker = $body")

                progressDialog.dismiss()

                if (response.code() == 200) {
                    val dataset = getDataSetArrayFromResponse(body)
                    manufacturers.clear()

                    val brandTitle: MutableList<String> = ArrayList()

                    var selectedIndex = -1

                    for (i in 0 until dataset.length()) {
                        val data = Gson().fromJson<Models.Manufacturer>(dataset.getJSONObject(i).toString(), Models.Manufacturer::class.java)
                        manufacturers.add(data)
                        brandTitle.add(data.brand)

                        if (isForEdit) {
                            if (data.brandID == myCar?.carMakeName)
                                selectedIndex = i
                        }

                    }



                    bindSpinner(spinner_manufacturer, brandTitle)
                    if (selectedIndex > -1) {
                        spinner_manufacturer.setSelection(selectedIndex)
                    }

                    spinner_manufacturer.setOnSpinnerItemClickListener { position, itemAtPosition ->

                        clearSpinners()
                        spinner_model.setAdapter(getEmptyAdapter())
                        loadCarModel(manufacturers[position].brandID)

                    }


                }
            }

        })
    }

    private fun loadCarModel(brandID: String) {
        progressDialog.show()

        RetrofitClient.client.carModels(brandID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                Log.d("AddVehicleActivity", "onResponse: models = $body")
                if (!isForEdit)
                    progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataSetArrayFromResponse(body)


                    val modelTitle: MutableList<String> = ArrayList()
                    model.clear()

                    var selectedIndex = -1

                    for (i in 0 until dataset.length()) {
                        val data = Gson().fromJson<Models.CarModels>(dataset.getJSONObject(i).toString(), Models.CarModels::class.java)
                        model.add(data)
                        modelTitle.add(data.model + " [${data.modelYear}]")

                        if (isForEdit) {
                            if (data.modelID == myCar?.carModel?.modelID) {
                                selectedIndex = i
                            }
                        }
                    }

                    bindSpinner(spinner_model, modelTitle)

                    if (selectedIndex > -1)
                        spinner_model.setSelection(selectedIndex)

                    spinner_model.setOnSpinnerItemClickListener { position, _ ->
                        spinner_fuel.setAdapter(getEmptyAdapter())
                        spinner_version.setAdapter(getEmptyAdapter())


                        loadCarVersion(model[position].modelID, model[position].modelYear)
                    }


                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
            }

        })
    }

    private fun loadCarVersion(modelID: String, year: String) {

        progressDialog.show()

        RetrofitClient.client.carVersion(modelID, year).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                snackbar(add_from_fields, getString(R.string.ConnectionErrorPleaseretry))
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("AddVehicleActivity", "onResponse: dataset")
                val body = response.body()?.string()
                val fuelType: MutableList<String> = ArrayList()
                progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    carVersions.clear()
                    fuelType.clear()


                    val dataset = getDataSetArrayFromResponse(body)

                    var selectedIndex = -1
                    for (i in 0 until dataset.length()) {
                        Log.d("AddVehicleActivity", "onResponse: forloop")
                        val carVersion = dataset.getJSONObject(i)
                        val version = Gson().fromJson<Models.CarVersion>(carVersion.toString(), Models.CarVersion::class.java)
                        Log.d("AddVehicleActivity", "onResponse: forloop ${version}")

                        carVersions.add(version)
                        if (!fuelType.contains(version.fueltype) && version.fueltype.isNotEmpty()) {
                            fuelType.add(version.fueltype)
                        }

                    }


                    Log.d("AddVehicleActivity", "onResponse: fuel type array = $fuelType")

                    if (fuelType.isEmpty()) {
                        fuelType.add("Other")
                    }

                    bindSpinner(spinner_fuel, fuelType)

                    if (isForEdit) {

                        var currentFuelType = myCar?.carVersionModel?.fuelType
                        Log.d("AddVehicleActivity", "onResponse: current fuel type = $currentFuelType")

                        if (currentFuelType == null || currentFuelType?.isEmpty()!!)
                            currentFuelType = "Other"
                        var index = 0
                        if(currentFuelType.equals("Others")|| currentFuelType.equals("Other")){
                            if (fuelType.indexOf("Others") > -1) {
                                index = fuelType.indexOf("Others")

                            }else
                                index = fuelType.indexOf(currentFuelType)
                        }
                        else {
                            index = fuelType.indexOf(currentFuelType)
                        }


                        if (index > -1) {
                            spinner_fuel.setSelection(index)
                            if (spinner_fuel.selectedItem != null) {
                                loadVersionAccordingToFuel(carVersions, spinner_fuel.selectedItem,
                                        fuelType.size == 1 && (spinner_fuel.selectedItem == "Other" || spinner_fuel.selectedItem == "Others"))
                            }

                        }

                        Log.d("AddVehicleActivity", "onResponse: fuel selected =  ${spinner_fuel.selectedItem}")


                    }


                    spinner_fuel.setOnSpinnerItemClickListener { _, _ ->

                        loadVersionAccordingToFuel(carVersions, spinner_fuel.selectedItem, fuelType.size == 1 && spinner_fuel.selectedItem == "Other")
                    }


                    isLoaded = true

                }

            }


        })


    }


    private fun loadVersionAccordingToFuel(carVersions: MutableList<Models.CarVersion>, fuelType: String?, shouldBindAll: Boolean) {

//        if(isForEdit)
        spinner_version.clearSelection()


        var list = carVersions.filter { it.fueltype == fuelType }
        if (fuelType?.isEmpty()!!)
            list = carVersions

        if (shouldBindAll)
            list = carVersions

        finalCarVersion = list as MutableList<Models.CarVersion>


        val titles: MutableList<String> = ArrayList()
        titles.clear()
        var selIndex = -1
        list.forEachWithIndex { i, it ->
            /* val title = "${it.version} (${it.kw}/${it.cv}) (${it.dal})"*/
            val title = "${it.version + ","}${it.motore + ","} ${it.modelloCodice + ","} ${it.idVehicle + ","} ${it.body + ","} ${it.cm3}"
            if (true) {
                titles.add(title)

                if (isForEdit) {
                    if (it.idVehicle == myCar?.carVersionModel?.idVehicle)
                        selIndex = i
                }
            } else finalCarVersion.removeAt(i)
        }

        bindSpinner(spinner_version, titles)


        if (selIndex > -1 && titles.size > selIndex) {
            spinner_version.setSelection(selIndex)
        }


    }

    private fun bindSpinner(spinner: AwesomeSpinner, titles: List<String>) {


        spinner.clearSelection()

        if (spinner.id == R.id.spinner_version) {
            spinner.setSpinnerHint(if (titles.isEmpty()) "No Car for this type" else getString(R.string.sel_version))
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, titles)

        spinner.setOnSpinnerItemClickListener { _, _ -> }

        spinner.setAdapter(adapter)

//        Handler().postDelayed({
//            spinner.clearSelection()
//        },50)


    }

    private fun pickImage() {
        hideKeyboard()
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this@AddVehicleActivity)
    }

    private fun addFromPlate() {
        progressDialog.show()


        showOnlineSnack(progressDialog)
        val token = getBearerToken()

        if (getStoredToken()?.isEmpty()!!) {
            showInfoDialog("You have been logged out. Please login again!!!", false) {
                startActivity(intentFor<LoginActivity>().clearTop().clearTask())
                finish()
            }
            return
        }


        RetrofitClient.client.addCarFromPlate(plate_editText.text.toString(), authToken = token!!)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                        Log.e("AddVehicleActivity", "onFailure: onFailure", t)
                        toast("Connection timed out")
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        progressDialog.dismiss()
                        val body = response.body()?.string()!!
                        Log.d("AddVehicleActivity", "onResponse: add car from plate = $body")

                        if (response.code() == 200) {
                            handleAddCarResponse(body)
                        }
                    }

                })
    }

    private fun searchFromPlate() {

        progressDialog.show()
        RetrofitClient.client.searchCarPlate(plate_editText.text.toString())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        progressDialog.dismiss()

                        val body = response.body()?.string()
                        val messageString = if (body.isNullOrEmpty()) getMessageFromJSON(body) else getString(R.string.Something_went_wrong_Please_try_again)


                        Log.d("AddVehicleActivity", "onResponse: search from plate = $body")

                        if (!isStatusCodeValid(body)) {
                            showInfoDialog(messageString, true) {}
                            return
                        }

                        if (isStatusCodeValid(body)) {
                            val dataJson = JSONObject(body)
                            val json = dataJson.getString("data")

                            alert {
                                message =getString(R.string.Doyouwanttocompletethecardetailspersonalizedsuggestions)
                                positiveButton(getString(R.string.yes)) {
                                    val lastCarIntent = getLastCarIntent(body!!)
                                    val car = lastCarIntent.getSerializableExtra(Constant.Key.myCar)

                                    myCar = car as Models.MyCarDataSet
                                    isForEdit = true
                                    setEditMode()
                                }

                                negativeButton(getString(R.string.Completelater)) {
                                    setResult(Activity.RESULT_OK, getLastCarIntent(body!!))
                                    finish()
                                }
                                isCancelable = false
                            }.show()



                            saveLocalCarInJSON(json)
//                            alert {
//                                message = "Car added successfully"
//                                okButton { finish() }
//                            }.show()


                        } else {
                            try {
                                snackbar(add_from_plate, getMessageFromJSON(body))
                            } catch (e: Exception) {
                            }
                        }
                    }

                })

    }

    private fun handleAddCarResponse(body: String) {
        val messageString = getMessageFromJSON(body)

        if (!isStatusCodeValid(body)) {
            showInfoDialog(if (messageString.isNotEmpty()) messageString else getString(R.string.Something_went_wrong_Please_try_again), true) {}
            return
        }

        alert {
            message = getString(R.string.Doyouwanttocompletethecardetailspersonalizedsuggestions)
            positiveButton(getString(R.string.yes)) {
                val lastCarIntent = getLastCarIntent(body)
                val car = lastCarIntent.getSerializableExtra(Constant.Key.myCar)

                myCar = car as Models.MyCarDataSet
                isForEdit = true
                setEditMode()
            }

            negativeButton(getString(R.string.Completelater)) {
                setResult(Activity.RESULT_OK, getLastCarIntent(body))
                finish()
            }
            isCancelable = false
        }.show()


    }


    private fun getLastCarIntent(bodyString: String): Intent {

        val dataSet = getDataSetArrayFromResponse(bodyString)
        val last = (dataSet[dataSet.length() - 1].toString())
        val lastCar = Gson().fromJson<Models.MyCarDataSet>(last, Models.MyCarDataSet::class.java)

        val carIntent = Intent()
        carIntent.putExtra(Constant.Key.myCar, lastCar as Serializable)


        return carIntent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {


                val resultUri: Uri = result.uri
                progressDialog.show()

                uploadCarImageToServer(resultUri, "1")

//                alert {
//                    isCancelable = false
//                    message =  "Do you want to set this as default car image?"
//                positiveButton("Yes"){ uploadCarImageToServer(resultUri, "1") }
//                    negativeButton("No") { uploadCarImageToServer(resultUri, "0") }
//                }.show()


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                toast("Failed to load Image")
            }
        }
    }

    private fun uploadCarImageToServer(imageUri: Uri, isDefault: String) {
        getBearerToken()?.let {

            // request body for multipart image
            val imageFile: File = File(imageUri.path)
            val requestFile: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            val imageBody: MultipartBody.Part = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

            // request body for string parameters
            val requestBodyString: RequestBody = RequestBody.create(MediaType.parse("text/plain"), myCar!!.id)
            val requestBodyDefaultImage = RequestBody.create(MediaType.parse("text/plain"), isDefault)

            RetrofitClient.client.uploadCarImage(requestBodyString, imageBody, requestBodyDefaultImage, it).enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressDialog.dismiss()
                    toast(t.message!!)
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val body = response.body()?.string()
                    progressDialog.dismiss()
                    if (response.code() == 200) {

                        // Save image details
                        val carImagesNewValues = Models.CarImages(JSONObject(body).getJSONObject("data").getString("id"), JSONObject(body).getJSONObject("data").getString("image_name"))

                        carImageList.add(carImagesNewValues)

                        val messageString = JSONObject(body).getString("message")
                        if (messageString.isNotEmpty())
                            alert {
                                message = (messageString)
                                okButton { }
                            }.show()

                        carListImageAdapter.notifyDataSetChanged()
                    }
                }

            })
        }
    }

    fun deleteCarImageFromServer(position: Int) {
        progressDialog.show()
        getBearerToken()!!.let {
            RetrofitClient.client.deleteCarImage(carImageList[position].id, it).enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressDialog.dismiss()
                    snackbar(add_from_fields, getString(R.string.ConnectionErrorPleaseretry))
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    progressDialog.dismiss()

                    val body = response.body()?.string()

                    if (response.code() == 200) {

                        // Delete image details
                        if (position < carImageList.size)
                            carImageList.removeAt(position)
                        carListImageAdapter.notifyDataSetChanged()

                        val messageString = JSONObject(body).getString("message")
                        if (messageString.isNotEmpty())
                            alert {
                                message = (messageString)
                                okButton { }
                            }.show()

                    }
                }
            })
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_RC -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.PermissionDenied), Toast.LENGTH_LONG).show()
                } else {
                    pickImage()
                }
            }
        }
    }

    private fun setDatePickers(textView: TextView) {

        textView.setOnClickListener {
            hideKeyboard()
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this,
                    DatePickerDialog.OnDateSetListener { it, year, month, dayOfMonth ->
                        var dd = (dayOfMonth).toString()
                        var mm = (month + 1).toString()
                        val yyyy = year.toString()

                        try {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_YEAR, year)
                            cal.set(Calendar.MONTH, month)
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                            val timeInMillis = cal.timeInMillis
                            val dateString = getFormattedDate(timeInMillis)
                            textView.text = dateString

                        } catch (e: java.lang.Exception) {
                            if (dayOfMonth <= 9)
                                dd = "0$dd"
                            if (month <= 9)
                                mm = "0$mm"

                            val date = "$dd-$mm-$yyyy"
                            textView.text = date
                        }

                    }
                    , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePicker.show()
        }
    }


    private fun showOnlineSnack(progressDialog: ProgressDialog?) {
        val view = add_from_fields
        if (!isOnline()) {
            Snackbar.make(view, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG)
                    .show()
            progressDialog?.dismiss()
        }
    }


    private fun getEmptyAdapter() = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf())
}
