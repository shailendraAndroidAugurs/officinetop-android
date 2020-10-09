package com.officinetop.officine.WishList

import android.os.Bundle
import com.google.gson.GsonBuilder
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.getProgressDialog
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_wish_list.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.json.JSONArray
import org.json.JSONObject

class WishListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.wishList)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getWishList()

    }

    private fun bindRecyclerView(WishListAdapter: ArrayList<Models.WishList>) {
        val wishListadpater = WishListAdapter(this, WishListAdapter)
        recycler_view_WishList.adapter = wishListadpater
    }


    private fun getWishList() {

        var wishListIterator: ArrayList<Models.WishList> = ArrayList<Models.WishList>()
        val ProgressDialog = getProgressDialog(true)
        RetrofitClient.client.getUserWishList(getBearerToken() ?: "")
                .onCall { networkException, response ->
                    networkException.let {
                        ProgressDialog.dismiss()
                    }
                    response?.let {
                        if (response.isSuccessful) {

                            val data = JSONObject(response.body()?.string())
                            if (data.has("data_set") && !data.isNull("data_set")) {
                                val dataSet = data.get("data_set") as JSONArray
                                val gson = GsonBuilder().create()
                                wishListIterator = gson.fromJson(dataSet.toString(), Array<Models.WishList>::class.java).toCollection(java.util.ArrayList<Models.WishList>())
                                bindRecyclerView(wishListIterator)

                            } else {
                                if (data.has("message") && !data.isNull("message")) {
                                    showInfoDialog(data.get("message").toString())
                                }
                            }
                        }
                        ProgressDialog.dismiss()
                    }
                }

    }
}
