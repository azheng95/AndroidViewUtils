package com.azheng.androidviewutils.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.components.ComponentsDemoActivity
import com.azheng.androidviewutils.demo.databinding.ActivityMainBinding
import com.azheng.androidviewutils.demo.image.ImageDemoActivity
import com.azheng.androidviewutils.demo.sequentialanimator.SequentialAnimDemoActivity
import com.azheng.androidviewutils.demo.storage.StorageDemoActivity
import com.azheng.androidviewutils.demo.text.TextDemoActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSequentialAnim.setOnClickListener {
            startActivity(Intent(this, SequentialAnimDemoActivity::class.java))
        }
        binding.btnImageDemo.setOnClickListener {
            startActivity(Intent(this, ImageDemoActivity::class.java))
        }
        binding.btnComponentsDemo.setOnClickListener {
            startActivity(Intent(this, ComponentsDemoActivity::class.java))
        }
        binding.btnTextDemo.setOnClickListener {
            startActivity(Intent(this, TextDemoActivity::class.java))
        }
        binding.btnStorageDemo.setOnClickListener {
            startActivity(Intent(this, StorageDemoActivity::class.java))
        }
    }
}
