package com.officinetop.invite_frnds

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.data.isLoggedIn
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import kotlinx.android.synthetic.main.activity_invite_friends.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class InviteFriendsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_friends)

        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.inviteFriends)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initview()
    }

    var bannerImage: Bitmap? = null
    private var inviteCode = ""


    private fun initview() {

        invite_friends.setOnClickListener {
            inviteCode = intent?.getStringExtra("inviteCode") ?: ""
            if (!inviteCode.isNullOrEmpty()) {
                shareCode()
            } else if (!isLoggedIn()) {
                showConfirmDialog(getString(R.string.please_login_to_continue_for_invite_friends),  { moveToLoginPage(this@InviteFriendsActivity) })

            }

        }

        how_it_works.setOnClickListener {

            startActivity(intentFor<HowItWorksActivity>())
        }
        if (isOnline()) {
            getApi()
        } else {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {}
        }
    }


    private fun getApi() {
        val dialog = getProgressDialog(true)


        RetrofitClient.client.getInviteFrndsSummaryAPI().onCall { networkException, response ->

            networkException?.let {
            }

            response?.let {
                dialog.dismiss()
                if (response.isSuccessful) {
                    val data = JSONObject(response.body()?.string())
                    if (data.has("data") && !data.isNull("data") && data.get("data") is JSONObject) {
                        if (data.getJSONObject("data").has("for_registration") && !data.getJSONObject("data").isNull("for_registration")) {
                            first_invite.text = data.getJSONObject("data").getString("for_registration") + "???"
                        }
                        if (data.getJSONObject("data").has("two_level_amount") && !data.getJSONObject("data").isNull("two_level_amount")) {
                            two_invite.text = data.getJSONObject("data").getString("two_level_amount") + "???"
                        }
                        if (data.getJSONObject("data").has("three_level_amount") && !data.getJSONObject("data").isNull("three_level_amount")) {
                            three_invite.text = data.getJSONObject("data").getString("three_level_amount") + "???xN"
                        }
                        if (data.getJSONObject("data").has("image_url") && !data.getJSONObject("data").isNull("image_url")) {
                            // loadImageBitmap(data.getJSONObject("data").getString("image_url"))
                            loadImage(data.getJSONObject("data").getString("image_url"), banner_image)

                            Log.d("image_url", data.getJSONObject("data").getString("image_url"))
                        }
                    }
                } else {

                }
            }
        }

    }

    private fun shareCode() {

        /*"Hi, I am using the OfficineTop App, the only one that offers the highest quality and convenience services throughout Italy: \n" +
                "overhall,service, maintenance, tow truck, car wash, spare parts, tires and car accessories\n" +
                "I'll give you a ???6 coupon. Just register by entering this friend code \"${inviteCode}\", \"download from here\":"*/
        try {

            val drawable = banner_image!!.drawable as BitmapDrawable
            val bitmap = drawable.bitmap
            val textMsg = getString(R.string.inviteShareMessage, inviteCode)
            val file = File(externalCacheDir, "offers.png")
            val fOut = FileOutputStream(file)
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            // bannerImage!!.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_TEXT, textMsg/* + referralcode*/)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()))
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "Officine"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadImageBitmap(url: String) {
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        bannerImage = resource
                        banner_image.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
    }
}
