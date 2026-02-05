package com.azheng.androidviewutils.demo.edge

import android.os.Bundle

/**
 * 聊天/评论等输入页面
 * - 底部输入框跟随键盘弹起
 */
class ChatActivity : BaseEdgeActivity() {

    // 启用输入法适配
    override fun isFitIme(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_chat)

        // 或者只对特定区域适配
        // findViewById<View>(R.id.inputContainer).applyImePadding()
    }
}
