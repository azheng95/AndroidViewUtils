package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityExtensionDemoBinding
import com.azheng.viewutils.edge.*

/**
 * 扩展函数使用示例
 * 
 * 展示各种便捷的扩展函数用法
 */
class ExtensionDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExtensionDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 使用扩展函数启用 Edge-to-Edge
        enableEdgeToEdge {
            lightStatusBar()
            lightNavigationBar()
            fitStatusBar(true)
            fitNavigationBar(true)
        }
        
        super.onCreate(savedInstanceState)
        binding = ActivityExtensionDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 使用扩展函数应用 insets
        binding.tvToolbar.applyStatusBarPadding()
        binding.bottomBar.applyNavigationBarPaddingIfNeeded()
        
        setupSystemInfo()
        setupClickListeners()
    }

    private fun setupSystemInfo() {
        // 使用扩展函数获取系统栏高度
        val statusBarHeight = getStatusBarHeight()
        val navBarHeight = getNavigationBarHeight()
        
        binding.tvSystemInfo.text = """
            📊 系统栏信息
            
            状态栏高度: ${statusBarHeight}px
            导航栏高度: ${navBarHeight}px
            导航模式: ${getNavigationModeText()}
        """.trimIndent()
    }

    private fun getNavigationModeText(): String {
        return when (NavigationBarUtils.getNavigationMode(this)) {
            NavigationBarUtils.NavigationMode.GESTURE -> "手势导航"
            NavigationBarUtils.NavigationMode.TWO_BUTTON -> "两按钮导航"
            NavigationBarUtils.NavigationMode.THREE_BUTTON -> "三按钮导航"
        }
    }

    private fun setupClickListeners() {
        binding.btnHide.setOnClickListener {
            // 使用扩展函数隐藏系统栏
            hideSystemBars()
        }

        binding.btnShow.setOnClickListener {
            // 使用扩展函数显示系统栏
            showSystemBars()
        }
    }
}
