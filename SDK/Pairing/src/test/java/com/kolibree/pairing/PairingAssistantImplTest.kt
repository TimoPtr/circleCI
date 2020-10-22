package com.kolibree.pairing

import android.content.Context
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.android.sdk.scan.ToothbrushApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.scan.ToothbrushApp.MAIN
import com.kolibree.android.sdk.scan.ToothbrushApp.UNKNOWN
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.android.test.TestForcedException
import com.kolibree.pairing.assistant.PairingAssistantImpl
import com.kolibree.pairing.assistant.PairingAssistantImpl.AvailableDevice
import com.kolibree.pairing.session.PairingSession
import com.kolibree.pairing.session.PairingSessionCreator
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [PairingAssistantImpl] test units
 */
class PairingAssistantImplTest {

    private var scanner: ToothbrushScanner = mock()
    private var pairingSessionCreator: PairingSessionCreator = mock()
    private var accountToothbrushRepository: AccountToothbrushRepository = mock()
    private var serviceProvider: ServiceProvider = mock()

    private lateinit var pairingAssistant: PairingAssistantImpl

    @Before
    fun before() {
        val context: Context = mock()
        val kolibreeConnector: InternalKolibreeConnector = mock()
        whenever(context.applicationContext).thenReturn(context)

        val toothbrushScannerFactory: ToothbrushScannerFactory = mock()
        doReturn(scanner).whenever(toothbrushScannerFactory).getCompatibleBleScanner()
        pairingAssistant = spy(
            PairingAssistantImpl(
                toothbrushScannerFactory,
                accountToothbrushRepository,
                serviceProvider,
                kolibreeConnector,
                pairingSessionCreator
            )
        )
    }

    @Test
    fun subscribingToScannerObservable_callsStartScan() {
        val testObserver = pairingAssistant.scannerObservable().test()
        assertEquals(1, pairingAssistant.scanResultObservableObserverCount.get().toLong())
        verify(scanner).startScan(any(), any<Boolean>())
        testObserver.dispose()
    }

    @Test
    fun subscribingTwiceToScannerObservable_callsStartScanOnlyOnce() {
        val testObserver1 = pairingAssistant.scannerObservable().test()
        val testObserver2 = pairingAssistant.scannerObservable().test()
        assertEquals(2, pairingAssistant.scanResultObservableObserverCount.get().toLong())
        verify(scanner).startScan(any(), any<Boolean>())
        testObserver1.dispose()
        testObserver2.dispose()
    }

    @Test
    fun disposingScannerObservable_callsStopScan() {
        val testObserver = pairingAssistant.scannerObservable().test()
        testObserver.dispose()
        verify(scanner).stopScan(any())
        assertEquals(0, pairingAssistant.scanResultObservableObserverCount.get().toLong())
    }

    @Test
    fun disposingScannerObservableWithOneObserverLeft_doesNotCallsStopScan() {
        val testObserver1 = pairingAssistant.scannerObservable().test()
        val testObserver2 = pairingAssistant.scannerObservable().test()
        testObserver1.dispose()
        verify(scanner, never()).stopScan(pairingAssistant)
        assertEquals(1, pairingAssistant.scanResultObservableObserverCount.get().toLong())
        testObserver2.dispose()
    }

    /*
    PAIR
     */

    @Test
    fun pair_invokesPairingSessionProvider_createAndStopScanAndRemovesCallback() {
        val pairingSession: PairingSession = mock()
        doReturn(Single.just(pairingSession))
            .whenever(pairingSessionCreator).create(any<String>(), any(), any<String>())
        val result: ToothbrushScanResult = mockResult()
        pairingAssistant.pair(result).test()
        verify(pairingSessionCreator)
            .create(result.mac, result.model, result.name)
        verify(scanner).stopScan(pairingAssistant)
        verify(scanner, never()).startScan(eq(pairingAssistant), any<Boolean>())
    }

    @Test
    fun pair_creationFailed_restartsScanIfSomeoneIsSubscribed() {
        doReturn(Single.error<PairingSession>(IllegalArgumentException()))
            .whenever(pairingSessionCreator).create(any<String>(), any(), any<String>())
        val result: ToothbrushScanResult = mockResult()
        pairingAssistant.scannerObservable().test()
        pairingAssistant.pair(result).test()
        verify(pairingSessionCreator).create(result.mac, result.model, result.name)
        verify(scanner).stopScan(any())
        verify(scanner, times(2)).startScan(any(), any<Boolean>())
    }

    @Test
    fun pair_creationFailed_doesNotRestartScanIfNobodyIsSubscribed() {
        doReturn(Single.error<PairingSession>(Exception("Test forced error")))
            .whenever(pairingSessionCreator).create(any<String>(), any(), any<String>())
        val result: ToothbrushScanResult = mockResult()

        doReturn(Completable.complete()).whenever(pairingAssistant).unpair(any<String>())

        pairingAssistant.pair(result).test()

        verify(pairingSessionCreator).create(result.mac, result.model, result.name)
        verify(scanner).stopScan(pairingAssistant)
        verify(scanner, never()).startScan(eq(pairingAssistant), any<Boolean>())
    }

    @Test
    fun `pair invokes unpair if there's an error`() {
        doReturn(Single.error<PairingSession>(Exception("Test forced error")))
            .whenever(pairingSessionCreator).create(any<String>(), any(), any<String>())
        val result: ToothbrushScanResult = mockResult()

        doReturn(Completable.complete()).whenever(pairingAssistant).unpair(any<String>())

        pairingAssistant.pair(result).test()

        verify(pairingAssistant).unpair(DEFAULT_MAC)
    }

    /*
    START SCAN AND ADD SCAN CALLBACK
     */

    @Test
    fun startScan_startsBleScannerAndSubscribesToIt() {
        pairingAssistant.startScan()
        verify(scanner).startScan(eq(pairingAssistant), any<Boolean>())
    }

    @Test
    fun stopScanAndRemoveScanCallback_stopsBleScannerAndUnsubscribesFromIt() {
        pairingAssistant.stopScan()
        verify(scanner).stopScan(pairingAssistant)
    }

    /*
    UNPAIR
     */

    @Test
    fun unpair_invokes_pairingRepository_invokesRemoveAfterForget() {
        val mac = "mAc::123::000"
        val service = mock<KolibreeService>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))

        val inOrder = inOrder(service, accountToothbrushRepository)

        pairingAssistant.unpair(mac).test()

        inOrder.verify(service).forget(mac)
        inOrder.verify(accountToothbrushRepository).remove(mac)
    }

    @Test
    fun unpair_serviceForgetFails_neverInvokesRemoveIfForgetCrashes() {
        val mac = "mAc::123::000"
        val service = mock<KolibreeService>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))

        whenever(service.forget(mac)).thenAnswer { throw TestForcedException() }

        pairingAssistant.unpair(mac).test().assertError(Exception::class.java)

        verify(accountToothbrushRepository, never()).remove(mac)
    }

    @Test
    fun valueOfTICKER_PERIOD() {
        assertEquals(400L, PairingAssistantImpl.TICKER_PERIOD)
    }

    @Test
    fun isOutDated_15seconds_true() {
        val lastSeen = 8599L
        val availableDevice = AvailableDevice(mockResult(), 8599L)
        assertTrue(pairingAssistant.isOutdated(lastSeen + 15000, availableDevice))
    }

    @Test
    fun isOutDated_10seconds_false() {
        val lastSeen = 8600L
        val availableDevice = AvailableDevice(mockResult(), lastSeen)
        assertFalse(pairingAssistant.isOutdated(lastSeen + 10000L, availableDevice))
    }

    @Test
    fun onRealTimeScannerObservableSubscription_callsSubscriptionMethods() {
        pairingAssistant.onRealTimeScannerObservableSubscription()
        verify(pairingAssistant).subscribeToScannerObservable()
        verify(pairingAssistant).subscribeToTickerObservable()
        assertEquals(2, pairingAssistant.rtResultsDisposable.size())
    }

    @Test
    fun onRealTimeScannerObservableDisposition_disposesRealTimeObservables() {
        pairingAssistant.onRealTimeScannerObservableDisposition()
        assertEquals(0, pairingAssistant.rtResultsDisposable.size())
    }

    @Test
    fun restartScanIfAnySubscriber_withNobodySubscribed_doesNothing() {
        assertEquals(0, pairingAssistant.scanResultObservableObserverCount.get())
        pairingAssistant.restartScanIfAnySubscriber()
        verify(pairingAssistant, never()).startScan()
    }

    @Test
    fun restartScanIfAnySubscriber_withSomeoneSubscribed_callsStartScanAndAddScanCallback() {
        pairingAssistant.scanResultObservableObserverCount.set(1)
        pairingAssistant.restartScanIfAnySubscriber()
        verify(pairingAssistant).startScan()
    }

    @Test
    fun addNewScanResult_replacesExistingResult() {
        val result1 = mockResult(false)
        val result2 = mockResult(true)
        pairingAssistant.addNewScanResult(result1)
        assertFalse(
            pairingAssistant.availableDeviceList[DEFAULT_MAC]!!.toothbrushScanResult
                .isSeamlessConnectionAvailable
        )
        pairingAssistant.addNewScanResult(result2)
        assertTrue(
            pairingAssistant.availableDeviceList[DEFAULT_MAC]!!.toothbrushScanResult
                .isSeamlessConnectionAvailable
        )
    }

    @Test
    fun cleanList_dataSetChanged() {
        val result1 = mockResult("mac1")
        pairingAssistant.availableDeviceList["mac1"] = AvailableDevice(result1, 99L)
        assertTrue(pairingAssistant.cleanList())
        assertEquals(0, pairingAssistant.availableDeviceList.size)
    }

    @Test
    fun cleanList_dataSetNotChanged() {
        val result1 = mockResult("mac1")
        pairingAssistant.availableDeviceList["mac1"] = AvailableDevice(result1)
        assertFalse(pairingAssistant.cleanList())
        assertEquals(1, pairingAssistant.availableDeviceList.size)
    }

    @Test
    fun realTimeScannerObservable_isShared() {
        val mac = "iLoveOldWiseManButHeNeedsToRest!"
        val observable1 = pairingAssistant.realTimeScannerObservable()
        val observable2 = pairingAssistant.realTimeScannerObservable()
        assertEquals(observable1, observable2)

        val testObserver1 = observable1.test()
        val testObserver2 = observable2.test()
        pairingAssistant.availableDeviceList[mac] = AvailableDevice(mockResult(mac))
        pairingAssistant.emitScanResultList()

        val listValue1: List<ToothbrushScanResult> = testObserver1.values()[0]
        val listValue2: List<ToothbrushScanResult> = testObserver2.values()[0]

        // Kotlin's referential equality asserts that the observable is shared, since we received
        // the exact same object (two references to the same object)
        assertTrue(listValue1 === listValue2)
    }

    /*
    CONNECT AND LINK BLUE
     */

    @Test
    fun `connectAndBlinkBlue is delegated to PairingSessionCreator with mac from getRealMacAddress`() {
        val mac = "mY:5u:P3:Rr:M4:Cc"
        val model = CONNECT_M1
        val name = "MY_TB_NAME"
        val bootloader = false
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.model).thenReturn(model)
        whenever(scanResult.name).thenReturn(name)
        whenever(scanResult.isRunningBootloader).thenReturn(bootloader)

        val expectedMac = "new mac"
        doReturn(expectedMac).whenever(pairingAssistant).getRealMacAddress(scanResult)

        val expectedConnection = mock<KLTBConnection>()
        whenever(
            pairingSessionCreator.connectAndBlinkBlue(
                expectedMac,
                model,
                name
            )
        ).thenReturn(Single.just(expectedConnection))

        pairingAssistant.connectAndBlinkBlue(scanResult).test().assertValue(expectedConnection)

        verify(pairingSessionCreator).connectAndBlinkBlue(expectedMac, model, name)
    }

    @Test
    fun `connectAndBlinkBlue invokes unpair if there's an error`() {
        val mac = "mY:5u:P3:Rr:M4:Cc"
        val model = CONNECT_M1
        val name = "MY_TB_NAME"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.model).thenReturn(model)
        whenever(scanResult.name).thenReturn(name)

        whenever(
            pairingSessionCreator.connectAndBlinkBlue(
                mac,
                model,
                name
            )
        ).thenReturn(Single.error(Exception("Test forced error")))

        doReturn(Completable.complete()).whenever(pairingAssistant).unpair(mac)

        pairingAssistant.connectAndBlinkBlue(scanResult).test()

        verify(pairingAssistant).unpair(mac)
    }

    @Test
    fun `connectAndBlinkBlue emits error if there's an error`() {
        val mac = "mY:5u:P3:Rr:M4:Cc"
        val model = CONNECT_M1
        val name = "MY_TB_NAME"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.model).thenReturn(model)
        whenever(scanResult.name).thenReturn(name)

        val expectedError = Exception("Test forced error")
        whenever(
            pairingSessionCreator.connectAndBlinkBlue(
                mac,
                model,
                name
            )
        ).thenReturn(Single.error(expectedError))

        doReturn(Completable.complete()).whenever(pairingAssistant).unpair(mac)

        pairingAssistant.connectAndBlinkBlue(scanResult).test().assertError(expectedError)
    }

    /*
    GET REAL MAC ADDRESS
     */

    @Test
    fun getRealMacAddress_doesNothingForAra() {
        val mac = "01:02:03:04:05"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(ARA)
        whenever(scanResult.mac).thenReturn(mac)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForAra_bootloader() {
        val mac = "01:02:03:04:05"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(ARA)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(UNKNOWN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForE1() {
        val mac = "01:02:03:04:05"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_E1)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(UNKNOWN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForE1_bootloader() {
        val mac = "01:02:03:04:05"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_E1)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(UNKNOWN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForM1() {
        val mac = "01:02:03:04:05"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_M1)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(MAIN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_changesForM1_bootloader() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_M1)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(DFU_BOOTLOADER)
        assertEquals("01:02:03:04:05:05", pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForE2() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_E2)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(MAIN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_changesForE2_bootloader() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_E2)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(DFU_BOOTLOADER)
        assertEquals("01:02:03:04:05:05", pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForB1() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_B1)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(MAIN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_changesForB1_bootloader() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(CONNECT_B1)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(DFU_BOOTLOADER)
        assertEquals("01:02:03:04:05:05", pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_doesNothingForPQL() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(PLAQLESS)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(MAIN)
        assertEquals(mac, pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun getRealMacAddress_changesForPQL_bootloader() {
        val mac = "01:02:03:04:05:06"
        val scanResult = mock<ToothbrushScanResult>()
        whenever(scanResult.model).thenReturn(PLAQLESS)
        whenever(scanResult.mac).thenReturn(mac)
        whenever(scanResult.toothbrushApp).thenReturn(DFU_BOOTLOADER)
        assertEquals("01:02:03:04:05:05", pairingAssistant.getRealMacAddress(scanResult))
    }

    @Test
    fun `getPairingSession return pairingSession with connection`() {
        val connection = mock<KLTBConnection>()
        val pairingSession = mock<PairingSession>()
        whenever(pairingSessionCreator.create(connection)).thenReturn(pairingSession)
        whenever(pairingSession.connection()).thenReturn(connection)
        val result = pairingAssistant.createPairingSession(connection)
        assertEquals(connection, result.connection())
    }

    /*
    UTILS
     */

    private fun mockResult(
        seamlessConnectionAvailable: Boolean,
        mac: String
    ): ToothbrushScanResult {
        return mock {
            whenever(it.isRunningBootloader).thenReturn(false)
            whenever(it.isSeamlessConnectionAvailable).thenReturn(seamlessConnectionAvailable)
            whenever(it.mac).thenReturn(mac)
            whenever(it.model).thenReturn(ARA)
            whenever(it.name).thenReturn("My Ara")
            whenever(it.ownerDevice).thenReturn(0xFFFFFFFF)
        }
    }

    private fun mockResult(mac: String): ToothbrushScanResult {
        return mockResult(false, mac)
    }

    private fun mockResult(seamlessConnectionAvailable: Boolean): ToothbrushScanResult {
        return mockResult(seamlessConnectionAvailable, DEFAULT_MAC)
    }

    private fun mockResult(): ToothbrushScanResult {
        return mockResult(true, DEFAULT_MAC)
    }

    companion object {
        private const val DEFAULT_MAC = "MAC"
    }
}
