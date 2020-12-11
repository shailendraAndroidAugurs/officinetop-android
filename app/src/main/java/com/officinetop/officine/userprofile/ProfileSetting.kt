package com.officinetop.officine.userprofile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.R
import com.officinetop.officine.authentication.LoginActivity
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.getLangLocale
import com.officinetop.officine.data.removeUserDetail
import com.officinetop.officine.data.storeLangLocale
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.setAppLanguage
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_profile__setting.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.json.JSONObject

class ProfileSetting : BaseActivity() {


    private var lang: String = "it"
    private var notification: String = ""
    var carservices: String = ""
    var carRevision: String = ""
    private var privacy_policy: String? = null
    private var Terms_and_Conditions: String? = null
    private var Cookies_information: String? = null
    private var How_does_it_work: String? = null
    private var isLanguageChanged = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile__setting)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initView()


    }

    private fun settextInView() {
        toolbar_title.text = getString(R.string.profile_setting)
        button_updatesetting.setText(R.string.update)
        tv_preferredlanguage.setText(R.string.language)
        radiobuton_english.setText(R.string.language_eng)
        radiobuton_italy.setText(R.string.language_ita)
        tv_notificationSetting.setText(R.string.notification_setting)
        tv_send_me_notification.setText(R.string.notification_newOffers)
        tv_carWashingOffer.setText(R.string.notification_workshop)
        tv_notify_me_about_my_car_revision.setText(R.string.notification_revision)
        tv_information.setText(R.string.app_info)
        tv_who_we_are.setText(R.string.whoweare)
        tv_privacy.setText(R.string.privacy)
        tv_Contact.setText(R.string.contactus)
        tv_condition_purchage.setText(R.string.condition)
        textview_whowe_are.setText(R.string.readme)
        textview_privacy.setText(R.string.readme)
        textview_contact.setText(R.string.readme)
        textview_conditions.setText(R.string.readme)
        textview_deleteaccount.setText(R.string.account_delete)
    }

    private fun initView() {
        toolbar_title.text = getString(R.string.profile_setting)

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            switch_notification.isChecked = true
        } else {
            switch_notification.isChecked = false
        }


        button_updatesetting.setOnClickListener(View.OnClickListener {
            UpdateSettings(lang)
        })
        textview_deleteaccount.setOnClickListener(View.OnClickListener {
            alert {
                message = getString(R.string.deleteaccount)
                positiveButton(getString(R.string.yes)) {
                    deleteAccount()
                }
                negativeButton(getString(R.string.no)) {
                }
            }.show()
        })
        val language = getLangLocale()
        if (language != null && language != "" && language == "en") {
            radiobuton_english.isChecked = true
            radiobuton_italy.isChecked = false
            lang = "en"
        } else if (language != null && language != "" && language == "it") {
            radiobuton_italy.isChecked = true
            radiobuton_english.isChecked = false
            lang = "it"
        }

        switch_notification?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                notification = "1"
            else notification = "0"
            //Toast.makeText(this@ProfileSetting,notification, Toast.LENGTH_LONG).show()
        }
        if (switch_notification.isChecked) {
            notification = "1"
        } else {
            notification = "0"
        }

        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.notify, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_carservices.adapter = adapter
        spinner_carRevision.adapter = adapter


        if (!carservices.isNullOrBlank()) {
            spinner_carservices.setSelection(Integer.parseInt(carservices))

        }
        if (!carRevision.isNullOrBlank()) {

            spinner_carRevision.setSelection(Integer.parseInt(carRevision))

        }

        spinner_carservices?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Toast.makeText(this@ProfileSetting,spinner_carservices.selectedItem.toString()+ position, Toast.LENGTH_SHORT).show()
                carservices = position.toString()
            }
        }
        spinner_carRevision?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                carRevision = position.toString()
                // Toast.makeText(this@ProfileSetting,spinner_carRevision.selectedItem.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        radiogroup_language.setOnCheckedChangeListener(
                RadioGroup.OnCheckedChangeListener { group, checkedId ->
                    val radio: RadioButton = findViewById(checkedId)
                    if (radio.tag == "en") {
                        lang = "en"
                        //UpdateSettings()
                    } else {
                        lang = "it"
                        //UpdateSettings()
                    }
                    /*  Toast.makeText(applicationContext,language,
                              Toast.LENGTH_SHORT).show()*/
                })

        textview_whowe_are.setOnClickListener(View.OnClickListener {


            How_does_it_work?.let { it1 -> browse(it1) }
        })
        textview_privacy.setOnClickListener(View.OnClickListener {
            privacy_policy?.let { it1 -> browse(it1) }
        })
        textview_contact.setOnClickListener(View.OnClickListener {
            Cookies_information?.let { it1 -> browse(it1) }
        })
        textview_conditions.setOnClickListener(View.OnClickListener {
            Terms_and_Conditions?.let { it1 -> browse(it1) }
        })
        getUpdateSettings()

    }

    private fun UpdateSettings(lang: String) {
        getBearerToken()?.let {
            RetrofitClient.client.updatesettings(it, lang, notification, carservices, carRevision).onCall { networkException, response ->
                response.let {
                    if (!response?.body().toString().isNullOrEmpty()) {
                        val res = response?.body()
                        isLanguageChanged = true
                        storeLangLocale(lang)
                        setAppLocale()
                        settextInView()
                        initView()
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.Update_SuccessFully), Snackbar.LENGTH_SHORT).show()
                        getUpdateSettings()
                    }
                }
            }
        }
    }

    private fun deleteAccount() {
        try {
            getBearerToken()?.let {
                RetrofitClient.client.deleteAccount(it).onCall { networkException, response ->
                    response.let {
                        if (!response?.body().toString().isNullOrEmpty()) {
                            val res = response?.body()
                            Log.e("RES", response?.body().toString())
                            showInfoDialog("Account Deleted Successfully", false) {
                                removeUserDetail()
                                val i = Intent(applicationContext, LoginActivity::class.java)        // Specify any activity here e.g. home or splash or login etc
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(i)
                                finish()
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getUpdateSettings() {
        //var ProgressDialog = getProgressDialog(true)
        try {
            getBearerToken()?.let {
                RetrofitClient.client.getUpdatesettings(it).onCall { networkException, response ->
                    networkException.let {
                        //ProgressDialog.dismiss()
                    }
                    response.let {
                        if (!response?.body().toString().isNullOrEmpty()) {
                            if (response != null) {
                                if (response.isSuccessful) {
                                    // ProgressDialog.dismiss()
                                    val data = JSONObject(response.body()?.string())
                                    if (data.has("data") && !data.isNull("data")) {
                                        val fulldata = data.getJSONObject("data")
                                        if (fulldata.has("lang") && !fulldata.isNull("lang"))
                                            lang = fulldata.getString("lang")
                                        if (fulldata.has("notification_setting") && !fulldata.isNull("notification_setting"))
                                            notification = fulldata.getString("notification_setting")
                                        if (fulldata.has("notification_for_offer") && !fulldata.isNull("notification_for_offer"))
                                            carservices = fulldata.getString("notification_for_offer")
                                        if (fulldata.has("notification_for_revision") && !fulldata.isNull("notification_for_revision"))
                                            carRevision = fulldata.getString("notification_for_revision")
                                        if (fulldata.has("privacy_policy") && !fulldata.isNull("privacy_policy"))
                                            privacy_policy = fulldata.getString("privacy_policy")
                                        if (fulldata.has("Terms_and_Conditions") && !fulldata.isNull("Terms_and_Conditions"))
                                            Terms_and_Conditions = fulldata.getString("Terms_and_Conditions")
                                        if (fulldata.has("Cookies_information") && !fulldata.isNull("Cookies_information"))
                                            Cookies_information = fulldata.getString("Cookies_information")
                                        if (fulldata.has("who_we_are") && !fulldata.isNull("who_we_are"))
                                            How_does_it_work = fulldata.getString("who_we_are")

                                        if (getLangLocale() != lang) {
                                            UpdateSettings(getLangLocale())
                                        }
                                        switch_notification.isChecked = notification == "1"

                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun recreateActivity() {
        recreate()
    }

    private fun setAppLocale() {
        setAppLanguage()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {//because on click back arrow, ProfileFragment ui not get updated, so we manually remove and replace ProfileFragment from HomeActivity again .
                ifLangChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /*//if language changed, navigate to HomeActivity to reload ProfileFragment to update ui
    */
    private fun ifLangChanged() {
        try {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            intent.putExtra("loadProfileFragment", true)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        ifLangChanged()
    }


}
