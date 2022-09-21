package com.kddang.library.common

import com.kddang.library.bean.Image

interface OnItemClickListener {

    fun onCheckedClick(position: Int, image: Image): Int

    fun onImageClick(position: Int, image: Image)
}