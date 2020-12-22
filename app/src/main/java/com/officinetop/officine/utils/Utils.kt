@file:Suppress("NOTHING_TO_INLINE")

package com.officinetop.officine.utils

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.InsetDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.IdRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.officinetop.officine.R
import com.officinetop.officine.data.getLangLocale
import kotlinx.android.synthetic.main.feedback_dialog_item.*
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.okButton
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

inline fun Context.getProgressDialog(shouldShow: Boolean = false): ProgressDialog {
    val progressDialog = ProgressDialog(this)
    progressDialog.setMessage(getString(R.string.Please_wait))
    progressDialog.setCancelable(false)
    progressDialog.window?.setDimAmount(0.8f)
    if (shouldShow)
        progressDialog.show()
    return progressDialog
}

inline fun Context.hideKeyboard() {
    val context = this
    val activity = context as Activity
    val windowToken = activity.window.decorView.rootView.windowToken
    val inputService = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputService.hideSoftInputFromWindow(windowToken, 0)
    Log.d("hidekeyboardcall", "yes")

}

inline fun Context.getRotateAnimation(): Animation = AnimationUtils.loadAnimation(this, R.anim.rotate)


inline fun Context.showInfoDialog(dialogMessage: String, cancelable: Boolean = true, noinline onOkClick: (() -> Unit?)? = null): AlertBuilder<DialogInterface> {
    val alert = alert {
        message = dialogMessage
        okButton { onOkClick?.let { it1 -> it1() } }
        isCancelable = cancelable
    }
    alert.show()
    return alert
}


inline fun Context.showConfirmDialog(dialogMessage: String, noinline onOkClick: (() -> Unit?)?): AlertBuilder<DialogInterface> {
    val alert = alert {
        message = dialogMessage
        positiveButton(getString(R.string.yes)) { onOkClick?.let { it1 -> it1() } }
        negativeButton(getString(R.string.no)) { }
    }
    alert.show()
    return alert
}

inline fun Context.showConnectionFailedDialog(noinline onOkClick: (() -> Unit?)?): AlertBuilder<DialogInterface> {
    val alert = alert {
        message = Constant.connection_failed_dialog
        positiveButton(getString(R.string.retry)) { onOkClick?.let { it1 -> it1() } }
        negativeButton(getString(R.string.cancel)) { }
    }
    alert.show()
    return alert
}


inline fun isEditTextValid(context: Context, vararg editText: EditText): Boolean {
    var isValid = true
    editText.forEach {
        if (it.text.isEmpty()) {
            isValid = false
            it.error = context.getString(R.string.FiledRequired)
        }
    }
    return isValid
}


inline fun getFormattedDate(timeInMillis: Long): String {
    return SimpleDateFormat("dd MMMM yyyy")
            .format(Date(timeInMillis))
}


inline fun Context.isOnline(): Boolean {

    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    //should check null because in airplane mode it will be null
    return netInfo != null && netInfo.isConnected
}


inline fun isAlphaNumeric(string: String): Boolean {
    val pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{7,}$"
    //"^[a-zA-Z0-9]*$"
    return Pattern.matches(pattern, string)
}

inline fun Dialog.makeRound(context: Context) {
    val insetDrawable = InsetDrawable(ContextCompat.getDrawable(context, R.drawable.rounded_white_background), 30)
    window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
    window?.setBackgroundDrawable(insetDrawable)
}

inline fun Spinner.setSampleSpinnerAdapter(context: Context, optionalList: List<String>? = null) {


    val adapter = ArrayAdapter<String>(context, R.layout.spinner_list_item, optionalList
            ?: Constant.sample_array)
    this.adapter = adapter
}


inline fun parseTimeHHmmss(timeString: String): Date {
    return try {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(timeString)
    } catch (e: Exception) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeString)
    }
}


inline fun parseServerDateTime(timeString: String): String {

    return try {
        val date = SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.getDefault()).parse(timeString)
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(date)

    } catch (e: Exception) {
        SimpleDateFormat("dd-mm-yyyy", Locale.getDefault()).format(timeString)
    }
}


inline fun getCurrentTime(): String {
    val date: Date = Date()
    val strDateFormat = "hh:mm"
    val dateFormat: DateFormat = SimpleDateFormat(strDateFormat, Locale.ITALY)
    val currentTime = dateFormat.format(date)
    return currentTime
}


inline fun parseTimeHHmmssInCalendar(timeString: String): Calendar {
    return try {
        val date = SimpleDateFormat("HH:mm:SS", Locale.getDefault()).parse(timeString)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar
    } catch (e: Exception) {
        val date = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeString)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar
    }
}

inline fun Intent.printValues(className: String = "Utils") {
    val bundle = extras
    if (bundle != null) {
        val keys = bundle.keySet()
        val it = keys.iterator()
        Log.d(className, "----- printValues: printing intent values -----> bundle = $bundle")
        while (it.hasNext()) {
            val key = it.next()
            Log.d("$className: printValues", "[" + key + "=" + bundle.get(key) + "]")
        }
        Log.d(className, "----- printValues: printing intent values completed -----")
    }
}

inline fun Intent.forwardResults(): Intent {
    this.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
    addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
    return this
}

inline fun getIntegerStringList(offlimit: Int, isProductSellOnpair: Boolean = false): List<String> {
    val list: MutableList<String> = ArrayList()
    if (isProductSellOnpair) {
        for (i in 1..offlimit / 2) {
            list.add((i * 2).toString())
            // Log.d("cartItemAdapter","IntegerStringList : isProductSellOnpair: "+list.toString())
        }

    } else {
        for (i in 1..offlimit) {
            list.add(i.toString())
        }
    }

    return list.toList()
}


inline fun Double.roundTo2Places() = BigDecimal(this).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()


fun View.snack(message: String, left: Int = 20, top: Int = 0, right: Int = 20, bottom: Int = 10, duration: Int = Snackbar.LENGTH_SHORT, actionText: String = context.getString(R.string.retry), onActionClicked: (() -> Unit?)? = null) {
    Snackbar.make(this, message, duration).apply {

        val params = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(left, top, right, bottom)
        params.gravity = Gravity.BOTTOM
        params.anchorGravity = Gravity.BOTTOM

        view.layoutParams = params

        onActionClicked?.let {
            setAction(actionText) {
                onActionClicked.invoke()
            }
        }
        show()
    }

    fun sort(array: IntArray = intArrayOf()): IntArray {

        for (i in 1 until array.size) {
            val key: Int = array[i]
            var j = i - 1
            while (j >= 0 && key < array[j]) {
                array[j + 1] = array[j]
                j -= 1
            }
            array[j + 1] = key
        }
        return array
    }

}

inline fun Any.convertToJsonString(): String {
    return try {
        Gson().toJson(this) ?: ""
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("Extensions", "convertToJsonString: ${e.message}")
        "{}"
    }
}

fun getSubString(str: String, index: Int): Int {
    var position: Int = 0
    var indexValue: Int = 0
    for (i in 0 until str.length) {
        val ch: Char = str[i]
        if (ch == ' ') {
            position += 1
        }
        if (index == 2)
            if (position == 2) {
                indexValue = i
            }
        if (index == 6)
            if (position == 6) {
                indexValue = i
            }
    }
    return indexValue
}


fun Context?.hasArgs(): Boolean {

    return try {
        val data = this?.getSharedPreferences("file", Context.MODE_PRIVATE)?.getString("res", "{}")
                ?: "{}"
        JSONObject(data).optBoolean("com.officinetop.officine", false)
    } catch (e: Exception) {
        false
    }

}

fun Bundle?.printValues() {

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

        if (this == null || this.isEmpty)
            Log.d("Fragment", "No values in bundle")
        else
            Log.d("Fragment", "Bundle values: " + this.convertToJsonString())
    }
}


fun FragmentManager?.setPlacePicker(
        context: Context, @IdRes resId: Int = R.id.autocomplete_fragment,
        onSelected: (place: Place?, error: Status?) -> Unit
): AutocompleteSupportFragment {

    val fragment = this?.findFragmentById(resId) as AutocompleteSupportFragment
    try {
        Places.initialize(context, context.getString(R.string.google_places_key))
        Places.createClient(context)
        fragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.PLUS_CODE))

        fragment.setHint(context.getString(R.string.Search_Location))

        fragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                Log.e("EditProfileFragment", "onPlaceSelected: $p0")
                onSelected(p0, null)
            }

            override fun onError(p0: Status) {
                Log.e("EditProfileerrFragment", "onError: $p0")
                onSelected(null, p0)
            }

        })
    } catch (e: Exception) {
        e.printStackTrace(); }

    return fragment
}

fun Context?.setAppLanguage() {
    val languageCode = this?.getLangLocale() ?: ""
    val resources: Resources = this?.resources!!
    val dm: DisplayMetrics = resources.displayMetrics
    val config: Configuration = resources.configuration
    if (!languageCode.isNullOrEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(Locale(languageCode.toLowerCase()))
        } else {
            config.locale = Locale(languageCode.toLowerCase())
        }
        resources.updateConfiguration(config, dm)

    }

}

fun Context?.createImageSliderDialog(imageUrl: String) {

    val imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)

    with(imageDialog) {
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        setContentView(R.layout.feedback_dialog_item)

        window?.setGravity(android.view.Gravity.TOP)
        window?.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
        context.loadImage(imageUrl, iv_feedbackImage)
        create()
        show()
    }
}


fun Context.movetologinPage(context: Context?) {
    startActivity(intentFor<com.officinetop.officine.authentication.LoginActivity>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    var activity = context as Activity
    activity.finishAffinity()
}


/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logAddToCartEvent(context: Context, contentData: String, contentId: String, contentType: String, currency: String, price: Double) {
    var params = Bundle()
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
    params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency)
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, price, params)
}

/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logAddPaymentInfoEvent(context: Context, success: Boolean) {
    var params = Bundle()
    params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, if (success) 1 else 0)
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO, params)
}


/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logAddToWishlistEvent(context: Context, contentData: String, contentId: String, contentType: String, currency: String, price: Double) {
    var params = Bundle()
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
    params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency)
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_WISHLIST, price, params)
}


/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logCompleteRegistrationEvent(context: Context, registrationMethod: String) {
    val params = Bundle()
    params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, registrationMethod)
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params)
}

/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logContactEvent(context: Context) {
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_CONTACT)
}

/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logFindLocationEvent(context: Context) {
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_FIND_LOCATION)
}


/**
 * logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 * purchaseAmount is BigDecimal, e.g. BigDecimal.valueOf(4.32).
 * currency is Currency, e.g. Currency.getInstance("USD"),
 *     where the string is from http://en.wikipedia.org/wiki/ISO_4217.
 * parameters is Bundle.
 */

fun logPurchageEvent(context: Context, purchaseAmount: BigDecimal, currency: Currency, parameters: Bundle) {
    var logger = AppEventsLogger.newLogger(context)
    logger.logPurchase(purchaseAmount, currency, parameters)

}

/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logRateEvent(context: Context, contentType: String, contentData: String, contentId: String, maxRatingValue: Int, ratingGiven: Double) {
    var params = Bundle()
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
    params.putInt(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, maxRatingValue)
    var logger = AppEventsLogger.newLogger(context)
    logger.logEvent(AppEventsConstants.EVENT_NAME_RATED, ratingGiven, params)
}

/**
 * This function assumes logger is an instance of AppEventsLogger and has been
 * created using AppEventsLogger.newLogger() call.
 */
fun logSearchEvent(context: Context, contentType: String, contentData: String, contentId: String, searchString: String, success: Boolean) {
    var params = Bundle()
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
    params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
    params.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, searchString)
    params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, if (success) 1 else 0)
    var logger = AppEventsLogger.newLogger(context)

    logger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, params)
}


inline fun Context.showConfirmDialogforPayment(dialogMessage: String, noinline onOkClick: (() -> Unit?)?, noinline onNoClick: (() -> Unit?)?): AlertBuilder<DialogInterface> {
    val alert = alert {
        message = dialogMessage
        positiveButton(getString(R.string.yes)) { onOkClick?.let { it1 -> it1() } }
        negativeButton(getString(R.string.no)) { onNoClick?.let { noclick -> noclick() } }
    }
    alert.show()
    return alert
}

inline fun Context.createImageDialog(imageRes: String): Dialog {
    val imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)
    val slider = ImageView(this)
    loadImage(imageRes, slider)
    slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    with(imageDialog) {
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        setContentView(slider)
        window?.setGravity(android.view.Gravity.TOP)
        window?.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
        create()
        return imageDialog

    }
}