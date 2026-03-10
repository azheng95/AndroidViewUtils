package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityAutoThemeEdgeBinding
import com.azheng.viewutils.edge.EdgeToEdgeConfig
import com.azheng.viewutils.edge.EdgeToEdgeHelper

/**
 * 自动主题 Edge-to-Edge 示例
 * 
 * 系统栏样式会自动跟随系统深色模式
 */
class AutoThemeEdgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutoThemeEdgeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 使用自动主题配置
        EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.auto())
        
        super.onCreate(savedInstanceState)
        binding = ActivityAutoThemeEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        EdgeToEdgeHelper.applyInsets(binding.root)
    }
}
