package com.azheng.viewutils.imagepicker

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

// ==================== 通用扩展 ====================

/** 获取文件名 */
fun Uri.getFileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else null
    }
}

/** 获取文件大小（字节） */
fun Uri.getFileSize(context: Context): Long {
    return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex >= 0 && cursor.moveToFirst()) {
            cursor.getLong(sizeIndex)
        } else 0L
    } ?: 0L
}

/** 获取 MIME 类型 */
fun Uri.getMimeType(context: Context): String? {
    return context.contentResolver.getType(this)
}

/** 是否是图片 */
fun Uri.isImage(context: Context): Boolean {
    return getMimeType(context)?.startsWith("image/") == true
}

/** 是否是视频 */
fun Uri.isVideo(context: Context): Boolean {
    return getMimeType(context)?.startsWith("video/") == true
}

/** 复制到缓存目录 */
fun Uri.copyToCache(context: Context, subDir: String = ""): File? {
    return try {
        val fileName = getFileName(context) ?: "file_${System.currentTimeMillis()}"
        val cacheDir = if (subDir.isNotEmpty()) {
            File(context.cacheDir, subDir).also { it.mkdirs() }
        } else {
            context.cacheDir
        }
        val cacheFile = File(cacheDir, fileName)

        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }
        cacheFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ==================== 视频专用扩展 ====================

/** 获取视频时长（毫秒） */
fun Uri.getVideoDuration(context: Context): Long {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, this)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        }
    } catch (e: Exception) {
        0L
    }
}

/** 获取视频时长（秒） */
fun Uri.getVideoDurationSeconds(context: Context): Int {
    return (getVideoDuration(context) / 1000).toInt()
}

/** 获取视频时长格式化字符串 (MM:SS 或 HH:MM:SS) */
fun Uri.getVideoDurationFormatted(context: Context): String {
    val totalSeconds = getVideoDurationSeconds(context)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/** 获取视频宽度 */
fun Uri.getVideoWidth(context: Context): Int {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, this)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        }
    } catch (e: Exception) {
        0
    }
}

/** 获取视频高度 */
fun Uri.getVideoHeight(context: Context): Int {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, this)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        }
    } catch (e: Exception) {
        0
    }
}

/** 获取视频尺寸 (width, height) */
fun Uri.getVideoSize(context: Context): Pair<Int, Int> {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, this)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            Pair(width, height)
        }
    } catch (e: Exception) {
        Pair(0, 0)
    }
}

/** 获取视频缩略图 */
fun Uri.getVideoThumbnail(context: Context, timeUs: Long = 0L): Bitmap? {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, this)
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
    } catch (e: Exception) {
        null
    }
}

/** 获取视频比特率 */
fun Uri.getVideoBitrate(context: Context): Int {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, this)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
        }
    } catch (e: Exception) {
        0
    }
}

// ==================== MediaMetadataRetriever 扩展 ====================

private inline fun <T> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> T): T {
    return try {
        block(this)
    } finally {
        release()
    }
}
