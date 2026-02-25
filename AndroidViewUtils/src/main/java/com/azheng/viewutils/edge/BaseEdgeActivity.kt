package com.azheng.viewutils.edge


import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

abstract class BaseEdgeActivity : AppCompatActivity() {

    /** 防止 Insets 重复配置 */
    private var isInsetsConfigured = false

    // ==================== 子类可重写的配置 ====================

    protected open fun isEdgeToEdgeEnabled(): Boolean = true
    protected open fun isLightStatusBar(): Boolean = true
    protected open fun isLightNavigationBar(): Boolean = true
    protected open fun isFitStatusBar(): Boolean = true
    protected open fun isFitNavigationBar(): Boolean = true
    protected open fun isFitIme(): Boolean = false
    protected open fun getEdgeToEdgeContentView(): View? = null

    /**
     * 获取 Edge-to-Edge 配置
     * 子类可重写以完全自定义
     */
    protected open fun getEdgeToEdgeConfig(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig.Builder()
            .lightStatusBar(isLightStatusBar())
            .lightNavigationBar(isLightNavigationBar())
            .fitStatusBar(isFitStatusBar())
            .fitNavigationBar(isFitNavigationBar())
            .fitIme(isFitIme())
            .build()
    }

    // ==================== 生命周期 ====================

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ 使用工具类配置 Window（透明、颜色等）
        if (isEdgeToEdgeEnabled()) {
            EdgeToEdgeHelper.applyWindow(this, getEdgeToEdgeConfig())
        }
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applyInsetsIfNeeded()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        applyInsetsIfNeeded()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        applyInsetsIfNeeded()
    }

    /**
     * 应用 Insets 配置（仅执行一次）
     */
    private fun applyInsetsIfNeeded() {
        if (!isEdgeToEdgeEnabled() || isInsetsConfigured) return
        isInsetsConfigured = true

        // ✅ 使用工具类配置 Insets
        val targetView = getEdgeToEdgeContentView() ?: findViewById(android.R.id.content)
        EdgeToEdgeHelper.applyInsets(targetView, getEdgeToEdgeConfig())
    }

    // ==================== 公开方法 ====================

    /**
     * 动态更新配置
     */
    protected fun updateEdgeToEdge(config: EdgeToEdgeConfig) {
        EdgeToEdgeHelper.apply(this, config, getEdgeToEdgeContentView())
    }

    /**
     * 强制重新应用 Insets（用于动态切换布局）
     */
    protected fun reapplyInsets() {
        isInsetsConfigured = false
        applyInsetsIfNeeded()
    }

    protected fun getStatusBarHeight(): Int = EdgeToEdgeHelper.getStatusBarHeight(this)
    protected fun getNavigationBarHeight(): Int = EdgeToEdgeHelper.getNavigationBarHeight(this)
}