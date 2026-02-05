package com.azheng.androidviewutils.demo

import android.app.Application
import com.azheng.viewutils.imageviewer.ImageViewer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ImageViewer.init(this)
    }
}
