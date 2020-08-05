package com.officinetop.officine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.car_parts.PartCategoryInterface
import com.officinetop.officine.utils.loadImage
import kotlinx.android.synthetic.main.item_part_detail.view.*
import org.json.JSONArray

class PartCategoryAdapter(categoryArray: JSONArray, partCategoryInterface: PartCategoryInterface): RecyclerView.Adapter<PartCategoryAdapter.PartViewHolder>(){

    private var mCategoryArrayList: JSONArray
    lateinit var context: Context
    var mView: PartCategoryInterface

    init {
        mCategoryArrayList = categoryArray
        mView = partCategoryInterface
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        context = parent.context
        var view = LayoutInflater.from(context).inflate(R.layout.item_part_detail, parent, false)
        return PartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {

        //get details of each category
        var categoryDetails = mCategoryArrayList.getJSONObject(position)

        if(!categoryDetails.isNull("images")) {
            context.loadImage(categoryDetails.getJSONArray("images").getJSONObject(0).getString("image_url"), holder.partImage)
        }

        holder.partName.text = categoryDetails.optString("group_name", categoryDetails.optString("item"))

        if(position == 0)
            mView?.onCategoryClicked(categoryDetails.getInt("id"))
//            holder.partContainer.performClick()

        holder.partContainer.setOnClickListener {
            mView?.onCategoryClicked(categoryDetails.getInt("id"))
        }
    }

    override fun getItemCount(): Int {
        return mCategoryArrayList.length()
    }

    class PartViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        var partContainer = view.part_container
        var partImage: ImageView = view.part_image
        var partName: TextView = view.part_name
    }
}