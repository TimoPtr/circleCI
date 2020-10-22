package com.kolibree.pairing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.pairing.exception.KolibreeServiceDisconnectedException
import com.kolibree.pairing.session.PairingSessionCreatorImpl
import com.kolibree.pairing.session.PairingSessionImpl
import com.kolibree.sdkws.core.GruwareRepository
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * [PairingSessionCreatorImplTest] test units
 */
class PairingSessionCreatorImplTest : BaseUnitTest() {

    private val gruwareRepository: GruwareRepository = mock()
    private val serviceProvider: ServiceProvider = mock()
    private val connectionProvider: KLTBConnectionProvider = mock()
    private val connector: IKolibreeConnector = mock()
    private val accountToothbrushRepository: AccountToothbrushRepository = mock()
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase = mock()

    private val pairingSessionCreator = spy(
        PairingSessionCreatorImpl(
            connector,
            gruwareRepository,
            accountToothbrushRepository,
            serviceProvider,
            connectionProvider,
            synchronizeBrushingModeUseCase
        )
    )

    /*
    CREATE
     */
    @Test
    fun create_activeConnectionSingleEmitsError_returnsError() {
        val scanResult = mockToothbrushScanResult()
        val expectedError = Throwable("Test forced error")
        doReturn(Single.error<KLTBConnection>(expectedError))
            .whenever(pairingSessionCreator)
            .activeConnectionSingle(any(), any(), any())

        pairingSessionCreator.create(scanResult.mac, scanResult.model, scanResult.name)
            .test()
            .assertError(expectedError)
    }

    @Test
    fun create_activeConnectionSingleEmitsConnection_invokesPairToActiveProfileAndPersist() {
        val scanResult = mockToothbrushScanResult()
        val connection: KLTBConnection = mock()
        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .activeConnectionSingle(any(), any(), any())

        val pairToActiveProfileSubject = SingleSubject.create<KLTBConnection>()
        doReturn(pairToActiveProfileSubject).whenever(pairingSessionCreator)
            .pairToActiveProfileAndPersist(connection)
        pairingSessionCreator.create(scanResult.mac, scanResult.model, scanResult.name)
            .test()
            .assertNoErrors()
            .assertNoValues()
            .assertNotComplete()

        verify(pairingSessionCreator).pairToActiveProfileAndPersist(connection)
    }

    @Test
    fun create_activeConnectionSingleEmitsConnection_pairToActiveProfileAndStopVibratorAndPersistEmitsConnection_createsPairingSession() {
        val scanResult = mockToothbrushScanResult()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withVibration(true)
            .withSupportMonitorCurrent()
            .build()

        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .activeConnectionSingle(any(), any(), any())

        val pairToActiveProfileSubject: SingleSubject<KLTBConnection> =
            SingleSubject.create<KLTBConnection>()
        doReturn(pairToActiveProfileSubject).whenever(pairingSessionCreator)
            .pairToActiveProfileAndPersist(connection)

        val observer =
            pairingSessionCreator.create(scanResult.mac, scanResult.model, scanResult.name)
                .test()
                .assertNoErrors()
                .assertNoValues()
                .assertNotComplete()

        pairToActiveProfileSubject.onSuccess(connection)

        val expectedPairingSession = PairingSessionImpl(
            connection, accountToothbrushRepository,
            gruwareRepository, synchronizeBrushingModeUseCase
        )

        observer.assertValue(expectedPairingSession).assertComplete()
        verify(connection.vibrator()).off()
    }

    @Test
    fun `create PairingSession with connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(true)
                .withSupportMonitorCurrent()
                .build()
        val pairingSession = pairingSessionCreator.create(connection)
        assertEquals(connection, pairingSession.connection())
    }

    @Test
    fun `create PairingSession with connection, toothbrushFacade is NotNull`() {
        val connection = mock<KLTBConnection>()
        whenever(connection.toothbrush()).thenReturn(mock())
        whenever(connection.userMode()).thenReturn(mock())
        whenever(connection.brushing()).thenReturn(mock())
        whenever(connection.parameters()).thenReturn(mock())
        val pairingSession = pairingSessionCreator.create(connection)
        assertNotNull(pairingSession.toothbrush())
    }

    /*
    MAYBE DISCARD OFFLINE AND STOP VIBRATOR
     */

    @Test
    fun `When maybeDiscardOfflineAndStopVibrator and connection is in bootloader, we return connection without attempting any interaction`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withBootloader(true).build()

        pairingSessionCreator.maybeDiscardOfflineAndStopVibrator(connection).test()
            .assertValue(connection)

        verifyNoMoreInteractions(connection.vibrator())
    }

    @Test
    fun `When maybeDiscardOfflineAndStopVibrator and is not bootloader, we discard offline brushing and then stop vibration`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withVibration(true)
            .withSupportMonitorCurrent()
            .build()

        val inOrder = inOrder(connection.brushing(), connection.vibrator())

        pairingSessionCreator.maybeDiscardOfflineAndStopVibrator(connection).test()
            .assertValue(connection)

        inOrder.verify(connection.brushing()).monitorCurrent()
        inOrder.verify(connection.vibrator()).off()
    }

    /*
    CONNECT AND BLINK BLUE
     */

    @Test
    fun connectAndBlinkBlue_connectsAndSendsPlayLedSignal() {
        val toothbrush: Toothbrush = mock()
        whenever(toothbrush.playLedSignal(any(), any(), any(), any(), any(), any()))
            .thenReturn(Completable.complete())
        val connection: KLTBConnection = mock()
        whenever(connection.toothbrush()).thenReturn(toothbrush)
        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .activeConnectionSingle(any(), any(), any())

        pairingSessionCreator.connectAndBlinkBlue("mac", ARA, "name")
            .test()
            .assertComplete()

        verify(toothbrush).playLedSignal(any(), any(), any(), any(), any(), any())
        verify(pairingSessionCreator, never()).persistToothbrush(any())
    }

    @Test
    fun connectAndBlinkBlue_neverPersistsToothbrush() {
        val toothbrush: Toothbrush = mock()
        whenever(toothbrush.playLedSignal(any(), any(), any(), any(), any(), any()))
            .thenReturn(Completable.complete())
        val connection: KLTBConnection = mock()
        whenever(connection.toothbrush()).thenReturn(toothbrush)
        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .activeConnectionSingle(any(), any(), any())

        pairingSessionCreator.connectAndBlinkBlue("mac", ARA, "name")
            .test()
            .assertComplete()

        verify(pairingSessionCreator, never()).persistToothbrush(any())
    }

    @Test
    fun connectAndBlinkBlue_returnsConnection() {
        val toothbrush: Toothbrush = mock()
        whenever(toothbrush.playLedSignal(any(), any(), any(), any(), any(), any()))
            .thenReturn(Completable.complete())
        val connection: KLTBConnection = mock()
        whenever(connection.toothbrush()).thenReturn(toothbrush)
        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .activeConnectionSingle(any(), any(), any())

        pairingSessionCreator.connectAndBlinkBlue("mac", ARA, "name")
            .test()
            .assertValue(connection)
    }

    /*
    PAIR TO ACTIVE PROFILE AND PERSIST
     */
    @Test
    fun pairToActiveProfileAndPersist_updateProfileIdUnlessBootloaderError_emitsError() {
        val connection: KLTBConnection = mock()
        val toothbrush: Toothbrush = mock()
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        val expectedError = Throwable("Test forced error")
        doReturn(Completable.error(expectedError)).whenever(pairingSessionCreator)
            .updateProfileIdUnlessBootloader(connection)
        doReturn(Completable.complete()).whenever(pairingSessionCreator)
            .persistToothbrush(toothbrush)

        pairingSessionCreator.pairToActiveProfileAndPersist(connection).test()
            .assertError(expectedError)
    }

    @Test
    fun pairToActiveProfileAndPersist_updateProfileIdUnlessBootloaderCompletes_persistToothbrushError_emitsError() {
        val connection: KLTBConnection = mock()
        val toothbrush: Toothbrush = mock()
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        val expectedError = Throwable("Test forced error")
        doReturn(Completable.complete()).whenever(pairingSessionCreator)
            .updateProfileIdUnlessBootloader(connection)
        doReturn(Completable.error(expectedError)).whenever(pairingSessionCreator)
            .persistToothbrush(toothbrush)

        pairingSessionCreator.pairToActiveProfileAndPersist(connection)
            .test()
            .assertError(expectedError)
    }

    @Test
    fun pairToActiveProfileAndPersist_updateProfileIdUnlessBootloaderCompletes_persistToothbrushCompletes_emitsConnection() {
        val connection: KLTBConnection = mock()
        val toothbrush: Toothbrush = mock()
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        doReturn(Completable.complete()).whenever(pairingSessionCreator)
            .updateProfileIdUnlessBootloader(connection)
        doReturn(Completable.complete()).whenever(pairingSessionCreator)
            .persistToothbrush(toothbrush)

        pairingSessionCreator.pairToActiveProfileAndPersist(connection)
            .test()
            .assertNoErrors()
            .assertValue(connection)
    }

    /*
    CREATE CONNECTION SINGLE
     */
    @Test
    fun createConnectionSingle_serviceProviderReturnsError_emitsKolibreeServiceDisconnectedException() {
        val scanResult = mockToothbrushScanResult()

        whenever(serviceProvider.connectOnce()).thenReturn(Single.error(Throwable("Test forced error")))

        pairingSessionCreator.createConnectionSingle(
            scanResult.mac,
            scanResult.model,
            scanResult.name
        )
            .test()
            .assertError(KolibreeServiceDisconnectedException::class.java)
    }

    @Test
    fun createConnectionSingle_serviceProviderEmitsService_createConnectionEmitsError_emitsKolibreeServiceDisconnectedException() {
        val scanResult = mockToothbrushScanResult()

        val service: KolibreeService = mock()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
        whenever(service.createAndEstablishConnection(scanResult)).thenAnswer { throw Throwable("Test forced error") }

        pairingSessionCreator.createConnectionSingle(
            scanResult.mac,
            scanResult.model,
            scanResult.name
        )
            .test()
            .assertError(KolibreeServiceDisconnectedException::class.java)
    }

    @Test
    fun createConnectionSingle_serviceProviderEmitsService_returnsValueFromCreateConnectionOnService() {
        val scanResult = mockToothbrushScanResult()
        val expectedConnection: KLTBConnection = mock()

        val service: KolibreeService = mock()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
        whenever(service.createAndEstablishConnection(any(), any(), any()))
            .thenReturn(expectedConnection)

        pairingSessionCreator.createConnectionSingle(
            scanResult.mac,
            scanResult.model,
            scanResult.name
        )
            .test()
            .assertValue(expectedConnection)
    }

    /*
    PERSIST TOOTHBRUSH
     */
    @Test
    fun persistToothbrush_invokesAssociate() {
        val expectedProfileId = 78L
        doReturn(expectedProfileId).whenever(pairingSessionCreator).currentProfileId()
        val expectedAccountId = 2L
        whenever(connector.accountId).thenReturn(expectedAccountId)

        val toothbrush: Toothbrush = mock()
        whenever(toothbrush.model).thenReturn(CONNECT_E1)
        whenever(toothbrush.mac).thenReturn("mac")
        whenever(toothbrush.getName()).thenReturn("name")

        whenever(
            accountToothbrushRepository.associate(
                any<Toothbrush>(),
                any(),
                any()
            )
        ).thenAnswer { invocation ->
            Single.just(
                AccountToothbrush(
                    accountId = invocation.getArgument(1),
                    profileId = invocation.getArgument(2),
                    mac = toothbrush.mac,
                    name = toothbrush.getName(),
                    model = toothbrush.model
                )
            )
        }

        pairingSessionCreator.persistToothbrush(toothbrush).test().assertComplete()

        verify(accountToothbrushRepository).associate(
            toothbrush,
            expectedAccountId,
            expectedProfileId
        )
    }

    /*
    ACTIVE CONNECTION SINGLE
     */
    @Test
    fun activeConnectionSingle_createConnectionSingleEmitsError_emitsError() {
        val scanResult = mockToothbrushScanResult()

        doReturn(Single.error<KLTBConnection>(KolibreeServiceDisconnectedException()))
            .whenever(pairingSessionCreator)
            .createConnectionSingle(any(), any(), any())

        pairingSessionCreator.activeConnectionSingle(
            scanResult.mac,
            scanResult.model,
            scanResult.name
        )
            .test()
            .assertError(KolibreeServiceDisconnectedException::class.java)
    }

    @Test
    fun activeConnectionSingle_createConnectionSingleEmitsConnection_delegatesConnectionFetchToKLTBConnectionProvider() {
        val scanResult = mockToothbrushScanResult()

        val connection: KLTBConnection = mock()
        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .createConnectionSingle(any(), any(), any())

        pairingSessionCreator.activeConnectionSingle(
            scanResult.mac,
            scanResult.model,
            scanResult.name
        ).test()

        verify(connectionProvider).existingActiveConnection(mac)
    }

    @Test
    fun activeConnectionSingle_dispose_invokesDisconnect() {
        val scanResult = mockToothbrushScanResult()

        val connection: KLTBConnection = mock()
        doReturn(Single.just(connection))
            .whenever(pairingSessionCreator)
            .createConnectionSingle(any(), any(), any())

        val activeConnectionSubject: Single<KLTBConnection> = SingleSubject.create()
        whenever(connectionProvider.existingActiveConnection(scanResult.mac)).thenReturn(
            activeConnectionSubject
        )

        val observer =
            pairingSessionCreator.activeConnectionSingle(
                scanResult.mac,
                scanResult.model,
                scanResult.name
            ).test()

        verify(connection, never()).disconnect()

        observer.dispose()

        verify(connection).disconnect()
    }

    /*
    UPDATE PROFILE ID
     */
    @Test
    fun updateProfileId_invokesSetMultimodeFalseAndSetUserIdForCurrentProfile() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withSupportForSetOperationsOnUserMode()
            .build()

        val expectedUserId = 45L
        doReturn(expectedUserId).whenever(pairingSessionCreator).currentProfileId()

        pairingSessionCreator.updateProfileId(connection).test().assertComplete()

        verify(connection.userMode()).setProfileId(45L)
    }

    /*
    UPDATE PROFILE UNLESS DFU_BOOTLOADER
     */
    @Test
    fun updateProfileIdUnlessBootloader_isBootloaderTrue_completeAndNeverInvokes() {
        val connection: KLTBConnection = mock()

        doReturn(true).whenever(pairingSessionCreator).isInBootloader(connection)

        pairingSessionCreator.updateProfileIdUnlessBootloader(connection).test().assertComplete()

        verify(pairingSessionCreator, never()).updateProfileId(any())
    }

    @Test
    fun updateProfileIdUnlessBootloader_isBootloaderFalse_returnsUpdateProfileId() {
        val connection: KLTBConnection = mock()

        doReturn(false).whenever(pairingSessionCreator).isInBootloader(connection)

        val expectedCompletable = Completable.complete()
        doReturn(expectedCompletable).whenever(pairingSessionCreator).updateProfileId(connection)

        val returnedCompletable = pairingSessionCreator.updateProfileIdUnlessBootloader(connection)
        assertEquals(expectedCompletable, returnedCompletable)
    }

    /*
    IS IN DFU_BOOTLOADER
     */

    @Test
    fun isInBootloader_bootloader_returnTrue() {
        val connection = mockConnectionBootloader(true)

        Assert.assertTrue(pairingSessionCreator.isInBootloader(connection))
    }

    @Test
    fun isInBootloader_notInBootloader_returnFalse() {
        val connection = mockConnectionBootloader(false)

        Assert.assertFalse(pairingSessionCreator.isInBootloader(connection))
    }

    private fun mockToothbrushScanResult(): ToothbrushScanResult {
        val tb = mock<ToothbrushScanResult>()
        whenever(tb.mac).thenReturn(mac)
        whenever(tb.model).thenReturn(CONNECT_E1)
        whenever(tb.name).thenReturn(name)
        whenever(tb.isRunningBootloader).thenReturn(false)
        return tb
    }

    private fun mockConnectionBootloader(isBootloader: Boolean): KLTBConnection {
        val connection = mock<KLTBConnection>()
        val toothbrush = mock<Toothbrush>()
        whenever(toothbrush.isRunningBootloader).thenReturn(isBootloader)
        whenever(connection.toothbrush()).thenReturn(toothbrush)
        return connection
    }

    companion object {
        private const val mac = "dasdsa"
        private const val name = "name"
    }
}
