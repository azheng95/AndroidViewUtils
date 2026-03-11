package com.azheng.viewutils.sequentialanimator

import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

/**
 * 动画配置类
 *
 * 不可变数据类，包含动画的所有配置参数
 *
 * 内存安全提示：
 * - listener 可能持有 Activity/Fragment 引用
 * - 确保在 Activity/Fragment 销毁时调用 AnimationController.release()
 */
data class AnimationConfig(
    /** 每个 View 动画之间的延迟时间（毫秒） */
    val delayBetween: Long = 100L,

    /** 每个动画的持续时间（毫秒） */
    val duration: Long = 300L,

    /** 整体动画开始前的延迟时间（毫秒） */
    val startDelay: Long = 0L,

    /** 位移距离（像素） */
    val translationDistance: Float = 80f,

    /** 动画方向 */
    val direction: AnimationDirection = AnimationDirection.BOTTOM_TO_TOP,

    /** 动画插值器 */
    val interpolator: Interpolator = DecelerateInterpolator(1.5f),

    /** 动画策略 */
    val strategy: AnimationStrategy = FadeSlideStrategy(),

    /** 是否使用硬件加速层 */
    val useHardwareLayer: Boolean = true,

    /** 动画监听器（注意：可能持有外部引用，需在合适时机释放） */
    val listener: SequentialAnimationListener? = null
) {
    /**
     * 配置建造者
     */
    class Builder {
        private var delayBetween: Long = 100L
        private var duration: Long = 300L
        private var startDelay: Long = 0L
        private var translationDistance: Float = 80f
        private var direction: AnimationDirection = AnimationDirection.BOTTOM_TO_TOP
        private var interpolator: Interpolator = DecelerateInterpolator(1.5f)
        private var strategy: AnimationStrategy = FadeSlideStrategy()
        private var useHardwareLayer: Boolean = true
        private var listener: SequentialAnimationListener? = null

        /** 设置每个 View 动画之间的延迟 */
        fun delayBetween(delay: Long) = apply { this.delayBetween = delay }

        /** 设置每个动画的持续时间 */
        fun duration(duration: Long) = apply { this.duration = duration }

        /** 设置整体动画开始前的延迟 */
        fun startDelay(delay: Long) = apply { this.startDelay = delay }

        /** 设置位移距离 */
        fun translationDistance(distance: Float) = apply { this.translationDistance = distance }

        /** 设置动画方向 */
        fun direction(direction: AnimationDirection) = apply { this.direction = direction }

        /** 设置插值器 */
        fun interpolator(interpolator: Interpolator) = apply { this.interpolator = interpolator }

        /** 设置动画策略 */
        fun strategy(strategy: AnimationStrategy) = apply { this.strategy = strategy }

        /** 设置是否使用硬件加速层 */
        fun useHardwareLayer(use: Boolean) = apply { this.useHardwareLayer = use }

        /** 设置监听器 */
        fun listener(listener: SequentialAnimationListener) = apply { this.listener = listener }

        /** 构建配置对象 */
        fun build(): AnimationConfig {
            return AnimationConfig(
                delayBetween = delayBetween,
                duration = duration,
                startDelay = startDelay,
                translationDistance = translationDistance,
                direction = direction,
                interpolator = interpolator,
                strategy = strategy,
                useHardwareLayer = useHardwareLayer,
                listener = listener
            )
        }
    }

    companion object {
        /** 默认配置 */
        val DEFAULT = AnimationConfig()

        /** 快速动画配置 */
        val FAST = AnimationConfig(
            delayBetween = 50L,
            duration = 200L,
            translationDistance = 40f
        )

        /** 慢速动画配置 */
        val SLOW = AnimationConfig(
            delayBetween = 150L,
            duration = 400L,
            translationDistance = 100f
        )

        /** 获取建造者实例 */
        fun builder() = Builder()
    }
}
