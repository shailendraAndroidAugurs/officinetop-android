package com.officinetop.Washing

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.utils.Constant.defaultDistance
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class CarServiceListActivity : BaseActivity() {
    var arrayList: MutableList<Models.ServiceCategory> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = resources.getString(R.string.washing_type)
        getLocation()
        loadData()
    }

    private fun loadData() {
        if (isOnline()) {
            val dialog = getProgressDialog(true)

            RetrofitClient.client.getServiceCategory(1, getSelectedCar()?.carSize, getLat(), getLong(), defaultDistance)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            dialog.dismiss()
                            recycler_view.snackbar(getString(R.string.Failed_to_load_data))
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                            dialog.dismiss()
                            val body = response.body()?.string()
                            Log.d("CarServiceListActivity", "onResponse: body = $body")

                            if (response.isSuccessful) {
                                body?.let {
                                    val dataset = getDataSetArrayFromResponse(it)

                                    for (i in 0 until dataset.length()) {
                                        val serviceCategory = Gson().fromJson<Models.ServiceCategory>(dataset[i].toString()
                                                , Models.ServiceCategory::class.java)
                                        arrayList.add(serviceCategory)

                                    }
                                    bindRecycler(serviceList = arrayList)
                                }

                            }
                        }
                    })
        } else {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
           /* showConnectionFailedDialog {
                loadData()
            }*/
        }
    }

    private fun bindRecycler(serviceList: MutableList<Models.ServiceCategory>) {

        recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.item_category, p0, false)) {}
            }

            override fun getItemCount(): Int = serviceList.size

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

                with(p0.itemView) {
                    if (serviceList[p1].category_name != null)
                        item_title.text = serviceList[p1].category_name
                    else
                        item_title.text = context.getString(R.string.Services_Name)

                    if (serviceList[p1].services_price != null)
                        item_sub_title.text = getString(R.string.prepend_euro_symbol_with_from_string, serviceList[p1].services_price)
                    else
                        item_sub_title.text = getString(R.string.prepend_euro_symbol_with_from_string, "0")


                    if (!serviceList[p1].cat_image_url.isNullOrEmpty())
                        loadImage(serviceList[p1].cat_image_url, item_image, R.drawable.no_image_placeholder)

                    setOnClickListener {
                        startActivity(intentFor<ServiceDetailActivity>(Constant.Key.is_workshop to true, Constant.Key.is_car_wash to true,
                                Constant.Path.washServiceDetails to serviceList[p1],
                                "servicesList" to arrayList
                        )


                        )

                    }
                }

            }

        }

    }
}