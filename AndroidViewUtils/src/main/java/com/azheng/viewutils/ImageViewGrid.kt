package com.azheng.viewutils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @author azheng
 * @date 2019/5/21.
 * description：正方形 根据宽度设置高度
 */
@SuppressLint("AppCompatCustomView")
class ImageViewGrid : ImageView {
    constructor(context: Context?):super(context)
    constructor(context: Context?,attributeSet: AttributeSet):super(context,attributeSet)
    constructor(context: Context?,attributeSet: AttributeSet,defstyle:Int):super(context,attributeSet,defstyle)

    /**
     * 宽度
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}