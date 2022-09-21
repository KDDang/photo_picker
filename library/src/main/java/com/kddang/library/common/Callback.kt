package com.kddang.library.common

import java.io.File
import java.io.Serializable

interface Callback : Serializable{

    fun onSingleImageSelected(path: String)

    fun onImageSelected(path: String)

    fun onImageUnselected(path: String)

    fun onCameraShot(imageFile: File)

    fun onPreviewChanged(select: Int, sum: Int, visible: Boolean)
}