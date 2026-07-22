package com.azheng.viewutils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ViewToImageUtils {

    /**
     * 图片加载参数封装
     */
    data class ImageViewLoadParam(
        val model: Any?,
        val config: ImageLoadConfig = ImageLoadConfig()
    )

    /**
     * 提前注册ImageView的加载参数（在设置图片时调用）
     */
    fun registerImageViewLoadParam(
        imageView: ImageView,
        model: Any?,
        config: ImageLoadConfig = ImageLoadConfig()
    ) {
        imageView.setTag(
            R.id.avu_image_load_param,
            ImageViewLoadParam(model, config)
        )
    }

    /**
     * 将View保存为图片（自动获取宽高，适配未挂载View的ImageView）
     */
    fun saveViewToImage(
        context: Context,
        view: View,
        lifecycleScope: LifecycleCoroutineScope,
        fileName: String = "view_${System.currentTimeMillis()}",
        isSaveToInternal: Boolean = true,
        onResult: (Boolean, String) -> Unit
    ) {
        lifecycleScope.launch {
            var bitmap: Bitmap? = null
            try {
                preloadAllImageViewImages(context, view)
                bitmap = createValidBitmapFromView(view)

                val saveResult = if (isSaveToInternal) {
                    BitmapSaveUtils.saveBitmapToInternalStorage(
                        context = context,
                        bitmap = bitmap,
                        fileName = fileName
                    ).map { it.absolutePath }
                } else {
                    BitmapSaveUtils.saveBitmapToMediaStore(
                        context = context,
                        bitmap = bitmap,
                        fileName = fileName
                    ).map { it.toString() }
                }

                saveResult.fold(
                    onSuccess = { path -> onResult(true, path) },
                    onFailure = { error ->
                        onResult(false, "保存失败：${error.message ?: "未知错误"}")
                    }
                )
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                onResult(false, "保存失败：${e.message ?: "未知错误"}")
            } finally {
                bitmap?.recycle()
                clearRegisteredImageParams(view)
            }
        }
    }

    /**
     * 同步加载View中所有ImageView的图片（修改为挂起函数，内部调用挂起的遍历）
     */
    private suspend fun preloadAllImageViewImages(context: Context, view: View) {
        // 调用支持挂起函数的遍历方法
        traverseViewSuspend(view) { childView ->
            if (childView is ImageView) {
                val imageView = childView
                val param = imageView.getTag(R.id.avu_image_load_param)
                    as? ImageViewLoadParam ?: return@traverseViewSuspend
                // 同步加载图片为Bitmap（使用新的配置类方式）
                val bitmap = loadImageToBitmapResult(
                    context = context,
                    model = param.model,
                    config = param.config
                ).getOrThrow()

                // 手动设置Bitmap到ImageView（切回主线程）
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }



    /**
     * 【关键修改】支持挂起函数的递归遍历View树
     * @param view 根View
     * @param action 挂起的遍历动作
     */
    private suspend fun traverseViewSuspend(view: View, action: suspend (View) -> Unit) {
        // 先执行当前View的动作（支持挂起）
        action(view)
        // 递归遍历子View
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                // 子View也执行挂起动作
                traverseViewSuspend(child, action)
            }
        }
    }

    private fun clearRegisteredImageParams(view: View) {
        if (view is ImageView) {
            view.setTag(R.id.avu_image_load_param, null)
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                clearRegisteredImageParams(view.getChildAt(index))
            }
        }
    }

    /**
     * 生成有效Bitmap（自动计算宽高）
     */
    /**
     * 生成有效Bitmap（修复测量模式，限制最大宽度为屏幕宽度）
     */
    private fun createValidBitmapFromView(view: View): Bitmap {
        val isViewLaidOut = view.width > 0 && view.height > 0
        val finalWidth: Int
        val finalHeight: Int

        if (isViewLaidOut) {
            finalWidth = view.width
            finalHeight = view.height
        } else {
            // 获取屏幕宽度（作为最大宽度限制）
            val displayMetrics = view.context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            // 预留边距（比如20dp，转成px），避免贴边
            val marginPx = (20 * displayMetrics.density).toInt()
            val maxWidth = screenWidth - 2 * marginPx

            val layoutParams = view.layoutParams
            // 计算宽度MeasureSpec：优先用LayoutParams，否则限制最大宽度为屏幕宽度（AT_MOST）
            val widthSpec = when {
                layoutParams != null && layoutParams.width > 0 -> {
                    MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY)
                }

                layoutParams?.width == ViewGroup.LayoutParams.MATCH_PARENT -> {
                    MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY) // 强制匹配屏幕宽度
                }

                else -> {
                    MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST) // 最大宽度不超过屏幕宽度
                }
            }

            // 计算高度MeasureSpec：自适应内容，但不超过屏幕高度
            val maxHeight = displayMetrics.heightPixels
            val heightSpec = when {
                layoutParams != null && layoutParams.height > 0 -> {
                    MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
                }

                else -> {
                    MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST) // 最大高度限制
                }
            }

            // 强制测量（带尺寸约束）
            view.measure(widthSpec, heightSpec)
            // 最终宽高：测量后的尺寸，且不超过屏幕限制
            finalWidth = view.measuredWidth.coerceAtMost(maxWidth)
            finalHeight = view.measuredHeight.coerceAtMost(maxHeight)
            // 强制布局（基于约束后的宽高）
            view.layout(0, 0, finalWidth, finalHeight)
        }

        require(finalWidth > 0 && finalHeight > 0) { "无法从尺寸为 ${finalWidth}x${finalHeight} 的 View 创建图片" }

        val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

}
