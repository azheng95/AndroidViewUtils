package com.azheng.viewutils

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import androidx.annotation.ColorInt

object SpanMatchKeywordUtils {
    /**
     * 将文本中第一个与关键词匹配的字符标红（忽略大小写）
     * @param text 原始文本（如 "Grossus"）
     * @param keyword 搜索关键词（如 "s"）
     * @return 标红后的SpannableString
     */
    fun highlightFirstMatchKeyword(text: String, keyword: String, @ColorInt color: Int=Color.RED): SpannableString {
        val spannable = SpannableString(text)
        // 关键词为空/长度大于文本长度，直接返回原文本
        if (keyword.isEmpty() || keyword.length > text.length) {
            return spannable
        }

        // 忽略大小写，找到第一个匹配的起始索引
        val textLower = text.lowercase()
        val keywordLower = keyword.lowercase()
        val firstMatchStart = textLower.indexOf(keywordLower)

        // 找到匹配位置，标红对应区域
        if (firstMatchStart != -1) {
            val firstMatchEnd = firstMatchStart + keyword.length
            val redSpan = ForegroundColorSpan(color)
            // 设置标红范围（SPAN_EXCLUSIVE_EXCLUSIVE：不包含前后边界）
            spannable.setSpan(
                redSpan,
                firstMatchStart,
                firstMatchEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}