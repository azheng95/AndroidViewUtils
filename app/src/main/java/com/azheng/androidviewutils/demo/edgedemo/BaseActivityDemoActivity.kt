package com.azheng.androidviewutils.demo.edgedemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.azheng.androidviewutils.demo.databinding.ActivityBaseActivityDemoBinding
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.edge.EdgeToEdgeConfig

/**
 * BaseEdgeActivity 使用示例
 * 
 * 展示如何通过继承 BaseEdgeActivity 简化 Edge-to-Edge 配置
 */
class BaseActivityDemoActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivityBaseActivityDemoBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.tvCurrentSettings.text = """
            📋 当前页面配置
            
            • isEdgeToEdgeEnabled(): ${isEdgeToEdgeEnabled()}
            • needAdaptSystemBar(): ${needAdaptSystemBar()}
            • getInsetsTargetView(): ${if (getInsetsTargetView() != null) "自定义 View" else "android.R.id.content"}
            • getEdgeToEdgeConfig(): 深色主题
        """.trimIndent()
    }

    // 演示：自定义配置
    override fun getEdgeToEdgeConfig(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig.Builder()
            .darkStatusBar()
            .lightNavigationBar()
            .fitStatusBar(true)
            .fitNavigationBar(true)
            .build()
    }

    // 演示：自定义 Insets 目标 View
    override fun getInsetsTargetView(): View = binding.root
}
