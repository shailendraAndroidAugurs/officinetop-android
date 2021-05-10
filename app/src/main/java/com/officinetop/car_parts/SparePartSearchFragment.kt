package com.officinetop.car_parts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.cunoraz.tagview.Tag
import com.cunoraz.tagview.TagView
import com.officinetop.R
import com.officinetop.data.SearchKeywordResponse
import com.officinetop.data.getBearerToken
import com.officinetop.data.isStatusCodeValid
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.Constant
import com.officinetop.utils.genericAPICall
import com.officinetop.utils.showInfoDialog
import org.jetbrains.anko.intentFor
import java.util.concurrent.Executors


class SparePartSearchFragment : Fragment(), SearchFilterInterface {
    var searchKeywordHistory = ArrayList<String>()
    var searchKeywordData = ArrayList<String>()
    lateinit var allSearchSection: TagView
    lateinit var recentSearchSection: TagView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val content = inflater.inflate(R.layout.activity_search_view, container, false)
        (activity as PartCategories?)?.setActivityListener(this@SparePartSearchFragment)
        openSearchDialog(content)
        return content
    }


    private fun openSearchDialog(view: View) {

        allSearchSection = view.findViewById<TagView>(R.id.all_search_section)
        recentSearchSection = view.findViewById<TagView>(R.id.recent_search_section)

        view.findViewById<TextView>(R.id.search_all_categories).setOnClickListener {
            val ft: FragmentTransaction = requireFragmentManager().beginTransaction()
            ft.detach(this).commit()
            activity?.findViewById<FrameLayout>(R.id.containerFor_search)?.visibility = View.GONE
            parentFragmentManager.popBackStackImmediate()
        }

        allSearchSection.removeAll()
        recentSearchSection.removeAll()

        context?.getBearerToken()?.let { token ->
            searchKeywordData.clear()
            searchKeywordHistory.clear()

            RetrofitClient.client.getSearchKeyWords(token).genericAPICall { _, response ->
                val modifiedResult = response?.body() as SearchKeywordResponse

                modifiedResult.data?.all?.forEach {
                    it?.keyword?.let { it1 ->
                        searchKeywordHistory.add(it1)
                        allSearchSection.addTag(Tag(it1))
                    }
                }

                modifiedResult.data?.self?.forEach {
                    it?.keyword?.let { it1 ->
                        searchKeywordData.add(it1)
                        recentSearchSection.addTag(Tag(it1))
                    }
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

    private fun bindSearchData() {

    }

    override fun SearchProduct(SearchString: String) {
        allSearchSection.removeAll()
        recentSearchSection.removeAll()
        if (searchKeywordData.size != 0) {
            searchKeywordData.forEach {
                if (it.matches(Regex(SearchString)) || it.contains(SearchString)) {
                    recentSearchSection.addTag(Tag(it))
                }
            }
        }
        if (searchKeywordHistory.size != 0) {
            searchKeywordHistory.forEach {
                if (it.matches(Regex(SearchString)) || it.contains(SearchString)) {
                    allSearchSection.addTag(Tag(it))
                }
            }
        }
    }
}
