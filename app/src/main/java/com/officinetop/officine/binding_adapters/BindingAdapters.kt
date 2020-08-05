package com.officinetop.officine.binding_adapters

import android.graphics.Color
import android.os.Build
import android.text.Html
import android.util.Log
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
import com.officinetop.officine.utils.DateFormatChangeYearToMonth
import com.officinetop.officine.utils.loadImage
import com.officinetop.officine.utils.parseServerDateTime
import com.officinetop.officine.utils.roundTo2Places


@BindingAdapter("imageResource")
fun bindImages(imageView: ImageView, imageUrl: String) {
    if (imageUrl.contains("https"))
        imageView.context.loadImage(imageUrl, imageView)
}

@BindingAdapter("amount")
fun bindPrice(textView: TextView, price: String) {
    if (!price.isNullOrBlank()) {
     //   var amount = price.toDouble().roundTo2Places()
        textView.text = price.toString()

    }
    textView.visibility = if (!price.toString().isNullOrEmpty()) View.VISIBLE else View.GONE

}

@BindingAdapter("rating")
fun bindRating(ratingBar: RatingBar, rating: String) {
    if (!rating.isNullOrEmpty()) {
        ratingBar.rating = rating.toDouble().roundTo2Places().toFloat()
    }
}


@BindingAdapter("date")
fun bindDate(date: TextView, dateText: String) {

    if (!dateText.isNullOrEmpty() && !dateText.equals("0.0") && !dateText.equals("0") && !dateText.equals("-")) {
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
        var starttime = startTime.split("-")[0]
        var endtime = startTime.split("-")[1]
        if (starttime.equals("null") && endtime.equals("")) {
            textview.text = ""

        } else {
            if (!starttime.equals("null") && !endtime.equals("null")) {
                textview.text = "${starttime.removeSuffix(":00")}  -  ${endtime.removeSuffix(":00")}"
            } else if (starttime.equals("null") && !endtime.equals("null")) {
                textview.text = "${endtime.removeSuffix(":00")}"
            } else if (!starttime.equals("null") && endtime.equals("null")) {
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
    if (!tyrePfu.isNullOrBlank() && !tyrePfu.equals("0") && !tyreQuantity.isNullOrBlank() && !tyreQuantity.equals("0")) {
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
                text.text = "2 " + pairText
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@BindingAdapter("payment")
fun payment(textView: TextView, status: String) {

    if (status.equals("C")) {
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
    if (!status.isNullOrBlank() && !status.equals("null") && status.equals("F"))
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}

@BindingAdapter("orderReturn")
fun orderReturn(textview: TextView, status: String) {

    if (!status.isNullOrBlank() && !status.equals("null") && status != null) {
        textview.visibility = View.VISIBLE
        if (status.equals("P")) {
            textview.setText(R.string.return_request_inProcess)
            textview.setTextColor(Color.parseColor("#2E7D32"))
        } else if (status.equals("C")) {
            textview.setTextColor(Color.parseColor("#2E7D32"))
            textview.setText(R.string.return_request_accepted)
        } else if (status.equals("CA")) {
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
    if (status.isNullOrBlank() || status.equals("-") || status.equals("p")) {
        textview.text = R.string.request_invoice.toString()
        textview.setTextColor(Color.parseColor("#2E7D32"))
    } else if (status.equals("c")) {
        textview.setTextColor(Color.parseColor("#2E7D32"))
        textview.text = R.string.invoice_Request_inProcess.toString()
    }


}


@BindingAdapter("orderReturnbutton")
fun orderReturnbutton(textview: TextView, status: String) {

    if (!status.isNullOrBlank() && !status.equals("null")) {
        textview.visibility = View.GONE
    } else if (status == null)
        textview.visibility = View.VISIBLE
    else
        textview.visibility = View.GONE

}


@BindingAdapter("couponvisivility")
fun couponvisivility(textview: TextView, status: Integer) {

    if (status.equals(0)) {
        textview.visibility = View.GONE
    } else
        textview.visibility = View.VISIBLE


}

@BindingAdapter("orderProgress")
fun orderProgress(linearLayout: LinearLayout, status: String) {
    if (!status.isNullOrBlank() && !status.equals("null") && !status.equals("-") && !status.equals("F"))
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE


}

@BindingAdapter("feedbackvisibility", "feedbackStatus")
fun feedbackvisibility(linearLayout: LinearLayout, status: String, feedbackStatus: String) {
    if (feedbackStatus.equals("0") && status.equals("F"))
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
    if (status.equals("1"))
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
                if (!ispair.isNullOrBlank() && !ispair.equals("0"))
                    text.text = (quantity.toInt() /** 2*/).toString()
                else
                    text.text = quantity
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@BindingAdapter("coupontitle", "coupontype","couponPrices")
fun setcouponDetail(text: TextView, coupontitle: String, coupontype: String,couponPrices:String) {
    text.text=coupontitle+" :"+" €" +couponPrices
}