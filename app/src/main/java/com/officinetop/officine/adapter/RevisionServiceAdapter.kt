package com.officinetop.officine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.workshop.WorkshopListActivity
import com.officinetop.officine.data.RevDataSetItem
import kotlinx.android.synthetic.main.item_revision_service_cell.view.*
import org.jetbrains.anko.intentFor

class RevisionServiceAdapter(context: Context, revisionServiceList: MutableList<RevDataSetItem?>?): RecyclerView.Adapter<RevisionServiceAdapter.RevisionServiceViewHolder>() {

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
            it[position]?.let {
            holder.revTitle.text = it.categoryName
            holder.revDesc.text = it.description
            holder.revMinPrice.text = mContext.getString(R.string.prepend_euro_symbol_with_from_string, it.minPrice)

            holder.revChooseWorkshopBtn.setOnClickListener {view->

                mContext.startActivity(mContext.intentFor<WorkshopListActivity>(
                        com.officinetop.officine.utils.Constant.Key.is_revision to true,
                        com.officinetop.officine.utils.Constant.Path.revisionServiceDetails to it))
            }
            }
        }
    }

    override fun getItemCount(): Int {
        return mRevisionServiceList?.count()?: 0
    }

    class RevisionServiceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var revTitle = itemView.rev_title
        var revDesc = itemView.rev_desc
        var revMinPrice = itemView.rev_min_price
        var revChooseWorkshopBtn = itemView.rev_choose_workshop
    }
}