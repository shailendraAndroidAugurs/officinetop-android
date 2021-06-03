package com.officinetop.sos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.data.*
import com.officinetop.databinding.MapLayoutBinding
import com.officinetop.databinding.SosEmergencyPopupWindowBinding
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.workshop.WorkshopBookingDetailsActivity
import com.officinetop.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.activity_sos.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import kotlinx.android.synthetic.main.sos_list_layout.view.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SOSActivity : BaseActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener {


    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
        container.snack("Suspend")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        container.snack("Failed")
    }

    private lateinit var mMap: GoogleMap
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLatitude: String? = null
    private var mLongitude: String? = null

    private lateinit var locationMarker: Marker

    private var latlongList: MutableList<Models.SOSWorkshop> = ArrayList()

    private var allWrackerServicesWorkshopList: MutableList<Models.AllWrackerWorkshops> = ArrayList()

    private var wrackersList: MutableList<Models.WrackerServices> = ArrayList()
    private var sosActivityListener: SOSActivityListener? = null
    private var googleApiClient: GoogleApiClient? = null
    private var main_category_id = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.title_activity_sos)
        mLatitude = getLat()
        mLongitude = getLong()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
        checkpermission(storagePermissionRequestList(), {
            enableLocation()
            getcurrentlocation()
        }, true)

        emergency_call.setOnClickListener {
            callEmergency()
        }
        if (!isOnline()) {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }
    }

    private fun callEmergency() {
        // Use format with "tel:" and phoneNumber created is
        // stored in uri.
        val uri: Uri = Uri.parse("tel:+393285668826")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnInfoWindowClickListener {
            val allWrackers = it.tag as? Models.AllWrackerWorkshops
            allWrackers?.let { it1 ->
                getWrackersServices(it1.id.toString(), it1.usersId.toString(), getSelectedCar()?.carVersionModel?.idVehicle)
                if (it.isInfoWindowShown) {
                    it.hideInfoWindow()
                }
            }
        }
    }

    override fun onInfoWindowClick(p0: Marker?) {
    }

    override fun onStart() {
        super.onStart()
        if (googleApiClient != null) {
            googleApiClient!!.connect()
        }
    }

    private fun getAllWrackerWorkshopAddressInfo() {

        val selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
        RetrofitClient.client.getAllWrackerServices(latitude = mLatitude!!, longitude = mLongitude!!, selectedDate = selectedFormattedDate, type = "1")
                .onCall { networkException, response ->
                    networkException?.let {
                        Log.d("networkException", it.localizedMessage)
                    }
                    response?.let {
                        if (response.isSuccessful) {
                            try {
                                val jsonObject = JSONObject(response.body()?.string())
                                val data_set = if (jsonObject.has("data_set") && jsonObject.get("data_set") != null && !jsonObject.getString("data_set").isNullOrBlank() && !jsonObject.getString("data_set").equals("null")) jsonObject.getJSONArray("data_set") else JSONArray()
                                Log.e("isSuccessful", data_set.toString())
                                if (data_set.length() > 0) {
                                    allWrackerServicesWorkshopList.clear()
                                    for (i in 0 until data_set.length()) {
                                        val data = Gson().fromJson<Models.AllWrackerWorkshops>(data_set.getJSONObject(i).toString(), Models.AllWrackerWorkshops::class.java)
                                        allWrackerServicesWorkshopList.add(data)
                                    }
                                    val data = allWrackerServicesWorkshopList.get(0)
                                    getWrackersServices(data.id.toString(), data.usersId.toString(), getSelectedCar()?.carVersionModel?.idVehicle
                                            ?: "", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            bindAllWrackerServicesOnMap()
                        }
                    }
                }
    }

    private fun bindAllWrackerServicesOnMap() {
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(p0: Marker?): View = View(this@SOSActivity)
            override fun getInfoWindow(p0: Marker?): View {
                val allWrakerService = p0?.tag as? Models.AllWrackerWorkshops
                allWrakerService?.let {
                    //val view = LayoutInflater.from(this@SOSActivity).inflate(R.layout.map_layout,null)
                    val binding: MapLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(this@SOSActivity), R.layout.map_layout, null, false)
                    binding.data = allWrakerService
                    binding.address1.text = if (!allWrakerService.address1.toString().isNullOrBlank() && allWrakerService.address1.toString() != "null") allWrakerService.address1.toString() else "" //allWrakerService.address1.takeIf { !it.isNullOrEmpty() }
                    binding.address2.text = if (!allWrakerService.address2.toString().isNullOrBlank() && allWrakerService.address2.toString() != "null") allWrakerService.address2.toString() else ""
                    binding.mobileNumber.text = if (!allWrakerService.mobileNumber.toString().isNullOrBlank() && allWrakerService.mobileNumber.toString() != "null") allWrakerService.mobileNumber.toString() else ""//allWrakerService.mobileNumber.toString().takeIf { !it.isNullOrEmpty() }
                    binding.workshopName.text = if (!allWrakerService.WorkshopName.toString().isNullOrBlank() && allWrakerService.WorkshopName.toString() != "null") allWrakerService.WorkshopName.toString() else ""//allWrakerService.WorkshopName.toString().takeIf { !it.isNullOrEmpty() }

                    return binding.root
                }
                return View(this@SOSActivity)
            }
        })
        var position: LatLng?
        allWrackerServicesWorkshopList.forEach { listData ->
            listData.let {
                position = LatLng(listData.latitude.toString().toDouble(), listData.longitude.toString().toDouble())
                locationMarker = mMap.addMarker(
                        MarkerOptions()
                                .position(position!!)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
                locationMarker.tag = listData
            }
        }
    }

    private fun getWrackersServices(id: String, userId: String, selectedCarId: String?, showAllServices: Boolean = false) {
        var workshopId = ""
        if (!showAllServices) {
            workshopId = userId
        }

        val progressDialog = getProgressDialog(true)

        RetrofitClient.client.getWrackerServices(id, workshopId, selectedCarId!!.toInt())
                .onCall { networkException, response ->
                    networkException?.let {
                        Log.d("networkException", it.localizedMessage)
                        progressDialog.dismiss()
                    }
                    response?.let {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            try {
                                val jsonObject = JSONObject(response.body()?.string())
                                if (jsonObject.has("data_set") && !jsonObject.isNull("data_set") && !jsonObject.getString("data_set").isNullOrBlank() && !jsonObject.getString("data_set").equals("null")) {
                                    val data_set = jsonObject.getJSONArray("data_set")
                                    wrackersList.clear()
                                    for (i in 0 until data_set.length()) {
                                        val data = Gson().fromJson<Models.WrackerServices>(data_set.getJSONObject(i).toString(), Models.WrackerServices::class.java)

                                        wrackersList.add(data)
                                        main_category_id = wrackersList.get(i).main_category_id
                                        Log.d("wrakerServicesCall", data.id.toString())
                                    }

                                    Log.d("wrakerServicesCall", "sos")

                                } else {
                                    if (jsonObject.has("message") && !jsonObject.isNull("message"))
                                        Snackbar.make(container, getString(R.string.Servicenotavailablepleaseselectotherworkshop), Snackbar.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            bindWrackerServices(userId.toInt())
                        }
                    }
                }
    }

    private fun emergencySOsServicesWorkshops(items: Models.WrackerServices) {
        val selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
        RetrofitClient.client.emergencySosServiceBooking(getBearerToken() ?: "",
                "",
                items.workshopId.toString(),
                getCurrentTime(),
                "",
                selectedFormattedDate,
                mLatitude!!, mLongitude!!,
                items.selectedCarId,
                "",
                items.addressId,
                items.id.toString(),
                getOrderId(), "", "", "", ""
        )
                .onCall { _, response ->

                    response?.let {
                        if (response.isSuccessful) {
                            try {
                                val body = JSONObject(response.body()?.string())
                                if (body.has("message")) {
                                    showInfoDialog(body.optString("message")) {
                                        //check also by status code , if 1 then booking inserted .
                                        if (body.has("status_code") && body.get("status_code") != null) {
                                            if (body.get("status_code") == 1) {

                                                startActivity(intentFor<WorkshopBookingDetailsActivity>().forwardResults())
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
    }

    private fun bindAllSOSServicesWorkshops() {
        // Removes all markers, overlays, and polylines from the map.
        mMap.clear()
        for (i in 0 until latlongList.size) {
            for (j in 0 until latlongList.get(i).address.size) {
                if (i == 0) {
                    val cameraPosition: CameraPosition? = CameraPosition.Builder().target(LatLng(latlongList.get(i).address.get(j).latitude.toString().toDouble(),
                            latlongList.get(i).address.get(j).longitude.toString().toDouble())).zoom(5f).build()
                    with(mMap) {
                        animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
                locationMarker = mMap.addMarker(
                        MarkerOptions()
                                .position(LatLng(latlongList.get(i).address.get(j).latitude.toString().toDouble(), latlongList.get(i).address.get(j).longitude.toString().toDouble()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title(latlongList.get(i).business_name)
                                .snippet(latlongList.get(i).registered_office)
                )
                locationMarker.showInfoWindow()
            }
        }
    }


    private fun bindWrackerServices(userId: Int) {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val images = view.workshop_images
            val name = view.workshop_name
            val workshopEmergency = view.workshop_emergency
            val workshopAppointment = view.workshop_appointment
            val rootview = view.wracker_services_rl
        }
        recycler_view.adapter = object : RecyclerView.Adapter<ViewHolder>() {

            override fun getItemCount(): Int {
                return wrackersList.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val items = wrackersList.get(position)
                holder.name.text = items.servicesName
                loadImage(items.serviceImageUrl, holder.images)

                holder.rootview.setOnClickListener {}
                holder.workshopEmergency.setOnClickListener {
                    /*dialogForEmergency(items)*/
                    navigateToWorkshopList(items, userId, isEmergency = true, isAppointment = false)
                }
                holder.workshopAppointment.setOnClickListener {
                    navigateToWorkshopList(items, userId, isEmergency = false, isAppointment = true)
                }
                holder.name.setOnClickListener {
                    showInfoDialog(items.description)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return ViewHolder(layoutInflater.inflate(R.layout.sos_list_layout, container, false))
            }
        }
    }

    private fun navigateToWorkshopList(items: Models.WrackerServices, userId: Int, isEmergency: Boolean, isAppointment: Boolean) {

        val selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
        startActivity(intentFor<WorkshopListActivity>(
                Constant.Key.is_sos_service to isAppointment,
                Constant.Key.is_sos_service_emergency to isEmergency,
                Constant.Path.serviceID to items.id.toString(),
                Constant.Path.latitude to mLatitude,
                Constant.Path.longitude to mLongitude,
                Constant.Path.workshopUsersId to userId.toString(),
                Constant.Path.addressId to items.addressId,
                Constant.Path.workshopWreckerId to items.workshopWreckerId.toString(),
                Constant.Path.workshopFilterSelectedDate to selectedFormattedDate,
                Constant.Path.mainCategoryId to main_category_id
        ))
    }


    private fun dialogForEmergency(items: Models.WrackerServices) {
        val dialog = Dialog(this)
        val binding: SosEmergencyPopupWindowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.sos_emergency_popup_window, null, false)
        binding.wrackerServices = items
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(binding.root)
        val window: Window = dialog.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        setSOSListener(object : SOSActivityListener {
            override fun onClickEvent(flag: Boolean) {
                if (flag) emergencySOsServicesWorkshops(items)
                dialog.dismiss()
            }
        }, items)
        dialog.show()
    }

    interface SOSActivityListener {
        fun onClickEvent(flag: Boolean)
    }

    private fun setSOSListener(sosActivityListener: SOSActivityListener, items: Models.WrackerServices) {
        this.sosActivityListener = sosActivityListener
        sosActivityListener.let {
            items.sosActivityListener = it
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1000) {

            when (resultCode) {
                0 -> finish()
                -1 -> getcurrentlocation()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("MissingPermission")
    private fun getcurrentlocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient?.lastLocation?.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            Log.e("latitudecurrentlocation", location.toString() + "location")

                val langCode = getSharedPreferences(Constant.Key.currentLatLong, Context.MODE_PRIVATE)
                val UserSavedLatitude = langCode.getString(Constant.Path.latitude, "0.0")
                val UserSavedLogitude = langCode.getString(Constant.Path.longitude, "0.0")
            Log.d("check_location_data",""+UserSavedLatitude+"   "+UserSavedLogitude)

            if (!UserSavedLatitude.isNullOrBlank() && !UserSavedLogitude.isNullOrBlank() && UserSavedLatitude != "0.0" && UserSavedLogitude != "0.0" &&isLoggedIn()) {
                    mLatitude = UserSavedLatitude
                    mLongitude = UserSavedLogitude
                    // currentLatLong = LatLng(UserSavedLatitude.toDouble(), UserSavedLogitude.toDouble())
                } else {

                    if (location == null) {
                        requestNewLocationData()
                    }else {
                   mLatitude = location.latitude.toString()//"44.1571507"
                   mLongitude = location.longitude.toString()//"12.2142107"
              /*      mLatitude = "44.1571507"
                     mLongitude = "12.2142107"*/
                }
                loadMapView()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            Log.e("latitudemLastLocation", mLastLocation.latitude.toString())
            mLatitude = mLastLocation.latitude.toString()
            mLongitude = mLastLocation.longitude.toString()
            loadMapView()
}
}

@SuppressLint("MissingPermission")
private fun loadMapView() {
    mMap.isMyLocationEnabled = true
    if (mLatitude != null && mLongitude != null) {
        val myLocation = LatLng(mLatitude!!.toDouble(),
                mLongitude!!.toDouble())
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10f))
        mMap.addMarker(MarkerOptions()
                .position(myLocation))
    }

    if (isOnline()) {
        getAllWrackerWorkshopAddressInfo()
    } else {
        showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
    }
}
}

