package com.azheng.viewutils.imageviewer

import android.content.Context
import android.view.View

interface IIndicator {
    /** 创建指示器View */
    fun createView(context: Context): View
    
    /** 更新指示器状态 */
    fun onPageSelected(position: Int, total: Int)
    
    /** 设置总数 */
    fun setTotal(total: Int)
}
