package com.azheng.viewutils.startup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.azheng.viewutils.imagepicker.ImagePicker

/**
 * 使用 App Startup 自动初始化，无需用户手动调用
 */
class ImagePickerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ImagePicker.autoInit(context.applicationContext as Application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}