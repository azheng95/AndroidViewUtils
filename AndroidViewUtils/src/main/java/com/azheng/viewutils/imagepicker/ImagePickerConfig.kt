package com.azheng.viewutils.imagepicker

import android.net.Uri

/**
 * 图片/视频选择配置
 */
class ImagePickerConfig private constructor(
    val maxCount: Int,
    val mediaType: MediaType,
    val enablePersistPermission: Boolean,
    val maxVideoLength: Long  // 新增：视频最大时长限制（毫秒），0表示不限制
) {

    enum class MediaType {
        IMAGE_ONLY,
        VIDEO_ONLY,
        IMAGE_AND_VIDEO
    }

    class Builder {
        private var maxCount: Int = 9
        private var mediaType: MediaType = MediaType.IMAGE_ONLY
        private var enablePersistPermission: Boolean = true
        private var maxVideoLength: Long = 0L

        /** 最大选择数量 */
        fun maxCount(count: Int) = apply { this.maxCount = count.coerceIn(1, 99) }

        /** 媒体类型 */
        fun mediaType(type: MediaType) = apply { this.mediaType = type }

        /** 仅选择图片 */
        fun imageOnly() = apply { this.mediaType = MediaType.IMAGE_ONLY }

        /** 仅选择视频 */
        fun videoOnly() = apply { this.mediaType = MediaType.VIDEO_ONLY }

        /** 图片和视频都可选 */
        fun imageAndVideo() = apply { this.mediaType = MediaType.IMAGE_AND_VIDEO }

        /** 是否获取持久URI权限 */
        fun enablePersistPermission(enable: Boolean) = apply { this.enablePersistPermission = enable }

        /** 视频最大时长限制（毫秒） */
        fun maxVideoLength(millis: Long) = apply { this.maxVideoLength = millis }

        /** 视频最大时长限制（秒） */
        fun maxVideoLengthSeconds(seconds: Int) = apply { this.maxVideoLength = seconds * 1000L }

        fun build() = ImagePickerConfig(maxCount, mediaType, enablePersistPermission, maxVideoLength)
    }

    companion object {
        fun default() = Builder().build()

        /** 快捷方法：仅图片 */
        fun imageOnly(maxCount: Int = 9) = Builder()
            .maxCount(maxCount)
            .imageOnly()
            .build()

        /** 快捷方法：仅视频 */
        fun videoOnly(maxCount: Int = 9) = Builder()
            .maxCount(maxCount)
            .videoOnly()
            .build()

        /** 快捷方法：图片和视频 */
        fun imageAndVideo(maxCount: Int = 9) = Builder()
            .maxCount(maxCount)
            .imageAndVideo()
            .build()
    }
}

/**
 * 选择结果封装
 */
sealed class ImagePickerResult {
    data class Success(val uris: List<Uri>) : ImagePickerResult()
    data object Cancelled : ImagePickerResult()
    data class Error(val exception: Throwable) : ImagePickerResult()

    /** 是否成功且有数据 */
    val isSuccessful: Boolean get() = this is Success && uris.isNotEmpty()

    /** 获取 Uri 列表（失败返回空列表） */
    fun getUrisOrEmpty(): List<Uri> = (this as? Success)?.uris ?: emptyList()
}
