package com.azheng.androidviewutils.demo

import android.content.Intent
import android.os.Bundle
import com.azheng.androidviewutils.demo.databinding.ActivityMainBinding
import com.azheng.androidviewutils.demo.edgedemo.EdgeDemoListActivity
import com.azheng.androidviewutils.demo.sequentialanimator.SequentialAnimDemoActivity
import com.azheng.viewutils.edge.BaseEdgeActivity


class MainActivity : BaseEdgeActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun needAdaptSystemBar(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEdgeDemo.setOnClickListener {
            startActivity(Intent(this, EdgeDemoListActivity::class.java))
        }

        // ========== 新增：顺序动画示例跳转 ==========
        binding.btnSequentialAnim.setOnClickListener {
            // 跳转到顺序动画示例页面
            startActivity(Intent(this, SequentialAnimDemoActivity::class.java))
        }
    }
}
