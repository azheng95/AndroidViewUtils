package com.azheng.viewutils.imagepicker

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

object ImagePicker {

    @Volatile
    private var application: Application? = null
    private var pendingCallback: WeakReference<(ImagePickerResult) -> Unit>? = null
    private var currentConfig: ImagePickerConfig = ImagePickerConfig.default()

    /**
     * 手动初始化（可选）
     * 优先级最高，可覆盖自动初始化
     */
    @JvmStatic
    fun init(app: Application) {
        application = app
    }

    /**
     * 内部自动初始化（供 App Startup 调用）
     */
    internal fun autoInit(context: Context) {
        if (application == null) {
            application = context.applicationContext as Application
        }
    }

    /**
     * 懒加载初始化（兜底方案）
     * 从传入的 Context 获取 Application
     */
    private fun ensureInitialized(context: Context) {
        if (application == null) {
            synchronized(this) {
                if (application == null) {
                    application = context.applicationContext as Application
                }
            }
        }
    }

    // ==================== 图片选择 ====================

    /** 选择图片 */
    @JvmStatic
    @MainThread
    fun pickImages(
        context: Context,
        maxCount: Int = 9,
        onResult: (ImagePickerResult) -> Unit
    ) {
        pick(context, ImagePickerConfig.imageOnly(maxCount), onResult)
    }

    /** 选择图片 - 协程版 */
    @JvmStatic
    suspend fun pickImagesSuspend(context: Context, maxCount: Int = 9): List<Uri> {
        return pickSuspend(context, ImagePickerConfig.imageOnly(maxCount)).getUrisOrEmpty()
    }

    // ==================== 视频选择 ====================

    /** 选择视频 */
    @JvmStatic
    @MainThread
    fun pickVideos(
        context: Context,
        maxCount: Int = 9,
        onResult: (ImagePickerResult) -> Unit
    ) {
        pick(context, ImagePickerConfig.videoOnly(maxCount), onResult)
    }

    /** 选择视频 - 协程版 */
    @JvmStatic
    suspend fun pickVideosSuspend(context: Context, maxCount: Int = 9): List<Uri> {
        return pickSuspend(context, ImagePickerConfig.videoOnly(maxCount)).getUrisOrEmpty()
    }

    // ==================== 混合选择 ====================

    /** 选择图片或视频 */
    @JvmStatic
    @MainThread
    fun pickMedia(
        context: Context,
        maxCount: Int = 9,
        onResult: (ImagePickerResult) -> Unit
    ) {
        pick(context, ImagePickerConfig.imageAndVideo(maxCount), onResult)
    }

    /** 选择图片或视频 - 协程版 */
    @JvmStatic
    suspend fun pickMediaSuspend(context: Context, maxCount: Int = 9): List<Uri> {
        return pickSuspend(context, ImagePickerConfig.imageAndVideo(maxCount)).getUrisOrEmpty()
    }

    // ==================== 通用方法 ====================

    /** 完整配置选择 */
    @JvmStatic
    @MainThread
    fun pick(
        context: Context,
        config: ImagePickerConfig = ImagePickerConfig.default(),
        onResult: (ImagePickerResult) -> Unit
    ) {
        ensureInitialized(context) // 懒加载兜底
        currentConfig = config
        pendingCallback = WeakReference(onResult)

        val intent = Intent(context, ImagePickerActivity::class.java).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }

    /** 完整配置选择 - 协程版 */
    @JvmStatic
    suspend fun pickSuspend(
        context: Context,
        config: ImagePickerConfig = ImagePickerConfig.default()
    ): ImagePickerResult = suspendCancellableCoroutine { continuation ->
        pick(context, config) { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        continuation.invokeOnCancellation { pendingCallback = null }
    }

    // ==================== 内部方法 ====================

    internal fun getConfig(): ImagePickerConfig = currentConfig

    internal fun deliverResult(result: ImagePickerResult) {
        pendingCallback?.get()?.invoke(result)
        pendingCallback = null
    }

    internal fun getApplication(): Application {
        return application
            ?: throw IllegalStateException("ImagePicker not initialized")
    }
}
