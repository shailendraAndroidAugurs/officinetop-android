package com.officinetop.officine.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var currentPosition : Int? = 0

    protected abstract fun clear()


     open fun onBind(position : Int){
        currentPosition = position
        clear()
    }

     fun getCurrentPosition(): Int? {
        return currentPosition
    }


}