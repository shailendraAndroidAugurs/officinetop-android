package com.officinetop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.R
import com.officinetop.data.RevDataSetItem
import com.officinetop.utils.addReadMore
import com.officinetop.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.item_revision_service_cell.view.*
import org.jetbrains.anko.intentFor

class RevisionServiceAdapter(context: Context, revisionServiceList: MutableList<RevDataSetItem?>?) : RecyclerView.Adapter<RevisionServiceAdapter.RevisionServiceViewHolder>() {

    private lateinit var mContext: Context
    private var mRevisionServiceList: MutableList<RevDataSetItem?>?

    init {
        mContext = context
        mRevisionServiceList = revisionServiceList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevisionServiceViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_revision_service_cell, parent, false)
        return RevisionServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: RevisionServiceViewHolder, position: Int) {

        mRevisionServiceList?.let {
            it[position]?.let { it1 ->
                holder.revTitle.text = it1.categoryName
                holder.revDesc.text = it1.description
                holder.revMinPrice.text = if (!it1.minPrice.isNullOrBlank()) mContext.getString(R.string.prepend_euro_symbol_with_from_string, it1.minPrice) else mContext.getString(R.string.prepend_euro_symbol_with_from_string, "0")


                holder.revChooseWorkshopBtn.setOnClickListener { view ->

                    mContext.startActivity(mContext.intentFor<WorkshopListActivity>(
                            com.officinetop.utils.Constant.Key.is_revision to true,
                            com.officinetop.utils.Constant.Path.revisionServiceDetails to it1))
                }
            }
        }

        if(holder.revDesc.text.toString().length >= 150){
            mContext.addReadMore(holder.revDesc.text.toString(), holder.revDesc)
        }

    }

    override fun getItemCount(): Int {
        return mRevisionServiceList?.count() ?: 0
    }

    class RevisionServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var revTitle = itemView.rev_title
        var revDesc = itemView.rev_desc
        var revMinPrice = itemView.rev_min_price
        var revChooseWorkshopBtn = itemView.rev_choose_workshop
    }
}