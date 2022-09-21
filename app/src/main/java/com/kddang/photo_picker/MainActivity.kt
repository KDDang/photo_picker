package com.kddang.photo_picker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import coil.load
import com.kddang.library.ISNav
import com.kddang.library.common.ImageLoader
import com.kddang.library.config.ISCameraConfig
import com.kddang.library.config.ISListConfig

class MainActivity : AppCompatActivity() {

    companion object{
        private const val REQUEST_LIST_CODE = 0
        private const val REQUEST_CAMERA_CODE = 1
    }

    private lateinit var btnOpenAlbum: AppCompatButton
    private lateinit var btnOpenPhoto: AppCompatButton
    private lateinit var img: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenAlbum = findViewById(R.id.btn_open_album)
        btnOpenPhoto = findViewById(R.id.btn_open_photo)
        img = findViewById(R.id.img)

        initData()

        btnOpenAlbum.setOnClickListener {
            multiselect()
        }

        btnOpenPhoto.setOnClickListener {
            multiselectPhoto()
        }
    }

    fun initData(){
        ISNav.instance.init(object : ImageLoader {
            override fun displayImage(context: Context?, path: String?, imageView: ImageView?) {
                imageView?.load(path)
            }
        })
    }

    fun multiselect(){
        val config: ISListConfig = ISListConfig.Builder().apply {
            multiSelect = false
            rememberSelected = false
            statusBarColor = Color.parseColor("#3F51B5")
            needCrop = true
            cropSize(1,1, 200, 200)
        }.build()
        ISNav.instance.toListActivity(this, config, REQUEST_LIST_CODE)
    }

    fun multiselectPhoto(){
        val config: ISCameraConfig = ISCameraConfig.Builder().apply {
            needCrop = true
            cropSize(1,1, 200, 200)
        }.build()
        ISNav.instance.toCameraActivity(this, config, REQUEST_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            val pathList: List<String>? = data.getStringArrayListExtra("result")

            img.load(pathList?.get(0))
            // 测试Fresco
            // draweeView.setImageURI(Uri.parse("file://"+pathList.get(0)));

        } else if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            val path = data.getStringExtra("result")
            img.load(path)
        }
    }
}