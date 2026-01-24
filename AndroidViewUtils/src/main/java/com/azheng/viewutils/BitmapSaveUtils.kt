package com.azheng.viewutils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object BitmapSaveUtils {

    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    /**
     * 保存 Bitmap 到 MediaStore（相册/公共存储）
     * 保存路径: Pictures/自定义文件夹
     * 需要权限: Android 10+ 不需要权限，Android 9及以下需要 WRITE_EXTERNAL_STORAGE
     */
    suspend fun saveBitmapToMediaStore(
        context: Context,
        bitmap: Bitmap,
        folderName: String = "MyApp-Photos",
        quality: Int = 100,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val name = generateFileName()
            val (mimeType, extension) = getFormatInfo(format)

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
                }
            }
            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("创建 MediaStore 记录失败")

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(format, quality, outputStream)) {
                    throw Exception("Bitmap 压缩失败")
                }
            } ?: throw Exception("打开输出流失败")

            uri
        }
    }

    /**
     * 保存 Bitmap 到 APP 内部存储（私有目录）
     * 保存路径: /data/data/包名/files/images/
     * 特点: 不需要权限，APP 卸载后自动删除，其他应用无法访问
     */
    suspend fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        subFolder: String = "images",
        fileName: String? = null,
        quality: Int = 100,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val name = fileName ?: generateFileName()
            val (_, extension) = getFormatInfo(format)

            val directory = File(context.filesDir, subFolder).apply {
                if (!exists()) mkdirs()
            }

            val file = File(directory, "$name$extension")

            FileOutputStream(file).use { outputStream ->
                if (!bitmap.compress(format, quality, outputStream)) {
                    throw Exception("Bitmap 压缩失败")
                }
            }

            file
        }
    }

    /**
     * 保存 Bitmap 到 APP 外部私有存储
     * 保存路径: /storage/emulated/0/Android/data/包名/files/Pictures/
     * 特点: 不需要权限，APP 卸载后自动删除，空间较大
     */
    suspend fun saveBitmapToExternalAppStorage(
        context: Context,
        bitmap: Bitmap,
        fileName: String? = null,
        quality: Int = 100,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val name = fileName ?: generateFileName()
            val (_, extension) = getFormatInfo(format)

            val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: throw Exception("外部存储不可用")

            val file = File(directory, "$name$extension")

            FileOutputStream(file).use { outputStream ->
                if (!bitmap.compress(format, quality, outputStream)) {
                    throw Exception("Bitmap 压缩失败")
                }
            }

            file
        }
    }

    /**
     * 保存 Bitmap 到缓存目录
     * 保存路径: /data/data/包名/cache/images/
     * 特点: 系统可能会自动清理
     */
    suspend fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String? = null,
        quality: Int = 100,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val name = fileName ?: generateFileName()
            val (_, extension) = getFormatInfo(format)

            val directory = File(context.cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }

            val file = File(directory, "$name$extension")

            FileOutputStream(file).use { outputStream ->
                if (!bitmap.compress(format, quality, outputStream)) {
                    throw Exception("Bitmap 压缩失败")
                }
            }

            file
        }
    }

    private fun generateFileName(): String {
        return SimpleDateFormat(FILENAME_FORMAT, Locale.CHINA)
            .format(System.currentTimeMillis())
    }

    @Suppress("DEPRECATION")
    private fun getFormatInfo(format: Bitmap.CompressFormat): Pair<String, String> {
        return when (format) {
            Bitmap.CompressFormat.JPEG -> "image/jpeg" to ".jpg"
            Bitmap.CompressFormat.PNG -> "image/png" to ".png"
            else -> {
                // 处理 WEBP 相关格式
                if (isWebpFormat(format)) {
                    "image/webp" to ".webp"
                } else {
                    "image/jpeg" to ".jpg"
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isWebpFormat(format: Bitmap.CompressFormat): Boolean {
        // 旧版 WEBP (API < 30)
        if (format == Bitmap.CompressFormat.WEBP) return true

        // 新版 WEBP (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (format == Bitmap.CompressFormat.WEBP_LOSSY ||
                format == Bitmap.CompressFormat.WEBP_LOSSLESS
            ) {
                return true
            }
        }
        return false
    }

    /**
     * 获取适合当前 API 级别的 WEBP 格式
     * @param lossless true 为无损压缩，false 为有损压缩
     */
    @Suppress("DEPRECATION")
    fun getWebpFormat(lossless: Boolean = false): Bitmap.CompressFormat {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (lossless) {
                Bitmap.CompressFormat.WEBP_LOSSLESS
            } else {
                Bitmap.CompressFormat.WEBP_LOSSY
            }
        } else {
            // API 30 以下使用旧版 WEBP（有损）
            Bitmap.CompressFormat.WEBP
        }
    }
}
