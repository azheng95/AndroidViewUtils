package com.azheng.androidviewutils.demo.edge

import android.os.Bundle
import com.azheng.androidviewutils.demo.R
import com.azheng.viewutils.edge.applyNavigationBarPadding
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 带 BottomNavigationView 的主页
 */
class HomeActivity : BaseEdgeActivity() {

    // 不自动适配导航栏（BottomNavigationView 会处理）
    override fun isFitNavigationBar(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // BottomNavigationView 添加导航栏 padding
        findViewById<BottomNavigationView>(R.id.bottomNav).applyNavigationBarPadding()
    }

    /**
     * 场景	isLightStatusBar	isFitStatusBar	isFitNavigationBar	isFitIme
     * 默认白色主题	true	true	true	false
     * 深色主题	false	true	true	false
     * 图片头部沉浸	false	false	true	false
     * 列表页(自定义滚动)	true	true	false	false
     * 聊天输入页	true	true	true	true
     * 全屏视频/图片	false	false	false	false
     * 带底部Tab主页	true	true	false	false
     */
}
