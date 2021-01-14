package com.officinetop.officine.feedback

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getUserId
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_feedback_list.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_showfeedback.view.*
import org.jetbrains.anko.intentFor

class FeedbackListActivity : BaseActivity(), OnGetFeedbacks {

    private var workshopId: String = ""
    private var productId: String = ""
    private var list: MutableList<Models.FeedbacksList> = ArrayList()
    private var type: String = ""//type for product type, type for tyre is 2, type for spare parts for 1 and type for rim 3
    private var productorWorkshopName: String = ""
    private var sellerId: String = ""
    private var productType: String = ""//type for product type, type for tyre is 2, type for spare parts for 1 and type for rim 3
    private var mainCategoryId: String = ""
    private var serviceID: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.Feedback_List)

        //    AndroidThreeTen.init(this)

        intent.printValues(localClassName)

        if (intent.hasExtra(Constant.Path.workshopId))
            workshopId = intent?.getStringExtra(Constant.Path.workshopId) ?: ""

        if (intent.hasExtra(Constant.Path.productId))
            productId = intent?.getStringExtra(Constant.Path.productId) ?: ""

        if (intent.hasExtra(Constant.Path.type))
            type = intent?.getStringExtra(Constant.Path.type) ?: ""
        if (intent.hasExtra(Constant.Path.sellerId))
            sellerId = intent?.getStringExtra(Constant.Path.sellerId) ?: ""

        if (intent.hasExtra(Constant.Path.ProductOrWorkshopName))
            productorWorkshopName = intent?.getStringExtra(Constant.Path.ProductOrWorkshopName)
                    ?: ""


        if (intent.hasExtra(Constant.Path.productType))
            productType = intent?.getStringExtra(Constant.Path.productType) ?: ""
        if (intent.hasExtra(Constant.Path.mainCategoryId))
            mainCategoryId = intent?.getStringExtra(Constant.Path.mainCategoryId) ?: ""

        if (intent.hasExtra(Constant.Path.serviceID))
            serviceID = intent?.getStringExtra(Constant.Path.serviceID)
                    ?: ""



        btn_addfedback.setOnClickListener {
            startActivityForResult(intentFor<FeedbackAddActivity>(
                    Constant.Path.workshopId to workshopId,
                    Constant.Path.productId to productId,
                    Constant.Path.productType to productType,
                    Constant.Path.sellerId to sellerId,
                    Constant.Path.ProductOrWorkshopName to productorWorkshopName,
                    Constant.Path.mainCategoryId to mainCategoryId,
                    Constant.Path.serviceID to serviceID,
                    Constant.Path.type to type,
                    Constant.Path.withoutPurchase to "1"


            ), 102)
        }

        feed_back_swipe_layout.setOnRefreshListener {
            getFeedbacks(this, workshopId, productId, type, productType)
        }

        try {
            getFeedbacks(this, workshopId, productId, type, productType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun bindFeedbackList() {
        if (fedback_recycler_view != null) {
            overall_rating.rating = list[0].avgRatings.toFloat()
            tv_rating_count.text = list[0].avgRatings.toFloat().toDouble().roundTo2Places().toString() + " " + "out of 5"
            tv_overAll_rating_count.text = list[0].noOfPeople
            fedback_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            //workshop feedback
            fedback_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(R.layout.item_showfeedback, p0, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }

                override fun getItemCount(): Int = list.size
                override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

                    if (!list[p1].ProductOrWorkshopName.isNullOrBlank()) {
                        p0.itemView.tv_NameofProductorWorkshop.text = list[p1].ProductOrWorkshopName
                    } else {
                        p0.itemView.tv_NameofProductorWorkshop.text = getString(R.string.concat)
                    }
                    if (!list[p1].profile_image.isNullOrBlank()) {
                        loadImage(list[p1].profile_image, p0.itemView.Iv_UserImage)
                    }
                    if (!list[p1].rating.isNullOrBlank()) {
                        p0.itemView.ratngbar_ratingFeedback.rating = list[p1].rating.toFloat()
                    } else {
                        p0.itemView.ratngbar_ratingFeedback.rating = 0.0f
                    }



                    if (!list[p1].lName.isNullOrBlank() && !list[p1].fName.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = list[p1].fName + " " + list[p1].lName
                    } else if (!list[p1].fName.isNullOrBlank() && list[p1].lName.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = list[p1].fName.toString()
                    } else if (list[p1].fName.isNullOrBlank() && !list[p1].lName.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = list[p1].lName.toString()
                    } else {
                        p0.itemView.tv_userName.text = getString(R.string.concat)
                    }
                    if (list[p1].workshopId != null) {
                        p0.itemView.tv_product_type.text = getString(R.string.Workshop)
                    } else if (list[p1].product_type == "1") {
                        p0.itemView.tv_product_type.text = getString(R.string.spare_part)
                    } else if (list[p1].product_type == "2") {
                        p0.itemView.tv_product_type.text = getString(R.string.tyres)
                    }

                    if (!list[p1].withoutPurchase.isNullOrBlank() && list[p1].withoutPurchase == "1" && list[p1].usersId == getUserId()) {
                        btn_addfedback.visibility = View.GONE

                    }



                    if (!list[p1].createdAt.isNullOrBlank()) {
                        p0.itemView.tv_date.text = DateFormatChangeYearToMonth(list[p1].createdAt.split(" ")[0])
                    } else {
                        p0.itemView.tv_date.text = getString(R.string.concat)
                    }
                    if (!list[p1].comments.isNullOrBlank()) {
                        p0.itemView.tv_userComment.text = list[p1].comments
                    } else {
                        p0.itemView.tv_userComment.text = getString(R.string.concat)
                    }

                    if (list[p1].images != null && list[p1].images.size != 0) {


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

                            override fun getItemCount(): Int = list[p1].images.size

                            override fun onBindViewHolder(view: RecyclerView.ViewHolder, position: Int) {
                                loadImage(list[p1].images[position].imageUrl, view.itemView.item_image_view)

                                view.itemView.setOnClickListener {
                                    createImageSliderDialog(list[p1].images[position].imageUrl)

                                }
                            }
                        }


                    } else {
                        p0.itemView.rv_feedbackImage.adapter = null
                    }

                    p0.itemView.setOnClickListener {
                        displayDetails(list[p1])

                    }

                }
            }
        }

    }

    private fun displayDetails(feedbacksList: Models.FeedbacksList) {

        startActivity(intentFor<FeedbackDetailActivity>(
                Constant.Path.type to productType,
                Constant.Path.model to Gson().toJson(feedbacksList),
                Constant.Path.ProductOrWorkshopName to feedbacksList.ProductOrWorkshopName
        ))

    }

    override fun getFeedbackList(feedbacklist: MutableList<Models.FeedbacksList>) {
        if (feed_back_swipe_layout != null) feed_back_swipe_layout.isRefreshing = false
        list = feedbacklist


        bindFeedbackList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getFeedbacks(this, workshopId, productId, type, productType)
    }
}





