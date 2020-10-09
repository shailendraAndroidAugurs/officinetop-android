package com.officinetop.officine.feedback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.Support.Support_Activity
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.json.JSONObject
import java.lang.Exception

class ComplaintTypeFragment : BaseActivity() {
    val mainCategoryList : MutableList<Models.CompCategory> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint_type)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.support_services)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
       // overridePendingTransition(R.anim.slide_up,R.anim.slide_up)
        getQuotesCategory()
    }
    private fun getQuotesCategory() {
        try{
            RetrofitClient.client.getComplaintCategory(getBearerToken()?:"")
                    .onCall { networkException, response ->

                        response?.let {
                            if (response.isSuccessful){
                                val body = JSONObject(response.body()?.string(
                                ))
                                if (body.has("data_set") && !body.isNull("data_set")) {
                                    val data = body.getJSONArray("data_set")
                                    for (i in 0 until data.length()){
                                        val modelData = Gson().fromJson<Models.CompCategory>(data.get(i).toString(), Models.CompCategory::class.java)
                                        mainCategoryList.add(modelData)
                                    }
                                    bindView()
                                }
                            }
                        }
                    }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    private fun bindView() {
        val genericAdapter = GenericAdapter<Models.CompCategory>(this, R.layout.item_complaint_type)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                Log.e("orderClickedItems::", "${mainCategoryList[position]}")
            }
            override fun onItemClick(view: View, position: Int) {
                Log.e("ClickedItems::", "${mainCategoryList[position].code}")
                val intent = Intent(this@ComplaintTypeFragment, Support_Activity::class.java)
                intent.putExtra("Cat_Id", mainCategoryList[position].code.toString())
                startActivity(intent)
                finish()
            }
        })
        recycler_view.adapter = genericAdapter
        genericAdapter.addItems(mainCategoryList)
    }
}
