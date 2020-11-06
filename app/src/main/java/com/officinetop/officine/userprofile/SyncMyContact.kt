package com.officinetop.officine.userprofile

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.utils.Constant
import kotlinx.android.synthetic.main.activity_sync_my_contact.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.*
import net.kibotu.urlshortener.UrlShortener
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream


class SyncMyContact : BaseActivity() {

    private var contactsInfoList: MutableList<ContactsInfo> = ArrayList()
    private lateinit var UserMobile: String
    private lateinit var ReferralCode: String
    private lateinit var shortlink: String


    companion object {
        const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.officinetop.officine.R.layout.activity_sync_my_contact)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(com.officinetop.officine.R.string.contact_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra("UserMobile")) {
            UserMobile = intent?.getStringExtra("UserMobile") ?: ""
            ReferralCode = intent?.getStringExtra("ReferralCode") ?: ""

        }
        loadContacts()


        // Search View
        search_contact.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchQuery: String?): Boolean {
                //expanded_search.visibility = if(searchQuery.isNullOrEmpty()) View.VISIBLE else View.GONE
                return false
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                val contactsInfoList: MutableList<ContactsInfo> = ArrayList()
                if (searchQuery?.length!! > 0) {
                    for (i in 0 until contactsInfoList.size) {
                        //Log.e("Name ===>",searchQuery)

                        if (searchQuery.let { contactsInfoList[i].phoneNumber?.contains(it) }!!) {
                            Log.e("search", contactsInfoList[i].toString())
                        }
                    }
                    // Log.e("searchlist",contactsInfoList.toString())
                    bindview(contactsInfoList)
                } else {
                    loadContacts()
                }
                return true
            }
        })
    }

    private fun loadContacts() {
        var builder = StringBuilder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS)
            //callback onRequestPermissionsResult
        } else {
            builder = getContacts()
            // listContacts.text = builder.toString()
            bindview(contactsInfoList)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                //  toast("Permission must be granted in order to display contacts information")
            }
        }
    }

    private fun getContacts(): StringBuilder {
        val builder = StringBuilder()
        val resolver: ContentResolver = contentResolver
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC")
        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()
                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
                    if (cursorPhone!!.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
                                    phoneNumValue).append("\n\n")
                            Log.e("Name ===>", "$phoneNumValue/$name")
                            //ContactsInfo.phoneNumber=phoneNumValue
                            contactsInfoList.add(ContactsInfo(id, name, phoneNumValue))
                        }
                    }
                    cursorPhone.close()
                }
            }
        } else {
        }
        cursor.close()
        return builder
    }

    private fun bindview(contactsInfoList: MutableList<ContactsInfo>) {
        val genericAdapter = GenericAdapter<ContactsInfo>(this, R.layout.item_contact_list)
        genericAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                Log.e("orderClickedItems::", "${contactsInfoList[position].phoneNumber}")
                geratelinkforshare(contactsInfoList[position].phoneNumber.toString().trim())
            }

            override fun onItemClick(view: View, position: Int) {
                Log.e("ClickedItems::", "${contactsInfoList[position]}")
            }
        })
        recycler_view.adapter = genericAdapter
        genericAdapter.addItems(contactsInfoList)
        //progress_bar_ticket.visibility= View.GONE
    }

    private fun geratelinkforshare(phoneNumber: String?) {
        val url = Constant.domainBaseURL + "public/manage_referal_code?user=" + UserMobile + "&inviteto=" + phoneNumber + "&referalcode=" + ReferralCode
        UrlShortener.enableLogging
        UrlShortener.shortenUrlByTinyUrl(url)
                .subscribeOn(Schedulers.newThread())
                // .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action1 { t ->
                    Log.v("link", "shortenUrlByGoogle$t")
                    shortlink = t
                    shareimage()
                })
    }

    @SuppressLint("SetWorldReadable")
    private fun shareimage() {
        val post_image = findViewById<View>(R.id.share_image) as ImageView
        val drawable = post_image.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        try {
            val file = File(externalCacheDir, "devofandroid.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_TEXT, "Use OfficineTop app. and Get cashback in your wallet. Use my link $shortlink")
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()))
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "OfficineTop"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}