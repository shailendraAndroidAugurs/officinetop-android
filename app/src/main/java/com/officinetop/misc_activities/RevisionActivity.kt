package com.officinetop.misc_activities

import android.os.Bundle
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.RevisionServiceAdapter
import com.officinetop.data.RevDataSetItem
import com.officinetop.data.getLat
import com.officinetop.data.getLong
import com.officinetop.data.getSelectedCar
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.Constant.defaultDistance
import com.officinetop.utils.genericAPICall
import com.officinetop.utils.getProgressDialog
import com.officinetop.utils.isOnline
import com.officinetop.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_revision.*
import kotlinx.android.synthetic.main.include_toolbar.*

class RevisionActivity : BaseActivity() {
    private lateinit var revisionServiceAdapter: RevisionServiceAdapter
    private var revisionServiceList: MutableList<RevDataSetItem?>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revision)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.revision)

        initView()
    }

    private fun initView() {
        getLocation()
        revisionServiceAdapter = RevisionServiceAdapter(this, revisionServiceList)
        revision_service_list.adapter = revisionServiceAdapter
        if (isOnline()) {
            getRevisionServices()
        }else{
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }
        
    }

    private fun getRevisionServices() {
        val dialog = getProgressDialog(true)
        RetrofitClient.client.getRevisionServices("2", getSelectedCar()?.carVersionModel?.idVehicle!!, getLat(), getLong(), defaultDistance).genericAPICall { _, response ->
            response?.let {
                dialog.dismiss()
                val revisionServices = it.body()

                revisionServiceList?.let {
                    revisionServiceAdapter = RevisionServiceAdapter(this, revisionServices?.revDataSet) //revisionServiceList)
                    revision_service_list.adapter = revisionServiceAdapter
                }
            }
        }
    }
}
