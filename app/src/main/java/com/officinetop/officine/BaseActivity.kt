package com.officinetop.officine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.novoda.merlin.Merlin
import com.officinetop.officine.data.getLangLocale
import com.officinetop.officine.data.storeLangLocale
import com.officinetop.officine.data.storeLatLong
import com.officinetop.officine.utils.setAppLanguage
import com.officinetop.officine.utils.showInfoDialog
import com.officinetop.officine.utils.snack
import kotlinx.android.synthetic.main.activity_product_list.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    lateinit var connectionCallback: Merlin
    private var LOCATION_RQ = 10001
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
        if (getLangLocale() != null && !getLangLocale().equals("")) {
            setAppLanguage()
        } else {
            storeLangLocale("it")
            setAppLanguage()
        }

        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = 150000
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkRequestLocationPermission()
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

            R.id.item_home_options -> {

                var menuBuilder = MenuBuilder(this)
                var menuInflater = MenuInflater(this)
                menuInflater.inflate(R.menu.menu_navigation_options, menuBuilder)

                var menuPopUpHelper = MenuPopupHelper(this, menuBuilder, findViewById(R.id.item_home_options))
                menuPopUpHelper.setForceShowIcon(true)

                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                        startActivity(intentFor<HomeActivity>("fragmentID" to item?.itemId))
                        finishAffinity()

                        return true
                    }

                    override fun onMenuModeChange(menu: MenuBuilder) {}
                })

                menuPopUpHelper.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun getFusedLocation() {

        mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

    }

    private fun checkRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_RQ)
        }
    }

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            var locationList = locationResult?.locations
            if (locationList != null && locationList.size > 0) {
                val latestLocation = locationList[locationList.size - 1]
                // add marker
                 currentLatLong = LatLng(latestLocation.latitude, latestLocation.longitude)
              // currentLatLong = LatLng(44.186516, 12.1662333)
                if(currentLatLong?.latitude!=0.0 && currentLatLong?.longitude!=0.0  ){
                    storeLatLong(currentLatLong!!.latitude,currentLatLong!!.longitude)
                }


            }
        }
    }

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
                        getFusedLocation()
                        Log.d("WorkshopList", "Location Permission from sucess")


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


}