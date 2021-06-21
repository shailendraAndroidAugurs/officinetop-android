package com.officinetop.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.officinetop.data.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.officinetop.Orders.OrderListActivity
import com.officinetop.R
import com.officinetop.WishList.WishListActivity
import com.officinetop.authentication.LoginActivity
import com.officinetop.invite_frnds.InviteFriendsActivity
import com.officinetop.push_notification.NotificationList
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.userprofile.AddressListActivity
import com.officinetop.userprofile.ContactListActivity
import com.officinetop.userprofile.Edit_Profile
import com.officinetop.userprofile.ProfileSetting
import com.officinetop.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.changepswrd_dialog.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(), OnGetLoginUserDetail {
    private lateinit var textview_mobile: TextView
    private lateinit var textview_email: TextView
    private lateinit var textview_name: TextView
    private lateinit var textview_lname: TextView
    private lateinit var text_address: TextView
    private lateinit var text_landmark: TextView
    private lateinit var text_zipcode: TextView
    private lateinit var text_contactnumber: TextView
    private lateinit var txt_referralcode: TextView
    private lateinit var post_image: ImageView
    private lateinit var textview_walletamount: TextView
    private lateinit var profileimage: CircleImageView
    private var imageurl: String = ""

    private lateinit var rootView: View
    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)



        myClipboard = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val button_logout = view.findViewById(R.id.button_logout) as TextView
        textview_mobile = view.findViewById(R.id.textview_mobile) as TextView
        textview_walletamount = view.findViewById(R.id.textview_walletamount) as TextView
        textview_email = view.findViewById(R.id.textview_email) as TextView
        textview_name = view.findViewById(R.id.textview_name) as TextView
        textview_lname = view.findViewById(R.id.textview_lname) as TextView
        text_address = view.findViewById(R.id.text_address) as TextView
        text_landmark = view.findViewById(R.id.text_landmark) as TextView
        text_zipcode = view.findViewById(R.id.text_zipcode) as TextView
        txt_referralcode = view.findViewById(R.id.txt_referralcode) as TextView
        text_contactnumber = view.findViewById(R.id.text_contactnumber) as TextView
        post_image = view.findViewById(R.id.share_image) as ImageView
        profileimage = view.findViewById(R.id.profile_image) as CircleImageView
        val change_password = view.findViewById(R.id.change_password) as Button
        val layout_editprofile = view.findViewById(R.id.layout_editprofile) as LinearLayout
        val layout_addcontact = view.findViewById(R.id.layout_addcontact) as LinearLayout
        val layout_address = view.findViewById(R.id.layout_address) as LinearLayout
        val button_invite = view.findViewById(R.id.button_invite) as TextView

        val layout_orders = view.findViewById(R.id.layout_orders) as LinearLayout
        val layout_profile_setting = view.findViewById(R.id.layout_profile_setting) as LinearLayout
        val layout_notification = view.findViewById(R.id.layout_notification) as LinearLayout
        val copyreferral = view.findViewById(R.id.copyreferral) as LinearLayout
        val layout_wishlist = view.findViewById(R.id.layout_wishlist) as LinearLayout
        Log.d("HomeActivity", "Click")

        button_logout.setOnClickListener(View.OnClickListener {
            if (!context?.isLoggedIn()!!) {
                alert {
                    message = getString(R.string.not_logged_in)
                    positiveButton(getString(R.string.login)) {


                        startActivity(intentFor<LoginActivity>().clearTop().clearTask())
                        activity?.finishAffinity()
                        context!!.storeLangLocale("")
                    }
                    negativeButton(getString(R.string.ok)) {}
                }.show()

            } else {
                alert {
                    message = getString(R.string.LoggedinAs, context?.getStoredEmail())
                    positiveButton(getString(R.string.logout)) {
                        logout(context?.getBearerToken()!!)
                    }
                    negativeButton(getString(R.string.cancel)) {}
                }.show()
            }
        })

        change_password.setOnClickListener {
            alertChangePasssword()
        }
        layout_editprofile.setOnClickListener {
            if (context?.isOnline()!!) {
                try {
                    if (!textview_email.text.toString().isNullOrEmpty() && !textview_mobile.text.toString().isNullOrEmpty()) {
                        val intent = Intent(context, Edit_Profile::class.java)
                        intent.putExtra("Email", textview_email.text.toString())
                        intent.putExtra("Mobile", textview_mobile.text.toString())
                        intent.putExtra("Pic_url", imageurl)
                        intent.putExtra("user_name", textview_name.text.toString())
                        intent.putExtra("user_lname", textview_lname.text.toString())
                        intent.putExtra("Token", context?.getBearerToken()!!)
                        startActivityForResult(intent, 100)
                    }
                } catch (e: Exception) {

                }
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }


        }

        layout_addcontact.setOnClickListener {
            if (context?.isOnline()!!) {
                val intent = Intent(context, ContactListActivity::class.java)
                startActivityForResult(intent, 100)
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }
        }
        layout_address.setOnClickListener {
            if (context?.isOnline()!!) {
                val intent = Intent(context, AddressListActivity::class.java)
                startActivityForResult(intent, 100)
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }
        }
        layout_profile_setting.setOnClickListener {
            val intent = Intent(context, ProfileSetting::class.java)
            startActivity(intent)
        }
        copyreferral.setOnClickListener {
            val clip = ClipData.newPlainText("text", txt_referralcode.text)
            myClipboard?.setPrimaryClip(clip)
            Toast.makeText(context, ""+resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }

        rootView = view
        layout_wishlist.setOnClickListener {
            if (context?.isOnline()!!) {
                startActivity(intentFor<WishListActivity>())
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }

        }

        button_invite.setOnClickListener {
            /*  val intent = Intent(context, SyncMyContact::class.java)
              intent.putExtra("UserMobile", textview_mobile.text.toString())
              intent.putExtra("ReferralCode", txt_referralcode.text.toString())
              startActivity(intent)*/
            //shareCode(txt_referralcode.text.toString())

            val intent = Intent(context, InviteFriendsActivity::class.java)
            intent.putExtra("inviteCode", txt_referralcode.text.toString())
            startActivity(intent)
        }
        layout_orders.setOnClickListener {
            if (context?.isOnline()!!) {
                val intent = Intent(context, OrderListActivity::class.java)
                startActivity(intent)
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }

        }
        layout_notification.setOnClickListener(View.OnClickListener {
            if (context?.isOnline()!!) {
                val intent = Intent(context, NotificationList::class.java)
                startActivity(intent)
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }

        })
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context?.isLoggedIn()!!) {
            if (context?.isOnline()!!) {
                activity?.let { getUserDetail(this, it) }
            } else {
                context?.showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
            }


        }

        if (context?.getLangLocale() != null && !context?.getLangLocale().equals("")) {
            context?.setAppLanguage()
        } else {
            context?.storeLangLocale("it")
            context?.setAppLanguage()
        }
    }

    private fun alertChangePasssword() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.changepswrd_dialog, null)
        val mBuilder = context?.let {
            AlertDialog.Builder(it)
                    .setView(mDialogView)
                    .setCancelable(false)
        }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        val oldpaswrdEditText = mDialogView.findViewById(R.id.oldpaswrdEditText) as EditText
        val newpaswrdEditText = mDialogView.findViewById(R.id.newpaswrdEditText) as EditText
        val retypepaswrdEditText = mDialogView.findViewById(R.id.retypepaswrdEditText) as EditText

        //login button click of custom layout
        mDialogView.submit_change_password.setOnClickListener {
            if (oldpaswrdEditText.text.isEmpty()) {
                Toast.makeText(context, getString(R.string.old_password), Toast.LENGTH_LONG).show()
            } else if (newpaswrdEditText.text.isEmpty()) {
                Toast.makeText(context, getString(R.string.new_password), Toast.LENGTH_LONG).show()
            } else if (retypepaswrdEditText.text.isEmpty()) {
                Toast.makeText(context, getString(R.string.re_type_password), Toast.LENGTH_LONG).show()
            } else {
                if (newpaswrdEditText.text.toString() == retypepaswrdEditText.text.toString()) {
                    mAlertDialog?.dismiss()
                    changePassword(oldpaswrdEditText.text.toString(), newpaswrdEditText.text.toString(), retypepaswrdEditText.text.toString(), context?.getBearerToken()!!)
                } else {
                    Toast.makeText(context, getString(R.string.password_not_matched), Toast.LENGTH_LONG).show()
                }
            }
        }
        //cancel button click of custom layout
        mDialogView.close_dialog.setOnClickListener {
            mAlertDialog?.dismiss()
        }
    }

    private fun changePassword(oldPaswword: String, newPassword: String, confirmPasword: String, authToken: String) {
        try {
            val progressDialog = context?.getProgressDialog()
            progressDialog?.show()
            if (!activity?.let { showOnlineSnack(progressDialog, rootView, it) }!!)
                return
            RetrofitClient.client.changepassword(authToken, old_password = oldPaswword, new_password = newPassword, confirm_password = confirmPasword)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progressDialog?.dismiss()
                            toast(t.message!!)
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val body = response.body()?.string()
                            progressDialog?.dismiss()
                            Log.d("password", "onResponse: logout = $body")
                            if (response.isSuccessful) {
                                try {
                                    val jsonObject = JSONObject(body)
                                    val status_code = jsonObject.optString("status_code")
                                    val msg = jsonObject.optString("message")
                                    if (status_code == "1") {
                                        alert {
                                            message = msg
                                            positiveButton(getString(R.string.ok)) {
                                            }
                                        }.show()
                                    } else {
                                        alert {
                                            message = msg
                                            positiveButton(getString(R.string.retry)) {
                                                alertChangePasssword()
                                            }
                                            negativeButton(getString(R.string.ok)) {}
                                        }.show()
                                    }

                                } catch (e: java.lang.Exception) {
                                    e.message
                                    e.printStackTrace()
                                }
                            }
                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun logout(authToken: String) {
        val progressDialog = context?.getProgressDialog()
        progressDialog?.show()
        if (!activity?.let { showOnlineSnack(progressDialog, rootView, it) }!!)
            return
        RetrofitClient.client.logout(authToken)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressDialog?.dismiss()
                        toast(t.message!!)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val responseString = response.body()?.string()
                        progressDialog?.dismiss()
                        Log.d("LoginActivity", "onResponse: logout = $responseString")
                        context?.removeLocallocation()
                        if (response.code() == 200) {
                            context?.removeUserDetail()
                        } else if (response.code() == 401) {
                            context?.removeUserDetail()
                            startActivity(intentFor<LoginActivity>().clearTop())
                            activity?.finish()
                        }
                        Log.d("logout", responseString.toString())
                        responseString?.let {
                            toast(getMessageFromJSON(it))
                            startActivity(intentFor<LoginActivity>().clearTop())
                            activity?.finish()
                            Log.d("LogoutSuccessfully", "yes")
                        }
                    }
                })
    }


    private fun uiContentsBinding(mobileNumber: String?, email: String?, fname: String?, lname: String?, imageurl: String, address: String?, zipCode: String, landmark: String?, mobileno: String, referralcode: String, walletamount: String) {
        if (!isAdded) return
        if (walletamount.isNullOrEmpty() || walletamount.equals("null"))
            textview_walletamount.text = getString(R.string.euro_symbol) + " 0"
        else
            textview_walletamount.text = getString(R.string.euro_symbol) + " " + walletamount


        textview_mobile.text = mobileNumber
        textview_email.text = email
        textview_name.text = fname?.trim()
        textview_lname.text = lname?.trim()
        text_address.text = address
        txt_referralcode.text = if (!referralcode.isNullOrBlank()) referralcode.toUpperCase() else ""
        text_zipcode.text = getString(R.string.zip_code) + zipCode
        text_contactnumber.text = mobileno
        context?.loadImageProfile(imageurl, profileimage)
    }


    private fun setDataInView(userDetailData: Models.UserDetailData?, apiRespoinsewallet: Models.UserWallet?) {
        val userContact: Models.UserContact
        val userAddress: Models.UserAddres
        if (userDetailData?.userContact != null && userDetailData.userContact.size > 0 && userDetailData.userAddress.size > 0) {
            if (userDetailData.userDetails != null && userDetailData.userDetails.size != 0) {
                val userDetail = userDetailData.userDetails.get(0)
                val sb = StringBuilder()
                sb.append(Constant.profileBaseUrl).append(userDetail.profileImage)
                imageurl = sb.toString()
                userContact = userDetailData.userContact.get(0)
                userAddress = userDetailData.userAddress.get(0)
                uiContentsBinding(userDetail.mobileNumber.toString(), userDetail.email, if (!userDetail.fName.isNullOrBlank()) userDetail.fName else "", if (!userDetail.lName.isNullOrBlank()) userDetail.lName else "", imageurl, userAddress.address1, userAddress.zipCode.toString(), userAddress.landmark, userContact.mobile.toString(), userDetail.ownreferalcode.toString(), apiRespoinsewallet?.amount.toString())

            } else {
                userContact = userDetailData.userContact.get(0)
                userAddress = userDetailData.userAddress.get(0)
                uiContentsBinding("", "", "", "", "", userAddress.address1, userAddress.zipCode.toString(), userAddress.landmark, userContact.mobile.toString(), "", apiRespoinsewallet?.amount.toString())
            }
        } else if (userDetailData?.userContact?.size!! > 0) {
            if (userDetailData.userDetails != null && userDetailData.userDetails.size != 0) {
                val userDetail = userDetailData.userDetails.get(0)
                val sb = StringBuilder()
                sb.append(Constant.profileBaseUrl).append(userDetail.profileImage)
                imageurl = sb.toString()
                userContact = userDetailData.userContact.get(0)
                uiContentsBinding(userDetail.mobileNumber.toString(), userDetail.email, if (!userDetail.fName.isNullOrBlank()) userDetail.fName else "", if (!userDetail.lName.isNullOrBlank()) userDetail.lName else "", imageurl, getString(R.string.Add_address), "", "", userContact.mobile.toString(), userDetail.ownreferalcode.toString(), apiRespoinsewallet?.amount.toString())
            } else {
                userContact = userDetailData.userContact.get(0)
                uiContentsBinding("", "", "", "", "", getString(R.string.Add_address), "", "", userContact.mobile.toString(), "", apiRespoinsewallet?.amount.toString())
            }
        } else if (userDetailData.userAddress.size > 0) {
            if (userDetailData.userDetails != null && userDetailData.userDetails.size != 0) {
                val userDetail = userDetailData.userDetails.get(0)
                val sb = StringBuilder()
                sb.append(Constant.profileBaseUrl).append(userDetail.profileImage)
                imageurl = sb.toString()
                userAddress = userDetailData.userAddress.get(0)
                uiContentsBinding(userDetail.mobileNumber.toString(), userDetail.email, if (!userDetail.fName.isNullOrBlank()) userDetail.fName else "", if (!userDetail.lName.isNullOrBlank()) userDetail.lName else "", imageurl, userAddress.address1, userAddress.zipCode.toString(), userAddress.landmark, "", userDetail.ownreferalcode.toString(), apiRespoinsewallet?.amount.toString())
            } else {
                userAddress = userDetailData.userAddress.get(0)
                uiContentsBinding("", "", "", "", "", userAddress.address1, userAddress.zipCode.toString(), userAddress.landmark, "", "", apiRespoinsewallet?.amount.toString())
            }
        } else {
            if (userDetailData.userDetails != null && userDetailData.userDetails.size != 0) {
                val userDetail = userDetailData.userDetails.get(0)
                val sb = StringBuilder()
                sb.append(Constant.profileBaseUrl).append(userDetail.profileImage)
                imageurl = sb.toString()
                uiContentsBinding(userDetail.mobileNumber, userDetail.email, if (!userDetail.fName.isNullOrBlank()) userDetail.fName else "", if (!userDetail.lName.isNullOrBlank()) userDetail.lName else "", imageurl, getString(R.string.Add_address), "", "", getString(R.string.Add_mobile_number), userDetail.ownreferalcode.toString(), apiRespoinsewallet?.amount.toString())
            } else {
                uiContentsBinding("", "", "", "", "", getString(R.string.Add_address), "", "", getString(R.string.Add_mobile_number), "", apiRespoinsewallet?.amount.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (context?.isLoggedIn()!!) {
            activity?.let { getUserDetail(this, it) }
        }
    }

    override fun getUserDetailData(ApiRespoinse: Models.UserDetailData?, ApiRespoinsewallet: Models.UserWallet?) {
        if (!isAdded) return

        setDataInView(ApiRespoinse, ApiRespoinsewallet)
    }


    private fun createLink(linkUri: Uri) {
        /* FirebaseDynamicLinks.getInstance().createDynamicLink()
                 .setLink(Uri.parse("https://www.example.com/"))
                 .setDomainUriPrefix("https://example.page.link")
                 .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                 .buildShortDynamicLink()
                 .addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         val shortLink = task.result?.shortLink
                         val msg = "Hey, check out this nutritious salad I found: $shortLink"
                         val sendIntent = Intent()
                         sendIntent.action = Intent.ACTION_SEND
                         sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
                         sendIntent.type = "text/plain"
                         startActivity(sendIntent)
                     } else {
                         // Timber.e(task.exception)
                     }


                 }*/


        val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.example.com/"))
                .setDomainUriPrefix("https://example.page.link")

                .buildShortDynamicLink()
                .addOnSuccessListener { result ->
                    // Short link created
                    val shortLink = result.shortLink
                    val flowchartLink = result.previewLink
                    val msg = "Hey, check out this nutritious salad I found: $shortLink"
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                }.addOnFailureListener {

                    Log.e("Exception", "")
                }
    }

    private fun createShareUri(saladId: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(getString(R.string.config_scheme)) // "http"
                .authority(getString(R.string.config_host)) // "365salads.xyz"
                .appendPath(getString(R.string.config_path_salads)) // "salads"
                .appendQueryParameter("hi", saladId)
        return builder.build()
    }

    private fun shareCode(referralcode: String) {

        val drawable = post_image.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        try {
            val file = File(context?.externalCacheDir, "offers.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.useofficine_and_Get_cashback_in_your_wallet_Use_my_Code) + referralcode)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()))
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "Officine"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
