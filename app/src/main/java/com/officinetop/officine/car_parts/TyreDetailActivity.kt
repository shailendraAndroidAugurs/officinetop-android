package com.officinetop.officine.car_parts

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.slider.library.Indicators.PagerIndicator
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.daimajia.slider.library.Tricks.ViewPagerEx
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.*
import com.officinetop.officine.feedback.FeedbackListActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.utils.Constant.defaultDistance
import com.officinetop.officine.views.DialogTouchImageSlider
import com.officinetop.officine.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.activity_tyre_detail.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import kotlinx.android.synthetic.main.tyre_info_layout.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TyreDetailActivity : BaseActivity(), OnGetFeedbacks {
    private var tyreType: String = ""
    private lateinit var imageDialog: Dialog
    private lateinit var dialogSlider: SliderLayout
    private var disableSliderTouch = false
    private var productDetails: Models.TyreDetailItem? = null
    private var selectedProductID = 0
    private var totalPrice = 0.0f
    private var oneItemAdditionalPrice = 0.0f
    private var pfuAmount = 0.0f
    private var tyreTypeAmount = 0.0f
    private var tyre_response = JSONObject()
    var price: String = ""
    private var speedIndexMap: HashMap<String, String> = HashMap<String, String>()
    private var cartItem: Models.CartItem? = null
    private var productIsPair = false
    private var minimumServicePrices = ""
    private var Deliverydays = ""
    private var tyre_mainCategory_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyre_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.tyre_detail)
        initViews()
        getLocation()
    }


    private fun initViews() {
        speedIndexMapFun()
        see_all_feedback.setOnClickListener {
            startActivity(intentFor<FeedbackListActivity>(Constant.Path.productId to selectedProductID.toString(),
                    Constant.Path.productType to "2", Constant.Path.sellerId to productDetails?.user_id.toString(), Constant.Path.ProductOrWorkshopName to productDetails?.manufacturer_description.takeIf { !it.isNullOrEmpty() }
                    , Constant.Path.type to "1", Constant.Path.mainCategoryId to "", Constant.Path.serviceID to ""

            ))
        }
        if (intent.hasExtra(Constant.Path.productDetails))

            productDetails = intent.getSerializableExtra(Constant.Path.productDetails) as Models.TyreDetailItem
        if (productDetails != null && productDetails?.tyrePfu != null && !productDetails?.tyrePfu?.price.isNullOrBlank()) {
            pfuAmount = productDetails?.tyrePfu?.price?.toFloat()!!
        }

        if (productDetails != null && productDetails?.couponList != null && productDetails?.couponList?.size != 0) {
            AppliedCouponName.text = (productDetails?.couponList?.get(0)?.couponTitle)
            AppliedCouponName.visibility = View.VISIBLE
            CouponLabel.visibility = View.INVISIBLE
            offerBadge.visibility = View.VISIBLE
            productDetails?.SelectedTyreCouponId = productDetails?.couponList?.get(0)?.id.toString()
            Log.d("tyre", "CouponId :Tyre" + productDetails?.SelectedTyreCouponId)
        } else {
            productDetails?.SelectedTyreCouponId = ""
            CouponLabel.visibility = View.GONE
            AppliedCouponName.visibility = View.GONE
            offerBadge.visibility = View.GONE
        }


        offerBadge.setOnClickListener {
            if (productDetails?.couponList != null) {
                displayCoupons(productDetails?.couponList as MutableList<Models.Coupon>, "workshop_coupon", AppliedCouponName, productDetails)
            }
        }


        tyre_response = JSONObject(productDetails?.tyre_response)

        loadTyreDetails(productDetails!!.id!!, productDetails!!.user_id!!)//api call get_tyre_details

        price = productDetails?.seller_price?.takeIf { !it.isNullOrEmpty() }.toString()// ?: productDetails?.price.toString()

        tyre_label_img.visibility = View.GONE
        getSimilarProduct(productDetails!!.id?.toString()!!)
        if (price.isEmpty())
            return


        if (productDetails?.brand_image.isNullOrBlank()) {
            item_brand_image.visibility = View.GONE
        } else {
            loadImage(productDetails?.brand_image, item_brand_image)
        }


        if (productDetails?.wish_list != null && productDetails?.wish_list.equals("1")) {

            iv_wishList.setImageResource(R.drawable.ic_heart)

        } else {

            iv_wishList.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)

        }

        iv_wishList.setOnClickListener {

            addRemoveProductToWishlist()

        }


        loadProductDetails(productDetails!!)
        Log.d("assemblestatus", productDetails?.assemblestatus.toString())

        if (productDetails?.assemblestatus.equals("Y")) {
            buy_product_with_assembly.visibility = View.VISIBLE
        } else {
            buy_product_with_assembly.visibility = View.GONE
        }


        try {
            if (productDetails?.quantity.isNullOrBlank() || productDetails?.quantity.equals("null") || productDetails?.quantity.equals("0")) {
                add_product_to_cart.isEnabled = false
                buy_product_with_assembly.isEnabled = false

                productTotalPrices.visibility = View.GONE
                product_price_.text = getString(R.string.tyre_price_total, "0.0")
                item_qty.text = "0"
            } else if (price.isNullOrEmpty() || price == "null" || price == "0" || price == "0.0") {
                add_product_to_cart.isEnabled = false
                buy_product_with_assembly.isEnabled = false

                productTotalPrices.visibility = View.GONE
                product_price_.text = getString(R.string.tyre_price_total, "0.0")
            } else {
                cartItem = Models.CartItem(name = product_name.text.toString().trim().takeIf { !it.isNullOrEmpty() }!!,
                        description = productDetails!!.description,
                        price = price.replace(",", "").toDouble(),
                        type = Constant.type_product,
                        tyreDetail = productDetails

                )
                productTotalPrices.visibility = View.VISIBLE
                product_price_.visibility = View.VISIBLE

                if (!productDetails?.pair.isNullOrBlank() && !productDetails?.pair.equals("0")) {
                    Log.d("spare part", "is pair")
                    productIsPair = true
                    item_qty.text = "2" // According to client requremenent if product is on pair then quantity is multiply of 2
                    setTotalPriceForQty(2)

                } else setTotalPriceForQty(1)
                itemQty()

            }

            add_product_to_cart.setOnClickListener {
                /*  if (item_qty.text.toString().toInt() >= 0) {
                      showInfoDialog("")
                  } else if (price.toFloat() >= 0.toFloat()) {
                      showInfoDialog("")
                  } else {*/
                cartItem?.quantity = item_qty.text.toString().toInt()
                cartItem?.additionalPrice = oneItemAdditionalPrice.toDouble().roundTo2Places()
                cartItem?.finalPrice = ((price.toFloat() + pfuAmount.toFloat()).toInt() * cartItem?.quantity.toString().toInt()).toDouble().roundTo2Places()
                try {
                    addToCartProducts(this, selectedProductID.toString(), cartItem?.quantity.toString(), (pfuAmount + tyreTypeAmount).toString(),
                            if (productDetails?.SelectedTyreCouponId != null && !productDetails?.SelectedTyreCouponId.equals("null")) productDetails?.SelectedTyreCouponId else "",
                            price, ((price.toFloat() + pfuAmount).toInt() * cartItem?.quantity.toString().toInt()).toString(), "0.0", "2", productDetails!!.user_id.toString(), cartItem!!.name, cartItem!!.description!!, productDetails!!.user_id.toString())

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //  }


            }

            buy_product_with_assembly.setOnClickListener {

                if (productDetails != null) {
                    cartItem?.quantity = item_qty.text.toString().toInt()
                    cartItem?.additionalPrice = oneItemAdditionalPrice.toDouble().roundTo2Places()
                    cartItem?.finalPrice = ((price.toFloat() + pfuAmount).toInt() * cartItem?.quantity.toString().toInt()).toDouble().roundTo2Places()
                    cartItem?.pfu_tax = (pfuAmount + tyreTypeAmount).toString()
                    cartItem?.tyretotalPrice = ((price.toFloat() + pfuAmount).toInt() * cartItem?.quantity.toString().toInt()).toDouble().roundTo2Places().toString()
                    cartItem?.price = price.toDouble()
                    cartItem?.Deliverydays = Deliverydays
                    Log.d("Date", "Deliverydays: tyreDetail" + cartItem?.Deliverydays)
                    val myIntent = intentFor<WorkshopListActivity>(
                            Constant.Key.is_workshop to false,
                            Constant.Key.is_tyre to true,
                            Constant.Path.productId to selectedProductID,
                            Constant.Key.productDetail to productDetails?.toString(),
                            Constant.Key.cartItem to cartItem,
                            "tyre_mainCategory_id" to tyre_mainCategory_id

                    )


                    startActivity(myIntent)


                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            getFeedbacks(this, "", selectedProductID.toString(), "1", "2")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun speedIndexMapFun() {
        speedIndexMap["L"] = "120 km/h"
        speedIndexMap["M"] = "130 km/h"
        speedIndexMap["N"] = "140 km/h"
        speedIndexMap["P"] = "150 km/h"
        speedIndexMap["Q"] = "160 km/h"
        speedIndexMap["R"] = "170 km/h"
        speedIndexMap["S"] = "180 km/h"
        speedIndexMap["T"] = "190 km/h"
        speedIndexMap["U"] = "200 km/h"
        speedIndexMap["H"] = "210 km/h"
        speedIndexMap["V"] = "240 km/h"
        speedIndexMap["Y"] = "300 km/h"
        speedIndexMap["ZR"] = "240 km/h"
    }

    private fun addRemoveProductToWishlist() {
        try {
            if (productDetails?.wish_list == "0") {
                RetrofitClient.client.addToFavorite(getBearerToken()
                        ?: "", productDetails?.id.toString(), "2", "", getSelectedCar()?.carVersionModel?.idVehicle
                        ?: "").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage(this) }


                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                iv_wishList.setImageResource(R.drawable.ic_heart)

                                productDetails?.wish_list = "1"


                                showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))

                                logAddToWishlistEvent(this, product_name.text.toString(), productDetails?.id.toString(), "2", "USD", if (!productDetails?.seller_price.isNullOrBlank()) productDetails?.seller_price?.toDouble()!! else 0.0)
                            }

                        }

                    }
                }

            } else {

                RetrofitClient.client.removeFromFavorite(getBearerToken()
                        ?: "", productDetails?.id.toString(), "", "2").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage(this) }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                iv_wishList!!.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                                productDetails?.wish_list = "0"
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


    private fun itemQty(): String {
        var qty_text = 1
        if (productIsPair) {
            qty_text = 2
        }

        btn_qty_increment.setOnClickListener {
            if (productDetails?.quantity.isNullOrBlank() || productDetails?.quantity.equals("null") || productDetails?.quantity.equals("0")) {
                qty_text = 0
                item_qty.text = qty_text.toString()
            } else if (if (productIsPair) qty_text / 2 < productDetails?.quantity?.toInt()!! else qty_text < productDetails?.quantity?.toInt()!!) {
                if (productIsPair) {
                    qty_text += 2
                    setTotalPriceForQty(qty_text.toString().toInt())
                    if (minimumServicePrices != "") {
                        Log.d("tyre", "minimum prices * quntity" + (minimumServicePrices.toDouble() * qty_text).toString())
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (minimumServicePrices.toDouble() * qty_text).toString())})"
                    }
                    item_qty.text = qty_text.toString()
                    Log.d("tyreDetail", "ispair" + "yes")
                } else {
                    Log.d("tyreDetail", "nonpair" + "yes")
                    qty_text += 1
                    setTotalPriceForQty(qty_text.toString().toInt())
                    if (minimumServicePrices != "") {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (minimumServicePrices.toDouble() * qty_text).toString())})"
                    }
                    item_qty.text = qty_text.toString()
                }
            }


        }
        btn_qty_decrement.setOnClickListener {
            if (if (productIsPair) qty_text > 2 else qty_text > 1) {

                if (productIsPair) {
                    qty_text -= 2
                    item_qty.text = (qty_text).toString()
                    setTotalPriceForQty(item_qty.text.toString().toInt())
                    if (minimumServicePrices != "") {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (minimumServicePrices.toDouble() * qty_text).toString())})"
                    }
                } else {
                    qty_text -= 1
                    item_qty.text = qty_text.toString()
                    if (minimumServicePrices != "") {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (minimumServicePrices.toDouble() * qty_text).toString())})"
                    }

                    setTotalPriceForQty(item_qty.text.toString().toInt())
                }


            }
        }
        return if (productIsPair) (item_qty.text.toString().trim().toInt() * 2).toString() else item_qty.text.toString().trim()
    }

    private fun setTotalPriceForQty(qty: Int) {
        val t_price = /*qty * */price.toFloat() + pfuAmount
        oneItemAdditionalPrice = t_price
        product_price_.text = getString(R.string.tyreprice_text, price.toFloat().toDouble().roundTo2Places().toString()) /*+ "," + getString(R.string.PFU_price, pfuAmount.toDouble().roundTo2Places().toString())*/
        productTotalPrices.text = getString(R.string.tyre_price_total, (price.toFloat().toDouble().roundTo2Places() * qty.toDouble()).toDouble().roundTo2Places().toString())

        totalPrice = t_price * qty


    }

    private fun loadTyreDetails(id: Int, userId: Int) {
        try {
            val selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
            RetrofitClient.client.getTyreDetails(userId.toString(), id.toString(), selectedFormattedDate, getSelectedCar()?.carVersionModel?.idVehicle!!, getLat(), getLong(), defaultDistance)
                    .onCall { _, response ->

                        response?.body()?.string()?.let {
                            if (isStatusCodeValid(it)) {
                                val data = getDataFromResponse(it)
                                data.let { it1 ->

                                    val tyreDetailData = Gson().fromJson<Models.TyreDetailData>(it1.toString(), Models.TyreDetailData::class.java)
                                    if (!tyreDetailData.delivery_days.isNullOrBlank()) {
                                        delivery_date.text = getDate(tyreDetailData.delivery_days.toInt())
                                        Deliverydays = tyreDetailData.delivery_days
                                    } else {
                                        delivery_date.text = getDate(0)
                                    }
                                    if (!tyreDetailData.tyre_mainCategory_id.isNullOrBlank()) {
                                        tyre_mainCategory_id = tyreDetailData.tyre_mainCategory_id
                                    }

                                    setDetailedInformation(tyreDetailData)
                                    seller_details.isEnabled = false
                                    if (it1.has("min_prices")) {
                                        minimumServicePrices = it1.getString("min_prices")
                                        if (!minimumServicePrices.isNullOrBlank()) {
                                            if (productIsPair)
                                                buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (minimumServicePrices.toDouble().roundTo2Places() * 2).toString())})"
                                            else
                                                buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, minimumServicePrices)})"
                                        }


                                    }


                                    if (!tyreDetailData.tyreDot.isNullOrBlank()) {
                                        ll_DOT.visibility = View.VISIBLE
                                        tv_dot.text = tyreDetailData.tyreDot
                                    }
                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                /*   val result = data?.getStringExtra("result")
                   //Log.d("result==" ,result)

                   //totalPrice -= pfuAmount
                   oneItemAdditionalPrice -= pfuAmount
                   val pfu_detail = JSONObject(result) as JSONObject
                   pfuAmount = pfu_detail.getString("add_money").toFloat()
                   //totalPrice += pfuAmount
                   oneItemAdditionalPrice += pfuAmount
                   product_price_total.text = getString(R.string.tyre_price_total, (totalPrice + pfuAmount).toString())//41.33+2.75+4.133
                   delivery_date.text = getDate(pfu_detail.getString("no_of_days").toInt())
   */
            }
        }
    }

    private fun loadProductDetails(detail: Models.TyreDetailItem) {
        var productPrice = ""
        val maxWidth = detail.max_width
        val maxDiameter = detail.max_diameter
        val maxAspectRatio = detail.max_aspect_ratio

        ll_manufacturer.visibility = if (!detail.manufacturer_description.isNullOrEmpty()) View.VISIBLE else View.GONE
        ll_wheel_size.visibility = if (!detail.max_width.isNullOrEmpty() && !detail.max_aspect_ratio.isNullOrEmpty() && !detail.max_diameter.isNullOrEmpty()) View.VISIBLE else View.GONE
        ll_seson_type.visibility = if (!detail.type.isNullOrEmpty()) View.VISIBLE else View.GONE
        ll_speed_index.visibility = if (!detail.speed_index.isNullOrEmpty()) View.VISIBLE else View.GONE
        ll_tyre_grip.visibility = if (!detail.wetGrip.isNullOrEmpty()) View.VISIBLE else View.GONE
        ll_ean_no.visibility = if (!detail.ean_number.isNullOrEmpty()) View.VISIBLE else View.GONE
        ll_three_peak_mountain_snowflake.visibility = if (detail.type.equals("w")) View.VISIBLE else View.GONE


        manufacturer.text = detail.manufacturer_description
        wheel_size.text = "${maxWidth}/${maxAspectRatio} R${maxDiameter}"
        ll_three_peak_mountain_snowflake.visibility = if (detail.is3PMSF.equals("1")) View.VISIBLE else View.GONE
        three_peak_moountain.text = if (detail.is3PMSF.equals("1")) getString(R.string.yes) else getString(R.string.no)

        when (detail.season_tyre_type) {
            "s" -> {
                season_type.text = getString(R.string.Summer)
                tyreType = getString(R.string.Summer)
            }
            "w" -> {
                season_type.text = getString(R.string.Winter)
                tyreType = getString(R.string.Winter)
            }
            "g" -> {
                season_type.text = getString(R.string.All_Season)
                tyreType = getString(R.string.All_Season)
            }

        }

        for ((k, v) in speedIndexMap) {
            if (!detail?.speed_index.isNullOrBlank() && k.contains(detail?.speed_index!!)) {
                speed_index.text = "${detail.speed_index}: " + getString(R.string.speed_index_value, v)
            }
        }
        tyre_grip.text = detail.wetGrip
        ean_no.text = detail.ean_number


        selectedProductID = detail.id!!

        product_name.text = "${detail.manufacturer_description} ${tyreType} ${detail.pr_description}\n${detail.max_width}/${detail.max_aspect_ratio} R${detail.max_diameter}"

        if (TextUtils.isEmpty(detail.seller_price) && detail.seller_price == null) productPrice = detail.price!!
        else productPrice = detail.seller_price.toString()

        product_price.text =
                if (intent.hasExtra(Constant.Path.productType) && intent.getStringExtra(Constant.Path.productType).equals("tyre", true)) {
                    getString(R.string.prepend_euro_symbol_string, "$productPrice PFU")
                } else {
                    getString(R.string.prepend_euro_symbol_string, productPrice)
                }

        if (productDetails!!.rating != null && !productDetails!!.rating!!.rating.isNullOrBlank()) {
            product_rating.rating = productDetails!!.rating!!.rating.toFloat()
        } else
            product_rating.rating = 0f

        if (!productDetails!!.ratingCount.isNullOrBlank())
            ratingCount.text = productDetails!!.ratingCount

        product_description.text = productDetails!!.description

        val imagesArray: MutableList<Models.TyreImage> = ArrayList()


        if (productDetails?.images != null && productDetails?.images!!.size > 0) {
            productDetails!!.images?.let {
                try {
                    imagesArray.addAll(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } else {
            if (!productDetails!!.imageUrl.isNullOrBlank()) {
                productDetails?.imageUrl?.let {
                    try {

                        imagesArray.add(Models.TyreImage(productDetails?.imageUrl))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }




        }


        if (!productDetails?.tyre_label_images.isNullOrEmpty() && productDetails?.tyre_label_images?.size!! > 0) {
            productDetails?.tyre_label_images?.let {
                try {
                    imagesArray.addAll(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        setImageSlider(imagesArray)

    }

    private fun setImageSlider(imagesArray: List<Models.TyreImage>?) {
        //set slider

        if (imagesArray?.size!! > 1) {
            image_slideview.visibility = View.GONE
            image_slider.visibility = View.VISIBLE
            createImageSliderDialog()
            image_slider.removeAllSliders()
            for (i in 0 until imagesArray.size) {
                val imageRes = imagesArray[i].image_url
                val slide = TextSliderView(this).image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside).empty(R.drawable.no_image_placeholder)
                image_slider.addSlider(slide)
                val scaledSlide = DialogTouchImageSlider(this, R.drawable.no_image_placeholder)
                        .description("Description")
                        .image(imageRes)
                dialogSlider.addSlider(scaledSlide)
                slide.setOnSliderClickListener {
                    if (disableSliderTouch)
                        return@setOnSliderClickListener
                    dialogSlider.currentPosition = i
                    imageDialog.show()
                }
            }
            image_slider.addOnPageChangeListener(object : ViewPagerEx.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    disableSliderTouch = state != ViewPagerEx.SCROLL_STATE_IDLE
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                }

            })
        } else {
            val imageRes = imagesArray[0].image_url
            image_slideview.visibility = View.VISIBLE
            image_slider.visibility = View.GONE
            loadImage(imageRes, image_slideview)
            image_slideview.setOnClickListener({
                imageDialog= createImageDialog(imageRes!!)
                imageDialog.show()
            })
        }




    }

    private fun createImageSliderDialog() {

        imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)

        val slider = SliderLayout(this)

        slider.stopAutoCycle()
        slider.indicatorVisibility = PagerIndicator.IndicatorVisibility.Visible
        slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialogSlider = slider

        with(imageDialog) {
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            setContentView(slider)

            window?.setGravity(android.view.Gravity.TOP)
            window?.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
            create()

        }
    }

    override fun getFeedbackList(list: MutableList<Models.FeedbacksList>) {
        bindFeedbackList(list, this)
    }

    private fun displayCoupons(couponsList: MutableList<Models.Coupon>, couponType: String, AppliedCouponName: TextView, productDetails: Models.TyreDetailItem?) {
        val dialog = Dialog(this@TyreDetailActivity)
        val dialogView: View = LayoutInflater.from(this@TyreDetailActivity).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, 1200)//height shoud be fixed
        val title = dialog.findViewById(R.id.title) as TextView

        title.text = getString(R.string.coupon_list)


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
                    if (!items.offerType.isNullOrBlank()) {

                        if (items.offerType == "2") {
                            holder.couponsAmount.text = getString(R.string.prepend_euro_symbol_string, items.amount.toString())
                        } else {
                            holder.couponsAmount.text = items.amount.toString() + getString(R.string.prepend_percentage_symbol)
                        }
                    }



                    holder.itemView.setOnClickListener {


                        if (!couponsList[position].id.isNullOrBlank()) {
                            productDetails?.SelectedTyreCouponId = couponsList[position].id.toString()
                            AppliedCouponName.text = (couponsList[position].couponTitle)
                            AppliedCouponName.visibility = View.VISIBLE
                            dialog.dismiss()
                        }

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

    private fun getSimilarProduct(productId: String) {
        //val dialog = getProgressDialog(true)
        RetrofitClient.client.getSimilarProduct(getBearerToken()
                ?: "", getSelectedCar()?.carVersionModel?.idVehicle
                ?: "", "2", productId, getUserId())
                .enqueue(
                        object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                // dialog.dismiss()
                                toast(getString(R.string.Failed_load_product_detail))
                            }

                            @SuppressLint("SetTextI18n")
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                // dialog.dismiss()
                                if (response.isSuccessful) {
                                    val body = response.body()?.string()
                                    body?.let {
                                        try {
                                            val productData = JSONObject(body)
                                            if (productData.has("data_set") && !productData.isNull("data_set")) {
                                                val jsonArray = JSONArray(JSONObject(it).getString("data_set"))
                                                val gson = GsonBuilder().create()
                                                val Smilar_Product: ArrayList<Models.TyreDetailItem> = gson.fromJson(jsonArray.toString(), Array<Models.TyreDetailItem>::class.java).toCollection(java.util.ArrayList<Models.TyreDetailItem>())
                                                loadProductRecommendationGridListForTyre(product_recommendation_recycler_view, Smilar_Product)

                                            } else {
                                                Log.e("data is null", "${productData}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ProductDetailActivity", "onResponse: onResponse", e)
                                        }
                                    }
                                } else showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again)) { finish() }
                            }


                        })
    }

    private fun setDetailedInformation(tyreDetailData: Models.TyreDetailData) {
        Log.d("setDatainUI:", "yes")
        if (tyreDetailData.rolling_resistance_arr != null) {

            if (tyreDetailData.rolling_resistance_arr.icon != null) {
                loadImage(tyreDetailData.rolling_resistance_arr.icon, iv_Rolling_Resistance)
                loadImage(tyreDetailData.rolling_resistance_arr.icon, iv_FuelEfficiency)

            }
            if (tyreDetailData.rolling_resistance_arr.name != null) {
                tv_Rolling_Resistance.text = tyreDetailData.rolling_resistance_arr.name
            }
            if (tyreDetailData.rolling_resistance_arr.description != null) {
                tv_fuelEfficiencyDescription.text = Html.fromHtml(tyreDetailData.rolling_resistance_arr.description)
            }
            if (!tyreDetailData.rolling_resistance_arr.graphical_image.isNullOrBlank()) {
                loadImage(tyreDetailData.rolling_resistance_arr.graphical_image, iv_FuelEfficiencyGraph)

            } else {
                iv_FuelEfficiencyGraph.visibility = View.GONE
            }
            if (tyreDetailData.rolling_resistance_arr.title != null) {
                tv_fuleEfficincyTitle.text = tyreDetailData.rolling_resistance_arr.title
            }


        } else {
            iv_FuelEfficiencyGraph.visibility = View.GONE
            tv_fuleEfficincyTitle.visibility = View.GONE
            tv_fuelEfficiencyDescription.visibility = View.GONE
            tv_Rolling_Resistance.visibility = View.GONE
            iv_Rolling_Resistance.visibility = View.GONE
            iv_FuelEfficiency.visibility = View.GONE
            Log.d("Detail:", "rolling: null")
        }


        if (tyreDetailData.wet_grip_arr != null) {

            if (tyreDetailData.wet_grip_arr.icon != null) {
                loadImage(tyreDetailData.wet_grip_arr.icon, iv_wetgrip)
                loadImage(tyreDetailData.wet_grip_arr.icon, iv_wetgripDetailImage)

            }
            if (tyreDetailData.wet_grip_arr.name != null) {
                tv_wetgripValue.text = tyreDetailData.wet_grip_arr.name
            }
            if (tyreDetailData.wet_grip_arr.description != null) {
                tv_wetgripDescription.text = Html.fromHtml(tyreDetailData.wet_grip_arr.description)
                Iv_WetgripGraph.visibility = View.VISIBLE
            }
            if (tyreDetailData.wet_grip_arr.title != null) {
                tv_wetgripDescriptionTitle.text = tyreDetailData.wet_grip_arr.title
            }
            if (!tyreDetailData.wet_grip_arr.graphical_image.isNullOrBlank()) {
                loadImage(tyreDetailData.wet_grip_arr.graphical_image, Iv_WetgripGraph)
            } else {
                Iv_WetgripGraph.visibility = View.GONE
            }

        } else {
            Iv_WetgripGraph.visibility = View.GONE
            tv_wetgripDescriptionTitle.visibility = View.GONE
            tv_wetgripDescription.visibility = View.GONE
            iv_wetgrip.visibility = View.GONE
            iv_wetgripDetailImage.visibility = View.GONE
            tv_wetgripValue.visibility = View.GONE
            Log.d("Detail:", "wet grip: null")
        }



        if (tyreDetailData.noice_db_arr != null) {

            if (tyreDetailData.noice_db_arr.icon != null) {
                loadImage(tyreDetailData.noice_db_arr.icon, iv_noices_db)
                loadImage(tyreDetailData.noice_db_arr.icon, iv_noiseDB_Image)

            }
            if (tyreDetailData.noice_db_arr.name != null) {
                tv_noice_dbValue.text = tyreDetailData.noice_db_arr.name + " " + "db"
            }
            if (tyreDetailData.noice_db_arr.description != null) {
                tv_NoiseDbDescription.text = Html.fromHtml(tyreDetailData.noice_db_arr.description)

            }
            if (!tyreDetailData.noice_db_arr.graphical_image.isNullOrBlank()) {
                loadImage(tyreDetailData.noice_db_arr.graphical_image, Iv_noiseGraph)
            } else {
                Iv_noiseGraph.visibility = View.GONE
            }
            if (tyreDetailData.noice_db_arr.title != null) {
                tv_NoiseDbDescriptionTitle.text = tyreDetailData.noice_db_arr.title
            }


        } else {

            tv_NoiseDbDescriptionTitle.visibility = View.GONE
            tv_NoiseDbDescription.visibility = View.GONE
            tv_noice_dbValue.visibility = View.GONE
            iv_noices_db.visibility = View.GONE
            iv_noiseDB_Image.visibility = View.GONE
            Iv_noiseGraph.visibility = View.GONE
            Log.d("Detail:", "noise: null")
        }

        if (tyreDetailData.rolling_resistance_arr == null && tyreDetailData.noice_db_arr == null && tyreDetailData.wet_grip_arr == null) {
            tv_TyreDetailinfo.visibility = View.GONE
            Log.d("Detail:", "Allobject null: null")
        }
    }
}
