package com.officinetop.misc_activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.json.JSONObject


class LocationActivity : BaseActivity() {
    var Address = ""
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    var container: ConstraintLayout? = null
    private var geoCoder: Geocoder? = null
    private var addressList: MutableList<Address> = ArrayList()
    private var latitude: String? = null
    private var longitude: String? = null
    private var completeAddress: String? = null
    private var zipCode: String? = null
    private var cityName: String? = null
    private var countryName: String? = null
    private var stateName: String? = null
    private var streetName: String? = null
    private var thoroughfare: String? = null
    private var isAutomaticLocationSave = false
    var manual_location = false


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        setSupportActionBar(toolbar)
        toolbar_title.text = resources.getString(R.string.user_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

       // checkpermission(storagePermissionRequestList(), { setPlacePicker() }, true)
        setPlacePicker()
        disableTextField()
        var address_data :ArrayList<String>  = getAdress()

        if(!isLoggedIn()){
             prov.setText(address_data.get(0))
             cap.setText(address_data.get(1))
             citta.setText(address_data.get(2))
             via.setText(address_data.get(3))
             complete_address.visibility = View.VISIBLE
             complete_address.setText(address_data.get(5))
             location.visibility = View.VISIBLE
             location.text = "Lat: " +getLat() + ", Long: " + getLong()
      }
        /*else if(isLoggedIn() &&address_data.get(4).equals("true")){
          prov.setText(address_data.get(0))
          cap.setText(address_data.get(1))
          citta.setText(address_data.get(2))
          via.setText(address_data.get(3))
          saveLocation(address_data.get(4))
      }*/

        getSavedUserLocation()

        locationBtn.setOnClickListener {

          if(!manual_location) {
              if (isLocationEnabled(this)) {
                  isAutomaticLocationSave = true
                  currentLocation(true)
                  getcurrentLocation()
              } else {
                  showInfoDialog(resources.getString(R.string.alert_message_location))
              }
          }


        }

        aggioraBtn.setOnClickListener {
            if(!isLoggedIn()){
                applicationContext.storeAddress(stateName,zipCode,cityName,thoroughfare+", "+"${streetName}",completeAddress,"true")
            }
            storeLatLong(latitude?.toDouble()!!, longitude?.toDouble()!!, true)
            saveLocation()
        }
    }

    private fun getSavedUserLocation() {
/*
        applicationContext.storeAddress(stateName,zipCode,cityName,dataModels.address1)
*/


        RetrofitClient.client.getUserSavedAddress(getBearerToken()
                ?: "")
                .onCall { networkException, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val bodystring = response.body()?.string()
                            if (isStatusCodeValid(bodystring)) {
                                val jsonObject = JSONObject(bodystring)

                                if (jsonObject.has("data") && !jsonObject.getString("data").isNullOrBlank()) {

                                    val dataModels = Gson().fromJson<Models.UserAddres>(jsonObject.getString("data").toString(), Models.UserAddres::class.java)
                                    prov.setText(dataModels.stateName ?: "")
                                    cap.setText(dataModels.countryName ?: "")
                                    citta.setText(dataModels.cityName ?: "")
                                    via.setText(dataModels.address1)
                                    location.text = "Lat: " + dataModels.latitude + ", Long: " + dataModels.longitude
                                    completeAddress = dataModels.address1


                                    complete_address.visibility = View.VISIBLE
                                    complete_address.text = completeAddress

                                    latitude = if (dataModels.latitude.isNullOrBlank() || dataModels.latitude.equals("null")) "0" else dataModels.latitude
                                    longitude = if (dataModels.longitude.isNullOrBlank() || dataModels.longitude.equals("null")) "0" else dataModels.longitude
                                    zipCode = dataModels.zipCode ?: ""
                                    disableTextField()
                                    logFindLocationEvent(this)
                                    if (!latitude.isNullOrBlank() && !latitude.equals("null") && !longitude.isNullOrBlank() && !longitude.equals("null")) {
                                        storeLatLong(latitude!!.toDouble(), longitude!!.toDouble(), true)

                                    } else {
                                   //     storeLatLong(00.0, 0.0)

                                    }

                                } else {
                                    getcurrentLocation()
                                 //   storeLatLong(0.0, 0.0)
                                }

                            } else {
                                getcurrentLocation()

                              //  storeLatLong(0.0, 0.0)
                            }

                        } else {


                        }

                    }
                }
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
                            setLocationInView(mLastLocation!!.latitude, mLastLocation!!.longitude)


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.exception)
                        showMessage(getString(R.string.no_location_detected))
                    }
                }
    }


    private fun showMessage(text: String) {
        if (container != null) {
            Toast.makeText(this@LocationActivity, text, Toast.LENGTH_LONG).show()
        }
    }


    private fun showSnackbar(mainTextStringId: Int) {

        //Toast.makeText(this@LocationActivity, getString(mainTextStringId), Toast.LENGTH_LONG).show()
        container?.snack(getString(mainTextStringId))

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == Constant.REQUEST_PERMISSIONS_LOCATION) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            } else {
                // Permission denied.


                showSnackbar(R.string.permission_denied_explanation)
            }
        }
    }

    companion object {

        private const val TAG = "LocationProvider"


    }

    private var selectedPlace: Place? = null
    private var placePickerFragment: AutocompleteSupportFragment? = null
    private fun setPlacePicker() {

        placePickerFragment = supportFragmentManager.setPlacePicker(this@LocationActivity) { place, error ->
            selectedPlace = place
            latitude = place?.latLng?.latitude.toString()
            longitude = place?.latLng?.longitude.toString()
            Address = place?.address.toString()
            if (place != null) {
                isAutomaticLocationSave = false
                currentLocation(false)
                setLocationInView(place.latLng?.latitude, place.latLng?.longitude)


                if (error != null)
                    recycler_view.snack(getString(R.string.Failed_pickplace))
            }
        }


    }


    private fun setLocationInView(lat: Double?, long: Double?) {
        geoCoder = Geocoder(this, getLocale())
        addressList = geoCoder!!.getFromLocation(lat!!, long!!, 5)
        latitude = lat.toString()
        longitude = long.toString()
        Log.d("user current location: ", "${latitude} ${longitude}")
        location.text = "lat: $latitude, long:$longitude"


        completeAddress = addressList.get(0).getAddressLine(0)
        cityName = addressList.get(0).locality.takeIf { !it.isNullOrEmpty() }
        stateName = addressList.get(0).adminArea.takeIf { !it.isNullOrEmpty() }
        countryName = addressList.get(0).countryName.takeIf { !it.isNullOrEmpty() }
        if (addressList.get(0).postalCode !== null) {
            zipCode = addressList.get(0).postalCode.takeIf { !it.isNullOrEmpty() }
        } else {
            zipCode = ""
        }

        streetName = addressList.get(0).featureName.takeIf { !it.isNullOrEmpty() }
        val street = addressList.get(0).subLocality.takeIf { !it.isNullOrEmpty() }
        val subArea = addressList.get(0).subAdminArea.takeIf { !it.isNullOrEmpty() }
        val countryCode = addressList.get(0).countryCode.takeIf { !it.isNullOrEmpty() }
        val extras = addressList.get(0).extras != null
        val locale = addressList.get(0).locale.toString().takeIf { !it.isEmpty() }
        val phone = addressList.get(0).phone.takeIf { !it.isNullOrEmpty() }
        val premises = addressList.get(0).premises.takeIf { !it.isNullOrEmpty() }
        val subThoroughfare = addressList.get(0).subThoroughfare.takeIf { !it.isNullOrEmpty() }
         thoroughfare = addressList.get(0).thoroughfare.takeIf { !it.isNullOrEmpty() }
        val url = addressList.get(0).url.takeIf { !it.isNullOrEmpty() }

       Log.e("subarea:", ""+geoCoder!!.getFromLocation(lat!!, long!!, 5))

        manual_location = true

        complete_address.visibility = View.VISIBLE
        complete_address.text = completeAddress

        prov.setText(stateName)
        cap.setText(zipCode)
        citta.setText(cityName)
        via.setText(thoroughfare+", "+"${streetName}")
        disableTextField()
        if (isAutomaticLocationSave) {
            applicationContext.storeAddress(stateName,zipCode,cityName,thoroughfare+", "+"${streetName}",completeAddress,"true")
            storeLatLong(latitude?.toDouble()!!, longitude?.toDouble()!!, true)
            saveLocation()
        }
    }

    private fun getcurrentLocation() {
        checkpermission(storagePermissionRequestList(), { getLastLocation() }, true)


    }

    private fun disableTextField() {
        prov.isEnabled = false
        cap.isEnabled = false
        citta.isEnabled = false
        via.isEnabled = false
    }

    private fun saveLocation() {
        if (isEditTextValid(this@LocationActivity, prov, cap, citta, via)) {
         /*   latitude = getLat()
            longitude =getLong()*/
            completeAddress = via.text.toString() + " " + zipCode + " " + citta.text.toString() + " " + prov.text.toString() + " " + cap.text.toString()
            RetrofitClient.client.saveUserLocation(getBearerToken()
                    ?: "", latitude, longitude, "", completeAddress, "", "",
                    via.text.toString(), zipCode, cap.text.toString(), prov.text.toString(), citta.text.toString())
                    .onCall { networkException, response ->
                        response?.let {
                            if (response.isSuccessful) {
                                val jsonObject = JSONObject(response.body()?.string())
                                if (jsonObject.has("status_code") && jsonObject.optString("status_code") == "1" && jsonObject.has("message")) {
                                    var address_data :ArrayList<String>  = getAdress()
                                    applicationContext.storeAddress(address_data.get(0),address_data.get(1),address_data.get(2),address_data.get(3),address_data.get(5),"false")
                                    showInfoDialog(jsonObject.optString("message")) {

                                        finish()
                                        }
                                }
                            } else if (response.code() == 401) {
                                storeLatLong(latitude?.toDouble()!!, longitude?.toDouble()!!, true)
                                showInfoDialog("successFully Saved") {
                                    finish()
                                }
                            }
                        }
                    }
        }



    }
}