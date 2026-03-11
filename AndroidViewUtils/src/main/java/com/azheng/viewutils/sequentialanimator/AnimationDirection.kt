package com.azheng.viewutils.sequentialanimator

/**
 * 动画进入方向
 */
enum class AnimationDirection {
    /** 从上往下 */
    TOP_TO_BOTTOM,

    /** 从下往上 */
    BOTTOM_TO_TOP,

    /** 从左往右 */
    LEFT_TO_RIGHT,

    /** 从右往左 */
    RIGHT_TO_LEFT,

    /** 无位移，仅透明度变化 */
    NONE
}
