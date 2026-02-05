// EdgeToEdgeConfig.kt
package com.azheng.viewutils.edge

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt

/**
 * Edge-to-Edge 配置类
 * 使用Builder模式，支持链式调用
 */
data class EdgeToEdgeConfig private constructor(
    val isEnabled: Boolean,
    val isStatusBarTransparent: Boolean,
    val isNavigationBarTransparent: Boolean,
    @ColorInt val statusBarColor: Int,
    @ColorInt val navigationBarColor: Int,
    val isLightStatusBar: Boolean,
    val isLightNavigationBar: Boolean,
    val fitStatusBar: Boolean,
    val fitNavigationBar: Boolean,
    val fitIme: Boolean,
    val fitDisplayCutout: Boolean,
    val paddingView: View?,
    val marginView: View?
) {
    
    class Builder {
        private var isEnabled: Boolean = true
        private var isStatusBarTransparent: Boolean = true
        private var isNavigationBarTransparent: Boolean = true
        @ColorInt private var statusBarColor: Int = Color.TRANSPARENT
        @ColorInt private var navigationBarColor: Int = Color.TRANSPARENT
        private var isLightStatusBar: Boolean = true
        private var isLightNavigationBar: Boolean = true
        private var fitStatusBar: Boolean = true
        private var fitNavigationBar: Boolean = true
        private var fitIme: Boolean = false
        private var fitDisplayCutout: Boolean = true
        private var paddingView: View? = null
        private var marginView: View? = null

        /** 是否启用Edge-to-Edge */
        fun enabled(enabled: Boolean) = apply { isEnabled = enabled }

        /** 状态栏透明 */
        fun statusBarTransparent(transparent: Boolean) = apply { 
            isStatusBarTransparent = transparent 
        }

        /** 导航栏透明 */
        fun navigationBarTransparent(transparent: Boolean) = apply { 
            isNavigationBarTransparent = transparent 
        }

        /** 状态栏颜色 */
        fun statusBarColor(@ColorInt color: Int) = apply { 
            statusBarColor = color
            isStatusBarTransparent = color == Color.TRANSPARENT
        }

        /** 导航栏颜色 */
        fun navigationBarColor(@ColorInt color: Int) = apply { 
            navigationBarColor = color
            isNavigationBarTransparent = color == Color.TRANSPARENT
        }

        /** 亮色状态栏（深色图标） */
        fun lightStatusBar(light: Boolean) = apply { isLightStatusBar = light }

        /** 亮色导航栏（深色图标） */
        fun lightNavigationBar(light: Boolean) = apply { isLightNavigationBar = light }

        /** 是否适配状态栏（添加padding/margin避免重叠） */
        fun fitStatusBar(fit: Boolean) = apply { fitStatusBar = fit }

        /** 是否适配导航栏（添加padding/margin避免重叠） */
        fun fitNavigationBar(fit: Boolean) = apply { fitNavigationBar = fit }

        /** 是否适配输入法 */
        fun fitIme(fit: Boolean) = apply { fitIme = fit }

        /** 是否适配刘海屏 */
        fun fitDisplayCutout(fit: Boolean) = apply { fitDisplayCutout = fit }

        /** 设置需要添加padding的View */
        fun paddingView(view: View?) = apply { paddingView = view }

        /** 设置需要添加margin的View */
        fun marginView(view: View?) = apply { marginView = view }

        fun build() = EdgeToEdgeConfig(
            isEnabled, isStatusBarTransparent, isNavigationBarTransparent,
            statusBarColor, navigationBarColor, isLightStatusBar, isLightNavigationBar,
            fitStatusBar, fitNavigationBar, fitIme, fitDisplayCutout,
            paddingView, marginView
        )
    }

    companion object {
        /** 默认配置：全透明，亮色图标 */
        fun default() = Builder().build()

        /** 沉浸式配置：全透明，不适配（内容延伸到系统栏下） */
        fun immersive() = Builder()
            .fitStatusBar(false)
            .fitNavigationBar(false)
            .build()

        /** 仅状态栏透明 */
        fun transparentStatusBar() = Builder()
            .navigationBarTransparent(false)
            .navigationBarColor(Color.WHITE)
            .build()
    }
}
