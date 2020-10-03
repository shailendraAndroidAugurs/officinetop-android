package com.officinetop.officine.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.daimajia.slider.library.Tricks.FixedSpeedScroller
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.officinetop.officine.HomeActivity
import com.officinetop.officine.MOT.MotListActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.ViewPagerAdapter
import com.officinetop.officine.car_parts.PartCategories
import com.officinetop.officine.car_parts.ProductDetailActivity
import com.officinetop.officine.car_parts.ProductListActivity
import com.officinetop.officine.car_parts.TyreDetailActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.feedback.FeedbackDetailActivity
import com.officinetop.officine.misc_activities.MaintenanceActivity
import com.officinetop.officine.misc_activities.RevisionActivity
import com.officinetop.officine.quotes.QuotesActivity
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.sos.SOSActivity
import com.officinetop.officine.tyre.TyreDiameterActivity
import com.officinetop.officine.tyre.TyreListActivity
import com.officinetop.officine.utils.*
import com.officinetop.officine.Washing.CarServiceListActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.item_title
import kotlinx.android.synthetic.main.item_grid_home_btn.view.item_icon
import kotlinx.android.synthetic.main.item_grid_home_square.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_showfeedback.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.support.v4.intentFor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentHome : Fragment() {

    private var AdvertisementImagearray: ArrayList<Models.AdvertisingImages> = ArrayList<Models.AdvertisingImages>()

    private lateinit var rootView: View

    var bestSellingProductList: ArrayList<Models.BestSellingProduct_home> = ArrayList()
    val buttonIcons = arrayListOf<Int>(R.drawable.tire, R.drawable.ic_maintenance,

            R.drawable.ic_revisione, R.drawable.ic_washing, R.drawable.settings,
            R.drawable.ic_scheduled_car_inspection, R.drawable.ic_sos, R.drawable.ic_estimate)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)

        bindViews()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicks()
        if (context?.getLangLocale() != null && !context?.getLangLocale().equals("")) {
            context?.setAppLanguage()
        } else {
            context?.storeLangLocale("it")
            context?.setAppLanguage()
        }
    }

    private fun clicks() {
        item_more_product.setOnClickListener {
            if (bestSellingProductList != null && bestSellingProductList.size != 0) {
                startActivity(intentFor<ProductListActivity>(Constant.Key.is_best_selling to true))
            }

        }
    }

    private fun bindViews() {
        highRatingFeedback("1")
        if (context?.getSelectedCar() != null && context?.getSelectedCar()?.carVersionModel != null && context?.getSelectedCar()?.carVersionModel?.idVehicle != null) {
            bestSellingApi()
        }

        allAdvertisementApi()
        val buttonTitles = rootView.resources.getStringArray(R.array.home_icon_titles)


        rootView.home_grid_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_grid_home_btn, p0, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun getItemCount(): Int = buttonTitles.size

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                p0.itemView.item_title.text = buttonTitles[p1].toUpperCase()
                p0.itemView.item_icon.setImageResource(buttonIcons[p1])
                p0.itemView.setOnClickListener {

                    if (!(context as HomeActivity).checkForSelectedCar()) {
                        return@setOnClickListener
                    }

                    context?.let {
                        when (p1) {
                            0 -> {
                                if (!TextUtils.isEmpty(it.getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).getString("tyre_detail", ""))) {
                                    startActivityForResult(it.intentFor<TyreListActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)
                                } else {
                                    startActivityForResult(it.intentFor<TyreDiameterActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)
                                }
                            }
                            1 -> startActivityForResult(it.intentFor<MaintenanceActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)
                            4 -> startActivityForResult(it.intentFor<PartCategories>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)//PartsCategoryActivity>())
                            5 -> startActivityForResult(it.intentFor<MotListActivity>(), 108)
                            2 -> startActivityForResult(it.intentFor<RevisionActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)
                            3 -> startActivityForResult(it.intentFor<CarServiceListActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)
                            6 -> startActivityForResult(it.intentFor<SOSActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)
                            7 -> startActivityForResult(it.intentFor<QuotesActivity>().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 500)

                            else -> Log.d("no item", "no item")
                        }
                    }
                }
            }
        }



        rootView.home_More_feedback.setOnClickListener {
            val fragmentChangeListener: FragmentChangeListener = (activity) as FragmentChangeListener
            fragmentChangeListener?.replaceFragment(FragmentFeedback())

        }
    }

    private fun allAdvertisementApi() {
        AdvertisementImagearray.clear()
        RetrofitClient.client.getAllAdvertising(context!!.getBearerToken() ?: "")
                .onCall { networkException, response ->

                    response?.let {
                        if (response.isSuccessful) {
                            try {
                                val body = JSONObject(response.body()?.string())
                                if (body.has("data_set") && body.get("data_set") != null) {
                                    val jsonarray = body.get("data_set") as JSONArray
                                    val gson = GsonBuilder().create()
                                    val allAdvertismentList = gson.fromJson(jsonarray.toString(), Array<Models.AllAdvertisment>::class.java).toCollection(java.util.ArrayList<Models.AllAdvertisment>())
                                    for (n in 0 until allAdvertismentList.size) {
                                        if (!allAdvertismentList[n].addLocation.isNullOrBlank() && allAdvertismentList[n].addLocation == "HOME" && allAdvertismentList[n].advertisingImages.size != 0) {
                                            AdvertisementImagearray.addAll(allAdvertismentList[n].advertisingImages)

                                        }
                                    }
                                    setSlider()
                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
    }

    /*best selling api call.*/
    private fun bestSellingApi() {

        //best selling products
        RetrofitClient.client.bestSellingProductHome("", 0, brands = "", version_id = context?.getSelectedCar()?.carVersionModel?.idVehicle!!)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val body = response.body()?.string()
                        Log.d("FragmentHome", "onResponse Best Selling Products: \n " + response.isSuccessful + "\n " + isStatusCodeValid(body))
                        if (response.isSuccessful) {
                            if (isStatusCodeValid(body)) {
                                val jsonData: JSONObject = JSONObject(body)
                                if (jsonData.has("data_set") && !jsonData.getString("data_set").isNullOrBlank()) {
                                    val jsonArrayData = JSONArray(jsonData.getString("data_set"))
                                    val gson = GsonBuilder().create()
                                    bestSellingProductList = gson.fromJson(jsonArrayData.toString(), Array<Models.BestSellingProduct_home>::class.java).toCollection(java.util.ArrayList<Models.BestSellingProduct_home>())
                                    if (bestSellingProductList.size != 0 && isAdded) {
                                        setBestSellingProduct(bestSellingProductList)

                                    }

                                    Log.d("bestSellingProduct size", bestSellingProductList.size.toString())
                                }


                            }
                        }
                    }
                })
    }


    private fun setSlider() {
        try {
            val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(FragmentHomeSlider())

            if (AdvertisementImagearray.size != 0) {
                for (i in 0 until AdvertisementImagearray.size) {
                    rootView.image_slider.addSlider(TextSliderView(context).image(AdvertisementImagearray[i].imageUrl).setScaleType(BaseSliderView.ScaleType.CenterInside)/*.description(AdvertisementImagearray[i].image)*/.setOnSliderClickListener(BaseSliderView.OnSliderClickListener {
                        if (!AdvertisementImagearray[i].MainCatId.isNullOrBlank())
                            MoveSliderToWorkshop(AdvertisementImagearray[i].MainCatId?.toInt(), context)
                    }))
                    rootView.image_slider.setOnClickListener {

                    }
                }
            } else {
                for (i in 0..2) {
                    rootView.image_slider.addSlider(TextSliderView(context).image(R.drawable.banner))
                }
            }


            val scroller = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(rootView.slider_viewpager, FixedSpeedScroller(context!!, AccelerateInterpolator()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun MoveSliderToWorkshop(n: Int?, context: Context?) {
        when (n) {
            23 -> {
                if (!TextUtils.isEmpty(context?.getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)?.getString("tyre_detail", ""))) {
                    startActivity(intentFor<TyreListActivity>())
                } else {
                    startActivity(intentFor<TyreDiameterActivity>())
                }
            }


            12 -> startActivity(intentFor<MaintenanceActivity>())
            4 -> startActivity(intentFor<PartCategories>())//PartsCategoryActivity>())
            3 -> startActivityForResult(intentFor<MotListActivity>(), 100)
            2 -> startActivity(intentFor<RevisionActivity>())
            1 -> startActivity(intentFor<CarServiceListActivity>())
            13 -> startActivity(intentFor<SOSActivity>())
            25 -> startActivity(intentFor<QuotesActivity>())
        }

    }


    private fun highRatingFeedback(type: String) {
        RetrofitClient.client.getHighRatingFeedback(type)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                        if (response.isSuccessful) {
                            try {
                                val body = JSONObject(response.body()?.string())
                                Log.d("highRatingApicall", "yes")
                                if (body.has("data_set") && body.get("data_set") != null && !body.get("data_set").equals("[]")) {
                                    val jsonarray = body.get("data_set") as JSONArray
                                    val gson = GsonBuilder().create()
                                    val productOrworkshopFeedback = gson.fromJson(jsonarray.toString(), Array<Models.HighRatingfeedback>::class.java).toCollection(java.util.ArrayList<Models.HighRatingfeedback>())

                                    if (type == "1") {
                                        getHighRatingProductData(productOrworkshopFeedback)
                                        highRatingFeedback("2")
                                    } else if (type == "2") {
                                        getHighRatingWorkshopData(productOrworkshopFeedback)
                                    }


                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }


                    }
                })
    }

    private fun getHighRatingProductData(productFeedbackList: ArrayList<Models.HighRatingfeedback>) {

        if (rootView != null && rootView.home_grid_product_feedback_recycler_view != null) {
            rootView.home_grid_product_feedback_recycler_view.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)


            rootView.home_grid_product_feedback_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(R.layout.item_showfeedback, p0, false)

                    return object : RecyclerView.ViewHolder(view) {}
                }

                override fun getItemCount(): Int = productFeedbackList.size

                override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                    if (productFeedbackList[p1].ProductFeedbackDetail != null && !productFeedbackList[p1].ProductFeedbackDetail.product_name.isNullOrBlank()) {
                        p0.itemView.tv_NameofProductorWorkshop.text = productFeedbackList[p1].ProductFeedbackDetail.product_name
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



                    if (!productFeedbackList[p1]?.last_name.isNullOrBlank() && !productFeedbackList[p1]?.first_name.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = productFeedbackList[p1]?.first_name + " " + productFeedbackList[p1]?.last_name
                    } else if (!productFeedbackList[p1]?.first_name.isNullOrBlank() && productFeedbackList[p1]?.last_name.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = productFeedbackList[p1]?.first_name.toString()
                    } else if (productFeedbackList[p1]?.first_name.isNullOrBlank() && !productFeedbackList[p1]?.last_name.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = productFeedbackList[p1]?.last_name.toString()
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
                        p0.itemView.rv_feedbackImage.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

                            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                            }

                            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                                val action: Int = e.action;
                                when (action) {
                                    MotionEvent.ACTION_MOVE ->
                                        rv.parent.requestDisallowInterceptTouchEvent(true);

                                }
                                return false;
                            }

                            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                            }
                        }

                        );




                        p0.itemView.rv_feedbackImage.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                            override fun onCreateViewHolder(p0: ViewGroup, p2: Int): RecyclerView.ViewHolder {
                                val view = layoutInflater.inflate(R.layout.item_image, p0, false)
                                return object : RecyclerView.ViewHolder(view) {}
                            }

                            override fun getItemCount(): Int = productFeedbackList[p1].feedback_image.size

                            override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, postion: Int) {

                                if (productFeedbackList[p1] != null && productFeedbackList[p1].feedback_image != null && productFeedbackList[p1].feedback_image.size != 0 && productFeedbackList[p1].feedback_image[postion].imageUrl != null) {
                                    context?.loadImage(productFeedbackList[p1].feedback_image[postion].imageUrl, viewHolder.itemView.item_image_view)

                                }
                                viewHolder.itemView.setOnClickListener {
                                    context?.createImageSliderDialog(productFeedbackList[p1].feedback_image[postion].imageUrl)

                                }


                            }
                        }


                    } else {
                        p0.itemView.rv_feedbackImage.adapter = null

                    }





                    p0.itemView.setOnClickListener {
                        Log.d("productType", productFeedbackList[p1].product_type.toString())
                        startActivity(intentFor<FeedbackDetailActivity>(
                                Constant.Path.type to productFeedbackList[p1].product_type,
                                Constant.Path.model to Gson().toJson(productFeedbackList[p1]),
                                Constant.Path.ProductOrWorkshopName to if (productFeedbackList[p1].ProductFeedbackDetail != null) productFeedbackList[p1].ProductFeedbackDetail.product_name else "-",
                                "forHighRating" to "1"
                        ))
                    }
                }
            }
        }


    }


    private fun getHighRatingWorkshopData(WorshopFeedbackList: ArrayList<Models.HighRatingfeedback>) {


        if (rootView != null && rootView.home_grid_workshop_feedback_recycler_view != null) {
            rootView.home_grid_workshop_feedback_recycler_view.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)


            //workshop feedback
            rootView.home_grid_workshop_feedback_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(R.layout.item_showfeedback, p0, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }

                override fun getItemCount(): Int = WorshopFeedbackList.size

                override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

                    if (WorshopFeedbackList[p1].workshop_details != null && !WorshopFeedbackList[p1].workshop_details.company_name.isNullOrBlank()) {
                        p0.itemView.tv_NameofProductorWorkshop.text = WorshopFeedbackList[p1].workshop_details.company_name
                    } else {
                        p0.itemView.tv_NameofProductorWorkshop.text = getString(R.string.Concat)
                    }
                    if (!WorshopFeedbackList[p1].profile_image.isNullOrBlank()) {
                        context?.loadImage(WorshopFeedbackList[p1].profile_image, p0.itemView.Iv_UserImage)
                    }
                    if (!WorshopFeedbackList[p1].rating.isNullOrBlank()) {
                        p0.itemView.ratngbar_ratingFeedback.rating = WorshopFeedbackList[p1].rating.toFloat()
                    } else {
                        p0.itemView.ratngbar_ratingFeedback.rating = 0.0f
                    }



                    if (!WorshopFeedbackList[p1]?.last_name.isNullOrBlank() && !WorshopFeedbackList[p1]?.first_name.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = WorshopFeedbackList[p1]?.first_name + " " + WorshopFeedbackList[p1]?.last_name
                    } else if (!WorshopFeedbackList[p1]?.first_name.isNullOrBlank() && WorshopFeedbackList[p1]?.last_name.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = WorshopFeedbackList[p1]?.first_name.toString()
                    } else if (WorshopFeedbackList[p1]?.first_name.isNullOrBlank() && !WorshopFeedbackList[p1]?.last_name.isNullOrBlank()) {
                        p0.itemView.tv_userName.text = WorshopFeedbackList[p1]?.last_name.toString()
                    } else {
                        p0.itemView.tv_userName.text = getString(R.string.Concat)
                    }



                    p0.itemView.tv_product_type.text = getString(R.string.Workshop)

                    if (!WorshopFeedbackList[p1].created_at.isNullOrBlank()) {
                        p0.itemView.tv_date.text = DateFormatChangeYearToMonth(WorshopFeedbackList[p1].created_at.split(" ")[0])
                    } else {
                        p0.itemView.tv_date.text = getString(R.string.Concat)
                    }
                    if (!WorshopFeedbackList[p1].comments.isNullOrBlank()) {
                        p0.itemView.tv_userComment.text = WorshopFeedbackList[p1].comments
                    } else {
                        p0.itemView.tv_userComment.text = getString(R.string.Concat)
                    }

                    if (WorshopFeedbackList[p1].feedback_image != null && WorshopFeedbackList[p1].feedback_image.size != 0) {


                        p0.itemView.rv_feedbackImage.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

                            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                            }

                            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                                val action: Int = e.action;
                                when (action) {
                                    MotionEvent.ACTION_MOVE ->
                                        rv.parent.requestDisallowInterceptTouchEvent(true);

                                }
                                return false;
                            }

                            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                            }
                        }

                        );
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
                                Constant.Path.ProductOrWorkshopName to if (WorshopFeedbackList[p1].workshop_details != null) WorshopFeedbackList[p1].workshop_details.company_name else "-",
                                "forHighRating" to "1"
                        ))
                    }

                }
            }
        }

    }


    private fun setBestSellingProduct(bestSellingProductList: ArrayList<Models.BestSellingProduct_home>) {


        if (rootView != null && rootView.home_grid_product_recycler_view != null) {
            rootView.home_grid_product_recycler_view.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)


            //best selling Product
            rootView.home_grid_product_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(R.layout.item_grid_home_square, p0, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }

                override fun getItemCount(): Int = bestSellingProductList.size

                override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                    val item = bestSellingProductList[p1]

                    if (item != null) {
                        Log.d("bestSellingProduct item", item.toString())
                        if (item.tyreProductDetail != null) {
                            val tyredetail = item.tyreProductDetail
                            p0.itemView.setOnClickListener {

                                startActivity(intentFor<TyreDetailActivity>(
                                        Constant.Path.productDetails to tyredetail,
                                        Constant.Path.productType to "Tyre"))


                            }

                            if (tyredetail.imageUrl != null)
                                activity?.loadImage(tyredetail.imageUrl, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                            else
                                activity?.loadImage("", p0.itemView.item_icon, R.drawable.no_image_placeholder)
                            var tyreType = ""
                            when (tyredetail.type) {
                                "s" -> {

                                    tyreType = getString(R.string.Car)
                                }
                                "w" -> {

                                    tyreType = getString(R.string.Car)
                                }
                                "g" -> {

                                    tyreType = getString(R.string.Car)
                                }
                                "m" -> {

                                    tyreType = getString(R.string.Wheel_Quad_tyre)
                                }
                                "o" -> {

                                    tyreType = getString(R.string.Off_road_tyre)
                                }
                                "i" -> {

                                    tyreType = getString(R.string.Truck)
                                }
                                "a" -> {

                                    tyreType = getString(R.string.All)
                                }


                            }
                            try {

                                p0.itemView.item_name.text = "${tyredetail.manufacturer_description} ${tyreType} ${tyredetail.pr_description}\n${tyredetail.max_width}/${tyredetail.max_aspect_ratio} R${tyredetail.max_diameter}   ${if (tyredetail.load_speed_index != null) tyredetail.load_speed_index else ""} ${if (tyredetail.speed_index != null) tyredetail.speed_index else ""}"//

                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }




                            p0.itemView.item_sub_titleGrid.text = if (tyredetail.ean_number != null) tyredetail.ean_number else ""

                        } else if (item.spareProductDetail != null) {
                            val sparePart = item.spareProductDetail


                            p0.itemView.setOnClickListener {

                                startActivity(intentFor<ProductDetailActivity>(
                                        Constant.Path.productDetails to Gson().toJson(sparePart), Constant.Key.wishList to sparePart.wish_list).forwardResults())

                            }


                            if (!sparePart.product_image_url.isNullOrBlank()) {
                                activity?.loadImage(sparePart.product_image_url, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                            } else if (sparePart.images != null && sparePart.images?.size != 0)
                                activity?.loadImage(sparePart.images?.get(0)?.imageUrl, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                            else
                                activity?.loadImageWithName("", p0.itemView.item_icon, R.drawable.no_image_placeholder)
                            p0.itemView.item_name.text = if (sparePart.productName != null) sparePart.productName else ""
                            p0.itemView.item_sub_titleGrid.text = if (sparePart.Productdescription != null) sparePart.Productdescription else ""
                        }


                    }
                }
            }
        }

    }

}


