package com.officinetop.officine

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.novoda.merlin.Merlin
import com.officinetop.officine.data.*
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_product_list.*

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private lateinit var connectionCallback: Merlin
    private var currentLatLong: LatLng? = LatLng(0.0, 0.0)
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var googleApiClient: GoogleApiClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionCallback = Merlin.Builder().withConnectableCallbacks()
                .withDisconnectableCallbacks().build(this)
        connectionCallback.registerDisconnectable {
            showInfoDialog(getString(R.string.Connection_Error))
        }
        if (getLangLocale() != "") {
            setAppLanguage()
        } else {
            storeLangLocale("it")
            setAppLanguage()
        }
    }

    @Override
    fun getMerlinConnectionCallback() = connectionCallback


    override fun onResume() {

        connectionCallback.bind()
        super.onResume()
    }

    override fun onPause() {
        connectionCallback.unbind()
        super.onPause()
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> finish()

        }
        return super.onOptionsItemSelected(item)
    }


    private fun getFusedLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("getFusedLocation", "Location Permission from sucess")
            mLocationRequest = LocationRequest()
            mLocationRequest?.interval = 150000
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

        }
    }


    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val locationList = locationResult?.locations
            if (locationList != null && locationList.size > 0) {
                val latestLocation = locationList[locationList.size - 1]
                // add marker


                currentLatLong = LatLng(latestLocation.latitude, latestLocation.longitude)
                //currentLatLong = LatLng(44.186516, c)
               // currentLatLong = LatLng(44.186516, 12.2142107)
                val langCode = getSharedPreferences(Constant.Key.usertLatLong, Context.MODE_PRIVATE)
                val UserSavedLatitude = langCode.getString(Constant.Path.latitude, "0.0")
                val UserSavedLogitude = langCode.getString(Constant.Path.longitude, "0.0")
                if (!UserSavedLatitude.isNullOrBlank() && !UserSavedLogitude.isNullOrBlank() && UserSavedLatitude != "0.0" && UserSavedLogitude != "0.0")
                    storeLatLong(UserSavedLatitude.toDouble(), UserSavedLogitude.toDouble())
                else {
                    if (currentLatLong?.latitude != 0.0 && currentLatLong?.longitude != 0.0) {
                        storeLatLong(currentLatLong!!.latitude, currentLatLong!!.longitude)
                    }

                }

            }
        }
    }

    fun enableLocation() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build()
            googleApiClient!!.connect()
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 200 * 1000
            locationRequest.fastestInterval = 30 * 1000
            val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback {
                val status: Status = it.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        getFusedLocation()
                        Log.d("WorkshopList", "Location Permission from success")
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            status.startResolutionForResult(this@BaseActivity, 1000)
                            Log.d("WorkshopList", "Location Permission RESOLUTION_REQUIRED")
                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Log.d("WorkshopList", "Location Permission SETTINGS_CHANGE_UNAVAILABLE")
                        workshop_container.snack(getString(R.string.settings_change_not_allowed))
                    }

                }
            }
        }
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            getFusedLocation()
            Log.d("WorkshopList", "Location Permission sucess2")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getLocation() {

        if (getLat().isNullOrBlank() || getLat().equals("0") || getLat().equals("0.0") || getLong().isNullOrBlank() || getLong().equals("0") || getLong().equals("0.0") || !isUserSavedAddress()) {
            checkpermission(storagePermissionRequestList(), { enableLocation() }, true)
        }
    }

}