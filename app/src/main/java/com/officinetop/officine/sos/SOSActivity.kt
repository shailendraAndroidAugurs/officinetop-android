package com.officinetop.officine.sos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
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
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.WorkshopListActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.databinding.MapLayoutBinding
import com.officinetop.officine.databinding.SosEmergencyPopupWindowBinding
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.workshop.WorkshopBookingDetailsActivity
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

    private var mLastLocation: Location? = null

    private var mLatitude: String? = null
    private var mLongitude: String? = null

    private lateinit var locationMarker: Marker

    private var latlongList: MutableList<Models.SOSWorkshop> = ArrayList()

    private var allWrackerServicesWorkshopList: MutableList<Models.AllWrackerWorkshops> = ArrayList()

    private var wrackersList: MutableList<Models.WrackerServices> = ArrayList()
    private var sosActivityListener: SOSActivityListener? = null

    private var isLocationOn: Boolean = false

    private var googleApiClient: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.title_activity_sos)


        //check location permission.


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //this.infoWindow = layoutInflater.inflate(R.layout.)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (hasLocationPermission()) {

            if (isLocationOn)
                getcurrentlocation() else
                enableLocation()

        }

        emergency_call.setOnClickListener {
            callEmergency()
        }

    }


    private fun callEmergency() {
        // Use format with "tel:" and phoneNumber created is
        // stored in uri.
        val uri: Uri = Uri.parse("tel:+0012345678998")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)

    }

    private fun getPixelsFromDp(context: Context, dp: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return ((dp * scale + 0.5f).toInt())
    }


    /**
     * enable location
     */

    private fun enableLocation() {

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build()
            googleApiClient!!.connect()
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationRequest.setInterval(10 * 1000)
            locationRequest.setFastestInterval(2 * 1000)
            val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)


            val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback {
                val status: Status = it.status
                val state: LocationSettingsStates = it.locationSettingsStates
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        isLocationOn = true
                        //container.snack(getString(R.string.success))
                        getcurrentlocation()

                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        container.snack(getString(R.string.gps_not_on))
                        try {
                            status.startResolutionForResult(this@SOSActivity, 1000)
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mMap.setOnInfoWindowClickListener {

            val allWrackers = it.tag as? Models.AllWrackerWorkshops

            allWrackers?.let {
                getWrackersServices(it.id.toString(), it.usersId.toInt(), getSavedSelectedVehicleID())
            }
        }
    }

    override fun onInfoWindowClick(p0: Marker?) {

    }


    /* private fun getLastLocation() {
         mFusedLocationClient!!.lastLocation
                 .addOnCompleteListener(this) { task ->
                     if (task.isSuccessful && task.result != null) {

                         mLastLocation = task.result

                         mLatitude = mLastLocation!!.latitude.toString()
                         mLongitude = mLastLocation!!.longitude.toString()

                         //container.snack(getString(R.string.location_enabled))

                         getAllWrackerWorkshopAddressInfo()

                         Log.d("user current location: ", "${mLastLocation!!.latitude} ${mLastLocation!!.longitude}")

                         mMap.isMyLocationEnabled = true

                         if (mLatitude != null && mLongitude != null) {
                             var myLocation = LatLng(mLatitude!!.toDouble(),
                                     mLongitude!!.toDouble());
                             var center: CameraUpdate =
                                     CameraUpdateFactory.newLatLng(myLocation);
                             var zoom: CameraUpdate = CameraUpdateFactory.zoomTo(13.0F);

                             mMap.moveCamera(center);
                             mMap.animateCamera(zoom);
                         }


                     } else {
                         Log.w("SOSActivity", "getLastLocation:exception", task.exception)

                         //container.snack(getString(R.string.no_location_detected))
                         //enableLocation()
                     }
                 }
     }*/

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
                                val jsonObject = JSONObject(response.body()?.string()) as JSONObject
                                val data_set = if (jsonObject.has("data_set") && jsonObject.get("data_set") != null) jsonObject.getJSONArray("data_set") else JSONArray()

                                if (data_set.length() > 0) {
                                    for (i in 0 until data_set.length()) {
                                        val data = Gson().fromJson<Models.AllWrackerWorkshops>(data_set.getJSONObject(i).toString(), Models.AllWrackerWorkshops::class.java)
                                        allWrackerServicesWorkshopList.add(data)
                                        //val data = Gson().fromJson<Models.SOSWorkshop>(data_set.getJSONObject(i).toString(), Models.SOSWorkshop::class.java)
                                        //latlongList.add(data)
                                    }
                                    val data = allWrackerServicesWorkshopList.get(0) as Models.AllWrackerWorkshops
                                    getWrackersServices(data.id.toString(), data.usersId.toInt(), getSavedSelectedVehicleID())
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
                    binding.address1.text = if (!allWrakerService.address1.toString().isNullOrBlank() && !allWrakerService.address1.toString().equals("null")) allWrakerService.address1.toString() else "" //allWrakerService.address1.takeIf { !it.isNullOrEmpty() }
                    binding.address2.text = if (!allWrakerService.address2.toString().isNullOrBlank() && !allWrakerService.address2.toString().equals("null")) allWrakerService.address2.toString() else ""
                    binding.mobileNumber.text = if (!allWrakerService.mobileNumber.toString().isNullOrBlank() && !allWrakerService.mobileNumber.toString().equals("null")) allWrakerService.mobileNumber.toString() else ""//allWrakerService.mobileNumber.toString().takeIf { !it.isNullOrEmpty() }
                    binding.workshopName.text = if (!allWrakerService.WorkshopName.toString().isNullOrBlank() && !allWrakerService.WorkshopName.toString().equals("null")) allWrakerService.WorkshopName.toString() else ""//allWrakerService.WorkshopName.toString().takeIf { !it.isNullOrEmpty() }

                    return binding.root
                }
                return View(this@SOSActivity)
            }
        })

        var position: LatLng?
        allWrackerServicesWorkshopList.forEach { listData ->
            listData?.let {
                position = LatLng(listData.latitude.toString().toDouble(), listData.longitude.toString().toDouble())
/*
                with(mMap) {
                    animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                }*/
                locationMarker = mMap.addMarker(
                        MarkerOptions()
                                .position(position!!)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                )

                locationMarker.tag = listData
            }
        }
    }

    private fun getWrackersServices(id: String, userId: Int, selectedCarId: String?) {
        RetrofitClient.client.getWrackerServices(id, userId, selectedCarId!!.toInt())
                .onCall { networkException, response ->
                    networkException?.let {
                        Log.d("networkException", it.localizedMessage)
                    }
                    response?.let {
                        if (response.isSuccessful) {
                            try {
                                val jsonObject = JSONObject(response.body()?.string())
                                if (jsonObject.has("data_set") && !jsonObject.isNull("data_set")) {
                                    val data_set = jsonObject.getJSONArray("data_set")
                                    for (i in 0 until data_set.length()) {
                                        val data = Gson().fromJson<Models.WrackerServices>(data_set.getJSONObject(i).toString(), Models.WrackerServices::class.java)
                                        if (wrackersList.size == 0) {
                                            wrackersList.add(data)
                                        } else {
                                            if (!wrackersList.contains(data)) {
                                                wrackersList.add(data)
                                            }
                                        }

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
                            bindWrackerServices(userId)
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
                getOrderId()
        )
                .onCall { _, response ->

                    response?.let {
                        if (response.isSuccessful) {
                            try {
                                val body = JSONObject(response?.body()?.string())
                                if (body.has("message")) {
                                    showInfoDialog(body.optString("message")) {
                                        //check also by status code , if 1 then booking inserted .
                                        if (body.has("status_code") && body.get("status_code") != null) {
                                            if (body.get("status_code").equals(1)) {
                                                val serviceModel: Models.ServiceBookingModel = Models.ServiceBookingModel(0, getCurrentTime(), "", 0.0, "SOS Service", Gson().toJson(items).toString(), selectedFormattedDate)
                                                val cartItem = Models.CartItem(serviceModel = serviceModel)
                                                cartItem.type = Constant.type_workshop
                                                setCartItem(cartItem)
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
                    navigateToWorkshopList(items, userId, true, false)
                }
                holder.workshopAppointment.setOnClickListener {
                    navigateToWorkshopList(items, userId, false, true)
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
                Constant.Path.workshopFilterSelectedDate to selectedFormattedDate
        ))
    }


    private fun dialogForEmergency(items: Models.WrackerServices) {
        val dialog = Dialog(this)
        val binding: SosEmergencyPopupWindowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.sos_emergency_popup_window, null, false)
        binding.wrackerServices = items
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(binding.root)
        val window: Window = dialog!!.window!!
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

    fun setSOSListener(sosActivityListener: SOSActivityListener, items: Models.WrackerServices) {
        this.sosActivityListener = sosActivityListener
        sosActivityListener.let {
            items.sosActivityListener = it
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == Constant.REQUEST_PERMISSIONS_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted.
            enableLocation()
            //  getLastLocation()

        } else {
            // Permission denied.
            container.snack(getString(R.string.permission_denied_explanation), duration = Snackbar.LENGTH_INDEFINITE) {
                if (hasLocationPermission()) getcurrentlocation()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    fun getcurrentlocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient?.lastLocation?.addOnCompleteListener(this) { task ->
            var location: Location? = task.result
            Log.e("latitudecurrentlocation", location.toString() + "location")
            if (location == null) {
                requestNewLocationData()
            } else {
              mLatitude = location!!.latitude.toString()
             mLongitude = location!!.longitude.toString()
                /* mLatitude = "44.186516"
                 mLongitude ="12.1662333"*/
                loadmapview()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
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
            var mLastLocation: Location = locationResult.lastLocation
            Log.e("latitudemLastLocation", mLastLocation?.latitude.toString())
            mLatitude = mLastLocation!!.latitude.toString()
            mLongitude = mLastLocation!!.longitude.toString()
            loadmapview()
        }
    }

    private fun loadmapview() {
        mMap.isMyLocationEnabled = true
        if (mLatitude != null && mLongitude != null) {
            var myLocation = LatLng(mLatitude!!.toDouble(),
                    mLongitude!!.toDouble())
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10f))
            mMap?.addMarker(MarkerOptions()
                    .position(myLocation))
        }
        Log.e("latitudeloadmapview", mLatitude.toString() + "location")
        getAllWrackerWorkshopAddressInfo()
    }
}

