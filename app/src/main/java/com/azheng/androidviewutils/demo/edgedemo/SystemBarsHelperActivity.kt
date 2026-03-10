package com.azheng.androidviewutils.demo.edgedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.databinding.ActivitySystemBarsHelperBinding
import com.azheng.viewutils.edge.EdgeToEdgeHelper
import com.azheng.viewutils.edge.SystemBarsHelper

/**
 * 系统栏工具类示例
 * 
 * 展示 SystemBarsHelper 的各种功能
 */
class SystemBarsHelperActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySystemBarsHelperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        EdgeToEdgeHelper.enable(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivitySystemBarsHelperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        EdgeToEdgeHelper.applyInsets(binding.rootLayout)
        
        displaySystemBarsInfo()
        setupClickListeners()
    }

    private fun displaySystemBarsInfo() {
        // 使用 SystemBarsHelper 获取信息
        val statusBarHeight = SystemBarsHelper.getStatusBarHeight(window)
        val navBarHeight = SystemBarsHelper.getNavigationBarHeight(window)
        
        binding.tvInfo.text = """
            📏 系统栏尺寸信息
            
            状态栏高度: ${statusBarHeight}px (${pxToDp(statusBarHeight)}dp)
            导航栏高度: ${navBarHeight}px (${pxToDp(navBarHeight)}dp)
            
            📱 设备信息
            屏幕密度: ${resources.displayMetrics.density}
            屏幕尺寸: ${resources.displayMetrics.widthPixels} x ${resources.displayMetrics.heightPixels}
        """.trimIndent()
    }

    private fun pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    private fun setupClickListeners() {
        binding.btnHideAll.setOnClickListener {
            SystemBarsHelper.hideSystemBars(window)
        }

        binding.btnHideStatus.setOnClickListener {
            SystemBarsHelper.hideSystemBars(
                window,
                hideStatusBar = true,
                hideNavigationBar = false
            )
        }

        binding.btnHideNav.setOnClickListener {
            SystemBarsHelper.hideSystemBars(
                window,
                hideStatusBar = false,
                hideNavigationBar = true
            )
        }

        binding.btnShowAll.setOnClickListener {
            SystemBarsHelper.showSystemBars(window)
        }
    }
}
