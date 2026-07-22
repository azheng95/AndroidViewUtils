package com.azheng.viewutils.markwon

import android.content.Context
import android.util.TypedValue
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.core.MarkwonTheme

/**
 * 自定义文字大小插件
 */
class CustomTextSizePlugin(
    private val textSizeSp: Float = 16f,      // 正文字号 (sp)
    private val headingBreakHeight: Int = 0,   // 标题下方间距
    context: Context,
) : AbstractMarkwonPlugin() {

    private val displayMetrics = context.applicationContext.resources.displayMetrics

    override fun configureTheme(builder: MarkwonTheme.Builder) {
        builder
            // ⭐ 标题字号倍数（相对于正文）
            // H1 = textSize * 2.0, H2 = textSize * 1.8, ...
            .headingTextSizeMultipliers(
                floatArrayOf(
                    3.0f,   // H1
                    2f,   // H2
                    1.8f,   // H3
                    1.6f,   // H4
                    1.2f,   // H5
                    1.0f    // H6
                )
            )
            // 标题下方分隔线高度（0 = 不显示）
            .headingBreakHeight(headingBreakHeight)
            // 代码块字号（相对于正文的比例）
            .codeTextSize(spToPx(textSizeSp * 0.9f))
            // 列表项缩进
            .bulletWidth(spToPx(textSizeSp * 0.4f))
    }

    private fun spToPx(value: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, displayMetrics).toInt()
    }
}
