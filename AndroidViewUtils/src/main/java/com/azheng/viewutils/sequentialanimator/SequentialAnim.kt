package com.azheng.viewutils.sequentialanimator

import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope

/**
 * 顺序动画库统一入口 - 门面模式
 *
 * 提供简洁的 API 入口，隐藏内部实现细节
 *
 * 使用示例：
 * ```kotlin
 * // 方式1：快速调用
 * SequentialAnim.animate(views, lifecycleScope)
 *
 * // 方式2：建造者模式
 * SequentialAnim.with(view1, view2, view3)
 *     .config { delayBetween(100L) }
 *     .buildAndStart(lifecycleScope)
 * ```
 */
object SequentialAnim {

    // ==================== 预定义策略 ====================

    /**
     * 预定义动画策略
     */
    object Strategies {
        /** 淡入 + 滑动（默认） */
        val FADE_SLIDE = FadeSlideStrategy()

        /** 纯淡入 */
        val FADE = FadeStrategy()

        /** 缩放 + 淡入（默认从 50% 开始） */
        val SCALE_FADE = ScaleFadeStrategy()

        /**
         * 创建自定义初始缩放的缩放淡入策略
         */
        fun scaleFade(startScale: Float = 0.5f) = ScaleFadeStrategy(startScale)
    }

    // ==================== 预定义配置 ====================

    /**
     * 预定义动画配置
     */
    object Configs {
        /** 默认配置 */
        val DEFAULT = AnimationConfig.DEFAULT

        /** 快速动画配置（间隔 50ms，持续 200ms） */
        val FAST = AnimationConfig.FAST

        /** 慢速动画配置（间隔 150ms，持续 400ms） */
        val SLOW = AnimationConfig.SLOW

        /** 获取配置建造者 */
        fun custom() = AnimationConfig.builder()
    }

    // ==================== 方向常量 ====================

    /**
     * 动画方向常量
     */
    object Direction {
        val TOP_TO_BOTTOM = AnimationDirection.TOP_TO_BOTTOM
        val BOTTOM_TO_TOP = AnimationDirection.BOTTOM_TO_TOP
        val LEFT_TO_RIGHT = AnimationDirection.LEFT_TO_RIGHT
        val RIGHT_TO_LEFT = AnimationDirection.RIGHT_TO_LEFT
        val NONE = AnimationDirection.NONE
    }

    // ==================== 快速 API ====================

    /**
     * 从多个 View 创建动画建造者
     *
     * @param views 可变参数，需要动画的 View
     * @return 动画建造者
     */
    fun with(vararg views: View): SequentialAnimator.Builder {
        return SequentialAnimator.builder().addViews(*views)
    }

    /**
     * 从 View 列表创建动画建造者
     *
     * @param views View 列表
     * @return 动画建造者
     */
    fun with(views: List<View>): SequentialAnimator.Builder {
        return SequentialAnimator.builder().addViews(views)
    }

    /**
     * 从 ViewGroup 的子 View 创建动画建造者
     *
     * @param viewGroup 父容器
     * @return 动画建造者
     */
    fun withChildren(viewGroup: ViewGroup): SequentialAnimator.Builder {
        val children = (0 until viewGroup.childCount).map { viewGroup.getChildAt(it) }
        return SequentialAnimator.builder().addViews(children)
    }

    /**
     * 最快速的调用方式
     *
     * @param views View 列表
     * @param scope 协程作用域
     * @param config 动画配置
     * @return 动画控制器
     */
    fun animate(
        views: List<View>,
        scope: CoroutineScope,
        config: AnimationConfig = AnimationConfig.DEFAULT
    ): AnimationController {
        return SequentialAnimator.animate(views, scope, config)
    }
}
