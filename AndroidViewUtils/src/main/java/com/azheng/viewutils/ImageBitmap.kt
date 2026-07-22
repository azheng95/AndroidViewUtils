@file:JvmName("ImageBitmapUtils")

package com.azheng.viewutils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * 在可取消的 IO 上下文中加载一份由调用方独立持有的 Bitmap。
 *
 * 返回值是 Glide BitmapPool 资源的副本，调用方可以在不再使用时安全 recycle。
 */
suspend fun loadImageToBitmapResult(
    context: Context,
    model: Any?,
    config: ImageLoadConfig = ImageLoadConfig()
): Result<Bitmap> {
    return try {
        val bitmap = runInterruptible(Dispatchers.IO) {
            val requestManager = Glide.with(context.applicationContext)
            val target = requestManager
                .asBitmap()
                .load(model)
                .applyImageConfig(config)
                .submit()

            try {
                val pooledBitmap = target.get()
                pooledBitmap.copy(Bitmap.Config.ARGB_8888, false)
                    ?: error("无法复制 Glide Bitmap")
            } finally {
                target.cancel(true)
                requestManager.clear(target)
            }
        }
        Result.success(bitmap)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }
}

@Deprecated(
    message = "使用 loadImageToBitmapResult 获取明确的成功或失败结果",
    replaceWith = ReplaceWith("loadImageToBitmapResult(context, url, config).getOrNull()")
)
suspend fun loadImageToBitmap(
    context: Context,
    url: Any?,
    config: ImageLoadConfig = ImageLoadConfig()
): Bitmap? = loadImageToBitmapResult(context, url, config).getOrNull()
