package com.azheng.viewutils.sequentialanimator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewPropertyAnimator
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 顺序动画执行器 - 核心类
 *
 * 内存安全设计：
 * 1. 使用 WeakReference 持有 View，避免阻止 View 被回收
 * 2. 动画完成/取消后清理所有引用
 * 3. 监听器在动画结束后自动解除引用
 */
class SequentialAnimator private constructor(
    views: List<View>,
    private var config: AnimationConfig?  // 修改为可空，便于释放
) : AnimationController {

    // 使用弱引用持有 View 列表，防止内存泄漏
    private val viewRefs: List<WeakReference<View>> = views.map { WeakReference(it) }

    // 动画协程 Job
    private var animationJob: Job? = null

    // 动画状态标记
    private val isAnimating = AtomicBoolean(false)

    // 已完成动画的 View 计数
    private val completedCount = AtomicInteger(0)

    // 活跃的动画器列表（用于取消）
    // 注意：ViewPropertyAnimator 本身不会阻止 View 回收，因为它内部也是弱引用
    private val activeAnimators = mutableListOf<ViewPropertyAnimator>()

    /**
     * 获取有效的 View 列表（过滤掉已被回收的 View）
     */
    private fun getValidViews(): List<View> {
        return viewRefs.mapNotNull { it.get() }
    }

    /**
     * 启动动画
     *
     * @param scope 协程作用域，建议使用 lifecycleScope 确保生命周期安全
     * @return 动画控制器
     */
    fun start(scope: CoroutineScope): AnimationController {
        val views = getValidViews()
        val currentConfig = config

        // 如果没有有效的 View 或配置为空，直接返回
        if (views.isEmpty() || currentConfig == null) return this

        // 取消之前的动画
        cancel()

        isAnimating.set(true)
        completedCount.set(0)
        activeAnimators.clear()

        // 初始化所有 View 的状态
        views.forEach { view ->
            currentConfig.strategy.initializeView(view, currentConfig)
        }

        // 通知动画开始
        currentConfig.listener?.onAnimationStart()

        // 使用传入的 scope 启动协程
        // 如果使用 lifecycleScope，当 Activity/Fragment 销毁时会自动取消
        animationJob = scope.launch {
            try {
                // 整体开始延迟
                if (currentConfig.startDelay > 0) {
                    delay(currentConfig.startDelay)
                }

                views.forEachIndexed { index, view ->
                    // 检查协程是否仍然活跃
                    if (!isActive) return@launch

                    // 计算并执行延迟
                    if (index > 0) {
                        delay(currentConfig.delayBetween)
                    }

                    // 再次检查
                    if (!isActive) return@launch

                    // 在主线程执行动画
                    withContext(Dispatchers.Main.immediate) {
                        // 检查 View 是否仍然有效（可能在延迟期间被回收）
                        if (viewRefs[index].get() != null) {
                            animateView(view, index, views.size, currentConfig)
                        }
                    }
                }
            } catch (e: CancellationException) {
                // 协程被取消，这是正常情况，不需要特殊处理
                throw e
            }
        }

        return this
    }

    /**
     * 执行单个 View 的动画
     *
     * @param view 目标 View
     * @param index View 在列表中的索引
     * @param totalCount View 总数
     * @param currentConfig 当前配置
     */
    private fun animateView(
        view: View,
        index: Int,
        totalCount: Int,
        currentConfig: AnimationConfig
    ) {
        // 通知单个 View 动画开始
        currentConfig.listener?.onViewAnimationStart(view, index)

        // 使用弱引用持有当前对象，避免 AnimatorListener 泄漏
        val animatorRef = WeakReference(this)
        val configRef = WeakReference(currentConfig)

        val animator = view.animate()
            .setDuration(currentConfig.duration)
            .setInterpolator(currentConfig.interpolator)
            .also { anim ->
                // 应用策略配置的动画属性
                currentConfig.strategy.configureAnimation(anim, view, currentConfig)

                // 硬件加速层
                if (currentConfig.useHardwareLayer) {
                    anim.withLayer()
                }

                // 设置动画监听器
                anim.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        val self = animatorRef.get() ?: return
                        val cfg = configRef.get()

                        // 从活跃列表中移除
                        self.activeAnimators.remove(anim)

                        // 通知单个 View 动画结束
                        cfg?.listener?.onViewAnimationEnd(view, index)

                        // 检查是否所有动画都完成
                        if (self.completedCount.incrementAndGet() == totalCount) {
                            self.isAnimating.set(false)
                            cfg?.listener?.onAnimationEnd()

                            // 动画全部完成后，清理引用
                            self.cleanupReferences()
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        val self = animatorRef.get() ?: return
                        self.activeAnimators.remove(anim)
                    }
                })
            }

        // 添加到活跃动画列表
        activeAnimators.add(animator)

        // 启动动画
        animator.start()
    }

    /**
     * 清理引用，防止内存泄漏
     * 在动画完成或取消后调用
     */
    private fun cleanupReferences() {
        activeAnimators.clear()
        // 注意：不清理 viewRefs，因为是弱引用，不会阻止 GC
        // config 包含 listener，在需要时可以置空
        // 但为了支持重新启动动画，这里保留 config
    }

    /**
     * 取消动画
     */
    override fun cancel() {
        // 1. 取消协程
        animationJob?.cancel()
        animationJob = null

        // 2. 取消所有活跃的属性动画
        // 创建副本遍历，因为 cancel 会触发 onAnimationCancel 修改列表
        activeAnimators.toList().forEach { animator ->
            animator.cancel()
        }
        activeAnimators.clear()

        // 3. 通知取消
        if (isAnimating.getAndSet(false)) {
            config?.listener?.onAnimationCancel()
        }

        // 4. 重置计数
        completedCount.set(0)
    }

    /**
     * 动画是否正在运行
     */
    override fun isRunning(): Boolean = isAnimating.get()

    /**
     * 跳过动画，直接设置为最终状态
     */
    override fun skipToEnd() {
        // 先取消当前动画
        cancel()

        // 将所有有效 View 设置为最终状态
        getValidViews().forEach { view ->
            view.alpha = 1f
            view.translationX = 0f
            view.translationY = 0f
            view.scaleX = 1f
            view.scaleY = 1f
            view.rotation = 0f
        }

        // 通知完成
        config?.listener?.onAnimationEnd()
    }

    /**
     * 释放所有资源
     * 当确定不再使用此动画器时调用
     */
    override fun release() {
        cancel()
        config = null  // 释放配置（包括 listener）
    }

    /**
     * 建造者类
     */
    class Builder {
        private val views = mutableListOf<View>()
        private var config: AnimationConfig = AnimationConfig.DEFAULT

        /**
         * 添加单个 View
         */
        fun addView(view: View) = apply {
            views.add(view)
        }

        /**
         * 添加多个 View（可变参数）
         */
        fun addViews(vararg viewList: View) = apply {
            views.addAll(viewList)
        }

        /**
         * 添加 View 列表
         */
        fun addViews(viewList: List<View>) = apply {
            views.addAll(viewList)
        }

        /**
         * 设置配置对象
         */
        fun config(config: AnimationConfig) = apply {
            this.config = config
        }

        /**
         * 使用 DSL 方式配置
         */
        fun config(block: AnimationConfig.Builder.() -> Unit) = apply {
            this.config = AnimationConfig.Builder().apply(block).build()
        }

        /**
         * 构建 SequentialAnimator 实例
         */
        fun build(): SequentialAnimator {
            return SequentialAnimator(views.toList(), config)
        }

        /**
         * 构建并立即启动动画
         *
         * @param scope 协程作用域
         * @return 动画控制器
         */
        fun buildAndStart(scope: CoroutineScope): AnimationController {
            return build().start(scope)
        }
    }

    companion object {
        /**
         * 创建建造者
         */
        fun builder() = Builder()

        /**
         * 快速创建并启动动画
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
            return SequentialAnimator(views, config).start(scope)
        }
    }
}
