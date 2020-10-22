package com.kolibree.android.coachplus.controller

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl.Companion.KOLIBREE_LEGACY_SEQUENCE_ID
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl.Companion.SEQUENCE
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.MouthZone16.LoIncExt
import com.kolibree.kml.MouthZone16.LoIncInt
import com.kolibree.kml.MouthZone16.LoMolLeExt
import com.kolibree.kml.MouthZone16.LoMolLeInt
import com.kolibree.kml.MouthZone16.LoMolLeOcc
import com.kolibree.kml.MouthZone16.LoMolRiExt
import com.kolibree.kml.MouthZone16.LoMolRiInt
import com.kolibree.kml.MouthZone16.LoMolRiOcc
import com.kolibree.kml.MouthZone16.UpIncExt
import com.kolibree.kml.MouthZone16.UpIncInt
import com.kolibree.kml.MouthZone16.UpMolLeExt
import com.kolibree.kml.MouthZone16.UpMolLeInt
import com.kolibree.kml.MouthZone16.UpMolLeOcc
import com.kolibree.kml.MouthZone16.UpMolRiExt
import com.kolibree.kml.MouthZone16.UpMolRiInt
import com.kolibree.kml.MouthZone16.UpMolRiOcc
import com.kolibree.sdkws.data.model.CreateBrushingData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Duration

/**
 * [BaseCoachPlusControllerImpl] tests
 */
class BaseCoachPlusControllerImplTest {

    @Test
    fun `KOLIBREE_LEGACY_SEQUENCE_ID equals 0`() {
        assertEquals(0.toByte(), KOLIBREE_LEGACY_SEQUENCE_ID)
    }

    @Test
    fun `zoneChangePublishSubject is initialized with the first value of the SEQUENCE`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.zoneChangeObservable.test().assertValue(SupervisionInfo(SEQUENCE[0], KOLIBREE_LEGACY_SEQUENCE_ID))
    }

    @Test
    fun `getSequenceLength returns correct value`() {
        val controller = BaseCoachPlusControllerImplStub()
        assertEquals(16, controller.getSequenceLength())
    }

    @Test
    fun `getCurrentZone sequence is respected`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.currentZoneIndex = 0
        assertEquals(UpMolLeExt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoMolLeExt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpMolRiExt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoMolRiExt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpIncExt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoIncExt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpMolLeOcc, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpMolLeInt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoMolLeInt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoMolLeOcc, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpMolRiOcc, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpMolRiInt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoMolRiInt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoMolRiOcc, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(UpIncInt, controller.getCurrentZone())
        controller.currentZoneIndex = controller.currentZoneIndex + 1
        assertEquals(LoIncInt, controller.getCurrentZone())
    }

    @Test
    fun `reset resets currentZoneIndex`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.currentZoneIndex = 10
        controller.reset()
        assertEquals(0, controller.currentZoneIndex)
    }

    @Test
    fun `brushNextZone increases CurrentZoneIndexAndEmitsZoneChange`() {
        val controller = BaseCoachPlusControllerImplStub()
        val testObserver = controller.zoneChangeObservable.test()
        controller.currentZoneIndex = 8
        controller.brushNextZone()
        assertEquals(9, controller.currentZoneIndex)
        testObserver.assertValues(
            SupervisionInfo(SEQUENCE[0], KOLIBREE_LEGACY_SEQUENCE_ID),
            SupervisionInfo(LoMolLeOcc, KOLIBREE_LEGACY_SEQUENCE_ID)
        )
    }

    @Test
    fun `hasMoreZone true`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.currentZoneIndex = 14
        assertTrue(controller.hasMoreZones())
    }

    @Test
    fun `hasMoreZone false`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.currentZoneIndex = 15
        assertFalse(controller.hasMoreZones())
    }

    @Test
    fun `subscribe to zoneChangeObservable emit the last value`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.currentZoneIndex = 8
        controller.brushNextZone()
        val testObserver = controller.zoneChangeObservable.test()
        testObserver.assertValue(SupervisionInfo(LoMolLeOcc, KOLIBREE_LEGACY_SEQUENCE_ID))
    }

    @Test
    fun `getCurrentSupervisionInfo returns current zone`() {
        val controller = BaseCoachPlusControllerImplStub()
        controller.currentZoneIndex = 8

        assertEquals(
            SupervisionInfo(SEQUENCE[8], KOLIBREE_LEGACY_SEQUENCE_ID),
            controller.getCurrentSupervisionInfo()
        )
    }

    private class BaseCoachPlusControllerImplStub : BaseCoachPlusControllerImpl(Duration.ofSeconds(1)) {
        override fun onPause() {
            // no-op
        }

        override fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState) {
            // no-op
        }

        override fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState) {
            // no-op
        }

        override fun onRawData(isPlaying: Boolean, sensorState: RawSensorState) {
            // no-op
        }

        override fun onOverpressureState(overpressureState: OverpressureState) {
            // no-op
        }

        override fun onTick(): CoachPlusControllerResult =
            CoachPlusControllerResult(LoMolLeOcc, 0, true, true, FeedBackMessage.EmptyFeedback)

        override fun onSvmData(possibleZones: List<MouthZone16>) {
        }

        override fun createBrushingData(): CreateBrushingData =
            CreateBrushingData("", 0L, 0, TrustedClock.getNowOffsetDateTime(), 0)

        override fun computeBrushingDuration(): Int = 0

        override fun getAvroTransitionsTable(): IntArray = intArrayOf()

        override fun notifyReconnection() {
            // no-op
        }
    }
}
