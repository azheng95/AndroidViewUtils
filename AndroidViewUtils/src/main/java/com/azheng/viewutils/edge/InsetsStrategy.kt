// WindowInsetsHelper.kt
package com.azheng.viewutils.edge

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * WindowInsets适配策略接口
 */
interface InsetsStrategy {
    fun apply(view: View, insets: Insets)
}

/**
 * Padding适配策略
 */
class PaddingInsetsStrategy(
    private val applyTop: Boolean = true,
    private val applyBottom: Boolean = true,
    private val applyLeft: Boolean = true,
    private val applyRight: Boolean = true
) : InsetsStrategy {
    
    // 保存原始padding
    private var originalPadding: IntArray? = null

    override fun apply(view: View, insets: Insets) {
        if (originalPadding == null) {
            originalPadding = intArrayOf(
                view.paddingLeft, view.paddingTop,
                view.paddingRight, view.paddingBottom
            )
        }
        
        val original = originalPadding!!
        view.updatePadding(
            left = original[0] + if (applyLeft) insets.left else 0,
            top = original[1] + if (applyTop) insets.top else 0,
            right = original[2] + if (applyRight) insets.right else 0,
            bottom = original[3] + if (applyBottom) insets.bottom else 0
        )
    }
}

/**
 * Margin适配策略
 */
class MarginInsetsStrategy(
    private val applyTop: Boolean = true,
    private val applyBottom: Boolean = true,
    private val applyLeft: Boolean = true,
    private val applyRight: Boolean = true
) : InsetsStrategy {
    
    private var originalMargin: IntArray? = null

    override fun apply(view: View, insets: Insets) {
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        
        if (originalMargin == null) {
            originalMargin = intArrayOf(
                lp.leftMargin, lp.topMargin,
                lp.rightMargin, lp.bottomMargin
            )
        }
        
        val original = originalMargin!!
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = original[0] + if (applyLeft) insets.left else 0
            topMargin = original[1] + if (applyTop) insets.top else 0
            rightMargin = original[2] + if (applyRight) insets.right else 0
            bottomMargin = original[3] + if (applyBottom) insets.bottom else 0
        }
    }
}

/**
 * WindowInsets辅助类
 * 处理各种Insets类型
 */
object WindowInsetsHelper {

    /**
     * 获取Insets类型掩码
     */
    fun getInsetTypeMask(config: EdgeToEdgeConfig): Int {
        var typeMask = 0
        
        if (config.fitStatusBar) {
            typeMask = typeMask or WindowInsetsCompat.Type.statusBars()
        }
        if (config.fitNavigationBar) {
            typeMask = typeMask or WindowInsetsCompat.Type.navigationBars()
        }
        if (config.fitIme) {
            typeMask = typeMask or WindowInsetsCompat.Type.ime()
        }
        if (config.fitDisplayCutout) {
            typeMask = typeMask or WindowInsetsCompat.Type.displayCutout()
        }
        
        return typeMask
    }

    /**
     * 应用WindowInsets到View
     */
    fun applyInsetsToView(
        view: View,
        config: EdgeToEdgeConfig,
        strategy: InsetsStrategy
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val typeMask = getInsetTypeMask(config)
            val insets = windowInsets.getInsets(typeMask)
            
            strategy.apply(v, insets)
            
            // 返回consumed的insets
            WindowInsetsCompat.CONSUMED
        }
        
        // 请求重新应用insets
        if (view.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    ViewCompat.requestApplyInsets(v)
                    v.removeOnAttachStateChangeListener(this)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    /**
     * 为View添加padding适配
     */
    fun applyPaddingInsets(
        view: View,
        config: EdgeToEdgeConfig,
        applyTop: Boolean = config.fitStatusBar,
        applyBottom: Boolean = config.fitNavigationBar
    ) {
        applyInsetsToView(
            view, config,
            PaddingInsetsStrategy(applyTop = applyTop, applyBottom = applyBottom)
        )
    }

    /**
     * 为View添加margin适配
     */
    fun applyMarginInsets(
        view: View,
        config: EdgeToEdgeConfig,
        applyTop: Boolean = config.fitStatusBar,
        applyBottom: Boolean = config.fitNavigationBar
    ) {
        applyInsetsToView(
            view, config,
            MarginInsetsStrategy(applyTop = applyTop, applyBottom = applyBottom)
        )
    }
}
