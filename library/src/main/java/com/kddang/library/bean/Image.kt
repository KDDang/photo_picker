package com.kddang.library.bean


data class Image(var path: String = "", var name: String = ""){

    override fun equals(o: Any?): Boolean {
        try {
            val other = o as Image
            return path.equals(other.path, ignoreCase = true)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return super.equals(o)
    }
}