package com.officinetop.officine

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.getLangLocale
import com.officinetop.officine.data.isLoggedIn
import com.officinetop.officine.data.storeLangLocale
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.setAppLanguage
import org.json.JSONObject

class OfficineApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        FacebookSdk.fullyInitialize()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        if (isLoggedIn()) {
            getUserAppSettings()
        }
        if (getLangLocale() != null && getLangLocale() != "") {
            setAppLanguage()
        } else {
            storeLangLocale("it")
            setAppLanguage()
        }


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

                                    val fulldata = data.getJSONObject("data")
                                    Log.d("DefaultLanguage", "From Login:" + fulldata.getString("lang"))
                                    if (getLangLocale() != fulldata.getString("lang")) {
                                        UpdateSettings(getLangLocale())

                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun UpdateSettings(lang: String) {
        getBearerToken()?.let {
            RetrofitClient.client.Updatesettings(it, lang, "0", "0", "0").onCall { networkException, response ->
                response.let {
                    if (!response?.body().toString().isNullOrEmpty()) {
                        val res = response?.body()
                        Log.d("updateSettingCall", "yes")
                    }
                }
            }
        }
    }

}