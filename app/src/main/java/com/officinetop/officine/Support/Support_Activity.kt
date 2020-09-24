package com.officinetop.officine.Support

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.RecyclerViewAdapterChating
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import kotlinx.android.synthetic.main.activity_support.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.json.JSONObject

class Support_Activity : BaseActivity() {

    var mListRecyclerView = null
    private var selecttokenid = ""
    private var selectcat_id = ""

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
            selectcat_id = Cat_Id
            selecttokenid = ""
            text_tagline.visibility = VISIBLE
        } else {
            val message_string = JSONObject(intent.getStringExtra("MSG"))

            if (message_string.has("status") && message_string.optString("status") == "C") {
                ll_forSendMessage.visibility=View.GONE
            } else {
                ll_forSendMessage.visibility=View.VISIBLE
            }
            if (message_string != null) {
                text_tagline.visibility = GONE
                val messageList = Gson().fromJson<Models.TicketList>(message_string.toString(), Models.TicketList::class.java)
                val messagemodel = messageList.messages
                selecttokenid = messageList.ticketId
                selectcat_id = messageList.ticket_type
                val linearLayoutManager = LinearLayoutManager(this)
                recycler_view_chatlist.layoutManager = linearLayoutManager
                recyclerViewAdapter = RecyclerViewAdapterChating(messagemodel as MutableList<Models.Messages>, this)
                recycler_view_chatlist.adapter = recyclerViewAdapter
                val count = recyclerViewAdapter!!.changeposition()
                recycler_view_chatlist.scrollToPosition(count)
            } else {
                text_tagline.visibility = VISIBLE
            }
        }



        layout_send_message.setOnClickListener(View.OnClickListener {
            if (edit_text_message.text.toString().isNotEmpty()) {
                sendMessage(edit_text_message.text.toString(), selectcat_id, selecttokenid)
                Log.d("sendString support",edit_text_message.text.toString())
            } else {
                Toast.makeText(
                        applicationContext,
                        getString(R.string.entermessage), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sendMessage(message: String, keytype: String, tokenid: String) {
        RetrofitClient.client.generatesupportticket(
                getBearerToken() ?: "", message, tokenid, keytype
        ).onCall { networkException, response ->
            response?.let {
                if (response.isSuccessful) {
                    val responseData = JSONObject(response?.body()?.string())
                    if (responseData.has("data") && !responseData.isNull("data")) {
                        val datalist = responseData.getJSONObject("data")
                        // val comID=datalist.getJSONObject(0)
                        selecttokenid = datalist.getString("support_ticket_id")
                        getmessage()
                        edit_text_message.text = null
                    }
                }
            }
        }
    }

    private fun getmessage() {
        RetrofitClient.client.getTicketlist(getBearerToken() ?: "")
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response?.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set")) {
                                val dataSetArray = body.getJSONArray("data_set")
                                for (i in 0 until dataSetArray!!.length()) {
                                    val datalist = dataSetArray.getJSONObject(i)
                                    val tokenid = datalist.getString("id")
                                    if (tokenid == selecttokenid) {
                                        text_tagline.visibility = GONE
                                        val messagedata = Gson().fromJson<Models.TicketList>(dataSetArray!!.get(i).toString(), Models.TicketList::class.java)
                                        Log.d("get sendString support",messagedata.messages.toString())
                                        bindView(messagedata)
                                    }
                                }
                            }
                        }
                    }
                }
    }

    private fun bindView(messageList: Models.TicketList) {
        val messagemodel = messageList.messages
        val linearLayoutManager = LinearLayoutManager(this)
        recycler_view_chatlist.layoutManager = linearLayoutManager
        recyclerViewAdapter = RecyclerViewAdapterChating(messagemodel as MutableList<Models.Messages>, this)
        recycler_view_chatlist.adapter = recyclerViewAdapter
        val count = recyclerViewAdapter!!.changeposition()
        recycler_view_chatlist.scrollToPosition(count)

    }
}


