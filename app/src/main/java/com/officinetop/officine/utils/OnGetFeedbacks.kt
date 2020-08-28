package com.officinetop.officine.utils

import com.officinetop.officine.data.Models

interface OnGetFeedbacks {

    fun getFeedbackList(list: MutableList<Models.FeedbacksList>,feedbackwithoutPurchage:String)

}