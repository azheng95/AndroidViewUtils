package com.azheng.viewutils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
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
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        fileName: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatchingCancellable {
            requireValidQuality(quality)
            val (mimeType, extension) = getFormatInfo(format)
            val displayName = normalizeFileName(fileName, extension)
            var insertedUri: Uri? = null

            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        val safeFolderName = sanitizePathSegment(folderName, "Pictures")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/$safeFolderName"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val contentResolver = context.contentResolver
                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: error("创建 MediaStore 记录失败")
                insertedUri = uri

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    check(bitmap.compress(format, quality, outputStream)) { "Bitmap 压缩失败" }
                } ?: error("打开输出流失败")

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    contentResolver.update(
                        uri,
                        ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                        null,
                        null
                    )
                }

                uri
            } catch (error: Throwable) {
                insertedUri?.let { context.contentResolver.delete(it, null, null) }
                throw error
            }
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
        runCatchingCancellable {
            requireValidQuality(quality)
            val (_, extension) = getFormatInfo(format)
            val directory = resolveSafeDirectory(context.filesDir, subFolder)
            compressToFile(bitmap, directory, fileName, extension, quality, format)
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
        runCatchingCancellable {
            requireValidQuality(quality)
            val (_, extension) = getFormatInfo(format)

            val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: error("外部存储不可用")
            compressToFile(bitmap, directory, fileName, extension, quality, format)
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
        runCatchingCancellable {
            requireValidQuality(quality)
            val (_, extension) = getFormatInfo(format)
            val directory = resolveSafeDirectory(context.cacheDir, "images")
            compressToFile(bitmap, directory, fileName, extension, quality, format)
        }
    }

    private fun compressToFile(
        bitmap: Bitmap,
        directory: File,
        fileName: String?,
        extension: String,
        quality: Int,
        format: Bitmap.CompressFormat
    ): File {
        ensureDirectory(directory)
        val file = File(directory, normalizeFileName(fileName, extension))
        FileOutputStream(file).use { outputStream ->
            check(bitmap.compress(format, quality, outputStream)) { "Bitmap 压缩失败" }
        }
        return file
    }

    private fun resolveSafeDirectory(root: File, subFolder: String): File {
        require(subFolder.isNotBlank()) { "目录名不能为空" }
        val rootPath = root.canonicalFile
        val directory = File(rootPath, subFolder).canonicalFile
        require(directory.path.startsWith(rootPath.path + File.separator)) { "目录必须位于应用存储内" }
        return directory
    }

    private fun ensureDirectory(directory: File) {
        check(directory.isDirectory || directory.mkdirs()) { "无法创建目录: ${directory.path}" }
    }

    private fun normalizeFileName(fileName: String?, extension: String): String {
        val rawName = fileName?.takeIf { it.isNotBlank() } ?: generateFileName()
        val safeName = sanitizePathSegment(rawName, generateFileName())
        return if (safeName.endsWith(extension, ignoreCase = true)) safeName else "$safeName$extension"
    }

    private fun sanitizePathSegment(value: String, fallback: String): String {
        val sanitized = value
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .trim('.')
        return sanitized.ifBlank { fallback }
    }

    private fun requireValidQuality(quality: Int) {
        require(quality in 0..100) { "quality 必须在 0..100 之间" }
    }

    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
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
