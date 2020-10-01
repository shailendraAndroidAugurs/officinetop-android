package com.officinetop.officine

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.officinetop.officine.authentication.LoginActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.Support.Support_FAQ_Activity
import com.officinetop.officine.fragment.*
import com.officinetop.officine.misc_activities.LocationActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.virtual_garage.AddVehicleActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_car_list.*
import kotlinx.android.synthetic.main.item_my_cars_small.view.*
import kotlinx.android.synthetic.main.layout_home_custom_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
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
    private val TAG = "MyFirebaseToken"
    private var googleApiClient: GoogleApiClient? = null
    private var isLocationOn: Boolean = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null


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
            R.id.action_menu_home, R.id.menu_home -> {

                if (!(supportFragmentManager.findFragmentByTag("Home") is FragmentHome)) {


                    supportFragmentManager.beginTransaction()
                            .replace(R.id.container, FragmentHome(), "Home")
                            .commit()


                    home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
                }


            }

            R.id.action_news, R.id.menu_news -> {

                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NewsFragment())
                        .commit()

                home_bottom_navigation_view.menu.findItem(R.id.action_news).isChecked = true
            }

            R.id.action_menu_profile, R.id.menu_profile -> {


                if (!isLoggedIn()) {
                    alert {
                        message = getString(R.string.not_logged_in)
                        positiveButton(getString(R.string.login)) {
                            startActivity(intentFor<LoginActivity>().clearTop())
                            //                                finish()
                        }
                        negativeButton(getString(R.string.ok)) {}
                    }.show()
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
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra(Constant.pref_login_from, "AddFirstVehicle")
                if (shouldShowAddCarInfoDialog()) {
                    showInfoDialog(getString(R.string.dialog_message_add_car), true) {
                        startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded)
                    }
                } else
                    startActivityForResult(intent, Constant.RC.onCarAdded)
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

        loadMyCars()

        // load screens if navigated from options menu
        if (intent != null && intent.hasExtra("fragmentID")) {
            Log.v("INTENT", "*************** " + intent.getIntExtra("fragmentID", R.id.action_menu_home))
            bindFragment(intent.getIntExtra("fragmentID", R.id.action_menu_home))
        } else {

            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentHome, "Home")
                    .commit()
        }


        user_location.setOnClickListener {
            startActivity(intentFor<LocationActivity>())
        }

        image_support.setOnClickListener(View.OnClickListener {
            startActivity(intentFor<Support_FAQ_Activity>())
        })
        if (intent.hasExtra("loadProfileFragment")) {
            val isTrue = intent?.getBooleanExtra("loadProfileFragment", false) ?: false
            if (isTrue) {
                loadNavigationItems(R.id.action_menu_profile)
            }
        }


    }

    override fun onResume() {
        try {

            if (!isLoggedIn())
                setCarListFromLocal()
            else if (intent.hasExtra("login_success") && intent.getBooleanExtra("login_success", false)) {
                getCarListAPI()
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
                    Log.e("HomeActivity", "onFailure: onFailure = ${t.message}", t)
                    isConnectionError = true
                }

                override fun onResponse(call: Call<Models.MyCar>, response: Response<Models.MyCar>) {
                    val responseBody = response.body()
                    val body = responseBody?.toString()
                    try {
                        dialog.dialog_refresh.clearAnimation()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    progressDialog.dismiss()

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

                    if (responseBody == null) {
                        Log.d("HomeActivity", "onResponse: response body is null")
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
                setToolbarValues(it)
                hasAddedCar = true
            }

            hasSelectedCar = carList.any { it.id == getSavedSelectedVehicleID() }

            Log.d("HomeActivity", "setCarList: has selected = $hasSelectedCar")

            if (!hasSelectedCar) {
                Log.d("HomeActivity", "setCarList: $carList")
                setToolbarValues(carList.find { it.selected == "1" })
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
//                Log.d("HomeActivity", "getView: $car")

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
                        loadCarImage(carDefaultImage, car.carMakeModel.brandID, view.item_car_image_view)
                    } catch (e: java.lang.Exception) {
                    }


                    view.item_delete_car.setOnClickListener {

                        if (isLoggedIn())
                            deleteCar(car.id)
                        else {
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

                        setToolbarValues(car)

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
                } else
                    startActivityForResult(intentFor<AddVehicleActivity>(), Constant.RC.onCarAdded)
                dismiss()
            }

            dialog_refresh.setOnClickListener { loadMyCars() }

            create()

        }
    }


    @SuppressLint("SetTextI18n")
    private fun setToolbarValues(car: Models.MyCarDataSet?) {

        try {

            if (car == null)
                return


            //if (toolbar_car_title.text ==/*"Add Car"*/ getString(R.string.add_car) || toolbar_car_title.text == "Add Car" || !isLoggedIn()) {


            toolbar_car_title.text = car?.carMakeModel?.brand
            toolbar_car_subtitle.text = "${car?.carModel?.model} (${car?.carModel?.modelYear})"
            toolbar_car_subtitle.visibility = View.VISIBLE

            val carDefaultImage = car.carDefaultImage ?: ""
            loadCarImage(carDefaultImage, car.carMakeModel?.brandID, toolbar_image_view)

            Log.d("HomeActivity", "getView: setting toolbar values, value = ${car?.carMakeName}, ${car?.carModelName}")
//            storeSelectedCar(car.carMakeName, car.carModelName)

            val json =/* car?.let { it1 -> convertToJson(it1) }*/convertToJson(car)
            Log.d("HomeActivity", "setToolbarValues: $json")

            //call select car API
            if (car != null) {
                if (!car.id.isNullOrBlank()) {
                    selectCar(car.id!!)
                }

                saveMotCarKM(car.km_of_cars)

            }

            if (json != null) {
                saveSelectedCar(json)
                hasSelectedCar = true
                hasAddedCar
                if (car.id != getSavedSelectedVehicleID()) {

                    if ((supportFragmentManager.findFragmentByTag("Home") is FragmentHome)) {

                        supportFragmentManager.beginTransaction()
                                .replace(R.id.container, FragmentHome(), "Home")
                                .commit()

                        home_bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
                    }


                }

                Log.d("Version id", json.toString())

            }


            // }

            if (car.id != getSavedSelectedVehicleID() && isLoggedIn()) {
                if (getIsAvailableDataInCart()) {
                    Log.d("HomeActivity", "Delete Call")
                    showConfirmDialog(getString(R.string.CartDataRemoved)) { DeleteCartData(car) }
                } else {
                    DeleteCartData(car)
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
                            removeUserDetail()
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
                                    Log.d("HomeActivity", "on Car $carID Selection : ${getMessageFromJSON(body)}")
                                }
                            }

                        }
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {


            if (requestCode == Constant.RC.onCarEdited) {
                getCarListAPI()
            } else if (requestCode == Constant.RC.onCarAdded) {
                try {
                    if (data != null && data?.extras != null) {
                        val lastCar = data?.extras?.getSerializable(Constant.Key.myCar)!! as Models.MyCarDataSet
                        Log.d("HomeActivity", "onActivityResult: $lastCar")
                        setToolbarValues(lastCar)
                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == 108) {
                if (getMotKm() != getSelectedCar()?.km_of_cars)
                    getCarListAPI()
            }


        }
        super.onActivityResult(requestCode, resultCode, data)

    }


    fun deleteCar(carID: String) {

        alert {
            message = getString(R.string.Delete_car_from_virtual_garage)
            positiveButton(getString(R.string.yes)) {
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

                                loadMyCars()
                                progressDialog.dismiss()
                                setCarList(responseBody)
                            }
                        }

                    })
                }
            }
            noButton { }
        }
                .show()


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
                        snackbar(window.decorView.rootView, getString(R.string.SyncFailed))

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


    /** For fragment callable
     *
     * */

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (/*requestCode == Constant.REQUEST_PERMISSIONS_LOCATION &&*/ grantResults[0] == PackageManager.PERMISSION_GRANTED /*&&
                grantResults[1] == PackageManager.PERMISSION_GRANTED*/) {
            if (!isLocationOn)

                getLastLocation(mFusedLocationClient!!, this) else
                enableLocation()
        } else {
            // Permission denied.
            container.snack(getString(R.string.permission_denied_explanation), duration = Snackbar.LENGTH_INDEFINITE) {
                if (hasLocationPermission()) getLastLocation(mFusedLocationClient!!, this)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (requestCode == 1000) {
             if (resultCode == Activity.RESULT_OK) {
                 //val result : String = data!!.getStringExtra("result")
                 getLastLocation()
             }
             if (resultCode == Activity.RESULT_CANCELED) {
                 //Write your code if there's no result
             }
         }
         super.onActivityResult(requestCode, resultCode, data)
     }*/


    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
        container.snack("Suspend")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        container.snack("Failed")
    }

    private fun enableLocation() {

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build()
            googleApiClient!!.connect()
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10 * 1000
            locationRequest.fastestInterval = 2 * 1000
            val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)


            val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback {
                val status: Status = it.status
                val state: LocationSettingsStates = it.locationSettingsStates
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        isLocationOn = true
                        container.snack(getString(R.string.success))
                        getLastLocation(mFusedLocationClient!!, this)
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        container.snack(getString(R.string.gps_not_on))
                        try {
                            status.startResolutionForResult(this, 1000)
                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        container.snack(getString(R.string.settings_change_not_allowed))
                    }

                }

            }
        }
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

    private fun DeleteCartData(car: Models.MyCarDataSet?) {
        getBearerToken()?.let {
            RetrofitClient.client.RemoveCart(it)
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
                                    loadCarImage(carDefaultImage, car?.carMakeModel?.brandID, toolbar_image_view)
                                    1
                                    Log.d("HomeActivity", "getView: setting toolbar values, value = ${car?.carMakeName}, ${car?.carModelName}")
//            storeSelectedCar(car.carMakeName, car.carModelName)

                                    val json = car?.let { it1 -> convertToJson(it1) }
                                    Log.d("HomeActivity", "setToolbarValues: $json")

                                    //call select car API
                                    if (car != null) {
                                        selectCar(car.id!!)
                                        saveMotCarKM(car.km_of_cars)

                                    }

                                    if (json != null) {

                                        saveSelectedCar(json)
                                        Log.d("Version id", json.toString())

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

    override fun onBackPressed() {
        //  super.onBackPressed()

        if (supportFragmentManager.findFragmentByTag("Home") is FragmentHome || iscurrentFragmentHome()) {
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

    private fun iscurrentFragmentHome(): Boolean {

        val fragments = supportFragmentManager.getFragments();
        for (fragment in fragments) {
            if (fragment != null && fragment is FragmentHome)
                return true
        }
        return false;
    }

}

