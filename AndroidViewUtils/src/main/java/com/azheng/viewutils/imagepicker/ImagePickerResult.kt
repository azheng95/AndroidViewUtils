package com.azheng.viewutils.imagepicker

import android.net.Uri

/**
 * 选择结果封装
 */
sealed class ImagePickerResult {
    data class Success(val uris: List<Uri>) : ImagePickerResult()
    data object Cancelled : ImagePickerResult()
    data class Error(val exception: Throwable) : ImagePickerResult()

    /** 已达到最大选择限制 */
    data class MaxLimitReached(
        val maxCount: Int,
        val currentCount: Int,
        val message: String = "最多只能选择 $maxCount 张，当前已选择 $currentCount 张"
    ) : ImagePickerResult() {
        val remainingCount: Int get() = (maxCount - currentCount).coerceAtLeast(0)

        /** 转换为 MaxLimitInfo */
        fun toInfo(): MaxLimitInfo = MaxLimitInfo(maxCount, currentCount)
    }

    /** 是否成功且有数据 */
    val isSuccessful: Boolean get() = this is Success && uris.isNotEmpty()

    /** 获取 Uri 列表（失败返回空列表） */
    fun getUrisOrEmpty(): List<Uri> = (this as? Success)?.uris ?: emptyList()
}
