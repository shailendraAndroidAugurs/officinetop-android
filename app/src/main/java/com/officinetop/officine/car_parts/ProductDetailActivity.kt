package com.officinetop.officine.car_parts

import android.annotation.SuppressLint
import android.app.Dialog
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
import com.daimajia.slider.library.Indicators.PagerIndicator
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.daimajia.slider.library.Tricks.ViewPagerEx
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.WorkshopListActivity
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.feedback.FeedbackListActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.views.DialogTouchImageSlider
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.dialog_offer_coupons_layout.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : BaseActivity(), OnGetFeedbacks {


    lateinit var imageDialog: Dialog
    lateinit var dialogSlider: SliderLayout
    var disableSliderTouch = false
    private var productDetails: JSONObject? = JSONObject()
    private var selectedProductID = 0
    private var detail: Models.ProductDetail? = null
    private var wish_list = "0"
    private var productIsPair = false
    private var min_price = ""
    private var Deliverydays = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.Product_detail)

        initViews()
        setImageSlider()
        loadProductDetails()
        see_all_feedback.setOnClickListener {
            startActivity(intentFor<FeedbackListActivity>(
                    Constant.Path.productId to selectedProductID.toString(),
                    Constant.Path.type to "1", Constant.Path.sellerId to detail?.usersId, Constant.Path.ProductOrWorkshopName to detail?.descrizione.takeIf { !it.isNullOrEmpty() }
                    , Constant.Path.productType to "1", Constant.Path.mainCategoryId to "", Constant.Path.serviceID to ""))
        }

        try {
            getFeedbacks(this, "", selectedProductID.toString(), "1", "1")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViews() {

        if (intent.hasExtra(Constant.Path.productDetails))
            productDetails = JSONObject(intent.getStringExtra(Constant.Path.productDetails))

        productDetails?.let {
            Log.d("ProductDetailActivity", "initViews: = $it")
            detail = Gson().fromJson<Models.ProductDetail>(it.toString(), Models.ProductDetail::class.java)

            loadProductDetailApi(detail?.id ?: "")
            getSimilarProduct(detail?.id ?: "")
            val json = it
            if(!detail?.productsName.isNullOrBlank()){
                item_number.visibility=View.VISIBLE
                item_number.text=getString(R.string.itemnumber,detail?.productsName)
            }
            var price: String? = if (detail?.sellerPrice == "null" || detail?.sellerPrice.isNullOrEmpty()) "0"
            else json.optString("seller_price")
            if (!detail?.forPair.isNullOrBlank() && !detail?.forPair.equals("0")) {
                Log.d("spare part", "is pair")
                productIsPair = true
                item_qty.text = "2"
            }
            if (intent.hasExtra(Constant.Key.wishList) && intent.getStringExtra(Constant.Key.wishList) != null) {
               wish_list = intent.getStringExtra(Constant.Key.wishList)
                if (wish_list == "1")
                    Iv_favorite.setImageResource(R.drawable.ic_heart)
                else {
                    Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                }
            } else {
                Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
            }

            Iv_favorite.setOnClickListener {

                add_remove_product__Wishlist()

            }
            if (detail?.couponList != null && detail?.couponList?.size != 0) {
                detail?.selectedProductCouponId = (detail?.couponList?.get(0)?.id.toString())
                AppliedCouponName_SP.text = (detail?.couponList?.get(0)?.couponTitle)
                AppliedCouponName_SP.visibility = View.VISIBLE
                CouponLabel_SP.visibility = View.INVISIBLE
                offerBadge_SP.visibility = View.VISIBLE
            } else {
                CouponLabel_SP.visibility = View.GONE
                AppliedCouponName_SP.visibility = View.GONE
                offerBadge_SP.visibility = View.GONE
                detail?.selectedProductCouponId = ""
            }

            offerBadge_SP.setOnClickListener {
                if (detail?.couponList != null && detail?.couponList?.size != 0) {

                    displayCoupons(detail?.couponList as MutableList<Models.Coupon>, "workshop_coupon", AppliedCouponName_SP)
                }
            }
            loadImage(json.optString("brand_image_url"), item_brand_image)
            val cartItem = Models.CartItem(name = detail?.productName.toString(),
                    description = detail?.Productdescription,
                    price = price?.replace(",", "")?.toDouble() ?: 0.0,
                    type = Constant.type_product,
                    productDetail = detail)


            Log.d("ProductDetailActivity", "initViews: adding item = $cartItem")



            add_product_to_cart.setOnClickListener {
                Log.e("Sparepart Add to cart", "onClickCall:yes")
                cartItem.quantity = item_qty.text.toString().toInt()

                cartItem.finalPrice = cartItem.price * cartItem.quantity
                try {
                    addToCartProducts(selectedProductID.toString(), cartItem?.quantity.toString(), "0.0",
                            if (detail?.selectedProductCouponId != null && !detail?.selectedProductCouponId.equals("null")) detail?.selectedProductCouponId else "", price!!, cartItem?.finalPrice.toString(), "0.0", "1", detail!!.usersId, if (cartItem?.name != null) cartItem?.name!! else "",

                            cartItem?.description!!, detail!!.usersId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            buy_product_with_assembly.setOnClickListener {
                Log.e("Cartassembly", productDetails.toString())
                if (productDetails != null) {
                    cartItem.quantity = item_qty.text.toString().toInt()

                    cartItem.finalPrice = cartItem.price * cartItem.quantity
                    cartItem?.Deliverydays = Deliverydays
                    Log.e("assembly", cartItem.finalPrice.toString())

                    val myIntent = intentFor<WorkshopListActivity>(
                            Constant.Key.is_workshop to true,
                            Constant.Key.is_assembly_service to true,
                            Constant.Path.productId to selectedProductID,
                            Constant.Key.productDetail to productDetails?.toString(),
                            Constant.Key.cartItem to cartItem
                    )
                    startActivity(myIntent)
                }
            }

            itemQty(cartItem)


        }


    }

    private fun add_remove_product__Wishlist() {
        try {
            if (wish_list == "0") {
                RetrofitClient.client.addToFavorite(getBearerToken()
                        ?: "", detail?.id.toString(), "1", "", getSelectedCar()?.carVersionModel?.idVehicle
                        ?: "").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite.setImageResource(R.drawable.ic_heart)

                                wish_list = "1"


                                showInfoDialog(getString(R.string.SuccessfullyaddedthisWorkshopfavorite))


                            }

                        }

                    }
                }

            } else {

                RetrofitClient.client.removeFromFavorite(getBearerToken()
                        ?: "", detail?.id.toString(), "", "1").onCall { networkException, response ->

                    response.let {
                        val body = response?.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)
                            showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }

                        if (response?.isSuccessful!!) {
                            val body = JSONObject(body)
                            if (body.has("message")) {
                                Iv_favorite!!.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                                wish_list = "0"
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

    private fun itemQty(cartItem: Models.CartItem): String {
        var qty_text = 1
        if (productIsPair)
            qty_text = 2
        btn_qty_increment.setOnClickListener {
            if (cartItem.productDetail?.productsQuantiuty.isNullOrBlank() || cartItem.productDetail?.productsQuantiuty.equals("0") || cartItem.productDetail?.productsQuantiuty.equals("")) {
                qty_text = 0
            } else if (if (productIsPair) qty_text / 2 != cartItem.productDetail?.productsQuantiuty?.toInt() else qty_text != cartItem.productDetail?.productsQuantiuty?.toInt()) {

                if (productIsPair) {
                    qty_text += 2
                    item_qty.text = (qty_text).toString()
                    productTotalPrices.text = getString(R.string.tyre_price_total, (cartItem.price.toFloat().toDouble().roundTo2Places() * qty_text.toDouble()).toDouble().roundTo2Places().toString())
                    if (!min_price.equals("")) {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (min_price.toDouble() * qty_text).toString())})"
                    }

                } else {
                    qty_text += 1
                    item_qty.text = qty_text.toString()

                    productTotalPrices.text = getString(R.string.tyre_price_total, (cartItem.price.toFloat().toDouble().roundTo2Places() * qty_text.toDouble()).toDouble().roundTo2Places().toString())
                    if (!min_price.equals("")) {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (min_price.toDouble() * qty_text).toString())})"
                    }
                }
            }


        }
        btn_qty_decrement.setOnClickListener {
            if (if (productIsPair) qty_text > 2 else qty_text > 1) {

                if (productIsPair) {
                    qty_text -= 2
                    item_qty.text = (qty_text).toString()
                    if (!min_price.equals("")) {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (min_price.toDouble() * qty_text).toString())})"
                    }
                    productTotalPrices.text = getString(R.string.tyre_price_total, (cartItem.price.toFloat().toDouble().roundTo2Places() * qty_text.toDouble()).toDouble().roundTo2Places().toString())

                } else {
                    qty_text -= 1
                    item_qty.text = qty_text.toString()
                    productTotalPrices.text = getString(R.string.tyre_price_total, (cartItem.price.toFloat().toDouble().roundTo2Places() * qty_text.toDouble()).toDouble().roundTo2Places().toString())
                    if (!min_price.equals("")) {
                        buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (min_price.toDouble() * qty_text).toString())})"
                    }
                }
            }

        }
        return if (productIsPair) (item_qty.text.toString().trim().toInt() * 2).toString() else item_qty.text.toString().trim()
    }

    private fun loadProductDetails() {

        var productDetailsString = intent.getStringExtra(Constant.Path.productDetails)
        var productPrice: Double? = 0.0

        if (productDetailsString != null)
            productDetails = JSONObject(productDetailsString)

        if (productDetails != null) {
            selectedProductID = productDetails!!.optInt("id")

            if (detail?.productName == null) {
                product_name.text = ""
                detail?.productName = ""
            } else {
                product_name.text = detail?.productName.toString()
            }
            if (detail?.Productdescription == null) {
                product_description.text = ""
                detail?.Productdescription = ""
            } else {
                product_description.text = detail?.Productdescription.toString()
            }
            Log.d("product name", detail?.productName)

            Log.d("product Description", detail?.Productdescription)

            if (!productDetails?.optString("seller_price").isNullOrBlank() && !productDetails?.optString("seller_price").equals("null") && !productDetails?.optString("seller_price").equals("0") && !productDetails?.optString("seller_price").equals("0.0")) {
                productPrice = productDetails?.optString("seller_price")?.toDouble()?.roundTo2Places()
            } else {
                productPrice = 0.0
            }
            Log.v("PRODUCT", "DETAILS **************************** $productDetails" + "\n--" + productPrice + "--" +
                    intent.hasExtra(Constant.Path.productType) + "--" + intent.getStringExtra(Constant.Path.productType).equals("tyre", true))

            product_price.text = getString(R.string.tyreprice_text, productPrice.toString())
            if (productIsPair) {
                if (productPrice != null) {
                    productTotalPrices.text = getString(R.string.tyre_price_total, (productPrice * 2).toString())
                }
            } else {
                productTotalPrices.text = getString(R.string.tyre_price_total, productPrice.toString())
            }

            if (productDetails!!.has("rating_star") && !productDetails!!.isNull("rating_star") && productDetails!!.getString("rating_star") != null && !productDetails!!.getString("rating_star").equals("")) {
                product_rating.rating = productDetails!!.getString("rating_star").toFloat()
            } else
                product_rating.rating = 0f

            if (productDetails!!.has("rating_count") && !productDetails!!.isNull("rating_count") && !productDetails!!.getString("rating_count").equals(""))
                ratingCount.text = productDetails!!.getString("rating_count")


            //Add slider images
            if (productDetails!!.has("images") && !productDetails!!.isNull("images") && productDetails!!.getJSONArray("images").length() > 0) {

                val imagesArray = productDetails!!.getJSONArray("images")
                setImageSlider(imagesArray)
            }





            if (detail?.productsQuantiuty.isNullOrBlank() || detail?.productsQuantiuty.equals("null") || detail?.productsQuantiuty.equals("0")) {
                add_product_to_cart.isEnabled = false
                buy_product_with_assembly.isEnabled = false

                item_qty.text = "0"
            } else if (productPrice == 0.0) {
                add_product_to_cart.isEnabled = false
                buy_product_with_assembly.isEnabled = false


            }
            Log.d("SparePart", "PartISAssemble:" + detail?.assembleStatus)

            if (detail?.assembleStatus.equals("Y")) {
                buy_product_with_assembly.visibility = View.VISIBLE
            } else {
                buy_product_with_assembly.visibility = View.GONE
            }


        }
        Log.d("SparePart", "JSON ***************** " + productDetails)

    }

    private fun setImageSlider() {
        //set slider
        image_slider.removeAllSliders()
        createImageSliderDialog()
        for (i in 0..1) {
            val imageRes = if (i % 2 == 0) R.drawable.no_image_placeholder else R.drawable.no_image_placeholder

            val slide = TextSliderView(this)
                    .image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .empty(R.drawable.no_image_placeholder)

            image_slider.addSlider(slide)

            val scaledSlide = DialogTouchImageSlider(this, imageRes)
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
    }

    private fun setImageSlider(imagesArray: JSONArray) {
        //set slider
        createImageSliderDialog()
        image_slider.removeAllSliders()

        for (i in 0 until imagesArray.length()) {
            //   val imageRes = Constant.itemImageBaseURL + imagesArray.getJSONObject(i).getString("image_name")
            val imageRes = imagesArray.getJSONObject(i).getString("image_url")
            val slide = TextSliderView(this).image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside).empty(R.drawable.no_image_placeholder)
            image_slider.addSlider(slide)

            val scaledSlide = DialogTouchImageSlider(this, R.drawable.no_image_placeholder)
                    .description("Description")
                    .image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .empty(R.drawable.no_image_placeholder)
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
    }

    private fun createImageSliderDialog() {

        imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)

        val slider = SliderLayout(this)

        slider.stopAutoCycle()
        slider.indicatorVisibility = PagerIndicator.IndicatorVisibility.Visible
        slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        slider.setCustomIndicator(PagerIndicator(this).apply {
//            setDefaultIndicatorColor(Color.BLACK, Color.GRAY)
//            indicatorVisibility =  PagerIndicator.IndicatorVisibility.Visible
//        })

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


    private fun loadProductDetailApi(id: String) {


        if (id.isEmpty()) {
            Log.e("ProductDetailActivity", "loadProductDetailApi: loadProductDetailApi , invalid id ")
            return
        }
        val dialog = getProgressDialog(true)

        RetrofitClient.client.productDetail(id)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        dialog.dismiss()
                        toast(getString(R.string.Failed_load_product_detail))
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            val body = response.body()?.string()
                            body?.let {
                                try {
                                    val productData = JSONObject(body)
                                    if (productData.has("data") && !productData.isNull("data")) {
                                        val data = JSONObject(JSONObject(it).getString("data"))
                                        min_price = data.getString("min_service_price")
                                        if (!min_price.isNullOrBlank()) {
                                            if (productIsPair)
                                                buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, (min_price.toDouble().roundTo2Places() * 2).toString())})"
                                            else buy_product_with_assembly.text = getString(R.string.buy_with_assembly) + " (${getString(R.string.prepend_euro_symbol_string, min_price)})"

                                        }
                                        Log.d("Product_Detail", "min_price" + min_price)
                                        if (data.has("number_of_delivery_days") && !data.getString("number_of_delivery_days").isNullOrBlank() && !data.getString("number_of_delivery_days").equals("null")) {

                                            ll_delivery_date.visibility = View.VISIBLE
                                            delivery_date.text = getDate(data.getString("number_of_delivery_days").toInt() + 1)
                                            Deliverydays = data.getString("number_of_delivery_days")
                                        } else {
                                            Deliverydays = ""
                                        }
                                    } else {
                                        Log.e("data is null", "${productData}")
                                    }
                                } catch (e: Exception) {
//                                    toast("Failed to load some product detail")
//                                finish()
                                    Log.e("ProductDetailActivity", "onResponse: onResponse", e)
                                }
                            }
                        } else showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again)) { finish() }
                    }


                })
    }

    override fun getFeedbackList(list: MutableList<Models.FeedbacksList>, feedbackwithoutPurchage: String) {
        bindFeedbackList(list, this,feedbackwithoutPurchage)
    }

    private fun displayCoupons(couponsList: MutableList<Models.Coupon>, couponType: String, AppliedCouponName: TextView) {
        val dialog = Dialog(this@ProductDetailActivity)
        val dialogView: View = LayoutInflater.from(this@ProductDetailActivity).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogView)
        val window: Window = dialog!!.window!!
        window.setDimAmount(0f)
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, 1200)//height shoud be fixed
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

                        if (items.offerType.equals("2")) {
                            holder.couponsAmount.text = getString(R.string.prepend_euro_symbol_string, items.amount.toString())
                        } else {
                            holder.couponsAmount.text = items.amount.toString() + getString(R.string.prepend_percentage_symbol)
                        }
                    }


                    holder.itemView.setOnClickListener {


                        if (!couponsList[position].id.isNullOrBlank()) {
                            detail?.selectedProductCouponId = (detail?.couponList?.get(position)?.id.toString())
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

        }

        dialog.show()
    }

    fun getSimilarProduct(productId: String) {
        val dialog = getProgressDialog(true)
        RetrofitClient.client.getSimilarProduct(getBearerToken()
                ?: "", getSelectedCar()?.carVersionModel?.idVehicle
                ?: "", "1", productId)
                .enqueue(
                        object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                dialog.dismiss()
                                toast(getString(R.string.Failed_load_product_detail))
                            }

                            @SuppressLint("SetTextI18n")
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                dialog.dismiss()
                                if (response.isSuccessful) {
                                    val body = response.body()?.string()
                                    body?.let {
                                        try {
                                            val productData = JSONObject(body)
                                            if (productData.has("data_set") && !productData.isNull("data_set")) {
                                                val jsonArray = JSONArray(JSONObject(it).getString("data_set"))
                                                val gson = GsonBuilder().create()
                                                var productOrWorkshopList: ArrayList<Models.ProductOrWorkshopList> = gson.fromJson(jsonArray.toString(), Array<Models.ProductOrWorkshopList>::class.java).toCollection(java.util.ArrayList<Models.ProductOrWorkshopList>())
                                                loadProductRecommendationGridList(product_recommendation_recycler_view, productOrWorkshopList)

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

}
