package com.azheng.viewutils.edge

import android.graphics.Color
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.annotation.ColorInt

/**
 * Edge-to-Edge 配置类
 * 支持 Android 8.0 - 16
 */
data class EdgeToEdgeConfig(
    /** 状态栏样式 */
    val statusBarStyle: StatusBarStyle = StatusBarStyle.Light,
    /** 导航栏样式 */
    val navigationBarStyle: NavigationBarStyle = NavigationBarStyle.Light,
    /** 是否适配状态栏 (添加 padding 避免内容重叠) */
    val fitStatusBar: Boolean = true,
    /** 是否适配导航栏 */
    val fitNavigationBar: Boolean = true,
    /** 是否适配 IME (软键盘) */
    val fitIme: Boolean = false,
    /** 是否适配刘海屏 */
    val fitDisplayCutout: Boolean = true
) {

    /**
     * 状态栏样式
     */
    sealed class StatusBarStyle {
        /** 亮色背景，深色图标 */
        object Light : StatusBarStyle()
        /** 深色背景，亮色图标 */
        object Dark : StatusBarStyle()
        /** 自动：根据系统主题自动切换 */
        object Auto : StatusBarStyle()
        /** 自定义颜色 */
        data class Custom(
            @ColorInt val lightScrim: Int,
            @ColorInt val darkScrim: Int
        ) : StatusBarStyle()
    }

    /**
     * 导航栏样式
     */
    sealed class NavigationBarStyle {
        /** 亮色背景，深色图标 */
        object Light : NavigationBarStyle()
        /** 深色背景，亮色图标 */
        object Dark : NavigationBarStyle()
        /** 自动：根据系统主题自动切换 */
        object Auto : NavigationBarStyle()
        /** 自定义颜色 */
        data class Custom(
            @ColorInt val lightScrim: Int,
            @ColorInt val darkScrim: Int
        ) : NavigationBarStyle()
    }

    /**
     * 转换为 AndroidX SystemBarStyle
     */
    internal fun toStatusBarSystemStyle(): SystemBarStyle {
        return when (statusBarStyle) {
            is StatusBarStyle.Light -> SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
            is StatusBarStyle.Dark -> SystemBarStyle.dark(Color.TRANSPARENT)
            is StatusBarStyle.Auto -> SystemBarStyle.auto(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
            is StatusBarStyle.Custom -> SystemBarStyle.auto(
                statusBarStyle.lightScrim,
                statusBarStyle.darkScrim
            )
        }
    }

    internal fun toNavigationBarSystemStyle(): SystemBarStyle {
        return when (navigationBarStyle) {
            is NavigationBarStyle.Light -> SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
            is NavigationBarStyle.Dark -> SystemBarStyle.dark(Color.TRANSPARENT)
            is NavigationBarStyle.Auto -> SystemBarStyle.auto(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
            is NavigationBarStyle.Custom -> SystemBarStyle.auto(
                navigationBarStyle.lightScrim,
                navigationBarStyle.darkScrim
            )
        }
    }

    companion object {
        /** 默认配置：亮色主题 */
        fun default() = EdgeToEdgeConfig()

        /** 深色主题 */
        fun dark() = EdgeToEdgeConfig(
            statusBarStyle = StatusBarStyle.Dark,
            navigationBarStyle = NavigationBarStyle.Dark
        )

        /** 自动主题：跟随系统 */
        fun auto() = EdgeToEdgeConfig(
            statusBarStyle = StatusBarStyle.Auto,
            navigationBarStyle = NavigationBarStyle.Auto
        )

        /** 沉浸式：内容延伸到系统栏下方 */
        fun immersive() = EdgeToEdgeConfig(
            fitStatusBar = false,
            fitNavigationBar = false
        )

        /** 带输入法适配 */
        fun withIme() = EdgeToEdgeConfig(fitIme = true)
    }

    /** Builder 模式 */
    class Builder {
        private var statusBarStyle: StatusBarStyle = StatusBarStyle.Light
        private var navigationBarStyle: NavigationBarStyle = NavigationBarStyle.Light
        private var fitStatusBar: Boolean = true
        private var fitNavigationBar: Boolean = true
        private var fitIme: Boolean = false
        private var fitDisplayCutout: Boolean = true

        fun statusBarStyle(style: StatusBarStyle) = apply { statusBarStyle = style }
        fun navigationBarStyle(style: NavigationBarStyle) = apply { navigationBarStyle = style }
        fun lightStatusBar() = apply { statusBarStyle = StatusBarStyle.Light }
        fun darkStatusBar() = apply { statusBarStyle = StatusBarStyle.Dark }
        fun lightNavigationBar() = apply { navigationBarStyle = NavigationBarStyle.Light }
        fun darkNavigationBar() = apply { navigationBarStyle = NavigationBarStyle.Dark }
        fun fitStatusBar(fit: Boolean) = apply { fitStatusBar = fit }
        fun fitNavigationBar(fit: Boolean) = apply { fitNavigationBar = fit }
        fun fitIme(fit: Boolean) = apply { fitIme = fit }
        fun fitDisplayCutout(fit: Boolean) = apply { fitDisplayCutout = fit }

        fun build() = EdgeToEdgeConfig(
            statusBarStyle, navigationBarStyle,
            fitStatusBar, fitNavigationBar, fitIme, fitDisplayCutout
        )
    }
}
