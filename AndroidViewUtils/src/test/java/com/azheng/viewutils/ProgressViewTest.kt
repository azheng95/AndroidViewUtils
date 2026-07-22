package com.azheng.viewutils

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProgressViewTest {

    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Test
    fun progressBarsClampInvalidMaximum() {
        val arc = ArcProgressBar(context).apply { maxProgress = 0f }
        val diamond = DiamondProgressBar(context).apply { maxProgress = -10f }

        assertEquals(1f, arc.maxProgress)
        assertEquals(1f, diamond.maxProgress)
    }

    @Test
    fun gradientsRequireAtLeastTwoColors() {
        val arc = ArcProgressBar(context)
        val diamond = DiamondProgressBar(context)

        assertThrows(IllegalArgumentException::class.java) {
            arc.setGradientColors(intArrayOf(0xFF000000.toInt()))
        }
        assertThrows(IllegalArgumentException::class.java) {
            diamond.setGradientColors(intArrayOf(0xFF000000.toInt()))
        }
    }

    @Test
    fun javaApiSeparatesPropertyAndFluentSetters() {
        val propertySetter = ArcProgressBar::class.java.getMethod(
            "setProgress",
            Float::class.javaPrimitiveType
        )
        val fluentSetter = ArcProgressBar::class.java.getMethod(
            "setProgressFluent",
            Float::class.javaPrimitiveType
        )

        assertEquals(Void.TYPE, propertySetter.returnType)
        assertEquals(ArcProgressBar::class.java, fluentSetter.returnType)
        assertNotNull(fluentSetter)
    }

    @Test
    fun dashedLineCanBeConstructed() {
        assertNotNull(DashedLineView(context))
    }
}
