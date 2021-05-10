package com.officinetop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.R
import com.officinetop.data.MeasurementDataSetItem
import com.officinetop.data.getBearerToken
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.onCall
import com.officinetop.utils.showConfirmDialog
import com.officinetop.utils.showInfoDialog
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
        val data: MeasurementDataSetItem = titleList.get(p1)


        val item = "${data.width}/${data.aspectRatio}  R${data.rimDiameter}  ${if (!data.speed_load_index.isNullOrBlank() && data.speed_load_index.trim() != "0" && data.speed_load_index.trim() != context.getString(R.string.all) && data.speed_load_index.trim() != context.getString(R.string.all_in_italin)) data.speed_load_index.trim() else " "}   ${if (!data.speedindexStatus.isNullOrBlank() && data.speedindexStatus != "0" && data.speedindexStatus.trim() != context.getString(R.string.All) && data.speedindexStatus.trim() != context.getString(R.string.all_in_italin)) data.speedindexStatus else " "} "

        viewHolder.bindView(item, data.id.toString())

    }


    inner class SimpleTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemTitle = itemView.item_title
        private val deleteItem = itemView.delete_item
        private val editItem = itemView.edit_item
        fun bindView(item: String, id: String) {
            itemTitle.text = item
            deleteItem.setOnClickListener {

                context.showConfirmDialog(context.resources.getString(R.string.are_u_sure_want_to_delete_this_item)) {


                    RetrofitClient.client.removeTyreDetail(context.getBearerToken() ?: "", id)
                            .onCall { networkException, response ->

                                response?.let {
                                    if (response.isSuccessful) {
                                        val body = JSONObject(response.body()?.string())
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
            editItem.setOnClickListener {
                listenerRecycler.onItemClick(it, titleList[adapterPosition], adapterPosition, true)
            }

            itemTitle.setOnClickListener {
                listenerRecycler.onItemClick(it, titleList[adapterPosition], adapterPosition)
            }
        }
    }

    interface OnRecyclerItemClickListener {
        fun onItemClick(view: View, title: MeasurementDataSetItem, position: Int, Isedit: Boolean = false)
        fun onItemRemove()

    }
}