package com.azheng.viewutils.imagepicker

/**
 * 最大限制信息
 */
data class MaxLimitInfo(
    val maxCount: Int,
    val currentCount: Int
) {
    val message: String get() = "最多只能选择 $maxCount 张，当前已选择 $currentCount 张"
    val remainingCount: Int get() = (maxCount - currentCount).coerceAtLeast(0)
}