/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.VisibleForTesting
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeCustomizerImpl
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicReference
import timber.log.Timber

@VisibleForTesting
internal const val BRUSHING_MODE_PARAMETER: Byte = 0x50

/** [BrushingModeManager] implementation */
@SuppressWarnings("TooManyFunctions")
internal class BrushingModeManagerImpl(
    private val bleDriver: BleDriver,
    private val brushingModeChangedUseCase: BrushingModeChangedUseCase,
    connectionState: ConnectionState
) : BrushingModeManager, ConnectionStateListener, BrushingModeStateObserver {

    private val customBrushingModeCustomizer = BrushingModeCustomizerImpl(bleDriver)

    private val brushingModeStateProcessor = PublishProcessor.create<BrushingModeState>()

    @VisibleForTesting
    val brushingModeStateCache = AtomicReference<BrushingModeState>()

    @VisibleForTesting
    var brushingModeChangedDisposable: Disposable? = null

    init {
        connectionState.register(this)
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) = when (newState) {
        KLTBConnectionState.ACTIVE -> updateCacheOnNewBrushingModeState()
        else -> stopListeningToBrushingModeChanges()
    }

    @VisibleForTesting
    fun updateCacheOnNewBrushingModeState() {
        brushingModeChangedDisposable.forceDispose()

        brushingModeChangedDisposable =
            brushingModeChangedUseCase.brushingModeChangedStream(
                bleDriver.deviceParametersCharacteristicChangedStream()
            )
                .map { parseBrushingModeParameterResponse(PayloadReader(it)) }
                .filter { it != brushingModeStateCache.get() }
                .subscribe(::cacheAndEmitBrushingModeState, Timber::e)
    }

    @VisibleForTesting
    fun stopListeningToBrushingModeChanges() {
        brushingModeChangedDisposable.forceDispose()
    }

    override fun isAvailable() = true

    override fun availableBrushingModes() =
        getCachedBrushingModeStateOrRequest()
            .map(BrushingModeState::availableModes)

    override fun lastUpdateDate() =
        getCachedBrushingModeStateOrRequest()
            .map(BrushingModeState::lastUpdateDate)

    override fun set(mode: BrushingMode): Completable =
        requestAndCacheParsedResponse(byteArrayOf(BRUSHING_MODE_PARAMETER, mode.bleIndex.toByte()))
            .ignoreElement()

    override fun getCurrent() =
        getCachedBrushingModeStateOrRequest()
            .map(BrushingModeState::currentMode)

    override fun brushingModeStateFlowable(): Flowable<BrushingModeState> =
        brushingModeStateProcessor.hide()
            .observeOn(Schedulers.io())

    override fun customize() = customBrushingModeCustomizer

    @VisibleForTesting
    fun getCachedBrushingModeStateOrRequest() =
        brushingModeStateCache.get()
            ?.let { Single.just(it) }
            ?: requestAndCacheParsedResponse(byteArrayOf(BRUSHING_MODE_PARAMETER))

    @VisibleForTesting
    fun requestAndCacheParsedResponse(payload: ByteArray) =
        callParameter(payload)
            .map(::parseBrushingModeParameterResponse)
            .doOnSuccess(::cacheAndEmitBrushingModeState)

    @VisibleForTesting
    fun callParameter(payload: ByteArray) =
        bleDriver.setAndGetDeviceParameterOnce(payload)

    @VisibleForTesting
    fun parseBrushingModeParameterResponse(payloadReader: PayloadReader) =
        BrushingModeState(
            currentMode = BrushingMode.values()[payloadReader.skip(1).readInt8().toInt()],
            lastUpdateDate = payloadReader.readDateFromUnixTimeStamp(),
            availableModes = parseAvailableBrushingModesMask(payloadReader.readInt8())
        )

    @VisibleForTesting
    fun parseAvailableBrushingModesMask(mask: Byte): List<BrushingMode> {
        val bitMask = Bitmask(mask)
        val list = ArrayList<BrushingMode>()

        // BrushingMode's bleIndex is the flag bit's index in the mask
        for (brushingMode in BrushingMode.values()) {
            if (bitMask.getBit(brushingMode.bleIndex)) {
                list.add(brushingMode)
            }
        }

        return list
    }

    @VisibleForTesting
    fun cacheAndEmitBrushingModeState(brushingModeState: BrushingModeState) {
        brushingModeStateCache.set(brushingModeState)
        brushingModeStateProcessor.onNext(brushingModeState)
    }
}
