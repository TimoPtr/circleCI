/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.driver.DeviceDriver
import com.kolibree.android.sdk.core.driver.KLTBDriver
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.toothbrush.ToothbrushImplementation
import com.kolibree.android.sdk.error.ConnectionEstablishException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.Arrays
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean

/** Created by miguelaragues on 21/12/17.  */
class KLTBConnectionImplTest : BaseUnitTest() {

    private lateinit var connection: KLTBConnectionImpl

    private val driver: FakeDriver = mock()

    private val toothbrushImplementation: ToothbrushImplementation = mock()

    private val detectorsManager: DetectorsManagerImpl = mock()

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        whenever(toothbrushImplementation.model).thenReturn(ToothbrushModel.ARA)
        val mainHandler = mock<Handler>()

        whenever(mainHandler.post(any())).thenAnswer {
            it.getArgument<Runnable>(0).run()
            true
        }

        connection =
            spy(KLTBConnectionImpl(driver, toothbrushImplementation, detectorsManager, mainHandler))

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    /*
  ON DISCONNECTED
   */

    @Test
    fun onDisconnected_invokesSetTagNull() {
        connection.tag = Any()

        connection.onDisconnected()

        assertNull(connection.tag)
    }

    /*
  deviceParametersCharacteristicChangedStream
   */

    @Test
    fun `deviceParametersCharacteristicChangedStream returns deviceDriver deviceParametersCharacteristicChangedStream() when it's a BleDriver`() {
        val subject = PublishProcessor.create<ByteArray>()
        whenever(driver.deviceParametersCharacteristicChangedStream()).thenReturn(subject)

        connection.deviceParametersCharacteristicChangedStream().test()

        assertTrue(subject.hasSubscribers())
    }

    /*
  DISCONNECT
   */

    @Test
    fun disconnect_stateNotTerminatedOTAOrTerminating_setsStateToTERMINATINGBeforeInvokingDisconnect() {
        val ignoredStates = Arrays.asList(
            KLTBConnectionState.OTA,
            KLTBConnectionState.TERMINATED,
            KLTBConnectionState.TERMINATING
        )
        for (i in 0 until KLTBConnectionState.values().size) {
            val state = KLTBConnectionState.values()[i]

            if (ignoredStates.contains(state)) continue

            connection.setState(state)

            assertEquals(state, connection.state().current)

            connection.disconnect()

            assertEquals(KLTBConnectionState.TERMINATING, connection.state().current)
        }
    }

    @Test
    fun disconnect_stateTerminatedOTAOrTerminating_doesNotSetState() {
        val states = Arrays.asList(
            KLTBConnectionState.OTA,
            KLTBConnectionState.TERMINATED,
            KLTBConnectionState.TERMINATING
        )
        for (state in states) {
            connection.setState(state)

            assertEquals(state, connection.state().current)

            connection.disconnect()

            assertEquals(state, connection.state().current)
        }
    }

    @Test
    @Throws(FailureReason::class)
    fun disconnect_anyState_invokesDriverDisconnect() {
        for (i in 0 until KLTBConnectionState.values().size) {
            val state = KLTBConnectionState.values()[i]

            connection.setState(state)

            assertEquals(state, connection.state().current)

            connection.disconnect()

            verify(driver, times(i + 1)).disconnect()
        }
    }

    @Test
    fun disconnect_existingCompletableEmitter_emitsConnectionEstablishException() {
        val completableObserver = connection.establishCompletable().test()

        assertNotNull(connection.establishCompletable)
        assertNotNull(connection.establishCompletableEmitter)

        completableObserver.assertNoErrors()

        connection.disconnect()

        completableObserver.assertError(ConnectionEstablishException::class.java)

        assertNull(connection.establishCompletable)
        assertNull(connection.establishCompletableEmitter)
    }

    /*
  ESTABLISH OBSERVABLE
   */

    @Test
    fun establishObservable_returnsCompletable() {
        assertNotNull(connection.establishCompletable())
    }

    @Test
    fun establishObservable_subscribe_storesEmitter() {
        doNothing().whenever(connection).establish(anyBoolean())

        assertNull(connection.establishCompletableEmitter)

        connection.establishCompletable().test()

        assertNotNull(connection.establishCompletableEmitter)
    }

    @Test
    fun establishObservable_storesCompletable() {
        assertNull(connection.establishCompletable)

        connection.establishCompletable().test()

        assertNotNull(connection.establishCompletable)
    }

    @Test
    fun establishObservable_dispose_setsStateToNew() {
        assertNull(connection.establishCompletable)

        val observer = connection.establishCompletable().test()

        connection.setState(KLTBConnectionState.ESTABLISHING)
        assertEquals(KLTBConnectionState.ESTABLISHING, connection.state().current)

        observer.dispose()

        assertEquals(KLTBConnectionState.NEW, connection.state().current)
    }

    @Test
    fun establishObservable_emitterCompletes_receivesOnComplete() {
        doNothing().whenever(connection).establish(anyBoolean())

        val observer = connection.establishCompletable().test()

        observer.assertNotComplete()

        connection.establishCompletableEmitter!!.onComplete()

        observer.assertComplete()
    }

    @Test
    fun establishObservable_emitterCompletes_nullifiesEmitter() {
        doNothing().whenever(connection).establish(anyBoolean())

        connection.establishCompletable().test()

        assertNotNull(connection.establishCompletableEmitter)

        connection.establishCompletableEmitter!!.onComplete()

        assertNull(connection.establishCompletableEmitter)
    }

    @Test
    fun establishObservable_emitterCompletes_nullifiesCompletable() {
        doNothing().whenever(connection).establish(anyBoolean())

        connection.establishCompletable().test()

        assertNotNull(connection.establishCompletable)

        connection.establishCompletableEmitter!!.onComplete()

        assertNull(connection.establishCompletable)
    }

    @Test
    fun establishObservable_emitterError_receivesOnError() {
        doNothing().whenever(connection).establish(anyBoolean())

        val observer = connection.establishCompletable().test()

        observer.assertNoErrors()

        val e = Exception()

        connection.establishCompletableEmitter!!.tryOnError(e)

        observer.assertError(e)
    }

    @Test
    fun establishObservable_multipleInvocations_returnsSameCompletable() {
        assertEquals(connection.establishCompletable(true), connection.establishCompletable(true))
        assertEquals(connection.establishCompletable(false), connection.establishCompletable(false))
    }

    @Test
    fun establishObservable_multipleInvocations_evenWithDfu_returnsSameCompletable() {
        assertEquals(connection.establishCompletable(false), connection.establishCompletable(true))
    }

    @Test
    fun establishObservable_multipleInvocations_nullCompletable_returnsNewCompletableAndEmitsErrorToFormer() {
        doNothing().whenever(connection).establish(anyBoolean())

        val firstObserver = connection.establishCompletable().test()

        connection.establishCompletable = null

        firstObserver.assertNoErrors()

        val secondObserver = connection.establishCompletable().test()

        firstObserver.assertError(ConnectionEstablishException::class.java)

        secondObserver.assertNoErrors()
    }

    /*
  ON CONNECTION ESTABLISHED
   */

    @Test
    fun onConnectionEstablished_invokesMaybeNotifyThroughCompletable_when_in_bootloader() {
        val connectionState = mock<ConnectionStateImpl>()
        doReturn(connectionState).whenever(connection).innerState()
        doReturn(true).whenever(toothbrushImplementation).isRunningBootloader

        doNothing().whenever(connection).maybeNotifyThroughCompletable()
        doNothing().whenever(connection).injectDependencies()

        connection.onConnectionEstablished()

        verify(connection).maybeNotifyThroughCompletable()
        verify(connection).injectDependencies()
    }

    @Test
    fun onConenctionEstablished_set_infos_when_not_bootloader() {
        val softwareVersion = mock<SoftwareVersion>()
        val expectedSerialNumber = "serialNumber"

        val connectionState = mock<ConnectionStateImpl>()
        doReturn(connectionState).whenever(connection).innerState()

        doNothing().whenever(connection).maybeNotifyThroughCompletable()
        doNothing().whenever(connection).injectDependencies()
        doNothing().whenever(detectorsManager).setGruInfo(any(), any())
        doNothing().whenever(driver).setTime()
        doNothing().whenever(driver).disableMultiUserMode()
        doReturn(expectedSerialNumber).whenever(driver).getSerialNumber()
        doReturn(true).whenever(driver).hasValidGruData()
        doReturn(softwareVersion).whenever(driver).gruDataVersion
        doNothing().whenever(toothbrushImplementation).setSerialNumber(any())

        connection.onConnectionEstablished()

        verify(connection).maybeNotifyThroughCompletable()
        verify(connection).injectDependencies()
        verify(detectorsManager).setGruInfo(true, softwareVersion)
        verify(driver).setTime()
        verify(driver).disableMultiUserMode()
        verify(driver).getSerialNumber()
        verify(driver).hasValidGruData()
        verify(driver).gruDataVersion
        verify(toothbrushImplementation).setSerialNumber(expectedSerialNumber)
    }

    @Test
    fun onConenctionEstablished_NotInbootloaderAndModelIsHiLink_callCacheNameWithRealName() {
        mockForSetNameOnConnectionsEstablished()

        doReturn(ToothbrushModel.HILINK).whenever(toothbrushImplementation).model
        val expectedName = "testName"
        doReturn(expectedName).whenever(driver).queryRealName()
        doNothing().whenever(toothbrushImplementation).cacheName(any())

        connection.onConnectionEstablished()

        verify(toothbrushImplementation).cacheName(expectedName)
    }

    @Test
    fun onConenctionEstablished_NotInbootloaderAndModelIsNotHiLink_neverCallCacheName() {
        mockForSetNameOnConnectionsEstablished()

        doReturn(ToothbrushModel.CONNECT_E2).whenever(toothbrushImplementation).model

        connection.onConnectionEstablished()

        verify(toothbrushImplementation, never()).cacheName(any())
    }

    @Test
    fun onConenctionEstablished_InbootloaderAndModelIsHiLink_neverCallCacheName() {
        mockForSetNameOnConnectionsEstablished()
        doReturn(false).whenever(toothbrushImplementation).isRunningBootloader

        doReturn(ToothbrushModel.HILINK).whenever(toothbrushImplementation).model

        connection.onConnectionEstablished()

        verify(toothbrushImplementation, never()).cacheName(any())
    }

    private fun mockForSetNameOnConnectionsEstablished() {
        val softwareVersion = mock<SoftwareVersion>()
        val expectedSerialNumber = "serialNumber"

        val connectionState = mock<ConnectionStateImpl>()
        doReturn(connectionState).whenever(connection).innerState()

        doReturn(false).whenever(toothbrushImplementation).isRunningBootloader
        doNothing().whenever(driver).setTime()
        doNothing().whenever(driver).disableMultiUserMode()
        doReturn(expectedSerialNumber).whenever(driver).getSerialNumber()
        doReturn(true).whenever(driver).hasValidGruData()
        doReturn(softwareVersion).whenever(driver).gruDataVersion
        doNothing().whenever(toothbrushImplementation).setSerialNumber(any())
        doNothing().whenever(detectorsManager).setGruInfo(any(), any())
        doNothing().whenever(connection).maybeNotifyThroughCompletable()
        doNothing().whenever(connection).injectDependencies()
    }

    /*
      ON CONNECTION ERROR
       */
    @Test
    fun onConnectionError_invokesMaybeNotifyErrorThroughCompletable() {
        val driver = mock<DeviceDriver>()
        doReturn(driver).whenever(connection).driver()

        val connectionState = mock<ConnectionStateImpl>()
        doReturn(connectionState).whenever(connection).innerState()

        doNothing().whenever(connection).maybeNotifyErrorThroughCompletable(any())

        val expectedThrowable = mock<Throwable>()
        connection.onConnectionError(expectedThrowable)

        verify(connection).maybeNotifyErrorThroughCompletable(eq(expectedThrowable))
    }

    /*
  MAYBE NOTIFY ERROR THROUGH COMPLETABLE
   */
    @Test
    fun maybeNotifyErrorThroughCompletable_nullEmitter_doesNothing() {
        connection.maybeNotifyErrorThroughCompletable(Exception())
    }

    @Test
    fun maybeNotifyErrorThroughCompletable_nonNullEmitter_invokesOnError() {
        connection.establishCompletableEmitter = mock()

        connection.maybeNotifyErrorThroughCompletable(Exception())

        verify(connection.establishCompletableEmitter)!!.tryOnError(any())
    }

    /*
  MAYBE NOTIFY THROUGH COMPLETABLE
   */
    @Test
    fun maybeNotifyThroughCompletable_nullEmitter_doesNothing() {
        connection.maybeNotifyThroughCompletable()
    }

    @Test
    fun maybeNotifyThroughCompletable_nonNullEmitter_invokesOnComplete() {
        connection.establishCompletableEmitter = mock()

        connection.maybeNotifyThroughCompletable()

        verify(connection.establishCompletableEmitter)!!.onComplete()
    }

    /*
  SET TAG
   */
    @Test
    fun setTag_storesTag() {
        assertNull(connection.tag)

        val expectedTag = Any()

        connection.tag = expectedTag

        assertEquals(expectedTag, connection.tag)
    }

    @Test
    fun setTag_invokesTagRelayAccept() {
        connection.tagRelay.test().assertEmpty()

        val expectedTag = Any()

        connection.tag = expectedTag

        connection.tagRelay.test().assertValue(true)
    }

    /*
  HAS OTA OBSERVABLE
   */
    @Test
    fun hasOtaObservable_emptyRelay_emitsFalse() {
        connection.tagRelay.test().assertEmpty()

        connection.hasOTAObservable().test().assertValue(false)
    }

    @Test
    fun hasOtaObservable_relayWithValueTrue_emitsTrue() {
        connection.tagRelay.test().assertEmpty()

        connection.tagRelay.accept(true)

        connection.hasOTAObservable().test().assertValue(true)
    }

    @Test
    fun hasOtaObservable_relayWithValueAfterSubscriptionTrue_startsWithFalse_thenEmitsTrue() {
        connection.tagRelay.test().assertEmpty()

        val observer = connection.hasOTAObservable().test()

        observer.assertValue(false)

        connection.tagRelay.accept(true)

        observer.assertValues(false, true)
    }

    @Test
    fun `onBrushingSessionStateChange notify the monitor`() {
        val testObserver = connection.brushingSessionMonitor().sessionMonitorStream.test()

        connection.onBrushingSessionStateChanged(true)
        connection.onBrushingSessionStateChanged(false)

        testObserver.assertValues(true, false)
    }

    internal interface FakeDriver : KLTBDriver, BleDriver {
        override fun plaqlessNotifications(): Flowable<PlaqlessSensorState> {
            return Flowable.empty()
        }
    }
}
