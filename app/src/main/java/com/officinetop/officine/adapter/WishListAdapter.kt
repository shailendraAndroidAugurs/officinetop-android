package com.officinetop.officine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.officine.R
import com.officinetop.officine.adapter.WishListAdapter.wishListViewholder
import com.officinetop.officine.car_parts.ProductDetailActivity
import com.officinetop.officine.car_parts.TyreDetailActivity
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.item_wishlist.view.*
import org.jetbrains.anko.intentFor
import org.json.JSONObject

class WishListAdapter(var context: Context, var wishListIterator: ArrayList<Models.WishList>) : RecyclerView.Adapter<wishListViewholder>() {

    class wishListViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): wishListViewholder {
        return wishListViewholder(LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false))

    }

    override fun getItemCount(): Int {
        return wishListIterator.size
    }

    override fun onBindViewHolder(holder: wishListViewholder, position: Int) {

        holder.itemView.iv_delete.setOnClickListener {
            var product_type = ""
            if (wishListIterator[position].workshopDetails != null) {
                removefromwishList("", wishListIterator[position].workshopId.toString(), wishListIterator[position], "")
            } else {
                if (wishListIterator[position].spareProductDetail != null) {
                    product_type = "1"
                } else if (wishListIterator[position].tyreProductDetail != null) {
                    product_type = "2"
                }
                removefromwishList(wishListIterator[position].productId.toString(), "", wishListIterator[position], product_type)
            }

        }

        holder.itemView.iv_AddtoCart.setOnClickListener {

            if (wishListIterator[position].spareProductDetail != null) {
                context.startActivity(context.intentFor<ProductDetailActivity>(
                        Constant.Path.productDetails to Gson().toJson(wishListIterator[position].spareProductDetail).toString(), Constant.Key.wishList to "1").forwardResults())
            } else if (wishListIterator[position].tyreProductDetail != null) {
                var tyreString = Gson().toJson(wishListIterator[position].tyreProductDetail)
                var selectedData: Models.TyreDetailItem = Gson().fromJson<Models.TyreDetailItem>(tyreString, Models.TyreDetailItem::class.java)
                try {
                    context.startActivity(context.intentFor<TyreDetailActivity>(
                            Constant.Path.productDetails to selectedData,
                            Constant.Path.productType to "Tyre").forwardResults())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (wishListIterator[position].workshopDetails != null) {
            }


        }
        if (wishListIterator[position].spareProductDetail != null || wishListIterator[position].tyreProductDetail != null || wishListIterator[position].workshopDetails != null) {
            holder.itemView.item_cardview.visibility = View.VISIBLE
            if (wishListIterator[position].spareProductDetail != null) {
                holder.itemView.wishlist_season_icon.visibility = View.GONE
                holder.itemView.ll_VehicleType.visibility = View.GONE
                holder.itemView.item_workshop_layout.visibility = View.GONE
                holder.itemView.item_Product_layout.visibility = View.VISIBLE
                holder.itemView.iv_AddtoCart.visibility = View.VISIBLE
                holder.itemView.item_sub_Product_title.visibility = View.VISIBLE
                bindSparePartDataToView(holder.itemView, wishListIterator[position])
            } else if (wishListIterator[position].tyreProductDetail != null) {
                holder.itemView.wishlist_season_icon.visibility = View.VISIBLE
                holder.itemView.ll_VehicleType.visibility = View.VISIBLE
                holder.itemView.item_workshop_layout.visibility = View.GONE
                holder.itemView.item_Product_layout.visibility = View.VISIBLE
                holder.itemView.iv_AddtoCart.visibility = View.VISIBLE
                holder.itemView.item_sub_Product_title.visibility = View.GONE
                bindTyreDataToView(holder.itemView, wishListIterator[position])
            } else if (wishListIterator[position].workshopDetails != null) {
                holder.itemView.view_forline.visibility = View.GONE
                holder.itemView.iv_AddtoCart.visibility = View.INVISIBLE
                holder.itemView.item_workshop_layout.visibility = View.VISIBLE
                holder.itemView.item_Product_layout.visibility = View.GONE
                bindWorkshopDataToView(holder.itemView, wishListIterator[position])
            }

        } else {
            holder.itemView.item_cardview.visibility = View.GONE
        }
    }

    private fun bindTyreDataToView(view: View, wishList: Models.WishList) {
        var tyreType = ""
        var tyreSeason = ""
        context.loadImage(wishList.tyreProductDetail?.imageUrl, view.item_Productimage)
        context.loadImage(wishList.tyreProductDetail?.brandImage, view.item_Productbrand_image)


        if (!wishList.tyreProductDetail?.sellerPrice.isNullOrEmpty()) {
            view.item_tyreprice.text = context.getString(R.string.prepend_euro_symbol_string, wishList.tyreProductDetail?.sellerPrice)
        } else {
            view.item_tyreprice.text = context.getString(R.string.prepend_euro_symbol_string, "0")
        }


        if (!wishList.tyreProductDetail?.description.isNullOrBlank()) {
            view.item_sub_Product_title.text = wishList.tyreProductDetail?.description
        } else if (!wishList.tyreProductDetail?.ourDescription.toString().isNullOrEmpty()) {
            view.item_sub_Product_title.text = wishList.tyreProductDetail?.ourDescription
        } else if (!wishList.tyreProductDetail?.prDescription.isNullOrEmpty()) {
            view.item_sub_Product_title.text = wishList.tyreProductDetail?.prDescription
        }


        // set tyre icons
        view.wishList_tyre_wet_grip_value.text = wishList.tyreProductDetail?.wetGrip.toString()
        view.wishList_tyre_fuel_value.text = wishList.tyreProductDetail?.rollingResistance.toString()
        view.wishList_tyre_db_value.text = wishList.tyreProductDetail?.extRollingNoiseDb.toString() + " db"

        when (wishList.tyreProductDetail?.type) {
            "s" -> {
                context.loadImageFromDrawable(R.drawable.car, view.tyre_type_icon)
                tyreType = context.getString(R.string.Car)
            }
            "w" -> {
                context.loadImageFromDrawable(R.drawable.car, view.tyre_type_icon)
                tyreType = context.getString(R.string.Car)
            }
            "g" -> {
                context.loadImageFromDrawable(R.drawable.car, view.tyre_type_icon)
                tyreType = context.getString(R.string.Car)
            }
            "m" -> {
                context.loadImageFromDrawable(R.drawable.wheel_quad, view.tyre_type_icon)
                tyreType = context.getString(R.string.Wheel_Quad_tyre)
            }
            "o" -> {
                context.loadImageFromDrawable(R.drawable.off_road_, view.tyre_type_icon)
                tyreType = context.getString(R.string.Off_road_tyre)
            }
            "i" -> {
                context.loadImageFromDrawable(R.drawable.truck, view.tyre_type_icon)
                tyreType = context.getString(R.string.Truck)
            }
            "a" -> {
                context.loadImageFromDrawable(R.drawable.all_season_tyre, view.tyre_type_icon)
                tyreType = context.getString(R.string.All)
            }


        }


        if (!wishList.tyreProductDetail?.tyreImageURL.isNullOrBlank()) {
            context.loadImage(wishList.tyreProductDetail?.tyreImageURL, view.tyre_type_icon)
        }


        when (wishList.tyreProductDetail?.type) {
            "s" -> {

                context.loadImageFromDrawable(R.drawable.summer, view.wishlist_season_icon)
                tyreSeason = "Summer"

            }
            "w" -> {

                context.loadImageFromDrawable(R.drawable.winter, view.wishlist_season_icon)
                tyreSeason = "Winter"
            }
            "g" -> {

                context.loadImageFromDrawable(R.drawable.all_seasons_tyre, view.wishlist_season_icon)
                tyreSeason = "All-Season"
            }

        }
        if (!wishList.tyreProductDetail?.tyreSeasonImageURL.isNullOrBlank()) {
            context.loadImage(wishList.tyreProductDetail?.tyreSeasonImageURL, view.wishlist_season_icon)
        }


        if (wishList.tyreProductDetail?.couponList != null && wishList.tyreProductDetail?.couponList?.size != 0) {

            view.tv_AppliedCoupon.text = (wishList.tyreProductDetail?.couponList?.get(0)?.couponTitle)
            view.tv_AppliedCoupon.visibility = View.VISIBLE
            view.tv_couponLabel.visibility = View.VISIBLE
            view.product_offer_badge.visibility = /*if (p1 % 2 == 0)*/ View.VISIBLE /*else View.GONE*/
        } else {
            view.tv_couponLabel.visibility = View.GONE
            view.tv_AppliedCoupon.visibility = View.GONE
            view.product_offer_badge.visibility = View.GONE
        }


        try {
            val startIndex = getSubString(wishList.tyreProductDetail?.description!!, 2)
            val endIndex = getSubString(wishList.tyreProductDetail?.description!!, 6)
            val description = wishList.tyreProductDetail?.description?.substring(startIndex!!, endIndex!!)
            //Log.e("index of values=", "=startindex="+startIndex+"=endindesx="+endIndex+"=descr="+description)
            view.item_Product_title.text = "${wishList.tyreProductDetail?.manufacturerDescription} ${tyreType} ${wishList.tyreProductDetail?.prDescription}\n${wishList.tyreProductDetail?.maxWidth}/${wishList.tyreProductDetail?.maxAspectRatio} R${wishList.tyreProductDetail?.maxDiameter}"//
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!wishList.tyreProductDetail?.rating_star.isNullOrBlank()) {
            view.item_tyrerating.rating = wishList.tyreProductDetail?.rating_star?.toFloat()!!
        } else
            view.item_tyrerating.rating = 0f

        if (!wishList.tyreProductDetail?.rating_count.isNullOrBlank())
            view.item_rating_tyrecount.text = wishList.tyreProductDetail?.rating_count
        else
            view.item_rating_tyrecount.text = "0"
    }

    private fun bindSparePartDataToView(view: View, wishList: Models.WishList) {
        if (!wishList.spareProductDetail?.sellerPrice.isNullOrEmpty()) {
            view.item_tyreprice.text = context.getString(R.string.prepend_euro_symbol_string, wishList.spareProductDetail?.sellerPrice)
        } else {
            view.item_tyreprice.text = context.getString(R.string.prepend_euro_symbol_string, "0")
        }


        if (!wishList.spareProductDetail?.rating_star.isNullOrBlank()) {
            view.item_tyrerating.rating = wishList.spareProductDetail?.rating_star?.toFloat()!!
        } else
            view.item_tyrerating.rating = 0f

        if (!wishList.spareProductDetail?.rating_count.isNullOrBlank())
            view.wishList_item_rating_count.text = wishList.spareProductDetail?.rating_count
        else
            view.wishList_item_rating_count.text = "0"
        if (wishList.spareProductDetail?.couponList != null && wishList.spareProductDetail?.couponList?.size != 0) {

            view.tv_AppliedCoupon.text = (wishList.spareProductDetail?.couponList?.get(0)?.couponTitle)
            view.tv_AppliedCoupon.visibility = View.VISIBLE
            view.tv_couponLabel.visibility = View.VISIBLE
            view.product_offer_badge.visibility = /*if (p1 % 2 == 0)*/ View.VISIBLE /*else View.GONE*/
        } else {
            view.tv_couponLabel.visibility = View.GONE
            view.tv_AppliedCoupon.visibility = View.GONE
            view.product_offer_badge.visibility = View.GONE
        }

        if (!wishList.spareProductDetail?.imageUrl.isNullOrBlank())
            context.loadImage(wishList.spareProductDetail?.imageUrl, view.item_Productimage, R.drawable.no_image_placeholder)

        // set title

        if (wishList.spareProductDetail?.productName == null) {
            view.item_Product_title.text = ""
        } else {
            view.item_Product_title.text = wishList.spareProductDetail?.productName.toString()
        }
        if (wishList.spareProductDetail?.Productdescription == null) {
            view.item_sub_Product_title.text = ""

        } else {
            view.item_sub_Product_title.text = wishList.spareProductDetail?.Productdescription.toString()
        }


        // Display brand image if this is spare part

        context.loadImage(wishList.spareProductDetail?.brandImage, view.item_Productbrand_image)
    }

    private fun bindWorkshopDataToView(view: View, wishList: Models.WishList) {


        view.wishList_item_title.text = wishList.workshopDetails?.companyName//json.optString("company_name")
        view.wishList_workshopAddress.text = wishList.workshopDetails?.registeredOffice

        if (wishList.workshopDetails?.ratingStar != null) {
            view.workshop_item_rating.rating = wishList.workshopDetails?.ratingStar.toFloat()

        } else
            view.workshop_item_rating.rating = 0f
        if (wishList.workshopDetails?.ratingCount!=0)
            view.wishList_item_rating_count.text = wishList.workshopDetails?.ratingCount.toString()
        else
            view.wishList_item_rating_count.text = "0"

        view.workshop_item_image.setImageResource(R.drawable.no_image_placeholder)

        //load images
        try {

            context.loadImageWithName(wishList.workshopDetails?.Profile_image, view.workshop_item_image, R.drawable.no_image_placeholder, baseURL = Constant.profileBaseUrl)

            // context.loadImage(wishList.workshopDetails?.workShopImageUrl, view.workshop_item_image)

        } catch (e: Exception) {
        }

        if (wishList.workshopDetails?.couponList != null && wishList.workshopDetails?.couponList?.size != 0) {

            view.WS_AppliedCoupon.text = (wishList.workshopDetails?.couponList?.get(0)?.couponTitle)
            view.WS_AppliedCoupon.visibility = View.VISIBLE
            view.WS_couponLabel.visibility = View.VISIBLE
            view.WSoffer_badge.visibility = /*if (p1 % 2 == 0)*/ View.VISIBLE /*else View.GONE*/
        } else {
            view.WS_couponLabel.visibility = View.GONE
            view.WS_AppliedCoupon.visibility = View.GONE
            view.WSoffer_badge.visibility = View.GONE
        }


    }

    private fun removefromwishList(productId: String, workshopId: String, wishList: Models.WishList, productType: String) {
        RetrofitClient.client.removeFromFavorite(context.getBearerToken()
                ?: "", productId, workshopId, productType).onCall { networkException, response ->

            response.let {
                if (response?.isSuccessful!!) {
                    val body = JSONObject(response?.body()?.string())
                    if (body.has("message")) {

                        wishListIterator.remove(wishList)
                        notifyDataSetChanged()
                        /* if (productId != null && !productId.equals(""))
                             context.showInfoDialog("This product Removed from wishlist ")
                         else
                             context.showInfoDialog("This Workshop Removed from favorite ")*/

                    }

                }

            }
        }
    }
}


