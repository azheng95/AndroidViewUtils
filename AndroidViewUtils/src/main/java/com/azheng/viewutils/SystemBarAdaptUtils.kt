package com.azheng.viewutils

import android.R
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.graphics.Point
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import java.lang.reflect.Method

/**
 * 系统栏（底部导航栏/状态栏）适配工具类
 * 兼容Android 16（API 16）及以上版本
 * 核心功能：解决布局与底部导航栏重叠问题
 */
object SystemBarAdaptUtils {
    // 用于标记View是否已适配，避免重复Padding叠加
    private const val TAG_ADAPTED = "tag_navigation_bar_adapted"
    /**
     * 新增核心方法：只适配底部导航栏（不处理顶部状态栏）
     * 解决布局顶到顶部的问题，仅添加底部导航栏的padding
     * 适配XPopup BottomPopupView 在addInnerContent()方法内调用
     * @param targetView 需要适配的目标View
     */
    fun adaptOnlyBottomNavigationBar(targetView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
            // 获取底部系统窗口内边距（如导航栏高度）
            val bottomInset = insets.systemWindowInsetBottom
            // 将LayoutParams强制转换为MarginLayoutParams（与原Java逻辑一致）
            val layoutParams = v.layoutParams as MarginLayoutParams
            // 设置View的margin（注意：原Java代码中误用了v.getRight()，正确应为v.getPaddingRight()，已修正）
            layoutParams.setMargins(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight, // 原Java代码是v.getRight()，这是错误的，已修正
                v.paddingBottom + bottomInset
            )
            // 返回Insets（符合接口要求）
            insets
        }
    }

    /**
     * 核心方法：适配单个View/ViewGroup（兼容API 16+）
     * 适配XPopup BottomPopupView
     * @param targetView 需要适配的目标View（如Activity根布局、Fragment根布局、自定义FrameLayout等）
     */
    fun adaptNavigationBar(targetView: View) {
        // 避免重复适配
        if (targetView.getTag(TAG_ADAPTED.hashCode()) as? Boolean == true) return

        val context = targetView.context
        // 1. 兼容API 16设置fitsSystemWindows（让系统自动避开系统栏）
        targetView.fitsSystemWindows = true

        // 2. 手动计算底部导航栏高度，添加Padding兜底（防止fitsSystemWindows失效）
        val navHeight = getNavigationBarHeight(context)
        if (navHeight > 0) {
            // 保留原有Padding，仅叠加导航栏高度的底部Padding
            targetView.setPadding(
                targetView.paddingLeft,
                targetView.paddingTop,
                targetView.paddingRight,
                targetView.paddingBottom + navHeight
            )
        }

        // 标记为已适配
        targetView.setTag(TAG_ADAPTED.hashCode(), true)
    }

    /**
     * 适配Activity的根布局（一键调用，兼容API 16+）
     * @param activity 目标Activity
     */
    fun adaptActivityRootView(activity: Activity) {
        // 安全获取Activity根布局（强转为ViewGroup，避免getChildAt调用问题）
        val contentContainer = activity.window.decorView.findViewById<ViewGroup>(R.id.content)
        val rootView = if (contentContainer.childCount > 0) {
            contentContainer.getChildAt(0)
        } else {
            contentContainer
        }
        rootView?.let { adaptNavigationBar(it) }
    }

    /**
     * 适配Fragment的根布局（一键调用，兼容API 16+）
     * @param fragment 目标Fragment
     */
    fun adaptFragmentRootView(fragment: Fragment) {
        val rootView = fragment.view ?: return
        adaptNavigationBar(rootView)
    }

    /**
     * 重置View的适配状态（如需动态重新适配时调用）
     * @param targetView 需要重置的View
     */
    fun resetAdaptState(targetView: View) {
        targetView.setTag(TAG_ADAPTED.hashCode(), false)
        // 可选：重置底部Padding（根据需求决定是否恢复原Padding）
        // targetView.setPadding(
        //     targetView.paddingLeft,
        //     targetView.paddingTop,
        //     targetView.paddingRight,
        //     targetView.paddingBottom - getNavigationBarHeight(targetView.context)
        // )
    }

    /**
     * 获取底部导航栏高度（兼容API 16+）
     * @param context 上下文
     * @return 导航栏高度（px），无导航栏则返回0
     */
    fun getNavigationBarHeight(context: Context): Int {
        var navHeight = 0
        val resources = context.resources
        // 获取Android系统内置的导航栏高度资源ID
        val navBarHeightId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

        if (navBarHeightId > 0 && isNavigationBarVisible(context)) {
            // 只有导航栏存在且可见时才返回高度
            navHeight = resources.getDimensionPixelSize(navBarHeightId)
        }
        return navHeight
    }

    /**
     * 判断设备是否显示底部导航栏（兼容API 16+）
     * @param context 上下文
     * @return true=有底部导航栏，false=无
     */
    private fun isNavigationBarVisible(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val normalSize = Point()
        val realSize = Point()

        // 获取不含导航栏的显示尺寸
        display.getSize(normalSize)

        // 获取含导航栏的真实尺寸（兼容API 16）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(realSize)
        } else {
            try {
                // API 16反射调用getRealSize方法（低版本兜底）
                val method: Method = Display::class.java.getMethod("getRealSize", Point::class.java)
                method.invoke(display, realSize)
            } catch (e: Exception) {
                // 反射失败则默认无导航栏
                realSize.set(normalSize.x, normalSize.y)
            }
        }

        // 真实高度 > 普通高度 → 存在底部导航栏
        return realSize.y > normalSize.y || realSize.x > normalSize.x
    }
}