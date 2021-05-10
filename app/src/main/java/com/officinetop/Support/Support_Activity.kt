package com.officinetop.Support

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.RecyclerViewAdapterChating
import com.officinetop.data.Models
import com.officinetop.data.getBearerToken
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.onCall
import kotlinx.android.synthetic.main.activity_support.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.json.JSONObject

class Support_Activity : BaseActivity() {


    private var selecttokenid = ""
    private var selectcatId = ""

    private var recyclerViewAdapter: RecyclerViewAdapterChating? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.support)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        intiView()
    }

    private fun intiView() {
        val Cat_Id = intent.getStringExtra("Cat_Id")
        if (Cat_Id != null) {
            selectcatId = Cat_Id
            selecttokenid = ""
            text_tagline.visibility = VISIBLE
        } else {
            val message_string = JSONObject(intent.getStringExtra("MSG"))

            if (message_string.has("status") && message_string.optString("status") == "C") {
                ll_forSendMessage.visibility = View.GONE
            } else {
                ll_forSendMessage.visibility = View.VISIBLE
            }
            if (message_string != null) {
                text_tagline.visibility = GONE
                val messageList = Gson().fromJson<Models.TicketList>(message_string.toString(), Models.TicketList::class.java)
                val messageModel = messageList.messages
                selecttokenid = messageList.ticketId
                selectcatId = messageList.ticket_type
                val linearLayoutManager = LinearLayoutManager(this)
                recycler_view_chatlist.layoutManager = linearLayoutManager
                recyclerViewAdapter = RecyclerViewAdapterChating(this, messageModel as MutableList<Models.Messages>, this)
                recycler_view_chatlist.adapter = recyclerViewAdapter
                val count = recyclerViewAdapter!!.changeposition()
                recycler_view_chatlist.scrollToPosition(count)
            } else {
                text_tagline.visibility = VISIBLE
            }
        }



        layout_send_message.setOnClickListener {
            if (edit_text_message.text.toString().isNotEmpty()) {
                sendMessage(edit_text_message.text.toString(), selectcatId, selecttokenid)
            } else {
                Toast.makeText(
                        applicationContext,
                        getString(R.string.enter_message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendMessage(message: String, keytype: String, tokenid: String) {
        RetrofitClient.client.generatesupportticket(
                getBearerToken() ?: "", message, tokenid, keytype
        ).onCall { networkException, response ->
            response?.let {
                if (response.isSuccessful) {
                    val responseData = JSONObject(response.body()?.string())
                    if (responseData.has("data") && !responseData.isNull("data")) {
                        val dataList = responseData.getJSONObject("data")
                        selecttokenid = dataList.getString("support_ticket_id")
                        getMessage()
                        edit_text_message.text = null
                    }
                }
            }
        }
    }

    private fun getMessage() {
        RetrofitClient.client.getTicketlist(getBearerToken() ?: "")
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set")) {
                                val dataSetArray = body.getJSONArray("data_set")
                                for (i in 0 until dataSetArray!!.length()) {
                                    val datalist = dataSetArray.getJSONObject(i)
                                    val tokenid = datalist.getString("id")
                                    if (tokenid == selecttokenid) {
                                        text_tagline.visibility = GONE
                                        val messagedata = Gson().fromJson<Models.TicketList>(dataSetArray.get(i).toString(), Models.TicketList::class.java)
                                        Log.d("get sendString support", messagedata.messages.toString())
                                        bindView(messagedata)
                                    }
                                }
                            }
                        }
                    }
                }
    }

    private fun bindView(messageList: Models.TicketList) {
        val messageModel = messageList.messages
        val linearLayoutManager = LinearLayoutManager(this)
        recycler_view_chatlist.layoutManager = linearLayoutManager
        recyclerViewAdapter = RecyclerViewAdapterChating(this, messageModel as MutableList<Models.Messages>, this)
        recycler_view_chatlist.adapter = recyclerViewAdapter
        val count = recyclerViewAdapter!!.changeposition()
        recycler_view_chatlist.scrollToPosition(count)

    }
}


