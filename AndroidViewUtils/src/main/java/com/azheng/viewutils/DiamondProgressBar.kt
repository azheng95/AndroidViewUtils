package com.azheng.viewutils

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes

class DiamondProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ============ 基础属性 ============
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, maxProgress)
            invalidate()
        }

    var maxProgress: Float = 100f
        set(value) {
            field = value
            invalidate()
        }

    var barHeight: Float = dpToPx(8f)
        set(value) {
            field = value
            trackPaint.strokeWidth = value
            progressPaint.strokeWidth = value
            invalidate()
        }

    // ============ 弧形相关属性 ============
    var isArcMode: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var arcHeight: Float = dpToPx(30f)
        set(value) {
            field = value
            invalidate()
        }

    var arcControlPosition: Float = 0.5f
        set(value) {
            field = value.coerceIn(0.1f, 0.9f)
            invalidate()
        }

    enum class ArcType {
        QUADRATIC,  // 二次贝塞尔（单控制点，简单弧形）
        CUBIC,      // 三次贝塞尔（双控制点，更平滑）
        ARC         // 真正的圆弧
    }

    var arcType: ArcType = ArcType.QUADRATIC
        set(value) {
            field = value
            invalidate()
        }

    var arcControlHeight1: Float = 1f
        set(value) {
            field = value
            invalidate()
        }

    var arcControlHeight2: Float = 1f
        set(value) {
            field = value
            invalidate()
        }

    // ============ 线条端点样式（弧形模式） ============
    var strokeCap: Paint.Cap = Paint.Cap.ROUND
        set(value) {
            field = value
            trackPaint.strokeCap = value
            progressPaint.strokeCap = value
            invalidate()
        }

    // ============ 圆角属性 ============
    var barCornerRadius: Float = dpToPx(4f)
        set(value) {
            field = value
            topLeftRadius = value
            topRightRadius = value
            bottomLeftRadius = value
            bottomRightRadius = value
            invalidate()
        }

    var topLeftRadius: Float = dpToPx(4f)
    var topRightRadius: Float = dpToPx(4f)
    var bottomLeftRadius: Float = dpToPx(4f)
    var bottomRightRadius: Float = dpToPx(4f)

    var progressStartRadius: Float = dpToPx(4f)
    var progressEndRadius: Float = dpToPx(4f)

    var isCapsuleShape: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    // ============ 颜色属性 ============
    var trackColor: Int = Color.parseColor("#E0E0E0")
        set(value) {
            field = value
            trackPaint.color = value
            trackFillPaint.color = value
            invalidate()
        }

    var gradientColors: IntArray = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#FFE66D"),
        Color.parseColor("#4ECDC4")
    )
        set(value) {
            field = value
            updateGradient()
            invalidate()
        }

    var gradientPositions: FloatArray? = null
        set(value) {
            field = value
            updateGradient()
            invalidate()
        }

    // ============ 钻石图标属性 ============
    private var diamondDrawable: Drawable? = null

    var diamondSize: Float = dpToPx(32f)
        set(value) {
            field = value
            invalidate()
        }

    var diamondOffsetY: Float = dpToPx(0f)
        set(value) {
            field = value
            invalidate()
        }

    var diamondRotateWithPath: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    // ============ 临时参数（链式调用专用） ============
    private var hasPendingChanges = false

    // 基础属性临时参数
    private var pendingProgress: Float = 0f
    private var pendingMaxProgress: Float = 100f
    private var pendingBarHeight: Float = dpToPx(8f)

    // 弧形属性临时参数
    private var pendingIsArcMode: Boolean = false
    private var pendingArcHeight: Float = dpToPx(30f)
    private var pendingArcControlPosition: Float = 0.5f
    private var pendingArcType: ArcType = ArcType.QUADRATIC
    private var pendingArcControlHeight1: Float = 1f
    private var pendingArcControlHeight2: Float = 1f

    // 样式属性临时参数
    private var pendingStrokeCap: Paint.Cap = Paint.Cap.ROUND
    private var pendingBarCornerRadius: Float = dpToPx(4f)
    private var pendingTopLeftRadius: Float = dpToPx(4f)
    private var pendingTopRightRadius: Float = dpToPx(4f)
    private var pendingBottomLeftRadius: Float = dpToPx(4f)
    private var pendingBottomRightRadius: Float = dpToPx(4f)
    private var pendingProgressStartRadius: Float = dpToPx(4f)
    private var pendingProgressEndRadius: Float = dpToPx(4f)
    private var pendingIsCapsuleShape: Boolean = false

    // 颜色属性临时参数
    private var pendingTrackColor: Int = Color.parseColor("#E0E0E0")
    private var pendingGradientColors: IntArray = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#FFE66D"),
        Color.parseColor("#4ECDC4")
    )
    private var pendingGradientPositions: FloatArray? = null

    // 钻石图标属性临时参数
    private var pendingDiamondDrawable: Drawable? = null
    private var pendingDiamondSize: Float = dpToPx(32f)
    private var pendingDiamondOffsetY: Float = 0f
    private var pendingDiamondRotateWithPath: Boolean = false

    // ============ 画笔 ============
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = trackColor
        strokeWidth = barHeight
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = barHeight
        strokeCap = Paint.Cap.ROUND
    }

    private val trackFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = trackColor
    }

    private val progressFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ============ 路径 ============
    private val trackPath = Path()
    private val progressPath = Path()
    private val trackRect = RectF()
    private val progressRect = RectF()
    private val pathMeasure = PathMeasure()
    private val pathPosition = FloatArray(2)
    private val pathTangent = FloatArray(2)
    private var gradientShader: LinearGradient? = null
    private var trackCornerRadii = FloatArray(8)
    private var progressCornerRadii = FloatArray(8)

    // ============ 内存泄漏防护 ============
    private var mProgressAnimator: ValueAnimator? = null

    init {
        updateCornerRadii()

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.DiamondProgressBar) {
                progress = getFloat(R.styleable.DiamondProgressBar_dpb_progress, 0f)
                maxProgress = getFloat(R.styleable.DiamondProgressBar_dpb_maxProgress, 100f)
                barHeight = getDimension(R.styleable.DiamondProgressBar_dpb_barHeight, dpToPx(8f))
                trackColor = getColor(
                    R.styleable.DiamondProgressBar_dpb_trackColor,
                    Color.parseColor("#E0E0E0")
                )
                diamondSize =
                    getDimension(R.styleable.DiamondProgressBar_dpb_diamondSize, dpToPx(32f))
                diamondOffsetY = getDimension(R.styleable.DiamondProgressBar_dpb_diamondOffsetY, 0f)

                isArcMode = getBoolean(R.styleable.DiamondProgressBar_dpb_arcMode, false)
                arcHeight = getDimension(R.styleable.DiamondProgressBar_dpb_arcHeight, dpToPx(30f))
                arcControlPosition =
                    getFloat(R.styleable.DiamondProgressBar_dpb_arcControlPosition, 0.5f)
                val arcTypeInt = getInt(R.styleable.DiamondProgressBar_dpb_arcType, 0)
                arcType = ArcType.values()[arcTypeInt]
                arcControlHeight1 =
                    getFloat(R.styleable.DiamondProgressBar_dpb_arcControlHeight1, 1f)
                arcControlHeight2 =
                    getFloat(R.styleable.DiamondProgressBar_dpb_arcControlHeight2, 1f)
                diamondRotateWithPath =
                    getBoolean(R.styleable.DiamondProgressBar_dpb_diamondRotate, false)

                val strokeCapInt = getInt(R.styleable.DiamondProgressBar_dpb_strokeCap, 1)
                strokeCap = when (strokeCapInt) {
                    0 -> Paint.Cap.BUTT
                    1 -> Paint.Cap.ROUND
                    2 -> Paint.Cap.SQUARE
                    else -> Paint.Cap.ROUND
                }

                val defaultRadius = dpToPx(4f)
                barCornerRadius =
                    getDimension(R.styleable.DiamondProgressBar_dpb_cornerRadius, defaultRadius)
                topLeftRadius =
                    getDimension(R.styleable.DiamondProgressBar_dpb_topLeftRadius, barCornerRadius)
                topRightRadius =
                    getDimension(R.styleable.DiamondProgressBar_dpb_topRightRadius, barCornerRadius)
                bottomLeftRadius = getDimension(
                    R.styleable.DiamondProgressBar_dpb_bottomLeftRadius,
                    barCornerRadius
                )
                bottomRightRadius = getDimension(
                    R.styleable.DiamondProgressBar_dpb_bottomRightRadius,
                    barCornerRadius
                )
                progressStartRadius = getDimension(
                    R.styleable.DiamondProgressBar_dpb_progressStartRadius,
                    barCornerRadius
                )
                progressEndRadius = getDimension(
                    R.styleable.DiamondProgressBar_dpb_progressEndRadius,
                    barCornerRadius
                )
                isCapsuleShape = getBoolean(R.styleable.DiamondProgressBar_dpb_capsuleShape, false)

                val diamondResId =
                    getResourceId(R.styleable.DiamondProgressBar_dpb_diamondDrawable, 0)
                if (diamondResId != 0) {
                    diamondDrawable =
                        ContextCompat.getDrawable(context.applicationContext, diamondResId)
                }

                val startColor = getColor(
                    R.styleable.DiamondProgressBar_dpb_gradientStartColor,
                    Color.parseColor("#FF6B6B")
                )
                val centerColor = getColor(
                    R.styleable.DiamondProgressBar_dpb_gradientCenterColor,
                    Color.parseColor("#FFE66D")
                )
                val endColor = getColor(
                    R.styleable.DiamondProgressBar_dpb_gradientEndColor,
                    Color.parseColor("#4ECDC4")
                )
                gradientColors = intArrayOf(startColor, centerColor, endColor)

            }
        }

        trackPaint.strokeWidth = barHeight
        progressPaint.strokeWidth = barHeight
        trackPaint.strokeCap = strokeCap
        progressPaint.strokeCap = strokeCap

        // 初始化临时参数
        initPendingParams()
    }

    // ============ 临时参数管理 ============

    /**
     * 初始化临时参数（与当前实际值同步）
     */
    private fun initPendingParams() {
        pendingProgress = progress
        pendingMaxProgress = maxProgress
        pendingBarHeight = barHeight
        pendingIsArcMode = isArcMode
        pendingArcHeight = arcHeight
        pendingArcControlPosition = arcControlPosition
        pendingArcType = arcType
        pendingArcControlHeight1 = arcControlHeight1
        pendingArcControlHeight2 = arcControlHeight2
        pendingStrokeCap = strokeCap
        pendingBarCornerRadius = barCornerRadius
        pendingTopLeftRadius = topLeftRadius
        pendingTopRightRadius = topRightRadius
        pendingBottomLeftRadius = bottomLeftRadius
        pendingBottomRightRadius = bottomRightRadius
        pendingProgressStartRadius = progressStartRadius
        pendingProgressEndRadius = progressEndRadius
        pendingIsCapsuleShape = isCapsuleShape
        pendingTrackColor = trackColor
        pendingGradientColors = gradientColors.copyOf()
        pendingGradientPositions = gradientPositions?.copyOf()
        pendingDiamondDrawable = diamondDrawable
        pendingDiamondSize = diamondSize
        pendingDiamondOffsetY = diamondOffsetY
        pendingDiamondRotateWithPath = diamondRotateWithPath
    }

    /**
     * 重置临时参数
     */
    private fun resetPendingParams() {
        hasPendingChanges = false
        initPendingParams()
    }

    /**
     * 统一应用临时参数并更新View（核心方法）
     */
    private fun applyPendingChanges() {
        if (!hasPendingChanges) return

        var needLayout = false
        var needInvalidate = false
        var needUpdateGradient = false
        var needUpdateCornerRadii = false
        var needUpdatePaintSettings = false

        // 基础属性
        if (pendingProgress != progress) {
            progress = pendingProgress.coerceIn(0f, pendingMaxProgress)
            needInvalidate = true
        }
        if (pendingMaxProgress != maxProgress) {
            maxProgress = pendingMaxProgress
            needInvalidate = true
        }
        if (pendingBarHeight != barHeight) {
            barHeight = pendingBarHeight
            needUpdatePaintSettings = true
            needInvalidate = true
        }

        // 弧形属性
        if (pendingIsArcMode != isArcMode) {
            isArcMode = pendingIsArcMode
            needLayout = true
            needInvalidate = true
        }
        if (pendingArcHeight != arcHeight) {
            arcHeight = pendingArcHeight
            needLayout = true
            needInvalidate = true
        }
        if (pendingArcControlPosition != arcControlPosition) {
            arcControlPosition = pendingArcControlPosition.coerceIn(0.1f, 0.9f)
            needInvalidate = true
        }
        if (pendingArcType != arcType) {
            arcType = pendingArcType
            needInvalidate = true
        }
        if (pendingArcControlHeight1 != arcControlHeight1) {
            arcControlHeight1 = pendingArcControlHeight1
            needInvalidate = true
        }
        if (pendingArcControlHeight2 != arcControlHeight2) {
            arcControlHeight2 = pendingArcControlHeight2
            needInvalidate = true
        }

        // 样式属性
        if (pendingStrokeCap != strokeCap) {
            strokeCap = pendingStrokeCap
            needUpdatePaintSettings = true
            needInvalidate = true
        }
        if (pendingBarCornerRadius != barCornerRadius) {
            barCornerRadius = pendingBarCornerRadius
            needUpdateCornerRadii = true
            needInvalidate = true
        }
        if (pendingTopLeftRadius != topLeftRadius ||
            pendingTopRightRadius != topRightRadius ||
            pendingBottomLeftRadius != bottomLeftRadius ||
            pendingBottomRightRadius != bottomRightRadius
        ) {
            topLeftRadius = pendingTopLeftRadius
            topRightRadius = pendingTopRightRadius
            bottomLeftRadius = pendingBottomLeftRadius
            bottomRightRadius = pendingBottomRightRadius
            needUpdateCornerRadii = true
            needInvalidate = true
        }
        if (pendingProgressStartRadius != progressStartRadius ||
            pendingProgressEndRadius != progressEndRadius
        ) {
            progressStartRadius = pendingProgressStartRadius
            progressEndRadius = pendingProgressEndRadius
            needInvalidate = true
        }
        if (pendingIsCapsuleShape != isCapsuleShape) {
            isCapsuleShape = pendingIsCapsuleShape
            needInvalidate = true
        }

        // 颜色属性
        if (pendingTrackColor != trackColor) {
            trackColor = pendingTrackColor
            trackPaint.color = trackColor
            trackFillPaint.color = trackColor
            needInvalidate = true
        }
        if (!pendingGradientColors.contentEquals(gradientColors) ||
            !pendingGradientPositions.contentEquals(gradientPositions)
        ) {
            gradientColors = pendingGradientColors.copyOf()
            gradientPositions = pendingGradientPositions?.copyOf()
            needUpdateGradient = true
            needInvalidate = true
        }

        // 钻石图标属性
        if (pendingDiamondDrawable != diamondDrawable) {
            diamondDrawable = pendingDiamondDrawable
            needInvalidate = true
        }
        if (pendingDiamondSize != diamondSize) {
            diamondSize = pendingDiamondSize
            needLayout = true
            needUpdateGradient = true
            needInvalidate = true
        }
        if (pendingDiamondOffsetY != diamondOffsetY) {
            diamondOffsetY = pendingDiamondOffsetY
            needInvalidate = true
        }
        if (pendingDiamondRotateWithPath != diamondRotateWithPath) {
            diamondRotateWithPath = pendingDiamondRotateWithPath
            needInvalidate = true
        }

        // 应用画笔设置
        if (needUpdatePaintSettings) {
            trackPaint.strokeWidth = barHeight
            progressPaint.strokeWidth = barHeight
            trackPaint.strokeCap = strokeCap
            progressPaint.strokeCap = strokeCap
        }

        // 更新圆角
        if (needUpdateCornerRadii) {
            updateCornerRadiiInternal()
        }

        // 更新渐变
        if (needUpdateGradient) {
            updateGradient()
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

    /**
     * 统一应用所有链式设置的参数（只执行一次布局/重绘）
     */
    fun apply(): DiamondProgressBar {
        applyPendingChanges()
        return this
    }

    // ============ 链式调用方法（只设置临时参数，不触发invalidate） ============

    /** 基础属性链式设置 */
    fun setProgressPending(progress: Float): DiamondProgressBar {
        pendingProgress = progress.coerceIn(0f, pendingMaxProgress)
        hasPendingChanges = true
        return this
    }

    fun setMaxProgressPending(maxProgress: Float): DiamondProgressBar {
        pendingMaxProgress = maxProgress
        hasPendingChanges = true
        return this
    }

    fun setBarHeightPending(barHeight: Float): DiamondProgressBar {
        pendingBarHeight = barHeight
        hasPendingChanges = true
        return this
    }

    /** 弧形属性链式设置 */
    fun setArcModePending(isArcMode: Boolean): DiamondProgressBar {
        pendingIsArcMode = isArcMode
        hasPendingChanges = true
        return this
    }

    fun setArcHeightPending(arcHeight: Float): DiamondProgressBar {
        pendingArcHeight = arcHeight
        hasPendingChanges = true
        return this
    }

    fun setArcControlPositionPending(arcControlPosition: Float): DiamondProgressBar {
        pendingArcControlPosition = arcControlPosition.coerceIn(0.1f, 0.9f)
        hasPendingChanges = true
        return this
    }

    fun setArcTypePending(arcType: ArcType): DiamondProgressBar {
        pendingArcType = arcType
        hasPendingChanges = true
        return this
    }

    fun setArcControlHeight1Pending(arcControlHeight1: Float): DiamondProgressBar {
        pendingArcControlHeight1 = arcControlHeight1
        hasPendingChanges = true
        return this
    }

    fun setArcControlHeight2Pending(arcControlHeight2: Float): DiamondProgressBar {
        pendingArcControlHeight2 = arcControlHeight2
        hasPendingChanges = true
        return this
    }

    /** 样式属性链式设置 */
    fun setStrokeCapPending(strokeCap: Paint.Cap): DiamondProgressBar {
        pendingStrokeCap = strokeCap
        hasPendingChanges = true
        return this
    }

    fun setBarCornerRadiusPending(barCornerRadius: Float): DiamondProgressBar {
        pendingBarCornerRadius = barCornerRadius
        pendingTopLeftRadius = barCornerRadius
        pendingTopRightRadius = barCornerRadius
        pendingBottomLeftRadius = barCornerRadius
        pendingBottomRightRadius = barCornerRadius
        hasPendingChanges = true
        return this
    }

    fun setTopLeftRadiusPending(topLeftRadius: Float): DiamondProgressBar {
        pendingTopLeftRadius = topLeftRadius
        hasPendingChanges = true
        return this
    }

    fun setTopRightRadiusPending(topRightRadius: Float): DiamondProgressBar {
        pendingTopRightRadius = topRightRadius
        hasPendingChanges = true
        return this
    }

    fun setBottomLeftRadiusPending(bottomLeftRadius: Float): DiamondProgressBar {
        pendingBottomLeftRadius = bottomLeftRadius
        hasPendingChanges = true
        return this
    }

    fun setBottomRightRadiusPending(bottomRightRadius: Float): DiamondProgressBar {
        pendingBottomRightRadius = bottomRightRadius
        hasPendingChanges = true
        return this
    }

    fun setProgressStartRadiusPending(progressStartRadius: Float): DiamondProgressBar {
        pendingProgressStartRadius = progressStartRadius
        hasPendingChanges = true
        return this
    }

    fun setProgressEndRadiusPending(progressEndRadius: Float): DiamondProgressBar {
        pendingProgressEndRadius = progressEndRadius
        hasPendingChanges = true
        return this
    }

    fun setCapsuleShapePending(isCapsuleShape: Boolean): DiamondProgressBar {
        pendingIsCapsuleShape = isCapsuleShape
        hasPendingChanges = true
        return this
    }

    /** 颜色属性链式设置 */
    fun setTrackColorPending(trackColor: Int): DiamondProgressBar {
        pendingTrackColor = trackColor
        hasPendingChanges = true
        return this
    }

    fun setGradientColorsPending(colors: IntArray, positions: FloatArray? = null): DiamondProgressBar {
        pendingGradientColors = colors.copyOf()
        pendingGradientPositions = positions?.copyOf()
        hasPendingChanges = true
        return this
    }

    /** 钻石图标属性链式设置 */
    fun setDiamondSizePending(diamondSize: Float): DiamondProgressBar {
        pendingDiamondSize = diamondSize
        hasPendingChanges = true
        return this
    }

    fun setDiamondOffsetYPending(diamondOffsetY: Float): DiamondProgressBar {
        pendingDiamondOffsetY = diamondOffsetY
        hasPendingChanges = true
        return this
    }

    fun setDiamondRotateWithPathPending(diamondRotateWithPath: Boolean): DiamondProgressBar {
        pendingDiamondRotateWithPath = diamondRotateWithPath
        hasPendingChanges = true
        return this
    }

    fun setDiamondDrawablePending(@DrawableRes resId: Int): DiamondProgressBar {
        pendingDiamondDrawable = ContextCompat.getDrawable(context.applicationContext, resId)
        hasPendingChanges = true
        return this
    }

    fun setDiamondDrawablePending(drawable: Drawable?): DiamondProgressBar {
        pendingDiamondDrawable = drawable
        hasPendingChanges = true
        return this
    }

    /** 组合链式设置方法 */
    fun setCornerRadiusPending(radius: Float): DiamondProgressBar {
        pendingBarCornerRadius = radius
        pendingTopLeftRadius = radius
        pendingTopRightRadius = radius
        pendingBottomLeftRadius = radius
        pendingBottomRightRadius = radius
        pendingProgressStartRadius = radius
        pendingProgressEndRadius = radius
        hasPendingChanges = true
        return this
    }

    fun setCornerRadiiPending(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    ): DiamondProgressBar {
        pendingTopLeftRadius = topLeft
        pendingTopRightRadius = topRight
        pendingBottomRightRadius = bottomRight
        pendingBottomLeftRadius = bottomLeft
        hasPendingChanges = true
        return this
    }

    fun setProgressCornerRadiusPending(startRadius: Float, endRadius: Float): DiamondProgressBar {
        pendingProgressStartRadius = startRadius
        pendingProgressEndRadius = endRadius
        hasPendingChanges = true
        return this
    }

    fun setArcParamsPending(
        height: Float,
        controlPosition: Float = 0.5f,
        type: ArcType = ArcType.QUADRATIC
    ): DiamondProgressBar {
        pendingArcHeight = height
        pendingArcControlPosition = controlPosition.coerceIn(0.1f, 0.9f)
        pendingArcType = type
        pendingIsArcMode = true
        hasPendingChanges = true
        return this
    }

    fun setCubicControlHeightsPending(height1: Float, height2: Float): DiamondProgressBar {
        pendingArcControlHeight1 = height1
        pendingArcControlHeight2 = height2
        hasPendingChanges = true
        return this
    }

    // ============ 保留原有的即时生效方法（向后兼容） ============

    fun setProgress(progressValue: Float): DiamondProgressBar {
        this.progress = progressValue
        return this
    }

    fun setMaxProgress(maxProgressValue: Float): DiamondProgressBar {
        this.maxProgress = maxProgressValue
        return this
    }

    fun setBarHeight(barHeightValue: Float): DiamondProgressBar {
        this.barHeight = barHeightValue
        return this
    }

    fun setArcMode(isArcModeValue: Boolean): DiamondProgressBar {
        this.isArcMode = isArcModeValue
        return this
    }

    fun setArcHeight(arcHeightValue: Float): DiamondProgressBar {
        this.arcHeight = arcHeightValue
        return this
    }

    fun setArcControlPosition(arcControlPositionValue: Float): DiamondProgressBar {
        this.arcControlPosition = arcControlPositionValue
        return this
    }

    fun setArcType(arcTypeValue: ArcType): DiamondProgressBar {
        this.arcType = arcTypeValue
        return this
    }

    fun setArcControlHeight1(arcControlHeight1Value: Float): DiamondProgressBar {
        this.arcControlHeight1 = arcControlHeight1Value
        return this
    }

    fun setArcControlHeight2(arcControlHeight2Value: Float): DiamondProgressBar {
        this.arcControlHeight2 = arcControlHeight2Value
        return this
    }

    fun setStrokeCap(strokeCapValue: Paint.Cap): DiamondProgressBar {
        this.strokeCap = strokeCapValue
        return this
    }

    fun setBarCornerRadius(barCornerRadiusValue: Float): DiamondProgressBar {
        this.barCornerRadius = barCornerRadiusValue
        return this
    }

    fun setTopLeftRadius(topLeftRadiusValue: Float): DiamondProgressBar {
        this.topLeftRadius = topLeftRadiusValue
        updateCornerRadiiInternal()
        invalidate()
        return this
    }

    fun setTopRightRadius(topRightRadiusValue: Float): DiamondProgressBar {
        this.topRightRadius = topRightRadiusValue
        updateCornerRadiiInternal()
        invalidate()
        return this
    }

    fun setBottomLeftRadius(bottomLeftRadiusValue: Float): DiamondProgressBar {
        this.bottomLeftRadius = bottomLeftRadiusValue
        updateCornerRadiiInternal()
        invalidate()
        return this
    }

    fun setBottomRightRadius(bottomRightRadiusValue: Float): DiamondProgressBar {
        this.bottomRightRadius = bottomRightRadiusValue
        updateCornerRadiiInternal()
        invalidate()
        return this
    }

    fun setProgressStartRadius(progressStartRadiusValue: Float): DiamondProgressBar {
        this.progressStartRadius = progressStartRadiusValue
        invalidate()
        return this
    }

    fun setProgressEndRadius(progressEndRadiusValue: Float): DiamondProgressBar {
        this.progressEndRadius = progressEndRadiusValue
        invalidate()
        return this
    }

    fun setCapsuleShape(isCapsuleShapeValue: Boolean): DiamondProgressBar {
        this.isCapsuleShape = isCapsuleShapeValue
        return this
    }

    fun setTrackColor(trackColorValue: Int): DiamondProgressBar {
        this.trackColor = trackColorValue
        return this
    }

    fun setGradientColors(colors: IntArray, positions: FloatArray? = null): DiamondProgressBar {
        gradientColors = colors
        gradientPositions = positions
        return this
    }

    fun setDiamondSize(diamondSizeValue: Float): DiamondProgressBar {
        this.diamondSize = diamondSizeValue
        return this
    }

    fun setDiamondOffsetY(diamondOffsetYValue: Float): DiamondProgressBar {
        this.diamondOffsetY = diamondOffsetYValue
        return this
    }

    fun setDiamondRotateWithPath(diamondRotateWithPathValue: Boolean): DiamondProgressBar {
        this.diamondRotateWithPath = diamondRotateWithPathValue
        return this
    }

    // ============ 原有方法改造 ============

    private fun updateCornerRadii() {
        updateCornerRadiiInternal()
    }

    private fun updateCornerRadiiInternal() {
        trackCornerRadii = floatArrayOf(
            topLeftRadius, topLeftRadius,
            topRightRadius, topRightRadius,
            bottomRightRadius, bottomRightRadius,
            bottomLeftRadius, bottomLeftRadius
        )
    }

    fun setCornerRadius(radius: Float): DiamondProgressBar {
        barCornerRadius = radius
        progressStartRadius = radius
        progressEndRadius = radius
        return this
    }

    fun setCornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): DiamondProgressBar {
        topLeftRadius = topLeft
        topRightRadius = topRight
        bottomRightRadius = bottomRight
        bottomLeftRadius = bottomLeft
        updateCornerRadiiInternal()
        invalidate()
        return this
    }

    fun setProgressCornerRadius(startRadius: Float, endRadius: Float): DiamondProgressBar {
        progressStartRadius = startRadius
        progressEndRadius = endRadius
        invalidate()
        return this
    }

    fun setArcParams(
        height: Float,
        controlPosition: Float = 0.5f,
        type: ArcType = ArcType.QUADRATIC
    ): DiamondProgressBar {
        arcHeight = height
        arcControlPosition = controlPosition
        arcType = type
        isArcMode = true
        return this
    }

    fun setCubicControlHeights(height1: Float, height2: Float): DiamondProgressBar {
        arcControlHeight1 = height1
        arcControlHeight2 = height2
        return this
    }

    fun setDiamondDrawable(@DrawableRes resId: Int): DiamondProgressBar {
        diamondDrawable = ContextCompat.getDrawable(context.applicationContext, resId)
        invalidate()
        return this
    }

    fun setDiamondDrawable(drawable: Drawable?): DiamondProgressBar {
        diamondDrawable = drawable
        invalidate()
        return this
    }

    // ============ 动画方法 ============
    fun setProgressWithAnimation(targetProgress: Float, duration: Long = 1000L): DiamondProgressBar {
        mProgressAnimator?.cancel()

        mProgressAnimator = ValueAnimator.ofFloat(progress, targetProgress).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                progress = animator.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    mProgressAnimator = null
                }
                override fun onAnimationCancel(animation: Animator) {
                    mProgressAnimator = null
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
        return this
    }

    // ============ 生命周期方法 ============
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mProgressAnimator?.apply {
            removeAllUpdateListeners()
            removeAllListeners()
            cancel()
        }
        mProgressAnimator = null

        diamondDrawable?.apply {
            callback = null
        }
        diamondDrawable = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    // ============ 内部工具方法 ============
    private fun updateGradient() {
        if (width > 0) {
            val effectiveWidth = width - paddingLeft - paddingRight - diamondSize
            gradientShader = LinearGradient(
                paddingLeft + diamondSize / 2,
                0f,
                paddingLeft + diamondSize / 2 + effectiveWidth,
                0f,
                gradientColors,
                gradientPositions,
                Shader.TileMode.CLAMP
            )
            progressPaint.shader = gradientShader
            progressFillPaint.shader = gradientShader
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGradient()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val arcExtraHeight = if (isArcMode) Math.abs(arcHeight) else 0f
        val desiredHeight = (diamondSize + arcExtraHeight + paddingTop + paddingBottom).toInt()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> dpToPx(200f).toInt()
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isArcMode) {
            drawArcProgress(canvas)
        } else {
            drawLinearProgress(canvas)
        }
    }

    private fun drawArcProgress(canvas: Canvas) {
        val halfDiamond = diamondSize / 2
        val effectiveWidth = width - paddingLeft - paddingRight - diamondSize
        val baseY = if (arcHeight > 0) {
            height - paddingBottom - halfDiamond
        } else {
            paddingTop + halfDiamond + Math.abs(arcHeight)
        }

        val startX = paddingLeft + halfDiamond
        val endX = width - paddingRight - halfDiamond
        val startY = baseY
        val endY = baseY
        val controlX = startX + effectiveWidth * arcControlPosition
        val controlY = baseY - arcHeight

        trackPath.reset()
        when (arcType) {
            ArcType.QUADRATIC -> {
                trackPath.moveTo(startX, startY)
                trackPath.quadTo(controlX, controlY, endX, endY)
            }

            ArcType.CUBIC -> {
                val control1X = startX + effectiveWidth * 0.25f
                val control1Y = baseY - arcHeight * arcControlHeight1
                val control2X = startX + effectiveWidth * 0.75f
                val control2Y = baseY - arcHeight * arcControlHeight2
                trackPath.moveTo(startX, startY)
                trackPath.cubicTo(control1X, control1Y, control2X, control2Y, endX, endY)
            }

            ArcType.ARC -> {
                val centerX = (startX + endX) / 2
                val radius = Math.sqrt(
                    Math.pow((effectiveWidth / 2).toDouble(), 2.0) +
                            Math.pow(arcHeight.toDouble(), 2.0)
                ).toFloat() / 2 * effectiveWidth / Math.abs(arcHeight)

                val arcRect = RectF(
                    centerX - radius,
                    baseY - radius - arcHeight / 2,
                    centerX + radius,
                    baseY + radius - arcHeight / 2
                )

                val sweepAngle = Math.toDegrees(
                    2 * Math.atan2(arcHeight.toDouble(), (effectiveWidth / 2).toDouble())
                ).toFloat()
                val startAngle = if (arcHeight > 0) 180f - sweepAngle / 2 else -sweepAngle / 2

                trackPath.addArc(arcRect, startAngle, sweepAngle)
            }
        }

        canvas.drawPath(trackPath, trackPaint)

        val progressRatio = progress / maxProgress
        if (progressRatio > 0) {
            pathMeasure.setPath(trackPath, false)
            val pathLength = pathMeasure.length
            val progressLength = pathLength * progressRatio

            progressPath.reset()
            pathMeasure.getSegment(0f, progressLength, progressPath, true)
            canvas.drawPath(progressPath, progressPaint)
        }

        drawDiamondOnPath(canvas, progressRatio)
    }

    private fun drawDiamondOnPath(canvas: Canvas, progressRatio: Float) {
        diamondDrawable?.let { drawable ->
            pathMeasure.setPath(trackPath, false)
            val pathLength = pathMeasure.length
            val currentLength = pathLength * progressRatio

            pathMeasure.getPosTan(currentLength, pathPosition, pathTangent)

            val diamondCenterX = pathPosition[0]
            val diamondCenterY = pathPosition[1] + diamondOffsetY
            val halfDiamond = diamondSize / 2

            canvas.save()

            if (diamondRotateWithPath) {
                val angle = Math.toDegrees(
                    Math.atan2(pathTangent[1].toDouble(), pathTangent[0].toDouble())
                ).toFloat()
                canvas.rotate(angle, diamondCenterX, diamondCenterY)
            }

            drawable.setBounds(
                (diamondCenterX - halfDiamond).toInt(),
                (diamondCenterY - halfDiamond).toInt(),
                (diamondCenterX + halfDiamond).toInt(),
                (diamondCenterY + halfDiamond).toInt()
            )
            drawable.draw(canvas)

            canvas.restore()
        }
    }

    private fun drawLinearProgress(canvas: Canvas) {
        val halfDiamond = diamondSize / 2
        val effectiveWidth = width - paddingLeft - paddingRight - diamondSize
        val centerY = height / 2f + diamondOffsetY

        val actualTrackRadii = if (isCapsuleShape) {
            val capsuleRadius = barHeight / 2
            floatArrayOf(
                capsuleRadius, capsuleRadius,
                capsuleRadius, capsuleRadius,
                capsuleRadius, capsuleRadius,
                capsuleRadius, capsuleRadius
            )
        } else {
            trackCornerRadii
        }

        trackRect.set(
            paddingLeft + halfDiamond,
            centerY - barHeight / 2,
            width - paddingRight - halfDiamond,
            centerY + barHeight / 2
        )

        trackPath.reset()
        trackPath.addRoundRect(trackRect, actualTrackRadii, Path.Direction.CW)
        canvas.drawPath(trackPath, trackFillPaint)

        val progressRatio = progress / maxProgress
        val progressWidth = effectiveWidth * progressRatio
        if (progressWidth > 0) {
            progressRect.set(
                paddingLeft + halfDiamond,
                centerY - barHeight / 2,
                paddingLeft + halfDiamond + progressWidth,
                centerY + barHeight / 2
            )

            val actualStartRadius = if (isCapsuleShape) barHeight / 2 else progressStartRadius
            val actualEndRadius = if (isCapsuleShape) barHeight / 2 else progressEndRadius

            progressCornerRadii = floatArrayOf(
                actualStartRadius, actualStartRadius,
                actualEndRadius, actualEndRadius,
                actualEndRadius, actualEndRadius,
                actualStartRadius, actualStartRadius
            )

            progressPath.reset()
            progressPath.addRoundRect(progressRect, progressCornerRadii, Path.Direction.CW)
            canvas.drawPath(progressPath, progressFillPaint)
        }

        diamondDrawable?.let { drawable ->
            val diamondCenterX = paddingLeft + halfDiamond + progressWidth
            drawable.setBounds(
                (diamondCenterX - halfDiamond).toInt(),
                (centerY - halfDiamond).toInt(),
                (diamondCenterX + halfDiamond).toInt(),
                (centerY + halfDiamond).toInt()
            )
            drawable.draw(canvas)
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
