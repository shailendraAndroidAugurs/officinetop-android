package com.officinetop.Orders

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.HomeActivity
import com.officinetop.R
import com.officinetop.adapter.GenericAdapter
import com.officinetop.data.Models
import com.officinetop.data.getBearerToken
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.onCall
import com.officinetop.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_order__list.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.progress_bar
import kotlinx.android.synthetic.main.layout_recycler_view.recycler_view
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable


class OrderListActivity : BaseActivity() {
    private var fromBooking = false
    private var isListLoading = false
    private val PAGESTART = 0
    private var currentPage = PAGESTART
    private var isLastPageOfList = false
    private var isFirstTimeLoading = true
    private var couponsListItem: MutableList<Models.CartItemList> = ArrayList()
    val genericAdapter = GenericAdapter<Models.CartItemList>(this, R.layout.item_orderlist)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order__list)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.orders)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progress_bar.visibility = View.VISIBLE
        initview(currentPage,false)
        setpagination()

    }

    private fun setpagination() {
        val linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager
        val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = linearLayoutManager.childCount
                val totalItemCount: Int = linearLayoutManager.itemCount
                val firstVisibleItemPosition: Int = linearLayoutManager.findFirstVisibleItemPosition()

                if (!isListLoading && !isLastPageOfList) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= MifareUltralight.PAGE_SIZE) {
                        currentPage += 5
                        isListLoading = true
                        initview(currentPage, true)
                        progress_bar2.visibility = View.VISIBLE
                    }
                }
            }
        }

        recycler_view.addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun initview(start : Int,isPaginationRequest: Boolean) {


        if (intent.hasExtra("fromBooking") && !intent.getStringExtra("fromBooking").isNullOrEmpty())
            fromBooking = true



        RetrofitClient.client.getOrderlist(getBearerToken() ?: "",start)
                .onCall { _, response ->
                    progress_bar.visibility = View.GONE
                    progress_bar2.visibility = View.GONE
                    response?.let {
                        if (response.isSuccessful) {
                            isListLoading = false
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set")) {
                                val dataSetArray = body.getJSONArray("data_set")
                                progress_bar.visibility = View.GONE
//                                if(!isPaginationRequest)
                                bindView(dataSetArray,isPaginationRequest)
                               /* else{
                                  *//*  couponsListItem.clear()*//*
                                    for (i in 0 until dataSetArray!!.length()) {
                                        val modelCartList = Gson().fromJson<Models.CartItemList>(dataSetArray.get(i).toString(), Models.CartItemList::class.java)
                                        couponsListItem.add(modelCartList)
                                    }

                                    genericAdapter!!.addItemAfterScroll(couponsListItem)

                                }*/
                            } else if (body.has("message") && !body.isNull("message")) {
                                if(!isPaginationRequest)
                                image_no_order.visibility = View.VISIBLE
                                isListLoading = false
                                isLastPageOfList = true
                            }

                        }
                    }
                }
    }

    private fun bindView(dataSetArray: JSONArray?, paginationRequest: Boolean) {
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                Log.e("orderClickedItems::", "${couponsListItem[position]}")


            }

            override fun onItemClick(view: View, position: Int) {
   /*             Log.e("ClickedItems::", "${couponsListItem[position]}")
                Log.e("ClickedView::", "${view.tag}")*/

                if (view.tag == "100") {
                    val intent = Intent(this@OrderListActivity, OrderDetailActivity::class.java)
                    intent.putExtra("OrderDetailList", couponsListItem[position].serviceProductDescription as Serializable)
                    intent.putExtra("forwhich", "SP")
                    intent.putExtra("orderid", couponsListItem[position].id)
                    startActivity(intent)
                } else if (view.tag == "101") {
                    if(couponsListItem[position].tyreProductDescription != null){
                        val intent = Intent(this@OrderListActivity, OrderDetailActivity::class.java)
                        intent.putExtra("OrderDetailList", couponsListItem[position].tyreProductDescription  as Serializable)
                        intent.putExtra("orderid", couponsListItem[position].id)
                        intent.putExtra("forwhich", "T")
                    }
                    startActivity(intent)
                } else if (view.tag == "102") {
                    val intent = Intent(this@OrderListActivity, OrderDetailActivity::class.java)
                    intent.putExtra("OrderDetailList", couponsListItem[position].spareProductDescription as Serializable)
                    intent.putExtra("orderid", couponsListItem[position].id)
                    intent.putExtra("forwhich", "S")

                    startActivity(intent)
                } else if (view.tag == "105") {
                    try {
                        if (!couponsListItem[position].orderTracking.trackingUrl.startsWith("http://") && !couponsListItem[position].orderTracking.trackingUrl.startsWith("https://")) {
                            if (couponsListItem[position].orderTracking.sample_tracking_id != null && couponsListItem[position].orderTracking.trackingUrl.contains(couponsListItem[position].orderTracking.sample_tracking_id)) {

                                val trackingurl = "http://" + couponsListItem[position].orderTracking.trackingUrl.replace(couponsListItem[position].orderTracking.sample_tracking_id, couponsListItem[position].orderTracking.trackingId)
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trackingurl))
                                startActivity(browserIntent)
                            }

                        } else {
                            if (couponsListItem[position].orderTracking.sample_tracking_id != null && couponsListItem[position].orderTracking.trackingUrl.contains(couponsListItem[position].orderTracking.sample_tracking_id)) {
                                val trackingurl = couponsListItem[position].orderTracking.trackingUrl.replace(couponsListItem[position].orderTracking.sample_tracking_id, couponsListItem[position].orderTracking.trackingId)
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trackingurl))
                                startActivity(browserIntent)
                            }


                        }
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this@OrderListActivity, getString(R.string.Noapplicationcanhandlethisrequest)
                                + getString(R.string.Pleaseinstallwebbrowser), Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    } catch (e: Exception) {
                        Toast.makeText(this@OrderListActivity, getString(R.string.retry), Toast.LENGTH_LONG).show()
                    }

                } else if (view.tag == "200") {
                    if (couponsListItem[position].orderTracking != null && !couponsListItem[position].orderTracking.id.isNullOrBlank()) {

                        DownloadInvoice(couponsListItem[position].orderTracking.id, true)
                    } else {
                        Toast.makeText(this@OrderListActivity, getString(R.string.TrackingIdnotFound), Toast.LENGTH_SHORT).show()
                    }
                } else if (view.tag == "201") {
                    if (couponsListItem[position].orderTracking != null && !couponsListItem[position].orderTracking.id.isNullOrBlank()) {
                        DownloadInvoice(couponsListItem[position].orderTracking.id, false)

                    } else {
                        Toast.makeText(this@OrderListActivity, getString(R.string.TrackingIdnotFound), Toast.LENGTH_SHORT).show()
                    }
                } else if (view.tag == "203") {
                    if (!couponsListItem[position].id.isNullOrBlank()) {
                        Returnpolicy(couponsListItem[position].id)
                    } else {
                        Toast.makeText(this@OrderListActivity, getString(R.string.TrackingIdnotFound), Toast.LENGTH_SHORT).show()
                    }
                } else if (view.tag == "500") {
                    RequestInvoicesPolicy(couponsListItem[position].id)

                }
            }
        })

        for (i in 0 until dataSetArray!!.length()) {
            val modelCartList = Gson().fromJson<Models.CartItemList>(dataSetArray.get(i).toString(), Models.CartItemList::class.java)
            couponsListItem.add(modelCartList)
        }
         if(!paginationRequest)
        recycler_view.adapter = genericAdapter
        genericAdapter.addItems(couponsListItem)
    }

    fun DownloadInvoice(orderId: String, isDownload: Boolean) {
        //progress_bar.visibility = View.VISIBLE
        RetrofitClient.client.downloadInvoice(orderId, getBearerToken() ?: "")
                .onCall { _, response ->
                    // progress_bar.visibility = View.GONE
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())

                            if (body.has("status_code") && !body.getString("status_code").isNullOrBlank() && body.getString("status_code").contains("1"))
                                if (isDownload)
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(body.getString("message"))))
                                else {
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    intent.putExtra(Intent.EXTRA_TEXT, "Download Invoice From : " + body.getString("message"))
                                    intent.type = "text/plain"
                                    startActivity(Intent.createChooser(intent, "Officine"))
                                }
                        }
                    }
                }

    }

    fun Returnpolicy(orderId: String) {
        RetrofitClient.client.returnOrder(orderId, getBearerToken() ?: "")
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            Log.e("RETURN", body.toString())
                            if (body.has("status_code") && !body.getString("status_code").isNullOrBlank() && body.getString("status_code") == "1") {
                                showInfoDialog(body.get("message").toString())
                                //couponsListItem[potion].returnRequest="C"
                                initview(0,false)
                            } else {
                                showInfoDialog(body.get("message").toString())
                            }

                        }
                    }
                }
    }

    fun RequestInvoicesPolicy(orderId: String) {
        RetrofitClient.client.customerInvoicePolicy(orderId, getBearerToken() ?: "")
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            Log.e("RETURN", body.toString())
                            if (body.has("status_code") && !body.getString("status_code").isNullOrBlank() && body.getString("status_code") == "1") {
                                showInfoDialog(body.get("message").toString())
                                initview(0,false)
                            } else {
                                showInfoDialog(body.get("message").toString())
                            }

                        }
                    }
                }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (fromBooking) {
            startActivity(intentFor<HomeActivity>().putExtra("login_success", true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        } else {
            super.onBackPressed()
        }

    }










}