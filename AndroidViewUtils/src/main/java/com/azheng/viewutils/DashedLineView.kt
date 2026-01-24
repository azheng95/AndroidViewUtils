package com.azheng.viewutils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View

/**
 * 自定义虚线 View
 *
 * 支持功能：
 * - 横向/纵向虚线
 * - 自定义虚线粗细、段长、间隔、颜色
 * - 支持 padding
 * - 支持代码动态修改属性（单独设置/链式批量设置）
 *
 * XML 使用示例：
 * <com.azheng.viewutils.DashedLineView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:dashedLineWidth="1dp"
 *     app:dashedLineDashWidth="4dp"
 *     app:dashedLineDashGap="2dp"
 *     app:dashedLineColor="#F5EFE0"
 *     app:dashedLineOrientation="horizontal" />
 *
 * 链式调用示例：
 * dashedLineView
 *     .setLineWidthDp(2f)
 *     .setDashParamsDp(6f, 3f)
 *     .setLineColor(Color.RED)
 *     .setOrientation(DashedLineView.VERTICAL)
 *     .apply() // 统一应用参数并重绘
 */
class DashedLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==================== 常量定义 ====================
    companion object {
        private const val DEFAULT_LINE_WIDTH_DP = 1f
        private const val DEFAULT_DASH_WIDTH_DP = 4f
        private const val DEFAULT_DASH_GAP_DP = 2f
        private const val DEFAULT_LINE_COLOR = "#F5EFE0"

        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }

    // 方向枚举
    enum class Orientation(val value: Int) {
        HORIZONTAL(DashedLineView.HORIZONTAL),
        VERTICAL(DashedLineView.VERTICAL);

        companion object {
            fun fromValue(value: Int): Orientation = if (value == VERTICAL.value) VERTICAL else HORIZONTAL
        }
    }

    // ==================== 实际生效的参数（原有） ====================
    private var lineWidth = dp2px(DEFAULT_LINE_WIDTH_DP)
    private var dashWidth = dp2px(DEFAULT_DASH_WIDTH_DP)
    private var dashGap = dp2px(DEFAULT_DASH_GAP_DP)
    private var lineColor = Color.parseColor(DEFAULT_LINE_COLOR)
    private var orientation: Orientation = Orientation.HORIZONTAL

    // ==================== 临时缓存参数（新增：用于链式调用） ====================
    // 标记是否有临时参数待应用
    private var hasPendingChanges = false
    // 临时缓存的参数（初始值与实际参数一致）
    private var pendingLineWidth: Float = lineWidth
    private var pendingDashWidth: Float = dashWidth
    private var pendingDashGap: Float = dashGap
    private var pendingLineColor: Int = lineColor
    private var pendingOrientation: Orientation = orientation

    // ==================== 绑定对象 ====================
    private val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()
    private var pathDirty = true

    // ==================== 初始化 ====================
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        context.obtainStyledAttributes(attrs, R.styleable.DashedLineView).use { ta ->
            lineWidth = ta.getDimension(R.styleable.DashedLineView_dashedLineWidth, dp2px(DEFAULT_LINE_WIDTH_DP))
                .coerceAtLeast(0.5f)
            dashWidth = ta.getDimension(R.styleable.DashedLineView_dashedLineDashWidth, dp2px(DEFAULT_DASH_WIDTH_DP))
                .coerceAtLeast(1f)
            dashGap = ta.getDimension(R.styleable.DashedLineView_dashedLineDashGap, dp2px(DEFAULT_DASH_GAP_DP))
                .coerceAtLeast(0f)
            lineColor = ta.getColor(R.styleable.DashedLineView_dashedLineColor, Color.parseColor(DEFAULT_LINE_COLOR))
            orientation = Orientation.fromValue(ta.getInt(R.styleable.DashedLineView_dashedLineOrientation, HORIZONTAL))
        }

        // 初始化临时参数（新增）
        resetPendingParams()
        applyPaintSettings()
    }

    // ==================== 私有方法 ====================
    /**
     * 重置临时参数为当前实际参数（新增）
     */
    private fun resetPendingParams() {
        pendingLineWidth = lineWidth
        pendingDashWidth = dashWidth
        pendingDashGap = dashGap
        pendingLineColor = lineColor
        pendingOrientation = orientation
        hasPendingChanges = false
    }

    /**
     * 应用画笔设置（原有）
     */
    private fun applyPaintSettings() {
        paint.strokeWidth = lineWidth
        paint.color = lineColor
        val effectiveDashWidth = if (dashWidth <= 0) 1f else dashWidth
        val effectiveDashGap = if (dashGap < 0) 0f else dashGap
        paint.pathEffect = DashPathEffect(floatArrayOf(effectiveDashWidth, effectiveDashGap), 0f)
    }

    /**
     * 统一应用临时参数并更新View（新增核心方法）
     */
    private fun applyPendingChanges() {
        if (!hasPendingChanges) return

        // 标记是否需要重新布局（尺寸/方向变化）
        var needLayout = false
        // 标记是否需要重绘（颜色/虚线参数变化）
        var needInvalidate = false

        // 对比临时参数与实际参数，只处理变化的部分
        if (pendingLineWidth != lineWidth) {
            lineWidth = pendingLineWidth
            needLayout = true
            needInvalidate = true
        }
        if (pendingDashWidth != dashWidth || pendingDashGap != dashGap) {
            dashWidth = pendingDashWidth
            dashGap = pendingDashGap
            needInvalidate = true
        }
        if (pendingLineColor != lineColor) {
            lineColor = pendingLineColor
            needInvalidate = true
        }
        if (pendingOrientation != orientation) {
            orientation = pendingOrientation
            pathDirty = true
            needLayout = true
            needInvalidate = true
        }

        // 应用画笔设置（如果有需要）
        if (needInvalidate) {
            applyPaintSettings()
        }

        // 统一执行布局/重绘
        if (needLayout) {
            requestLayout()
        }
        if (needInvalidate) {
            invalidate()
        }

        // 重置临时参数
        resetPendingParams()
    }

    // ==================== View 生命周期方法 ====================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val (desiredWidth, desiredHeight) = if (orientation == Orientation.HORIZONTAL) {
            val w = if (widthMode == MeasureSpec.AT_MOST) widthSize else lineWidth.toInt()
            val h = resolveSize(lineWidth.toInt(), heightMeasureSpec)
            w to h
        } else {
            val w = resolveSize(lineWidth.toInt(), widthMeasureSpec)
            val h = if (heightMode == MeasureSpec.AT_MOST) heightSize else lineWidth.toInt()
            w to h
        }

        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pathDirty = true
    }

    override fun onDraw(canvas: Canvas) {
        if (pathDirty) {
            rebuildPath()
            pathDirty = false
        }
        canvas.drawPath(path, paint)
    }

    private fun rebuildPath() {
        path.reset()

        val centerY = height / 2f
        val centerX = width / 2f

        if (orientation == Orientation.HORIZONTAL) {
            val startX = paddingLeft.toFloat()
            val endX = (width - paddingRight).toFloat()
            if (startX < endX) {
                path.moveTo(startX, centerY)
                path.lineTo(endX, centerY)
            }
        } else {
            val startY = paddingTop.toFloat()
            val endY = (height - paddingBottom).toFloat()
            if (startY < endY) {
                path.moveTo(centerX, startY)
                path.lineTo(centerX, endY)
            }
        }
    }

    // ==================== 工具方法 ====================
    private fun dp2px(dp: Float): Float {
        return dp * displayMetrics.density + 0.5f
    }

    // ==================== 链式调用方法（新增核心） ====================
    /**
     * 链式设置虚线粗细（dp）
     */
    fun setLineWidthDp(widthDp: Float): DashedLineView {
        pendingLineWidth = dp2px(widthDp).coerceAtLeast(0.5f)
        hasPendingChanges = true
        return this // 返回this实现链式调用
    }

    /**
     * 链式设置虚线段参数（dp）
     */
    fun setDashParamsDp(dashWidthDp: Float, dashGapDp: Float): DashedLineView {
        pendingDashWidth = dp2px(dashWidthDp).coerceAtLeast(1f)
        pendingDashGap = dp2px(dashGapDp).coerceAtLeast(0f)
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置虚线颜色
     */
    fun setLineColor(color: Int): DashedLineView {
        pendingLineColor = color
        hasPendingChanges = true
        return this
    }

    /**
     * 链式设置虚线方向（int常量）
     */
    fun setOrientation(orientation: Int): DashedLineView {
        return setOrientation(Orientation.fromValue(orientation))
    }

    /**
     * 链式设置虚线方向（枚举）
     */
    fun setOrientation(orientation: Orientation): DashedLineView {
        pendingOrientation = orientation
        hasPendingChanges = true
        return this
    }

    /**
     * 统一应用所有链式设置的参数（核心：只执行一次布局/重绘）
     */
    fun apply(): DashedLineView {
        applyPendingChanges()
        return this
    }

    // ==================== 原有单独设置方法（保留，兼容旧代码） ====================
    /**
     * 原有单独设置方法（立即生效）
     */
    fun setLineWidth(widthDp: Float) {
        // 复用链式方法，然后立即apply
        setLineWidthDp(widthDp).apply()
    }

    /**
     * 原有单独设置方法（立即生效）
     */
    fun setDashParams(dashWidthDp: Float, dashGapDp: Float) {
        setDashParamsDp(dashWidthDp, dashGapDp).apply()
    }
}