package com.officinetop.officine.adapter

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
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
import com.google.gson.GsonBuilder
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
import kotlin.math.roundToInt

class ProductOrWorkshopListAdapter(productOrWorkshopList: ArrayList<Models.ProductOrWorkshopList>, search_view: androidx.appcompat.widget.SearchView, mJsonArray: JSONArray, isCarWash: Boolean, isSOSAppointment: Boolean, isMotService: Boolean, isQuotes: Boolean, isCarMaintenanceServices: Boolean, mIsWorkshop: Boolean, mIsRevision: Boolean, mIsTyre: Boolean,

                                   mSelectedFormattedDate: String, mView: FilterListInterface, mContext: Context, mCalendarPriceMap: HashMap<String, String>, mPartIdMap: HashMap<String, Models.servicesCouponData>, motPartIdMap: HashMap<String, Models.MotservicesCouponData>, currentLatLong: LatLng?, motservicesTime: String, mot_type: String = "")

    : RecyclerView.Adapter<ProductOrWorkshopListAdapter.ViewHolder>(), Filterable {
    var productOrWorkshopListParent = productOrWorkshopList
    var productOrWorkshopList = productOrWorkshopList
    var jsonArray: JSONArray
    var isWorkshop: Boolean
    var selectedFormattedDate: String
    var mcontext: Context
    var mView: FilterListInterface

    var assembledProductDetail = ""
    var isAssembly = false
    var isRevision = false
    var isTyre = false
    var isCarMaintenanceServices = false
    var isQuotesServices = false
    var isSOSAppointment = false
    var isMotService = false
    val search_view: androidx.appcompat.widget.SearchView = search_view
    var quotesServiceQuotesInsertedId = ""
    var quotesMainCategoryId = ""

    var couponsArray: JSONArray = JSONArray()

    var workshopCouponId: String = ""
    var productCouponId: String = ""

    var quotesWorkshopUserDaysId: String = ""


    private var workshopCategoryDetail = ""
    //    var calendarPriceList: MutableList<Models.CalendarPrice> = ArrayList()
    var calendarPriceMap: HashMap<String, String> = HashMap()
    var mPartIdMap: HashMap<String, Models.servicesCouponData> = HashMap()
    var motPartIdMap: HashMap<String, Models.MotservicesCouponData> = HashMap()

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

    fun setWorkshopCategory(workshopDetail: String) {
        this.workshopCategoryDetail = workshopDetail
    }

    fun getQuotesIds(quotesServiceQuotesInsertedId: String,
                     quotesMainCategoryId: String) {
        this.quotesServiceQuotesInsertedId = quotesServiceQuotesInsertedId
        this.quotesMainCategoryId = quotesMainCategoryId
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


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mcontext).inflate(R.layout.item_product_or_workshop_detail, p0, false))
    }

    override fun getItemCount(): Int = getProductOrWorkshopListAdapter()

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        with(p0) {
            var product_workshopList = productOrWorkshopList[p1]
            if (isQuotesServices) {
                llayoutSpecialCoupons.visibility = View.GONE
            }

            if (isCarWash || isWorkshop || isTyre || isRevision || isCarMaintenanceServices || isQuotesServices ||
                    isMotService || isSOSAppointment || isSosEmergency) {
                brandImage.visibility = View.INVISIBLE

                //val json = JSONObject(filteredJSONArray[p1].toString())

                getDistancebetweenTwoLatLong(currentLatLong!!, product_workshopList.latitude, product_workshopList.longitude, tv_workshopKm)
                if (isSosEmergency) {
                    llAverageTimeEmergency.visibility = View.VISIBLE
                    if (!product_workshopList.average_time_in_min.isNullOrBlank()) {
                        var time = product_workshopList.average_time_in_min.toDouble().roundToInt()
                        var hr = time / 60
                        var minute = time % 60
                        if (hr == 0)
                            averageTimeInMin.text = minute.toString() + " min"
                        else if (minute == 0)
                            averageTimeInMin.text = hr.toString() + " hr"
                        else {
                            averageTimeInMin.text = hr.toString() + " hr" + "," + minute.toString() + " min"

                        }


                    }


                }

                if (!product_workshopList.workshop_user_days_id.isNullOrBlank()) {
                    quotesWorkshopUserDaysId = product_workshopList.workshop_user_days_id
                }

                title.text = product_workshopList.companyName

                subtitle.text = product_workshopList.registeredOffice

                if (!product_workshopList.servicesPrice.isNullOrEmpty())
                    price.text = mcontext.getString(R.string.prepend_euro_symbol_string, product_workshopList.servicesPrice)

                //distance.text = "(11.6 Km)"
                if (!product_workshopList.ratingStar.isNullOrEmpty()) {
                    rating.rating = product_workshopList.ratingStar.toFloat()
                } else
                    rating.rating = 0f
                if (!product_workshopList.ratingCount.isNullOrEmpty()) {
                    ratingCount.text = product_workshopList.ratingCount
                } else
                    ratingCount.text = ""


                //available_status
                if (!product_workshopList.availableStatus.isNullOrEmpty() && product_workshopList.availableStatus == "0") {
                    workshopAvailability.visibility = View.VISIBLE
                    AppliedCouponName.visibility = View.GONE
                    CouponLabel.visibility = View.GONE
                    offerBadge.visibility = View.GONE
                } else {
                    workshopAvailability.visibility = View.GONE
                    if (productOrWorkshopList[p1].couponList != null && productOrWorkshopList[p1].couponList.size != 0) {
                        AppliedCouponName.text = (productOrWorkshopList[p1].couponList[0].couponTitle)
                        AppliedCouponName.visibility = View.VISIBLE
                        CouponLabel.visibility = View.VISIBLE
                        offerBadge.visibility = /*if (p1 % 2 == 0)*/ View.GONE /*else View.GONE*/
                    } else {
                        AppliedCouponName.visibility = View.GONE
                        CouponLabel.visibility = View.GONE
                        offerBadge.visibility = View.GONE
                    }
                }





                icon.setImageResource(R.drawable.no_image_placeholder)

                //load images
                try {
                    val name = product_workshopList.profileImage
                    mcontext.loadImageWithName(name, icon, R.drawable.no_image_placeholder, baseURL = Constant.profileBaseUrl)

                } catch (e: Exception) {
                }
            } else {

                if (!product_workshopList.forPair.isNullOrBlank()) {
                    if (product_workshopList.forPair.equals("0")) {
                        textview_quantity.text = ""
                    } else {
                        textview_quantity.text = "2 " + mcontext.getString(R.string.price)
                    }
                }
                price.text = if (productOrWorkshopList[p1].sellerPrice == null)
                    mcontext.getString(R.string.prepend_euro_symbol_string, "0")
                else mcontext.getString(R.string.prepend_euro_symbol_string, productOrWorkshopList[p1].sellerPrice)




                if (!productOrWorkshopList[p1].ratingStar.isNullOrBlank()) {
                    rating.rating = productOrWorkshopList[p1].ratingStar.toFloat()
                } else
                    rating.rating = 0f

                if (!productOrWorkshopList[p1].ratingCount.isNullOrBlank())
                    ratingCount.text = productOrWorkshopList[p1].ratingCount

                if (productOrWorkshopList[p1].couponList != null && productOrWorkshopList[p1].couponList.size != 0) {
                    // workshopCouponId = productOrWorkshopList[p1].couponList[0].id.toString()
                    Log.e("workshopCouponIdADAP", (workshopCouponId))
                    AppliedCouponName.text = (productOrWorkshopList[p1].couponList[0].couponTitle)
                    AppliedCouponName.visibility = View.VISIBLE
                    CouponLabel.visibility = View.VISIBLE
                    offerBadge.visibility = /*if (p1 % 2 == 0)*/ View.GONE /*else View.GONE*/
                } else {
                    CouponLabel.visibility = View.GONE
                    AppliedCouponName.visibility = View.GONE
                    offerBadge.visibility = View.GONE
                }


                /*    offerBadge.setOnClickListener {
                        if (productOrWorkshopList[p1].couponList != null) {
                            // val couponObject = Gson().toJson(productOrWorkshopList[p1].couponList) as JsonObject
                            // couponsArray = Gson().toJson(couponObject.toString()) as JSONArray
                            displayCoupons(productOrWorkshopList[p1].couponList as MutableList<Models.Coupon>, "workshop_coupon",AppliedCouponName)
                            //else Snackbar.make("no coupons assigned!")
                        }
                    }*/

                if (productOrWorkshopList[p1].images != null && productOrWorkshopList[p1].images?.size != 0)
                    mcontext.loadImage(productOrWorkshopList[p1].images?.get(0)?.imageUrl, icon, R.drawable.no_image_placeholder)
                else if (!productOrWorkshopList[p1].profileImage.isNullOrBlank()) {
                    mcontext.loadImage(Constant.profileBaseUrl + productOrWorkshopList[p1].profileImage, icon, R.drawable.no_image_placeholder)
                } else
                    mcontext.loadImageWithName("", icon, R.drawable.no_image_placeholder)

//                "car_makers_name" + "products_name" + "front_rear" + "left_right" + "CodiceOE"
//                listino+products_name+front_rear+left_right
                // set title

                //  var titleString = "${if (productOrWorkshopList[p1].listino != null) productOrWorkshopList[p1].listino else ""} ${if (productOrWorkshopList[p1].productsName != null) productOrWorkshopList[p1].productsName else ""} "

                var titleString = "${if (productOrWorkshopList[p1].productName != null) productOrWorkshopList[p1].productName else ""} "

                titleString = titleString.replace(" null ", " ")
                title.text = titleString
                subtitle.text = "${if (productOrWorkshopList[p1].Productdescription != null) productOrWorkshopList[p1].Productdescription else ""} "

                ll_workshopKm.visibility=View.GONE
                // Display brand image if this is spare part
                brandImage.visibility = View.VISIBLE
                if(!productOrWorkshopList[p1]?.brandImage.isNullOrBlank())
                mcontext.loadImage(productOrWorkshopList[p1]?.brandImage, brandImage)
                else{
                    mcontext.loadImage(productOrWorkshopList[p1]?.brandImageurl, brandImage)
                }
            }


            itemView.setOnClickListener {


                if (!isCarWash && !isWorkshop && !isRevision && !isTyre && !isCarMaintenanceServices && !isQuotesServices && !isMotService && !isSOSAppointment && !isSosEmergency) {
                    mcontext.startActivity(mcontext.intentFor<ProductDetailActivity>(
                            Constant.Path.productDetails to JSONObject(filteredJSONArray[p1].toString()).toString(), Constant.Key.wishList to productOrWorkshopList[p1].wish_list).forwardResults())
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
                            , Constant.Path.serviceQuotesInsertedId to quotesServiceQuotesInsertedId
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
                            , "QutoesServiceAverageTime" to if (json.has("service_average_time") && json.optString("service_average_time") != null && !json.optString("service_average_time").equals("null")) json.optString("service_average_time") else ""

                    ).putExtras(bundle)


                    if (isAssembly || isTyre) {
                        intent.putExtra(Constant.Key.cartItem, (mcontext as Activity).intent.getSerializableExtra(Constant.Key.cartItem))
                    }

                    //intent.printValues(javaClass.name)

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

                            mcontext.AddToFavoritesendRquest(mcontext, "", "", itemView.Iv_favorite, null, productOrWorkshopList[p1].usersId, productOrWorkshopList[p1])

                        } else {
                            mcontext.RemoveFromFavoritesendRquest(mcontext, "", itemView.Iv_favorite, null, productOrWorkshopList[p1].usersId, productOrWorkshopList[p1])

                        }


                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }


                } else {
                    try {
                        if ((productOrWorkshopList[p1].wish_list == null || productOrWorkshopList[p1].wish_list == "") || productOrWorkshopList[p1].wish_list == "0") {

                            mcontext.AddToFavoritesendRquest(mcontext, productOrWorkshopList[p1].id, "1", itemView.Iv_favorite, null, "", productOrWorkshopList[p1])


                        } else {
                            mcontext.RemoveFromFavoritesendRquest(mcontext, productOrWorkshopList[p1].id, itemView.Iv_favorite, null, "", productOrWorkshopList[p1], "1")

                        }


                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }


            }

        }
    }

    /*private fun displayCoupons(couponsList: MutableList<Models.Coupon>, couponType: String,AppliedCouponName:TextView) {

       *//* val couponsList: MutableList<Models.Coupon> = ArrayList()

        *//**//* for (i in 0 until couponsArray!!.length()) {
             val couponData = couponsArray.get(i) as JSONObject
             val couponOffer = Gson().fromJson<Models.Coupon>(couponData.toString(), Models.Coupon::class.java)

         }*//**//*
        couponsList.add(couponsArray)*//*


        val dialog = Dialog(mcontext)
        val dialogView: View = LayoutInflater.from(mcontext).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, 1200)//height shoud be fixed
        val title = dialog.findViewById(R.id.title) as TextView
       // val textview_quantity = dialog.findViewById(R.id.textview_quantity) as TextView
        val ll_coupons = dialog.findViewById(R.id.ll_coupons) as LinearLayout
        val apply_coupons = dialog.findViewById(R.id.apply_coupons) as Button
        val cancel_coupons = dialog.findViewById(R.id.cancel_coupons) as Button
        ll_coupons.visibility = View.VISIBLE
        apply_coupons.visibility = View.GONE
        title.text = "Coupons List"


        with(dialog) {
            var selectedPosition: Int = -1

            class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val couponsName = view.coupons_name
                val couponsQuantity = view.coupons_quantity
                val couponsCheck = view.coupons_check
                val couponsAmount = view.coupons_amount
            }

            dialog_recycler_view.adapter = object : RecyclerView.Adapter<ViewHolder>() {


                override fun getItemCount(): Int = couponsList.size

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val items = couponsList[position]
                    holder.couponsName.text = items.couponTitle
                    holder.couponsQuantity.text = items.couponQuantity.toString()
                    holder.couponsAmount.text = mcontext.getString(R.string.prepend_euro_symbol_string, items.amount.toString())


                    holder.itemView.setOnClickListener {


                        if (!couponsList[position].id.isNullOrBlank() ) {
                            workshopCouponId = couponsList[position].id.toString()
                            AppliedCouponName.text=(couponsList[position].couponTitle)
                            AppliedCouponName.visibility = View.VISIBLE
                            dialog.dismiss()
                        }

                    }

                    // holder.couponsCheck.tag = position

                    //chkBoxList.add(holder.couponsCheck)

                    // holder.couponsCheck.isChecked = position == selectedPosition

                    // holder.couponsCheck.setOnClickListener(onStateChangedListener(holder.couponsCheck, position))

                    *//*holder.couponsCheck.setOnCheckedChangeListener { _, b ->
                        if (b){
                            if(!holder.couponsCheck.tag.equals(chkBoxList[position].tag))
                                chkBoxList[position].isChecked=false
                        }else{
                            holder.couponsCheck.isChecked=false
                        }
                    }*//*
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    return ViewHolder(layoutInflater.inflate(R.layout.dialog_offer_coupons_layout, parent, false))
                }

                *//*private fun onStateChangedListener(checkBox: CheckBox, position: Int): View.OnClickListener {

                    return View.OnClickListener {
                        if (checkBox.isChecked) {
                            selectedPosition = position
                            //apply_coupons.visibility = View.VISIBLE
                            if (couponType.equals("workshop_coupon")) {
                                //Log.e("workshopCouponId==", couponsList[position].id.toString())
                                workshopCouponId = couponsList[position].id.toString()
                            } else if (couponType.equals("product_coupon")) {
                                workshopCouponId = couponsList[position].id.toString()
                            }
                        } else {
                            selectedPosition = -1
                           // apply_coupons.visibility = View.GONE
                        }
                        notifyDataSetChanged()
                    }
                }*//*
            }

            *//*val genericAdapter = GenericAdapter<Models.Coupon>(mcontext, R.layout.dialog_offer_coupons_layout)
            genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
                override fun onClick(view: View, position: Int) {
                }

                override fun onItemClick(view: View, position: Int) {
                    val checkBox = view.findViewById(R.id.coupons_check) as? CheckBox
                    //checkBox?.id = couponsList[position].id


                    if (checkBox!!.isChecked) {

                        Log.e("couponsListChecked::", "${position} ")
                        apply_coupons.visibility = View.VISIBLE

                        *//**//*for (i in 0 until couponsList.size){
                        if (checkBox?.id!!.equals(couponsList[i].id)){

                        }else{
                            couponsList[i].isChecked = false
                        }
                    }*//**//*
                    couponsList[position].isChecked = true
                } else {
                    apply_coupons.visibility = View.GONE
                    couponsList[position].isChecked = false
                    Log.e("couponsListUNChecked::", "${position}")
                }

            }
        })*//*

            imageCross.setOnClickListener {
                dialog.dismiss()
            }


            *//*dialog_recycler_view.adapter = genericAdapter
            genericAdapter.addItems(couponsList)*//*

            cancel_coupons.setOnClickListener {
                dialog.dismiss()
            }
            apply_coupons.setOnClickListener {
                dialog.dismiss()
            }

        }

        dialog.show()
    }*/


    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                val filteredList = JSONArray()

                if (constraint == null || constraint.isEmpty()) {
                    filteredJSONArray = JSONArray()
                    filteredJSONArray = jsonArray

                    filterResults.count = filteredJSONArray.length()
                    filterResults.values = filteredJSONArray


                } else {


                    for (i in 0 until jsonArray.length()) {
                        val json = JSONObject(jsonArray[i].toString())

                        var titleString = json.getString("listino")
                        var productNo = json.getString("products_name")
                        if (titleString.toLowerCase().contains(constraint.toString().toLowerCase()) || productNo.toLowerCase().contains(constraint.toString().toLowerCase())) {

                            filteredList.put(json)
                        }
                        // filteredJSONArray = JSONArray()
                        // filteredJSONArray = filteredList
                        /*with(json) {
                            var titleString = "${optString("listino", "")}${optString("products_name", "")}"

                            *//*var titleString = "${optString("listino", "")} ${optString("products_name", "")} " +
                                    "${optString("front_rear", "")} ${optString("left_right", "")}"
                            titleString = titleString.replace(" null ", " ")*//*

                            if (titleString.toLowerCase().contains(constraint.toString().toLowerCase())) {

                                filteredList.put(json)
                            }
                        }*/
                    }


                    if (filteredList.length() == 0){
                        filteredJSONArray = JSONArray()
                        filteredJSONArray = jsonArray



                        Toast.makeText(mcontext, mcontext.getString(R.string.SearchProductNotFound), Toast.LENGTH_SHORT).show()}
                    else{
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

                    notifyDataSetChanged();
                }


                // Log.e("filteredJSONArray",filteredJSONArray.toString())
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

//        init {
//            if (isWorkshop) icon.setImageResource(R.drawable.workshop1)
//        }

    }

    fun getProductOrWorkshopListAdapter(): Int {

        if (productOrWorkshopList.size != 0 && search_view.query.isNullOrBlank()) {

            productOrWorkshopList = productOrWorkshopListParent

        }

        return productOrWorkshopList.size
    }

    private fun getDistancebetweenTwoLatLong1(currentLatLong: LatLng, latitude: String, longitude: String, tv_workshopKm: TextView) {
        if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank() && currentLatLong != null && !currentLatLong.latitude.equals("0.0") && !currentLatLong.latitude.equals("0.0") && !latitude.equals("0") && !latitude.equals("0")) {
            var currentLocation = Location("")
            currentLocation.latitude = currentLatLong.latitude
            currentLocation.longitude = currentLatLong.longitude
            var workshopLocation = Location("")
            workshopLocation.latitude = latitude.toDouble()
            workshopLocation.longitude = longitude.toDouble()
            var distance = (currentLocation.distanceTo(workshopLocation).roundToInt()) / 1000;
            tv_workshopKm.text = mcontext.getString(R.string.append_km, distance)

        }
    }

    private fun getDistancebetweenTwoLatLong(currentLatLong: LatLng, latitude: String, longitude: String, tv_workshopKm: TextView) {
        if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank() && currentLatLong != null && !currentLatLong.latitude.equals("0.0") && !currentLatLong.latitude.equals("0.0") && !latitude.equals("0") && !latitude.equals("0") && !latitude.equals("0") && !latitude.equals("0")) {
            val Radius = 6371
            var lat1 = currentLatLong.latitude;
            val lat2 = latitude.toDouble();
            val lon1 = currentLatLong.longitude;
            val lon2 = longitude.toDouble();
            var dLat = Math.toRadians(lat2.toDouble() - lat1.toDouble());
            var dLon = Math.toRadians(lon2.toDouble() - lon1.toDouble());
            var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            var c = 2 * Math.asin(Math.sqrt(a));
            var valueResult = Radius * c;
            var km = valueResult / 1;
            var newFormat: DecimalFormat = DecimalFormat("####");
            var kmInDec = Integer.valueOf(newFormat.format(km));
            var meter = valueResult % 1000;
            val meterInDec = Integer.valueOf(newFormat.format(meter));
            Log.d(" test Current Lat long", currentLatLong.latitude.toString() + "  " + currentLatLong.longitude.toString())
            Log.d(" test WorkShop Lat long", latitude + "  " + longitude)

            Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                    + " Meter   " + meterInDec);

            var distance = (Radius * c).roundToInt();
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
    /*  fun getDistancebetweenTwoLatLong(currentLatLong: LatLng, latitude: String, longitude: String, tv_workshopKm: TextView) {
         var parsedDistance = "";
         var response = "";

         var thread = Thread(Runnable {

             try {

                 var lat1 = currentLatLong.latitude;

                 val lon1 = currentLatLong.longitude;
                 var url = URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + latitude.toDouble() + "," + longitude.toDouble() + "&sensor=false&units=metric&mode=driving");
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

         try {
             thread.join();
         } catch (e: InterruptedException) {
             e.printStackTrace();
         }
         Log.d(" test Distance", parsedDistance.toString())
         tv_workshopKm.text = (parsedDistance);
     }

     private fun getDistanceOnRoad(latitude:Double, longitude:Double,
                                   prelatitute:Double, prelongitude:Double):String {
         val result_in_kms = ""
         *//*val url = ("http://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&sensor=false&units=metric")*//*
        var url = URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + latitude + "," + longitude + "&destination=" + prelatitute.toDouble() + "," + prelongitude.toDouble() + "&sensor=false&units=metric&mode=driving");

        val tag = arrayOf<String>("text")
        val response: HttpResponse  ? = null
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
}