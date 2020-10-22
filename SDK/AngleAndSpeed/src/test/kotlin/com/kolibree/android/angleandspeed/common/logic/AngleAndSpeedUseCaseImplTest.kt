/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic

import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.kml.AnglesAndSpeedAppContext
import com.kolibree.kml.AnglesAndSpeedResult
import com.kolibree.kml.EulerAnglesDegrees
import com.kolibree.kml.KPIAggregate
import com.kolibree.kml.OptionalKPIAggregate
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.RawData
import com.kolibree.kml.SpeedKPI
import com.kolibree.kml.get
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AngleAndSpeedUseCaseImplTest : BaseUnitTest() {

    private val checkupCalculator = mock<CheckupCalculator>()
    private val appContext = mock<AnglesAndSpeedAppContext>()
    private val appContextProvider = mock<Provider<AnglesAndSpeedAppContext>>()

    private lateinit var useCase: AngleAndSpeedUseCaseImpl

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        whenever(appContextProvider.get()).thenReturn(appContext)
        useCase = spy(
            AngleAndSpeedUseCaseImpl(
                appContextProvider
            )
        )
    }

    @Test
    fun angleAndSpeedFlowable_doesnt_return_value_on_start() {
        useCase.angleAndSpeedFlowable.test().assertEmpty()
    }

    @Test
    fun `angleAndSpeedFlowable does not return value until prescribed zones are set and result available`() {
        val rawData = mock<RawData>()
        val rawSensorState = mock<RawSensorState>()
        val result = mockAngleResult()
        whenever(rawSensorState.convertToKmlRawData()).thenReturn(rawData)

        val testObserver = useCase.angleAndSpeedFlowable.test()

        // send data but prescribedsZones is null and no result
        useCase.onRawData(true, rawSensorState)

        testObserver.assertEmpty()

        whenever(appContext.addRawData(eq(rawData), eq(PauseStatus.Running), any())).thenReturn(false)

        useCase.prescribedZones = mock()

        // send data prescribedZones not null but no result
        useCase.onRawData(true, rawSensorState)

        testObserver.assertEmpty()

        whenever(appContext.addRawData(eq(rawData), eq(PauseStatus.Running), any())).thenReturn(true)
        whenever(appContext.lastResult).thenReturn(result)

        // send data should get value on processor
        useCase.onRawData(true, rawSensorState)

        testObserver.assertValueCount(1)
    }

    @Test
    fun `angleAndSpeedFlowable does not return value in pause even if result available`() {
        val rawData = mock<RawData>()
        val rawSensorState = mock<RawSensorState>()
        val result = mockAngleResult()
        whenever(rawSensorState.convertToKmlRawData()).thenReturn(rawData)
        whenever(appContext.addRawData(eq(rawData), eq(PauseStatus.Running), any())).thenReturn(true)
        whenever(appContext.lastResult).thenReturn(result)

        val testObserver = useCase.angleAndSpeedFlowable.test()

        // send data should get value on processor
        useCase.onRawData(false, rawSensorState)

        testObserver.assertNoValues()
    }

    @Test
    fun `fromKml eulerAnglesDegress map to AngleFeedback`() {
        val angleResult = mockAngleResult()
        val result = useCase.fromKml(angleResult.orientationDegrees)
        assertEquals(0.0f, result.roll)
        assertEquals(1.0f, result.yaw)
        assertEquals(2.0f, result.pitch)
    }

    @Test
    fun `fromKml Corecct speedKpi map to CORRECT SpeedFeedback`() {
        val result = useCase.fromKml(SpeedKPI.Correct)
        assertEquals(SpeedFeedback.CORRECT, result)
    }

    @Test
    fun `fromKml Overspeed speedKpi map to Overspeed SpeedFeedback`() {
        val result = useCase.fromKml(SpeedKPI.Overspeed)
        assertEquals(SpeedFeedback.OVERSPEED, result)
    }

    @Test
    fun `fromKml Underspeed speedKpi map to Underspeed SpeedFeedback`() {
        val result = useCase.fromKml(SpeedKPI.Underspeed)
        assertEquals(SpeedFeedback.UNDERSPEED, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromKml null throw`() {
        useCase.fromKml(null)
    }

    private fun mockAngleResult(): AnglesAndSpeedResult {
        val result = mock<AnglesAndSpeedResult>()
        val angles = mock<EulerAnglesDegrees>()
        val optionalKPI = mock<OptionalKPIAggregate>()
        val kpi = mock<KPIAggregate>()

        whenever(result.orientationDegrees).thenReturn(angles)
        whenever(result.optionalKpi).thenReturn(optionalKPI)
        whenever(angles.roll).thenReturn(0.0f)
        whenever(angles.yaw).thenReturn(1.0f)
        whenever(angles.pitch).thenReturn(2.0f)
        whenever(optionalKPI.first).thenReturn(true)
        whenever(optionalKPI.second).thenReturn(kpi)
        whenever(kpi.speedCorrectness).thenReturn(SpeedKPI.Correct)
        whenever(kpi.isOrientationCorrect).thenReturn(true)

        return result
    }
}
