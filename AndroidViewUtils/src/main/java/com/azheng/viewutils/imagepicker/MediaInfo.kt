package com.azheng.viewutils.imagepicker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

/**
 * 媒体文件信息封装
 */
data class MediaInfo(
    val uri: Uri,
    val fileName: String?,
    val mimeType: String?,
    val fileSize: Long,
    val isVideo: Boolean,
    val isImage: Boolean,
    // 视频专属
    val videoDuration: Long = 0L,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0
) {
    /** 视频时长（秒） */
    val videoDurationSeconds: Int get() = (videoDuration / 1000).toInt()

    /** 格式化时长 */
    val videoDurationFormatted: String
        get() {
            val totalSeconds = videoDurationSeconds
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    /** 格式化文件大小 */
    val fileSizeFormatted: String
        get() {
            return when {
                fileSize < 1024 -> "$fileSize B"
                fileSize < 1024 * 1024 -> String.format("%.1f KB", fileSize / 1024f)
                fileSize < 1024 * 1024 * 1024 -> String.format("%.1f MB", fileSize / (1024f * 1024f))
                else -> String.format("%.2f GB", fileSize / (1024f * 1024f * 1024f))
            }
        }

    companion object {
        /** 从 Uri 创建 MediaInfo */
        fun from(context: Context, uri: Uri): MediaInfo {
            val mimeType = uri.getMimeType(context)
            val isVideo = mimeType?.startsWith("video/") == true
            val isImage = mimeType?.startsWith("image/") == true

            return MediaInfo(
                uri = uri,
                fileName = uri.getFileName(context),
                mimeType = mimeType,
                fileSize = uri.getFileSize(context),
                isVideo = isVideo,
                isImage = isImage,
                videoDuration = if (isVideo) uri.getVideoDuration(context) else 0L,
                videoWidth = if (isVideo) uri.getVideoWidth(context) else 0,
                videoHeight = if (isVideo) uri.getVideoHeight(context) else 0
            )
        }
    }
}

/** Uri 转 MediaInfo */
fun Uri.toMediaInfo(context: Context): MediaInfo = MediaInfo.from(context, this)

/** Uri 列表转 MediaInfo 列表 */
fun List<Uri>.toMediaInfoList(context: Context): List<MediaInfo> = map { it.toMediaInfo(context) }
