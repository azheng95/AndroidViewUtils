// EdgeToEdgeExt.kt
package com.azheng.viewutils.edge

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

/**
 * Activity扩展函数
 */
fun Activity.applyEdgeToEdge(
    config: EdgeToEdgeConfig = EdgeToEdgeConfig.Companion.default(),
    contentView: View? = null
) {
    EdgeToEdgeHelper.apply(this, config, contentView)
}

fun Activity.applyEdgeToEdge(builder: EdgeToEdgeConfig.Builder.() -> Unit) {
    val config = EdgeToEdgeConfig.Builder().apply(builder).build()
    EdgeToEdgeHelper.apply(this, config)
}

/**
 * Fragment扩展函数
 */
fun Fragment.applyEdgeToEdge(
    config: EdgeToEdgeConfig = EdgeToEdgeConfig.Companion.default(),
    contentView: View? = null
) {
    activity?.let { EdgeToEdgeHelper.apply(it, config, contentView) }
}

/**
 * View扩展函数 - 应用顶部padding（状态栏）
 */
fun View.applyStatusBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        view.updatePadding(top = statusBarInsets.top)
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * View扩展函数 - 应用底部padding（导航栏）
 */
fun View.applyNavigationBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        view.updatePadding(bottom = navBarInsets.bottom)
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * View扩展函数 - 应用系统栏padding（状态栏+导航栏）
 */
fun View.applySystemBarsPadding() {
    val initialPadding = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
    
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialPadding[0] + systemBars.left,
            top = initialPadding[1] + systemBars.top,
            right = initialPadding[2] + systemBars.right,
            bottom = initialPadding[3] + systemBars.bottom
        )
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * View扩展函数 - 应用IME(输入法) padding
 */
fun View.applyImePadding() {
    val initialPaddingBottom = paddingBottom
    
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        
        // 取IME和导航栏的较大值
        val bottomPadding = maxOf(imeInsets.bottom, navInsets.bottom)
        view.updatePadding(bottom = initialPaddingBottom + bottomPadding)
        
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * View扩展函数 - 设置状态栏高度
 */
fun View.setHeightToStatusBar() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.layoutParams = view.layoutParams.apply { 
            height = statusBarHeight 
        }
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}
