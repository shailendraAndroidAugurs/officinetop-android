package com.officinetop.rim

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.officinetop.R
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.activity_rim_product_details.*
import kotlinx.android.synthetic.main.activity_rim_product_details.add_product_to_cart
import kotlinx.android.synthetic.main.activity_rim_product_details.buy_product_with_assembly
import kotlinx.android.synthetic.main.activity_rim_product_details.productTotalPrices
import okhttp3.ResponseBody
import org.jetbrains.anko.intentFor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RimProductDetailsActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    private var productDetails: Models.rimProductDetails? = null
    var front_id = ""
    var rear_id = ""
    var spinner_data  = ArrayList<Int>()
    var front_price = 0.00
    var rear_price = 0.00


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim_product_details)
        progressDialog = getProgressDialog()
        if(intent.hasExtra("front_id")){
            front_id = intent.getStringExtra("front_id")
        }
        if(intent.hasExtra("rear_id")){
            rear_id = intent.getStringExtra("rear_id")
        }
        bindSpinnerValue()
        loadRimProductDetails()
        add_product_to_cart.setOnClickListener {
            if (!isLoggedIn()) {
                showConfirmDialogForLogin(getString(R.string.PleaselogintocontinueforAddtocart), { moveToLoginPage(this) })
            }
        }

        buy_product_with_assembly.setOnClickListener {
            if (productDetails != null) {
                val myIntent = intentFor<WorkshopListActivity>(
                        Constant.Key.is_workshop to true,
                        Constant.Key.is_rim_workshop_service to true,
                        Constant.Key.productDetail to productDetails?.toString()
                )
                startActivity(myIntent)
            }
        }
    }

    private fun bindSpinnerValue() {
        spinner_data.add(1)
        spinner_data.add(2)
        spinner_data.add(3)
        spinner_data.add(4)
        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, spinner_data)
        spinner_front.adapter = arrayAdapter
        spinner_rear.adapter = arrayAdapter
        spinner_front.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                tv_rim_front_price.text   = getString(R.string.prepend_euro_symbol_string, ""+(front_price*selectedItem.toInt()))
                productTotalPrices.text =getString(R.string.total)+" "+getString(R.string.prepend_euro_symbol_string, ""+(front_price*selectedItem.toInt())+(rear_price*selectedItem.toInt()))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        spinner_rear.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext,""+selectedItem,Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    fun loadRimProductDetails(){
        progressDialog.show()

        RetrofitClient.client.rimdetails(front_id,rear_id,getLat(),getLong(), getSelectedCar()?.carVersionModel?.idVehicle!!,getUserId()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()

                progressDialog.dismiss()

                if (isStatusCodeValid(body)) {
                    val dataset = getDataFromResponse(body)
                    Log.d("pdetails_data", "onResponse: models = "+body)
                    dataset.let { it1 ->
                        productDetails = Gson().fromJson<Models.rimProductDetails>(it1.toString(), Models.rimProductDetails::class.java)

                        if (productDetails != null) {
                            loadImage(productDetails!!.ImageUrl, prodcut_image)
                            tv_type.text = productDetails!!.Manufacturer
                            tv_type_name.text = productDetails!!.TypeName
                            tv_type_color.text = productDetails!!.TypeColor
                            tv_rim_anteriore.text = resources.getString(R.string.front)+" "+if(!productDetails!!.rim_anteriore.isNullOrEmpty()) productDetails!!.rim_anteriore else ""
                            tv_rim_posteriore.text = resources.getString(R.string.rear)+" "+if(!productDetails!!.rim_posteriore.isNullOrEmpty())productDetails!!.rim_posteriore else ""
                            productTotalPrices.text =getString(R.string.total)+" "+getString(R.string.prepend_euro_symbol_string, productDetails!!.AlloyRimPrice)
                            tv_rim_front_price.text   = getString(R.string.prepend_euro_symbol_string, if(!productDetails!!.anteriore_price.isNullOrEmpty())productDetails!!.anteriore_price else "0")
                            tv_rim_rear_price.text   = getString(R.string.prepend_euro_symbol_string, if(!productDetails!!.posteriore_price.isNullOrEmpty())productDetails!!.posteriore_price else "0")
                            front_price = if(!productDetails!!.anteriore_price.isNullOrEmpty())productDetails!!.anteriore_price.toDouble() else 0.00
                            rear_price = if(!productDetails!!.posteriore_price.isNullOrEmpty())productDetails!!.posteriore_price.toDouble() else 0.00
                            buy_product_with_assembly.text =  getString(R.string.buy_with_assembly)+"("+getString(R.string.prepend_euro_symbol_string, productDetails!!.min_service_price)+")"
                            //binding p
                            // roduct description data
                            tv_width.text = "Ant. "+if (!productDetails!!.front_width.isNullOrEmpty())productDetails!!.front_width else "" +" - Post."+if(!productDetails!!.rear_width.isNullOrEmpty())productDetails!!.rear_width else ""
                            tv_diameter.text = productDetails!!.front_diameter
                            tv_etoffset.text = productDetails!!.AlloyRimET
                            tv_no_of_holes.text = productDetails!!.AlloyRimLochzahl
                            tv_distance_between_holes.text = productDetails!!.AlloyRimLochkreis
                            tv_winter_compatibilty.text = productDetails!!.winter
                            tv_color.text = productDetails!!.TypeColor

                        }
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("pdetails_data",""+t.message)
            }
        })
    }

}