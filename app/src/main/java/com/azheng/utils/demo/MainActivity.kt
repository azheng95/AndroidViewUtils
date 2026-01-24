package com.azheng.utils.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.AndroidViewUtils.demo.R
import com.azheng.AndroidViewUtils.demo.databinding.ActivityMainBinding
import com.azheng.viewutils.ArcProgressBar
import dev.androidbroadcast.vbpd.viewBinding


class MainActivity : AppCompatActivity() {

    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val arcProgressBar = ArcProgressBar(this)
    }

}
