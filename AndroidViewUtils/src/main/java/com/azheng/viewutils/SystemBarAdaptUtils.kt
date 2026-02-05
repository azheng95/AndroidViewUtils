package com.azheng.viewutils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

/**
 * 系统栏（底部导航栏/状态栏）适配工具类
 * 兼容 Android 8 (API 26) ~ Android 16 (API 36)
 * 核心功能：解决布局与底部导航栏重叠问题
 */
object SystemBarAdaptUtils {

    private const val TAG_ADAPTED = "tag_navigation_bar_adapted"
    private const val TAG_ORIGINAL_PADDING_BOTTOM = "tag_original_padding_bottom"
    private const val TAG_ORIGINAL_PADDING_LEFT = "tag_original_padding_left"
    private const val TAG_ORIGINAL_PADDING_RIGHT = "tag_original_padding_right"

    /**
     * 【推荐】适配底部导航栏（支持横屏侧边导航栏）
     * 使用 WindowInsets 监听器，自动响应导航栏显示/隐藏变化
     *
     * @param targetView 需要适配的目标View
     * @param consumeInsets 是否消费 insets，防止子 View 重复处理（默认 false）
     */
    fun adaptNavigationBar(targetView: View, consumeInsets: Boolean = false) {
        if (targetView.getTag(TAG_ADAPTED.hashCode()) as? Boolean == true) return

        // 保存原始 padding
        saveOriginalPadding(targetView)

        ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val originalBottom = v.getTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode()) as? Int ?: 0
            val originalLeft = v.getTag(TAG_ORIGINAL_PADDING_LEFT.hashCode()) as? Int ?: 0
            val originalRight = v.getTag(TAG_ORIGINAL_PADDING_RIGHT.hashCode()) as? Int ?: 0

            v.setPadding(
                originalLeft + navInsets.left,    // 侧边导航栏（横屏左侧）
                v.paddingTop,                      // 顶部不处理
                originalRight + navInsets.right,  // 侧边导航栏（横屏右侧）
                originalBottom + navInsets.bottom // 底部导航栏
            )

            if (consumeInsets) {
                // 消费导航栏 insets
                WindowInsetsCompat.Builder(insets)
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.NONE)
                    .build()
            } else {
                insets
            }
        }

        requestInsetsWhenReady(targetView)
        targetView.setTag(TAG_ADAPTED.hashCode(), true)
    }

    /**
     * 只适配底部导航栏（不处理侧边）
     * 适用于只需要底部适配的场景
     *
     * @param targetView 需要适配的目标View
     */
    fun adaptBottomNavigationBar(targetView: View) {
        if (targetView.getTag(TAG_ADAPTED.hashCode()) as? Boolean == true) return

        targetView.setTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode(), targetView.paddingBottom)

        ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val originalPadding = v.getTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode()) as? Int ?: 0

            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                originalPadding + navBarHeight
            )
            insets
        }

        requestInsetsWhenReady(targetView)
        targetView.setTag(TAG_ADAPTED.hashCode(), true)
    }

    /**
     * 使用 Margin 方式适配底部导航栏
     * 适用于需要保持 View 原有 padding 的场景
     *
     * @param targetView 需要适配的目标View（需要有 MarginLayoutParams）
     */
    fun adaptNavigationBarWithMargin(targetView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams ?: return@setOnApplyWindowInsetsListener insets

            layoutParams.bottomMargin = navInsets.bottom
            layoutParams.leftMargin = navInsets.left
            layoutParams.rightMargin = navInsets.right
            v.layoutParams = layoutParams

            insets
        }

        requestInsetsWhenReady(targetView)
    }

    /**
     * 适配 Activity 的根布局
     *
     * @param activity 目标 Activity
     * @param includeStatusBar 是否同时适配状态栏（默认 false）
     */
    fun adaptActivityRootView(activity: Activity, includeStatusBar: Boolean = false) {
        val rootView = getActivityRootView(activity) ?: return

        if (includeStatusBar) {
            adaptAllSystemBars(rootView)
        } else {
            adaptNavigationBar(rootView)
        }
    }

    /**
     * 仅适配 Activity 底部导航栏（推荐：与沉浸式状态栏配合使用）
     *
     * @param activity 目标 Activity
     */
    fun adaptActivityBottomOnly(activity: Activity) {
        val rootView = getActivityRootView(activity) ?: return

        if (rootView.getTag(TAG_ADAPTED.hashCode()) as? Boolean == true) return

        rootView.setTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode(), rootView.paddingBottom)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val originalPadding = v.getTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode()) as? Int ?: 0

            v.setPadding(
                v.paddingLeft,
                0,  // 顶部交给状态栏工具处理
                v.paddingRight,
                originalPadding + navBarHeight
            )
            insets
        }

        requestInsetsWhenReady(rootView)
        rootView.setTag(TAG_ADAPTED.hashCode(), true)
    }

    /**
     * 适配 Fragment 的根布局
     *
     * @param fragment 目标 Fragment
     */
    fun adaptFragmentRootView(fragment: Fragment) {
        val rootView = fragment.view ?: return
        adaptNavigationBar(rootView)
    }

    /**
     * 同时适配状态栏和导航栏
     *
     * @param targetView 需要适配的目标 View
     */
    fun adaptAllSystemBars(targetView: View) {
        if (targetView.getTag(TAG_ADAPTED.hashCode()) as? Boolean == true) return

        saveOriginalPadding(targetView)
        val originalTop = targetView.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val originalBottom = v.getTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode()) as? Int ?: 0
            val originalLeft = v.getTag(TAG_ORIGINAL_PADDING_LEFT.hashCode()) as? Int ?: 0
            val originalRight = v.getTag(TAG_ORIGINAL_PADDING_RIGHT.hashCode()) as? Int ?: 0

            v.setPadding(
                originalLeft + systemBars.left,
                originalTop + systemBars.top,
                originalRight + systemBars.right,
                originalBottom + systemBars.bottom
            )
            insets
        }

        requestInsetsWhenReady(targetView)
        targetView.setTag(TAG_ADAPTED.hashCode(), true)
    }

    /**
     * 重置 View 的适配状态（恢复原始 padding）
     *
     * @param targetView 需要重置的 View
     */
    fun resetAdaptState(targetView: View) {
        // 移除监听器
        ViewCompat.setOnApplyWindowInsetsListener(targetView, null)

        // 恢复原始 padding
        val originalBottom = targetView.getTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode()) as? Int
        val originalLeft = targetView.getTag(TAG_ORIGINAL_PADDING_LEFT.hashCode()) as? Int
        val originalRight = targetView.getTag(TAG_ORIGINAL_PADDING_RIGHT.hashCode()) as? Int

        if (originalBottom != null || originalLeft != null || originalRight != null) {
            targetView.setPadding(
                originalLeft ?: targetView.paddingLeft,
                targetView.paddingTop,
                originalRight ?: targetView.paddingRight,
                originalBottom ?: targetView.paddingBottom
            )
        }

        // 清除标记
        targetView.setTag(TAG_ADAPTED.hashCode(), false)
        targetView.setTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode(), null)
        targetView.setTag(TAG_ORIGINAL_PADDING_LEFT.hashCode(), null)
        targetView.setTag(TAG_ORIGINAL_PADDING_RIGHT.hashCode(), null)
    }

    /**
     * 同步获取导航栏高度（View 必须已 attach 到 Window）
     *
     * @param targetView 目标 View
     * @return 导航栏高度（px），获取失败返回 0
     */
    fun getNavigationBarHeight(targetView: View): Int {
        return ViewCompat.getRootWindowInsets(targetView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())
            ?.bottom ?: 0
    }

    /**
     * 同步获取导航栏 Insets（包含四边）
     *
     * @param targetView 目标 View
     * @return Insets 对象，获取失败返回 Insets.NONE
     */
    fun getNavigationBarInsets(targetView: View): Insets {
        return ViewCompat.getRootWindowInsets(targetView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())
            ?: Insets.NONE
    }

    /**
     * 从 Context 获取导航栏高度（兜底方案，不依赖 View）
     *
     * @param context 上下文
     * @return 导航栏高度（px），无导航栏返回 0
     */
    fun getNavigationBarHeightFromContext(context: Context): Int {
        // 先检查是否有导航栏
        if (!hasNavigationBarByScreenSize(context)) {
            return 0
        }

        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 判断导航栏是否可见
     *
     * @param targetView 目标 View（必须已 attach）
     * @return true = 导航栏可见
     */
    fun isNavigationBarVisible(targetView: View): Boolean {
        val insets = ViewCompat.getRootWindowInsets(targetView) ?: return false
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        return navInsets.bottom > 0 || navInsets.left > 0 || navInsets.right > 0
    }

    /**
     * 判断设备是否存在虚拟导航栏（综合判断）
     *
     * @param targetView 目标 View
     * @return true = 存在虚拟导航栏
     */
    fun hasVirtualNavigationBar(targetView: View): Boolean {
        // 优先使用 WindowInsets API（更准确）
        ViewCompat.getRootWindowInsets(targetView)?.let { insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            if (navInsets.bottom > 0 || navInsets.left > 0 || navInsets.right > 0) {
                return true
            }
        }

        // 兜底：屏幕尺寸对比法
        return hasNavigationBarByScreenSize(targetView.context)
    }

    /**
     * 异步获取导航栏高度（适用于 View 未 attach 的情况）
     *
     * @param targetView 目标 View
     * @param callback 回调，返回导航栏高度
     */
    fun getNavigationBarHeightAsync(targetView: View, callback: (Int) -> Unit) {
        if (targetView.isAttachedToWindow) {
            callback(getNavigationBarHeight(targetView))
            return
        }

        targetView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.post {
                    callback(getNavigationBarHeight(v))
                }
                v.removeOnAttachStateChangeListener(this)
            }

            override fun onViewDetachedFromWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
            }
        })
    }

    // ==================== 私有方法 ====================

    /**
     * 保存原始 padding
     */
    private fun saveOriginalPadding(view: View) {
        if (view.getTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode()) == null) {
            view.setTag(TAG_ORIGINAL_PADDING_BOTTOM.hashCode(), view.paddingBottom)
            view.setTag(TAG_ORIGINAL_PADDING_LEFT.hashCode(), view.paddingLeft)
            view.setTag(TAG_ORIGINAL_PADDING_RIGHT.hashCode(), view.paddingRight)
        }
    }

    /**
     * 在 View ready 时请求 insets
     */
    private fun requestInsetsWhenReady(targetView: View) {
        if (targetView.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(targetView)
        } else {
            targetView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    ViewCompat.requestApplyInsets(v)
                    v.removeOnAttachStateChangeListener(this)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                }
            })
        }
    }

    /**
     * 获取 Activity 根布局
     */
    private fun getActivityRootView(activity: Activity): View? {
        val contentContainer = activity.window.decorView
            .findViewById<ViewGroup>(android.R.id.content)
        return if (contentContainer.childCount > 0) {
            contentContainer.getChildAt(0)
        } else {
            contentContainer
        }
    }

    /**
     * 通过屏幕尺寸判断是否有导航栏（兜底方案）
     */
    @Suppress("DEPRECATION")
    private fun hasNavigationBarByScreenSize(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用新 API
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars())
            insets.bottom > 0 || insets.left > 0 || insets.right > 0
        } else {
            // Android 8~10 使用 Display 尺寸对比
            val display = windowManager.defaultDisplay
            val normalSize = Point()
            val realSize = Point()
            display.getSize(normalSize)
            display.getRealSize(realSize)
            realSize.y > normalSize.y || realSize.x > normalSize.x
        }
    }
}
