package com.officinetop.officine.misc_activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.getProgressDialog
import com.officinetop.officine.utils.onCall
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import org.json.JSONArray
import org.json.JSONObject

class PartList_Replacement : BaseActivity() {

    private var carMaintenanceServiceList: MutableList<Models.Part> = ArrayList()
    lateinit var partID: String
    lateinit var versionId: String
    lateinit var n3_services_id: String
    lateinit var mottype: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_part_list__replacement)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.partlist)
        partID = intent?.getStringExtra("ID") ?: ""
        n3_services_id = intent?.getStringExtra("n3_services_Id") ?: ""
        versionId = intent?.getStringExtra("version_id") ?: ""
        mottype = intent?.getStringExtra("Mot_type") ?: ""
        N3partList(partID)
    }

    private fun N3partList(partID: String) {
        // PartList(partID)
       val progressDialog= getProgressDialog()
        progressDialog.show()
        RetrofitClient.client.PartListForMotReplacement(n3_services_id,versionId,mottype).onCall { networkException, response ->
            networkException?.let {
                progressDialog.dismiss()
            }
            response.let {

                    if (response!!.isSuccessful) {
                        val body = JSONObject(response?.body()?.string())
                        if (body.has("data_set") && body.get("data_set") != null && body.get("data_set") is JSONArray) {
                            progressDialog.dismiss()
                            for (i in 0 until body.getJSONArray("data_set").length()) {

                                val servicesObj = body.getJSONArray("data_set").get(i) as JSONObject

                                val carMaintenance = Gson().fromJson<Models.Part>(servicesObj.toString(), Models.Part::class.java)
                                if (carMaintenance.images != null && carMaintenance.images.size!=0 ) {
                                    carMaintenance.partimage = carMaintenance.images[0].imageUrl.takeIf { !it.isNullOrEmpty() }!!
                                }
                                carMaintenanceServiceList.add(carMaintenance)
                            }
                            setAdapter()
                        }

                }
            }
        }
    }

    private fun setAdapter() {
        val genericAdapter = GenericAdapter<Models.Part>(this, R.layout.maintenance_part_replacement)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val output = Intent()
                output.putExtra("data", Gson().toJson(carMaintenanceServiceList[position]).toString())
                output.putExtra("ID", position.toString())
                setResult(Activity.RESULT_OK, output)
                finish()
            }

            override fun onItemClick(view: View, position: Int) {
                Log.e("itemsObjetData::", "${carMaintenanceServiceList[position]}")
                if (carMaintenanceServiceList[position].couponList != null) {
                    displayCoupons(carMaintenanceServiceList[position].couponList, "workshop_coupon")
                }
            }
        })
        recycler_view.adapter = genericAdapter
        //  recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.getContext(), DividerItemDecoration.VERTICAL))
        genericAdapter.addItems(carMaintenanceServiceList)
    }

    private fun displayCoupons(couponsList: List<Models.Coupon>, couponType: String) {
        /*  val couponsList: MutableList<Models.Coupon> = ArrayList()
          couponsList.add(couponsArray)*/
        val dialog = Dialog(this)
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog!!.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, 1200)//height shoud be fixed
        val title = dialog.findViewById(R.id.title) as TextView
        val ll_coupons = dialog.findViewById(R.id.ll_coupons) as LinearLayout
        val apply_coupons = dialog.findViewById(R.id.apply_coupons) as Button
        val cancel_coupons = dialog.findViewById(R.id.cancel_coupons) as Button
        ll_coupons.visibility = View.VISIBLE
        apply_coupons.visibility = View.GONE
        title.text = "Coupons List"
        with(dialog) {
            var selectedPosition: Int = -1

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
            cancel_coupons.setOnClickListener {
                dialog.dismiss()
            }
            apply_coupons.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

}
