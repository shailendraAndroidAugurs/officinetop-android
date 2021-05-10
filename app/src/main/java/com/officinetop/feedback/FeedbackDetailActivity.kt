@file:Suppress("Annotator")

package com.officinetop.feedback

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.GridItemDecoration
import com.officinetop.adapter.QuotesGridAdapter
import com.officinetop.data.Models
import com.officinetop.databinding.ActivityFeedbackDetailBinding
import com.officinetop.utils.Constant
import com.officinetop.utils.loadImage
import kotlinx.android.synthetic.main.activity_feedback_detail.*
import kotlinx.android.synthetic.main.include_toolbar.*

class FeedbackDetailActivity : BaseActivity() {

    private var type: String = ""
    private var productorWorkshopName: String = ""
    private var feedbackList: Models.FeedbacksList? = null
    private var highRatingfeedbackList: Models.HighRatingfeedback? = null
    private var binding: ActivityFeedbackDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback_detail)

        binding?.root

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.Feedback_Detail)
        initViews()

    }

    private fun initViews() {

        if (intent.hasExtra(Constant.Path.type))
            type = intent?.getStringExtra(Constant.Path.type) ?: ""
        Log.d("feedbackProducttype", type)
        if (intent.hasExtra(Constant.Path.ProductOrWorkshopName))
            productorWorkshopName = intent?.getStringExtra(Constant.Path.ProductOrWorkshopName)
                    ?: ""

        if (intent.hasExtra(Constant.Path.model)) {
            if (intent.hasExtra("forHighRating")) {
                highRatingfeedbackList = Gson().fromJson<Models.HighRatingfeedback>(intent?.getStringExtra(Constant.Path.model)
                        ?: "", Models.HighRatingfeedback::class.java)

            }
            feedbackList = Gson().fromJson<Models.FeedbacksList>(intent?.getStringExtra(Constant.Path.model)
                    ?: "", Models.FeedbacksList::class.java)


        }


        if (intent.hasExtra("forHighRating")) {

            if (!highRatingfeedbackList?.last_name.isNullOrBlank() && !highRatingfeedbackList?.first_name.isNullOrBlank()) {
                feedbackList?.fName = highRatingfeedbackList?.first_name + " " + highRatingfeedbackList?.last_name
            } else if (!highRatingfeedbackList?.first_name.isNullOrBlank() && highRatingfeedbackList?.last_name.isNullOrBlank()) {
                feedbackList?.fName = highRatingfeedbackList?.first_name.toString()
            } else if (highRatingfeedbackList?.first_name.isNullOrBlank() && !highRatingfeedbackList?.last_name.isNullOrBlank()) {
                feedbackList?.fName = highRatingfeedbackList?.last_name.toString()
            } else {
                feedbackList?.fName = ""
            }
            if (!highRatingfeedbackList?.rating.isNullOrBlank()) {
                feedbackList?.avgRatings = highRatingfeedbackList?.rating!!
            } else {
                feedbackList?.avgRatings = "0.0"
            }

            if (!highRatingfeedbackList?.feedback_image.isNullOrEmpty()) {
                feedbackList?.images = highRatingfeedbackList?.feedback_image!!
            }
            if (!highRatingfeedbackList?.profile_image.isNullOrBlank()) {
                loadImage(highRatingfeedbackList?.profile_image, profile_pic)

            }


        }


        when (type) {
            "1" -> feedbackList?.feedbackType = getString(R.string.spare_part)
            "2" -> feedbackList?.feedbackType = getString(R.string.tyres)
            "3" -> feedbackList?.feedbackType = getString(R.string.Rim)
            else -> feedbackList?.feedbackType = getString(R.string.Workshop)
        }
        if (type == "") {
            nome_prodoto.text = getString(R.string.WorkshopName)
        }
        feedbackList?.ProductOrWorkshopName = productorWorkshopName

        binding?.listItemViewModel = feedbackList



        if (feedbackList?.images != null) {

            val mLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
            if (feedbackList?.images!!.size > 2) {
                fedback_images_recycler_view.addItemDecoration(GridItemDecoration(10, 2))

            } else {
                //fedback_images_recycler_view.addItemDecoration(GridItemDecoration(10, 2))

            }

            fedback_images_recycler_view.layoutManager = mLayoutManager

            val imagesArray: MutableList<String> = ArrayList()
            for (i in 0 until feedbackList?.images!!.size) {
                imagesArray.add(feedbackList?.images!!.get(i).imageUrl)
            }
            val imagesAdapter = QuotesGridAdapter(true, this, ArrayList(), object : QuotesGridAdapter.OnRecyclerItemClickListener {

                override fun onItemAdd(item: MutableList<String>) {

                }

                override fun onItemRemove(path: String) {

                }
            })
            fedback_images_recycler_view.adapter = imagesAdapter
            imagesAdapter.addItem(imagesArray)
        }

    }
}
