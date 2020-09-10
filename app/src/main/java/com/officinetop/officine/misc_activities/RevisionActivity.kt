package com.officinetop.officine.misc_activities

import android.os.Bundle
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.RevisionServiceAdapter
import com.officinetop.officine.data.RevDataSetItem
import com.officinetop.officine.data.getLat
import com.officinetop.officine.data.getLong
import com.officinetop.officine.data.getSelectedCar
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.genericAPICall
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
        revisionServiceAdapter = RevisionServiceAdapter(this, revisionServiceList)
        revision_service_list.adapter = revisionServiceAdapter
        getRevisionServices()
    }

    private fun getRevisionServices() {
        RetrofitClient.client.getRevisionServices("2", getSelectedCar()?.carVersionModel?.idVehicle!!,getLat(),getLong(),"0,25").genericAPICall { _, response ->
            response?.let {
                val revisionServices = it.body()

                revisionServiceList?.let { revisionServiceAdapter = RevisionServiceAdapter(this, revisionServices?.revDataSet) //revisionServiceList)
                    revision_service_list.adapter = revisionServiceAdapter
                }
            }
        }
    }
}
