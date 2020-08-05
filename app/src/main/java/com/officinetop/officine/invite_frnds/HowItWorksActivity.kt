package com.officinetop.officine.invite_frnds

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import kotlinx.android.synthetic.main.activity_howit_works.*
import kotlinx.android.synthetic.main.include_toolbar.*

class HowItWorksActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howit_works)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.how_works)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}
