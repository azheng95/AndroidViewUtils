package com.azheng.viewutils.imageviewer

interface ImageViewerCallback {
    /** 页面切换 */
    fun onPageSelected(position: Int, url: String) {}
    
    /** 图片单击 */
    fun onImageClick(position: Int, url: String) {}
    
    /** 图片长按 */
    fun onImageLongClick(position: Int, url: String): Boolean = false
    
    /** 图片加载开始 */
    fun onLoadStart(position: Int) {}
    
    /** 图片加载成功 */
    fun onLoadSuccess(position: Int) {}
    
    /** 图片加载失败 */
    fun onLoadFailed(position: Int, error: Exception?) {}
    
    /** 浏览器关闭 */
    fun onDismiss() {}
}
