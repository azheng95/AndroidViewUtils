package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.R
import com.azheng.androidviewutils.demo.databinding.ActivityBasicEdgeBinding
import com.azheng.viewutils.edge.EdgeToEdgeHelper

/**
 * 基础 Edge-to-Edge 示例
 * 
 * 演示最简单的 Edge-to-Edge 使用方式
 */
class BasicEdgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBasicEdgeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. 在 super.onCreate() 之前启用 Edge-to-Edge
        EdgeToEdgeHelper.enable(this)
        
        super.onCreate(savedInstanceState)
        
        // 2. 设置布局
        binding = ActivityBasicEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 3. 应用系统栏 Insets
        EdgeToEdgeHelper.applyInsets(binding.root)
    }
}
