package com.azheng.viewutils.sequentialanimator

/**
 * 动画控制器接口
 * 用于控制动画的生命周期
 */
interface AnimationController {

    /**
     * 取消动画
     * 会触发 onAnimationCancel 回调
     */
    fun cancel()

    /**
     * 动画是否正在运行
     */
    fun isRunning(): Boolean

    /**
     * 跳过动画，直接将所有 View 设置为最终状态
     * 会触发 onAnimationEnd 回调
     */
    fun skipToEnd()

    /**
     * 释放所有资源
     * 当确定不再使用此动画器时调用
     * 调用后不应再使用此控制器
     */
    fun release() {
        // 默认实现：仅取消动画
        cancel()
    }
}
