package com.officinetop.feedback

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.officinetop.BaseActivity
import com.officinetop.Orders.OrderListActivity
import com.officinetop.R
import com.officinetop.adapter.GridItemDecoration
import com.officinetop.adapter.QuotesGridAdapter
import com.officinetop.data.getBearerToken
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import kotlinx.android.synthetic.main.add_fedback_layout.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.io.OutputStream
import okhttp3.MultipartBody
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class FeedbackAddActivity : BaseActivity() {

    private var attachedImage: File? = null
    private var attachedImagePath: String? = null
    private var imagesList: MutableList<String> = ArrayList()//to bind in adapter view
    private var imagesAdapter: QuotesGridAdapter? = null

    private var images: MutableList<String> = ArrayList()//to send with api

    private var workshopId: String = ""
    private var productId: String = ""
    private var sellerId: String = ""


    private var mainCategoryId: String = ""
    private var orderid: String = ""
    private var serviceID: String = ""
    private var productType: String = ""//type for product type, type for tyre is 2, type for spare parts for 1 and type for rim 3
    private var type: String = ""// for product or services
    private var feedbackType: String = ""

    private var withoutPurchase: String = "" // when a customer logs in with an account without a purchase, he can only release one feedback for each product and workshop.
    private var productorWorkshopName: String = ""
    private var motservicetype: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_fedback_layout)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.add_feedback)

        ratings.setOnClickListener {
            Log.d("rating", ratings.rating.toString())
        }

        if (intent.hasExtra(Constant.Path.workshopId))
            workshopId = intent?.getStringExtra(Constant.Path.workshopId) ?: ""
        Log.v("WORKSHOPIDCICK", workshopId)
        if (intent.hasExtra(Constant.Path.productId))
            productId = intent?.getStringExtra(Constant.Path.productId) ?: ""
        if (intent.hasExtra(Constant.Path.productType))
            productType = intent?.getStringExtra(Constant.Path.productType) ?: ""

        if (intent.hasExtra(Constant.Path.sellerId))
            sellerId = intent?.getStringExtra(Constant.Path.sellerId) ?: ""

        if (intent.hasExtra(Constant.Path.ProductOrWorkshopName))
            productorWorkshopName = intent?.getStringExtra(Constant.Path.ProductOrWorkshopName)
                    ?: ""
        if (intent.hasExtra(Constant.Path.motservicetype))
            motservicetype = intent?.getStringExtra(Constant.Path.motservicetype)
                    ?: ""



        if (intent.hasExtra(Constant.Path.mainCategoryId))
            mainCategoryId = intent?.getStringExtra(Constant.Path.mainCategoryId)
                    ?: ""
        if (intent.hasExtra(Constant.Path.serviceID))
            serviceID = intent?.getStringExtra(Constant.Path.serviceID)
                    ?: ""
        if (intent.hasExtra(Constant.Path.type))
            type = intent?.getStringExtra(Constant.Path.type)
                    ?: ""
        if (intent.hasExtra(Constant.Path.orderid))
            orderid = intent?.getStringExtra(Constant.Path.orderid)
                    ?: ""
        if (intent.hasExtra(Constant.Path.withoutPurchase))
            withoutPurchase = intent?.getStringExtra(Constant.Path.withoutPurchase)
                    ?: ""

        capture_images_for_fedback.setOnClickListener {
            if (images.size < 5) {
                var permissionlist = ArrayList<String>()

                permissionlist.add(Manifest.permission.CAMERA)


                checkpermission(permissionlist, { showSelectImageDialog() })


            } else showInfoDialog(getString(R.string.MaxImagesnotmorethanthat))
        }


        when (productType) {
            "1" -> feedbackType = getString(R.string.spare_part)
            "2" -> feedbackType = getString(R.string.tyres)
            "3" -> feedbackType = getString(R.string.Rim)
            else -> feedbackType = getString(R.string.Workshop)
        }

        edt_tipologio.setText(feedbackType)
        product_name.setText(productorWorkshopName)


        fedback_recycler_view.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        fedback_recycler_view.addItemDecoration(GridItemDecoration(10, 2))

        imagesAdapter = QuotesGridAdapter(false, this, ArrayList(), object : QuotesGridAdapter.OnRecyclerItemClickListener {
            override fun onItemRemove(path: String) {
                try {
                    /*for (i in 0 until images.size){*/
                    if (images.contains(path)) {
                        images.remove(path)
                    }
                    /* }*/
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onItemAdd(item: MutableList<String>) {
                try {
                    for (i in 0 until item.size) {
                        images.add(item.get(i))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        fedback_recycler_view.adapter = imagesAdapter



        submit_fedback.setOnClickListener {
            if (!isEditTextValid(this@FeedbackAddActivity, comments)) return@setOnClickListener
            else if (ratings.rating == 0.0f) {
                Snackbar.make(submit_fedback, getString(R.string.PleaseSelectRatings), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                sendFeedback()
            }
        }
    }


    private fun showSelectImageDialog() {
        val imageSelectDialog = AlertDialog.Builder(this)
        imageSelectDialog.setTitle(getString(R.string.PleaseSelect))
        val pictureDialogItems = arrayOf(getString(R.string.Gallery), getString(R.string.Camera))
        imageSelectDialog.setItems(pictureDialogItems) { dialogInterface, i ->
            when (i) {
                0 -> chooseFromGallery()
                1 -> takePicture()
            }
        }
        imageSelectDialog.show()
    }

    private fun chooseFromGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, Constant.GALLERY)
        } catch (e: Exception) {

        }

    }

    private fun takePicture() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, Constant.CAMERA)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun sendFeedback() = try {
        val ProgressDialog = getProgressDialog(true)


        val imageList: ArrayList<MultipartBody.Part?> = ArrayList()
        for (i in 0 until images.size) {
            val file = File(images.get(i))
            imageList.add(file.toMultipartBody("images[]"))
        }

        if (motservicetype == null) {

            motservicetype = ""
        }
        val res = "workshopId=" + workshopId + ",productId=" + productId + ",ratings=" + ratings.rating.toString() + ",images=" + imageList + ",comments=" + comments.text.toString() + ",sellerId=" + sellerId + ",productType=" + productType + ",mainCategoryId=" + mainCategoryId + ",type=" + type + ",serviceID=" + serviceID + ", withoutPurchase" + withoutPurchase
        Log.v("FEEDBACK", res)
        RetrofitClient.client.addFeedback(authToken = getBearerToken()
                ?: "", workshopId = workshopId.toRequestBody(),
                productId = productId.toRequestBody(), ratings = ratings.rating.toString().toRequestBody(), images = imageList,
                comments = comments.text.toString().toRequestBody(), sellerId = sellerId.toRequestBody(), productType = productType.toRequestBody(), mainCategoryId = mainCategoryId.toRequestBody(),
                serviceID = serviceID.toRequestBody(), type = type.toRequestBody(), orderid = orderid.toRequestBody(), motservicetype = motservicetype.toRequestBody(), withoutPurchase = withoutPurchase.toRequestBody()
        )
                .onCall { networkException, response ->
                    ProgressDialog.dismiss()

                    response?.let {
                        ProgressDialog.dismiss()

                        val body = response.body()?.string()
                        if (body.isNullOrEmpty() || response.code() == 401)

                            showConfirmDialog(getString(R.string.Pleaselogintocontinuewithsforating), { moveToLoginPage(this) })
                        if (response.isSuccessful) {

                            val data = JSONObject(body)
                            if (data.has("message") && !data.isNull("message")) {
                                showInfoDialog(data.get("message").toString())
                                if (withoutPurchase.isNullOrBlank()) {
                                    startActivity(intentFor<OrderListActivity>().clearTask().clearTop())
                                }



                                finish()
                                logRateEvent(this, if (type.equals("1")) "product" else if (type.equals("2")) "workshop" else "workshop with product", productorWorkshopName, if (workshopId.equals("")) productId else workshopId, 5, ratings.rating.toDouble())

                            }

                            imagesList.clear()
                            images.clear()
                            comments.setText("")
                            attachedImagePath = null
                            attachedImage = null
                            imagesAdapter?.removeAll()
                            ratings.rating = 0.0f

                        }

                    }
                }
    } catch (e: Exception) {

        e.printStackTrace()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty()) {

            try {
                showSelectImageDialog()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        try {

            if (requestCode == Constant.GALLERY) {
                if (data != null) {
                    val contentUri = data.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        convertTofile(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (requestCode == Constant.CAMERA) {
                val bitmap = data!!.extras!!.get("data") as Bitmap
                if (bitmap != null)
                    convertTofile(bitmap)
            }


            //Log.e("QuotesActivity", "onActivityResult: Picked File path = ${file?.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun convertTofile(bitmap: Bitmap): File {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

            attachedImage = file
            attachedImagePath = file.absolutePath
            //Log.e("file select camera=", "${attachedImage} ${attachedImagePath}")

            imagesList.clear()
            //imageList.add(attachedImage?.toMultipartBodyImages("images[]"))

            attachedImagePath?.let {

                imagesList.add(it)

            }

            imagesAdapter?.addItem(imagesList)


        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}
