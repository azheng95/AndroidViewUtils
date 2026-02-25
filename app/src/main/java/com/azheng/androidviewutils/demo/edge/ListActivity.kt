package com.azheng.androidviewutils.demo.edge

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.azheng.androidviewutils.demo.R
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.edge.applyNavigationBarPadding

/**
 * RecyclerView 列表页
 * - 列表可以滑动到导航栏下方
 * - 最后一项不被遮挡
 */
class ListActivity : BaseEdgeActivity() {

    // 不自动适配导航栏（由 RecyclerView 自己处理）
    override fun isFitNavigationBar(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            // 添加底部 padding，并允许内容滑动到 padding 区域
            applyNavigationBarPadding()
            clipToPadding = false  // 关键！允许滑动到 padding 区域
        }
    }
}
