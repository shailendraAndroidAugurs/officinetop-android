package com.officinetop.officine.authentication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.R
import com.officinetop.officine.authentication.linkedin.LinkedInWebView
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import com.officinetop.officine.virtual_garage.AddVehicleActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.bottom_sheet_referral.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

class LoginActivity : BaseActivity() {

    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var facebookCallbackManager: CallbackManager
    private val RC_GOOGLE = 101
    private val RC_LINKEDIN = 102

    lateinit var progressDialog: ProgressDialog

    private lateinit var referralCode: String

    private var socialmediaflag: Int? = null
    private var uniqueId: String? = null

    //0=facebook 1=google  2=linkdin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        printHash()


        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.login)
        progressDialog = getProgressDialog()

        initRetrofitSigning()
        initGoogleSignIn()
        initFacebookLogin()
        referralCode = ""

        continueWithoutLoginBtn.setOnClickListener()
        {
            handleLoginType("continueWithoutLogin")
        }

        linkedinSignIn.setOnClickListener {
            socialmediaflag = 2

            AlertReferralcode()
        }

        googleSignIn.setOnClickListener {
            socialmediaflag = 1
            AlertReferralcode()

        }


        facebookSignIn.setOnClickListener {
            socialmediaflag = 0
            AlertReferralcode()


        }

        uniqueId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        register.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        reset_password.setOnClickListener { startActivity(Intent(this, ResetPasswordActivity::class.java)) }

        if (isLoggedIn())
            Log.d("LoginActivity", "onCreate: current token = ${getStoredToken()}")
    }

    private fun handleLoginType(loginType: String) {
        if ((intent != null && intent.hasExtra(Constant.pref_login_from) && intent.getStringExtra(Constant.pref_login_from) == "AddFirstVehicle")
                || (loginType == "loginSuccess" && intent != null && intent.getStringExtra(Constant.pref_login_from) == "AddSecondVehicle")) {

            startActivity(intentFor<AddVehicleActivity>().clearTask().clearTop())

        } else if (loginType == "continueWithoutLogin" && intent != null && intent.hasExtra(Constant.pref_login_from) && intent.getStringExtra(Constant.pref_login_from) == "AddSecondVehicle"){
            startActivity(intentFor<HomeActivity>().putExtra("login_success", true).clearTask().clearTop().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()
        }
        else {

            startActivity(intentFor<HomeActivity>().putExtra("login_success", true).clearTask().clearTop().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()
        }
    }

    private fun initGoogleSignIn() {

        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)

        lastAccount?.let {
            googleSignInClient.signOut().addOnCompleteListener {
                //toast("Logged out from ${lastAccount.email}")
            }
        }
    }


    private fun initFacebookLogin() {
        FacebookSdk.sdkInitialize(applicationContext);
        facebookCallbackManager = CallbackManager.Factory.create()

    }


    private fun registerWithSocial(f_name: String, l_name: String, provider_name: String, provider_id: String, email: String) {

        if (!isOnline()) {
            Snackbar.make(loginBtn, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG).show()
            return
        }

        progressDialog.show()

        RetrofitClient.client.registerWithSocial(f_name, l_name, email, "", "1", provider_name, getFCMToken(), referralCode, provider_id, "", uniqueId!!)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog.dismiss()
                        toast(t.message!!)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {


                        val responseString = response.body()?.string()
                        progressDialog.dismiss()
                        Log.d("LoginActivity", "onResponse: code = ${response.code()}")
                        Log.d("LoginActivity", "onResponse: social login = $responseString")
                        if (response.code() == 200) {

                            if (!isStatusCodeValid(responseString)) {
                                if (getMessageFromJSON(responseString).isEmpty())
                                    longSnackbar(loginBtn, getString(R.string.Invalidusernamepassword))
                                else
                                    longSnackbar(loginBtn, getMessageFromJSON(responseString))
                                return
                            }
                            if (isStatusCodeValid(responseString)) {

                                val body = JSONObject(responseString)
                                if (!body.getString("message").isNullOrBlank()) {
                                    showInfoDialog(body.getString("message"))
                                }

                                storeUserId(getUserIdFromJSON(responseString!!))
                                storeToken(getTokenFromJSON(responseString!!), email)

                                handleLoginType("loginSuccess")
                                logCompleteRegistrationEvent(this@LoginActivity, provider_name)

                            }
                        }

                    }
                })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d("LoginActivity", requestCode.toString() + "///" + resultCode + "//" + Activity.RESULT_OK + "//" + data)
        val googleSignedAccount = GoogleSignIn.getSignedInAccountFromIntent(data)
        Log.d("GogleSign=ActivityRslt", googleSignedAccount.toString())
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            RC_GOOGLE -> {
                val googleSignedAccount = GoogleSignIn.getSignedInAccountFromIntent(data)
                Log.d("GogleSign=ActivityRslt", googleSignedAccount.toString())
                if (googleSignedAccount.isSuccessful) {
                    val result = googleSignedAccount?.result
                    val id = result?.id
                    val name = result?.displayName
                    val email = result?.email
                    Log.d("LoginActivity", "onActivityResult: account =  ${result?.account}")
//                  toast("$id , $email , $name")
                    var firstName = name
                    var lastName = name

                    if (name?.contains(" ")!!) {
                        firstName = name.split(" ")[0]
                        lastName = name.split(" ")[1]
                    }

                    registerWithSocial(firstName!!, lastName!!, "google", id ?: "", email ?: "")


                }


            }

            RC_LINKEDIN -> {
                val firstName = data?.getStringExtra("first")
                val lastName = data?.getStringExtra("last")
                val id = data?.getStringExtra("id")
                val email = data?.getStringExtra("email")

                Log.d("LoginActivity", "onActivityResult: $firstName, $lastName, $id, $email")


                registerWithSocial(firstName!!, lastName!!, "linkedin", id!!, email!!)
            }
        }

        try {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun printHash() {

        val packageInfo = packageManager.getPackageInfo(packageName,
                PackageManager.GET_SIGNATURES)

        packageInfo.let {
            packageInfo.signatures.forEach {
                val messageDigest = MessageDigest.getInstance("SHA")
                messageDigest.update(it.toByteArray())
                val hash = String(Base64.encode(messageDigest.digest(), 0))
                Log.i("LoginActivity", "printHash: $hash")
            }
        }
    }

    private fun initRetrofitSigning() {


        val retrofitClientInterface = RetrofitClient.client
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        Log.d("LoginActivity", "initRetrofitSigning: IP = $ipAddress")


//        emailEditText.setText("singh.abhishek+1@augurs.in")
//        passwordEditText.setText( "123456" )

        val loginCallBack = object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("LoginActivity", "onFailure: ${t.message}")
                t.printStackTrace()
                snackbar(loginBtn, t.message.toString())
                progressDialog.dismiss()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val bodyString = response.body()?.string()

                Log.d("LoginActivity", "onResponse: $bodyString")

                progressDialog.dismiss()


                when (response.code()) {

                    200 -> {
                        response.body()?.let {

                            if (!isStatusCodeValid(bodyString)) {
                                if (getMessageFromJSON(bodyString).isEmpty())
                                    longSnackbar(loginBtn, getString(R.string.Invalidusernamepassword))
                                else
                                    longSnackbar(loginBtn, getMessageFromJSON(bodyString))
                                return
                            }

                            storeUserId(getUserIdFromJSON(bodyString!!))
                            val token = getTokenFromJSON(bodyString!!)
                            storeToken(token, emailEditText.text.toString())
                            clearTyreDetail()
                            startActivity(intentFor<HomeActivity>().putExtra("login_success", true).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)//.clearTask().clearTop()

                            )
                            // getUserAppSettings()
                            finishAffinity()
                        }
                    }

                    401 -> {
                        longSnackbar(loginBtn, getString(R.string.FailedtologinCheckyourcredential))
                    }

                    500 -> {
                        longSnackbar(loginBtn, getString(R.string.Requesttimedout))
                    }
                }


            }


        }
        loginBtn.setOnClickListener {

            hideKeyboard()
            if (!isOnline()) {
                Snackbar.make(loginBtn, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val callBack = retrofitClientInterface.login(emailEditText.text.toString(), getFCMToken(), passwordEditText.text.toString())
            callBack.enqueue(loginCallBack)
            progressDialog.show()

        }

    }


    private fun AlertReferralcode() {

        alert {
            message = getString(R.string.checkreferral)
            positiveButton(getString(R.string.yes)) {
                setBottomSheet()
            }
            negativeButton(getString(R.string.no)) {
                referralCode = ""
                if (socialmediaflag == 0) {
                    Facebooklogin()

                } else if (socialmediaflag == 1) {
                    Googlelogin()
                } else {
                    Linkedlogin()
                }
            }
        }.show()
    }

    private fun setBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_referral, null)
        dialog.setCancelable(false)
        bottomSheet.referralBtn.setOnClickListener {
            referralCode = bottomSheet.edt_referralcode.text.toString()
            if (referralCode.length > 1) {
                if (socialmediaflag == 0) {
                    Facebooklogin()
                } else if (socialmediaflag == 1) {
                    Googlelogin()
                } else {
                    Linkedlogin()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, getString(R.string.enterreferralcode), Toast.LENGTH_LONG).show()
            }
        }
        bottomSheet.dialogclose.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            referralCode = ""
        })

        dialog.setContentView(bottomSheet)
        dialog.show()

    }

    private fun Googlelogin() {
        startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE)
    }

    private fun Linkedlogin() {
        startActivityForResult(intentFor<LinkedInWebView>(), RC_LINKEDIN)
    }

    private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return
        }
        GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, object : GraphRequest.Callback {
            override fun onCompleted(graphResponse: GraphResponse) {
                LoginManager.getInstance().logOut()
            }
        }).executeAsync()
    }

    private fun Facebooklogin() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                arrayListOf("email", "public_profile", "user_friends"))

        disconnectFromFacebook()
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        result?.let {

                            val graphRequest = GraphRequest.newMeRequest(it.accessToken) { _, response ->
                                Log.d("LoginActivity", "onSuccess: $response")

                                if (response.error != null) {
                                    toast(response.error.errorMessage)

                                    return@newMeRequest
                                }

                                val json = response.jsonObject
                                var email: String = ""
                                var name: String = ""
                                var id: String = ""
                                try {
                                    if (json.has("id") && !json.isNull("id")) {
                                        id = json.getString("id")

                                    }
                                    if (json.has("name") && !json.isNull("name")) {
                                        name = json.getString("name") ?: ""

                                    }
                                    if (json.has("email") && !json.isNull("email")) {
                                        email = json.getString("email") ?: ""
                                    }


                                    var firstName = name
                                    var lastName = name

                                    if (name.contains(" ")) {
                                        firstName = name.split(" ")[0]
                                        lastName = name.split(" ")[1]
                                    }

                                    registerWithSocial(firstName, lastName, "facebook", id, email)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                            val parameters = Bundle()
                            parameters.putString("fields", "id,name,email,gender, birthday")
                            graphRequest.parameters = parameters
                            graphRequest.executeAsync()

                        }
                    }

                    override fun onCancel() {
                    }

                    override fun onError(error: FacebookException?) {
                        error?.let { toast(it.message.toString()) }
                        Log.d("Error", error.toString())
                    }

                })
    }


}
