package com.azheng.viewutils.imageviewer

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

class DefaultIndicator(
    private val style: IndicatorStyle
) : IIndicator {

    private var textView: TextView? = null
    private var total: Int = 0

    override fun createView(context: Context): View {
        return when (style) {
            IndicatorStyle.TEXT -> createTextIndicator(context)
            IndicatorStyle.DOT -> createDotIndicator(context)
            IndicatorStyle.NONE -> View(context).apply { visibility = View.GONE }
        }
    }

    private fun createTextIndicator(context: Context): View {
        return TextView(context).apply {
            textView = this
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setBackgroundColor(0x66000000)
            setPadding(dp2px(context, 16), dp2px(context, 8), dp2px(context, 16), dp2px(context, 8))
            gravity = Gravity.CENTER
            
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dp2px(context, 32)
            }
        }
    }

    private fun createDotIndicator(context: Context): View {
        // 简化实现，可替换为更复杂的圆点指示器
        return createTextIndicator(context)
    }

    override fun onPageSelected(position: Int, total: Int) {
        this.total = total
        textView?.text = "${position + 1} / $total"
        textView?.visibility = if (total > 1) View.VISIBLE else View.GONE
    }

    override fun setTotal(total: Int) {
        this.total = total
    }

    private fun dp2px(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
