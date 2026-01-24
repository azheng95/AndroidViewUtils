package com.azheng.viewutils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

/**
 * 弧形进度条
 * 支持链式调用批量设置参数，统一应用并重绘，减少性能消耗
 *
 * 链式调用示例：
 * arcProgressBar
 *     .setBgColor(Color.parseColor("#F5F5F5"))
 *     .setProgressColor(Color.parseColor("#FF673AB7"))
 *     .setStrokeWidth(50f)
 *     .setStartAngle(180)
 *     .setSweepAngle(180)
 *     .setProgress(80f)
 *     .setUseGradient(true)
 *     .setGradientColors(intArrayOf(Color.RED, Color.YELLOW))
 *     .apply() // 统一应用所有参数，只执行一次重绘/更新
 */
class ArcProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 添加动画引用，方便取消
    private var progressAnimator: ValueAnimator? = null

    // 底色画笔
    private val bgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // 进度画笔
    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // ==================== 实际生效的参数（原有） ====================
    private var _bgColor: Int = Color.parseColor("#F0F0F0")
    private var _progressColor: Int = Color.parseColor("#FFC107")
    private var _progress: Float = 0f
    private var _maxProgress: Float = 100f
    private var _strokeWidth: Float = 40f
    private var _startAngle: Int = 180
    private var _sweepAngle: Int = 180

    // 渐变颜色数组
    private var _gradientColors: IntArray = intArrayOf(
        Color.parseColor("#EBB400"),
        Color.parseColor("#EBB400")
    )
    private var _gradientPositions: FloatArray? = floatArrayOf(0f, 1f)
    private var _useGradient: Boolean = true

    // ==================== 临时缓存参数（新增：链式调用专用） ====================
    // 标记是否有临时参数待应用
    private var hasPendingChanges = false
    // 临时参数（初始值与实际参数一致）
    private var pendingBgColor: Int = _bgColor
    private var pendingProgressColor: Int = _progressColor
    private var pendingProgress: Float = _progress
    private var pendingMaxProgress: Float = _maxProgress
    private var pendingStrokeWidth: Float = _strokeWidth
    private var pendingStartAngle: Int = _startAngle
    private var pendingSweepAngle: Int = _sweepAngle
    private var pendingUseGradient: Boolean = _useGradient
    private var pendingGradientColors: IntArray = _gradientColors
    private var pendingGradientPositions: FloatArray? = _gradientPositions
    private var pendingStrokeCap: Paint.Cap = bgPaint.strokeCap

    // ==================== 绘制相关（原有） ====================
    // 圆弧绘制区域
    private val rectF = RectF()
    // 渐变渲染器
    private var sweepGradient: SweepGradient? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcProgressBar)
        _bgColor = typedArray.getColor(R.styleable.ArcProgressBar_arc_bg_color, _bgColor)
        _progressColor = typedArray.getColor(R.styleable.ArcProgressBar_arc_progress_color, _progressColor)
        _progress = typedArray.getFloat(R.styleable.ArcProgressBar_arc_progress, _progress)
        _maxProgress = typedArray.getFloat(R.styleable.ArcProgressBar_arc_max_progress, _maxProgress)
        _strokeWidth = typedArray.getDimension(R.styleable.ArcProgressBar_arc_stroke_width, _strokeWidth)
        _startAngle = typedArray.getInt(R.styleable.ArcProgressBar_arc_start_angle, _startAngle)
        _sweepAngle = typedArray.getInt(R.styleable.ArcProgressBar_arc_sweep_angle, _sweepAngle)
        typedArray.recycle()

        // 初始化临时参数（新增）
        resetPendingParams()
        updatePaints()
    }

    // ==================== 私有方法（原有+新增） ====================
    /**
     * 重置临时参数为当前实际参数（新增）
     */
    private fun resetPendingParams() {
        pendingBgColor = _bgColor
        pendingProgressColor = _progressColor
        pendingProgress = _progress
        pendingMaxProgress = _maxProgress
        pendingStrokeWidth = _strokeWidth
        pendingStartAngle = _startAngle
        pendingSweepAngle = _sweepAngle
        pendingUseGradient = _useGradient
        pendingGradientColors = _gradientColors
        pendingGradientPositions = _gradientPositions
        pendingStrokeCap = bgPaint.strokeCap
        hasPendingChanges = false
    }

    /**
     * 统一应用临时参数并更新View（新增核心）
     */
    private fun applyPendingChanges() {
        if (!hasPendingChanges) return

        // 标记是否需要更新画笔
        var needUpdatePaints = false
        // 标记是否需要更新绘制区域
        var needUpdateRectF = false
        // 标记是否需要更新渐变
        var needUpdateGradient = false
        // 标记是否需要重绘
        var needInvalidate = false

        // 1. 处理底色变化
        if (pendingBgColor != _bgColor) {
            _bgColor = pendingBgColor
            bgPaint.color = _bgColor
            needInvalidate = true
        }

        // 2. 处理进度颜色变化
        if (pendingProgressColor != _progressColor) {
            _progressColor = pendingProgressColor
            progressPaint.color = _progressColor
            needUpdatePaints = true
            needInvalidate = true
        }

        // 3. 处理进度变化
        if (pendingProgress != _progress) {
            _progress = pendingProgress.coerceIn(0f, pendingMaxProgress)
            needInvalidate = true
        }

        // 4. 处理最大进度变化
        if (pendingMaxProgress != _maxProgress) {
            _maxProgress = pendingMaxProgress
            _progress = _progress.coerceIn(0f, _maxProgress)
            pendingProgress = _progress // 同步临时参数
            needInvalidate = true
        }

        // 5. 处理圆弧宽度变化
        if (pendingStrokeWidth != _strokeWidth) {
            _strokeWidth = pendingStrokeWidth
            needUpdatePaints = true
            needUpdateRectF = true
            needUpdateGradient = true
            needInvalidate = true
        }

        // 6. 处理起始角度变化
        if (pendingStartAngle != _startAngle) {
            _startAngle = pendingStartAngle
            needUpdateGradient = true
            needInvalidate = true
        }

        // 7. 处理扫过角度变化
        if (pendingSweepAngle != _sweepAngle) {
            _sweepAngle = pendingSweepAngle
            needInvalidate = true
        }

        // 8. 处理渐变开关变化
        if (pendingUseGradient != _useGradient) {
            _useGradient = pendingUseGradient
            progressPaint.shader = if (_useGradient) sweepGradient else null
            needInvalidate = true
        }

        // 9. 处理渐变颜色变化
        if (!pendingGradientColors.contentEquals(_gradientColors) || pendingGradientPositions != _gradientPositions) {
            _gradientColors = pendingGradientColors
            _gradientPositions = pendingGradientPositions
            _useGradient = true
            pendingUseGradient = true // 同步临时参数
            needUpdateGradient = true
            needInvalidate = true
        }

        // 10. 处理端点样式变化
        if (pendingStrokeCap != bgPaint.strokeCap) {
            bgPaint.strokeCap = pendingStrokeCap
            progressPaint.strokeCap = pendingStrokeCap
            needInvalidate = true
        }

        // 按需执行更新操作（避免重复执行）
        if (needUpdatePaints) {
            updatePaints()
        }
        if (needUpdateRectF) {
            updateRectF()
        }
        if (needUpdateGradient) {
            updateGradient()
        }
        if (needInvalidate) {
            invalidate()
        }

        // 重置临时参数
        resetPendingParams()
    }

    private fun updatePaints() {
        bgPaint.strokeWidth = _strokeWidth
        bgPaint.color = _bgColor
        progressPaint.strokeWidth = _strokeWidth
        progressPaint.color = _progressColor
    }

    private fun updateGradient() {
        if (width == 0 || height == 0) return

        sweepGradient = SweepGradient(
            width / 2f, height / 2f,
            _gradientColors,
            _gradientPositions
        )
        val matrix = Matrix()
        matrix.setRotate(_startAngle.toFloat(), width / 2f, height / 2f)
        sweepGradient?.setLocalMatrix(matrix)

        if (_useGradient) {
            progressPaint.shader = sweepGradient
        }
    }

    private fun updateRectF() {
        val padding = _strokeWidth / 2
        rectF.set(padding, padding, width - padding, height - padding)
    }

    // ==================== View 生命周期方法（原有） ====================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = minOf(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateRectF()
        updateGradient()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制底色圆弧
        canvas.drawArc(
            rectF,
            _startAngle.toFloat(),
            _sweepAngle.toFloat(),
            false,
            bgPaint
        )

        // 绘制进度圆弧
        val progressSweepAngle = (_progress / _maxProgress) * _sweepAngle
        canvas.drawArc(
            rectF,
            _startAngle.toFloat(),
            progressSweepAngle,
            false,
            progressPaint
        )
    }

    // ==================== 公开属性（原有，兼容链式调用） ====================
    /** 当前进度 */
    var progress: Float
        get() = _progress
        set(value) {
            // 复用链式方法，立即应用
            setProgress(value).apply()
        }

    /** 最大进度 */
    var maxProgress: Float
        get() = _maxProgress
        set(value) {
            setMaxProgress(value).apply()
        }

    /** 底色 */
    var bgColor: Int
        get() = _bgColor
        set(value) {
            setBgColor(value).apply()
        }

    /** 进度条颜色（纯色） */
    var progressColor: Int
        get() = _progressColor
        set(value) {
            setProgressColor(value).apply()
        }

    /** 圆弧宽度 */
    var strokeWidth: Float
        get() = _strokeWidth
        set(value) {
            setStrokeWidth(value).apply()
        }

    /** 起始角度 */
    var startAngle: Int
        get() = _startAngle
        set(value) {
            setStartAngle(value).apply()
        }

    /** 扫过角度 */
    var sweepAngle: Int
        get() = _sweepAngle
        set(value) {
            setSweepAngle(value).apply()
        }

    /** 是否使用渐变 */
    var useGradient: Boolean
        get() = _useGradient
        set(value) {
            setUseGradient(value).apply()
        }

    // ==================== 链式调用方法（新增核心） ====================
    /**
     * 链式设置底色
     */
    fun setBgColor(color: Int): ArcProgressBar {
        pendingBgColor = color
        hasPendingChanges = true
        return this // 返回this实现链式调用
    }

    /**
     * 链式设置进度条颜色（纯色，自动关闭渐变）
     */
    fun setProgressColor(color: Int): ArcProgressBar {
        pendingProgressColor = color
        pendingUseGradient = false
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置当前进度
     */
    fun setProgress(progress: Float): ArcProgressBar {
        pendingProgress = progress
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置最大进度
     */
    fun setMaxProgress(maxProgress: Float): ArcProgressBar {
        pendingMaxProgress = maxProgress
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置圆弧宽度
     */
    fun setStrokeWidth(width: Float): ArcProgressBar {
        pendingStrokeWidth = width
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置起始角度
     */
    fun setStartAngle(angle: Int): ArcProgressBar {
        pendingStartAngle = angle
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置扫过角度
     */
    fun setSweepAngle(angle: Int): ArcProgressBar {
        pendingSweepAngle = angle
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置是否使用渐变
     */
    fun setUseGradient(useGradient: Boolean): ArcProgressBar {
        pendingUseGradient = useGradient
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置渐变颜色（自动开启渐变）
     * @param colors 颜色数组
     * @param positions 位置数组（可选，传null则均匀分布）
     */
    fun setGradientColors(colors: IntArray, positions: FloatArray? = null): ArcProgressBar {
        pendingGradientColors = colors
        pendingGradientPositions = positions
        pendingUseGradient = true
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置画笔端点样式
     * @param cap Paint.Cap.ROUND / BUTT / SQUARE
     */
    fun setStrokeCap(cap: Paint.Cap): ArcProgressBar {
        pendingStrokeCap = cap
        hasPendingChanges = true
        return this
    }

    /**
     * 统一应用所有链式设置的参数（核心：只执行一次必要的更新和重绘）
     */
    fun apply(): ArcProgressBar {
        applyPendingChanges()
        return this
    }


    /**
     * 设置进度（带动画）
     * @param targetProgress 目标进度
     * @param duration 动画时长（毫秒）
     */
    fun setProgressWithAnimation(targetProgress: Float, duration: Long = 300L) {
        progressAnimator?.cancel()

        val target = targetProgress.coerceIn(0f, _maxProgress)

        progressAnimator = ValueAnimator.ofFloat(_progress, target).apply {
            this.duration = duration
            addUpdateListener {
                _progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * 恢复渐变填充
     */
    fun restoreGradient(): ArcProgressBar {
        pendingUseGradient = true
        hasPendingChanges = true
        return this
    }


    /**
     * 获取进度百分比
     */
    fun getProgressPercent(): Float = _progress / _maxProgress * 100

    /**
     * 取消动画
     */
    fun cancelAnimation() {
        progressAnimator?.cancel()
        progressAnimator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // View 被移除时自动取消动画，防止泄漏
        cancelAnimation()
    }
}