package com.azheng.viewutils.imageviewer

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.Serializable

/**
 * 图片源密封类，支持多种图片类型
 */
sealed class ImageSource : Serializable {

    /**
     * URL/路径字符串类型
     */
    data class Url(val url: String) : ImageSource() {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * Uri 类型（存储为字符串以支持序列化）
     */
    data class UriSource(val uriString: String) : ImageSource() {
        constructor(uri: Uri) : this(uri.toString())

        override fun toUri(): Uri = Uri.parse(uriString)

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * 文件类型（存储路径以支持序列化）
     */
    data class FileSource(val filePath: String) : ImageSource() {
        constructor(file: File) : this(file.absolutePath)

        fun toFile(): File = File(filePath)

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * 资源ID类型
     */
    data class Resource(
        val resId: Int,
        val packageName: String
    ) : ImageSource() {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * 转换为 Uri 用于图片加载
     */
    open fun toUri(): Uri {
        return when (this) {
            is Url -> parseStringToUri(url)
            is UriSource -> toUri()
            is FileSource -> Uri.fromFile(toFile())
            is Resource -> Uri.parse("android.resource://$packageName/$resId")
        }
    }

    /**
     * 获取显示用的字符串表示
     */
    fun toDisplayString(): String {
        return when (this) {
            is Url -> url
            is UriSource -> uriString
            is FileSource -> filePath
            is Resource -> "android.resource://$packageName/$resId"
        }
    }

    companion object {
        private const val serialVersionUID = 1L

        /**
         * 从字符串创建 ImageSource
         */
        fun fromString(path: String): ImageSource = Url(path)

        /**
         * 从 Uri 创建 ImageSource
         */
        fun fromUri(uri: Uri): ImageSource = UriSource(uri)

        /**
         * 从 File 创建 ImageSource
         */
        fun fromFile(file: File): ImageSource = FileSource(file)

        /**
         * 从资源 ID 创建 ImageSource
         */
        fun fromResource(resId: Int, packageName: String): ImageSource = Resource(resId, packageName)

        /**
         * 解析字符串路径为 Uri
         */
        private fun parseStringToUri(path: String): Uri {
            return when {
                path.startsWith("http://") ||
                path.startsWith("https://") ||
                path.startsWith("file://") ||
                path.startsWith("content://") ||
                path.startsWith("android.resource://") -> {
                    Uri.parse(path)
                }
                // 本地文件路径（以 / 开头）
                path.startsWith("/") -> {
                    Uri.fromFile(File(path))
                }
                // 其他情况，尝试作为网络 URL
                else -> {
                    Uri.parse(path)
                }
            }
        }
    }
}
