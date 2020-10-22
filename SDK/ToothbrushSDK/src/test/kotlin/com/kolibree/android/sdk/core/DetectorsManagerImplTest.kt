package com.kolibree.android.sdk.core

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.core.DetectorsManagerImpl.NOTIFY_DELAY_MILLIS
import com.kolibree.android.sdk.core.driver.SensorDriver
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mock

class DetectorsManagerImplTest : BaseUnitTest() {

    @Mock
    internal lateinit var driver: SensorDriver

    @Mock
    lateinit var notifyHandler: Handler

    private lateinit var detectorsManager: DetectorsManagerImpl

    override fun setup() {
        super.setup()

        init()
    }

    @Test
    fun notifyDriver_queuesNotifyDriverRunnableWithSpecifiedDelay() {
        spy()

        val expectedDelay = 65L
        whenever(detectorsManager.notifyDelay).thenReturn(expectedDelay)
        whenever(notifyHandler.postDelayed(any(), any())).thenReturn(true)

        detectorsManager.notifyDriver()

        verify(notifyHandler).postDelayed(
            any<DetectorsManagerImpl.NotifyDriverRunnable>(),
            eq(expectedDelay)
        )
    }

    /*
    GET NOTIFY DELAY
     */

    @Test
    fun getNotifyDelay_noTaskPreviouslyScheduled_returns0() {
        assertEquals(0, detectorsManager.notifyDelay)
    }

    @Test
    fun getNotifyDelay_lastTaskScheduledBeforeThreshold_returnsThresholdDelay() {
        val currentTime = System.currentTimeMillis()

        detectorsManager.lastNotificationTimestamp = currentTime - 1

        assertEquals(NOTIFY_DELAY_MILLIS.toLong(), detectorsManager.notifyDelay)
    }

    @Test
    fun getNotifyDelay_lastTaskScheduledOverThreshold_returns0() {
        val currentTime = System.currentTimeMillis()

        detectorsManager.lastNotificationTimestamp = currentTime - NOTIFY_DELAY_MILLIS - 1

        assertEquals(0, detectorsManager.notifyDelay)
    }

    /*
    plaqlessRawDataNotifications
     */
    @Test
    fun `plaqlessRawDataNotifications returns Flowable from driver`() {
        val expectedFlowable: Flowable<PlaqlessRawSensorState> = Flowable.empty()
        whenever(driver.plaqlessRawDataNotifications()).thenReturn(expectedFlowable)

        assertEquals(expectedFlowable, detectorsManager.plaqlessRawDataNotifications())
    }

    /*
    plaqlessNotifications
     */
    @Test
    fun `plaqlessNotifications returns Flowable from driver`() {
        val expectedFlowable: Flowable<PlaqlessSensorState> = Flowable.empty()
        whenever(driver.plaqlessNotifications()).thenReturn(expectedFlowable)

        assertEquals(expectedFlowable, detectorsManager.plaqlessNotifications())
    }

    /*
    plaqlessRingLedState
     */
    @Test
    fun `plaqlessRingLedState returns Flowable from driver`() {
        val expectedFlowable: Flowable<PlaqlessRingLedState> = Flowable.empty()
        whenever(driver.plaqlessRingLedState()).thenReturn(expectedFlowable)

        assertEquals(expectedFlowable, detectorsManager.plaqlessRingLedState())
    }

    /*
    enableOverpressureDetector
     */

    @Test
    fun `enableOverpressureDetector invokes driver with expected parameter`() {
        val expectedParameter = true
        whenever(driver.enableOverpressureDetector(any())).thenReturn(Completable.complete())

        detectorsManager.enableOverpressureDetector(expectedParameter).test()
            .assertComplete()
            .assertNoErrors()

        verify(driver).enableOverpressureDetector(expectedParameter)
    }

    /*
    isOverpressureDetectorEnabled
     */

    @Test
    fun `isOverpressureDetectorEnabled invokes driver with expected parameter`() {
        val expectedResult = true
        whenever(driver.isOverpressureDetectorEnabled()).thenReturn(Single.just(expectedResult))

        detectorsManager.isOverpressureDetectorEnabled().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(expectedResult)

        verify(driver).isOverpressureDetectorEnabled()
    }

    /*
    enablePickupDetector
     */

    @Test
    fun `enablePickupDetector invokes driver with expected parameter`() {
        val expectedParameter = true
        whenever(driver.enablePickupDetector(any())).thenReturn(Completable.complete())

        detectorsManager.enablePickupDetector(expectedParameter).test()
            .assertComplete()
            .assertNoErrors()

        verify(driver).enablePickupDetector(expectedParameter)
    }

    /*
    UTILS
     */

    private fun init(toothbrushModel: ToothbrushModel = ToothbrushModel.CONNECT_M1) {
        detectorsManager = DetectorsManagerImpl(toothbrushModel, driver, notifyHandler)
    }

    private fun spy() {
        detectorsManager = spy(detectorsManager)
    }
}
