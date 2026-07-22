package com.azheng.viewutils

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImageUtilityTest {

    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Test
    fun imageLoadRegistrationIsOwnedByTheView() {
        val imageView = ImageView(context)

        ViewToImageUtils.registerImageViewLoadParam(imageView, "https://example.com/image.png")

        assertNotNull(imageView.getTag(R.id.avu_image_load_param))
    }

    @Test
    fun bitmapSaveRejectsInvalidQuality() = runBlocking {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        try {
            val result = BitmapSaveUtils.saveBitmapToInternalStorage(
                context = context,
                bitmap = bitmap,
                quality = 101
            )

            assertTrue(result.isFailure)
        } finally {
            bitmap.recycle()
        }
    }
}
