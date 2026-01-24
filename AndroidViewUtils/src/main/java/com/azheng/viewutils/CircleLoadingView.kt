package com.azheng.viewutils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 * 圆形加载视图
 *
 * 功能特性：
 * - 支持链式调用批量设置参数，统一应用并重绘，减少性能消耗
 * - 支持配置/动态设置背景环、进度环的起始角度和扫描角度
 * - 支持生命周期绑定，自动管理动画资源
 *
 * 使用示例：
 * ```
 * circleLoadingView
 *     .setCircleStrokeWidthDp(10f)
 *     .setProgressColor(Color.RED)
 *     .setTargetProgress(75f)
 *     .apply()  // 统一应用所有更改
 *     .startLoadingAnimation()
 * ```
 *
 * @param context 上下文
 * @param attrs XML属性集合
 * @param defStyleAttr 默认样式属性
 */
class CircleLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==================== 画笔对象 ====================

    /** 背景环画笔，用于绘制底部的圆环轨道 */
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 进度环画笔，用于绘制当前进度的弧形 */
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 环形绘制区域，定义圆弧的边界矩形 */
    private val circleRect = RectF()

    // ==================== 实际生效的参数（当前正在使用的值） ====================

    /** 圆环的线条宽度（单位：像素） */
    private var circleStrokeWidth = dp2px(8f)

    /** 进度环的颜色 */
    private var progressColor = ContextCompat.getColor(context, R.color.CircleLoadingViewProgressColor)

    /** 背景环的颜色 */
    private var bgCircleColor = ContextCompat.getColor(context, R.color.CircleLoadingViewBgCircleColor)

    /** 动画持续时间（单位：毫秒） */
    private var animationDuration = 2000L

    /** 目标进度值（范围：0-100） */
    private var targetProgress = 80f

    // ---------- 角度参数（控制圆弧的起始位置和绘制范围） ----------

    /**
     * 背景环起始角度
     * 270f = 12点钟方向（Android坐标系中，0°在3点钟方向，顺时针增加）
     */
    private var bgStartAngle = 270f

    /** 背景环扫描角度（360f表示绘制完整圆环） */
    private var bgSweepAngle = 360f

    /** 进度环起始角度（默认从12点钟方向开始） */
    private var progressStartAngle = 270f

    /**
     * 进度环总扫描角度
     * 负值表示逆时针绘制，正值表示顺时针绘制
     * -360f = 逆时针绘制完整圆（视觉上是顺时针效果）
     */
    private var progressSweepAngle = -360f

    // ==================== 临时缓存参数（链式调用专用，apply后才生效） ====================

    /** 标记是否有待应用的参数变更 */
    private var hasPendingChanges = false

    // 原有临时参数（缓存用户设置的新值，直到调用apply）
    private var pendingCircleStrokeWidth: Int = circleStrokeWidth
    private var pendingProgressColor: Int = progressColor
    private var pendingBgCircleColor: Int = bgCircleColor
    private var pendingAnimationDuration: Long = animationDuration
    private var pendingTargetProgress: Float = targetProgress.coerceIn(0f, 100f)

    // 新增角度临时参数
    private var pendingBgStartAngle: Float = bgStartAngle
    private var pendingBgSweepAngle: Float = bgSweepAngle
    private var pendingProgressStartAngle: Float = progressStartAngle
    private var pendingProgressSweepAngle: Float = progressSweepAngle

    /** 当前动画进度值（范围：0-100），由动画驱动更新 */
    private var currentProgress = 0f

    /**
     * 进度动画器
     * 使用lazy延迟初始化，确保线程安全且只创建一次
     */
    private val progressAnimator by lazy {
        createProgressAnimator()
    }

    /**
     * 生命周期观察者
     * 用于在宿主（Activity/Fragment）销毁时自动停止动画
     */
    private var lifecycleObserver: DefaultLifecycleObserver? = null

    // ==================== 初始化代码块 ====================
    init {
        // 解析XML中定义的自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CircleLoadingView)

            // 解析原有属性（线条宽度、颜色、动画时长、目标进度）
            circleStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CircleLoadingView_circleStrokeWidth,
                dp2px(8f)  // 默认8dp
            )
            progressColor = typedArray.getColor(
                R.styleable.CircleLoadingView_progressColor,
                progressColor
            )
            bgCircleColor = typedArray.getColor(
                R.styleable.CircleLoadingView_bgCircleColor,
                bgCircleColor
            )
            animationDuration = typedArray.getInt(
                R.styleable.CircleLoadingView_animationDuration,
                2000  // 默认2秒
            ).toLong()
            targetProgress = typedArray.getFloat(
                R.styleable.CircleLoadingView_targetProgress,
                80f  // 默认80%
            ).coerceIn(0f, 100f)  // 限制范围在0-100

            // 解析新增角度属性（带默认值，保证向后兼容）
            bgStartAngle = typedArray.getFloat(
                R.styleable.CircleLoadingView_bgStartAngle,
                270f  // 默认12点钟方向
            )
            bgSweepAngle = typedArray.getFloat(
                R.styleable.CircleLoadingView_bgSweepAngle,
                360f  // 默认完整圆环
            )
            progressStartAngle = typedArray.getFloat(
                R.styleable.CircleLoadingView_progressStartAngle,
                270f
            )
            progressSweepAngle = typedArray.getFloat(
                R.styleable.CircleLoadingView_progressSweepAngle,
                -360f  // 默认逆时针（视觉顺时针）
            )

            // 回收TypedArray，避免内存泄漏
            typedArray.recycle()
        }

        // 同步临时参数为初始值
        resetPendingParams()
        // 初始化画笔样式
        initPaint()
    }

    /**
     * 创建进度动画器
     *
     * 动画从0到targetProgress进行数值变化，
     * 每次数值更新时刷新当前进度并触发重绘
     *
     * @return 配置好的ValueAnimator实例
     */
    private fun createProgressAnimator(): ValueAnimator {
        return ValueAnimator.ofFloat(0f, targetProgress).apply {
            duration = animationDuration
            addUpdateListener { anim ->
                // 安全检查：仅当View附着到Window时才更新，避免无效绘制
                if (isAttachedToWindow) {
                    currentProgress = anim.animatedValue as Float
                    invalidate()  // 触发onDraw重绘
                }
            }
        }
    }

    /**
     * 重置所有临时参数为当前实际参数值
     *
     * 调用时机：
     * 1. 初始化完成后
     * 2. apply()应用完参数后
     */
    private fun resetPendingParams() {
        // 重置原有参数
        pendingCircleStrokeWidth = circleStrokeWidth
        pendingProgressColor = progressColor
        pendingBgCircleColor = bgCircleColor
        pendingAnimationDuration = animationDuration
        pendingTargetProgress = targetProgress

        // 重置角度参数
        pendingBgStartAngle = bgStartAngle
        pendingBgSweepAngle = bgSweepAngle
        pendingProgressStartAngle = progressStartAngle
        pendingProgressSweepAngle = progressSweepAngle

        // 清除变更标记
        hasPendingChanges = false
    }

    /**
     * 统一应用所有待处理的参数变更
     *
     * 核心逻辑：
     * 1. 检测每个参数是否真正发生变化
     * 2. 只更新有变化的部分，减少性能消耗
     * 3. 最后统一触发重绘（如需要）
     */
    private fun applyPendingChanges() {
        // 无变更直接返回
        if (!hasPendingChanges) return

        // 标记需要执行的更新操作
        var needUpdatePaint = false      // 是否需要重新初始化画笔
        var needUpdateAnimator = false   // 是否需要更新动画器配置
        var needUpdateRectF = false      // 是否需要重新计算绘制区域
        var needInvalidate = false       // 是否需要触发重绘

        // ---------- 1. 处理原有参数 ----------

        // 线条宽度变化：需要更新画笔、绘制区域和重绘
        if (pendingCircleStrokeWidth != circleStrokeWidth) {
            circleStrokeWidth = pendingCircleStrokeWidth
            needUpdatePaint = true
            needUpdateRectF = true
            needInvalidate = true
        }

        // 进度颜色变化：直接更新画笔颜色并重绘
        if (pendingProgressColor != progressColor) {
            progressColor = pendingProgressColor
            progressPaint.color = progressColor
            needInvalidate = true
        }

        // 背景颜色变化：直接更新画笔颜色并重绘
        if (pendingBgCircleColor != bgCircleColor) {
            bgCircleColor = pendingBgCircleColor
            bgPaint.color = bgCircleColor
            needInvalidate = true
        }

        // 动画时长变化：更新动画器配置
        if (pendingAnimationDuration != animationDuration) {
            animationDuration = pendingAnimationDuration
            progressAnimator.duration = animationDuration
            needUpdateAnimator = true
        }

        // 目标进度变化：更新动画器的目标值
        if (pendingTargetProgress != targetProgress) {
            targetProgress = pendingTargetProgress
            progressAnimator.setFloatValues(0f, targetProgress)
            needUpdateAnimator = true
        }

        // ---------- 2. 处理角度参数（仅需重绘，无需其他更新） ----------

        if (pendingBgStartAngle != bgStartAngle) {
            bgStartAngle = pendingBgStartAngle
            needInvalidate = true
        }
        if (pendingBgSweepAngle != bgSweepAngle) {
            bgSweepAngle = pendingBgSweepAngle
            needInvalidate = true
        }
        if (pendingProgressStartAngle != progressStartAngle) {
            progressStartAngle = pendingProgressStartAngle
            needInvalidate = true
        }
        if (pendingProgressSweepAngle != progressSweepAngle) {
            progressSweepAngle = pendingProgressSweepAngle
            needInvalidate = true
        }

        // ---------- 3. 按需执行更新操作 ----------

        if (needUpdatePaint) initPaint()           // 重新初始化画笔
        if (needUpdateRectF) updateCircleRect()    // 重新计算绘制区域
        if (needInvalidate && isAttachedToWindow) invalidate()  // 安全触发重绘

        // 重置临时参数，准备下一轮链式调用
        resetPendingParams()
    }

    /**
     * 更新环形绘制区域
     *
     * 计算逻辑：根据View的宽高和线条宽度，确定圆弧的边界矩形
     * 边界需要向内收缩半个线条宽度，确保线条不会超出View边界
     */
    private fun updateCircleRect() {
        val padding = circleStrokeWidth / 2f  // 内边距 = 线条宽度的一半
        circleRect.set(
            padding,           // 左边界
            padding,           // 上边界
            width - padding,   // 右边界
            height - padding   // 下边界
        )
    }

    /**
     * 初始化画笔样式
     *
     * 两个画笔使用相同的样式配置，仅颜色不同：
     * - Style.STROKE: 只绘制边框（空心）
     * - Cap.ROUND: 线条端点为圆形，使圆弧两端更美观
     */
    private fun initPaint() {
        // 配置背景环画笔
        bgPaint.run {
            style = Paint.Style.STROKE      // 空心样式
            strokeWidth = circleStrokeWidth.toFloat()  // 线条宽度
            color = bgCircleColor           // 背景颜色
            strokeCap = Paint.Cap.ROUND     // 圆形端点
        }

        // 配置进度环画笔
        progressPaint.run {
            style = Paint.Style.STROKE
            strokeWidth = circleStrokeWidth.toFloat()
            color = progressColor
            strokeCap = Paint.Cap.ROUND
        }
    }

    // ==================== View 生命周期回调方法 ====================

    /**
     * View尺寸发生变化时回调
     *
     * @param w 新宽度
     * @param h 新高度
     * @param oldw 旧宽度
     * @param oldh 旧高度
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 尺寸变化后必须重新计算绘制区域
        updateCircleRect()
    }

    /**
     * View可见性发生变化时回调
     *
     * @param changedView 可见性发生变化的View
     * @param visibility 新的可见性状态
     */
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // 当View不可见时停止动画，节省性能
        if (visibility != VISIBLE) {
            stopLoadingAnimation()
        }
    }

    /**
     * View从Window分离时回调
     *
     * 重要：必须在此处清理动画，防止内存泄漏和无效绘制
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoadingAnimation()
    }

    // ==================== 链式调用方法（返回this支持连续调用） ====================

    /**
     * 设置圆环线条宽度（dp单位）
     *
     * @param dp 线条宽度值，单位dp
     * @return 当前实例，支持链式调用
     */
    fun setCircleStrokeWidthDp(dp: Float): CircleLoadingView {
        pendingCircleStrokeWidth = dp2px(dp)
        hasPendingChanges = true
        return this
    }

    /**
     * 设置圆环线条宽度（像素单位）
     *
     * @param px 线条宽度值，单位像素
     * @return 当前实例，支持链式调用
     */
    fun setCircleStrokeWidthPx(px: Int): CircleLoadingView {
        pendingCircleStrokeWidth = px
        hasPendingChanges = true
        return this
    }

    /**
     * 设置进度环颜色
     *
     * @param color 颜色值（如Color.RED或资源颜色）
     * @return 当前实例，支持链式调用
     */
    fun setProgressColor(color: Int): CircleLoadingView {
        pendingProgressColor = color
        hasPendingChanges = true
        return this
    }

    /**
     * 设置背景环颜色
     *
     * @param color 颜色值
     * @return 当前实例，支持链式调用
     */
    fun setBgCircleColor(color: Int): CircleLoadingView {
        pendingBgCircleColor = color
        hasPendingChanges = true
        return this
    }

    /**
     * 设置动画持续时间
     *
     * @param duration 时长，单位毫秒
     * @return 当前实例，支持链式调用
     */
    fun setAnimationDuration(duration: Long): CircleLoadingView {
        pendingAnimationDuration = duration
        hasPendingChanges = true
        return this
    }

    /**
     * 设置目标进度值
     *
     * @param progress 进度值（0-100），超出范围会自动限制
     * @return 当前实例，支持链式调用
     */
    fun setTargetProgress(progress: Float): CircleLoadingView {
        pendingTargetProgress = progress.coerceIn(0f, 100f)
        hasPendingChanges = true
        return this
    }

    /**
     * 设置背景环起始角度
     *
     * 角度说明（Android坐标系）：
     * - 0° = 3点钟方向
     * - 90° = 6点钟方向
     * - 180° = 9点钟方向
     * - 270° = 12点钟方向
     *
     * @param angle 起始角度
     * @return 当前实例，支持链式调用
     */
    fun setBgStartAngle(angle: Float): CircleLoadingView {
        pendingBgStartAngle = angle
        hasPendingChanges = true
        return this
    }

    /**
     * 设置背景环扫描角度（绘制范围）
     *
     * @param angle 扫描角度，360为完整圆环，180为半圆
     * @return 当前实例，支持链式调用
     */
    fun setBgSweepAngle(angle: Float): CircleLoadingView {
        pendingBgSweepAngle = angle
        hasPendingChanges = true
        return this
    }

    /**
     * 设置进度环起始角度
     *
     * @param angle 起始角度
     * @return 当前实例，支持链式调用
     */
    fun setProgressStartAngle(angle: Float): CircleLoadingView {
        pendingProgressStartAngle = angle
        hasPendingChanges = true
        return this
    }

    /**
     * 设置进度环扫描角度
     *
     * @param angle 扫描角度，负值为逆时针，正值为顺时针
     * @return 当前实例，支持链式调用
     */
    fun setProgressSweepAngle(angle: Float): CircleLoadingView {
        pendingProgressSweepAngle = angle
        hasPendingChanges = true
        return this
    }

    /**
     * 应用所有待处理的参数变更
     *
     * 调用此方法后，之前通过链式调用设置的所有参数才会真正生效
     * 这种设计可以批量更新参数，只触发一次重绘，提高性能
     *
     * @return 当前实例，支持链式调用
     */
    fun apply(): CircleLoadingView {
        applyPendingChanges()
        return this
    }

    /**
     * 启动加载动画
     *
     * 安全处理：
     * - 先取消可能正在运行的动画
     * - 重新配置动画参数，确保使用最新设置
     * - 每次启动都从0开始
     *
     * @return 当前实例，支持链式调用
     */
    fun startLoadingAnimation(): CircleLoadingView {
        progressAnimator.apply {
            cancel()  // 取消当前动画（如果有）
            setFloatValues(0f, targetProgress)  // 设置动画范围
            duration = animationDuration  // 设置动画时长
            start()  // 启动动画
        }
        return this
    }

    /**
     * 停止加载动画
     *
     * 安全处理：
     * - 检查动画是否正在运行或暂停
     * - 重置进度为0
     * - 安全触发重绘
     *
     * @return 当前实例，支持链式调用
     */
    fun stopLoadingAnimation(): CircleLoadingView {
        progressAnimator.apply {
            if (isRunning || isPaused) cancel()  // 安全取消动画
        }
        currentProgress = 0f  // 重置进度
        if (isAttachedToWindow) invalidate()  // 安全重绘
        return this
    }

    /**
     * 绑定到生命周期所有者（Activity/Fragment）
     *
     * 作用：当宿主销毁时自动停止动画，防止内存泄漏
     *
     * 实现细节：
     * - 使用WeakReference弱引用持有View，避免循环引用
     * - 在onDestroy时自动清理资源
     *
     * @param owner 生命周期所有者（通常是Activity或Fragment）
     * @return 当前实例，支持链式调用
     */
    fun bindToLifecycle(owner: LifecycleOwner): CircleLoadingView {
        // 移除旧的观察者，防止重复绑定
        lifecycleObserver?.let { owner.lifecycle.removeObserver(it) }

        // 创建弱引用，避免Observer持有View导致内存泄漏
        val viewRef = WeakReference(this)

        // 创建新的生命周期观察者
        lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                // 宿主销毁时停止动画
                viewRef.get()?.stopLoadingAnimation()
                // 移除自身，避免重复回调
                owner.lifecycle.removeObserver(this)
            }
        }

        // 注册观察者
        owner.lifecycle.addObserver(lifecycleObserver!!)
        return this
    }

    /**
     * dp转px工具方法
     *
     * 使用Android官方推荐的TypedValue进行单位转换，
     * 确保在不同屏幕密度下的一致性
     *
     * @param dp dp值
     * @return 转换后的像素值
     */
    private fun dp2px(dp: Float): Int {
        return android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    // ==================== 绘制逻辑 ====================

    /**
     * 核心绘制方法
     *
     * 绘制顺序：
     * 1. 先绘制背景环（底层）
     * 2. 再绘制进度环（上层，覆盖在背景环上）
     *
     * @param canvas 画布对象
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. 绘制背景环（使用动态角度参数）
        canvas.drawArc(
            circleRect,        // 绘制区域
            bgStartAngle,      // 起始角度（如270f = 12点钟方向）
            bgSweepAngle,      // 扫描角度（如360f = 完整圆环）
            false,             // 不绘制圆心连线（false = 弧形，true = 扇形）
            bgPaint            // 背景画笔
        )

        // 2. 绘制进度环（仅当进度大于0时才绘制）
        if (currentProgress > 0) {
            // 计算当前应绘制的扫描角度
            // 公式：总扫描角度 × 进度百分比
            // 例如：progressSweepAngle = -360f，currentProgress = 50
            // 则 currentSweepAngle = -360 × (50/100) = -180°
            val currentSweepAngle = progressSweepAngle * (currentProgress / 100)

            canvas.drawArc(
                circleRect,          // 绘制区域
                progressStartAngle,  // 起始角度
                currentSweepAngle,   // 当前扫描角度（根据进度动态计算）
                false,               // 不绘制圆心连线
                progressPaint        // 进度画笔
            )
        }
    }
}
