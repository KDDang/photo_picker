package com.kddang.library.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.kddang.library.R


/**
 * Translucent Bars Utils
 *
 * @author yuyh.
 * @date 17/2/8.
 */
object StatusBarCompat {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun compat(activity: Activity, statusColor: Int, isDarkStyle: Boolean): View? {
        val color: Int = ContextCompat.getColor(activity, R.color.colorPrimary)
        if (color == statusColor) {
            compatTransStatusBar(activity, Color.TRANSPARENT, isDarkStyle)
        } else {
            compatTransStatusBar(activity, statusColor, isDarkStyle)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
            var statusBarView = contentView.getChildAt(0)
            if (statusBarView != null && statusBarView.measuredHeight == getStatusBarHeight(activity)) {
                statusBarView.setBackgroundColor(statusColor)
                return statusBarView
            }
            statusBarView = View(activity)
            val lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity)
            )
            statusBarView.setBackgroundColor(color)
            contentView.addView(statusBarView, lp)

            //activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            return statusBarView
        }
        return null
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun compatTransStatusBar(activity: Activity, color: Int, isDarkStyle: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = activity.window.decorView
            val option: Int = if (isDarkStyle) {
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            } else {
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            }
            decorView.systemUiVisibility = option
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 适配华为EMUI 沉浸式状态栏
                if (AndroidRomUtil.isEMUI) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                } else {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                }
                activity.window.statusBarColor = color // Color.parseColor("#33333333")
            } else {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}