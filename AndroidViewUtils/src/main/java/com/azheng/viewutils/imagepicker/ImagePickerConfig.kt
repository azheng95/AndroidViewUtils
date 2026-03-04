package com.azheng.viewutils.imagepicker

import android.net.Uri

/**
 * 图片/视频选择配置
 */
class ImagePickerConfig private constructor(
    val maxCount: Int,
    val mediaType: MediaType,
    val enablePersistPermission: Boolean,
    val maxVideoLength: Long,
    val currentSelectedCount: Int,
    val onMaxLimitReached: ((MaxLimitInfo) -> Unit)?  // ✅ 修改为带参数的回调
) {

    /** 还能选择的数量 */
    val remainingCount: Int get() = (maxCount - currentSelectedCount).coerceAtLeast(0)

    /** 是否已达到最大限制 */
    val isMaxLimitReached: Boolean get() = remainingCount <= 0

    /** 获取限制信息 */
    val maxLimitInfo: MaxLimitInfo get() = MaxLimitInfo(maxCount, currentSelectedCount)

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
        private var currentSelectedCount: Int = 0
        private var onMaxLimitReached: ((MaxLimitInfo) -> Unit)? = null

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

        /**
         * 当前已选择的数量
         */
        fun currentSelectedCount(count: Int) = apply {
            this.currentSelectedCount = count.coerceAtLeast(0)
        }

        /**
         * 传入已选择的 Uri 列表，自动计算数量
         */
        fun currentSelectedUris(uris: List<Uri>?) = apply {
            this.currentSelectedCount = uris?.size ?: 0
        }

        /**
         * 达到最大选择限制时的回调
         * @param callback 回调函数，参数包含 maxCount、currentCount、message
         */
        fun onMaxLimitReached(callback: (MaxLimitInfo) -> Unit) = apply {
            this.onMaxLimitReached = callback
        }

        fun build() = ImagePickerConfig(
            maxCount,
            mediaType,
            enablePersistPermission,
            maxVideoLength,
            currentSelectedCount,
            onMaxLimitReached
        )
    }

    companion object {
        fun default() = Builder().build()

        fun imageOnly(maxCount: Int = 9, currentSelected: Int = 0) = Builder()
            .maxCount(maxCount)
            .currentSelectedCount(currentSelected)
            .imageOnly()
            .build()

        fun videoOnly(maxCount: Int = 9, currentSelected: Int = 0) = Builder()
            .maxCount(maxCount)
            .currentSelectedCount(currentSelected)
            .videoOnly()
            .build()

        fun imageAndVideo(maxCount: Int = 9, currentSelected: Int = 0) = Builder()
            .maxCount(maxCount)
            .currentSelectedCount(currentSelected)
            .imageAndVideo()
            .build()
    }
}
