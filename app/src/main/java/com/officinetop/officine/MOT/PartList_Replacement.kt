package com.officinetop.officine.MOT

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.adapter.PaginationListener
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.data.getUserId
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import org.json.JSONArray
import org.json.JSONObject

class PartList_Replacement : BaseActivity() {
    private var carMaintenanceServiceList: MutableList<Models.Part> = ArrayList()
    private lateinit var partID: String
    private lateinit var versionId: String
    private lateinit var n3_services_id: String
    private lateinit var mottype: String
    private val PAGE_START = 0
    private var current_page = PAGE_START
    private var isLastPage = false
    private var totalPage = 500
    private var isLoading = false
    private var genericAdapter: GenericAdapter<Models.Part>? = null
    lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_part_list__replacement)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.part_list)
        partID = intent?.getStringExtra("ID") ?: ""
        n3_services_id = intent?.getStringExtra("n3_services_Id") ?: ""
        versionId = intent?.getStringExtra("version_id") ?: ""
        mottype = intent?.getStringExtra("Mot_type") ?: ""
        Log.d("mottype", mottype)
        N3partList()
    }

    private fun N3partList() {
        val progressDialog = getProgressDialog()
        progressDialog.show()
        RetrofitClient.client.partListForMotReplacement(n3_services_id, versionId, mottype, getUserId(), current_page.toString()).onCall { networkException, response ->
            networkException?.let {
                progressDialog.dismiss()
            }
            response.let {

                if (response!!.isSuccessful) {
                    val body = JSONObject(response.body()?.string())
                    if (body.has("data_set") && body.get("data_set") != null && body.get("data_set") is JSONArray) {
                        progressDialog.dismiss()
                        for (i in 0 until body.getJSONArray("data_set").length()) {

                            val servicesObj = body.getJSONArray("data_set").get(i) as JSONObject

                            val carMaintenance = Gson().fromJson<Models.Part>(servicesObj.toString(), Models.Part::class.java)
                            if (carMaintenance.images != null && carMaintenance.images.size != 0) {
                                carMaintenance.partimage = carMaintenance.images[0].imageUrl.takeIf { !it.isNullOrEmpty() }!!
                            }

                            carMaintenanceServiceList.add(carMaintenance)
                        }

                        if (current_page == 0) {
                            setAdapter()
                        } else {
                            genericAdapter?.addItems(carMaintenanceServiceList)
                            genericAdapter?.notifyDataSetChanged()
                            isLoading = false

                        }

                    } else {
                        progressDialog.dismiss()
                        if (carMaintenanceServiceList.size == 0) {
                            showInfoDialog(body.getString("message"))
                        } else {
                        }


                    }

                } else {
                    progressDialog.dismiss()
                    if (carMaintenanceServiceList.size != 0) {
                        showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again))
                    } else {

                    }

                }

            }
        }
    }

    private fun setAdapter() {
        genericAdapter = GenericAdapter<Models.Part>(this, R.layout.maintenance_part_replacement)
        genericAdapter!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val output = Intent()
                output.putExtra("data", Gson().toJson(carMaintenanceServiceList[position]).toString())
                output.putExtra("ID", position.toString())
                setResult(Activity.RESULT_OK, output)
                finish()
            }

            override fun onItemClick(view: View, position: Int) {
                if (view.tag == "103") {
                    Log.d("partist_Replacement", "listPosition$position")
                    add_remove_product__Wishlist(carMaintenanceServiceList[position].wishlist, view.findViewById(R.id.Iv_favorite_mot_part), carMaintenanceServiceList[position].id, position)
                } else {
                    if (carMaintenanceServiceList[position].couponList != null) {
                        displayCoupons(carMaintenanceServiceList[position].couponList)
                    }
                }
            }
        })


        linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.adapter = genericAdapter


        /*  if (current_page == 0) {
              recycler_view.smoothScrollToPosition(0)
          } else {
              recycler_view.smoothScrollToPosition(carMaintenanceServiceList.size-1)
          }*/
        genericAdapter?.addItems(carMaintenanceServiceList)


        if (this::linearLayoutManager.isInitialized) {
            recycler_view.addOnScrollListener(object : PaginationListener(linearLayoutManager) {

                override fun loadMoreItems() {
                    isLoading = true
                    current_page += 10

                    N3partList()
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

    private fun displayCoupons(couponsList: List<Models.Coupon>) {
        val dialog = Dialog(this)
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, 1200)//height shoud be fixed
        val title = dialog.findViewById(R.id.title) as TextView

        title.text = getString(R.string.coupon_list)
        with(dialog) {
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
                    holder.couponsAmount.text = getString(R.string.prepend_euro_symbol_string, items.amount.toString())
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

    private fun add_remove_product__Wishlist(wish_list: String, Iv_favorite: ImageView, ProductId: String, position: Int) {
        try {
            if (wish_list.isNullOrBlank() || wish_list == "0") {

                Log.d("perameterAddtoWishlist", "Productid: $ProductId ")
                RetrofitClient.client.addToFavorite(getBearerToken()
                        ?: "", ProductId, "1", "", getSelectedCar()?.carVersionModel?.idVehicle
                        ?: "").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage(this) }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite.setImageResource(R.drawable.ic_heart)


                                carMaintenanceServiceList[position].wishlist = "1"

                                showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))
                                logAddToWishlistEvent(this, carMaintenanceServiceList[position].productName, ProductId, "1", "USD", if (!carMaintenanceServiceList[position].sellerPrice.isNullOrBlank()) carMaintenanceServiceList[position].sellerPrice.toDouble() else 0.0)


                            }

                        }

                    }
                }

            } else {

                RetrofitClient.client.removeFromFavorite(getBearerToken()
                        ?: "", ProductId, "", "1").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage(this) }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                                carMaintenanceServiceList[position].wishlist = "0"
                                showInfoDialog(getString(R.string.productRemoved_formWishList))

                            }

                        }

                    }
                }
            }


        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}
