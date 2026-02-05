package com.azheng.viewutils.imageviewer

import android.content.Context
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.piasy.biv.view.ImageViewFactory

internal class ExifImageViewFactory : ImageViewFactory() {
    override fun createStillImageView(context: Context): SubsamplingScaleImageView {
        return SubsamplingScaleImageView(context).apply {
            orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
            setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
            maxScale = 10f
        }
    }
}
