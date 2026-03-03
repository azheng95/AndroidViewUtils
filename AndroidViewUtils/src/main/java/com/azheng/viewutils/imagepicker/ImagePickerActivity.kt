package com.azheng.viewutils.imagepicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

internal class ImagePickerActivity : AppCompatActivity() {

    private val config: ImagePickerConfig by lazy { ImagePicker.getConfig() }

    // 不在这里初始化，改为延迟创建
    private var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        // 在 onCreate 中根据 maxCount 动态注册
        registerLaunchers()

        if (savedInstanceState == null) {
            launchPicker()
        }
    }

    private fun registerLaunchers() {
        // 单选
        pickSingleMedia = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            handleResult(if (uri != null) listOf(uri) else emptyList())
        }

        // 多选 - 使用实际的 maxCount
        pickMultipleMedia = registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(config.maxCount)  // ✅ 动态值
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

        if (config.maxCount == 1) {
            pickSingleMedia?.launch(request)
        } else {
            pickMultipleMedia?.launch(request)
        }
    }

    private fun handleResult(uris: List<Uri>) {
        val result = if (uris.isEmpty()) {
            ImagePickerResult.Cancelled
        } else {
            // 双重保险：再次限制数量
            val limitedUris = uris.take(config.maxCount)

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
}
