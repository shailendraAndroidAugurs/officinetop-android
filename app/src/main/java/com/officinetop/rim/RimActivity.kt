package com.officinetop.rim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.officinetop.R
import kotlinx.android.synthetic.main.include_toolbar.*

class RimActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rim)
        setSupportActionBar(toolbar)
/*
        toolbar_title.text = resources.getString(R.string.user_location)
*/
    }






}