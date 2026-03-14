package com.azheng.viewutils.imagepicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 最大限制信息
 */
@Parcelize
data class MaxLimitInfo(
    val maxCount: Int,
    val currentCount: Int
) : Parcelable {
    val message: String get() = "最多只能选择 $maxCount 张，当前已选择 $currentCount 张"
    val remainingCount: Int get() = (maxCount - currentCount).coerceAtLeast(0)
}