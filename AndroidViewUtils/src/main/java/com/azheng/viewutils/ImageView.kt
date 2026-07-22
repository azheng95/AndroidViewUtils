@file:JvmName("ImageViewExt")
@file:JvmMultifileClass

package com.azheng.viewutils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.MainThread
import androidx.annotation.Px
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.util.Collections

sealed interface ImageShape {
    data object None : ImageShape
    data object Circle : ImageShape

    data class Rounded(@Px val radius: Int) : ImageShape {
        init {
            require(radius > 0) { "圆角半径必须大于 0px" }
        }
    }
}

enum class ImageScale {
    NONE,
    CENTER_CROP,
    FIT_CENTER,
    CENTER_INSIDE
}

/**
 * Glide 图片加载配置。
 *
 * [extraTransformations] 会在缩放之后、形状裁剪之前按顺序执行。
 * 传入的变换列表会被复制为只读快照；占位、错误和空模型资源彼此独立。
 */
class ImageLoadConfig(
    val shape: ImageShape = ImageShape.None,
    val scale: ImageScale = ImageScale.CENTER_CROP,
    extraTransformations: List<Transformation<Bitmap>> = emptyList(),
    @DrawableRes val placeholderRes: Int? = null,
    @DrawableRes val errorRes: Int? = null,
    @DrawableRes val fallbackRes: Int? = null,
    val cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false, toInclusive = false)
    val thumbnailMultiplier: Float? = null,
    val skipMemoryCache: Boolean = false
) {
    val extraTransformations: List<Transformation<Bitmap>> =
        Collections.unmodifiableList(ArrayList(extraTransformations))

    init {
        require(placeholderRes == null || placeholderRes != 0) { "placeholderRes 必须是有效资源 ID" }
        require(errorRes == null || errorRes != 0) { "errorRes 必须是有效资源 ID" }
        require(fallbackRes == null || fallbackRes != 0) { "fallbackRes 必须是有效资源 ID" }
        require(thumbnailMultiplier == null || thumbnailMultiplier > 0f && thumbnailMultiplier < 1f) {
            "thumbnailMultiplier 必须大于 0 且小于 1"
        }
    }
}

/**
 * 将任意 Glide model 加载到当前 ImageView。
 *
 * 返回的 [Target] 可用于高级调用方追踪请求；加载同一个 ImageView 时，Glide 会自动取消旧请求。
 */
@MainThread
@JvmOverloads
fun ImageView.loadImage(
    model: Any?,
    config: ImageLoadConfig = ImageLoadConfig()
): Target<Drawable> {
    return Glide.with(this)
        .load(model)
        .applyImageConfig(config)
        .into(this)
}

@JvmSynthetic
internal fun <TranscodeType> RequestBuilder<TranscodeType>.applyImageConfig(
    config: ImageLoadConfig
): RequestBuilder<TranscodeType> {
    val configuredRequest = apply(config.toRequestOptions())
    val multiplier = config.thumbnailMultiplier ?: return configuredRequest
    val thumbnailRequest = configuredRequest.clone().sizeMultiplier(multiplier)
    return configuredRequest.thumbnail(thumbnailRequest)
}

@JvmSynthetic
internal fun ImageLoadConfig.toRequestOptions(): RequestOptions {
    var options = RequestOptions()
        .diskCacheStrategy(cacheStrategy)
        .skipMemoryCache(skipMemoryCache)

    val transformations = buildTransformations()
    options = when (transformations.size) {
        0 -> options.dontTransform()
        1 -> options.transform(transformations.first())
        else -> options.transform(MultiTransformation(transformations))
    }

    placeholderRes?.let { options = options.placeholder(it) }
    errorRes?.let { options = options.error(it) }
    fallbackRes?.let { options = options.fallback(it) }

    return options
}

@JvmSynthetic
internal fun ImageLoadConfig.buildTransformations(): List<Transformation<Bitmap>> {
    return buildList {
        when (scale) {
            ImageScale.NONE -> Unit
            ImageScale.CENTER_CROP -> add(CenterCrop())
            ImageScale.FIT_CENTER -> add(FitCenter())
            ImageScale.CENTER_INSIDE -> add(CenterInside())
        }

        addAll(extraTransformations)

        when (val imageShape = shape) {
            ImageShape.None -> Unit
            ImageShape.Circle -> add(CircleCrop())
            is ImageShape.Rounded -> add(RoundedCorners(imageShape.radius))
        }
    }
}
