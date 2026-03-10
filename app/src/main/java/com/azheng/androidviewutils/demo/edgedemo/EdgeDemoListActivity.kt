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

        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            // 基础示例
            btnBasicDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, BasicEdgeActivity::class.java))
            }

            btnDarkThemeDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, DarkThemeEdgeActivity::class.java))
            }

            btnAutoThemeDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, AutoThemeEdgeActivity::class.java))
            }

            // 高级示例
            btnImmersiveDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, ImmersiveEdgeActivity::class.java))
            }

            btnImeDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, ImeEdgeActivity::class.java))
            }

            btnCustomConfigDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, CustomConfigEdgeActivity::class.java))
            }

            // 工具类示例
            btnExtensionDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, ExtensionDemoActivity::class.java))
            }

            btnSystemBarsDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, SystemBarsHelperActivity::class.java))
            }

            btnNavigationModeDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, NavigationModeActivity::class.java))
            }

            // BaseEdgeActivity 示例
            btnBaseActivityDemo.setOnClickListener {
                startActivity(Intent(this@EdgeDemoListActivity, BaseActivityDemoActivity::class.java))
            }
        }
    }
}
