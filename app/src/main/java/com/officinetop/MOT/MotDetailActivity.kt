package com.officinetop.MOT

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
import com.google.gson.GsonBuilder
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.GenericAdapter
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.activity_mot_detail.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_sparepart_mot.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MotDetailActivity : BaseActivity() {
    private var mKPartServicesList: ArrayList<Models.Part> = ArrayList()

    private var mOPerationServicesList: ArrayList<Models.Operation> = ArrayList()
    lateinit var motServiceObject: Models.MotServicesList

    lateinit var motServiceDetailObject: Models.ServiceDetail
    private lateinit var itemsData: Models.MotDetail
    var selectitem_position: Int = 0
    var genericAdapter: GenericAdapter<Models.Part>? = null
    private var hashMap: HashMap<String, Models.MotservicesCouponData> = HashMap<String, Models.MotservicesCouponData>()
    var workshopPrices = 0.0
    var sparePartPrices = 0.0
    var main_category_id: Int = 0
    var partListFromCart = ArrayList<Models.Part>()
    var deliveryDate = 0
    var isFromCart = false
    var isProcced = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mot_detail)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.MOTDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progress_bar.visibility = View.GONE
        getLocation()
        if (intent.hasExtra("isFromMotList")) {
            if (intent.hasExtra("motObject")) {
                motServiceObject = Gson().fromJson<Models.MotServicesList>(intent.extras!!.getString("motObject"), Models.MotServicesList::class.java)
                tv_title.text = motServiceObject.serviceName
                tv_description.text = motServiceObject.intervalDescriptionForKms
                main_category_id = motServiceObject.main_category_id
                if (isOnline()) {
                    motDetailService(motServiceObject.id.toString(), motServiceObject.type)
                } else {
                    showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
                }
                saveServicesType(motServiceObject.type.toString())
            }

            button_proceed.setOnClickListener {
                val partId: ArrayList<String> = ArrayList()
                val couponId: ArrayList<String> = ArrayList()
                val sellerId: ArrayList<String> = ArrayList()
                 if(isProcced){
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


                         hashMap[motServiceObject.id.toString()] = Models.MotservicesCouponData(couponId, partId, sellerId)
                         val bundle = Bundle()
                         bundle.putSerializable(Constant.Path.Motpartdata, hashMap as Serializable)
                         Log.e("replacePartID", partId.toString())
                         startActivity(intentFor<WorkshopListActivity>(
                                 Constant.Key.is_motService to true,
                                 Constant.Path.mot_id to motServiceObject.id.toString(),
                                 "mot_type" to motServiceObject.type.toString(),
                                 Constant.Path.deliveryDate to deliveryDate.toString(),
                                 if(this::itemsData.isInitialized){
                                     Constant.Path.motservices_time to  itemsData.data.serviceaveragetime
                                 }else {
                                     Constant.Path.motservices_time to ""
                                 },

                                 Constant.Path.mainCategoryId to motServiceObject.main_category_id.toString()).putExtras(bundle))
                     } else {
                         showInfoDialog(getString(R.string.partNotFound))
                     }
                 }
                else{
                     showInfoDialog(getString(R.string.mot_details_alert))
                 }

            }
        } else if (intent.hasExtra("isFromCart")) {
            isFromCart = true
            if (intent.hasExtra("PartList")) {
                val jsonString = intent.getStringExtra("PartList")
                val gson = GsonBuilder().create()
                partListFromCart = gson.fromJson(jsonString.toString(), Array<Models.Part>::class.java).toCollection(java.util.ArrayList<Models.Part>())
            }
            motServiceDetailObject = intent.extras!!.getSerializable("ServiceDetail") as Models.ServiceDetail
            if (motServiceDetailObject != null) {
                tv_title.text = motServiceDetailObject.serviceName
                tv_description.text = motServiceDetailObject.serviceDescription
                main_category_id = motServiceDetailObject.mainCategoryId?.toInt()!!
                if (isOnline()) {
                    motDetailService(motServiceDetailObject.serviceId, motServiceDetailObject.type.toInt())
                } else {
                    showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
                }
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
                            if(this::itemsData.isInitialized &&itemsData.data.kPartList == null || this::itemsData.isInitialized && itemsData.data.serviceaveragetime == null){
                                isProcced = false
                            }
                            if (body.has("data") && !body.isNull("data")) {

                                itemsData = Gson().fromJson<Models.MotDetail>(body.toString(), Models.MotDetail::class.java)

                                if (this::itemsData.isInitialized &&itemsData.data.serviceaveragetime == null) {
                                    itemsData.data.serviceaveragetime = "0"
                                }
                                getminPriceForMotServicesl(mot_id, type, itemsData.data.serviceaveragetime)
                                if (this::itemsData.isInitialized && itemsData.data.operations != null) {
                                    for (i in 0 until itemsData.data.operations.size) {
                                        mOPerationServicesList.add(itemsData.data.operations[i])
                                    }
                                }
                                if (isFromCart) {
                                    mKPartServicesList.addAll(partListFromCart)
                                } else if (this::itemsData.isInitialized && itemsData.data.kPartList != null) {
                                    for (i in 0 until itemsData.data.kPartList.size) {
                                        if (!(itemsData.data.kPartList[i].couponList == null || itemsData.data.kPartList[i].couponList.size == 0)) {
                                            itemsData.data.kPartList[i].couponTitle = itemsData.data.kPartList[i].couponList[0].couponTitle
                                            itemsData.data.kPartList[i].couponId = itemsData.data.kPartList[i].couponList[0].id
                                        }
                                        mKPartServicesList.add(itemsData.data.kPartList[i])
                                    }
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


    private fun getminPriceForMotServicesl(mot_id: String, type: Int, serviceaveragetime: String) {
        val selectedCar = getSelectedCar() ?: Models.MyCarDataSet()
        val selectedVehicleVersionID = selectedCar.carVersionModel.idVehicle
        RetrofitClient.client.getminPriceForMotServicesl(mot_id, type.toString(), selectedVehicleVersionID, getUserId(), serviceaveragetime, Constant.defaultDistance, SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date()), getLat(), getLong(), mainCategoryId = main_category_id.toString())
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data") && !body.isNull("data")) {
                                val jsondata = JSONObject(body.getString("data"))
                                if (jsondata.has("workshop_price") && !jsondata.getString("workshop_price").isNullOrBlank()) {
                                    workshopPrices = (jsondata.getString("workshop_price")).toDouble().roundTo2Places()
                                } else {
                                    workshopPrices = 0.0
                                }
                                bindPricesinButton()

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
        if (!isFromCart)
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
            /* val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                     width, height / 2)
             ll_sparePart.layoutParams = params*/
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
                if (!isFromCart) {
                    val intent = Intent(this@MotDetailActivity, PartList_Replacement::class.java)
                    intent.putExtra("n3_services_Id", mKPartServicesList[position].n3_service_id)
                    intent.putExtra("version_id", mKPartServicesList[position].version_id)
                    intent.putExtra("Mot_type", if (mKPartServicesList[position].mot_type.isNullOrBlank()) motServiceObject.type.toString() else mKPartServicesList[position].mot_type)
                    startActivityForResult(intent, 100)
                    selectitem_position = position
                }


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

        recycler_view.adapter = genericAdapter
        genericAdapter!!.addItems(mKPartServicesList)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val partdata = data.getStringExtra("data")
            // val partID=data.getStringExtra("ID")
            val partmod = Gson().fromJson<Models.Part>(partdata, Models.Part::class.java)
            /*if (partmod.partimage != null) {
                partmod.product_image_url = partmod.partimage.takeIf { !it.isNullOrEmpty() }!!
            }
            if (partmod.brandImage != null) {
                partmod.brandImageURL = partmod.brandImage.takeIf { !it.isNullOrEmpty() }!!
            }

            partmod.productName = partmod.productName
            partmod.rating_count = partmod.rating_count
            partmod.rating_star = partmod.rating_star
            partmod.wishlist = partmod.wishlist
            if (partmod.couponList != null && partmod.couponList.size != 0) {
                partmod.couponTitle = partmod.couponList[0].couponTitle
                partmod.couponId = partmod.couponList[0].id

            }*/
            mKPartServicesList[selectitem_position] = partmod
            bindPricesinButton()
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
        val window: Window = dialog.window!!
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
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { moveToLoginPage(this) }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite.setImageResource(R.drawable.ic_heart)

                                mKPartServicesList[position].wishlist = "1"
                                showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))
                                logAddToWishListEvent(this, mKPartServicesList[position].productName, ProductId, "1", "USD", if (!mKPartServicesList[position].sellerPrice.isNullOrBlank()) mKPartServicesList[position].sellerPrice.toDouble() else 0.0)


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
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { moveToLoginPage(this) }

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

    private fun bindPricesinButton() {
        if (mKPartServicesList != null && mKPartServicesList.size != 0) {
            sparePartPrices = 0.0
            deliveryDate = 0
            for (partobject in mKPartServicesList) {
                if (!partobject.forPair.isNullOrBlank() && partobject.forPair.equals("1")) {
                    sparePartPrices = sparePartPrices + if (!partobject.sellerPrice.isNullOrBlank()) (2 * (partobject.sellerPrice.toDouble())) else 0.0
                } else {
                    sparePartPrices = sparePartPrices + if (!partobject.sellerPrice.isNullOrBlank()) partobject.sellerPrice.toDouble() else 0.0
                }
                if (!partobject.numberOfDeliveryDays.isNullOrBlank() && !partobject.numberOfDeliveryDays.equals("0")) {

                    if (deliveryDate < partobject.numberOfDeliveryDays.toInt()) {
                        deliveryDate = partobject.numberOfDeliveryDays.toInt()
                    }

                }

            }

        }
        button_proceed.text = getString(R.string.workshopWithSparepart, sparePartPrices.roundTo2Places().toString(), workshopPrices.roundTo2Places().toString())
    }
}
