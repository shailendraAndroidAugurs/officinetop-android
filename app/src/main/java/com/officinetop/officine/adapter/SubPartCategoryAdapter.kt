package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.car_parts.PartCategoryInterface
import com.officinetop.officine.data.DataSetItem
import com.officinetop.officine.data.DataSetSubGroupCatItem
import com.officinetop.officine.utils.loadImage
import com.officinetop.officine.utils.loadImageWithName


class SubPartCategoryAdapter(subGroupArrayList: MutableList<DataSetItem?>, subN3GroupArrayList: MutableList<DataSetSubGroupCatItem?>, partCategoryInterface: PartCategoryInterface) : BaseExpandableListAdapter() {

    private lateinit var mSubGroupArrayList: MutableList<DataSetItem?>
    private lateinit var mSubN3GroupArrayList: MutableList<DataSetSubGroupCatItem?>
    private lateinit var mView: PartCategoryInterface
    private var mContext: Context? = null

    init {
        mSubGroupArrayList = subGroupArrayList
        mSubN3GroupArrayList = subN3GroupArrayList
        mView = partCategoryInterface
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


//    override fun registerDataSetObserver(observer: DataSetObserver?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }


    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childView = LayoutInflater.from(parent?.context).inflate(R.layout.item_subgroup_child, parent, false)

        val childGridView = childView.findViewById(R.id.n3_subgroup_category) as RecyclerView
        childGridView.adapter = ChildViewAdapter(mSubN3GroupArrayList, mContext, mView)
        return childView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        mContext = parent?.context
        val groupView = LayoutInflater.from(mContext).inflate(R.layout.item_group_header, parent, false)
        try {


            val subCategoryDetails = mSubGroupArrayList.get(groupPosition)
            val subCategoryName = subCategoryDetails?.groupName

            val title = groupView.findViewById<TextView>(R.id.sub_category_title)
            val subCategoryImage = groupView.findViewById<ImageView>(R.id.sub_category_image)

            title.text = subCategoryName

            if (subCategoryDetails?.images != null)
                mContext?.loadImage(subCategoryDetails.images[0].imageUrl, subCategoryImage)

            // set expanded/ collapsed arrow image
            val groupIndicator = groupView.findViewById<ImageView>(R.id.group_indicator)
            val groupImageRotation = if (isExpanded) 180.0f else 0.0f
            groupIndicator.rotation = groupImageRotation

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return groupView

    }

    override fun getGroupCount(): Int {
        return mSubGroupArrayList.size
    }

    // Child GridView adapter
    class ChildViewAdapter(subN3GroupArrayList: MutableList<DataSetSubGroupCatItem?>, context: Context?, partCategoryInterface: PartCategoryInterface) : RecyclerView.Adapter<ChildViewAdapter.SubGroupViewHolder>() {
        private lateinit var mSubN3GroupArrayList: MutableList<DataSetSubGroupCatItem?>
        private var mContext: Context?
        private lateinit var mView: PartCategoryInterface

        init {
            mSubN3GroupArrayList = subN3GroupArrayList
            mContext = context
            mView = partCategoryInterface
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewAdapter.SubGroupViewHolder {
            val subGroupLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_part_detail, parent, false)
            return SubGroupViewHolder(subGroupLayout)
        }

        override fun onBindViewHolder(holder: ChildViewAdapter.SubGroupViewHolder, position: Int) {
            val productDetails = mSubN3GroupArrayList[position]

            if (productDetails != null) {

                if (!productDetails.ourCategoryName.isNullOrBlank()) {
                    holder.productTitle.text = productDetails.ourCategoryName
                } else {
                    holder.productTitle.text = productDetails.item + " ${productDetails.frontRear
                            ?: ""} ${productDetails.leftRight ?: ""}"
                }



                if (productDetails.images != null && productDetails.images.size != 0)

                    mContext?.loadImageWithName(productDetails.images[0].imageName,
                            holder.productImage, R.drawable.no_image_placeholder,
                            productDetails.images[0].imageUrl
                    )

                holder.productContainer.setOnClickListener {
                    val id = productDetails.id

                    if (id != null)
                        mView.onGroupCategoryClicked(id)
                }
            }
        }

        override fun getItemCount(): Int {
            return mSubN3GroupArrayList.size
        }


        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        class SubGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var productContainer = itemView.findViewById<LinearLayout>(R.id.part_container)
            var productImage = itemView.findViewById<ImageView>(R.id.part_image)
            var productTitle = itemView.findViewById<TextView>(R.id.part_name)
        }
    }

}