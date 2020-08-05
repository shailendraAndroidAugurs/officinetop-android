package com.officinetop.officine.car_parts

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.cunoraz.tagview.Tag
import com.officinetop.officine.R
import com.officinetop.officine.data.SearchKeywordResponse
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.isStatusCodeValid
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.genericAPICall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_search_view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.alert
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SearchActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_title.text = "Search"

        initTagView()

        search_btn.setOnClickListener { sendBackResult(search_field.text.toString()) }

        clear_searches.setOnClickListener {
            getBearerToken()?.let {token ->

                RetrofitClient.client.clearSearchKeyWords(token).genericAPICall { _, response ->
                    if(isStatusCodeValid(response?.body()?.string())) {
                        showInfoDialog(getString(R.string.Keywordcleared))
                        initTagView()
                    }
                    else
                        showInfoDialog(getString(R.string.Cannotclearsearchkeywords))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun initTagView() {
        all_search_section.removeAll()
        recent_search_section.removeAll()
        getBearerToken()?.let { token ->

            RetrofitClient.client.getSearchKeyWords(token).genericAPICall { _, response ->
                val modifiedResult = response?.body() as SearchKeywordResponse

                modifiedResult.data?.all?.forEach {
                    it?.keyword?.let { all_search_section.addTag(Tag(it))  }
                }

                modifiedResult.data?.self?.forEach {
                    it?.keyword?.let { recent_search_section.addTag(Tag(it)) }
                }

                all_search_section.setOnTagClickListener { tag, _ -> sendBackResult(tag.text) }
                recent_search_section.setOnTagClickListener { tag, _ -> sendBackResult(tag.text) }
            }

        }
    }

    private fun sendBackResult(query:String){
        setResult(Activity.RESULT_OK, intent?.apply { putExtra("query", query) } )
        finish()

        storeSearchQuery(query)
    }


    private fun storeSearchQuery(query: String){

        Executors.newSingleThreadExecutor().submit {

        RetrofitClient.client.addSearchQuery(query, getBearerToken()?:"").genericAPICall { _, response -> }

    }

    }
}