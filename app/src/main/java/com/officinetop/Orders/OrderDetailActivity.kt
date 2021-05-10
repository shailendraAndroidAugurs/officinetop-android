package com.officinetop.Orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.GenericAdapter
import com.officinetop.data.Models
import com.officinetop.feedback.FeedbackAddActivity
import com.officinetop.utils.Constant
import com.officinetop.utils.moveToMotDetailPageFromCart
import kotlinx.android.synthetic.main.alertdialog_feedback.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import org.jetbrains.anko.intentFor

class OrderDetailActivity : BaseActivity() {
    private lateinit var forWhich: String
    lateinit var orderid: String
    lateinit var Orderstatus: String
    var SpareProductList: ArrayList<Models.SpareProductDescription> = ArrayList<Models.SpareProductDescription>()
    var TyreProductList: ArrayList<Models.TyreProductDescription> = ArrayList<Models.TyreProductDescription>()
    var ServiceProductList: ArrayList<Models.ServiceProductDescription> = ArrayList<Models.ServiceProductDescription>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.orderDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        forWhich = intent.extras!!.get("forwhich") as String
        orderid = intent.extras!!.get("orderid") as String

        if (intent.getSerializableExtra("OrderDetailList") != null) {
            if (forWhich == "S") {
                SpareProductList = intent.getSerializableExtra("OrderDetailList") as ArrayList<Models.SpareProductDescription>
                bindViewForSpareProduct()

            } else if (forWhich == "T") {
                TyreProductList = intent.getSerializableExtra("OrderDetailList") as ArrayList<Models.TyreProductDescription>
                bindViewForTyre()
            } else if (forWhich == "SP") {
                ServiceProductList = intent.getSerializableExtra("OrderDetailList") as ArrayList<Models.ServiceProductDescription>
                bindViewForServiceProduct()
            }
        }
    }

    private fun bindViewForTyre() {
        val tyreProductAdapter = GenericAdapter<Models.TyreProductDescription>(this, R.layout.item_cart_product)

        tyreProductAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                // Log.v("WORKSHOPID",TyreProductList[position].workshopDetails.id.toString())
                startActivityForResult(intentFor<FeedbackAddActivity>(
                        Constant.Path.workshopId to "",
                        Constant.Path.productId to TyreProductList[position].productsId,
                        Constant.Path.productType to "2",
                        Constant.Path.orderid to orderid,
                        Constant.Path.sellerId to if (!TyreProductList[position].sellerId.isNullOrBlank()) TyreProductList[position].sellerId
                        else if (!TyreProductList[position].usersId.isNullOrBlank()) TyreProductList[position].usersId
                        else "",
                        Constant.Path.ProductOrWorkshopName to TyreProductList[position].productName
                        , Constant.Path.type to "1", Constant.Path.mainCategoryId to "", Constant.Path.serviceID to "", Constant.Path.motservicetype to ""

                ), 102)
                finish()
            }

            override fun onItemClick(view: View, position: Int) {

                showrating(TyreProductList[position].feedbackdetail.comments, TyreProductList[position].feedbackdetail.rating)
            }
        })
        tyreProductAdapter.addItems(TyreProductList)
        recycler_view.adapter = tyreProductAdapter


    }

    private fun bindViewForSpareProduct() {
        try{
            val spareProductAdapter = GenericAdapter<Models.SpareProductDescription>(this, R.layout.item_spare_product)

            spareProductAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
                override fun onClick(view: View, position: Int) {


                    startActivityForResult(intentFor<FeedbackAddActivity>(
                            Constant.Path.workshopId to "",
                            Constant.Path.productId to SpareProductList[position].productsId,
                            Constant.Path.productType to "1",
                            Constant.Path.orderid to orderid,
                            Constant.Path.sellerId to if (!SpareProductList[position].sellerId.isNullOrBlank()) SpareProductList[position].sellerId
                            else if (!SpareProductList[position].usersId.isNullOrBlank()) SpareProductList[position].usersId
                            else "",
                            Constant.Path.ProductOrWorkshopName to SpareProductList[position].productName,
                            Constant.Path.type to "1", Constant.Path.mainCategoryId to "", Constant.Path.serviceID to "", Constant.Path.motservicetype to ""), 102)
                    finish()


                }

                override fun onItemClick(view: View, position: Int) {
                    showrating(SpareProductList[position].feedbackdetail.comments, SpareProductList[position].feedbackdetail.rating)
                }
            })
            spareProductAdapter.addItems(SpareProductList)
            recycler_view.adapter = spareProductAdapter
        }
        catch (e: Exception){
            Log.d("check_error",""+e.message);
        }



    }

    private fun bindViewForServiceProduct() {
        val serviceProductAdapter = GenericAdapter<Models.ServiceProductDescription>(this, R.layout.item_cart_assembled)

        serviceProductAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                if (view.tag != null && view.tag == "100" && ServiceProductList[position].serviceAssemblyProductDescription != null && ServiceProductList[position].serviceDetail != null) {

                    startActivityForResult(intentFor<FeedbackAddActivity>(
                            Constant.Path.workshopId to ServiceProductList[position].workshopDetails.id.toString(),
                            Constant.Path.productId to if (ServiceProductList[position].serviceAssemblyProductDescription != null) ServiceProductList[position].serviceAssemblyProductDescription.productsId else "",
                            Constant.Path.productType to if (ServiceProductList[position].serviceAssemblyProductDescription != null) ServiceProductList[position].serviceAssemblyProductDescription.forOrderType else "",
                            Constant.Path.sellerId to if (!ServiceProductList[position].serviceAssemblyProductDescription.sellerId.isNullOrBlank()) ServiceProductList[position].serviceAssemblyProductDescription.sellerId
                            else if (!ServiceProductList[position].serviceAssemblyProductDescription.usersId.isNullOrBlank()) ServiceProductList[position].serviceAssemblyProductDescription.usersId
                            else "",

                            Constant.Path.orderid to orderid,
                            Constant.Path.ProductOrWorkshopName to ServiceProductList[position].serviceAssemblyProductDescription.productName,
                            // Constant.Path.type to "2",Constant.Path.mainCategoryId to ServiceProductList[position].serviceDetail.mainCategoryId,Constant.Path.serviceID to ServiceProductList[position].serviceDetail.serviceId), 102)
                            Constant.Path.type to "3", Constant.Path.mainCategoryId to ServiceProductList[position].serviceDetail.mainCategoryId, Constant.Path.serviceID to ServiceProductList[position].serviceDetail.serviceId, Constant.Path.motservicetype to ServiceProductList[position].motServiceType), 102)

                } else if (view.tag != null && view.tag.equals("102") && ServiceProductList[position].partDetails != null){
                    moveToMotDetailPageFromCart(ServiceProductList[position].partDetails as ArrayList<Models.Part>, ServiceProductList[position].serviceDetail,context = this@OrderDetailActivity)
                }
                else {

                    startActivityForResult(intentFor<FeedbackAddActivity>(
                            Constant.Path.workshopId to ServiceProductList[position].workshopDetails.id.toString(),
                            Constant.Path.productId to if (ServiceProductList[position].serviceAssemblyProductDescription != null) ServiceProductList[position].serviceAssemblyProductDescription.productsId else "",
                            Constant.Path.productType to ""/*if(ServiceProductList[position].serviceAssemblyProductDescription!=null)ServiceProductList[position].serviceAssemblyProductDescription.forOrderType else ""*/,
                            Constant.Path.sellerId to "",
                            Constant.Path.orderid to orderid,
                            Constant.Path.ProductOrWorkshopName to ServiceProductList[position].workshopDetails.companyName,
                            Constant.Path.type to "2", Constant.Path.mainCategoryId to ServiceProductList[position].serviceDetail.mainCategoryId, Constant.Path.serviceID to ServiceProductList[position].serviceDetail.serviceId), 102)


                }

                finish()

            }

            override fun onItemClick(view: View, position: Int) {
                if (view.tag != null && view.tag == "101") {
                    if (ServiceProductList[position].serviceAssemblyProductDescription != null && ServiceProductList[position].serviceAssemblyProductDescription.feedbackdetail != null) {
                        showrating(ServiceProductList[position].serviceAssemblyProductDescription.feedbackdetail.comments, ServiceProductList[position].serviceAssemblyProductDescription.feedbackdetail.rating)
                    }


                } else {
                    showrating(ServiceProductList[position].serviceDetail.feedbackdetail.comments, ServiceProductList[position].serviceDetail.feedbackdetail.rating)
                }


                /*  startActivity(intentFor<FeedbackListActivity>(Constant.Path.workshopId to workshopUsersId.toString()
                          , Constant.Path.ProductOrWorkshopName to company_name.takeIf { !it.isNullOrEmpty() }))*/
            }
        })
        serviceProductAdapter.addItems(ServiceProductList)
        recycler_view.adapter = serviceProductAdapter

        /* class Holder(view: View) : RecyclerView.ViewHolder(view) {

         }

         val myadpter = object : RecyclerView.Adapter<Holder>() {
             var viewBinderHelper = ViewBinderHelper()
             override fun getItemCount(): Int {
                 return ServiceProductList.size
             }

             override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                 val layoutInflater = LayoutInflater.from(parent.context)
                 viewBinderHelper.setOpenOnlyOne(true);
                 return Holder(layoutInflater.inflate(R.layout.item_cart_assembled, parent, false))
             }

             override fun onBindViewHolder(holder: Holder, position: Int) {
                 if (ServiceProductList[position].product_info != null) {
                     loadImage(ServiceProductList[position].product_info.product_imageUrl, holder.itemView.cart_assembled_product_image)
                     holder.itemView.item_assemble_title.text = ServiceProductList[position].product_info.productsName
                     holder.itedmView.item_assemble_product_subtitle.text = ServiceProductList[position].product_info.kromedaDescription
                     holder.itembView.item_product_price.text = ServiceProductList[position].product_info.price
                 }else{c
                     holder.itemView.ll_productInfo.visibility=View.GONE
                 }c
                 if (ServiceProductList[position].Services != null) {
c
                     loadImage(ServiceProductList[position].Services.ServiceImagesUrl, holder.itemView.cart_assembled_service_image)
 cg                    holder.itemView.tv_service_name.text = ServiceProductList[position].Services.ServiceName
f
   mot              }
g
 f                if (ServiceProductList[position].workshop_details != null) {
  gdfgsfd                   loadImage(ServiceProductList[position].workshop_details.workShopImageUrl, holder.itemView.cart_assembled_workshop_image)
                     holder.itemView.item_assemble_workshop_subtitle.text = ServiceProductList[position].workshop_details.businessName

                     holder.itemView.item_assemble_service_price.text = ServiceProductList[position].price
                             holder.itemView.item_assemble_date.te
                             xt = ServiceProductList[position].bookingDate
                             holder.itemView.item_assemble_time.text = ServiceProductList[position].startTime
                             holder.itemView.tv_workshopEndtime.text = ServiceProductList[position].endTime
                             holder.itemView.selected_car_name.text = getSelectedCar()?.carMakeName
                 }


             }
         }


         recycler_view.adapter = myadpter
 */

    }


    private fun showrating(comments: String, ratings: String?) {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.alertdialog_feedback, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Feedback")
        //show dialog
        val s = comments
        val ss = ratings
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.textview_comment.text = comments
        mDialogView.rating.rating = ratings?.toFloat()!!
        //cancel button click of custom layout
        mDialogView.button_close.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }
    }


}
