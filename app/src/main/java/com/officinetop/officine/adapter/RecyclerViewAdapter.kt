package com.officinetop.officine.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.officine.R
import com.officinetop.officine.car_parts.TyreDetailActivity
import com.officinetop.officine.data.Models
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.item_tyre.view.*
import org.jetbrains.anko.intentFor

class RecyclerViewAdapter(val context: Context, list: MutableList<Models.TyreDetailItem>) : RecyclerView.Adapter<BaseViewHolder>() {
    private var isLoadingVisible = false
    private var listItems = list
    private var tyreType = ""
    private var tyreSeason = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_tyre, parent, false))


    }

    override fun getItemCount(): Int {
        return if (listItems.size == null) 0 else listItems.size

    }


    override fun onBindViewHolder(@NonNull holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }


    fun addItems(items: MutableList<Models.TyreDetailItem>) {
        listItems.addAll(items)
        notifyDataSetChanged()
    }

    fun getLoadedItem(): Int {
        return listItems.size

    }


    fun addLoading() {
        isLoadingVisible = true

    }

    fun removeLoading() {
        isLoadingVisible = false
    }


    fun clear() {
        listItems.clear()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Models.TyreDetailItem {
        return listItems.get(position)
    }

    open inner class ViewHolder(view: View) : BaseViewHolder(view) {

        private val title = view.item_title
        private val distance = view.item_sub_title
        private val icon = view.item_image
        private val price = view.item_price
        private val rating = view.item_rating
        private val ratingCount = view.item_rating_count
        private val tireCellContainer = view.tire_cell
        private val matchCode = view.item_sub_title
        private val ourDescription = view.item_description
        private val tyreTypeIcon = view.tyre_season_icon
        private val tyreFuelValue = view.tyre_fuel_value
        private val tyreWetGripValue = view.tyre_wet_grip_value
        private val tyreDbValue = view.tyre_db_value
        private val seasonIcon = view.season_icon
        private val brandImage = view.item__brand_image
        private val ivFavorite = view.Iv_favorite
        private val appliedCouponName = view.tv_AppliedCoupon
        private val couponLabel = view.tv_couponLabel
        private val offerBadge = view.offer_badge
        override fun clear() {
        }

        override fun onBind(position: Int) {

            val items: Models.TyreDetailItem = listItems.get(position)
            context.loadImage(items.imageUrl, icon)

            if (items.brand_image.isNullOrBlank()) {
                brandImage.visibility = View.GONE
            } else {
                context.loadImage(items.brand_image, brandImage)
            }

            matchCode.text = "ean ${items.ean_number}"

            if (!items.seller_price.isNullOrEmpty()) {
                price.text = context.getString(R.string.prepend_euro_symbol_string, items.seller_price)
            } else {
                price.text = context.getString(R.string.prepend_euro_symbol_string, "0")
            }

            if (!items.description.equals("null")) {
                ourDescription.text = items.description
            } else if (items.our_description.toString().isEmpty() || items.our_description!! == "null") {
                ourDescription.text = items.manufacturer_description
            }
            rating.rating = if (!items.ratingStar.isNullOrEmpty()) items.ratingStar.toFloat() else 0.0F
            ratingCount.text = if (!items.ratingCount.isNullOrEmpty()) items.ratingCount else ""

            // set tyre icons
            tyreWetGripValue.text = if (items.wetGrip == null) "db" else items.wetGrip
            tyreFuelValue.text = if (items.rollingResistance == null) "db" else items.rollingResistance
            tyreDbValue.text = if (items.noiseDb == null) "db" else items.noiseDb + " db"

            when (items.type) {
                "s" -> {
                    context.loadImageFromDrawable(R.drawable.car, tyreTypeIcon)
                    tyreType = context.getString(R.string.Car)
                }
                "w" -> {
                    context.loadImageFromDrawable(R.drawable.car, tyreTypeIcon)
                    tyreType = context.getString(R.string.Car)
                }
                "g" -> {
                    context.loadImageFromDrawable(R.drawable.car, tyreTypeIcon)
                    tyreType = context.getString(R.string.Car)
                }
                "m" -> {
                    context.loadImageFromDrawable(R.drawable.wheel_quad, tyreTypeIcon)
                    tyreType = context.getString(R.string.Wheel_Quad_tyre)
                }
                "o" -> {
                    context.loadImageFromDrawable(R.drawable.off_road_, tyreTypeIcon)
                    tyreType = context.getString(R.string.Off_road_tyre)
                }
                "i" -> {
                    context.loadImageFromDrawable(R.drawable.truck, tyreTypeIcon)
                    tyreType = context.getString(R.string.Truck)
                }
                "a" -> {
                    context.loadImageFromDrawable(R.drawable.all_season_tyre, tyreTypeIcon)
                    tyreType = context.getString(R.string.All)
                }


            }


            if (!items.tyreImageURL.isNullOrBlank()) {
                context.loadImage(items.tyreImageURL, tyreTypeIcon)
            }


            when (items.season_tyre_type) {
                "s" -> {
                    context.loadImageFromDrawable(R.drawable.summer, seasonIcon)
                    tyreSeason = context.getString(R.string.Summer)

                }
                "w" -> {

                    context.loadImageFromDrawable(R.drawable.winter, seasonIcon)
                    tyreSeason = context.getString(R.string.Winter)
                }
                "g" -> {

                    context.loadImageFromDrawable(R.drawable.all_seasons_tyre, seasonIcon)
                    tyreSeason = context.getString(R.string.All_Season)
                }

            }



            if (!items.tyreSeasonImageURL.isNullOrBlank()) {
                context.loadImage(items.tyreSeasonImageURL, seasonIcon)
            }


            if (items.couponList != null && items.couponList.size != 0) {

                appliedCouponName.text = (items.couponList.get(0).couponTitle)
                appliedCouponName.visibility = View.VISIBLE
                couponLabel.visibility = View.VISIBLE
                offerBadge.visibility = /*if (p1 % 2 == 0)*/ View.GONE /*else View.GONE*/
            } else {
                couponLabel.visibility = View.GONE
                appliedCouponName.visibility = View.GONE
                offerBadge.visibility = View.GONE
            }

            try {
                if (!items.typeStatus.isNullOrBlank() && items.typeStatus.equals("2")) {
                    title.text = "${if (items.manufacturer_description != null) items.manufacturer_description else ""} ${if (tyreSeason != null) tyreSeason else ""} ${if (items.pr_description != null) items.pr_description else ""}\n${items.max_width}/${items.max_aspect_ratio} R${items.max_diameter}   ${if (items.load_speed_index != null) items.load_speed_index else ""} ${if (items.speed_index != null) items.speed_index else ""}"//
                    Log.d("tyreType", "  ${if (items.load_speed_index != null) items.load_speed_index else ""} ${if (items.speed_index != null) items.speed_index else ""}")
                } else
                    title.text = "${if (items.manufacturer_description != null) items.manufacturer_description else ""} ${if (tyreSeason != null) tyreSeason else ""} ${if (items.pr_description != null) items.pr_description else ""}\n${items.max_width}/${items.max_aspect_ratio} R${items.max_diameter}   ${if (items.load_speed_index != null) items.load_speed_index else ""} ${if (items.speed_index != null) items.speed_index else ""}"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            tireCellContainer.setOnClickListener {
                try {
                    context.startActivity(context.intentFor<TyreDetailActivity>(
                            Constant.Path.productDetails to items,

                            Constant.Path.productType to "Tyre").forwardResults())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            if (items.wish_list == "1") {
                ivFavorite.setImageResource(R.drawable.ic_heart)
                items.wish_list = "1"
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
                items.wish_list = "0"
            }

            ivFavorite.setOnClickListener {

                try {
                    if (items.wish_list == "0") {

                        context.addToFavoriteSendRequest(context, items.id.toString(), "2", ivFavorite, items)
                        notifyDataSetChanged()
                    } else {
                        context.removeFromFavoriteSendRquest(context, items.id.toString(), ivFavorite, items, "", null, "2")
                        notifyDataSetChanged()
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }


    }

}