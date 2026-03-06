package com.azheng.viewutils.edge

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * 导航栏工具类
 *
 * 用于判断设备导航模式（三按钮/两按钮/手势导航）
 *
 * ## 导航模式说明
 *
 * | 模式 | 说明 | Android 版本 |
 * |------|------|-------------|
 * | 三按钮 | 返回、Home、最近任务 | 所有版本 |
 * | 两按钮 | 药丸导航（Android P 引入） | 9.0+ |
 * | 手势 | 全面屏手势导航 | 10.0+ |
 *
 * ## 使用示例
 *
 * ```kotlin
 * // 判断导航模式
 * when (NavigationBarUtils.getNavigationMode(context)) {
 *     NavigationMode.GESTURE -> { /* 手势导航 */ }
 *     NavigationMode.TWO_BUTTON -> { /* 两按钮 */ }
 *     NavigationMode.THREE_BUTTON -> { /* 三按钮 */ }
 * }
 *
 * // 快捷判断
 * if (NavigationBarUtils.isGestureNavigation(context)) {
 *     // 手势导航模式，底部空间更大
 * }
 *
 * if (NavigationBarUtils.hasVisibleNavigationBar(context)) {
 *     // 有可见的按钮式导航栏，需要适配
 * }
 * ```
 *
 * @since 1.0.0
 */
object NavigationBarUtils {

    /**
     * 导航模式枚举
     */
    enum class NavigationMode {
        /** 三按钮导航（返回、Home、最近任务） */
        THREE_BUTTON,
        /** 两按钮导航（Android 9 的药丸导航） */
        TWO_BUTTON,
        /** 全面屏手势导航 */
        GESTURE
    }

    private const val NAV_MODE_THREE_BUTTON = 0
    private const val NAV_MODE_TWO_BUTTON = 1
    private const val NAV_MODE_GESTURE = 2

    /**
     * 获取当前设备的导航模式
     *
     * @param context 上下文
     * @return 导航模式枚举值
     */
    @JvmStatic
    fun getNavigationMode(context: Context): NavigationMode {
        // Android 10 (Q) 及以上支持手势导航
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return try {
                // navigation_mode: 0=三按钮, 1=两按钮, 2=手势
                val mode = Settings.Secure.getInt(
                    context.contentResolver,
                    "navigation_mode",
                    NAV_MODE_THREE_BUTTON
                )
                when (mode) {
                    NAV_MODE_THREE_BUTTON -> NavigationMode.THREE_BUTTON
                    NAV_MODE_TWO_BUTTON -> NavigationMode.TWO_BUTTON
                    NAV_MODE_GESTURE -> NavigationMode.GESTURE
                    else -> NavigationMode.THREE_BUTTON
                }
            } catch (e: Exception) {
                NavigationMode.THREE_BUTTON
            }
        }
        // Android 9 及以下默认三按钮
        return NavigationMode.THREE_BUTTON
    }

    /**
     * 判断是否有【可见的】底部导航栏
     *
     * 三按钮或两按钮导航模式返回 true
     * 手势导航模式返回 false（手势条不算可见导航栏）
     *
     * @param context 上下文
     * @return true=有可见导航栏（按钮式），false=手势导航或无导航栏
     *
     * ### 使用场景
     * ```kotlin
     * if (hasVisibleNavigationBar(context)) {
     *     // 底部有导航栏按钮，可能需要为 View 添加 padding
     *     view.applyNavigationBarPadding()
     * }
     * ```
     */
    @JvmStatic
    fun hasVisibleNavigationBar(context: Context): Boolean {
        return getNavigationMode(context) != NavigationMode.GESTURE
    }

    /**
     * 判断是否为手势导航模式
     *
     * @param context 上下文
     * @return true=手势导航，false=按钮导航
     */
    @JvmStatic
    fun isGestureNavigation(context: Context): Boolean {
        return getNavigationMode(context) == NavigationMode.GESTURE
    }

    /**
     * 判断是否为三按钮导航模式
     *
     * @param context 上下文
     * @return true=三按钮导航
     */
    @JvmStatic
    fun isThreeButtonNavigation(context: Context): Boolean {
        return getNavigationMode(context) == NavigationMode.THREE_BUTTON
    }

    /**
     * 判断是否为两按钮导航模式（药丸导航）
     *
     * @param context 上下文
     * @return true=两按钮导航
     */
    @JvmStatic
    fun isTwoButtonNavigation(context: Context): Boolean {
        return getNavigationMode(context) == NavigationMode.TWO_BUTTON
    }
}
