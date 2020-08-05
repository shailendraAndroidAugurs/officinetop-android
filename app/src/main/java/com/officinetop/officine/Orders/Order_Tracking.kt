package com.officinetop.officine.Orders

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import kotlinx.android.synthetic.main.activity_order__tracking.*
import kotlinx.android.synthetic.main.include_toolbar.*

class Order_Tracking : BaseActivity(){



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order__tracking)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.shipment)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        progressView.progress += 40
        progressView.labelText = "On the way"
        progressView.setOnProgressChangeListener { progressView.labelText = "On the way" }



    }





}
