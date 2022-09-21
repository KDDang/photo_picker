package com.kddang.library.utils

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Log工具类，可控制Log输出开关、保存Log到文件、过滤输出等级
 *
 * @author yuyh.
 * @date 16/4/9.
 */
object LogUtils {
    private const val LOG_SWITCH = true // 日志文件总开关
    private const val LOG_TO_FILE = false // 日志写入文件开关
    private const val LOG_TAG = "TAG" // 默认的tag
    private const val LOG_TYPE = 'v' // 输入日志类型，v代表输出所有信息,w则只输出警告...
    private const val LOG_SAVE_DAYS = 7 // sd卡中日志文件的最多保存天数
    private val LOG_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 日志的输出格式
    private val FILE_SUFFIX = SimpleDateFormat("yyyy-MM-dd") // 日志文件格式
    private var LOG_FILE_PATH // 日志文件保存路径
            : String? = null
    private var LOG_FILE_NAME // 日志文件保存名称
            : String? = null

    fun init(context: Context?) { // 在Application中初始化
        LOG_FILE_PATH = FileUtils.createRootPath(context!!)
        LOG_FILE_NAME = "Log"
    }

    /**************************** Warn  */
    fun w(msg: Any) {
        w(LOG_TAG, msg)
    }

    @JvmOverloads
    fun w(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'w')
    }

    /*************************** Error  */
    fun e(msg: Any) {
        e(LOG_TAG, msg)
    }

    @JvmOverloads
    fun e(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'e')
    }

    /*************************** Debug  */
    fun d(msg: Any) {
        d(LOG_TAG, msg)
    }

    @JvmOverloads
    fun d(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'd')
    }

    /**************************** Info  */
    fun i(msg: Any) {
        i(LOG_TAG, msg)
    }

    @JvmOverloads
    fun i(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'i')
    }

    /************************** Verbose  */
    fun v(msg: Any) {
        v(LOG_TAG, msg)
    }

    @JvmOverloads
    fun v(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'v')
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     */
    private fun log(tag: String, msg: String, tr: Throwable?, level: Char) {
        if (LOG_SWITCH) {
            if ('e' == level && ('e' == LOG_TYPE || 'v' == LOG_TYPE)) { // 输出错误信息
                Log.e(tag, msg, tr)
            } else if ('w' == level && ('w' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.w(tag, msg, tr)
            } else if ('d' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.d(tag, msg, tr)
            } else if ('i' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.i(tag, msg, tr)
            } else {
                Log.v(tag, msg, tr)
            }
            if (LOG_TO_FILE) log2File(
                level.toString(), tag, if (msg + tr == null) "" else """
     
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
    }

    /**
     * 打开日志文件并写入日志
     *
     * @return
     */
    @Synchronized
    private fun log2File(mylogtype: String, tag: String, text: String) {
        val nowtime = Date()
        val date = FILE_SUFFIX.format(nowtime)
        val dateLogContent =
            LOG_FORMAT.format(nowtime) + ":" + mylogtype + ":" + tag + ":" + text // 日志输出格式
        val destDir = File(LOG_FILE_PATH)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        val file = File(LOG_FILE_PATH, LOG_FILE_NAME + date)
        try {
            val filerWriter = FileWriter(file, true)
            val bufWriter = BufferedWriter(filerWriter)
            bufWriter.write(dateLogContent)
            bufWriter.newLine()
            bufWriter.close()
            filerWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 删除指定的日志文件
     */
    fun delFile() { // 删除日志文件
        val needDelFiel = FILE_SUFFIX.format(dateBefore)
        val file = File(LOG_FILE_PATH, needDelFiel + LOG_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * 得到LOG_SAVE_DAYS天前的日期
     *
     * @return
     */
    private val dateBefore: Date
        private get() {
            val nowtime = Date()
            val now = Calendar.getInstance()
            now.time = nowtime
            now[Calendar.DATE] = now[Calendar.DATE] - LOG_SAVE_DAYS
            return now.time
        }
}