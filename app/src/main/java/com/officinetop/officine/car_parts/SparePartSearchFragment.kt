package com.officinetop.officine.car_parts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.cunoraz.tagview.Tag
import com.cunoraz.tagview.TagView
import com.officinetop.officine.R
import com.officinetop.officine.data.SearchKeywordResponse
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.isStatusCodeValid
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.genericAPICall
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_part_categories.*
import org.jetbrains.anko.intentFor
import java.util.concurrent.Executors


class SparePartSearchFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val content = inflater.inflate(R.layout.activity_search_view, container, false)
        openSearchDialog(content)
        return content
    }


    private fun openSearchDialog(view: View) {

        val allSearchSection = view.findViewById<TagView>(R.id.all_search_section)
        val recentSearchSection = view.findViewById<TagView>(R.id.recent_search_section)

        view.findViewById<TextView>(R.id.search_all_categories).setOnClickListener {
            val ft: FragmentTransaction = fragmentManager!!.beginTransaction()
            ft.detach(this).commit()
           // activity?.findViewById<EditText>(R.id.search_product)?.setText("")
            activity?.findViewById<FrameLayout>(R.id.containerFor_search)?.visibility = View.GONE
            parentFragmentManager.popBackStackImmediate()
        }

        allSearchSection.removeAll()
        recentSearchSection.removeAll()

        context?.getBearerToken()?.let { token ->

            RetrofitClient.client.getSearchKeyWords(token).genericAPICall { _, response ->
                val modifiedResult = response?.body() as SearchKeywordResponse

                modifiedResult.data?.all?.forEach {
                    it?.keyword?.let { allSearchSection.addTag(Tag(it)) }
                }

                modifiedResult.data?.self?.forEach {
                    it?.keyword?.let { recentSearchSection.addTag(Tag(it)) }
                }

                allSearchSection.setOnTagClickListener { tag, _ -> searchStoreQuery(tag.text) }
                recentSearchSection.setOnTagClickListener { tag, _ -> searchStoreQuery(tag.text) }

            }
        }



        view.findViewById<TextView>(R.id.clear_searches).setOnClickListener {
            context?.getBearerToken()?.let { token ->

                RetrofitClient.client.clearSearchKeyWords(token).genericAPICall { _, response ->
                    if (isStatusCodeValid(response?.body()?.string())) {
                        context?.showInfoDialog(getString(R.string.Keywordcleared))
                        //activity?.findViewById<EditText>(R.id.search_product)?.setText("")
                        activity?.findViewById<FrameLayout>(R.id.containerFor_search)?.visibility = View.GONE
                        parentFragmentManager.popBackStackImmediate()
                    } else
                        context?.showInfoDialog(getString(R.string.Cannotclearsearchkeywords))
                }
            }
        }


    }

    private fun searchStoreQuery(query: String) {

        Executors.newSingleThreadExecutor().submit {

            Log.v("Save QUERY", "************* $query")
            RetrofitClient.client.addSearchQuery(query, context?.getBearerToken()
                    ?: "").genericAPICall { _, response ->
                Log.v("Save QUERY", "************* response $response")
            }

        }
        startActivity(context?.intentFor<ProductListActivity>(Constant.Key.searchedKeyword to query,
                Constant.Key.searchedCategoryType to null))
       // activity?.findViewById<EditText>(R.id.search_product)?.setText("")
        activity?.findViewById<FrameLayout>(R.id.containerFor_search)?.visibility = View.GONE
        parentFragmentManager.popBackStackImmediate()
    }
}
