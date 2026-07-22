package com.azheng.androidviewutils.demo.text

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.azheng.androidviewutils.demo.R
import com.azheng.androidviewutils.demo.databinding.ActivityTextDemoBinding
import com.azheng.viewutils.SpanMatchKeywordUtils
import com.azheng.viewutils.markwon.BlockImagePlugin
import com.azheng.viewutils.markwon.CustomTextSizePlugin
import com.azheng.viewutils.markwon.MatchParentImagePlugin
import io.noties.markwon.Markwon
import io.noties.markwon.image.glide.GlideImagesPlugin

private const val SAMPLE_TEXT = "AndroidViewUtils makes repeated Android UI work easier"

private val SAMPLE_MARKDOWN = """
    # AndroidViewUtils

    A compact Android utility library with **focused APIs** and optional dependencies.

    - Custom progress views
    - Image loading helpers
    - Lifecycle-aware animations

    `ImageLoadConfig` keeps image behavior explicit.

    ![Landscape](https://picsum.photos/id/1043/900/360)
""".trimIndent()

class TextDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.inputKeyword.doAfterTextChanged { renderKeyword(it?.toString().orEmpty()) }
        binding.inputKeyword.setText(R.string.text_keyword_default)
        renderMarkdown()
    }

    private fun renderKeyword(keyword: String) {
        binding.highlightResult.text = SpanMatchKeywordUtils.highlightFirstMatchKeyword(
            text = SAMPLE_TEXT,
            keyword = keyword,
            color = Color.rgb(220, 38, 38)
        )
    }

    private fun renderMarkdown() {
        val markwon = Markwon.builder(this)
            .usePlugin(GlideImagesPlugin.create(this))
            .usePlugin(BlockImagePlugin())
            .usePlugin(MatchParentImagePlugin())
            .usePlugin(
                CustomTextSizePlugin(
                    textSizeSp = 16f,
                    headingBreakHeight = 0,
                    context = this
                )
            )
            .build()
        markwon.setMarkdown(binding.markdownContent, SAMPLE_MARKDOWN)
    }
}
