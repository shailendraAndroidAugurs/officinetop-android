package com.officinetop.officine.car_parts

import adapter.SubPartCategoryAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.PartCategoryAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_part_categories.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.serialization.json.jsonArray
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Array
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
    var stringlist: ArrayList<String> = ArrayList<String>()
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
        search_product.requestFocus()
        search_product.setOnClickListener {

            if (supportFragmentManager.backStackEntryCount == 0) {
                containerFor_search.visibility = View.VISIBLE
                supportFragmentManager.beginTransaction()
                        .replace(R.id.containerFor_search, SparePartSearchFragment()).addToBackStack("Search")
                        .commit()
            } else {
                if (search_product.text.toString().isNotEmpty() && search_product.text.toString().length > 3)
                    searchStoreQuery(search_product.text.toString())
                /*else
                    showInfoDialog(getString(R.string.Enterkeywordwithminimumfourcharacters))*/

            }
        }

        search_product.setThreshold(2);
        search_product.addTextChangedListener(textWatcher)
        search_product.setOnItemClickListener(onItemClickListener)
        /* search_btn.setOnClickListener {
             if (search_product.text.toString().isNotEmpty() && search_product.text.toString().length > 3)
                 searchStoreQuery(search_product.text.toString())
             else
                 showInfoDialog(getString(R.string.Enterkeywordwithminimumfourcharacters))
         }*/


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
            val ft: FragmentTransaction = supportFragmentManager!!.beginTransaction()
            ft.detach(SparePartSearchFragment()).commit()
            search_product.setText("")
            getSupportFragmentManager().popBackStackImmediate();
            containerFor_search.visibility = View.GONE
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchPart(s.toString())
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            search_product.setText("")
            containerFor_search.visibility = View.GONE
            getSupportFragmentManager().popBackStackImmediate()

        } else {
            finish()
        }

    }

    public fun setActivityListener(activityListener: SearchFilterInterface) {
        this.searchLitener = activityListener; }


    private fun searchPart(searchProductString: String) {


        /*  var progLanguages = arrayOf("Java", "aa", "aaa", "aaaa", "aaaaa", "adfgdfgdg", "aaassssss", "C", "C++", ".Net", "PHP", "Perl", "Objective-c", "Small-Talk", "C#", "Ruby", "ASP", "ASP .NET")

          var arrayAdapter = ArrayAdapter(this@PartCategories, android.R.layout.select_dialog_item, progLanguages);
          //Used to specify minimum number of
          //characters the user has to type in order to display the drop down hint.
          search_product.setThreshold(1);
          //Setting adapter
          search_product.setAdapter(arrayAdapter);*/

        RetrofitClient.client.getSearchPartAutocomplete(searchProductString, selectedVehicleVersionID).genericAPICall { networkException, response ->

            val body = response?.body()?.string()
            if (!body.isNullOrBlank()) {

                val jsonbody = JSONObject(body)

                if (jsonbody.has("data_set") && !jsonbody.getString("data_set").isNullOrBlank() && !jsonbody.getString("data_set").equals("null")) {

                    val gson = GsonBuilder().create()
                   // stringlist.clear()
                    stringlist = gson.fromJson(jsonbody.getString("data_set").toString(), kotlin.Array<String>::class.java).toCollection(java.util.ArrayList<String>())

                    runOnUiThread(Runnable() {
                        run() {
                            val adaptorAutoComplete = ArrayAdapter<String>(this@PartCategories, android.R.layout.simple_dropdown_item_1line, stringlist);

                            search_product.setAdapter(adaptorAutoComplete);
                            adaptorAutoComplete.notifyDataSetChanged();
                        }
                    });
                }
            }


        }
    }

    private val onItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            searchStoreQuery(stringlist[p2].split(" ")[0])
        }

    }
}