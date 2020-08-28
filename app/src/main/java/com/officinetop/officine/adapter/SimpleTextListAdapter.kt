package com.officinetop.officine.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.data.MeasurementDataSetItem
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.showConfirmDialog
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.item_text_only.view.*
import org.json.JSONObject

class SimpleTextListAdapter(private var context: Context, private var titleList: MutableList<MeasurementDataSetItem>, private var listenerRecycler: OnRecyclerItemClickListener) :
        RecyclerView.Adapter<SimpleTextListAdapter.SimpleTextViewHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SimpleTextViewHolder {
        //return object : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text_only, p0, false)){}

        return SimpleTextViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_text_only, p0, false))
    }

    override fun getItemCount(): Int = titleList.size

    override fun onBindViewHolder(p0: SimpleTextViewHolder, p1: Int) {


        val viewHolder = p0
        //"Front: ${it.width}/${it.aspectRatio} R${it.rimDiameter} ${it.speedindex} . Rear: ${it.width}/${it.aspectRatio} R${it.rimDiameter} ${it.speedindex}"
        val data: MeasurementDataSetItem = titleList.get(p1)



        val item = "${data.width}/${data.aspectRatio}  R${data.rimDiameter}  ${if (!data.speed_load_index.isNullOrBlank() && !data.speed_load_index.trim().equals("0") && !data.speed_load_index.trim().equals(context.getString(R.string.all)) && !data.speed_load_index.trim().equals(context.getString(R.string.all_in_italin))) data.speed_load_index.trim() else " "}   ${if (!data.speedindexStatus.isNullOrBlank() && !data.speedindexStatus.equals("0") && !data.speedindexStatus.trim().equals(context.getString(R.string.All)) && !data.speedindexStatus.trim().equals(context.getString(R.string.all_in_italin))) data.speedindexStatus else " "} "

        viewHolder.bindView(item, data.id.toString())

    }


    inner class SimpleTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemTitle = itemView.item_title
        val deleteItem = itemView.delete_item

        fun bindView(item: String, id: String) {
            itemTitle.text = item
            deleteItem.visibility = View.VISIBLE
            deleteItem.setOnClickListener {

                context.showConfirmDialog(context.resources.getString(R.string.are_u_sure_want_to_delete_this_item)) {


                    RetrofitClient.client.removeTyreDetail(context.getBearerToken() ?: "", id)
                            .onCall { networkException, response ->

                                response?.let {
                                    if (response.isSuccessful) {
                                        val body = JSONObject(response?.body()?.string())
                                        context.showInfoDialog(body.optString("message")) {
                                            listenerRecycler.onItemRemove()
                                            titleList.clear()
                                            notifyDataSetChanged()
                                        }
                                    }
                                }
                            }
                }
            }
            itemTitle.setOnClickListener {
                listenerRecycler.onItemClick(it, titleList[adapterPosition], adapterPosition)
            }
        }
    }

    interface OnRecyclerItemClickListener {
        fun onItemClick(view: View, title: MeasurementDataSetItem, position: Int)
        fun onItemRemove()
    }
}