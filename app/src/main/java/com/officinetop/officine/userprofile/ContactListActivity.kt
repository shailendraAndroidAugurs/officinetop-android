package com.officinetop.officine.userprofile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.data.saveContact_ContactForShipping
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_contactlist.*
import kotlinx.android.synthetic.main.changepswrd_dialog.view.close_dialog
import kotlinx.android.synthetic.main.changepswrd_dialog.view.submit_change_password
import kotlinx.android.synthetic.main.dialog_add_contact.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_list_contact.view.*

class ContactListActivity : BaseActivity(), OnGetLoginUserDetail {
    var fromPayment = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactlist)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.User_Contact)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.extras != null) {
            fromPayment = intent.extras!!.getBoolean("FromPayment", false)
        }
        fab_add_contact.setOnClickListener { view ->

            addContact()
        }

        getUserDetail(this, this)


    }

    private fun addContact() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        //AlertDialogBuilder
        val mBuilder = this.let {
            AlertDialog.Builder(it)
                    .setView(mDialogView)
                    .setCancelable(false)

        }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        val edMobile = mDialogView.findViewById(R.id.edt_entered_mobile) as EditText

        mDialogView.submit_change_password.setOnClickListener {

            if (edMobile.text.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_contact_number), Toast.LENGTH_LONG).show()

            } else {

                addContactToServer(edMobile.text.toString())
                mAlertDialog?.dismiss()

            }
        }
        //cancel button click of custom layout
        mDialogView.close_dialog.setOnClickListener {
            mAlertDialog?.dismiss()
        }
    }

    override fun getUserDetailData(ApiRespoinse: Models.UserDetailData?, ApiRespoinsewallet: Models.UserWallet?) {


        val UserContactList: List<Models.UserContact> = ApiRespoinse!!.userContact

        class Holder(view: View) : RecyclerView.ViewHolder(view)

        val myAdapter = object : RecyclerView.Adapter<Holder>() {
            var viewBinderHelper = ViewBinderHelper()
            override fun getItemCount(): Int {
                return UserContactList.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                viewBinderHelper.setOpenOnlyOne(true)
                return Holder(layoutInflater.inflate(R.layout.item_list_contact, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                holder.itemView.text_contactno.text = UserContactList[position].mobile
                viewBinderHelper.bind(holder.itemView.swipelayout, UserContactList[position].id.toString())
                holder.itemView.item_edit_contact.setOnClickListener {

                    updateContact(UserContactList[position].id.toString(), UserContactList[position].mobile.toString())
                }
                holder.itemView.item_delete_car.setOnClickListener {
                    showConfirmDialog(getString(R.string.delete_ContactConfirmation), { deleteContact(UserContactList[position].id.toString()) })


                }

                holder.itemView.cv_Contact.setOnClickListener {
                    if (fromPayment) {
                        val output = Intent()
                        output.putExtra("contactno", UserContactList[position].mobile)
                        output.putExtra("contactId", UserContactList[position].id.toString())
                        setResult(100, output)
                        finish()
                    }


                }
            }
        }


        recycler_view_contacts.adapter = myAdapter
    }


    private fun addContactToServer(mobileNo: String) {

        RetrofitClient.client.addUserContactList(this@ContactListActivity.getBearerToken(), mobileNo).onCall { networkException, response ->
            response.let {
                if (!response?.body().toString().isNullOrEmpty()) {
                    showInfoDialog(getString(R.string.ContactAddSuccessFully), false) { getUserDetail(this, this) }
                    logContactEvent(this)
                }

            }

        }

    }

    private fun updateContactFromServer(mobileNo: String, ContactId: String) {

        RetrofitClient.client.updateContact(this@ContactListActivity.getBearerToken(), ContactId, mobileNo).onCall { networkException, response ->
            response.let {
                if (!response?.body().toString().isNullOrEmpty()) {
                    showInfoDialog(getString(R.string.ContactUpdateSuccessFully), false) { getUserDetail(this, this) }
                    logContactEvent(this)
                }

            }

        }

    }

    private fun deleteContact(ContactId: String) {
        RetrofitClient.client.deleteContact(this@ContactListActivity.getBearerToken(), ContactId).onCall { networkException, response ->
            response.let {
                if (!response?.body().toString().isNullOrEmpty()) {
                    showInfoDialog(getString(R.string.ContactDeletedSuccessFully), true) { getUserDetail(this, this) }
                    val sharedPref = getSharedPreferences("ShippingContact_Address", Context.MODE_PRIVATE)
                    val contactId = sharedPref.getString("contactId", "")
                    if(ContactId.equals(contactId))
                    saveContact_ContactForShipping("","")
                }

            }

        }

    }


    private fun updateContact(contactId: String, mobileNo: String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        mDialogView.tv_ContactNoTitle.text = getString(R.string.update_contact_number)
        mDialogView.Tv_enterMobileNoTitle.visibility = View.GONE
        //AlertDialogBuilder
        val mBuilder = this.let {
            AlertDialog.Builder(it)
                    .setView(mDialogView)
                    .setCancelable(false)

        }

        val mAlertDialog = mBuilder?.show()
        val edt_entered_mobile = mDialogView.findViewById(R.id.edt_entered_mobile) as EditText
        edt_entered_mobile.setText(mobileNo)

        mDialogView.submit_change_password.setOnClickListener {

            when {
                edt_entered_mobile.text.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.enter_contact_number), Toast.LENGTH_LONG).show()

                }
                mobileNo == edt_entered_mobile.text.toString() -> {
                    Toast.makeText(this, getString(R.string.update_contact), Toast.LENGTH_LONG).show()
                }
                else -> {

                    updateContactFromServer(edt_entered_mobile.text.toString(), contactId)
                    mDialogView.tv_ContactNoTitle.text = getString(R.string.add_contact)
                    mDialogView.Tv_enterMobileNoTitle.visibility = View.VISIBLE
                    mAlertDialog?.dismiss()

                }
            }
        }

        mDialogView.close_dialog.setOnClickListener {
            mDialogView.Tv_enterMobileNoTitle.visibility = View.VISIBLE
            mDialogView.tv_ContactNoTitle.text = getString(R.string.add_contact)
            mAlertDialog?.dismiss()
        }
    }
}
