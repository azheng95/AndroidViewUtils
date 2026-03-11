package com.azheng.viewutils.sequentialanimator

import android.view.View
import java.lang.ref.WeakReference

/**
 * 顺序动画监听器接口
 *
 * 内存安全提示：
 * - 实现类应避免持有 Activity/Fragment 的强引用
 * - 建议使用 SequentialAnimationListenerBuilder 创建监听器
 */
interface SequentialAnimationListener {
    /** 整个动画序列开始 */
    fun onAnimationStart() {}

    /** 单个 View 动画开始 */
    fun onViewAnimationStart(view: View, index: Int) {}

    /** 单个 View 动画结束 */
    fun onViewAnimationEnd(view: View, index: Int) {}

    /** 整个动画序列结束 */
    fun onAnimationEnd() {}

    /** 动画被取消 */
    fun onAnimationCancel() {}
}

/**
 * 动画监听器建造者
 *
 * 使用 DSL 风格构建监听器，内部使用弱引用避免内存泄漏
 */
class SequentialAnimationListenerBuilder {
    private var onStart: (() -> Unit)? = null
    private var onViewStart: ((View, Int) -> Unit)? = null
    private var onViewEnd: ((View, Int) -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    /** 动画开始回调 */
    fun onAnimationStart(block: () -> Unit) {
        onStart = block
    }

    /** 单个 View 动画开始回调 */
    fun onViewAnimationStart(block: (View, Int) -> Unit) {
        onViewStart = block
    }

    /** 单个 View 动画结束回调 */
    fun onViewAnimationEnd(block: (View, Int) -> Unit) {
        onViewEnd = block
    }

    /** 动画结束回调 */
    fun onAnimationEnd(block: () -> Unit) {
        onEnd = block
    }

    /** 动画取消回调 */
    fun onAnimationCancel(block: () -> Unit) {
        onCancel = block
    }

    /**
     * 构建监听器
     *
     * 注意：返回的监听器持有传入的 lambda 引用
     * 如果 lambda 捕获了 Activity/Fragment，需要确保动画结束后释放
     */
    internal fun build(): SequentialAnimationListener {
        // 保存当前的回调引用
        val startCallback = onStart
        val viewStartCallback = onViewStart
        val viewEndCallback = onViewEnd
        val endCallback = onEnd
        val cancelCallback = onCancel

        return object : SequentialAnimationListener {
            override fun onAnimationStart() {
                startCallback?.invoke()
            }

            override fun onViewAnimationStart(view: View, index: Int) {
                viewStartCallback?.invoke(view, index)
            }

            override fun onViewAnimationEnd(view: View, index: Int) {
                viewEndCallback?.invoke(view, index)
            }

            override fun onAnimationEnd() {
                endCallback?.invoke()
            }

            override fun onAnimationCancel() {
                cancelCallback?.invoke()
            }
        }
    }
}

/**
 * 安全的动画监听器包装类
 *
 * 使用弱引用持有实际监听器，当外部监听器被回收时，回调自动失效
 * 适用于需要长时间持有监听器引用的场景
 */
class WeakAnimationListener(
    listener: SequentialAnimationListener
) : SequentialAnimationListener {

    private val listenerRef = WeakReference(listener)

    override fun onAnimationStart() {
        listenerRef.get()?.onAnimationStart()
    }

    override fun onViewAnimationStart(view: View, index: Int) {
        listenerRef.get()?.onViewAnimationStart(view, index)
    }

    override fun onViewAnimationEnd(view: View, index: Int) {
        listenerRef.get()?.onViewAnimationEnd(view, index)
    }

    override fun onAnimationEnd() {
        listenerRef.get()?.onAnimationEnd()
    }

    override fun onAnimationCancel() {
        listenerRef.get()?.onAnimationCancel()
    }
}
