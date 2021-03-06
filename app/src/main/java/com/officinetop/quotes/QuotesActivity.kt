package com.officinetop.quotes

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.officinetop.BaseActivity
import com.officinetop.R
import com.officinetop.adapter.GridItemDecoration
import com.officinetop.adapter.QuotesGridAdapter
import com.officinetop.data.*
import com.officinetop.retrofit.RetrofitClient
import com.officinetop.utils.*
import com.officinetop.workshop.WorkshopListActivity
import kotlinx.android.synthetic.main.activity_quotes.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_recycler_view.progress_bar
import kotlinx.android.synthetic.main.spinner_item_layout.view.*
import kotlinx.io.OutputStream
import okhttp3.MultipartBody
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class QuotesActivity : BaseActivity() {
    private var attachedImage: File? = null
    private var attachedImagePath: String? = null
    private var imagesList: MutableList<String> = ArrayList()//to bind in adapter view
    private var imagesAdapter: QuotesGridAdapter? = null
    private var servicesId: String? = ""
    private var mainCategoryId: String? = ""
    private var images: MutableList<String> = ArrayList()//to send with api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)
        setSupportActionBar(toolbar)
        toolbar_title.text = resources.getString(R.string.quotes)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ll_take_images.setOnClickListener {
            var permissionlist = ArrayList<String>()
            permissionlist.add(Manifest.permission.CAMERA)

            checkpermission(permissionlist, { showSelectImageDialog() })
        }

        quotes_recycler_view.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        quotes_recycler_view.addItemDecoration(GridItemDecoration(10, 2))

        imagesAdapter = QuotesGridAdapter(false, this, ArrayList(), object : QuotesGridAdapter.OnRecyclerItemClickListener {
            override fun onItemRemove(path: String) {
                try {
                    for (i in 0 until images.size) {
                        if (images.contains(path)) {
                            images.removeAt(i)
                        }
                    }
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
        quotes_recycler_view.adapter = imagesAdapter

        if (isOnline()) {
            getQuotesCategory()
        } else {
            showInfoDialog(getString(R.string.TheInternetConnectionAppearstobeoffline), true) {
                finish()
            }
        }


        confirm_online_request_only.setOnClickListener {
            if (!isEditTextValid(this@QuotesActivity, edt_describe_request)) return@setOnClickListener
            if (isLoggedIn()) {
                quotesApiCall("1")
            } else {
                showConfirmDialogForLogin(getString(R.string.please_login_to_continue_for_qutoes_request_send), { moveToLoginPage(this@QuotesActivity) })
            }


        }
        appointment_for_control.setOnClickListener {
            if (!isEditTextValid(this@QuotesActivity, edt_describe_request)) return@setOnClickListener
            //quotesApiCall("2")


            startActivity(intentFor<WorkshopListActivity>(
                    Constant.Key.is_quotes to true,
                    Constant.Path.categoryId to servicesId,
                    Constant.Path.serviceQuotesInsertedId to "",
                    Constant.Path.qutoesUserDescription to edt_describe_request.text.toString(),
                    Constant.Path.qutoesUserAttachImage to Gson().toJson(images),
                    Constant.Path.mainCategoryId to mainCategoryId))

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

    private fun quotesApiCall(btnType: String) {
        try {
            val imageList: MutableList<MultipartBody.Part?> = ArrayList()
            for (i in 0 until images.size) {
                val file = File(images.get(i))
                imageList.add(file.toMultipartBody("images[]"))
            }



            progress_bar.visibility = View.VISIBLE

            val selectedFormattedDate = SimpleDateFormat(Constant.dateformat_workshop, getLocale()).format(Date())
            Log.d("ServicesQutoesParameter", "user_id=" + getUserId() + "&categoryType=" + mainCategoryId!! + "&description=" + edt_describe_request.text.toString() + "&images=" + "" + "&selectedDate=" + selectedFormattedDate + "&carSize=" + getSelectedCar()?.carSize.toString() + "&buttonType=" + btnType)

            RetrofitClient.client.serviceQuotes(
                    authToken = getBearerToken() ?: "",
                    user_id = getUserId().toRequestBody(),
                    categoryType = mainCategoryId!!.toRequestBody(),
                    description = edt_describe_request.text.toString().toRequestBody(),
                    images = imageList,
                    selectedDate = selectedFormattedDate.toRequestBody(),
                    carSize = getSelectedCar()?.carSize.toString().toRequestBody(),
                    buttonType = btnType.toRequestBody()
            )
                    .onCall { networkException, response ->

                        networkException?.let {
                            progress_bar.visibility = View.GONE
                        }

                        response?.let {
                            progress_bar.visibility = View.GONE
                            if (response.isSuccessful) {
                                try {
                                    showInfoDialog(getString(R.string.quotes_request_send_successfully),false) { resetView() }


                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getQuotesCategory() {
        try {
            RetrofitClient.client.getQuotesCategory(getBearerToken() ?: "")
                    .onCall { networkException, response ->

                        response?.let {
                            if (response.isSuccessful) {

                                val body = JSONObject(response.body()?.string())
                                if (body.has("data_set") && !body.isNull("data_set")) {
                                    val data = body.getJSONArray("data_set")
                                    val mainCategoryList: MutableList<Models.MainCategory> = ArrayList()
                                    for (i in 0 until data.length()) {
                                        val modelData = Gson().fromJson<Models.MainCategory>(data.get(i).toString(), Models.MainCategory::class.java)
                                        mainCategoryList.add(modelData)
                                    }
                                    spinner_categories.adapter = object : ArrayAdapter<Models.MainCategory>(this@QuotesActivity,
                                            R.layout.spinner_item_layout, mainCategoryList) {
                                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                            return createView(position, convertView, parent)
                                        }

                                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                            return createView(position, convertView, parent)
                                        }

                                        fun createView(position: Int, v: View?, parent: ViewGroup): View {
                                            val view = v
                                                    ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_layout, parent, false)
                                            val item = getItem(position)
                                            view.name.text = item?.mainCatName
                                            return view
                                        }
                                    }
                                    spinner_categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                            val items: Models.MainCategory = p0?.getItemAtPosition(p2) as Models.MainCategory
                                            servicesId = items.serviceId.toString()
                                            mainCategoryId = items.Qutoes_mainCategoryId.toString()
                                        }

                                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                                    }
                                }
                            }
                        }
                    }
        } catch (e: Exception) {
            e.printStackTrace()
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
            Log.e("file select camera=", "${attachedImage} ${attachedImagePath}")

            imagesList.clear()
            attachedImagePath?.let {

                imagesList.add(it)

            }

            imagesAdapter?.addItem(imagesList)


        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun resetView() {
        imagesList.clear()
        images.clear()
        edt_describe_request.setText("")
        attachedImagePath = null
        attachedImage = null
        imagesAdapter?.removeAll()
    }

}
