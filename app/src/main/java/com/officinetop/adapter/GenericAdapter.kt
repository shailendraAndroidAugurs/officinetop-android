package com.officinetop.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.BR
import com.officinetop.view_models.ListItemViewModel

class GenericAdapter<T : ListItemViewModel>(val context: Context, @LayoutRes val layoutId: Int) :
        RecyclerView.Adapter<GenericAdapter.GenericViewHolder<T>>() {


    private var isLoadingVisible = false
    private val items = mutableListOf<T>()
    private val inflater: LayoutInflater? = null
    private var onListItemViewClickListener: OnListItemViewClickListener? = null

    fun addItems(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }


    fun addItemAfterScroll(items: List<T>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }



    fun getItem(position: Int): T {
        return items.get(position)
    }


    fun setOnListItemViewClickListener(onListItemViewClickListener: OnListItemViewClickListener?) {
        this.onListItemViewClickListener = onListItemViewClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val layoutInflater = inflater ?: LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, layoutId, parent, false)

        return GenericViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        val itemViewModel = items[position]
        itemViewModel.adapterPostion = position

        onListItemViewClickListener?.let {
            itemViewModel.onListItemViewClickListener = it
            holder.bind(itemViewModel, position, items)

        }


    }

    class GenericViewHolder<T : ListItemViewModel>(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(itemViewModel: T, position: Int, items: MutableList<T>) {
            binding.setVariable(BR.listItemViewModel, itemViewModel)
            binding.executePendingBindings()

        }
    }

    interface OnListItemViewClickListener {
        fun onClick(view: View, position: Int)
        fun onItemClick(view: View, position: Int)
    }

}