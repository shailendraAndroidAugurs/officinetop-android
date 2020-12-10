package com.officinetop.officine.workshop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.Online_Payment.OnlinePaymentScreen
import com.officinetop.officine.R
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.OnCartListCallback
import com.officinetop.officine.utils.getCartItemsList
import kotlinx.android.synthetic.main.activity_shopping_cart_single_item_detail.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.intentFor

class WorkshopBookingDetailsActivity : AppCompatActivity(), OnCartListCallback {

    private var service_id = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart_single_item_detail)
       getCartApi()
        // set on-click listener
        proceed_to_pay.setOnClickListener {
            if (!cart_total_item_price.text.isNullOrEmpty() && !cart_total_price.text.isNullOrBlank() && !cart_total_service_price.text.isNullOrBlank()) {
                val sharedprefrence1 = getSharedPreferences("Cart", Context.MODE_PRIVATE)
                val userWalletPref = getSharedPreferences("UserWallet", Context.MODE_PRIVATE)
                val totalvat = sharedprefrence1?.getString("TotalVat", "")
                val totalDiscount = sharedprefrence1?.getString("TotalDiscount", "")
                val totalPfu = sharedprefrence1?.getString("TotalPFU", "")
                val userWalletAmount = userWalletPref?.getString(Constant.Path.user_WalletAmount, "0")
                startActivity(intentFor<OnlinePaymentScreen>(
                        Constant.Path.totalAmount to cart_total_price.text.split(" ")[1].toString(),
                        Constant.Path.totalPfu to totalPfu,
                        Constant.Path.totalItemAmount to (cart_total_item_price.text.split(" ")[1].toDouble() + cart_total_service_price.text.split(" ")[1].toDouble()).toString(),
                        Constant.Path.totalDiscount to totalDiscount,
                        Constant.Path.totalVat to totalvat,
                        Constant.Path.user_WalletAmount to userWalletAmount,
                        "fromBooking" to "yes"

                ))
            }
            val sharedprefrence2 = getSharedPreferences("Cart", Context.MODE_PRIVATE)
            sharedprefrence2?.edit()?.clear()
            finish()

        }


/*
        cartProducts.forEach {

            Log.d("WorkshopBookingDetails", "onCreate: itemID = ${it.key}, value = $it")

            val isWorkshop = it.value.toString().contains("startingTime")
            val isCombined = it.value.toString().contains("car_makers_name") && isWorkshop


                adapter.addProduct(product = it.value.toString(),
                    productType = if(isCombined) Constant.type_assembled else if(!isWorkshop) Constant.type_product else Constant.type_workshop,
                    itemID = it.key)


//            adapter.addProduct(it.value.toString(), Constant.type_workshop)
        }

        recycler_view.adapter = adapter*/

    }


    private fun getCartApi() {
        recycler_view.removeAllViews()
        getCartItemsList(let {
            this
        }, this, ll_workshop_booking)
    }

    override fun callCartApi() {
        getCartApi()
    }

    override fun onBackPressed() {
        startActivity(intentFor<HomeActivity>().putExtra("login_success", true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()

    }
}
