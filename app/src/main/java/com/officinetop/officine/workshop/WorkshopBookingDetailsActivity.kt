package com.officinetop.officine.workshop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.Online_Payment.OnlinePaymentScreen
import com.officinetop.officine.R
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.OnCartListCallback
import com.officinetop.officine.utils.getCartItemsList
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_shopping_cart_single_item_detail.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.intentFor

class WorkshopBookingDetailsActivity : AppCompatActivity(), OnCartListCallback {
    var isProductOrServiceOrProductServices = "" // 0 for only product, 1 for product or services , 2 only for services
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
                val cartItemType = sharedprefrence1?.getString(Constant.Path.cartItemType, "3")
                val isMultipleServicesAvailable = sharedprefrence1?.getBoolean("isMultipleServicesAvailable", false)
                if (isMultipleServicesAvailable!!) {
                    showInfoDialog(getString(R.string.confirm_order_with_multiple_workshop), false)
                    return@setOnClickListener
                }

                Log.d("servicesData", cartItemType)


                startActivity(intentFor<OnlinePaymentScreen>(
                        Constant.Path.totalAmount to cart_total_price.text.split(" ")[1].toString(),
                        Constant.Path.totalPfu to totalPfu,
                        Constant.Path.totalItemAmount to (cart_total_item_price.text.split(" ")[1].toDouble() + cart_total_service_price.text.split(" ")[1].toDouble()).toString(),
                        Constant.Path.totalDiscount to totalDiscount,
                        Constant.Path.totalVat to totalvat,
                        Constant.Path.user_WalletAmount to userWalletAmount,
                        Constant.Path.cartItemType to cartItemType

                ))
            }
            val sharedprefrence2 = getSharedPreferences("Cart", Context.MODE_PRIVATE)
            sharedprefrence2?.edit()?.clear()
        }
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
