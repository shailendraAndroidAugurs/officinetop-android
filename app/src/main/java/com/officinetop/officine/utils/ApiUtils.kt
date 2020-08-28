@file:Suppress("NOTHING_TO_INLINE")

package com.officinetop.officine.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestOptions
import com.daimajia.slider.library.Indicators.PagerIndicator
import com.daimajia.slider.library.SliderLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.officinetop.officine.R

import com.officinetop.officine.adapter.CartItemAdapter
import com.officinetop.officine.car_parts.ProductDetailActivity
import com.officinetop.officine.car_parts.TyreDetailActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.feedback.FeedbackDetailActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.views.DialogTouchImageSlider
import com.officinetop.officine.workshop.WorkshopBookingDetailsActivity
import kotlinx.android.synthetic.main.activity_shopping_cart_single_item_detail.view.*
import kotlinx.android.synthetic.main.activity_workshop_detail.*
import kotlinx.android.synthetic.main.include_toolbar.view.*
import kotlinx.android.synthetic.main.item_grid_home_square.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_showfeedback.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

inline fun Context.loadCarImage(defaultCarImage: String, brandID: String?, imageView: ImageView) {
    var carImageURL = defaultCarImage

    if (carImageURL.trim().isEmpty() || carImageURL == ("default.jpg")) {

        carImageURL = "$brandID.png"
    }

    //Log.d("inline", "loadCarImage: url = $carImageURL")

//    try { if((this as Activity).isFinishing) return } catch(e: Exception) { }

    loadImage("${Constant.imageBaseURL}$carImageURL", imageView, R.drawable.no_image_placeholder)


}


inline fun Context.loadImageprofile(url: String?, imageView: ImageView) {


    try {
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_no_userprofile).error(R.drawable.ic_no_userprofile))
                .load(url)
                .thumbnail(0.7f)
                .into(imageView)
    } catch (e: Exception) {
        e.printStackTrace()
    }


}


inline fun Context.loadImage(url: String?, imageView: ImageView) {
    try {
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(R.drawable.no_image_placeholder).error(R.drawable.no_image_placeholder))
                .load(url)
                .thumbnail(0.7f)
                .into(imageView)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

inline fun Context.loadImage(url: String?, imageView: ImageView, placeholderImage: Int = R.drawable.no_image_placeholder) {
    try {

        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                .load(url)
                .thumbnail(0.7f)
                .into(imageView)
    } catch (e: GlideException) {
        //Log.e("ApiUtils", "loadImage: loadImage: " + e.rootCauses.toString())
    }
}


inline fun Context.loadImageWithName(name: String?, imageView: ImageView, placeholderImage: Int, overrideURL: String = "", baseURL: String = Constant.itemImageBaseURL) {
    try {
        var imageURL = baseURL + name

        if (overrideURL.isNotEmpty()) {
            imageURL = overrideURL
        }

        //Log.v("IMG ", "URL &&&&&&&&&&&&&&&&&&&&&&&& $imageURL , filename = $name")
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                .load(imageURL)
                .thumbnail(0.7f)
                .into(imageView)
        /*Glide.with(context)
                    .load(imageRes)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .fitCenter()
                    .placeholder(mPlaceholderImage)
                    .into(imageView)*/
    } catch (e: GlideException) {
        //Log.e("ApiUtils", "loadImage: loadImage: " + e.rootCauses.toString())
    }
}

inline fun Context.loadImageFromDrawable(drawable: Int?, imageView: ImageView, placeholderImage: Int = R.drawable.summer_tyre) {
    try {
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                .load(drawable)
                .thumbnail(0.7f)
                .into(imageView)
        /*Glide.with(context)
                    .load(imageRes)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .fitCenter()
                    .placeholder(mPlaceholderImage).
                    .into(imageView)*/
    } catch (e: GlideException) {
        //Log.e("ApiUtils", "loadImage: loadImage: " + e.rootCauses.toString())
    }
}


inline fun Context.deleteCar(carID: String, crossinline reloadList: () -> String) {

    alert {
        message = getString(R.string.Delete_car_from_virtual_garage)
        positiveButton(getString(R.string.yes)) {
            getBearerToken()?.let {
                val progressDialog = getProgressDialog()
                progressDialog.show()
                RetrofitClient.client.deleteCar(carID, it).enqueue(object : Callback<Models.MyCar> {
                    override fun onFailure(call: Call<Models.MyCar>, t: Throwable) {
                        progressDialog.dismiss()
                        toast(t.message!!)
                    }

                    override fun onResponse(call: Call<Models.MyCar>, response: Response<Models.MyCar>) {
                        val responseString = response.body()?.toString()
                        progressDialog.dismiss()
                        Log.d("deleteCar", "onResponse: $responseString")

                        if (isStatusCodeValid(responseString)) {
                            toast(getString(R.string.Cardeletedfromvirtualgarage))
                            getDataSetArrayFromResponse(responseString)
                            reloadList()
                        }
                    }

                })
            }
        }
        noButton { }
    }
            .show()


}


inline fun Activity.loadProductRecommendationGridList(recyclerView: RecyclerView, SimilarproductList: ArrayList<Models.ProductOrWorkshopList>) {

    //product recycler
    recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

    recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_grid_home_square, p0, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int {
            return SimilarproductList.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            if (SimilarproductList[p1] != null) {

                p0.itemView.setOnClickListener {

                    startActivity(intentFor<ProductDetailActivity>(
                            Constant.Path.productDetails to Gson().toJson(SimilarproductList[p1]), Constant.Key.wishList to SimilarproductList[p1].wish_list).forwardResults())
                    finish()

                }



                if (SimilarproductList[p1].images != null && SimilarproductList[p1].images?.size != 0)
                    loadImage(SimilarproductList[p1].images?.get(0)?.imageUrl, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                else if (!SimilarproductList[p1].profileImage.isNullOrBlank()) {
                    loadImage(Constant.profileBaseUrl + SimilarproductList[p1].profileImage, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                } else if (!SimilarproductList[p1].product_image_url.isNullOrBlank()) {
                    loadImage(SimilarproductList[p1].product_image_url, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                } else
                    loadImageWithName("", p0.itemView.item_icon, R.drawable.no_image_placeholder)
                p0.itemView.item_name.text = if (SimilarproductList[p1].productName != null) SimilarproductList[p1].productName else ""
                p0.itemView.item_sub_titleGrid.text = if (SimilarproductList[p1].Productdescription != null) SimilarproductList[p1].Productdescription else ""
            }

        }

    }

}


inline fun Activity.loadProductRecommendationGridListForTyre(recyclerView: RecyclerView, SimilarproductList: ArrayList<Models.TyreDetailItem>) {

    //product recycler
    recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

    recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_grid_home_square, p0, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int {
            return SimilarproductList.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            if (SimilarproductList[p1] != null) {
                p0.itemView.setOnClickListener {

                    startActivity(intentFor<TyreDetailActivity>(
                            Constant.Path.productDetails to SimilarproductList[p1],
                            Constant.Path.productType to "Tyre"))
                    finish()

                }




                if (SimilarproductList[p1].images != null && SimilarproductList[p1].images?.size != 0)
                    loadImage(SimilarproductList[p1].imageUrl, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                else
                    loadImageWithName("", p0.itemView.item_icon, R.drawable.no_image_placeholder)
                var tyreType = ""
                when (SimilarproductList[p1].type) {
                    "s" -> {

                        tyreType = getString(R.string.Car)
                    }
                    "w" -> {

                        tyreType = getString(R.string.Car)
                    }
                    "g" -> {

                        tyreType = getString(R.string.Car)
                    }
                    "m" -> {

                        tyreType = getString(R.string.Wheel_Quad_tyre)
                    }
                    "o" -> {

                        tyreType = getString(R.string.Off_road_tyre)
                    }
                    "i" -> {

                        tyreType = getString(R.string.Truck)
                    }
                    "a" -> {

                        tyreType = getString(R.string.All)
                    }


                }
                try {

                    p0.itemView.item_name.text = "${SimilarproductList[p1].manufacturer_description} ${tyreType} ${SimilarproductList[p1].pr_description}\n${SimilarproductList[p1].max_width}/${SimilarproductList[p1].max_aspect_ratio} R${SimilarproductList[p1].max_diameter}   ${if (SimilarproductList[p1].load_speed_index != null) SimilarproductList[p1].load_speed_index else ""} ${if (SimilarproductList[p1].speed_index != null) SimilarproductList[p1].speed_index else ""}"//

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }




                p0.itemView.item_sub_titleGrid.text = if (SimilarproductList[p1].ean_number != null) SimilarproductList[p1].ean_number else ""

            }
        }

    }

}


inline fun convertToJson(model: Any): String {
    return try {
        Gson().toJson(model)
    } catch (e: Exception) {
        ""
    }
}




inline fun addeditemcart(context: Context?, shouldHideToolbar: Boolean = false): Int? {
    val cartProducts = context?.getCartItems()
    if (cartProducts?.size == 0) {


    }
    return cartProducts?.size
}

fun calculateCartViews(view: View, context: Context?) {
    var servicePrice = 0.0
    var productPrice = 0.0

    context?.getCartItems()?.forEachWithIndex { _, map ->
        // Log.d("bindCarts", "bindCartViews: item = $map")

        map.entries.forEach {

            val item = it.value
            /*val serviceModel = item.serviceModel*/

            when {
                item.type == Constant.type_product -> productPrice += item.finalPrice
                item.type == Constant.type_workshop -> servicePrice += item.serviceModel?.finalPrice
                        ?: 0.0
                else -> {
                    productPrice += item.finalPrice
                    servicePrice += item.serviceModel?.finalPrice ?: 0.0
                }
            }
        }
    }

    context?.let {
        view.cart_total_price.text = context.getString(R.string.prepend_euro_symbol_string, (productPrice + servicePrice).roundTo2Places().toString())
        view.cart_total_item_price.text = context.getString(R.string.prepend_euro_symbol_string, productPrice.roundTo2Places().toString())
        view.cart_total_service_price.text = context.getString(R.string.prepend_euro_symbol_string, servicePrice.roundTo2Places().toString())
    }
}

fun calculateCartItemViews(view: View, context: Context?, cartDataList: ArrayList<Models.CartDataList>) {
    var servicePrice = 0.0
    var productPrice = 0.0
    var productPricewithVat_Discount = 0.0
    var ServicesPricewithVat_Discount = 0.0
    var deliveryDatePridicted = ""
    var IsServicesAvailable = false
    var Totalvat = 0.0
    var TotalDiscount = 0.0
    var TotalPFU = 0.0
    for (i in 0 until cartDataList.size) {
        val cartData = cartDataList.get(i)
        if (cartData.CartType.equals("SP")) {
            IsServicesAvailable=true
            if (cartData.afterDiscountPrice != null && !cartData.afterDiscountPrice.equals("null") && !cartData.afterDiscountPrice.equals(""))
                servicePrice += cartData.afterDiscountPrice.toDouble()




            if (cartData.serviceAssemblyProductDescription != null) {


                if (!cartData.serviceAssemblyProductDescription.totalPrice.isNullOrBlank() && !cartData.serviceAssemblyProductDescription.totalPrice.equals("null"))
                    productPrice += cartData.serviceAssemblyProductDescription.totalPrice.toDouble()

                if (!cartData.serviceAssemblyProductDescription.pfuTax.isNullOrBlank() && !cartData.serviceAssemblyProductDescription.pfuTax.equals("0") && !cartData.serviceAssemblyProductDescription.pfuTax.equals("0.0") && !cartData.serviceAssemblyProductDescription.productQuantity.isNullOrBlank() && !cartData.serviceAssemblyProductDescription.productQuantity.equals("0")) {
                    TotalPFU += cartData.serviceAssemblyProductDescription.pfuTax.toDouble().roundTo2Places() * cartData.serviceAssemblyProductDescription.productQuantity.toInt()
                }
                if (cartData.serviceAssemblyProductDescription.finalOrderPrice != null && !cartData.serviceAssemblyProductDescription.finalOrderPrice.equals("null") && !cartData.serviceAssemblyProductDescription.finalOrderPrice.equals(""))
                    productPricewithVat_Discount += cartData.serviceAssemblyProductDescription.finalOrderPrice.toDouble()
                if (!cartData.serviceAssemblyProductDescription.discount.isNullOrBlank() && !cartData.serviceAssemblyProductDescription.discount.equals("null"))
                    TotalDiscount += cartData.serviceAssemblyProductDescription.discount.toDouble()

                if (!cartData.serviceAssemblyProductDescription.product_vat.isNullOrBlank() && !cartData.serviceAssemblyProductDescription.product_vat.equals("null"))
                    Totalvat += cartData.serviceAssemblyProductDescription.product_vat.toDouble()
            }
            if (!cartData.afterDiscountPrice.isNullOrBlank() && !cartData.afterDiscountPrice.equals("null"))
                ServicesPricewithVat_Discount += cartData.afterDiscountPrice.toDouble()




            if (!cartData.discount.isNullOrBlank() && !cartData.discount.equals("null"))
                TotalDiscount += cartData.discount.toDouble()
            if (!cartData.serviceVat.isNullOrBlank() && !cartData.serviceVat.equals("null"))
                Totalvat += cartData.serviceVat.toDouble()


        } else if (cartData.CartType.equals("T") || cartData.CartType.equals("S")) {

            var bookingDate = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(if (!cartData.deliveryDays.isNullOrBlank()) cartData.deliveryDays.toInt() + 1 else 0))
            if (deliveryDatePridicted.isNullOrBlank()) {
                var dateFormat = SimpleDateFormat("yyy-MM-dd");
                deliveryDatePridicted = dateFormat.format(bookingDate);
            } else {

                var deliveryDate = SimpleDateFormat("yyy-MM-dd").parse(deliveryDatePridicted)
                if (deliveryDate < bookingDate) {
                    var dateFormat = SimpleDateFormat("yyy-MM-dd");
                    deliveryDatePridicted = dateFormat.format(bookingDate);

                }
            }
            if (!cartData.totalPrice.isNullOrBlank() && !cartData.totalPrice.equals("null"))
                productPrice += cartData.totalPrice.toDouble()

            if (!cartData.pfuTax.isNullOrBlank() && !cartData.pfuTax.equals("0") && !cartData.pfuTax.equals("0.0") && !cartData.productQuantity.isNullOrBlank() && !cartData.productQuantity.equals("0")) {
                TotalPFU += cartData.pfuTax.toDouble().roundTo2Places() * cartData.productQuantity.toInt()
            }

            if (cartData.finalOrderPrice != null && !cartData.finalOrderPrice.equals("null") && !cartData.finalOrderPrice.equals(""))
                productPricewithVat_Discount += cartData.finalOrderPrice.toDouble()

            if (!cartData.discount.isNullOrBlank() && !cartData.discount.equals("null"))
                TotalDiscount += cartData.discount.toDouble()

            if (!cartData.ProductVat.isNullOrBlank() && !cartData.ProductVat.equals("null"))
                Totalvat += cartData.ProductVat.toDouble()

        }

    }
    if (!IsServicesAvailable) {
        view.Rl_deliveryDatePridicted.visibility = View.VISIBLE
        view.cart_Delivery_predictedDate.text = DateFormatChangeYearToMonth(deliveryDatePridicted)

    } else {
        view.Rl_deliveryDatePridicted.visibility = View.GONE
    }
    context?.saveCartPricesData(Totalvat.toString(), TotalDiscount.toString(), TotalPFU.toString())
    context?.let {
        view.cart_total_price.text = context.getString(R.string.prepend_euro_symbol_string, ((productPricewithVat_Discount + ServicesPricewithVat_Discount + TotalPFU) - TotalDiscount).roundTo2Places().toString())
        view.cart_total_item_price.text = context.getString(R.string.prepend_euro_symbol_string, (productPrice.roundTo2Places() + TotalPFU).roundTo2Places().toString())
        view.cart_total_service_price.text = context.getString(R.string.prepend_euro_symbol_string, servicePrice.roundTo2Places().toString())
    }
}


inline fun RecyclerView.loadImagesRecycler(context: Context?, image_urls: List<String>?, isHorizontal: Boolean = true) {

    if (isHorizontal)
        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image, p0, false)) {}
        }

        override fun getItemCount(): Int = image_urls?.size ?: 5

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val imageView = p0.itemView.item_image_view

            image_urls?.let {
                context?.loadImage(it[p1], imageView, R.drawable.no_image_placeholder)
            }

            if (context == null)
                return

            imageView.setOnClickListener {
                val imageDialog = Dialog(context, R.style.DialogSlideAnimStyle)

                val slider = SliderLayout(context)

                slider.stopAutoCycle()
                slider.indicatorVisibility = PagerIndicator.IndicatorVisibility.Visible
                slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                with(imageDialog) {
                    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                    setContentView(slider)

                    window?.setGravity(android.view.Gravity.TOP)
                    window?.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT)
                    window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
                    create()
                    show()
                }

                image_urls?.forEach { url ->

                    val scaledSlide = DialogTouchImageSlider(context, R.drawable.ic_battery)
                            .description("Description")
                            .image(url)
                            .empty(R.drawable.no_image_placeholder)
                    slider.addSlider(scaledSlide)
                }

                slider.setCurrentPosition(p1, true)

                if (image_urls?.isNullOrEmpty() != false)
                    slider.addSlider(DialogTouchImageSlider(context, R.drawable.no_image_placeholder)
                            .description("Description")
                            .image("")
                            .empty(R.drawable.no_image_placeholder))
            }

        }

    }
}

inline fun Call<ResponseBody>.onCall(context: Context? = null, crossinline onResponse: (networkException: Throwable?, response: Response<ResponseBody>?) -> Unit) {
    this.enqueue(object : Callback<ResponseBody> {

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            onResponse.invoke(t, null)

            context?.let { context.showInfoDialog(context.getString(R.string.ConnectionErrorPleaseretry)) }
        }

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            onResponse.invoke(null, response)

        }

    })
}


inline fun RecyclerView.setJSONArrayAdapter(context: Context, jsonArray: JSONArray, resID: Int, crossinline onBindViewHolder:
(itemView: View, position: Int, jsonObject: JSONObject) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Log.d("FragmentHomeeee", "onResponse Best Selling Products: -- context $context \n jsonArray $jsonArray \n resID $resID")

    val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(resID, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int = jsonArray.length()

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            onBindViewHolder.invoke(holder.itemView, position, jsonArray.getJSONObject(position))

        }

        /*override fun getItemId(position: Int): Long {
            return jsonArray.get(position).

        }*/
    }

    this.adapter = adapter
    return adapter
}


//JSON Extensions

inline fun JSONArray.forEach(action: (jsonObject: JSONObject) -> Unit) {
    for (i in 0 until length()) {
        action(getJSONObject(i))
    }
}


inline fun JSONArray.forEachNames(action: (name: String) -> Unit) {
    for (i in 0 until length()) {
        action(getJSONObject(i).optString("name"))
    }
}

inline fun JSONArray.forEachIndexed(action: (i: Int, jsonObject: JSONObject) -> Unit) {
    for (i in 0 until length()) {
        action(i, getJSONObject(i))
    }
}

inline fun JSONArray.isEmpty() = this.length() == 0


inline fun String.toRequestBody(): RequestBody {
    return RequestBody.create(MediaType.parse("text/plain"), this)
}

inline fun File.toMultipartBody(key: String): MultipartBody.Part {
    val requestBody = MultipartBody.create(MediaType.parse("multipart/form-data"), this)
    return MultipartBody.Part.createFormData(key, this.name, requestBody)
}


sealed class GenericResult<T> {
    data class Success<T>(val call: Call<T>, val response: Response<T>) : GenericResult<T>()
    data class Failure<T>(val call: Call<T>, val error: Throwable) : GenericResult<T>()
}

inline fun <reified T> Call<T>.enqueue(crossinline result: (GenericResult<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, error: Throwable) {
            result(GenericResult.Failure(call, error))
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            result(GenericResult.Success(call, response))
        }
    })
}


inline fun <reified T> Call<T>.genericAPICall(crossinline onResponse: (networkException: Throwable?, response: Response<T>?) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, error: Throwable) {
            onResponse.invoke(error, null)
//            result(GenericResult.Failure(call,error))
        }

        override fun onResponse(call: Call<T>, response: Response<T>?) {
            if (response != null)
                onResponse.invoke(null, response)
//            result(GenericResult.Success(call, response))
        }
    })
}

inline fun Activity.hasLocationPermission(): Boolean {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            true
        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constant.REQUEST_PERMISSIONS_LOCATION)
            false
        }
    } else true
}

inline fun Activity.hasStoragePermission(): Boolean {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            true
        else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constant.RP_STORAGE)
            false
        }
    } else true
}


fun getDate(days: Int): String? {
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    val cal = Calendar.getInstance()
    cal.time = Date()//current date.
    cal.add(Calendar.DATE, days)
    return sdf.format(cal.time)//new date
}

fun getDateFor(days: Int): String? {
    val sdf = SimpleDateFormat("yyy-MM-dd")
    val cal = Calendar.getInstance()
    cal.time = Date()//current date.
    cal.add(Calendar.DATE, days)
    return sdf.format(cal.time)//new date
}

const val unspecified_error = "Unspecified error"

suspend fun <T : Any> safeAPICall(call: suspend () -> Response<T>): T {
    val response = try {
        call.invoke()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        val message = if (e is ConnectException || e is SocketTimeoutException ||
                e is HttpException ||
                e.message?.contains("unexpected end of stream on Connection") == true ||
                e is SocketException
        )
            "Connection Error" else unspecified_error
        val responseError = ResponseError(message, 500).convertToJsonString()
        Log.e("safeAPICall", "safeAPICall: error thrown = $responseError")

        throw IOException(responseError)
    }



    if (response.isSuccessful) {
        return response.body()!!
    } else {
        val error = response.errorBody()?.string()

        error?.let {
            val message = JSONObject(it).optString("message", "Something went wrong")
            val responseError = ResponseError(message, response.code())
            throw IOException(responseError.convertToJsonString())

        }
        throw IOException(ResponseError("Something went wrong. Please try again.", 500).convertToJsonString())
    }
}

data class ResponseError(val message: String, val errorCode: Int)


fun Activity.getFeedbacks(getfedback: OnGetFeedbacks, workshopId: String, productId: String, type: String, productType: String) {

    RetrofitClient.client.getfeedbackList(getBearerToken()
            ?: "", workshopId, productId, type, productType,getUserId())
            .onCall { networkException, response ->

                val feedbackList: MutableList<Models.FeedbacksList> = ArrayList()

                response?.let {
                    if (response.isSuccessful) {
                        feedbackList.clear()
                        val data = JSONObject(response?.body()?.string())
                        if (data.has("data_set") && !data.isNull("data_set")) {
                            val dataSet = data.get("data_set") as JSONArray

                            for (i in 0 until dataSet.length()) {
                                val dataItems = dataSet.get(i) as JSONObject
                                val dataModels = Gson().fromJson<Models.FeedbacksList>(dataItems.toString(), Models.FeedbacksList::class.java)
                                feedbackList.add(dataModels)


                            }

                            /*bindFeedbackList()*/

                            if (getfedback != null) {
                                getfedback.getFeedbackList(feedbackList,if(data.has("data") && !data.getInt("data").toString().isNullOrBlank())data.getInt("data").toString() else "1")
                            }

                        } else {
                            if (data.has("message") && !data.isNull("message")) {
                                // showInfoDialog(data.get("message").toString())

                            }
                        }
                    }
                }
            }
}

inline fun Activity.bindFeedbackList(list: MutableList<Models.FeedbacksList>, context: Context?,feedbackwithoutPurchage:String) {
    val feedbackList: MutableList<Models.FeedbacksList> = ArrayList()
    val feedbackItems = if (list.size > 5) 4 else list.size//to display only 4 items
    for (i in 0 until feedbackItems) {
        feedbackList.add(list.get(i))
    }
    if (fedback_recycler_list != null) {
        fedback_recycler_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)


        //workshop feedback
        fedback_recycler_list.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_showfeedback, p0, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun getItemCount(): Int = list.size

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

                if (!list[p1].ProductOrWorkshopName.isNullOrBlank()) {
                    p0.itemView.tv_NameofProductorWorkshop.text = list[p1].ProductOrWorkshopName


                } else {
                    p0.itemView.tv_NameofProductorWorkshop.text = getString(R.string.Concat)
                }
                if (!list[p1].profile_image.isNullOrBlank()) {
                    context?.loadImage(list[p1].profile_image, p0.itemView.Iv_UserImage)
                }
                if (!list[p1].rating.isNullOrBlank()) {
                    p0.itemView.ratngbar_ratingFeedback.rating = list[p1].rating.toFloat()
                } else {
                    p0.itemView.ratngbar_ratingFeedback.rating = 0.0f
                }



                if (!list[p1]?.lName.isNullOrBlank() && !list[p1]?.fName.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = list[p1]?.fName + " " + list[p1]?.lName
                } else if (!list[p1]?.fName.isNullOrBlank() && list[p1]?.lName.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = list[p1]?.fName.toString()
                } else if (list[p1]?.fName.isNullOrBlank() && !list[p1]?.lName.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = list[p1]?.lName.toString()
                } else {
                    p0.itemView.tv_userName.text = getString(R.string.Concat)
                }
                if (list[p1].workshopId != null) {
                    p0.itemView.tv_product_type.text = getString(R.string.Workshop)
                } else if (list[p1].product_type.equals("1")) {
                    p0.itemView.tv_product_type.text = getString(R.string.spare_part)
                } else if (list[p1].product_type.equals("2")) {
                    p0.itemView.tv_product_type.text = getString(R.string.tyres)
                }




                if (!list[p1].createdAt.isNullOrBlank()) {
                    p0.itemView.tv_date.text = DateFormatChangeYearToMonth(list[p1].createdAt.split(" ")[0])
                } else {
                    p0.itemView.tv_date.text = getString(R.string.Concat)
                }
                if (!list[p1].comments.isNullOrBlank()) {
                    p0.itemView.tv_userComment.text = list[p1].comments
                } else {
                    p0.itemView.tv_userComment.text = getString(R.string.Concat)
                }

                if (list[p1].images != null && list[p1].images.size != 0) {


                    p0.itemView.rv_feedbackImage.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                        }

                        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                            var action: Int = e.getAction();
                            when (action) {
                                MotionEvent.ACTION_MOVE ->
                                    rv.getParent().requestDisallowInterceptTouchEvent(true);

                            }
                            return false;
                        }

                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                        }
                    }

                    );
                    p0.itemView.rv_feedbackImage.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                            val view = layoutInflater.inflate(R.layout.item_image, p0, false)
                            return object : RecyclerView.ViewHolder(view) {}
                        }

                        override fun getItemCount(): Int = list[p1].images.size

                        override fun onBindViewHolder(view: RecyclerView.ViewHolder, position: Int) {
                            context?.loadImage(list[p1].images[position].imageUrl, view.itemView.item_image_view)

                            view.itemView.setOnClickListener {
                                context?.createImageSliderDialog(list[p1].images[position].imageUrl)

                            }
                        }
                    }


                } else {
                    p0.itemView.rv_feedbackImage.adapter = null
                }

                p0.itemView.setOnClickListener {

                    startActivity(intentFor<FeedbackDetailActivity>(
                            Constant.Path.type to if (list[p1].workshopId != null) "" else list[p1].product_type,
                            Constant.Path.model to Gson().toJson(list[p1]),
                            Constant.Path.ProductOrWorkshopName to list[p1].ProductOrWorkshopName

                    ))
                }

            }
        }
    }

    /* val genericAdapter = GenericAdapter<Models.FeedbacksList>(this, R.layout.feedback_items_layout)

     genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
         override fun onClick(view: View, position: Int) {
         }

         override fun onItemClick(view: View, position: Int) {

         }
     })
     val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
     fedback_recycler_list.layoutManager = linearLayoutManager
     fedback_recycler_list.adapter = genericAdapter
     genericAdapter.addItems(feedbackList)*/
}


fun getUserDetail(loginUserDetail: OnGetLoginUserDetail, activity: Activity) {
    var ProgressDialog = activity?.getProgressDialog(true)
    RetrofitClient.client.getUserDetails(activity.getBearerToken() ?: "")
            .onCall { networkException, response ->
                networkException.let {
                    ProgressDialog.dismiss()
                }
                response?.let {
                    if (response.isSuccessful) {
                        ProgressDialog.dismiss()
                        val data = JSONObject(response?.body()?.string())
                        if (data.has("data") && !data.isNull("data")) {
                            val dataSet = data.get("data") as JSONObject
                            var walletModels: Models.UserWallet? = null
                            val dataModels = Gson().fromJson<Models.UserDetailData>(dataSet.toString(), Models.UserDetailData::class.java)
                            if (dataSet.has("user_wallet") && !dataSet.isNull("user_wallet")) {
                                val datawallet = dataSet.getJSONObject("user_wallet")
                                walletModels = Gson().fromJson<Models.UserWallet>(datawallet.toString(), Models.UserWallet::class.java)
                                if (dataModels != null) {
                                    loginUserDetail.getUserDetailData(dataModels, walletModels)
                                }

                            } else {
                                if (dataModels != null) {
                                    loginUserDetail.getUserDetailData(dataModels, walletModels)
                                }
                            }
                        } else {
                            if (data.has("message") && !data.isNull("message")) {
                                activity.showInfoDialog(data.get("message").toString())
                            }
                        }
                    }
                }
            }
}

inline fun showOnlineSnack(progressDialog: ProgressDialog?, view: View, context: Context): Boolean {

    if (!context?.isOnline()!!) {
        Snackbar.make(view, context?.getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG)
                .show()
        progressDialog?.dismiss()
    }
    return context?.isOnline()!!
}


inline fun View.bindCartitemViews(onCartListCallback: OnCartListCallback, context: Context?, shouldHideToolbar: Boolean = false, modelCart: Models.CartData) {

    if (context == null)
        return

    val adapter = CartItemAdapter(context, proceed_to_pay)
    adapter.setCartListener(onCartListCallback)
    if (shouldHideToolbar)
    //toolbar.visibility = View.GONE
        Log.d("bindCarts", "bindCartViews: total items = ${modelCart.CartDataList.size}")

    for (i in 0 until modelCart.CartDataList.size) {
        adapter.addProduct(modelCart.CartDataList[i])
        adapter.getItemAt(i)

        calculateCartItemViews(this@bindCartitemViews, context, modelCart.CartDataList)

        adapter.setOnItemChangedListener(object : CartItemAdapter.OnItemChanged {
            override fun onDeleted(position: Int) {
                //Log.d("bindCart", "onDeleted: position = $position")
                calculateCartItemViews(this@bindCartitemViews, context, modelCart.CartDataList)
            }

            override fun onQuantityChanged(cartItem: Models.CartDataList?) {
                //Log.d("bindCart", "onQuantityChanged: item = $cartItem")
                calculateCartItemViews(this@bindCartitemViews, context, modelCart.CartDataList)
            }

        })


    }

    this.recycler_view.adapter = adapter
}

fun Context.getCartItemsList(context: Context?, onCartListCallback: OnCartListCallback, view: View) {

    view.progress_bar.visibility = View.VISIBLE

    RetrofitClient.client.getCartList(context!!.getBearerToken()
            ?: "", context!!.getOrderId())
            .onCall { networkException, response ->

                networkException?.let {
                    view.progress_bar.visibility = View.GONE

                }
                response?.let {
                    view.progress_bar.visibility = View.GONE
                    if (response.isSuccessful) {

                        try {
                            val body = JSONObject(response?.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set") && body.get("data_set") is JSONArray) {
                                val data = body.get("data_set") as JSONArray
                                Log.e("CartData", data.toString())
                                for (i in 0 until data.length()) {
                                    val modelCartList = Gson().fromJson<Models.CartItemList>(data.get(i).toString(), Models.CartItemList::class.java)
                                    val cartData = Gson().fromJson<Models.CartData>(data.get(i).toString(), Models.CartData::class.java)
                                    if (modelCartList != null) {

                                        for (n in 0 until modelCartList.spareProductDescription.size) {
                                            insertInCartList(modelCartList.spareProductDescription[n], "S", cartData)
                                        }

                                        for (n in 0 until modelCartList.tyreProductDescription.size) {
                                            insertInCartList(modelCartList.tyreProductDescription[n], "T", cartData)
                                        }

                                        for (n in 0 until modelCartList.serviceProductDescription.size) {
                                            insertInCartList(modelCartList.serviceProductDescription[n], "SP", cartData)
                                        }


                                        if (cartData.CartDataList != null && cartData.CartDataList.size > 0) {

                                            cartData.CartDataList.sortedWith(compareBy({ it.createdAt }))
                                            view.image_emptycart.visibility = View.GONE
                                            view.proceed_to_pay_container.visibility = View.VISIBLE
                                            view.bindCartitemViews(onCartListCallback, context, true, cartData)
                                            saveOrderId(cartData.id)
                                            saveIsAvailableDataInCart(true)
                                            Log.d("WalletAount", cartData.userWallet.amount)
                                            if (cartData.userWallet != null && cartData.userWallet.amount != null)
                                                SaveUserWallet(cartData.userWallet.amount)
                                            else {
                                                SaveUserWallet("0")
                                                Log.d("WalletAount", "0")
                                            }
                                            if (!cartData.vat_Admin.isNullOrBlank())
                                                view.tv_totalamount.text = getString(R.string.Total_order_inciva22, cartData.vat_Admin) + "%)"
                                            else {
                                                view.tv_totalamount.text = getString(R.string.Total_order)

                                            }
                                        } else {
                                            cartData.CartDataList = ArrayList()

                                            view.bindCartitemViews(onCartListCallback, context, true, cartData)
                                            view.image_emptycart.visibility = View.VISIBLE
                                            view.proceed_to_pay_container.visibility = View.GONE
                                            saveIsAvailableDataInCart(false)
                                        }
                                    }

                                }
                            } else {

                                view.proceed_to_pay_container.visibility = View.GONE
                                view.image_emptycart.visibility = View.VISIBLE
                            }

                            view.progress_bar.visibility = View.GONE
                        } catch (e: java.lang.Exception) {
                            Log.e("Error", e.toString())
                            e.printStackTrace()
                        }
                    } else {
                        context.showInfoDialog(context.getString(R.string.Failed_to_load_data))
                    }


                }
            }
}

fun insertInCartList(modelData: Any, cartType: String, modelCart: Models.CartData): ArrayList<Models.CartDataList> {
    var data = Gson().toJson(modelData)
    val modelCartList1 = Gson().fromJson<Models.CartDataList>(data.toString(), Models.CartDataList::class.java)
    modelCartList1.CartType = cartType
    if (modelCart.CartDataList == null) {
        modelCart.CartDataList = ArrayList()
        modelCart.CartDataList.add(modelCartList1)
    } else {
        modelCart.CartDataList.add(modelCartList1)
    }


    return modelCart.CartDataList
}


fun Activity.addToCartProducts(productId: String, productQuantity: String, pfuAmount: String, couponId: String?, price: String, totalPrice: String,
                               discount: String, productType: String, usersID: String, productName: String, productDescription: String, sellerId: String) {

    RetrofitClient.client.addToCartProduct(getBearerToken() ?: "", productId, productQuantity
            , pfuAmount, couponId, price, totalPrice, discount, productType, getOrderId(), "", sellerId,
            productName, productDescription, getSavedSelectedVehicleID(), getSelectedCar()?.carVersionModel?.idVehicle
            ?: "").onCall { networkException, response ->

        response?.let {
            val body = response.body()?.string()
            if (body.isNullOrEmpty() || response.code() == 401)
                showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }

            if (response.isSuccessful) {
                try {
                    val responseData = JSONObject(body)

                    if (responseData.has("data") && !responseData.isNull("data")) {
                        if (responseData.get("data") is JSONObject) {
                            val data = responseData.get("data") as JSONObject
                            if (data.has("products_orders_id") && !data.isNull("products_orders_id")) {
                                saveOrderId(data.get("products_orders_id").toString())
                            }
                        }
                    }

                    if (responseData.has("message") && !responseData.isNull("message")) {
                        showInfoDialog(responseData.get("message").toString(), false) {
                            startActivity(intentFor<WorkshopBookingDetailsActivity>())
                            finish()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

            }

        }
    }
}


fun Context.AddToFavoritesendRquest(context: Context, productId: String, ProductType: String, Iv_favorite: ImageView, item: Models.TyreDetailItem? = null, workshopId: String = "", productorworkshopObject: Models.ProductOrWorkshopList? = null) {
    RetrofitClient.client.addToFavorite(context.getBearerToken()
            ?: "", productId, ProductType, workshopId, getSelectedCar()?.carVersionModel?.idVehicle
            ?: "").onCall { networkException, response ->

        response.let {
            val body = response?.body()?.string()
            if (body.isNullOrEmpty() || response.code() == 401)
                showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }


            if (response?.isSuccessful!!) {
                val body = JSONObject(body)
                if (body.has("message")) {
                    Iv_favorite.setImageResource(R.drawable.ic_heart)

                    if (item != null) {
                        item?.wish_list = "1"
                    } else if (productorworkshopObject != null) {
                        productorworkshopObject?.wish_list = "1"
                    }

                    if (productId != null && !productId.equals(""))
                        showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))
                    else
                        showInfoDialog(getString(R.string.SuccessfullyaddedthisWorkshopfavorite))


                }

            }

        }
    }
}


fun Context.RemoveFromFavoritesendRquest(context: Context, productId: String, Iv_favorite: ImageView, item: Models.TyreDetailItem? = null, workshopId: String = "", productorworkshopObject: Models.ProductOrWorkshopList? = null, productType: String = "") {
    RetrofitClient.client.removeFromFavorite(context.getBearerToken()
            ?: "", productId, workshopId, productType).onCall { networkException, response ->

        response.let {
            val body = response?.body()?.string()
            if (body.isNullOrEmpty() || response.code() == 401)
                showInfoDialog(getString(R.string.Pleaselogintocontinuewithslotbooking), true) { movetologinPage() }

            if (response?.isSuccessful!!) {
                val body = JSONObject(body)
                if (body.has("message")) {
                    Iv_favorite!!.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                    if (item != null) {
                        item?.wish_list = "0"
                    } else if (productorworkshopObject != null) {
                        productorworkshopObject?.wish_list = "0"
                    }
                    if (productId != null && !productId.equals(""))
                        showInfoDialog(getString(R.string.productRemoved_formWishList))
                    else
                        showInfoDialog(getString(R.string.WorkshopRemovedfromfavorite))

                }

            }

        }
    }
}

fun Context.RemoveFromFavoritesendRquestForWorkShop(context: Context, productId: String, Iv_favorite: ImageView, item: Models.ProductOrWorkshopList, workshopId: String = "") {
    RetrofitClient.client.removeFromFavorite(context.getBearerToken()
            ?: "", productId, workshopId, "").onCall { networkException, response ->

        response.let {
            if (response?.isSuccessful!!) {
                val body = JSONObject(response?.body()?.string())
                if (body.has("message")) {

                    Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                    item.wish_list = "0"


                }

            }

        }
    }
}

inline fun getLastLocation(mFusedLocationClient: FusedLocationProviderClient, activity: Activity) {

    mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result != null) {

                    task.result

                } else {
                    Log.w("SOSActivity", "getLastLocation:exception", task.exception)

                }
            }

}

fun Context.getLocale(): Locale {
    var locale = Locale.getDefault()
    if (getLangLocale() != null && !getLangLocale().equals("")) {
        locale = Locale(getLangLocale()?.toLowerCase())
    }
    return locale
}


fun DateFormatChangeYearToMonth(date: String): String? {
    var outputString = date
    val input = SimpleDateFormat("yyyy-MM-dd");
    val output = SimpleDateFormat("dd/MM/yyyy");
    try {
        val inputDateFormat = input.parse(date);                 // parse input
        outputString = (output.format(inputDateFormat));    // format output
    } catch (e: Exception) {
        e.printStackTrace();
    }
    return outputString
}




