package com.azheng.viewutils.markwon

import android.graphics.Rect
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolverDef

/**
 * 自定义图片插件：让图片宽度填满容器（match_parent效果）
 */
class MatchParentImagePlugin : AbstractMarkwonPlugin() {
    
    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.imageSizeResolver(MatchParentImageSizeResolver())
    }
    
    /**
     * 自定义 ImageSizeResolver
     * 将图片宽度设为容器宽度，高度按比例缩放
     */
    private class MatchParentImageSizeResolver : ImageSizeResolverDef() {
        
        override fun resolveImageSize(
            imageSize: ImageSize?,
            imageBounds: Rect,
            canvasWidth: Int,
            textSize: Float
        ): Rect {
            // canvasWidth 就是 TextView 的可用宽度
            if (canvasWidth > 0 && imageBounds.width() > 0 && imageBounds.height() > 0) {
                // 计算缩放比例
                val ratio = canvasWidth.toFloat() / imageBounds.width().toFloat()
                val newHeight = (imageBounds.height() * ratio).toInt()
                return Rect(0, 0, canvasWidth, newHeight)
            }
            return imageBounds
        }

    }
}
