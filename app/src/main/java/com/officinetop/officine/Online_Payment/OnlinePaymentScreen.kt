package com.officinetop.officine.Online_Payment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.samples.wallet.PaymentsUtil
import com.google.android.gms.wallet.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.Orders.Order_List
import com.officinetop.officine.R
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.userprofile.Addresslist_Activity
import com.officinetop.officine.userprofile.ContactList_Activity
import com.officinetop.officine.utils.*
import com.paypal.android.sdk.payments.*
import kotlinx.android.synthetic.main.activity_online_payment.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.json.JSONException
import org.json.JSONObject

class OnlinePaymentScreen : BaseActivity() {
    private lateinit var radioButton_bankTransfer: RadioButton
    private lateinit var radioButton_COD: RadioButton

    private lateinit var radioButton_googlepay: RadioButton

    private lateinit var paymentsClient: PaymentsClient

    private lateinit var paypal_payment: LinearLayout

    private lateinit var layout_amazonupid: LinearLayout
    private lateinit var tv_emailAddress: TextView

    private lateinit var tv_bank_holdername: TextView
    private lateinit var tv_iban: TextView


    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    private val UPI_PAYMENT = 0
    private var payableAmount: String = ""
    private var TotalAmount: String = ""
    private var contactNo: String? = ""
    private var contactId: String? = ""
    var Address: String? = ""
    private var AddressId: String? = ""
    private var IsCheckAvailablity: Boolean = false
    private lateinit var progressBar: ProgressBar
    private var TotalDiscount: String = "0"
    private var totalPrices: String = "0"
    private var totalVat: String = "0"
    private var totalPfu: String = "0"
    private var user_WalletAmount: String = "0"
    private var AmazonPayRequestCode = 992
    private var usedWalletAmount = "0"
    private var HaveBrowser = true
    private var fromBooking = false
    private lateinit var bankpaymentobject: Models.BankPaymentInfo
    private var cartItemType = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_payment)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.payment_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initviewsId()
        initviews()
    }

    private fun initviewsId() {
        radioButton_COD = findViewById(R.id.radio_button_cod)
        radioButton_bankTransfer = findViewById(R.id.radio_button_banktransfer)
        radioButton_googlepay = findViewById(R.id.radio_button_banktransfer)
        paypal_payment = findViewById<LinearLayout>(R.id.layout_cardpayment)
        layout_amazonupid = findViewById<LinearLayout>(R.id.layout_amazonupid)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        tv_emailAddress = findViewById<TextView>(R.id.tv_emailAddress)
        tv_bank_holdername = findViewById<TextView>(R.id.tv_registered_person)

        tv_iban = findViewById<TextView>(R.id.tv_iban)
    }

    private fun initviews() {
        if (getLangLocale() != "") {
            setAppLanguage()
        } else {
            storeLangLocale("it")
            setAppLanguage()
        }

        if (intent.hasExtra(Constant.Path.totalAmount) && !intent.getStringExtra(Constant.Path.totalAmount).isNullOrEmpty())
            TotalAmount = intent.getStringExtra(Constant.Path.totalAmount)

        if (intent.hasExtra("fromBooking") && !intent.getStringExtra("fromBooking").isNullOrEmpty())
            fromBooking = true


        if (intent.hasExtra(Constant.Path.totalItemAmount) && !intent.getStringExtra(Constant.Path.totalItemAmount).isNullOrEmpty())
            totalPrices = intent.getStringExtra(Constant.Path.totalItemAmount)
        if (intent.hasExtra(Constant.Path.totalDiscount) && !intent.getStringExtra(Constant.Path.totalDiscount).isNullOrEmpty())
            TotalDiscount = intent.getStringExtra(Constant.Path.totalDiscount)
        if (intent.hasExtra(Constant.Path.totalVat) && !intent.getStringExtra(Constant.Path.totalVat).isNullOrEmpty())
            totalVat = intent.getStringExtra(Constant.Path.totalVat)
        if (intent.hasExtra(Constant.Path.totalPfu) && !intent.getStringExtra(Constant.Path.totalPfu).isNullOrEmpty())
            totalPfu = intent.getStringExtra(Constant.Path.totalPfu)
        if (intent.hasExtra(Constant.Path.user_WalletAmount) && !intent.getStringExtra(Constant.Path.user_WalletAmount).isNullOrEmpty())
            user_WalletAmount = intent.getStringExtra(Constant.Path.user_WalletAmount)

        if (intent.hasExtra(Constant.Path.cartItemType) && !intent.getStringExtra(Constant.Path.cartItemType).isNullOrEmpty())
            cartItemType = intent.getStringExtra(Constant.Path.cartItemType)

        // 0 only for product, 1 only for services , 2 only for services with product ,3 mix item
        if (cartItemType.equals("1")) {
            tv_notes.visibility = View.VISIBLE
            tv_notes.text = getString(R.string.services_address_note)
            layout_address.visibility = View.GONE
            tv_delivery_address_txt.visibility = View.GONE
        } else if (cartItemType.equals("2") || cartItemType.equals("3")) {
            tv_notes.visibility = View.VISIBLE
            tv_notes.text = getString(R.string.product_services_address_note)
            layout_address.visibility = View.GONE
            tv_delivery_address_txt.visibility = View.GONE

        }



        tv_userWallet.text = getString(R.string.wallet) + user_WalletAmount.toDouble().roundTo2Places().toString()

        if (user_WalletAmount == "0" || user_WalletAmount == "") {
            tv_TotalAmount.visibility = View.GONE
            tv_userWallet.text = getString(R.string.wallet) + "0"
            tv_payable_amount.text = getString(R.string.payable_amount, TotalAmount)
            payableAmount = TotalAmount
            usedWalletAmount = "0"
        } else if (TotalAmount.toFloat() > user_WalletAmount.toFloat()) {
            tv_TotalAmount.visibility = View.VISIBLE
            tv_TotalAmount.text = getString(R.string.prepend_euro_symbol_string, TotalAmount.toDouble().roundTo2Places().toString())
            tv_TotalAmount.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            payableAmount = ((TotalAmount.toDouble() - user_WalletAmount.toDouble()).roundTo2Places()).toString()
            tv_payable_amount.text = getString(R.string.payable_amount, payableAmount)
            usedWalletAmount = user_WalletAmount.toDouble().roundTo2Places().toString()
        } else if (user_WalletAmount.toFloat() >= TotalAmount.toFloat()) {
            tv_TotalAmount.visibility = View.VISIBLE
            tv_TotalAmount.text = getString(R.string.prepend_euro_symbol_string, TotalAmount.toDouble().roundTo2Places().toString())
            tv_TotalAmount.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            payableAmount = "0"
            usedWalletAmount = ((user_WalletAmount.toDouble() - TotalAmount.toDouble()).roundTo2Places()).toString()
            tv_payable_amount.text = getString(R.string.payable_amount, payableAmount)
        }

        if (payableAmount == "0" || payableAmount == "0.0") {
            ll_PaymentLayout.visibility = View.GONE
            ll_WalletPayment.visibility = View.VISIBLE
        } else {
            ll_PaymentLayout.visibility = View.VISIBLE
            ll_WalletPayment.visibility = View.GONE
        }
        proceed_to_pay.setOnClickListener {
            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                updatePaymentStatusWithWallet()
            }
        }


        Log.e("Totalprices", totalPrices)
        Log.e("Totalpfu", totalPfu)
        Log.e("TotalVat", totalVat)
        Log.e("payableAmount", payableAmount)
        Log.e("user_WalletAmount", user_WalletAmount)
        Log.e("FinalPrices", TotalAmount)
        getBankTransferPaymentInfo()



        paypal_payment.visibility = View.GONE
        layout_amazonupid.visibility = View.GONE

        radioButton_googlepay.setOnClickListener(View.OnClickListener {
            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                payUsingUpi()
                paypal_payment.visibility = View.GONE
                layout_amazonupid.visibility = View.GONE
                radioButton_googlepay.isChecked = true
                radioButton_COD.isChecked = false
                radioButton_bankTransfer.isChecked = false
                //startUpiPayment()
            } else {
                radioButton_googlepay.isChecked = false
            }
        })

        radioButton_COD.setOnClickListener(View.OnClickListener {
            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                paypal_payment.visibility = View.GONE
                layout_amazonupid.visibility = View.GONE
                radioButton_COD.isChecked = true
                radioButton_googlepay.isChecked = false
                radioButton_bankTransfer.isChecked = false

                showConfirmDialogforPayment(getString(R.string.PayOnDelivery), { updatePaymentStatusForCOD_BankTransfer("", "2") }, { uncheckedAllPaymentmethod() }, false)


            } else {
                radioButton_COD.isChecked = false
            }
        })


        radioButton_bankTransfer.setOnClickListener(View.OnClickListener {
            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                paypal_payment.visibility = View.GONE
                layout_amazonupid.visibility = View.GONE
                radioButton_COD.isChecked = false
                radioButton_googlepay.isChecked = false
                radioButton_bankTransfer.isChecked = true
                if (this::bankpaymentobject.isInitialized) {
                    showConfirmDialogforPayment(getString(R.string.bank_transfer_proceed), {
                        updatePaymentStatusForCOD_BankTransfer(bankpaymentobject.id, "4")

                    }, { uncheckedAllPaymentmethod() }, false)
                } else {
                    showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again), true, { uncheckedAllPaymentmethod() })
                }


            } else {
                radioButton_bankTransfer.isChecked = false
            }
        })


        ////////////////////////////////PayPal  payment/////////////////////////////////////////
        /*val intent = Intent(this, PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        startService(intent)*/

        btn_paypal_payment.setOnClickListener(View.OnClickListener {
            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                radioButton_COD.isChecked = false
                radioButton_googlepay.isChecked = false
                radioButton_bankTransfer.isChecked = false
                // paypal sdk implement for payment through paypal , now it is commented as a client requirement
                /*val thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE)
                val intent = Intent(this@OnlinePaymentScreen, PaymentActivity::class.java)
                // send the same configuration for restart resiliency
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy)
                startActivityForResult(intent, REQUEST_CODE_PAYMENT)*/
                try {
                    HaveBrowser = true

                    val loadUrl: String = "https://services.officinetop.com/public/paypal_home?order=${getOrderId()}&used_wallet_amount=${usedWalletAmount}&address=${AddressId}&contact=${contactId}"
                    Log.d("PayPalRequest :", loadUrl)
                    val openURL = Intent(android.content.Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(loadUrl)
                    startActivityForResult(openURL, AmazonPayRequestCode)

                } catch (e: Exception) {
                    HaveBrowser = false
                    Toast.makeText(this, getString(R.string.there_is_noBrowser), Toast.LENGTH_LONG).show()
                }


            }

        })
        paymentsClient = PaymentsUtil.createPaymentsClient(this)
        possiblyShowGooglePayButton()
        googlePayButton.setOnClickListener {
            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                requestPayment()
                radioButton_COD.isChecked = false
                radioButton_googlepay.isChecked = false
                radioButton_bankTransfer.isChecked = false

            }
        }
        btn_amazon_pay.setOnClickListener {


            if (isaddress_ContactSelect() && isCartDataAvailable()) {
                ll_container_payment.visibility = View.GONE
                /*val webSettings = webview!!.settings
                webview!!.loadUrl("https://services.officinetop.com/public/amazon_pay_checkout_add?user_id=${getUserId()}&payble_amount=${payableAmount}")
                webview.requestFocus();
                webSettings.javaScriptEnabled = true
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setUseWideViewPort(true);
                webSettings.setAllowFileAccess(true);
                webSettings.setAllowContentAccess(true);
                webSettings.domStorageEnabled = true
                webSettings.setSupportMultipleWindows(true)

                //These two lines are specific for my need. These are not necessary
                //These two lines are specific for my need. These are not necessary
                if (Build.VERSION.SDK_INT >= 21) {
                    webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                //Cookie manager for the webview
                //Cookie manager for the webview
                val cookieManager: CookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                webview!!.setInitialScale(1);

                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                val webViewClient = WebViewClientImpl(this, progress_bar_webview)
                webview!!.webViewClient = webViewClient*/
                // https://services.officinetop.com/public/amazon_pay_checkout_add?user_id=101&payble_amount=1.8&used_wallet_amount=0.2&address=188&contact=57
                try {
                    HaveBrowser = true
                    val loadUrl: String = "https://services.officinetop.com/public/amazon_pay_checkout_add?user_id=${getUserId()}&payble_amount=${payableAmount}&used_wallet_amount=${usedWalletAmount}&address=${AddressId}&contact=${contactId}"
                    Log.d("AmazonPayRequest :", loadUrl)
                    val openURL = Intent(android.content.Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(loadUrl)
                    /*openURL.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    openURL.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)*/
                    startActivityForResult(openURL, AmazonPayRequestCode)

                } catch (e: Exception) {
                    HaveBrowser = false
                    Toast.makeText(this, getString(R.string.there_is_noBrowser), Toast.LENGTH_LONG).show()
                }


            }
        }
        tv_contactNo.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ContactList_Activity::class.java)
            intent.putExtra("FromPayment", true)
            startActivityForResult(intent, 100)

        })
        tv_Address.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Addresslist_Activity::class.java)
            intent.putExtra("FromPayment", true)
            startActivityForResult(intent, 101)

        })


        val sharedPref = getSharedPreferences("ShippingContact_Address", Context.MODE_PRIVATE)
        contactNo = sharedPref.getString("contactNo", "")
        Address = sharedPref.getString("Address", "")
        AddressId = sharedPref.getString("AddressId", "")
        contactId = sharedPref.getString("contactId", "")
        if (contactNo != null) {
            text_contactnumber.text = contactNo
        }
        if (Address != null) {
            text_address.text = Address
        }
        if (contactId != null || AddressId != null) {
            CheckCartItemAvailability()
        }

        tv_emailAddress.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                val recipients = arrayOf(tv_emailAddress.text.toString())
                intent.putExtra(Intent.EXTRA_EMAIL, recipients)
                intent.type = "text/html"
                intent.setPackage("com.google.android.gm")
                startActivity(Intent.createChooser(intent, "Send mail"))
            } catch (e: Exception) {

            }


        }
    }


    ////////////////////////////GPAY/////////////////////////////////////////////
    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.

        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            googlePayButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                    this,
                    "Unfortunately, Google Pay is not available on this device",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun requestPayment() {
        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false
        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        //val garmentPriceMicros = (selectedGarment.getDouble("price") * 1000000).roundToLong()
        // val price = (garmentPriceMicros + shippingCost).microsToString()

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest("1")
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return
        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("token") == "examplePaymentMethodToken") {

                AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Gateway name set to \"example\" - please modify " +
                                "Constants.java and replace it with your own gateway.")
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
            }

            val billingName = paymentMethodData.getJSONObject("info")
                    .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show()
            // Logging token string.
            Log.d("GooglePaymentToken", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token"))
        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $e")
        }
    }

    ///////////////////////////////paypal payment/////////////////////////
    public override fun onDestroy() {
        // Stop service when done
        stopService(Intent(this, PayPalService::class.java))
        super.onDestroy()
    }

    companion object {
        private const val TAG = "paymentExample"

        private const val CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX

        // note that these credentials will differ between live & sandbox environments.
        private const val CONFIG_CLIENT_ID = "ARJLfJNfSNkUyq1pzF5r2F9GPbgWrbKDf8jqVHTtIHEsvMBCZWdGR90MCEdWZGDrJV5bp2b9i-O6MkW3"//create at 18052020 - sandbox client id with xn32541bbbbb password- !D8@z%i$
        //  "Ae-dgSrONxM4oozHBTjqIsqqCPBlX3o1KbWgJyOgWuGvZECGQ7K2xsNaA6bQWdpIZ1k59wDil5K88pmA"

        //    "AQaROYvr5Gv9Fmu7OVGoIRtPjA359XD5F1oUVA4RFOfx0ZbPHDSkV-QvvGh9dl04oUb5Tpe3_R2Q6IJj" comment on 18052020


        //  ATzTTqLAcY8X5GxCWFb52Do38kmnmgRfmtHwcmN_3LgHI05mC7JfHePaHHt0uXVrjxHJe_gRRXqYb7Dq
        //  AZQj8TMgW9IRfmPZVmo0Nd_-WJOKsP-yx2NUFV2_AdskE6a_waYmxwPQNfUD4426YwFZhv_YIdYV6toq

        private const val REQUEST_CODE_PAYMENT = 1
        private const val REQUEST_CODE_FUTURE_PAYMENT = 2
        private const val REQUEST_CODE_PROFILE_SHARING = 3
        private val config = PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(CONFIG_CLIENT_ID)
                .merchantName("Example Merchant")
                .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"))
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val confirm = data?.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4))
                        Log.i(TAG, confirm.payment.toJSONObject().toString(4))

                        Log.e("payment===", confirm.payment.toJSONObject().toString(4))
                        //displayResultText("PaymentConfirmation info received from PayPal")
                        showInfoDialog(getString(R.string.PaymentConfirmationinforeceivedfromPayPal)) {
                            updatePaymentStatus(confirm.toJSONObject())
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e)
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.")
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.")
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val auth = data?.getParcelableExtra<PayPalAuthorization>(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION)
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4))

                        val authorization_code = auth.authorizationCode
                        Log.i("FuturePaymentExample", authorization_code)


                    } catch (e: JSONException) {
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e)
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.")
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.")
            }
        } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {
            if (resultCode == Activity.RESULT_OK) {
                val auth = data?.getParcelableExtra<PayPalAuthorization>(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION)
                if (auth != null) {
                    try {
                        Log.i("ProfileSharingExample", auth.toJSONObject().toString(4))

                        val authorization_code = auth.authorizationCode
                        Log.i("ProfileSharingExample", authorization_code)


                    } catch (e: JSONException) {
                        Log.e("ProfileSharingExample", "an extremely unlikely failure occurred: ", e)
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("ProfileSharingExample", "The user canceled.")
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "ProfileSharingExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.")
            }
        }
        if (requestCode == 100) {
            contactNo = data?.extras?.getString("contactno")
            contactId = data?.extras?.getString("contactId")


            if (contactNo != null) {
                text_contactnumber.text = contactNo
                saveContact_ContactForShipping(contactNo!!, contactId!!)
                if (contactId != null || AddressId != null) {
                    CheckCartItemAvailability()
                }
            }
        } else if (requestCode == 101) {
            Address = data?.extras?.getString("Address")
            AddressId = data?.extras?.getString("Id")
            if (Address != null) {
                text_address.text = Address
                saveAddress_ContactForShipping(Address!!, AddressId!!)
                if (contactId != null || AddressId != null) {
                    CheckCartItemAvailability()
                }
            }
        } else if (requestCode == AmazonPayRequestCode) {
            if (HaveBrowser) {
                startActivity(intentFor<Order_List>().putExtra("fromBooking", fromBooking.toString()))
                finish()
                saveIsAvailableDataInCart(true)
                logAddPaymentInfoEvent(this, true)
            }
        } else {
            /////////////////////Upi pay//////////////////////////////////////
            when (requestCode) {
                UPI_PAYMENT -> if (Activity.RESULT_OK == resultCode || resultCode == 11) {
                    if (data != null) {
                        val trxt = data.getStringExtra("response")
                        Log.d("UPI", "onActivityResult: $trxt")
                        val dataList = ArrayList<String>()
                        dataList.add(trxt)
                        //upiPaymentDataOperation(dataList)
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null")
                        val dataList = ArrayList<String>()
                        dataList.add("nothing")

                        //upiPaymentDataOperation(dataList)
                    }
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null") //when user simply back without payment
                    val dataList = ArrayList<String>()
                    dataList.add("nothing")
                    // upiPaymentDataOperation(dataList)
                }
            }
        }

        /////////////////////Gpay onActivityresult//////////////////////////////////////


    }


    /* private fun getThingToBuy(paymentIntent: String): PayPalPayment {
         return PayPalPayment(BigDecimal(payableAmount), "USD", "sample item",
                 paymentIntent)
     }*/

    ////////////////////////////////////// UPI Payment method //////////////////////////////////////////
    private fun payUsingUpi() {
        val uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "8418956190@ybl")
                .appendQueryParameter("pn", "Amit Kumar")
                .appendQueryParameter("tn", "")
                .appendQueryParameter("am", "1.00")
                .appendQueryParameter("cu", "INR")
                .build()

        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri
        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
        // check if intent resolves
        if (null != chooser.resolveActivity(packageManager)) {
            startActivityForResult(chooser, UPI_PAYMENT)
        } else {
            Toast.makeText(this@OnlinePaymentScreen, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show()
        }

    }


    private fun updatePaymentStatus(paymentObject: JSONObject) {
        Log.e("payment mode", "OnlinePayment :1")
        var id = ""
        if (paymentObject.has("response") && !paymentObject.isNull("response")) {
            id = paymentObject.getJSONObject("response").get("id") as String
        }
        RetrofitClient.client.updatePaymentStatus(
                getBearerToken()
                        ?: "", id, "1", getOrderId(), TotalAmount, totalPrices, TotalDiscount, totalVat, totalPfu, payableAmount, usedWalletAmount, ""

        ).onCall { _, response ->
            response?.let {
                if (response.isSuccessful) {

                    val responseData = JSONObject(response.body()?.string())
                    if (responseData.has("message") && !responseData.isNull("message")) {
                        showInfoDialog(responseData.get("message").toString()) {
                            startActivity(intentFor<Order_List>().putExtra("fromBooking", fromBooking.toString()))
                            finish()
                            saveIsAvailableDataInCart(false)
                            logAddPaymentInfoEvent(this, true)
                        }

                    }
                }
            }
        }


    }

    private fun isaddress_ContactSelect(): Boolean {

        if (cartItemType.equals("0") && Address.isNullOrBlank() && AddressId.isNullOrBlank()) {
            Snackbar.make(ll_container_payment, getString(R.string.PleaseSelectAddress), Snackbar.LENGTH_SHORT).show()
            return false
        } else if (contactNo.isNullOrBlank()) {

            Snackbar.make(ll_container_payment, getString(R.string.PleaseSelectContactNo), Snackbar.LENGTH_SHORT).show()

            return false
        }

        return true

    }

    private fun isCartDataAvailable(): Boolean {
        return if (!IsCheckAvailablity) {
            Snackbar.make(ll_container_payment, getString(R.string.Something_went_wrong_Please_try_again), Snackbar.LENGTH_SHORT).show()

            false


        } else
            true

    }

    private fun CheckCartItemAvailability() {
        AddressId?.let {
            contactId?.let { it1 ->
                getBearerToken()?.let { it2 ->
                    progressBar.visibility = View.VISIBLE
                    RetrofitClient.client.checkUserCartItems(getOrderId(), it, it1, it2)
                            .onCall { networkException, response ->
                                networkException.let {
                                    progressBar.visibility = View.GONE
                                }
                                response?.let {
                                    progressBar.visibility = View.GONE
                                    if (response.isSuccessful) {
                                        response.body()?.string()?.let { body ->
                                            if (isStatusCodeValid(body)) {
                                                IsCheckAvailablity = true
                                            } else {
                                                showInfoDialog(getString(R.string.PleasedeleteOutofStockorExpiredservices))
                                            }
                                        }
                                    } else {
                                        showInfoDialog(getString(R.string.Something_went_wrong_Please_try_again))
                                    }
                                }
                            }
                }
            }


        }

    }

    private fun updatePaymentStatusForCOD_BankTransfer(bankDetailId: String = "", paymentMode: String) {
        Log.e("payment mode", "COD :2")

        RetrofitClient.client.updatePaymentStatus(
                getBearerToken()
                        ?: "", "", paymentMode, getOrderId(), TotalAmount, totalPrices, TotalDiscount, totalVat, totalPfu, payableAmount, usedWalletAmount, bankDetailId
        ).onCall { _, response ->


            response?.let {
                if (response.isSuccessful) {

                    val responseData = JSONObject(response.body()?.string())
                    if (responseData.has("message") && !responseData.isNull("message")) {
                        showInfoDialog(responseData.get("message").toString()) {
                            startActivity(intentFor<Order_List>().putExtra("fromBooking", fromBooking.toString()))
                            finish()
                            saveIsAvailableDataInCart(false)
                            logAddPaymentInfoEvent(this, true)
                        }
                    }
                }
            }
        }
    }


    private fun updatePaymentStatusWithWallet() {
        Log.e("payment mode", "wallet :3")

        RetrofitClient.client.updatePaymentStatus(
                getBearerToken()
                        ?: "", "", "3", getOrderId(), TotalAmount, totalPrices, TotalDiscount, totalVat, totalPfu, payableAmount, usedWalletAmount, ""
        ).onCall { _, response ->
            response?.let {
                if (response.isSuccessful) {

                    val responseData = JSONObject(response.body()?.string())
                    if (responseData.has("message") && !responseData.isNull("message")) {
                        showInfoDialog(responseData.get("message").toString()) {
                            startActivity(intentFor<Order_List>().putExtra("fromBooking", fromBooking.toString()))
                            finish()
                            saveIsAvailableDataInCart(false)
                            logAddPaymentInfoEvent(this, true)
                            //saveOrderId("")
                        }

                    }
                }
            }
        }


    }

    private fun uncheckedAllPaymentmethod() {
        radioButton_googlepay.isChecked = false
        radioButton_bankTransfer.isChecked = false
        radioButton_COD.isChecked = false
    }

    private fun bindBankdetailInView() {
        if (this::bankpaymentobject.isInitialized) {
            tv_emailAddress.text = bankpaymentobject.email.let { it }
            tv_bank_holdername.text = bankpaymentobject.instatario.let { it }
            tv_iban.text = bankpaymentobject.iban.let { it }
        }
    }

    private fun getBankTransferPaymentInfo() {

        getBearerToken()?.let {
            RetrofitClient.client.getBankPaymentInformation(it).onCall { _, response ->

                val responseDataBank = JSONObject(response?.body()?.string())
                responseDataBank.let {
                    if (it.has("data") && !it.getString("data").isNullOrBlank()) {
                        bankpaymentobject = Gson().fromJson(it.getString("data"), Models.BankPaymentInfo::class.java)
                        bindBankdetailInView()
                    }
                }


            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (fromBooking) {
            startActivity(intentFor<HomeActivity>().clearTop())
        } else finish()
    }
}
