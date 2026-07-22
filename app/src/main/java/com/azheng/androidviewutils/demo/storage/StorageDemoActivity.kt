package com.azheng.androidviewutils.demo.storage

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.azheng.androidviewutils.demo.R
import com.azheng.androidviewutils.demo.databinding.ActivityStorageDemoBinding
import com.azheng.viewutils.BitmapSaveUtils
import com.azheng.viewutils.ViewToImageUtils
import kotlinx.coroutines.launch

class StorageDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStorageDemoBinding

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveToGallery()
        } else {
            showResult(false, getString(R.string.storage_permission_denied))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonSaveView.setOnClickListener { saveViewExample() }
        binding.buttonSaveCache.setOnClickListener {
            saveBitmap { bitmap ->
                BitmapSaveUtils.saveBitmapToCache(this, bitmap).map { it.absolutePath }
            }
        }
        binding.buttonSaveInternal.setOnClickListener {
            saveBitmap { bitmap ->
                BitmapSaveUtils.saveBitmapToInternalStorage(this, bitmap).map { it.absolutePath }
            }
        }
        binding.buttonSaveExternal.setOnClickListener {
            saveBitmap { bitmap ->
                BitmapSaveUtils.saveBitmapToExternalAppStorage(this, bitmap).map { it.absolutePath }
            }
        }
        binding.buttonSaveGallery.setOnClickListener { requestGallerySave() }

        binding.storageStatus.text = getString(
            R.string.storage_webp_format,
            BitmapSaveUtils.getWebpFormat(lossless = true).name
        )
    }

    private fun saveViewExample() {
        setBusy(true)
        ViewToImageUtils.saveViewToImage(
            context = this,
            view = binding.captureTarget,
            lifecycleScope = lifecycleScope,
            fileName = "view-utils-example",
            isSaveToInternal = true
        ) { success, location ->
            setBusy(false)
            showResult(success, location)
        }
    }

    private fun requestGallerySave() {
        val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED

        if (needsLegacyPermission) {
            requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveToGallery()
        }
    }

    private fun saveToGallery() {
        saveBitmap { bitmap ->
            BitmapSaveUtils.saveBitmapToMediaStore(
                context = this,
                bitmap = bitmap,
                folderName = "AndroidViewUtils"
            ).map { it.toString() }
        }
    }

    private fun saveBitmap(save: suspend (Bitmap) -> Result<String>) {
        lifecycleScope.launch {
            setBusy(true)
            val bitmap = binding.captureTarget.drawToBitmap(Bitmap.Config.ARGB_8888)
            try {
                save(bitmap).fold(
                    onSuccess = { showResult(true, it) },
                    onFailure = { showResult(false, it.message ?: getString(R.string.storage_unknown_error)) }
                )
            } finally {
                bitmap.recycle()
                setBusy(false)
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.storageProgress.isVisible = busy
        listOf(
            binding.buttonSaveView,
            binding.buttonSaveCache,
            binding.buttonSaveInternal,
            binding.buttonSaveExternal,
            binding.buttonSaveGallery
        ).forEach { it.isEnabled = !busy }
    }

    private fun showResult(success: Boolean, detail: String) {
        binding.storageStatus.text = getString(
            if (success) R.string.storage_success else R.string.storage_failed,
            detail
        )
    }
}
