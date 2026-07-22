package com.azheng.viewutils

import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class ImageLoadConfigTest {

    @Test
    fun defaultsUseAutomaticDiskCache() {
        assertSame(DiskCacheStrategy.AUTOMATIC, ImageLoadConfig().cacheStrategy)
    }

    @Test
    fun rejectsInvalidShapeAndThumbnailValues() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageShape.Rounded(radius = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageLoadConfig(thumbnailMultiplier = 1f)
        }
    }

    @Test
    fun appliesScaleThenExtraTransformationsThenShape() {
        val transformations = ImageLoadConfig(
            shape = ImageShape.Circle,
            extraTransformations = listOf(CenterInside())
        ).buildTransformations()

        assertEquals(CenterCrop::class.java, transformations[0]::class.java)
        assertEquals(CenterInside::class.java, transformations[1]::class.java)
        assertEquals(CircleCrop::class.java, transformations[2]::class.java)
    }

    @Test
    fun copiesAndProtectsExtraTransformations() {
        val source = mutableListOf<Transformation<Bitmap>>(CenterInside())
        val config = ImageLoadConfig(extraTransformations = source)

        source.clear()

        assertEquals(1, config.extraTransformations.size)
        @Suppress("UNCHECKED_CAST")
        val exposed = config.extraTransformations as MutableList<Transformation<Bitmap>>
        assertThrows(UnsupportedOperationException::class.java) {
            exposed.clear()
        }
    }

    @Test
    fun placeholderDoesNotImplicitlyConfigureFailureResources() {
        val config = ImageLoadConfig(placeholderRes = 1)

        assertNull(config.errorRes)
        assertNull(config.fallbackRes)
    }
}
