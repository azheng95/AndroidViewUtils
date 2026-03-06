package com.azheng.viewutils.edge

import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Edge-to-Edge 核心工具类
 *
 * 使用 Google 官方推荐的 `enableEdgeToEdge()` API
 * 支持 Android 8.0 (API 26) - Android 16 (API 36)
 *
 * ## 快速开始
 *
 * ```kotlin
 * class MainActivity : AppCompatActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         // 必须在 super.onCreate() 之前调用
 *         EdgeToEdgeHelper.enable(this)
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *
 *         // 为内容区域应用 insets 适配
 *         EdgeToEdgeHelper.applyInsets(binding.root)
 *     }
 * }
 * ```
 *
 * ## 配置选项
 *
 * ```kotlin
 * // 深色主题
 * EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.dark())
 *
 * // 自动跟随系统主题
 * EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.auto())
 *
 * // 沉浸式（内容延伸到系统栏下方）
 * EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.immersive())
 *
 * // 自定义配置
 * EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.Builder()
 *     .lightStatusBar()
 *     .darkNavigationBar()
 *     .fitIme(true)
 *     .build())
 * ```
 *
 * @see EdgeToEdgeConfig 配置类
 * @see SystemBarsHelper 系统栏工具类（获取高度、显示/隐藏）
 * @since 1.0.0
 */
object EdgeToEdgeHelper {

    /**
     * 启用 Edge-to-Edge 模式
     *
     * ⚠️ **必须在 `super.onCreate()` 之前调用**
     *
     * 此方法会：
     * 1. 设置系统栏透明
     * 2. 允许内容绘制到系统栏区域
     * 3. 根据配置设置系统栏图标颜色
     *
     * @param activity AppCompatActivity 实例
     * @param config Edge-to-Edge 配置，默认为亮色主题
     *
     * ### 使用示例
     * ```kotlin
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     EdgeToEdgeHelper.enable(this)  // 默认配置
     *     // 或自定义配置
     *     EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.dark())
     *
     *     super.onCreate(savedInstanceState)
     *     setContentView(R.layout.activity_main)
     * }
     * ```
     */
    @JvmStatic
    @JvmOverloads
    fun enable(
        activity: AppCompatActivity,
        config: EdgeToEdgeConfig = EdgeToEdgeConfig.default()
    ) {
        // 使用 Google 官方 API
        activity.enableEdgeToEdge(
            statusBarStyle = config.toStatusBarSystemStyle(),
            navigationBarStyle = config.toNavigationBarSystemStyle()
        )

        // 处理刘海屏
        if (config.fitDisplayCutout && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    /**
     * 为 View 应用系统栏 Insets
     *
     * 根据配置自动为 View 添加适当的 padding，避免内容被系统栏遮挡
     *
     * @param view 需要适配的 View（通常是根布局）
     * @param config Edge-to-Edge 配置
     *
     * ### 使用示例
     * ```kotlin
     * // 应用默认配置（适配状态栏和导航栏）
     * EdgeToEdgeHelper.applyInsets(binding.root)
     *
     * // 仅适配状态栏
     * EdgeToEdgeHelper.applyInsets(binding.root,
     *     EdgeToEdgeConfig(fitStatusBar = true, fitNavigationBar = false))
     *
     * // 适配软键盘
     * EdgeToEdgeHelper.applyInsets(binding.root, EdgeToEdgeConfig.withIme())
     * ```
     */
    @JvmStatic
    @JvmOverloads
    fun applyInsets(
        view: View,
        config: EdgeToEdgeConfig = EdgeToEdgeConfig.default()
    ) {
        // 保存原始 padding
        val initialPadding = InitialPadding(
            view.paddingLeft,
            view.paddingTop,
            view.paddingRight,
            view.paddingBottom
        )

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insetTypes = buildInsetTypes(config)
            val insets = windowInsets.getInsets(insetTypes)

            v.updatePadding(
                left = initialPadding.left + insets.left,
                top = if (config.fitStatusBar) initialPadding.top + insets.top else initialPadding.top,
                right = initialPadding.right + insets.right,
                bottom = if (config.fitNavigationBar || config.fitIme) {
                    initialPadding.bottom + insets.bottom
                } else {
                    initialPadding.bottom
                }
            )

            // 返回 insets，让子 View 也能处理
            windowInsets
        }

        requestApplyInsetsWhenAttached(view)
    }

    /**
     * 为 View 仅应用状态栏 Insets
     *
     * 仅在 View 顶部添加状态栏高度的 padding
     *
     * @param view 需要适配的 View
     */
    @JvmStatic
    fun applyStatusBarInsets(view: View) {
        applyInsets(view, EdgeToEdgeConfig(fitStatusBar = true, fitNavigationBar = false))
    }

    /**
     * 为 View 仅应用导航栏 Insets
     *
     * 仅在 View 底部添加导航栏高度的 padding
     *
     * @param view 需要适配的 View
     */
    @JvmStatic
    fun applyNavigationBarInsets(view: View) {
        applyInsets(view, EdgeToEdgeConfig(fitStatusBar = false, fitNavigationBar = true))
    }

    /**
     * 为 View 应用 IME (软键盘) Insets
     *
     * 当软键盘弹出时，自动为 View 底部添加 padding
     * 适用于输入页面，避免输入框被键盘遮挡
     *
     * @param view 需要适配的 View
     *
     * ### 使用示例
     * ```kotlin
     * // 为包含 EditText 的布局应用 IME 适配
     * EdgeToEdgeHelper.applyImeInsets(binding.scrollView)
     * ```
     */
    @JvmStatic
    fun applyImeInsets(view: View) {
        val initialPaddingBottom = view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // 取 IME 和导航栏的较大值
            val bottomPadding = maxOf(imeInsets.bottom, navInsets.bottom)
            v.updatePadding(bottom = initialPaddingBottom + bottomPadding)

            windowInsets
        }

        requestApplyInsetsWhenAttached(view)
    }

    /**
     * 智能应用导航栏 Insets
     *
     * 仅在非手势导航模式下应用导航栏 padding
     * 手势导航模式下不添加 padding，最大化屏幕利用率
     *
     * @param view 需要适配的 View
     * @return true=已应用（非手势导航），false=未应用（手势导航）
     *
     * ### 使用示例
     * ```kotlin
     * // 底部有按钮的布局
     * EdgeToEdgeHelper.applyNavigationBarInsetsIfNeeded(binding.bottomButtonContainer)
     * ```
     *
     * @see NavigationBarUtils.isGestureNavigation 判断是否为手势导航
     */
    @JvmStatic
    fun applyNavigationBarInsetsIfNeeded(view: View): Boolean {
        return SystemBarsHelper.applyNavigationBarPaddingIfNeeded(view)
    }

    // ==================== 私有方法 ====================

    private fun buildInsetTypes(config: EdgeToEdgeConfig): Int {
        var types = 0
        if (config.fitStatusBar) {
            types = types or WindowInsetsCompat.Type.statusBars()
        }
        if (config.fitNavigationBar) {
            types = types or WindowInsetsCompat.Type.navigationBars()
        }
        if (config.fitIme) {
            types = types or WindowInsetsCompat.Type.ime()
        }
        if (config.fitDisplayCutout) {
            types = types or WindowInsetsCompat.Type.displayCutout()
        }
        return types
    }

    private fun requestApplyInsetsWhenAttached(view: View) {
        if (view.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    ViewCompat.requestApplyInsets(v)
                    v.removeOnAttachStateChangeListener(this)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    private data class InitialPadding(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
