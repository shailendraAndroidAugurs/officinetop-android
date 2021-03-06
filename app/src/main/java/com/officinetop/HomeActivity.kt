package com.officinetop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.officinetop.Support.Support_FAQ_Activity
import com.officinetop.authentication.LoginActivity
import com.officinetop.data.*
import com.officinetop.fragment.*
import com.officinetop.misc_activities.LocationActivity
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.virtual_garage.AddVehicleActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.container
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.dialog_car_list.*
import kotlinx.android.synthetic.main.item_my_cars_small.view.*
import kotlinx.android.synthetic.main.layout_home_custom_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class HomeActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, FragmentChangeListener {
    lateinit var dialog: Dialog
    val carList: MutableList<Models.MyCarDataSet> = ArrayList()
    private lateinit var carListAdapter: BaseAdapter
    private var lastListSize = -1
    private var hasAddedCar = false
    lateinit var progressDialog: ProgressDialog
    private var hasSelectedCar = false
    var isConnectionError = false
    private var googleApiClient: GoogleApiClient? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var carListResponse = ""
    private var mLastLocation: Location? = null

    private val internetBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
        }
    }

    private fun showOnlineSnack(progressDialog: ProgressDialog?): Boolean {
        val view = home_bottom_navigation_view
        if (!isOnline()) {
            Snackbar.make(view, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG)
                    .show()
            progressDialog?.dismiss()
        }
        return isOnline()
    }


    private fun loadNavigationItems(itemId: Int) {
        when (itemId) {
            R.id.action_menu_home, R.id.menu_home -> if (!(supportFragmentManager.findFragmentByTag("Home") is FragmentHome)) {

                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentHome(), "Home")
                        .commit()
                home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
            }

            R.id.action_news, R.id.menu_news -> {

                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NewsFragment())
                        .commit()

                home_bottom_navigation_view.menu.findItem(R.id.action_news).isChecked = true
            }

            R.id.action_menu_profile, R.id.menu_profile -> {


                if (!isLoggedIn()) {
                    startActivity(intentFor<LoginActivity>().clearTop())
                } else {
                    supportFragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, ProfileFragment())
                            .commit()
                }


                home_bottom_navigation_view.menu.findItem(R.id.action_menu_profile).isChecked = true

            }


            R.id.action_cart, R.id.menu_cart -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentCart(), "cart")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                home_bottom_navigation_view.menu.findItem(R.id.action_cart).isChecked = true
            }

            R.id.action_feedback, R.id.menu_feedback -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentFeedback())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()

                home_bottom_navigation_view.menu.findItem(R.id.action_feedback).isChecked = true
            }

            else -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NewsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        getLocation()
        var address_data: ArrayList<String> = getAdress()
        if (isLoggedIn() && address_data.get(4).equals("true")) {
            saveLocation(address_data)
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progressDialog = getProgressDialog()
        registerReceiver(internetBroadcast, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val fragmentHome = FragmentHome()
        home_bottom_navigation_view.setOnNavigationItemSelectedListener {
            loadNavigationItems(it.itemId)
            return@setOnNavigationItemSelectedListener true
        }

        createMyCarDialog()
        toolbar_car_subtitle.visibility = View.GONE

//        if (isAutomaticLocation())
//        updateCurrentLocation()

        user_location.setOnClickListener {
            startActivity(intentFor<LocationActivity>())
        }

        image_support.setOnClickListener(View.OnClickListener {
            startActivity(intentFor<Support_FAQ_Activity>())
        })


        toolbar_title_layout.setOnClickListener {

            if (isLoggedIn() && isConnectionError) {

                alert {
                    message = getString(R.string.ConnectionErrorPleaseretry)
                    positiveButton(getString(R.string.retry)) { loadMyCars() }
                    negativeButton(getString(R.string.cancel)) {}
                }.show()
                return@setOnClickListener
            }


            if (!isLoggedIn() && !hasAddedCar) {
                showInfoDialog(getString(R.string.dialog_message_add_car), true) {
                    startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded)
                }
                return@setOnClickListener
            }

            //show drop down filterDialog
            if (hasAddedCar) {
                dialog.show()
                loadMyCars()
            } else {
                if (shouldShowAddCarInfoDialog()) {
                    showInfoDialog(getString(R.string.dialog_message_add_car), true) { startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded) }
                } else
                    startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded)
            }
        }
        //
        //setting initial car image
        Glide.with(this).load(R.drawable.ic_car).thumbnail(0.1f).into(toolbar_image_view)


        // load screens if navigated from options menu
        if (intent != null && intent.hasExtra("fragmentID")) {
            bindFragment(intent.getIntExtra("fragmentID", R.id.action_menu_home))
        } else {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentHome, "Home")
                    .commit()
        }
        if (!isLoggedIn()) {
            setCarListFromLocal()
            return
        } else {
            getSelectedCarAccordingToUser()
        }

        if (intent.hasExtra("loadProfileFragment")) {
            val isTrue = intent?.getBooleanExtra("loadProfileFragment", false) ?: false
            if (isTrue) {
                loadNavigationItems(R.id.action_menu_profile)
            }
        }

        if (!isOnline()) {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }

    }

    private fun updateCurrentLocation() {
        if (isLocationEnabled(this)) {
            getcurrentLocation()
        }
    }

    private fun getcurrentLocation() {
        checkpermission(storagePermissionRequestList(), { getLastLocation() }, true)
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient!!.lastLocation
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLastLocation = task.result
                        try {
                            storeLatLong(mLastLocation!!.latitude, mLastLocation!!.longitude, true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

    }



    override fun onResume() {
        try {

            if (!isLoggedIn())
                setCarListFromLocal()
            else if (intent.hasExtra("login_success") && intent.getBooleanExtra("login_success", false)) {
                getSelectedCarAccordingToUser()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onResume()
    }


    private fun loadMyCars() {
        carList.clear()
        carListAdapter.notifyDataSetChanged()

        if (!isLoggedIn()) {
            setCarListFromLocal()

            return
        }
        val rotateAnimation = getRotateAnimation()
        dialog.dialog_refresh.startAnimation(rotateAnimation)
        getCarListAPI()
    }

    private fun getCarListAPI() {


        if (!showOnlineSnack(progressDialog))
            return
        progressDialog.show()

        getBearerToken()?.let { token ->
            RetrofitClient.client.myCars(token).enqueue(object : Callback<Models.MyCar> {
                override fun onFailure(call: Call<Models.MyCar>, t: Throwable) {
                    try {
                        dialog.dialog_refresh.clearAnimation()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        progressDialog.dismiss()
                    }
                    progressDialog.dismiss()
                    toast(t.message!!)
                    isConnectionError = true
                }

                override fun onResponse(call: Call<Models.MyCar>, response: Response<Models.MyCar>) {
                    val responseBody = response.body()
                    carListResponse = responseBody.toString()
                    try {
                        dialog.dialog_refresh.clearAnimation()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    progressDialog.dismiss()

                    isConnectionError = false

                    if (response.code() == 500) {
                        isConnectionError = true
                        return
                    }



                    if (response.code() == 401) {
                        showInfoDialog(getString(R.string.Login_session_expired), false) {
                            removeUserDetail()
                            startActivity(intentFor<LoginActivity>().clearTop())
                            finish()
                        }
                        return
                    }

                    if (responseBody == null) {
                        isConnectionError = true
                        return
                    }


                    setCarList(responseBody)

                }

            })

        }
    }


    private fun setCarListFromLocal() {

        val json = getLocalSavedCarJSON()

        if (hasLocalSavedCar()) {


            val carDataset = Gson().fromJson<Models.MyCarDataSet>(json, Models.MyCarDataSet::class.java)


            if (!carList.contains(carDataset)) {

                carList.add(carDataset)
                carListAdapter.notifyDataSetChanged()
                lastListSize = carList.size
            }


            hasAddedCar = carList.isNotEmpty()
            if (hasAddedCar)
                setToolbarValues(carDataset)
        } else {
            resetToolbar()
            hasAddedCar = false
        }


    }

    private fun setCarList(responseBody: Models.MyCar?) {

        carList.clear()
        carListAdapter.notifyDataSetChanged()


        responseBody?.dataSet?.forEach {

            if (!carList.contains(it)) {
                carList.add(it)
                carListAdapter.notifyDataSetChanged()
                lastListSize = carList.size
            }
        }

        hasAddedCar = carList.isNotEmpty()

        //set toolbar items
        if (hasAddedCar) {

            carList.filter {
                it.id == getSavedSelectedVehicleID()
            }.forEach {

                toolbar_car_subtitle.visibility = View.VISIBLE
                if (it.id != getSavedSelectedVehicleID() && isLoggedIn() && getIsAvailableDataInCart()) {
                    showConfirmDialog(getString(R.string.cart_data_removed)) { deleteCartData(it) }

                } else {
                    setToolbarValues(it)
                }
                hasAddedCar = true
            }

            hasSelectedCar = carList.any { it.id == getSavedSelectedVehicleID() }

            if (!hasSelectedCar) {


                if (carList.find { it.selected == "1" }?.id ?: 0 != getSavedSelectedVehicleID() && isLoggedIn() && getIsAvailableDataInCart()) {
                    showConfirmDialog(getString(R.string.cart_data_removed)) { deleteCartData(carList.find { it.selected == "1" }) }

                } else {
                    setToolbarValues(carList.find { it.selected == "1" })
                }




                if (carList.find { it.selected == "1" } == null) {
                    if (carList.last().id != getSavedSelectedVehicleID() && isLoggedIn() && getIsAvailableDataInCart()) {
                        showConfirmDialog(getString(R.string.cart_data_removed)) { deleteCartData(carList.last()) }

                    } else {
                        setToolbarValues(carList.last())
                    }

                }
            }


        } else {
            resetToolbar()
            hasAddedCar = false
        }

        carListAdapter.notifyDataSetChanged()
        if (carListAdapter.count == 0) {
            dialog.dismiss()
            hasAddedCar = false
        }


    }

    private fun resetToolbar() {
        progressDialog.hide()
        toolbar_car_subtitle.visibility = View.GONE
        toolbar_car_title.text = getString(R.string.add_car)
        Glide.with(this).load(R.drawable.ic_car).thumbnail(0.1f).into(toolbar_image_view)
        Log.d("HomeActivity", "resetToolbar: ")
    }

    private fun createMyCarDialog() {

        dialog = Dialog(this@HomeActivity, R.style.DialogSlideAnimStyle)

        with(dialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_car_list)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)


            carListAdapter = object : BaseAdapter() {
                @SuppressLint("ViewHolder", "SetTextI18n")
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

                    val car = carList[position]

                    Log.d("checked_created_date", car.id + "  ")

                    val view = layoutInflater.inflate(R.layout.item_my_cars_small, parent, false)

                    try {
                        view.item_title_car.text = "${car.carMakeModel.brand} - ${car.carModel.model} (${car.carModel.modelYear})"
                    } catch (e: Exception) {
                        view.item_sub_title_car.text = ""
                    }


                    try {
                        view.item_sub_title_car.text = ("${car.carVersionModel.version} (${car.carVersionModel.kw}/${car.carVersionModel.cv}) (${car.carVersionModel.dal})")
                    } catch (e: Exception) {
                        Log.d("HomeActivity", "getView: ${e.message}")
                        view.item_sub_title_car.text = ""
                    }
                    val carDefaultImage = car.carDefaultImage ?: ""

                    try {

                        if (carDefaultImage.isNullOrBlank() || carDefaultImage.contains("http")) {
                            loadImage(carDefaultImage, view.item_car_image_view)

                        } else {
                            loadCarImage(carDefaultImage, car.carMakeModel.brandID, view.item_car_image_view)

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }


                    view.item_delete_car.setOnClickListener {

                        if (isLoggedIn()) {
                            if (car.id.equals(getSavedSelectedVehicleID()) && getIsAvailableDataInCart()) {
                                showConfirmDialogWithTitle(getString(R.string.cart_data_removed_title), getString(R.string.Delete_car_from_virtual_garage_home)) { deleteCartDataWithCarDeleted(car) }
                            } else
                                deleteCar(car.id)
                        } else {
                            alert {
                                message = "Delete this car from virtual garage?"
                                yesButton {
                                    removeLocalSavedCar()
                                    loadMyCars()
                                    dismiss()
                                }
                                noButton { }
                            }.show()
                        }
                    }

                    view.item_edit_car.setOnClickListener {

                        if (!isLoggedIn()) {
                            startActivityForResult(intentFor<AddVehicleActivity>(
                                    Constant.Key.myCar to car as Serializable
                            ), Constant.RC.onCarAdded)
                        } else {
                            startActivityForResult(intentFor<AddVehicleActivity>(
                                    Constant.Key.myCar to car as Serializable
                            ), Constant.RC.onCarEdited)
                        }

                        dismiss()
                    }

                    view.item_my_car_small_layout.setOnClickListener {
                        if (car.id != getSavedSelectedVehicleID() && isLoggedIn() && getIsAvailableDataInCart()) {
                            showConfirmDialog(getString(R.string.cart_data_removed)) { deleteCartData(car) }

                        } else {
                            setToolbarValues(car)
                        }


                        dismiss()

                    }

                    return view

                }

                override fun getItem(position: Int): Any = carList[position]

                override fun getItemId(position: Int): Long = position.toLong()

                override fun getCount(): Int = carList.size

            }



            dialog_top_layout.setOnClickListener { dismiss() }
            select_dialog_listview.adapter = carListAdapter

            add_car.setOnClickListener {
                if (!isLoggedIn()) {
                    alert {
                        message = getString(R.string.LoginToAddMultipalCar)
                        positiveButton(getString(R.string.yes)) {
                            //                        startActivityForResult(intentFor<LoginActivity>(), Constant.RC.onCarAdded)
                            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                            intent.putExtra(Constant.pref_login_from, "AddSecondVehicle")
                            startActivity(intent)
                        }
                        negativeButton(getString(R.string.no)) {}
                    }.show()
                } else {

                    val jsonString = Gson().toJson(carList)
                     val intent = Intent(this@HomeActivity, AddVehicleActivity::class.java)
                    intent.putExtra(Constant.car_list, jsonString)
                    startActivityForResult(intent, Constant.RC.onCarAdded)
                }
                dismiss()
            }

            dialog_refresh.setOnClickListener { loadMyCars() }

            create()

        }
    }


    private fun setToolbarValues(car: Models.MyCarDataSet?, fromAddNewCar: Boolean = false) {

        try {

            if (car == null)
                return

            toolbar_car_title.text = car.carMakeModel.brand
            toolbar_car_subtitle.text = "${car.carModel.model} (${car.carModel.modelYear})"
            toolbar_car_subtitle.visibility = View.VISIBLE

            val carDefaultImage = car.carDefaultImage ?: ""


            if (!carDefaultImage.isNullOrBlank() || carDefaultImage.contains("http")) {
                loadImage(carDefaultImage, toolbar_image_view)
            } else {
                loadImage(carDefaultImage, toolbar_image_view)

            }

            val json = convertToJson(car)
            Log.d("HomeActivity", "setToolbarValues: $json")


            //call select car API
            if (car != null) {
                if (!car.id.isNullOrBlank()) {
                    selectCar(car.id)
                }

                saveMotCarKM(car.km_of_cars)

            }
            if (car.id != getSavedSelectedVehicleID() && isLoggedIn()) {
                if (getIsAvailableDataInCart()) {
                    showConfirmDialog(getString(R.string.cart_data_removed)) { deleteCartData(car) }
                }

            }
            Log.d("check_card_id", car.id + "   " + getSavedSelectedVehicleID())



            if (json != null) {
                hasSelectedCar = true
                hasAddedCar = true

                if (car.id != getSavedSelectedVehicleID()) {

                    if ((supportFragmentManager.findFragmentByTag("Home") is FragmentHome)) {

                        /* supportFragmentManager.beginTransaction()
                                .replace(R.id.container, FragmentHome(), "Home")
                                .commit()

                        home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
*/                      saveSelectedCar(json)
                        val fragment: FragmentHome = supportFragmentManager.findFragmentByTag("Home") as FragmentHome
                        fragment.bestSellingApi()


                    } else {
                        saveSelectedCar(json)
                    }

                } else {
                    saveSelectedCar(json)
                }


            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("exception", e.toString())
        }

    }


    private fun logout(authToken: String) {


        progressDialog.show()


        if (!showOnlineSnack(progressDialog))
            return

        RetrofitClient.client.logout(authToken)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                        toast(t.message!!)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val responseString = response.body()?.string()
                        progressDialog.dismiss()
                        Log.d("LoginActivity", "onResponse: logout = $responseString")
                        if (response.code() == 200) {
                           // removeUserDetail()
                        }

                        responseString?.let {
                            toast(getMessageFromJSON(it))
                            startActivity(intentFor<LoginActivity>().clearTop())
                            finish()
                        }
                    }

                })
    }

    private fun selectCar(carID: String) {

        if (carID == getSavedSelectedVehicleID())
            return

        getBearerToken()?.let {
            RetrofitClient.client.selectCar(carID, it)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                            response.body()?.string()?.let { body ->
                                if (isStatusCodeValid(body)) {
                                }
                            }

                        }
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.RC.onCarEdited) {
                getSelectedCarAccordingToUser()
            } else if (requestCode == Constant.RC.onCarAdded) {
                try {
                    if (data != null && data.extras != null) {
                        val lastCar = data.extras?.getSerializable(Constant.Key.myCar)!! as Models.MyCarDataSet

                        if (!getIsAvailableDataInCart()) {
                            setToolbarValues(lastCar, true)
                        }


                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == 108) {
                if (getMotKm() != getSelectedCar()?.km_of_cars)
                    getSelectedCarAccordingToUser()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)

    }


    fun deleteCar(carID: String, shownAlert: Boolean = true) {
        if (shownAlert) {
            alert {
                message = getString(R.string.Delete_car_from_virtual_garage)
                positiveButton(getString(R.string.yes)) {
                    carDeletedApi(carID)
                }
                noButton { }
            }
                    .show()
        } else {
            carDeletedApi(carID)
        }


    }

    private fun carDeletedApi(carID: String) {
        getBearerToken()?.let {
            val progressDialog = getProgressDialog()
            progressDialog.show()

            if (!showOnlineSnack(progressDialog))
                return@let
            RetrofitClient.client.deleteCar(carID, it).enqueue(object : Callback<Models.MyCar> {
                override fun onFailure(call: Call<Models.MyCar>, t: Throwable) {
                    progressDialog.dismiss()
                    toast(t.message!!)
                }

                override fun onResponse(call: Call<Models.MyCar>, response: Response<Models.MyCar>) {
                    val responseBody = response.body()
                    progressDialog.dismiss()
                    Log.d("deleteCar", "onResponse: $responseBody")


                    if (response.code() == 200) {
                        progressDialog.dismiss()
                        setCarList(responseBody)
                    }
                }

            })
        }
    }

    private fun syncLastAddedCar() {

        if (!hasLocalSavedCar())
            return

        if (!isLoggedIn())
            return

        val json = getLocalSavedCarJSON()
        val dataSet = Gson().fromJson<Models.MyCarDataSet>(json, Models.MyCarDataSet::class.java)

        val progressDialog = getProgressDialog()
        progressDialog.show()

        Log.d("HomeActivity", "syncLastAddedCar: car = ${convertToJson(dataSet)}")

        RetrofitClient.client.addCar(dataSet.carMakeModel.brandID,
                dataSet.carModel.modelID + "/" + dataSet.carModel.modelYear,
                dataSet.carVersionModel.idVehicle,
                dataSet.carSize,
                getBearerToken() ?: "", "ENG", "0", "0")
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                        window.decorView.rootView.snackbar(getString(R.string.SyncFailed))

                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        progressDialog.dismiss()
                        val body = response.body()?.string()
                        Log.d("HomeActivity", "onResponse: sync response = $body")
                        if (isStatusCodeValid(body)) {
                            Log.d("HomeActivity", "onResponse: Previous car synced")
                            removeLocalSavedCar()
                        }

                    }

                })
    }

    fun checkForSelectedCar(): Boolean {

        if (isLoggedIn()) {

            if (hasAddedCar && hasSelectedCar) {
                return true
            } else if (hasAddedCar && !hasSelectedCar) {
                showInfoDialog(getString(R.string.dialog_message_select_car), true) {
                    dialog.show()
                }
            } else {
                showInfoDialog(getString(R.string.dialog_message_add_car), true) {
                    startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded)
                }
            }

            return false
        } else {
            if (hasLocalSavedCar()) {
                return true
            } else {
                showInfoDialog(getString(R.string.dialog_message_add_car), true) {
                    startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded)
                }
            }

            return false
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(internetBroadcast)
    }


    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
        container.snack("Suspend")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        container.snack("Failed")
    }


    override fun onStart() {
        super.onStart()

        if (googleApiClient != null) {
            googleApiClient!!.connect()
        }
    }

    private fun bindFragment(FragmentId: Int) {
        loadNavigationItems(FragmentId)
    }

    private fun deleteCartData(car: Models.MyCarDataSet?) {
        getBearerToken()?.let {
            RetrofitClient.client.removeCart(it)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            response.body()?.string()?.let { body ->
                                if (isStatusCodeValid(body)) {

                                    toolbar_car_title.text = car?.carMakeModel?.brand
                                    toolbar_car_subtitle.text = "${car?.carModel?.model} (${car?.carModel?.modelYear})"
                                    toolbar_car_subtitle.visibility = View.VISIBLE
                                    saveIsAvailableDataInCart(false)
                                    val carDefaultImage = car?.carDefaultImage ?: ""

                                    if (carDefaultImage.isNullOrBlank() || carDefaultImage.contains("http")) {
                                        loadImage(carDefaultImage, toolbar_image_view)

                                    } else {
                                        loadCarImage(carDefaultImage, car?.carMakeModel?.brandID, toolbar_image_view)

                                    }


                                    val json = car?.let { it1 -> convertToJson(it1) }
                                    //call select car API
                                    if (car != null) {
                                        selectCar(car.id)
                                        saveMotCarKM(car.km_of_cars)

                                    }

                                    if (json != null) {
                                        saveSelectedCar(json)

                                    }
                                    if (supportFragmentManager.findFragmentByTag("cart") is FragmentCart) {
                                        supportFragmentManager.beginTransaction()
                                                .replace(R.id.container, FragmentHome(), "Home")
                                                .commit()


                                        home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
                                    }

                                }


                            }
                        }


                    })
        }

    }

    private fun deleteCartDataWithCarDeleted(car: Models.MyCarDataSet?) {
        getBearerToken()?.let {
            RetrofitClient.client.removeCart(it)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            response.body()?.string()?.let { body ->
                                if (isStatusCodeValid(body)) {

                                    saveIsAvailableDataInCart(false)
                                    deleteCar(car!!.id, false)
                                }
                            }
                        }


                    })
        }

    }


    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("Home") is FragmentHome || isCurrentFragmentHome()) {
            finish()

        } else {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, FragmentHome())
                    .commit()
            home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
        }
    }

    override fun replaceFragment(fragmnet: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(container.id, fragmnet)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()

        home_bottom_navigation_view?.menu?.findItem(R.id.action_feedback)?.isChecked = true

    }

    private fun isCurrentFragmentHome(): Boolean {

        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment != null && fragment is FragmentHome)
                return true
        }
        return false
    }

    private fun getSelectedCarAccordingToUser() {
        progressDialog.show()
        getBearerToken()?.let {
            RetrofitClient.client.getselectedUserCar(it)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progressDialog.dismiss()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            progressDialog.dismiss()
                            response.body()?.string()?.let { body ->
                                if (isStatusCodeValid(body)) {

                                    Log.d("HomeActivity", "my car : onResponse: code = ${response.code()}, body = $body")

                                    isConnectionError = false

                                    if (response.code() == 500) {
                                        Log.e("HomeActivity", getString(R.string.onResponseCode500) + { response.errorBody()?.string() })
                                        isConnectionError = true
                                        return
                                    }



                                    if (response.code() == 401) {
                                        showInfoDialog(getString(R.string.Login_session_expired), false) {
                                            removeUserDetail()
                                            startActivity(intentFor<LoginActivity>().clearTop())
                                            finish()
                                        }
                                        return
                                    }

                                    if (body == null) {
                                        isConnectionError = true
                                        return
                                    }
                                    val userSelectedCarRespnces = Gson().fromJson<Models.MyCar>(body, Models.MyCar::class.java)
                                    setCarList(userSelectedCarRespnces)
                                }
                            }

                        }
                    })
        }
    }

    private fun saveLocation(address_data: ArrayList<String>) {
        var  completeAddress =address_data.get(5)
        RetrofitClient.client.saveUserLocation(getBearerToken()
                ?: "", getLat(), getLong(), "", completeAddress, "", "",
                address_data.get(3), address_data.get(1), address_data.get(1), address_data.get(0), address_data.get(2))
                .onCall { networkException, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val jsonObject = JSONObject(response.body()?.string())
                            if (jsonObject.has("status_code") && jsonObject.optString("status_code") == "1" && jsonObject.has("message")) {
                                applicationContext.storeAddress(address_data.get(0),address_data.get(1),address_data.get(2),address_data.get(3),address_data.get(5),"false")
                            }
                        } else if (response.code() == 401) {
                            /*storeLatLong(latitude?.toDouble()!!, longitude?.toDouble()!!, true)
                                showInfoDialog("successFully Saved") {
                                    finish()
                                }*/
                        }
                    }
                }
    }




}


