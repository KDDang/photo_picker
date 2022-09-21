package com.kddang.library

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.kddang.library.common.ImageLoader
import com.kddang.library.config.ISCameraConfig
import com.kddang.library.config.ISListConfig
import com.kddang.library.ui.ISCameraActivity
import com.kddang.library.ui.ISListActivity.Companion.startForResult

class ISNav {

    companion object {
        val instance by lazy (LazyThreadSafetyMode.SYNCHRONIZED){
            ISNav()
        }
    }

    private var loader: ImageLoader? = null

    /**
     * 图片加载必须先初始化
     *
     * @param loader
     */
    fun init(@NonNull loader: ImageLoader?) {
        this.loader = loader
    }

    fun displayImage(context: Context?, path: String?, imageView: ImageView?) {
        if (loader != null) {
            loader!!.displayImage(context, path, imageView)
        }
    }

    fun toListActivity(source: Any, config: ISListConfig, reqCode: Int) {
        if (source is Activity) {
            startForResult(source, config, reqCode)
        } else if (source is Fragment) {
            startForResult(source as Fragment, config, reqCode)
        }
    }

    fun toCameraActivity(source: Any?, config: ISCameraConfig, reqCode: Int) {
        if (source is Activity) {
            ISCameraActivity.startForResult(source, config, reqCode)
        } else if (source is Fragment) {
            ISCameraActivity.startForResult(source, config, reqCode)
        } else if (source is Fragment) {
            ISCameraActivity.startForResult(source, config, reqCode)
        }
    }
}