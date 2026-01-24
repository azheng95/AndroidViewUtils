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
import java.util.concurrent.ExecutionException

private const val TAG = "ViewImage"


/**
 * 图片加载扩展函数优化版（解决圆角+centerCrop冲突）
 * @param url 图片地址
 * @param isRound 是否显示为圆形（与圆角参数互斥）
 * @param radius 圆角半径（单位：px，默认不生效）
 * @param blurRadius 模糊半径（单位：px，默认不生效）
 * @param sampling 模糊采样率（配合模糊半径使用）
 * @param isPlaceholder 是否显示占位图
 */
fun <T> ImageView.setImageUrl(
    url: T,
    isRound: Boolean = false,
    radius: Float? = null,
    blurRadius: Int? = null,
    sampling: Int? = null,
    isPlaceholder: Boolean = false,
    imagePlaceholder: Int? = ViewConstantUtils.imagePlaceholder
) {
    // 1. 校验上下文是否存活
    if (!isContextValid()) {
        return
    }

    // 2. 构建变换集合（核心修复：默认添加CenterCrop，保证列表非空）
    val transformations = mutableListOf<Transformation<Bitmap>>().apply {
        // 无论是否有其他变换，都先添加CenterCrop（替代ImageView的scaleType="centerCrop"）
        add(CenterCrop())

        // 叠加其他变换
        when {
            // 圆形效果（互斥优先）
            isRound -> add(CircleCrop())
            else -> {
                // 圆角效果
                radius?.takeIf { it > 0 }?.let {
                    add(RoundedCornersTransformation(it.toInt(), 0))
                }
                // 模糊效果
                blurRadius?.takeIf { it > 0 }?.let {
                    add(
                        if (sampling != null) BlurTransformation(
                            it,
                            sampling
                        ) else BlurTransformation(it)
                    )
                }
            }
        }
    }

    // 3. 构建RequestOptions（核心修复：仅当列表非空时才应用变换）
    val requestOptions = RequestOptions().apply {
        // 保证列表非空时才调用MultiTransformation（双重保险）
        if (transformations.isNotEmpty()) {
            transform(MultiTransformation(transformations))
        }
        // 占位图/错误图逻辑
        if (isPlaceholder) {
            placeholder(imagePlaceholder!!)
            error(imagePlaceholder)
        }

        // 缓存策略保留
        diskCacheStrategy(DiskCacheStrategy.ALL)
    }
    // 4. 加载图片
    Glide.with(this)
        .load(url)
        .apply(requestOptions)
        .into(this)
}

/**
 * 检查ImageView的上下文是否有效（Activity/Fragment未销毁）
 */
private fun ImageView.isContextValid(): Boolean {
    val context: Context = this.context ?: return false
    return when (context) {
        // Activity上下文：检查是否销毁或正在销毁
        is ComponentActivity -> !context.isDestroyed && !context.isFinishing
        // 包装类上下文：获取基础上下文再检查
        is ContextWrapper -> {
            val baseContext = context.baseContext
            when (baseContext) {
                is ComponentActivity -> !baseContext.isDestroyed && !baseContext.isFinishing
                else -> true // 非Activity上下文，默认有效（如Application）
            }
        }

        else -> true // 其他上下文（如Application）默认有效
    }
}


/**
 * 用于本地id资源
 * @param url 图片地址
 * @param isRound 是否显示为圆形（与圆角参数互斥）
 * @param radius 圆角半径（单位：px，默认不生效）
 * @param blurRadius 模糊半径（单位：px，默认不生效）
 * 不复用
 */
fun <T> ImageView.setImageUrlRes(
    url: T,
    isRound: Boolean = false,
    radius: Int? = null,
    blurRadius: Int? = null,
    imagePlaceholder: Int? = ViewConstantUtils.imagePlaceholder
) {
    // 1. 校验上下文是否存活
    if (!isContextValid()) {
        return
    }
    val requestOptions = RequestOptions().run {
        placeholder(if (isRound) imagePlaceholder!! else imagePlaceholder!!)
        error(if (isRound) imagePlaceholder else imagePlaceholder)
    }
    if (isRound) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(this)
    } else {
        if ((radius ?: 0) > 0) {
            Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                .transform(CenterCrop(), RoundedCorners(radius ?: 0))
                .apply(requestOptions)
                .into(this)
        } else {
            Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                .transform(CenterCrop())
                .apply(requestOptions)
                .into(this)
        }
    }
}

/**
 * item 中图片
 */
fun <T> ImageView.setImageUrlItem(
    url: T,
    isRound: Boolean = false,
    radius: Int? = null,
    blurRadius: Int? = null,
    imagePlaceholder: Int? = ViewConstantUtils.imagePlaceholder
) {
    // 优化点1：增加缩略图预加载（降低首次加载时间）
    val thumbnailSize = 0.25f // 使用10%尺寸的缩略图
    // 优化点3：合并多个Transformation
    val transformations = mutableListOf<Transformation<Bitmap>>()
    radius?.takeIf { it > 0 }?.let {
        transformations.add(RoundedCorners(radius.toInt()))
//        transformations.add(RoundedCornersTransformation(it.toInt(), 0))

    }
    blurRadius?.takeIf { it > 0 }?.let {
        transformations.add(BlurTransformation(it))
    }

    val requestOptions = RequestOptions().run {
        placeholder(if (isRound) imagePlaceholder!! else imagePlaceholder!!)
        error(if (isRound) imagePlaceholder else imagePlaceholder)

    }
    // 优化点6：优化缓存策略
    /*    Glide.with(this)
            .load(url)
            .thumbnail(thumbnailSize)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 智能选择缓存策略
            .apply(requestOptions)
            .into(this)*/

    if (isRound) {
        Glide.with(this)
            .load(url)
            .thumbnail(thumbnailSize)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 智能选择缓存策略
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(this)
    } else {
        if ((radius ?: 0) > 0) {
            Glide.with(this)
                .load(url)
                .thumbnail(thumbnailSize)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 智能选择缓存策略
                .transform(CenterCrop(), RoundedCorners(radius ?: 0))
                .apply(requestOptions)
                .into(this)
        } else {
            Glide.with(this)
                .load(url)
                .thumbnail(thumbnailSize)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 智能选择缓存策略
                .transform(CenterCrop())
                .apply(requestOptions)
                .into(this)
        }
    }
}

/**
 * 图片加载
 */
fun <T> ImageView.setImageUrlNoPlaceholder(
    url: T?,
) {
    Glide
        .with(this)
        .load(url)
        .into(this)
}

// 新增：同步加载图片为Bitmap（用于未挂载的ImageView）
suspend fun <T> ImageView.loadImageToBitmapSync(
    context: Context,
    url: T,
    isRound: Boolean = false,
    radius: Float? = null,
    blurRadius: Int? = null,
    sampling: Int? = null,
    placeholderRes:Int? = ViewConstantUtils.imagePlaceholder
): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            // 构建和原有扩展函数一致的变换规则
            val transformations = mutableListOf<Transformation<Bitmap>>().apply {
                add(CenterCrop())
                when {
                    isRound -> add(CircleCrop())
                    else -> {
                        radius?.takeIf { it > 0 }
                            ?.let { add(RoundedCornersTransformation(it.toInt(), 0)) }
                        blurRadius?.takeIf { it > 0 }?.let {
                            add(
                                if (sampling != null) BlurTransformation(
                                    it,
                                    sampling
                                ) else BlurTransformation(it)
                            )
                        }
                    }
                }
            }
            val requestOptions = RequestOptions().apply {
                if (transformations.isNotEmpty()) transform(MultiTransformation(transformations))
                placeholder(placeholderRes!!)
                error(placeholderRes)
                diskCacheStrategy(DiskCacheStrategy.ALL)
            }
            // 同步加载图片（submit+get，在IO线程执行）
            Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(requestOptions)
                .submit()
                .get()
        } catch (e: ExecutionException) {
            null
        } catch (e: InterruptedException) {
            null
        }
    }
}