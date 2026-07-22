package com.azheng.androidviewutils.demo.components

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.azheng.androidviewutils.demo.R
import com.azheng.androidviewutils.demo.databinding.ActivityComponentsDemoBinding
import com.azheng.viewutils.DashedLineView
import com.azheng.viewutils.DiamondProgressBar

class ComponentsDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComponentsDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComponentsDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        setupArcProgress()
        setupCircleLoading()
        setupDiamondProgress()
        setupSwitch()
    }

    private fun setupArcProgress() {
        binding.arcProgress.setProgressWithAnimation(72f, 700L)
        binding.seekArcProgress.setOnSeekBarChangeListener(seekListener { progress ->
            binding.arcProgress.setProgress(progress.toFloat())
        })
        binding.buttonArcAnimate.setOnClickListener {
            binding.arcProgress.setProgressWithAnimation(
                targetProgress = binding.seekArcProgress.progress.toFloat(),
                duration = 700L
            )
        }
    }

    private fun setupCircleLoading() {
        binding.circleLoading
            .bindToLifecycle(this)
            .setTargetProgress(100f)
            .setAnimationDuration(1800L)
            .setProgressColor(Color.rgb(37, 99, 235))
            .setBgCircleColor(Color.rgb(219, 234, 254))
            .apply()
            .startLoadingAnimation()

        binding.buttonCircleStart.setOnClickListener {
            binding.circleLoading.startLoadingAnimation()
        }
        binding.buttonCircleStop.setOnClickListener {
            binding.circleLoading.stopLoadingAnimation()
        }
    }

    private fun setupDiamondProgress() {
        binding.diamondProgress
            .setProgress(64f)
            .setMaxProgress(100f)
            .setGradientColors(
                intArrayOf(
                    Color.rgb(239, 68, 68),
                    Color.rgb(245, 158, 11),
                    Color.rgb(16, 185, 129)
                )
            )
            .setDiamondDrawable(android.R.drawable.ic_menu_gallery)
            .setDiamondSize(20.dp.toFloat())
            .setDiamondTiltEnabled(true)
            .apply()

        binding.diamondArcProgress
            .setProgress(42f)
            .setArcMode(true)
            .setArcType(DiamondProgressBar.ArcType.ARC)
            .setArcHeight(24.dp.toFloat())
            .setDiamondDrawable(android.R.drawable.ic_menu_gallery)
            .setDiamondSize(18.dp.toFloat())
            .apply()

        binding.seekDiamondProgress.setOnSeekBarChangeListener(seekListener { progress ->
            binding.diamondProgress.setProgress(progress.toFloat())
            binding.diamondArcProgress.setProgress(progress.toFloat())
        })
    }

    private fun setupSwitch() {
        binding.demoSwitch.setOnCheckedChangeListener { _, checked ->
            binding.switchState.text = getString(
                if (checked) R.string.components_switch_on else R.string.components_switch_off
            )
        }
    }

    private fun seekListener(onProgress: (Int) -> Unit) =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) onProgress(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
