package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import com.azheng.androidviewutils.demo.databinding.ActivitySimpleDemoBinding
import com.azheng.viewutils.edge.*

/**
 * 最简单的 Edge-to-Edge 使用示例
 * 
 * 展示最基础的用法，适合快速上手
 */
class SimpleDemoActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivitySimpleDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 一行代码完成整个布局的 Insets 适配
        applyInsets(binding.root)
    }
}
