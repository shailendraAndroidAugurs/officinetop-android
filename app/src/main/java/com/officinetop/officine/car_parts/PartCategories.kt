package com.officinetop.officine.car_parts

import adapter.SubPartCategoryAdapter
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.PartCategoryAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_part_categories.*
import kotlinx.android.synthetic.main.activity_search_preview.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.search_preview_layout.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
class   PartCategories : BaseActivity(), PartCategoryInterface {
    private lateinit var searchLitener: SearchFilterInterface
    private var selectedVehicleVersionID: String = ""
    private var categoryArrayList: JSONArray = JSONArray()
    private var subGroupCategoryArrayList: MutableList<DataSetItem?> = ArrayList()
    private var subN3GroupCategoryArrayList: MutableList<DataSetSubGroupCatItem?> = ArrayList()
    lateinit var partCategoryAdapter: PartCategoryAdapter
    lateinit var subCategoryAdapter: SubPartCategoryAdapter
    private var previousExpandedGroupPosition: Int = 0
    private var SearchProductList: ArrayList<Models.Search_SparePart> = ArrayList()
    private var SearchN3PartList: ArrayList<Models.Search_N3response> = ArrayList()
    private var SearchOENList: ArrayList<Models.Search_OenPart> = ArrayList()
    private var SearchcategoryList: ArrayList<Models.Search_N3response> = ArrayList()
    var layoutheight = 0
    private lateinit var myadpterOEN: RecyclerView.Adapter<Holder>
    private lateinit var myadpterSparePart: RecyclerView.Adapter<Holder>
    private lateinit var myadpterN3Part: RecyclerView.Adapter<Holder>
    private lateinit var myadpterN3Category: RecyclerView.Adapter<Holder>
    private var searchText = ""

    var delay: Long = 500 // 1 seconds after user stops typing

    var last_text_edit: Long = 0
    var handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_part_categories)
        selectedVehicleVersionID = getSelectedCar()?.carVersionModel?.idVehicle ?: ""
        Log.d("PartsCategoryActivity", "onCreate: $selectedVehicleVersionID")
        initViews()
        layoutheight = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18.toFloat(), resources.displayMetrics)).toInt()
        if (isOnline()) {
            loadN1Groups(RetrofitClient.client.sparePartsGroup(selectedVehicleVersionID))
        }else{
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.spare_part)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        partCategoryAdapter = PartCategoryAdapter(categoryArrayList, this)
        category_list.adapter = partCategoryAdapter

        subGroupCategoryArrayList = ArrayList()
        subCategoryAdapter = SubPartCategoryAdapter(subGroupCategoryArrayList, subN3GroupCategoryArrayList, this@PartCategories)

        sub_category_list.setAdapter(subCategoryAdapter)

        sub_category_list.setOnGroupClickListener { parent, v, groupPosition, id ->
            if (sub_category_list.isGroupExpanded(groupPosition))
                sub_category_list.collapseGroup(groupPosition)
            else {
                collapseHeaderGroup()
                previousExpandedGroupPosition = groupPosition
                val subCategoryDetails = subGroupCategoryArrayList.get(groupPosition)
                val subCategoryId = subCategoryDetails?.id
                if (subCategoryId != null)
                    if (isOnline()){
                        loadN3Groups(RetrofitClient.client.spareN3GroupsUpdated(subCategoryId), groupPosition)
                    }else{
                        showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
                    }
            }
            return@setOnGroupClickListener true
        }

        search_product.requestFocus()
        search_product.setOnClickListener {

            if (supportFragmentManager.backStackEntryCount == 0) {
                containerFor_search.visibility = View.VISIBLE
                supportFragmentManager.beginTransaction()
                        .replace(R.id.containerFor_search, SparePartSearchFragment()).addToBackStack("Search")
                        .commit()

            }
        }
        search_product.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (search_product.text.toString().isNotEmpty() && search_product.text.toString().length >= 2) {
                    searchStoreQuery(search_product.text.toString())
                    startActivity(intentFor<ProductListActivity>(Constant.Key.searchedKeyword to search_product.text.toString(),
                            Constant.Key.searchedCategoryType to null))
                }
                return@OnEditorActionListener true
            }
            false
        })

        search_product.addTextChangedListener(textWatcher)
        iv_cross.setOnClickListener {

            containerFor_search.visibility = View.VISIBLE
            layout_searchview.visibility = View.GONE
            progressbar.visibility = View.GONE
            rv_partCategory.visibility = View.VISIBLE
            iv_cross.visibility = View.GONE
            searchText = ""

            search_product.setText("")
        }
    }

    private fun collapseHeaderGroup() {
        try {
            sub_category_list.collapseGroup(previousExpandedGroupPosition)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun loadN1Groups(call: Call<ResponseBody>) {
        progress_bar.visibility = View.VISIBLE

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress_bar.visibility = View.GONE

                val body = response.body()?.string()
                body?.let {

                    if (isStatusCodeValid(body)) {
                        categoryArrayList = JSONArray()
                        categoryArrayList = getDataSetArrayFromResponse(it)

                        partCategoryAdapter = PartCategoryAdapter(categoryArrayList, this@PartCategories)
                        category_list.adapter = partCategoryAdapter
                    } else {
                        showInfoDialog(getMessageFromJSON(it)) {
                        }
                    }
                }
            }
        })
    }

    private fun loadN2Groups(call: Call<SpareSubCategoriesResponse>) {
        progress_bar.visibility = View.VISIBLE

        call.enqueue(object : Callback<SpareSubCategoriesResponse> {
            override fun onFailure(call: Call<SpareSubCategoriesResponse>, t: Throwable) {

            }

            override fun onResponse(call: Call<SpareSubCategoriesResponse>, response: Response<SpareSubCategoriesResponse>) {
                progress_bar.visibility = View.GONE

                subGroupCategoryArrayList.clear()

                if (response.body() != null && response.body()?.statusCode == 1) {
                    val dataSetList: MutableList<DataSetItem?>? = response.body()?.dataSet

                    if (dataSetList != null) {
                        subGroupCategoryArrayList.addAll(dataSetList)
                    }
                } else if (response.body() != null && response.body()?.message != null) {
                    val message = response.body()?.message

                    showInfoDialog(message ?: "Cannot load groups")
                }

                subCategoryAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun loadN3Groups(call: Call<SpareSubGroupCategoryResponse>, expandedGroupPosition: Int) {
        progress_bar.visibility = View.VISIBLE
        call.enqueue(object : Callback<SpareSubGroupCategoryResponse> {
            override fun onFailure(call: Call<SpareSubGroupCategoryResponse>, t: Throwable) {

            }

            override fun onResponse(call: Call<SpareSubGroupCategoryResponse>, response: Response<SpareSubGroupCategoryResponse>) {
                progress_bar.visibility = View.GONE

                if (response.body() != null && response.body()?.statusCode == 1) {
                    val dataSetList: MutableList<DataSetSubGroupCatItem?>? = response.body()?.dataSetSubGroupCat
                    if (dataSetList != null) {
                        subN3GroupCategoryArrayList.clear()
                        subN3GroupCategoryArrayList.addAll(dataSetList)
                        sub_category_list.expandGroup(expandedGroupPosition)
                    }
                } else if (response.body() != null && response.body()?.message != null) {
                    val message = response.body()?.message
                    showInfoDialog(message ?: getString(R.string.Cannotloadsubgroups))
                }
            }
        })
    }

    override fun onCategoryClicked(selectedCategoryID: Int) {
        collapseHeaderGroup()
        // Load N2 category
        if (isOnline()) {
            loadN2Groups(RetrofitClient.client.sparePartsSubGroupUpdated(selectedCategoryID))
        }else{
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }

    }

    override fun onSubCategoryClicked(selectedSubCategoryID: Int) {
        // Load N3 category
    }

    override fun onGroupCategoryClicked(selectedGroupCategoryID: Int) {
        RetrofitClient.client.saveN3id(selectedGroupCategoryID).genericAPICall { _, response ->
            Log.v("SaveN3id", "************* response $response")
        }
        startActivity(intentFor<ProductListActivity>(Constant.Key.partItemID to selectedGroupCategoryID,
                Constant.Key.searchedCategoryType to "3"))
    }


    private fun searchStoreQuery(query: String) {

        Executors.newSingleThreadExecutor().submit {

            Log.v("Save QUERY", "************* $query")
            RetrofitClient.client.addSearchQuery(query, getBearerToken()
                    ?: "").genericAPICall { _, response ->
                Log.v("Save QUERY", "************* response $response")
            }

        }


        if (containerFor_search.isVisible) {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.detach(SparePartSearchFragment()).commit()
            search_product.setText("")
            clearAdpater()
            supportFragmentManager.popBackStackImmediate()
            containerFor_search.visibility = View.GONE
            layout_searchview.visibility = View.GONE
            progressbar.visibility = View.GONE
            rv_partCategory.visibility = View.VISIBLE
        }
    }

    //text watcher of edit text search
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isBlank()) {
                containerFor_search.visibility = View.VISIBLE
                layout_searchview.visibility = View.GONE
                progressbar.visibility = View.GONE
                rv_partCategory.visibility = View.VISIBLE
                iv_cross.visibility = View.GONE
                clearAdpater()
            }
            else {
                if (s?.length!! >= 2) {
                    last_text_edit = System.currentTimeMillis()
                    handler.postDelayed(input_finish_checker, delay)
                } else {
                    clearAdpater()
                    iv_cross.visibility = View.GONE
                }
                containerFor_search.visibility = View.GONE
                layout_searchview.visibility = View.VISIBLE
                rv_partCategory.visibility = View.GONE
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // send search value to fragment for filter search of discovery or history
            // searchLitener.SearchProduct(s.toString())
            //You need to remove this to run only once
            handler.removeCallbacks(input_finish_checker)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            search_product.setText("")
            clearAdpater()
            containerFor_search.visibility = View.GONE
            layout_searchview.visibility = View.GONE
            progressbar.visibility = View.GONE
            rv_partCategory.visibility = View.VISIBLE
            supportFragmentManager.popBackStackImmediate()

        } else {
            finish()
        }

    }

    fun setActivityListener(activityListener: SearchFilterInterface) {
        this.searchLitener = activityListener; }


    private fun getDataforSerachaccordingTokeyword(serachKeyword: String) {
        progressbar.visibility = View.VISIBLE
        RetrofitClient.client.getSearchPartAutocomplete(serachKeyword, getSelectedCar()?.carVersionModel?.idVehicle
                ?: "").enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressbar.visibility = View.GONE

                val body = response.body()?.string()
                body?.let {

                    if (isStatusCodeValid(body)) {
                        val bodyJsonObject = JSONObject(body)
                        if (bodyJsonObject.has("data") && bodyJsonObject.opt("data") != null && !bodyJsonObject.getString("data").isNullOrBlank() && !bodyJsonObject.getString("data").equals("null")) {
                            val SearchData = Gson().fromJson<Models.SparePartSearchData>(bodyJsonObject.getString("data"), Models.SparePartSearchData::class.java)
                            SearchOENList.clear()
                            SearchN3PartList.clear()
                            SearchProductList.clear()
                            SearchcategoryList.clear()
                            if (!SearchData.spareParts.isNullOrEmpty()) SearchProductList.addAll(SearchData.spareParts)
                            if (!SearchData.n3response.isNullOrEmpty()) SearchN3PartList.addAll(SearchData.n3response)
                            if (!SearchData.oeResponse.isNullOrEmpty()) SearchOENList.addAll(SearchData.oeResponse)
                            if (!SearchData.ourn3response.isNullOrEmpty()) SearchcategoryList.addAll(SearchData.ourn3response)
                            OENSerachBindInView()
                            PartSerachBindInView()
                            ProductSerachBindInView()
                            CategorySerachBindInView()
                            Log.d("check_size",""+SearchcategoryList.size)
                          } else {

                        }

                    } else {
                        showInfoDialog(getMessageFromJSON(it)) {
                        }
                    }
                }
            }
        })
    }


    private fun OENSerachBindInView() {


        myadpterOEN = object : RecyclerView.Adapter<Holder>() {

            override fun getItemCount(): Int {
                return SearchOENList.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.search_preview_layout, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                holder.itemView.tv_search_item.text = SearchOENList[position].name
                if (searchText.length > 0) {
                    var index: Int = SearchOENList[position].name.toLowerCase().indexOf(searchText.toLowerCase())

                    while (index >= 0) {
                        val sb = SpannableStringBuilder(SearchOENList[position].name)
                        val fcs = BackgroundColorSpan(resources.getColor(R.color.theme_orange))
                        sb.setSpan(fcs, index, searchText.length + index, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        index = SearchOENList[position].name.indexOf(searchText, index + 1, true)
                        holder.itemView.tv_search_item.text = sb
                    }

                }

                holder.itemView.setOnClickListener {
                    if (isLoggedIn()) {
                        searchStoreQuery(SearchOENList[position].name)
                    }

                    startActivity(intentFor<ProductListActivity>(Constant.Key.is_searchPreview to true, Constant.Key.searchedCategoryType to SearchOENList[position].type, Constant.Key.searchedKeyword to SearchOENList[position].name))

                }
            }
        }
        var heightdata = 0
        if (SearchOENList.size >= 5) {
            for (i in 0 until 5) {
                heightdata = layoutheight * 5
            }
        } else {
            for (i in 0 until SearchOENList.size) {
                heightdata = layoutheight * SearchOENList.size
            }
        }
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightdata.toFloat(), resources.displayMetrics)
        var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())
        llOEn.layoutParams = param
        rv_OENSearch.adapter = myadpterOEN
    }

    private fun PartSerachBindInView() {
        myadpterN3Part = object : RecyclerView.Adapter<Holder>() {
            override fun getItemCount(): Int {
                return SearchN3PartList.size
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.search_preview_layout, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                holder.itemView.tv_search_item.text = SearchN3PartList[position].name
                if (searchText.length > 0) {
                    var index: Int = SearchN3PartList[position].name.toLowerCase().indexOf(searchText.toLowerCase())

                    while (index >= 0) {
                        val sb = SpannableStringBuilder(SearchN3PartList[position].name)
                        val fcs = BackgroundColorSpan(resources.getColor(R.color.theme_orange))
                        sb.setSpan(fcs, index, searchText.length + index, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        index = SearchN3PartList[position].name.indexOf(searchText, index + 1, true)
                        holder.itemView.tv_search_item.text = sb
                    }
                }

                holder.itemView.setOnClickListener {

                    if (isLoggedIn()) {
                        searchStoreQuery(SearchN3PartList[position].name)
                    }
                    startActivity(intentFor<ProductListActivity>(Constant.Key.is_searchPreview to true, Constant.Key.searchedCategoryType to SearchN3PartList[position].type, Constant.Key.searchedKeyword to SearchN3PartList[position].name))

                }
            }
        }
        var heightdata = 0
        if (SearchN3PartList.size >= 5) {
            for (i in 0 until 5) {
                heightdata = layoutheight * 5
            }
        } else {
            for (i in 0 until SearchN3PartList.size) {
                heightdata = layoutheight * SearchN3PartList.size
            }
        }

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightdata.toFloat(), resources.displayMetrics)
        var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())
        ll_part.layoutParams = param
        rv_Partearch.adapter = myadpterN3Part
    }

    private fun CategorySerachBindInView() {
        myadpterN3Category = object : RecyclerView.Adapter<Holder>() {
            override fun getItemCount(): Int {
                return SearchcategoryList.size
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.search_preview_layout, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                holder.itemView.tv_search_item.text = SearchcategoryList[position].name
                if (searchText.length > 0) {
                    var index: Int = SearchcategoryList[position].name.toLowerCase().indexOf(searchText.toLowerCase())

                    while (index >= 0) {
                        val sb = SpannableStringBuilder(SearchcategoryList[position].name)
                        val fcs = BackgroundColorSpan(resources.getColor(R.color.theme_orange))
                        sb.setSpan(fcs, index, searchText.length + index, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        index = SearchcategoryList[position].name.indexOf(searchText, index + 1, true)
                        holder.itemView.tv_search_item.text = sb
                    }
                }

                holder.itemView.setOnClickListener {

                    if (isLoggedIn()) {
                        searchStoreQuery(SearchcategoryList[position].name)
                    }
                    startActivity(intentFor<ProductListActivity>(Constant.Key.is_searchPreview to true, Constant.Key.searchedCategoryType to SearchcategoryList[position].type, Constant.Key.searchedKeyword to SearchcategoryList[position].name))

                }
            }
        }
        var heightdata = 0
        if (SearchcategoryList.size >= 5) {
            for (i in 0 until 5) {
                heightdata = layoutheight * 5
            }
        } else {
            for (i in 0 until SearchcategoryList.size) {
                heightdata = layoutheight * SearchN3PartList.size
            }
        }

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightdata.toFloat(), resources.displayMetrics)
        var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())
        ll_part.layoutParams = param
        rv_CategorySearch.adapter = myadpterN3Category
    }


    private fun ProductSerachBindInView() {


        myadpterSparePart = object : RecyclerView.Adapter<Holder>() {

            override fun getItemCount(): Int {
                return SearchProductList.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.search_preview_layout, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                holder.itemView.tv_search_item.text = SearchProductList[position].name
                if (searchText.length > 0) {
                    var index: Int = SearchProductList[position].name.toLowerCase().indexOf(searchText.toLowerCase())

                    while (index >= 0) {
                        val sb = SpannableStringBuilder(SearchProductList[position].name)
                        val fcs = BackgroundColorSpan(resources.getColor(R.color.theme_orange))
                        sb.setSpan(fcs, index, searchText.length + index, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        index = SearchProductList[position].name.indexOf(searchText, index + 1, true)
                        holder.itemView.tv_search_item.text = sb
                    }

                }


                holder.itemView.setOnClickListener {
                    if (isLoggedIn()) {
                        searchStoreQuery(SearchProductList[position].productsName)
                    }


                    startActivity(intentFor<ProductDetailActivity>(
                            Constant.Path.productDetails to SearchProductList[position].productid).forwardResults())
                }
            }
        }
        var heightdata = 0
        if (SearchProductList.size >= 5) {
            for (i in 0 until 5) {
                heightdata = layoutheight * 5
            }
        } else {
            for (i in 0 until SearchProductList.size) {
                heightdata = layoutheight * SearchProductList.size
            }
        }

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightdata.toFloat(), resources.displayMetrics)
        var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())
        ll_product.layoutParams = param

        rv_ProductSearch.adapter = myadpterSparePart
    }

    fun clearAdpater() {
        SearchOENList.clear()
        SearchN3PartList.clear()
        SearchProductList.clear()
        SearchcategoryList.clear()
        OENSerachBindInView()
        PartSerachBindInView()
        ProductSerachBindInView()
        CategorySerachBindInView()
    }
    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 250) {
            searchText = search_product.text.toString()
            iv_cross.visibility = View.VISIBLE
            getDataforSerachaccordingTokeyword(search_product.text.toString())
        }
    }


    class Holder(view: View) : RecyclerView.ViewHolder(view)
}