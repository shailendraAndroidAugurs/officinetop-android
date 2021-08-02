package com.officinetop.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.officinetop.R
import com.officinetop.binding_adapters.imageLoad
import com.officinetop.data.Models
import com.officinetop.data.getUserId
import com.officinetop.rim.AvailableRimActivity
import com.officinetop.rim.RimProductDetailsActivity
import com.officinetop.utils.addToFavoriteSendRequest
import com.officinetop.utils.addToFavoriterimSendRequest
import com.officinetop.utils.removeFromFavoriteSendRquest
import kotlinx.android.synthetic.main.activity_available_rim.*
import kotlinx.android.synthetic.main.activity_rim_product_details.*
import kotlinx.android.synthetic.main.item_faq.view.*
import kotlinx.android.synthetic.main.item_list_car_availble_rim.view.*
import kotlinx.android.synthetic.main.item_list_rim_product.view.*
import kotlinx.android.synthetic.main.item_list_rim_product.view.Iv_favorite
import kotlinx.android.synthetic.main.item_list_rim_product.view.item_price
import kotlinx.android.synthetic.main.item_list_version_car.view.*
import kotlinx.android.synthetic.main.item_product_or_workshop_detail.view.*
import org.jetbrains.anko.intentFor

class RimProductlistAdapter(private val context: Context, private val productlist: List<Models.rimProductDetails>) : RecyclerView.Adapter<RimProductlistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_rim_product, parent, false))
    }

    override fun getItemCount(): Int = productlist.size


    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        imageLoad(holder.prodcut_image, productlist.get(i).ImageUrl)
        holder.tv_type.text = productlist.get(i).Manufacturer
        holder.tv_type_name.text = productlist.get(i).TypeName
        holder.tv_type_color.text = productlist.get(i).TypeColor
        holder.item_price.text = context.getString(R.string.prepend_euro_symbol_string, productlist.get(i).AlloyRimPrice)

        if (!productlist.get(i).rim_posteriore.isNullOrEmpty())
            holder.tv_rim_posteriore.text = productlist.get(i).rim_posteriore
        else
            holder.tv_rim_posteriore.visibility = View.GONE

        if (!productlist.get(i).rim_anteriore.isNullOrEmpty())
            holder.tv_rim_anteriore.text = productlist.get(i).rim_anteriore
        else
            holder.tv_rim_anteriore.visibility = View.GONE

        holder.parent_layout.setOnClickListener {
            context.startActivity(context.intentFor<RimProductDetailsActivity>().putExtra("front_id", productlist.get(i).AlloyRimFrontID).putExtra("rear_id", productlist.get(i).AlloyRimRearID))
        }


        if (productlist.get(i).wish_list != null && productlist.get(i).wish_list == "1") {
            holder.Iv_favorite.setImageResource(R.drawable.ic_heart)

        } else {
            holder.Iv_favorite.setImageResource(R.drawable.ic_favorite_border_black_empty_24dp)
        }


        holder.Iv_favorite.setOnClickListener {

            try {
                if ((productlist.get(i).wish_list == null || productlist.get(i).wish_list == "") || productlist.get(i).wish_list == "0") {

                    context.addToFavoriterimSendRequest(context, productlist.get(i).Id, "3", holder.Iv_favorite, productlist.get(i).AlloyRimFrontID, productlist.get(i).AlloyRimRearID,productlist.get(i))

                } else {
                    //   context.removeFromFavoriteSendRquest(context, productlist.get(i).Id, holder.Iv_favorite, null, context.getUserId(), productlist.get(i))

                }


            } catch (e: java.lang.Exception) {

            }


        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val prodcut_image: ImageView = view.prodcut_image
        val tv_type: TextView = view.tv_type
        val tv_type_name: TextView = view.tv_type_name
        val tv_type_color: TextView = view.tv_type_color
        val tv_rim_anteriore: TextView = view.tv_rim_anteriore
        val tv_rim_posteriore: TextView = view.tv_rim_posteriore
        val item_price: TextView = view.item_price
        val parent_layout: LinearLayout = view.parent_layout
        val Iv_favorite: ImageView = view.Iv_favorite


    }


}