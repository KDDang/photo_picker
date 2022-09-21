package com.kddang.library.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.kddang.library.R
import com.kddang.library.bean.Image
import com.kddang.library.config.ISCameraConfig
import com.kddang.library.utils.FileUtils
import com.kddang.library.utils.LogUtils
import com.kddang.library.utils.StatusBarCompat
import java.io.File

class ISCameraActivity: AppCompatActivity() {

    companion object{
        fun startForResult(activity: Activity, config: ISCameraConfig, requestCode: Int) {
            val intent = Intent(activity, ISCameraActivity::class.java)
            intent.putExtra("config", config)
            activity.startActivityForResult(intent, requestCode)
        }

//        fun startForResult(fragment: Fragment, config: ISCameraConfig, requestCode: Int) {
//            val intent = Intent(fragment.getActivity(), ISCameraActivity::class.java)
//            Intent.putExtra("config", config)
//            fragment.startActivityForResult(intent, requestCode)
//        }

        fun startForResult(fragment: Fragment, config: ISCameraConfig, requestCode: Int) {
            val intent = Intent(fragment.activity, ISCameraActivity::class.java)
            intent.putExtra("config", config)
            fragment.startActivityForResult(intent, requestCode)
        }
    }


    private val REQUEST_CAMERA = 5
    private val IMAGE_CROP_CODE = 1
    private val CAMERA_REQUEST_CODE = 2

    private var cropImageFile: File? = null
    private var tempPhotoFile: File? = null

    private var config: ISCameraConfig? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        StatusBarCompat.compatTransStatusBar(this, 0x33333333, true)
        super.onCreate(savedInstanceState)
        config = intent.getSerializableExtra("config") as ISCameraConfig?
        if (config == null) return
        camera()
    }

    private fun camera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), CAMERA_REQUEST_CODE
                )
            }
            return
        }
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            tempPhotoFile = File(
                FileUtils.createRootPath(this)
                    .toString() + "/" + System.currentTimeMillis() + ".jpg"
            )
            LogUtils.e(tempPhotoFile!!.absolutePath)
            FileUtils.createFile(tempPhotoFile!!)
            val uri: Uri = FileProvider.getUriForFile(
                this,
                FileUtils.getApplicationId(this).toString() + ".image_provider",
                tempPhotoFile!!
            )
            val resInfoList = packageManager.queryIntentActivities(
                cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri) //Uri.fromFile(tempFile)
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.open_camera_failure),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun crop(imagePath: String) {
        cropImageFile = File(
            FileUtils.createRootPath(this).toString() + "/" + System.currentTimeMillis() + ".jpg"
        )
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(getImageContentUri(File(imagePath)), "image/*")
        intent.putExtra("crop", "true")
        config?.let { config ->
            intent.putExtra("aspectX", config.aspectX)
            intent.putExtra("aspectY", config.aspectY)
            intent.putExtra("outputX", config.outputX)
            intent.putExtra("outputY", config.outputY)
        }
        intent.putExtra("scale", true)
        intent.putExtra("scaleUpIfNeeded", true)
        intent.putExtra("return-data", false)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropImageFile))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)
        startActivityForResult(intent, IMAGE_CROP_CODE)
    }

    fun getImageContentUri(imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val column = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
            val id = cursor.getInt(column)
            val baseUri = Uri.parse("content://media/external/images/media")
            cursor.close()
            Uri.withAppendedPath(baseUri, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                cursor?.close()
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } else {
                null
            }
        }
    }

    private fun complete(image: Image?) {
        val intent = Intent()
        if (image != null) {
            intent.putExtra("result", image.path)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            complete(Image(cropImageFile!!.path, cropImageFile!!.name))
        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                if (tempPhotoFile != null) {
                    if (config!!.needCrop) {
                        crop(tempPhotoFile!!.absolutePath)
                    } else {
                        // complete(new Image(cropImageFile.getPath(), cropImageFile.getName()));
                        complete(Image(tempPhotoFile!!.path, tempPhotoFile!!.name))
                    }
                }
            } else {
                if (tempPhotoFile != null && tempPhotoFile!!.exists()) {
                    tempPhotoFile!!.delete()
                }
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.size >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                camera()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permission_camera_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }
}