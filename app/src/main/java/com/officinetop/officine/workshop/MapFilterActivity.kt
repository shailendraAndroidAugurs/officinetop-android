package com.officinetop.officine.workshop

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.loadImageWithName
import kotlinx.android.synthetic.main.activity_map_filter.*
import kotlinx.android.synthetic.main.custom_marker_mapfilter.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

class MapFilterActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleMap: GoogleMap? = null
    private var mMapMarker: Marker? = null
    private var mSupportMapFragment: SupportMapFragment? = null
    private var LOCATION_RQ = 10001
    private var WorkShopJSonArray: JSONArray? = null
    private var latLngArray: ArrayList<LatLng> = ArrayList()
    private var isSOSAppointment = false
    private var isMotService = false
    private var isQuotes = false
    private var isCarWash = false

    private var isAssembly = false
    private var cartItem: Models.CartItem? = null

    private var isCarMaintenanceServices = false
    private var mIsWorkshop = false
    private var mIsRevision = false
    private var mIsTyre = false
    private var motservices_time = ""
    private var partidhasMap: java.util.HashMap<String, Models.servicesCouponData> = java.util.HashMap<String, Models.servicesCouponData>()
    private var motpartlist: java.util.HashMap<String, Models.MotservicesCouponData> = java.util.HashMap<String, Models.MotservicesCouponData>()

    private var selectedFormattedDate = ""
    private var motType = ""
    private var calendarPriceMap: HashMap<String, String> = HashMap()
    private var workshopCategoryDetail = ""

    // set location callback
    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val locationList = locationResult?.locations
            if (locationList != null && locationList.size > 0) {
                val latestLocation = locationList[locationList.size - 1]
                // add marker
                var currentLatLong = LatLng(0.0, 0.0)
                val langCode = getSharedPreferences(Constant.Key.usertLatLong, Context.MODE_PRIVATE)
                val UserSavedLatitude = langCode.getString(Constant.Path.latitude, "0.0")
                val UserSavedLogitude = langCode.getString(Constant.Path.longitude, "0.0")
                if (!UserSavedLatitude.isNullOrBlank() && !UserSavedLogitude.isNullOrBlank() && UserSavedLatitude != "0.0" && UserSavedLogitude != "0.0") {
                    currentLatLong = LatLng(UserSavedLatitude.toDouble(), UserSavedLogitude.toDouble())
                } else {
                    currentLatLong = LatLng(latestLocation.latitude, latestLocation.longitude)
                }

                mGoogleMap?.addMarker(MarkerOptions()
                        .position(currentLatLong)/*.icon(BitmapDescriptorFactory.fromBitmap(
                                createDrawableFromView(this@MapFilterActivity, "25")))*/
                        .title("Officine"))

                mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 11.0f))

                mGoogleMap?.setOnMarkerClickListener(this@MapFilterActivity)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_filter)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.Filter_Workshops)


        if (!intent.extras!!.getString("WorkshopList").isNullOrBlank()) {
            val data = intent.extras!!.getString("WorkshopList")
            WorkShopJSonArray = JSONArray(data)

        }

        if (!intent.extras!!.getString("selectedFormattedDate").isNullOrBlank()) {
            selectedFormattedDate = intent!!.extras!!.getString("selectedFormattedDate").toString()


        }
        if (!intent.extras!!.getString("mot_type").isNullOrBlank()) {
            motType = intent!!.extras!!.getString("mot_type").toString()


        }




        if (!intent!!.extras!!.getString("workshopCategoryDetail").isNullOrBlank()) {
            workshopCategoryDetail = intent!!.extras!!.getString("workshopCategoryDetail").toString()


        }

        if (intent!!.extras!!.getSerializable("cartItem") != null) {
            if (intent!!.extras!!.getSerializable("cartItem") as Models.CartItem != null) {
                cartItem = intent!!.extras!!.getSerializable("cartItem") as Models.CartItem?


            }
        }




        if (intent!!.extras!!.getBoolean("isSOSAppointment")) {
            isSOSAppointment = intent!!.extras!!.getBoolean("isSOSAppointment")


        }
        if (intent!!.extras!!.getBoolean("isMotService")) {
            isMotService = intent!!.extras!!.getBoolean("isMotService")


        }
        if (intent.extras!!.getBoolean("isQuotes")) {
            isQuotes = intent.extras!!.getBoolean("isQuotes")


        }
        if (intent.extras!!.getBoolean("isAssembly")) {
            isAssembly = intent.extras!!.getBoolean("isAssembly")


        }





        if (intent!!.extras!!.getBoolean("isCarMaintenanceServices")) {
            isCarMaintenanceServices = intent!!.extras!!.getBoolean("isCarMaintenanceServices")


        }
        if (intent!!.extras!!.getBoolean("mIsWorkshop")) {
            mIsWorkshop = intent!!.extras!!.getBoolean("mIsWorkshop")


        }
        if (intent!!.extras!!.getBoolean("mIsRevision")) {
            mIsRevision = intent!!.extras!!.getBoolean("mIsRevision")


        }
        if (intent.extras!!.getBoolean("mIsTyre")) {
            mIsTyre = intent.extras!!.getBoolean("mIsTyre")


        }
        if (!intent.extras!!.getString("motservicesTime").isNullOrBlank()) {
            motservices_time = intent.extras!!.getString("motservices_time", "")


        }
        if (!intent.extras!!.getString("calendarPriceMap").isNullOrBlank()) {
            calendarPriceMap = intent.extras!!.getSerializable("calendarPriceMap") as HashMap<String, String>


        }
        if (intent.extras!!.getSerializable("partidhasMap") as java.util.HashMap<String, Models.servicesCouponData> != null) {
            partidhasMap = intent.extras!!.getSerializable("partidhasMap") as java.util.HashMap<String, Models.servicesCouponData>


        }
        if (intent.extras!!.get("motpartlist") as java.util.HashMap<String, Models.MotservicesCouponData> != null) {
            motpartlist = intent.extras!!.getSerializable("motpartlist") as java.util.HashMap<String, Models.MotservicesCouponData>


        }

        if (intent!!.extras!!.getBoolean("isCarWash")) {
            isCarWash = intent!!.extras!!.getBoolean("isCarWash")


        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSupportMapFragment = supportFragmentManager.findFragmentById(R.id.map_filter_view) as SupportMapFragment?

        mSupportMapFragment?.let {
            it.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mGoogleMap = googleMap

            // create location request
            mLocationRequest = LocationRequest()
            mLocationRequest?.interval = 150000

            checkRequestLocationPermission()

            for (n in 0 until WorkShopJSonArray!!.length()) {
                val jsondata: JSONObject = (WorkShopJSonArray?.get(n)) as JSONObject
                if (jsondata != null) {
                    val latitude: String = jsondata.getDouble("latitude").toString()
                    val longitude: String = jsondata.getDouble("longitude").toString()
                    var workshopPrices = jsondata.get("services_price")

                    if (workshopPrices == null || workshopPrices == "null") {
                        workshopPrices = "0.0"
                    }
                    if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank() && latitude != "null" && longitude != "null" && latitude != "0.0" && longitude != "0.0") {
                        mGoogleMap?.addMarker(MarkerOptions()
                                .position(LatLng(latitude.toDouble(), longitude.toDouble())).icon(BitmapDescriptorFactory.fromBitmap(
                                        createDrawableFromView(this@MapFilterActivity, workshopPrices.toString())))
                                .title("Officine "))?.tag = WorkShopJSonArray?.get(n)
                    }

                }


            }
        }
    }

    private fun checkRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getFusedLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_RQ)
        }
    }

    private fun getFusedLocation() {
        mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        mGoogleMap?.isMyLocationEnabled = true
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker?.tag != null) {
            val jsonObject = JSONObject(marker.tag.toString())

            workshop_name.text = jsonObject.optString("company_name")
            workshop_description.text = jsonObject.optString("registered_office")
            loadImageWithName(jsonObject.optString("profile_image"), item_image, R.drawable.no_image_placeholder, baseURL = Constant.profileBaseUrl)

            // loadImage(jsonObject.optString("profile_image_url"), item_image)
            if (!jsonObject.optString("services_price").isNullOrEmpty())
                item_price.text = getString(R.string.prepend_euro_symbol_with_da_string, jsonObject.optString("services_price"))


            /*  if (!jsonObject.optString("rating_star").isNullOrEmpty() && !jsonObject.optString("rating_star").equals("null")) {
                  item_rating.rating = jsonObject.optString("rating_star").toFloat()
              } else
                  item_rating.rating = 0f*/



            if (jsonObject.has("rating")) {
                val jsonObjectRating = JSONObject(jsonObject.optString("rating"))
                if (jsonObjectRating != null && jsonObjectRating.has("rating") && !jsonObjectRating.optString("rating").isNullOrEmpty() && jsonObjectRating.optString("rating") != "null") {
                    item_rating.rating = jsonObjectRating.optString("rating").toFloat()
                } else
                    item_rating.rating = 0f

            }





            if (!jsonObject.optString("rating_count").isNullOrEmpty() && jsonObject.optString("rating_count") != "null") {
                item_rating_count.text = jsonObject.optString("rating_count")
            } else
                item_rating_count.text = ""

            var couponList: List<Models.Coupon>? = null
            if (jsonObject.has("coupon_list") && jsonObject.getString("coupon_list") != null && jsonObject.getString("coupon_list") != "null") {
                val jsonArray = JSONArray(jsonObject.getString("coupon_list")).toString()
                val gson = GsonBuilder().create()
                couponList = gson.fromJson(jsonArray.toString(), Array<Models.Coupon>::class.java).toCollection(java.util.ArrayList<Models.Coupon>())


            }

            if (couponList != null && couponList.size != 0) {
                //   workshopCouponId = productOrWorkshopList[p1].couponList[0].id.toString()

                tv_AppliedCoupon.text = (couponList[0].couponTitle)
                tv_AppliedCoupon.visibility = View.VISIBLE
                tv_couponLabel.visibility = View.VISIBLE
                offer_badge.visibility = /*if (p1 % 2 == 0)*/ View.VISIBLE /*else View.GONE*/
            } else {
                tv_AppliedCoupon.visibility = View.GONE
                tv_couponLabel.visibility = View.GONE
                offer_badge.visibility = View.GONE
            }



            selected_workshop_detail.visibility = View.VISIBLE
            selected_workshop_detail.setOnClickListener {

                val json = jsonObject

                val bundle = Bundle()
                bundle.putSerializable(Constant.Key.workshopCalendarPrice, calendarPriceMap as Serializable)
                bundle.putSerializable(Constant.Key.PartIdMap, partidhasMap as Serializable)
                bundle.putSerializable(Constant.Key.MotPartIdMap, motpartlist as Serializable)


                var calendarPriceMap: HashMap<String, String> = HashMap()
                val id: Any

                if (json.has("category_id")) {
                    id = json.optInt("category_id")
                } else {
                    if (isCarMaintenanceServices) {
                        id = json.optString("service_id")
                    } else {
                        id = json.optInt("service_id")
                    }
                } //id is of string type for car maintenance service because for multiple service id's


                val intent = intentFor<WorkshopDetailActivity>(
                        Constant.Path.workshopUsersId to json.optInt("users_id")
                        , Constant.Path.categoryId to id.toString()
                        , Constant.Path.workshopFilterSelectedDate to selectedFormattedDate
                        , Constant.Key.workshopCategoryDetail to workshopCategoryDetail
                        , Constant.Key.is_revision to mIsRevision
                        , Constant.Key.is_assembly_service to isAssembly
                        , Constant.Key.is_tyre to mIsTyre
                        , Constant.Key.is_car_maintenance_service to isCarMaintenanceServices
                        , Constant.Key.is_quotes to isQuotes
                        , Constant.Key.is_sos_service to isSOSAppointment
                        , Constant.Key.is_motService to isMotService
                        , Constant.Path.mot_id to id
                        , Constant.Path.latitude to json.opt("latitude")
                        , Constant.Path.longitude to json.opt("longitude")
                        , Constant.Path.motservices_time to motservices_time
                        , Constant.Path.couponId to ""
                        , Constant.Path.couponList to couponList

                        , Constant.Key.is_car_wash to isCarWash
                        , Constant.Path.mainCategoryIdTyre to json.optString("main_category_id")
                        , Constant.Path.mainCategoryIdCar_wash to json.optString("main_category_id")
                        , Constant.Key.cartItem to cartItem, Constant.Key.wishList to json.optString("wish_list")
                        , Constant.Key.mot_type to motType
                        , "WorkshopJson" to json.toString()


                ).putExtras(bundle)


                //intent.printValues(javaClass.name)

                startActivity(intent)
            }


        } else {
            selected_workshop_detail.visibility = View.GONE
        }


        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_RQ -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFusedLocation()
                }
            }
        }
    }

    private fun createDrawableFromView(context: Context, price: String): Bitmap {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.custom_marker_mapfilter, null)


        view.tv_workshopPrices.text = getString(R.string.prepend_euro_symbol_with_da_string, price)
        view.tv_workshopPrices.textSize = 10.0f
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.isDrawingCacheEnabled = true
        view.invalidate()
        view.buildDrawingCache(false)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }


    /*   public @NonNull static Bitmap createBitmapFromView(@NonNull View view, int width, int height) {
          if (width > 0 && height > 0) {
              view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                              .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                      View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                              .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
          }
          view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

          Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                  view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
          Canvas canvas = new Canvas(bitmap);
          Drawable background = view.getBackground();

          if (background != null) {
              background.draw(canvas);
          }
          view.draw(canvas);

          return bitmap;
      }*/

}