package com.azheng.viewutils.sequentialanimator

import android.view.View
import android.view.ViewPropertyAnimator

/**
 * 动画策略接口 - 策略模式核心
 *
 * 定义动画的初始化和执行逻辑
 */
interface AnimationStrategy {

    /**
     * 初始化 View 状态（动画开始前）
     *
     * @param view 目标 View
     * @param config 动画配置
     */
    fun initializeView(view: View, config: AnimationConfig)

    /**
     * 配置动画属性
     *
     * @param animator ViewPropertyAnimator 实例
     * @param view 目标 View
     * @param config 动画配置
     * @return 配置后的 ViewPropertyAnimator
     */
    fun configureAnimation(
        animator: ViewPropertyAnimator,
        view: View,
        config: AnimationConfig
    ): ViewPropertyAnimator
}

/**
 * 淡入 + 滑动策略（默认策略）
 *
 * 根据配置的方向，从指定方向滑入并淡入
 */
class FadeSlideStrategy : AnimationStrategy {

    override fun initializeView(view: View, config: AnimationConfig) {
        view.alpha = 0f
        when (config.direction) {
            AnimationDirection.TOP_TO_BOTTOM -> {
                view.translationY = -config.translationDistance
            }
            AnimationDirection.BOTTOM_TO_TOP -> {
                view.translationY = config.translationDistance
            }
            AnimationDirection.LEFT_TO_RIGHT -> {
                view.translationX = -config.translationDistance
            }
            AnimationDirection.RIGHT_TO_LEFT -> {
                view.translationX = config.translationDistance
            }
            AnimationDirection.NONE -> {
                // 仅透明度变化，无位移
            }
        }
    }

    override fun configureAnimation(
        animator: ViewPropertyAnimator,
        view: View,
        config: AnimationConfig
    ): ViewPropertyAnimator {
        return animator.alpha(1f).apply {
            when (config.direction) {
                AnimationDirection.TOP_TO_BOTTOM,
                AnimationDirection.BOTTOM_TO_TOP -> translationY(0f)
                AnimationDirection.LEFT_TO_RIGHT,
                AnimationDirection.RIGHT_TO_LEFT -> translationX(0f)
                AnimationDirection.NONE -> { /* 无位移动画 */ }
            }
        }
    }
}

/**
 * 缩放 + 淡入策略
 *
 * 从指定的初始缩放值放大到原始大小，同时淡入
 *
 * @param startScale 初始缩放比例，默认 0.5 (50%)
 */
class ScaleFadeStrategy(
    private val startScale: Float = 0.5f
) : AnimationStrategy {

    override fun initializeView(view: View, config: AnimationConfig) {
        view.alpha = 0f
        view.scaleX = startScale
        view.scaleY = startScale
    }

    override fun configureAnimation(
        animator: ViewPropertyAnimator,
        view: View,
        config: AnimationConfig
    ): ViewPropertyAnimator {
        return animator
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
    }
}

/**
 * 纯淡入策略
 *
 * 仅透明度变化，无位移或缩放
 */
class FadeStrategy : AnimationStrategy {

    override fun initializeView(view: View, config: AnimationConfig) {
        view.alpha = 0f
    }

    override fun configureAnimation(
        animator: ViewPropertyAnimator,
        view: View,
        config: AnimationConfig
    ): ViewPropertyAnimator {
        return animator.alpha(1f)
    }
}

/**
 * 自定义策略
 *
 * 通过 lambda 自定义动画逻辑，提供最大灵活性
 *
 * @param onInitialize 初始化 View 状态的 lambda
 * @param onConfigure 配置动画属性的 lambda
 */
class CustomStrategy(
    private val onInitialize: (View, AnimationConfig) -> Unit,
    private val onConfigure: (ViewPropertyAnimator, View, AnimationConfig) -> ViewPropertyAnimator
) : AnimationStrategy {

    override fun initializeView(view: View, config: AnimationConfig) {
        onInitialize(view, config)
    }

    override fun configureAnimation(
        animator: ViewPropertyAnimator,
        view: View,
        config: AnimationConfig
    ): ViewPropertyAnimator {
        return onConfigure(animator, view, config)
    }
}
