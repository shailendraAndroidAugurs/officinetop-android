package com.officinetop.officine.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.officinetop.officine.R
import kotlinx.android.synthetic.main.dialog_image_viewer.view.*

class DialogTouchImageSlider(context: Context, placeholderImage: Int) : BaseSliderView(context) {

//    private var imageRes :Int = 0

    private var imageRes: String = ""
    private var mPlaceholderImage: Int = R.drawable.ic_placeholder

    override fun image(res: String): BaseSliderView {
        imageRes = res
        return super.image(res)
    }

    init {
        if (placeholderImage != null)
            mPlaceholderImage = placeholderImage
    }

    override fun getView(): View {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_image_viewer, null)
        val imageView = view.dialog_image_view
        try {
            Glide.with(context)
            .setDefaultRequestOptions(RequestOptions().placeholder(mPlaceholderImage).error(mPlaceholderImage))
                    .load(imageRes)
                    .thumbnail(0.7f)
                    .into(imageView)
            /*Glide.with(context)
                    .load(imageRes)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .fitCenter()
                    .placeholder(mPlaceholderImage)
                    .into(imageView)*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        bindEventAndShow(view, imageView)

        return view
    }


}