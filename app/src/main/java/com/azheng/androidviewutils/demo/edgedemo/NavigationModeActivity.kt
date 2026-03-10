package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivityNavigationModeBinding
import com.azheng.viewutils.edge.EdgeToEdgeHelper
import com.azheng.viewutils.edge.NavigationBarUtils

/**
 * 导航栏模式检测示例
 * 
 * 展示如何检测和适配不同的导航模式
 */
class NavigationModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        EdgeToEdgeHelper.enable(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        EdgeToEdgeHelper.applyInsets(binding.rootLayout)
        
        displayNavigationMode()
    }

    private fun displayNavigationMode() {
        val mode = NavigationBarUtils.getNavigationMode(this)
        
        val (modeText, emoji) = when (mode) {
            NavigationBarUtils.NavigationMode.GESTURE -> "手势导航" to "👆"
            NavigationBarUtils.NavigationMode.TWO_BUTTON -> "两按钮导航" to "⬅️ ⚪"
            NavigationBarUtils.NavigationMode.THREE_BUTTON -> "三按钮导航" to "◀️ ⚪ ⬛"
        }
        
        binding.tvNavigationMode.text = "$emoji\n$modeText"
        
        binding.tvModeDescription.text = when (mode) {
            NavigationBarUtils.NavigationMode.GESTURE -> """
                ✅ 当前设备使用手势导航
                
                特点：
                • 从屏幕底部上滑返回主屏幕
                • 从左/右边缘滑动返回
                • 导航栏高度较小（仅手势指示条）
                • 建议：不为底部内容添加额外 padding
            """.trimIndent()
            
            NavigationBarUtils.NavigationMode.TWO_BUTTON -> """
                ⚠️ 当前设备使用两按钮导航（药丸导航）
                
                特点：
                • Android 9 (Pie) 引入的导航方式
                • 有 Home 键和返回手势
                • 导航栏高度约 48dp
                • 建议：为底部内容添加导航栏 padding
            """.trimIndent()
            
            NavigationBarUtils.NavigationMode.THREE_BUTTON -> """
                ℹ️ 当前设备使用三按钮导航
                
                特点：
                • 传统导航方式（返回、Home、最近任务）
                • 导航栏高度约 48dp
                • 建议：为底部内容添加导航栏 padding
            """.trimIndent()
        }
    }
}
