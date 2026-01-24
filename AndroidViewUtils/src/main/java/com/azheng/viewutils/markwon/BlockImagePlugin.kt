package com.azheng.viewutils.markwon


import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.image.AsyncDrawableSpan

/**
 * 图片独占一行插件（推荐方案）
 */
class BlockImagePlugin : AbstractMarkwonPlugin() {

    override fun afterSetText(textView: TextView) {
        val text = textView.text
        if (text is Spannable) {
            val spannable = SpannableStringBuilder(text)

            // 查找所有图片 Span
            val imageSpans = spannable.getSpans(0, spannable.length, AsyncDrawableSpan::class.java)

            // 需要插入换行的位置（从后往前处理，避免位置偏移）
            val insertions = mutableListOf<Pair<Int, String>>()

            for (span in imageSpans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)

                // 图片前：如果前面不是换行符，添加换行
                if (start > 0 && spannable[start - 1] != '\n') {
                    insertions.add(Pair(start, "\n"))
                }

                // 图片后：如果后面不是换行符，添加换行
                if (end < spannable.length && spannable[end] != '\n') {
                    insertions.add(Pair(end, "\n"))
                }
            }

            // 从后往前插入，避免位置偏移问题
            insertions.sortedByDescending { it.first }.forEach { (position, char) ->
                spannable.insert(position, char)
            }

            textView.text = spannable
        }
    }
}
