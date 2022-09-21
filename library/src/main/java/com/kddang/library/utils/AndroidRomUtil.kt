package com.kddang.library.utils

import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


/**
 * 判断国产rom
 *
 */
object AndroidRomUtil {
    private const val KEY_EMUI_VERSION_CODE = "ro.build.version.emui"
    private const val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
    private const val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
    private const val KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage"

    /**
     * 华为rom
     *
     * @return
     */
    val isEMUI: Boolean
        get() = try {
            val prop = BuildProperties.newInstance()
            prop.getProperty(KEY_EMUI_VERSION_CODE, null) != null
        } catch (e: IOException) {
            false
        }/*String rom = "" + prop.getProperty(KEY_MIUI_VERSION_CODE, null) + prop.getProperty(KEY_MIUI_VERSION_NAME, null)+prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null);
            Log.d("Android_Rom", rom);*/

    /**
     * 小米rom
     *
     * @return
     */
    val isMIUI: Boolean
        get() {
            return try {
                val prop = BuildProperties.newInstance()
                /*String rom = "" + prop.getProperty(KEY_MIUI_VERSION_CODE, null) + prop.getProperty(KEY_MIUI_VERSION_NAME, null)+prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null);
                     Log.d("Android_Rom", rom);*/prop.getProperty(
                    KEY_MIUI_VERSION_CODE,
                    null
                ) != null || prop.getProperty(
                    KEY_MIUI_VERSION_NAME, null
                ) != null || prop.getProperty(
                    KEY_MIUI_INTERNAL_STORAGE, null
                ) != null
            } catch (e: IOException) {
                false
            }
        }

    /**
     * 魅族rom
     *
     * @return
     */
    val isFlyme: Boolean
        get() {
            return try {
                val method = Build::class.java.getMethod("hasSmartBar")
                method != null
            } catch (e: Exception) {
                false
            }
        }

    class BuildProperties private constructor() {
        private val properties: Properties = Properties()
        fun containsKey(key: Any): Boolean {
            return properties.containsKey(key)
        }

        fun containsValue(value: Any): Boolean {
            return properties.containsValue(value)
        }

        fun entrySet(): Set<Map.Entry<Any, Any>> {
            return properties.entries
        }

        fun getProperty(name: String?): String {
            return properties.getProperty(name)
        }

        fun getProperty(name: String?, defaultValue: String?): String? {
            return properties.getProperty(name, defaultValue)
        }

        val isEmpty: Boolean
            get() = properties.isEmpty

        fun keys(): Enumeration<Any> {
            return properties.keys()
        }

        fun keySet(): Set<Any> {
            return properties.keys
        }

        fun size(): Int {
            return properties.size
        }

        fun values(): Collection<Any> {
            return properties.values
        }

        companion object {
            @Throws(IOException::class)
            fun newInstance(): BuildProperties {
                return BuildProperties()
            }
        }

        init {
            properties.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
        }
    }
}