package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import android.widget.Toast
import com.azheng.androidviewutils.demo.databinding.ActivityDemoEdgeBinding
import com.azheng.viewutils.edge.*

/**
 * Edge-to-Edge 库功能演示 Activity
 * 
 * 演示内容：
 * 1. 基本 Edge-to-Edge 启用
 * 2. 状态栏/导航栏 Insets 适配
 * 3. 智能导航栏适配（区分手势/按钮导航）
 * 4. 系统栏显示/隐藏
 * 5. 获取系统栏高度
 * 6. 导航模式判断
 */
class DemoEdgeActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivityDemoEdgeBinding

    // ==================== 配置重写 ====================

    /**
     * 自定义 Edge-to-Edge 配置
     * 这里使用亮色状态栏 + 导航栏，并启用 IME 适配
     */
    override fun getEdgeToEdgeConfig(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig.Builder()
            .lightStatusBar()           // 亮色状态栏背景，深色图标
            .lightNavigationBar()       // 亮色导航栏背景，深色图标
            .fitStatusBar(true)         // 启用状态栏适配
            .fitNavigationBar(true)     // 启用导航栏适配
            .fitIme(true)               // 启用软键盘适配
            .fitDisplayCutout(true)     // 启用刘海屏适配
            .build()
    }

    // ==================== 生命周期 ====================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupButtons()
        displaySystemInfo()
    }

    // ==================== Insets 适配设置 ====================

    private fun setupInsets() {
        // 方式1: 为整个根布局应用系统栏 Insets
        // applyInsets(binding.root)

        // 方式2: 分别为不同区域应用不同的 Insets
        
        // 顶部 Toolbar 区域 - 仅适配状态栏
        applyStatusBarInsets(binding.toolbarContainer)

        // 底部按钮区域 - 智能适配导航栏（手势导航不添加 padding）
        applyNavigationBarInsetsIfNeeded(binding.bottomContainer)

        // 中间内容区域 - 适配 IME（软键盘）
        applyImeInsets(binding.scrollContent)
    }

    // ==================== 按钮事件 ====================

    private fun setupButtons() {
        
        // 隐藏所有系统栏（沉浸式全屏）
        binding.btnHideSystemBars.setOnClickListener {
            SystemBarsHelper.hideSystemBars(window)
            showToast("系统栏已隐藏，从边缘滑动可临时显示")
        }

        // 显示系统栏
        binding.btnShowSystemBars.setOnClickListener {
            SystemBarsHelper.showSystemBars(window)
            showToast("系统栏已显示")
        }

        // 仅隐藏状态栏
        binding.btnHideStatusBar.setOnClickListener {
            SystemBarsHelper.hideSystemBars(
                window,
                hideStatusBar = true,
                hideNavigationBar = false
            )
            showToast("状态栏已隐藏")
        }

        // 仅隐藏导航栏
        binding.btnHideNavBar.setOnClickListener {
            SystemBarsHelper.hideSystemBars(
                window,
                hideStatusBar = false,
                hideNavigationBar = true
            )
            showToast("导航栏已隐藏")
        }

        // 获取系统栏高度
        binding.btnGetHeights.setOnClickListener {
            val statusBarHeight = SystemBarsHelper.getStatusBarHeight(window)
            val navBarHeight = SystemBarsHelper.getNavigationBarHeight(window)
            
            binding.tvInfo.text = buildString {
                appendLine("📏 系统栏高度")
                appendLine("状态栏: ${statusBarHeight}px (${pxToDp(statusBarHeight)}dp)")
                appendLine("导航栏: ${navBarHeight}px (${pxToDp(navBarHeight)}dp)")
            }
        }

        // 检测导航模式
        binding.btnCheckNavMode.setOnClickListener {
            displayNavigationModeInfo()
        }

        // 切换深色主题（需要重建 Activity）
        binding.btnToggleTheme.setOnClickListener {
            showToast("主题切换需要重建 Activity，请查看 DarkThemeDemoActivity")
        }
    }

    // ==================== 信息显示 ====================

    /**
     * 显示系统信息
     */
    private fun displaySystemInfo() {
        val statusBarHeight = SystemBarsHelper.getStatusBarHeight(window)
        val navBarHeight = SystemBarsHelper.getNavigationBarHeight(window)
        val navMode = NavigationBarUtils.getNavigationMode(this)
        
        binding.tvInfo.text = buildString {
            appendLine("📱 设备信息")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📏 系统栏高度")
            appendLine("  状态栏: ${statusBarHeight}px (${pxToDp(statusBarHeight)}dp)")
            appendLine("  导航栏: ${navBarHeight}px (${pxToDp(navBarHeight)}dp)")
            appendLine()
            appendLine("🧭 导航模式")
            appendLine("  当前模式: ${getNavModeName(navMode)}")
            appendLine("  手势导航: ${if (NavigationBarUtils.isGestureNavigation(this@DemoEdgeActivity)) "是" else "否"}")
            appendLine("  有可见导航栏: ${if (NavigationBarUtils.hasVisibleNavigationBar(this@DemoEdgeActivity)) "是" else "否"}")
        }
    }

    /**
     * 显示导航模式详细信息
     */
    private fun displayNavigationModeInfo() {
        val navMode = NavigationBarUtils.getNavigationMode(this)
        
        binding.tvInfo.text = buildString {
            appendLine("🧭 导航模式详情")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("当前模式: ${getNavModeName(navMode)}")
            appendLine()
            appendLine("模式判断结果:")
            appendLine("  • isGestureNavigation: ${NavigationBarUtils.isGestureNavigation(this@DemoEdgeActivity)}")
            appendLine("  • isThreeButtonNavigation: ${NavigationBarUtils.isThreeButtonNavigation(this@DemoEdgeActivity)}")
            appendLine("  • isTwoButtonNavigation: ${NavigationBarUtils.isTwoButtonNavigation(this@DemoEdgeActivity)}")
            appendLine("  • hasVisibleNavigationBar: ${NavigationBarUtils.hasVisibleNavigationBar(this@DemoEdgeActivity)}")
            appendLine()
            appendLine("💡 适配建议:")
            when (navMode) {
                NavigationBarUtils.NavigationMode.GESTURE -> {
                    appendLine("  手势导航模式，底部可以不留额外空间")
                    appendLine("  建议使用 applyNavigationBarInsetsIfNeeded()")
                }
                NavigationBarUtils.NavigationMode.TWO_BUTTON -> {
                    appendLine("  两按钮导航，需要为底部内容预留空间")
                }
                NavigationBarUtils.NavigationMode.THREE_BUTTON -> {
                    appendLine("  三按钮导航，需要为底部内容预留空间")
                }
            }
        }
    }

    // ==================== 工具方法 ====================

    private fun getNavModeName(mode: NavigationBarUtils.NavigationMode): String {
        return when (mode) {
            NavigationBarUtils.NavigationMode.THREE_BUTTON -> "三按钮导航 (返回/Home/最近)"
            NavigationBarUtils.NavigationMode.TWO_BUTTON -> "两按钮导航 (药丸导航)"
            NavigationBarUtils.NavigationMode.GESTURE -> "全面屏手势导航"
        }
    }

    private fun pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
