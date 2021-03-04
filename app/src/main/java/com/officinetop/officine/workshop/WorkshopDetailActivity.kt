package com.officinetop.officine.workshop

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.slider.library.Indicators.PagerIndicator
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.daimajia.slider.library.Tricks.ViewPagerEx
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.*
import com.officinetop.officine.feedback.FeedbackListActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.views.DialogTouchImageSlider
import com.officinetop.officine.views.TimeWheelPicker
import kotlinx.android.synthetic.main.activity_workshop_detail.*
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.dialog_booking_calendar.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_calendar_day.view.*
import kotlinx.android.synthetic.main.item_intervention_setting.view.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WorkshopDetailActivity : BaseActivity(), OnGetFeedbacks {
    private var bookServicesWithoutCoupon = false
    private var PHONE_CALL_RC = 50001
    private var workshopPhoneNumber = ""
    private lateinit var imageDialog: Dialog
    lateinit var dialogSlider: SliderLayout
    var serviceID = ""
    private var mainCategoryId = ""
    private var servicesAverageTime = ""
    private var hourlyRate = ""
    var disableSliderTouch = false
    var averageServiceTime = 0.0
    var categoryID = ""
    private var specialConditionId = ""
    private var discountType = ""
    private var discountPrices = ""
    private var maxAppointment = ""
    private var slotStartTime = ""
    private var slotEndTime = ""
    private var slotId = ""
    var selectedDateFilter = ""
    var calendarSelectedDateFilter = ""
    var currentWorkshopDetail = ""
    private var isSparePartAssembly = false
    var isRevision = false
    var isAssembly = false
    var isTyre = false
    var isSOSService = false
    private var isCarWash = false
    private var isSosEmergency: Boolean = false
    var couponList: List<Models.Coupon>? = null
    var isCarMaintenanceService = false
    var isQuotesService = false
    private var qutoesUserDescription = ""
    private var quotesUserImage = ""
    private var isMotService = false
    private var productID = ""
    var productQuantity = ""
    private var motservicesaveragetime = ""
    var mainCategoryIDForAssembly: String = ""
    private var mainCategoryIDForCarWash: String = ""
    var calendarPriceMap: HashMap<String, String> = HashMap()
    lateinit var today: LocalDate
    lateinit var bookingCalendar: CalendarView
    var previousSelectedDate: LocalDate? = null
    private var quotesWorkshopUsersDaysId: String = ""
    private var quotesServiceQuotesInsertedId: String = ""
    private var quotesMainCategoryId: String = ""
    private var quotesServicesAvarageTime: String = ""
    private var workshopUsersId: Int = 0
    private var workshopCategoryId: String = ""//var workshopCategoryId:Int , change type to String because for car maintenance services id's
    private var sosUserLatitude: String = ""
    private var sosUserLongitude: String = ""
    private var sosServiceId: String = ""
    private var motServiceId: Int = 0
    private var workshopWreckerId: String = ""
    private var addressId: String = ""
    private var serviceSpecification: JSONArray = JSONArray()
    var serviceSpecArray: JSONArray = JSONArray()
    private var motServiceSpecArray: JSONArray = JSONArray()
    var carMaintenanceServicePrice: String = ""
    var carMaintenanceServiceId: String = ""
    private var workshopCouponId: String = ""
    private var company_name: String = ""
    private var startTimeBooking = ""
    private var endTimeBooking = ""
    private var motType = ""
    private var type: String = ""//type for product type, type for tyre is 2, type for spare parts for 1 and type for rim 3
    private var cartItem: Models.CartItem? = null
    private var partidMap: java.util.HashMap<String, Models.servicesCouponData> = java.util.HashMap<String, Models.servicesCouponData>()
    private var motpartlist: java.util.HashMap<String, Models.MotservicesCouponData> = java.util.HashMap<String, Models.MotservicesCouponData>()
    var wishList = "0"
    private var WorkshopJson: JSONObject = JSONObject()
    private var feedbackMainCategoryId = ""
    private var feedbackServicesId = ""
    var maintenanceServiceJson: JSONArray = JSONArray()
    var servicesPrice = ""
    var serviceAveragetime: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workshop_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = resources.getString(R.string.workshop_detail)
        createImageSliderDialog()
        AndroidThreeTen.init(this)

        intent.printValues(localClassName)

        if (intent != null && intent.hasExtra(Constant.Path.mainCategoryIdCar_wash))
            mainCategoryIDForCarWash = intent.getStringExtra(Constant.Path.mainCategoryIdCar_wash)

        // get selected Date from workshop list calendar screen
        if (intent != null && intent.hasExtra(Constant.Path.workshopFilterSelectedDate))
            selectedDateFilter = intent.getStringExtra(Constant.Path.workshopFilterSelectedDate)

        if (intent != null && intent.hasExtra(Constant.Path.couponId))
            workshopCouponId = intent.getStringExtra(Constant.Path.couponId)


        // Intent for revision workshop
        if (intent.hasExtra(Constant.Key.is_revision))
            isRevision = intent.getBooleanExtra(Constant.Key.is_revision, false)

        //Intent for tyre workshop
        if (intent.hasExtra(Constant.Key.is_tyre))
            isTyre = intent.getBooleanExtra(Constant.Key.is_tyre, false)
        if (intent.hasExtra("QutoesServiceAverageTime"))
            quotesServicesAvarageTime = intent.getStringExtra("QutoesServiceAverageTime")
        if (intent.hasExtra("WorkshopJson")) {
            val JsonString = intent.getStringExtra("WorkshopJson")
            WorkshopJson = JSONObject(JsonString)
            serviceAveragetime = if (WorkshopJson.has("service_average_time") && WorkshopJson.getString("service_average_time") != null) WorkshopJson.getString("service_average_time") else ""
            servicesPrice = if (WorkshopJson.has("services_price") && WorkshopJson.getString("services_price") != null) WorkshopJson.getString("services_price") else ""
            maintenanceServiceJson = if (WorkshopJson.has("services") && WorkshopJson.getJSONArray("services") != null) WorkshopJson.getJSONArray("services") else JSONArray()
            Log.d("servicejson", "" + maintenanceServiceJson)
        }


        if (WorkshopJson != null) {
            servicesAverageTime = WorkshopJson.optString("service_average_time")
            Log.d("service_average_time1: ", WorkshopJson.optString("service_average_time"))
            maxAppointment = WorkshopJson.optString("max_appointment")
            hourlyRate = WorkshopJson.optString("hourly_rate")
            mainCategoryId = WorkshopJson.optString("main_category_id")
        }

        if (intent.hasExtra(Constant.Key.is_assembly_service)) {
            isAssembly = intent.getBooleanExtra(Constant.Key.is_assembly_service, false)
            isSparePartAssembly = true
        }

        isSOSService = intent?.getBooleanExtra(Constant.Key.is_sos_service, false) ?: false
        isCarWash = intent?.getBooleanExtra(Constant.Key.is_car_wash, false) ?: false
        isSosEmergency = intent?.getBooleanExtra(Constant.Key.is_sos_service_emergency, false)
                ?: false

        if (isSOSService || isSosEmergency) {

            isSosEmergency = intent?.getBooleanExtra(Constant.Key.is_sos_service_emergency, false)
                    ?: false

            if (intent.hasExtra(Constant.Path.addressId))
                addressId = intent?.getStringExtra(Constant.Path.addressId) ?: ""

            if (intent.hasExtra(Constant.Path.latitude))
                sosUserLatitude = intent?.getStringExtra(Constant.Path.latitude) ?: ""

            if (intent.hasExtra(Constant.Path.longitude))
                sosUserLongitude = intent?.getStringExtra(Constant.Path.longitude) ?: ""

            if (intent.hasExtra(Constant.Path.serviceID))
                sosServiceId = intent?.getStringExtra(Constant.Path.serviceID) ?: ""

            if (intent.hasExtra(Constant.Path.workshopWreckerId))
                workshopWreckerId = intent?.getStringExtra(Constant.Path.workshopWreckerId) ?: ""
        }

        if (intent.hasExtra(Constant.Key.is_car_maintenance_service))
            isCarMaintenanceService = intent?.getBooleanExtra(Constant.Key.is_car_maintenance_service, false)
                    ?: false

        if (intent.hasExtra(Constant.Key.is_quotes))
            isQuotesService = intent?.getBooleanExtra(Constant.Key.is_quotes, false) ?: false


        if (intent.hasExtra(Constant.Path.qutoesUserDescription))
            qutoesUserDescription = intent?.getStringExtra(Constant.Path.qutoesUserDescription)
                    ?: ""

        if (intent.hasExtra(Constant.Path.qutoesUserAttachImage))
            quotesUserImage = intent?.getStringExtra(Constant.Path.qutoesUserAttachImage) ?: ""



        if (intent.hasExtra(Constant.Key.is_motService))
            isMotService = intent?.getBooleanExtra(Constant.Key.is_motService, false) ?: false


        if (intent.hasExtra(Constant.Path.workshopUsersId))
            workshopUsersId = intent.getIntExtra(Constant.Path.workshopUsersId, 1)


        if (intent.hasExtra(Constant.Path.type))
            type = intent?.getStringExtra(Constant.Path.type) ?: ""

        if (intent.hasExtra(Constant.Key.mot_type))
            motType = intent?.getStringExtra(Constant.Key.mot_type) ?: ""


        if (intent.hasExtra(Constant.Key.PartIdMap))
            partidMap = intent.getSerializableExtra(Constant.Key.PartIdMap) as HashMap<String, Models.servicesCouponData>

        if (intent.hasExtra(Constant.Key.MotPartIdMap))
            motpartlist = intent.getSerializableExtra(Constant.Key.MotPartIdMap) as HashMap<String, Models.MotservicesCouponData>

        if (intent.hasExtra(Constant.Path.motservices_time))
            motservicesaveragetime = intent?.getStringExtra(Constant.Path.motservices_time) ?: ""
        if (intent.hasExtra(Constant.Path.categoryId))
            workshopCategoryId = intent.getStringExtra(Constant.Path.categoryId)


        if (intent.hasExtra(Constant.Path.workshopUserDaysId))
            quotesWorkshopUsersDaysId = intent.getStringExtra(Constant.Path.workshopUserDaysId)

        if (intent.hasExtra(Constant.Path.serviceQuotesInsertedId))
            quotesServiceQuotesInsertedId = intent.getStringExtra(Constant.Path.serviceQuotesInsertedId)

        if (intent.hasExtra(Constant.Path.mainCategoryId))
            quotesMainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryId)

        if (isMotService && intent.hasExtra(Constant.Path.mot_id)) {
            motServiceId = intent.getIntExtra(Constant.Path.mot_id, 0)
        }

        if (intent.hasExtra(Constant.Key.wishList)) {
            wishList = intent?.getStringExtra(Constant.Key.wishList) ?: ""

            if (wishList == "1") {
                Iv_favorite.setImageResource(R.drawable.ic_heart)
            } else {
                Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)

            }
        }
        Iv_favorite.setOnClickListener {

            try {
                if (wishList == "0") {
                    RetrofitClient.client.addToFavorite(getBearerToken()
                            ?: "", "", "", workshopUsersId.toString(), getSelectedCar()?.carVersionModel?.idVehicle
                            ?: "").onCall { networkException, response ->

                        response.let {
                            val body = response?.body()?.string()
                            if (body.isNullOrEmpty() || response.code() == 401)

                                showConfirmDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), { moveToLoginPage(this) })


                            if (response?.isSuccessful!!) {
                                val body = JSONObject(body)
                                if (body.has("message")) {
                                    Iv_favorite.setImageResource(R.drawable.ic_heart)

                                    wishList = "1"


                                    showInfoDialog(getString(R.string.SuccessfullyaddedthisWorkshopfavorite))
                                    logAddToWishListEvent(this, workshop_name.text.toString(), workshopUsersId.toString(), "workshop", "USD", 0.0)


                                }

                            }

                        }
                    }

                } else {

                    RetrofitClient.client.removeFromFavorite(getBearerToken()
                            ?: "", "", workshopUsersId.toString(), "").onCall { networkException, response ->

                        response.let {
                            val body = response?.body()?.string()
                            if (body.isNullOrEmpty() || response.code() == 401)
                                showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { moveToLoginPage(this) }

                            if (response?.isSuccessful!!) {
                                val body = JSONObject(body)
                                if (body.has("message")) {
                                    Iv_favorite!!.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                                    wishList = "0"
                                    showInfoDialog(getString(R.string.WorkshopRemovedfromfavorite))

                                }

                            }

                        }
                    }
                }


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }

        if (intent.hasExtra(Constant.Path.couponList) && intent.getSerializableExtra(Constant.Path.couponList) != null && intent.getSerializableExtra(Constant.Path.couponList) as List<Models.Coupon> != null) {
            couponList = intent.getSerializableExtra(Constant.Path.couponList) as List<Models.Coupon>
            if (couponList != null && couponList?.size != 0) {
                AppliedCouponName.text = (couponList?.get(0)?.couponTitle)
                AppliedCouponName.visibility = View.VISIBLE
                CouponLabel.visibility = View.GONE
                offerBadge.visibility = View.VISIBLE
                workshopCouponId = couponList?.get(0)?.id.toString()
            } else {
                workshopCouponId = ""
                CouponLabel.visibility = View.GONE
                AppliedCouponName.visibility = View.GONE
                offerBadge.visibility = View.GONE
            }

        } else {
            workshopCouponId = ""
            CouponLabel.visibility = View.GONE
            AppliedCouponName.visibility = View.GONE
            offerBadge.visibility = View.GONE
        }
        offerBadge.setOnClickListener {
            if (couponList != null) {
                displayCoupons(couponList as MutableList<Models.Coupon>, AppliedCouponName)

            }
        }
        //set selected date from workshop calendar screen calendar
        booking_date.text = dateFormatChangeYearToMonth(selectedDateFilter)
        calendarSelectedDateFilter = selectedDateFilter
        if (isCarWash || isRevision || isTyre || isAssembly || isMotService) {
            getCalendarPrices()
        } else if (isSOSService) {
            getCalendarSOS()
        } else if (isCarMaintenanceService) {
            getCalendarMaintenance()
        } else {
            if (intent != null && intent.hasExtra(Constant.Key.workshopCalendarPrice))
                calendarPriceMap = intent.getSerializableExtra(Constant.Key.workshopCalendarPrice) as HashMap<String, String>
        }

        // open calendar for booking date
        booking_date.setOnClickListener {
            val formatter = org.threeten.bp.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", getLocale())
            val localDate = LocalDate.parse(dateFormatChangeYearToMonth(calendarSelectedDateFilter), formatter)
            today = localDate
            val dialogView = Dialog(this, R.style.DialogSlideAnimStyle)
            dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE)

            initCalendar(dialogView)
        }

        see_all_feedback.setOnClickListener {
            startActivity(intentFor<FeedbackListActivity>(Constant.Path.workshopId to workshopUsersId.toString()
                    , Constant.Path.ProductOrWorkshopName to company_name.takeIf { !it.isNullOrEmpty() }
                    , Constant.Path.type to "2", Constant.Path.mainCategoryId to feedbackMainCategoryId, Constant.Path.serviceID to feedbackServicesId
            ))

        }

        if (isSosEmergency) {
            booking_date.isEnabled = false
        }

        if (intent.hasExtra(Constant.Key.cartItem) && intent?.getSerializableExtra(Constant.Key.cartItem) != null) {

            cartItem = intent.getSerializableExtra(Constant.Key.cartItem) as Models.CartItem
            Log.d("Date", "Deliverydays: WorkshopDetail" + cartItem?.Deliverydays)
        }


    }

    internal fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))
    private fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)
    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun initCalendar(dialogView: Dialog) {
        var selectedDate: LocalDate? = null

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textView = view.exFiveDayText
            val layout = view.exFiveDayLayout
            val bookingMinPrice = view.booking_min_price

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && ((day.date == today || day.date.isAfter(today)) && day.date.isBefore(today.plusDays(30)))) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            previousSelectedDate = selectedDate
                            bookingCalendar.notifyDateChanged(day.date)
                            oldDate?.let { bookingCalendar.notifyDateChanged(it) }
                        }
                    }
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout: LinearLayout = view.legendLayout
        }

        with(dialogView) {

            setContentView(R.layout.dialog_booking_calendar)
            window?.setGravity(Gravity.CENTER)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            show()
            bookingCalendar = dialogView.findViewById<CalendarView>(R.id.booking_calendar)
            dialogView.findViewById<TextView>(R.id.close_calendar).setOnClickListener { dialogView.dismiss() }

            //Set up calendar date
            val currentMonth = org.threeten.bp.YearMonth.now()

            val startMonth = currentMonth
            val endMonth = currentMonth.plusMonths(1)
            val daysOfWeek = daysOfWeekFromLocale()

            bookingCalendar.setup(startMonth, endMonth, daysOfWeek.first())
            bookingCalendar.scrollToMonth(currentMonth)


            bookingCalendar.dayBinder = object : DayBinder<DayViewContainer> {

                override fun create(view: View) = DayViewContainer(view)

                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.day = day

                    val textView = container.textView
                    val bookingMinPrice = container.bookingMinPrice
                    val layout = container.layout

                    //2019-09-26
                    val calendarDate = day.date.year.toString() + "-" + String.format("%02d", day.date.month.value, getLocale()) + "-" + String.format("%02d", day.date.dayOfMonth, getLocale())
                    if (day.owner == DayOwner.THIS_MONTH) {
                        textView.text = day.date.dayOfMonth.toString()
                        if ((day.date == today || day.date.isAfter(today)) && day.date.isBefore(today.plusDays(30))) {
                            when (day.date) {
                                previousSelectedDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    bookingMinPrice.setTextColorRes(R.color.white)
                                    layout.setBackgroundResource(R.drawable.calendar_selected_day_bg)
                                }
                                today -> {
                                    textView.setTextColorRes(R.color.white)
                                    bookingMinPrice.setTextColorRes(R.color.white)
                                    layout.setBackgroundResource(R.drawable.calendar_selected_today_bg)
                                }
                                else -> {
                                    textView.setTextColorRes(R.color.black)
                                    bookingMinPrice.setTextColorRes(R.color.grey_light)
                                    layout.setBackgroundResource(0)
                                }
                            }

                            if (calendarPriceMap.get(calendarDate) != null)
                                bookingMinPrice.text = getString(R.string.prepend_euro_symbol_string, calendarPriceMap.get(calendarDate))

                            // load packages for selected date
                            if (selectedDate == day.date) {
                                Log.v("SELECTED DATE", "******* $selectedDate")
                                selectedDateFilter = selectedDate.toString()

                                loadWorkShopPackages()
                                dismiss()
                            }

                        } else {
                            textView.setTextColorRes(R.color.md_grey_500)
                            bookingMinPrice.setTextColorRes(R.color.md_grey_500)
                            layout.background = null
                        }


                    } else {
                        textView.setTextColorRes(R.color.md_grey_500)
                        bookingMinPrice.setTextColorRes(R.color.md_grey_500)
                        layout.background = null
                    }
                }
            }

            // Bind month view for calendar
            val monthTitleFormatter = org.threeten.bp.format.DateTimeFormatter.ofPattern("MMMM")
            bookingCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = month.yearMonth

                        month.yearMonth
                    }
                }
            }

            bookingCalendar.monthScrollListener = { month ->
                val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
                calendarHeaderYearText.text = title

                selectedDate?.let {
                    // Clear selection if we scroll to a new month.
                    selectedDate = null
                    bookingCalendar.notifyDateChanged(it)
                }
            }

            bookingCalendar.monthScrollListener = { month ->
                val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
                calendarHeaderYearText.text = title

                selectedDate?.let {
                    // Clear selection if we scroll to a new month.
                    selectedDate = null
                    bookingCalendar.notifyDateChanged(it)
                }
            }

            nextMonthArrow.setOnClickListener {
                bookingCalendar.findFirstVisibleMonth()?.let {
                    bookingCalendar.smoothScrollToMonth(it.yearMonth.next)
                }
            }

            previousMonthArrow.setOnClickListener {
                bookingCalendar.findFirstVisibleMonth()?.let {
                    bookingCalendar.smoothScrollToMonth(it.yearMonth.previous)
                }
            }
        }
    }

    override fun onResume() {
        loadWorkShopPackages()
        getFeedback(this, workshopUsersId.toString(), "", "2", "")
        super.onResume()
    }

    private fun setImageSlider(imagesArray: JSONArray) {
        if (imagesArray.length() > 1) {
            image_slideview_workshop.visibility = View.GONE
            image_slider.visibility = View.VISIBLE
            //set slider
            createImageSliderDialog()
            image_slider.removeAllSliders()

            for (i in 0 until imagesArray.length()) {
                //   val imageRes = Constant.itemImageBaseURL + imagesArray.getJSONObject(i).getString("image_name")
                val imageRes = imagesArray.getJSONObject(i).getString("image_url")
                val slide = TextSliderView(this).image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside).empty(R.drawable.no_image_placeholder)
                image_slider.addSlider(slide)

                val scaledSlide = DialogTouchImageSlider(this, R.drawable.no_image_placeholder)
                        .description("Description")
                        .image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside)
                        .empty(R.drawable.no_image_placeholder)
                dialogSlider.addSlider(scaledSlide)

                slide.setOnSliderClickListener {

                    if (disableSliderTouch)
                        return@setOnSliderClickListener

                    dialogSlider.currentPosition = i
                    if(imageDialog!=null)
                    imageDialog.show()
                }
            }
            image_slider.addOnPageChangeListener(object : ViewPagerEx.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    disableSliderTouch = state != ViewPagerEx.SCROLL_STATE_IDLE
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                }

            })
        } else {

            if(imagesArray!=null && imagesArray.length() !=0&& imagesArray.getJSONObject(0) != null &&  imagesArray.getJSONObject(0)?.getString("image_url")!=null ){
                  val imageRes = imagesArray.getJSONObject(0).getString("image_url")
            image_slideview_workshop.visibility = View.VISIBLE
            image_slider.visibility = View.GONE
            loadImage(imageRes, image_slideview_workshop)
            image_slideview_workshop.setOnClickListener {
                imageDialog = createImageDialog(imageRes)
                imageDialog.show()
            }
            }

        }

    }

    private fun loadWorkShopPackages() {

        booking_date.text = dateFormatChangeYearToMonth(selectedDateFilter)

        Log.d("ProductOrWorkshopList", "loadWorkshops: service id = $workshopUsersId -- workshopCategoryId = $workshopCategoryId  -- selectedDateFilter = $selectedDateFilter")


        val dialog = getProgressDialog(true)

        val callback = object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                dialog.dismiss()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                dialog.dismiss()
                val body = response.body()?.string()


                body?.let {

                    if (isStatusCodeValid(body)) {
                        val dataJson = JSONObject(body)
                        val json = dataJson.getJSONObject("data")

                        currentWorkshopDetail = json.toString()

                        mainCategoryIDForAssembly = json.optString("main_category_id", "")


                        if (json.has("company_name") && json.getString("company_name") != null) {
                            workshop_name.text = json.getString("company_name")
                            company_name = json.getString("company_name")
                        }

                        if (json.has("services_price") && !json.isNull("services_price")) {
                            carMaintenanceServicePrice = json.optString("services_price")
                        }
                        if (json.has("service_id") && !json.isNull("service_id")) {
                            carMaintenanceServiceId = json.optString("service_id")
                        }


                        if (json.has("service_specification") && !json.isNull("service_specification")) {
                            try {
                                serviceSpecArray = json.getJSONArray("service_specification")
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }

                        }

                        services_detail.removeAllViews()

                        if (json.has("service_detail") && json.get("service9_detail") != null) {
                            val serviceDetail: JSONArray = json.getJSONArray("service_detail")
                            ll_services.visibility = View.VISIBLE
                            for (i in 0 until serviceDetail.length()) {
                                val serviceName = serviceDetail.get(i) as JSONObject
                                val textView = TextView(this@WorkshopDetailActivity)
                                textView.text = serviceName.optString("main_cat_name")
                                textView.setTextColor(resources.getColor(R.color.black))
                                services_detail.addView(textView)
                            }
                        } else {
                            ll_services.visibility = View.GONE
                        }

                        image_slider.removeAllSliders()//when chnage calendar date package api calls , so we have to remove previosuly loaded images from it
                        dialogSlider.removeAllSliders()//when chnage calendar date package api calls , so we have to remove previosuly loaded images from it

                        if (json.has("workshop_gallery") && !json.isNull("workshop_gallery")) {
                            if (json.get("workshop_gallery") is JSONArray) {
                                Log.d("workshop_gallery", "workshop_gallery")
                                setImageSlider(json.getJSONArray("workshop_gallery"))
                            }
                        } else if (json.has("service_images") && !json.isNull("service_images")) {
                            if (json.get("service_images") is JSONArray) {
                                setImageSlider(json.getJSONArray("service_images"))
                                Log.d("workshop_gallery", "service_images")
                            }
                        }

                        workshop_address.text = if (json.has("registered_office") && json.get("registered_office") != null) json.optString("registered_office") else ""
                        workshop_address.setOnClickListener {

                            val mapUri = Uri.parse("geo:0,0?q=${workshop_address.text.toString().trim().takeIf { it1 -> !it1.isNullOrEmpty() }}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            startActivity(mapIntent)
                        }



                        workshop_description.text = json.optString("about_business", "")

                        if (workshop_description.text == "null")
                            workshop_description.text = getString(R.string.not_applicable)


                        if (json.has("rating_count") && json.getString("rating_count") != null && json.getString("rating_count") != "" && json.getString("rating_count") != "null")
                            workshop_rating_count.text = json.getInt("rating_count").toString()

                        if (json.has("rating")) {
                            val jsonRating = JSONObject(json.getString("rating"))
                            if (jsonRating.has("rating") && !jsonRating.isNull("rating") && jsonRating.getString("rating") != "" && jsonRating.getString("rating") != "null")
                                workshop_rating.rating = jsonRating.getString("rating").toFloat()
                            else
                                workshop_rating.rating = 0f
                        }


                        if (json.has("profile_image"))
                            loadImage(json.getString("profile_image"), workshop_image, R.drawable.no_image_placeholder)


                        if (json.has("mobile_number") && json.getString("mobile_number") != null) {
                            workshopPhoneNumber = json.getString("mobile_number")
                            if (!workshopPhoneNumber.isNullOrBlank() && workshopPhoneNumber.trim() != "null") {
                                workshopPhoneNo.text = "+ $workshopPhoneNumber"
                            }

                            call_workshop.setOnClickListener {

                                if (!workshopPhoneNumber.isNullOrBlank() && workshopPhoneNumber.trim() != "null") {
                                    //Check for Call Permission

                                    val permission = ContextCompat.checkSelfPermission(this@WorkshopDetailActivity, android.Manifest.permission.CALL_PHONE)
                                    if (permission == PackageManager.PERMISSION_GRANTED)
                                        callWorkShop()
                                    else
                                        ActivityCompat.requestPermissions(this@WorkshopDetailActivity, arrayOf(android.Manifest.permission.CALL_PHONE), PHONE_CALL_RC)
                                } else
                                    alert {
                                        message = getString(R.string.no_phone_number)
                                        okButton { }
                                    }.show()
                            }
                        }
                        if (isRevision) {
                            categoryID = json.optString("service_id", "")
                            averageServiceTime = json.optDouble("service_average_time", 0.0) * 60


                        } else if (isQuotesService) {
                            averageServiceTime = json.optDouble("service_average_time_in_hour", 0.0) * 60


                        } else {


                            categoryID = json.optString("category_id", "")
                            averageServiceTime = json.optDouble("service_average_time", 0.0) * 60



                            Log.d("motavarageTime", json.optDouble("service_average_time").toString() + "    " + averageServiceTime)
                        }

                        if (averageServiceTime != 0.0) {
                            if (averageServiceTime.toString().split(".")[1] != null && averageServiceTime.toString().split(".")[1].toLong() != 0L) {
                                averageServiceTime = (averageServiceTime.toInt() + 1).toDouble()
                            }
                        }


                        if (json.has("main_category_id") && !json.optInt("main_category_id").toString().isNullOrBlank()) {
                            feedbackMainCategoryId = json.optInt("main_category_id").toString()
                        }
                        if (json.has("service_id") && !json.optString("service_id").toString().isNullOrBlank()) {
                            feedbackServicesId = json.optInt("service_id").toString()
                        }


                        // set up workshop working time slots
                        if (json.has("package_list") && !json.isNull("package_list") && json.getJSONArray("package_list") != null
                                && json.getJSONArray("package_list").length() > 0) {
                            bindWorkingSlots(json.getJSONArray("package_list"))
                        } else
                            bindWorkingSlots(JSONArray())

                        //getSpecialCondition(workshopCategoryId,workshopUsersId)

                    } else {
                        bindWorkingSlots(JSONArray())
                        showInfoDialog(getMessageFromJSON(it)) {
                            //                            finish()
                        }
                    }
                    return@let
                }

                if (!response.isSuccessful) {
                    showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again)) {
                        //                        finish()
                    }
                }
            }
        }

        if (intent.hasExtra(Constant.Key.cartItem) && intent?.getSerializableExtra(Constant.Key.cartItem) != null) {
            // for assembly
            val cartItem = intent.getSerializableExtra(Constant.Key.cartItem) as Models.CartItem

            if (isTyre) {
                productID = cartItem.tyreDetail?.id.toString()
            } else if (isAssembly) {
                productID = cartItem.productDetail?.id ?: ""
            }
            productQuantity = cartItem.quantity.toString()
            Log.e("productDetailswork==", productID)
            Log.e("productQuantity==", productQuantity)

        }


        if (isAssembly)
            RetrofitClient.client.getAssemblyWorkshopPackageDetail(workshopUsersId, selectedDateFilter, productID, getSavedSelectedVehicleID(), getUserId(), workshopCategoryId, servicesAverageTime.toString(), getSelectedCar()?.carVersionModel?.idVehicle!!, mainCategoryId).enqueue(callback)
        else if (isRevision) {
            if (WorkshopJson != null) {
                serviceID = WorkshopJson.optString("service_id")

            }

            RetrofitClient.client.getCarRevisionPackageDetail(workshopUsersId, selectedDateFilter, getSelectedCar()?.carVersionModel?.idVehicle!!, serviceID, mainCategoryId, getUserId(), servicesAverageTime.toString(), if (getBearerToken().isNullOrBlank()) "" else getBearerToken()!!).enqueue(callback) // use for Revision, api call.


        } else if (isTyre) {

            var maincategoryId = intent.getStringExtra(Constant.Path.mainCategoryIdTyre)
            if (maincategoryId == null) {
                maincategoryId = ""
            }

            RetrofitClient.client.getTyrePackageDetail(workshopUsersId, selectedDateFilter, workshopCategoryId.toInt(), maincategoryId, productID, productQuantity, workshopCouponId, getUserId(), getSelectedCar()?.carVersionModel?.idVehicle!!, if (cartItem?.quantity != null) cartItem?.quantity.toString() else "", servicesAverageTime).enqueue(callback)

        } else if (isSOSService) {
            RetrofitClient.client.getSOSPackageDetail(
                    workshopUsersId.toString(), sosServiceId, selectedDateFilter, sosUserLatitude, sosUserLongitude, getSavedSelectedVehicleID(),
                    addressId, workshopWreckerId, getUserId(), version_id = getSelectedCar()?.carVersionModel?.idVehicle.toString(), mainCategoryId = mainCategoryId, wrecker_service_type = "1", service_average_time = servicesAverageTime, servicesPrice = servicesPrice, max_appointment = maxAppointment, hourlyrate = hourlyRate).enqueue(callback)

        } else if (isSosEmergency) {
            RetrofitClient.client.getSOSPackageDetailEmergency(workshopUsersId.toString(), sosServiceId, selectedDateFilter, sosUserLatitude, sosUserLongitude, getSavedSelectedVehicleID(),
                    addressId, getCurrentTime(), getUserId(), "2", version_id = getSelectedCar()?.carVersionModel?.idVehicle.toString(), service_average_time = servicesAverageTime, mainCategoryId = mainCategoryId).enqueue(callback)
        } else if (isCarMaintenanceService)
            RetrofitClient.client.getCarMaintenacePackageDetail(versionId = getSelectedCar()?.carVersionModel?.idVehicle.toString(), selected_date = selectedDateFilter, userid = getUserId(), mainCategoryId = mainCategoryId, workshopUsersId = workshopUsersId.toString(), services = maintenanceServiceJson, services_price = servicesPrice, service_average_time = serviceAveragetime).enqueue(callback)
        else if (isQuotesService)
            RetrofitClient.client.getServiceQuotesPackageDetail(workshopCategoryId, quotesWorkshopUsersDaysId, selectedDateFilter, quotesServiceQuotesInsertedId,
                    getSavedSelectedVehicleID(), mainCategoryId, workshopUsersId.toString(), workshopCouponId, getUserId(), getSelectedCar()?.carVersionModel?.idVehicle
                    ?: "", quotesServicesAvarageTime, maxAppointment = maxAppointment).enqueue(callback)
        else if (isMotService)
            RetrofitClient.client.getMotServicePackageDetail(workshopUsersId, workshopCategoryId.toInt(), motType, selectedDateFilter, getSavedSelectedVehicleID(), getUserId(), motservicesaveragetime, workshopCouponId, mainCategoryId).enqueue(callback)
        else RetrofitClient.client.getWorkshopPackageDetailNew(workshopUsersId, workshopCategoryId.toInt(), selectedDateFilter, getSelectedCar()?.carSize, getUserId(), getSavedSelectedVehicleID(), mainCategoryIDForCarWash, getSelectedCar()?.carVersionModel?.idVehicle!!, servicesAverageTime, maxAppointment, hourlyRate, getSelectedCar()?.carMakeModel?.brandID!!, getSelectedCar()?.carModel?.modelID + "/" + getSelectedCar()?.carModel?.modelYear).enqueue(callback)

    }

    private fun callWorkShop() {
        if (workshopPhoneNumber != null && workshopPhoneNumber.trim().isNotEmpty()) {
            // Call intent
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$workshopPhoneNumber"))
            startActivity(intent)
        } else
            alert {
                message = getString(R.string.no_phone_number)
                okButton { }
            }.show()
    }

    private fun bindWorkingSlots(workingSlotList: JSONArray) {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val packageTiming = itemView.package_timing
            val packagePrice = itemView.package_price
            val bookPackage = itemView.book_package
        }

        working_slot_list.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
                val view: View = layoutInflater.inflate(R.layout.item_intervention_setting, p0, false)
                return ViewHolder(view)
            }

            override fun getItemCount(): Int {
                return workingSlotList.length()
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(p0: ViewHolder, position: Int) {

                // Update for revision services booking
                with(p0) {


                    val packageDetails = workingSlotList[position] as JSONObject
                    packageTiming.text = getFormattedTime(packageDetails.getString("start_time"), packageDetails.getString("end_time"))
                    packagePrice.text = try {
                        if (isCarMaintenanceService) {
                            getString(R.string.prepend_euro_symbol_string, carMaintenanceServicePrice.toDouble().roundTo2Places().toString())

                        } else if (isRevision) {
                            if (packageDetails.has("discount_type") && packageDetails.optString("discount_type") == "0") {
                                getString(R.string.prepend_euro_symbol_string, packageDetails.getString("price").toDouble().roundTo2Places().toString())
                            } else if (packageDetails.has("discount_type") && (packageDetails.optString("discount_type") == "1" || packageDetails.optString("discount_type") == "2")) {
                                val booking_prices = packageDetails.getString("price").toDouble().roundTo2Places()
                                val discountPrices = packageDetails.getString("discount_price").toDouble().roundTo2Places()

                                getString(R.string.prepend_euro_symbol_string, (booking_prices - discountPrices).toString())
                            } else {
                                getString(R.string.prepend_euro_symbol_string, packageDetails.getString("price").toDouble().roundTo2Places().toString())

                            }
                        } else {
                            getString(R.string.prepend_euro_symbol_string, packageDetails.getString("price").toDouble().roundTo2Places().toString())

                        }
                    } catch (e: Exception) {
                        ""
                    }
                    var isAvailable: String = ""
                    if (packageDetails.has("available_status") && !packageDetails.isNull("available_status")) {
                        isAvailable = packageDetails.getString("available_status")
                    }


                    val startTimeString = packageDetails.getString("start_time")
                    val endTimeString = packageDetails.getString("end_time")
                    val startHour = startTimeString.split(":")[0].trim().toInt()
                    val startMin = startTimeString.split(":")[1].trim().toInt()
                    packageTiming.text = "${startTimeString.split(":")[0]}:${startTimeString.split(":")[1]} - " + endTimeString.split(":")[0] + ":" + endTimeString.split(":")[1]
                    Log.d("Time start end time", packageTiming.text.toString())
                    val parsedEndTime = parseTimeHHmmss(endTimeString)
                    val averageMillis = (averageServiceTime * 60 * 1000)
                    Log.d("Time averageServiceTime", averageServiceTime.toString())
                    val delayMillis = 20 * 60 * 1000
                    Log.d("Time Delay", delayMillis.toString())

                    var endLimit = parsedEndTime.time - averageMillis /*- delayMillis*/ // additional 20 min delay
                    Log.d("Time endLimit", endLimit.toString())

                    //re adding if additional delay is greater
                    if (endLimit < parseTimeHHmmss(startTimeString).time)
                        endLimit += delayMillis

                    var endTime = SimpleDateFormat("HH:m", getLocale()).format(Date(endLimit.toLong()))

                    var endHour = endTime.split(":")[0].toInt()
                    var endMin = endTime.split(":")[1].toInt()
                    var service_day = 0
                    Log.d("check_status_avilable", "" + isAvailable)

                    if (isAvailable == "1") {
                        bookPackage.text = getString(R.string.book)
                        bookPackage.setBackgroundColor(ContextCompat.getColor(this@WorkshopDetailActivity, R.color.theme_orange))
                    } else {
                        bookPackage.text = getString(R.string.busy)
                        bookPackage.setBackgroundColor(ContextCompat.getColor(this@WorkshopDetailActivity, R.color.red))
                    }
                    bookPackage.setOnClickListener {
                        if (isAvailable == "0") {
                            //   showInfoDialog(getString(R.string.Thisslotisbusy))
                            return@setOnClickListener
                        } else if (isAvailable == "2") {
                            // showInfoDialog(getString(R.string.Thisslotnotapplicable))
                            return@setOnClickListener
                        } else if (isAvailable == "3") {
                            // showInfoDialog(getString(R.string.this_slot_not_available))
                            return@setOnClickListener
                        } else if (startHour > endHour) {
                            showInfoDialog(getString(R.string.Invalidinterventiontime) + "($startHour:$startMin - $endHour:$endMin)")
                            return@setOnClickListener
                        } else {
                            if (isLoggedIn()) {

                                val sdf = SimpleDateFormat("yyyy-MM-dd", getLocale())
                                sdf.isLenient = true
                                val date = sdf.parse(selectedDateFilter)
                                val formattedDate = SimpleDateFormat("dd/M (E)", getLocale()).format(date)
                                var car1 = 0
                                if (!cartItem?.Deliverydays.isNullOrBlank()) {
                                    car1 = cartItem?.Deliverydays?.toInt()?.plus(1)!!

                                }
                                val DeleviryDate: Date = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(car1))

                                val currentDate = SimpleDateFormat("yyy-MM-dd").parse(selectedDateFilter)


                                if (isSosEmergency || isSOSService) {
                                    startTimeBooking = startTimeString
                                    endTimeBooking = endTimeString
                                    showConfirmDialog(getString(R.string.are_you_sure_to_book_this_slot)) {
                                        checkAndBookService(packageDetails, startTimeString)
                                    }
                                } else if ((isTyre || isAssembly) && cartItem != null && (currentDate < DeleviryDate)) {
                                    showInfoDialog(getString(R.string.Invalidinterventiontime))
                                    return@setOnClickListener
                                } else {
                                    Log.d("start_end_time", "startHour: " + startHour + " startMin: " + startMin + " endHour:" + endHour + " endMin: " + endMin)
                                    TimeWheelPicker.Builder(this@WorkshopDetailActivity)
                                            .setStartEndTime(startHour, endHour, startMin, endMin)
                                            .setTitle(formattedDate)
                                            .setOnTimePickedListener(object : TimeWheelPicker.OnTimePicked {
                                                override fun onPicked(hourOfDay: Int, minute: Int) {
                                                    val time = "${if (hourOfDay < 10) "0" else ""}$hourOfDay:${if (minute < 10) "0" else ""}$minute:00"
                                                    Log.d("WorkshopDetailActivity", "Time: $time")
                                                    if (!isQuotesService)
                                                        if (packagePrice.text.isEmpty()) {
                                                            toast(getString(R.string.PriceNotSpecified))
                                                            return
                                                        }
                                                    checkAndBookService(packageDetails, time)
                                                }
                                            }
                                            ).build()
                                            .show()
                                }
                            } else {
                                showConfirmDialogForLogin(getString(R.string.PleaselogintocontinueforAddtocart), { moveToLoginPage(this@WorkshopDetailActivity) })

                            }

                        }
                    }
                }
            }
        }
    }

    private fun getFormattedTime(startTime: String, endTime: String): String {
        var workingTimeSlot = "$startTime - $endTime"

        try {
            val inputDateFormat = SimpleDateFormat("HH:mm")//, getLocale())//HH:mm:ss
            inputDateFormat.timeZone = TimeZone.getDefault()

            val outputDateFormat = SimpleDateFormat("hh:mm a")//, getLocale()))
            outputDateFormat.timeZone = TimeZone.getDefault()

            val inputStartTime = inputDateFormat.parse(startTime)
            val inputEndTime = inputDateFormat.parse(endTime)

            workingTimeSlot = outputDateFormat.format(inputStartTime) + " - " + outputDateFormat.format(inputEndTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return workingTimeSlot
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PHONE_CALL_RC) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                callWorkShop()
            else
                Toast.makeText(this@WorkshopDetailActivity, getString(R.string.CallPermissionDenied), Toast.LENGTH_LONG).show()
        }
    }

    private fun createImageSliderDialog() {
        imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)
        val slider = SliderLayout(this)
        slider.stopAutoCycle()
        slider.indicatorVisibility = PagerIndicator.IndicatorVisibility.Visible
        slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialogSlider = slider
        with(imageDialog) {
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            setContentView(slider)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            create()
        }
    }

    private fun checkAndBookService(packageDetail: JSONObject, bookingStartTime: String) {
        var endTime = "00:00"
        var finalPrice: Double = 0.0
        slotId = packageDetail.optString("temp_slot_id")
        specialConditionId = if (packageDetail.has("special_condition_id")) packageDetail.optString("special_condition_id", "0") else "0"
        discountPrices = if (packageDetail.has("discount_price")) packageDetail.optString("discount_price", "0") else "0"
        discountType = if (packageDetail.has("discount_type")) packageDetail.optString("discount_type", "0.0") else ""
        maxAppointment = if (packageDetail.has("max_appointment")) packageDetail.optString("max_appointment", "0") else "0"
        //check for timeslot
        val packageID = packageDetail.getInt("id")
        if (bookServicesWithoutCoupon) {
            workshopCouponId = ""
        }
        if (isRevision) {
            finalPrice = packageDetail.optString("price", "0.0").toDouble()
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))
        } else if (isCarMaintenanceService) {
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))

        } else if (isSosEmergency || isSOSService) {

            if (packageDetail.has("price") && !packageDetail.isNull("price") && !packageDetail.getString("price").equals("")) {
                finalPrice = packageDetail.getString("price").toDouble().roundTo2Places()
            }

            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))
        } else if (isMotService) {
            if (packageDetail.has("price") && !packageDetail.isNull("price") && !packageDetail.getString("price").equals("")) {
                finalPrice = packageDetail.getString("price").toDouble().roundTo2Places()
            }
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))
        } else if (isTyre) {
            slotStartTime = packageDetail.optString("start_time")
            slotEndTime = packageDetail.optString("end_time")
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))
            if (packageDetail.has("price") && !packageDetail.isNull("price") && !packageDetail.getString("price").equals("")) {
                finalPrice = packageDetail.getString("price").toDouble().roundTo2Places()
            }


        } else if (isQuotesService) {
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))
        } else if (isCarWash) {
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000) /*+ additionalDelay*/ // add 20 min
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))
            if (packageDetail.has("price") && !packageDetail.isNull("price") && !packageDetail.getString("price").equals("")) {
                finalPrice = packageDetail.getString("price").toDouble().roundTo2Places()
            }


        } else {
            val parsedEndTimeCalendar = parseTimeHHmmssInCalendar(bookingStartTime)
            var bookingDuration = (averageServiceTime * 60 * 1000)
            bookingDuration /= 60000
            parsedEndTimeCalendar.add(Calendar.HOUR_OF_DAY, (bookingDuration / 60).toInt())
            parsedEndTimeCalendar.add(Calendar.MINUTE, (bookingDuration % 60).toInt())
            val endLimit = parsedEndTimeCalendar.time.time + bookingDuration
            endTime = SimpleDateFormat("HH:mm:ss", getLocale()).format(Date(endLimit.toLong()))

            if (packageDetail.has("price") && !packageDetail.isNull("price") && !packageDetail.getString("price").equals("")) {
                finalPrice = packageDetail.getString("price").toDouble().roundTo2Places()
            }
        }

        val dialog = getProgressDialog(true)
        val callback = (object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("WorkshopDetailActivity", "onFailure: onFailure", t)
                toast(getString(R.string.ConnectionErrorPleaseretry))
                dialog.dismiss()
                serviceSpecArray = JSONArray()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                dialog.dismiss()
                serviceSpecArray = JSONArray()
                val body = response.body()?.string()
                if (response.code() == 401)
                    showConfirmDialogForLogin(getString(R.string.PleaselogintocontinueforAddtocart), { moveToLoginPage(this@WorkshopDetailActivity) })
                Log.d("WorkshopDetailActivity", "onResponse: $response, $body RC " + response.code())
                body?.let {
                    if (isStatusCodeValid(body)) {
                        val bookingResponse = JSONObject(body)
                        if (bookingResponse.has("coupon_status") && bookingResponse.getString("coupon_status") == "1") {
                            dialogForInvalidCoupon(packageDetail, bookingStartTime)
                        } else {
                            if (!isQuotesService) {
                                startActivity(intentFor<WorkshopBookingDetailsActivity>().forwardResults())
                                finishAffinity()
                            } else {
                                showInfoDialog(getString(R.string.quotes_alert_msg), true, { moveTohomepage() })
                            }
                        }
                    } else {
                        val bookingResponse = JSONObject(body)
                        if (bookingResponse.has("coupon_status") && bookingResponse.getString("coupon_status") == "1") {
                            dialogForInvalidCoupon(packageDetail, bookingStartTime)
                        } else {
                            showInfoDialog(getMessageFromJSON(it), true)
                        }
                    }
                    Log.d("WorkshopDetailActivity", "onResponse: $it")
                }
            }
        })

        if (isAssembly) {
            val serviceAssemblyBookingCall = cartItem?.productDetail?.usersId?.let {
                RetrofitClient.client.serviceAssemblyBooking(
                        productID,
                        packageID,
                        bookingStartTime,
                        endTime,
                        finalPrice.toString(),
                        selectedDateFilter,
                        mainCategoryId,
                        getBearerToken() ?: "",
                        specialConditionId,
                        getSelectedCar()?.carVersionModel?.idVehicle ?: "",
                        getSelectedCar()?.id.toString(),
                        workshopCouponId,
                        productQuantity,
                        getOrderId(),
                        if (cartItem?.name != null) cartItem?.name!! else "",
                        if (cartItem?.description != null) cartItem?.description!! else "",
                        "0.0",
                        cartItem!!.pfu_tax,
                        cartItem!!.finalPrice.toString(),
                        cartItem!!.price.toString(),
                        it,
                        if (cartItem?.productDetail?.selectedProductCouponId != null) cartItem?.productDetail?.selectedProductCouponId!! else "",
                        specialConditionId,
                        slotId,
                        workshopUsersId.toString(),
                        discountType,
                        workshopCategoryId)

            }
            serviceAssemblyBookingCall?.enqueue(callback)
        } else if (isRevision) {
            Log.d("Revision finalPrice ", finalPrice.toString())
            Log.d("Revision startTime", bookingStartTime)
            Log.d("Revision endTime", endTime)
            val serviceRevisionBookingCall = RetrofitClient.client.revisionServiceBooking(categoryID,
                    packageID, bookingStartTime, selectedDateFilter,
                    finalPrice.toString(), mainCategoryId, getBearerToken() ?: "",
                    specialConditionId,
                    getSelectedCar()?.carVersionModel?.idVehicle
                            ?: "", workshopUsersId, getSelectedCar()?.id.toString(), workshopCouponId, getOrderId(), endTime, discountPrices, discountType, slotId)

            serviceRevisionBookingCall.enqueue(callback)
        } else if (isTyre) {
            var mainCategoryId = intent.getStringExtra(Constant.Path.mainCategoryIdTyre)
            if (mainCategoryId == null) {
                mainCategoryId = ""
            }
            val productCoupon = if (cartItem?.tyreDetail?.SelectedTyreCouponId != null) cartItem?.tyreDetail?.SelectedTyreCouponId else ""
            val serviceTyreBookingCall = RetrofitClient.client.tyreServiceBooking(
                    productID, productQuantity, packageID, bookingStartTime, endTime, selectedDateFilter,
                    finalPrice.toString(), categoryID, getBearerToken() ?: "",
                    getSelectedCar()?.carVersionModel?.idVehicle
                            ?: "", getSelectedCar()?.id.toString(), workshopCouponId, getOrderId(), "0.0",
                    cartItem!!.pfu_tax, cartItem!!.tyretotalPrice, cartItem!!.price.toString(),
                    cartItem!!.name, cartItem!!.description!!, workshopCategoryId, productCoupon!!,
                    cartItem?.tyreDetail?.user_id.toString(), mainCategoryId,
                    specialConditionId = specialConditionId,
                    slotStartTime = slotStartTime,
                    slotEndTime = slotEndTime,
                    maxAppointment = maxAppointment,
                    service_average_time = (averageServiceTime.toInt() / 60).toString(),
                    workshopUsersId = workshopUsersId.toString(),
                    discountPrice = discountPrices, slot_id = slotId
            )

            serviceTyreBookingCall.enqueue(callback)
        } else if (isSOSService) {
            val serviceSOSBookingCall = RetrofitClient.client.sosServiceBooking(
                    packageID.toString(),
                    workshopUsersId.toString(), bookingStartTime, endTime, selectedDateFilter, sosUserLatitude, sosUserLongitude,
                    getSelectedCar()?.id?.toInt(), finalPrice.toString(), addressId, sosServiceId,
                    workshopWreckerId, getOrderId(), getBearerToken()
                    ?: "", workshopCouponId, specialConditionId, slotId, discountType, mainCategoryId, getSavedSelectedVehicleID(),servicesAverageTime
            )
            serviceSOSBookingCall.enqueue(callback)
        } else if (isSosEmergency) {
            val serviceSOSBookingCallEmergency = RetrofitClient.client.emergencySosServiceBooking(
                    getBearerToken() ?: "",
                    packageID.toString(), workshopUsersId.toString(), startTimeBooking, endTimeBooking, selectedDateFilter, sosUserLatitude, sosUserLongitude,
                    getSavedSelectedVehicleID(), finalPrice.toString(), addressId, sosServiceId,
                    getOrderId(),specialConditionId, slotId,mainCategoryId,servicesAverageTime
            )
            serviceSOSBookingCallEmergency.enqueue(callback)
        } else if (isCarMaintenanceService) {
            val serviceCarMaintenanceBooking = RetrofitClient.client.serviceBookingCarMaintenance(
                    bookingStartTime, endTime, selectedDateFilter, carMaintenanceServicePrice, getSelectedCar()?.carVersionModel?.idVehicle!!,
                    carMaintenanceServiceId, workshopUsersId, packageID, getOrderId()
                    , serviceSpecification, getSavedSelectedVehicleID(),
                    getBearerToken()
                            ?: "", workshopCouponId, specialConditionId, slotId, discountType, mainCategoryId
                    , maintenanceServiceJson)
            serviceCarMaintenanceBooking.enqueue(callback)
        } else if (isQuotesService) {

            val imageList: MutableList<MultipartBody.Part?> = ArrayList()
            val images = Gson().fromJson(quotesUserImage, Array<String>::class.java).asList()

            for (i in 0 until images.size) {
                val file = File(images.get(i))
                imageList.add(file.toMultipartBody("images[]"))
            }
            Log.d("QutoesBooking", "service_id=" + workshopCategoryId + "&selected_date=" + selectedDateFilter +
                    "&main_category_id=" + quotesMainCategoryId + "&selected_car_id=" + getSavedSelectedVehicleID() + "&workshop_id=" + workshopUsersId + "&start_time=" + bookingStartTime +
                    "&package_id=" + packageID.toString() + "&order_id=" + getOrderId() + "&coupon_id=" + workshopCouponId + "&end_time=" + endTime + "&version_id=" + getSelectedCar()?.carVersionModel?.idVehicle!! +
                    "&special_condition_id=" + specialConditionId + "&temp_slot_id=" + slotId + "&text=" + qutoesUserDescription + "&images=" + images)

            val serviceQuotesBooking = RetrofitClient.client.serviceQuotesBooking(
                    workshopCategoryId.toRequestBody(), selectedDateFilter.toRequestBody(), quotesServiceQuotesInsertedId.toRequestBody(), mainCategoryId.toRequestBody(), getSavedSelectedVehicleID().toRequestBody(),
                    workshopUsersId.toString().toRequestBody(), bookingStartTime.toRequestBody(), packageID.toString().toRequestBody(), getOrderId().toRequestBody(), getBearerToken()
                    ?: "", workshopCouponId.toRequestBody(), endTime.toRequestBody(), getSelectedCar()?.carVersionModel?.idVehicle!!.toRequestBody(),
                    specialConditionId.toRequestBody(), slotId.toRequestBody(), discountType.toRequestBody(), qutoesUserDescription.toRequestBody(), images = imageList)
            serviceQuotesBooking.enqueue(callback)
        } else if (isMotService) {
            val motPart = motpartlist.get(motServiceId.toString())
            for (i in 0 until motPart!!.service_partID.size) {
                val jsonObjpart = JSONObject()
                jsonObjpart.put("part_id", motPart.service_partID[i])
                jsonObjpart.put("quantity", "1")
                jsonObjpart.put("part_coupon_id", motPart.service_partcouponID[i])
                jsonObjpart.put("seller_id", motPart.seller_ID[i])
                motServiceSpecArray.put(jsonObjpart)
            }
            Log.d("mot avragetime", averageServiceTime.toString())
            Log.d("mot bookingStartTime", bookingStartTime)
            Log.d("mot endTime", endTime)
            val serviceMotBooking = RetrofitClient.client.serviceMotBooking(packageID.toString(), bookingStartTime, endTime, selectedDateFilter,
                    motServiceId.toString(), finalPrice.toString(), getOrderId(), getSavedSelectedVehicleID(), workshopCouponId, workshopUsersId.toString(), getServicesType(), motServiceSpecArray, specialConditionId, mainCategoryId, slotId, discountType, getBearerToken()
                    ?: "")
            serviceMotBooking.enqueue(callback)
        } else if (isCarWash) {
            val serviceBookingCarWash = RetrofitClient.client.carWashServiceBooking(
                    packageID, bookingStartTime, endTime, finalPrice.toString(), selectedDateFilter, getSelectedCar()?.carSize
                    ?: "",
                    categoryID, mainCategoryIDForCarWash, getSavedSelectedVehicleID(), workshopCouponId, getOrderId(), getSelectedCar()?.carVersionModel?.idVehicle
                    ?: "", slotId, specialConditionId, workshopUsersId.toString(), discountType, getBearerToken()
                    ?: ""
            )
            serviceBookingCarWash.enqueue(callback)
        } else {
            val serviceBookingCall = RetrofitClient.client.serviceBooking(
                    categoryID, packageID, bookingStartTime, endTime, finalPrice.toString(),
                    getSelectedCar()?.carSize ?: "", selectedDateFilter,
                    getSelectedCar()?.carVersionModel?.idVehicle ?: "",
                    getOrderId(), getBearerToken()
                    ?: "", getSavedSelectedVehicleID(), workshopUsersId, workshopCouponId, specialConditionId, slotId, discountType)
            serviceBookingCall.enqueue(callback)
        }
    }

    private fun moveTohomepage() {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()

    }

    private fun displayCoupons(couponsList: MutableList<Models.Coupon>, AppliedCouponName: TextView) {
        val dialog = Dialog(this@WorkshopDetailActivity)
        val dialogView: View = LayoutInflater.from(this@WorkshopDetailActivity).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)//height shoud be fixed
        val title = dialog.findViewById(R.id.title) as TextView

        title.text = getString(R.string.coupon_list)
        with(dialog) {
            class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val couponsName = view.coupons_name
                val couponsQuantity = view.coupons_quantity
                val couponsAmount = view.coupons_amount
            }
            dialog_recycler_view.adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun getItemCount(): Int = couponsList.size
                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val items = couponsList[position]
                    holder.couponsName.text = items.couponTitle
                    holder.couponsQuantity.text = items.couponQuantity.toString()
                    if (!items.offerType.isNullOrBlank()) {
                        if (items.offerType == "2") {
                            holder.couponsAmount.text = getString(R.string.prepend_euro_symbol_string, items.amount.toString())
                        } else {
                            holder.couponsAmount.text = items.amount.toString() + getString(R.string.prepend_percentage_symbol)
                        }
                    }
                    holder.itemView.setOnClickListener {
                        if (!couponsList[position].id.isNullOrBlank()) {
                            workshopCouponId = couponsList[position].id.toString()
                            AppliedCouponName.text = (couponsList[position].couponTitle)
                            AppliedCouponName.visibility = View.VISIBLE
                            dialog.dismiss()
                        }
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    return ViewHolder(layoutInflater.inflate(R.layout.dialog_offer_coupons_layout, parent, false))
                }
            }
            imageCross.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun dialogForInvalidCoupon(packageDetail: JSONObject, bookingStartTime: String) {
        val builder = AlertDialog.Builder(this@WorkshopDetailActivity)
        builder.setTitle(getString(R.string.coupon_is_invalid))
        builder.setMessage(getString(R.string.doyouwanttobookservices))
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            bookServicesWithoutCoupon = true
            checkAndBookService(packageDetail, bookingStartTime)
        }
        builder.setNegativeButton("No") { dialogInterface, which ->

        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun getFeedbackList(list: MutableList<Models.FeedbacksList>) {
        bindFeedbackList(list, this)
    }

    private fun getCalendarPrices() {
        Log.d("serviceAverageTime ", WorkshopJson.optString("service_average_time"))
        RetrofitClient.client.getSelectedWorkshopCalendarPrice(workshopUsersId.toString(), getUserId(), workshopCategoryId, hourlyRate, servicesAverageTime, calendarSelectedDateFilter, mainCategoryId).onCall { networkException, response ->
            if (response!!.isSuccessful) {
                val bodyResponse = response.body()?.string()
                if (bodyResponse != null && isStatusCodeValid(bodyResponse)) {
                    val bodyJsonObject = JSONObject(bodyResponse)
                    if (bodyResponse != null && bodyJsonObject.has("data_set") && bodyJsonObject.opt("data_set") != null) {
                        val dataSet = getDataSetArrayFromResponse(bodyResponse)
                        calendarPriceMap.clear()
                        for (i in 0 until dataSet.length()) {
                            val calendarDatePricesObject = Gson().fromJson<Models.CalendarPrice>(dataSet[i].toString(), Models.CalendarPrice::class.java)
                            calendarPriceMap[calendarDatePricesObject.date] = calendarDatePricesObject.minPrice.toString()
                        }
                    }
                }
            }
        }
    }

    private fun getCalendarMaintenance() {
        RetrofitClient.client.getSelectedWorkshopCalendarPriceMaintence(workshopUsersId.toString(), getUserId(), workshopCategoryId, hourlyRate, servicesAverageTime, calendarSelectedDateFilter, mainCategoryId, version_id = getSelectedCar()?.carVersionModel?.idVehicle.toString(), services = maintenanceServiceJson, services_price = servicesPrice).onCall { networkException, response ->
            if (response!!.isSuccessful) {
                val bodyResponse = response.body()?.string()

                if (bodyResponse != null && isStatusCodeValid(bodyResponse)) {
                    val bodyJsonObject = JSONObject(bodyResponse)
                    if (bodyResponse != null && bodyJsonObject.has("data_set") && bodyJsonObject.opt("data_set") != null) {
                        val dataSet = getDataSetArrayFromResponse(bodyResponse)
                        calendarPriceMap.clear()
                        for (i in 0 until dataSet.length()) {
                            val calendarDatePricesObject = Gson().fromJson<Models.CalendarPrice>(dataSet[i].toString(), Models.CalendarPrice::class.java)
                            calendarPriceMap[calendarDatePricesObject.date] = calendarDatePricesObject.minPrice.toString()
                        }
                    }
                }
            }
        }
    }

    private fun getCalendarSOS() {
        RetrofitClient.client.getSelectedWorkshopCalendarPriceSOs(workshopUsersId.toString(), calendarSelectedDateFilter, "1", sosServiceId, servicesPrice, servicesAverageTime, maxAppointment, mainCategoryId).onCall { networkException, response ->
            if (response!!.isSuccessful) {
                val bodyResponse = response.body()?.string()

                if (bodyResponse != null && isStatusCodeValid(bodyResponse)) {
                    val bodyJsonObject = JSONObject(bodyResponse)
                    if (bodyResponse != null && bodyJsonObject.has("data_set") && bodyJsonObject.opt("data_set") != null) {
                        val dataSet = getDataSetArrayFromResponse(bodyResponse)
                        calendarPriceMap.clear()
                        for (i in 0 until dataSet.length()) {
                            val calendarDatePricesObject = Gson().fromJson<Models.CalendarPrice>(dataSet[i].toString(), Models.CalendarPrice::class.java)
                            calendarPriceMap[calendarDatePricesObject.date] = calendarDatePricesObject.minPrice.toString()
                        }
                    }
                }
            }
        }
    }

}


