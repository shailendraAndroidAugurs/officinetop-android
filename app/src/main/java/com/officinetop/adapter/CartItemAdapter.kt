package com.officinetop.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.officinetop.MOT.PartList_Replacement
import com.officinetop.R
import com.officinetop.data.Models
import com.officinetop.data.getBearerToken
import com.officinetop.data.isStatusCodeValid
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import kotlinx.android.synthetic.main.activity_mot_detail.*
import kotlinx.android.synthetic.main.cart_items_layout.view.*
import kotlinx.android.synthetic.main.item_sparepart_mot.*
import kotlinx.android.synthetic.main.recycler_view_for_dialog.view.*
import org.json.JSONObject

class CartItemAdapter(private var context: Context, view: Button) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {
    private var cartItems: MutableList<Models.CartDataList> = ArrayList()
    private var onItemChanged: OnItemChanged? = null
    private var onCartListCallback: OnCartListCallback? = null
    private var totalPrice: Double = 0.0
    private var ProceedToPay: Button? = null
    var dialog: Dialog? = null
    private var genericAdapterParts: GenericAdapter<Models.Part>? = null

    init {
        ProceedToPay = view
    }

    fun setCartListener(onCartListCallback: OnCartListCallback) {
        this.onCartListCallback = onCartListCallback
    }

    fun setOnItemChangedListener(onItemChanged: OnItemChanged) {
        this.onItemChanged = onItemChanged
    }


    fun addProduct(cartItem: Models.CartDataList?) {
        cartItem?.let {


            if (!cartItems.contains(cartItem)) {
                cartItems.add(cartItem)
            } else {
                val index = cartItems.indexOf(cartItem)
                cartItems[index] = cartItem
            }

            notifyDataSetChanged()
        }
    }


    fun getItemAt(position: Int) = cartItems[position]


    interface OnItemChanged {
        fun onDeleted(position: Int)
        fun onQuantityChanged(cartItem: Models.CartDataList?)
    }


    private fun removeCartItem(cartItem: Models.CartDataList) {
        var carttype = ""
        var CartTotalPrice = "0.0"
        if (cartItem.CartType == "SP") {
            carttype = "2"
            if (cartItem.serviceAssemblyProductDescription != null) {
                CartTotalPrice = cartItem.serviceAssemblyProductDescription.totalPrice.takeIf { !it.isNullOrEmpty() }?.toDouble().toString()

            } else {
                CartTotalPrice += cartItem.price

            }
        } else if (cartItem.CartType == "T" || cartItem.CartType == "S") {
            carttype = "1"
            CartTotalPrice = cartItem.totalPrice.takeIf { !it.isNullOrEmpty() }?.toDouble().toString()
        }
        context.showConfirmDialog(context.getString(R.string.DeleteFromCart)) {
            deleteCartItem(cartItem, carttype, CartTotalPrice)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): CartViewHolder {

        return CartViewHolder(LayoutInflater.from(context).inflate(R.layout.cart_items_layout, p0, false))
    }

    override fun getItemCount(): Int = cartItems.size


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartViewHolder, p1: Int) {

        val item = cartItems[p1]
        var isProductSellOnpair = false


        with(holder) {

            var productID = ""
            var quantity = 1

            if(item.CartType == "S"){
                productDescription.visibility = View.GONE
            }

            if (item.CartType == "T" || item.CartType == "S") {
                if (item.CartType == "T") {
                    productPFU.visibility = View.VISIBLE
                } else {
                    pfutextLabel.visibility = View.GONE
                    productPFU.visibility = View.GONE
                    productVat.visibility = View.GONE

                }
                isProductSellOnpair = !item.IsProductPair.isNullOrBlank() && item.IsProductPair != "0"
                Log.d("cartItemAdapter", "isProductSellOnpair: $isProductSellOnpair")
                Log.d("cartItemAdapter", "quantity:Sp : $quantity")
                if (item.pfuDesc.isNullOrBlank()) {
                    productVat.text = context.getString(R.string.concat)
                } else {
                    productVat.text = item.pfuDesc
                }
                if (item.discount.isNullOrBlank() || item.discount == "0" || item.discount == "0.0") {
                    productDiscount.text = context.getString(R.string.prepend_euro_symbol_string, "0.0")
                    productDiscount.visibility = View.GONE
                    tv_labeldiscount.visibility = View.GONE
                } else {
                    productDiscount.text = context.getString(R.string.prepend_euro_symbol_string, item.discount)
                    productDiscount.visibility = View.VISIBLE
                    tv_labeldiscount.visibility = View.VISIBLE
                }

                if (item.finalOrderPrice.isNullOrBlank()) {
                    ProductTotal.text = context.getString(R.string.prepend_euro_symbol_string, "0.0")
                } else {
                    ProductTotal.text = context.getString(R.string.prepend_euro_symbol_string, item.finalOrderPrice)
                }

                productName.text = item.productName.takeIf { !it.isNullOrEmpty() }
                productDescription.text = item.productDescription.takeIf { !it.isNullOrEmpty() }
                cartItemProductLayout.visibility = View.VISIBLE
                cartItemServiceLayout.visibility = View.GONE

                if (!item.productImageUrl.isNullOrBlank()) {
                    context.loadImage(item.productImageUrl, cartItemProductImage)
                } else {
                    cartItemProductImage.visibility = View.GONE
                }
                productPrice.text = context.getString(R.string.prepend_euro_symbol_string, item.price.takeIf { !it.isNullOrEmpty() })
                if (item.productQuantity != "0")
                    quantity = item.productQuantity.toInt()
                productID = item.id

                if (!item.avilability.isNullOrBlank() && item.avilability.equals("0")) {
                    isProduct_Available.visibility = View.VISIBLE
                    if (!item.maxProductQuantity.isNullOrBlank() && item.maxProductQuantity.toInt() >= 0) {
                        isProduct_Available.text = context.getString(R.string.out_of_stock)
                        quantitySpinner.visibility = View.GONE
                        ProceedToPay?.isEnabled = false
                        ProceedToPay?.let { Snackbar.make(it, context.getString(R.string.PleaseDeleteOutOfStockProduct), Snackbar.LENGTH_SHORT).show() }

                    } else if (!item.maxProductQuantity.isNullOrBlank() && !item.productQuantity.isNullOrBlank() && item.maxProductQuantity < item.productQuantity) {
                        quantitySpinner.visibility = View.VISIBLE
                        quantity = item.maxProductQuantity.toInt()

                        updateCartQuantity(quantity, item, item.id)
                        isProduct_Available.text = context.getString(R.string.re_maining_item, item.maxProductQuantity)
                    }

                }

                if (quantitySpinner.isVisible) {
                    if (item.pfuTax.isNullOrBlank() || item.pfuTax == "0") {
                        pfutextLabel.visibility = View.GONE
                        productPFU.visibility = View.GONE
                        productVat.visibility = View.GONE
                    } else {

                        pfutextLabel.visibility = View.VISIBLE
                        productPFU.visibility = View.VISIBLE
                        productVat.visibility = View.VISIBLE
                        productPFU.text = context.getString(R.string.prepend_euro_symbol_string, (quantity * item.pfuTax.toDouble().roundTo2Places()).roundTo2Places().toString())
                    }
                } else {
                    pfutextLabel.visibility = View.GONE
                    productPFU.visibility = View.GONE
                    productVat.visibility = View.GONE

                }




                if (item.couponTitle != null && !item.CouponPrices.isNullOrBlank() && !item.couponType.isNullOrBlank() && item.couponType == "1") {

                    tv_couponDetailProduct.text = item.couponTitle + " :" + context.getString(R.string.euro_symbol) + item.CouponPrices /*+ " %"*/
                } else if (item.couponTitle != null && !item.CouponPrices.isNullOrBlank() && !item.couponType.isNullOrBlank() && item.couponType == "2") {
                    tv_couponDetailProduct.text = item.couponTitle + " :" + context.getString(R.string.euro_symbol) + item.CouponPrices
                } else {
                    tv_couponDetailProduct.visibility = View.GONE
                    Log.d("coupondetail", item.couponTitle + item.CouponPrices + item.couponType)
                }


            } else if (item.CartType == "SP") {
                llMotSparePartLayout.visibility = View.GONE
                if (item.serviceAssemblyProductDescription != null) {
                    productName.text = item.serviceAssemblyProductDescription.productName.takeIf { !it.isNullOrEmpty() }
                    productDescription.text = item.serviceAssemblyProductDescription.productDescription.takeIf { !it.isNullOrEmpty() }
                    cartItemProductLayout.visibility = View.VISIBLE
                    cartItemServiceLayout.visibility = View.VISIBLE
                    context.loadImage(item.serviceAssemblyProductDescription.productImageUrl, cartItemProductImage)

                    productID = item.serviceAssemblyProductDescription.id
                    isProductSellOnpair = !item.serviceAssemblyProductDescription.IsProductPair.isNullOrBlank() && item.serviceAssemblyProductDescription.IsProductPair != "0"

                    productPrice.text = context.getString(R.string.prepend_euro_symbol_string, item.serviceAssemblyProductDescription.price.takeIf { !it.isNullOrEmpty() })
                    if (!item.serviceAssemblyProductDescription.productQuantity.isNullOrBlank() && item.serviceAssemblyProductDescription.productQuantity != "0")
                        quantity = item.serviceAssemblyProductDescription.productQuantity.toInt()
                    if (item.serviceAssemblyProductDescription.pfuDesc.isNullOrBlank()) {
                        productVat.text = context.getString(R.string.concat)
                        productVat.visibility = View.GONE

                    } else {
                        productVat.visibility = View.VISIBLE
                        productVat.text = item.serviceAssemblyProductDescription.pfuDesc
                    }


                    if (item.serviceAssemblyProductDescription.discount.isNullOrBlank() || item.serviceAssemblyProductDescription.discount == "0" || item.serviceAssemblyProductDescription.discount == "0.0") {
                        productDiscount.visibility = View.GONE
                        tv_labeldiscount.visibility = View.GONE
                        productDiscount.text = context.getString(R.string.prepend_euro_symbol_string, "0.0")
                    } else {
                        productDiscount.visibility = View.VISIBLE
                        tv_labeldiscount.visibility = View.VISIBLE
                        productDiscount.text = context.getString(R.string.prepend_euro_symbol_string, item.serviceAssemblyProductDescription.discount)
                    }
                    if (item.serviceAssemblyProductDescription.pfuTax.isNullOrBlank() || item.serviceAssemblyProductDescription.pfuTax == "0") {
                        pfutextLabel.visibility = View.GONE
                        productPFU.visibility = View.GONE
                        productVat.visibility = View.GONE

                        productPFU.text = context.getString(R.string.prepend_euro_symbol_string, "0.0")
                    } else {
                        productPFU.text = context.getString(R.string.prepend_euro_symbol_string, (quantity * item.serviceAssemblyProductDescription.pfuTax.toDouble().roundTo2Places()).roundTo2Places().toString())
                    }
                    if (item.serviceAssemblyProductDescription.finalOrderPrice.isNullOrBlank()) {
                        ProductTotal.text = context.getString(R.string.prepend_euro_symbol_string, "0.0")
                    } else {
                        ProductTotal.text = context.getString(R.string.prepend_euro_symbol_string, item.serviceAssemblyProductDescription.finalOrderPrice)
                    }
                    if (item.serviceAssemblyProductDescription.couponTitle != null && !item.serviceAssemblyProductDescription.CouponPrices.isNullOrBlank() && !item.serviceAssemblyProductDescription.couponType.isNullOrBlank() && item.serviceAssemblyProductDescription.couponType == "1") {

                        tv_couponDetailProduct.text = item.serviceAssemblyProductDescription.couponTitle + " : " + context.getString(R.string.euro_symbol) + item.serviceAssemblyProductDescription.CouponPrices /*+ " %"*/
                    } else if (item.serviceAssemblyProductDescription.couponTitle != null && !item.serviceAssemblyProductDescription.CouponPrices.isNullOrBlank() && !item.serviceAssemblyProductDescription.couponType.isNullOrBlank() && item.serviceAssemblyProductDescription.couponType == "2") {
                        tv_couponDetailProduct.text = item.serviceAssemblyProductDescription.couponTitle + " : " + context.getString(R.string.euro_symbol) + item.serviceAssemblyProductDescription.CouponPrices
                    } else {
                        tv_couponDetailProduct.visibility = View.GONE
                    }

                } else {
                    cartItemProductLayout.visibility = View.GONE
                    cartItemServiceLayout.visibility = View.VISIBLE
                }
                if (item.couponTitle != null && !item.CouponPrices.isNullOrBlank() && !item.couponType.isNullOrBlank() && item.couponType == "1") {

                    tv_coupon_cart_services.text = item.couponTitle + " : " + context.getString(R.string.euro_symbol) + item.CouponPrices /*+ " %"*/
                } else if (item.couponTitle != null && !item.CouponPrices.isNullOrBlank() && !item.couponType.isNullOrBlank() && item.couponType == "2") {
                    tv_coupon_cart_services.text = item.couponTitle + " : " + context.getString(R.string.euro_symbol) + item.CouponPrices
                } else {
                    tv_coupon_cart_services.visibility = View.GONE
                }
                if (!item.discount.isNullOrBlank() && item.discount != "0") {
                    servicesVat.visibility = View.VISIBLE
                    tv_discount.visibility = View.VISIBLE
                    servicesVat.text = context.getString(R.string.prepend_euro_symbol_string, item.discount)

                } else {
                    servicesVat.visibility = View.GONE
                    tv_discount.visibility = View.GONE
                    servicesVat.text = context.getString(R.string.prepend_euro_symbol_string, item.discount.takeIf { !it.isNullOrEmpty() })
                }


                if (item.serviceDetail != null && item.serviceDetail.mainCategoryId != null && item.serviceDetail.mainCategoryId.equals("2")) {
                    if (item.afterDiscountPrice.isNullOrBlank()) {
                        if (item.price != null && item.discount != null) {
                            ServicesTotalPrices.text = context.getString(R.string.prepend_euro_symbol_string, (item.price.toInt() - item.discount.toInt()).toString())
                        }


                    } else {
                        ServicesTotalPrices.text = context.getString(R.string.prepend_euro_symbol_string, item.afterDiscountPrice.takeIf { !it.isNullOrEmpty() })
                    }
                } else {
                    if (item.afterDiscountPrice.isNullOrBlank()) {
                        ServicesTotalPrices.text = context.getString(R.string.prepend_euro_symbol_string, item.afterDiscountPrice.takeIf { !it.isNullOrEmpty() })
                    } else {
                        ServicesTotalPrices.text = context.getString(R.string.prepend_euro_symbol_string, item.afterDiscountPrice.takeIf { !it.isNullOrEmpty() })
                    }
                }


               /* if (item.partDetails != null && item.partDetails.size != 0 && item.serviceDetail != null) {
                     partInfo.visibility = View.VISIBLE

                } else {
                    partInfo.visibility = View.GONE
                }
                // partInfo.setOnClickListener { partsDialog(cartItems[p1].partDetails as ArrayList<Models.Part>, context) }
                  partInfo.setOnClickListener { moveToMotDetailPageFromCart(item.partDetails as ArrayList<Models.Part>, item.serviceDetail,context) }*/

                if (item.serviceDetail != null) {
                    if (!item.serviceDetail.mainCategoryId.isNullOrBlank() && item.serviceDetail.mainCategoryId.equals("25")) {
                        serviceName.text = context.getString(R.string.cart_quotes) + " " + item.serviceDetail.serviceName.takeIf { !it.isNullOrEmpty() }
                        cartItemServiceImage.visibility = View.GONE
                        serviceName.gravity = Gravity.CENTER

                        servicePrice.text = if (!item.price.isNullOrEmpty() && item.price != "null") context.getString(R.string.prepend_euro_symbol_string, item.price) else context.getString(R.string.prepend_euro_symbol_string, "0")
                    } else {
                        serviceName.text = item.serviceDetail.serviceName.takeIf { !it.isNullOrEmpty() }
                        if (!item.serviceDetail.serviceDescription.isNullOrBlank()) {
                            tvCartServiceDescription.text = item.serviceDetail.serviceDescription


                            /* if(item.serviceDetail.serviceDescription.toString().length >= 150){
                                 addReadMore(holder.revDesc.text.toString(), holder.revDesc)
                             }*/


                        } else tvCartServiceDescription.visibility = View.GONE

                        context.loadImage(item.serviceDetail.catImageUrl.takeIf { !it.isNullOrEmpty() }, cartItemServiceImage)

                        if (!item.serviceDetail.catImageUrl.isNullOrBlank()) {
                            context.loadImage(item.serviceDetail.catImageUrl, cartItemServiceImage)
                        } else {
                            serviceName.gravity = Gravity.CENTER
                            cartItemServiceImage.visibility = View.GONE
                        }

                        servicePrice.text = if (!item.price.isNullOrEmpty() && item.price != "null") context.getString(R.string.prepend_euro_symbol_string, item.price) else context.getString(R.string.prepend_euro_symbol_string, "0")
                    }

                    if (item.partDetails != null && item.partDetails.size != 0 && !item.serviceDetail.mainCategoryId.isNullOrBlank() && item.serviceDetail.mainCategoryId.equals("12")) {
                        layout_maintenance.visibility = View.VISIBLE
                        partInfo.visibility = View.GONE
                        if (!item.partDetails[0].productName.isNullOrBlank()) {
                            tvServicesProductTitle.text = item.partDetails[0].productName
                        }

                        if (item.partDetails[0].couponList != null && item.partDetails[0].couponList.size != 0 && item.partDetails[0].couponList[0] != null) {
                            tvServicesProductAppliedCoupon.text = item.partDetails[0].couponList[0].couponTitle
                        } else tvServicesProductAppliedCoupon.visibility = View.GONE

                        if (!item.partDetails[0].sellerPrice.isNullOrBlank()) {
                            tvServicesProductPrices.text = context.getString(R.string.euro_symbol) + " " + item.partDetails[0].sellerPrice
                        } else tvServicesProductPrices.text = context.getString(R.string.euro_symbol) + " " + "0"
                        if (!item.partDetails[0].product_image_url.isNullOrBlank()) {
                            context.loadImage(item.partDetails[0].product_image_url, ivServicesProductImage)
                        }
                        if (!item.partDetails[0].brandImageURL.isNullOrBlank()) {
                            context.loadImage(item.partDetails[0].brandImageURL, ivServicesProductPricesBrandImage)

                        }
                        if (!item.partDetails[0].forPair.isNullOrBlank() && !item.partDetails[0].forPair.equals("0")) {
                            tvServicesProductPair.text = "2 " + context.getString(R.string.pieces)
                        } else tvServicesProductPair.visibility = View.GONE

                        if (!item.partDetails[0].rating_star.isNullOrBlank()) {
                            crServicesProductRating.rating = item.partDetails[0].rating_star.toFloat()
                        } else crServicesProductRating.rating = 0f

                        if (!item.partDetails[0].rating_count.isNullOrBlank()) {
                            tvServicesProductCount.text = item.partDetails[0].rating_count
                        } else tvServicesProductCount.text = "0"


                    } else {
                        layout_maintenance.visibility = View.GONE

                        if (item.partDetails != null && item.partDetails.size != 0 && !item.serviceDetail.mainCategoryId.isNullOrBlank() && item.serviceDetail.mainCategoryId.equals("3")) {
                            llMotSparePartLayout.visibility = View.VISIBLE
                            var motSparePartTotalPrices = 0.0
                            for (part in item.partDetails) {
                                if (!part.sellerPrice.isNullOrBlank()) {
                                    if (!part.forPair.isNullOrBlank() && !part.forPair.equals("0"))
                                        motSparePartTotalPrices += part.sellerPrice.toDouble() * 2
                                    else motSparePartTotalPrices += part.sellerPrice.toDouble()
                                }

                            }
                            var mKPartServicesList: ArrayList<Models.Part> = ArrayList()
                            var partListFromCart = ArrayList<Models.Part>()
                            val jsonString = Gson().toJson(item.partDetails);
                            val gson = GsonBuilder().create()
                            partListFromCart = gson.fromJson(jsonString.toString(), Array<Models.Part>::class.java).toCollection(java.util.ArrayList<Models.Part>())
                            mKPartServicesList.addAll(partListFromCart)
                            val   genericAdapter = GenericAdapter<Models.Part>(context, R.layout.item_sparepart_mot)
                            genericAdapter!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
                                override fun onClick(view: View, position: Int) {
                                }
                                override fun onItemClick(view: View, position: Int) {
                                }
                            })
                            holder.mot_parts_list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
                            holder.mot_parts_list.adapter = genericAdapter
                            genericAdapter!!.addItems(mKPartServicesList)

                            holder.partInfo.visibility = View.GONE
                            holder.tvPlus.visibility = View.GONE
                            holder.plus_icon2.visibility = View.VISIBLE

                            tvMotSpareParttotalPrices.text = context.getString(R.string.prepend_euro_symbol_string, motSparePartTotalPrices.roundTo2Places().toString())
                        } else
                            llMotSparePartLayout.visibility = View.GONE

                    }
                }
                if (item.workshopDetail != null) {
                    workshopName.text = item.workshopDetail.companyName.takeIf { !it.isNullOrEmpty() }
                    workshopAddress.text = item.workshopDetail.registeredOffice.takeIf { !it.isNullOrEmpty() }
                    context.loadImage(item.workshopDetail.workShopImageUrl.takeIf { !it.isNullOrEmpty() }, cartItemWorkshopImage)
                }
                date.text = if (!item.bookingDate.isNullOrBlank()) {
                    dateFormatChangeYearToMonth(item.bookingDate)
                } else {
                    (item.bookingDate).takeIf { !it.isNullOrEmpty() }
                }
                time.text = "${item.startTime.takeIf { !it.isNullOrEmpty() }?.removeSuffix(":00")}  -  ${if (!item.endTime.isNullOrBlank() && item.endTime != "null") item.endTime.removeSuffix(":00") else "--"}"
                item.endTime.takeIf { !it.isNullOrEmpty() && it != "null" }?.removeSuffix(":00")


                if (!item.avilability.isNullOrBlank() && item.avilability.equals("0")) {
                    isProduct_Available.visibility = View.VISIBLE
                    if (!item.maxProductQuantity.isNullOrBlank() && item.maxProductQuantity.toInt() >= 0) {
                        isServicesAvailable.text = context.getString(R.string.out_of_stock)
                        quantitySpinner.visibility = View.GONE
                        ProceedToPay?.isEnabled = false
                        ProceedToPay?.let { Snackbar.make(it, context.getString(R.string.PleaseDeleteOutOfStockProduct), Snackbar.LENGTH_SHORT).show() }
                    } else if (!item.maxProductQuantity.isNullOrBlank() && !item.productQuantity.isNullOrBlank() && item.maxProductQuantity < item.productQuantity) {
                        quantitySpinner.visibility = View.VISIBLE
                        quantity = item.maxProductQuantity.toInt()
                        if (item.serviceAssemblyProductDescription != null) {
                            updateCartQuantity(quantity, item, item.serviceAssemblyProductDescription.id)
                        }
                        isServicesAvailable.text = context.getString(R.string.re_maining_item, item.maxProductQuantity)
                    } else {
                        isServicesAvailable.text = context.getString(R.string.workshop_not_provide_services)
                        ProceedToPay?.isEnabled = false
                    }

                }
                if (item.serviceAssemblyProductDescription != null) {
                    if (item.serviceAssemblyProductDescription.pfuTax.isNullOrBlank() || item.serviceAssemblyProductDescription.pfuTax == "0") {
                        pfutextLabel.visibility = View.GONE
                        productPFU.visibility = View.GONE
                        productVat.visibility = View.GONE

                        productPFU.text = context.getString(R.string.prepend_euro_symbol_string, "0.0")
                    } else {
                        productPFU.text = context.getString(R.string.prepend_euro_symbol_string, (quantity * item.serviceAssemblyProductDescription.pfuTax.toFloat()).toString())
                    }
                }
            }


            productVat.setOnClickListener {
                if (item.CartType == "SP" && item.serviceAssemblyProductDescription != null && !item.serviceAssemblyProductDescription.pfuDesc.isNullOrBlank() && item.serviceAssemblyProductDescription.pfuDesc != "-") {
                    context.showInfoDialog(item.serviceAssemblyProductDescription.pfuDesc)
                } else if (!item.pfuDesc.isNullOrBlank() && item.pfuDesc != "-") {
                    context.showInfoDialog(item.pfuDesc)
                }

            }
            var ProductMaxQuantity = 1
            if (item.CartType == "SP") {
                if (item.serviceAssemblyProductDescription != null && item.serviceAssemblyProductDescription.maxProductQuantity != null) {
                    ProductMaxQuantity = if (item.serviceAssemblyProductDescription.maxProductQuantity.isNullOrBlank()) 1 else item.serviceAssemblyProductDescription.maxProductQuantity.toInt()
                }
            } else {
                ProductMaxQuantity = if (item.maxProductQuantity.isNullOrBlank()) 1 else item.maxProductQuantity.toInt()

            }

            val list = getIntegerStringList(ProductMaxQuantity, isProductSellOnpair)
            quantitySpinner.setSampleSpinnerAdapter(context, list)
            if (item.CartType == "SP") {

                quantitySpinner.isEnabled = false
                quantitySpinner.isClickable = false
            } else {
                quantitySpinner.isEnabled = true
                quantitySpinner.isClickable = true
            }

            if (list.indexOf(quantity.toString()) == -1) {
                if (isProductSellOnpair) {
                    quantity = 2
                } else {
                    quantity = 1
                }
            }

            quantitySpinner.setSelection(list.indexOf(quantity.toString()))

            quantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    if ((quantity).toString() != quantitySpinner.selectedItem) {
                        updateCartQuantity(position + 1, item, productID, isProductSellOnpair)
                    }


                }
            }


            deleteItem.setOnClickListener {
                try {

                    removeCartItem(item)
                } catch (e: Exception) {
                }
            }


        }
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deleteItem = itemView.cart_item_delete
        val productName = itemView.cart_item_product_name
        val productDescription = itemView.cart_item_product_description
        val productPrice = itemView.cart_item_product_price
        val workshopName = itemView.cart_item_workshop_name
        val time = itemView.cart_item_booking_selected_time
        val date = itemView.cart_item_workshop_booking_date
        val servicePrice = itemView.cart_item_assemble_service_price
        val quantitySpinner = itemView.cart_product_qty_spinner
        val serviceName = itemView.cart_service_name
        val workshopAddress = itemView.cart_item_workshop_registered_office
        val cartItemServiceLayout = itemView.cart_item_service_layout
        val cartItemProductLayout = itemView.cart_item_product_layout
        val cartItemServiceImage = itemView.cart_item_service_image
        val cartItemProductImage = itemView.cart_item_product_image
        val cartItemWorkshopImage = itemView.cart_item_workshop_image
        val isProduct_Available = itemView.tv_IsStockavailable_Product
        val isServicesAvailable = itemView.tv_IsStockavailable
        val partInfo = itemView.tv_partInfo
        val productVat = itemView.tv_product_Vat
        val productDiscount = itemView.tv_ProductDiscount
        val productPFU = itemView.tv_ProductPFU
        val ProductTotal = itemView.tv_ProductTotal
        val pfutextLabel = itemView.tv_pfutext
        val tv_labeldiscount = itemView.tv_labeldiscount
        val servicesVat = itemView.tv_servicesVat
        val ServicesTotalPrices = itemView.tv_services_TotalPrices
        val tv_discount = itemView.tv_discount
        val tv_couponDetailProduct = itemView.tv_coupon_cart
        val tv_coupon_cart_services = itemView.tv_coupon_cart_services
        val tvPlus = itemView.tv_plus
        val layout_maintenance = itemView.layout_maintenance
        val tvServicesProductPair = itemView.tv_services_product_pair
        val tvServicesProductTitle = itemView.tv_services_product_title
        private val tvServicesProductDescription = itemView.tv_services_product_description
        val tvServicesProductAppliedCoupon = itemView.tv_services_product_appliedCouponName
        val tvServicesProductPrices = itemView.tv_services_product_prices

        val ivServicesProductImage = itemView.iv_services_product_image
        val ivServicesProductPricesBrandImage = itemView.iv_services_product_brand_image
        val tvCartServiceDescription = itemView.cart_service_description
        val crServicesProductRating = itemView.cr_services_product_rating
        val tvServicesProductCount = itemView.tv_services_product_rating_count


        val llMotSparePartLayout = itemView.ll_motSparePart
        val tvMotSpareParttotalPrices = itemView.tv_motPartPrices
        val mot_parts_list = itemView.mot_parts_list
        val plus_icon2 = itemView.plus_icon2

    }


    @JvmOverloads
    fun updateCartQuantity(selectedQuantityofProduct: Int, item: Models.CartDataList, productID: String, isProductSellOnpair: Boolean = false) {
        var price: String = ""
        var selectedQuantity = selectedQuantityofProduct
        if (isProductSellOnpair)
            selectedQuantity = selectedQuantityofProduct * 2
        if (item.CartType == "T" || item.CartType == "S") {
            if (!item.price.isNullOrEmpty() && !item.pfuTax.isNullOrEmpty()) {
                totalPrice = item.price.toDouble() - item.pfuTax.toDouble()
                totalPrice = (selectedQuantity * totalPrice) + item.pfuTax.toDouble()
            }
            price = item.price
        } else if (item.CartType == "SP") {
            if (!item.serviceAssemblyProductDescription.price.isNullOrEmpty() && !item.serviceAssemblyProductDescription.pfuTax.isNullOrEmpty()) {
                totalPrice = item.serviceAssemblyProductDescription.price.toDouble() - item.serviceAssemblyProductDescription.pfuTax.toDouble()
                totalPrice = (selectedQuantity * totalPrice) + item.serviceAssemblyProductDescription.pfuTax.toDouble()
            } else if (!item.serviceAssemblyProductDescription.price.isNullOrEmpty() && item.serviceAssemblyProductDescription.pfuTax.isNullOrEmpty()) {
                totalPrice = item.serviceAssemblyProductDescription.price.toDouble()
                totalPrice *= selectedQuantity
            }
            price = item.serviceAssemblyProductDescription.price
        }

        RetrofitClient.client.updateCartProductQuantity(context.getBearerToken(), selectedQuantity.toString(), productID,
                price, totalPrice.toString())
                .onCall { networkException, response ->
                    response?.let {

                        response.body()?.let {

                            if (response.isSuccessful) {
                                if (onCartListCallback == null) {
                                } else {
                                    onCartListCallback?.callCartApi()
                                }

                            }
                        }

                    }


                }

    }


    private fun deleteCartItem(item: Models.CartDataList, type: String, CartTotalPrice: String) {


        RetrofitClient.client.deleteCartItem(context.getBearerToken(), item.id, CartTotalPrice, type).onCall { networkException, response ->
            response?.let {


                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    if (isStatusCodeValid(body)) {
                        ProceedToPay?.isEnabled = true
                        val data = JSONObject(body)
                        if (onCartListCallback == null) {

                        } else {
                            onCartListCallback?.callCartApi()
                        }
                        context.showInfoDialog(data.get("message").toString())

                    } else {
                        context.showInfoDialog(context.getString(R.string.Something_went_wrong_Please_try_again))
                    }
                }
            }

        }

    }



    private fun partsDialog(parts: ArrayList<Models.Part>, context: Context) {

        dialog = Dialog(context)
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_view_for_dialog, null, false)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(view)
        val window: Window = dialog!!.window!!
        window.setDimAmount(0f)
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        dialog!!.setContentView(view)
        val title = dialog!!.findViewById(R.id.title) as TextView
        title.text = context.getString(R.string.parts)
        title.setTextColor(Color.parseColor("#FFFFFF"))

        genericAdapterParts = com.officinetop.adapter.GenericAdapter<Models.Part>(context, com.officinetop.R.layout.maintenance_part_dialog)
        genericAdapterParts!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {

            }

            override fun onItemClick(view: View, position: Int) {


            }
        })

        view.imageCross.setOnClickListener {
            dialog!!.dismiss()
        }


        view.dialog_recycler_view.adapter = genericAdapterParts
        genericAdapterParts!!.addItems(parts)
        dialog!!.show()
    }


}