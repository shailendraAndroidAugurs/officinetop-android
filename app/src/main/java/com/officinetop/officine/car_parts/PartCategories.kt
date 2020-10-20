package com.officinetop.officine.car_parts

import adapter.SubPartCategoryAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView.OnEditorActionListener
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.PartCategoryAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.forwardResults
import com.officinetop.officine.utils.genericAPICall
import com.officinetop.officine.utils.showInfoDialog
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


class PartCategories : BaseActivity(), PartCategoryInterface {
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

    var layoutheight = 0
    private lateinit var myadpterOEN: RecyclerView.Adapter<Holder>
    private lateinit var myadpterSparePart: RecyclerView.Adapter<Holder>
    private lateinit var myadpterN3Part: RecyclerView.Adapter<Holder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_part_categories)

        selectedVehicleVersionID = getSelectedCar()?.carVersionModel?.idVehicle ?: ""
        Log.d("PartsCategoryActivity", "onCreate: $selectedVehicleVersionID")
        initViews()
        layoutheight = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18.toFloat(), resources.displayMetrics)).toInt()
        loadN1Groups(RetrofitClient.client.sparePartsGroup(selectedVehicleVersionID))


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
                    loadN3Groups(RetrofitClient.client.spareN3GroupsUpdated(subCategoryId), groupPosition)
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
                if (search_product.text.toString().isNotEmpty() && search_product.text.toString().length >= 2)
                    searchStoreQuery(search_product.text.toString())
                return@OnEditorActionListener true
            }
            false
        })

        search_product.addTextChangedListener(textWatcher)

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
        loadN2Groups(RetrofitClient.client.sparePartsSubGroupUpdated(selectedCategoryID))
    }

    override fun onSubCategoryClicked(selectedSubCategoryID: Int) {
        // Load N3 category
    }

    override fun onGroupCategoryClicked(selectedGroupCategoryID: Int) {
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

        startActivity(intentFor<ProductListActivity>(Constant.Key.searchedKeyword to query,
                Constant.Key.searchedCategoryType to null))
        if (containerFor_search.isVisible) {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.detach(SparePartSearchFragment()).commit()
            search_product.setText("")
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
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // send search value to fragment for filter search of discovery or history
            // searchLitener.SearchProduct(s.toString())


            if (s.toString().isBlank()) {
                containerFor_search.visibility = View.VISIBLE
                layout_searchview.visibility = View.GONE
                progressbar.visibility = View.GONE
                rv_partCategory.visibility = View.VISIBLE
            } else {
                getDataforSerachaccordingTokeyword(s.toString())
                containerFor_search.visibility = View.GONE
                layout_searchview.visibility = View.VISIBLE
                rv_partCategory.visibility = View.GONE


            }

        }


    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            search_product.setText("")
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
                            /*  var searchproductlistlocal = ArrayList<Models.Search_SparePart>()
                              var searchOENlistlocal = ArrayList<Models.Search_OenPart>()
                              var searchN3Responceslistlocal = ArrayList<Models.Search_N3response>()

                              if (!SearchData.spareParts.isNullOrEmpty()) {
                                  searchproductlistlocal.addAll(SearchData.spareParts)
                              }
                              if (!SearchData.oeResponse.isNullOrEmpty()) {
                                  searchOENlistlocal.addAll(SearchData.oeResponse)
                              }
                              if (!SearchData.n3response.isNullOrEmpty()) {
                                  searchN3Responceslistlocal.addAll(SearchData.n3response)
                              }



                              if (searchproductlistlocal.size > 0) {
                                  SearchProductList = searchproductlistlocal.filter { it.name.contains(serachKeyword) || it.name.matches(serachKeyword.toRegex()) } as ArrayList

                              }
                              if (searchN3Responceslistlocal.size > 0) {
                                  SearchN3PartList = searchN3Responceslistlocal.filter { it.name.contains(serachKeyword) || it.name.matches(serachKeyword.toRegex()) } as ArrayList

                              }
                              if (searchOENlistlocal.size > 0) {
                                  SearchOENList = searchOENlistlocal.filter { it.name.contains(serachKeyword) || it.name.matches(serachKeyword.toRegex()) } as ArrayList

                              }*/
                            SearchProductList.clear()
                            SearchN3PartList.clear()
                            SearchOENList.clear()
                            if (!SearchData.spareParts.isNullOrEmpty()) SearchProductList.addAll(SearchData.spareParts)
                            if (!SearchData.n3response.isNullOrEmpty()) SearchN3PartList.addAll(SearchData.n3response)
                            if (!SearchData.oeResponse.isNullOrEmpty()) SearchOENList.addAll(SearchData.oeResponse)
                            OENSerachBindInView()
                            PartSerachBindInView()
                            ProductSerachBindInView()
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

                holder.itemView.setOnClickListener {
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


    class Holder(view: View) : RecyclerView.ViewHolder(view)
}