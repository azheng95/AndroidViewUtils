package com.azheng.androidviewutils.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.azheng.androidviewutils.demo.databinding.ActivityMainBinding
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.imagepicker.ImagePicker
import com.azheng.viewutils.imageviewer.ImageViewer
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.launch


class MainActivity : BaseEdgeActivity() {

    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewBinding.tvTestView.setOnClickListener {
            setImageViewer()
        }
        viewBinding.btnSelectImage.setOnClickListener {
            lifecycleScope.launch {
                val uris = ImagePicker.pickMediaSuspend(this@MainActivity)
                if (uris.isNotEmpty()) {

                    // 处理图片
                    Log.d( TAG, "图片数量：${uris.size}")
                }
            }

        }
    }


    private fun setImageViewer() {
        ImageViewer.with(this)
            .setImages(mutableListOf<String>("https:/d2aag0s7ngp5r1.cloudfront.net/rock2/img/Bloodstone tumbled_3_676.jpg"))
            .showIndicator(false)  // ← 隐藏指示器
            .show()

    }

}
