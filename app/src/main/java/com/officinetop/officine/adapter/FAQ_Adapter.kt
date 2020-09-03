package com.officinetop.officine.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import kotlinx.android.synthetic.main.item_checkbox.view.*
import kotlinx.android.synthetic.main.item_faq.view.*

class FAQ_Adapter(private val context :Context,private val faqList: List<Models.FAQ_Question_Answer>) : RecyclerView.Adapter<FAQ_Adapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false))
    }

    override fun getItemCount(): Int = faqList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
           if(!faqList[position].title.isNullOrBlank() || faqList[position].title != "") {
               holder.question.text=faqList[position].title

               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                   holder.question.text= Html.fromHtml(faqList[position].title,Html.FROM_HTML_MODE_LEGACY)
               }else{
                   holder.question.text= Html.fromHtml(faqList[position].title)
               }
           }else
           {
               holder.question.text=context.getString(R.string.Concat)
           }


            if(!faqList[position].terms_conditions_detail.isNullOrBlank() || faqList[position].terms_conditions_detail != "") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.answers.text= Html.fromHtml(faqList[position].terms_conditions_detail,Html.FROM_HTML_MODE_LEGACY)
                }else{
                    holder.answers.text= Html.fromHtml(faqList[position].terms_conditions_detail)
                }
            }else
            {
                holder.answers.text=context.getString(R.string.Concat)
            }

        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val question: TextView = view.tv_faq_question
        val answers: TextView = view.tv_faq_answer

    }


}