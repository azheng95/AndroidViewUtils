package com.azheng.androidviewutils.demo.edge

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.azheng.androidviewutils.demo.R
import com.azheng.viewutils.edge.BaseEdgeActivity

/**
 * 根据页面状态动态切换
 */
class DynamicActivity : BaseEdgeActivity() {

    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_dynamic)

//        findViewById<Button>(R.id.toggleButton).setOnClickListener {
//            isDarkMode = !isDarkMode
//            applyTheme()
//        }
    }

    private fun applyTheme() {
        // 动态更新配置
//        updateEdgeToEdge {
//            lightStatusBar(!isDarkMode)
//            lightNavigationBar(!isDarkMode)
//        }
        
        // 更新 UI
//        val bgColor = if (isDarkMode) Color.BLACK else Color.WHITE
//        findViewById<View>(R.id.rootView).setBackgroundColor(bgColor)
    }
}
