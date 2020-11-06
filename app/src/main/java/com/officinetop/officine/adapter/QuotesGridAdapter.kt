package com.officinetop.officine.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.utils.createImageSliderDialog
import com.officinetop.officine.utils.loadImage
import kotlinx.android.synthetic.main.quotes_images_item_view.view.*

class QuotesGridAdapter(val isFeedback: Boolean, val context: Context, private val imagesList: MutableList<String>, private var listenerRecycler: OnRecyclerItemClickListener)
    : RecyclerView.Adapter<QuotesGridAdapter.QuotesImagesViewHolder>() {


    override fun getItemCount(): Int = imagesList.size

    override fun onBindViewHolder(holder: QuotesImagesViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(imagesList.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotesImagesViewHolder {
        return QuotesImagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.quotes_images_item_view, parent, false))
    }

    fun addItem(item: MutableList<String>) {
        imagesList.addAll(item)
        notifyDataSetChanged()

        listenerRecycler.onItemAdd(item)
    }

    fun getItem(position: Int): String {
        return imagesList.get(position)
    }

    fun removeItem(position: Int) {
        imagesList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, imagesList.size)
    }

    fun removeAll() {
        try {
            imagesList.clear()
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    inner class QuotesImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image_view = itemView.quotation_images
        private val delete_image = itemView.delete_image

        fun bindView(item: String) {
            Log.e("imagesPath==", item)
            context.loadImage(item, image_view)
            if (isFeedback) {
                delete_image.visibility = View.GONE
            }

            delete_image.setOnClickListener {
                removeItem(adapterPosition)
                listenerRecycler.onItemRemove(item)
            }

            itemView.setOnClickListener {
                context.createImageSliderDialog(item)

            }

        }
    }

    interface OnRecyclerItemClickListener {
        fun onItemAdd(item: MutableList<String>)
        fun onItemRemove(path: String)
    }
}