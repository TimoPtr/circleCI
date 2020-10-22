/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.error.FailureReason
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
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

/** [BrushingModeManagerImpl] tests */
class BrushingModeManagerImplTest : BaseUnitTest() {

    private val bleDriver = mock<BleDriver>()

    private val brushingModeChangedUseCase: BrushingModeChangedUseCase = mock()
    private val connectionState: ConnectionState = mock()

    private lateinit var brushingModeManager: BrushingModeManagerImpl

    @Before
    fun before() {
        brushingModeManager = spy(
            BrushingModeManagerImpl(
                bleDriver = bleDriver,
                brushingModeChangedUseCase = brushingModeChangedUseCase,
                connectionState = connectionState
            )
        )
    }

    /*
    init
     */
    @Test
    fun `init registers as state listener`() {
        val localBrushingModeManager = BrushingModeManagerImpl(
            bleDriver = bleDriver,
            brushingModeChangedUseCase = brushingModeChangedUseCase,
            connectionState = connectionState
        )

        verify(connectionState).register(localBrushingModeManager)
    }

    /*
    onConnectionStateChanged
     */
    @Test
    fun `onConnectionStateChanged ACTIVE invokes updateCacheOnNewBrushingModeState`() {
        doNothing().whenever(brushingModeManager).updateCacheOnNewBrushingModeState()

        brushingModeManager.onConnectionStateChanged(mock(), KLTBConnectionState.ACTIVE)

        verify(brushingModeManager).updateCacheOnNewBrushingModeState()
    }

    @Test
    fun `onConnectionStateChanged newState different than ACTIVE invokes brushingModeChangedDisposable dispose`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val disposable = mock<Disposable>()
                brushingModeManager.brushingModeChangedDisposable = disposable

                brushingModeManager.onConnectionStateChanged(mock(), state)

                verify(disposable).dispose()
            }
    }

    /*
    updateCacheOnNewBrushingModeState
     */
    @Test
    fun `updateCacheOnNewBrushingModeState disposes previous subscription to device parameters stream`() {
        val oldDisposable = mock<Disposable>()
        brushingModeManager.brushingModeChangedDisposable = oldDisposable

        whenever(brushingModeChangedUseCase.brushingModeChangedStream(bleDriver.deviceParametersCharacteristicChangedStream()))
            .thenReturn(Flowable.never())

        brushingModeManager.updateCacheOnNewBrushingModeState()

        verify(oldDisposable).dispose()
    }

    @Test
    fun `updateCacheOnNewBrushingModeState stores subscription to device parameters stream`() {
        TestCase.assertNull(brushingModeManager.brushingModeChangedDisposable)

        val subject = PublishProcessor.create<ByteArray>()
        whenever(brushingModeChangedUseCase.brushingModeChangedStream(bleDriver.deviceParametersCharacteristicChangedStream()))
            .thenReturn(subject)

        brushingModeManager.updateCacheOnNewBrushingModeState()

        assertTrue(subject.hasSubscribers())
        assertNotNull(brushingModeManager.brushingModeChangedDisposable)
    }

    @Test
    fun `updateCacheOnNewBrushingModeState caches BrushingModeState if it's different than current cache`() {
        val subject = PublishProcessor.create<ByteArray>()
        whenever(brushingModeChangedUseCase.brushingModeChangedStream(bleDriver.deviceParametersCharacteristicChangedStream()))
            .thenReturn(subject)

        brushingModeManager.updateCacheOnNewBrushingModeState()

        val brushingModeState =
            BrushingModeState(
                BrushingMode.Regular,
                TrustedClock.getNowOffsetDateTime(),
                emptyList()
            )
        brushingModeManager.cacheAndEmitBrushingModeState(brushingModeState)

        assertEquals(brushingModeState, brushingModeManager.brushingModeStateCache.get())

        verify(brushingModeManager, times(1)).cacheAndEmitBrushingModeState(any())

        val brushingModePayload = brushingModePayload(BrushingMode.Strong)
        subject.onNext(brushingModePayload)

        verify(brushingModeManager, times(2)).cacheAndEmitBrushingModeState(any())

        val expectedBrushingModeState =
            brushingModeManager.parseBrushingModeParameterResponse(PayloadReader(brushingModePayload))
        assertEquals(
            expectedBrushingModeState,
            brushingModeManager.brushingModeStateCache.get()
        )
    }

    @Test
    fun `updateCacheOnNewBrushingModeState doesn't update cache if BrushingModeState is the same than current`() {
        val subject = PublishProcessor.create<ByteArray>()
        whenever(brushingModeChangedUseCase.brushingModeChangedStream(bleDriver.deviceParametersCharacteristicChangedStream()))
            .thenReturn(subject)

        brushingModeManager.updateCacheOnNewBrushingModeState()

        val brushingModePayload = brushingModePayload(BrushingMode.Regular)
        val brushingModeState =
            brushingModeManager.parseBrushingModeParameterResponse(PayloadReader(brushingModePayload))
        brushingModeManager.cacheAndEmitBrushingModeState(brushingModeState)

        assertEquals(brushingModeState, brushingModeManager.brushingModeStateCache.get())

        verify(brushingModeManager, times(1)).cacheAndEmitBrushingModeState(any())

        subject.onNext(brushingModePayload)

        verify(brushingModeManager, times(1)).cacheAndEmitBrushingModeState(any())
    }

    /*
    cacheAndEmitBrushingModeState
     */

    @Test
    fun `cacheAndEmitBrushingModeState updates brushingModeStateCache`() {
        val state = BrushingModeState(
            BrushingMode.Regular,
            TrustedClock.getNowOffsetDateTime(),
            emptyList()
        )
        brushingModeManager.cacheAndEmitBrushingModeState(state)
        assertEquals(state, brushingModeManager.brushingModeStateCache.get())
    }

    @Test
    fun `cacheAndEmitBrushingModeState emits state through brushingModeStateFlowable`() {
        val state = BrushingModeState(
            BrushingMode.Regular,
            TrustedClock.getNowOffsetDateTime(),
            emptyList()
        )

        val testObserver = brushingModeManager.brushingModeStateFlowable().test()
        brushingModeManager.cacheAndEmitBrushingModeState(state)

        testObserver.assertValue(state)
    }

    /*
    parseAvailableBrushingModesMask
     */

    @Test
    fun `parseAvailableBrushingModesMask returns empty list when no flag is set`() =
        assertTrue(brushingModeManager.parseAvailableBrushingModesMask(0b00000000).isEmpty())

    @Test
    fun `parseAvailableBrushingModesMask uses BrushingMode bleIndexes`() {
        assertEquals(
            BrushingMode.Regular,
            brushingModeManager.parseAvailableBrushingModesMask(0b00000001)[0]
        )
        assertEquals(
            BrushingMode.Slow,
            brushingModeManager.parseAvailableBrushingModesMask(0b00000010)[0]
        )
        assertEquals(
            BrushingMode.Strong,
            brushingModeManager.parseAvailableBrushingModesMask(0b00000100)[0]
        )
    }

    /*
    parseBrushingModeParameterResponse
     */

    @Test
    fun `parseBrushingModeParameterResponse correctly parses the response payload`() {
        val payload = brushingModePayload()

        val parsedResponse = brushingModeManager.parseBrushingModeParameterResponse(
            PayloadReader(payload)
        )

        assertEquals(BrushingMode.Strong, parsedResponse.currentMode)
        assertEquals(
            OffsetDateTime
                .ofInstant(Instant.ofEpochSecond(1567069153L), TrustedClock.systemZone),
            parsedResponse.lastUpdateDate
        )
        assertEquals(2, parsedResponse.availableModes.size)
        assertTrue(parsedResponse.availableModes.contains(BrushingMode.Regular))
        assertTrue(parsedResponse.availableModes.contains(BrushingMode.Strong))
    }

    /*
    callParameter
     */

    @Test
    fun `callParameter calls Brushing Mode parameter through the driver and emits the response`() {
        val response = PayloadReader(byteArrayOf())
        whenever(bleDriver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.just(response))

        brushingModeManager
            .callParameter(byteArrayOf())
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(response)
    }

    @Test
    fun `callParameter calls Brushing Mode parameter through the driver and emits errors if any`() {
        whenever(bleDriver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.error(FailureReason("")))
        brushingModeManager
            .callParameter(byteArrayOf())
            .test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(FailureReason::class.java)
    }

    /*
    getCachedBrushingModeStateOrRequest
     */

    @Test
    fun `getCachedBrushingModeStateOrRequest with cache returns the cached value, no ble query`() {
        val state = mock<BrushingModeState>()
        brushingModeManager.brushingModeStateCache.set(state)
        brushingModeManager
            .getCachedBrushingModeStateOrRequest()
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(state)
        verify(brushingModeManager, never()).requestAndCacheParsedResponse(any())
    }

    @Test
    fun `getCachedBrushingModeStateOrRequest with no cache queries ble, caches and returns the value`() {
        val bleResponse = PayloadReader(
            byteArrayOf(BRUSHING_MODE_PARAMETER, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)
        )
        whenever(bleDriver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.just(bleResponse))

        // No cache
        assertNull(brushingModeManager.brushingModeStateCache.get())

        brushingModeManager
            .getCachedBrushingModeStateOrRequest()
            .test()
            .assertComplete()
            .assertNoErrors()

        // BLE is called
        verify(brushingModeManager).requestAndCacheParsedResponse(any())

        // Cache is updated
        assertNotNull(brushingModeManager.brushingModeStateCache.get())
    }

    /*
    isAvailable
     */

    @Test
    fun `isAvailable returns true`() = assertTrue(brushingModeManager.isAvailable())

    /*
    set
     */

    @Test
    fun `set queries ble with proper payload`() {
        val state = mock<BrushingModeState>()
        doReturn(Single.just(state))
            .whenever(brushingModeManager).requestAndCacheParsedResponse(any())

        brushingModeManager.set(BrushingMode.Slow)

        verify(brushingModeManager).requestAndCacheParsedResponse(
            eq(byteArrayOf(BRUSHING_MODE_PARAMETER, BrushingMode.Slow.bleIndex.toByte()))
        )
    }

    private fun brushingModePayload(brushingMode: BrushingMode = BrushingMode.Strong): ByteArray {
        return byteArrayOf(
            BRUSHING_MODE_PARAMETER, // Parameter ID callback
            brushingMode.bleIndex.toByte(), // Current mode is Strong mode
            0xE1.toByte(), 0x93.toByte(), 0x67, 0x5D, // 29/8/2019 at 8:59:13 UTC (1567069153)
            0b00000101 // Regular and Strong modes available
        )
    }
}
