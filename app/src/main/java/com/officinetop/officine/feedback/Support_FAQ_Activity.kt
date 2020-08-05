package com.officinetop.officine.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.gson.GsonBuilder
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.FAQ_Adapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_support__f_a_q_.*
import kotlinx.android.synthetic.main.include_toolbar.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Support_FAQ_Activity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support__f_a_q_)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.FAQtitle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getFAQ_Data_fromServer()
        ll_call_phone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + getString(R.string.phoneNo))
            startActivity(intent)
        }
        ll_chat_online.setOnClickListener {
            startActivity(intentFor<Genrated_UserTicket>())
        }
        ll_email.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                val recipients = arrayOf(getString(R.string.emailAddress))
                intent.putExtra(Intent.EXTRA_EMAIL, recipients)
                intent.type = "text/html"
                intent.setPackage("com.google.android.gm")
                startActivity(Intent.createChooser(intent, "Send mail"))
            } catch (e: Exception) {

            }
        }


    }

    private fun getFAQ_Data_fromServer() {
        progress_bar.visibility= View.VISIBLE
        RetrofitClient.client.getFAQ_Data(getBearerToken()
                ?: "")
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progress_bar.visibility= View.GONE
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        progress_bar.visibility= View.GONE
                        if (response.isSuccessful) {
                            try {
                                var body = JSONObject(response.body()?.string())
                                Log.d("FAQ_Api_Call", "yes")
                                if (body.has("data_set") && body.get("data_set") != null) {
                                    val jsonarray = body.get("data_set") as JSONArray
                                    val gson = GsonBuilder().create()
                                    var fAQ_Question_AnswerList = gson.fromJson(jsonarray.toString(), Array<Models.FAQ_Question_Answer>::class.java).toCollection(java.util.ArrayList<Models.FAQ_Question_Answer>())
                                    rv_FaqQuestion_Answer.adapter = FAQ_Adapter(this@Support_FAQ_Activity, fAQ_Question_AnswerList)
                                }


                            } catch (e: Exception) {

                                e.printStackTrace()
                            }

                        }


                    }
                })
    }
}
