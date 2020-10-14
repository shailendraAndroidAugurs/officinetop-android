package com.officinetop.officine.workshop

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.ProductOrWorkshopListAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.views.FilterListInterface
import kotlinx.android.synthetic.main.activity_part_categories.progress_bar
import kotlinx.android.synthetic.main.activity_product_list.*
import kotlinx.android.synthetic.main.dialog_layout_filter.*
import kotlinx.android.synthetic.main.dialog_sorting.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_dateview_selected.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class WorkshopListActivity : BaseActivity(), FilterListInterface {
    private lateinit var filterDialog: Dialog
    private lateinit var sortDialog: Dialog
    var isAssemblyService = false
    private var isRevisonService = false
    private var isWorkshop = false
    private var isTyreService = false
    private var isQuotes = false
    private var isSOSAppointment = false
    private var isSOSServiceEmergency = false
    private var quotesServiceQuotesInsertedId = ""
    private var quotesMainCategoryId = ""
    private var isCarMaintenanceService = false
    private var isMotService = false
    private var isCarWash = false
    private var mot_type = ""
    private val filterBrandList: MutableList<String> = ArrayList()
    var selectedItemPosition = 0
    var selectedFormattedDate = ""
    private var ratingString = ""
    private var priceRangeInitial = 0
    private var priceRangeFinal = -1
    var tempPriceFinal = -1
    var tempPriceInitial = 0
    var tempDistanceFinal = 100
    var tempDistanceInitial = 0
    private var seekbarPriceInitialLimit = 0f
    private var seekbarPriceFinalLimit = 0f
    private var isFavouriteChecked = false
    private var isOfferChecked = false
    private var isPriceLowToHigh = true
    private var isDistanceLowToHigh = true
    var serviceID = 0
    private var multipleServiceIdOfCarMaintenance = ""
    private var productID = 0

    private var revisionServiceID = 0
    private var revisionMain_categoryId = ""
    private var motServiceID = 0
    private var motservices_time = ""
    private var assembledProductDetail = ""
    private var isAssembly = false

    var hasRecyclerLoadedOnce = false
    var calendarPriceMap: HashMap<String, String> = HashMap()

    private lateinit var listAdapter: ProductOrWorkshopListAdapter

    private var latitude: String = ""
    private var longitude: String = ""
    private var workshopUserId: String = ""
    private var addressId: String = ""
    private var workshopWreckerId: String = ""
    private var cartItem: Models.CartItem? = null
    var dataSet: JSONArray = JSONArray()
    private var partidhasMap: java.util.HashMap<String, Models.servicesCouponData> = java.util.HashMap()
    private var motpartlist: java.util.HashMap<String, Models.MotservicesCouponData> = java.util.HashMap()
    private var WorkshopDistanceforDefault = "0,25"
    private var tyre_mainCategory_id = ""
    private var washing_mainCategory_id = ""
    private var misdistancefilter = false
    private var misclearselection = false
    private lateinit var drawableLeft: Drawable
    private lateinit var drawableRight: Drawable
    private var SelectedCalendarDateIntial: String = ""
    private var pricesFilter = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        setSupportActionBar(toolbar)
        search_view_layout.visibility = View.GONE

        toolbar_title.text = getString(R.string.Workshop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        filter_btn.setOnClickListener { filterDialog.show() }
        sort_btn.setOnClickListener { sortDialog.show() }
        selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
        isAssemblyService = intent?.getBooleanExtra(Constant.Key.is_assembly_service, false)
                ?: false
        isRevisonService = intent?.getBooleanExtra(Constant.Key.is_revision, false) ?: false
        isTyreService = intent?.getBooleanExtra(Constant.Key.is_tyre, false) ?: false
        isWorkshop = intent?.getBooleanExtra(Constant.Key.is_workshop, false) ?: false
        isQuotes = intent?.getBooleanExtra(Constant.Key.is_quotes, false) ?: false
        isCarMaintenanceService = intent?.getBooleanExtra(Constant.Key.is_car_maintenance_service, false)
                ?: false
        isMotService = intent?.getBooleanExtra(Constant.Key.is_motService, false) ?: false


        isSOSAppointment = intent?.getBooleanExtra(Constant.Key.is_sos_service, false) ?: false
        isSOSServiceEmergency = intent?.getBooleanExtra(Constant.Key.is_sos_service_emergency, false)
                ?: false
        isCarWash = intent?.getBooleanExtra(Constant.Key.is_car_wash, false) ?: false

        if (intent.hasExtra(Constant.Key.cartItem)) {

            // for assembly
            cartItem = intent.getSerializableExtra(Constant.Key.cartItem) as Models.CartItem
            Log.d("Date", "Deliverydays: WorkshopList" + cartItem?.Deliverydays)


        }
        if (intent.hasExtra(Constant.Path.PartID))
            partidhasMap = intent.getSerializableExtra(Constant.Path.PartID) as HashMap<String, Models.servicesCouponData>

        if (intent.hasExtra(Constant.Path.Motpartdata)) {
            motpartlist = intent.getSerializableExtra(Constant.Path.Motpartdata) as HashMap<String, Models.MotservicesCouponData>
        }



        if (isQuotes) {
            if (intent.hasExtra(Constant.Path.serviceQuotesInsertedId))
                quotesServiceQuotesInsertedId = intent.getStringExtra(Constant.Path.serviceQuotesInsertedId)
            if (intent.hasExtra(Constant.Path.mainCategoryId))
                quotesMainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryId)
        }




        if (intent.hasExtra(Constant.Path.washServiceDetails)) {
            val serviceDetail = intent.getSerializableExtra(Constant.Path.washServiceDetails) as Models.ServiceCategory

            serviceID = serviceDetail.id!!
            washing_mainCategory_id = serviceDetail.main_category_id!!


        }


        if (isAssemblyService) {
            if (intent.hasExtra(Constant.Path.productId)) {
                productID = intent.getIntExtra(Constant.Path.productId, 0)
            }
            if (intent.hasExtra(Constant.Key.productDetail))
                assembledProductDetail = intent.getStringExtra(Constant.Key.productDetail)

            if (!cartItem?.Deliverydays.isNullOrBlank() && (!cartItem?.Deliverydays.equals("0"))) {
                val DeleviryDate: Date = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(cartItem?.Deliverydays?.toInt()!! + 1))
                val SelectedWorkShopDate = SimpleDateFormat("yyy-MM-dd").parse(selectedFormattedDate)
                if (DeleviryDate > SelectedWorkShopDate) {
                    val dateFormat = SimpleDateFormat("yyy-MM-dd")
                    selectedFormattedDate = dateFormat.format(DeleviryDate)

                }

            }
        }


        if (isRevisonService) {
            if (intent.hasExtra(Constant.Path.revisionServiceDetails)) {
                val revisionServiceDetails = intent.getSerializableExtra(Constant.Path.revisionServiceDetails) as? RevDataSetItem
                if (revisionServiceDetails != null) {
                    revisionServiceID = revisionServiceDetails.id!!
                    revisionMain_categoryId = revisionServiceDetails.main_category
                }
            }
        }

        if (isTyreService) {
            if (intent.hasExtra(Constant.Path.productId)) {
                productID = intent.getIntExtra(Constant.Path.productId, 0)
                serviceID = productID
            }
            if (intent.hasExtra("tyre_mainCategory_id")) {
                tyre_mainCategory_id = intent.getStringExtra("tyre_mainCategory_id")

            }
            if (!cartItem?.Deliverydays.isNullOrBlank() && (!cartItem?.Deliverydays.equals("0"))) {
                val DeleviryDate: Date = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(cartItem?.Deliverydays?.toInt()!! + 1))
                val SelectedWorkShopDate = SimpleDateFormat("yyy-MM-dd").parse(selectedFormattedDate)
                if (DeleviryDate > SelectedWorkShopDate) {
                    val dateFormat = SimpleDateFormat("yyy-MM-dd")
                    selectedFormattedDate = dateFormat.format(DeleviryDate)


                }

            }

        }

        if (isQuotes) {
            if (intent.hasExtra(Constant.Path.categoryId)) {
                serviceID = intent.getStringExtra(Constant.Path.categoryId).toInt()
            }
        }

        if (isCarMaintenanceService) {
            if (intent.hasExtra(Constant.Path.serviceID)) {
                multipleServiceIdOfCarMaintenance = intent.getStringExtra(Constant.Path.serviceID)

                Log.e("mutlsservice", multipleServiceIdOfCarMaintenance)
            }
        }


        if (isMotService) {
            if (intent.hasExtra(Constant.Path.mot_id)) {
                motServiceID = intent.getStringExtra(Constant.Path.mot_id).toInt()
                Log.e("mutlsservice", motServiceID.toString())
            }
            if (intent.hasExtra(Constant.Path.motservices_time)) {
                motservices_time = intent.getStringExtra(Constant.Path.motservices_time).toString()
                Log.e("mutlsservice", motservices_time.toString())
            }
            if (intent.hasExtra("mot_type")) {
                mot_type = intent.getStringExtra("mot_type").toString()

            }


        }

        if (isSOSAppointment || isSOSServiceEmergency) {
            if (intent.hasExtra(Constant.Path.serviceID)) {
                serviceID = intent?.getStringExtra(Constant.Path.serviceID)!!.toInt()
            }
            if (intent.hasExtra(Constant.Path.latitude)) {
                latitude = intent?.getStringExtra(Constant.Path.latitude) ?: ""
            }
            if (intent.hasExtra(Constant.Path.longitude)) {
                longitude = intent?.getStringExtra(Constant.Path.longitude) ?: ""
            }
            if (intent.hasExtra(Constant.Path.workshopUsersId)) {
                workshopUserId = intent?.getStringExtra(Constant.Path.workshopUsersId) ?: ""
            }
            if (intent.hasExtra(Constant.Path.workshopFilterSelectedDate)) {
                selectedFormattedDate = intent?.getStringExtra(Constant.Path.workshopFilterSelectedDate)
                        ?: ""
            }
            if (intent.hasExtra(Constant.Path.addressId)) {
                addressId = intent?.getStringExtra(Constant.Path.addressId) ?: ""
            }
            if (intent.hasExtra(Constant.Path.workshopWreckerId)) {
                workshopWreckerId = intent?.getStringExtra(Constant.Path.workshopWreckerId) ?: ""
            }
        }

        SelectedCalendarDateIntial = selectedFormattedDate
        createFilterDialog()
        createSortDialog()

        getCalendarMinPriceRange(selectedFormattedDate)
        reloadPage()

        app_bar.post {
            val params = progress_bar.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, app_bar.height / 2)
            progress_bar.layoutParams = params
        }

        app_bar.viewTreeObserver.addOnGlobalLayoutListener {

        }


    }

    private fun getCalendarMinPriceRange(selectedFormattedDate: String) {
        Log.d("workshop", "calendar api call")
        Log.d("getCalendarMin", "getcalendarMiniPricesRange")
        val pricesFinal = priceRangeFinal + 1
        val priceRangeString = "$priceRangeInitial,$pricesFinal"
        val priceSortLevel = if (isPriceLowToHigh) 1 else 2

        var workshopType = 1
        if (isAssemblyService)
            workshopType = 2

        val nonAssemblyCall = RetrofitClient.client.getCalendarMinPrice(serviceID, productID, selectedFormattedDate,
                ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, workshopType,
                getSelectedCar()?.carSize
                        ?: "", user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = washing_mainCategory_id)

        val assemblyCall = RetrofitClient.client.getAssemblyCalendarPrice(serviceID, productID, selectedFormattedDate,
                ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, workshopType,
                getSelectedCar()?.carSize
                        ?: "", getSelectedCar()?.carVersionModel?.idVehicle!!, productqty = cartItem?.quantity.toString())


        val revisionServiceCall = RetrofitClient.client.getRevisionCalendar(revisionServiceID, getSelectedCar()?.carVersionModel?.idVehicle!!, selectedFormattedDate, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = revisionMain_categoryId)
        val tyreServiceCall = RetrofitClient.client.getTyreCalendar(serviceID, getSelectedCar()?.carVersionModel?.idVehicle!!, selectedFormattedDate, productqty = cartItem?.quantity.toString(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = tyre_mainCategory_id)

        val quotesCalendarCall = RetrofitClient.client.getQuotesCalendar(serviceID, selectedFormattedDate, ratingString,
                if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, serviceQuotesInsertedId = quotesServiceQuotesInsertedId, mainCategoryId = quotesMainCategoryId, versionId = getSelectedCar()?.carVersion!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal")

        val carMaintenanceCalendarCall = RetrofitClient.client.getCarMaintenanceCalendar(multipleServiceIdOfCarMaintenance,
                selectedFormattedDate, ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, 1, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal")

        val motServiceCall = RetrofitClient.client.getMotCalendar(motServiceID, selectedFormattedDate, mot_type, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal")

        val sosAppointmentCall = RetrofitClient.client.getSosAppointmentCalendar(workshopUserId, selectedFormattedDate,
                latitude, longitude, serviceID.toString(), if (getSavedSelectedVehicleID().equals("")) getSelectedCar()?.carVersionModel?.idVehicle!! else getSavedSelectedVehicleID(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", version_id = getSelectedCar()?.carVersionModel?.idVehicle!!)

        val callback = object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress_bar.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                progress_bar.visibility = View.GONE
                body?.let {

                    if (isStatusCodeValid(body)) {
                        val dataSet = getDataSetArrayFromResponse(it)

                        val arrayList: MutableList<Models.CalendarPrice> = ArrayList()
                        for (i in 0 until dataSet.length()) {
                            val serviceCategory = Gson().fromJson<Models.CalendarPrice>(dataSet[i].toString(), Models.CalendarPrice::class.java)
                            arrayList.add(serviceCategory)
                            calendarPriceMap[arrayList[i].date] = arrayList[i].minPrice.toString()

                        }
                        setUpCalendarPrices(arrayList)
                    } else {
                        showInfoDialog(getMessageFromJSON(it)) {
                        }
                    }
                    return@let
                }

            }
        }

        if (isAssemblyService) assemblyCall.enqueue(callback)
        else if (isRevisonService) revisionServiceCall.enqueue(callback)
        else if (isTyreService) tyreServiceCall.enqueue(callback)
        else if (isQuotes) quotesCalendarCall.enqueue(callback)
        else if (isCarMaintenanceService) carMaintenanceCalendarCall.enqueue(callback)
        else if (isMotService) motServiceCall.enqueue(callback)
        else if (isSOSAppointment) sosAppointmentCall.enqueue(callback)
        else if (isCarWash) nonAssemblyCall.enqueue(callback)

    }

    private fun setUpCalendarPrices(calendarDetailList: MutableList<Models.CalendarPrice>) {


        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {

                val selectedView = layoutInflater.inflate(R.layout.item_dateview_selected, p0, false)
                val unselectedView = layoutInflater.inflate(R.layout.item_dateview_unselected, p0, false)

                return if (p1 == 0) object : RecyclerView.ViewHolder(selectedView) {}
                else object : RecyclerView.ViewHolder(unselectedView) {}

            }

            override fun getItemViewType(position: Int): Int {
                return if (position == selectedItemPosition) 0 else 1
            }

            override fun getItemCount(): Int = calendarDetailList.size

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, position: Int) {

                with(p0.itemView) {
                    val dateString = calendarDetailList[position].date

                    setOnClickListener {
                        hasRecyclerLoadedOnce = false
                        selectedItemPosition = position
                        selectedFormattedDate = dateString
                        reloadPage()
                        notifyDataSetChanged()

                    }

                    val sdf = SimpleDateFormat("yyyy-MM-dd", getLocale())
                    try {
                        val formattedDate = sdf.parse(dateString)
                        item_text_middle.text = SimpleDateFormat("d - M", getLocale()).format(formattedDate)
                        val format2 = SimpleDateFormat("E", getLocale())
                        val finalDay = format2.format(formattedDate)

                        item_text_top.text = finalDay

                    } catch (ex: Exception) {
                        Log.v("Exception", ex.localizedMessage)
                    }
                    val minimumPrice = calendarDetailList[position].minPrice.toString()
                    item_text_bottom.text = getString(R.string.prepend_euro_symbol_string, minimumPrice)

                }
            }

        }
        horizontal_calendar_view.adapter = adapter
        horizontal_calendar_view.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL))
        // horizontal_calendar_view.smoothScrollToPosition(selectedItemPosition)

    }

    private fun reloadPage() {


        if (!isSOSServiceEmergency)
            horizontal_calendar_layout.visibility = View.VISIBLE

        loadWorkshops()

        // Click for map filter
        map_btn.setOnClickListener {

            var workshopCategoryDetail = (convertToJson(intent.getSerializableExtra(Constant.Path.washServiceDetails)
                    ?: Any()))


            if (intent.getBooleanExtra(Constant.Key.is_tyre, false)) {
                workshopCategoryDetail = (convertToJson(intent.getSerializableExtra(Constant.Key.productDetail)
                        ?: Any()))
            }


            startActivity(intentFor<MapFilterActivity>().putExtra("WorkshopList", dataSet.toString())
                    .putExtra("isSOSAppointment", isSOSAppointment)
                    .putExtra("isMotService", isMotService)
                    .putExtra("isQuotes", isQuotes)
                    .putExtra("isCarMaintenanceServices", isCarMaintenanceService)
                    .putExtra("mIsWorkshop", isWorkshop)
                    .putExtra("mIsRevision", isRevisonService)
                    .putExtra("mIsTyre", isTyreService)
                    .putExtra("motservicesTime", motservices_time)
                    .putExtra("calendarPriceMap", calendarPriceMap)
                    .putExtra("partidhasMap", partidhasMap)
                    .putExtra("motpartlist", motpartlist)
                    .putExtra("selectedFormattedDate", selectedFormattedDate)
                    .putExtra("workshopCategoryDetail", workshopCategoryDetail)
                    .putExtra("isAssembly", isAssembly)
                    .putExtra("cartItem", cartItem)
                    .putExtra("isCarWash", isCarWash)
                    .putExtra("mot_type", mot_type)


            )


        }

    }

    private fun loadWorkshops() {
        Log.d("workshoplist", "loadworkshop call")
        progress_bar.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE

        var workshopType = 1
        if (isAssemblyService)
            workshopType = 2
        val pricesFinal = priceRangeFinal + 1
        var priceRangeString = "$priceRangeInitial,$pricesFinal"
        val priceSortLevel = if (isPriceLowToHigh) 1 else 2

        if (isFavouriteChecked || isOfferChecked || !ratingString.equals("") || misdistancefilter || pricesFilter) {
            Log.d("WorkshopList", "FilterDot : " + "yes")
            this@WorkshopListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, drawableRight, null)
        } else {
            Log.d("reloadpage", "drawableleft")
            this@WorkshopListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, null, null)

        }

        if (pricesFilter) {
            Log.d("filterApply", "pricesFilter")
        }
        if (misdistancefilter) {
            Log.d("filterApply", "misdistancefilter")
        }

        if (misclearselection) {
            Log.d("filterApply", "misclearselection")
        }


        if (!hasRecyclerLoadedOnce) {
            priceRangeString = ""
        }

        if (isAssemblyService) {

            Log.d("ProductOrWorkshopList", "loadWorkshops: productID $productID -- selectedFormattedDate = $selectedFormattedDate -- ratingString = $ratingString " +
                    "-- priceRangeString = $priceRangeString -- priceSortLevel = $priceSortLevel  -- workshopType $workshopType -- isAssemblyService --  $isAssemblyService")

            RetrofitClient.client.getAssemblyWorkshops(productID, selectedFormattedDate, ratingString,
                    if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, workshopType, getSelectedCar()?.carSize
                    ?: "", getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, selectedCarId = getSavedSelectedVehicleID(), productqty = cartItem?.quantity.toString(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal")
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progress_bar.visibility = View.GONE
                            recycler_view.visibility = View.VISIBLE
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            Log.d("ProductOrWorkshopList", "loadAssemblyWorkshops: onResponse $response  -- isAssemblyService --  $isAssemblyService")

                            setWorkshopValues(response)
                        }
                    })
        } else if (isRevisonService) {
            Log.d("Workshoplis", "Is Revision Yes")
            CallRevisionApi(priceRangeString, priceSortLevel, ratingString)
        } else if (isTyreService) {
            Log.d("Date", "DeliveryDate WorkshopList$selectedFormattedDate")
            Log.d("IsTyreAvailable", "yes")

            RetrofitClient.client.getTyreWorkshops(productID, selectedFormattedDate, ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", productqty = cartItem?.quantity.toString(), favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0")
                    .onCall { networkException, response ->
                        networkException?.let { }
                        response?.let {
                            setWorkshopValues(response)
                        }
                    }
        } else if (isMotService) {
            RetrofitClient.client.getMotWorkshops(motServiceID, mot_type, selectedFormattedDate, ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, motservices_time, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0")
                    .onCall { networkException, response ->
                        networkException?.let { }
                        response?.let {
                            setWorkshopValues(response)
                        }
                    }
        } else if (isQuotes) {
            Log.d("workshopList", "isQuotes peramter : " + "categoryType : " + serviceID + " workshopFilterSelectedDate : " + selectedFormattedDate + " rating : " + ratingString + " priceRange : " + " " + "priceSortLevel : " + priceSortLevel + " serviceQuotesInsertedId : " + quotesServiceQuotesInsertedId + " mainCategoryId : " + quotesMainCategoryId + " versionId : " + getSelectedCar()?.carVersionModel?.idVehicle!!)
            RetrofitClient.client.getQuotesWorkshops(
                    authToken = getBearerToken()
                            ?: "", categoryType = serviceID, workshopFilterSelectedDate = selectedFormattedDate,
                    rating = ratingString, priceRange = if (priceRangeFinal == -1) "" else priceRangeString,
                    priceSortLevel = priceSortLevel, serviceQuotesInsertedId = quotesServiceQuotesInsertedId, mainCategoryId = quotesMainCategoryId, versionId = getSelectedCar()?.carVersionModel?.idVehicle!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0"
            ).onCall { _, response ->

                response?.let {
                    progress_bar.visibility = View.GONE
                    if (response.isSuccessful) {

                        try {
                            setWorkshopValues(response)

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else if (isCarMaintenanceService) {
            RetrofitClient.client.getCarMaintenanceWorkshop(getSelectedCar()?.carVersionModel?.idVehicle!!,
                    "en", selectedFormattedDate, multipleServiceIdOfCarMaintenance, ratingString, if (priceRangeFinal == -1) "" else priceRangeString,
                    priceSortLevel, getSavedSelectedVehicleID(), getUserId(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0").onCall { _, response ->

                response?.let {
                    progress_bar.visibility = View.GONE
                    if (response.isSuccessful) {

                        try {
                            setWorkshopValues(response)

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else if (isSOSAppointment) {

            RetrofitClient.client.getSOSWorkshopListforAppointment(getBearerToken()
                    ?: "", if (getSavedSelectedVehicleID().equals("")) getSelectedCar()?.carVersionModel?.idVehicle!! else getSavedSelectedVehicleID(), selectedFormattedDate, serviceID.toString(),
                    latitude, longitude, distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0", rating = ratingString,
                    priceRange = if (priceRangeFinal == -1) "" else priceRangeString, priceLevel = priceSortLevel, versionId = getSelectedCar()?.carVersionModel?.idVehicle!!).onCall { _, response ->

                response?.let {
                    progress_bar.visibility = View.GONE
                    if (response.isSuccessful) {

                        try {
                            setWorkshopValues(response)


                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        } else if (isSOSServiceEmergency) {
            RetrofitClient.client.getSOSWorkshopListforEmergency(getBearerToken()
                    ?: "", if (getSavedSelectedVehicleID().equals("")) getSelectedCar()?.carVersionModel?.idVehicle!! else getSavedSelectedVehicleID(), selectedFormattedDate,
                    serviceID.toString(), latitude, longitude, getCurrentTime(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0", rating = ratingString,
                    priceRange = if (priceRangeFinal == -1) "" else priceRangeString, priceLevel = priceSortLevel, versionId = getSelectedCar()?.carVersionModel?.idVehicle!!)
                    .onCall { _, response ->
                        response?.let {
                            progress_bar.visibility = View.GONE
                            if (response.isSuccessful) {

                                try {
                                    setWorkshopValues(response)

                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
        } else {
            Log.d("ProductOrWorkshopList", "loadWorkshops: serviceID $serviceID -- selectedFormattedDate = $selectedFormattedDate -- ratingString = $ratingString " +
                    "-- priceRangeString = $priceRangeString -- priceSortLevel = $priceSortLevel  -- workshopType $workshopType -- isAssemblyService --  $isAssemblyService")

            RetrofitClient.client.getWorkshops(serviceID, selectedFormattedDate, ratingString,
                    if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, workshopType, getSelectedCar()?.carSize
                    ?: "", getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, selectedCarId = getSavedSelectedVehicleID(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0")
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progress_bar.visibility = View.GONE
                            recycler_view.visibility = View.VISIBLE
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            Log.d("ProductOrWorkshopList", "loadWorkshops: onResponse $response  -- isAssemblyService --  $isAssemblyService")
                            setWorkshopValues(response)
                        }
                    })
        }

    }

    private fun setWorkshopValues(response: Response<ResponseBody>) {
        try {
            val body = response.body()?.string()
            progress_bar.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE

            body?.let {

                if (isStatusCodeValid(body)) {
                    dataSet = getDataSetArrayFromResponse(it)
                    Log.d("ProductOrWorkshopList", "setWorkshopValues: dataSet $dataSet  -- isAssemblyService --  $isAssemblyService")
                    bindRecyclerView(dataSet)
                } else {
                    bindRecyclerView(JSONArray())
                    showInfoDialog(getMessageFromJSON(it)) {
                    }
                }
                return@let
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindRecyclerView(jsonArray: JSONArray) {

        calendarPriceMap = HashMap()
        val gson = GsonBuilder().create()
        val productOrWorkshopList: ArrayList<Models.ProductOrWorkshopList> = gson.fromJson(jsonArray.toString(), Array<Models.ProductOrWorkshopList>::class.java).toCollection(java.util.ArrayList<Models.ProductOrWorkshopList>())
        listAdapter = ProductOrWorkshopListAdapter(productOrWorkshopList, search_view, jsonArray, isCarWash, isSOSAppointment, isMotService, isQuotes, isCarMaintenanceService, isWorkshop, isRevisonService, isTyreService, selectedFormattedDate, this, this, calendarPriceMap, partidhasMap, motpartlist, getLat(), getLong(), motservices_time, mot_type)
        listAdapter.getQuotesIds(quotesServiceQuotesInsertedId, quotesMainCategoryId)
        if (intent.hasExtra(Constant.Key.cartItem))
            listAdapter.getCartItem(cartItem!!)

        if (isSOSAppointment || isSOSServiceEmergency) {

            listAdapter.userLocationSOS(isSOSServiceEmergency, latitude, longitude, serviceID, workshopUserId, addressId, workshopWreckerId)
        }
        listAdapter.setWorkshopCategory(convertToJson(intent.getSerializableExtra(Constant.Path.washServiceDetails)
                ?: Any()))

        intent.printValues(localClassName)

        if (intent.getBooleanExtra(Constant.Key.is_assembly_service, false)) {
            isAssembly = true
            listAdapter.setAssembledProduct(assembledProductDetail)
        }
        if (intent.getBooleanExtra(Constant.Key.is_tyre, false)) {
            listAdapter.setWorkshopCategory(convertToJson(intent.getSerializableExtra(Constant.Key.productDetail)
                    ?: Any()))
        }

        recycler_view.adapter = listAdapter

        if (hasRecyclerLoadedOnce)
            return

        val list: MutableList<Float> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val json = JSONObject(jsonArray[i].toString())
            val priceString = if (!json.isNull("services_price")) json.optString("services_price") else ""
            list.add(if (priceString.isEmpty()) 0f else priceString.replace(",", "").trim().toFloat())
            seekbarPriceInitialLimit = if (!json.isNull("min_price")) json.optString("min_price", "0").toFloat() else 0f
            seekbarPriceFinalLimit = if (!json.isNull("max_price")) json.optString("max_price", "0").toFloat() else 0f
            Log.d("WorkshopList", "max prices" + seekbarPriceFinalLimit.toString() + jsonArray.length().toString())
        }
        if (jsonArray.length() > 0) {
            try {
                hasRecyclerLoadedOnce = true

                filterDialog.dialog_price_range.setRange(0f, seekbarPriceFinalLimit)
                filterDialog.dialog_price_range.setValue(0f, seekbarPriceFinalLimit)
                //  filterDialog.clear_selection.callOnClick()
                filterDialog.price_end_range.text = getString(R.string.prepend_euro_symbol_string, seekbarPriceFinalLimit.toString())
                filterDialog.price_start_range.text = getString(R.string.prepend_euro_symbol_string, "0")


                /*filterDialog.dialog_distance_range.setValue(0f, 25f)
                misdistancefilter = false

                filterDialog.distance_end_range.text = getString(R.string.append_km, 25)
                filterDialog.distance_start_range.text = getString(R.string.append_km, 0)*/

                pricesFilter = false


                Log.d("WorkshopList", "max prices$seekbarPriceFinalLimit")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }

        Log.d("ProductOrWorkshop", "bindRecyclerView: price range = $seekbarPriceInitialLimit - $seekbarPriceFinalLimit")
    }

    private fun createFilterDialog() {
        filterDialog = Dialog(this, R.style.DialogSlideAnimStyle)
        val brandFilterAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null


        drawableLeft = ContextCompat.getDrawable(this@WorkshopListActivity, R.drawable.ic_sort_black_24dp)!!
        drawableRight = ContextCompat.getDrawable(this@WorkshopListActivity, R.drawable.shape_circle_orange_8dp)!!

        drawableRight.setBounds(100, 100, 100, 100)

        with(filterDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_layout_filter)

            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            Log.d("createFilterDialog", "createFilterDialog created")
            dialog_price_range.setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {


                    tempPriceInitial = leftValue.toInt()
                    tempPriceFinal = rightValue.toInt() /*+ 1*/
                    //  price_start_range.text = getString(R.string.prepend_euro_symbol_string, priceInitialString)
                    price_end_range.text = getString(R.string.prepend_euro_symbol_string, rightValue.toString())
                    price_start_range.text = getString(R.string.prepend_euro_symbol_string, leftValue.toString() /*seekPriceInitial.toString()*/)
                    pricesFilter = true

                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
            })
            filterDialog.dialog_distance_range.setValue(0f, 25f)
            dialog_distance_range.setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                    tempDistanceInitial = leftValue.toInt()
                    tempDistanceFinal = rightValue.toInt() /*+ 1*/

                    distance_end_range.text = getString(R.string.append_km, tempDistanceFinal)
                    distance_start_range.text = getString(R.string.append_km, tempDistanceInitial)
                    misdistancefilter = true
                    Log.d("WorkshopList", "FilterDot : " + "4" + "  distance filtercall")
                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
            })

            toolbar.setOnMenuItemClickListener {

                ratingString = ""

                if (dialog_rating_five.isChecked)
                    ratingString += "5,"
                if (dialog_rating_four.isChecked)
                    ratingString += "4,"
                if (dialog_rating_three.isChecked)
                    ratingString += "3,"
                if (dialog_rating_two.isChecked)
                    ratingString += "2,"
                if (dialog_rating_one.isChecked)
                    ratingString += "1,"

                isFavouriteChecked = dialog_favourite_check_box.isChecked
                isOfferChecked = dialog_offers_check_box.isChecked

                if (ratingString.isNotEmpty()) {
                    if (ratingString.toCharArray()[ratingString.lastIndex] == ',')
                        ratingString = ratingString.substring(0, ratingString.lastIndex).trim()
                }


                priceRangeInitial = tempPriceInitial
                priceRangeFinal = tempPriceFinal


                reloadPage()
                if (misdistancefilter || misclearselection) {
                    Log.d("getCalendarMin", "misdistancefilter : " + misdistancefilter.toString() + " misclearselection : " + misclearselection.toString())
                    Log.d("getCalendarMin", "distance filter or clear selection")
                    getCalendarMinPriceRange(SelectedCalendarDateIntial)
                }

                dismiss()
                return@setOnMenuItemClickListener true
            }
            toolbar.setNavigationOnClickListener { dismiss() }

            dialog_distance_layout.visibility = View.VISIBLE
            dialog_distance_range.visibility = View.VISIBLE

            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar_title.text = getText(R.string.filter)

            setCheckedListener(dialog_layout_rating_five, dialog_rating_five)
            setCheckedListener(dialog_layout_rating_four, dialog_rating_four)
            setCheckedListener(dialog_layout_rating_three, dialog_rating_three)
            setCheckedListener(dialog_layout_rating_two, dialog_rating_two)
            setCheckedListener(dialog_layout_rating_one, dialog_rating_one)

            setCheckedListener(dialog_layout_favourite, dialog_favourite_check_box)
            setCheckedListener(dialog_layout_offers, dialog_offers_check_box)

            dialog_brand_layout.visibility = View.GONE

            clear_selection.setOnClickListener {

                priceRangeFinal = -1
                try {

                    dialog_rating_five.isChecked = false
                    dialog_rating_four.isChecked = false
                    dialog_rating_three.isChecked = false
                    dialog_rating_two.isChecked = false
                    dialog_rating_one.isChecked = false
                    //reset other categories
                    dialog_favourite_check_box.isChecked = false
                    dialog_offers_check_box.isChecked = false
                    filterDialog.dialog_price_range.setValue(0f, seekbarPriceFinalLimit)
                    filterDialog.price_end_range.text = getString(R.string.prepend_euro_symbol_string, seekbarPriceFinalLimit.toString())
                    filterDialog.price_start_range.text = getString(R.string.prepend_euro_symbol_string, "0")
                    filterBrandList.clear()

                    brandFilterAdapter?.notifyDataSetChanged()
                    filterDialog.dialog_distance_range.setValue(0f, 25f)

                    filterDialog.distance_end_range.text = getString(R.string.append_km, 25)
                    filterDialog.distance_start_range.text = getString(R.string.append_km, 0)
                    misclearselection = true
                    misdistancefilter = false
                    pricesFilter = false
                    Log.d("clear_selection", "clear_selection")

                } catch (e: Exception) {

                }

            }
            misdistancefilter = false
            pricesFilter = false
            misclearselection = false
            create()
        }

    }

    private fun createSortDialog() {
        sortDialog = Dialog(this@WorkshopListActivity, R.style.DialogSlideAnimStyle)




        with(sortDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_sorting)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            //Show distance sort option only if workshop
            sort_distance_container.visibility = View.VISIBLE

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { dismiss() }

            toolbar_title.text = getString(R.string.sort)
            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar.setOnMenuItemClickListener {

                val priceIndex = radio_grp_price.indexOfChild(radio_grp_price.findViewById(radio_grp_price.checkedRadioButtonId))
                val distanceIndex = radio_grp_distance.indexOfChild(radio_grp_distance.findViewById(radio_grp_distance.checkedRadioButtonId))

                isPriceLowToHigh = priceIndex == 0
                isDistanceLowToHigh = distanceIndex == 0

                reloadPage()
                dismiss()
                return@setOnMenuItemClickListener true
            }
            create()

        }
    }

    private fun setCheckedListener(layout: View, checkbox: CheckBox, onCheckedChanged: ((isChecked: Boolean) -> Unit)? = null) {
        layout.setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked //onCheckedChanged?.invoke(checkbox.isChecked)
        }
        checkbox.setOnCheckedChangeListener { _, isChecked -> onCheckedChanged?.invoke(isChecked) }
    }

    override fun onListFiltered(jsonArray: JSONArray) {
        listAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && requestCode == 111) {
            val query = data?.getStringExtra("query")
            recycler_view.snackbar(getString(R.string.Searchingresultfor) + " \"$query\"")
            search_view.setQuery(query, true)

        } else {
            search_view.setQuery("", true)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun CallRevisionApi(priceRangeString: String, priceSortLevel: Int, ratingformate: String) {
        Log.d("Revision", "DistanceInitial$tempDistanceInitial")
        Log.d("Revision", "DistanceFinal$tempDistanceFinal")
        RetrofitClient.client.getRevisionWorkshop(revisionServiceID, selectedFormattedDate, ratingformate, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, user_id = getUserId(), selectedCarId = getSavedSelectedVehicleID(), version_id = getSelectedCar()?.carVersionModel?.idVehicle!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0")
                .onCall { networkException, response ->
                    networkException?.let {
                    }
                    response?.let {
                        setWorkshopValues(response)
                    }
                }
    }
}
