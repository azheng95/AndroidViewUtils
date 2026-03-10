package com.azheng.androidviewutils.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.azheng.androidviewutils.demo.databinding.ActivityMainBinding
import com.azheng.androidviewutils.demo.edgedemo.EdgeDemoListActivity
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.imagepicker.ImagePicker
import com.azheng.viewutils.imagepicker.MediaType
import com.azheng.viewutils.imageviewer.ImageViewer
import com.azheng.viewutils.setImageUrl


class MainActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivityMainBinding

    private val TAG = "MainActivity"

    private var imageUrl: Uri? = null
    override fun needAdaptSystemBar(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImageView.setOnClickListener {
            ImagePicker.pickMedia(
                context = this,
                maxCount = 1,
                mediaType = MediaType.IMAGE_ONLY
            ) { result ->
                imageUrl = result.getUrisOrEmpty().getOrNull(0)
                binding.ivImage.setImageUrl(imageUrl)
            }
        }
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(this, ImagePickerDemoActivity::class.java)
            startActivity(intent)
        }
        binding.btnEdgeDemo.setOnClickListener {
            startActivity(Intent(this, EdgeDemoListActivity::class.java))
        }
        binding.ivImage.setOnClickListener {
            setImageViewer()
        }

    }


    private fun setImageViewer() {
        imageUrl?.let {
            ImageViewer.with(this)
                .setImage(it)
                .showIndicator(false)  // ← 隐藏指示器
                .show()
        }

    }

}
