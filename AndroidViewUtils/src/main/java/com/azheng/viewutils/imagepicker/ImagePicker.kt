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

/**
 * 图片/视频选择器单例对象
 *
 * 提供简洁的 API 用于从相册选择图片和视频，支持：
 * - 仅图片选择
 * - 仅视频选择
 * - 图片和视频混合选择
 * - 回调方式和协程挂起方式
 * - 已选数量限制检测
 *
 * 使用示例：
 * ```kotlin
 * // 回调方式
 * ImagePicker.pickImages(context, maxCount = 9) { result ->
 *     when (result) {
 *         is ImagePickerResult.Success -> handleUris(result.uris)
 *         is ImagePickerResult.Canceled -> showCanceledMessage()
 *         is ImagePickerResult.MaxLimitReached -> showLimitWarning()
 *     }
 * }
 *
 * // 协程方式
 * val uris = ImagePicker.pickImagesSuspend(context, maxCount = 9)
 * ```
 *
 * @author AZheng
 */
object ImagePicker {

    // ==================== 私有属性 ====================

    /** Application 实例，用于获取应用上下文 */
    @Volatile
    private var application: Application? = null

    /** 初始化锁对象，确保线程安全 */
    private val initLock = Any()

    /** 待处理的回调函数，使用弱引用避免内存泄漏 */
    private var pendingCallback: WeakReference<(ImagePickerResult) -> Unit>? = null

    /** 当前选择器配置 */
    private var currentConfig: ImagePickerConfig = ImagePickerConfig.default()

    // ==================== 初始化方法 ====================

    /**
     * 手动初始化 ImagePicker
     *
     * 建议在 Application.onCreate() 中调用，但这是可选的，
     * 如果未手动初始化，将在首次使用时自动初始化
     *
     * @param app Application 实例
     */
    @JvmStatic
    fun init(app: Application) {
        application = app
    }

    /**
     * 内部自动初始化方法
     *
     * 供 AndroidX App Startup 库调用，实现无感知初始化
     *
     * @param context 上下文对象
     */
    internal fun autoInit(context: Context) {
        if (application == null) {
            application = context.applicationContext as Application
        }
    }

    /**
     * 确保已初始化（懒加载兜底方案）
     *
     * 使用双重检查锁定模式保证线程安全和性能
     *
     * @param context 上下文对象
     */
    private fun ensureInitialized(context: Context) {
        if (application != null) return

        synchronized(initLock) {
            if (application == null) {
                application = context.applicationContext as Application
            }
        }
    }

    // ==================== 图片选择 API ====================

    /**
     * 选择图片（仅图片）
     *
     * @param context 上下文，Activity 或 Context
     * @param maxCount 最大可选数量，默认为 9
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickImages(
        context: Context,
        maxCount: Int = 9,
        onResult: (ImagePickerResult) -> Unit
    ) {
        pick(context, ImagePickerConfig.imageOnly(maxCount), onResult)
    }

    /**
     * 选择图片 - 带已选数量限制
     *
     * 适用于编辑场景，需要扣除已选择的数量
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedCount 当前已选数量（将从 maxCount 中扣除）
     * @param onMaxLimitReached 达到最大限制时的回调（可选）
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickImages(
        context: Context,
        maxCount: Int = 9,
        currentSelectedCount: Int,
        onMaxLimitReached: ((MaxLimitInfo) -> Unit)? = null,
        onResult: (ImagePickerResult) -> Unit
    ) {
        val config = buildConfigWithLimit(
            maxCount = maxCount,
            currentSelectedCount = currentSelectedCount,
            onMaxLimitReached = onMaxLimitReached
        ) { imageOnly() }
        pick(context, config, onResult)
    }

    /**
     * 选择图片 - 带已选 Uri 列表限制
     *
     * 根据已选 Uri 列表自动计算当前已选数量
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedUris 当前已选的 Uri 列表
     * @param onMaxLimitReached 达到最大限制时的回调（可选）
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickImages(
        context: Context,
        maxCount: Int = 9,
        currentSelectedUris: List<Uri>?,
        onMaxLimitReached: ((MaxLimitInfo) -> Unit)? = null,
        onResult: (ImagePickerResult) -> Unit
    ) {
        val config = ImagePickerConfig.Builder()
            .maxCount(maxCount)
            .currentSelectedUris(currentSelectedUris)
            .imageOnly()
            .apply { onMaxLimitReached?.let { onMaxLimitReached(it) } }
            .build()
        pick(context, config, onResult)
    }

    /**
     * 选择图片 - 协程挂起版
     *
     * @param context 上下文
     * @param maxCount 最大可选数量
     * @return 选中的 Uri 列表，取消或失败时返回空列表
     */
    @JvmStatic
    suspend fun pickImagesSuspend(context: Context, maxCount: Int = 9): List<Uri> {
        return pickSuspend(context, ImagePickerConfig.imageOnly(maxCount)).getUrisOrEmpty()
    }

    /**
     * 选择图片 - 协程版，带已选数量限制
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedCount 当前已选数量
     * @return 完整的选择结果对象
     */
    @JvmStatic
    suspend fun pickImagesSuspend(
        context: Context,
        maxCount: Int = 9,
        currentSelectedCount: Int
    ): ImagePickerResult {
        val config = buildConfigWithLimit(
            maxCount = maxCount,
            currentSelectedCount = currentSelectedCount
        ) { imageOnly() }
        return pickSuspend(context, config)
    }

    // ==================== 视频选择 API ====================

    /**
     * 选择视频（仅视频）
     *
     * @param context 上下文
     * @param maxCount 最大可选数量，默认为 9
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickVideos(
        context: Context,
        maxCount: Int = 9,
        onResult: (ImagePickerResult) -> Unit
    ) {
        pick(context, ImagePickerConfig.videoOnly(maxCount), onResult)
    }

    /**
     * 选择视频 - 带已选数量限制
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedCount 当前已选数量
     * @param onMaxLimitReached 达到最大限制时的回调
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickVideos(
        context: Context,
        maxCount: Int = 9,
        currentSelectedCount: Int,
        onMaxLimitReached: ((MaxLimitInfo) -> Unit)? = null,
        onResult: (ImagePickerResult) -> Unit
    ) {
        val config = buildConfigWithLimit(
            maxCount = maxCount,
            currentSelectedCount = currentSelectedCount,
            onMaxLimitReached = onMaxLimitReached
        ) { videoOnly() }
        pick(context, config, onResult)
    }

    /**
     * 选择视频 - 协程挂起版
     *
     * @param context 上下文
     * @param maxCount 最大可选数量
     * @return 选中的 Uri 列表
     */
    @JvmStatic
    suspend fun pickVideosSuspend(context: Context, maxCount: Int = 9): List<Uri> {
        return pickSuspend(context, ImagePickerConfig.videoOnly(maxCount)).getUrisOrEmpty()
    }

    /**
     * 选择视频 - 协程版，带已选数量限制
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedCount 当前已选数量
     * @return 完整的选择结果对象
     */
    @JvmStatic
    suspend fun pickVideosSuspend(
        context: Context,
        maxCount: Int = 9,
        currentSelectedCount: Int
    ): ImagePickerResult {
        val config = buildConfigWithLimit(
            maxCount = maxCount,
            currentSelectedCount = currentSelectedCount
        ) { videoOnly() }
        return pickSuspend(context, config)
    }

    // ==================== 混合选择 API ====================

    /**
     * 选择媒体文件（图片和视频）
     *
     * @param context 上下文
     * @param maxCount 最大可选数量，默认为 9
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickMedia(
        context: Context,
        maxCount: Int = 9,
        onResult: (ImagePickerResult) -> Unit
    ) {
        pick(context, ImagePickerConfig.imageAndVideo(maxCount), onResult)
    }

    /**
     * 选择媒体文件 - 带已选数量限制
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedCount 当前已选数量
     * @param onMaxLimitReached 达到最大限制时的回调
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pickMedia(
        context: Context,
        maxCount: Int = 9,
        currentSelectedCount: Int,
        onMaxLimitReached: ((MaxLimitInfo) -> Unit)? = null,
        onResult: (ImagePickerResult) -> Unit
    ) {
        val config = buildConfigWithLimit(
            maxCount = maxCount,
            currentSelectedCount = currentSelectedCount,
            onMaxLimitReached = onMaxLimitReached
        ) { imageAndVideo() }
        pick(context, config, onResult)
    }

    /**
     * 选择媒体文件 - 协程挂起版
     *
     * @param context 上下文
     * @param maxCount 最大可选数量
     * @return 选中的 Uri 列表
     */
    @JvmStatic
    suspend fun pickMediaSuspend(context: Context, maxCount: Int = 9): List<Uri> {
        return pickSuspend(context, ImagePickerConfig.imageAndVideo(maxCount)).getUrisOrEmpty()
    }

    /**
     * 选择媒体文件 - 协程版，带已选数量限制
     *
     * @param context 上下文
     * @param maxCount 最大可选总数
     * @param currentSelectedCount 当前已选数量
     * @return 完整的选择结果对象
     */
    @JvmStatic
    suspend fun pickMediaSuspend(
        context: Context,
        maxCount: Int = 9,
        currentSelectedCount: Int
    ): ImagePickerResult {
        val config = buildConfigWithLimit(
            maxCount = maxCount,
            currentSelectedCount = currentSelectedCount
        ) { imageAndVideo() }
        return pickSuspend(context, config)
    }

    // ==================== 核心通用方法 ====================

    /**
     * 使用完整配置进行选择
     *
     * 这是所有选择方法的最终入口，提供最大的灵活性
     *
     * @param context 上下文对象
     * @param config 选择器配置
     * @param onResult 结果回调
     */
    @JvmStatic
    @MainThread
    fun pick(
        context: Context,
        config: ImagePickerConfig = ImagePickerConfig.default(),
        onResult: (ImagePickerResult) -> Unit
    ) {
        // 确保已初始化
        ensureInitialized(context)

        // 前置检查：是否已达到最大限制
        if (config.isMaxLimitReached) {
            handleMaxLimitReached(config, onResult)
            return
        }

        // 保存配置和回调
        currentConfig = config
        pendingCallback = WeakReference(onResult)

        // 启动选择器 Activity
        launchPickerActivity(context)
    }

    /**
     * 使用完整配置进行选择 - 协程挂起版
     *
     * 将回调式 API 转换为挂起函数，支持协程取消
     *
     * @param context 上下文对象
     * @param config 选择器配置
     * @return 选择结果
     */
    @JvmStatic
    suspend fun pickSuspend(
        context: Context,
        config: ImagePickerConfig = ImagePickerConfig.default()
    ): ImagePickerResult = suspendCancellableCoroutine { continuation ->
        pick(context, config) { result ->
            // 仅在协程仍处于活跃状态时恢复
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }

        // 协程取消时清理回调，避免内存泄漏
        continuation.invokeOnCancellation {
            pendingCallback = null
        }
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 构建带数量限制的配置
     *
     * 减少重复代码的私有辅助方法
     *
     * @param maxCount 最大数量
     * @param currentSelectedCount 当前已选数量
     * @param onMaxLimitReached 达到限制的回调
     * @param mediaTypeConfig 媒体类型配置 lambda
     * @return 配置对象
     */
    private inline fun buildConfigWithLimit(
        maxCount: Int,
        currentSelectedCount: Int,
        noinline onMaxLimitReached: ((MaxLimitInfo) -> Unit)? = null,
        mediaTypeConfig: ImagePickerConfig.Builder.() -> ImagePickerConfig.Builder
    ): ImagePickerConfig {
        return ImagePickerConfig.Builder()
            .maxCount(maxCount)
            .currentSelectedCount(currentSelectedCount)
            .mediaTypeConfig()
            .apply { onMaxLimitReached?.let { onMaxLimitReached(it) } }
            .build()
    }

    /**
     * 处理达到最大限制的情况
     *
     * @param config 当前配置
     * @param onResult 结果回调
     */
    private fun handleMaxLimitReached(
        config: ImagePickerConfig,
        onResult: (ImagePickerResult) -> Unit
    ) {
        val limitInfo = config.maxLimitInfo

        // 触发开发者设置的限制回调
        config.onMaxLimitReached?.invoke(limitInfo)

        // 返回最大限制结果
        onResult(
            ImagePickerResult.MaxLimitReached(
                maxCount = limitInfo.maxCount,
                currentCount = limitInfo.currentCount,
                message = limitInfo.message
            )
        )
    }

    /**
     * 启动选择器 Activity
     *
     * @param context 上下文对象
     */
    private fun launchPickerActivity(context: Context) {
        val intent = Intent(context, ImagePickerActivity::class.java).apply {
            // 非 Activity 上下文需要添加 NEW_TASK 标志
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }

    // ==================== 内部访问方法 ====================

    /**
     * 获取当前配置
     *
     * 供 ImagePickerActivity 内部调用
     *
     * @return 当前选择器配置
     */
    internal fun getConfig(): ImagePickerConfig = currentConfig

    /**
     * 传递选择结果
     *
     * 由 ImagePickerActivity 在完成选择后调用
     *
     * @param result 选择结果
     */
    internal fun deliverResult(result: ImagePickerResult) {
        pendingCallback?.get()?.invoke(result)
        pendingCallback = null  // 清理引用
    }

    /**
     * 获取 Application 实例
     *
     * @return Application 实例
     * @throws IllegalStateException 如果未初始化
     */
    internal fun getApplication(): Application {
        return application
            ?: throw IllegalStateException(
                "ImagePicker 未初始化。请先调用 ImagePicker.init(application) 或通过 App Startup 自动初始化。"
            )
    }
}
