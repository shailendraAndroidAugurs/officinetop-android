package com.officinetop.officine.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.getMessageFromJSON
import com.officinetop.officine.data.isStatusCodeValid
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.getProgressDialog
import com.officinetop.officine.utils.hideKeyboard
import com.officinetop.officine.utils.isOnline
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_reset_password.emailEditText
import kotlinx.android.synthetic.main.activity_reset_password.reset_password
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

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

            if(!isOnline()){
                Snackbar.make(loginBtn, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(TextUtils.isEmpty(emailEditText.text) ||
                    !Patterns.EMAIL_ADDRESS.matcher(emailEditText.text).matches()){
                emailEditText.error = "Email not valid"
                return@setOnClickListener
            }

            dialog.show()

            RetrofitClient.client.resetPassword(emailEditText.text.toString())
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            dialog.dismiss()}

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val body = response.body()?.string()
                            Log.d("ResetPasswordActivity", "onResponse: $body")
                            Log.d("ResetPasswordActivity", "onResponse: ${response.code()}")

                            dialog.dismiss()

                            when(response.code()){
                                200 -> {

                                       // snackbar(reset_password, getMessageFromJSON(body))
                                    alert {
                                        message = getMessageFromJSON(body)
                                        positiveButton(getString(R.string.opengmail)) {
                                            startActivity(Intent(packageManager.getLaunchIntentForPackage("com.google.android.gm")))
                                            finish()
                                        }
                                        negativeButton(getString(R.string.ok)) { finish()}
                                    }.show()

                                }
                                401 -> snackbar(reset_password,getString(R.string.Failedsendresetemail))
                                500 -> snackbar(reset_password,getString(R.string.Requesttimedout))
                            }

                        }


                    })
        }
    }

//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        if(item?.itemId == android.R.id.home) finish()
//        return super.onOptionsItemSelected(item)
//    }
}
