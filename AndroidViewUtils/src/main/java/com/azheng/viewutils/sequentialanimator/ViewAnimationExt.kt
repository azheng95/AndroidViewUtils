package com.azheng.viewutils.sequentialanimator

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import kotlinx.coroutines.CoroutineScope

// ==================== List<View> 扩展函数 ====================

/**
 * 最简单的使用方式
 *
 * 内存安全：使用 lifecycleScope 时，协程会在 Activity/Fragment 销毁时自动取消
 *
 * @param scope 协程作用域，建议使用 lifecycleScope
 * @param config 动画配置
 * @return 动画控制器，可用于取消动画或释放资源
 */
fun List<View>.animateSequentially(
    scope: CoroutineScope,
    config: AnimationConfig = AnimationConfig.DEFAULT
): AnimationController {
    return SequentialAnimator.animate(this, scope, config)
}

/**
 * 带参数的快捷方式
 *
 * @param scope 协程作用域
 * @param delayBetween 每个 View 动画之间的延迟
 * @param duration 每个动画的持续时间
 * @param translationDistance 位移距离
 * @param direction 动画方向
 * @return 动画控制器
 */
fun List<View>.animateSequentially(
    scope: CoroutineScope,
    delayBetween: Long = 100L,
    duration: Long = 300L,
    translationDistance: Float = 80f,
    direction: AnimationDirection = AnimationDirection.BOTTOM_TO_TOP
): AnimationController {
    val config = AnimationConfig.builder()
        .delayBetween(delayBetween)
        .duration(duration)
        .translationDistance(translationDistance)
        .direction(direction)
        .build()
    return SequentialAnimator.animate(this, scope, config)
}

/**
 * DSL 风格配置
 *
 * 使用示例：
 * ```kotlin
 * views.animateSequentially(lifecycleScope) {
 *     delayBetween(100L)
 *     duration(300L)
 *     direction(AnimationDirection.BOTTOM_TO_TOP)
 * }
 * ```
 *
 * @param scope 协程作用域
 * @param block 配置 DSL
 * @return 动画控制器
 */
fun List<View>.animateSequentially(
    scope: CoroutineScope,
    block: AnimationConfig.Builder.() -> Unit
): AnimationController {
    val config = AnimationConfig.Builder().apply(block).build()
    return SequentialAnimator.animate(this, scope, config)
}

/**
 * 带监听器的 DSL 风格
 *
 * 使用示例：
 * ```kotlin
 * views.animateSequentiallyWithListener(
 *     scope = lifecycleScope,
 *     configBlock = {
 *         delayBetween(100L)
 *         duration(300L)
 *     },
 *     listenerBlock = {
 *         onAnimationStart { Log.d("Anim", "开始") }
 *         onAnimationEnd { Log.d("Anim", "结束") }
 *     }
 * )
 * ```
 *
 * 内存安全提示：
 * - listenerBlock 中的 lambda 可能捕获外部变量
 * - 使用 lifecycleScope 时，协程会自动取消，但建议在 onDestroy 中调用 release()
 *
 * @param scope 协程作用域
 * @param configBlock 配置 DSL
 * @param listenerBlock 监听器 DSL
 * @return 动画控制器
 */
fun List<View>.animateSequentiallyWithListener(
    scope: CoroutineScope,
    configBlock: AnimationConfig.Builder.() -> Unit = {},
    listenerBlock: SequentialAnimationListenerBuilder.() -> Unit
): AnimationController {
    val listener = SequentialAnimationListenerBuilder().apply(listenerBlock).build()
    val config = AnimationConfig.Builder()
        .apply(configBlock)
        .listener(listener)
        .build()
    return SequentialAnimator.animate(this, scope, config)
}

// ==================== ViewGroup 扩展函数 ====================

/**
 * 对 ViewGroup 的直接子 View 执行顺序动画
 *
 * @param scope 协程作用域
 * @param config 动画配置
 * @return 动画控制器
 */
fun ViewGroup.animateChildrenSequentially(
    scope: CoroutineScope,
    config: AnimationConfig = AnimationConfig.DEFAULT
): AnimationController {
    return children.toList().animateSequentially(scope, config)
}

/**
 * 对 ViewGroup 的直接子 View 执行顺序动画（DSL 风格）
 *
 * @param scope 协程作用域
 * @param block 配置 DSL
 * @return 动画控制器
 */
fun ViewGroup.animateChildrenSequentially(
    scope: CoroutineScope,
    block: AnimationConfig.Builder.() -> Unit
): AnimationController {
    return children.toList().animateSequentially(scope, block)
}

// ==================== 单个 View 扩展函数 ====================

/**
 * 单个 View 的入场动画
 *
 * @param scope 协程作用域
 * @param config 动画配置
 * @return 动画控制器
 */
fun View.animateEntrance(
    scope: CoroutineScope,
    config: AnimationConfig = AnimationConfig.DEFAULT
): AnimationController {
    return listOf(this).animateSequentially(scope, config)
}

/**
 * 单个 View 的入场动画（带参数）
 *
 * @param scope 协程作用域
 * @param duration 动画持续时间
 * @param translationDistance 位移距离
 * @param direction 动画方向
 * @return 动画控制器
 */
fun View.animateEntrance(
    scope: CoroutineScope,
    duration: Long = 300L,
    translationDistance: Float = 80f,
    direction: AnimationDirection = AnimationDirection.BOTTOM_TO_TOP
): AnimationController {
    return listOf(this).animateSequentially(
        scope = scope,
        delayBetween = 0L,
        duration = duration,
        translationDistance = translationDistance,
        direction = direction
    )
}
