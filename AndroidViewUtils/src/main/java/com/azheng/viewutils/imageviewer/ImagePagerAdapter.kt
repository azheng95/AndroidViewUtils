package com.azheng.viewutils.imageviewer

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import java.io.File

internal class ImagePagerAdapter(
    private val config: ViewerConfig,
    private val callback: ImageViewerCallback?
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    private val imageViewFactory = ExifImageViewFactory()

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bigImageView: BigImageView = itemView.findViewWithTag("bigImageView")
        val progressBar: ProgressBar = itemView.findViewWithTag("progressBar")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val context = parent.context

        // 动态创建布局，避免依赖 XML 资源
        val rootView = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(config.backgroundColor)
        }

        val bigImageView = BigImageView(context).apply {
            tag = "bigImageView"
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val progressBar = ProgressBar(context).apply {
            tag = "progressBar"
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
            visibility = View.GONE
        }

        rootView.addView(bigImageView)
        rootView.addView(progressBar)

        return ImageViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val imageSource = config.imageSources[position]
        val displayString = imageSource.toDisplayString()

        holder.bigImageView.apply {
            // 设置 EXIF 方向处理
            setImageViewFactory(imageViewFactory)

            // 加载回调
            setImageLoaderCallback(object : ImageLoader.Callback {
                override fun onStart() {
                    holder.progressBar.visibility = View.VISIBLE
                    callback?.onLoadStart(position)
                }

                override fun onSuccess(image: File?) {
                    holder.progressBar.visibility = View.GONE
                    callback?.onLoadSuccess(position)
                }

                override fun onFail(error: Exception?) {
                    holder.progressBar.visibility = View.GONE
                    callback?.onLoadFailed(position, error)
                }

                override fun onCacheHit(imageType: Int, image: File?) {
                    holder.progressBar.visibility = View.GONE
                }

                override fun onCacheMiss(imageType: Int, image: File?) {}
                override fun onProgress(progress: Int) {}
                override fun onFinish() {}
            })

            // 点击事件
            if (config.clickToClose) {
                setOnClickListener {
                    callback?.onImageClick(position, displayString)
                }
            }

            // 长按事件
            setOnLongClickListener {
                callback?.onImageLongClick(position, displayString) ?: false
            }

            // 根据 ImageSource 类型加载图片
            val uri = imageSource.toUri()
            showImage(uri)
        }
    }

    override fun onViewRecycled(holder: ImageViewHolder) {
        super.onViewRecycled(holder)
        holder.bigImageView.cancel()
    }

    override fun getItemCount(): Int = config.imageSources.size
}
