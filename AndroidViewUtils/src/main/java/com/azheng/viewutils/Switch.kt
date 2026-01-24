package com.azheng.viewutils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Switch

class Switch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.switchStyle
) : Switch(context, attrs, defStyleAttr) {

    // 默认颜色（匹配你UI图的浅灰+白色样式）
    private val DEFAULT_ON_TRACK_COLOR = Color.parseColor("#4CAF50")   // 开启时轨道色（绿色）
    private val DEFAULT_OFF_TRACK_COLOR = Color.parseColor("#E0E0E0")  // 关闭时轨道色（浅灰）
    private val DEFAULT_THUMB_COLOR = Color.parseColor("#FFFFFF")     // 滑块白色（匹配UI）
    private val DEFAULT_THUMB_SIZE = dp2px(24f)                       // 滑块默认大小24dp

    init {
        // 解析XML中设置的自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.Switch)
            
            // 读取自定义颜色属性
            val onTrackColor = typedArray.getColor(
                R.styleable.Switch_switchOnTrackColor,
                DEFAULT_ON_TRACK_COLOR
            )
            val offTrackColor = typedArray.getColor(
                R.styleable.Switch_switchOffTrackColor,
                DEFAULT_OFF_TRACK_COLOR
            )
            val thumbColor = typedArray.getColor(
                R.styleable.Switch_switchThumbColor,
                DEFAULT_THUMB_COLOR
            )
            val thumbSize = typedArray.getDimension(
                R.styleable.Switch_switchThumbSize,
                DEFAULT_THUMB_SIZE
            )

            // 回收TypedArray，避免内存泄漏
            typedArray.recycle()

            // 设置开关样式（修复核心）
            setupSwitchStyle(onTrackColor, offTrackColor, thumbColor, thumbSize)
        } ?: setupSwitchStyle(DEFAULT_ON_TRACK_COLOR, DEFAULT_OFF_TRACK_COLOR, DEFAULT_THUMB_COLOR, DEFAULT_THUMB_SIZE)
    }

    /**
     * 修复后：配置Switch的轨道（track）和滑块（thumb）样式
     * 核心修改：用StateListDrawable管理开关不同状态的轨道颜色
     */
    private fun setupSwitchStyle(onTrackColor: Int, offTrackColor: Int, thumbColor: Int, thumbSize: Float) {
        // 1. 创建滑块（Thumb）的Drawable（圆形，无修改）
        val thumbDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL  // 圆形滑块（匹配UI）
            setColor(thumbColor)           // 滑块颜色
            setSize(thumbSize.toInt(), thumbSize.toInt()) // 滑块大小
            // 添加轻微阴影（可选，匹配UI质感）
            setStroke(dp2px(1f).toInt(), Color.parseColor("#F0F0F0"))
        }

        // 2. 修复核心：创建「状态列表Drawable」，根据开关checked状态切换轨道颜色
        val trackStateDrawable = StateListDrawable().apply {
            // 开关开启状态（checked=true）的轨道样式
            val trackOn = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = thumbSize / 2  // 轨道圆角和滑块直径一致（胶囊形）
                setColor(onTrackColor)        // 开启时纯色：绿色
                setSize(ViewGroup.LayoutParams.MATCH_PARENT, (thumbSize * 0.6).toInt())
            }
            // 开关关闭状态（checked=false）的轨道样式
            val trackOff = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = thumbSize / 2
                setColor(offTrackColor)       // 关闭时纯色：浅灰
                setSize(ViewGroup.LayoutParams.MATCH_PARENT, (thumbSize * 0.6).toInt())
            }

            // 绑定「状态-样式」：checked=true（开启）显示trackOn，默认（关闭）显示trackOff
            addState(intArrayOf(android.R.attr.state_checked), trackOn)
            addState(intArrayOf(), trackOff) // 无状态（关闭）显示trackOff
        }

        // 3. 给Switch设置自定义的thumb和track（修复后）
        this.thumbDrawable = thumbDrawable
        this.trackDrawable = trackStateDrawable // 替换为状态列表Drawable

        // 移除默认的padding，让样式更贴合UI
        this.setPadding(dp2px(2f).toInt(), 0, dp2px(2f).toInt(), 0)
    }

    /**
     * dp转px工具方法，适配不同屏幕
     */
    private fun dp2px(dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    // 可选：提供代码中动态修改颜色的方法
    fun updateSwitchColors(onTrackColor: Int, offTrackColor: Int, thumbColor: Int) {
        setupSwitchStyle(onTrackColor, offTrackColor, thumbColor, DEFAULT_THUMB_SIZE)
        invalidate() // 重绘开关
    }
}