package com.officinetop.officine.userprofile

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.officinetop.officine.BaseActivity
import com.officinetop.officine.R
import com.officinetop.officine.data.getBearerToken
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.OutputStream
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class Edit_Profile : BaseActivity() {
    private lateinit var profile_imagefull: ImageView
    private lateinit var edittext_mobile: TextView
    private lateinit var edittext_email: TextView
    private lateinit var textview_changepic: TextView
    private val GALLERY = 1
    private val CAMERA = 2
    private var attachedImage: File? = null
    private var updateimage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.edit_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        profile_imagefull = findViewById(R.id.profile_imagefull)
        edittext_mobile = findViewById(R.id.EditText_mobile)
        edittext_email = findViewById(R.id.EditText_email)
        textview_changepic = findViewById(R.id.textview_changepic)

        if (intent.hasExtra("Email")) {
            val user_email = intent?.getStringExtra("Email") ?: ""
            val user_mobile = intent?.getStringExtra("Mobile") ?: ""
            val user_picurl = intent?.getStringExtra("Pic_url") ?: ""
            val userName = intent?.getStringExtra("user_name") ?: ""

            edittext_mobile.text = user_mobile
            edittext_email.text = user_email
            ed_userName.setText(userName)
            ed_userName.requestFocus()
            loadImageprofile(user_picurl, profile_imagefull)
        }

        textview_changepic.setOnClickListener(View.OnClickListener {
            var permissionlist = ArrayList<String>()

            permissionlist.add(Manifest.permission.CAMERA)

            checkpermission(permissionlist, { showPictureDialog() })
        })

        button_updateprofile.setOnClickListener(View.OnClickListener {

            if (edittext_mobile.text.isEmpty()) {
                Toast.makeText(this@Edit_Profile, getString(R.string.mobileblank), Toast.LENGTH_LONG).show()
            } else if (edittext_email.text.isEmpty()) {
                Toast.makeText(this@Edit_Profile, getString(R.string.emailblank), Toast.LENGTH_LONG).show()
            } else {
                val imagedata = attachedImage?.toMultipartBody("profile_pic")
                val emailid = edittext_email.text.toString()
                val mobileno = edittext_mobile.text.toString()
                updateuserprofile(emailid, mobileno, imagedata, ed_userName.text.toString())
            }

        })

    }

    private fun updateuserprofile(emailid: String, mobileno: String, imagedata: MultipartBody.Part?, User_name: String) {
        try {
            val progressDialog = this.getProgressDialog()
            progressDialog.show()
            if (!showOnlineSnack(progressDialog))
                return
            RetrofitClient.client.updateprofile(authToken = getBearerToken()
                    ?: "", email = emailid.toRequestBody(), mobile = mobileno.toRequestBody(), profile_pic = imagedata, User_name = User_name.toRequestBody(), last_name = "".toRequestBody())
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            progressDialog.dismiss()
                            toast(t.message!!)
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val body = response.body()?.string()
                            progressDialog.dismiss()
                            Log.d("updateprofile", "onResponse: profile = $body")
                            if (response.isSuccessful) {
                                try {
                                    val jsonObject = JSONObject(body)
                                    val status_code = jsonObject.optString("status_code")
                                    val msg = jsonObject.optString("message")
                                    if (status_code == "1") {
                                        alert {
                                            message = msg
                                            positiveButton(getString(R.string.ok)) {
                                                finish()
                                            }
                                        }.show()
                                    } else {
                                        alert {
                                            message = msg
                                            positiveButton(getString(R.string.retry)) {

                                            }
                                            negativeButton(getString(R.string.ok)) {
                                                finish()
                                            }
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

    private fun showOnlineSnack(progressDialog: ProgressDialog?): Boolean {
        val view = home_bottom_navigation_view
        if (!this.isOnline()) {
            Snackbar.make(view, getString(R.string.ConnectionErrorPleaseretry), Snackbar.LENGTH_LONG)
                    .show()
            progressDialog?.dismiss()
        }
        return this.isOnline()
    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(getString(R.string.SelectAction))
        val pictureDialogItems = arrayOf(getString(R.string.Selectphotofromgallery), getString(R.string.Capturephotofromcamera))
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    profile_imagefull.setImageBitmap(bitmap)
                    convertTofile(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@Edit_Profile, getString(R.string.Failed), Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            profile_imagefull.setImageBitmap(thumbnail)
            convertTofile(thumbnail)
        }
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
            Log.e("file select camera=", "${attachedImage}")
            updateimage = true
            //val file = File(images.get(i))

            //imageList.add(attachedImage?.toMultipartBodyImages("images[]"))


        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }


    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
                (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdirs()
        }

        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                    .timeInMillis).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                    arrayOf(f.path),
                    arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/demonuts"
    }
}
