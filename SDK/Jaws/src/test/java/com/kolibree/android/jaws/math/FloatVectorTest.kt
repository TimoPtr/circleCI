/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.math

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.math.FloatVector.Companion.X_AXIS
import com.kolibree.android.jaws.math.FloatVector.Companion.Y_AXIS
import com.kolibree.android.jaws.math.FloatVector.Companion.Z_AXIS
import org.junit.Assert.assertEquals
import org.junit.Test

/** [FloatVector] tests */
class FloatVectorTest : BaseUnitTest() {

    /*
    Constants
     */

    @Test
    fun `value of X_AXIS is 0`() {
        assertEquals(0, X_AXIS)
    }

    @Test
    fun `value of Y_AXIS is 1`() {
        assertEquals(1, Y_AXIS)
    }

    @Test
    fun `value of Z_AXIS is 2`() {
        assertEquals(2, Z_AXIS)
    }

    /*
    Constructor
     */

    @Test
    fun `vector is full of 0 after construction`() {
        val newVector = FloatVector()
        assertEquals(0f, newVector.x)
        assertEquals(0f, newVector.y)
        assertEquals(0f, newVector.z)
    }

    /*
    set
     */

    @Test
    fun `set copies values from source vector`() {
        val expectedX = 1.1f
        val expectedY = -2.4f
        val expectedZ = 1.5f
        val expectedValues = floatArrayOf(expectedX, expectedY, expectedZ)
        val newVector = FloatVector()
        newVector.set(expectedValues)

        assertEquals(expectedX, newVector.x)
        assertEquals(expectedY, newVector.y)
        assertEquals(expectedZ, newVector.z)
    }

    @Test
    fun `set copies values from parameters`() {
        val expectedX = 1.1f
        val expectedY = -2.4f
        val expectedZ = 1.5f

        val newVector = FloatVector()
        newVector.set(expectedX, expectedY, expectedZ)

        assertEquals(expectedX, newVector.x)
        assertEquals(expectedY, newVector.y)
        assertEquals(expectedZ, newVector.z)
    }

    /*
    offsetX
     */

    @Test
    fun `offsetX adds offset to X axis`() {
        val expectedX = 1.1f
        val expectedY = 2.4f
        val expectedZ = 1.5f
        val expectedOffset = -3f

        val newVector = FloatVector()
        newVector.set(expectedX, expectedY, expectedZ)
        newVector.offsetX(expectedOffset)

        assertEquals(expectedX + expectedOffset, newVector.x)
        assertEquals(expectedY, newVector.y)
        assertEquals(expectedZ, newVector.z)
    }

    /*
    offsetY
     */

    @Test
    fun `offsetY adds offset to Y axis`() {
        val expectedX = 1.1f
        val expectedY = 2.4f
        val expectedZ = 1.5f
        val expectedOffset = -3f

        val newVector = FloatVector()
        newVector.set(expectedX, expectedY, expectedZ)
        newVector.offsetY(expectedOffset)

        assertEquals(expectedX, newVector.x)
        assertEquals(expectedY + expectedOffset, newVector.y)
        assertEquals(expectedZ, newVector.z)
    }

    /*
    offsetZ
     */

    @Test
    fun `offsetZ adds offset to Z axis`() {
        val expectedX = 1.1f
        val expectedY = 2.4f
        val expectedZ = 1.5f
        val expectedOffset = -3f

        val newVector = FloatVector()
        newVector.set(expectedX, expectedY, expectedZ)
        newVector.offsetZ(expectedOffset)

        assertEquals(expectedX, newVector.x)
        assertEquals(expectedY, newVector.y)
        assertEquals(expectedZ + expectedOffset, newVector.z)
    }
}
