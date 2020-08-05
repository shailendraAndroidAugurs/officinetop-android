package com.officinetop.officine.push_notification

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.json.JSONArray
import org.json.JSONObject

class NotificationList : BaseActivity() {


    private val couponsListItem: MutableList<Models.NotificationList> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_list)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.notificationslist)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initview()
    }

    private fun initview() {
        RetrofitClient.client.getNotification(getBearerToken() ?: "")
                .onCall { _, response ->
                    response?.let {
                        if (response.isSuccessful) {
                            val body = JSONObject(response?.body()?.string())
                            if (body.has("data_set") && !body.isNull("data_set")) {
                                val dataSetArray = body.getJSONArray("data_set")
                                bindView(dataSetArray)
                            }
                        }
                    }
                }
    }

    private fun bindView(dataSetArray: JSONArray?) {
        val genericAdapter = GenericAdapter<Models.NotificationList>(this, R.layout.item_notificationlist)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                Log.e("orderClickedItems::", "${couponsListItem[position]}")
            }
            override fun onItemClick(view: View, position: Int) {
                Log.e("ClickedItems::", "${couponsListItem[position]}")
            }
        })
        for (i in 0 until dataSetArray!!.length()) {
            val data = Gson().fromJson<Models.NotificationList>(dataSetArray!!.get(i).toString(), Models.NotificationList::class.java)
            couponsListItem.add(data)
        }
        recycler_view.adapter = genericAdapter
        genericAdapter.addItems(couponsListItem)
    }


}
