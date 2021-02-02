package com.officinetop.officine.car_parts

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.gson.GsonBuilder
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.PaginationListener
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
import kotlinx.android.synthetic.main.item_checkbox.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.design.snackbar
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.floor

class ProductListActivity : BaseActivity(), FilterListInterface {
    private lateinit var drawableLeft: Drawable
    private lateinit var drawableRight: Drawable
    private lateinit var filterDialog: Dialog
    private lateinit var sortDialog: Dialog
    private var isBestSelling = false
    private var isSearchPreview = false
    val filterBrandList: MutableList<String> = ArrayList()
    private var selectedFormattedDate = ""
    private var ratingString = ""
    private var priceRangeInitial = 0
    private var priceRangeFinal = -1
    lateinit var linearLayoutManager: LinearLayoutManager
    var tempPriceFinal = -1
    var tempPriceInitial = 0
    var isfilterApply = false
    private var distanceRangeFinal = 0
    private var distanceRangeInitial = 100

    var tempDistanceFinal = -1
    var tempDistanceInitial = 0

    private var seekbarPriceInitialLimit = 0f
    private var seekbarPriceFinalLimit = 1000f


    private var isFavouriteChecked = false
    private var isOfferChecked = false

    private var isPriceLowToHigh = true
    private var isDistanceLowToHigh = true
    var serviceID = 0
    var productID = 0

    private var assembledProductDetail = ""

    private var hasRecyclerLoadedOnce = false
    private var calendarPriceMap: HashMap<String, String> = HashMap()

    private var searchedKeyWord = ""
    private var searchedCategoryType: String? = null

    lateinit var listAdapter: ProductOrWorkshopListAdapter
    var brandlist = ArrayList<Models.brand>()
    private val PAGE_START = 0
    private var current_page = PAGE_START
    private var isLastPage = false
    private var totalPage = 500
    private var isLoading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        filter_btn.setOnClickListener { filterDialog.show() }
        sort_btn.setOnClickListener { sortDialog.show() }

        isBestSelling = intent?.getBooleanExtra(Constant.Key.is_best_selling, false) ?: false
        isSearchPreview = intent?.getBooleanExtra(Constant.Key.is_searchPreview, false) ?: false

        selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
        searchedKeyWord = intent?.getStringExtra(Constant.Key.searchedKeyword) ?: ""
        searchedCategoryType = intent?.getStringExtra(Constant.Key.searchedCategoryType)
        createFilterDialog(progress_bar)
        createSortDialog()

        intent.printValues(localClassName)

        // push up the progress bar
        app_bar.post {
            val params = progress_bar.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, app_bar.height / 2)
            progress_bar.layoutParams = params
        }
        drawableLeft = ContextCompat.getDrawable(this@ProductListActivity, R.drawable.ic_sort_black_24dp)!!
        drawableRight = ContextCompat.getDrawable(this@ProductListActivity, R.drawable.shape_circle_orange_8dp)!!

        drawableRight.setBounds(100, 100, 100, 100)
        // products list
        reloadPage()
    }

    private fun reloadPage() {
        horizontal_calendar_layout.visibility = View.VISIBLE
        map_btn.visibility = View.GONE
        toolbar_title.text = getString(R.string.parts)
        if (isOnline()) {
            if (isBestSelling)
                bestSellingApi()
            else
                loadProductItems()
        } else {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }
        if (isFavouriteChecked || isOfferChecked || !ratingString.equals("") || (priceRangeFinal != -1 || priceRangeInitial != 0) || filterBrandList.size != 0) {

            this@ProductListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, drawableRight, null)
        } else {
            this@ProductListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, null, null)

        }
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchQuery: String?): Boolean {
                //expanded_search.visibility = if(searchQuery.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (this@ProductListActivity::listAdapter.isInitialized)
                listAdapter.filter.filter(searchQuery)
                return true
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                if (this@ProductListActivity::listAdapter.isInitialized)
                    listAdapter.filter.filter(searchQuery)


                return true
            }
        })
    }

    private fun loadProductItems() {
        val partID = intent.getIntExtra(Constant.Key.partItemID, 0)
        if (current_page == 0) {
            progress_bar.visibility = View.VISIBLE

        } else {
            layoutprogress.visibility = View.VISIBLE
        }
        //

        //    bindRecyclerView(JSONArray())


        val priceRangeString = "$priceRangeInitial,${priceRangeFinal}"
        val priceSortLevel = if (isPriceLowToHigh) 1 else 2

        val selectedCar = getSelectedCar() ?: Models.MyCarDataSet()
        val selectedVehicleVersionID = selectedCar.carVersionModel.idVehicle


        Log.d("ProductOrWorkshopList", "loadProductItems: loading for =  $selectedVehicleVersionID")

        var ratingLevel = ""
        var coupon = ""

        var CategoryType = searchedCategoryType
        if (ratingString != null && ratingString != "") {
            ratingLevel = "2"
        }
        var favorite: String = "0"

        if (isFavouriteChecked) {
            favorite = "1"
            //CategoryType = "0"
        }

        if (isOfferChecked) {
            coupon = "1"
            //CategoryType = "0"
        }

        if (isSearchPreview) {
            searchedCategoryType?.let {

                RetrofitClient.client.getSearchSparePartsBykeywords(keyword = searchedKeyWord, version_id = selectedVehicleVersionID, type = it, coupon = coupon, limit = current_page.toString(), favorite = favorite, user_id = getUserId()
                        , priceRange = if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel = priceSortLevel, brands = filterBrandList.joinToString(","), ratingLevel = ratingLevel, ratingRange = ratingString)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                progress_bar.visibility = View.GONE
                            }

                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                sparepartServerResponces(response)
                            }
                        })
            }
        } else {
            RetrofitClient.client.getSpareParts(selectedVehicleVersionID, partID, if (priceRangeFinal == -1) "" else priceRangeString, priceSortLevel, selectedCar.carSize, brands = filterBrandList.joinToString(",")
                    , categoryType = CategoryType, productKeyword = searchedKeyWord, product_type = "1", user_id = getUserId(), ratingLevel = ratingLevel, ratingRange = ratingString, favorite = favorite, coupon = coupon, model = if (getSelectedCar()?.carModel != null) getSelectedCar()?.carModel?.modelID + "/" + getSelectedCar()?.carModel?.modelYear!! else "", limit = current_page.toString())
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progress_bar.visibility = View.GONE
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            sparepartServerResponces(response)
                        }
                    })
        }


    }

    private fun sparepartServerResponces(response: Response<ResponseBody>) {
        try {
            layoutprogress.visibility = View.GONE
            progress_bar.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
            val body = response.body()?.string()

            body?.let {
                if (isStatusCodeValid(body)) {
                    val dataSet = getDataSetArrayFromResponse(it)
                    bindRecyclerView(dataSet)
                    Log.d("check_list_size",""+brandlist.size)
                    if (brandlist.size == 0) {
                        val brandJSONArray = JSONObject(body).getJSONArray("brands")
                        val gson = GsonBuilder().create()
                        brandlist = gson.fromJson(brandJSONArray.toString(), Array<Models.brand>::class.java).toCollection(java.util.ArrayList<Models.brand>())
                        brandlist.sortBy { it.brandName }
                        bindBrandData()

                    } else {
                    }
                } else {
                    //bindRecyclerView(JSONArray())

                    if (!this::listAdapter.isInitialized || listAdapter.getListSize() == 0) {
                        showInfoDialog(getMessageFromJSON(it)) {
                            if (!isfilterApply) {
                                finish()
                            }
                            logSearchEvent(this@ProductListActivity, "Spare part", "Product search", "1", searchedKeyWord, true)
                        }
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    //best selling products
    private fun bestSellingApi() {
        val priceRangeString = "$priceRangeInitial,${priceRangeFinal}"
        val priceSortLevel = if (isPriceLowToHigh) 1 else 2
        Log.d("IsBestSelling", "yes")
        RetrofitClient.client.bestSeller(if (priceRangeFinal == -1) "" else priceRangeString,
                priceSortLevel, brands = filterBrandList.joinToString(","), version_id = getSelectedCar()?.carVersionModel?.idVehicle!!).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                Log.d("IsBestSelling", "onResponse Best Selling Products: $body")
                if (response.isSuccessful) {
                    body?.let {
                        if (isStatusCodeValid(body)) {

                            val dataSet = getDataSetArrayFromResponse(body)
                            bindRecyclerView(dataSet)
                        } else {
                            //   bindRecyclerView(JSONArray())
                            showInfoDialog(getMessageFromJSON(it)) {
                            }
                        }
                    }
                }
            }
        })
    }

    fun bindRecyclerView(jsonArray: JSONArray) {
        if (calendarPriceMap == null)
            calendarPriceMap = HashMap()
        val gson = GsonBuilder().create()
        val productOrWorkshopList: ArrayList<Models.ProductOrWorkshopList> = gson.fromJson(jsonArray.toString(), Array<Models.ProductOrWorkshopList>::class.java).toCollection(java.util.ArrayList<Models.ProductOrWorkshopList>())
        if (current_page == 0) {
            listAdapter = ProductOrWorkshopListAdapter(productOrWorkshopList, search_view, jsonArray, isCarWash = false, isSOSAppointment = false, isMotService = false, isQuotes = false, isCarMaintenanceServices = false, mIsWorkshop = false, mIsRevision = false, mIsTyre = false, mSelectedFormattedDate = selectedFormattedDate, mView = this, mContext = this, mCalendarPriceMap = calendarPriceMap, mPartIdMap = hashMapOf(), motPartIdMap = hashMapOf(), currentLat = "0.0", currentLong = "0.0", motservicesTime = "")
            linearLayoutManager = LinearLayoutManager(this)
            recycler_view.layoutManager = linearLayoutManager
            recycler_view.adapter = listAdapter
        } else
            listAdapter.addItems(productOrWorkshopList)

        Log.d("currentPageList", current_page.toString() + " : " + productOrWorkshopList.size.toString())

        if (current_page != PAGE_START) listAdapter.removeLoading()
        //check if last page or not
        if (current_page < totalPage) {
            listAdapter.addLoading()
        } else {
            isLastPage = true
            listAdapter.removeLoading()
        }
        isLoading = false

        intent.printValues(localClassName)

        if (intent.getBooleanExtra(Constant.Key.is_assembly_service, false)) {
            listAdapter.setAssembledProduct(assembledProductDetail)
        }
        if (this::linearLayoutManager.isInitialized) {
            recycler_view.addOnScrollListener(object : PaginationListener(linearLayoutManager) {

                override fun loadMoreItems() {
                    isLoading = true
                    current_page += 10

                    reloadPage()
                }

                override fun isLastPage(): Boolean {
                    layoutprogress.visibility = View.GONE
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }
            })
        }

        if (hasRecyclerLoadedOnce)
            return

        val list: MutableList<Float> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val json = JSONObject(jsonArray[i].toString())
            val priceString = json.optString("price")
            list.add(if (priceString.isNullOrBlank() || priceString == "null") 0f else priceString.replace(",", "").trim().toFloat())
            seekbarPriceInitialLimit = if (json.has("min_price") && !json.isNull("min_price") && json.getString("min_price") != "" && json.getString("min_price") != "null")
                json.optString("min_price", "0").toFloat() else 0.0f

            seekbarPriceFinalLimit = if (json.has("max_price") && !json.isNull("max_price") && json.getString("max_price") != "" && json.getString("max_price") != "null")
                json.optString("max_price", "0").toFloat() else 0.0f
        }

        if (jsonArray.length() > 0) {
            hasRecyclerLoadedOnce = true
            filterDialog.dialog_price_range.setValue(0f, 100f)
            filterDialog.clear_selection.callOnClick()
            filterDialog.price_end_range.text = getString(R.string.prepend_euro_symbol_string, seekbarPriceFinalLimit.toString())
        }

        Log.d("ProductOrWorkshop", "bindRecyclerView: price range = $seekbarPriceInitialLimit - $seekbarPriceFinalLimit")
    }

    private fun createFilterDialog(progress_bar: ProgressBar) {
        filterDialog = Dialog(this, R.style.DialogSlideAnimStyle)


        with(filterDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_layout_filter)

            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)

            dialog_price_range.setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                    tempPriceInitial = floor(leftValue).toInt()
                    tempPriceFinal = ceil(rightValue).toInt()

                    val seekPriceFinal = (ceil(rightValue) / 100) * seekbarPriceFinalLimit
                    val seekPriceInitial = (floor(leftValue) / 100) * seekbarPriceFinalLimit

                    val priceFinalString = String.format("%.2f", seekPriceFinal)
                    val priceInitialString = String.format("%.2f", seekPriceInitial)

                    tempPriceInitial = floor(seekPriceInitial).toInt()
                    tempPriceFinal = ceil(seekPriceFinal).toInt()

                    price_start_range.text = getString(R.string.prepend_euro_symbol_string, priceInitialString)
                    price_end_range.text = getString(R.string.prepend_euro_symbol_string, priceFinalString)

                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
            })

            dialog_distance_range.setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                    tempDistanceInitial = leftValue.toInt()
                    tempDistanceFinal = rightValue.toInt() + 1

                    distance_end_range.text = getString(R.string.append_km, tempDistanceFinal)
                    distance_start_range.text = getString(R.string.append_km, tempDistanceInitial)
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

                //    this@ProductListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, null, null)
                if (ratingString.isNotEmpty()) {
                    if (ratingString.toCharArray()[ratingString.lastIndex] == ',')
                        ratingString = ratingString.substring(0, ratingString.lastIndex).trim()
                    //  this@ProductListActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, drawableRight, null)
                }

                priceRangeInitial = tempPriceInitial
                priceRangeFinal = tempPriceFinal

                distanceRangeInitial = tempDistanceInitial
                distanceRangeFinal = tempDistanceFinal

                current_page = PAGE_START//for every filter click we set limit to its initial and clear recycler adapter
                listAdapter.clear()

                isfilterApply = true
                if (!isLoggedIn() && isFavouriteChecked) {
                    showInfoDialog(getString(R.string.Products_not_available_at_the_moment))
                } else {
                    reloadPage()
                }
                dismiss()
                return@setOnMenuItemClickListener true
            }
            toolbar.setNavigationOnClickListener { dismiss() }

            dialog_price_range.setValue(0f, dialog_price_range.maxProgress)
            dialog_distance_range.setValue(0f, dialog_distance_range.maxProgress)

            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar_title.text = getString(R.string.filter)
            setCheckedListener(dialog_layout_rating_five, dialog_rating_five)
            setCheckedListener(dialog_layout_rating_four, dialog_rating_four)
            setCheckedListener(dialog_layout_rating_three, dialog_rating_three)
            setCheckedListener(dialog_layout_rating_two, dialog_rating_two)
            setCheckedListener(dialog_layout_rating_one, dialog_rating_one)
            setCheckedListener(dialog_layout_favourite, dialog_favourite_check_box)
            setCheckedListener(dialog_layout_offers, dialog_offers_check_box)

            dialog_distance_layout.visibility = View.GONE
            clear_selection.setOnClickListener {
                dialog_price_range.setValue(0f, dialog_price_range.maxProgress)
                dialog_distance_range.setValue(0f, dialog_distance_range.maxProgress)
                priceRangeFinal = -1
                priceRangeInitial = 0
                tempPriceInitial = 0
                tempPriceFinal = -1
                //reset rating filter
                dialog_rating_five.isChecked = false
                dialog_rating_four.isChecked = false
                dialog_rating_three.isChecked = false
                dialog_rating_two.isChecked = false
                dialog_rating_one.isChecked = false

                //reset other categories
                dialog_favourite_check_box.isChecked = false
                dialog_offers_check_box.isChecked = false


                filterBrandList.clear()
                bindBrandData(true)
                current_page = PAGE_START//for every filter click we set limit to its initial and clear recycler adapter
                listAdapter.clear()

                isfilterApply = false
if(!isfilterApply){
    reloadPage()
}



            }
            create()
        }
    }

    private fun createSortDialog() {
        sortDialog = Dialog(this@ProductListActivity, R.style.DialogSlideAnimStyle)
        with(sortDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_sorting)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            //Show distance sort option only if workshop
            sort_distance_container.visibility = View.GONE

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { dismiss() }

            toolbar_title.text = "Sort"
            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar.setOnMenuItemClickListener {

                val priceIndex = radio_grp_price.indexOfChild(radio_grp_price.findViewById(radio_grp_price.checkedRadioButtonId))
                val distanceIndex = radio_grp_distance.indexOfChild(radio_grp_distance.findViewById(radio_grp_distance.checkedRadioButtonId))

                isPriceLowToHigh = priceIndex == 0
                isDistanceLowToHigh = distanceIndex == 0
                current_page = PAGE_START//for every filter click we set limit to its initial and clear recycler adapter
                listAdapter.clear()
                isfilterApply = true
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
        recycler_view.adapter = listAdapter
        recycler_view.adapter?.notifyDataSetChanged()
        Log.e("filteredJSONArray", jsonArray.toString())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && requestCode == 111) {
            val query = data?.getStringExtra("query")
            recycler_view.snackbar(getString(R.string.Searchingresultfor) + "\"$query\"")
            search_view.setQuery(query, true)

        } else {
            search_view.setQuery("", true)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun bindBrandData(isFromClearSelection: Boolean = false) {
        val brandFilterAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

        class Holder(view: View) : RecyclerView.ViewHolder(view)

        val myadpter = object : RecyclerView.Adapter<Holder>() {
            var viewBinderHelper = ViewBinderHelper()
            override fun getItemCount(): Int {
                return brandlist.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return Holder(layoutInflater.inflate(R.layout.item_checkbox, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                if (position == 0) {
                    progress_bar.visibility = View.VISIBLE
                } else if (brandlist.size - 1 == position) {
                    progress_bar.visibility = View.GONE
                }
                val brandName = brandlist[position].brandName
                holder.itemView.item_checkbox_text.text = brandName
                holder.itemView.item_checkbox.isChecked = false
                Log.d("brand loaded", brandName)

                setCheckedListener(holder.itemView.item_checkbox_container, holder.itemView.item_checkbox) { isChecked ->

                }



                holder.itemView.item_checkbox.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked) {
                        if (!filterBrandList.contains(brandName)) brandName?.let { it1 -> filterBrandList.add(it1) }
                    } else filterBrandList.remove(brandName)

                    Log.d("ProductOrWorkshopList", "createFilterDialog: brands = $filterBrandList")

                }
                if (!isFromClearSelection) {
                    if (holder.itemView.item_checkbox.isChecked) {
                        if (!filterBrandList.contains(brandName)) {
                            brandName?.let { it1 -> filterBrandList.add(it1) }
                        }
                    } else filterBrandList.remove(brandName)
                }

            }
        }


        filterDialog.dialog_product_checkbox_recycler.adapter = myadpter
    }
}
