package com.officinetop.utils

import com.officinetop.data.Models

interface OnGetFeedbacks {

    fun getFeedbackList(list: MutableList<Models.FeedbacksList>)

}