package com.officinetop.workshop

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.PaginationListener
import com.officinetop.adapter.ProductOrWorkshopListAdapter
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.utils.Constant.defaultDistance
import com.officinetop.views.FilterListInterface
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
    private var revisonService = false
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
    private var isrimService = false
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
 /*   private var isPriceLowToHigh = true
    private var isDistanceLowToHigh = true*/
     private var isPriceDistance = false
    var serviceID = 0
    private var multipleServiceIdOfCarMaintenance = ""
    private var productID = 0

    private var revisionServiceID = 0
    private var revisionMain_categoryId = ""
    private var motServiceID = 0
    private var motservices_time = ""
    private var assembledProductDetail = ""
    private var isAssembly = false
    private var assmbled_time = ""
    private var hasRecyclerLoadedOnce = false
    private var calendarPriceMap: HashMap<String, String> = HashMap()

    private lateinit var listAdapter: ProductOrWorkshopListAdapter
    private var latitude: String = ""
    private var longitude: String = ""
    private var workshopUserId: String = ""
    private var addressId: String = ""
    private var workshopWreckerId: String = ""
    private var cartItem: Models.CartItem? = null
    private var dataSet: JSONArray = JSONArray()
    private var partidhasMap: java.util.HashMap<String, Models.servicesCouponData> = java.util.HashMap()
    private var motpartlist: java.util.HashMap<String, Models.MotservicesCouponData> = java.util.HashMap()
    private var WorkshopDistanceforDefault = defaultDistance
    private var tyre_mainCategory_id = ""
    private var washing_mainCategory_id = ""
    private var misdistancefilter = false
    private var misclearselection = false
    private lateinit var drawableLeft: Drawable
    private lateinit var drawableRight: Drawable
    private var SelectedCalendarDateIntial: String = ""
    private var pricesFilter = false

    private var servicesAverageTime = ""
    private var mainCategoryId = ""
    private var deliveryDate = ""
    var qutoesUserDescription = ""
    var qutoesUserImage = ""

    private val PAGE_START = 0
    private var totalPage = 29
    private var current_page = PAGE_START
    private var isLastPage = false
    private var isLoading = false
    lateinit var calendarAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var calendarDetailList: MutableList<Models.CalendarPrice> = ArrayList()
    lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        setSupportActionBar(toolbar)
        search_view_layout.visibility = View.GONE
        toolbar_title.text = getString(R.string.Workshop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getLocation()
        filter_btn.setOnClickListener { filterDialog.show() }
        sort_btn.setOnClickListener { sortDialog.show() }

        selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
        if (intent?.hasExtra(Constant.Path.deliveryDate)!!) {
            deliveryDate = intent?.getStringExtra(Constant.Path.deliveryDate)!!
        }
        isAssemblyService = intent?.getBooleanExtra(Constant.Key.is_assembly_service, false)
                ?: false
        revisonService = intent?.getBooleanExtra(Constant.Key.is_revision, false) ?: false
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
        isrimService = intent?.getBooleanExtra(Constant.Key.is_rim_workshop_service, false) ?: false
        if (intent.hasExtra(Constant.Key.cartItem)) {
            // for assembly
            cartItem = intent.getSerializableExtra(Constant.Key.cartItem) as Models.CartItem
        }
        if (intent.hasExtra(Constant.Path.PartID))
            partidhasMap = intent.getSerializableExtra(Constant.Path.PartID) as HashMap<String, Models.servicesCouponData>


        if (isQuotes) {
            if (intent.hasExtra(Constant.Path.serviceQuotesInsertedId))
                quotesServiceQuotesInsertedId = intent.getStringExtra(Constant.Path.serviceQuotesInsertedId)
            if (intent.hasExtra(Constant.Path.mainCategoryId))
                quotesMainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryId)
            if (intent.hasExtra(Constant.Path.categoryId)) {
                serviceID = intent.getStringExtra(Constant.Path.categoryId).toInt()
            }

            if (intent.hasExtra(Constant.Path.qutoesUserDescription)) {
                qutoesUserDescription = intent.getStringExtra(Constant.Path.qutoesUserDescription)
            }
            if (intent.hasExtra(Constant.Path.qutoesUserAttachImage)) {
                qutoesUserImage = intent.getStringExtra(Constant.Path.qutoesUserAttachImage)
            }
        } else if (intent.hasExtra(Constant.Path.washServiceDetails)) {
            val serviceDetail = intent.getSerializableExtra(Constant.Path.washServiceDetails) as Models.ServiceCategory
            serviceID = serviceDetail.id!!
            washing_mainCategory_id = serviceDetail.main_category_id!!
        } else if (isAssemblyService) {
            if (intent.hasExtra(Constant.Path.productId)) {
                productID = intent.getIntExtra(Constant.Path.productId, 0)
            }
            if (intent.hasExtra(Constant.Key.productDetail)) {
                assembledProductDetail = intent.getStringExtra(Constant.Key.productDetail)

                if (!assembledProductDetail.isNullOrBlank()) {
                    val productdetail = Gson().fromJson<Models.ProductDetail>(assembledProductDetail, Models.ProductDetail::class.java)
                    servicesAverageTime = productdetail.serviceAverageTime
                    mainCategoryId = productdetail.mainCategoryId
                }
            }
            if (!cartItem?.Deliverydays.isNullOrBlank()) {
                val DeleviryDate: Date = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(cartItem?.Deliverydays?.toInt()!! + 1))
                val SelectedWorkShopDate = SimpleDateFormat("yyy-MM-dd").parse(selectedFormattedDate)
                if (DeleviryDate > SelectedWorkShopDate) {
                    val dateFormat = SimpleDateFormat("yyy-MM-dd")
                    selectedFormattedDate = dateFormat.format(DeleviryDate)
                }
            }
        } else if (revisonService) {
            if (intent.hasExtra(Constant.Path.revisionServiceDetails)) {
                val revisionServiceDetails = intent.getSerializableExtra(Constant.Path.revisionServiceDetails) as? RevDataSetItem
                if (revisionServiceDetails != null) {
                    revisionServiceID = revisionServiceDetails.id!!
                    revisionMain_categoryId = revisionServiceDetails.main_category
                }
            }
        } else if (isTyreService) {
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
        } else if (isCarMaintenanceService) {
            if (intent.hasExtra(Constant.Path.serviceID)) {
                multipleServiceIdOfCarMaintenance = intent.getStringExtra(Constant.Path.serviceID)
                Log.e("mutlsservice", multipleServiceIdOfCarMaintenance)
            }
            if (!deliveryDate.isNullOrBlank()) {
                val DeleviryDate: Date = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(deliveryDate.toInt() + 1))
                val SelectedWorkShopDate = SimpleDateFormat("yyy-MM-dd").parse(selectedFormattedDate)
                if (DeleviryDate > SelectedWorkShopDate) {
                    val dateFormat = SimpleDateFormat("yyy-MM-dd")
                    selectedFormattedDate = dateFormat.format(DeleviryDate)
                }
            }
            if (intent.hasExtra(Constant.Path.mainCategoryId))
                mainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryId)
        } else if (isMotService) {
            if (intent.hasExtra(Constant.Path.Motpartdata)) {
                motpartlist = intent.getSerializableExtra(Constant.Path.Motpartdata) as HashMap<String, Models.MotservicesCouponData>
            }
            if (intent.hasExtra(Constant.Path.mot_id)) {
                motServiceID = intent.getStringExtra(Constant.Path.mot_id).toInt()
            }
            if (intent.hasExtra(Constant.Path.motservices_time)) {
                motservices_time = intent.getStringExtra(Constant.Path.motservices_time).toString()

            }
            if (intent.hasExtra("mot_type")) {
                mot_type = intent.getStringExtra("mot_type").toString()
            }
            if (intent.hasExtra(Constant.Path.mainCategoryId)) {
                mainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryId).toString()
            }

            if (!deliveryDate.isNullOrBlank()) {
                val DeleviryDate: Date = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(deliveryDate.toInt() + 1))
                val SelectedWorkShopDate = SimpleDateFormat("yyy-MM-dd").parse(selectedFormattedDate)
                if (DeleviryDate > SelectedWorkShopDate) {
                    val dateFormat = SimpleDateFormat("yyy-MM-dd")
                    selectedFormattedDate = dateFormat.format(DeleviryDate)
                }
            }
        } else if (isSOSAppointment || isSOSServiceEmergency) {
            if(isSOSServiceEmergency){
                horizontal_calendar_view.visibility = View.GONE
            }
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
            if (intent.hasExtra(Constant.Path.mainCategoryId)) {
                mainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryId)

            }
        }
        else if(isrimService){
                   Toast.makeText(this,"worksff",Toast.LENGTH_SHORT).show()
                   Log.d("ischeckloading",""+true)
            if (intent.hasExtra(Constant.Key.productDetail)) {
                val rimserviceDetails = intent.getSerializableExtra(Constant.Key.productDetail) as Models.rimProductDetails
                Log.d("ischeckloading",""+rimserviceDetails)

                if (rimserviceDetails != null) {
                    Log.d("ischeckloading",""+true)

                   assmbled_time = rimserviceDetails.assemble_time
                }
            }
            
            
        }

        SelectedCalendarDateIntial = selectedFormattedDate
        defaultCalendarShow()
        createFilterDialog()
        createSortDialog()
        getCalendarMinPriceRange(selectedFormattedDate)
        if (isOnline()) {
            reloadPage()
        } else {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }

        app_bar.post {
            val params = progress_bar.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, app_bar.height / 2)
            progress_bar.layoutParams = params
        }

        app_bar.viewTreeObserver.addOnGlobalLayoutListener {

        }
    }

    private fun getCalendarMinPriceRange(selectedFormattedDate: String) {
        val progressDialog= getProgressDialog(false)
        if (current_page > 5){
            Log.d("check_page_no","no "+current_page)
            progressDialog.show()
        }

        val pricesFinal = priceRangeFinal + 1
        val priceRangeString = "$priceRangeInitial,$pricesFinal"
        val priceSortLevel : Int
        val distanceSortLevel : Int

        if(isPriceDistance){
            priceSortLevel = 1
            distanceSortLevel = 0
        }
        else{
            priceSortLevel = 0
            distanceSortLevel = 1
        }

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
                        ?: "", getSelectedCar()?.carVersionModel?.idVehicle!!, productqty = cartItem?.quantity.toString(), user_lat = getLat(), user_long = getLong(), distance_range = defaultDistance, mainCategoryId = mainCategoryId, servicesAverageTime = servicesAverageTime, serviceId = if (cartItem != null) cartItem?.serviceId!! else "0")


        val revisionServiceCall = RetrofitClient.client.getRevisionCalendar(revisionServiceID, getSelectedCar()?.carVersionModel?.idVehicle!!, selectedFormattedDate, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = revisionMain_categoryId)
        val tyreServiceCall = RetrofitClient.client.getTyreCalendar(serviceID, getSelectedCar()?.carVersionModel?.idVehicle!!, selectedFormattedDate, productqty = cartItem?.quantity.toString(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = tyre_mainCategory_id, service_average_time = if (cartItem != null) cartItem?.servicesAverageTime!! else "", serviceID = if (cartItem != null) cartItem?.serviceId!! else "")

        val quotesCalendarCall = RetrofitClient.client.getQuotesCalendar(serviceID, selectedFormattedDate, ratingString,
                if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, serviceQuotesInsertedId = quotesServiceQuotesInsertedId, mainCategoryId = quotesMainCategoryId, versionId = getSelectedCar()?.carVersion!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal")

        val carMaintenanceCalendarCall = RetrofitClient.client.getCarMaintenanceCalendar(multipleServiceIdOfCarMaintenance,
                selectedFormattedDate, ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, 1, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = mainCategoryId)


        val motServiceCall = RetrofitClient.client.getMotCalendar(motServiceID, selectedFormattedDate, mot_type, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", version_id = getSelectedCar()?.carVersionModel?.idVehicle!!, userId = getUserId(), service_average_time = motservices_time, mainCategoryId = mainCategoryId)

        val sosAppointmentCall = RetrofitClient.client.getSosAppointmentCalendar(workshopUserId, selectedFormattedDate,
                latitude, longitude, serviceID.toString(), if (getSavedSelectedVehicleID().equals("")) getSelectedCar()?.carVersionModel?.idVehicle!! else getSavedSelectedVehicleID(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", version_id = getSelectedCar()?.carVersionModel?.idVehicle!!, mainCategoryId = mainCategoryId, wrecker_service_type = "1")

        val rimServicecall = RetrofitClient.client.getRimCalender(5,0,selectedFormattedDate,assmbled_time,getLat(),getLong(),distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal")

        val callback = object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress_bar.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
                progressDialog.dismiss()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                progress_bar.visibility = View.GONE
                progressDialog.dismiss()
                body?.let {
                    if (isStatusCodeValid(body)) {
                        val dataSet = getDataSetArrayFromResponse(it)
                        val arrayList: MutableList<Models.CalendarPrice> = ArrayList()
                        for (i in 0 until dataSet.length()) {
                            val serviceCategory = Gson().fromJson<Models.CalendarPrice>(dataSet[i].toString(), Models.CalendarPrice::class.java)
                            arrayList.add(serviceCategory)


                            if (calendarPriceMap.containsKey(arrayList[i].date))
                                calendarPriceMap[arrayList[i].date] = arrayList[i].minPrice.toString()

                          val containsDate=  calendarDetailList.find { it.date.contains(serviceCategory.date) }
                            if ( containsDate != null) {
                                var index= calendarDetailList.indexOf(containsDate)
                                calendarDetailList.get(index).minPrice = arrayList[i].minPrice
                            }else{
                                calendarDetailList.add(serviceCategory)
                            }




                          /* var calendarPrice= calendarDetailList.find { it.date.contains(arrayList[i].date)}
                            if ( calendarPrice != null) {
                               var index= calendarDetailList.indexOf(calendarPrice)
                                calendarDetailList.get(index).minPrice = arrayList[i].minPrice
                            }*/
                        }

                      /*  setUpCalendarPrices()
*/
                        //calendarDetailList.addAll(arrayList)
                         if (current_page == 0) {
                             setUpCalendarPrices()
                         } else {
                             calendarAdapter.notifyDataSetChanged()
                             isLoading = false


                         }
                       /* calendarAdapter.notifyDataSetChanged()
                        isLoading = false*/

                    } else {
                        showInfoDialog(getMessageFromJSON(it)) {
                        }
                    }
                    return@let
                }
            }
        }

        if (isAssemblyService) assemblyCall.enqueue(callback)
        else if (revisonService) revisionServiceCall.enqueue(callback)
        else if (isTyreService) tyreServiceCall.enqueue(callback)
        else if (isQuotes) quotesCalendarCall.enqueue(callback)
        else if (isCarMaintenanceService) carMaintenanceCalendarCall.enqueue(callback)
        else if (isMotService) motServiceCall.enqueue(callback)
        else if (isSOSAppointment) sosAppointmentCall.enqueue(callback)
        else if (isCarWash) nonAssemblyCall.enqueue(callback)
        else if (isrimService) rimServicecall.enqueue(callback)
    }

    @SuppressLint("WrongConstant")
    private fun setUpCalendarPrices() {

        calendarAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
                        item_text_middle.text = SimpleDateFormat("d / M", getLocale()).format(formattedDate)
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
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayout.HORIZONTAL
        horizontal_calendar_view.layoutManager = linearLayoutManager
        horizontal_calendar_view.adapter = calendarAdapter
        horizontal_calendar_view.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL))

        if (this::calendarAdapter.isInitialized) {
            horizontal_calendar_view.addOnScrollListener(object : PaginationListener(linearLayoutManager, 5, current_page + 1) {

                override fun loadMoreItems() {
                    isLoading = true
                    current_page += 5
                    if (current_page <= totalPage) {
                        val dateForPagination = getDateWithDayAddInDate(current_page, SelectedCalendarDateIntial)
                        if (!dateForPagination.isNullOrBlank()) {
                            getCalendarMinPriceRange(dateForPagination)

                        }

                    }else{
                        stopLoading(true)
                    }

                }

                override fun isLastPage(): Boolean {

                    return isLastPage
                }


                override fun isLoading(): Boolean {
                    return isLoading
                }
            })
        }

        isLoading = false


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
                    .putExtra("mIsRevision", revisonService)
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
                    .putExtra(Constant.Path.qutoesUserDescription, qutoesUserDescription)
                    .putExtra(Constant.Path.qutoesUserAttachImage, qutoesUserImage)


            )
        }
    }

    private fun loadWorkshops() {
        Log.d("check_boolean_value", ""+isPriceDistance)
        progress_bar.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE
        var workshopType = 1
        if (isAssemblyService)
            workshopType = 2
        val pricesFinal = priceRangeFinal + 1
        var priceRangeString = "$priceRangeInitial,$pricesFinal"
        val priceSortLevel : Int
        val distanceSortLevel  : Int
        if(isPriceDistance){
            priceSortLevel = 1
            distanceSortLevel = 0
        }
        else{
            priceSortLevel = 0
            distanceSortLevel = 1
        }

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
            RetrofitClient.client.getAssemblyWorkshops(productID, selectedFormattedDate, ratingString,
                    if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, workshopType, getSelectedCar()?.carSize
                    ?: "", getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, selectedCarId = getSavedSelectedVehicleID(), productqty = cartItem?.quantity.toString(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", mainCategoryId = mainCategoryId, servicesAverageTime = servicesAverageTime, serviceId = if (cartItem != null) cartItem?.serviceId!! else "0",sort_by_distance = distanceSortLevel)
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
        } else if (revisonService) {
            Log.d("Workshoplis", "Is Revision Yes")
            callRevisionApi(priceRangeString, priceSortLevel,distanceSortLevel, ratingString)
        } else if (isTyreService) {
            Log.d("Date", "DeliveryDate WorkshopList$selectedFormattedDate")
            Log.d("IsTyreAvailable", "yes")
            RetrofitClient.client.getTyreWorkshops(productID, selectedFormattedDate, ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", productqty = cartItem?.quantity.toString(), favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0",
                    service_average_time = cartItem?.servicesAverageTime!!, serviceID = cartItem?.serviceId!!, mainCategoryId = cartItem?.mainCategoryId!!,sort_by_distance = distanceSortLevel)
                    .onCall { networkException, response ->
                        networkException?.let { }
                        response?.let {
                            setWorkshopValues(response)
                        }
                    }
        } else if (isMotService) {
            RetrofitClient.client.getMotWorkshops(motServiceID, mot_type, selectedFormattedDate, ratingString, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, motservices_time, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0", mainCategoryId = mainCategoryId,sort_by_distance =  distanceSortLevel)
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
                    priceSortLevel = priceSortLevel, serviceQuotesInsertedId = quotesServiceQuotesInsertedId, mainCategoryId = quotesMainCategoryId, versionId = getSelectedCar()?.carVersionModel?.idVehicle!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0",sort_by_distance = distanceSortLevel
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
                    priceSortLevel, getSavedSelectedVehicleID(), getUserId(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0", mainCategoryId = mainCategoryId,sort_by_distance = distanceSortLevel).onCall { _, response ->

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
                    priceRange = if (priceRangeFinal == -1) "" else priceRangeString, priceLevel = priceSortLevel, versionId = getSelectedCar()?.carVersionModel?.idVehicle!!, mainCategoryId = mainCategoryId, wrecker_service_type = "1", userid = getUserId(),sort_by_distance = distanceSortLevel).onCall { _, response ->

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
            Log.d("chek_service_id", "" + serviceID.toString())
            RetrofitClient.client.getSOSWorkshopListforEmergency(getBearerToken()
                    ?: "", if (getSavedSelectedVehicleID().equals("")) getSelectedCar()?.carVersionModel?.idVehicle!! else getSavedSelectedVehicleID(), selectedFormattedDate,
                    serviceID.toString(), latitude, longitude, getCurrentTime(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0", rating = ratingString,
                    priceRange = if (priceRangeFinal == -1) "" else priceRangeString, priceLevel = priceSortLevel, versionId = getSelectedCar()?.carVersionModel?.idVehicle!!, mainCategoryId = mainCategoryId, wrecker_service_type = "2", userid = getUserId(),sort_by_distance = distanceSortLevel)
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
        } else if (isrimService) {
            Log.d("chek_service_id", "" + serviceID.toString())
            RetrofitClient.client.getRimWorkshop(selectedFormattedDate,assmbled_time,getLat(),getLong(),distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal",favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0",priceRange = if (priceRangeFinal == -1) "" else priceRangeString, sortPrice = priceSortLevel,sort_by_distance = distanceSortLevel)
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
        }
        else {
            Log.d("ProductOrWorkshopList", "loadWorkshops: serviceID $serviceID -- selectedFormattedDate = $selectedFormattedDate -- ratingString = $ratingString " +
                    "-- priceRangeString = $priceRangeString -- priceSortLevel = $priceSortLevel  -- workshopType $workshopType -- isAssemblyService --  $isAssemblyService")

            RetrofitClient.client.getWorkshops(serviceID, selectedFormattedDate, ratingString,
                    if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, workshopType, getSelectedCar()?.carSize
                    ?: "", getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, selectedCarId = getSavedSelectedVehicleID(), user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0",mainCategoryId = washing_mainCategory_id,sort_by_distance = distanceSortLevel)
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
        var productOrWorkshopList: ArrayList<Models.ProductOrWorkshopList> = ArrayList<Models.ProductOrWorkshopList>()
        if (jsonArray.length() == 0) {
            productOrWorkshopList.clear()
            listAdapter = ProductOrWorkshopListAdapter(productOrWorkshopList, search_view, jsonArray, isCarWash, isSOSAppointment, isMotService, isQuotes, isCarMaintenanceService, isWorkshop, revisonService, isTyreService,isrimService, selectedFormattedDate, this, this, calendarPriceMap, partidhasMap, motpartlist, getLat(), getLong(), motservices_time, mot_type,assmbled_time)
            recycler_view.adapter = listAdapter
            //   Toast.makeText(this,"If is run..."+jsonArray.length(),Toast.LENGTH_LONG).show();
        } else {
            //  Toast.makeText(this,"else is run..."+jsonArray.length(),Toast.LENGTH_LONG).show();
            val gson = GsonBuilder().create()
            productOrWorkshopList = gson.fromJson(jsonArray.toString(), Array<Models.ProductOrWorkshopList>::class.java).toCollection(java.util.ArrayList<Models.ProductOrWorkshopList>())
            productOrWorkshopList.get(0).service_id
            listAdapter = ProductOrWorkshopListAdapter(productOrWorkshopList, search_view, jsonArray, isCarWash, isSOSAppointment, isMotService, isQuotes, isCarMaintenanceService, isWorkshop, revisonService, isTyreService,isrimService, selectedFormattedDate, this, this, calendarPriceMap, partidhasMap, motpartlist, getLat(), getLong(), motservices_time, mot_type,assmbled_time)
            listAdapter.getQuotesIds(quotesServiceQuotesInsertedId, quotesMainCategoryId, qutoesUserDescription, qutoesUserImage)
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
                    filterDialog.price_end_range.text = getString(R.string.prepend_euro_symbol_string, seekbarPriceFinalLimit.toString())
                    filterDialog.price_start_range.text = getString(R.string.prepend_euro_symbol_string, "0")
                    pricesFilter = false
                    Log.d("WorkshopList", "max prices$seekbarPriceFinalLimit")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
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

                    tempPriceInitial = leftValue.toDouble().roundTo2Places().toInt()
                    tempPriceFinal = rightValue.toDouble().roundTo2Places().toInt() /*+ 1*/
                    //  price_start_range.text = getString(R.string.prepend_euro_symbol_string, priceInitialString)
                    price_end_range.text = getString(R.string.prepend_euro_symbol_string, rightValue.toDouble().roundTo2Places().toString())
                    price_start_range.text = getString(R.string.prepend_euro_symbol_string, leftValue.toDouble().roundTo2Places().toString() /*seekPriceInitial.toString()*/)
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

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { dismiss() }

            toolbar_title.text = getString(R.string.sort)
            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar.setOnMenuItemClickListener {

              /*  val priceIndex = radio_grp_price.indexOfChild(radio_grp_price.findViewById(radio_grp_price.checkedRadioButtonId))
                val distanceIndex = radio_grp_distance.indexOfChild(radio_grp_distance.findViewById(radio_grp_distance.checkedRadioButtonId))

                isPriceLowToHigh = priceIndex == 0
                isDistanceLowToHigh = distanceIndex == 0*/


             /*   val pricedistanceIndex = radio_group_price_distance.indexOfChild(radio_group_price_distance.findViewById(radio_group_price_distance.checkedRadioButtonId))
                isPriceDistance = pricedistanceIndex == 0*/

                    isPriceDistance = rb_price.isChecked

                reloadPage()
                dismiss()
                return@setOnMenuItemClickListener true
            }
            tv_Sort_ClearSection.setOnClickListener {
               /* rb_price_low.isChecked = true
                rb_price_high.isChecked = false
                rb_growing.isChecked = true
                rb_decending.isChecked = false*/

                rb_price.isChecked = false
                rb_distance.isChecked = true


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

    private fun callRevisionApi(priceRangeString: String, priceSortLevel: Int,distancesort : Int, ratingformate: String) {
        Log.d("check_lat_long",""+""+getLat()+"  "+getLong())
        RetrofitClient.client.getRevisionWorkshop(revisionServiceID, selectedFormattedDate, ratingformate, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, user_id = getUserId(), selectedCarId = getSavedSelectedVehicleID(), version_id = getSelectedCar()?.carVersionModel?.idVehicle!!, user_lat = getLat(), user_long = getLong(), distance_range = if ((tempDistanceInitial.toString() == "0" && tempDistanceFinal.toString() == "100")) WorkshopDistanceforDefault else "$tempDistanceInitial,$tempDistanceFinal", favorite = if (isFavouriteChecked) "1" else "0", couponfilter = if (isOfferChecked) "1" else "0", mainCategoryId = revisionMain_categoryId,sort_by_distance = distancesort)
                .onCall { networkException, response ->
                    networkException?.let {
                    }
                    response?.let {
                        setWorkshopValues(response)
                    }
                }
    }

    private fun defaultCalendarShow() {

        val sdf = SimpleDateFormat("yyy-MM-dd")
        for (i in 0..4) {
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(SelectedCalendarDateIntial)
            cal.add(Calendar.DATE, i)

            calendarDetailList.add(Models.CalendarPrice(sdf.format(cal.time)))

        }
        setUpCalendarPrices()
    }
}
