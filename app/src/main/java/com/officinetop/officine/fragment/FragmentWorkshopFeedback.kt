package com.officinetop.officine.fragment

import android.nfc.tech.MifareUltralight
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
import com.google.gson.GsonBuilder
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getUserId
import com.officinetop.officine.feedback.FeedbackDetailActivity
import com.officinetop.officine.feedback.FeedbackReview
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.DateFormatChangeYearToMonth
import com.officinetop.officine.utils.createImageSliderDialog
import com.officinetop.officine.utils.loadImage
import kotlinx.android.synthetic.main.fragment_feedback_show.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_showfeedback.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.support.v4.intentFor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentWorkshopFeedback : Fragment() ,FeedbackReview{
    private lateinit var rootView: View
    private var WorkshopFeedBackList: ArrayList<Models.HighRatingfeedback> = ArrayList()
    private var isLoading = false
    private val PAGESTART = 0
    private var currentPage = PAGESTART
    private var isLastPage = false
    private lateinit var  adapter :  RecyclerView.Adapter<RecyclerView.ViewHolder>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_feedback_show, container, false)


        if (arguments != null) {
            WorkshopFeedBackList = requireArguments().getSerializable("list") as ArrayList<Models.HighRatingfeedback>
            if (!requireArguments().getBoolean("product")) {
                if (requireArguments().getBoolean("MyReview")) {
                    val WorkshopFeedBackListfilter: ArrayList<Models.HighRatingfeedback> = ArrayList()

                    WorkshopFeedBackListfilter.addAll(WorkshopFeedBackList.filter {
                        Log.d("MyReview", "userid:" + it.users_id)
                        it.users_id == activity?.getUserId()
                    })

                    getHighRatingWorkshopData(WorkshopFeedBackListfilter)


                    Log.d("MyReview", "true_forProduct + list size" + WorkshopFeedBackList.size)
                } else {
                    getHighRatingWorkshopData(WorkshopFeedBackList)
                }



                Log.d("FragmnetFor", "workshop" + "workshop list size" + WorkshopFeedBackList.size)
            }

            Log.d("list", "WorkshopFeedBackListfragment" + WorkshopFeedBackList.size)
            val linearLayoutManager = LinearLayoutManager(activity)
            rootView.rv_product_feedback_recycler_view.layoutManager = linearLayoutManager

            val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                                                           override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount: Int = linearLayoutManager.getChildCount()
                    val totalItemCount: Int = linearLayoutManager.getItemCount()
                    val firstVisibleItemPosition: Int = linearLayoutManager.findFirstVisibleItemPosition()
                    if (!isLoading && !isLastPage) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= MifareUltralight.PAGE_SIZE) {
                            currentPage += 10
                            isLoading =true;
                            highRatingFeedback("2",currentPage)
                            rootView.progress_bar.visibility = View.VISIBLE
                        }
                    }
                }
            }

            rootView.rv_product_feedback_recycler_view.addOnScrollListener(recyclerViewOnScrollListener);

        }
        return rootView
    }

    private fun getHighRatingWorkshopData(WorshopFeedbackList: ArrayList<Models.HighRatingfeedback>) {
        //workshop feedback
        adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_showfeedback, p0, false)

                return object : RecyclerView.ViewHolder(view) {}
            }
            override fun getItemCount(): Int = WorshopFeedbackList.size

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

                if (WorshopFeedbackList[p1].workshop_details != null && !WorshopFeedbackList[p1].workshop_details.company_name.isNullOrBlank()) {
                    p0.itemView.tv_NameofProductorWorkshop.text = WorshopFeedbackList[p1].workshop_details.company_name
                } else {
                    p0.itemView.tv_NameofProductorWorkshop.text = getString(R.string.concat)
                }
                if (!WorshopFeedbackList[p1].profile_image.isNullOrBlank()) {
                    context?.loadImage(WorshopFeedbackList[p1].profile_image, p0.itemView.Iv_UserImage)
                }
                if (!WorshopFeedbackList[p1].rating.isNullOrBlank()) {
                    p0.itemView.ratngbar_ratingFeedback.rating = WorshopFeedbackList[p1].rating.toFloat()
                } else {
                    p0.itemView.ratngbar_ratingFeedback.rating = 0.0f
                }



                if (!WorshopFeedbackList[p1].last_name.isNullOrBlank() && !WorshopFeedbackList[p1].first_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = WorshopFeedbackList[p1].first_name + " " + WorshopFeedbackList[p1].last_name
                } else {
                    p0.itemView.tv_userName.text = getString(R.string.concat)
                }



                if (!WorshopFeedbackList[p1].last_name.isNullOrBlank() && !WorshopFeedbackList[p1].first_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = WorshopFeedbackList[p1].first_name + " " + WorshopFeedbackList[p1].last_name
                } else if (!WorshopFeedbackList[p1].first_name.isNullOrBlank() && WorshopFeedbackList[p1].last_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = WorshopFeedbackList[p1].first_name.toString()
                } else if (WorshopFeedbackList[p1].first_name.isNullOrBlank() && !WorshopFeedbackList[p1].last_name.isNullOrBlank()) {
                    p0.itemView.tv_userName.text = WorshopFeedbackList[p1].last_name.toString()
                } else {
                    p0.itemView.tv_userName.text = getString(R.string.concat)
                }
                p0.itemView.tv_product_type.text = getString(R.string.Workshop)
                if (!WorshopFeedbackList[p1].created_at.isNullOrBlank()) {
                    p0.itemView.tv_date.text = DateFormatChangeYearToMonth(WorshopFeedbackList[p1].created_at.split(" ")[0])
                } else {
                    p0.itemView.tv_date.text = getString(R.string.concat)
                }
                if (!WorshopFeedbackList[p1].comments.isNullOrBlank()) {
                    p0.itemView.tv_userComment.text = WorshopFeedbackList[p1].comments
                } else {
                    p0.itemView.tv_userComment.text = getString(R.string.concat)
                }
                if (WorshopFeedbackList[p1].feedback_image != null && WorshopFeedbackList[p1].feedback_image.size != 0) {
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

                        override fun getItemCount(): Int = WorshopFeedbackList[p1].feedback_image.size

                        override fun onBindViewHolder(view: RecyclerView.ViewHolder, position: Int) {
                            context?.loadImage(WorshopFeedbackList[p1].feedback_image[position].imageUrl, view.itemView.item_image_view)

                            view.itemView.setOnClickListener {
                                context?.createImageSliderDialog(WorshopFeedbackList[p1].feedback_image[position].imageUrl)

                            }
                        }
                    }


                } else {
                    p0.itemView.rv_feedbackImage.adapter = null
                }

                p0.itemView.setOnClickListener {

                    startActivity(intentFor<FeedbackDetailActivity>(
                            Constant.Path.type to "",
                            Constant.Path.model to Gson().toJson(WorshopFeedbackList[p1]),
                            Constant.Path.ProductOrWorkshopName to if (WorshopFeedbackList[p1].workshop_details != null) WorshopFeedbackList[p1].workshop_details.company_name else "",
                            "forHighRating" to "1"
                    ))
                }

            }
        }

        rootView.rv_product_feedback_recycler_view.adapter = adapter

    }

    override fun myReviewshow(MyReview: Boolean) {
        if (MyReview) {

            val WorkshopFeedBackListfilter: ArrayList<Models.HighRatingfeedback> = ArrayList()

            WorkshopFeedBackListfilter.addAll(WorkshopFeedBackList.filter {
                Log.d("MyReview", "userid:" + it.users_id)
                it.users_id == activity?.getUserId()
            })

            getHighRatingWorkshopData(WorkshopFeedBackListfilter)
        } else {
            getHighRatingWorkshopData(WorkshopFeedBackList)
        }
    }

    override fun onResume() {
        super.onResume()
        (parentFragment as FragmentFeedback?)?.setActivityListener(this@FragmentWorkshopFeedback)
    }

    private fun highRatingFeedback(type: String,limit : Int){
        var HighRatingArrayList: ArrayList<Models.HighRatingfeedback> = ArrayList()
        RetrofitClient.client.getHighRatingFeedback(type,limit)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        isLoading = false;
                        if (response.isSuccessful) {
                            try
                            {   val body = JSONObject(response.body()?.string())
                                if (body.has("data_set") && body.get("data_set") != null) {
                                    val jsonarray = body.get("data_set") as JSONArray
                                    val gson = GsonBuilder().create()
                                    val productOrworkshopFeedback = gson.fromJson(jsonarray.toString(), Array<Models.HighRatingfeedback>::class.java).toCollection(java.util.ArrayList<Models.HighRatingfeedback>())
                                    WorkshopFeedBackList.addAll(productOrworkshopFeedback)
                                    if (arguments!!.getBoolean("MyReview")) {
                                        val WorkshopFeedBackList: ArrayList<Models.HighRatingfeedback> = ArrayList()
                                        WorkshopFeedBackList.addAll(WorkshopFeedBackList.filter {
                                            it.users_id == activity?.getUserId()
                                        })
                                    }
                                    if (::adapter.isInitialized){
                                        adapter.notifyDataSetChanged()
                                    }

                                    if (jsonarray.length() == 0) {
                                        isLastPage = true;
                                    }
                                }
                                else{
                                    isLastPage = true;
                                }
                                rootView.progress_bar.visibility = View.GONE

                            } catch (e: Exception) {
                                e.printStackTrace()
                                isLoading = false;
                                isLastPage = true;
                                rootView.progress_bar.visibility = View.GONE
                            }
                        }
                        else{
                            isLastPage = true;
                            rootView.progress_bar.visibility = View.GONE

                        }

                    }
                })
    }

}
