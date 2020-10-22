/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrushupdate

import android.os.Build
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.ota.GruwareFilter
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.AvailableUpdate.Companion.empty
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_DSP
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.TestFeatureToggle
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.toothbrushupdate.OtaUpdateType.MANDATORY
import com.kolibree.android.toothbrushupdate.OtaUpdateType.MANDATORY_NEEDS_INTERNET
import com.kolibree.android.toothbrushupdate.OtaUpdateType.STANDARD
import com.kolibree.sdkws.data.model.GruwareData
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertNotSame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtaCheckerTest : BaseUnitTest() {
    private val serviceProvider: ServiceProvider = mock()

    private val gruwareInteractor: GruwareInteractor = mock()

    private val networkChecker: NetworkChecker = mock()

    private val alwaysOfferOtaUpdateFeatureToggle = TestFeatureToggle(
        AlwaysOfferOtaUpdateFeature,
        initialValue = false
    )

    private val gruwareFilter: GruwareFilter = mock()

    private lateinit var otaChecker: OtaChecker

    override fun setup() {
        super.setup()

        otaChecker =
            spy(
                OtaChecker(
                    serviceProvider,
                    gruwareInteractor,
                    networkChecker,
                    setOf(alwaysOfferOtaUpdateFeatureToggle),
                    gruwareFilter
                )
            )
    }

    /*
    CHECK OTA
     */
    @Test
    fun otaForConnectionObservable_mergesResultsFromCheckConnectionsFromCheckConnectionsFromServiceObservableAndRelay() {
        val expectedOtaForConnection1 = OtaForConnection(mockConnection(), STANDARD, mock())
        doReturn(Observable.just(expectedOtaForConnection1))
            .whenever(otaChecker)
            .checkConnectionsFromServiceObservable()

        val observer =
            otaChecker.otaForConnectionsOnce().test().assertValue(expectedOtaForConnection1)

        val expectedOtaForConnection2 = OtaForConnection(mockConnection(), MANDATORY, mock())
        otaChecker.otaForConnectionSubject.onNext(expectedOtaForConnection2)

        observer.assertValueAt(1, expectedOtaForConnection2)

        observer.assertNotComplete()
    }

    @Test
    fun `otaForConnectionObservable completes when service has zero connections`() {
        mockServiceConnections(listOf())

        otaChecker.otaForConnectionsOnce().test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun otaForConnectionObservable_dispose_clearsInternalCompositeDisposable() {
        assertEquals(0, otaChecker.disposables.size())

        doReturn(Observable.empty<OtaForConnection>()).whenever(otaChecker)
            .checkConnectionsFromServiceObservable()

        val checkOtaDisposable = otaChecker.otaForConnectionsOnce().subscribe()

        assertFalse(otaChecker.disposables.isDisposed)

        val fakeDisposable: Disposable = mock()
        otaChecker.disposables.add(fakeDisposable)

        doNothing().whenever(otaChecker).stopListeningToConnectionState()

        checkOtaDisposable.dispose()

        verify(fakeDisposable).dispose()

        assertFalse(otaChecker.disposables.isDisposed)
    }

    @Test
    fun otaForConnectionObservable_dispose_invokesStopListeningToConnectionState() {
        assertEquals(0, otaChecker.disposables.size())

        doReturn(Observable.empty<OtaForConnection>()).whenever(otaChecker)
            .checkConnectionsFromServiceObservable()

        val disposable = otaChecker.otaForConnectionsOnce().subscribe()

        doNothing().whenever(otaChecker).stopListeningToConnectionState()

        disposable.dispose()

        verify(otaChecker).stopListeningToConnectionState()
    }

    /*
    CHECK CONNECTIONS FROM SERVICE OBSERVABLE
     */
    @Test
    fun checkConnectionsFromServiceObservable_zeroConnections_completesWithoutEmitting() {
        mockServiceConnections(listOf())

        otaChecker.checkConnectionsFromServiceObservable().test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun checkConnectionsFromServiceObservable_zeroConnections_invokesCompleteAndRefreshSubject() {
        mockServiceConnections(listOf())

        otaChecker.checkConnectionsFromServiceObservable().test()

        verify(otaChecker).completeAndRefreshSubject()
    }

    @Test
    fun checkConnectionsFromServiceObservable_anyState_invokesMaybeListenToConnectionState() {
        val connections = mutableListOf<KLTBConnection>()
        KLTBConnectionState.values()
            .forEach {
                connections.add(mockConnection(it))
            }
        mockServiceConnections(connections)

        doNothing().whenever(otaChecker).maybeListenToConnectionState(any())

        doReturn(Maybe.empty<OtaForConnection>()).whenever(otaChecker).otaForConnectionMaybe(any())

        otaChecker.checkConnectionsFromServiceObservable().test()
            .assertComplete()
            .assertValueCount(0)

        connections.forEach {
            verify(otaChecker).maybeListenToConnectionState(it)
        }
    }

    @Test
    fun checkConnectionsFromServiceObservable_stateOtherThanActiveOrOta_neverInvokesOtaForConnectionMaybe() {
        val connections = mutableListOf<KLTBConnection>()
        KLTBConnectionState.values()
            .filterNot { it == OTA || it == ACTIVE }
            .forEach {
                connections.add(mockConnection(it))
            }
        mockServiceConnections(connections)

        otaChecker.checkConnectionsFromServiceObservable().test()
            .assertComplete()
            .assertValueCount(0)

        verify(otaChecker, never()).otaForConnectionMaybe(any())
    }

    @Test
    fun checkConnectionsFromServiceObservable_stateOTAorACTIVE_invokesOtaForConnectionMaybe() {
        val connections = mutableListOf<KLTBConnection>()
        KLTBConnectionState.values()
            .filter { it == OTA || it == ACTIVE }
            .forEach {
                connections.add(mockConnection(it))
            }
        mockServiceConnections(connections)

        doNothing().whenever(otaChecker).maybeListenToConnectionState(any())

        doReturn(Maybe.empty<OtaForConnection>()).whenever(otaChecker).otaForConnectionMaybe(any())

        otaChecker.checkConnectionsFromServiceObservable().test()

        connections.forEach {
            verify(otaChecker).otaForConnectionMaybe(it)
        }
    }

    @Test
    fun checkConnectionsFromServiceObservable_oneConnectionACTIVE_doesNotNeedUpdate_emitsEmptyObservable() {
        val connection: KLTBConnection = mockConnection(ACTIVE)
        mockServiceConnections(listOf(connection))

        doReturn(Maybe.empty<OtaForConnection>()).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        doNothing().whenever(otaChecker).maybeListenToConnectionState(any())

        otaChecker.checkConnectionsFromServiceObservable().test().assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun checkConnectionsFromServiceObservable_oneConnectionACTIVE_emitsMaybeFromCheckOta() {
        val connection: KLTBConnection = mockConnection(ACTIVE)
        mockServiceConnections(listOf(connection))

        val expectedOtaForConnection = OtaForConnection(connection, STANDARD, mock())

        doReturn(Maybe.just(expectedOtaForConnection)).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        doNothing().whenever(otaChecker).maybeListenToConnectionState(any())

        otaChecker.checkConnectionsFromServiceObservable().test().assertComplete()
            .assertValue(expectedOtaForConnection)
    }

    @Test
    fun checkConnectionsFromServiceObservable_multipleConnectionsValid_firstIsMandatoryUpdateSecondDoesNotNeed_emitsOneOtaForConnection() {
        val connection1: KLTBConnection = mockConnection(ACTIVE)
        val connection2: KLTBConnection = mockConnection(OTA)
        mockServiceConnections(listOf(connection1, connection2))

        val expectedOtaForConnection = OtaForConnection(connection1, MANDATORY, mock())

        doReturn(Maybe.just(expectedOtaForConnection)).whenever(otaChecker)
            .otaForConnectionMaybe(connection1)
        doReturn(Maybe.empty<OtaForConnection>()).whenever(otaChecker)
            .otaForConnectionMaybe(connection2)

        doNothing().whenever(otaChecker).maybeListenToConnectionState(any())

        otaChecker.checkConnectionsFromServiceObservable().test()
            .assertValueCount(1)
            .assertComplete()
            .assertValue(expectedOtaForConnection)
    }

    @Test
    fun checkConnectionsFromServiceObservable_multipleConnectionACTIVE_firstIsMandatoryUpdateSecondIsStandard_emitsTwoOtaForConnection() {
        val connectionActive: KLTBConnection = mockConnection(ACTIVE)
        val connectionOta: KLTBConnection = mockConnection(OTA)
        val connectionTerminated: KLTBConnection = mockConnection(TERMINATED)
        mockServiceConnections(listOf(connectionActive, connectionOta, connectionTerminated))

        val expectedOtaForConnectionActive = OtaForConnection(connectionActive, MANDATORY, mock())
        val expectedOtaForConnectionOta = OtaForConnection(connectionOta, STANDARD, mock())

        val subjectForActive = MaybeSubject.create<OtaForConnection>()
        val subjectForOta = MaybeSubject.create<OtaForConnection>()
        doReturn(subjectForActive).whenever(otaChecker).otaForConnectionMaybe(connectionActive)
        doReturn(subjectForOta).whenever(otaChecker).otaForConnectionMaybe(connectionOta)

        doNothing().whenever(otaChecker).maybeListenToConnectionState(any())

        val observer = otaChecker.checkConnectionsFromServiceObservable().test().assertEmpty()

        subjectForOta.onSuccess(expectedOtaForConnectionOta)

        observer.assertValueCount(1)

        subjectForActive.onSuccess(expectedOtaForConnectionActive)

        observer.assertValueCount(2)
            .assertComplete()
            .assertValues(expectedOtaForConnectionOta, expectedOtaForConnectionActive)
    }

    /*
    maybeListenToConnectionState
     */
    @Test
    fun `maybeListenToConnectionState doesn't register as listener if connections already contains parameter`() {
        val connection: KLTBConnection = mockConnection()

        otaChecker.connections.add(connection)

        val size = otaChecker.connections.size

        otaChecker.maybeListenToConnectionState(connection)

        verify(connection.state(), never()).register(otaChecker)
        assertEquals(size, otaChecker.connections.size)
    }

    @Test
    fun `maybeListenToConnectionState doesn't register as listener if connection is already active`() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(ACTIVE)
            .build()

        assertFalse(otaChecker.connections.contains(connection))

        otaChecker.maybeListenToConnectionState(connection)

        verify(connection.state(), never()).register(otaChecker)

        assertFalse(otaChecker.connections.contains(connection))
    }

    @Test
    fun `maybeListenToConnectionState registers as listener if connection is not active`() {
        KLTBConnectionState.values()
            .filterNot { it == ACTIVE }
            .forEach { state ->
                val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
                    .withState(state)
                    .build()

                otaChecker.connections.clear()

                otaChecker.maybeListenToConnectionState(connection)

                verify(connection.state()).register(otaChecker)

                assertTrue(otaChecker.connections.contains(connection))
            }
    }

    /*
    otaForConnectionMaybe
     */
    @Test
    fun otaForConnectionMaybe_connectionNeedsMandatoryUpdateTrue_getGruwareThrowsException_emitsMaybeError() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionNeedsMandatoryOTAUpdate(connection)

        val expectedError = Exception("Test forced error")
        whenever(gruwareInteractor.getGruware(connection)).thenReturn(Single.error(expectedError))

        otaChecker.otaForConnectionMaybe(connection).test().assertError(expectedError)
    }

    @Test
    fun otaForConnectionMaybe_connectionNeedsMandatoryUpdateTrue_getGruwareThrowsNetworkNotAvailableException_emitsOtaForConnectionMANDATORY_NEEDS_INTERNET() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionNeedsMandatoryOTAUpdate(connection)

        whenever(gruwareInteractor.getGruware(connection)).thenReturn(
            Single.error(
                NetworkNotAvailableException()
            )
        )

        val expectedOtaForConnection = OtaForConnection(connection, MANDATORY_NEEDS_INTERNET)

        doNothing().whenever(otaChecker).listenToNetworkState(connection)

        otaChecker.otaForConnectionMaybe(connection).test()
            .assertNoErrors()
            .assertValue(expectedOtaForConnection)
    }

    @Test
    fun otaForConnectionMaybe_connectionNeedsMandatoryUpdateTrue_getGruwareThrowsNetworkNotAvailableException_invokesListenToNetworkState() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionNeedsMandatoryOTAUpdate(connection)

        whenever(gruwareInteractor.getGruware(connection)).thenReturn(
            Single.error(
                NetworkNotAvailableException()
            )
        )

        doNothing().whenever(otaChecker).listenToNetworkState(connection)

        otaChecker.otaForConnectionMaybe(connection).test()

        verify(otaChecker).listenToNetworkState(connection)
    }

    @Test
    fun otaForConnectionMaybe_connectionNeedsMandatoryUpdateTrue_getGruwareEmitsGruware_returnsMaybeWithOtaForConnection() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionNeedsMandatoryOTAUpdate(connection)

        val gruwareData: GruwareData = mock()
        whenever(gruwareInteractor.getGruware(connection)).thenReturn(Single.just(gruwareData))

        val mandatoryOtaForConnection = OtaForConnection(connection, MANDATORY, gruwareData)

        otaChecker.otaForConnectionMaybe(connection).test().assertValue(mandatoryOtaForConnection)
    }

    @Test
    fun otaForConnectionMaybe_connectionNeedsMandatoryUpdateFalse_returnsConnectionFirmwareUpdateMaybe() {
        val connection: KLTBConnection = mockConnection()
        doReturn(false).whenever(otaChecker).connectionNeedsMandatoryOTAUpdate(connection)

        val expectedOtaForConnection = OtaForConnection(connection, MANDATORY, mock())
        doReturn(Maybe.just(expectedOtaForConnection)).whenever(otaChecker)
            .connectionFirmwareUpdateMaybe(connection)

        otaChecker.otaForConnectionMaybe(connection).test().assertValue(expectedOtaForConnection)
    }

    @Test
    fun `otaForConnection invokes completeAndRefreshSubject when connectionFirmwareUpdateMaybe completes`() {
        val connection: KLTBConnection = mockConnection()
        doReturn(false).whenever(otaChecker).connectionNeedsMandatoryOTAUpdate(connection)

        doReturn(Maybe.empty<OtaForConnection>()).whenever(otaChecker)
            .connectionFirmwareUpdateMaybe(connection)

        otaChecker.otaForConnectionMaybe(connection).test()
            .assertComplete()

        verify(otaChecker).completeAndRefreshSubject()
    }

    /*
    completeAndRefreshSubject
     */
    @Test
    fun `completeAndRefreshSubject completes previous instance`() {
        val oldSubject = otaChecker.otaForConnectionSubject

        otaChecker.completeAndRefreshSubject()

        assertTrue(oldSubject.hasComplete())
    }

    @Test
    fun `completeAndRefreshSubject replaces previous subject with new instance`() {
        val oldSubject = otaChecker.otaForConnectionSubject

        otaChecker.completeAndRefreshSubject()

        assertNotSame(oldSubject, otaChecker.otaForConnectionSubject)
    }

    /*
    LISTEN TO NETWORK STATE
     */
    @Test
    fun listenToNetworkState_onlyReactsToNetworkCheckerEmittingTrue() {
        val connection: KLTBConnection = mockConnection()

        val networkSubject = PublishSubject.create<Boolean>()
        whenever(networkChecker.connectivityStateObservable()).thenReturn(networkSubject)

        doReturn(MaybeSubject.create<OtaForConnection>()).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        otaChecker.listenToNetworkState(connection)

        verify(otaChecker, never()).otaForConnectionMaybe(connection)

        networkSubject.onNext(false)

        verify(otaChecker, never()).otaForConnectionMaybe(connection)

        networkSubject.onNext(true)

        verify(otaChecker).otaForConnectionMaybe(connection)
    }

    @Test
    fun listenToNetworkState_networkCheckerEmitsTrue_otaForConnectionMaybeEmitsNonEmpty_invokesOtaRelayAccept() {
        val connection: KLTBConnection = mockConnection()

        val networkSubject = PublishSubject.create<Boolean>()

        whenever(networkChecker.connectivityStateObservable()).thenReturn(networkSubject)

        val observer = otaChecker.otaForConnectionSubject.test()

        otaChecker.listenToNetworkState(connection)

        observer.assertEmpty()

        val expectedOtaForConnection = OtaForConnection(connection, MANDATORY, mock())
        doReturn(Maybe.just(expectedOtaForConnection)).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        networkSubject.onNext(true)

        observer.assertValue(expectedOtaForConnection)
    }

    @Test
    fun listenToNetworkState_networkCheckerEmitsTrue_otaForConnectionMaybeEmitsOneError_retries() {
        val connection: KLTBConnection = mockConnection()

        val networkSubject = PublishSubject.create<Boolean>()

        whenever(networkChecker.connectivityStateObservable()).thenReturn(networkSubject)

        val observer = otaChecker.otaForConnectionSubject.test()

        otaChecker.listenToNetworkState(connection)

        observer.assertEmpty()

        val expectedOtaForConnection = OtaForConnection(connection, MANDATORY, mock())
        var nbOfInvocations = 0

        doReturn(Maybe.create<OtaForConnection> {
            if (nbOfInvocations++ == 0) {
                it.onError(Exception("Test forced error"))
            } else {
                it.onSuccess(expectedOtaForConnection)
            }
        })
            .whenever(otaChecker).otaForConnectionMaybe(connection)

        networkSubject.onNext(true)

        observer.assertValue(expectedOtaForConnection)

        assertEquals(2, nbOfInvocations)
    }

    @Test
    fun listenToNetworkState_networkCheckerEmitsTrue_otaForConnectionMaybeEmitsTwoErrors_emitsNothing() {
        val connection: KLTBConnection = mockConnection()

        val networkSubject = PublishSubject.create<Boolean>()

        whenever(networkChecker.connectivityStateObservable()).thenReturn(networkSubject)

        val observer = otaChecker.otaForConnectionSubject.test()

        otaChecker.listenToNetworkState(connection)

        var nbOfInvocations = 0
        doReturn(Maybe.create<OtaForConnection> {
            nbOfInvocations++

            it.onError(Exception("Test forced error"))
        })
            .whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        networkSubject.onNext(true)

        observer.assertNoValues()

        assertEquals(2, nbOfInvocations)
    }

    /*
    CONNECTION NEEDS MANDATORY OTA UPDATE
     */
    @Test
    fun connectionNeedsMandatoryOTAUpdate_connectionSupportsOTAFalse_returnsFalse() {
        val connection: KLTBConnection = mockConnection()
        doReturn(false).whenever(otaChecker).connectionSupportsOTA(connection)

        assertFalse(otaChecker.connectionNeedsMandatoryOTAUpdate(connection))
    }

    @Test
    fun connectionNeedsMandatoryOTAUpdate_supportsOTATrue_isRunningBootloaderTrue_returnsTrue() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withBootloader(true)
            .build()

        doReturn(true).whenever(otaChecker).connectionSupportsOTA(connection)

        assertTrue(otaChecker.connectionNeedsMandatoryOTAUpdate(connection))
    }

    @Test
    fun connectionNeedsMandatoryOTAUpdate_supportsOTATrue_isRunningBootloaderFalse_needsGruUpdateFalse_returnsTrue() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withBootloader(true)
            .withValidGruData(false)
            .build()

        doReturn(false).whenever(otaChecker).needsGruUpdate(connection)

        doReturn(true).whenever(otaChecker).connectionSupportsOTA(connection)

        assertTrue(otaChecker.connectionNeedsMandatoryOTAUpdate(connection))
    }

    @Test
    fun connectionNeedsMandatoryOTAUpdate_supportsOTATrue_isRunningBootloaderFalse_needsGruUpdateTrue_returnsFalse() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withBootloader(true)
            .withValidGruData(true)
            .build()

        doReturn(true).whenever(otaChecker).needsGruUpdate(connection)

        doReturn(true).whenever(otaChecker).connectionSupportsOTA(connection)

        assertTrue(otaChecker.connectionNeedsMandatoryOTAUpdate(connection))
    }

    /*
    needsGruUpdate
     */
    @Test
    fun `needsGruUpdate returns false if connection does not support gru updates`() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withSupportGruUpdates(false)
            .build()

        assertFalse(otaChecker.needsGruUpdate(connection))
    }

    @Test
    fun `needsGruUpdate returns false if connection supports gru updates and has valid rnn data`() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withValidGruData(true)
            .build()

        assertFalse(otaChecker.needsGruUpdate(connection))
    }

    @Test
    fun `needsGruUpdate returns false if connection supports gru updates and has null rnn data`() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withSupportGruUpdates(true)
            .withNullRNN()
            .build()

        assertFalse(otaChecker.needsGruUpdate(connection))
    }

    @Test
    fun `needsGruUpdate returns true if connection supports gru updates and has invalid rnn data`() {
        val connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
            .withValidGruData(false)
            .withSupportGruUpdates(true)
            .build()

        assertTrue(otaChecker.needsGruUpdate(connection))
    }

    /*
    CONNECTION SUPPORTS OTA
     */
    @Test
    fun `connectionSupportsOTA returns false if model is not supported`() {
        doReturn(false).whenever(otaChecker).modelSupportsOTA(any())

        assertFalse(otaChecker.connectionSupportsOTA(KLTBConnectionBuilder.createAndroidLess().build()))
    }

    @Test
    fun `connectionSupportsOTA returns false if model is supported but connection is not active`() {
        doReturn(true).whenever(otaChecker).modelSupportsOTA(any())

        val states = KLTBConnectionState.values()

        for (state in states) {
            if (state === ACTIVE || state === OTA) {
                continue
            }

            assertFalse(
                otaChecker.connectionSupportsOTA(
                    KLTBConnectionBuilder.createAndroidLess()
                        .withState(state)
                        .build()
                )
            )
        }
    }

    @Test
    fun `connectionSupportsOTA returns true if model is supported and connection is active`() {
        doReturn(true).whenever(otaChecker).modelSupportsOTA(any())

        assertTrue(
            otaChecker.connectionSupportsOTA(
                KLTBConnectionBuilder.createAndroidLess()
                    .withState(ACTIVE)
                    .build()
            )
        )
    }

    @Test
    fun `connectionSupportsOTA returns true if model is supported and connection state is OTA`() {
        doReturn(true).whenever(otaChecker).modelSupportsOTA(any())

        assertTrue(
            otaChecker.connectionSupportsOTA(
                KLTBConnectionBuilder.createAndroidLess()
                    .withState(OTA)
                    .build()
            )
        )
    }

    /*
    MODEL SUPPORTS OTA
     */

    @Test
    fun `modelSupportsOTA returns false if model is ARA or E1 and android version is below N`() {
        arrayOf(
            Build.VERSION_CODES.LOLLIPOP,
            Build.VERSION_CODES.LOLLIPOP_MR1,
            Build.VERSION_CODES.M
        ).forEach { version ->
            doReturn(version).whenever(otaChecker).androidVersion()

            arrayOf(ToothbrushModel.ARA, ToothbrushModel.CONNECT_E1).forEach { model ->
                assertFalse(
                    "Model is $model\nVersion is $version",
                    otaChecker.modelSupportsOTA(model)
                )
            }
        }
    }

    @Test
    fun `modelSupportsOTA returns true if always offer ota is true, even if model is ARA or E1 and android version is below N`() {
        alwaysOfferOtaUpdateFeatureToggle.value = true

        arrayOf(
            Build.VERSION_CODES.LOLLIPOP,
            Build.VERSION_CODES.LOLLIPOP_MR1,
            Build.VERSION_CODES.M
        ).forEach { version ->
            doReturn(version).whenever(otaChecker).androidVersion()

            arrayOf(ToothbrushModel.ARA, ToothbrushModel.CONNECT_E1).forEach { model ->
                assertTrue(
                    "Model is $model\nVersion is $version",
                    otaChecker.modelSupportsOTA(model)
                )
            }
        }
    }

    @Test
    fun `modelSupportsOTA returns true if model is NOT KOLIBREE, ARA or E1 and android version is below N`() {
        arrayOf(
            Build.VERSION_CODES.LOLLIPOP,
            Build.VERSION_CODES.LOLLIPOP_MR1,
            Build.VERSION_CODES.M
        ).forEach { version ->
            doReturn(version).whenever(otaChecker).androidVersion()

            ToothbrushModel.values()
                .filter {
                    arrayOf(
                        ToothbrushModel.ARA,
                        ToothbrushModel.CONNECT_E1
                    ).contains(it).not()
                }
                .forEach { model ->
                    assertTrue(
                        "Model is $model\nVersion is $version",
                        otaChecker.modelSupportsOTA(model)
                    )
                }
        }
    }

    @Test
    fun `modelSupportsOTA returns true if model is ARA or E1 and android version is N or Above`() {
        val version = Build.VERSION_CODES.N
        doReturn(version).whenever(otaChecker).androidVersion()

        arrayOf(ToothbrushModel.ARA, ToothbrushModel.CONNECT_E1).forEach { model ->
            assertTrue(
                "Model is $model",
                otaChecker.modelSupportsOTA(model)
            )
        }
    }

    /*
    CONNECTION FIRMWARE UPDATE MAYBE
     */
    @Test
    fun connectionFirmwareUpdateMaybe_connectionSupportsOtaFalse_returnsEmptyMaybe() {
        val connection: KLTBConnection = mockConnection()
        doReturn(false).whenever(otaChecker).connectionSupportsOTA(connection)

        otaChecker.connectionFirmwareUpdateMaybe(connection).test().assertComplete()
    }

    @Test
    fun connectionFirmwareUpdateMaybe_connectionSupportsOtaTrue_gruwareDataSingleEmitsNoNetworkException_emitsEmptyMaybe() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionSupportsOTA(connection)

        doReturn(Single.error<GruwareData>(NetworkNotAvailableException())).whenever(otaChecker)
            .gruwareDataSingle(connection)

        otaChecker.connectionFirmwareUpdateMaybe(connection).test()
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun connectionFirmwareUpdateMaybe_connectionSupportsOtaTrue_gruwareDataSingleEmitsException_emitsErrorMaybe() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionSupportsOTA(connection)

        val expectedException = Exception("Test forced error")
        doReturn(Single.error<GruwareData>(expectedException)).whenever(otaChecker)
            .gruwareDataSingle(connection)

        otaChecker.connectionFirmwareUpdateMaybe(connection).test().assertError(expectedException)
    }

    @Test
    fun connectionFirmwareUpdateMaybe_connectionSupportsOtaTrue_gruwareDataSingleEmitsGruWareData_emitsOnGruwareDataForConnection() {
        val connection: KLTBConnection = mockConnection()
        doReturn(true).whenever(otaChecker).connectionSupportsOTA(connection)

        val gruwareData: GruwareData = mock()
        doReturn(Single.just(gruwareData)).whenever(otaChecker).gruwareDataSingle(connection)

        val expectedOtaForConnection = OtaForConnection(connection, STANDARD, gruwareData)
        doReturn(Maybe.just(expectedOtaForConnection)).whenever(otaChecker)
            .onGruwareDataForConnection(connection, gruwareData)

        otaChecker.connectionFirmwareUpdateMaybe(connection).test()
            .assertValue(expectedOtaForConnection)
    }

    /*
    ON CONNECTION STATE CHANGED
     */
    @Test
    fun onConnectionStateChanged_newStateNotActive_doesNothing() {
        val connection: KLTBConnection = mockConnection()
        KLTBConnectionState.values()
            .filter { it != ACTIVE }
            .forEach {
                otaChecker.onConnectionStateChanged(connection, it)
            }

        verify(otaChecker, never()).otaForConnectionMaybe(any())
    }

    @Test
    fun onConnectionStateChanged_newStateActive_noOTaForConnection_neverEmitsThroughRelay() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val connection: KLTBConnection = mockConnection()

        doReturn(Maybe.empty<OtaForConnection>()).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        val observer = otaChecker.otaForConnectionSubject.test()

        observer.assertEmpty()

        otaChecker.onConnectionStateChanged(connection, ACTIVE)

        observer.assertEmpty()
    }

    @Test
    fun onConnectionStateChanged_newStateActive_withOTaForConnection_emitsThroughRelay() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val connection: KLTBConnection = mockConnection()

        val expectedOtaForConnection = OtaForConnection(connection, STANDARD, mock())
        doReturn(Maybe.just(expectedOtaForConnection)).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        val observer = otaChecker.otaForConnectionSubject.test()

        observer.assertEmpty()

        otaChecker.onConnectionStateChanged(connection, ACTIVE)

        observer.assertValue(expectedOtaForConnection)
    }

    @Test
    fun onConnectionStateChanged_newStateActive_addsToInternalDisposable() {
        val connection: KLTBConnection = mockConnection()

        doReturn(MaybeSubject.create<OtaForConnection>()).whenever(otaChecker)
            .otaForConnectionMaybe(connection)

        assertEquals(0, otaChecker.disposables.size())

        otaChecker.onConnectionStateChanged(connection, ACTIVE)

        assertEquals(1, otaChecker.disposables.size())
    }

    private fun mockConnection(currentState: KLTBConnectionState = NEW): KLTBConnection {
        val connection: KLTBConnection = mock()

        val connectionState: ConnectionState = mock()
        whenever(connectionState.current).thenReturn(currentState)
        whenever(connection.state()).thenReturn(connectionState)

        return connection
    }

    /*
    ON GRUWARE DATA FOR CONNECTION
    */
    @Test
    fun onGruwareDataForConnection_availableUpdatesFilterEmitsEmptyGruwareData_emitsEmptyMaybe() {
        val gruwareData = GruwareData.create(
            empty(TYPE_FIRMWARE),
            empty(TYPE_GRU),
            empty(TYPE_BOOTLOADER),
            empty(TYPE_DSP)
        )

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withFirmwareVersion("1.0.0")
            .withGruDataVersion("0.1.0")
            .build()

        whenever(gruwareFilter.filterAvailableUpdates(connection, gruwareData))
            .thenReturn(Single.just(gruwareData))

        otaChecker.onGruwareDataForConnection(connection, gruwareData).test()
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun onGruwareDataForConnection_availableUpdatesFilterEmitsNonEmptyGruwareData_emitsOtaForConnectionWithEmittedGruwareData() {
        val gruwareData = GruwareData.create(
            AvailableUpdate.create("1.2.3", "", TYPE_FIRMWARE, 0L),
            empty(TYPE_GRU),
            empty(TYPE_BOOTLOADER),
            empty(TYPE_DSP)
        )

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withFirmwareVersion("1.0.0")
            .withGruDataVersion("0.1.0")
            .build()

        val emittedGruwareData = GruwareData.create(
            AvailableUpdate.create("4.5.7", "", TYPE_FIRMWARE, 0L),
            empty(TYPE_GRU),
            AvailableUpdate.create("1.6.0", "", TYPE_BOOTLOADER, 0L),
            empty(TYPE_DSP)
        )
        whenever(gruwareFilter.filterAvailableUpdates(connection, gruwareData))
            .thenReturn(Single.just(emittedGruwareData))

        val expectedOtaForConnection = OtaForConnection(connection, STANDARD, emittedGruwareData)

        otaChecker.onGruwareDataForConnection(connection, gruwareData).test()
            .assertValue(expectedOtaForConnection)
    }

    /*
    UTILS
     */

    private fun mockServiceConnections(connections: List<KLTBConnection>) {
        val service: KolibreeService = mock()
        whenever(service.knownConnections).thenReturn(connections)
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
    }
}
