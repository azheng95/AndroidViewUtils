package com.azheng.viewutils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ViewToImageUtils {

    /**
     * 存储ImageView的图片URL和加载参数（用于同步加载）
     */
    private val imageViewUrlMap = mutableMapOf<ImageView, ImageViewLoadParam>()

    /**
     * 图片加载参数封装
     */
    data class ImageViewLoadParam(
        val url: Any,
        val isRound: Boolean = false,
        val radius: Float? = null,
        val blurRadius: Int? = null,
        val sampling: Int? = null
    )

    /**
     * 提前注册ImageView的加载参数（在设置图片时调用）
     */
    fun registerImageViewLoadParam(
        imageView: ImageView,
        url: Any,
        isRound: Boolean = false,
        radius: Float? = null,
        blurRadius: Int? = null,
        sampling: Int? = null
    ) {
        imageViewUrlMap[imageView] = ImageViewLoadParam(url, isRound, radius, blurRadius, sampling)
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
            try {
                // 关键：先同步加载所有ImageView的图片
                preloadAllImageViewImages(context, view)
                // 生成Bitmap
                val bitmap = createValidBitmapFromView(view)
                if (bitmap.width <= 0 || bitmap.height <= 0) {
                    onResult(false, "生成的Bitmap为空")
                    return@launch
                }
                var savePath: String? = null
                if (isSaveToInternal) {
                    val saveResult = withContext(Dispatchers.IO) {
                        BitmapSaveUtils.saveBitmapToInternalStorage(context, bitmap, fileName)
                    }
                    saveResult.onSuccess { file ->
                        savePath = file.absolutePath
                    }
                } else {
                    val saveResult = withContext(Dispatchers.IO) {
                        BitmapSaveUtils.saveBitmapToMediaStore(context, bitmap, fileName)
                    }
                    saveResult.onSuccess { file ->
                        savePath = file.path
                    }
                }
                // 清空缓存的参数
                imageViewUrlMap.clear()
                onResult(true, "$savePath")
            } catch (e: Exception) {
                imageViewUrlMap.clear()
                onResult(false, "保存失败：${e.message ?: "未知错误"}")
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
                // 获取注册的加载参数
                val param = imageViewUrlMap[imageView] ?: return@traverseViewSuspend
                // 同步加载图片为Bitmap（挂起函数，现在可以正常调用）
                val bitmap = imageView.loadImageToBitmapSync(
                    context = context,
                    url = param.url,
                    isRound = param.isRound,
                    radius = param.radius,
                    blurRadius = param.blurRadius,
                    sampling = param.sampling
                )
                // 手动设置Bitmap到ImageView（切回主线程）
                bitmap?.let {
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(it)
                    }
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

        // 创建Bitmap（尺寸和布局一致）
        val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

}