package com.azheng.androidviewutils.demo.edgedemo

import android.content.Intent
import android.os.Bundle
import com.azheng.androidviewutils.demo.databinding.ActivityEdgeDemoListBinding
import com.azheng.viewutils.edge.BaseEdgeActivity

/**
 * Edge-to-Edge Demo 列表入口页面
 * 
 * 用于跳转到各个示例 Activity
 */
class EdgeDemoListActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivityEdgeDemoListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdgeDemoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 应用 Insets 适配
        applyInsets(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 完整功能演示
        binding.btnDemoEdge.setOnClickListener {
            startActivity(Intent(this, DemoEdgeActivity::class.java))
        }

        // 简单用法示例
        binding.btnSimpleDemo.setOnClickListener {
            startActivity(Intent(this, SimpleDemoActivity::class.java))
        }

        // 扩展函数用法示例
        binding.btnExtensionDemo.setOnClickListener {
            startActivity(Intent(this, ExtensionDemoActivity::class.java))
        }

        // 沉浸式全屏示例
        binding.btnImmersiveDemo.setOnClickListener {
            startActivity(Intent(this, ImmersiveDemoActivity::class.java))
        }
    }
}
