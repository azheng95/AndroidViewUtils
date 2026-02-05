package com.azheng.viewutils.imageviewer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ScalePageTransformer(
    private val minScale: Float = 0.85f,
    private val minAlpha: Float = 0.5f
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        when {
            position < -1 -> {
                page.alpha = 0f
            }
            position <= 1 -> {
                val scaleFactor = maxOf(minScale, 1 - abs(position))
                val vertMargin = pageHeight * (1 - scaleFactor) / 2
                val horzMargin = pageWidth * (1 - scaleFactor) / 2

                page.translationX = if (position < 0) {
                    horzMargin - vertMargin / 2
                } else {
                    -horzMargin + vertMargin / 2
                }

                page.scaleX = scaleFactor
                page.scaleY = scaleFactor

                page.alpha = minAlpha + (scaleFactor - minScale) / (1 - minScale) * (1 - minAlpha)
            }
            else -> {
                page.alpha = 0f
            }
        }
    }
}
