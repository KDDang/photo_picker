package com.kddang.library.bean



data class Folder(var name: String = "", var path: String = "", var cover: Image? = null, var images: MutableList<Image> = mutableListOf()) {

    override fun equals(o: Any?): Boolean {
        try {
            val other = o as Folder
            return path.equals(other.path, ignoreCase = true)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return super.equals(o)
    }
}