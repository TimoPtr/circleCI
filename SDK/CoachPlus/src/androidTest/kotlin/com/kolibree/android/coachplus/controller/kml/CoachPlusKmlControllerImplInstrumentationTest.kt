/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller.kml

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl.Companion.DEFAULT_MAX_FAIL_TIME_MS
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl.Companion.SEQUENCE
import com.kolibree.android.coachplus.logic.test.R
import com.kolibree.android.commons.RAW_DATA_WINDOW_SIZE
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.math.Axis
import com.kolibree.android.sdk.math.Vector
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.randomUnsigned8
import com.kolibree.android.test.utils.randomUnsignedSigned16
import com.kolibree.kml.Kml
import com.kolibree.kml.RawData
import com.kolibree.kml.ShortVector
import com.kolibree.kml.SupervisedBrushingAppContext16
import io.mockk.mockk
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertFalse
import org.junit.Test
import org.threeten.bp.Duration

internal class CoachPlusKmlControllerImplInstrumentationTest : BaseInstrumentationTest() {
    init {
        Kml.init()
    }

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    /*
    ON RAW DATA
    */
    @Test
    fun onRawData_dont_crash() {
        val controller = createCoachPlusKmlController()
        val rawSensorState: RawSensorState = createRawSensorState()

        controller.onRawData(true, rawSensorState)
    }

    /*
    onPlaqlessRawData
    */
    @Test
    fun onPlaqlessRawData_windowIsFull_setsLastSupervisedResult16() {
        val controller = createCoachPlusKmlController()
        val sensorState = PlaqlessRawSensorState(
            453L,
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8()
        )

        assertNull(controller.lastSupervisedResult16.get())

        repeat(RAW_DATA_WINDOW_SIZE + 1) {
            controller.onPlaqlessRawData(true, sensorState)
        }

        val lastSupervisedResult16 = controller.lastSupervisedResult16.get()
        assertNotNull(lastSupervisedResult16)
    }

    /*
    onRawData
    */
    @Test
    fun onRawData_windowIsFull_setsLastSupervisedResult16() {
        val controller = createCoachPlusKmlController()
        val rawData = createKmlRawData()

        assertNull(controller.lastSupervisedResult16.get())

        repeat(RAW_DATA_WINDOW_SIZE + 1) {
            controller.onRawData(true, rawData)
        }

        val lastSupervisedResult16 = controller.lastSupervisedResult16.get()
        assertNotNull(lastSupervisedResult16)
    }

    /*
    reset
     */
    @Test
    fun reset_resetsValues() {
        val controller = createCoachPlusKmlController()
        val rawData: RawSensorState = createRawSensorState()

        controller.onRawData(true, rawData)

        for (i in SEQUENCE.indices) {
            controller.zonePasses[i] = i * 100L
            controller.failTimes[i] = i * 50L
        }

        controller.reset()

        for (i in SEQUENCE.indices) {
            assertEquals(0, controller.zonePasses[i])
            assertEquals(0, controller.failTimes[i])
        }

        assertEquals(0, controller.computeBrushingDuration())

        assertNull(controller.lastSupervisedResult16.get())
    }

    /*
    ADD Plaqless DATA
    */
    @Test
    fun onPlaqlessData_dont_crash_update_wrongHandlePosition() {
        val controller = createCoachPlusKmlController()
        val plaqlessData = createPlaqlessData()
        val plaqlessDataWithWrongHandle = createPlaqlessData(PlaqlessError.WRONG_HANDLE)

        controller.onPlaqlessData(true, plaqlessData)
        assertEquals(PlaqlessError.NONE, controller.plaqlessError.get())

        controller.onPlaqlessData(true, plaqlessDataWithWrongHandle)
        assertEquals(PlaqlessError.WRONG_HANDLE, controller.plaqlessError.get())
    }

    /*
    onOverpressureState
     */

    @Test
    fun onOverpressureState_updatesKmlWithExpectedValues() {
        val controller = createCoachPlusKmlController()

        val expectedDetectorActive = true
        val expectedUiNotificationActive = true

        controller.onOverpressureState(
            OverpressureState(expectedDetectorActive, expectedUiNotificationActive)
        )

        val rawData = createKmlRawData()

        repeat(RAW_DATA_WINDOW_SIZE + 1) {
            controller.onRawData(true, rawData)
        }

        val lastSupervisedResult16 = controller.lastSupervisedResult16.get()
        assertFalse(lastSupervisedResult16.optionalKpi.second.isPressureCorrect)
    }

    private fun createCoachPlusKmlController(
        brushingDuration: Duration = Duration.ofSeconds(1),
        tickPeriod: Long = 1L
    ): CoachPlusKmlControllerImpl {

        return CoachPlusKmlControllerImpl(
            maxFailTime = DEFAULT_MAX_FAIL_TIME_MS,
            goalBrushingDuration = brushingDuration,
            tickPeriod = tickPeriod,
            checkupCalculator = mockk(),
            supervisedBrushingAppContextProvider = createSupervisedBrushingAppContext16Provider(),
            coachPlusFeedbackMapper = mockk(relaxUnitFun = true),
            durationAdjuster = mockk()
        )
    }

    val rnnWeightProvider: RnnWeightProvider = object : RnnWeightProvider {
        override fun getRnnWeight(): ShortVector {
            val shortVector = ShortVector()
            val bytes = context().resources.openRawResource(R.raw.gru_data_cm1_3_0_3)
                .use { it.readBytes() }
            shortVector.addAll(bytes.map(Byte::toShort).toList())
            return shortVector
        }
    }

    val angleProvider = object : AngleProvider {
        override fun getSupervisedAngle(): String =
            String(
                context().resources.openRawResource(R.raw.coach_angles_1_3_0)
                    .use { it.readBytes() })

        override fun getKPIAngle(): String =
            String(
                context().resources.openRawResource(R.raw.kpi_angles_1_0_0).use { it.readBytes() })
    }

    val transitionProvider = object : TransitionProvider {
        override fun getTransition(): String =
            String(
                context().resources.openRawResource(R.raw.transitions_1_4_0).use { it.readBytes() })
    }

    val kpiSpeedProvider = object : KpiSpeedProvider {
        override fun getKpiSpeed(): String =
            String(
                context().resources.openRawResource(R.raw.kpi_speed_ranges_1_0_0)
                    .use { it.readBytes() })
    }

    val thresholdProvider = object : ThresholdProvider {
        override fun getThresholdBalancing(): String =
            String(
                context().resources.openRawResource(R.raw.threshold_balancing_1)
                    .use { it.readBytes() })
    }

    val zoneValidatorProvider = object : ZoneValidatorProvider {
        override fun getZoneValidator(): String =
            String(
                context().resources.openRawResource(R.raw.zone_validator_1_3_0)
                    .use { it.readBytes() })
    }

    private var lastEmittedSupervisedBrushingAppContext16: SupervisedBrushingAppContext16? =
        null

    private fun supervisedBrushingAppContextHelper(): SupervisedBrushingAppContext16 =
        SupervisedBrushingAppContext16(
            rnnWeightProvider.getRnnWeight(),
            angleProvider.getSupervisedAngle(),
            angleProvider.getKPIAngle(),
            kpiSpeedProvider.getKpiSpeed(),
            transitionProvider.getTransition(),
            thresholdProvider.getThresholdBalancing(),
            zoneValidatorProvider.getZoneValidator(),
            false
        )

    private fun createSupervisedBrushingAppContext16Provider(): Provider<SupervisedBrushingAppContext16> {
        return Provider {
            supervisedBrushingAppContextHelper().apply {
                lastEmittedSupervisedBrushingAppContext16 = this
            }
        }
    }

    private fun createRawSensorState(): RawSensorState {
        return RawSensorState(
            0f,
            Vector(0f, 0f, 0f),
            Vector(0f, 0f, 0f),
            Vector(0f, 0f, 0f)
        )
    }

    private fun createKmlRawData(
        accelVector: Vector = Vector(0f, 0f, 0f),
        gyroVector: Vector = Vector(0f, 0f, 0f)
    ): RawData =
        RawData(
            0,
            gyroVector[Axis.X],
            gyroVector[Axis.Y],
            gyroVector[Axis.Z],
            accelVector[Axis.X],
            accelVector[Axis.Y],
            accelVector[Axis.Z]
        )

    private fun createPlaqlessData(error: PlaqlessError = PlaqlessError.NONE) = PlaqlessSensorState(
        0L,
        randomUnsignedSigned16(),
        randomUnsignedSigned16(),
        randomUnsignedSigned16(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        error
    )
}
