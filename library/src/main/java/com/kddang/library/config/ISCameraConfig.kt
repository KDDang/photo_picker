package com.kddang.library.config

import android.os.Environment
import com.kddang.library.utils.FileUtils
import java.io.Serializable

class ISCameraConfig(builder: Builder) : Serializable {
    /**
     * 是否需要裁剪
     */
    var needCrop: Boolean

    /**
     * 拍照存储路径
     */
    var filePath: String?

    /**
     * 裁剪输出大小
     */
    var aspectX = 1
    var aspectY = 1
    var outputX = 500
    var outputY = 500

    class Builder : Serializable {
        var needCrop = false
        var filePath: String? = null
        var aspectX = 1
        var aspectY = 1
        var outputX = 400
        var outputY = 400
        fun needCrop(needCrop: Boolean): Builder {
            this.needCrop = needCrop
            return this
        }

        private fun filePath(filePath: String): Builder {
            this.filePath = filePath
            return this
        }

        fun cropSize(aspectX: Int, aspectY: Int, outputX: Int, outputY: Int): Builder {
            this.aspectX = aspectX
            this.aspectY = aspectY
            this.outputX = outputX
            this.outputY = outputY
            return this
        }

        fun build(): ISCameraConfig {
            return ISCameraConfig(this)
        }

        init {
            filePath =
                if (FileUtils.isSdCardAvailable()) Environment.getExternalStorageDirectory().absolutePath + "/Camera" else Environment.getRootDirectory().absolutePath + "/Camera"
            filePath?.let {
                FileUtils.createDir(it)
            }
        }
    }

    init {
        needCrop = builder.needCrop
        filePath = builder.filePath
        aspectX = builder.aspectX
        aspectY = builder.aspectY
        outputX = builder.outputX
        outputY = builder.outputY
    }
}