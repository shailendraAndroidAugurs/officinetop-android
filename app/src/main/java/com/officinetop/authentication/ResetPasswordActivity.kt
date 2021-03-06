package com.officinetop.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.google.android.material.snackbar.Snackbar
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.data.getMessageFromJSON
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.hideKeyboard
import com.officinetop.utils.isOnline
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_reset_password.emailEditText
import kotlinx.android.synthetic.main.activity_reset_password.reset_password
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.design.snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.reset_password)

        reset_password.setOnClickListener { view ->
            val dialog = getProgressDialog()

            hideKeyboard()

            if (!isOnline()) {
                Snackbar.make(loginBtn, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(emailEditText.text) ||
                    !Patterns.EMAIL_ADDRESS.matcher(emailEditText.text).matches()) {
                emailEditText.error = getString(R.string.email_not_valid)
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            dialog.show()

            RetrofitClient.client.resetPassword(emailEditText.text.toString())
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            dialog.dismiss()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val body = response.body()?.string()
                            Log.d("ResetPasswordActivity", "onResponse: $body")
                            Log.d("ResetPasswordActivity", "onResponse: ${response.code()}")

                            dialog.dismiss()

                            when (response.code()) {
                                200 -> {
                                    alert {
                                        message = getMessageFromJSON(body)
                                        positiveButton(getString(R.string.open_gmail)) {
                                            startActivity(Intent(packageManager.getLaunchIntentForPackage("com.google.android.gm")))
                                            finish()
                                        }
                                        negativeButton(getString(R.string.ok)) { finish() }
                                    }.show()

                                }
                                401 -> reset_password.snackbar(getString(R.string.Failedsendresetemail))
                                500 -> reset_password.snackbar(getString(R.string.Requesttimedout))
                            }

                        }


                    })
        }
    }

}
