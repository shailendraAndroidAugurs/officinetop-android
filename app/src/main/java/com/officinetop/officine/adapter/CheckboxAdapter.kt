package com.officinetop.officine.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import kotlinx.android.synthetic.main.item_checkbox.view.*

class CheckboxAdapter(private val titles: List<String>) : RecyclerView.Adapter<CheckboxAdapter.ViewHolder>() {

    private var onCheckedListener: OnCheckedListener? = null
        set(listener) {
            field = listener
        }


    private val selectedList: MutableList<Models.CheckedItems> = ArrayList<Models.CheckedItems>()


    val selectedItems
        get() = selectedList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_checkbox, parent, false))
    }

    override fun getItemCount(): Int = titles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            title.text = titles[position]

            val onCheckedChange = {
                val isChecked = checkBox.isChecked
                val model = Models.CheckedItems(title = titles[position], position = position, isChecked = isChecked)

                if (isChecked)
                    selectedList.add(model)
                else
                    selectedList.removeAll { it.position == position }

                onCheckedListener?.onCheckedChanged(position, titles[position], isChecked)
            }


            checkBox.setOnCheckedChangeListener { _, _ ->
                onCheckedChange.invoke()
            }


            layout.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }

    interface OnCheckedListener {
        fun onCheckedChanged(position: Int, title: String, isChecked: Boolean)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.item_checkbox_text
        val checkBox: CheckBox = view.item_checkbox
        val layout: View = view.item_checkbox_container
    }

}