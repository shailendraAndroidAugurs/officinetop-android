package com.officinetop.officine.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getUserId
import com.officinetop.officine.Support.Support_Activity
import com.officinetop.officine.utils.parseServerDateTime

class RecyclerViewAdapterChating(val list: MutableList<Models.Messages>, private val supportActivity: Support_Activity) : RecyclerView.Adapter<RecyclerViewAdapterChating.ChatViewHolder>() {

    private var VIEW_TYPE_SEND: Int = 0


    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        if (viewType == 0) {
            return ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.send_message, parent, false))
        } else {
            return ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recieve_message, parent, false))
        }

    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {

        val itemlist = list[position]
        if (itemlist.sender_id == supportActivity.getUserId()) {
            VIEW_TYPE_SEND = 0
        } else {
            VIEW_TYPE_SEND = 1
        }
        // if(Models.ChatingList   )
        return VIEW_TYPE_SEND
    }

    fun changeposition(): Int {

        notifyDataSetChanged()

        Log.e("SEND", (list.size - 1).toString())
        return list.size - 1
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(chat: Models.Messages) {
            val message_body = itemView.findViewById(R.id.message_body) as TextView
            Log.d("sendString support", chat.messages)
            val message_time = itemView.findViewById(R.id.message_time) as TextView
            val special = "'"
            if (chat.messages.contains(special)){
                message_body.text= chat.messages.substring(1, chat.messages.length-1);

            }else{
                message_body.text = chat.messages
            }

            message_time.text = parseServerDateTime(chat.created_at)

        }


    }
}

