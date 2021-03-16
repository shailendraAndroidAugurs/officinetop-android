package com.officinetop.officine.adapter

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.officinetop.officine.R
import com.officinetop.officine.car_parts.ProductDetailActivity
import com.officinetop.officine.data.Models
import com.officinetop.officine.utils.*
import com.officinetop.officine.views.FilterListInterface
import com.officinetop.officine.workshop.WorkshopDetailActivity
import kotlinx.android.synthetic.main.item_product_or_workshop_detail.view.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.text.DecimalFormat
import kotlin.math.*

class ProductOrWorkshopListAdapter(productOrWorkshopList: ArrayList<Models.ProductOrWorkshopList>, search_view: androidx.appcompat.widget.SearchView, mJsonArray: JSONArray, isCarWash: Boolean, isSOSAppointment: Boolean, isMotService: Boolean, isQuotes: Boolean, isCarMaintenanceServices: Boolean, mIsWorkshop: Boolean, mIsRevision: Boolean, mIsTyre: Boolean,

                                   mSelectedFormattedDate: String, mView: FilterListInterface, mContext: Context, mCalendarPriceMap: HashMap<String, String>, mPartIdMap: HashMap<String, Models.servicesCouponData>, motPartIdMap: HashMap<String, Models.MotservicesCouponData>, private val currentLat: String = "0", private val currentLong: String = "0", motservicesTime: String, mot_type: String = "")

    : RecyclerView.Adapter<ProductOrWorkshopListAdapter.ViewHolder>(), Filterable {
    private var productOrWorkshopListParent = productOrWorkshopList
    var productOrWorkshopList = productOrWorkshopList
    var jsonArray: JSONArray
    private var isWorkshop: Boolean
    private var selectedFormattedDate: String
    var mcontext: Context
    var mView: FilterListInterface
    var searchText = ""
    private var assembledProductDetail = ""
    private var isAssembly = false
    private var isRevision = false
    private var isTyre = false
    private var isCarMaintenanceServices = false
    private var isQuotesServices = false
    private var isSOSAppointment = false
    private var isMotService = false
    private val search_view: androidx.appcompat.widget.SearchView = search_view
    private var quotesServiceQuotesInsertedId = ""

    private var qutoesUserDescription = ""

    private var qutoesUserImage = ""
    private var quotesMainCategoryId = ""
    private var workshopCouponId: String = ""

    private var quotesWorkshopUserDaysId: String = ""
    private var workshopCategoryDetail = ""
    private var calendarPriceMap: HashMap<String, String> = HashMap()
    private var mPartIdMap: HashMap<String, Models.servicesCouponData> = HashMap()
    private var motPartIdMap: HashMap<String, Models.MotservicesCouponData> = HashMap()

    private var latitude: String = ""
    private var longitude: String = ""
    private var workshopUserId: String = ""
    private var addressId: String = ""
    private var workshopWreckerId: String = ""
    private var motservicesTime: String = ""
    private var serviceId: String = ""
    private var isSosEmergency: Boolean = false
    private var isCarWash: Boolean = false
    private var cartItem: Models.CartItem? = null
    private var currentLatLong: LatLng? = null
    private var mot_type: String = ""

    private var isLoadingVisible = false
    fun setWorkshopCategory(workshopDetail: String) {
        this.workshopCategoryDetail = workshopDetail
    }

    fun getQuotesIds(quotesServiceQuotesInsertedId: String,
                     quotesMainCategoryId: String, qutoesUserDescription: String, qutoesUserImage: String) {
        this.quotesServiceQuotesInsertedId = quotesServiceQuotesInsertedId
        this.quotesMainCategoryId = quotesMainCategoryId
        this.qutoesUserDescription = qutoesUserDescription
        this.qutoesUserImage = qutoesUserImage
    }

    fun setAssembledProduct(assembledProduct: String) {
        isAssembly = true
        assembledProductDetail = assembledProduct
    }

    fun getCartItem(cartItems: Models.CartItem) {
        cartItem = cartItems
    }

    fun userLocationSOS(isSosEmergency: Boolean, latitude: String, longitude: String, serviceID: Int, workshopUserId: String, addressId: String,
                        workshopWreckerId: String) {

        this.latitude = latitude
        this.longitude = longitude
        this.workshopUserId = workshopUserId
        this.addressId = addressId
        this.workshopWreckerId = workshopWreckerId
        this.serviceId = serviceID.toString()
        this.isSosEmergency = isSosEmergency
    }

    private var filteredJSONArray = JSONArray()

    init {
        this.jsonArray = mJsonArray
        this.filteredJSONArray = jsonArray
        this.isWorkshop = mIsWorkshop
        this.isRevision = mIsRevision
        this.isTyre = mIsTyre
        this.isSOSAppointment = isSOSAppointment
        this.isCarMaintenanceServices = isCarMaintenanceServices
        this.isQuotesServices = isQuotes
        this.isMotService = isMotService
        this.mcontext = mContext
        this.selectedFormattedDate = mSelectedFormattedDate
        this.calendarPriceMap = mCalendarPriceMap
        this.mPartIdMap = mPartIdMap
        this.motPartIdMap = motPartIdMap
        this.mView = mView
        this.currentLatLong = currentLatLong
        this.motservicesTime = motservicesTime
        this.isCarWash = isCarWash
        this.mot_type = mot_type

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_product_or_workshop_detail, parent, false))


    }

    override fun getItemCount(): Int = getProductOrWorkshopListAdapter()

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        with(p0) {
            // tv_count.text=(p1+1).toString()
            val product_workshopList = productOrWorkshopList[p1]


            if (isCarWash || isWorkshop || isTyre || isRevision || isCarMaintenanceServices || isQuotesServices ||
                    isMotService || isSOSAppointment || isSosEmergency) {
                brandImage.visibility = View.GONE
                icon.visibility = View.GONE
                item_image_workshop.visibility = View.VISIBLE
                getDistancebetweenTwoLatLong(currentLat, currentLong, product_workshopList.latitude, product_workshopList.longitude, tv_workshopKm)
                if (isSosEmergency) {
                    llAverageTimeEmergency.visibility = View.VISIBLE
                    if (!product_workshopList.average_time_in_min.isNullOrBlank()) {
                        val time = product_workshopList.average_time_in_min.toDouble().roundToInt()
                        val hr = time / 60
                        val minute = time % 60
                        if (hr == 0)
                            averageTimeInMin.text = "$minute min"
                        else if (minute == 0)
                            averageTimeInMin.text = "$hr hr"
                        else {
                            averageTimeInMin.text = "$hr hr,$minute min"

                        }


                    }


                }

                if (!product_workshopList.workshop_user_days_id.isNullOrBlank()) {
                    quotesWorkshopUserDaysId = product_workshopList.workshop_user_days_id
                }

                title.text = product_workshopList.companyName

                subtitle.text = product_workshopList.registeredOffice

                if (!product_workshopList.servicesPrice.isNullOrEmpty() && product_workshopList.servicesPrice != "null") {
                    price.text = mcontext.getString(R.string.prepend_euro_symbol_string, product_workshopList.servicesPrice)
                } else {
                    price.text = mcontext.getString(R.string.prepend_euro_symbol_string, "0")
                }

                if (product_workshopList.rating != null && !product_workshopList.rating.rating.isNullOrEmpty()) {
                    rating.rating = product_workshopList.rating.rating.toFloat()
                } else
                    rating.rating = 0f
                if (!product_workshopList.ratingCount.isNullOrEmpty()) {
                    ratingCount.text = product_workshopList.ratingCount
                } else
                    ratingCount.text = ""


                //available_status
                Log.d("check_status_avilable", "" + product_workshopList.availableStatus)
                if (!product_workshopList.availableStatus.isNullOrEmpty() && product_workshopList.availableStatus != "1") {
                    workshopAvailability.visibility = View.VISIBLE
                    AppliedCouponName.visibility = View.VISIBLE
                    CouponLabel.visibility = View.VISIBLE
                    offerBadge.visibility = View.GONE
                    if (productOrWorkshopList[p1].couponList != null && productOrWorkshopList[p1].couponList.size != 0) {
                        AppliedCouponName.text = (productOrWorkshopList[p1].couponList[0].couponTitle)
                        AppliedCouponName.visibility = View.VISIBLE
                        CouponLabel.visibility = View.VISIBLE
                        offerBadge.visibility = View.GONE
                    } else {
                        AppliedCouponName.visibility = View.GONE
                        CouponLabel.visibility = View.GONE
                        offerBadge.visibility = View.GONE
                    }
                } else {
                    workshopAvailability.visibility = View.GONE
                    if (productOrWorkshopList[p1].couponList != null && productOrWorkshopList[p1].couponList.size != 0) {
                        AppliedCouponName.text = (productOrWorkshopList[p1].couponList[0].couponTitle)
                        AppliedCouponName.visibility = View.VISIBLE
                        CouponLabel.visibility = View.VISIBLE
                        offerBadge.visibility = View.GONE
                    } else {
                        AppliedCouponName.visibility = View.GONE
                        CouponLabel.visibility = View.GONE
                        offerBadge.visibility = View.GONE
                    }
                }

                if (isQuotesServices) {
                    AppliedCouponName.visibility = View.GONE
                    CouponLabel.visibility = View.GONE
                    offerBadge.visibility = View.GONE
                }


                //load images
                try {
                    val name = product_workshopList.profileImage
                    if (!name.isNullOrBlank())
                        mcontext.loadImageWithName(name, item_image_workshop, R.drawable.no_image_placeholder, baseURL = Constant.profileBaseUrl)

                } catch (e: Exception) {
                }
            } else {

                if (!product_workshopList.forPair.isNullOrBlank()) {

                    if (product_workshopList.forPair.equals("0")) {
                        textview_quantity.text = ""
                        textview_quantity.visibility = View.GONE
                    } else {
                        textview_quantity.visibility = View.VISIBLE
                        textview_quantity.text = "2 " + mcontext.getString(R.string.price)
                    }
                }
                price.text = if (!productOrWorkshopList[p1].sellerPrice.isNullOrBlank() && !productOrWorkshopList[p1].sellerPrice.equals("null"))
                    mcontext.getString(R.string.prepend_euro_symbol_string, productOrWorkshopList[p1].sellerPrice)
                else mcontext.getString(R.string.prepend_euro_symbol_string, "0")




                if (!productOrWorkshopList[p1].ratingStar.isNullOrBlank()) {
                    rating.rating = productOrWorkshopList[p1].ratingStar.toFloat()
                } else
                    rating.rating = 0f

                if (!productOrWorkshopList[p1].ratingCount.isNullOrBlank())
                    ratingCount.text = productOrWorkshopList[p1].ratingCount

                if (productOrWorkshopList[p1].couponList != null && productOrWorkshopList[p1].couponList.size != 0) {
                    AppliedCouponName.text = (productOrWorkshopList[p1].couponList[0].couponTitle)
                    AppliedCouponName.visibility = View.VISIBLE
                    CouponLabel.visibility = View.VISIBLE
                    offerBadge.visibility = View.GONE
                } else {
                    CouponLabel.visibility = View.GONE
                    AppliedCouponName.visibility = View.GONE
                    offerBadge.visibility = View.GONE
                }

                if (productOrWorkshopList[p1].images != null && productOrWorkshopList[p1].images?.size != 0)
                    if (!productOrWorkshopList[p1].images?.get(0)?.imageUrl.isNullOrBlank())

                        mcontext.loadImage(productOrWorkshopList[p1].images?.get(0)?.imageUrl, icon, R.drawable.no_image_placeholder)
                    else if (!productOrWorkshopList[p1].profileImage.isNullOrBlank()) {
                        mcontext.loadImage(Constant.profileBaseUrl + productOrWorkshopList[p1].profileImage, icon, R.drawable.no_image_placeholder)
                    }

                var titleString = "${if (productOrWorkshopList[p1].productName != null) productOrWorkshopList[p1].productName else ""} "

                titleString = titleString.replace(" null ", " ")
                title.text = titleString

                if (searchText.length > 0) {
                    var index: Int = titleString.toLowerCase().indexOf(searchText.toLowerCase())
                    val sb = SpannableStringBuilder(titleString)
                    while (index >= 0) {

                        val fcs = BackgroundColorSpan(mcontext.resources.getColor(R.color.theme_orange))
                        sb.setSpan(fcs, index, searchText.length + index, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        index = titleString.indexOf(searchText, index + 1, true)
                        title.text = sb
                    }

                }
                subtitle.text = "${if (productOrWorkshopList[p1].Productdescription != null) productOrWorkshopList[p1].Productdescription else ""} "

                ll_workshopKm.visibility = View.GONE
                // Display brand image if this is spare part
                brandImage.visibility = View.VISIBLE
                if (!productOrWorkshopList[p1].brandImage.isNullOrBlank())
                    mcontext.loadImage(productOrWorkshopList[p1].brandImage, brandImage)
                else if (!productOrWorkshopList[p1].brandImageurl.isNullOrBlank()) {
                    mcontext.loadImage(productOrWorkshopList[p1].brandImageurl, brandImage)
                } else {
                    brandImage.visibility = View.GONE
                }
            }


            itemView.setOnClickListener {


                if (!isCarWash && !isWorkshop && !isRevision && !isTyre && !isCarMaintenanceServices && !isQuotesServices && !isMotService && !isSOSAppointment && !isSosEmergency) {
                    mcontext.startActivity(mcontext.intentFor<ProductDetailActivity>(
                            Constant.Path.productDetails to productOrWorkshopList[p1].id, Constant.Key.wishList to productOrWorkshopList[p1].wish_list).forwardResults())
                } else {

                    val json = JSONObject(filteredJSONArray[p1].toString())

                    val bundle = Bundle()
                    bundle.putSerializable(Constant.Key.workshopCalendarPrice, calendarPriceMap as Serializable)
                    bundle.putSerializable(Constant.Key.PartIdMap, mPartIdMap as Serializable)
                    bundle.putSerializable(Constant.Key.MotPartIdMap, motPartIdMap as Serializable)
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
                    val intent = mcontext.intentFor<WorkshopDetailActivity>(
                            Constant.Path.workshopUsersId to json.optInt("users_id")
                            , Constant.Path.categoryId to id.toString()
                            , Constant.Path.workshopUserDaysId to quotesWorkshopUserDaysId
                            , Constant.Path.mainCategoryId to quotesMainCategoryId
                            , Constant.Path.workshopFilterSelectedDate to selectedFormattedDate
                            , Constant.Key.workshopCategoryDetail to workshopCategoryDetail
                            , Constant.Key.workshopAssembledDetail to assembledProductDetail
                            , Constant.Key.is_revision to isRevision
                            , Constant.Key.is_assembly_service to isAssembly
                            , Constant.Key.is_tyre to isTyre
                            , Constant.Key.is_car_maintenance_service to isCarMaintenanceServices
                            , Constant.Key.is_quotes to isQuotesServices
                            , Constant.Key.is_sos_service to isSOSAppointment
                            , Constant.Key.is_motService to isMotService
                            , Constant.Path.mot_id to id
                            , Constant.Path.latitude to latitude
                            , Constant.Path.longitude to longitude
                            , Constant.Path.serviceID to serviceId
                            , Constant.Path.addressId to addressId
                            , Constant.Path.workshopWreckerId to workshopWreckerId
                            , Constant.Path.motservices_time to motservicesTime
                            , Constant.Key.is_sos_service_emergency to isSosEmergency
                            , Constant.Key.is_car_wash to isCarWash
                            , Constant.Path.couponId to workshopCouponId
                            , Constant.Path.couponList to productOrWorkshopList[p1].couponList
                            , Constant.Path.mainCategoryIdTyre to json.optString("main_category_id")
                            , Constant.Path.mainCategoryIdCar_wash to json.optString("main_category_id")
                            , Constant.Key.cartItem to cartItem, Constant.Key.wishList to productOrWorkshopList[p1].wish_list
                            , Constant.Key.mot_type to mot_type
                            , "WorkshopJson" to json.toString()
                            , "QutoesServiceAverageTime" to if (json.has("service_average_time") && json.optString("service_average_time") != null && json.optString("service_average_time") != "null") json.optString("service_average_time") else ""
                            , Constant.Path.qutoesUserDescription to qutoesUserDescription
                            , Constant.Path.qutoesUserAttachImage to qutoesUserImage


                    ).putExtras(bundle)


                    if (isAssembly || isTyre) {
                        intent.putExtra(Constant.Key.cartItem, (mcontext as Activity).intent.getSerializableExtra(Constant.Key.cartItem))
                    }

                    mcontext.startActivity(intent)
                }
            }


            if (productOrWorkshopList[p1].wish_list != null && productOrWorkshopList[p1].wish_list == "1") {
                itemView.Iv_favorite.setImageResource(R.drawable.ic_heart)

            } else {
                itemView.Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
            }

            itemView.Iv_favorite.setOnClickListener {
                if (isCarWash || isWorkshop || isTyre || isRevision || isCarMaintenanceServices || isQuotesServices || isMotService || isSOSAppointment || isSosEmergency) {
                    try {
                        if ((productOrWorkshopList[p1].wish_list == null || productOrWorkshopList[p1].wish_list == "") || productOrWorkshopList[p1].wish_list == "0") {

                            mcontext.addToFavoriteSendRequest(mcontext, "", "", itemView.Iv_favorite, null, productOrWorkshopList[p1].usersId, productOrWorkshopList[p1])

                        } else {
                            mcontext.removeFromFavoriteSendRquest(mcontext, "", itemView.Iv_favorite, null, productOrWorkshopList[p1].usersId, productOrWorkshopList[p1])

                        }


                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }


                } else {
                    try {
                        if ((productOrWorkshopList[p1].wish_list == null || productOrWorkshopList[p1].wish_list == "") || productOrWorkshopList[p1].wish_list == "0") {

                            mcontext.addToFavoriteSendRequest(mcontext, productOrWorkshopList[p1].id, "1", itemView.Iv_favorite, null, "", productOrWorkshopList[p1])


                        } else {
                            mcontext.removeFromFavoriteSendRquest(mcontext, productOrWorkshopList[p1].id, itemView.Iv_favorite, null, "", productOrWorkshopList[p1], "1")

                        }


                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }


            }

        }
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                if (jsonArray.length() != productOrWorkshopList.size) {
                    val listString: String = Gson().toJson(
                            productOrWorkshopList,
                            object : TypeToken<ArrayList<Models.ProductOrWorkshopList?>?>() {}.type)
                    jsonArray = JSONArray()
                    jsonArray = JSONArray(listString)
                }
                val filterResults = FilterResults()
                val filteredList = JSONArray()

                if (constraint == null || constraint.isEmpty()) {
                    filteredJSONArray = JSONArray()
                    filteredJSONArray = jsonArray
                    filterResults.count = filteredJSONArray.length()
                    filterResults.values = filteredJSONArray
                    searchText = ""
                } else {
                    searchText = constraint.toString()

                    for (i in 0 until jsonArray.length()) {
                        val json = JSONObject(jsonArray[i].toString())

                        val titleString = json.getString("p_name")

                        if (titleString.matches(Regex(constraint.toString())) || titleString.toLowerCase().contains(constraint.toString().toLowerCase())) {

                            filteredList.put(json)
                        }
                    }


                    if (filteredList.length() == 0) {
                        filteredJSONArray = JSONArray()
                        filteredJSONArray = jsonArray



                        Toast.makeText(mcontext, mcontext.getString(R.string.SearchProductNotFound), Toast.LENGTH_SHORT).show()
                    } else {
                        filteredJSONArray = JSONArray()
                        filteredJSONArray = filteredList

                        filterResults.count = filteredJSONArray.length()
                        filterResults.values = filteredJSONArray
                    }

                }


                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null)
                    mView.onListFiltered(filteredJSONArray)
                if (filteredJSONArray.length() != 0) {
                    val gson = GsonBuilder().create()
                    productOrWorkshopList = gson.fromJson(filteredJSONArray.toString(), Array<Models.ProductOrWorkshopList>::class.java).toCollection(java.util.ArrayList<Models.ProductOrWorkshopList>())

                    notifyDataSetChanged()
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.item_title
        val subtitle = view.item_sub_title
        val icon = view.item_image

        val workshopAvailability = view.workshop_availability
        val price = view.item_price
        val rating = view.item_rating
        val ratingCount = view.item_rating_count
        val brandImage = view.item_brand_image
        val llayoutSpecialCoupons = view.ll_special_coupons
        val averageTimeInMin = view.average_time
        val llAverageTimeEmergency = view.ll_average_time_emergency
        val textview_quantity = view.textview_quantity
        val tv_workshopKm = view.item_workshopKM
        val AppliedCouponName = view.tv_AppliedCoupon
        val CouponLabel = view.tv_couponLabel
        val offerBadge = view.offer_badge
        val ll_workshopKm = view.ll_workshopKm
        val tv_count = view.tv_count
        val item_image_workshop = view.item_image_workshop

    }

    private fun getProductOrWorkshopListAdapter(): Int {

        if (productOrWorkshopList.size != 0 && search_view.query.isNullOrBlank()) {

            productOrWorkshopList = productOrWorkshopListParent

        }

        return productOrWorkshopList.size
    }

    private fun getDistancebetweenTwoLatLong1(currentLatLong: LatLng, latitude: String, longitude: String, tv_workshopKm: TextView) {
        if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank() && currentLatLong != null && !currentLatLong.latitude.equals("0.0") && !currentLatLong.latitude.equals("0.0") && latitude != "0" && latitude != "0") {
            val currentLocation = Location("")
            currentLocation.latitude = currentLatLong.latitude
            currentLocation.longitude = currentLatLong.longitude
            val workshopLocation = Location("")
            workshopLocation.latitude = latitude.toDouble()
            workshopLocation.longitude = longitude.toDouble()
            val distance = (currentLocation.distanceTo(workshopLocation).roundToInt()) / 1000
            tv_workshopKm.text = mcontext.getString(R.string.append_km, distance)

        }
    }

    private fun getDistancebetweenTwoLatLong(currentLat: String, currentLong: String, latitude: String, longitude: String, tv_workshopKm: TextView) {
        if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank() && currentLat != "0.0" && currentLong != "0.0" && latitude != "0" && latitude != "0" && latitude != "0" && latitude != "0") {
            val Radius = 6371
            val lat1 = currentLat.toDouble()
            val lat2 = latitude.toDouble()
            val lon1 = currentLong.toDouble()
            val lon2 = longitude.toDouble()
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * asin(sqrt(a))
            val valueResult = Radius * c
            val km = valueResult / 1
            val newFormat: DecimalFormat = DecimalFormat("####")
            var kmInDec = Integer.valueOf(newFormat.format(km))
            val meter = valueResult % 1000
            val meterInDec = Integer.valueOf(newFormat.format(meter))
            val distance = (Radius * c).roundToInt()
            Log.d(" test Distance", distance.toString())
            tv_workshopKm.text = mcontext.getString(R.string.append_km, distance)
        }
    }

    /*private fun getDistancebetweenTwoLatLong(currentLatLong: LatLng, latitude: String, longitude: String, tv_workshopKm: TextView) {
         if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank() && currentLatLong != null && !currentLatLong.latitude.equals("0.0") && !currentLatLong.latitude.equals("0.0") && !latitude.equals("0") && !latitude.equals("0") && !latitude.equals("0") && !latitude.equals("0")) {
             var lat1 = currentLatLong.latitude;
             val lat2 = latitude.toDouble();
             val lon1 = currentLatLong.longitude;
             val lon2 = longitude.toDouble();
             Log.d("CalculateDistanceClass", "Yes")
             CalculateDistance(mcontext,lat1.toString(),lon1.toString(),lat2.toString(),lon2.toString(),tv_workshopKm).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
         }
     }*/
/*
    fun getDistancebetweenTwoLatLong(currentLat: String,currentLong: String, latitude: String, longitude: String, tv_workshopKm: TextView) {
         var parsedDistance = "";
         var response = "";
         var thread = Thread(Runnable {
             try {
                 var lat1 = currentLat
                 val lon1 = currentLong
                 var url = URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + latitude.toDouble() + "," + longitude.toDouble() + "&sensor=false&units=metric&mode=driving&key="+mcontext.getString(R.string.google_api_key));
                 var conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                 conn.setRequestMethod("POST");
                 var inputStream: InputStream = BufferedInputStream(conn.getInputStream());
                 //response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
                 response = IOUtils.readInputStreamFully(inputStream).toString()
                 Log.d(" test Distance Response", response.toString())
                 var jsonObject: JSONObject = JSONObject(response);
                 var array: JSONArray = jsonObject.getJSONArray("routes");
                 var routes: JSONObject = array.getJSONObject(0);
                 var legs: JSONArray = routes.getJSONArray("legs");
                 var steps: JSONObject = legs.getJSONObject(0);
                 var distance: JSONObject = steps.getJSONObject("distance");
                 parsedDistance = distance.getString("text");
             } catch (e: ProtocolException) {
                 e.printStackTrace();
             } catch (e: MalformedURLException) {
                 e.printStackTrace();
             } catch (e: IOException) {
                 e.printStackTrace();
             } catch (e: JSONException) {
                 e.printStackTrace();
             }
         })
         thread.start();
        */
/* try {
             thread.join();
         } catch (e: InterruptedException) {
             e.printStackTrace();
         }*//*
         Log.d(" test Distance", parsedDistance.toString())
         tv_workshopKm.text = (parsedDistance);
     }
*/

    /* private fun getDistanceOnRoad(latitude:Double, longitude:Double,
                                   prelatitute:Double, prelongitude:Double):String {
         val result_in_kms = ""
    *//*     val url = ("http://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&sensor=false&units=metric")*//*
        var url = URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + latitude + "," + longitude + "&destination=" + prelatitute.toDouble() + "," + prelongitude.toDouble() + "&sensor=false&units=metric&mode=driving");
        val tag = arrayOf<String>("text")
        val response: HttpResponse? = null
        try
        {
            var conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.setRequestMethod("POST");
            var inputStream: InputStream = BufferedInputStream(conn.getInputStream());
            val `is` = response.getEntity().getContent()
            val builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
            val doc = builder.parse(`is`)
            if (doc != null)
            {
                val nl: NodeList
                val args = ArrayList()
                for (s in tag)
                {
                    nl = doc.getElementsByTagName(s)
                    if (nl.getLength() > 0)
                    {
                        val node = nl.item(nl.getLength() - 1)
                        args.add(node.getTextContent())
                    }
                    else
                    {
                        args.add(" - ")
                    }
                }
                result_in_kms = String.format("%s", args.get(0))
            }
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
        return result_in_kms
    }*/


    fun addItems(items: MutableList<Models.ProductOrWorkshopList>) {
        productOrWorkshopList.addAll(items)

        notifyDataSetChanged()
    }

    fun addLoading() {
        isLoadingVisible = true


    }

    fun removeLoading() {

        isLoadingVisible = false

    }


    fun clear() {
        productOrWorkshopList.clear()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Models.ProductOrWorkshopList {
        return productOrWorkshopList.get(position)
    }

    fun getListSize(): Int {
        return productOrWorkshopList.size
    }

}

