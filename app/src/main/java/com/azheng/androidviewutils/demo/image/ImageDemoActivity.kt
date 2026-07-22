package com.azheng.androidviewutils.demo.image

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.azheng.androidviewutils.demo.R
import com.azheng.androidviewutils.demo.databinding.ActivityImageDemoBinding
import com.azheng.viewutils.ImageLoadConfig
import com.azheng.viewutils.ImageScale
import com.azheng.viewutils.ImageShape
import com.azheng.viewutils.loadImage
import com.azheng.viewutils.loadImageToBitmapResult
import com.bumptech.glide.Glide
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.azheng.viewutils.R as ViewUtilsR

private const val LANDSCAPE_IMAGE_URL = "https://picsum.photos/id/1018/900/600"
private const val SQUARE_IMAGE_URL = "https://picsum.photos/id/1039/600/600"

class ImageDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDemoBinding
    private var ownedBitmap: Bitmap? = null
    private var bitmapLoadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonReload.setOnClickListener { loadImageExamples() }
        binding.buttonClear.setOnClickListener { clearImages() }
        binding.buttonLoadBitmap.setOnClickListener { loadBitmapExample() }

        loadImageExamples()
    }

    private fun loadImageExamples() {
        val placeholder = ViewUtilsR.drawable.avu_image_placeholder

        binding.imageRounded.loadImage(
            model = LANDSCAPE_IMAGE_URL,
            config = ImageLoadConfig(
                shape = ImageShape.Rounded(radius = 20.dp),
                scale = ImageScale.CENTER_CROP,
                placeholderRes = placeholder,
                errorRes = android.R.drawable.ic_dialog_alert,
                thumbnailMultiplier = 0.25f
            )
        )

        binding.imageCircle.loadImage(
            model = SQUARE_IMAGE_URL,
            config = ImageLoadConfig(
                shape = ImageShape.Circle,
                placeholderRes = placeholder,
                errorRes = android.R.drawable.ic_dialog_alert
            )
        )

        val failureConfig = ImageLoadConfig(
            scale = ImageScale.FIT_CENTER,
            placeholderRes = placeholder,
            errorRes = android.R.drawable.ic_dialog_alert,
            fallbackRes = android.R.drawable.ic_menu_help
        )
        binding.imageError.loadImage("invalid://image", failureConfig)
        binding.imageFallback.loadImage(null, failureConfig)
    }

    private fun loadBitmapExample() {
        binding.buttonLoadBitmap.isEnabled = false
        binding.progressBitmap.isVisible = true
        binding.textBitmapStatus.setText(R.string.image_demo_bitmap_loading)

        bitmapLoadJob = lifecycleScope.launch {
            val result = loadImageToBitmapResult(
                context = this@ImageDemoActivity,
                model = LANDSCAPE_IMAGE_URL,
                config = ImageLoadConfig(
                    shape = ImageShape.Rounded(radius = 12.dp),
                    thumbnailMultiplier = 0.25f
                )
            )

            binding.progressBitmap.isVisible = false
            binding.buttonLoadBitmap.isEnabled = true
            result.fold(
                onSuccess = { bitmap ->
                    releaseOwnedBitmap()
                    ownedBitmap = bitmap
                    binding.imageBitmap.setImageBitmap(bitmap)
                    binding.textBitmapStatus.text = getString(
                        R.string.image_demo_bitmap_success,
                        bitmap.width,
                        bitmap.height
                    )
                },
                onFailure = {
                    binding.textBitmapStatus.setText(
                        R.string.image_demo_bitmap_failed
                    )
                }
            )
            bitmapLoadJob = null
        }
    }

    private fun clearImages() {
        bitmapLoadJob?.cancel()
        bitmapLoadJob = null
        Glide.with(this).run {
            clear(binding.imageRounded)
            clear(binding.imageCircle)
            clear(binding.imageError)
            clear(binding.imageFallback)
        }
        releaseOwnedBitmap()
        binding.progressBitmap.isVisible = false
        binding.buttonLoadBitmap.isEnabled = true
        binding.textBitmapStatus.setText(R.string.image_demo_bitmap_idle)
    }

    private fun releaseOwnedBitmap() {
        binding.imageBitmap.setImageDrawable(null)
        ownedBitmap?.recycle()
        ownedBitmap = null
    }

    override fun onDestroy() {
        bitmapLoadJob?.cancel()
        bitmapLoadJob = null
        releaseOwnedBitmap()
        super.onDestroy()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
