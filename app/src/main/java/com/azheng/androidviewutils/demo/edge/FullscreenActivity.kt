package com.azheng.androidviewutils.demo.edge

import android.os.Bundle
import android.view.View
import com.azheng.androidviewutils.demo.R
import com.azheng.viewutils.edge.EdgeToEdgeHelper

/**
 * 完全沉浸式
 * - 内容占满全屏
 * - 状态栏和导航栏覆盖在内容上
 */
class FullscreenActivity : BaseEdgeActivity() {

    override fun isFitStatusBar(): Boolean = false
    override fun isFitNavigationBar(): Boolean = false
    override fun isLightStatusBar(): Boolean = false
    override fun isLightNavigationBar(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_fullscreen)

        // 点击切换系统栏显示/隐藏
//        findViewById<View>(R.id.content).setOnClickListener {
//            toggleSystemBars()
//        }
    }

    private var isSystemBarsVisible = true

    private fun toggleSystemBars() {
        if (isSystemBarsVisible) {
            EdgeToEdgeHelper.hideSystemBars(this)
        } else {
            EdgeToEdgeHelper.showSystemBars(this)
        }
        isSystemBarsVisible = !isSystemBarsVisible
    }
}
