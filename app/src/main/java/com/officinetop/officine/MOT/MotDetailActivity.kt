package com.officinetop.officine.MOT


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.google.gson.Gson
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.WorkshopListActivity
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.data.saveServicesType
import com.officinetop.officine.misc_activities.PartList_Replacement
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.onCall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_mot_detail.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class MotDetailActivity : BaseActivity() {
    private var mKPartServicesList: ArrayList<Models.Part> = ArrayList()
    private var motdeatilsList: ArrayList<Models.Data> = ArrayList()
    private var mOPerationServicesList: ArrayList<Models.Operation> = ArrayList()
    lateinit var motServiceObject: Models.MotServicesList
    lateinit var itemsData: Models.MotDetail


    var selectitem_position: Int = 0
    var genericAdapter: GenericAdapter<Models.Part>? = null
    var hashMap: HashMap<String, Models.MotservicesCouponData> = HashMap<String, Models.MotservicesCouponData>()
    lateinit var motdata: Models.MotservicesCouponData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mot_detail)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.MOTDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (intent.hasExtra("motObject")) {
            motServiceObject = Gson().fromJson<Models.MotServicesList>(intent.extras!!.getString("motObject"), Models.MotServicesList::class.java)
            tv_title.text = motServiceObject.serviceName
            tv_description.text = motServiceObject.intervalDescriptionForKms
            motDetailService(motServiceObject.id.toString(), motServiceObject.type)
            saveServicesType(motServiceObject.type.toString())
        }


        // Log.e("APPTYPE",appController.servicetype)
        button_proceed.setOnClickListener {
            var partId: ArrayList<String> = ArrayList()
            var couponId: ArrayList<String> = ArrayList()
            var sellerId: ArrayList<String> = ArrayList()

            if (mKPartServicesList.size != 0) {

                for (i in 0 until mKPartServicesList.size) {
                    val product_Id = mKPartServicesList[i].id
                    if (mKPartServicesList[i].couponList != null && mKPartServicesList[i].couponList.size != 0) {
                        couponId.add(mKPartServicesList[i].couponList[0].id)
                    } else {
                        couponId.add("")
                    }
                    partId.add(product_Id)
                    sellerId.add(mKPartServicesList[i].usersId)
                }


                hashMap.put(motServiceObject.id.toString(), Models.MotservicesCouponData(couponId, partId, sellerId))


                val bundle = Bundle()
                bundle.putSerializable(Constant.Path.Motpartdata, hashMap as Serializable)
                Log.e("replacePartID", partId.toString())
                startActivity(intentFor<WorkshopListActivity>(
                        Constant.Key.is_motService to true,
                        Constant.Path.mot_id to motServiceObject.id.toString(),
                        "mot_type" to motServiceObject.type.toString(),
                        Constant.Path.motservices_time to itemsData.data.serviceaveragetime).putExtras(bundle))

            } else {
                showInfoDialog(getString(R.string.partNotFound))
            }
        }
    }

    private fun motDetailService(mot_id: String, type: Int) {
        val selectedCar = getSelectedCar() ?: Models.MyCarDataSet()
        val selectedVehicleVersionID = selectedCar.carVersionModel.idVehicle
        progress_bar.visibility = View.VISIBLE
        RetrofitClient.client.getmotserviceDetail(mot_id, type.toString(), selectedVehicleVersionID)
                .onCall { networkException, response ->
                    response?.let {
                        progress_bar.visibility = View.GONE
                        if (response.isSuccessful) {
                            val body = JSONObject(response.body()?.string())
                            if (body.has("data") && !body.isNull("data")) {

                                itemsData = Gson().fromJson<Models.MotDetail>(body.toString(), Models.MotDetail::class.java)

                                if (itemsData?.data?.serviceaveragetime == null) {
                                    itemsData.data?.serviceaveragetime = "0"
                                }

                                if (itemsData?.data?.operations != null) {
                                    for (i in 0 until itemsData?.data?.operations.size) {
                                        mOPerationServicesList.add(itemsData?.data?.operations[i])
                                    }
                                }
                                if (itemsData?.data?.kPartList != null) {
                                    for (i in 0 until itemsData?.data?.kPartList.size) {
                                        mKPartServicesList.add(itemsData?.data?.kPartList[i])
                                    }
                                }
                                /* bindMotOPerationServices()
                                   bindMotPartNumberServices()*/
                                binndDataInRecyclerview()

                            } else {
                                showInfoDialog(getString(R.string.DatanotFound))
                            }
                        } else {
                            showInfoDialog(response.message())
                        }

                    }
                }
    }

    private fun binndDataInRecyclerview() {
        button_proceed.visibility = View.VISIBLE
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        if (mOPerationServicesList.size != 0 && mKPartServicesList.size == 0) {
            var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    width, height / 2)
            ll_intervalOperation.layoutParams = params
            ll_sparePart.visibility = View.GONE
            ll_intervalOperation.visibility = View.VISIBLE
            bindMotOPerationServices()
        } else if (mKPartServicesList.size != 0 && mOPerationServicesList.size == 0) {
            var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    width, height / 2)
            ll_sparePart.layoutParams = params
            ll_intervalOperation.visibility = View.GONE
            ll_sparePart.visibility = View.VISIBLE
            bindMotPartNumberServices()
        } else if (mKPartServicesList.size != 0 && mOPerationServicesList.size != 0) {
            ll_sparePart.visibility = View.VISIBLE
            ll_intervalOperation.visibility = View.VISIBLE
            bindMotOPerationServices()
            bindMotPartNumberServices()
        } else {
            ll_sparePart.visibility = View.GONE
            ll_intervalOperation.visibility = View.GONE
        }
    }

    private fun bindMotOPerationServices() {
        val genericAdapter = GenericAdapter<Models.Operation>(this@MotDetailActivity, R.layout.item_mot_operation_detail)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                /* val intent = Intent(this@MotDetailActivity, MotDetailActivity::class.java)
                   intent.putExtra("motObject", Gson().toJson(mServicesList[position]))
                   startActivity(intent)*/
            }

            override fun onItemClick(view: View, position: Int) {

            }
        })
        recycler_view_IntervalOperation.adapter = genericAdapter
        genericAdapter.addItems(mOPerationServicesList)
    }

    private fun bindMotPartNumberServices() {
        genericAdapter = GenericAdapter<Models.Part>(this@MotDetailActivity, R.layout.item_sparepart_mot)
        genericAdapter!!.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(this@MotDetailActivity, PartList_Replacement::class.java)
                intent.putExtra("n3_services_Id", mKPartServicesList[position].n3_service_id)
                intent.putExtra("version_id", mKPartServicesList[position].version_id)
                intent.putExtra("Mot_type", mKPartServicesList[position].mot_type)
                startActivityForResult(intent, 100)
                selectitem_position = position
                Log.e("SelectID", mKPartServicesList[position].productsName)
            }

            override fun onItemClick(view: View, position: Int) {

            }
        })
        /*if (mKPartServicesList.size <= 2) {
            val params = recycler_view.layoutParams
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT
            recycler_view.layoutParams = params
        } else {
            val params: ViewGroup.LayoutParams = recycler_view.getLayoutParams()
            params.height = 700
            recycler_view.setLayoutParams(params)
        }*/
        recycler_view

                .adapter = genericAdapter
        genericAdapter!!.addItems(mKPartServicesList)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val partdata = data.getStringExtra("data")
            // val partID=data.getStringExtra("ID")
            val partmod = Gson().fromJson<Models.Part>(partdata, Models.Part::class.java)
            if (partmod.partimage != null) {
                partmod.product_image_url = partmod.partimage.takeIf { !it.isNullOrEmpty() }!!
            }
            if (partmod.brandImage != null) {
                partmod.brandImageURL = partmod.brandImage.takeIf { !it.isNullOrEmpty() }!!
            }
            mKPartServicesList[selectitem_position] = partmod
            Log.e("replaceSelectID", partmod.toString())
            bindMotPartNumberServices()
            recycler_view.getLayoutManager()!!.scrollToPosition(selectitem_position)
        }
    }

}
