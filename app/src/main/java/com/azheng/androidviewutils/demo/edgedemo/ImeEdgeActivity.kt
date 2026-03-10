package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityImeEdgeBinding
import com.azheng.viewutils.edge.EdgeToEdgeConfig
import com.azheng.viewutils.edge.EdgeToEdgeHelper
import com.azheng.viewutils.edge.applyImePadding
import com.azheng.viewutils.edge.applyStatusBarPadding

/**
 * 软键盘适配 Edge-to-Edge 示例
 * 
 * 当软键盘弹出时，底部按钮会自动上移避免被遮挡
 */
class ImeEdgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImeEdgeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 使用带 IME 适配的配置
        EdgeToEdgeHelper.enable(this, EdgeToEdgeConfig.withIme())
        
        super.onCreate(savedInstanceState)
        binding = ActivityImeEdgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 方式1：使用配置自动应用
        // EdgeToEdgeHelper.applyInsets(binding.root, EdgeToEdgeConfig.withIme())
        
        // 方式2：分别处理不同区域
        binding.rootLayout.applyStatusBarPadding()
        binding.bottomContainer.applyImePadding()
    }
}
