package com.kddang.library.common

import android.content.Context
import android.widget.ImageView

interface ImageLoader {

    fun displayImage(context: Context?, path: String?, imageView: ImageView?)
}