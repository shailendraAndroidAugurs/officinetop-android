package com.officinetop.officine.coupons


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_coupons.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_coupons.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.json.JSONArray
import org.json.JSONObject


class CouponsActivity : BaseActivity() {

    private val couponsListItem: MutableList<Models.AllCoupons> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupons)
        initView()
    }


    private fun initView() {

        setSupportActionBar(toolbar)
        toolbar_title.text = "Coupons"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        terms_arrow.setOnClickListener {
            ll_terms.visibility = if (ll_terms.visibility == View.VISIBLE) View.GONE else View.VISIBLE

        }

        RetrofitClient.client.getAllCoupons(getBearerToken() ?: "")
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

        val selectItemMap: HashMap<String, JSONObject> = HashMap<String, JSONObject>()


        /*recycler_view?.setJSONArrayAdapter(this,jsonArray,R.layout.layout_coupons){
            itemView, position, jsonObject ->

            with(itemView){

                itemView.item_info.text = jsonObject.optString("item_info")
                itemView.item_text.text = jsonObject.optString("item_text")
                itemView.item_type.text = jsonObject.optString("item_type")
                itemView.item_price.text = jsonObject.optString("item_price")
                val dataList = ArrayList<String>()
                val array = jsonObject.getJSONArray("item_quantity")
                for (i in 0 until array.length()){
                    dataList.add(array.get(i).toString())
                }

                spinner_item_quantity?.setSampleSpinnerAdapter(context,dataList)
                spinner_item_quantity?.setSelection(0)

                var item_qty  = ""
                val jsonObj = JSONObject()

                spinner_item_quantity?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        itemView.item_price.text = ((position+1) * jsonObject.optString("item_price").replace("$","").toFloat()).toString()
                        Log.d("item price changes:", ((position+1) * jsonObject.optString("item_price").replace("$","").toFloat()).toString()+ position)
                        item_qty = p0?.getItemAtPosition(p2).toString()
                        jsonObj.put("item_info",jsonObject.optString("item_info"))
                        jsonObj.put("item_text",jsonObject.optString("item_text"))
                        jsonObj.put("item_type",jsonObject.optString("item_type"))
                        jsonObj.put("item_price",jsonObject.optString("item_price"))
                        jsonObj.put("item_quantity",item_qty)

                        if (selectItemMap.containsKey(jsonObj.optString("item_text")))
                            selectItemMap.put(jsonObject.optString("item_text"),jsonObj)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }

                itemView.item_type?.setOnCheckedChangeListener{buttonView, isChecked ->
                    if (isChecked){
                        jsonObj.put("item_info",jsonObject.optString("item_info"))
                        jsonObj.put("item_text",jsonObject.optString("item_text"))
                        jsonObj.put("item_type",jsonObject.optString("item_type"))
                        jsonObj.put("item_price",jsonObject.optString("item_price"))
                        jsonObj.put("item_quantity",item_qty)
                        selectItemMap.put(jsonObject.optString("item_text"),jsonObj)
                    }else{
                        selectItemMap.remove(jsonObject.optString("item_text"))
                    }
                }


                submit.setOnClickListener {
                    for ((k,v) in selectItemMap){
                        println("$k = $v")
                    }

                }

            }
        }*/

    }

    private fun bindView(dataSetArray: JSONArray?) {

        val genericAdapter = GenericAdapter<Models.AllCoupons>(this, R.layout.layout_coupons)

        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                Log.e("couponsClickedItems::", "${couponsListItem[position]}")
            }

            override fun onItemClick(view: View, position: Int) {
            }
        })
        for (i in 0 until dataSetArray!!.length()) {
            val data = Gson().fromJson<Models.AllCoupons>(dataSetArray!!.get(i).toString(), Models.AllCoupons::class.java)
            couponsListItem.add(data)
        }

        recycler_view.adapter = genericAdapter
        genericAdapter.addItems(couponsListItem)


        /* recycler_view?.setAdapter(this,couponsListItem,R.layout.layout_coupons){
             itemView, position, itemList ->

             with(itemView){
                 item_info.text = itemList.couponTitle
                 item_price.text = getString(R.string.prepend_euro_symbol_string, itemList.amount.toString())
             }
         }*/
    }

}






