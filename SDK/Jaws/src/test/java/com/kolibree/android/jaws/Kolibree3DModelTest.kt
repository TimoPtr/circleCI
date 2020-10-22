package com.kolibree.android.jaws

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [Kolibree3DModel] tests
 */
class Kolibree3DModelTest {

    @Test
    fun `scaling factor of TOOTHBRUSH is 5,3f`() {
        assertEquals(5.3f, Kolibree3DModel.TOOTHBRUSH.scalingFactor)
    }

    @Test
    fun `scaling factor of PLAQLESS is 5,3f`() {
        assertEquals(5.3f, Kolibree3DModel.PLAQLESS.scalingFactor)
    }

    @Test
    fun `scaling factor of UPPER_JAW is 1,3f`() {
        assertEquals(1.3f, Kolibree3DModel.UPPER_JAW.scalingFactor)
    }

    @Test
    fun `scaling factor of LOWER_JAW is 1f`() {
        assertEquals(1f, Kolibree3DModel.LOWER_JAW.scalingFactor)
    }

    @Test
    fun `scaling factor of HUM_UPPER_JAW is 1,3f`() {
        assertEquals(1.3f, Kolibree3DModel.HUM_UPPER_JAW.scalingFactor)
    }

    @Test
    fun `scaling factor of HUM_LOWER_JAW is 1,3f`() {
        assertEquals(1.3f, Kolibree3DModel.HUM_LOWER_JAW.scalingFactor)
    }
}
