package com.officinetop.officine.misc_activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.workshop.WorkshopListActivity
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.data.getUserId
import com.officinetop.officine.databinding.ActivityMaintenanceBinding
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_maintenance.*
import kotlinx.android.synthetic.main.car_maintenance_dialog_layout_filter.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.dialog_sorting.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_maintenance_selection.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.view.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor

class MaintenanceActivity : BaseActivity() {

    private var carMaintenanceServiceList: ArrayList<Models.CarMaintenanceServices> = ArrayList()
    private val selectedCarMaintenanceServices: ArrayList<Models.CarMaintenanceServices> = ArrayList()
    private lateinit var filterDialog: Dialog
    private lateinit var sortDialog: Dialog
    private var ratingString = ""
    private var tempPriceFinal: Float = -1f
    private var tempPriceInitial: Float = 0f
    private var priceRangeInitial: Float = 0f
    private var priceRangeFinal: Float = -1f
    private var seekbarPriceFinalLimit = 1000f
    private var isPriceLowToHigh = true
    private var frontRear: String = ""
    private var leftRight: String = ""
    private var maxPrice = 0f

    private var selectedServicesTotalPrice: Double = 0.0
    private var genericAdapterParts: GenericAdapter<Models.Part>? = null
    var genericAdapter: GenericAdapter<Models.CarMaintenanceServices>? = null
    private var selectitem_position: Int = 0
    var selectservice_position: Int = 0
    var dialog: Dialog? = null
    private var hashMap: HashMap<String, Models.servicesCouponData> = HashMap<String, Models.servicesCouponData>()
    lateinit var checkBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMaintenanceBinding = DataBindingUtil.setContentView(this, R.layout.activity_maintenance)
        binding.root
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.maintenance)
        getCarMaintenance()
        createFilterDialog()
        createSortDialog()

        filter_btn.setOnClickListener {
            filterDialog.show()
        }

        sort_btn.setOnClickListener {
            sortDialog.show()
        }
        selectedCarMaintenanceServices.clear()

        btn_choose_workshop.setOnClickListener {


            if (selectedCarMaintenanceServices.size > 0) {

                val selectedServices_partList = ArrayList<Models.servicesCouponData>()

                val serviceId: MutableList<String> = ArrayList()

                for (j in 0 until selectedCarMaintenanceServices.size) {
                    serviceId.add(selectedCarMaintenanceServices.get(j).id.toString())
                    for (i in 0 until carMaintenanceServiceList.size) {
                        if (selectedCarMaintenanceServices.get(j).id == carMaintenanceServiceList[i].id) {
                            Log.e("PARTIDSSERVICES", if (carMaintenanceServiceList[i].productId.isNullOrBlank()) "custom part" else carMaintenanceServiceList[i].productId)
                            if (!carMaintenanceServiceList[i].CouponId.isNullOrBlank()) {

                                        hashMap.put(carMaintenanceServiceList[i].id, Models.servicesCouponData(carMaintenanceServiceList[i].CouponId, if (carMaintenanceServiceList[i].productId.isNullOrBlank()) "" else carMaintenanceServiceList[i].productId, carMaintenanceServiceList[i].usersId,
                                                if (!carMaintenanceServiceList[i].productId.isNullOrBlank() && (carMaintenanceServiceList[i].forPair.isNullOrBlank() || carMaintenanceServiceList[i].forPair.equals("0"))) "1"
                                                else if (!carMaintenanceServiceList[i].productId.isNullOrBlank() && (carMaintenanceServiceList[i].forPair.equals("1"))) "2" else "0", carMaintenanceServiceList[i].id))
                                                selectedServices_partList.add(hashMap.get(carMaintenanceServiceList[i].id)!!)

                            } else {
                                hashMap.put(carMaintenanceServiceList[i].id, Models.servicesCouponData("", if (carMaintenanceServiceList[i].productId.isNullOrBlank()) "" else carMaintenanceServiceList[i].productId, carMaintenanceServiceList[i].usersId,
                                        if (!carMaintenanceServiceList[i].productId.isNullOrBlank() && (carMaintenanceServiceList[i].forPair.isNullOrBlank() || carMaintenanceServiceList[i].forPair.equals("0"))) "1"
                                        else if (!carMaintenanceServiceList[i].productId.isNullOrBlank() && (carMaintenanceServiceList[i].forPair.equals("1"))) "2" else "0", carMaintenanceServiceList[i].id))
                                selectedServices_partList.add(hashMap.get(carMaintenanceServiceList[i].id)!!)
                            }
                        }
                    }
                }


                val bundle = Bundle()
                bundle.putSerializable(Constant.Path.PartID, hashMap as Serializable)
                val selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
                startActivity(intentFor<WorkshopListActivity>(
                        Constant.Key.is_car_maintenance_service to true,
                        Constant.Path.serviceID to /*serviceId.joinToString(",")*/  Gson().toJson(selectedServices_partList),
                        Constant.Path.workshopFilterSelectedDate to selectedFormattedDate
                ).putExtras(bundle)
                )


            } else {
                Snackbar.make(btn_choose_workshop, getString(R.string.please_select_maintenance), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

    private fun getCarMaintenance() {
        try {
            progress_bar.visibility = View.VISIBLE
            val priceRangeString = if (priceRangeFinal == -1f) "" else "$priceRangeInitial,${priceRangeFinal}"
            val priceSortLevel = if (isPriceLowToHigh) 1 else 2
            val selectedCar = getSelectedCar() ?: Models.MyCarDataSet()
            val selectedVehicleVersionID = selectedCar.carVersionModel.idVehicle

            RetrofitClient.client.getCarMaintenanceService(getBearerToken()
                    ?: "", getSelectedCar()?.carVersion!!, "en", frontRear, leftRight,
                    priceRangeString, priceSortLevel, getUserId())
                    .onCall { networkException, response ->
                        progress_bar.visibility = View.GONE
                        networkException?.let {
                        }
                        response?.let {
                            val body = JSONObject(response.body()?.string())
                            if (response.isSuccessful) {

                                if (body.has("data_set") && body.get("data_set") != null && body.get("data_set") is JSONArray) {
                                    carMaintenanceServiceList.clear()//during reload page.
                                    for (i in 0 until body.getJSONArray("data_set").length()) {
                                        val servicesObj = body.getJSONArray("data_set").get(i) as JSONObject
                                        val carMaintenance = Gson().fromJson<Models.CarMaintenanceServices>(servicesObj.toString(), Models.CarMaintenanceServices::class.java)
                                        carMaintenanceServiceList.add(carMaintenance)

                                    }
                                    //bind recyclerview
                                    setAdapter()
                                } else {

                                    if (body.has("message") && !body.getString("message").isNullOrBlank() && !body.getString("message").equals("null")) {

                                        showInfoDialog(body.getString("message"))
                                    }


                                }
                            } else {
                                if (body.has("message") && !body.getString("message").isNullOrBlank() && !body.getString("message").equals("null")) {
                                    showInfoDialog(body.getString("message"))
                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAdapter() {
        for (i in 0 until carMaintenanceServiceList.size) {
            if (maxPrice < carMaintenanceServiceList.get(i).price.toFloat()) {
                maxPrice = carMaintenanceServiceList.get(i).price.toFloat()
                seekbarPriceFinalLimit = carMaintenanceServiceList.get(i).price.toFloat()
            }
            filterDialog.price_end_range.text = if (priceRangeFinal != -1f) priceRangeFinal.toString()
            else getString(R.string.prepend_euro_symbol_string, seekbarPriceFinalLimit.toString())
        }
        try {
            for (i in 0 until carMaintenanceServiceList.size) {
                if (carMaintenanceServiceList[i].parts != null) {
                    // Log.d("Maintenance", "part info" + carMaintenanceServiceList[i].parts[0].listino)
                    carMaintenanceServiceList[i].listino = carMaintenanceServiceList[i].parts[0].listino
                    carMaintenanceServiceList[i].descrizione = if (carMaintenanceServiceList[i].parts[0].descrizione != null) carMaintenanceServiceList[i].parts[0].descrizione else ""
                    carMaintenanceServiceList[i].productId = carMaintenanceServiceList[i].parts[0].id
                    carMaintenanceServiceList[i].couponList = carMaintenanceServiceList[i].parts[0].couponList
                    carMaintenanceServiceList[i].CouponTitle = if (carMaintenanceServiceList[i].parts[0].couponList != null && carMaintenanceServiceList[i].parts[0].couponList.size != 0) carMaintenanceServiceList[i].parts[0].couponList[0].couponTitle else ""
                    carMaintenanceServiceList[i].CouponId = if (carMaintenanceServiceList[i].parts[0].couponList != null && carMaintenanceServiceList[i].parts[0].couponList.size != 0) carMaintenanceServiceList[i].parts[0].couponList[0].id else ""

                    if (carMaintenanceServiceList[i].parts[0].forPair != null) {
                        carMaintenanceServiceList[i].forPair = carMaintenanceServiceList[i].parts[0].forPair
                    } else {
                        carMaintenanceServiceList[i].forPair = ""
                    }


                    if (carMaintenanceServiceList[i].parts[0].sellerPrice != null) {
                        carMaintenanceServiceList[i].seller_price = if (carMaintenanceServiceList[i].parts[0].sellerPrice != null) carMaintenanceServiceList[i].parts[0].sellerPrice else "0"
                    } else {
                        carMaintenanceServiceList[i].seller_price = "0"
                    }
                    if (carMaintenanceServiceList[i].parts[0].product_image_url != null) {
                        carMaintenanceServiceList[i].product_image_url = carMaintenanceServiceList[i].parts[0].product_image_url
                    } else {
                        carMaintenanceServiceList[i].product_image_url = "null"
                    }


                    if (carMaintenanceServiceList[i].parts[0].brandImageURL != null) {
                        carMaintenanceServiceList[i].brandImageURL = carMaintenanceServiceList[i].parts[0].brandImageURL
                    } else {
                        carMaintenanceServiceList[i].brandImageURL = ""
                    }



                    carMaintenanceServiceList[i].productName = carMaintenanceServiceList[i].parts[0].productName
                    carMaintenanceServiceList[i].rating_star = carMaintenanceServiceList[i].parts[0].rating_star
                    carMaintenanceServiceList[i].rating_count = carMaintenanceServiceList[i].parts[0].rating_count
                    carMaintenanceServiceList[i].wishlist = carMaintenanceServiceList[i].parts[0].wishlist

                }
            }
        } catch (e: Exception) {
            Log.e("Exception::", "${e}")
            Log.d("Maintenance", "part info$e")
            e.printStackTrace()
        }
        genericAdapter = GenericAdapter<Models.CarMaintenanceServices>(this, R.layout.item_maintenance_selection)
        genericAdapter!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                if (!carMaintenanceServiceList[position].ourDescription.isNullOrEmpty() && carMaintenanceServiceList[position].ourDescription != "null") {

                    val message = carMaintenanceServiceList[position].ourDescription
                    showInfoDialog(message)
                }
            }

            override fun onItemClick(view: View, position: Int) {
                if (view.tag == "100") {
                    checkBox = view.findViewById(R.id.maintenance_item_chk) as CheckBox
                    if (carMaintenanceServiceList[position].productId != null || carMaintenanceServiceList[position].type == "2") {
                        if (checkBox.isChecked) {

                            carMaintenanceServiceList[position].isChecked = true//for checkbox during recycler view scroll not to change view state.
                            selectedCarMaintenanceServices.add(carMaintenanceServiceList[position])
                            selectedServicesTotalPrice = (selectedServicesTotalPrice + (carMaintenanceServiceList[position].price.toDouble())).roundTo2Places()
                            btn_choose_workshop.text = getString(R.string.choose_workshop) + " (${getString(R.string.prepend_euro_symbol_string, selectedServicesTotalPrice.toString())})"

                        } else {
                            carMaintenanceServiceList[position].isChecked = false

                            if (selectedCarMaintenanceServices.contains(carMaintenanceServiceList[position])) {
                                //hashMap.remove(carMaintenanceServiceList[position].id)
                                Log.e("carMaintenance_Id::", carMaintenanceServiceList[position].id)
                                selectedCarMaintenanceServices.remove(carMaintenanceServiceList[position])
                                // Log.e("selecteServiceUNCHECD::", "${position} ${selectedCarMaintenanceServices.size}")
                                if (selectedServicesTotalPrice > 0) {
                                    // Log.e("Price", "${(carMaintenanceServiceList[position].price!!.toDouble()).roundTo2Places()}")
                                    selectedServicesTotalPrice = (selectedServicesTotalPrice - (carMaintenanceServiceList[position].price.toDouble())).roundTo2Places()
                                    btn_choose_workshop.text = getString(R.string.choose_workshop) + "  (${getString(R.string.prepend_euro_symbol_string, selectedServicesTotalPrice.toString())})"

                                }
                            }
                        }
                    } else {
                        checkBox.isChecked = false
                        Snackbar.make(btn_choose_workshop, getString(R.string.partNotAvailable), Snackbar.LENGTH_SHORT).show()
                        // return@setOnClickListener
                    }
                } else if (view.tag == "102") {
                    if (carMaintenanceServiceList[position].couponList != null) {
                        displayCoupons(carMaintenanceServiceList[position].couponList, "workshop_coupon", tv_CouponTitle, carMaintenanceServiceList[position])
                    }
                } else if (view.tag == "103") {
                    add_remove_product__Wishlist(carMaintenanceServiceList[position].wishlist, Iv_favorite_mainPart, carMaintenanceServiceList[position].productId, 0, position, false)

                } else {
                    if (carMaintenanceServiceList[position].parts != null && carMaintenanceServiceList[position].parts.size > 0) {

                        if (carMaintenanceServiceList[position].parts.size > 1) {
                            partsDialog(carMaintenanceServiceList[position].parts)
                        } else {
                            getAllPartsMaintaince(carMaintenanceServiceList[position].id, position)
                        }

                        selectservice_position = position

                    } else {
                        progress_bar.visibility = View.VISIBLE
                        getAllParts(carMaintenanceServiceList[position].id, position)
                    }
                }
            }
        })

        recycler_view.adapter = genericAdapter
        genericAdapter!!.addItems(carMaintenanceServiceList)
    }

    private fun getAllParts(serviceId: String, position: Int) {
        try {
            RetrofitClient.client.kromedaParts(getBearerToken() ?: "", serviceId, getUserId())
                    .onCall { networkException, response ->

                        networkException?.let { progress_bar.visibility = View.GONE }
                        response?.let {
                            progress_bar.visibility = View.GONE
                            if (response.isSuccessful) {

                                val jsonObj = JSONObject(response.body()?.string())
                                if (jsonObj.has("status_code") && !jsonObj.isNull("status_code") && jsonObj.getString("status_code").equals(0)
                                        && jsonObj.has("data_set") || jsonObj.isNull("data_set")) {
                                    Snackbar.make(btn_choose_workshop, getString(R.string.partNotAvailable), Snackbar.LENGTH_SHORT).show()
                                }
                                if (jsonObj.has("data_set") && jsonObj.get("data_set") is JSONArray && !jsonObj.isNull("data_set")) {
                                    val jsonPartsArray = jsonObj.getJSONArray("data_set")
                                    if (jsonPartsArray.length() > 0) {
                                        val arrayListParts = ArrayList<Models.Part>()
                                        arrayListParts.clear()
                                        for (i in 0 until jsonPartsArray.length()) {
                                            val modelPart = Gson().fromJson<Models.Part>(jsonPartsArray.get(i).toString(), Models.Part::class.java)
                                            arrayListParts.add(modelPart)
                                        }
                                        carMaintenanceServiceList[position].parts = arrayListParts
                                        partsDialog(carMaintenanceServiceList[position].parts)
                                        selectservice_position = position
                                        genericAdapter!!.addItems(carMaintenanceServiceList)
                                    }
                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            progress_bar.visibility = View.GONE
            e.printStackTrace()
        }
    }


    private fun getAllPartsMaintaince(serviceId: String, position: Int) {
        progress_bar.visibility = View.VISIBLE
        try {
            RetrofitClient.client.getCarMaintenancePart(getSelectedCar()?.carVersionModel?.idVehicle!!
                    ?: "", serviceId, getUserId())
                    .onCall { networkException, response ->

                        networkException?.let { progress_bar.visibility = View.GONE }
                        response?.let {
                            progress_bar.visibility = View.GONE
                            if (response.isSuccessful) {

                                val jsonObj = JSONObject(response.body()?.string())
                                if (jsonObj.has("status_code") && !jsonObj.isNull("status_code") && jsonObj.getString("status_code").equals(0)
                                        && jsonObj.has("data_set") || jsonObj.isNull("data_set")) {
                                    Snackbar.make(btn_choose_workshop, getString(R.string.partNotAvailable), Snackbar.LENGTH_SHORT).show()
                                }
                                if (jsonObj.has("data_set") && jsonObj.get("data_set") is JSONArray && !jsonObj.isNull("data_set")) {
                                    val jsonPartsArray = jsonObj.getJSONArray("data_set")
                                    if (jsonPartsArray.length() > 0) {
                                        val arrayListParts = ArrayList<Models.Part>()
                                        arrayListParts.clear()
                                        for (i in 0 until jsonPartsArray.length()) {
                                            val modelPart = Gson().fromJson<Models.Part>(jsonPartsArray.get(i).toString(), Models.Part::class.java)
                                            arrayListParts.add(modelPart)
                                        }
                                        carMaintenanceServiceList[position].parts = arrayListParts
                                        partsDialog(carMaintenanceServiceList[position].parts)
                                        selectservice_position = position
                                        genericAdapter!!.addItems(carMaintenanceServiceList)
                                    }
                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            progress_bar.visibility = View.GONE
            e.printStackTrace()
        }
    }

    private fun partsDialog(parts: ArrayList<Models.Part>) {

        dialog = Dialog(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(view)
        val window: Window = dialog!!.window!!
        window.setDimAmount(0f)
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        dialog!!.setContentView(view)
        val title = dialog!!.findViewById(R.id.title) as TextView

        title.text = getString(R.string.parts)
        title.setTextColor(Color.parseColor("#FFFFFF"))
        val noteForCoupon = dialog!!.findViewById(R.id.ll_Note_ApplyCoupon) as LinearLayout
        noteForCoupon.visibility = View.GONE
        genericAdapterParts = GenericAdapter<Models.Part>(this@MaintenanceActivity, R.layout.maintenance_part_dialog)
        genericAdapterParts!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                try {
                    if (carMaintenanceServiceList[selectservice_position].parts != null) {
                        carMaintenanceServiceList[selectservice_position].listino = carMaintenanceServiceList[selectservice_position].parts[position].listino
                        carMaintenanceServiceList[selectservice_position].descrizione = if (carMaintenanceServiceList[selectservice_position].parts[position].descrizione != null) carMaintenanceServiceList[selectservice_position].parts[position].descrizione else ""
                        carMaintenanceServiceList[selectservice_position].productId = carMaintenanceServiceList[selectservice_position].parts[position].id
                        carMaintenanceServiceList[selectservice_position].usersId = carMaintenanceServiceList[selectservice_position].parts[position].usersId
                        carMaintenanceServiceList[selectservice_position].couponList = carMaintenanceServiceList[selectservice_position].parts[position].couponList

                        carMaintenanceServiceList[selectservice_position].CouponTitle = if (carMaintenanceServiceList[selectservice_position].parts[position].couponList != null && carMaintenanceServiceList[selectservice_position].parts[position].couponList.size != 0) carMaintenanceServiceList[selectservice_position].parts[position].couponList[0].couponTitle else ""
                        carMaintenanceServiceList[selectservice_position].CouponId = if (carMaintenanceServiceList[selectservice_position].parts[position].couponList != null && carMaintenanceServiceList[selectservice_position].parts[position].couponList.size != 0) carMaintenanceServiceList[selectservice_position].parts[position].couponList[0].id else ""

                        if (carMaintenanceServiceList[selectservice_position].parts[position].forPair != null) {
                            carMaintenanceServiceList[selectservice_position].forPair = carMaintenanceServiceList[selectservice_position].parts[position].forPair
                        } else {
                            carMaintenanceServiceList[selectservice_position].forPair = ""
                        }


                        if (carMaintenanceServiceList[selectservice_position].parts[position].sellerPrice != null) {
                            carMaintenanceServiceList[selectservice_position].seller_price = carMaintenanceServiceList[selectservice_position].parts[position].sellerPrice
                        } else {
                            carMaintenanceServiceList[selectservice_position].seller_price = "0"
                        }
                        if (carMaintenanceServiceList[selectservice_position].parts[position].product_image_url != null) {
                            carMaintenanceServiceList[selectservice_position].product_image_url = carMaintenanceServiceList[selectservice_position].parts[position].product_image_url
                        } else {
                            carMaintenanceServiceList[selectservice_position].product_image_url = ""
                        }


                        if (carMaintenanceServiceList[selectservice_position].parts[position].brandImageURL != null) {
                            carMaintenanceServiceList[selectservice_position].brandImageURL = carMaintenanceServiceList[selectservice_position].parts[position].brandImageURL
                        } else {
                            carMaintenanceServiceList[selectservice_position].brandImageURL = ""
                        }
                        if (carMaintenanceServiceList[selectservice_position].parts[position].couponList != null) {
                            carMaintenanceServiceList[selectservice_position].parts[position].couponList = carMaintenanceServiceList[selectservice_position].parts[position].couponList

                        } else {

                        }


                        carMaintenanceServiceList[selectservice_position].productName = if (carMaintenanceServiceList[selectservice_position].parts[position].productName.isNullOrEmpty()) "" else carMaintenanceServiceList[selectservice_position].parts[position].productName
                        carMaintenanceServiceList[selectservice_position].rating_star = if (carMaintenanceServiceList[selectservice_position].parts[position].rating_star.isNullOrEmpty()) "" else carMaintenanceServiceList[selectservice_position].parts[position].rating_star
                        carMaintenanceServiceList[selectservice_position].rating_count = if (carMaintenanceServiceList[selectservice_position].parts[position].rating_count.isNullOrEmpty()) "" else carMaintenanceServiceList[selectservice_position].parts[position].rating_count
                        carMaintenanceServiceList[selectservice_position].wishlist = if (carMaintenanceServiceList[selectservice_position].parts[position].wishlist.isNullOrEmpty()) "" else carMaintenanceServiceList[selectservice_position].parts[position].wishlist

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                dialog!!.dismiss()
                genericAdapter!!.notifyDataSetChanged()
                Log.e("SELECTPART", Gson().toJson(carMaintenanceServiceList[selectservice_position]).toString())

            }

            override fun onItemClick(view: View, position: Int) {

                add_remove_product__Wishlist(carMaintenanceServiceList[selectservice_position].parts[position].wishlist, view.findViewById(R.id.part_Iv_favorite), carMaintenanceServiceList[selectservice_position].parts[position].productId, position, selectservice_position, true)
            }
        })

        view.imageCross.setOnClickListener {
            getCarMaintenance()
            dialog!!.dismiss()

        }
        view.dialog_recycler_view.adapter = genericAdapterParts
        genericAdapterParts!!.addItems(parts)

        dialog!!.show()
    }

    private fun add_remove_product__Wishlist(wish_list: String, Iv_favorite: ImageView, ProductId: String, position: Int, selectservice_position: Int, frompart: Boolean) {
        try {
            if (wish_list.isNullOrBlank() || wish_list == "0") {
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

                                if (frompart) {
                                    logAddToWishlistEvent(this, carMaintenanceServiceList[selectservice_position]?.productName!!, ProductId.toString(), "1", "USD", if (!carMaintenanceServiceList[selectservice_position]?.seller_price.isNullOrBlank()) carMaintenanceServiceList[selectservice_position]?.seller_price.toDouble()!! else 0.0)

                                    carMaintenanceServiceList[selectservice_position].parts[position].wishlist = "1"

                                } else {
                                    val list = carMaintenanceServiceList[selectservice_position].parts.filter { it.productId == ProductId }
                                    list[0].wishlist = "1"

                                }
                                carMaintenanceServiceList[selectservice_position].wishlist = "1"
                                showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))

                                logAddToWishlistEvent(this, carMaintenanceServiceList[position].productName!!, ProductId, "1", "USD", if (!carMaintenanceServiceList[position]?.seller_price.isNullOrBlank()) carMaintenanceServiceList[position]?.seller_price?.toDouble()!! else 0.0)

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

                                if (frompart) {
                                    carMaintenanceServiceList[selectservice_position].parts[position].wishlist = "0"

                                } else {
                                    val list = carMaintenanceServiceList[selectservice_position].parts.filter { it.productId == ProductId }
                                    list[0].wishlist = "0"

                                }
                                carMaintenanceServiceList[selectservice_position].wishlist = "0"
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

    private fun createFilterDialog() {
        filterDialog = Dialog(this, R.style.DialogSlideAnimStyle)
        val drawableLeft = ContextCompat.getDrawable(this@MaintenanceActivity, R.drawable.ic_sort_black_24dp)
        val drawableRight = ContextCompat.getDrawable(this@MaintenanceActivity, R.drawable.shape_circle_orange_8dp)
        drawableRight?.setBounds(100, 100, 100, 100)

        with(filterDialog) {

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.car_maintenance_dialog_layout_filter)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { dismiss() }

            dialog_price_range.setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                    tempPriceInitial = floor(leftValue)
                    tempPriceFinal = ceil(rightValue)

                    val seekPriceFinal = (ceil(rightValue) / 100) * seekbarPriceFinalLimit
                    val seekPriceInitial = (floor(leftValue) / 100) * seekbarPriceFinalLimit

                    val priceFinalString = String.format("%.2f", seekPriceFinal)
                    val priceInitialString = String.format("%.2f", seekPriceInitial)

                    tempPriceInitial = floor(seekPriceInitial)
                    tempPriceFinal = ceil(seekPriceFinal)

                    price_start_range.text = getString(R.string.prepend_euro_symbol_string, priceInitialString)
                    price_end_range.text = getString(R.string.prepend_euro_symbol_string, priceFinalString)

                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
            })

            all_check_box.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    rear_check_box.isChecked = false
                    front_check_box.isChecked = false
                    left_check_box.isChecked = false
                    right_check_box.isChecked = false
                }
            }

            front_check_box.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    rear_check_box.isChecked = false
                    all_check_box.isChecked = false
                } else frontRear = ""
            }
            rear_check_box.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    front_check_box.isChecked = false
                    all_check_box.isChecked = false
                } else frontRear = ""
            }
            left_check_box.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    right_check_box.isChecked = false
                    all_check_box.isChecked = false
                } else leftRight = ""
            }
            right_check_box.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    left_check_box.isChecked = false
                    all_check_box.isChecked = false
                } else leftRight = ""
            }

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

                if (right_check_box.isChecked)
                    leftRight = /*"${"rh."}"*/"${"sx."}"

                if (left_check_box.isChecked)
                    leftRight = /*"${"lh."}"*/"${"dx."}"

                if (rear_check_box.isChecked)
                    frontRear = /*"${"rear"}"*/"${"post."}"

                if (front_check_box.isChecked)
                    frontRear = /*"${"front"}"*/"${"ant."}"

                if (all_check_box.isChecked) {
                    frontRear = ""
                    leftRight = ""
                }

                if (ratingString.isNotEmpty()) {
                    if (ratingString.toCharArray()[ratingString.lastIndex] == ',')
                        ratingString = ratingString.substring(0, ratingString.lastIndex).trim()
                }





                priceRangeInitial = tempPriceInitial
                priceRangeFinal = tempPriceFinal
                if (!leftRight.isBlank() || !frontRear.isBlank() || !ratingString.isBlank() || priceRangeInitial.toInt() != 0 || priceRangeFinal.toInt() != -1) {
                    this@MaintenanceActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, drawableRight, null)
                } else {
                    this@MaintenanceActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, null, null)

                }
                getCarMaintenance()

                dismiss()

                return@setOnMenuItemClickListener true
            }

            dialog_price_range.setValue(0.00f, dialog_price_range.maxProgress)

            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar_title.text = getString(R.string.filter)



            clear_selection.setOnClickListener {
                dialog_price_range.setValue(0f, dialog_price_range.maxProgress)


                ratingString = ""
                frontRear = ""
                leftRight = ""
                all_check_box.isChecked = false
                front_check_box.isChecked = false
                rear_check_box.isChecked = false
                left_check_box.isChecked = false
                right_check_box.isChecked = false
                priceRangeFinal = -1f
                priceRangeInitial = 0f
                tempPriceInitial = 0f
                tempPriceFinal = -1f
                this@MaintenanceActivity.filter_text.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, null, null, null)


            }
            create()
        }
    }

    private fun createSortDialog() {
        sortDialog = Dialog(this@MaintenanceActivity, R.style.DialogSlideAnimStyle)
        with(sortDialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_sorting)
            window?.setGravity(Gravity.TOP)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            sort_distance_container.visibility = View.GONE

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { dismiss() }

            toolbar_title.text = getString(R.string.sort)//"Sort"
            toolbar.inflateMenu(R.menu.menu_single_item)
            toolbar.setOnMenuItemClickListener {

                val priceIndex = radio_grp_price.indexOfChild(radio_grp_price.findViewById(radio_grp_price.checkedRadioButtonId))
                val distanceIndex = radio_grp_distance.indexOfChild(radio_grp_distance.findViewById(radio_grp_distance.checkedRadioButtonId))

                isPriceLowToHigh = priceIndex == 0

                getCarMaintenance()
                dismiss()
                return@setOnMenuItemClickListener true
            }
            create()
        }
    }

    private fun displayCoupons(couponsList: ArrayList<Models.Coupon>, couponType: String, textView: TextView, CarMaintenanceServicesObject: Models.CarMaintenanceServices) {

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
                    if (!items.offerType.isNullOrBlank()) {

                        if (items.offerType == "2") {
                            holder.couponsAmount.text = getString(R.string.prepend_euro_symbol_string, items.amount.toString())
                        } else {
                            holder.couponsAmount.text = items.amount.toString() + getString(R.string.prepend_percentage_symbol)
                        }
                    }
                    holder.itemView.setOnClickListener {
                        textView.text = items.couponTitle
                        CarMaintenanceServicesObject.CouponTitle = items.couponTitle
                        CarMaintenanceServicesObject.CouponId = items.id
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val partdata = data.getStringExtra("data")
            val partID = data.getStringExtra("ID")
            val partmod = Gson().fromJson<Models.Part>(partdata, Models.Part::class.java)
            //carPartList.set(selectitem_position,partmod)
            carMaintenanceServiceList[selectservice_position].parts.set(selectitem_position, partmod)
            val dialog_recycler_view2 = dialog!!.findViewById(R.id.dialog_recycler_view) as RecyclerView
            dialog_recycler_view2.adapter = genericAdapterParts
            genericAdapterParts!!.addItems(carMaintenanceServiceList[selectservice_position].parts)
            dialog_recycler_view2.layoutManager!!.scrollToPosition(selectitem_position)


        }
    }


}