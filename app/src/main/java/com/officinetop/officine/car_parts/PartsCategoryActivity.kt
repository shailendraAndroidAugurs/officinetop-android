package com.officinetop.officine.car_parts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.getDataSetArrayFromResponse
import com.officinetop.officine.data.getMessageFromJSON
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.data.isStatusCodeValid
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.loadImageWithName
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import kotlinx.serialization.ImplicitReflectionSerializer
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartsCategoryActivity : BaseActivity() {


    private var selectedVehicleVersionID: String = ""

    @ImplicitReflectionSerializer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)

        toolbar_title.text = getString(R.string.car_parts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedVehicleVersionID = getSelectedCar()?.carVersionModel?.idVehicle ?: ""
        Log.d("PartsCategoryActivity", "onCreate: $selectedVehicleVersionID")

        progress_bar.visibility = View.VISIBLE


        when {
            intent.getBooleanExtra(Constant.Key.is_sub_group, false) -> {
                loadGroups(RetrofitClient.client.sparePartsSubGroup(intent.getIntExtra(Constant.Key.id, 0)))
                toolbar_title.text = intent.getStringExtra(Constant.Key.partCategory)
            }
            intent.getBooleanExtra(Constant.Key.is_sub_n3_group, false) -> {
                loadGroups(RetrofitClient.client.spareN3Groups(intent.getIntExtra(Constant.Key.id, 0)))
                toolbar_title.text = intent.getStringExtra(Constant.Key.partCategory)
            }
            else -> loadGroups(RetrofitClient.client.sparePartsGroup(selectedVehicleVersionID))
        }

    }


    private fun loadGroups(call: Call<ResponseBody>) {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress_bar.visibility = View.GONE
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                progress_bar.visibility = View.GONE
                body?.let {


                    if (isStatusCodeValid(body)) {
                        val dataset = getDataSetArrayFromResponse(it)
                        bindRecyclerView(dataset)
                    } else {
                        showInfoDialog(getMessageFromJSON(it)) {
                            finish()
                        }
                    }
                }

            }
        })

    }


    private fun bindRecyclerView(jsonArray: JSONArray) {

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val title = view.item_title
            val icon = view.item_image
            val priceView = view.item_sub_title
        }

        recycler_view.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
                return ViewHolder(layoutInflater.inflate(R.layout.item_category, p0, false))
            }

            override fun getItemCount(): Int = jsonArray.length()

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(p0: ViewHolder, position: Int) {

                val jsonDetails = jsonArray.getJSONObject(position)


                p0.title.text = jsonDetails.optString("group_name", jsonDetails.optString("item"))
                p0.priceView.visibility = View.GONE

                if (intent.hasExtra(Constant.Key.is_sub_n3_group)) {
                    //n3 group
                    p0.title.text = p0.title.text.toString() + " ${jsonDetails.optString("front_rear")} ${jsonDetails.optString("left_right")}"
                }


                if (jsonDetails.has("images") && !jsonDetails.isNull("images") && jsonDetails.getJSONArray("images").length() > 0)
                    loadImageWithName(jsonDetails.getJSONArray("images").getJSONObject(0).getString("image_name"),
                            p0.icon, R.drawable.no_image_placeholder,
                            jsonDetails.getJSONArray("images").getJSONObject(0).getString("image_url")
                    )

                p0.itemView.setOnClickListener {
                    val id = jsonDetails.getInt("id")

                    when {
                        intent.hasExtra(Constant.Key.is_sub_group) ->
                            startActivity(intentFor<PartsCategoryActivity>(Constant.Key.id to id,
                                    Constant.Key.is_sub_n3_group to true,
                                    Constant.Key.partCategory to jsonDetails.optString("group_name")))

                        intent.hasExtra(Constant.Key.is_sub_n3_group) ->
                            startActivity(intentFor<ProductListActivity>(
                                    Constant.Key.partItemID to id)
                            )
                        else ->
                            startActivity(intentFor<PartsCategoryActivity>(Constant.Key.id to id,
                                    Constant.Key.is_sub_group to true,
                                    Constant.Key.partCategory to jsonDetails.optString("group_name")))
                    }

                }

            }

        }

    }


}
