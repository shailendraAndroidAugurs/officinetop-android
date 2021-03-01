package com.officinetop.officine.binding_adapters

import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.officinetop.officine.R
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.utils.*


@BindingAdapter("imageResource")
fun bindImages(imageView: ImageView, imageUrl: String) {
    if (!imageUrl.isNullOrBlank() && imageUrl.contains("http"))
        imageView.context.loadImage(imageUrl, imageView)
}

@BindingAdapter("imageLoad")
fun imageLoad(imageView: ImageView, imageUrl: String) {
    if (!imageUrl.isNullOrBlank() && imageUrl.contains("http"))
        imageView.context.loadImage(imageUrl, imageView)
    else imageView.visibility=View.GONE
}

@BindingAdapter("amount", "value")
fun bindPrice(textView: TextView, price: String, value: String) {
    if (!price.isNullOrBlank()) {
        var amount = price.toDouble().roundTo2Places()
        textView.text = (value + " " + amount.toString()).trim()

    }


}

@BindingAdapter("rating")
fun bindRating(ratingBar: RatingBar, rating: String) {
    if (!rating.isNullOrEmpty()) {
        ratingBar.rating = rating.toDouble().roundTo2Places().toFloat()
    }
}

@BindingAdapter("bindrating")
fun bindRating(ratingBar: CustomRatingBar, rating: String) {
    if (!rating.isNullOrEmpty()) {
        ratingBar.rating = rating.toDouble().roundTo2Places().toFloat()
    }
}


@BindingAdapter("date")
fun bindDate(date: TextView, dateText: String) {

    if (!dateText.isNullOrEmpty() && dateText != "0.0" && dateText != "0" && dateText != "-") {
        try {
            date.text = dateFormatChangeYearToMonth(dateText.split(" ")[0])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@BindingAdapter("htmlText")
fun bindhtmlText(date: TextView, dateText: String) {

    if (!dateText.isNullOrEmpty()) {
        try {
            date.text = Html.fromHtml(dateFormatChangeYearToMonth(dateText), Html.FROM_HTML_MODE_COMPACT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("dateformateText")
fun bindDateFormate(date: TextView, dateText: String) {

    if (!dateText.isNullOrEmpty()) {
        try {
            date.text = dateFormatChangeYearToMonth(dateText)?.let { parseServerDateTime(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@BindingAdapter("setVisibility")
fun setVisibility(text: TextView, flag: Boolean) {

    if (!flag) text.visibility = View.GONE
    else text.visibility = View.VISIBLE
}

@BindingAdapter("carModelName")
fun carModelName(textview: TextView, startTime: String) {

    if (startTime != null)
        textview.text = textview.context.getSelectedCar()?.carMakeName

}

@BindingAdapter("price")
fun price(textView: TextView, price: String) {
    var amount = 0.0
    if (!price.isNullOrBlank()) {
        amount = price.toDouble()
    }


    if (price.contains(".")) {
        amount = price.toDouble().roundTo2Places()
    }
    textView.text = textView.context.getString(R.string.prepend_euro_symbol_string, amount.toString().takeIf { !it.isNullOrEmpty() })
    textView.visibility = if (!amount.toString().isNullOrEmpty()) View.VISIBLE else View.GONE
}


@BindingAdapter("tyre_Order_price", "tyrePfu", "tyreQuantity")
fun setTyreOrderPrice(textView: TextView, price: String, tyrePfu: String, tyreQuantity: String) {
    var amount = 0.0
    if (!price.isNullOrBlank()) {
        amount = price.toDouble()
    }

    if (price.contains(".")) {
        amount = price.toDouble().roundTo2Places()
    }
    if (!tyrePfu.isNullOrBlank() && tyrePfu != "0" && !tyreQuantity.isNullOrBlank() && tyreQuantity != "0") {
        amount += tyrePfu.toDouble().roundTo2Places() * tyreQuantity.toInt()
    }


    textView.text = textView.context.getString(R.string.prepend_euro_symbol_string, amount.toDouble().roundTo2Places().toString().takeIf { !it.isNullOrEmpty() })
    textView.visibility = if (!amount.toString().isNullOrEmpty()) View.VISIBLE else View.GONE
}


@BindingAdapter("TicketStatus")
fun setTicketStatus(statustext: TextView, status: String) {

    if (!status.isNullOrEmpty()) {
        try {
            if (status == "A") {
                statustext.text = "Active"
                statustext.setTextColor(Color.parseColor("#2E7D32"))
            } else {
                statustext.text = "Closed"
                statustext.setTextColor(Color.parseColor("#DD2600"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@BindingAdapter("Quantity", "PairText")
fun setQuantity(text: TextView, status: String, pairText: String) {

    if (!status.isNullOrEmpty()) {
        try {
            if (status == "0") {
                text.text = ""
            } else {
                text.text = "2 $pairText"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@BindingAdapter("payment")
fun payment(textView: TextView, status: String) {

    if (status == "C") {
        textView.text = textView.context.getString(R.string.paid)
    } else {
        textView.text = textView.context.getString(R.string.pending)
    }
}

@BindingAdapter("CarKM")
fun carKM(textView: TextView, CarKM: String) {
    textView.text = textView.context.getString(R.string.carKM) + CarKM

}

@BindingAdapter("orderDelivered")
fun orderDelivered(linearLayout: LinearLayout, status: String) {
    if (!status.isNullOrBlank() && status != "null" && (status == "F" || status == "WC"))
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}

@BindingAdapter("orderReturn")
fun orderReturn(textview: TextView, status: String) {

    if (!status.isNullOrBlank() && status != "null" && status != null) {
        textview.visibility = View.VISIBLE
        if (status == "P") {
            textview.setText(R.string.return_request_inProcess)
            textview.setTextColor(Color.parseColor("#2E7D32"))
        } else if (status == "C") {
            textview.setTextColor(Color.parseColor("#2E7D32"))
            textview.setText(R.string.return_request_accepted)
        } else if (status == "CA") {
            textview.setTextColor(Color.parseColor("#DD2600"))

            textview.setText(R.string.return_request_rejected)

        }
    } else if (status == null) {
        textview.visibility = View.VISIBLE
    } else
        textview.visibility = View.GONE


}


@BindingAdapter("orderProgress")
fun orderProgress(linearLayout: LinearLayout, status: String) {
    if (!status.isNullOrBlank() && status != "null" && status != "-" && status != "F" && status != "WC")
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}

@BindingAdapter("feedbackvisibility", "feedbackStatus")
fun feedbackVisibility(linearLayout: LinearLayout, status: String, feedbackStatus: String) {
    if (feedbackStatus == "0" && (status == "F" || status == "WC"))
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE

}

@BindingAdapter("layoutvisibility")
fun layoutVisibility(linearLayout: LinearLayout, status: String) {
    if (status == "1")
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}


@BindingAdapter("Order_Quantity", "Ispair")
fun setQuantityForOrder(text: TextView, quantity: String, ispair: String) {

    if (!quantity.isNullOrEmpty()) {
        try {
            if (quantity == "0") {
                text.text = ""
            } else {
                if (!ispair.isNullOrBlank() && ispair != "0")
                    text.text = (quantity.toInt()
                            /** 2*/
                            ).toString()
                else
                    text.text = quantity
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@BindingAdapter("coupontitle", "coupontype", "couponPrices")
fun setCouponDetail(text: TextView, coupontitle: String, coupontype: String, couponPrices: String) {

    if(!coupontitle.isNullOrBlank() && !couponPrices.isNullOrBlank()&& !couponPrices.equals("-")){
        text.text = "$coupontitle : €$couponPrices"
    }else {
        text.text = ""
    }

}

@BindingAdapter("wishlist")
fun setWishList(Iv_favorite: ImageView, wish_list: String) {

    if (wish_list == "1")
        Iv_favorite.setImageResource(R.drawable.ic_heart)
    else {
        Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
    }

}

@BindingAdapter("orderStatus")
fun orderStatus(tv_ordersatatus: TextView, status: String) {
    var order = when (status) {
        "I" -> tv_ordersatatus.context.getString(R.string.inProgress)
        "D" -> tv_ordersatatus.context.getString(R.string.dispatched)
        "IN" -> tv_ordersatatus.context.getString(R.string.intransit)
        "F" -> tv_ordersatatus.context.getString(R.string.delivered)
        "P" -> tv_ordersatatus.context.getString(R.string.pending)
        "C" -> tv_ordersatatus.context.getString(R.string.confirmOrder)
        "WC" -> tv_ordersatatus.context.getString(R.string.workComplete)
        else -> ""
    }
    tv_ordersatatus.text = order

}
@BindingAdapter( "pairVisiblity", "pairText")
fun pairVisiblity(text: TextView, pairVisiblity: String, pairText: String) {

    if (!pairVisiblity.isNullOrEmpty() && !pairVisiblity.equals("0")) {
        try {
            text.visibility=View.VISIBLE
            text.text="2 $pairText"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }else text.visibility=View.GONE
}



