package com.azheng.viewutils.startup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.azheng.viewutils.imageviewer.ImageViewer

class ImageViewerInitializer : Initializer<Unit> {
    
    override fun create(context: Context) {
        ImageViewer.init(context.applicationContext as Application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
