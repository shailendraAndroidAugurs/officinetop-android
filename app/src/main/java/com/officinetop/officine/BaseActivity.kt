package com.officinetop.officine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.fragment.app.FragmentTransaction
import com.novoda.merlin.Merlin
import com.officinetop.officine.authentication.LoginActivity
import com.officinetop.officine.data.*
import com.officinetop.officine.fragment.*
import com.officinetop.officine.utils.Constant.UrlEndPoints.logout
import com.officinetop.officine.utils.setAppLanguage
import com.officinetop.officine.utils.showInfoDialog
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_bottomnevigationbar.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    lateinit var connectionCallback: Merlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.layout_bottomnevigationbar)
        connectionCallback = Merlin.Builder().withConnectableCallbacks()
                .withDisconnectableCallbacks().build(this)

//        connectionCallback.registerConnectable {
//        }

        connectionCallback.registerDisconnectable {
            showInfoDialog(getString(R.string.Connection_Error))
        }
     /*   bottom_navigation_view.setOnNavigationItemSelectedListener {
           startActivity(intentFor<HomeActivity>("fragmentID" to it.itemId))


         // bindFragment(it.itemId)

            //  loadNavigationItems(it.itemId)
            return@setOnNavigationItemSelectedListener true
        }*/


        /* setSupportActionBar(toolbar)
         supportActionBar?.setDisplayHomeAsUpEnabled(true)
         supportActionBar?.setDisplayShowTitleEnabled(false)

         Log.d("BaseActivity", "onCreate: ")

         if(getStoredToken().isNullOrEmpty()){
             alert { message = "You have been logged out. Please return to login page."
                 okButton { startActivity(intentFor<LoginActivity>().clearTop().clearTask())
                     finish() }
             }.show()
         }*/
        if (getLangLocale() != null && !getLangLocale().equals("")) {
            setAppLanguage()
        } else {
            storeLangLocale("it")
            setAppLanguage()
        }
    }

    @Override
    fun getMerlinConnectionCallback() = connectionCallback


    override fun onResume() {

        connectionCallback.bind()
        super.onResume()
    }

    override fun onPause() {
        connectionCallback.unbind()
        super.onPause()
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var customMenuInflator = menuInflater
        customMenuInflator.inflate(R.menu.menu_options_item, menu)

        return true
    }*/

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> finish()

            R.id.item_home_options -> {

                var menuBuilder = MenuBuilder(this)
                var menuInflater = MenuInflater(this)
                menuInflater.inflate(R.menu.menu_navigation_options, menuBuilder)

                var menuPopUpHelper = MenuPopupHelper(this, menuBuilder, findViewById(R.id.item_home_options))
                menuPopUpHelper.setForceShowIcon(true)

                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                        startActivity(intentFor<HomeActivity>("fragmentID" to item?.itemId))
                        finishAffinity()

                        return true
                    }

                    override fun onMenuModeChange(menu: MenuBuilder) {}
                })

                menuPopUpHelper.show()

//                var popUp = PopupMenu(this, findViewById(R.id.item_home_options))
//                popUp.inflate(R.menu.menu_navigation_options)
//                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {item: MenuItem? ->
//
//
//                    true
//                })
//                popUp.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


  /*  fun loadNavigationItems(itemId: Int) {
        when (itemId) {
            R.id.action_menu_home, R.id.menu_home -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentHome())
                        .commit()


                bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
            }

            R.id.action_news, R.id.menu_news -> {

                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NewsFragment())
                        .commit()

                bottom_navigation_view.menu.findItem(R.id.action_news).isChecked = true
            }

            R.id.action_menu_profile, R.id.menu_profile -> {


                if (!isLoggedIn()) {
                    alert {
                        message = getString(R.string.not_logged_in)
                        positiveButton(getString(R.string.login)) {
                            startActivity(intentFor<LoginActivity>().clearTop())
//                                finish()
                        }
                        negativeButton(getString(R.string.ok)) {}
                    }.show()
                    //                        return@setOnNavigationItemSelectedListener true}
                } else {
                    supportFragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, ProfileFragment())
                            .commit()
                    alert {
                        message = "Logged in as " + getStoredEmail()
                        positiveButton(getString(R.string.logout)) {
                            //logout(getBearerToken()!!)
                        }
                        negativeButton(getString(R.string.ok)) {}
                    }.show()
                }

                bottom_navigation_view.menu.findItem(R.id.action_menu_profile).isChecked = true

            }


            R.id.action_cart, R.id.menu_cart -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentCart())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                bottom_navigation_view.menu.findItem(R.id.action_cart).isChecked = true
            }

            R.id.action_feedback, R.id.menu_feedback -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentFeedback())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()

                bottom_navigation_view.menu.findItem(R.id.action_feedback).isChecked = true
            }

            else -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NewsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                // Log.d("HomeActivity", "onCreate: removing fragment")

                bottom_navigation_view.menu.findItem(R.id.action_menu_home).isChecked = true
            }
        }
    }*/

}