package com.officinetop.binding_adapters

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.officinetop.R
import com.officinetop.adapter.GenericAdapter
import com.officinetop.data.Models
import com.officinetop.data.getSelectedCar
import com.officinetop.utils.*
import kotlinx.android.synthetic.main.search_preview_layout.view.*


@BindingAdapter("imageResource")
fun bindImages(imageView: ImageView, imageUrl: String) {
    if (!imageUrl.isNullOrBlank() && imageUrl.contains("http"))
        imageView.context.loadImage(imageUrl, imageView)
}

@BindingAdapter("imageLoad")
fun imageLoad(imageView: ImageView, imageUrl: String) {
    if (!imageUrl.isNullOrBlank() && imageUrl.contains("http"))
        imageView.context.loadImage(imageUrl, imageView)
    else imageView.visibility = View.GONE
}

@BindingAdapter("amount", "value")
fun bindPrice(textView: TextView, price: String, value: String) {
    if (!price.isNullOrBlank()) {
        var amount = price.toDouble().roundTo2Places()
        textView.text = (value + " " + amount.toString()).trim()

    }


}

@BindingAdapter("entries", "value")
fun bindMotSparePartPrice(textView: TextView, entries: Models.ServiceProductDescription, value: String) {
    if (entries != null && entries.partDetails != null) {
        var motSparePartTotalPrices = 0.0
        for (part in entries.partDetails) {
            if (!part.sellerPrice.isNullOrBlank()) {
                if (!part.forPair.isNullOrBlank() && !part.forPair.equals("0"))
                    motSparePartTotalPrices += part.sellerPrice.toDouble() * 2
                else motSparePartTotalPrices += part.sellerPrice.toDouble()
            }


        }

        textView.text = value + " " + motSparePartTotalPrices.roundTo2Places().toString()
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

    if (!coupontitle.isNullOrBlank() && !couponPrices.isNullOrBlank() && !couponPrices.equals("-")) {
        text.text = "$coupontitle : €$couponPrices"
    } else {
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

@BindingAdapter("pairVisiblity", "pairText")
fun pairVisiblity(text: TextView, pairVisiblity: String, pairText: String) {

    if (!pairVisiblity.isNullOrEmpty() && !pairVisiblity.equals("0")) {
        try {
            text.visibility = View.VISIBLE
            text.text = "2 $pairText"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else text.visibility = View.GONE
}

@BindingAdapter("entries")
fun setRecyclerViewAdapter(recyclerView: RecyclerView, entries: Models.ServiceProductDescription) {
    if(entries?.partDetails != null ){
        var mKPartServicesList: ArrayList<Models.Part> = ArrayList()
        var partListFromCart = ArrayList<Models.Part>()
        val jsonString = Gson().toJson(entries?.partDetails);
        val gson = GsonBuilder().create()
        partListFromCart = gson.fromJson(jsonString.toString(), Array<Models.Part>::class.java).toCollection(java.util.ArrayList<Models.Part>())
        mKPartServicesList.addAll(partListFromCart)
        val   genericAdapter = GenericAdapter<Models.Part>(recyclerView.context, R.layout.item_sparepart_mot)
        genericAdapter!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
            }
            override fun onItemClick(view: View, position: Int) {
            }
        })
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(recyclerView.context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = genericAdapter
        genericAdapter!!.addItems(mKPartServicesList)
    }




}




@BindingAdapter("searchText","title")
fun textHighlighted(text: TextView, searchText: String, title: String) {
    if (searchText.length > 0) {
        var index: Int = title.toLowerCase().indexOf(searchText.toLowerCase())

        while (index >= 0) {
            val sb = SpannableStringBuilder(title)
            val fcs = BackgroundColorSpan(text.context.resources.getColor(R.color.theme_orange))
            sb.setSpan(fcs, index, searchText.length + index, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            index = title.indexOf(searchText, index + 1, true)
            text.text = sb
        }

    }

}




