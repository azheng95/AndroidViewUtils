package com.azheng.viewutils.imageviewer

import android.app.Application
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.app.ActivityOptionsCompat
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader

class ImageViewer private constructor() {

    companion object {
        private var isInitialized = false

        /**
         * 初始化，在 Application 中调用
         */
        @JvmStatic
        fun init(application: Application) {
            if (!isInitialized) {
                BigImageViewer.initialize(GlideImageLoader.with(application))
                isInitialized = true
            }
        }

        /**
         * 获取 Builder
         */
        @JvmStatic
        fun with(context: Context): Builder {
            check(isInitialized) { "ImageViewer must be initialized first. Call ImageViewer.init(application) in your Application." }
            return Builder(context)
        }
    }

    /**
     * Builder 类
     */
    class Builder internal constructor(private val context: Context) {

        private val imageUrls = mutableListOf<String>()
        private var startPosition: Int = 0

        @ColorInt
        private var backgroundColor: Int = 0xFF000000.toInt()
        private var showIndicator: Boolean = true
        private var indicatorStyle: IndicatorStyle = IndicatorStyle.TEXT
        private var customIndicator: IIndicator? = null

        private var enableSwipeToDismiss: Boolean = true
        private var clickToClose: Boolean = true
        private var enableZoom: Boolean = true

        private var enablePageTransformer: Boolean = false
        private var animDuration: Long = 300L

        private var callback: ImageViewerCallback? = null

        // 动画配置
        private var enterAnim: Int = android.R.anim.fade_in
        private var exitAnim: Int = android.R.anim.fade_out
        private var activityOptions: ActivityOptionsCompat? = null
        private var useSharedElement: Boolean = false
        private var sharedElementView: View? = null
        private var sharedElementName: String? = null
        // ==================== 图片数据 ====================

        /** 设置单张图片 */
        fun setImage(url: String) = apply {
            imageUrls.clear()
            imageUrls.add(url)
        }

        /** 设置多张图片 */
        fun setImages(urls: List<String>) = apply {
            imageUrls.clear()
            imageUrls.addAll(urls)
        }

        /** 设置多张图片 (vararg) */
        fun setImages(vararg urls: String) = apply {
            imageUrls.clear()
            imageUrls.addAll(urls)
        }

        /** 设置起始位置 */
        fun setStartPosition(position: Int) = apply {
            this.startPosition = position
        }

        // ==================== UI 配置 ====================

        /** 设置背景颜色 */
        fun setBackgroundColor(@ColorInt color: Int) = apply {
            this.backgroundColor = color
        }

        /** 是否显示指示器 */
        fun showIndicator(show: Boolean) = apply {
            this.showIndicator = show
        }

        /** 设置指示器样式 */
        fun setIndicatorStyle(style: IndicatorStyle) = apply {
            this.indicatorStyle = style
        }

        /** 设置自定义指示器 */
        fun setCustomIndicator(indicator: IIndicator) = apply {
            this.customIndicator = indicator
        }

        // ==================== 行为配置 ====================

        /** 点击图片关闭 */
        fun setClickToClose(enable: Boolean) = apply {
            this.clickToClose = enable
        }

        /** 下滑关闭 (暂未实现) */
        fun setSwipeToDismiss(enable: Boolean) = apply {
            this.enableSwipeToDismiss = enable
        }

        /** 启用缩放 */
        fun setEnableZoom(enable: Boolean) = apply {
            this.enableZoom = enable
        }


        // ==================== 动画配置 ====================

        /** 设置进入动画 */
        fun setEnterAnim(anim: Int) = apply {
            this.enterAnim = anim
        }

        /** 设置退出动画 */
        fun setExitAnim(anim: Int) = apply {
            this.exitAnim = anim
        }

        /** 同时设置进入和退出动画 */
        fun setAnimation(enterAnim: Int, exitAnim: Int) = apply {
            this.enterAnim = enterAnim
            this.exitAnim = exitAnim
        }

        /** 使用自定义 ActivityOptionsCompat */
        fun setActivityOptions(options: ActivityOptionsCompat) = apply {
            this.activityOptions = options
        }

        /** 设置共享元素过渡动画 */
        fun setSharedElement(view: View, transitionName: String) = apply {
            this.useSharedElement = true
            this.sharedElementView = view
            this.sharedElementName = transitionName
        }

        /** 使用缩放动画（从指定 View 位置缩放） */
        fun setScaleAnimation(sourceView: View) = apply {
            if (context is android.app.Activity) {
                val location = IntArray(2)
                sourceView.getLocationOnScreen(location)
                this.activityOptions = ActivityOptionsCompat.makeScaleUpAnimation(
                    sourceView,
                    0, 0,
                    sourceView.width,
                    sourceView.height
                )
            }
        }

        /** 使用剪裁揭示动画（圆形展开） */
        fun setClipRevealAnimation(sourceView: View) = apply {
            if (context is android.app.Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.activityOptions = ActivityOptionsCompat.makeClipRevealAnimation(
                    sourceView,
                    sourceView.width / 2,
                    sourceView.height / 2,
                    0, 0
                )
            }
        }

        /** 禁用动画 */
        fun disableAnimation() = apply {
            this.enterAnim = 0
            this.exitAnim = 0
        }

        // ==================== 回调 ====================

        /** 设置回调监听 */
        fun setCallback(callback: ImageViewerCallback) = apply {
            this.callback = callback
        }

        // ==================== 启动 ====================

        /** 显示图片浏览器 */
        fun show() {
            if (imageUrls.isEmpty()) {
                throw IllegalArgumentException("Image urls cannot be empty!")
            }

            val config = ViewerConfig(
                imageUrls = imageUrls.toList(),
                startPosition = startPosition.coerceIn(0, imageUrls.size - 1),
                backgroundColor = backgroundColor,
                showIndicator = showIndicator,
                indicatorStyle = indicatorStyle,
                enableSwipeToDismiss = enableSwipeToDismiss,
                clickToClose = clickToClose,
                enableZoom = enableZoom,
                enablePageTransformer = enablePageTransformer,
                animDuration = animDuration,
                enterAnim = enterAnim,
                exitAnim = exitAnim
            )

            ImageViewerActivity.globalCallback = callback
            ImageViewerActivity.customIndicator = customIndicator

            // 构建 ActivityOptions
            val options = when {
                activityOptions != null -> activityOptions?.toBundle()
                useSharedElement && sharedElementView != null && context is android.app.Activity -> {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context,
                        sharedElementView!!,
                        sharedElementName ?: "image_transition"
                    ).toBundle()
                }

                else -> null
            }

            ImageViewerActivity.start(context, config, options)
        }
    }
}
