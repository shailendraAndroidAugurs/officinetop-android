package com.officinetop.officine

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_search_preview.*

class SearchPreview : AppCompatActivity() {
    lateinit var rv_OENSearch: RecyclerView
    lateinit var rv_Partearch: RecyclerView
    lateinit var rv_ProductSearch: RecyclerView
    val size = 10
    var layoutheight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_preview)
        rv_OENSearch = findViewById(R.id.rv_OENSearch)
        rv_Partearch = findViewById(R.id.rv_Partearch)
        rv_ProductSearch = findViewById(R.id.rv_ProductSearch)
        OENSerachBindInView()
        PartSerachBindInView()
        ProductSerachBindInView()


    }

    private fun OENSerachBindInView() {


        class Holder(view: View) : RecyclerView.ViewHolder(view)

        val myadpter = object : RecyclerView.Adapter<Holder>() {

            override fun getItemCount(): Int {
                return size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.item_list_address, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {


            }
        }


        rv_OENSearch.adapter = myadpter



        if (size >= 5) {
            for (i in 0 until 5) {
                layoutheight = 65 * 5 + 50
            }
        } else {
            for (i in 0 until size) {
                layoutheight = 50 * size + size * 10
            }
        }

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, layoutheight.toFloat(), getResources().getDisplayMetrics())
        var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())
        llOEn.layoutParams = param
    }

    private fun PartSerachBindInView() {


        class Holder(view: View) : RecyclerView.ViewHolder(view)

        val myadpter = object : RecyclerView.Adapter<Holder>() {

            override fun getItemCount(): Int {
                return size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.item_list_address, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {


            }
        }


        rv_Partearch.adapter = myadpter
    }

    private fun ProductSerachBindInView() {


        class Holder(view: View) : RecyclerView.ViewHolder(view)

        val myadpter = object : RecyclerView.Adapter<Holder>() {

            override fun getItemCount(): Int {
                return size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return Holder(layoutInflater.inflate(R.layout.item_list_address, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {


            }
        }


        rv_ProductSearch.adapter = myadpter
    }


}
