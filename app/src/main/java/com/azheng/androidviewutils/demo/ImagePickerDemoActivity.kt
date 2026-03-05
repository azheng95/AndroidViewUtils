package com.azheng.androidviewutils.demo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.imagepicker.ImagePicker
import com.azheng.viewutils.imagepicker.ImagePickerConfig
import com.azheng.viewutils.imagepicker.ImagePickerResult
import com.azheng.viewutils.imagepicker.MediaType
import kotlinx.coroutines.launch

/**
 * ImagePicker 使用示例
 *
 * 展示统一的 pickMedia / pickMediaSuspend API 用法
 * 通过 MediaType 参数控制选择类型：
 * - MediaType.IMAGE_ONLY: 仅图片
 * - MediaType.VIDEO_ONLY: 仅视频
 * - MediaType.IMAGE_AND_VIDEO: 图片和视频（默认）
 */
class ImagePickerDemoActivity : BaseEdgeActivity() {
    private val TAG = "ImagePickerDemoActivity"

    // 模拟已选择的图片列表（用于演示限制功能）
    private val selectedUris = mutableListOf<Uri>()
    private val maxSelectCount = 9

    private lateinit var tvSelectedCount: TextView
    private lateinit var llPreviewContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker_demo)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tvSelectedCount = findViewById(R.id.tvSelectedCount)
        llPreviewContainer = findViewById(R.id.llPreviewContainer)
        updateSelectedCountUI()
    }

    private fun setupClickListeners() {
        // ==================== 基础用法 ====================

        // 1. 选择单张图片
        findViewById<Button>(R.id.btnPickSingleImage).setOnClickListener {
            ImagePicker.pickMedia(
                context = this,
                maxCount = 1,
                mediaType = MediaType.IMAGE_ONLY
            ) { result ->
                handleResult("单张图片", result)
            }
        }

        // 2. 选择多张图片（最多9张）
        findViewById<Button>(R.id.btnPickMultipleImages).setOnClickListener {
            ImagePicker.pickMedia(
                context = this,
                maxCount = 9,
                mediaType = MediaType.IMAGE_ONLY
            ) { result ->
                handleResult("多张图片", result)
            }
        }

        // 3. 选择视频
        findViewById<Button>(R.id.btnPickVideos).setOnClickListener {
            ImagePicker.pickMedia(
                context = this,
                maxCount = 3,
                mediaType = MediaType.VIDEO_ONLY
            ) { result ->
                handleResult("视频", result)
            }
        }

        // 4. 选择图片或视频（混合选择 - 使用默认 MediaType.IMAGE_AND_VIDEO）
        findViewById<Button>(R.id.btnPickMedia).setOnClickListener {
            ImagePicker.pickMedia(this, maxCount = 9) { result ->
                handleResult("媒体文件", result)
            }
        }

        // ==================== 带已选限制的用法 ====================

        // 5. 继续选择（带已选数量限制）
        findViewById<Button>(R.id.btnPickWithLimit).setOnClickListener {
            ImagePicker.pickMedia(
                context = this,
                maxCount = maxSelectCount,
                mediaType = MediaType.IMAGE_ONLY,
                currentSelectedCount = selectedUris.size,
                onMaxLimitReached = { info ->
                    // 达到最大限制时的提示
                    Toast.makeText(this, info.message, Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "达到限制: max=${info.maxCount}, current=${info.currentCount}")
                }
            ) { result ->
                handleResultWithAdd(result)
            }
        }

        // 6. 使用 Uri 列表计算已选数量
        findViewById<Button>(R.id.btnPickWithUriList).setOnClickListener {
            ImagePicker.pickMedia(
                context = this,
                maxCount = maxSelectCount,
                mediaType = MediaType.IMAGE_ONLY,
                currentSelectedUris = selectedUris,  // 直接传入已选 Uri 列表
                onMaxLimitReached = { info ->
                    Toast.makeText(this, "已达上限：${info.message}", Toast.LENGTH_SHORT).show()
                }
            ) { result ->
                handleResultWithAdd(result)
            }
        }

        // ==================== 协程用法 ====================

        // 7. 协程方式 - 简单用法（返回 Uri 列表）
        findViewById<Button>(R.id.btnPickCoroutineSimple).setOnClickListener {
            lifecycleScope.launch {
                val uris = ImagePicker.pickMediaSuspend(
                    context = this@ImagePickerDemoActivity,
                    maxCount = 5,
                    mediaType = MediaType.IMAGE_ONLY
                )
                if (uris.isNotEmpty()) {
                    Log.d(TAG, "协程选择成功: ${uris.size}张")
                    showSelectedImages(uris)
                    Toast.makeText(
                        this@ImagePickerDemoActivity,
                        "选择了 ${uris.size} 张图片",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d(TAG, "协程选择取消或失败")
                    Toast.makeText(this@ImagePickerDemoActivity, "已取消", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 8. 协程方式 - 完整结果（返回 ImagePickerResult）
        findViewById<Button>(R.id.btnPickCoroutineFull).setOnClickListener {
            lifecycleScope.launch {
                val result = ImagePicker.pickMediaSuspend(
                    context = this@ImagePickerDemoActivity,
                    maxCount = maxSelectCount,
                    mediaType = MediaType.IMAGE_ONLY,
                    currentSelectedCount = selectedUris.size
                )
                handleResultWithAdd(result)
            }
        }

        // ==================== 自定义配置用法 ====================

        // 9. 使用 Builder 完全自定义配置
        findViewById<Button>(R.id.btnPickCustomConfig).setOnClickListener {
            val config = ImagePickerConfig.Builder()
                .maxCount(6)
                .mediaType(MediaType.IMAGE_AND_VIDEO)  // 或使用 .imageAndVideo()
                .enablePersistPermission(true)  // 获取持久权限
                .currentSelectedCount(selectedUris.size)
                .onMaxLimitReached { info ->
                    Toast.makeText(this, "自定义提示: ${info.message}", Toast.LENGTH_LONG).show()
                }
                .build()

            ImagePicker.pick(this, config) { result ->
                handleResultWithAdd(result)
            }
        }

        // 10. 清空已选
        findViewById<Button>(R.id.btnClearSelected).setOnClickListener {
            selectedUris.clear()
            updateSelectedCountUI()
            llPreviewContainer.removeAllViews()
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 处理选择结果（仅展示）
     */
    private fun handleResult(tag: String, result: ImagePickerResult) {
        when (result) {
            is ImagePickerResult.Success -> {
                Log.d(TAG, "[$tag] 选择成功: ${result.uris.size}个文件")
                result.uris.forEachIndexed { index, uri ->
                    Log.d(TAG, "  [$index] $uri")
                }
                showSelectedImages(result.uris)
                Toast.makeText(this, "选择了 ${result.uris.size} 个文件", Toast.LENGTH_SHORT).show()
            }
            is ImagePickerResult.Cancelled -> {
                Log.d(TAG, "[$tag] 用户取消")
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show()
            }
            is ImagePickerResult.MaxLimitReached -> {
                Log.w(TAG, "[$tag] 达到最大限制: ${result.message}")
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
            is ImagePickerResult.Error -> {
                Log.e(TAG, "[$tag] 错误", result.exception)
                Toast.makeText(this, "错误: ${result.exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 处理选择结果并添加到已选列表
     */
    private fun handleResultWithAdd(result: ImagePickerResult) {
        when (result) {
            is ImagePickerResult.Success -> {
                // 添加新选择的到已选列表
                selectedUris.addAll(result.uris)
                updateSelectedCountUI()
                showSelectedImages(result.uris)

                Log.d(TAG, "添加成功: +${result.uris.size}, 总计: ${selectedUris.size}")
                Toast.makeText(this, "已选择 ${selectedUris.size}/$maxSelectCount", Toast.LENGTH_SHORT).show()
            }
            is ImagePickerResult.Cancelled -> {
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show()
            }
            is ImagePickerResult.MaxLimitReached -> {
                // 回调中已处理，这里可以做额外处理
                Log.w(TAG, "已达上限: ${result.currentCount}/${result.maxCount}")
            }
            is ImagePickerResult.Error -> {
                Toast.makeText(this, "错误: ${result.exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 更新已选数量显示
     */
    private fun updateSelectedCountUI() {
        tvSelectedCount.text = "已选择: ${selectedUris.size}/$maxSelectCount"
    }

    /**
     * 显示选中的图片预览
     */
    private fun showSelectedImages(uris: List<Uri>) {
        uris.forEach { uri ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    marginEnd = 8
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }
            llPreviewContainer.addView(imageView)
        }
    }
}
