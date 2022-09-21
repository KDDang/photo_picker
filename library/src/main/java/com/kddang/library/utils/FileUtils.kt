package com.kddang.library.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import java.io.File

object FileUtils {
    private val TAG: String = FileUtils::class.java.simpleName

    /**
     * 创建根缓存目录
     *
     * @return
     */
    fun createRootPath(context: Context): String {
        var cacheRootPath = ""
        cacheRootPath = if (Build.VERSION.SDK_INT <= 28) {
            if (isSdCardAvailable()) {
                context.externalCacheDir!!.path
            } else {
                context.cacheDir.path
            }
        } else {
            val medias = context.externalMediaDirs
            if (medias != null && medias.isNotEmpty()) {
                medias[0].path
            } else {
                Environment.getExternalStorageDirectory().path + "/Android/media/temp"
            }
        }
        createDir(cacheRootPath)
        return cacheRootPath
    }

    fun isSdCardAvailable(): Boolean{
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    /**
     * 递归创建文件夹
     *
     * @param dirPath
     * @return 创建失败返回""
     */
    fun createDir(dirPath: String): String {
        try {
            val file = File(dirPath)
            if (file.parentFile.exists()) {
                LogUtils.i("----- 创建文件夹" + file.absolutePath)
                file.mkdir()
                return file.absolutePath
            } else {
                createDir(file.parentFile.absolutePath)
                LogUtils.i("----- 创建文件夹" + file.absolutePath)
                file.mkdir()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dirPath
    }

    /**
     * 递归创建文件夹
     *
     * @param file
     * @return 创建失败返回""
     */
    fun createFile(file: File): String {
        try {
            if (file.parentFile.exists()) {
                LogUtils.i("----- 创建文件" + file.absolutePath)
                file.createNewFile()
                return file.absolutePath
            } else {
                createDir(file.parentFile.absolutePath)
                file.createNewFile()
                LogUtils.i("----- 创建文件" + file.absolutePath)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @Throws(IllegalArgumentException::class)
    fun getApplicationId(appContext: Context): String? {
        var applicationInfo: ApplicationInfo? = null
        return try {
            applicationInfo = appContext.packageManager.getApplicationInfo(
                appContext.packageName,
                PackageManager.GET_META_DATA
            )
            requireNotNull(applicationInfo) { " get application info = null, has no meta data! " }
            LogUtils.d(appContext.packageName + " " + applicationInfo.metaData.getString("APP_ID"))
            applicationInfo.metaData.getString("APP_ID")
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalArgumentException(" get application info error! ", e)
        }
    }
}