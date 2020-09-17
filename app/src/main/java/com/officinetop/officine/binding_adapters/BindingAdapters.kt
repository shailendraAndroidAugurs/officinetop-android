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
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.utils.*


@BindingAdapter("imageResource")
fun bindImages(imageView: ImageView, imageUrl: String) {
    if (imageUrl.contains("https"))
        imageView.context.loadImage(imageUrl, imageView)
}

@BindingAdapter("amount", "value")
fun bindPrice(textView: TextView, price: String, value: String) {
    if (!price.isNullOrBlank()) {
        var amount = price.toDouble().roundTo2Places()
        textView.text = value + " " + amount.toString()

    }
    textView.visibility = if (!price.toString().isNullOrEmpty()) View.VISIBLE else View.GONE

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
            date.text = DateFormatChangeYearToMonth(dateText.split(" ")[0])
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
            date.text = Html.fromHtml(DateFormatChangeYearToMonth(dateText), Html.FROM_HTML_MODE_COMPACT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("dateformateText")
fun bindDateformate(date: TextView, dateText: String) {

    if (!dateText.isNullOrEmpty()) {
        try {
            date.text = DateFormatChangeYearToMonth(dateText)?.let { parseServerDateTime(it) }
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

@BindingAdapter("time")
fun settime(textview: TextView, startTime: String) {
    if (startTime != null) {
        val starttime = startTime.split("-")[0]
        val endtime = startTime.split("-")[1]
        if (starttime == "null" && endtime == "") {
            textview.text = ""

        } else {
            if (starttime != "null" && endtime != "null") {
                textview.text = "${starttime.removeSuffix(":00")}  -  ${endtime.removeSuffix(":00")}"
            } else if (starttime == "null" && endtime != "null") {
                textview.text = "${endtime.removeSuffix(":00")}"
            } else if (starttime != "null" && endtime == "null") {
                textview.text = "${starttime.removeSuffix(":00")}"
            }
        }
    }


}

@BindingAdapter("carModelName")
fun CarModelName(textview: TextView, startTime: String) {

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
fun settyreOrderprice(textView: TextView, price: String, tyrePfu: String, tyreQuantity: String) {
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
        textView.text = textView.context.getString(R.string.Paid)
    } else {
        textView.text = textView.context.getString(R.string.Pending)
    }
    /* var amount = price.toDouble() ?: 0.0
     if (price.contains(".")) {
         amount = price.toDouble().roundTo2Places()
     }
     textView.text = textView.context.getString(R.string.prepend_euro_symbol_string, amount.toString().takeIf { !it.isNullOrEmpty() })
     textView.visibility = if (!amount.toString().isNullOrEmpty()) View.VISIBLE else View.GONE*/
}

@BindingAdapter("CarKM")
fun CarKM(textView: TextView, CarKM: String) {
    textView.text = textView.context.getString(R.string.carKM) + CarKM

}

@BindingAdapter("orderDelivered")
fun orderDelivered(linearLayout: LinearLayout, status: String) {
    if (!status.isNullOrBlank() && status != "null" && status == "F")
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

@BindingAdapter("InvoiceRequest")
fun InvoiceRequest(textview: TextView, status: String) {
    textview.visibility = View.VISIBLE
    if (status.isNullOrBlank() || status == "-" || status == "p") {
        textview.text = R.string.request_invoice.toString()
        textview.setTextColor(Color.parseColor("#2E7D32"))
    } else if (status == "c") {
        textview.setTextColor(Color.parseColor("#2E7D32"))
        textview.text = R.string.invoice_Request_inProcess.toString()
    }


}


@BindingAdapter("orderReturnbutton")
fun orderReturnbutton(textview: TextView, status: String) {

    if (!status.isNullOrBlank() && status != "null") {
        textview.visibility = View.GONE
    } else if (status == null)
        textview.visibility = View.VISIBLE
    else
        textview.visibility = View.GONE

}


@BindingAdapter("orderProgress")
fun orderProgress(linearLayout: LinearLayout, status: String) {
    if (!status.isNullOrBlank() && status != "null" && status != "-" && status != "F")
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}

@BindingAdapter("feedbackvisibility", "feedbackStatus")
fun feedbackvisibility(linearLayout: LinearLayout, status: String, feedbackStatus: String) {
    if (feedbackStatus == "0" && status == "F")
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE
    /*if (!status.isNullOrBlank() && !status.equals("null") && !status.equals("-") && status.equals("F"))
        //if(feedstatus.equals("0")){
           linearLayout.visibility = View.VISIBLE
        *//*}else{
            linearLayout.visibility = View.GONE
        }*//*

    else
        linearLayout.visibility = View.GONE*/


}

@BindingAdapter("layoutvisibility")
fun layoutvisibility(linearLayout: LinearLayout, status: String) {
    if (status == "1")
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}

@BindingAdapter("CouponFirstIndex")
fun CouponFirstIndex(textview: TextView, status: List<Models.Coupon1>) {
    if (status != null && status.size != 0)

        textview.text = status[0].couponTitle
}

@BindingAdapter("Order_Quantity", "Ispair")
fun setQuantity_for_Order(text: TextView, quantity: String, ispair: String) {

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
fun setcouponDetail(text: TextView, coupontitle: String, coupontype: String, couponPrices: String) {
    text.text = "$coupontitle : €$couponPrices"
}

@BindingAdapter("wishlist")
fun setWishlist(Iv_favorite: ImageView, wish_list: String) {

    if (wish_list == "1")
        Iv_favorite.setImageResource(R.drawable.ic_heart)
    else {
        Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
    }

}