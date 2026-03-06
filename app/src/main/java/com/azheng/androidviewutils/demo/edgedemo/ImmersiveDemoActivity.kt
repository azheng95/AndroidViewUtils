package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import com.azheng.androidviewutils.demo.databinding.ActivityImmersiveDemoBinding
import com.azheng.viewutils.edge.*

/**
 * 沉浸式全屏示例
 * 
 * 适用于视频播放、图片查看、游戏等场景
 */
class ImmersiveDemoActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivityImmersiveDemoBinding
    private var isImmersive = false

    /**
     * 使用沉浸式配置
     * 内容会延伸到系统栏下方
     */
    override fun getEdgeToEdgeConfig(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig.immersive()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImmersiveDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 沉浸式模式不需要应用 Insets（内容延伸到系统栏下方）

        binding.root.setOnClickListener {
            toggleImmersive()
        }

        binding.tvHint.text = "点击屏幕切换沉浸式模式\n\n当前: 显示系统栏"
    }

    private fun toggleImmersive() {
        isImmersive = !isImmersive
        
        if (isImmersive) {
            SystemBarsHelper.hideSystemBars(window)
            binding.tvHint.text = "点击屏幕切换沉浸式模式\n\n当前: 沉浸式（隐藏系统栏）"
        } else {
            SystemBarsHelper.showSystemBars(window)
            binding.tvHint.text = "点击屏幕切换沉浸式模式\n\n当前: 显示系统栏"
        }
    }
}
