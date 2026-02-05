package com.azheng.viewutils.imageviewer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var rootView: FrameLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var config: ViewerConfig
    private var indicator: IIndicator? = null

    companion object {
        private const val EXTRA_CONFIG = "extra_config"

        internal var globalCallback: ImageViewerCallback? = null
        internal var customIndicator: IIndicator? = null
        internal var enterAnim: Int = android.R.anim.fade_in
        internal var exitAnim: Int = android.R.anim.fade_out

        internal fun start(context: Context, config: ViewerConfig, options: Bundle? = null) {
            enterAnim = config.enterAnim
            exitAnim = config.exitAnim

            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, config)
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            if (options != null && context is android.app.Activity) {
                context.startActivity(intent, options)
            } else {
                context.startActivity(intent)
                // 使用 overridePendingTransition 作为备选
                if (context is android.app.Activity) {
                    context.overridePendingTransition(config.enterAnim, 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 全屏显示
        setupFullScreen()
        
        // 获取配置
        config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CONFIG, ViewerConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_CONFIG) as? ViewerConfig
        } ?: ViewerConfig()

        // 创建布局
        createContentView()
        
        // 设置 ViewPager
        setupViewPager()
        
        // 设置指示器
        setupIndicator()
    }

    private fun setupFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // 隐藏导航栏
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    private fun createContentView() {
        rootView = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(config.backgroundColor)
        }

        viewPager = ViewPager2(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        rootView.addView(viewPager)
        setContentView(rootView)
    }

    private fun setupViewPager() {
        val callback = object : ImageViewerCallback {
            override fun onPageSelected(position: Int, url: String) {
                globalCallback?.onPageSelected(position, url)
            }

            override fun onImageClick(position: Int, url: String) {
                globalCallback?.onImageClick(position, url)
                if (config.clickToClose) {
                    dismissViewer()
                }
            }

            override fun onImageLongClick(position: Int, url: String): Boolean {
                return globalCallback?.onImageLongClick(position, url) ?: false
            }

            override fun onLoadStart(position: Int) {
                globalCallback?.onLoadStart(position)
            }

            override fun onLoadSuccess(position: Int) {
                globalCallback?.onLoadSuccess(position)
            }

            override fun onLoadFailed(position: Int, error: Exception?) {
                globalCallback?.onLoadFailed(position, error)
            }
        }

        val adapter = ImagePagerAdapter(config, callback)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(config.startPosition, false)

        // 页面切换动画
        if (config.enablePageTransformer) {
            viewPager.setPageTransformer(ScalePageTransformer())
        }

        // 页面切换监听
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                indicator?.onPageSelected(position, config.imageUrls.size)
                globalCallback?.onPageSelected(position, config.imageUrls[position])
            }
        })
    }

    private fun setupIndicator() {
        if (!config.showIndicator || config.indicatorStyle == IndicatorStyle.NONE) {
            return
        }

        indicator = customIndicator ?: DefaultIndicator(config.indicatorStyle)
        
        indicator?.let { ind ->
            val indicatorView = ind.createView(this)
            rootView.addView(indicatorView)
            ind.setTotal(config.imageUrls.size)
            ind.onPageSelected(config.startPosition, config.imageUrls.size)
        }
    }

    private fun dismissViewer() {
        globalCallback?.onDismiss()
        finish()
        overridePendingTransition(0, exitAnim)
    }

    override fun onBackPressed() {
        dismissViewer()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, exitAnim)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理静态引用，防止内存泄漏
        globalCallback = null
        customIndicator = null
    }
}
