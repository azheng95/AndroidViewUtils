package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityCustomConfigEdgeBinding
import com.azheng.viewutils.edge.EdgeToEdgeConfig
import com.azheng.viewutils.edge.EdgeToEdgeHelper
import com.azheng.viewutils.edge.matchStatusBarHeight

/**
 * 自定义配置 Edge-to-Edge 示例
 * 
 * 使用 Builder 模式灵活配置各种选项
 */
class CustomConfigEdgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomConfigEdgeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 使用 Builder 自定义配置
        val config = EdgeToEdgeConfig.Builder()
            .darkStatusBar()           // 状态栏深色背景，亮色图标
            .lightNavigationBar()      // 导航栏亮色背景，深色图标
            .fitStatusBar(false)       // 不自动添加状态栏 padding（我们手动处理）
            .fitNavigationBar(true)    // 自动添加导航栏 padding
            .fitIme(false)             // 不处理软键盘
            .fitDisplayCutout(true)    // 处理刘海屏
            .build()
        
        EdgeToEdgeHelper.enable(this, config)
        
        super.onCreate(savedInstanceState)
        binding = ActivityCustomConfigEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 使用自定义配置应用 insets
        EdgeToEdgeHelper.applyInsets(binding.rootLayout, config)
        
        // 手动让状态栏占位 View 高度等于状态栏高度
        binding.statusBarSpace.matchStatusBarHeight()
        
        // 显示当前配置
        binding.tvCurrentConfig.text = """
            • 状态栏样式: Dark (亮色图标)
            • 导航栏样式: Light (深色图标)
            • 适配状态栏: false (手动处理)
            • 适配导航栏: true
            • 适配软键盘: false
            • 适配刘海屏: true
        """.trimIndent()
    }
}
