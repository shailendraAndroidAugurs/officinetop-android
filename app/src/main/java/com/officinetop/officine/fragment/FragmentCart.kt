package com.officinetop.officine.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.officinetop.officine.Online_Payment.OnlinePaymentScreen
import com.officinetop.officine.R
import com.officinetop.officine.data.getLangLocale
import com.officinetop.officine.data.storeLangLocale
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_shopping_cart_single_item_detail.*
import kotlinx.android.synthetic.main.layout_recycler_view.view.*
import org.jetbrains.anko.support.v4.intentFor


class FragmentCart : Fragment(), OnCartListCallback {

    lateinit var rootView: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_shopping_cart_single_item_detail, container, false)
        val btn_click_me = view.findViewById(R.id.proceed_to_pay) as Button
        activity?.title = getString(R.string.cart_items)
        if (context?.getLangLocale() != null && !context?.getLangLocale().equals("")) {
            context?.setAppLanguage()
        } else {
            context?.storeLangLocale("it")
            context?.setAppLanguage()
        }

        btn_click_me.setOnClickListener {
            if (!cart_total_item_price.text.isNullOrEmpty() && !cart_total_price.text.isNullOrBlank() && !cart_total_service_price.text.isNullOrBlank()) {


                val sharedprefrence1 = context?.getSharedPreferences("Cart", Context.MODE_PRIVATE)
                val userWalletPref = context?.getSharedPreferences("UserWallet", Context.MODE_PRIVATE)

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
                        Constant.Path.user_WalletAmount to userWalletAmount

                ))
            }
            val sharedprefrence2 = context?.getSharedPreferences("Cart", Context.MODE_PRIVATE)
            sharedprefrence2?.edit()?.clear()
        }
        rootView = view

        if (context?.isOnline()!!) {
            getCartApi()
        }else{
            context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }


        // view.bindCartViews(context)


        return view
    }

    override fun callCartApi() {
        getCartApi()
    }

    private fun getCartApi() {
        if (!isAdded) {
            return
        } else {
            rootView.recycler_view.removeAllViews()
            context!!.getCartItemsList(let {
                context
            }, this, rootView)
        }


    }


}