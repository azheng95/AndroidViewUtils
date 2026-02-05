package com.azheng.viewutils.imageviewer

import androidx.annotation.ColorInt
import java.io.Serializable

data class ViewerConfig(
    // 图片数据
    val imageUrls: List<String> = emptyList(),
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
    val enterAnim: Int = android.R.anim.fade_in,      // 进入动画
    val exitAnim: Int = android.R.anim.fade_out,      // 退出动画
    val useActivityOptions: Boolean = true            // 是否使用 ActivityOptions
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

enum class IndicatorStyle : Serializable {
    TEXT,
    DOT,
    NONE
}
