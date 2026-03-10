package com.azheng.viewutils.edge

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

abstract class BaseEdgeActivity : AppCompatActivity() {

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
     * 是否需要适配系统栏，子类可重写返回 false 来禁用
     */
    protected open fun needAdaptSystemBar(): Boolean = true
    /**
     * 获取自动应用 Insets 的目标 View
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
     * Activity 创建时调用
     *
     * 在 `super.onCreate()` 之前启用 Edge-to-Edge（如果已启用）。
     *
     * ⚠️ 子类重写时必须调用 `super.onCreate(savedInstanceState)`
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate() 之前调用
        if (isEdgeToEdgeEnabled()) {
            EdgeToEdgeHelper.enable( this,getEdgeToEdgeConfig())
        }
        super.onCreate(savedInstanceState)

    }
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        if (needAdaptSystemBar()) {
            val targetView = getInsetsTargetView() ?: findViewById(android.R.id.content)
            EdgeToEdgeHelper.applyInsets(targetView)
        }
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        if (needAdaptSystemBar()) {
            val targetView = getInsetsTargetView() ?: findViewById(android.R.id.content)
            EdgeToEdgeHelper.applyInsets(targetView)
        }
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        if (needAdaptSystemBar()) {
            val targetView = getInsetsTargetView() ?: findViewById(android.R.id.content)
            EdgeToEdgeHelper.applyInsets(targetView)
        }
    }
}
