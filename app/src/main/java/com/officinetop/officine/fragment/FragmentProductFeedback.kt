package com.officinetop.officine.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getUserId
import com.officinetop.officine.feedback.FeedbackDetailActivity
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.DateFormatChangeYearToMonth
import com.officinetop.officine.utils.createImageSliderDialog
import com.officinetop.officine.utils.loadImage
import kotlinx.android.synthetic.main.fragment_feedback_show.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_showfeedback.view.*
import org.jetbrains.anko.support.v4.intentFor


class FragmentProductFeedback : Fragment()/*, FragmentFeedback.OnAboutDataReceivedListener*/ {
    private lateinit var rootView: View
    private var WorkshopFeedBackList: ArrayList<Models.HighRatingfeedback> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_feedback_show, container, false)
        if (arguments != null) {
            WorkshopFeedBackList = arguments!!.getSerializable("list") as ArrayList<Models.HighRatingfeedback>
            if (arguments!!.getBoolean("product")) {
                if (arguments!!.getBoolean("MyReview")) {

                     val WorkshopFeedBackListfilter: ArrayList<Models.HighRatingfeedback> = ArrayList()

                    WorkshopFeedBackListfilter.addAll( WorkshopFeedBackList.filter {
                        Log.d("MyReview", "userid:" + it.users_id)
                        it.users_id == activity?.getUserId()
                    })

                    getHighRatingProductData(WorkshopFeedBackListfilter)

                    Log.d("MyReview","true_forProduct + list size"+ WorkshopFeedBackList.size)
                }else{
                    getHighRatingProductData(WorkshopFeedBackList)
                }


                Log.d("FragmnetFor", "Product"+ "product list size"+WorkshopFeedBackList.size)
            }

            Log.d("list", "WorkshopFeedBackListfragment" + WorkshopFeedBackList.size)

        }
        return rootView
    }

    private fun getHighRatingProductData(productFeedbackList: ArrayList<Models.HighRatingfeedback>) {


        rootView.rv_product_feedback_recycler_view.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)


        rootView.rv_product_feedback_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_showfeedback, p0, false)

                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun getItemCount(): Int = productFeedbackList.size

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                if (productFeedbackList[p1].ProductFeedbackDetail!=null && !productFeedbackList[p1].ProductFeedbackDetail.product_name.isNullOrBlank()) {
                    p0.itemView.tv_NameofProductorWorkshop.text = productFeedbackList[p1].ProductFeedbackDetail.product_name
                    Log.d("productname",productFeedbackList[p1].ProductFeedbackDetail.product_name)
                } else {
                    p0.itemView.tv_NameofProductorWorkshop.text = getString(R.string.Concat)
                }
                if (!productFeedbackList[p1].profile_image.isNullOrBlank()) {
                    context?.loadImage(productFeedbackList[p1].profile_image, p0.itemView.Iv_UserImage)
                }
                if (!productFeedbackList[p1].rating.isNullOrBlank()) {
                    p0.itemView.ratngbar_ratingFeedback.rating = productFeedbackList[p1].rating.toFloat()
                } else {
                    p0.itemView.ratngbar_ratingFeedback.rating = 0.0f
                }



                if (!productFeedbackList[p1].last_name.isNullOrBlank() && !productFeedbackList[p1].first_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = productFeedbackList[p1].first_name + " " + productFeedbackList[p1].last_name
                } else if (!productFeedbackList[p1].first_name.isNullOrBlank() && productFeedbackList[p1].last_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = productFeedbackList[p1].first_name.toString()
                } else if (productFeedbackList[p1].first_name.isNullOrBlank() && !productFeedbackList[p1].last_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = productFeedbackList[p1].last_name.toString()
                } else {
                    p0.itemView.tv_userName.text = getString(R.string.Concat)
                }


                if (!productFeedbackList[p1].product_type.isNullOrBlank()) {
                    when (productFeedbackList[p1].product_type) {
                        "1" -> p0.itemView.tv_product_type.text = getString(R.string.spare_part)
                        "2" -> p0.itemView.tv_product_type.text = getString(R.string.tyres)
                        "3" -> p0.itemView.tv_product_type.text = getString(R.string.Rim)
                        else -> p0.itemView.tv_product_type.text = getString(R.string.Workshop)
                    }

                } else {
                    p0.itemView.tv_product_type.text = getString(R.string.Concat)
                }
                if (!productFeedbackList[p1].created_at.isBlank()) {
                    p0.itemView.tv_date.text = DateFormatChangeYearToMonth(productFeedbackList[p1].created_at.split(" ")[0])
                } else {
                    p0.itemView.tv_date.text = getString(R.string.Concat)
                }
                if (!productFeedbackList[p1].comments.isBlank()) {
                    p0.itemView.tv_userComment.text = productFeedbackList[p1].comments
                } else {
                    p0.itemView.tv_userComment.text = getString(R.string.Concat)
                }
                if (productFeedbackList[p1].feedback_image != null && productFeedbackList[p1].feedback_image.size != 0) {
                    p0.itemView.rv_feedbackImage.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
                    // p0.itemView.rv_feedbackImage.setHasFixedSize(true)

                    p0.itemView.rv_feedbackImage.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                        }

                        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                            val action: Int = e.action
                            when (action) {
                                MotionEvent.ACTION_MOVE ->
                                    rv.parent.requestDisallowInterceptTouchEvent(true)

                            }
                            return false
                        }

                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                        }
                    }

                    )




                    p0.itemView.rv_feedbackImage.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                            val view = layoutInflater.inflate(R.layout.item_image, p0, false)
                            return object : RecyclerView.ViewHolder(view) {}
                        }

                        override fun getItemCount(): Int = productFeedbackList[p1].feedback_image.size

                        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, postion: Int) {

                            if (productFeedbackList[p1] != null && productFeedbackList[p1].feedback_image != null && productFeedbackList[p1].feedback_image.size != 0 && productFeedbackList[p1].feedback_image[postion].imageUrl != null) {
                                context?.loadImage(productFeedbackList[p1].feedback_image[postion].imageUrl, viewHolder.itemView.item_image_view)

                                viewHolder.itemView.setOnClickListener {
                                    context?.createImageSliderDialog(productFeedbackList[p1].feedback_image[postion].imageUrl)

                                }
                            }


                        }
                    }


                }else
                {
                    p0.itemView.rv_feedbackImage.adapter= null
                }





                p0.itemView.setOnClickListener {
                    Log.d("productType", productFeedbackList[p1].product_type.toString())
                    startActivity(intentFor<FeedbackDetailActivity>(
                            Constant.Path.type to productFeedbackList[p1].product_type,
                            Constant.Path.model to Gson().toJson(productFeedbackList[p1]),
                            Constant.Path.ProductOrWorkshopName to if(productFeedbackList[p1].ProductFeedbackDetail!=null)productFeedbackList[p1].ProductFeedbackDetail.product_name else "",
                            "forHighRating" to "1"
                    ))
                }
            }
        }
    }

}
