package com.officinetop.officine

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.iid.FirebaseInstanceId
import com.officinetop.officine.authentication.LoginActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.setAppLanguage
import kotlinx.serialization.cbor.Cbor.Companion.context
import net.kibotu.urlshortener.UrlShortener
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.*

class SplashActivity : BaseActivity(){


    private val TAG = "MyFirebaseToken"
    private var googleApiClient: GoogleApiClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
       // getUserAppSettings()
        initView()
        clearStoreLatLong()
        Handler().postDelayed({

            if (!isLoggedIn() && isFirstRun()) {
                startActivity(intentFor<LoginActivity>())
                setFirstRun(false)
            } else {
                startActivity(intentFor<HomeActivity>())
            }


            finish()
        },1000)

        validateAppCode()

      /*  // Create a deep link and display it in the UI
        val newDeepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0)
        //linkViewSend.text = newDeepLink.toString()
        storeDeeplink(newDeepLink.toString())

        //shareDeepLink(newDeepLink.toString())

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    var deepLink: Uri? = null
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                    }
                    //Log.d("pendingDynamicLinkData", pendingDynamicLinkData.toString())
                    // Handle the deep link. For example, open the linked
                    // content, or apply promotional credit to the user's
                    // account.
                    // ...

                    // [START_EXCLUDE]
                    // Display deep link in the UI
                    if (deepLink != null) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Found deep link!", Snackbar.LENGTH_LONG).show()

                       // linkViewReceive.text = deepLink.toString()
                    } else {
                        Log.d(TAG, "getDynamicLink: no link found")
                    }
                    // [END_EXCLUDE]
                }
                .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
      */

    }



    fun shortenLongLink() {
        UrlShortener.enableLogging
        UrlShortener.shortenUrlByTinyUrl("https://services.officinetop.com/public/manage_referal_code?user=8089786756&inviteto=09098987676&Referalcode=ami009")
                .subscribeOn(Schedulers.newThread())
               // .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action1 { t ->
                    Log.v(TAG, "shortenUrlByGoogle " + t)


                })

       /* // [START shorten_long_link]
        val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse("https://services.officinetop.com?user=8089786756&inviteto=09098987676&Referalcode=ami009"))
                .buildShortDynamicLink()
                .addOnSuccessListener { result ->
                    // Short link created
                    val shortLink = result.shortLink
                    val flowchartLink = result.previewLink

                }
                .addOnFailureListener {
                    Log.e("shortLink", it.toString())
                    // Error
                    // ...
                }
        Log.e("shortLink", shortLinkTask.toString())*/
        // [END shorten_long_link]
    }


    private fun initView() {
        Thread(Runnable {
            try {
                Log.i(TAG, FirebaseInstanceId.getInstance().getToken(getString(R.string.SENDER_ID), "FCM"))
                FirebaseInstanceId.getInstance().getToken(getString(R.string.SENDER_ID),"FCM")?.let { storeUserFCMToken(it) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }




    //////////////////////////////////////////////linking////////////////////////////////
    private fun validateAppCode() {
        val uriPrefix = getString(R.string.dynamic_links_uri_prefix)
        if (uriPrefix.contains("YOUR_APP")) {
            context?.let {
                AlertDialog.Builder(this)
                        .setTitle("Invalid Configuration")
                        .setMessage("Please set your Dynamic Links domain in app/build.gradle")
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show()
            }
        }
    }

    companion object {

        private const val TAG = "MainActivity"
        private const val DEEP_LINK_URL = "https://services.officinetop.com/deeplinks"
    }

    @VisibleForTesting
    fun buildDeepLink(deepLink: Uri, minVersion: Int): Uri {
        val uriPrefix = "https://services.officinetop.com/public/manage_referal_code?user=8089786756&inviteto=09098987676&Referalcode=ami009"

        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        // [START build_dynamic_link]
        val builder = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setDomainUriPrefix(uriPrefix)
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(minVersion)
                        .build())
                .setLink(deepLink)

        // Build the dynamic link
        val link = builder.buildDynamicLink()
        // [END build_dynamic_link]

        // Return the dynamic link as a URI
        return link.uri
    }
    private fun shareDeepLink(deepLink: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link")
        intent.putExtra(Intent.EXTRA_TEXT, deepLink)

        startActivity(intent)
    }



    private fun getUserAppSettings() {
        getBearerToken()?.let {
            RetrofitClient.client.GetUpdatesettings(it).onCall { networkException, response ->
                networkException.let {
                    //ProgressDialog.dismiss()
                }
                response.let {
                    if (!response?.body().toString().isNullOrEmpty()) {
                        if (response != null) {
                            if (response.isSuccessful) {
                                val data = JSONObject(response.body()?.string())
                                if (data.has("data") && !data.isNull("data")) {
                                    val fulldata= data.getJSONObject("data")
                                    Log.d("DefaultLanguage","From Login:"+ fulldata.getString("lang"))
                                    storeLangLocale(fulldata.getString("lang"))
                                    setAppLanguage()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}

