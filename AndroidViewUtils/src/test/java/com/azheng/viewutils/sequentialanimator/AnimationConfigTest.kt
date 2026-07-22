package com.azheng.viewutils.sequentialanimator

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnimationConfigTest {

    @Test
    fun rejectsNegativeTimingValues() {
        assertThrows(IllegalArgumentException::class.java) {
            AnimationConfig(delayBetween = -1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AnimationConfig(duration = -1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AnimationConfig(startDelay = -1L)
        }
    }
}
