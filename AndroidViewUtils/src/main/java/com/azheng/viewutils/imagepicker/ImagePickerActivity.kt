package com.azheng.viewutils.imagepicker

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
internal class ImagePickerActivity : AppCompatActivity() {

    private val config: ImagePickerConfig by lazy { ImagePicker.getConfig() }

    private var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTransparentTheme()
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        registerLaunchers()

        if (savedInstanceState == null) {
            launchPicker()
        }
    }

    private fun registerLaunchers() {
        pickSingleMedia = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            handleResult(if (uri != null) listOf(uri) else emptyList())
        }

        // 使用 remainingCount 而不是 maxCount
        // 实际选择时会通过 launchPicker() 根据 remainingCount 决定使用单选还是多选
        val selectableCount = config.remainingCount.coerceAtLeast(2)

        pickMultipleMedia = registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(selectableCount)
        ) { uris ->
            handleResult(uris)
        }
    }

    private fun launchPicker() {
        val mediaType = when (config.mediaType) {
            ImagePickerConfig.MediaType.IMAGE_ONLY ->
                ActivityResultContracts.PickVisualMedia.ImageOnly
            ImagePickerConfig.MediaType.VIDEO_ONLY ->
                ActivityResultContracts.PickVisualMedia.VideoOnly
            ImagePickerConfig.MediaType.IMAGE_AND_VIDEO ->
                ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }

        val request = PickVisualMediaRequest(mediaType)

        // ✅ 根据剩余可选数量决定单选还是多选
        if (config.remainingCount == 1) {
            pickSingleMedia?.launch(request)
        } else {
            pickMultipleMedia?.launch(request)
        }
    }

    private fun handleResult(uris: List<Uri>) {
        val result = if (uris.isEmpty()) {
            ImagePickerResult.Cancelled
        } else {
            // ✅ 使用 remainingCount 限制
            val limitedUris = uris.take(config.remainingCount)

            if (config.enablePersistPermission) {
                limitedUris.forEach { uri -> takePersistPermission(uri) }
            }

            ImagePickerResult.Success(limitedUris)
        }

        ImagePicker.deliverResult(result)
        finishQuietly()
    }

    private fun takePersistPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) { }
    }

    private fun finishQuietly() {
        finish()
        overridePendingTransition(0, 0)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        ImagePicker.deliverResult(ImagePickerResult.Cancelled)
    }

    private fun applyTransparentTheme() {
        // 尝试使用 Material3 主题，失败则使用 AppCompat
        try {
            val themeId = resolveThemeId("Theme.Material3.DayNight.NoActionBar")
            if (themeId != 0) {
                setTheme(themeId)
            }
        } catch (e: Exception) {
            // fallback
        }

        // 手动设置透明属性
        window.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    private fun resolveThemeId(themeName: String): Int {
        return try {
            val clazz = Class.forName("com.google.android.material.R\$style")
            clazz.getField(themeName.replace(".", "_")).getInt(null)
        } catch (e: Exception) {
            0
        }
    }
}
