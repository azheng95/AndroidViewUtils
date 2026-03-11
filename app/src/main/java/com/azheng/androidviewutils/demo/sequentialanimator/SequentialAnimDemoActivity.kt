package com.azheng.androidviewutils.demo.sequentialanimator

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.azheng.androidviewutils.demo.databinding.ActivitySequentialAnimDemoBinding
import com.azheng.viewutils.sequentialanimator.SequentialAnim
import com.azheng.viewutils.sequentialanimator.AnimationConfig
import com.azheng.viewutils.sequentialanimator.AnimationController
import com.azheng.viewutils.sequentialanimator.AnimationDirection
import com.azheng.viewutils.sequentialanimator.CustomStrategy
import com.azheng.viewutils.sequentialanimator.ScaleFadeStrategy
import com.azheng.viewutils.sequentialanimator.animateSequentially
import com.azheng.viewutils.sequentialanimator.animateSequentiallyWithListener

/**
 * 顺序动画库示例 Activity
 *
 * 展示 SequentialAnim 库的各种使用方式：
 * 1. 基础用法 - 扩展函数调用
 * 2. 不同方向的动画效果
 * 3. 不同策略的动画效果（淡入滑动、缩放淡入、自定义）
 * 4. DSL 风格配置
 * 5. 监听器的使用
 * 6. 预定义配置（快速模式、慢速模式）
 *
 * 内存安全说明：
 * - 使用 lifecycleScope 确保协程随 Activity 生命周期自动取消
 * - 在 onDestroy 中调用 release() 彻底释放资源
 * - SequentialAnimator 内部使用 WeakReference 持有 View
 */
class SequentialAnimDemoActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivitySequentialAnimDemoBinding

    // 动画控制器，用于控制动画的取消、跳过等操作
    // 使用可空类型，便于在 onDestroy 中置空
    private var animationController: AnimationController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySequentialAnimDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化点击事件
        setupClickListeners()

        // 页面首次加载时执行入场动画
        // 使用 post 确保布局完成后再执行动画
        binding.root.post {
            playEnterAnimation()
        }
    }

    /**
     * 设置各按钮的点击事件
     */
    private fun setupClickListeners() {
        // 从下到上动画
        binding.btnBottomToTop.setOnClickListener {
            playAnimation(AnimationDirection.BOTTOM_TO_TOP, "从下到上动画")
        }

        // 从上到下动画
        binding.btnTopToBottom.setOnClickListener {
            playAnimation(AnimationDirection.TOP_TO_BOTTOM, "从上到下动画")
        }

        // 从左到右动画
        binding.btnLeftToRight.setOnClickListener {
            playAnimation(AnimationDirection.LEFT_TO_RIGHT, "从左到右动画")
        }

        // 缩放效果
        binding.btnScaleFade.setOnClickListener {
            playScaleFadeAnimation()
        }

        // 快速模式
        binding.btnFastMode.setOnClickListener {
            playFastAnimation()
        }

        // 自定义动画
        binding.btnCustom.setOnClickListener {
            playCustomAnimation()
        }
    }

    /**
     * 获取需要动画的 View 列表
     * 按照从上到下的视觉顺序排列
     */
    private fun getAnimatedViews(): List<View> {
        return listOf(
            binding.tvTitle,        // 标题
            binding.tvSubtitle,     // 副标题
            binding.cardFeature1,   // 功能卡片1
            binding.cardFeature2,   // 功能卡片2
            binding.cardFeature3,   // 功能卡片3
            binding.cardFeature4,   // 功能卡片4
            binding.tvStatus,       // 状态文字
            binding.llButtons,      // 按钮区域（作为整体）
            binding.tvFooter        // 底部信息
        )
    }

    /**
     * 页面入场动画
     * 使用最简洁的扩展函数调用方式
     */
    private fun playEnterAnimation() {
        updateStatus("正在播放入场动画...")

        // 先释放之前的控制器
        releaseAnimationController()

        // 使用 lifecycleScope：
        // - 当 Activity 销毁时，协程会自动取消
        // - 不需要手动管理协程的生命周期
        animationController = getAnimatedViews().animateSequentiallyWithListener(
            scope = lifecycleScope,  // 使用 lifecycleScope 确保生命周期安全
            configBlock = {
                delayBetween(80L)           // 每个 View 间隔 80ms
                duration(300L)               // 每个动画持续 300ms
                translationDistance(60f)     // 初始偏移 60dp
                direction(AnimationDirection.BOTTOM_TO_TOP)  // 从下往上
            },
            listenerBlock = {
                onAnimationStart {
                    // 动画开始时的回调
                }
                onAnimationEnd {
                    // 所有动画完成时的回调
                    updateStatus("入场动画完成！点击按钮尝试其他效果")
                }
            }
        )
    }

    /**
     * 播放指定方向的动画
     * 使用 DSL 风格配置
     *
     * @param direction 动画方向
     * @param message 提示信息
     */
    private fun playAnimation(direction: AnimationDirection, message: String) {
        // 先释放之前的控制器
        releaseAnimationController()

        updateStatus("正在播放: $message")

        // DSL 风格配置
        animationController = getAnimatedViews().animateSequentially(lifecycleScope) {
            delayBetween(100L)
            duration(350L)
            translationDistance(80f)
            direction(direction)
        }

        showToast(message)
    }

    /**
     * 播放缩放淡入效果
     * 使用 ScaleFadeStrategy 策略
     */
    private fun playScaleFadeAnimation() {
        releaseAnimationController()

        updateStatus("正在播放: 缩放淡入效果")

        // 使用预定义策略
        val config = AnimationConfig.builder()
            .delayBetween(100L)
            .duration(400L)
            .strategy(ScaleFadeStrategy(startScale = 0.3f))  // 从 30% 大小开始缩放
            .build()

        animationController = getAnimatedViews().animateSequentially(
            scope = lifecycleScope,
            config = config
        )

        showToast("缩放淡入效果")
    }

    /**
     * 播放快速模式动画
     * 使用预定义的 FAST 配置
     */
    private fun playFastAnimation() {
        releaseAnimationController()

        updateStatus("正在播放: 快速模式")

        // 使用预定义配置
        animationController = getAnimatedViews().animateSequentially(
            scope = lifecycleScope,
            config = SequentialAnim.Configs.FAST  // 预定义的快速配置
        )

        showToast("快速模式 (间隔50ms, 持续200ms)")
    }

    /**
     * 播放自定义动画效果
     * 使用 CustomStrategy 实现旋转+淡入效果
     */
    private fun playCustomAnimation() {
        releaseAnimationController()

        updateStatus("正在播放: 自定义旋转动画")

        // 完全自定义动画
        val customStrategy = CustomStrategy(
            // 初始化 View 状态
            onInitialize = { view, config ->
                view.alpha = 0f           // 初始透明
                view.rotation = -15f      // 初始旋转 -15 度
                view.translationX = -100f // 初始向左偏移
            },
            // 配置动画属性
            onConfigure = { animator, view, config ->
                animator
                    .alpha(1f)            // 淡入
                    .rotation(0f)         // 旋转归位
                    .translationX(0f)     // 位移归位
            }
        )

        // 使用建造者模式
        animationController = SequentialAnim.with(getAnimatedViews())
            .config {
                delayBetween(120L)
                duration(450L)
                strategy(customStrategy)
                interpolator(OvershootInterpolator(1.2f))  // 弹性插值器
            }
            .buildAndStart(lifecycleScope)

        showToast("自定义旋转效果")
    }

    /**
     * 释放动画控制器
     *
     * 调用 release() 而不仅仅是 cancel()，确保：
     * 1. 取消正在执行的动画
     * 2. 清理内部持有的引用（包括 listener）
     */
    private fun releaseAnimationController() {
        animationController?.release()
        animationController = null
    }

    /**
     * 更新状态文字
     */
    private fun updateStatus(message: String) {
        binding.tvStatus.text = message
    }

    /**
     * 显示 Toast 提示
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Activity 销毁时释放资源
     *
     * 虽然使用 lifecycleScope 时协程会自动取消，
     * 但调用 release() 可以更彻底地清理资源（如 listener）
     */
    override fun onDestroy() {
        super.onDestroy()
        // 释放动画控制器
        releaseAnimationController()
    }
}
