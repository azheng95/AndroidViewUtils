package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityDarkThemeEdgeBinding
import com.azheng.viewutils.edge.EdgeToEdgeConfig
import com.azheng.viewutils.edge.EdgeToEdgeHelper

/**
 * 深色主题 Edge-to-Edge 示例
 */
class DarkThemeEdgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDarkThemeEdgeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 使用深色主题配置
        EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.dark())
        
        super.onCreate(savedInstanceState)
        binding = ActivityDarkThemeEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        EdgeToEdgeHelper.applyInsets(binding.root)
    }
}
