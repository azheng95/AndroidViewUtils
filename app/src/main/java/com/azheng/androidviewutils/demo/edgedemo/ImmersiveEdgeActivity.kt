package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityImmersiveEdgeBinding
import com.azheng.viewutils.edge.EdgeToEdgeConfig
import com.azheng.viewutils.edge.EdgeToEdgeHelper
import com.azheng.viewutils.edge.applyNavigationBarPadding

/**
 * 沉浸式 Edge-to-Edge 示例
 * 
 * 内容延伸到系统栏下方，适用于图片查看器、视频播放等场景
 */
class ImmersiveEdgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImmersiveEdgeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 使用沉浸式配置（不添加系统栏 padding）
        EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.immersive())
        
        super.onCreate(savedInstanceState)
        binding = ActivityImmersiveEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 注意：沉浸式模式下，根布局不应用 insets
        // 但底部按钮需要单独处理，避免被导航栏遮挡
        binding.btnClose.applyNavigationBarPadding()
        
        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
