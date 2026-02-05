package com.azheng.androidviewutils.demo.edge

import android.os.Bundle
import android.view.View
import com.azheng.androidviewutils.demo.R
import com.azheng.viewutils.edge.applyNavigationBarPadding
import com.azheng.viewutils.edge.applyStatusBarPadding
import com.azheng.viewutils.edge.applySystemBarsPadding

/**
 * 不适配 content，而是适配特定的 View
 */
class SpecificViewActivity : BaseEdgeActivity() {

    // 关闭自动适配
    override fun isFitStatusBar(): Boolean = false
    override fun isFitNavigationBar(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_specific)

        // 手动指定需要适配的 View
//        findViewById<View>(R.id.mainContent).applySystemBarsPadding()
        
        // 或者分别适配
//        findViewById<View>(R.id.topBar).applyStatusBarPadding()
//        findViewById<View>(R.id.bottomBar).applyNavigationBarPadding()
    }
}
