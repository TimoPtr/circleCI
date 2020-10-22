/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.gyroscopic

import android.view.animation.DecelerateInterpolator
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopicJawsTiltControllerImpl.Companion.MAX_ANGLE_RADIANS
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopicJawsTiltControllerImpl.Companion.ROLL_FACTOR
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** [GyroscopicJawsTiltControllerImpl] tests */
class GyroscopicJawsTiltControllerImplTest : BaseUnitTest() {

    private val gyroscopeSensorInteractor = mock<GyroscopeSensorInteractor>()

    private val interpolator = mock<DecelerateInterpolator>()

    private lateinit var controller: GyroscopicJawsTiltControllerImpl

    @Before
    fun before() {
        controller = spy(
            GyroscopicJawsTiltControllerImpl(
                gyroscopeSensorInteractor,
                interpolator
            )
        )
    }

    /*
    onPause
     */

    @Test
    fun `onPause invokes interactor's unregisterListener`() {
        controller.onPause()
        verify(gyroscopeSensorInteractor).unregisterListener()
    }

    /*
    onResume
     */

    @Test
    fun `onResume invokes interactor's registerListener`() {
        controller.onResume()
        verify(gyroscopeSensorInteractor).registerListener()
    }

    /*
    getJawsRotationY
     */

    @Test
    fun `getJawsRotationY gets roll, fixes it, interpolates it then multiplies it by ROLL_FACTOR`() {
        val roll = 2.95f
        val fixedValue = 0.3f
        val interpolatedValue = 0.4f

        whenever(gyroscopeSensorInteractor.roll()).thenReturn(roll)
        doReturn(fixedValue).whenever(controller).safeRoll(roll)
        doReturn(interpolatedValue).whenever(controller).smoothRoll(fixedValue)

        val rotationY = controller.getJawsRotationY()

        verify(gyroscopeSensorInteractor).roll()
        verify(controller).safeRoll(roll)
        verify(controller).smoothRoll(fixedValue)
        assertEquals(interpolatedValue * ROLL_FACTOR, rotationY)
    }

    /*
    smoothRoll
     */

    @Test
    fun `smoothRoll with positive roll returns interpolated value * MAX_ANGLE_RADIANS`() {
        val interpolatedValue = 0.3f
        whenever(interpolator.getInterpolation(any())).thenReturn(interpolatedValue)

        val smoothedRoll = controller.smoothRoll(0.1f)

        verify(interpolator).getInterpolation(any())
        assertEquals(interpolatedValue * MAX_ANGLE_RADIANS, smoothedRoll)
    }

    @Test
    fun `smoothRoll with negative roll returns -interpolated value * MAX_ANGLE_RADIANS`() {
        val interpolatedValue = 0.3f
        whenever(interpolator.getInterpolation(any())).thenReturn(interpolatedValue)

        val smoothedRoll = controller.smoothRoll(-0.1f)

        verify(interpolator).getInterpolation(any())
        assertEquals(-interpolatedValue * MAX_ANGLE_RADIANS, smoothedRoll)
    }

    /*
    getTranslationX
     */

    @Test
    fun `getTranslationX returns 0f`() {
        assertEquals(0f, controller.getTranslationX())
    }

    /*
    Constants
     */

    @Test
    fun `value of ROLL_FACTOR is 52f`() {
        assertEquals(52f, ROLL_FACTOR)
    }

    @Test
    fun `value of MAX_ANGLE_RADIANS is 0,42f`() {
        assertEquals(0.42f, MAX_ANGLE_RADIANS)
    }
}
