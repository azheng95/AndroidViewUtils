package com.azheng.viewutils.edge

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

/**
 * Edge-to-Edge 基类 Activity
 *
 * 继承此类可自动启用 Edge-to-Edge 适配，简化子类代码。
 *
 * ## 功能特性
 *
 * - 自动在 `onCreate()` 中启用 Edge-to-Edge
 * - 支持自定义配置（状态栏/导航栏样式、Insets 适配等）
 * - 默认不自动应用 Insets，需子类手动调用 [applyInsets] 方法
 *
 * ## 基本使用
 *
 * ```kotlin
 * class MainActivity : BaseEdgeActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *
 *         // 手动为目标 View 应用 Insets 适配
 *         applyInsets(binding.root)
 *     }
 * }
 * ```
 *
 * ## 自定义配置
 *
 * ```kotlin
 * class DarkThemeActivity : BaseEdgeActivity() {
 *
 *     // 自定义 Edge-to-Edge 配置（深色主题）
 *     override fun getEdgeToEdgeConfig() = EdgeToEdgeConfig.dark()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_dark)
 *
 *         // 应用自定义配置的 Insets
 *         applyInsets(binding.root, getEdgeToEdgeConfig())
 *     }
 * }
 * ```
 *
 * ## 自动应用 Insets（可选）
 *
 * 如需在 `setContentView()` 后自动应用 Insets，可重写 [isAutoApplyInsets] 返回 `true`：
 *
 * ```kotlin
 * class AutoInsetsActivity : BaseEdgeActivity() {
 *
 *     // 启用自动应用 Insets
 *     override fun isAutoApplyInsets() = true
 *
 *     // 指定自动应用 Insets 的目标 View
 *     override fun getInsetsTargetView() = binding.rootLayout
 * }
 * ```
 *
 * ## 禁用 Edge-to-Edge
 *
 * ```kotlin
 * class LegacyActivity : BaseEdgeActivity() {
 *     // 禁用 Edge-to-Edge，使用传统系统栏样式
 *     override fun isEdgeToEdgeEnabled() = false
 * }
 * ```
 *
 * @see EdgeToEdgeHelper 核心工具类
 * @see EdgeToEdgeConfig 配置类
 * @see SystemBarsHelper 系统栏工具类
 * @since 1.0.0
 */
abstract class BaseEdgeActivity : AppCompatActivity() {

    /**
     * 标记是否已应用 Insets
     *
     * 默认为 `true`，表示不自动应用 Insets。
     * 子类可通过重写 [isAutoApplyInsets] 返回 `true` 来启用自动应用。
     */
    private var isInsetsApplied = true

    // ==================== 子类可重写的配置 ====================

    /**
     * 是否启用 Edge-to-Edge 模式
     *
     * 重写此方法返回 `false` 可禁用 Edge-to-Edge，
     * 此时系统栏将使用默认样式（不透明背景）。
     *
     * @return `true` = 启用（默认），`false` = 禁用
     *
     * ### 示例
     * ```kotlin
     * // 禁用 Edge-to-Edge
     * override fun isEdgeToEdgeEnabled() = false
     * ```
     */
    protected open fun isEdgeToEdgeEnabled(): Boolean = true

    /**
     * 是否在 setContentView() 后自动应用 Insets
     *
     * 重写此方法返回 `true` 可启用自动应用 Insets 功能，
     * 配合 [getInsetsTargetView] 指定目标 View。
     *
     * @return `true` = 自动应用，`false` = 手动应用（默认）
     *
     * ### 示例
     * ```kotlin
     * // 启用自动应用 Insets
     * override fun isAutoApplyInsets() = true
     *
     * // 指定目标 View
     * override fun getInsetsTargetView() = binding.root
     * ```
     */
    protected open fun isAutoApplyInsets(): Boolean = false

    /**
     * 获取 Edge-to-Edge 配置
     *
     * 重写此方法自定义系统栏样式和 Insets 适配行为。
     *
     * @return Edge-to-Edge 配置对象
     *
     * ### 可用的预设配置
     * - [EdgeToEdgeConfig.default] - 亮色主题（默认）
     * - [EdgeToEdgeConfig.dark] - 深色主题
     * - [EdgeToEdgeConfig.auto] - 自动跟随系统主题
     * - [EdgeToEdgeConfig.immersive] - 沉浸式（内容延伸到系统栏下）
     * - [EdgeToEdgeConfig.withIme] - 带软键盘适配
     *
     * ### 自定义配置示例
     * ```kotlin
     * override fun getEdgeToEdgeConfig() = EdgeToEdgeConfig.Builder()
     *     .darkStatusBar()           // 深色状态栏（亮色图标）
     *     .lightNavigationBar()      // 亮色导航栏（深色图标）
     *     .fitStatusBar(true)        // 适配状态栏
     *     .fitNavigationBar(true)    // 适配导航栏
     *     .fitIme(true)              // 适配软键盘
     *     .fitDisplayCutout(true)    // 适配刘海屏
     *     .build()
     * ```
     */
    protected open fun getEdgeToEdgeConfig(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig.default()
    }

    /**
     * 获取自动应用 Insets 的目标 View
     *
     * 仅当 [isAutoApplyInsets] 返回 `true` 时生效。
     * 重写此方法指定需要自动适配的 View。
     *
     * @return 目标 View，返回 `null` 则使用 `android.R.id.content`
     *
     * ### 示例
     * ```kotlin
     * private lateinit var binding: ActivityMainBinding
     *
     * override fun getInsetsTargetView(): View = binding.root
     * ```
     */
    protected open fun getInsetsTargetView(): View? = null

    // ==================== 生命周期 ====================

    /**
     * Activity 创建时调用
     *
     * 在 `super.onCreate()` 之前启用 Edge-to-Edge（如果已启用）。
     *
     * ⚠️ 子类重写时必须调用 `super.onCreate(savedInstanceState)`
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate() 之前调用
        if (isEdgeToEdgeEnabled()) {
            EdgeToEdgeHelper.enable(this, getEdgeToEdgeConfig())
        }
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        autoApplyInsetsIfNeeded()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        autoApplyInsetsIfNeeded()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        autoApplyInsetsIfNeeded()
    }

    // ==================== 私有方法 ====================

    /**
     * 自动应用 Insets（内部方法）
     *
     * 根据 [isAutoApplyInsets] 和 [isInsetsApplied] 判断是否需要自动应用。
     */
    private fun autoApplyInsetsIfNeeded() {
        // 检查是否启用自动应用
        if (!isAutoApplyInsets()) return
        // 检查是否已启用 Edge-to-Edge
        if (!isEdgeToEdgeEnabled()) return
        // 检查是否已应用过
        if (isInsetsApplied) return

        isInsetsApplied = true

        val config = getEdgeToEdgeConfig()
        // 如果所有适配选项都关闭，则不需要应用
        if (!config.fitStatusBar && !config.fitNavigationBar && !config.fitIme) return

        val targetView = getInsetsTargetView() ?: findViewById(android.R.id.content)
        EdgeToEdgeHelper.applyInsets(targetView, config)
    }

    // ==================== 公开方法（供子类使用） ====================

    /**
     * 为指定 View 应用 Insets 适配
     *
     * 根据当前配置为 View 添加系统栏 Padding，避免内容被遮挡。
     *
     * @param view 需要适配的 View（通常是根布局）
     * @param config 可选的自定义配置，默认使用 [getEdgeToEdgeConfig] 返回的配置
     *
     * ### 使用示例
     * ```kotlin
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     super.onCreate(savedInstanceState)
     *     setContentView(R.layout.activity_main)
     *
     *     // 使用默认配置
     *     applyInsets(binding.root)
     *
     *     // 或使用自定义配置
     *     applyInsets(binding.scrollView, EdgeToEdgeConfig.withIme())
     * }
     * ```
     */
    protected fun applyInsets(
        view: View,
        config: EdgeToEdgeConfig = getEdgeToEdgeConfig()
    ) {
        if (!isEdgeToEdgeEnabled()) return
        EdgeToEdgeHelper.applyInsets(view, config)
    }

    /**
     * 为指定 View 应用状态栏 Insets
     *
     * 仅在 View 顶部添加状态栏高度的 Padding。
     *
     * @param view 需要适配的 View
     *
     * ### 使用示例
     * ```kotlin
     * // 为 Toolbar 容器应用状态栏适配
     * applyStatusBarInsets(binding.toolbarContainer)
     * ```
     */
    protected fun applyStatusBarInsets(view: View) {
        if (!isEdgeToEdgeEnabled()) return
        EdgeToEdgeHelper.applyStatusBarInsets(view)
    }

    /**
     * 为指定 View 应用导航栏 Insets
     *
     * 仅在 View 底部添加导航栏高度的 Padding。
     *
     * @param view 需要适配的 View
     *
     * ### 使用示例
     * ```kotlin
     * // 为底部按钮容器应用导航栏适配
     * applyNavigationBarInsets(binding.bottomContainer)
     * ```
     */
    protected fun applyNavigationBarInsets(view: View) {
        if (!isEdgeToEdgeEnabled()) return
        EdgeToEdgeHelper.applyNavigationBarInsets(view)
    }

    /**
     * 智能应用导航栏 Insets
     *
     * 仅在非手势导航模式下（三按钮/两按钮）应用导航栏 Padding。
     * 手势导航模式下不添加 Padding，最大化屏幕利用率。
     *
     * @param view 需要适配的 View
     * @return `true` = 已应用（非手势导航），`false` = 未应用（手势导航）
     *
     * ### 使用示例
     * ```kotlin
     * // 底部有固定按钮的页面
     * applyNavigationBarInsetsIfNeeded(binding.bottomButtonContainer)
     * ```
     *
     * @see NavigationBarUtils.isGestureNavigation
     */
    protected fun applyNavigationBarInsetsIfNeeded(view: View): Boolean {
        if (!isEdgeToEdgeEnabled()) return false
        return EdgeToEdgeHelper.applyNavigationBarInsetsIfNeeded(view)
    }

    /**
     * 为指定 View 应用 IME（软键盘）Insets
     *
     * 当软键盘弹出时，自动为 View 底部添加 Padding，
     * 避免输入框被键盘遮挡。
     *
     * @param view 需要适配的 View（通常是包含输入框的 ScrollView）
     *
     * ### 使用示例
     * ```kotlin
     * // 为包含 EditText 的布局应用 IME 适配
     * applyImeInsets(binding.scrollView)
     * ```
     */
    protected fun applyImeInsets(view: View) {
        if (!isEdgeToEdgeEnabled()) return
        EdgeToEdgeHelper.applyImeInsets(view)
    }

    /**
     * 重新应用 Insets
     *
     * 用于动态切换布局后重新触发自动 Insets 适配。
     * 仅当 [isAutoApplyInsets] 返回 `true` 时有效。
     *
     * ### 使用示例
     * ```kotlin
     * // 动态切换布局后
     * setContentView(R.layout.new_layout)
     * reapplyInsets()  // 重新触发自动适配
     * ```
     */
    protected fun reapplyInsets() {
        isInsetsApplied = false
        autoApplyInsetsIfNeeded()
    }

    /**
     * 启用自动应用 Insets
     *
     * 调用此方法后，下次 `setContentView()` 时将自动应用 Insets。
     *
     * ### 使用示例
     * ```kotlin
     * enableAutoApplyInsets()
     * setContentView(R.layout.activity_main)  // 将自动应用 Insets
     * ```
     */
    protected fun enableAutoApplyInsets() {
        isInsetsApplied = false
    }
}
