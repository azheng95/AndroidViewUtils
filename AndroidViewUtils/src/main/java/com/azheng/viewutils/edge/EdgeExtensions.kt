package com.azheng.viewutils.edge

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

// ==================== Activity 扩展函数 ====================

/**
 * 启用 Edge-to-Edge 模式
 *
 * ⚠️ 必须在 `super.onCreate()` 之前调用
 *
 * ### 使用示例
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     enableEdgeToEdge()  // 默认配置
 *     super.onCreate(savedInstanceState)
 * }
 * ```
 *
 * @param config Edge-to-Edge 配置，默认为亮色主题
 */
fun AppCompatActivity.enableEdgeToEdge(
    config: EdgeToEdgeConfig = EdgeToEdgeConfig.default()
) {
    EdgeToEdgeHelper.enable(this, config)
}

/**
 * 使用 Builder DSL 启用 Edge-to-Edge
 *
 * ### 使用示例
 * ```kotlin
 * enableEdgeToEdge {
 *     lightStatusBar()
 *     darkNavigationBar()
 *     fitIme(true)
 * }
 * ```
 *
 * @param builder 配置构建器 DSL
 */
inline fun AppCompatActivity.enableEdgeToEdge(
    builder: EdgeToEdgeConfig.Builder.() -> Unit
) {
    val config = EdgeToEdgeConfig.Builder().apply(builder).build()
    EdgeToEdgeHelper.enable(this, config)
}

/**
 * 为 Activity 的内容区域应用 Insets
 *
 * @param config Edge-to-Edge 配置
 */
fun AppCompatActivity.applyContentInsets(
    config: EdgeToEdgeConfig = EdgeToEdgeConfig.default()
) {
    val contentView = findViewById<View>(android.R.id.content)
    EdgeToEdgeHelper.applyInsets(contentView, config)
}

/**
 * 获取状态栏高度
 *
 * @return 状态栏高度（像素）
 */
fun Activity.getStatusBarHeight(): Int = SystemBarsHelper.getStatusBarHeight(window)

/**
 * 获取导航栏高度
 *
 * @return 导航栏高度（像素）
 */
fun Activity.getNavigationBarHeight(): Int = SystemBarsHelper.getNavigationBarHeight(window)

/**
 * 隐藏系统栏（沉浸式全屏）
 *
 * @param hideStatusBar 是否隐藏状态栏，默认 true
 * @param hideNavigationBar 是否隐藏导航栏，默认 true
 */
fun Activity.hideSystemBars(
    hideStatusBar: Boolean = true,
    hideNavigationBar: Boolean = true
) {
    SystemBarsHelper.hideSystemBars(window, hideStatusBar, hideNavigationBar)
}

/**
 * 显示系统栏
 */
fun Activity.showSystemBars() {
    SystemBarsHelper.showSystemBars(window)
}

// ==================== Fragment 扩展函数 ====================

/**
 * 为 Fragment 的根 View 应用 Insets
 *
 * @param config Edge-to-Edge 配置
 */
fun Fragment.applyEdgeToEdgeInsets(
    config: EdgeToEdgeConfig = EdgeToEdgeConfig.default()
) {
    view?.let { EdgeToEdgeHelper.applyInsets(it, config) }
}

// ==================== View 扩展函数 ====================

/**
 * 应用系统栏 Padding（状态栏 + 导航栏）
 *
 * 自动在 View 的上下左右添加系统栏高度的 padding
 */
fun View.applySystemBarsPadding() {
    val initialPadding = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialPadding[0] + systemBars.left,
            top = initialPadding[1] + systemBars.top,
            right = initialPadding[2] + systemBars.right,
            bottom = initialPadding[3] + systemBars.bottom
        )
        insets
    }
    requestApplyInsetsCompat()
}

/**
 * 仅应用状态栏 Padding
 *
 * 在 View 顶部添加状态栏高度的 padding
 */
fun View.applyStatusBarPadding() {
    val initialPaddingTop = paddingTop

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.updatePadding(top = initialPaddingTop + statusBarHeight)
        insets
    }
    requestApplyInsetsCompat()
}

/**
 * 仅应用导航栏 Padding
 *
 * 在 View 底部添加导航栏高度的 padding
 */
fun View.applyNavigationBarPadding() {
    val initialPaddingBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        view.updatePadding(bottom = initialPaddingBottom + navBarHeight)
        insets
    }
    requestApplyInsetsCompat()
}

/**
 * 智能应用导航栏 Padding
 *
 * 仅在非手势导航模式下应用导航栏 padding
 * 手势导航模式下不添加 padding，最大化屏幕利用率
 *
 * @return true=已应用（非手势导航），false=未应用（手势导航）
 *
 * ### 使用示例
 * ```kotlin
 * binding.bottomContainer.applyNavigationBarPaddingIfNeeded()
 * ```
 */
fun View.applyNavigationBarPaddingIfNeeded(): Boolean {
    return SystemBarsHelper.applyNavigationBarPaddingIfNeeded(this)
}

/**
 * 智能应用系统栏 Padding
 *
 * - 状态栏：始终应用
 * - 导航栏：仅在非手势导航模式下应用
 *
 * @param applyStatusBar 是否应用状态栏 padding，默认 true
 */
fun View.applySystemBarsPaddingSmartly(applyStatusBar: Boolean = true) {
    SystemBarsHelper.applySystemBarsPaddingSmartly(this, applyStatusBar)
}

/**
 * 应用 IME（软键盘）Padding
 *
 * 当软键盘弹出时，自动在 View 底部添加 padding
 */
fun View.applyImePadding() {
    val initialPaddingBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val bottomPadding = maxOf(imeInsets.bottom, navInsets.bottom)
        view.updatePadding(bottom = initialPaddingBottom + bottomPadding)
        insets
    }
    requestApplyInsetsCompat()
}

/**
 * 设置 View 高度等于状态栏高度
 *
 * 适用于自定义状态栏占位 View
 */
fun View.matchStatusBarHeight() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.layoutParams = view.layoutParams.apply { height = statusBarHeight }
        insets
    }
    requestApplyInsetsCompat()
}

/**
 * 设置 View 高度等于导航栏高度
 *
 * 适用于自定义导航栏占位 View
 */
fun View.matchNavigationBarHeight() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        view.layoutParams = view.layoutParams.apply { height = navBarHeight }
        insets
    }
    requestApplyInsetsCompat()
}

// ==================== 私有辅助函数 ====================

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
