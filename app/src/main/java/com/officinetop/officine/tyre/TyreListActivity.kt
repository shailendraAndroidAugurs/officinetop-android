package com.officinetop.officine.tyre

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.PaginationListener
import com.officinetop.officine.adapter.RecyclerViewAdapter
import com.officinetop.officine.car_parts.TyreDetailActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_tyre_list.*
import kotlinx.android.synthetic.main.dialog_rating_tyrefilter.*
import kotlinx.android.synthetic.main.dialog_sorting.*
import kotlinx.android.synthetic.main.dialog_tyre_filter_category.*
import kotlinx.android.synthetic.main.dialog_tyre_filter_subcategory.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_checkbox.view.*
import kotlinx.android.synthetic.main.item_tyre.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil
import kotlin.math.floor

class TyreListActivity : BaseActivity() {

    private lateinit var filterDialog: Dialog
    private lateinit var sortDialog: Dialog
    var tempPriceFinal: Float = -1f
    var tempPriceInitial: Float = 0f
    var priceRangeInitial: Float = 0f
    var priceRangeFinal: Float = -1f
    val filterBrandList: MutableList<String> = ArrayList()
    val filterTyreSeasonList: MutableList<Models.TypeSpecification> = ArrayList()
    val filterTyreSpeedIndexList: MutableList<Models.TypeSpecification> = ArrayList()

    val filterTyreSpeedLoadIndexList: MutableList<Models.TypeSpecification> = ArrayList()
    private var searchString: String = ""
    private var priceSortLevel: Int = 0
    private var priceRange: String = ""
    private var recyclerViewAdapter: RecyclerViewAdapter? = null
    private val PAGE_START = 0
    private var current_page = PAGE_START
    private var isLastPage = false
    private var totalPage = 500
    private var isLoading = false
    private var maxPrice: Float = 1f
    private var minPrice: Float = 0f
    private var isFavouriteChecked = false
    private var isOfferChecked = false
    private lateinit var tyreDetail: Models.TyreDetail
    private lateinit var tyreDetailFilter: Models.TyreDetail
    private lateinit var textView_Brand_name: TextView
    private lateinit var textView_Rating_name: TextView
    private lateinit var textView_season_name: TextView
    private lateinit var textView_Speed_Index_name: TextView
    private lateinit var textView_Speed_Load_Index_name: TextView
    private lateinit var isSwitch_OfferCoupon: Switch
    private lateinit var isSwitch_OnlyFav: Switch
    private lateinit var isSwitch_Reinforced: Switch
    private lateinit var isSwitch_RunFlat: Switch
    private lateinit var priceRangeSeekerBar: RangeSeekBar
    private var brandFilterAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    var season_type: JSONArray = JSONArray()
    var speed_index: JSONArray = JSONArray()
    var speed_load_index: JSONArray = JSONArray()

    lateinit var brandList: ArrayList<Models.brand>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyre_list)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.tyres)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (getTyreDetail() == null) {
            finish()
        } else {
            try {
                tyreDetail = getTyreDetail()!!
                tyreDetailFilter = getTyreDetail()!!
                tyreDetail.let {
                    setTyreTitle()
                    searchString = "" + it.width.toInt() + it.aspectRatio.toInt() + it.diameter.toInt()
                    progress_bar.visibility = View.VISIBLE

                    loadTyreData()


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            recycler_view.setHasFixedSize(true)
            val linearLayoutManager = LinearLayoutManager(this)
            recycler_view.layoutManager = linearLayoutManager

            recyclerViewAdapter = RecyclerViewAdapter(this@TyreListActivity, ArrayList())
            recycler_view.adapter = recyclerViewAdapter

            recycler_view.addOnScrollListener(object : PaginationListener(linearLayoutManager) {

                override fun loadMoreItems() {
                    isLoading = true
                    current_page += 10
                    loadTyreData()
                }

                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }
            })


            RetrofitClient.client.getTyreSpecification(getSavedSelectedVehicleID(), searchString)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val body = response.body()?.string()
                            if (response.isSuccessful) {
                                if (isStatusCodeValid(body)) {
                                    try {


                                        val jsonObject = JSONObject(body)
                                        val data = jsonObject.getJSONObject("data") as JSONObject
                                        speed_index = data.getJSONArray("speed_index") as JSONArray
                                        season_type = data.getJSONArray("season_tyre_type") as JSONArray
                                        speed_load_index = data.getJSONArray("load_index") as JSONArray
                                        var PriceObject = data.getJSONObject("price")

                                        filterTyreSeasonList.clear()
                                        filterTyreSpeedIndexList.clear()
                                        filterTyreSpeedLoadIndexList.clear()
                                        filterTyreSeasonList.add(0, Models.TypeSpecification(getString(R.string.all), "0"))
                                        for (tyreType in 0 until season_type.length()) {
                                            val tyreTypeObject: JSONObject = season_type.get(tyreType) as JSONObject


                                            filterTyreSeasonList.add(tyreType + 1, Models.TypeSpecification(tyreTypeObject.optString("name"), tyreTypeObject.optString("id")))
                                        }
                                        filterTyreSpeedIndexList.add(0, Models.TypeSpecification(getString(R.string.all), "0"))
                                        for (speedIndex in 0 until speed_index.length()) {
                                            val speedIndexObject: JSONObject = speed_index.get(speedIndex) as JSONObject

                                            filterTyreSpeedIndexList.add(Models.TypeSpecification(speedIndexObject.optString("name"), speedIndexObject.optString("id")))
                                        }
                                        filterTyreSpeedLoadIndexList.add(0, Models.TypeSpecification(getString(R.string.all), "0"))
                                        for (speedLoadIndexObj in 0 until speed_load_index.length()) {
                                            val speedLoadIndexObject: JSONObject = speed_load_index.get(speedLoadIndexObj) as JSONObject
                                            filterTyreSpeedLoadIndexList.add(Models.TypeSpecification(speedLoadIndexObject.optString("load_speed_index"), ""))
                                        }


                                    } catch (e: Exception) {
                                        e.message
                                        e.printStackTrace()
                                    }


                                }
                            }


                        }
                    })

            edit_tyre.setOnClickListener {

                startActivityForResult(intentFor<TyreDiameterActivity>().putExtra("currentlySelectedMeasurement", tyreDetail.convertToJsonString()).putExtra("TyreSelectedId", tyreDetail.id), 100)
            }

            filter_btn.setOnClickListener {
                createFilterDialog()
                filterDialog.show()
            }

            sort_btn.setOnClickListener {
                sortDialog.show()

            }

            // createFilterDialog()
            createSortDialog()

        }


    }


    private fun loadTyreData() {
        setTyreTitle()
        val drawableRight = ContextCompat.getDrawable(this@TyreListActivity, R.drawable.shape_circle_orange_8dp)
        drawableRight?.setBounds(100, 100, 100, 100)
        if (tyreDetail.onlyFav || (tyreDetail.offerOrCoupon) || (tyreDetail.runFlat != tyreDetail.cust_runflat) || (tyreDetail.reinforced != tyreDetail.cust_reinforced) || tyreDetail.brands != "" || tyreDetail.Rating != "" || (!tyreDetail.seasonId.trim().equals("") && !tyreDetail.seasonId.trim().equals("0") && tyreDetail.cust_seasonId != tyreDetail.seasonId) || (!tyreDetail.speedIndexId.trim().equals("") && !tyreDetail.speedIndexId.trim().equals("0") && tyreDetail.speedIndexId != tyreDetail.cust_speedIndexId) || ((!tyreDetail.speed_load_index.trim().equals(getString(R.string.All)) && !tyreDetail.speed_load_index.trim().equals(getString(R.string.all_in_italin))) && tyreDetail.speed_load_index != tyreDetail.cust_speedLoad_indexId) || !tyreDetail.priceRange.isNullOrBlank()) {

            this@TyreListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableRight, null)
        } else {
            this@TyreListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)

        }

        if (tyreDetail.onlyFav) {
            Log.d("tyre_filter", "1")
        } else if (tyreDetail.offerOrCoupon) {
            Log.d("tyre_filter", "2")
        } else if ((tyreDetail.runFlat != tyreDetail.cust_runflat)) {
            Log.d("tyre_filter", "3" + tyreDetail.runFlat + "=" + tyreDetail.cust_runflat)
        } else if (tyreDetail.reinforced != tyreDetail.cust_reinforced) {
            Log.d("tyre_filter", "4" + tyreDetail.reinforced + "=" + tyreDetail.cust_reinforced)
        } else if (tyreDetail.brands != "") {
            Log.d("tyre_filter", "5")
        } else if ((!tyreDetail.seasonId.trim().equals("") && !tyreDetail.seasonId.trim().equals("0") && tyreDetail.cust_seasonId != tyreDetail.seasonId)) {

            Log.d("tyre_filter", "6:" + tyreDetail.seasonId + "=" + tyreDetail.cust_seasonId)
        } else if (!tyreDetail.speedIndexId.trim().equals("") && !tyreDetail.speedIndexId.trim().equals("0") && tyreDetail.speedIndexId != tyreDetail.cust_speedIndexId) {
            Log.d("tyre_filter", "7:" + tyreDetail.speedIndexId + "=" + tyreDetail.cust_speedIndexId)
        } else if (((!tyreDetail.speed_load_index.trim().equals(getString(R.string.All)) && !tyreDetail.speed_load_index.trim().equals(getString(R.string.all_in_italin))) && (tyreDetail.speed_load_index != tyreDetail.cust_speedLoad_indexId))) {
            Log.d("tyre_filter", "7:" + tyreDetail.speed_load_index + "=" + tyreDetail.cust_speedLoad_indexId)
        } else if (!tyreDetail.priceRange.isNullOrBlank()) {
            Log.d("tyre_filter", "8")
        }

        var seasonId = ""
        var speedindexId = ""
        var speedloadindex = ""

        if (tyreDetail.seasonId == "0") {
            seasonId = ""
        } else {
            seasonId = tyreDetail.seasonId
        }
        if (tyreDetail.speedIndexId == "0") {
            speedindexId = ""
        } else {
            speedindexId = tyreDetail.speedIndexId
        }
        if (tyreDetail.speed_load_index == "0" || tyreDetail.speed_load_index == getString(R.string.all_in_italin) || tyreDetail.speed_load_index == getString(R.string.all)) {
            speedloadindex = ""
        } else {
            speedloadindex = tyreDetail.speed_load_index
        }

        /*  if (!tyreDetail.Rating.isNullOrBlank()) {

              var ratingArray = tyreDetail.Rating.split(",")
              rating = if (ratingArray.size == 1) {
                  "0," + ratingArray[0]
              } else {
                  (ratingArray[0] + "," + ratingArray[ratingArray.size - 1])
              }

          }*/
        try {
            RetrofitClient.client.tyreList(
                    tyreDetail.vehicleType,
                    searchString,
                    tyreDetail.brands,
                    seasonId,
                    speedindexId,
                    current_page.toString(),
                    if (tyreDetail.onlyFav) "1" else "0",
                    if (tyreDetail.offerOrCoupon) "1" else "0",
                    if (tyreDetail.reinforced) "1" else "0",
                    if (tyreDetail.runFlat) "1" else "0",
                    priceSortLevel.toString(),
                    tyreDetail.priceRange,/* tyreDetail.AlphabeticalOrder,*/tyreDetail.Rating,
                    "2", getUserId(), speedloadindex
            )
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progress_bar.visibility = View.GONE
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            progress_bar.visibility = View.GONE
                            recycler_view.visibility = View.VISIBLE

                            val body = response.body()?.string()
                            if (isStatusCodeValid(body)) {
                                val dataSet = getDataSetArrayFromResponse(body)
                                progress_bar.visibility = View.GONE
                                if (!dataSet.isEmpty())
                                    setView(jsonArray = dataSet)
                                else {
                                    Toast.makeText(applicationContext, getString(R.string.NoItemFound), Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                try {
                                    isLastPage = true
                                    recyclerViewAdapter?.removeLoading()
                                    val jsonObject = JSONObject(body)
                                    progress_bar.visibility = View.GONE
                                    Toast.makeText(applicationContext, jsonObject.optString("message"), Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    })
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun createFilterDialog() {
        tyreDetail = getTyreDetail()!!
        filterDialog = Dialog(this, R.style.DialogSlideAnimStyle)
        with(filterDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_tyre_filter_category)
            initDialogCategory(filterDialog)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            textView_Brand_name = tv_Brand_name
            textView_Rating_name = tv_Rating_name
            textView_season_name = tv_season_name
            textView_Speed_Index_name = tv_Speed_Index_name
            priceRangeSeekerBar = RS_dialog_price_range
            isSwitch_OfferCoupon = switch_OfferCoupon
            isSwitch_OnlyFav = switch_OnlyFav
            isSwitch_Reinforced = switch_Reinforced
            isSwitch_RunFlat = switch_RunFlat
            textView_Speed_Load_Index_name = tv_Speed_load_Index_name





            try {
                priceRangeSeekerBar.setRange(minPrice, maxPrice)
                Log.d("tyre_list", "try filter minPrice$minPrice")
                Log.d("tyre_list", "try filter maxPrice$maxPrice")
                if (!tyreDetail.priceRange.isNullOrBlank()) {
                    var minPrice1 = tyreDetail.priceRange.split(",")[0].toFloat()
                    val maxPrice1 = tyreDetail.priceRange.split(",")[1].toFloat()

                    if (minPrice >= minPrice1) {
                        minPrice1 = minPrice
                    }
                    Log.d("tyre_list", "minPrice1 if $minPrice1")
                    Log.d("tyre_list", "maxPrice1 if $maxPrice1")
                    priceRangeSeekerBar.setValue(minPrice1, maxPrice1)
                    tv_price_start_range.text = getString(R.string.prepend_euro_symbol_string, minPrice1.toString())
                    tv_price_end_range.text = getString(R.string.prepend_euro_symbol_string, maxPrice1.toString())

                } else {
                    priceRangeSeekerBar.setValue(minPrice, maxPrice)
                    tv_price_start_range.text = getString(R.string.prepend_euro_symbol_string, minPrice.toString())
                    tv_price_end_range.text = getString(R.string.prepend_euro_symbol_string, maxPrice.toString())
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {


                priceRangeSeekerBar.setOnRangeChangedListener(object : OnRangeChangedListener {
                    override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                    override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {

                        if (rightValue.toString().contains(".0")) {
                            tempPriceInitial = floor(leftValue)
                            tempPriceFinal = ceil(rightValue)
                        } else {
                            tempPriceInitial = /*floor*/(leftValue).toDouble().roundTo2Places().toFloat()
                            tempPriceFinal = /*ceil*/(rightValue).toDouble().roundTo2Places().toFloat()
                        }



                        priceRangeInitial = tempPriceInitial
                        priceRangeFinal = tempPriceFinal
                        tv_price_start_range.text = getString(R.string.prepend_euro_symbol_string, tempPriceInitial.toString())
                        tv_price_end_range.text = getString(R.string.prepend_euro_symbol_string, tempPriceFinal.toString())

                        if (priceRangeInitial.equals(minPrice) && priceRangeFinal.equals(maxPrice)) {
                            priceRange = ""


                        } else {
                            priceRange = "$priceRangeInitial,$priceRangeFinal"

                        }


                    }

                    override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                })


                if (tyreDetail.runFlat) {
                    switch_RunFlat.isChecked = true
                }
                if (tyreDetail.reinforced) {
                    switch_Reinforced.isChecked = true
                }

                if (isOfferChecked || tyreDetail.offerOrCoupon) {
                    switch_OfferCoupon.isChecked = true
                }

                if (isFavouriteChecked || tyreDetail.onlyFav) {
                    switch_OnlyFav.isChecked = true
                }

                tv_Brand_name.text = tyreDetail.brands
                tv_Rating_name.text = if (tyreDetail.Rating.equals("0")) "" else tyreDetail.Rating


                tv_season_name.text = tyreDetail.seasonName
                tv_Speed_Index_name.text = tyreDetail.speedIndexName
                tv_Speed_load_Index_name.text = tyreDetail.speed_load_index
            } catch (e: Exception) {
                e.printStackTrace()
            }


            toolbar.setOnMenuItemClickListener {

                FinalFilter(false)
                dismiss()
                return@setOnMenuItemClickListener true
            }
            toolbar.setNavigationOnClickListener {
                setTyreDetail(tyreDetailFilter)
                dismiss()
            }
            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar_title.text = getText(R.string.filter)

            clearselection.setOnClickListener {
                try {
                    priceRangeSeekerBar.setValue(minPrice, maxPrice)
                    filterBrandList.clear()
                    filterTyreSeasonList.clear()
                    filterTyreSpeedIndexList.clear()
                    filterTyreSpeedLoadIndexList.clear()
                    tv_Brand_name.text = ""
                    tv_Rating_name.text = ""
                    tv_season_name.text = tyreDetail.cust_seasonName
                    tv_Speed_Index_name.text = tyreDetail.cust_speed_indexName
                    tv_Speed_load_Index_name.text = tyreDetail.cust_speedLoad_indexId
                    tyreDetail.brands = ""
                    tyreDetail.Rating = ""

                    tyreDetail.seasonName = tyreDetail.cust_seasonName
                    tyreDetail.seasonId = tyreDetail.cust_seasonId
                    tyreDetail.speedIndexName = tyreDetail.cust_speed_indexName
                    tyreDetail.speedIndexId = tyreDetail.cust_speedIndexId

                    tyreDetail.runFlat = tyreDetail.cust_runflat
                    tyreDetail.reinforced = tyreDetail.cust_reinforced
                    isOfferChecked = false
                    tyreDetail.offerOrCoupon = false
                    isFavouriteChecked = false
                    tyreDetail.onlyFav = false
                    tyreDetail.priceRange = ""

                    switch_RunFlat.isChecked = tyreDetail.cust_runflat
                    switch_Reinforced.isChecked = tyreDetail.cust_reinforced
                    switch_OfferCoupon.isChecked = false
                    switch_OnlyFav.isChecked = false
                    tyreDetail.speed_load_index = tyreDetail.cust_speedLoad_indexId
                    priceRange = ""
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            create()

        }


    }

    private fun setCheckedListener(layout: View, checkbox: CheckBox, onCheckedChanged: ((isChecked: Boolean) -> Unit)? = null) {
        layout.setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked
        }
        checkbox.setOnCheckedChangeListener { _, isChecked -> onCheckedChanged?.invoke(isChecked) }
    }


    private fun createSortDialog() {
        sortDialog = Dialog(this@TyreListActivity, R.style.DialogSlideAnimStyle)

        with(sortDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_sorting)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { dismiss() }
            toolbar_title.text = resources.getString(R.string.sort)
            toolbar.inflateMenu(R.menu.menu_single_item)
            if (!tyreDetail.priceLevel.isNullOrBlank()) if (tyreDetail.priceLevel == "0") radio_grp_price.check(R.id.rb_price_low) else radio_grp_price.check(R.id.rb_price_high) else radio_grp_price.check(R.id.rb_price_low)
            if (!tyreDetail.AlphabeticalOrder.isNullOrBlank()) if (tyreDetail.AlphabeticalOrder == "0") radio_grp_Alphabetical.check(R.id.rb_Alphabetical_Ascending) else radio_grp_Alphabetical.check(R.id.rb_Alphabetical_Descending) else radio_grp_Alphabetical.check(R.id.rb_Alphabetical_Ascending)

            tv_Sort_ClearSection.setOnClickListener {
                /*   tyreDetail.priceLevel = "0"
                   tyreDetail.AlphabeticOrder = "0"*/
                radio_grp_price.check(R.id.rb_price_low)
                radio_grp_Alphabetical.check(R.id.rb_Alphabetical_Ascending)

            }

            toolbar.setOnMenuItemClickListener {


                val priceIndex = radio_grp_price.indexOfChild(radio_grp_price.findViewById(radio_grp_price.checkedRadioButtonId))
                val radio_grp_Alphabetical = radio_grp_Alphabetical.indexOfChild(radio_grp_Alphabetical.findViewById(radio_grp_Alphabetical.checkedRadioButtonId))
                tyreDetail.priceLevel = priceIndex.toString()
                tyreDetail.AlphabeticalOrder = radio_grp_Alphabetical.toString()
                FinalFilter(true)
                dismiss()
                return@setOnMenuItemClickListener true
            }
            create()

        }
    }


    private fun loadSortedProducts() {

        progress_bar.visibility = View.VISIBLE

        priceSortLevel = tyreDetail.priceLevel.toInt()
        tyreDetail.priceRange = priceRange
        setTyreDetail(tyreDetail)
        tyreDetailFilter = getTyreDetail()!!
        current_page = PAGE_START//for every filter click we set limit to its initial and clear recycler adapter
        recyclerViewAdapter!!.clear()

        loadTyreData()

    }

    private fun initDialogCategory(dialog: Dialog) {


        dialog.ll_item_brand.setOnClickListener {

            val brandsDialog = CreateSubDialog(getString(R.string.brand))
            brandsDialog.progressbar.visibility = View.VISIBLE
            hideKeyboard()



            RetrofitClient.client.getProductBrandList("2")
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progress_bar.visibility = View.GONE
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            response?.body()?.string()?.let {

                                if (isStatusCodeValid(it)) {
                                    val dataSet = getDataSetArrayFromResponse(it)
                                    val gson = GsonBuilder().create()

                                    brandList = gson.fromJson(dataSet.toString(), Array<Models.brand>::class.java).toCollection(java.util.ArrayList<Models.brand>())
                                    setBrandAdapter(brandList, brandsDialog.rv_subcategory)
                                    brandsDialog.progressbar.visibility = View.GONE

                                }
                            }

                        }
                    })


            brandsDialog.create()
            brandsDialog.show()
        }
        dialog.ll_item_Rating.setOnClickListener {
            val ratingDialog = Dialog(this@TyreListActivity, R.style.DialogSlideAnimStyle)
            with(ratingDialog) {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.dialog_rating_tyrefilter)
                window?.setGravity(Gravity.TOP)
                window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)


                val filterTyreRatingList: MutableList<String> = ArrayList()

                if (!tyreDetail.Rating.isNullOrBlank()) {
                    textView_Rating_name.text = if (tyreDetail.Rating.equals("0")) "" else tyreDetail.Rating
                    val arrayList = tyreDetail.Rating.split(",")
                    for (n in 0 until arrayList.size) {
                        filterTyreRatingList.add(arrayList[n])

                        when (arrayList[n]) {

                            "5" -> dialog_rating_five.isChecked = true
                            "4" -> dialog_rating_four.isChecked = true
                            "3" -> dialog_rating_three.isChecked = true
                            "2" -> dialog_rating_two.isChecked = true
                            "1" -> dialog_rating_one.isChecked = true
                        }
                    }

                }


                toolbar.setNavigationOnClickListener {
                    setRatingToTyreDetail(filterTyreRatingList)
                    dismiss()
                }
                toolbar.setOnMenuItemClickListener {

                    setRatingToTyreDetail(filterTyreRatingList)
                    FinalFilter(false)
                    dismiss()
                    return@setOnMenuItemClickListener true
                }
                toolbar_title.text = getString(R.string.rating)
                toolbar.inflateMenu(R.menu.menu_single_item)

                dialog_rating_five.setOnCheckedChangeListener { compoundButton, b ->
                    setRatingtoFilterList(b, filterTyreRatingList, "5")


                }
                dialog_rating_four.setOnCheckedChangeListener { compoundButton, b ->
                    setRatingtoFilterList(b, filterTyreRatingList, "4")


                }
                dialog_rating_three.setOnCheckedChangeListener { compoundButton, b ->
                    setRatingtoFilterList(b, filterTyreRatingList, "3")


                }
                dialog_rating_two.setOnCheckedChangeListener { compoundButton, b ->
                    setRatingtoFilterList(b, filterTyreRatingList, "2")

                }
                dialog_rating_one.setOnCheckedChangeListener { compoundButton, b ->
                    setRatingtoFilterList(b, filterTyreRatingList, "1")


                }

            }
            ratingDialog.create()
            ratingDialog.show()

        }
        dialog.ll_item_Season.setOnClickListener {
            val SeasonDialog = CreateSubDialog(getString(R.string.season))
            SeasonDialog.item_All_category_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    tyreDetail.seasonId = "0"
                    tyreDetail.seasonName = getString(R.string.all)
                    filterTyreSeasonList.clear()
                    filterTyreSeasonList.add(Models.TypeSpecification(getString(R.string.all), "0"))
                    SeasonTypeCheckboxBinding(SeasonDialog)
                } else {
                    filterTyreSeasonList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    tyreDetail.seasonId = ""
                    tyreDetail.seasonName = ""
                }


            }
            SeasonDialog.item_All_category_checkbox.isChecked = tyreDetail.seasonId == "0" || tyreDetail.seasonName == getString(R.string.all) || tyreDetail.seasonName == getString(R.string.all_in_italin)
            SeasonTypeCheckboxBinding(SeasonDialog)
            SeasonDialog.create()
            SeasonDialog.show()
        }


        dialog.ll_item_Speed_load_index.setOnClickListener {
            val SpeedLoadIndexDialog = CreateSubDialog(getString(R.string.speed_load_index))
            SpeedLoadIndexDialog.item_All_category_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    tyreDetail.speed_load_index = getString(R.string.all)

                    filterTyreSpeedLoadIndexList.clear()
                    filterTyreSpeedLoadIndexList.add(Models.TypeSpecification(getString(R.string.all), "0"))
                    speedLoadindexCheckboxBinding(SpeedLoadIndexDialog)
                } else {
                    filterTyreSpeedLoadIndexList.remove(Models.TypeSpecification(getString(R.string.all), "0"))

                    tyreDetail.speed_load_index = ""

                }


            }
            SpeedLoadIndexDialog.item_All_category_checkbox.isChecked = tyreDetail.speed_load_index == getString(R.string.all_in_italin) || tyreDetail.speed_load_index == getString(R.string.all)


            speedLoadindexCheckboxBinding(SpeedLoadIndexDialog)

            SpeedLoadIndexDialog.create()
            SpeedLoadIndexDialog.show()
        }


        dialog.ll_item_Speed_index.setOnClickListener {


            val SpeedIndexDialog = CreateSubDialog(getString(R.string.speed_index))

            SpeedIndexDialog.item_All_category_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    tyreDetail.speedIndexName = getString(R.string.all)
                    tyreDetail.speedIndexId = "0"
                    filterTyreSpeedIndexList.clear()
                    filterTyreSpeedIndexList.add(Models.TypeSpecification(getString(R.string.all), "0"))
                    SpeedIndexCheckBox(SpeedIndexDialog)
                } else {
                    filterTyreSpeedIndexList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    tyreDetail.speedIndexName = ""
                    tyreDetail.speedIndexId = ""
                }


            }
            SpeedIndexDialog.item_All_category_checkbox.isChecked = tyreDetail.speedIndexId == "0" || tyreDetail.speedIndexName == getString(R.string.all_in_italin) || tyreDetail.speedIndexName == getString(R.string.all)


            SpeedIndexCheckBox(SpeedIndexDialog)

            SpeedIndexDialog.create()
            SpeedIndexDialog.show()
        }

    }

    private fun SeasonTypeCheckboxBinding(SeasonDialog: Dialog) {
        var tyreSeasonFilterAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
        tyreSeasonFilterAdapter = SeasonDialog.rv_subcategory.setJSONArrayAdapter(this@TyreListActivity, season_type, R.layout.item_checkbox) { itemView, _, jsonObject ->
            val tyreSeasonName = jsonObject.optString("name")
            val tyreSeasonCode = jsonObject.optString("id")

            itemView.item_checkbox_text.text = tyreSeasonName?.toUpperCase()
            if (tyreDetail.seasonName != null) {

                val seasonType = tyreDetail.seasonId.split(",")
                val seasonTypename = tyreDetail.seasonName.split(",")
                itemView.item_checkbox.isChecked = seasonType.contains(tyreSeasonCode) || seasonTypename.contains(tyreSeasonName)

            } else itemView.item_checkbox.isChecked = false


            setCheckedListener(itemView.item_checkbox_container, itemView.item_checkbox) { isChecked ->

            }

            itemView.item_checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (SeasonDialog.item_All_category_checkbox.isChecked) {
                    SeasonDialog.item_All_category_checkbox.isChecked = false
                    filterTyreSeasonList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    tyreDetail.seasonId = ""
                    tyreDetail.seasonName = ""
                }
                if (isChecked) {

                    filterTyreSeasonList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    if (!filterTyreSeasonList.contains(Models.TypeSpecification(tyreSeasonName, tyreSeasonCode)))
                        filterTyreSeasonList.add(Models.TypeSpecification(tyreSeasonName, tyreSeasonCode))
                } else filterTyreSeasonList.remove(Models.TypeSpecification(tyreSeasonName, tyreSeasonCode))

            }

            if (itemView.item_checkbox.isChecked) {
                if (!filterTyreSeasonList.contains(Models.TypeSpecification(tyreSeasonName, tyreSeasonCode))) {
                    filterTyreSeasonList.add(Models.TypeSpecification(tyreSeasonName, tyreSeasonCode))
                }
            } else filterTyreSeasonList.remove(Models.TypeSpecification(tyreSeasonName, tyreSeasonCode))


        }

    }

    private fun speedLoadindexCheckboxBinding(SpeedLoadIndexDialog: Dialog) {
        var tyreSpeedIndexFilterAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
        tyreSpeedIndexFilterAdapter = SpeedLoadIndexDialog.rv_subcategory.setJSONArrayAdapter(this@TyreListActivity, speed_load_index, R.layout.item_checkbox) { itemView, position, jsonObject ->
            val tyreSpeedloadIndex = jsonObject.optString("load_speed_index")
            itemView.item_checkbox_text.text = tyreSpeedloadIndex
            if (tyreDetail.speed_load_index != null) {
                val loadIndexArray = tyreDetail.speed_load_index.split(",")
                itemView.item_checkbox.isChecked = loadIndexArray.contains(tyreSpeedloadIndex)

            } else itemView.item_checkbox.isChecked = false

            setCheckedListener(itemView.item_checkbox_container, itemView.item_checkbox) { isChecked ->


            }
            itemView.item_checkbox.setOnCheckedChangeListener { _, isChecked ->

                if (SpeedLoadIndexDialog.item_All_category_checkbox.isChecked) {
                    SpeedLoadIndexDialog.item_All_category_checkbox.isChecked = false
                    filterTyreSpeedLoadIndexList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    tyreDetail.speed_load_index = ""

                }
                if (isChecked) {
                    filterTyreSpeedLoadIndexList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    if (!filterTyreSpeedLoadIndexList.contains(Models.TypeSpecification(tyreSpeedloadIndex, ""))) {
                        filterTyreSpeedLoadIndexList.add(Models.TypeSpecification(tyreSpeedloadIndex, ""))
                    }
                } else filterTyreSpeedLoadIndexList.remove(Models.TypeSpecification(tyreSpeedloadIndex, ""))


            }
            if (itemView.item_checkbox.isChecked) {
                if (!filterTyreSpeedLoadIndexList.contains(Models.TypeSpecification(tyreSpeedloadIndex, ""))) {
                    filterTyreSpeedLoadIndexList.add(Models.TypeSpecification(tyreSpeedloadIndex, ""))
                }
            } else filterTyreSpeedLoadIndexList.remove(Models.TypeSpecification(tyreSpeedloadIndex, ""))
        }

    }

    private fun SpeedIndexCheckBox(SpeedIndexDialog: Dialog) {
        var tyreSpeedIndexFilterAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

        tyreSpeedIndexFilterAdapter = SpeedIndexDialog.rv_subcategory.setJSONArrayAdapter(this@TyreListActivity, speed_index, R.layout.item_checkbox) { itemView, position, jsonObject ->
            val tyreSpeedIndexName = jsonObject.optString("name")
            val tyreSpeedIndexCode = jsonObject.optString("id")
            itemView.item_checkbox_text.text = tyreSpeedIndexName
            if (tyreDetail.speedIndexName != null) {
                val speedIdexArray = tyreDetail.speedIndexName.split(",")
                itemView.item_checkbox.isChecked = speedIdexArray.contains(tyreSpeedIndexName)

            } else itemView.item_checkbox.isChecked = false

            setCheckedListener(itemView.item_checkbox_container, itemView.item_checkbox) { isChecked ->

            }
            itemView.item_checkbox.setOnCheckedChangeListener { _, isChecked ->

                if (SpeedIndexDialog.item_All_category_checkbox.isChecked) {
                    SpeedIndexDialog.item_All_category_checkbox.isChecked = false
                    filterTyreSpeedIndexList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    tyreDetail.speedIndexId = ""
                    tyreDetail.speedIndexName = ""

                }
                if (isChecked) {
                    filterTyreSpeedIndexList.remove(Models.TypeSpecification(getString(R.string.all), "0"))
                    if (!filterTyreSpeedIndexList.contains(Models.TypeSpecification(tyreSpeedIndexName, tyreSpeedIndexCode)))
                        filterTyreSpeedIndexList.add(Models.TypeSpecification(tyreSpeedIndexName, tyreSpeedIndexCode))
                } else filterTyreSpeedIndexList.remove(Models.TypeSpecification(tyreSpeedIndexName, tyreSpeedIndexCode))

            }
            if (itemView.item_checkbox.isChecked) {
                if (!filterTyreSpeedIndexList.contains(Models.TypeSpecification(tyreSpeedIndexName, tyreSpeedIndexCode))) {
                    filterTyreSpeedIndexList.add(Models.TypeSpecification(tyreSpeedIndexName, tyreSpeedIndexCode))
                }
            } else filterTyreSpeedIndexList.remove(Models.TypeSpecification(tyreSpeedIndexName, tyreSpeedIndexCode))
        }

    }


    private fun CreateSubDialog(toolbarTitle: String): Dialog {
        val brandsDialog = Dialog(this@TyreListActivity, R.style.DialogSlideAnimStyle)
        with(brandsDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_tyre_filter_subcategory)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar_title.text = toolbarTitle
            toolbar.inflateMenu(R.menu.menu_single_item)

            if (toolbarTitle == context.getString(R.string.brand)) {
                search_view.visibility = View.VISIBLE


            } else {
                item_checkbox_container.visibility = View.VISIBLE
                search_view.visibility = View.GONE
            }
            toolbar.setNavigationOnClickListener {
                SetDataToTyreList(toolbarTitle)
                brandsDialog.dismiss()

            }

            search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {

                    if (!newText.isNullOrBlank()) {

                        val newText1 = newText.toLowerCase()

                        val brandList1: ArrayList<Models.brand> = ArrayList<Models.brand>()
                        for (brand in brandList) {
                            val text = brand.brandName?.toLowerCase()
                            if (text?.contains(newText1)!!) {
                                brandList1.add(brand)
                            }
                        }
                        setBrandAdapter(brandList1, brandsDialog.rv_subcategory)
                    } else {
                        setBrandAdapter(brandList, brandsDialog.rv_subcategory)
                    }


                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    // task HERE
                    return false
                }

            })



            toolbar.setOnMenuItemClickListener {

                SetDataToTyreList(toolbarTitle)
                FinalFilter(false)
                dismiss()


                return@setOnMenuItemClickListener true
            }

        }
        return brandsDialog
    }

    private fun SetDataToTyreList(toolbarTitle: String) {
        if (toolbarTitle == getString(R.string.brand)) {
            setBrandToTyreList()
        } else if (toolbarTitle == getString(R.string.season)) {
            setSeasonTypeToTyreList()

        } else if (toolbarTitle == getString(R.string.speed_index)) {
            setSpeedIndexToTyreList()
        } else if (toolbarTitle == getString(R.string.speed_load_index)) {
            setSpeedLoadIndexToTyreList()
        }
    }

    private fun setSpeedLoadIndexToTyreList() {
        var speed_load_IndexName = ""
        val tyreSpeed_load_Index: MutableList<String> = ArrayList()
        for (i in 0 until filterTyreSpeedLoadIndexList.size) {
            val item = filterTyreSpeedLoadIndexList.get(i)
            tyreSpeed_load_Index.add(item.name)
        }
        if (tyreSpeed_load_Index.size > 0) {
            speed_load_IndexName = tyreSpeed_load_Index.joinToString(",")
            tyreDetail.speed_load_index = speed_load_IndexName
            textView_Speed_Load_Index_name.text = speed_load_IndexName

        } else {
            textView_Speed_Load_Index_name.text = ""
            tyreDetail.speed_load_index = ""

        }
    }

    private fun setSpeedIndexToTyreList() {
        var speedIndexName = ""
        val tyreSpeedIndex: MutableList<String> = ArrayList()
        val tyreSpeedIndexId: MutableList<String> = ArrayList()
        for (i in 0 until filterTyreSpeedIndexList.size) {
            val item = filterTyreSpeedIndexList.get(i)
            tyreSpeedIndex.add(item.name)
            tyreSpeedIndexId.add((item.code))
        }
        Log.d("speedIndexvalue", filterTyreSpeedIndexList.size.toString())
        if (tyreSpeedIndex.size > 0) {
            speedIndexName = tyreSpeedIndex.joinToString(",")
            tyreDetail.speedIndexName = speedIndexName
            tyreDetail.speedIndexId = tyreSpeedIndexId.joinToString(",")
            textView_Speed_Index_name.text = speedIndexName
        } else {
            textView_Speed_Index_name.text = ""
            tyreDetail.speedIndexName = ""
            tyreDetail.speedIndexId = ""
        }
    }

    private fun setSeasonTypeToTyreList() {
        var seasonType = ""
        val tyreSeason: MutableList<String> = ArrayList()

        val tyreSeasonTypeId: MutableList<String> = ArrayList()
        for (i in 0 until filterTyreSeasonList.size) {
            val item = filterTyreSeasonList.get(i)
            tyreSeason.add(item.name)
            tyreSeasonTypeId.add(item.code)
        }
        if (tyreSeason.size > 0) {
            seasonType = tyreSeason.joinToString(",")
            tyreDetail.seasonName = seasonType
            tyreDetail.seasonId = tyreSeasonTypeId.joinToString(",")
            textView_season_name.text = seasonType
        } else {
            textView_season_name.text = ""
            tyreDetail.seasonName = ""
            tyreDetail.seasonId = ""

        }

    }

    private fun setBrandToTyreList() {
        var brands = ""
        val brands1: MutableList<String> = ArrayList()
        for (i in 0 until filterBrandList.size) {
            val item = filterBrandList.get(i)
            brands1.add(item)
        }
        if (brands1.size > 0) {
            brands = brands1.joinToString(",")
            tyreDetail.brands = brands
            textView_Brand_name.text = brands
        } else {
            textView_Brand_name.text = ""
            tyreDetail.brands = ""
        }

    }

    private fun setRatingtoFilterList(checked: Boolean, filterTyreRatingList: MutableList<String>, ratingStar: String) {
        if (checked) {
            filterTyreRatingList.add(ratingStar)
        } else {
            filterTyreRatingList.remove(ratingStar)
        }
    }

    private fun setRatingToTyreDetail(filterTyreRatingList: MutableList<String>) {
        var ratingString = ""
        filterTyreRatingList.sort()
        for (n in 0 until filterTyreRatingList.size) {
            if (n == filterTyreRatingList.size - 1) {
                ratingString += filterTyreRatingList[n]
            } else {
                ratingString = ratingString + filterTyreRatingList[n] + ","
            }

        }




        tyreDetail.Rating = ratingString

        textView_Rating_name.text = if (tyreDetail.Rating.equals("0")) "" else tyreDetail.Rating
    }

    private fun FinalFilter(isSort: Boolean) {
        if (!isSort) {
            tyreDetail.onlyFav = isSwitch_OnlyFav.isChecked
            tyreDetail.offerOrCoupon = isSwitch_OfferCoupon.isChecked
            tyreDetail.runFlat = isSwitch_RunFlat.isChecked
            tyreDetail.reinforced = isSwitch_Reinforced.isChecked

        }

        try {
            filterDialog.dismiss()
            loadSortedProducts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setView(jsonArray: JSONArray) {

        try {

            Handler().postDelayed(Runnable {
                kotlin.run {
                    val tyreListItems: MutableList<Models.TyreDetailItem> = ArrayList()
                    for (i in 0 until jsonArray.length()) {

                        val tyreObject = jsonArray.get(i) as JSONObject
                        val tyreDetailItem = Gson().fromJson<Models.TyreDetailItem>(tyreObject.toString(), Models.TyreDetailItem::class.java)
                        minPrice = if (!tyreDetailItem.min_price.isNullOrBlank()) tyreDetailItem.min_price.toFloat() else 0f
                        maxPrice = if (!tyreDetailItem.max_price.isNullOrBlank()) tyreDetailItem.max_price.toFloat() else 1f

                        tyreListItems.add(tyreDetailItem)
                    }
                    if (minPrice == maxPrice) {
                        minPrice = 0f
                    }
                    Log.d("tyre_List", "maxPrices$maxPrice")
                    Log.d("tyre_List", "minPrices$minPrice")
                    if (current_page != PAGE_START) recyclerViewAdapter?.removeLoading()
                    recyclerViewAdapter?.addItems(tyreListItems)

                    //check if last page or not
                    if (current_page < totalPage) {
                        recyclerViewAdapter?.addLoading()
                    } else {
                        isLastPage = true
                        recyclerViewAdapter?.removeLoading()
                    }
                    //if (tyreListItems.size<10) recyclerViewAdapter?.removeLoading()
                    isLoading = false

                }
            }, 250)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAdapter(jsonArray: JSONArray) {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title = view.item_title
            val distance = view.item_sub_title
            val icon = view.item_image
            val price = view.item_price
            val rating = view.item_rating
            val ratingCount = view.item_rating_count
            val tireCellContainer = view.tire_cell
            val tyreImage = view.item_image
            val matchCode = view.item_sub_title
            val ourDescription = view.item_description
            val tyreSeasonIcon = view.tyre_season_icon
            val tyreFuelValue = view.tyre_fuel_value
            val tyreWetGripValue = view.tyre_wet_grip_value
            val tyreDbValue = view.tyre_db_value
        }

        recycler_view.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
                return ViewHolder(layoutInflater.inflate(R.layout.item_tyre, p0, false))
            }

            override fun getItemCount(): Int = jsonArray.length()

            override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
                val jsonObject: JSONObject = jsonArray.get(p1) as JSONObject

                val tyreResponse: JSONObject = JSONObject(jsonObject.getString("tyre_response"))


                //wetGrip
                p0.title.text = tyreResponse.optString("manufacturer_description") + " " + tyreResponse.optString("wholesalerArticleNo")
                loadImage(tyreResponse.optString("imageUrl"), p0.icon)
                p0.matchCode.text = "ean ${tyreResponse.optString("ean_number")}"

                if (jsonObject.getString("seller_price").isNullOrEmpty() || jsonObject.getString("seller_price") == "null") {
                    p0.price.text = getString(R.string.prepend_euro_symbol_string, tyreResponse.optString("price"))
                } else {
                    p0.price.text = getString(R.string.prepend_euro_symbol_string, jsonObject.optString("seller_price"))
                }

                if (jsonObject.has("description") && jsonObject.get("description") != "null") {
                    p0.ourDescription.text = jsonObject.optString("description")
                } else if (jsonObject.optString("our_description").isEmpty() || jsonObject.optString("our_description") == "null") {
                    p0.ourDescription.text = tyreResponse.optString("manufacturer_description")
                }

                // set tyre icons
                p0.tyreWetGripValue.text = jsonObject.optString("wetGrip")
                p0.tyreFuelValue.text = jsonObject.optString("rollingResistance")
                p0.tyreDbValue.text = jsonObject.optString("extRollingNoiseDb") + " db"

                when (jsonObject.optString("type")) {
                    "s" -> loadImageFromDrawable(R.drawable.summer_tyre, p0.tyreSeasonIcon)
                    "w" -> loadImageFromDrawable(R.drawable.winter_tyre, p0.tyreSeasonIcon)
                    "m" -> loadImageFromDrawable(R.drawable.quad_tyre, p0.tyreSeasonIcon)
                    "g" -> loadImageFromDrawable(R.drawable.all_seasons_tyre, p0.tyreSeasonIcon)
                    "o" -> loadImageFromDrawable(R.drawable.off_road_, p0.tyreSeasonIcon)
                    "l" -> loadImageFromDrawable(R.drawable.truck, p0.tyreSeasonIcon)
                }


                p0.tireCellContainer.setOnClickListener {
                    startActivity(intentFor<TyreDetailActivity>(
                            Constant.Path.productDetails to JSONObject(jsonArray[p1].toString()).toString(),
                            Constant.Path.productType to "Tyre").forwardResults())
                }
            }

        }
    }

    private fun setBrandAdapter(brandList: ArrayList<Models.brand>, recyclerView: RecyclerView) {
        val BrandArray: ArrayList<String> = ArrayList<String>()
        if (!tyreDetail.brands.isNullOrBlank()) {
            val brand12 = tyreDetail.brands.split(",")
            for (brandobj in brand12) {
                BrandArray.add(brandobj)
            }
        }



        class Holder(view: View) : RecyclerView.ViewHolder(view)
        brandFilterAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


            override fun getItemCount(): Int {
                return brandList.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.item_checkbox, parent, false))
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val brandName = brandList[position].brandName
                holder.itemView.item_checkbox_text.text = brandList[position].brandName
                holder.itemView.item_checkbox.setOnCheckedChangeListener { compoundButton, b ->

                    if (b) {
                        if (!filterBrandList.contains(brandName)) brandName?.let {
                            filterBrandList.add(it)

                        }
                        brandList[position].isBrandchecked = true
                    } else {
                        brandList[position].isBrandchecked = false
                        filterBrandList.remove(brandName)
                        BrandArray.remove(brandName)

                    }

                }

                if (BrandArray.contains(brandName) || brandList[position].isBrandchecked) {
                    holder.itemView.item_checkbox.isChecked = true
                    brandList[position].isBrandchecked = true

                    if (!filterBrandList.contains(brandName)) {
                        brandName?.let { filterBrandList.add(it) }
                    }
                } else {
                    holder.itemView.item_checkbox.isChecked = false
                    brandList[position].isBrandchecked = false
                    filterBrandList.remove(brandName)
                }

            }
        }



        recyclerView.adapter = brandFilterAdapter
    }

    private fun setTyreTitle() {

        tyreDetail.let {
            title_tyre.text = resources.getString(R.string.select_measurements) + "\n" + (it.width.toInt()).toString() + "/" + it.aspectRatio + " R" + it.diameter.toInt().toString() + " " +

                    if (it.speed_load_index.equals(getString(R.string.All)) || it.speed_load_index.equals(getString(R.string.all_in_italin))) "" else it.speed_load_index + " " +

                            if (it.speedIndexId == "0" || it.speedIndexName == getString(R.string.All) || it.speedIndexName == getString(R.string.all_in_italin)) "" else it.speedIndexName

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        recreate()

        Log.d("TyreListActivity", "OnActivityResult")
    }
}
