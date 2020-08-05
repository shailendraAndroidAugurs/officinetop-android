package com.officinetop.officine.authentication

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.getFCMToken
import com.officinetop.officine.data.getMessageFromJSON
import com.officinetop.officine.data.isStatusCodeValid
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.getProgressDialog
import com.officinetop.officine.utils.hideKeyboard
import com.officinetop.officine.utils.isOnline
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.emailEditText
import kotlinx.android.synthetic.main.activity_register.passwordEditText
import kotlinx.android.synthetic.main.bottom_sheet_referral.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : BaseActivity() {


    lateinit var referralCode: String
    lateinit var checkbox_referral: CheckBox
    var uniqueId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.register)
        initRegistration()
        referralCode = ""
        setSpannableText()
        checkbox_referral = findViewById<CheckBox>(R.id.checkbox_referral)
        checkbox_referral.setOnClickListener(View.OnClickListener {

            if (checkbox_referral.isChecked) {
                setBottomSheet()
            } else {

                alert {
                    message = getString(R.string.removereferral)
                    positiveButton(getString(R.string.no)) {

                    }
                    negativeButton(getString(R.string.yes)) {
                        referralCode = ""
                    }
                }.show()

            }

        })
        uniqueId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }


    private fun initRegistration() {

        val dialog = getProgressDialog()
        dialog.setCancelable(false)

        registerBtn.setOnClickListener {

            hideKeyboard()

            if (!isOnline()) {
                Snackbar.make(loginBtn, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }


            if (!isValid(name, emailEditText, passwordEditText, mob_no)) {
                return@setOnClickListener

            }
            dialog.show()

            val callback = RetrofitClient.client.register(emailEditText.text.toString(),
                    passwordEditText.text.toString(), passwordEditText.text.toString(),
                    name.text.toString(), "", mob_no.text.toString(), getFCMToken(), referralCode,
                    "0", "", uniqueId!!)

            callback.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    dialog.dismiss()
                    it.snackbar(t.message!!)
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    val bodyString = response.body()?.string()
                    dialog.dismiss()

                    when (response.code()) {
                        200 -> {

                            if (!isStatusCodeValid(bodyString)) {
                                snackbar(registerBtn, getMessageFromJSON(bodyString))
                                return
                            }
                            val body = JSONObject(bodyString)
                            clearAll()

                            alert {
                                isCancelable = false
                                message = (if (!body.getString("message").isNullOrBlank()) {
                                    body.getString("message")
                                } else {
                                   getString(R.string.registered_successfully_verification_link_sent_on_your_registered_mail)
                                })
                                okButton {
                                    startActivity(
                                            intentFor<LoginActivity>().clearTask().clearTop()
                                    )
                                    finish()
                                }
                            }.show()


                        }

                        401 -> {
                            it.snackbar(JSONObject(response.errorBody()?.string()).getString("error"))
                        }

                        500 -> {
                            it.snackbar(getString(R.string.Unspecifiederroroccurred))
                        }


                    }
                }
            })
        }

    }

    private fun clearAll() {
        emailEditText.text.clear()
        passwordEditText.text.clear()
        confirmPasswordEditText.text.clear()
        mob_no.text.clear()
        name.text.clear()
        lastName.text.clear()
    }


    private fun isValid(vararg editText: EditText): Boolean {
        var valid = true
        editText.forEach {
            if (it.text.isEmpty()) {
                it.error = "Field required"
                valid = false
            }
        }

        if (!valid)
            return valid

//        if(passwordEditText.text.toString() != confirmPasswordEditText.text.toString()){
//            confirmPasswordEditText.error = "Password doesn't match"
//            passwordEditText.error = confirmPasswordEditText.error
//            valid = false
//            return valid
//        }


//        if(!TextUtils.isEmpty(emailEditText.text) &&
//                Patterns.EMAIL_ADDRESS.matcher(emailEditText.text).matches()) {
//            emailEditText.error = "Email not valid"
//            valid = false
//        }

        return valid
    }

    private fun setBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_referral, null)
        dialog.setCancelable(false)
        bottomSheet.referralBtn.setOnClickListener {
            referralCode = bottomSheet.edt_referralcode.text.toString()
            if (referralCode.length > 1) {
                dialog.dismiss()
            } else {
                Toast.makeText(this, getString(R.string.enterreferralcode), Toast.LENGTH_LONG).show()
            }
        }
        bottomSheet.dialogclose.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            referralCode = ""
            checkbox_referral.isChecked = false
        })

        dialog.setContentView(bottomSheet)
        dialog.show()

    }

    private fun setSpannableText() {

        val textMain = getString(R.string.register_privacy_text_1)

        val text1 = getString(R.string.Conditions)

        val termsAndConditions = SpannableString(text1)
        termsAndConditions.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                browse("https://officinetop.com")
            }

        }, 0, text1.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        // text.indexOf(getString(R.string.Conditions)), text.indexOf(getString(R.string.and)), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)


        val privacyPolicy = SpannableString(getString(R.string.register_privacy_text_2))
        privacyPolicy.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                browse("https://officinetop.com")
            }

        }, 0, privacyPolicy.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)



        register_privacy_text.text = TextUtils.concat(textMain, " ", termsAndConditions, " ", getString(R.string.and), " ", privacyPolicy)

        register_privacy_text.movementMethod = LinkMovementMethod.getInstance()
        register_privacy_text.highlightColor = Color.TRANSPARENT
    }
}
