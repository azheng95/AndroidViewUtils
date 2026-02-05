// EdgeToEdgeHelper.kt
package com.azheng.viewutils.edge

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Edge-to-Edge 核心工具类
 * 使用外观模式，封装复杂的适配逻辑
 * 场景	isLightStatusBar	isFitStatusBar	isFitNavigationBar	isFitIme
 * 默认白色主题	true	true	true	false
 * 深色主题	false	true	true	false
 * 图片头部沉浸	false	false	true	false
 * 列表页(自定义滚动)	true	true	false	false
 * 聊天输入页	true	true	true	true
 * 全屏视频/图片	false	false	false	false
 * 带底部Tab主页	true	true	false	false
 */
object EdgeToEdgeHelper {

    /**
     * 应用Edge-to-Edge配置
     * @param activity 目标Activity
     * @param config 配置参数
     * @param contentView 需要适配的内容View（可选，默认为DecorView的content）
     */
    fun apply(
        activity: Activity,
        config: EdgeToEdgeConfig = EdgeToEdgeConfig.default(),
        contentView: View? = null
    ) {
        if (!config.isEnabled) return
        applyWindow(activity, config)
        val targetView = contentView ?: activity.findViewById(android.R.id.content)
        applyInsets(targetView, config)
    }

    /**
     * 仅配置 Window 属性
     * - 透明状态栏/导航栏
     * - 系统栏颜色
     * - 图标颜色
     * - 刘海屏
     */
    fun applyWindow(activity: Activity, config: EdgeToEdgeConfig) {
        if (!config.isEnabled) return

        // 1. 启用 Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        // 2. 设置系统栏颜色
        applySystemBarColors(activity, config)

        // 3. 设置系统栏图标颜色
        applySystemBarAppearance(activity, config)

        // 4. 处理刘海屏
        if (config.fitDisplayCutout) {
            applyDisplayCutout(activity)
        }
    }

    /**
     * 仅配置 View 的 Insets
     * - 处理 padding/margin 避免重叠
     */
    fun applyInsets(view: View, config: EdgeToEdgeConfig) {
        if (!config.isEnabled) return
        if (!config.fitStatusBar && !config.fitNavigationBar && !config.fitIme) return

        WindowInsetsHelper.applyPaddingInsets(
            view = view,
            config = config,
            applyTop = config.fitStatusBar,
            applyBottom = config.fitNavigationBar || config.fitIme
        )
    }
    /**
     * 启用Edge-to-Edge模式
     */
    private fun enableEdgeToEdge(activity: Activity, config: EdgeToEdgeConfig) {
        // Android 15+ (API 35) 默认启用Edge-to-Edge
        if (Build.VERSION.SDK_INT >= 35) {
            // 系统自动启用，只需配置外观
            return
        }

        // 使用AndroidX库的方式（推荐）
        if (activity is ComponentActivity) {
            try {
                activity.enableEdgeToEdge()
                return
            } catch (e: Exception) {
                // 降级处理
            }
        }

        // 手动设置（兼容非ComponentActivity）
        val window = activity.window
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            window.setDecorFitsSystemWindows(false)
        } else {
            // Android 8-10
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        // 使用WindowCompat确保兼容性
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * 设置系统栏颜色
     */
    private fun applySystemBarColors(activity: Activity, config: EdgeToEdgeConfig) {
        val window = activity.window

        // 状态栏颜色
        window.statusBarColor = if (config.isStatusBarTransparent) {
            Color.TRANSPARENT
        } else {
            config.statusBarColor
        }

        // 导航栏颜色
        window.navigationBarColor = if (config.isNavigationBarTransparent) {
            Color.TRANSPARENT
        } else {
            config.navigationBarColor
        }

        // Android 10+ 移除导航栏分隔线
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Android 8.1+ 导航栏背景颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if (config.isNavigationBarTransparent) {
                window.navigationBarColor = Color.TRANSPARENT
            }
        }
    }

    /**
     * 设置系统栏外观（图标颜色）
     */
    private fun applySystemBarAppearance(activity: Activity, config: EdgeToEdgeConfig) {
        val windowInsetsController = WindowCompat.getInsetsController(
            activity.window,
            activity.window.decorView
        )

        // 状态栏图标颜色
        windowInsetsController.isAppearanceLightStatusBars = config.isLightStatusBar

        // 导航栏图标颜色（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowInsetsController.isAppearanceLightNavigationBars = config.isLightNavigationBar
        }
    }

    /**
     * 处理刘海屏适配
     */
    private fun applyDisplayCutout(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    /**
     * 处理WindowInsets
     */
    private fun applyWindowInsets(view: View, config: EdgeToEdgeConfig) {
        // 处理配置中指定的padding view
        config.paddingView?.let {
            WindowInsetsHelper.applyPaddingInsets(it, config)
        }

        // 处理配置中指定的margin view
        config.marginView?.let {
            WindowInsetsHelper.applyMarginInsets(it, config)
        }

        // 如果没有指定特定view，且需要适配，则对content view处理
        if (config.paddingView == null && config.marginView == null) {
            if (config.fitStatusBar || config.fitNavigationBar) {
                WindowInsetsHelper.applyPaddingInsets(view, config)
            }
        }
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(activity: Activity): Int {
        val windowInsets = ViewCompat.getRootWindowInsets(activity.window.decorView)
        return windowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    }

    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(activity: Activity): Int {
        val windowInsets = ViewCompat.getRootWindowInsets(activity.window.decorView)
        return windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
    }

    /**
     * 隐藏系统栏
     */
    fun hideSystemBars(activity: Activity, hideStatusBar: Boolean = true, hideNavBar: Boolean = true) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        
        var types = 0
        if (hideStatusBar) types = types or WindowInsetsCompat.Type.statusBars()
        if (hideNavBar) types = types or WindowInsetsCompat.Type.navigationBars()
        
        controller.hide(types)
        controller.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * 显示系统栏
     */
    fun showSystemBars(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
