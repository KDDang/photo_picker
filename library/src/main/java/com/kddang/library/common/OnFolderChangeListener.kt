package com.kddang.library.common

import com.kddang.library.bean.Folder

interface OnFolderChangeListener {

    fun onChange(position: Int, folder: Folder)
}