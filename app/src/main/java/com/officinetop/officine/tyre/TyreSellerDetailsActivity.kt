package com.officinetop.officine.tyre

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.SimpleTextListAdapter
import com.officinetop.officine.utils.getDate
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_text_only.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.json.JSONArray
import org.json.JSONObject

class TyreSellerDetailsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyre_seller_details)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = "Tyre Seller Detail"


        val seller_detail = JSONArray(intent?.getStringExtra("tyreSellerDetails"))


        class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val title = view.item_title
            val cellView = view.tyre_seller_view

        }
        recycler_view?.adapter = object : RecyclerView.Adapter<ViewHolder>(){
            override fun getItemCount(): Int = seller_detail.length()

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val jsonObject = seller_detail.get(position) as JSONObject
                holder.title.text = "pfu amount = "+jsonObject.getString("add_money")+", Delivery Date: "+ getDate(jsonObject.getString("no_of_days").toInt())

                holder.cellView.setOnClickListener {
                    val intent = Intent()
                    intent.putExtra("result",jsonObject.toString())
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return ViewHolder(layoutInflater.inflate(R.layout.item_text_only,parent,false))
            }
        }

        }


    }


