package com.officinetop.officine.Washing

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.daimajia.slider.library.Indicators.PagerIndicator
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.daimajia.slider.library.Tricks.ViewPagerEx
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.utils.*
import com.officinetop.officine.views.DialogTouchImageSlider
import com.officinetop.officine.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.activity_service_detail.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_grid_home_square.view.*
import org.jetbrains.anko.intentFor

class ServiceDetailActivity : BaseActivity() {
    var service_id = 0
    private lateinit var imageDialog: Dialog
    private lateinit var dialogSlider: SliderLayout
    var disableSliderTouch = false
    var serviceList: ArrayList<Models.ServiceCategory> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = resources.getString(R.string.detail)

        //Default slider images
        setImageSlider()
        if (isOnline()) {
            loadServiceDetails()
        }else{
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }
    }

    private fun loadServiceDetails() {
        val serviceDetails: Models.ServiceCategory = intent.getSerializableExtra(Constant.Path.washServiceDetails) as Models.ServiceCategory
        Log.v("SERVICE DTAILS ", "****************************** $serviceDetails")
        if (serviceDetails != null) {
            var servicePrice = ""

            service_id = serviceDetails.id!!

            loadImage(serviceDetails.cat_image_url, service_image)

            service_title.text = serviceDetails.category_name

            if (serviceDetails.description != null && serviceDetails.description.isNotEmpty())
                service_description.text = serviceDetails.description
            else
                service_description.text = getString(R.string.sample_desc)

            if (serviceDetails.services_price != null)
                servicePrice = serviceDetails.services_price
            service_price.text = getString(R.string.prepend_euro_symbol_with_from_string, servicePrice)
            if (!serviceDetails.vat_Admin.isNullOrBlank())
                tv_Include_Vat.text = getString(R.string.iva_inc_22Percentage, serviceDetails.vat_Admin) + "%"
            else
                tv_Include_Vat.text = ""
            proceed_to_list.setOnClickListener {
                startActivity(intentFor<WorkshopListActivity>(
                        Constant.Key.is_car_wash to true,
                        Constant.Path.washServiceDetails to serviceDetails))
            }

            //Add slider images
            if (serviceDetails.itemImages != null && serviceDetails.itemImages.size > 0) {
                setImageSlider(serviceDetails.itemImages)
            }
            if (intent.extras != null && intent.hasExtra("servicesList")) {
                val serviceListfilter: ArrayList<Models.ServiceCategory> = ArrayList()
                serviceList = intent.getSerializableExtra("servicesList") as ArrayList<Models.ServiceCategory>
                serviceList.forEach {

                    if (it.id != service_id)

                        serviceListfilter.add(it)
                }
                loadProductRecommendationGridList(product_recommendation_recycler_view, serviceListfilter)
            }
        }
    }

    private fun setImageSlider() {
        //set slider
        image_slider.removeAllSliders()
        createImageSliderDialog()
        for (i in 0..1) {
            val imageRes = if (i % 2 == 0) R.drawable.no_image_placeholder else R.drawable.no_image_placeholder

            val slide = TextSliderView(this).image(imageRes).empty(R.drawable.no_image_placeholder)
            image_slider.addSlider(slide)

            val scaledSlide = DialogTouchImageSlider(this, imageRes)
                    .description("Description")
                    .image(imageRes)
            dialogSlider.addSlider(scaledSlide)

            slide.setOnSliderClickListener {

                if (disableSliderTouch)
                    return@setOnSliderClickListener

                dialogSlider.currentPosition = i
                imageDialog.show()
            }
        }


        image_slider.addOnPageChangeListener(object : ViewPagerEx.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                disableSliderTouch = state != ViewPagerEx.SCROLL_STATE_IDLE
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
            }

        })
    }

    private fun setImageSlider(imagesArray: ArrayList<Models.ItemImages>) {
        //set slider
        if(imagesArray.size>1){
            image_slideview.visibility=View.GONE
            image_slider.visibility=View.VISIBLE
            createImageSliderDialog()
            image_slider.removeAllSliders()
            for (i in 0 until imagesArray.size) {
                val imageRes = imagesArray[i].image_url
                val slide = TextSliderView(this).image(imageRes).setScaleType(BaseSliderView.ScaleType.CenterInside).empty(R.drawable.no_image_placeholder)
                image_slider.addSlider(slide)
                val scaledSlide = DialogTouchImageSlider(this, R.drawable.no_image_placeholder)
                        .description("Description")
                        .image(imageRes)
                dialogSlider.addSlider(scaledSlide)
                slide.setOnSliderClickListener {
                    if (disableSliderTouch)
                        return@setOnSliderClickListener
                    dialogSlider.currentPosition = i
                    imageDialog.show()
                }
            }
            image_slider.addOnPageChangeListener(object : ViewPagerEx.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    disableSliderTouch = state != ViewPagerEx.SCROLL_STATE_IDLE
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                }

            })
        }else{
            val imageRes = imagesArray[0].image_url
            image_slideview.visibility=View.VISIBLE
            image_slider.visibility=View.GONE
            loadImage(imageRes, image_slideview)
            image_slideview.setOnClickListener({
                createImageDialog(imageRes)
                imageDialog.show()
            })
        }

    }

    private fun createImageSliderDialog() {

        imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)

        val slider = SliderLayout(this)

        slider.stopAutoCycle()
        slider.indicatorVisibility = PagerIndicator.IndicatorVisibility.Visible
        slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        dialogSlider = slider

        with(imageDialog) {
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            setContentView(slider)

            window?.setGravity(android.view.Gravity.TOP)
            window?.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
            create()

        }
    }

    private fun createImageDialog(imageRes: String) {
        imageDialog = Dialog(this, R.style.DialogSlideAnimStyle)
        val slider = ImageView(this)
        loadImage(imageRes, slider)
        slider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        with(imageDialog) {
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            setContentView(slider)
            window?.setGravity(android.view.Gravity.TOP)
            window?.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
            create()

        }
    }

    private inline fun Activity.loadProductRecommendationGridList(recyclerView: androidx.recyclerview.widget.RecyclerView, serviceListFiletr: ArrayList<Models.ServiceCategory>) {

        //product recycler
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

        recyclerView.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {


                val view = layoutInflater.inflate(R.layout.item_grid_home_square, p0, false)
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {}
            }

            override fun getItemCount(): Int = serviceListFiletr.size

            override fun onBindViewHolder(p0: androidx.recyclerview.widget.RecyclerView.ViewHolder, p1: Int) {
                if (serviceListFiletr[p1].id != service_id) {


                    if (serviceListFiletr[p1].category_name != null)
                        p0.itemView.item_name.text = serviceListFiletr[p1].category_name
                    else
                        p0.itemView.item_name.text = "Service Name"

                    if (serviceListFiletr[p1].services_price != null) {
                        p0.itemView.item_sub_titleGrid.text = getString(R.string.prepend_euro_symbol_with_da_string, serviceListFiletr[p1].services_price)
                    } else
                        p0.itemView.item_sub_titleGrid.text = getString(R.string.prepend_euro_symbol_with_da, 0)


//                    loadImage(serviceList[p1].cat_image_url, item_image)
                    if (!serviceListFiletr[p1].cat_image_url.isNullOrEmpty())
                        loadImage(serviceListFiletr[p1].cat_image_url, p0.itemView.item_icon, R.drawable.no_image_placeholder)
                    p0.itemView.setOnClickListener {
                        startActivity(intentFor<ServiceDetailActivity>(Constant.Key.is_workshop to true, Constant.Key.is_car_wash to true,
                                Constant.Path.washServiceDetails to serviceListFiletr[p1],
                                "servicesList" to serviceList
                        )
                        )
                        finish()

                    }
                }


            }

        }

    }


}
