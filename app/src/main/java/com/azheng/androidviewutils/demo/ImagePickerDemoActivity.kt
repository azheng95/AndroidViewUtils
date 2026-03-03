package com.azheng.androidviewutils.demo

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.imagepicker.ImagePicker
import com.azheng.viewutils.imagepicker.ImagePickerConfig
import com.azheng.viewutils.imagepicker.ImagePickerResult
import com.azheng.viewutils.imagepicker.getVideoDurationFormatted
import com.azheng.viewutils.imagepicker.getVideoThumbnail
import com.azheng.viewutils.imagepicker.isVideo
import com.azheng.viewutils.imagepicker.toMediaInfo
import com.azheng.viewutils.imagepicker.toMediaInfoList
import kotlinx.coroutines.launch

/**
 *
 */
class ImagePickerDemoActivity : BaseEdgeActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// ==================== 选择图片 ====================
        ImagePicker.pickImages(this@ImagePickerDemoActivity, maxCount = 3) { result ->
            when (result) {
                is ImagePickerResult.Success -> {
                    result.uris.forEach { uri ->
                        Log.d("Picker", "图片: $uri")
                    }
                }

                is ImagePickerResult.Cancelled -> Log.d("Picker", "取消")
                is ImagePickerResult.Error -> Log.e("Picker", "错误", result.exception)
            }
        }

// ==================== 选择视频 ====================
        ImagePicker.pickVideos(this@ImagePickerDemoActivity, maxCount = 1) { result ->
            if (result.isSuccessful) {
                val videoUri = result.getUrisOrEmpty().first()
                // 获取视频信息
                val duration = videoUri.getVideoDurationFormatted(this@ImagePickerDemoActivity)
                val thumbnail = videoUri.getVideoThumbnail(this@ImagePickerDemoActivity)
                Log.d("Picker", "视频时长: $duration")
            }
        }

// ==================== 选择图片或视频 ====================
        ImagePicker.pickMedia(this@ImagePickerDemoActivity, maxCount = 5) { result ->
            result.getUrisOrEmpty().forEach { uri ->
                if (uri.isVideo(this@ImagePickerDemoActivity)) {
                    Log.d(
                        "Picker",
                        "视频: $uri, 时长: ${uri.getVideoDurationFormatted(this@ImagePickerDemoActivity)}"
                    )
                } else {
                    Log.d("Picker", "图片: $uri")
                }
            }
        }

// ==================== 完整配置 ====================
        val config = ImagePickerConfig.Builder()
            .maxCount(3)
            .imageAndVideo()
            .maxVideoLengthSeconds(60)  // 限制60秒（需自行在回调中过滤）
            .enablePersistPermission(true)
            .build()

        ImagePicker.pick(this@ImagePickerDemoActivity, config) { result ->
            // 处理结果
        }

// ==================== 协程方式 ====================
        lifecycleScope.launch {
            // 选择视频
            val videoUris =
                ImagePicker.pickVideosSuspend(this@ImagePickerDemoActivity, maxCount = 1)
            if (videoUris.isNotEmpty()) {
                val info = videoUris.first().toMediaInfo(this@ImagePickerDemoActivity)
                Log.d(
                    "Picker",
                    "视频: ${info.fileName}, 时长: ${info.videoDurationFormatted}, 大小: ${info.fileSizeFormatted}"
                )
            }

            // 选择图片或视频
            val mediaUris = ImagePicker.pickMediaSuspend(this@ImagePickerDemoActivity, maxCount = 5)
            val mediaInfoList = mediaUris.toMediaInfoList(this@ImagePickerDemoActivity)
            mediaInfoList.forEach { info ->
                if (info.isVideo) {
                    Log.d("Picker", "视频: ${info.fileName}, ${info.videoDurationFormatted}")
                } else {
                    Log.d("Picker", "图片: ${info.fileName}")
                }
            }
        }

    }

}
