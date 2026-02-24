package com.azheng.viewutils

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ViewImage"

/**
 * 图片加载配置类
 */
data class ImageLoadConfig(
    val isRound: Boolean = false,
    val radius: Int = 0,
    val blurRadius: Int = 0,
    val sampling: Int = 1,
    val showPlaceholder: Boolean = false,
    val placeholderRes: Int = ViewConstantUtils.imagePlaceholder,
    val cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL,
    val thumbnailSize: Float = 0f,
    val applyCenterCrop: Boolean = true
)

// ==================== 公共 API ====================

/**
 * 通用图片加载扩展函数
 */
fun <T> ImageView.loadImage(url: T?, config: ImageLoadConfig = ImageLoadConfig()) {
    if (!isContextValid()) return

    Glide.with(this)
        .load(url)
        .apply(config.toRequestOptions())
        .apply { if (config.thumbnailSize > 0) thumbnail(config.thumbnailSize) }
        .into(this)
}

/**
 * 图片加载扩展函数（保持原有 API 兼容）
 */
fun <T> ImageView.setImageUrl(
    url: T,
    isRound: Boolean = false,
    radius: Int? = null,
    blurRadius: Int? = null,
    sampling: Int? = null,
    isPlaceholder: Boolean = false,
    imagePlaceholder: Int? = ViewConstantUtils.imagePlaceholder
) {
    loadImage(
        url = url,
        config = ImageLoadConfig(
            isRound = isRound,
            radius = radius ?: 0,
            blurRadius = blurRadius ?: 0,
            sampling = sampling ?: 1,
            showPlaceholder = isPlaceholder,
            placeholderRes = imagePlaceholder ?: ViewConstantUtils.imagePlaceholder,
            cacheStrategy = DiskCacheStrategy.ALL
        )
    )
}

/**
 * 本地资源图片加载（禁用磁盘缓存）
 */
fun <T> ImageView.setImageUrlRes(
    url: T,
    isRound: Boolean = false,
    radius: Int? = null,
    blurRadius: Int? = null,
    imagePlaceholder: Int? = ViewConstantUtils.imagePlaceholder
) {
    loadImage(
        url = url,
        config = ImageLoadConfig(
            isRound = isRound,
            radius = radius ?: 0,
            blurRadius = blurRadius ?: 0,
            showPlaceholder = true,
            placeholderRes = imagePlaceholder ?: ViewConstantUtils.imagePlaceholder,
            cacheStrategy = DiskCacheStrategy.NONE
        )
    )
}

/**
 * 列表项图片加载（带缩略图优化）
 */
fun <T> ImageView.setImageUrlItem(
    url: T,
    isRound: Boolean = false,
    radius: Int? = null,
    blurRadius: Int? = null,
    imagePlaceholder: Int? = ViewConstantUtils.imagePlaceholder
) {
    loadImage(
        url = url,
        config = ImageLoadConfig(
            isRound = isRound,
            radius = radius ?: 0,
            blurRadius = blurRadius ?: 0,
            showPlaceholder = true,
            placeholderRes = imagePlaceholder ?: ViewConstantUtils.imagePlaceholder,
            cacheStrategy = DiskCacheStrategy.AUTOMATIC,
            thumbnailSize = 0.25f
        )
    )
}

/**
 * 无占位图简单加载
 */
fun <T> ImageView.setImageUrlNoPlaceholder(url: T?) {
    if (!isContextValid()) return
    Glide.with(this).load(url).into(this)
}

/**
 * 同步加载图片为 Bitmap（协程环境使用）
 */
suspend fun <T> loadImageToBitmap(
    context: Context,
    url: T,
    config: ImageLoadConfig = ImageLoadConfig()
): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(config.toRequestOptions())
            .submit()
            .get()
    }.getOrNull()
}

// ==================== 私有工具方法 ====================

/**
 * 将配置转换为 RequestOptions
 */
private fun ImageLoadConfig.toRequestOptions(): RequestOptions {
    return RequestOptions().apply {
        // 构建并应用变换
        buildTransformations().takeIf { it.isNotEmpty() }?.let {
            transform(MultiTransformation(it))
        }

        // 占位图配置
        if (showPlaceholder) {
            placeholder(placeholderRes)
            error(placeholderRes)
        }

        // 缓存策略
        diskCacheStrategy(cacheStrategy)
    }
}

/**
 * 构建图片变换列表
 */
private fun ImageLoadConfig.buildTransformations(): List<Transformation<Bitmap>> {
    return buildList {
        // 1. CenterCrop（基础变换）
        if (applyCenterCrop) add(CenterCrop())

        // 2. 形状变换（圆形与圆角互斥）
        when {
            isRound -> add(CircleCrop())
            radius > 0 -> add(RoundedCornersTransformation(radius, 0))
        }

        // 3. 模糊变换（可叠加）
        if (blurRadius > 0) {
            add(BlurTransformation(blurRadius, sampling))
        }
    }
}

/**
 * 检查 ImageView 的 Context 是否有效
 */
private fun ImageView.isContextValid(): Boolean {
    val activity = context.findComponentActivity()
    return activity == null || (!activity.isDestroyed && !activity.isFinishing)
}

/**
 * 递归查找 ComponentActivity
 */
private tailrec fun Context.findComponentActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findComponentActivity()
        else -> null
    }
}
