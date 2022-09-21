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
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kddang.library.R
import com.kddang.library.common.Callback
import com.kddang.library.common.Constant
import com.kddang.library.config.ISListConfig
import com.kddang.library.ui.fragment.ImgSelFragment
import com.kddang.library.utils.FileUtils
import com.kddang.library.utils.StatusBarCompat
import java.io.File

class ISListActivity: AppCompatActivity(), View.OnClickListener, Callback {

    companion object{
        const val INTENT_RESULT = "result"
        const val IMAGE_CROP_CODE = 1
        const val STORAGE_REQUEST_CODE = 1

        fun startForResult(activity: Activity, config: ISListConfig, RequestCode: Int) {
            val intent = Intent(activity, ISListActivity::class.java)
            intent.putExtra("config", config)
            activity.startActivityForResult(intent, RequestCode)
        }

        fun startForResult(fragment: Fragment, config: ISListConfig, RequestCode: Int) {
            val intent = Intent(fragment.activity, ISListActivity::class.java)
            intent.putExtra("config", config)
            fragment.startActivityForResult(intent, RequestCode)
        }
    }

    private lateinit var config: ISListConfig

    private lateinit var rlTitleBar: RelativeLayout
    private lateinit var tvTitle: TextView
    private lateinit var btnConfirm: Button
    private lateinit var ivBack: ImageView

    private var cropImagePath: String = ""
    private var fragment: ImgSelFragment? = null
    private val result = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_sel)
        config = intent.getSerializableExtra("config") as ISListConfig

        // Android 6.0 checkSelfPermission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_REQUEST_CODE
            )
        } else {
            fragment = ImgSelFragment.instance()
            supportFragmentManager.beginTransaction()
                .add(R.id.fmImageList, fragment!!, null)
                .commit()
        }
        initView()
        if (!FileUtils.isSdCardAvailable()) {
            Toast.makeText(this, getString(R.string.sd_disable), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        rlTitleBar = findViewById(R.id.rlTitleBar)
        tvTitle = findViewById(R.id.tvTitle)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnConfirm.setOnClickListener(this)
        ivBack = findViewById(R.id.ivBack)
        ivBack.setOnClickListener(this)
        config?.let{ config ->
            if (config.backResId !== -1) {
                ivBack.setImageResource(config.backResId)
            }
            if (config.statusBarColor !== -1) {
                StatusBarCompat.compat(this, config.statusBarColor, config.isDark)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                ) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            }
            rlTitleBar.setBackgroundColor(config.titleBgColor)
            tvTitle.setTextColor(config.titleColor)
            tvTitle.text = config.title
            btnConfirm.setBackgroundColor(config.btnBgColor)
            btnConfirm.setTextColor(config.btnTextColor)
            if (config.multiSelect) {
                if (!config.rememberSelected) {
                    Constant.imageList.clear()
                }
                btnConfirm.text = java.lang.String.format(
                    getString(R.string.confirm_format),
                    config.btnText,
                    Constant.imageList.size,
                    config.maxNum
                )
            } else {
                Constant.imageList.clear()
                btnConfirm.setVisibility(View.GONE)
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btnConfirm) {
            if (Constant.imageList != null && Constant.imageList.isNotEmpty()) {
                exit()
            } else {
                Toast.makeText(this, getString(R.string.minnum), Toast.LENGTH_SHORT).show()
            }
        } else if (id == R.id.ivBack) {
            onBackPressed()
        }
    }

    override fun onSingleImageSelected(path: String) {
        if (config!!.needCrop) {
            crop(path)
        } else {
            Constant.imageList.add(path)
            exit()
        }
    }

    override fun onImageSelected(path: String) {
        btnConfirm.text = java.lang.String.format(
            getString(R.string.confirm_format),
            config!!.btnText,
            Constant.imageList.size,
            config!!.maxNum
        )
    }

    override fun onImageUnselected(path: String) {
        config?.let {
            btnConfirm.text = java.lang.String.format(
                getString(R.string.confirm_format),
                it.btnText,
                Constant.imageList.size,
                it.maxNum
            )
        }

    }

    override fun onCameraShot(imageFile: File) {
        config?.let {
            if (it.needCrop) {
                crop(imageFile.absolutePath)
            } else {
                Constant.imageList.add(imageFile.absolutePath)
                it.multiSelect = false // 多选点击拍照，强制更改为单选
                exit()
            }
        }
    }

    override fun onPreviewChanged(select: Int, sum: Int, visible: Boolean) {
        if (visible) {
            tvTitle.text = "$select/$sum"
        } else {
            tvTitle.text = config!!.title
        }
    }

    private fun crop(imagePath: String) {
        val file: File = File(
            FileUtils.createRootPath(this).toString() + "/" + System.currentTimeMillis() + ".jpg"
        )
        cropImagePath = file.absolutePath
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(getImageContentUri(File(imagePath)), "image/*")
        intent.putExtra("crop", "true")
        config?.let {config ->
            intent.putExtra("aspectX", config.aspectX)
            intent.putExtra("aspectY", config.aspectY)
            intent.putExtra("outputX", config.outputX)
            intent.putExtra("outputY", config.outputY)
        }
        intent.putExtra("scale", true)
        intent.putExtra("scaleUpIfNeeded", true)
        intent.putExtra("return-data", false)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)
        startActivityForResult(intent, IMAGE_CROP_CODE)
    }

    private fun getImageContentUri(imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val column = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
            val id = cursor.getInt(column)
            val baseUri = Uri.parse("content://media/external/images/media")
            Uri.withAppendedPath(baseUri, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                null
            }
        }
    }

    fun getConfig(): ISListConfig {
        return config
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            Constant.imageList.add(cropImagePath)
            config?.multiSelect = false // 多选点击拍照，强制更改为单选
            exit()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun exit() {
        val intent = Intent()
        result.clear()
        result.addAll(Constant.imageList)
        intent.putStringArrayListExtra(INTENT_RESULT, result)
        setResult(RESULT_OK, intent)
        if (!config!!.multiSelect) {
            Constant.imageList.clear()
        }
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        when (requestCode) {
            STORAGE_REQUEST_CODE -> if (grantResults.size >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fmImageList, ImgSelFragment.instance(), null)
                    .commitAllowingStateLoss()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_storage_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("config", config)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        config = savedInstanceState.getSerializable("config") as ISListConfig
    }

    override fun onBackPressed() {
        if (fragment == null || !fragment!!.hidePreview()) {
            Constant.imageList.clear()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}