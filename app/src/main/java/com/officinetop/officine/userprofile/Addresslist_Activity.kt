package com.officinetop.officine.userprofile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.isapanah.awesomespinner.AwesomeSpinner
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.Models
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_addresslist.*
import kotlinx.android.synthetic.main.dialog_add_address.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.item_list_address.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.*


class Addresslist_Activity : BaseActivity(), OnGetLoginUserDetail {
    var latitude = ""
    var longitude = ""
    var postalCode = ""

    var Address = ""
    lateinit var mDialogView: View
    var fromPayment = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addresslist)
        /*  val mFrame = findViewById<View>(R.id.content) as FrameLayout
          LayoutInflater.from(this).inflate(R.layout.activity_addresslist, mFrame, true)*/

        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.User_address)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fab.setOnClickListener { view ->
            add_Upadte_Address(null)
        }
        getUserDetail(this, this)
        if( intent.extras!==null){
            fromPayment = intent.extras!!.getBoolean("FromPayment", false)
        }

    }


    private fun add_Upadte_Address(useraddress: Models.UserAddres?) {
        val buttonTitles = resources.getStringArray(R.array.Address_Type)
        if (useraddress == null) {
            mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null, false)
            mDialogView.spinner_AddressType.setDownArrowTintColor(Color.LTGRAY)
            setPlacePicker("")
            bindSpinner(mDialogView.spinner_AddressType, buttonTitles, "")

        } else {
            mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_address, null, false)

            mDialogView.edt_entered_ZipCode.setText(useraddress?.zipCode)



            latitude = useraddress?.latitude
            longitude = useraddress?.longitude
            Address = useraddress?.address1
            setPlacePicker(useraddress?.address1)
            bindSpinner(mDialogView.spinner_AddressType, buttonTitles, useraddress?.addressType)
        }

        //AlertDialogBuilder
        val mBuilder = this?.let {
            AlertDialog.Builder(it)
                    .setView(mDialogView)
                    .setCancelable(false)

        }

        val mAlertDialog = mBuilder?.show()
        val edt_entered_ZipCode = mDialogView.findViewById(R.id.edt_entered_ZipCode) as EditText





        mDialogView.submit_change_password.setOnClickListener {

            if (Address.isNullOrEmpty() && latitude.isNullOrEmpty() && latitude.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.Please_Select_Address), Toast.LENGTH_LONG).show()
            } else if (!mDialogView.spinner_AddressType.isSelected) {
                Toast.makeText(this, getString(R.string.selectAddressType), Toast.LENGTH_LONG).show()
            } else if (edt_entered_ZipCode.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.enterZipCode), Toast.LENGTH_LONG).show()
            } else {
                DismissFragment()
                mAlertDialog?.dismiss()
                var addresType = ""
                if (mDialogView.spinner_AddressType.selectedItem.equals("Home")) {
                    addresType = "H"
                } else if (mDialogView.spinner_AddressType.selectedItem.equals("Office")) {
                    addresType = "O"
                } else
                    addresType = "Ot"

                postalCode = mDialogView.edt_entered_ZipCode.text.toString()

                if (useraddress == null)
                    AddAddressToServer(addresType)
                else

                    UpdateAddressFromServer(useraddress?.id.toString(), addresType)


            }
        }
        //cancel button click of custom layout
        mDialogView.close_dialog.setOnClickListener {
            //dismiss dialog

            DismissFragment()
            mAlertDialog?.dismiss()
        }

    }

    var selectedPlace: Place? = null
    var placePickerFragment: AutocompleteSupportFragment? = null
    private fun setPlacePicker(address: String?) {

        placePickerFragment = supportFragmentManager.setPlacePicker(this@Addresslist_Activity) { place, error ->
            //on picked
            selectedPlace = place
            latitude = place?.latLng?.latitude.toString()
            longitude = place?.latLng?.longitude.toString()
            Address = place?.address.toString()
            if (place != null) {
                for (i in 0 until place?.addressComponents?.asList()?.size!!) {

                    for (j in 0 until place?.addressComponents?.asList()!![i].types.size!!) {
                        if (place?.addressComponents?.asList()!![i].types[j].equals(getString(R.string.postal_code))) {
                            postalCode = place?.addressComponents?.asList()!![i].name
                            mDialogView.edt_entered_ZipCode.setText(postalCode)
                        }
                    }


                }


                if (error != null)
                    recycler_view.snack(getString(R.string.Failed_pickplace))
            }
        }.setText(
                if (!address.isNullOrEmpty())
                    address else "")


    }

    private fun DismissFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
        val fragmentTransaction: FragmentTransaction? = supportFragmentManager.beginTransaction()
        fragmentTransaction?.remove(fragment!!)
        fragmentTransaction?.commit()
    }


    private fun AddAddressToServer(addressType: String) {

        RetrofitClient.client.addUserAddress(this@Addresslist_Activity.getBearerToken(), Address, postalCode, latitude, longitude, addressType).onCall { networkException, response ->
            response.let {
                if (!response?.body().toString().isNullOrEmpty()) {
                    showInfoDialog(getString(R.string.AddressAddSuccessFully), true, { getUserDetail(this, this) })
                }

            }

        }

    }

    private fun UpdateAddressFromServer(address_id: String, addressType: String) {
        RetrofitClient.client.UpdateAddress(this@Addresslist_Activity.getBearerToken(), Address, postalCode, latitude, longitude, address_id, addressType).onCall { networkException, response ->
            response.let {
                if (!response?.body().toString().isNullOrEmpty()) {
                    showInfoDialog(getString(R.string.AddresUpdateSuccessFully), false, { getUserDetail(this, this) })
                }

            }

        }

    }


    private fun DeleteAddress(address_id: String) {
        RetrofitClient.client.DeleteAddress(this@Addresslist_Activity.getBearerToken(), address_id).onCall { networkException, response ->
            response.let {
                if (!response?.body().toString().isNullOrEmpty()) {
                    showInfoDialog(getString(R.string.AddressDeletedSuccessFully), false, { getUserDetail(this, this) })
                }

            }

        }

    }

    private fun bindSpinner(spinner: AwesomeSpinner, titles: Array<String>, selectedSpinnerValue: String?) {


        spinner.clearSelection()


        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, titles)

        spinner.setOnSpinnerItemClickListener { _, _ -> }

        spinner.setAdapter(adapter)
        if (!selectedSpinnerValue.isNullOrEmpty()) {
            when (selectedSpinnerValue) {
                "H" -> spinner.setSelection(0)
                "O" -> spinner.setSelection(1)
                // else -> spinner.setSelection(2)
            }


        }

    }

    override fun getUserDetailData(ApiRespoinse: Models.UserDetailData?, ApiRespoinsewallet: Models.UserWallet?) {
        val UserContactList: List<Models.UserAddres> = ApiRespoinse!!.userAddress

        class Holder(view: View) : RecyclerView.ViewHolder(view) {

        }

        val myadpter = object : RecyclerView.Adapter<Holder>() {
            var viewBinderHelper = ViewBinderHelper()
            override fun getItemCount(): Int {
                return UserContactList.size
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val layoutInflater = LayoutInflater.from(parent.context)
                viewBinderHelper.setOpenOnlyOne(true);
                return Holder(layoutInflater.inflate(R.layout.item_list_address, parent, false))
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                holder.itemView.tv_Address.setText(UserContactList[position].address1)
                holder.itemView.tv_ZipCode.setText(UserContactList[position].zipCode)
                viewBinderHelper.bind(holder.itemView.swipelayout, UserContactList[position].id.toString());
                holder.itemView.item_edit.setOnClickListener {

                    add_Upadte_Address(UserContactList[position])
                }
                holder.itemView.item_delete.setOnClickListener {
                    showConfirmDialog(getString(R.string.delete_AddressConfirmation), { DeleteAddress(UserContactList[position].id.toString()) })


                }

                holder.itemView.cv_Address.setOnClickListener {

                    if (fromPayment) {

                        val output = Intent()
                        output.putExtra("Address", UserContactList[position].address1 + "," + UserContactList[position].zipCode)
                        output.putExtra("Id", UserContactList[position].id.toString())

                        setResult(101, output)
                        finish()
                    }


                }
            }
        }


        recycler_view.adapter = myadpter
    }

}


