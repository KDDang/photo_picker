package com.kddang.library.config

import android.graphics.Color
import android.os.Environment
import com.kddang.library.utils.FileUtils
import java.io.Serializable

class ISListConfig : Serializable{
    /**
     * 是否需要裁剪
     */
    var needCrop = false

    /**
     * 是否多选
     */
    var multiSelect = false

    /**
     * 是否记住上次的选中记录(只对多选有效)
     */
    var rememberSelected = true

    /**
     * 最多选择图片数
     */
    var maxNum = 9

    /**
     * 第一个item是否显示相机
     */
    var needCamera = false

    var statusBarColor = -1

    /**
     * 状态栏字体颜色
     */
    var isDark = false

    /**
     * 返回图标资源
     */
    var backResId = -1

    /**
     * 标题
     */
    var title: String? = null

    /**
     * 标题颜色
     */
    var titleColor = 0

    /**
     * titlebar背景色
     */
    var titleBgColor = 0

    var btnText: String? = null

    /**
     * 确定按钮文字颜色
     */
    var btnTextColor = 0

    /**
     * 确定按钮背景色
     */
    var btnBgColor = 0

    var allImagesText: String? = null

    /**
     * 拍照存储路径
     */
    var filePath: String? = null

    /**
     * 裁剪输出大小
     */
    var aspectX = 1
    var aspectY = 1
    var outputX = 500
    var outputY = 500

    private constructor(builder: Builder) {
        needCrop = builder.needCrop
        multiSelect = builder.multiSelect
        rememberSelected = builder.rememberSelected
        maxNum = builder.maxNum
        needCamera = builder.needCamera
        statusBarColor = builder.statusBarColor
        isDark = builder.isDark
        backResId = builder.backResId
        title = builder.title
        titleBgColor = builder.titleBgColor
        titleColor = builder.titleColor
        btnText = builder.btnText
        btnBgColor = builder.btnBgColor
        btnTextColor = builder.btnTextColor
        allImagesText = builder.allImagesText
        filePath = builder.filePath
        aspectX = builder.aspectX
        aspectY = builder.aspectY
        outputX = builder.outputX
        outputY = builder.outputY
    }

    class Builder : Serializable {
        var needCrop = false
        var multiSelect = true
        var rememberSelected = true
        var maxNum = 9
        var needCamera = true
        var statusBarColor = -1
        var isDark = true
        var backResId = -1
        var title: String
        var titleColor: Int
        var titleBgColor: Int
        var btnText: String
        var btnTextColor: Int
        var btnBgColor: Int
        var allImagesText: String
        var filePath: String? = null
        var aspectX = 1
        var aspectY = 1
        var outputX = 400
        var outputY = 400
        fun needCrop(needCrop: Boolean): Builder {
            this.needCrop = needCrop
            return this
        }

        fun multiSelect(multiSelect: Boolean): Builder {
            this.multiSelect = multiSelect
            return this
        }

        fun rememberSelected(rememberSelected: Boolean): Builder {
            this.rememberSelected = rememberSelected
            return this
        }

        fun maxNum(maxNum: Int): Builder {
            this.maxNum = maxNum
            return this
        }

        fun needCamera(needCamera: Boolean): Builder {
            this.needCamera = needCamera
            return this
        }

        fun statusBarColor(statusBarColor: Int): Builder {
            this.statusBarColor = statusBarColor
            return this
        }

        fun isDarkStatusStyle(isDark: Boolean): Builder {
            this.isDark = isDark
            return this
        }

        fun backResId(backResId: Int): Builder {
            this.backResId = backResId
            return this
        }

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun titleColor(titleColor: Int): Builder {
            this.titleColor = titleColor
            return this
        }

        fun titleBgColor(titleBgColor: Int): Builder {
            this.titleBgColor = titleBgColor
            return this
        }

        fun btnText(btnText: String): Builder {
            this.btnText = btnText
            return this
        }

        fun btnTextColor(btnTextColor: Int): Builder {
            this.btnTextColor = btnTextColor
            return this
        }

        fun btnBgColor(btnBgColor: Int): Builder {
            this.btnBgColor = btnBgColor
            return this
        }

        fun allImagesText(allImagesText: String): Builder {
            this.allImagesText = allImagesText
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

        fun build(): ISListConfig {
            return ISListConfig(this)
        }


        init {
            filePath =
                if (FileUtils.isSdCardAvailable()) Environment.getExternalStorageDirectory().absolutePath + "/Camera" else Environment.getRootDirectory().absolutePath + "/Camera"
            title = "照片"
            titleBgColor = Color.parseColor("#3F51B5")
            titleColor = Color.WHITE
            btnText = "确定"
            btnBgColor = Color.TRANSPARENT
            btnTextColor = Color.WHITE
            allImagesText = "所有图片"
            FileUtils.createDir(filePath!!)
        }
    }
}
