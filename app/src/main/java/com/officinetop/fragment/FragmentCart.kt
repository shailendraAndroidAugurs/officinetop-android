package com.officinetop.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.officinetop.Online_Payment.OnlinePaymentScreen
import com.officinetop.R
import com.officinetop.data.getLangLocale
import com.officinetop.data.storeLangLocale
import com.officinetop.utils.*
import kotlinx.android.synthetic.main.activity_shopping_cart_single_item_detail.*
import kotlinx.android.synthetic.main.layout_recycler_view.view.*
import org.jetbrains.anko.support.v4.intentFor


class FragmentCart : Fragment(), OnCartListCallback {

    lateinit var rootView: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_shopping_cart_single_item_detail, container, false)
        val btnClickMe = view.findViewById(R.id.proceed_to_pay) as Button
        activity?.title = getString(R.string.cart_items)
        if (context?.getLangLocale() != null && !context?.getLangLocale().equals("")) {
            context?.setAppLanguage()
        } else {
            context?.storeLangLocale("it")
            context?.setAppLanguage()
        }

        btnClickMe.setOnClickListener {
            if (!cart_total_item_price.text.isNullOrEmpty() && !cart_total_price.text.isNullOrBlank() && !cart_total_service_price.text.isNullOrBlank()) {


                val sharedprefrence1 = context?.getSharedPreferences("Cart", Context.MODE_PRIVATE)
                val userWalletPref = context?.getSharedPreferences("UserWallet", Context.MODE_PRIVATE)

                val totalVat = sharedprefrence1?.getString("TotalVat", "")
                val totalDiscount = sharedprefrence1?.getString("TotalDiscount", "")
                val totalPfu = sharedprefrence1?.getString("TotalPFU", "")
                val userWalletAmount = userWalletPref?.getString(Constant.Path.user_WalletAmount, "0")
                val cartItemType = sharedprefrence1?.getString(Constant.Path.cartItemType, "3")
                val isMultipleServicesAvailable = sharedprefrence1?.getBoolean("isMultipleServicesAvailable", false)
                if (isMultipleServicesAvailable!!) {
                    context?.showInfoDialog(getString(R.string.confirm_order_with_multiple_workshop), false)
                    return@setOnClickListener
                }
                startActivity(intentFor<OnlinePaymentScreen>(
                        Constant.Path.totalAmount to cart_total_price.text.split(" ")[1].toString(),
                        Constant.Path.totalPfu to totalPfu,
                        Constant.Path.totalItemAmount to (cart_total_item_price.text.split(" ")[1].toDouble() + cart_total_service_price.text.split(" ")[1].toDouble()).toString(),
                        Constant.Path.totalDiscount to totalDiscount,
                        Constant.Path.totalVat to totalVat,
                        Constant.Path.user_WalletAmount to userWalletAmount,
                        Constant.Path.cartItemType to cartItemType

                ))
            }
            val preferences = context?.getSharedPreferences("Cart", Context.MODE_PRIVATE)
            preferences?.edit()?.clear()
        }
        rootView = view

        when {
            context?.isOnline()!! -> {
                getCartApi()
            }
            else -> {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true)


            }
        }


        return view
    }

    override fun callCartApi() {
        getCartApi()
    }

    private fun getCartApi() {
        when {
            !isAdded -> {
                return
            }
            else -> {
                rootView.recycler_view.removeAllViews()
                activity?.getCartItemsList(let {
                    context
                }, this, rootView)
            }
        }


    }


}