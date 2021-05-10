package com.officinetop.Support

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.GenericAdapter
import com.officinetop.data.Models
import com.officinetop.data.getBearerToken
import com.officinetop.feedback.ComplaintTypeFragment
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.onCall
import kotlinx.android.synthetic.main.activity_genrated__user_ticket.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject

class Genrated_UserTicket : BaseActivity() {

    private val ticketListItem: MutableList<Models.TicketList> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genrated__user_ticket)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.support)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fab_new_complaint.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ComplaintTypeFragment::class.java)
            startActivityForResult(intent, 100)
        })
        initview()
    }

    private fun initview() {
        progress_bar_ticket.visibility = View.VISIBLE
        RetrofitClient.client.getTicketlist(getBearerToken() ?: "")
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set")) {
                                val dataSetArray = body.getJSONArray("data_set")
                                ticketListItem.clear()
                                bindView(dataSetArray)
                            } else {
                                tv_no_ticket_msg.visibility = View.VISIBLE
                                progress_bar_ticket.visibility = View.GONE
                            }
                        } else {
                            tv_no_ticket_msg.visibility = View.VISIBLE
                            progress_bar_ticket.visibility = View.GONE
                        }
                    }
                }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ticketListItem.clear()
        recycler_view.visibility = View.GONE
        initview()
    }

    private fun bindView(dataSetArray: JSONArray?) {
        val genericAdapter = GenericAdapter<Models.TicketList>(this, R.layout.item_ticket_list)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                Log.e("orderClickedItems::", "${ticketListItem.size}")
                if (ticketListItem.size == 0) {
                } else {
                    /*if(ticketListItem[position].status=="C"){
                        alert {
                            message =getString(R.string.ticketClose)
                            positiveButton(getString(R.string.yes)) {
                                val intent = Intent(this@Genrated_UserTicket, ComplaintTypeFragment::class.java)
                                startActivityForResult(intent,100)
                            }
                            negativeButton(getString(R.string.no)) {}
                        }.show()

                        *//* Toast.makeText(
                                 applicationContext,
                                 getString(R.string.ticketClose), Toast.LENGTH_LONG).show()*//*
                    }else{

                        startActivityForResult(intentFor<Support_Activity>(
                                "MSG" to Gson().toJson(ticketListItem[position]).toString()),100)

                    }*/
                    startActivityForResult(intentFor<Support_Activity>(
                            "MSG" to Gson().toJson(ticketListItem[position]).toString()), 100)

                }
            }

            override fun onItemClick(view: View, position: Int) {
                Log.e("ClickedItems::", "${ticketListItem[position]}")
            }
        })
        for (i in 0 until dataSetArray!!.length()) {
            val data = Gson().fromJson<Models.TicketList>(dataSetArray.get(i).toString(), Models.TicketList::class.java)
            ticketListItem.add(data)
        }
        recycler_view.visibility = View.VISIBLE
        recycler_view.adapter = genericAdapter
        genericAdapter.addItems(ticketListItem)
        progress_bar_ticket.visibility = View.GONE

    }
}
