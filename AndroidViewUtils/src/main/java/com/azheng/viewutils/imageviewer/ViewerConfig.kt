package com.azheng.viewutils.imageviewer

import androidx.annotation.ColorInt
import java.io.Serializable

data class ViewerConfig(
    // 图片数据 - 支持多种类型
    val imageSources: List<ImageSource> = emptyList(),
    val startPosition: Int = 0,

    // UI配置
    @ColorInt val backgroundColor: Int = 0xFF000000.toInt(),
    val showIndicator: Boolean = true,
    val indicatorStyle: IndicatorStyle = IndicatorStyle.TEXT,

    // 行为配置
    val enableSwipeToDismiss: Boolean = true,
    val clickToClose: Boolean = true,
    val enableZoom: Boolean = true,

    // 动画配置
    val animDuration: Long = 300L,
    val enablePageTransformer: Boolean = false,
    val enterAnim: Int = android.R.anim.fade_in,
    val exitAnim: Int = android.R.anim.fade_out,
    val useActivityOptions: Boolean = true
) : Serializable {

    /**
     * 兼容性属性：获取图片URL字符串列表
     */
    val imageUrls: List<String>
        get() = imageSources.map { it.toDisplayString() }

    /**
     * 获取指定位置的图片源
     */
    fun getImageSource(position: Int): ImageSource? {
        return imageSources.getOrNull(position)
    }

    /**
     * 获取图片数量
     */
    val imageCount: Int
        get() = imageSources.size

    companion object {
        private const val serialVersionUID = 1L
    }
}

enum class IndicatorStyle : Serializable {
    TEXT,
    DOT,
    NONE;

    companion object {
        private const val serialVersionUID = 1L
    }
}
