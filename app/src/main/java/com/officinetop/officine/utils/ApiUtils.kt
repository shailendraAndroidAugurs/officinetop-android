@file:Suppress("NOTHING_TO_INLINE")

package com.officinetop.officine.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.officinetop.officine.R
import com.officinetop.officine.adapter.CartItemAdapter
import com.officinetop.officine.car_parts.ProductDetailActivity
import com.officinetop.officine.car_parts.TyreDetailActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.feedback.FeedbackDetailActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.workshop.WorkshopBookingDetailsActivity
import kotlinx.android.synthetic.main.activity_shopping_cart_single_item_detail.view.*
import kotlinx.android.synthetic.main.activity_workshop_detail.*
import kotlinx.android.synthetic.main.item_grid_home_square.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_showfeedback.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
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

    loadImage("${Constant.imageBaseURL}$carImageURL", imageView, R.drawable.no_image_placeholder)


}


inline fun Context.loadImageProfile(url: String?, imageView: ImageView) {


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

if(!url.isNullOrBlank())
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
        if(!url.isNullOrBlank())
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                .load(url)
                .thumbnail(0.7f)
                .into(imageView)
    } catch (e: GlideException) {
    }
}


inline fun Context.loadImageWithName(name: String?, imageView: ImageView, placeholderImage: Int, overrideURL: String = "", baseURL: String = Constant.itemImageBaseURL) {
    try {
        var imageURL = ""

        if (overrideURL.isNotEmpty()) {
            imageURL = overrideURL
        } else {
            imageURL = baseURL + name
        }
        if(!imageURL.isNullOrBlank())
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                .load(imageURL).centerInside()
                .thumbnail(0.7f)
                .into(imageView)
    } catch (e: GlideException) {

    }
}

inline fun Context.loadImageFromDrawable(drawable: Int?, imageView: ImageView, placeholderImage: Int = R.drawable.summer_tyre) {
    try {
        Glide.with(this.applicationContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                .load(drawable)
                .thumbnail(0.7f)
                .into(imageView)

    } catch (e: GlideException) {

    }
}


inline fun Activity.loadProductRecommendationGridList(recyclerView: RecyclerView, similarProductList: ArrayList<Models.ProductOrWorkshopList>) {

    //product recycler
    recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

    recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_grid_home_square, p0, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int {
            return similarProductList.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            if (similarProductList[p1] != null) {

                p0.itemView.setOnClickListener {

                    startActivity(intentFor<ProductDetailActivity>(
                            Constant.Path.productDetails to similarProductList[p1].id, Constant.Key.wishList to similarProductList[p1].wish_list).forwardResults())
                    finish()

                }

                if (similarProductList[p1].images != null && similarProductList[p1].images?.size != 0)
                    loadImage(similarProductList[p1].images?.get(0)?.imageUrl, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                else if (!similarProductList[p1].profileImage.isNullOrBlank()) {
                    loadImage(Constant.profileBaseUrl + similarProductList[p1].profileImage, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                } else if (!similarProductList[p1].product_image_url.isNullOrBlank()) {
                    loadImage(similarProductList[p1].product_image_url, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                } else
                    loadImageWithName("", p0.itemView.item_icon, R.drawable.no_image_placeholder)
                p0.itemView.item_name.text = if (similarProductList[p1].productName != null) similarProductList[p1].productName else ""
                p0.itemView.item_sub_titleGrid.text = if (similarProductList[p1].Productdescription != null) similarProductList[p1].Productdescription else ""
            }

        }

    }

}


inline fun Activity.loadProductRecommendationGridListForTyre(recyclerView: RecyclerView, similarProductList: ArrayList<Models.TyreDetailItem>) {

    //product recycler
    recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

    recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_grid_home_square, p0, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int {
            return similarProductList.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            if (similarProductList[p1] != null) {
                p0.itemView.setOnClickListener {

                    startActivity(intentFor<TyreDetailActivity>(
                            Constant.Path.productDetails to similarProductList[p1],
                            Constant.Path.productType to "Tyre"))
                    finish()

                }
                if (similarProductList[p1].images != null && similarProductList[p1].images?.size != 0)
                    loadImage(similarProductList[p1].imageUrl, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                else
                    loadImageWithName("", p0.itemView.item_icon, R.drawable.no_image_placeholder)
                var tyreType = ""
                when (similarProductList[p1].type) {
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

                    p0.itemView.item_name.text = "${similarProductList[p1].manufacturer_description} ${tyreType} ${similarProductList[p1].pr_description}\n${similarProductList[p1].max_width}/${similarProductList[p1].max_aspect_ratio} R${similarProductList[p1].max_diameter}   ${if (similarProductList[p1].load_speed_index != null) similarProductList[p1].load_speed_index else ""} ${if (similarProductList[p1].speed_index != null) similarProductList[p1].speed_index else ""}"//

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                p0.itemView.item_sub_titleGrid.text = if (similarProductList[p1].ean_number != null) similarProductList[p1].ean_number else ""

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


fun calculateCartItemViews(view: View, context: Context?, cartData: Models.CartData) {
    var servicePrice = 0.0
    var productPrice = 0.0
    var productPriceMaintenance = 0.0
    var productPriceWithVatDiscount = 0.0
    var servicesPriceWithVatDiscount = 0.0
    var deliveryFees = 0.0
    var deliveryDatePredicted = ""
    var isServicesAvailable = false
    var isProductServicesAvailable = false
    var isProductAvailable = false
    var totalVat = 0.0
    var totalDiscount = 0.0
    var totalPFU = 0.0
    val cartDataList = cartData.CartDataList
    var isMultipleWorkshopAvailable = false
    var workshopId: String = ""
    var cartItemType = "3"// 0 only for product, 1 only for services , 2 only for services with product ,3 mix item
    for (i in 0 until cartDataList.size) {
        val cartData = cartDataList.get(i)
        if (cartData.CartType == "SP") {
            isServicesAvailable = true
            if (cartData.afterDiscountPrice != null && cartData.afterDiscountPrice != "null" && cartData.afterDiscountPrice != "")
                servicePrice += cartData.afterDiscountPrice.toDouble()




            if (cartData.serviceAssemblyProductDescription != null) {
                isProductServicesAvailable = true

                if (!cartData.serviceAssemblyProductDescription.totalPrice.isNullOrBlank() && cartData.serviceAssemblyProductDescription.totalPrice != "null")
                    productPrice += cartData.serviceAssemblyProductDescription.totalPrice.toDouble()

                if (!cartData.serviceAssemblyProductDescription.pfuTax.isNullOrBlank() && cartData.serviceAssemblyProductDescription.pfuTax != "0" && cartData.serviceAssemblyProductDescription.pfuTax != "0.0" && !cartData.serviceAssemblyProductDescription.productQuantity.isNullOrBlank() && cartData.serviceAssemblyProductDescription.productQuantity != "0") {
                    totalPFU += cartData.serviceAssemblyProductDescription.pfuTax.toDouble().roundTo2Places() * cartData.serviceAssemblyProductDescription.productQuantity.toInt()
                }
                if (cartData.serviceAssemblyProductDescription.finalOrderPrice != null && cartData.serviceAssemblyProductDescription.finalOrderPrice != "null" && cartData.serviceAssemblyProductDescription.finalOrderPrice != "")
                    productPriceWithVatDiscount += cartData.serviceAssemblyProductDescription.finalOrderPrice.toDouble()
                if (!cartData.serviceAssemblyProductDescription.discount.isNullOrBlank() && cartData.serviceAssemblyProductDescription.discount != "null")
                    totalDiscount += cartData.serviceAssemblyProductDescription.discount.toDouble()

                if (!cartData.serviceAssemblyProductDescription.product_vat.isNullOrBlank() && cartData.serviceAssemblyProductDescription.product_vat != "null")
                    totalVat += cartData.serviceAssemblyProductDescription.product_vat.toDouble()

            }

            if (cartData.serviceDetail != null && cartData.serviceDetail.mainCategoryId != null && cartData.serviceDetail.mainCategoryId.equals("12") &&
                    cartData.partDetails != null   && cartData.partDetails.size!=0 && cartData.partDetails[0] != null && !cartData.partDetails[0].sellerPrice.isNullOrBlank()) {

              if(cartData.partDetails[0].forPair!=null && cartData.partDetails[0].forPair.equals("")&&cartData.partDetails[0].forPair.equals("0") ){
                  productPriceMaintenance += cartData.partDetails[0].sellerPrice.toDouble().roundTo2Places() *2
                  productPrice+=cartData.partDetails[0].sellerPrice.toDouble().roundTo2Places() *2
              }else{
                  productPriceMaintenance += cartData.partDetails[0].sellerPrice.toDouble().roundTo2Places()
                  productPrice+=cartData.partDetails[0].sellerPrice.toDouble().roundTo2Places()
              }

            }
            if (!cartData.afterDiscountPrice.isNullOrBlank() && cartData.afterDiscountPrice != "null")
                servicesPriceWithVatDiscount += cartData.afterDiscountPrice.toDouble()




            if (!cartData.discount.isNullOrBlank() && cartData.discount != "null")
                totalDiscount += cartData.discount.toDouble()
            if (!cartData.serviceVat.isNullOrBlank() && cartData.serviceVat != "null")
                totalVat += cartData.serviceVat.toDouble()

            if (cartData.workshopDetail != null && !cartData.workshopDetail.id.isNullOrBlank()) {
                if (workshopId.isNullOrBlank()) {
                    workshopId = cartData.workshopDetail.id
                } else if (!workshopId.isNullOrBlank() && !cartData.workshopDetail.id.equals(workshopId)) {
                    isMultipleWorkshopAvailable = true
                }

            }


        } else if (cartData.CartType == "T" || cartData.CartType == "S") {
            isProductAvailable = true
            val bookingDate = SimpleDateFormat("yyy-MM-dd").parse(getDateFor(if (!cartData.deliveryDays.isNullOrBlank()) cartData.deliveryDays.toInt() else 0))
            if (deliveryDatePredicted.isNullOrBlank()) {
                val dateFormat = SimpleDateFormat("yyy-MM-dd")
                deliveryDatePredicted = dateFormat.format(bookingDate)
            } else {

                val deliveryDate = SimpleDateFormat("yyy-MM-dd").parse(deliveryDatePredicted)
                if (deliveryDate < bookingDate) {
                    val dateFormat = SimpleDateFormat("yyy-MM-dd")
                    deliveryDatePredicted = dateFormat.format(bookingDate)

                }
            }
            if (!cartData.totalPrice.isNullOrBlank() && cartData.totalPrice != "null")
                productPrice += cartData.totalPrice.toDouble()

            if (!cartData.pfuTax.isNullOrBlank() && cartData.pfuTax != "0" && cartData.pfuTax != "0.0" && !cartData.productQuantity.isNullOrBlank() && cartData.productQuantity != "0") {
                totalPFU += cartData.pfuTax.toDouble().roundTo2Places() * cartData.productQuantity.toInt()
            }

            if (cartData.finalOrderPrice != null && cartData.finalOrderPrice != "null" && cartData.finalOrderPrice != "")
                productPriceWithVatDiscount += cartData.finalOrderPrice.toDouble()

            if (!cartData.discount.isNullOrBlank() && cartData.discount != "null")
                totalDiscount += cartData.discount.toDouble()

            if (!cartData.ProductVat.isNullOrBlank() && cartData.ProductVat != "null")
                totalVat += cartData.ProductVat.toDouble()

        }

    }
    if (!isServicesAvailable) {
        view.Rl_deliveryDatePridicted.visibility = View.VISIBLE
        view.cart_Delivery_predictedDate.text = dateFormatChangeYearToMonth(deliveryDatePredicted)

    } else {
        view.Rl_deliveryDatePridicted.visibility = View.GONE
    }

    context?.let {
        Log.d("productPrices 12",productPrice.toString())


        view.cart_total_price.text = context.getString(R.string.prepend_euro_symbol_string, ((productPriceWithVatDiscount + servicesPriceWithVatDiscount + totalPFU+productPriceMaintenance) - totalDiscount).roundTo2Places().toString())

        view.cart_total_item_price.text = context.getString(R.string.prepend_euro_symbol_string, (productPrice.roundTo2Places() + totalPFU).roundTo2Places().toString())
        view.cart_total_service_price.text = context.getString(R.string.prepend_euro_symbol_string, servicePrice.roundTo2Places().toString())
        if (!cartData.deliveryPrice.isNullOrEmpty()) {
            view.rv_delivery_prices.visibility = View.VISIBLE
            view.tv_delivery_prices.visibility = View.VISIBLE
            deliveryFees = cartData.deliveryPrice.toDouble()

            //if someone booked only service in the cart so we will be not add delivery fee inside total amount.
            if (isServicesAvailable && !isProductServicesAvailable && !isProductAvailable) {
                view.tv_delivery_prices.text = context.getString(R.string.prepend_euro_symbol_string, "0")
                view.cart_total_price.text = context.getString(R.string.prepend_euro_symbol_string, ((view.cart_total_price.text.split(" ")[1].toDouble()).roundTo2Places().toString()))
            } else {
                view.tv_delivery_prices.text = context.getString(R.string.prepend_euro_symbol_string, deliveryFees.roundTo2Places().toString())
                view.cart_total_price.text = context.getString(R.string.prepend_euro_symbol_string, ((view.cart_total_price.text.split(" ")[1].toDouble() + cartData.deliveryPrice.toDouble()).roundTo2Places().toString()))

            }

        }

        if (isServicesAvailable && isProductServicesAvailable)
            cartItemType = "2" // only services+ product
        else if (!isServicesAvailable && isProductAvailable) {
            cartItemType = "0" // only product
        } else if (isServicesAvailable && !isProductServicesAvailable && !isProductAvailable) {
            cartItemType = "1" // only services
        } else "3"// mixed item in cart


    }
    context?.saveCartPricesData(totalVat.toString(), totalDiscount.toString(), totalPFU.toString(), cartItemType, isMultipleWorkshopAvailable)
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

    val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(resID, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int = jsonArray.length()

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            onBindViewHolder.invoke(holder.itemView, position, jsonArray.getJSONObject(position))

        }
    }

    this.adapter = adapter
    return adapter
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

        }

        override fun onResponse(call: Call<T>, response: Response<T>?) {
            if (response != null)
                onResponse.invoke(null, response)

        }
    })
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


fun Activity.getFeedback(getFeedback: OnGetFeedbacks, workshopId: String, productId: String, type: String, productType: String) {

    RetrofitClient.client.getfeedbackList(getBearerToken()
            ?: "", workshopId, productId, type, productType, getUserId())
            .onCall { networkException, response ->

                val feedbackList: MutableList<Models.FeedbacksList> = ArrayList()

                response?.let {
                    if (response.isSuccessful) {
                        feedbackList.clear()
                        val data = JSONObject(response.body()?.string())
                        if (data.has("data_set") && !data.isNull("data_set")) {
                            val dataSet = data.get("data_set") as JSONArray

                            for (i in 0 until dataSet.length()) {
                                val dataItems = dataSet.get(i) as JSONObject
                                val dataModels = Gson().fromJson<Models.FeedbacksList>(dataItems.toString(), Models.FeedbacksList::class.java)
                                feedbackList.add(dataModels)


                            }

                            /*bindFeedbackList()*/

                            getFeedback.getFeedbackList(feedbackList)

                        } else {
                            if (data.has("message") && !data.isNull("message")) {
                                // showInfoDialog(data.get("message").toString())

                            }
                        }
                    }
                }
            }
}

inline fun Activity.bindFeedbackList(list: MutableList<Models.FeedbacksList>, context: Context?) {
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
                    p0.itemView.tv_NameofProductorWorkshop.text = getString(R.string.concat)
                }
                if (!list[p1].profile_image.isNullOrBlank()) {
                    context?.loadImage(list[p1].profile_image, p0.itemView.Iv_UserImage)
                }
                if (!list[p1].rating.isNullOrBlank()) {
                    p0.itemView.ratngbar_ratingFeedback.rating = list[p1].rating.toFloat()
                } else {
                    p0.itemView.ratngbar_ratingFeedback.rating = 0.0f
                }



                if (!list[p1].lName.isNullOrBlank() && !list[p1].fName.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = list[p1].fName + " " + list[p1].lName
                } else if (!list[p1].fName.isNullOrBlank() && list[p1].lName.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = list[p1].fName
                } else if (list[p1].fName.isNullOrBlank() && !list[p1].lName.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = list[p1].lName
                } else {
                    p0.itemView.tv_userName.text = getString(R.string.concat)
                }
                if (list[p1].workshopId != null) {
                    p0.itemView.tv_product_type.text = getString(R.string.Workshop)
                } else if (list[p1].product_type == "1") {
                    p0.itemView.tv_product_type.text = getString(R.string.spare_part)
                } else if (list[p1].product_type == "2") {
                    p0.itemView.tv_product_type.text = getString(R.string.tyres)
                }




                if (!list[p1].createdAt.isNullOrBlank()) {
                    p0.itemView.tv_date.text = dateFormatChangeYearToMonth(list[p1].createdAt.split(" ")[0])
                } else {
                    p0.itemView.tv_date.text = getString(R.string.concat)
                }
                if (!list[p1].comments.isNullOrBlank()) {
                    p0.itemView.tv_userComment.text = list[p1].comments
                } else {
                    p0.itemView.tv_userComment.text = getString(R.string.concat)
                }

                if (list[p1].images != null && list[p1].images.isNotEmpty()) {


                    p0.itemView.rv_feedbackImage.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                        }

                        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                            val action: Int = e.action
                            when (action) {
                                MotionEvent.ACTION_MOVE ->
                                    rv.parent.requestDisallowInterceptTouchEvent(true)

                            }
                            return false
                        }

                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                        }
                    }

                    )
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

}


fun getUserDetail(loginUserDetail: OnGetLoginUserDetail, activity: Activity) {
    val progressDialog = activity.getProgressDialog(true)
    RetrofitClient.client.getUserDetails(activity.getBearerToken() ?: "")
            .onCall { networkException, response ->
                networkException.let {
                    progressDialog.dismiss()
                }
                response?.let {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val data = JSONObject(response.body()?.string())
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

    if (!context.isOnline()) {
        Snackbar.make(view, context.getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG)
                .show()
        progressDialog?.dismiss()
    }
    return context.isOnline()
}


inline fun View.bindCartItemViews(onCartListCallback: OnCartListCallback, context: Context?, shouldHideToolbar: Boolean = false, modelCart: Models.CartData) {

    if (context == null)
        return

    val adapter = CartItemAdapter(context, proceed_to_pay)
    adapter.setCartListener(onCartListCallback)

    for (i in 0 until modelCart.CartDataList.size) {
        adapter.addProduct(modelCart.CartDataList[i])
        adapter.getItemAt(i)
    }
    calculateCartItemViews(this@bindCartItemViews, context, modelCart)

    adapter.setOnItemChangedListener(object : CartItemAdapter.OnItemChanged {
        override fun onDeleted(position: Int) {

            calculateCartItemViews(this@bindCartItemViews, context, modelCart)
        }

        override fun onQuantityChanged(cartItem: Models.CartDataList?) {
            calculateCartItemViews(this@bindCartItemViews, context, modelCart)
        }

    })




    this.recycler_view.adapter = adapter
}

fun Context.getCartItemsList(context: Context?, onCartListCallback: OnCartListCallback, view: View) {

    view.progress_bar.visibility = View.VISIBLE

    RetrofitClient.client.getCartList(context!!.getBearerToken()
            ?: "", context.getOrderId())
            .onCall { networkException, response ->

                networkException?.let {
                    view.progress_bar.visibility = View.GONE

                }
                response?.let {
                    view.progress_bar.visibility = View.GONE
                    if (response.isSuccessful) {

                        try {
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set") && body.get("data_set") is JSONArray) {
                                val data = body.get("data_set") as JSONArray
                                Log.e("CartData", data.toString())
                                for (i in 0 until data.length()) {
                                    val modelCartList = Gson().fromJson<Models.CartItemList>(data.get(i).toString(), Models.CartItemList::class.java)
                                    val cartData = Gson().fromJson<Models.CartData>(data.get(i).toString(), Models.CartData::class.java)
                                    if (modelCartList != null) {

                                        for (element in modelCartList.spareProductDescription) {
                                            insertInCartList(element, "S", cartData)
                                        }

                                        for (element in modelCartList.tyreProductDescription) {
                                            insertInCartList(element, "T", cartData)
                                        }

                                        for (element in modelCartList.serviceProductDescription) {
                                            insertInCartList(element, "SP", cartData)

                                        }


                                        if (cartData.CartDataList != null && cartData.CartDataList.size > 0) {

                                            cartData.CartDataList.sortedWith(compareBy { it.createdAt })
                                            view.image_emptycart.visibility = View.GONE
                                            view.proceed_to_pay_container.visibility = View.VISIBLE
                                            view.bindCartItemViews(onCartListCallback, context, true, cartData)
                                            saveOrderId(cartData.id)
                                            saveIsAvailableDataInCart(true)

                                            if (cartData.userWallet.amount != null)
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
                                            view.bindCartItemViews(onCartListCallback, context, true, cartData)
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
    val data = Gson().toJson(modelData)
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


fun Activity.addToCartProducts(context: Context?, productId: String, productQuantity: String, pfuAmount: String, couponId: String?, price: String, totalPrice: String,
                               discount: String, productType: String, usersID: String, productName: String, productDescription: String, sellerId: String) {

    RetrofitClient.client.addToCartProduct(getBearerToken() ?: "", productId, productQuantity
            , pfuAmount, couponId, price, totalPrice, discount, productType, getOrderId(), "", sellerId,
            productName, productDescription, getSavedSelectedVehicleID(), getSelectedCar()?.carVersionModel?.idVehicle
            ?: "").onCall { networkException, response ->

        response?.let {
            val body = response.body()?.string()
            if (body.isNullOrEmpty() || response.code() == 401)
                showConfirmDialogForLogin(getString(R.string.PleaselogintocontinueforAddtocart), { moveToLoginPage(context) })
            if (response.isSuccessful) {
                try {
                    val responseData = JSONObject(body)

                    if (responseData.has("data") && !responseData.isNull("data")) {
                        if (responseData.get("data") is JSONObject) {
                            val data = responseData.get("data") as JSONObject
                            if (data.has("products_orders_id") && !data.isNull("products_orders_id")) {
                                saveOrderId(data.get("products_orders_id").toString())
                            }
                            logAddToCartEvent(context!!, productDescription, productId, productType, "USD", totalPrice.toDouble())
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


fun Context.addToFavoriteSendRequest(context: Context, productId: String, ProductType: String, Iv_favorite: ImageView, item: Models.TyreDetailItem? = null, workshopId: String = "", productorworkshopObject: Models.ProductOrWorkshopList? = null) {
    RetrofitClient.client.addToFavorite(context.getBearerToken()
            ?: "", productId, ProductType, workshopId, getSelectedCar()?.carVersionModel?.idVehicle
            ?: "").onCall { _, response ->

        response.let {
            val body = response?.body()?.string()
            if (body.isNullOrEmpty() || response.code() == 401)
                showConfirmDialog(getString(R.string.please_login_to_continue_for_add_wish_list)) { moveToLoginPage(context) }


            if (response?.isSuccessful!!) {
                val body = JSONObject(body)
                if (body.has("message")) {
                    Iv_favorite.setImageResource(R.drawable.ic_heart)

                    if (item != null) {
                        item.wish_list = "1"
                        logAddToWishListEvent(this, item.pr_description!!, productId, ProductType, "USD", if (!item.seller_price.isNullOrBlank()) item.seller_price.toDouble() else 0.0)

                    } else if (productorworkshopObject != null) {
                        productorworkshopObject.wish_list = "1"
                        logAddToWishListEvent(this, if (productorworkshopObject.productName.isNullOrBlank()) productorworkshopObject.companyName else productorworkshopObject.productName, productId, ProductType, "USD", if (!productorworkshopObject.sellerPrice.isNullOrBlank()) productorworkshopObject.sellerPrice.toDouble() else 0.0)

                    }

                    if (productId != null && productId != "")
                        showInfoDialog(getString(R.string.Successfully_addedProduct_to_wishlist))
                    else
                        showInfoDialog(getString(R.string.SuccessfullyaddedthisWorkshopfavorite))


                }

            }

        }
    }
}


fun Context.removeFromFavoriteSendRquest(context: Context, productId: String, Iv_favorite: ImageView, item: Models.TyreDetailItem? = null, workshopId: String = "", productorworkshopObject: Models.ProductOrWorkshopList? = null, productType: String = "") {
    RetrofitClient.client.removeFromFavorite(context.getBearerToken()
            ?: "", productId, workshopId, productType).onCall { _, response ->

        response.let {
            val body = response?.body()?.string()
            if (body.isNullOrEmpty() || response.code() == 401)
                showConfirmDialog(getString(R.string.please_login_to_continue_for_remove_wish_list)) { moveToLoginPage(context) }
            if (response?.isSuccessful!!) {
                val body = JSONObject(body)
                if (body.has("message")) {
                    Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                    if (item != null) {
                        item.wish_list = "0"
                    } else if (productorworkshopObject != null) {
                        productorworkshopObject.wish_list = "0"
                    }
                    if (!productId.isNullOrBlank())
                        showInfoDialog(getString(R.string.productRemoved_formWishList))
                    else
                        showInfoDialog(getString(R.string.WorkshopRemovedfromfavorite))

                }

            }

        }
    }
}




fun Context.getLocale(): Locale {
    var locale = Locale.getDefault()
    if (getLangLocale() != null && getLangLocale() != "") {
        locale = Locale(getLangLocale().toLowerCase())
    }
    return locale
}


fun dateFormatChangeYearToMonth(date: String): String? {
    var outputString = date
    val input = SimpleDateFormat("yyyy-MM-dd")
    val output = SimpleDateFormat("dd/MM/yyyy")
    try {
        val inputDateFormat = input.parse(date)                 // parse input
        outputString = (output.format(inputDateFormat))    // format output
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return outputString
}

fun Context.addReadMore(text: String, textView: TextView) {
    val ss = SpannableString(text.substring(0, 150) + getString(R.string.more_content))
    val clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            addReadLess(text, textView)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ds.color = resources.getColor(R.color.com_facebook_blue, theme)
            } else {
                ds.color = resources.getColor(R.color.com_facebook_blue)
            }
        }
    }
    ss.setSpan(clickableSpan, ss.length - getString(R.string.more_content).length, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    textView.text = ss
    textView.movementMethod = LinkMovementMethod.getInstance()
}

fun Context.addReadLess(text: String, textView: TextView) {
    val ss = SpannableString("$text " + getString(R.string.less))
    val clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            if (text.length >= 150) {
                addReadMore(text, textView)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ds.color = resources.getColor(R.color.red, theme)
            } else {
                ds.color = resources.getColor(R.color.red)
            }
        }
    }
    ss.setSpan(clickableSpan, ss.length - getString(R.string.less).length, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    textView.text = ss
    textView.movementMethod = LinkMovementMethod.getInstance()
}


inline fun Activity.checkpermission(permissionlist: ArrayList<String>, noinline onOkClick: (() -> Unit?)? = null, islocationpermission: Boolean = true) {
    try {
        Dexter.withActivity(this)
                .withPermissions(permissionlist)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                            onOkClick?.let { it1 -> it1() }
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // show alert dialog navigating to Settings

                            showSettingsDialog(islocationpermission)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Toast.makeText(applicationContext, getString(R.string.Unspecifiederroroccurred), Toast.LENGTH_SHORT).show()
                    }
                })
                /*   .onSameThread()*/
                .check()
    } catch (e: java.lang.Exception) {

    }


}

fun Activity.showSettingsDialog(isLocationPermission: Boolean) {
    val builder = AlertDialog.Builder(this)
    if (isLocationPermission) {
        builder.setTitle(getString(R.string.unauthorized_location_title))
        builder.setMessage(getString(R.string.unauthorized_location_message))
    } else {
        builder.setTitle(getString(R.string.need_permissions))
        builder.setMessage(getString(R.string.permission_denied))
    }

    builder.setPositiveButton(getString(R.string.goto_setting)) { dialogInterface, i ->

        dialogInterface.cancel()
        openSettings()

    }

    builder.setNegativeButton(getString(R.string.cancel)) { dialogInterface, i ->

        dialogInterface.cancel()


    }
    builder.show()
}

// navigating user to app settings
fun Activity.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivityForResult(intent, 101)
}

fun storagePermissionRequestList(): ArrayList<String> {
    var permissionlist = ArrayList<String>()
    permissionlist.add(Manifest.permission.ACCESS_FINE_LOCATION)
    permissionlist.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    return permissionlist
}

fun isLocationEnabled(mContext: Context): Boolean {
    val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
}


