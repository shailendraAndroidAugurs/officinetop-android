package com.officinetop.officine.MOT


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.workshop.WorkshopListActivity
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_mot_detail.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_sparepart_mot.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class MotDetailActivity : BaseActivity() {
    private var mKPartServicesList: ArrayList<Models.Part> = ArrayList()
    private var motdeatilsList: ArrayList<Models.Data> = ArrayList()
    private var mOPerationServicesList: ArrayList<Models.Operation> = ArrayList()
    lateinit var motServiceObject: Models.MotServicesList
    private lateinit var itemsData: Models.MotDetail
    var selectitem_position: Int = 0
    var genericAdapter: GenericAdapter<Models.Part>? = null
    private var hashMap: HashMap<String, Models.MotservicesCouponData> = HashMap<String, Models.MotservicesCouponData>()
    lateinit var motdata: Models.MotservicesCouponData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mot_detail)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.MOTDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (intent.hasExtra("motObject")) {
            motServiceObject = Gson().fromJson<Models.MotServicesList>(intent.extras!!.getString("motObject"), Models.MotServicesList::class.java)
            tv_title.text = motServiceObject.serviceName
            tv_description.text = motServiceObject.intervalDescriptionForKms
            motDetailService(motServiceObject.id.toString(), motServiceObject.type)
            saveServicesType(motServiceObject.type.toString())
        }

        button_proceed.setOnClickListener {
            val partId: ArrayList<String> = ArrayList()
            val couponId: ArrayList<String> = ArrayList()
            val sellerId: ArrayList<String> = ArrayList()

            if (mKPartServicesList.size != 0) {

                for (i in 0 until mKPartServicesList.size) {
                    val product_Id = mKPartServicesList[i].id
                    if (!mKPartServicesList[i].couponId.isNullOrBlank()) {
                        couponId.add(mKPartServicesList[i].couponId)
                    } else {
                        couponId.add("")
                    }
                    partId.add(product_Id)
                    sellerId.add(mKPartServicesList[i].usersId)
                }


                hashMap.put(motServiceObject.id.toString(), Models.MotservicesCouponData(couponId, partId, sellerId))


                val bundle = Bundle()
                bundle.putSerializable(Constant.Path.Motpartdata, hashMap as Serializable)
                Log.e("replacePartID", partId.toString())
                startActivity(intentFor<WorkshopListActivity>(
                        Constant.Key.is_motService to true,
                        Constant.Path.mot_id to motServiceObject.id.toString(),
                        "mot_type" to motServiceObject.type.toString(),
                        Constant.Path.motservices_time to itemsData.data.serviceaveragetime).putExtras(bundle))

            } else {
                showInfoDialog(getString(R.string.partNotFound))
            }
        }
    }

    private fun motDetailService(mot_id: String, type: Int) {
        val selectedCar = getSelectedCar() ?: Models.MyCarDataSet()
        val selectedVehicleVersionID = selectedCar.carVersionModel.idVehicle
        progress_bar.visibility = View.VISIBLE
        RetrofitClient.client.getmotserviceDetail(mot_id, type.toString(), selectedVehicleVersionID, getUserId())
                .onCall { _, response ->
                    response?.let {
                        progress_bar.visibility = View.GONE
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data") && !body.isNull("data")) {

                                itemsData = Gson().fromJson<Models.MotDetail>(body.toString(), Models.MotDetail::class.java)

                                if (itemsData?.data?.serviceaveragetime == null) {
                                    itemsData.data?.serviceaveragetime = "0"
                                }

                                if (itemsData?.data?.operations != null) {
                                    for (i in 0 until itemsData?.data?.operations.size) {
                                        mOPerationServicesList.add(itemsData?.data?.operations[i])
                                    }
                                }
                                for (i in 0 until itemsData?.data?.kPartList.size) {
                                    if (!(itemsData.data.kPartList[i].couponList == null || itemsData?.data?.kPartList[i].couponList.size == 0)) {
                                        itemsData.data.kPartList[i].couponTitle = itemsData.data.kPartList[i].couponList[0].couponTitle
                                        itemsData.data.kPartList[i].couponId = itemsData.data.kPartList[i].couponList[0].id
                                    }
                                    mKPartServicesList.add(itemsData?.data?.kPartList[i])
                                }
                                binndDataInRecyclerview()

                            } else {
                                showInfoDialog(getString(R.string.DatanotFound))
                            }
                        } else {
                            showInfoDialog(response.message())
                        }

                    }
                }
    }

    private fun binndDataInRecyclerview() {
        button_proceed.visibility = View.VISIBLE
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        if (mOPerationServicesList.size != 0 && mKPartServicesList.size == 0) {
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    width, height / 2)
            ll_intervalOperation.layoutParams = params
            ll_sparePart.visibility = View.GONE
            ll_intervalOperation.visibility = View.VISIBLE
            bindMotOPerationServices()
        } else if (mKPartServicesList.size != 0 && mOPerationServicesList.size == 0) {
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    width, height / 2)
            ll_sparePart.layoutParams = params
            ll_intervalOperation.visibility = View.GONE
            ll_sparePart.visibility = View.VISIBLE
            bindMotPartNumberServices()
        } else if (mKPartServicesList.size != 0 && mOPerationServicesList.size != 0) {
            ll_sparePart.visibility = View.VISIBLE
            ll_intervalOperation.visibility = View.VISIBLE
            bindMotOPerationServices()
            bindMotPartNumberServices()
        } else {
            ll_sparePart.visibility = View.GONE
            ll_intervalOperation.visibility = View.GONE
        }
    }

    private fun bindMotOPerationServices() {
        val genericAdapter = GenericAdapter<Models.Operation>(this@MotDetailActivity, R.layout.item_mot_operation_detail)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {

            }

            override fun onItemClick(view: View, position: Int) {

            }
        })
        recycler_view_IntervalOperation.adapter = genericAdapter
        genericAdapter.addItems(mOPerationServicesList)
    }

    private fun bindMotPartNumberServices() {
        Log.d("motSparePartList", mKPartServicesList.toString())
        genericAdapter = GenericAdapter<Models.Part>(this@MotDetailActivity, R.layout.item_sparepart_mot)
        genericAdapter!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(this@MotDetailActivity, PartList_Replacement::class.java)
                intent.putExtra("n3_services_Id", mKPartServicesList[position].n3_service_id)
                intent.putExtra("version_id", mKPartServicesList[position].version_id)
                intent.putExtra("Mot_type", if (mKPartServicesList[position].mot_type.isNullOrBlank()) motServiceObject.type.toString() else mKPartServicesList[position].mot_type)
                startActivityForResult(intent, 100)
                selectitem_position = position

            }


            override fun onItemClick(view: View, position: Int) {
                if (view.tag == "102") {
                    if (mKPartServicesList[position].couponList != null) {
                        displayCoupons(mKPartServicesList[position].couponList, tv_CouponTitle_mot, mKPartServicesList[position])
                    }
                } else if (view.tag == "103") {
                    add_remove_product__Wishlist(mKPartServicesList[position].wishlist, view.findViewById(R.id.Iv_favorite_mainPart), mKPartServicesList[position].id.toString(), position)
                }
            }
        })

        recycler_view

                .adapter = genericAdapter
        genericAdapter!!.addItems(mKPartServicesList)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val partdata = data.getStringExtra("data")
            // val partID=data.getStringExtra("ID")
            val partmod = Gson().fromJson<Models.Part>(partdata, Models.Part::class.java)
            if (partmod.partimage != null) {
                partmod.product_image_url = partmod.partimage.takeIf { !it.isNullOrEmpty() }!!
            }
            if (partmod.brandImage != null) {
                partmod.brandImageURL = partmod.brandImage.takeIf { !it.isNullOrEmpty() }!!
            }
            mKPartServicesList[selectitem_position] = partmod
            partmod.productName = partmod.productName
            partmod.rating_count = partmod.rating_count
            partmod.rating_star = partmod.rating_star
            partmod.wishlist = partmod.wishlist
            if (partmod.couponList != null && partmod.couponList.size != 0) {
                partmod.couponTitle = partmod.couponList[0].couponTitle
                partmod.couponId = partmod.couponList[0].id

            }

            Log.e("replaceSelectID", partmod.toString())
            bindMotPartNumberServices()
            recycler_view.layoutManager!!.scrollToPosition(selectitem_position)
        }
    }

    private fun displayCoupons(couponsList: ArrayList<Models.Coupon>, textView: TextView, MotPart: Models.Part) {
        Log.d("couponList", couponsList.size.toString())
        val dialog = Dialog(this)
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog!!.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, 1200)//height should be fixed
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
                    Log.d("couponList", items.couponQuantity.toString())
                    if (!items.offerType.isNullOrBlank()) {

                        if (items.offerType == "2") {
                            holder.couponsAmount.text = getString(R.string.prepend_euro_symbol_string, items.amount.toString())
                        } else {
                            holder.couponsAmount.text = items.amount.toString() + getString(R.string.prepend_percentage_symbol)
                        }
                    }
                    holder.itemView.setOnClickListener {
                        textView.text = items.couponTitle
                        MotPart.couponTitle = items.couponTitle
                        MotPart.couponId = items.id
                        dismiss()
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

    private fun add_remove_product__Wishlist(wish_list: String, Iv_favorite: ImageView, ProductId: String, position: Int) {
        try {
            if (wish_list.isNullOrBlank() || wish_list == "0") {
                RetrofitClient.client.addToFavorite(getBearerToken()
                        ?: "", ProductId, "1", "", getSelectedCar()?.carVersionModel?.idVehicle
                        ?: "").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite.setImageResource(R.drawable.ic_heart)

                                mKPartServicesList[position].wishlist = "1"
                                showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))
                                logAddToWishlistEvent(this,mKPartServicesList[position].productName!!,ProductId,"1","USD",if(!mKPartServicesList[position]?.sellerPrice.isNullOrBlank())mKPartServicesList[position]?.sellerPrice?.toDouble()!! else 0.0)


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
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)

                                mKPartServicesList[position].wishlist = "0"
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
