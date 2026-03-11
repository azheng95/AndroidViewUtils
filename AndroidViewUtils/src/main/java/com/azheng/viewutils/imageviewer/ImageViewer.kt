package com.azheng.viewutils.imageviewer

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityOptionsCompat
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.loader.glide.GlideImageLoader
import java.io.File

/**
 * 图片查看器
 */
class ImageViewer private constructor() {

    companion object {
        private const val TAG = "ImageViewer"

        private var isInitialized = false
        private var initError: String? = null

        /**
         * 检查 BigImageViewer 依赖是否可用
         */
        private fun isBigImageViewerAvailable(): Boolean {
            return try {
                Class.forName("com.github.piasy.biv.BigImageViewer")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }

        /**
         * 获取缺失依赖的提示信息
         */
        private fun getMissingDependenciesMessage(): String {
            return buildString {
                appendLine("ImageViewer: 缺少必要的依赖库！")
                appendLine("请在 build.gradle 中添加以下依赖：")
                appendLine()
                appendLine("implementation(\"com.github.piasy:BigImageViewer:Tag\")")
                appendLine("implementation(\"com.github.piasy:GlideImageLoader:Tag\")")
                appendLine("implementation(\"com.github.piasy:GlideImageViewFactory:Tag\")")
                appendLine()
                appendLine("同时确保已添加 JitPack 仓库：")
                appendLine("maven { url 'https://jitpack.io' }")
            }
        }

        /**
         * 初始化 ImageViewer，使用默认的 GlideImageLoader
         * @param application Application 实例
         */
        @JvmStatic
        fun init(application: Application) {
            init(application, null)
        }

        /**
         * 初始化 ImageViewer，可传入自定义 ImageLoader
         * @param application Application 实例
         * @param imageLoader 自定义的 ImageLoader，为 null 时使用默认的 GlideImageLoader
         */
        @JvmStatic
        fun init(application: Application, imageLoader: ImageLoader?) {
            if (isInitialized) {
                return
            }
            // 检查依赖是否存在
            if (!isBigImageViewerAvailable()) {
                initError = getMissingDependenciesMessage()
                Log.e(TAG, initError!!)
                return
            }

            // 正常初始化
            try {
                val loader = imageLoader ?: GlideImageLoader.with(application)
                BigImageViewer.initialize(loader)
                isInitialized = true
                initError = null
                Log.d(TAG, "ImageViewer initialized successfully")
            } catch (e: Exception) {
                initError = "ImageViewer 初始化失败: ${e.message}\n${getMissingDependenciesMessage()}"
                Log.e(TAG, initError!!, e)
            }
        }

        /**
         * 检查是否已初始化
         */
        @JvmStatic
        fun isInitialized(): Boolean = isInitialized

        /**
         * 获取初始化错误信息
         */
        @JvmStatic
        fun getInitError(): String? = initError

        @JvmStatic
        fun with(context: Context): Builder {
            // 检查是否有初始化错误
            if (initError != null) {
                throw IllegalStateException(initError)
            }

            // 检查是否已初始化
            if (!isInitialized) {
                // 尝试自动检测问题
                if (!isBigImageViewerAvailable()) {
                    throw IllegalStateException(getMissingDependenciesMessage())
                }
                throw IllegalStateException(
                    "ImageViewer must be initialized first. Call ImageViewer.init(application) in your Application."
                )
            }

            return Builder(context)
        }
    }

    class Builder internal constructor(private val context: Context) {

        private val imageSources = mutableListOf<ImageSource>()
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

        private var enterAnim: Int = android.R.anim.fade_in
        private var exitAnim: Int = android.R.anim.fade_out
        private var activityOptions: ActivityOptionsCompat? = null
        private var useSharedElement: Boolean = false
        private var sharedElementView: View? = null
        private var sharedElementName: String? = null

        // ==================== 图片数据 (String) ====================

        /** 设置单张图片 (String URL/路径) */
        fun setImage(url: String) = apply {
            imageSources.clear()
            imageSources.add(ImageSource.fromString(url))
        }

        /** 设置多张图片 (String List) */
        fun setImages(urls: List<String>) = apply {
            imageSources.clear()
            imageSources.addAll(urls.map { ImageSource.fromString(it) })
        }

        /** 设置多张图片 (String vararg) */
        fun setImages(vararg urls: String) = apply {
            imageSources.clear()
            imageSources.addAll(urls.map { ImageSource.fromString(it) })
        }

        // ==================== 图片数据 (Uri) ====================

        /** 设置单张图片 (Uri) */
        fun setImage(uri: Uri) = apply {
            imageSources.clear()
            imageSources.add(ImageSource.fromUri(uri))
        }

        /** 设置多张图片 (Uri List) */
        @JvmName("setImagesUri")
        fun setImages(uris: List<Uri>) = apply {
            imageSources.clear()
            imageSources.addAll(uris.map { ImageSource.fromUri(it) })
        }

        /** 设置多张图片 (Uri vararg) */
        fun setImageUris(vararg uris: Uri) = apply {
            imageSources.clear()
            imageSources.addAll(uris.map { ImageSource.fromUri(it) })
        }

        // ==================== 图片数据 (File) ====================

        /** 设置单张图片 (File) */
        fun setImage(file: File) = apply {
            imageSources.clear()
            imageSources.add(ImageSource.fromFile(file))
        }

        /** 设置多张图片 (File List) */
        @JvmName("setImagesFile")
        fun setImages(files: List<File>) = apply {
            imageSources.clear()
            imageSources.addAll(files.map { ImageSource.fromFile(it) })
        }

        /** 设置多张图片 (File vararg) */
        fun setImageFiles(vararg files: File) = apply {
            imageSources.clear()
            imageSources.addAll(files.map { ImageSource.fromFile(it) })
        }

        // ==================== 图片数据 (Resource ID) ====================

        /** 设置单张图片 (Resource ID) */
        fun setImage(@DrawableRes resId: Int) = apply {
            imageSources.clear()
            imageSources.add(ImageSource.fromResource(resId, context.packageName))
        }

        /** 设置多张图片 (Resource ID List) */
        @JvmName("setImagesRes")
        fun setImages(resIds: List<Int>) = apply {
            imageSources.clear()
            imageSources.addAll(resIds.map { ImageSource.fromResource(it, context.packageName) })
        }

        /** 设置多张图片 (Resource ID vararg) */
        fun setImageResources(vararg resIds: Int) = apply {
            imageSources.clear()
            imageSources.addAll(resIds.map { ImageSource.fromResource(it, context.packageName) })
        }

        // ==================== 图片数据 (ImageSource) ====================

        /** 设置单张图片 (ImageSource) */
        fun setImage(source: ImageSource) = apply {
            imageSources.clear()
            imageSources.add(source)
        }

        /** 设置多张图片 (ImageSource List) */
        @JvmName("setImagesSources")
        fun setImages(sources: List<ImageSource>) = apply {
            imageSources.clear()
            imageSources.addAll(sources)
        }

        /** 设置多张图片 (ImageSource vararg) */
        fun setImageSources(vararg sources: ImageSource) = apply {
            imageSources.clear()
            imageSources.addAll(sources)
        }

        // ==================== 混合类型支持 ====================

        /** 添加单张图片 (String) */
        fun addImage(url: String) = apply {
            imageSources.add(ImageSource.fromString(url))
        }

        /** 添加单张图片 (Uri) */
        fun addImage(uri: Uri) = apply {
            imageSources.add(ImageSource.fromUri(uri))
        }

        /** 添加单张图片 (File) */
        fun addImage(file: File) = apply {
            imageSources.add(ImageSource.fromFile(file))
        }

        /** 添加单张图片 (Resource ID) */
        fun addImage(@DrawableRes resId: Int) = apply {
            imageSources.add(ImageSource.fromResource(resId, context.packageName))
        }

        /** 添加单张图片 (ImageSource) */
        fun addImage(source: ImageSource) = apply {
            imageSources.add(source)
        }

        /** 清除所有图片 */
        fun clearImages() = apply {
            imageSources.clear()
        }

        // ==================== UI 配置 ====================

        /** 设置起始位置 */
        fun setStartPosition(position: Int) = apply {
            this.startPosition = position
        }

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

        /** 点击图片关闭 */
        fun setClickToClose(enable: Boolean) = apply {
            this.clickToClose = enable
        }

        /** 下滑关闭 */
        fun setSwipeToDismiss(enable: Boolean) = apply {
            this.enableSwipeToDismiss = enable
        }

        /** 启用缩放 */
        fun setEnableZoom(enable: Boolean) = apply {
            this.enableZoom = enable
        }

        /** 启用页面切换动画 */
        fun setEnablePageTransformer(enable: Boolean) = apply {
            this.enablePageTransformer = enable
        }

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

        /** 使用缩放动画 */
        fun setScaleAnimation(sourceView: View) = apply {
            if (context is android.app.Activity) {
                this.activityOptions = ActivityOptionsCompat.makeScaleUpAnimation(
                    sourceView, 0, 0, sourceView.width, sourceView.height
                )
            }
        }

        /** 使用剪裁揭示动画 */
        fun setClipRevealAnimation(sourceView: View) = apply {
            if (context is android.app.Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.activityOptions = ActivityOptionsCompat.makeClipRevealAnimation(
                    sourceView, sourceView.width / 2, sourceView.height / 2, 0, 0
                )
            }
        }

        /** 禁用动画 */
        fun disableAnimation() = apply {
            this.enterAnim = 0
            this.exitAnim = 0
        }

        /** 设置回调监听 */
        fun setCallback(callback: ImageViewerCallback) = apply {
            this.callback = callback
        }

        /** 显示图片浏览器 */
        fun show() {
            if (imageSources.isEmpty()) {
                throw IllegalArgumentException("Image sources cannot be empty!")
            }

            val config = ViewerConfig(
                imageSources = imageSources.toList(),
                startPosition = startPosition.coerceIn(0, imageSources.size - 1),
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

            val options = when {
                activityOptions != null -> activityOptions?.toBundle()
                useSharedElement && sharedElementView != null && context is android.app.Activity -> {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context, sharedElementView!!, sharedElementName ?: "image_transition"
                    ).toBundle()
                }
                else -> null
            }

            ImageViewerActivity.start(context, config, options)
        }
    }
}
