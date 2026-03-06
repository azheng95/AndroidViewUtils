package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityExtensionDemoBinding
import com.azheng.viewutils.edge.*

/**
 * 使用扩展函数的示例
 * 
 * 不继承 BaseEdgeActivity，直接使用扩展函数
 */
class ExtensionDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExtensionDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 方式1: 使用默认配置
        // enableEdgeToEdge()

        // 方式2: 使用 DSL 配置
        enableEdgeToEdge {
            darkStatusBar()           // 深色状态栏
            lightNavigationBar()      // 亮色导航栏
            fitIme(true)              // 适配软键盘
        }

        super.onCreate(savedInstanceState)
        binding = ActivityExtensionDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 使用 View 扩展函数
        setupInsets()
        setupInfo()
    }

    private fun setupInsets() {
        // Toolbar 适配状态栏
        binding.toolbar.applyStatusBarPadding()

        // 底部区域智能适配导航栏
        binding.bottomContainer.applyNavigationBarPaddingIfNeeded()

        // 或者使用智能系统栏适配
        // binding.root.applySystemBarsPaddingSmartly()
    }

    private fun setupInfo() {
        // 使用 Activity 扩展函数获取系统栏高度
        val statusBarHeight = getStatusBarHeight()
        val navBarHeight = getNavigationBarHeight()

        binding.tvInfo.text = """
            状态栏高度: ${statusBarHeight}px
            导航栏高度: ${navBarHeight}px
            
            使用的扩展函数:
            • enableEdgeToEdge { }
            • view.applyStatusBarPadding()
            • view.applyNavigationBarPaddingIfNeeded()
            • activity.getStatusBarHeight()
            • activity.getNavigationBarHeight()
        """.trimIndent()

        // 隐藏/显示系统栏
        binding.btnHide.setOnClickListener { hideSystemBars() }
        binding.btnShow.setOnClickListener { showSystemBars() }
    }
}
