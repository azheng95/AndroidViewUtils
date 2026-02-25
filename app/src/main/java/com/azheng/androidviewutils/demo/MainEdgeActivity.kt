package com.azheng.androidviewutils.demo

import android.os.Bundle
import com.azheng.androidviewutils.demo.databinding.ActivityMainBinding
import com.azheng.viewutils.edge.BaseEdgeActivity
import com.azheng.viewutils.imageviewer.ImageViewer
import dev.androidbroadcast.vbpd.viewBinding


class MainEdgeActivity : BaseEdgeActivity() {

    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImageViewer.init(application)
        viewBinding.tvTestView.setOnClickListener {
            setImageViewer()
        }
    }


    private fun setImageViewer() {
        ImageViewer.with(this)
            .setImages(mutableListOf<String>("https:/d2aag0s7ngp5r1.cloudfront.net/rock2/img/Bloodstone tumbled_3_676.jpg"))
            .showIndicator(false)  // ← 隐藏指示器
            .show()

    }

}
