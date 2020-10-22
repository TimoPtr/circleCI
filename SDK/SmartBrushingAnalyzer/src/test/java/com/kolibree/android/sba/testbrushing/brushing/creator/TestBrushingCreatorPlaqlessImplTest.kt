/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.brushing.creator

import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.bi.Contract
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.processedbrushings.exception.ProcessedBrushingNotAvailableException
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.detectors.DetectorsManager
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.kml.BrushingSession
import com.kolibree.kml.CharVector
import com.kolibree.kml.FreeBrushingAppContext
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Test
import org.threeten.bp.Duration

class TestBrushingCreatorPlaqlessImplTest : BaseUnitTest() {

    private val checkupCalculator = mock<CheckupCalculator>()
    private val connector = mock<IKolibreeConnector>()
    private val rnnWeightProvider = mock<RnnWeightProvider>()
    private val angleProvider = mock<AngleProvider>()
    private val kpiSpeedProvider = mock<KpiSpeedProvider>()
    private val transitionProvider = mock<TransitionProvider>()
    private val thresholdProvider = mock<ThresholdProvider>()
    private val zoneValidatorProvider = mock<ZoneValidatorProvider>()
    private val appContext = mock<FreeBrushingAppContext>()
    private val avroCreator = mock<KmlAvroCreator>()

    private lateinit var brushingCreator: TestBrushingCreatorPlaqless

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
        val appVersions = mock<KolibreeAppVersions>()

        whenever(appVersions.appVersion).thenReturn(APP_VERSION)
        whenever(appVersions.buildVersion).thenReturn(BUILD_VERSION)

        brushingCreator = spy(
            TestBrushingCreatorPlaqless(
                checkupCalculator = checkupCalculator,
                connector = connector,
                rnnWeightProvider = rnnWeightProvider,
                angleProvider = angleProvider,
                kpiSpeedProvider = kpiSpeedProvider,
                transitionProvider = transitionProvider,
                thresholdProvider = thresholdProvider,
                zoneValidatorProvider = zoneValidatorProvider,
                appVersions = appVersions,
                appContext = appContext,
                avroCreator = avroCreator,
                toothbrushModel = ToothbrushModel.CONNECT_E2
            )
        )
    }

    @Test
    fun `create invokes CreateBrushing and clear subscription and avroCreator if isFullBrushingProcessingPossible return true`() {
        val profile = mock<Profile>()
        whenever(connector.currentProfile).thenReturn(profile)
        val subscripption = Single.never<Boolean>().subscribe({}, {})
        brushingCreator.disposables += subscripption

        val connection = mock<KLTBConnection>()
        val brushingSession = mock<BrushingSession>()
        val avroData = mock<CharVector>()
        val processedBrushing16 = mock<ProcessedBrushing16>()
        val processedBrushing = mock<ProcessedBrushing>()
        val processedData = "processed data"

        whenever(avroCreator.createBrushingSession(connection, Contract.ActivityName.FREE_BRUSHING)).thenReturn(Single.just(
            brushingSession))
        whenever(avroCreator.submitAvroData(avroData)).thenReturn(Completable.complete())

        whenever(appContext.isFullBrushingProcessingPossible).thenReturn(true)
        whenever(appContext.processFullBrushing()).thenReturn(processedBrushing16)
        whenever(processedBrushing16.toProcessedBrushing()).thenReturn(processedBrushing)
        whenever(processedBrushing.toJSON()).thenReturn(processedData)
        whenever(appContext.getAvro(brushingSession)).thenReturn(avroData)

        val checkupData = mock<CheckupData>()
        whenever(checkupCalculator.calculateCheckup(any<ProcessedBrushing>())).thenReturn(checkupData)

        val brushingData = mock<CreateBrushingData>()
        doReturn(brushingData).whenever(brushingCreator).createBrushingData(any(), any(), eq(processedData))

        brushingCreator.create(connection)

        verify(brushingCreator).createBrushing(profile, brushingData)
        verify(avroCreator).createBrushingSession(connection, Contract.ActivityName.FREE_BRUSHING)
        verify(avroCreator).submitAvroData(avroData)

        assertTrue(subscripption.isDisposed)
    }

    @Test
    fun `create clear subscription and throw when if isFullBrushingProcessingPossible return false`() {
        val connection = mock<KLTBConnection>()
        val subscripption = Single.never<Boolean>().subscribe({}, {})
        brushingCreator.disposables += subscripption
        whenever(appContext.isFullBrushingProcessingPossible).thenReturn(false)

        try {
            brushingCreator.create(connection)
            fail()
        } catch (e: ProcessedBrushingNotAvailableException) {
            assertTrue(subscripption.isDisposed)
        }
    }

    @Test
    fun `pause put isPlaying to false and invokes monitorCurrentBrushing`() {
        val connection = mock<KLTBConnection>()
        doNothing().whenever(brushingCreator).monitorCurrentBrushing(connection)

        brushingCreator.pause(connection)

        verify(brushingCreator).monitorCurrentBrushing(connection)
        assertFalse(brushingCreator.isPlaying.get())
    }

    @Test
    fun `start subscribe to plaqlessNotification and plaqlessRawDataNotification and put isPlaying to true`() {
        val connection = mock<KLTBConnection>()
        val detectorsManager = mock<DetectorsManager>()
        whenever(detectorsManager.plaqlessNotifications()).thenReturn(Flowable.never())
        whenever(detectorsManager.plaqlessRawDataNotifications()).thenReturn(Flowable.never())
        doNothing().whenever(appContext).start()
        whenever(connection.detectors()).thenReturn(detectorsManager)

        brushingCreator.start(connection)

        verify(detectorsManager).plaqlessNotifications()
        verify(detectorsManager).plaqlessRawDataNotifications()
        verify(appContext).start()
        assertTrue(brushingCreator.isPlaying.get())
    }

    @Test
    fun `resume put isPlaying to true and invokes monitorCurrentBrushing`() {
        val connection = mock<KLTBConnection>()
        doNothing().whenever(brushingCreator).monitorCurrentBrushing(connection)

        brushingCreator.resume(connection)

        verify(brushingCreator).monitorCurrentBrushing(connection)
        assertTrue(brushingCreator.isPlaying.get())
    }

    @Test
    fun `onDestroy clear disposable`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val testObserver = Observable.never<Boolean>().test()
        brushingCreator.disposables.add(testObserver)
        brushingCreator.onDestroy(lifecycleOwner)

        assertEquals(0, brushingCreator.disposables.size())
        assertTrue(testObserver.isDisposed)
    }

    @Test
    fun `notifyReconnection invokes appContext notifyReconnection`() {
        brushingCreator.notifyReconnection()

        verify(appContext).notifyReconnection()
    }

    @Test
    fun `createBrushing invokes connector CreateBrushing`() {
        val profileId = 123L
        val profile = mock<Profile>()
        whenever(profile.id).thenReturn(profileId)
        val brushingData = mock<CreateBrushingData>()
        val profileWrapper = mock<ProfileWrapper>()
        whenever(connector.withProfileId(profileId)).thenReturn(profileWrapper)
        doNothing().whenever(profileWrapper).createBrushing(brushingData)

        brushingCreator.createBrushing(profile, brushingData)

        verify(connector).withProfileId(profileId)
        verify(profileWrapper).createBrushing(brushingData)
    }

    @Test
    fun `createBrushingData invokes addProcessedData addSupportData`() {
        val connection = mock<KLTBConnection>()
        val checkupData = mock<CheckupData>()
        val surface = 25
        val tbSerial = "SU:PE:R0:SE:R1:AL"
        val tbMac = "mA:c0:ad:DR:re:55"
        val toothbrush = mock<Toothbrush>()
        val processedData = "{}"
        whenever(toothbrush.mac).thenReturn(tbMac)
        whenever(toothbrush.serialNumber).thenReturn(tbSerial)
        whenever(connection.toothbrush()).thenReturn(toothbrush)
        whenever(checkupData.surfacePercentage).thenReturn(surface)
        val brushingData = mock<CreateBrushingData>()
        doReturn(brushingData).whenever(brushingCreator).initBrushingData(checkupData)

        brushingCreator.createBrushingData(connection, checkupData, processedData)

        verify(brushingData).coverage = surface
        verify(brushingData).setProcessedData(processedData)
        verify(brushingData).addSupportData(
            tbSerial, tbMac,
            APP_VERSION,
            BUILD_VERSION
        )
    }

    @Test
    fun `initBrushing return valid CreateBrushingData`() {
        val checkupData = mock<CheckupData>()
        val duration = Duration.ofMillis(1000)
        val startDate = TrustedClock.getNowOffsetDateTime()

        whenever(checkupData.duration).thenReturn(duration)
        whenever(checkupData.dateTime).thenReturn(startDate)
        whenever(checkupData.surfacePercentage).thenReturn(0)

        val result = brushingCreator.initBrushingData(checkupData)

        assertEquals(GameApiConstants.GAME_SBA, result.game)
        assertEquals(duration, result.durationObject)
        assertEquals(DEFAULT_BRUSHING_GOAL, result.goalDuration)
        assertEquals(0, result.coins)
    }

    @Test
    fun `monitorCurrentBrushing invokes BrushingMonitorCurrents`() {
        val connection = mock<KLTBConnection>()
        val brushing = mock<Brushing>()
        whenever(connection.brushing()).thenReturn(brushing)
        doReturn(Completable.complete()).whenever(brushing).monitorCurrent()

        brushingCreator.monitorCurrentBrushing(connection)

        verify(connection).brushing()
        verify(brushing).monitorCurrent()
    }

    @Test
    fun `onPlaqlessSensorState invokes addPlaqlessData with PauseStatus InPause when isPlaying false`() {
        val plaqlesSensorState = mock<PlaqlessSensorState>()

        whenever(plaqlesSensorState.relativeTimestampMillis).thenReturn(0)
        brushingCreator.onPlaqlessSensorState(plaqlesSensorState)

        verify(brushingCreator.appContext).addPlaqlessData(
            anyOrNull(),
            eq(PauseStatus.InPause)
        )
    }

    @Test
    fun `onPlaqlessSensorState invokes addPlaqlessData with PauseStatus Running when is Playing true`() {
        val plaqlesSensorState = mock<PlaqlessSensorState>()

        whenever(plaqlesSensorState.relativeTimestampMillis).thenReturn(0)
        brushingCreator.isPlaying.set(true)
        brushingCreator.onPlaqlessSensorState(plaqlesSensorState)

        verify(brushingCreator.appContext).addPlaqlessData(
            anyOrNull(),
            eq(PauseStatus.Running)
        )
    }

    @Test
    fun `onPlaqlessRawSensorState invokes addRawData with PauseStatus InPause when isPlaying false`() {
        val plaqlesSensorState = mock<PlaqlessRawSensorState>()

        whenever(plaqlesSensorState.relativeTimestampMillis).thenReturn(0)
        brushingCreator.onPlaqlessRawSensorState(plaqlesSensorState)

        verify(brushingCreator.appContext).addRawData(
            anyOrNull(),
            eq(PauseStatus.InPause)
        )
    }

    @Test
    fun `onPlaqlessRawSensorState invokes addRawData with PauseStatus Running when isPlaying true`() {
        val plaqlesSensorState = mock<PlaqlessRawSensorState>()

        whenever(plaqlesSensorState.relativeTimestampMillis).thenReturn(0)
        brushingCreator.isPlaying.set(true)
        brushingCreator.onPlaqlessRawSensorState(plaqlesSensorState)

        verify(brushingCreator.appContext).addRawData(
            anyOrNull(),
            eq(PauseStatus.Running)
        )
    }

    companion object {
        const val APP_VERSION = "v1.2.3-android"
        const val BUILD_VERSION = "4.0.3_456.909"
    }
}
