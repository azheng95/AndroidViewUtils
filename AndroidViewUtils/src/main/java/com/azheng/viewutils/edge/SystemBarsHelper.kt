package com.azheng.viewutils.edge

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

/**
 * 系统栏工具类
 * 
 * 提供状态栏、导航栏的高度获取、显示/隐藏控制等功能
 * 
 * ## 使用示例
 * 
 * ### 获取系统栏高度
 * ```kotlin
 * // 在 Activity 中
 * val statusBarHeight = SystemBarsHelper.getStatusBarHeight(window)
 * val navBarHeight = SystemBarsHelper.getNavigationBarHeight(window)
 * 
 * // 使用 View
 * SystemBarsHelper.getStatusBarHeight(myView) { height ->
 *     // 获取到状态栏高度
 * }
 * ```
 * 
 * ### 隐藏/显示系统栏
 * ```kotlin
 * // 隐藏所有系统栏（沉浸式全屏）
 * SystemBarsHelper.hideSystemBars(window)
 * 
 * // 仅隐藏状态栏
 * SystemBarsHelper.hideSystemBars(window, hideStatusBar = true, hideNavigationBar = false)
 * 
 * // 显示系统栏
 * SystemBarsHelper.showSystemBars(window)
 * ```
 * 
 * ### 智能导航栏适配
 * ```kotlin
 * // 仅在非手势导航模式下应用导航栏 padding
 * SystemBarsHelper.applyNavigationBarPaddingIfNeeded(context, view)
 * ```
 * 
 * @since 1.0.0
 */
object SystemBarsHelper {

    // ==================== 状态栏高度 ====================

    /**
     * 获取状态栏高度
     * 
     * 通过 Window 的 DecorView 获取当前状态栏高度
     * 
     * @param window Activity 或 Dialog 的 Window 对象
     * @return 状态栏高度（像素），如果无法获取则返回 0
     * 
     * @see getStatusBarHeightAsync 异步获取（推荐用于 View 未 attach 时）
     */
    @JvmStatic
    fun getStatusBarHeight(window: Window): Int {
        val windowInsets = ViewCompat.getRootWindowInsets(window.decorView)
        return windowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    }

    /**
     * 获取状态栏高度（通过 Activity）
     * 
     * @param activity Activity 实例
     * @return 状态栏高度（像素），如果无法获取则返回 0
     */
    @JvmStatic
    fun getStatusBarHeight(activity: Activity): Int {
        return getStatusBarHeight(activity.window)
    }

    /**
     * 异步获取状态栏高度
     * 
     * 当 View 可能尚未 attach 到 Window 时使用此方法
     * 会在 View attach 后自动回调
     * 
     * @param view 任意已添加到布局的 View
     * @param callback 获取到高度后的回调，参数为状态栏高度（像素）
     * 
     * ### 使用示例
     * ```kotlin
     * SystemBarsHelper.getStatusBarHeightAsync(binding.root) { height ->
     *     Log.d("StatusBar", "高度: $height px")
     * }
     * ```
     */
    @JvmStatic
    fun getStatusBarHeightAsync(view: View, callback: (Int) -> Unit) {
        if (view.isAttachedToWindow) {
            val insets = ViewCompat.getRootWindowInsets(view)
            callback(insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0)
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val insets = ViewCompat.getRootWindowInsets(v)
                    callback(insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0)
                    v.removeOnAttachStateChangeListener(this)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    // ==================== 导航栏高度 ====================

    /**
     * 获取导航栏高度
     * 
     * 通过 Window 的 DecorView 获取当前导航栏高度
     * 
     * ⚠️ 注意：在手势导航模式下，此方法仍会返回手势指示条的高度（通常较小）
     * 如需判断是否有可见导航栏，请使用 [NavigationBarUtils.hasVisibleNavigationBar]
     * 
     * @param window Activity 或 Dialog 的 Window 对象
     * @return 导航栏高度（像素），如果无法获取则返回 0
     * 
     * @see NavigationBarUtils.hasVisibleNavigationBar 判断是否有可见导航栏
     */
    @JvmStatic
    fun getNavigationBarHeight(window: Window): Int {
        val windowInsets = ViewCompat.getRootWindowInsets(window.decorView)
        return windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
    }

    /**
     * 获取导航栏高度（通过 Activity）
     * 
     * @param activity Activity 实例
     * @return 导航栏高度（像素），如果无法获取则返回 0
     */
    @JvmStatic
    fun getNavigationBarHeight(activity: Activity): Int {
        return getNavigationBarHeight(activity.window)
    }

    /**
     * 异步获取导航栏高度
     * 
     * 当 View 可能尚未 attach 到 Window 时使用此方法
     * 
     * @param view 任意已添加到布局的 View
     * @param callback 获取到高度后的回调，参数为导航栏高度（像素）
     */
    @JvmStatic
    fun getNavigationBarHeightAsync(view: View, callback: (Int) -> Unit) {
        if (view.isAttachedToWindow) {
            val insets = ViewCompat.getRootWindowInsets(view)
            callback(insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0)
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val insets = ViewCompat.getRootWindowInsets(v)
                    callback(insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0)
                    v.removeOnAttachStateChangeListener(this)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    // ==================== 系统栏显示/隐藏控制 ====================

    /**
     * 隐藏系统栏
     * 
     * 隐藏状态栏和/或导航栏，实现沉浸式全屏效果
     * 用户滑动屏幕边缘时会临时显示系统栏
     * 
     * @param window Activity 或 Dialog 的 Window 对象
     * @param hideStatusBar 是否隐藏状态栏，默认 true
     * @param hideNavigationBar 是否隐藏导航栏，默认 true
     * 
     * ### 使用示例
     * ```kotlin
     * // 隐藏所有系统栏（视频播放、游戏等场景）
     * SystemBarsHelper.hideSystemBars(window)
     * 
     * // 仅隐藏状态栏
     * SystemBarsHelper.hideSystemBars(window, hideStatusBar = true, hideNavigationBar = false)
     * ```
     * 
     * @see showSystemBars 显示系统栏
     */
    @JvmStatic
    @JvmOverloads
    fun hideSystemBars(
        window: Window,
        hideStatusBar: Boolean = true,
        hideNavigationBar: Boolean = true
    ) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (hideStatusBar) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
        if (hideNavigationBar) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
        // 用户滑动边缘时临时显示，然后自动隐藏
        controller.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * 隐藏系统栏（通过 Activity）
     * 
     * @param activity Activity 实例
     * @param hideStatusBar 是否隐藏状态栏，默认 true
     * @param hideNavigationBar 是否隐藏导航栏，默认 true
     */
    @JvmStatic
    @JvmOverloads
    fun hideSystemBars(
        activity: Activity,
        hideStatusBar: Boolean = true,
        hideNavigationBar: Boolean = true
    ) {
        hideSystemBars(activity.window, hideStatusBar, hideNavigationBar)
    }

    /**
     * 显示系统栏
     * 
     * 恢复显示状态栏和导航栏
     * 
     * @param window Activity 或 Dialog 的 Window 对象
     * 
     * @see hideSystemBars 隐藏系统栏
     */
    @JvmStatic
    fun showSystemBars(window: Window) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 显示系统栏（通过 Activity）
     * 
     * @param activity Activity 实例
     */
    @JvmStatic
    fun showSystemBars(activity: Activity) {
        showSystemBars(activity.window)
    }

    // ==================== 智能导航栏适配 ====================

    /**
     * 智能应用导航栏 Padding
     * 
     * 仅在非手势导航模式下（三按钮/两按钮导航）为 View 添加底部 padding
     * 手势导航模式下不添加 padding，避免浪费屏幕空间
     * 
     * ⚠️ 适用场景：
     * - 底部有操作按钮或内容的页面
     * - 需要避免内容被导航栏遮挡，但在手势导航下又不想浪费空间
     * 
     * @param view 需要应用 padding 的 View
     * @return true 表示应用了 padding（非手势导航），false 表示未应用（手势导航）
     * 
     * ### 使用示例
     * ```kotlin
     * // 在 Activity 或 Fragment 中
     * SystemBarsHelper.applyNavigationBarPaddingIfNeeded(binding.bottomContainer)
     * 
     * // 也可以检查返回值
     * val applied = SystemBarsHelper.applyNavigationBarPaddingIfNeeded(view)
     * if (applied) {
     *     Log.d("Nav", "已应用导航栏 padding")
     * }
     * ```
     */
    @JvmStatic
    fun applyNavigationBarPaddingIfNeeded(view: View): Boolean {
        val context = view.context
        return if (!NavigationBarUtils.isGestureNavigation(context)) {
            view.applyNavigationBarPadding()
            true
        } else {
            false
        }
    }

    /**
     * 智能应用系统栏 Padding
     * 
     * - 状态栏 padding：始终应用
     * - 导航栏 padding：仅在非手势导航模式下应用
     * 
     * @param view 需要应用 padding 的 View
     * @param applyStatusBar 是否应用状态栏 padding，默认 true
     * 
     * ### 使用示例
     * ```kotlin
     * // 应用状态栏 + 智能导航栏 padding
     * SystemBarsHelper.applySystemBarsPaddingSmartly(binding.root)
     * 
     * // 仅智能应用导航栏（不处理状态栏）
     * SystemBarsHelper.applySystemBarsPaddingSmartly(binding.root, applyStatusBar = false)
     * ```
     */
    @JvmStatic
    @JvmOverloads
    fun applySystemBarsPaddingSmartly(view: View, applyStatusBar: Boolean = true) {
        val context = view.context
        val applyNavBar = !NavigationBarUtils.isGestureNavigation(context)
        
        val initialPadding = intArrayOf(
            view.paddingLeft, 
            view.paddingTop, 
            view.paddingRight, 
            view.paddingBottom
        )

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            v.updatePadding(
                left = initialPadding[0] + navBars.left,
                top = if (applyStatusBar) initialPadding[1] + statusBars.top else initialPadding[1],
                right = initialPadding[2] + navBars.right,
                bottom = if (applyNavBar) initialPadding[3] + navBars.bottom else initialPadding[3]
            )
            insets
        }
        view.requestApplyInsetsCompat()
    }
}

// ==================== View 扩展（内部使用） ====================

private fun View.requestApplyInsetsCompat() {
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                ViewCompat.requestApplyInsets(v)
                v.removeOnAttachStateChangeListener(this)
            }
            override fun onViewDetachedFromWindow(v: View) {}
        })
    }
}
