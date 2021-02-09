package com.officinetop.officine.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getLangLocale
import com.officinetop.officine.data.storeLangLocale
import com.officinetop.officine.feedback.FeedbackReview
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.isOnline
import com.officinetop.officine.utils.setAppLanguage
import com.officinetop.officine.utils.showInfoDialog
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentFeedback : Fragment(){

    private lateinit var feedbackListener: FeedbackReview
    private var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    private var MyFeedBackReview: TextView? = null
    var MyReview: Boolean = false
    private var WorkshopFeedBackList: ArrayList<Models.HighRatingfeedback> = ArrayList()
    private var ProductFeedBackList: ArrayList<Models.HighRatingfeedback> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)
        view.bindViews()
        return view
    }

    private fun View.bindViews() {
        if (context?.getLangLocale() != null && !context?.getLangLocale().equals("")) {
            context?.setAppLanguage()
        } else {
            context?.storeLangLocale("it")
            context?.setAppLanguage()
        }
        if (context?.isOnline()!!) {
        }else{
            context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }

        viewPager = findViewById(R.id.feedback_viewpager)
        tabLayout = findViewById(R.id.tabs)
        tabLayout!!.setupWithViewPager(viewPager)


        if (!isAdded) {
            return
        } else {
            if (context?.isOnline()!!) {
                highRatingFeedback("2",0)
                highRatingFeedback("1",0)
            }else{
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }
            MyFeedBackReview = findViewById(R.id.tv_review)
            MyFeedBackReview?.setOnClickListener {
                if (MyFeedBackReview?.tag == "100") {
                    MyFeedBackReview?.text = getString(R.string.MyReview)
                    MyFeedBackReview?.tag = "101"
                    MyReview = false
                } else {
                    MyReview = true
                    MyFeedBackReview?.text = getString(R.string.AllReview)
                    MyFeedBackReview?.tag = "100"
                }
                viewPager?.invalidate()
                viewPager?.adapter?.notifyDataSetChanged()
            }
        }
    }
    private fun setupViewPager(viewPager: ViewPager) {
        if (!isAdded) {
            return
        } else {
            val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(FragmentWorkshopFeedback(), getString(R.string.feedback_Workshop))
            adapter.addFragment(FragmentProductFeedback(), getString(R.string.feedback_Product))
            viewPager.adapter = adapter
        }
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()
        override fun getItem(position: Int): Fragment {
            val bundle = Bundle()
            if (position == 1) {
                if (ProductFeedBackList.size != 0 && MyFeedBackReview != null) {
                    MyFeedBackReview!!.visibility = View.VISIBLE

                } else {
                    MyFeedBackReview!!.visibility = View.GONE
                }
                bundle.putSerializable("list", ProductFeedBackList)
                bundle.putBoolean("product", true)
                bundle.putBoolean("MyReview", MyReview)
                Log.d("MyReview", "MyReview$MyReview")
                Log.d("FragmnetFor", "Product" + "true")
                Log.d("list", "ProductFeedBackList" + ProductFeedBackList.size)
            } else {

                bundle.putSerializable("list", WorkshopFeedBackList)
                bundle.putBoolean("product", false)
                bundle.putBoolean("MyReview", MyReview)
                Log.d("MyReview", "MyReview$MyReview")
                Log.d("FragmnetFor", "workhsop" + "true")
                Log.d("list", "WorkshopFeedBackList" + WorkshopFeedBackList.size)
            }
            mFragmentList[position].arguments = bundle

            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }

        override fun getItemPosition(postion: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }

     fun highRatingFeedback(type: String,limit : Int){
        RetrofitClient.client.getHighRatingFeedback(type,limit)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            try
                            {
                                val body = JSONObject(response.body()?.string())
                                Log.d("highRatingfromFragment", "yes")
                                if (body.has("data_set") && body.get("data_set") != null) {
                                    val jsonarray = body.get("data_set") as JSONArray
                                    val gson = GsonBuilder().create()
                                    val productOrworkshopFeedback = gson.fromJson(jsonarray.toString(), Array<Models.HighRatingfeedback>::class.java).toCollection(java.util.ArrayList<Models.HighRatingfeedback>())

                                    if (type == "1") {
                                        ProductFeedBackList.addAll(productOrworkshopFeedback)


                                    } else if (type == "2") {
                                        WorkshopFeedBackList.addAll(productOrworkshopFeedback)


                                    }



                                    setupViewPager(viewPager!!)


                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                        if (type == "2") {



                        }

                    }
                })
    }

    fun setActivityListener(activityListener: FeedbackReview) {
        this.feedbackListener = activityListener; }




}
