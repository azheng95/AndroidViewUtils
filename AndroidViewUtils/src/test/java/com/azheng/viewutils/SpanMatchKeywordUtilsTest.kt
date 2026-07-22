package com.azheng.viewutils

import android.graphics.Color
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SpanMatchKeywordUtilsTest {

    @Test
    fun highlightsFirstMatchIgnoringCase() {
        val result = SpanMatchKeywordUtils.highlightFirstMatchKeyword(
            text = "Android android",
            keyword = "ANDROID",
            color = Color.RED
        )
        val span = result.getSpans(0, result.length, ForegroundColorSpan::class.java).single()

        assertEquals(0, result.getSpanStart(span))
        assertEquals(7, result.getSpanEnd(span))
        assertEquals(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, result.getSpanFlags(span))
    }
}
