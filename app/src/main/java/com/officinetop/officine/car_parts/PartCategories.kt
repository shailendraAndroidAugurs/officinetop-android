package com.officinetop.officine.car_parts

import adapter.SubPartCategoryAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import com.cunoraz.tagview.Tag
import com.cunoraz.tagview.TagView
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.PartCategoryAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.genericAPICall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_part_categories.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors


class PartCategories : BaseActivity(), PartCategoryInterface {

    private var selectedVehicleVersionID: String = ""
    private var categoryArrayList: JSONArray = JSONArray()

    private var subGroupCategoryArrayList: MutableList<DataSetItem?> = ArrayList()
    private var subN3GroupCategoryArrayList: MutableList<DataSetSubGroupCatItem?> = ArrayList()

    lateinit var partCategoryAdapter: PartCategoryAdapter
    lateinit var subCategoryAdapter: SubPartCategoryAdapter
    private var previousExpandedGroupPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_part_categories)

        selectedVehicleVersionID = getSelectedCar()?.carVersionModel?.idVehicle ?: ""
        Log.d("PartsCategoryActivity", "onCreate: $selectedVehicleVersionID")
        initViews()

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

        search_product.setOnClickListener {
            openSearchDialog()
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

                    // showInfoDialog(message?:"Cannot load groups")
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

    private fun openSearchDialog() {
        val layoutInflater = baseContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = layoutInflater.inflate(R.layout.activity_search_view, null)
        val popupWindow = PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        // popupWindow.showAtLocation(this.findViewById(R.id.search_view_options), Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        val allSearchSection = popupView.findViewById<TagView>(R.id.all_search_section)
        val recentSearchSection = popupView.findViewById<TagView>(R.id.recent_search_section)
        val searchField = popupView.findViewById<EditText>(R.id.search_field)

        popupView.findViewById<TextView>(R.id.search_all_categories).setOnClickListener {
            popupWindow.dismiss()
        }

        allSearchSection.removeAll()
        recentSearchSection.removeAll()

        getBearerToken()?.let { token ->

            RetrofitClient.client.getSearchKeyWords(token).genericAPICall { _, response ->
                val modifiedResult = response?.body() as SearchKeywordResponse

                modifiedResult.data?.all?.forEach {
                    it?.keyword?.let { allSearchSection.addTag(Tag(it)) }
                }

                modifiedResult.data?.self?.forEach {
                    it?.keyword?.let { recentSearchSection.addTag(Tag(it)) }
                }

                allSearchSection.setOnTagClickListener { tag, _ -> searchStoreQuery(tag.text, popupWindow) }
                recentSearchSection.setOnTagClickListener { tag, _ -> searchStoreQuery(tag.text, popupWindow) }
            }
        }

        search_btn.setOnClickListener {
            if (searchField.text.toString().isNotEmpty() && searchField.text.toString().length > 3)
                searchStoreQuery(searchField.text.toString(), popupWindow)
            else
                showInfoDialog(getString(R.string.Enterkeywordwithminimumfourcharacters))
        }

        popupView.findViewById<TextView>(R.id.clear_searches).setOnClickListener {
            getBearerToken()?.let { token ->

                RetrofitClient.client.clearSearchKeyWords(token).genericAPICall { _, response ->
                    if (isStatusCodeValid(response?.body()?.string())) {
                        showInfoDialog(getString(R.string.Keywordcleared))
                        popupWindow.dismiss()
//                        initTagView()
                    } else
                        showInfoDialog(getString(R.string.Cannotclearsearchkeywords))
                }
            }
        }

        popupWindow.showAsDropDown(toolbar, 0, 0)
    }

    private fun searchStoreQuery(query: String, popupWindow: PopupWindow?) {

        Executors.newSingleThreadExecutor().submit {

            Log.v("Save QUERY", "************* $query")
            RetrofitClient.client.addSearchQuery(query, getBearerToken()
                    ?: "").genericAPICall { _, response ->
                Log.v("Save QUERY", "************* response $response")
            }

        }
        startActivity(intentFor<ProductListActivity>(Constant.Key.searchedKeyword to query,
                Constant.Key.searchedCategoryType to null))
        popupWindow?.dismiss()
    }
}