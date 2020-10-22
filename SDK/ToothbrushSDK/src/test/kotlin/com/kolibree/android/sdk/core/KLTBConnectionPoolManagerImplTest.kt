package com.kolibree.android.sdk.core

import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.driver.KLTBDriverFactory
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.scan.BluetoothSessionResetterRegisterer
import com.kolibree.android.sdk.scan.ConnectionScannedTracker
import com.kolibree.android.sdk.scan.EstablishConnectionFilter
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_NAME
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class KLTBConnectionPoolManagerImplTest : BaseUnitTest() {
    private val DEFAULT_TOOTHBRUSH_MODEL = ToothbrushModel.CONNECT_E1

    private lateinit var poolManager: KLTBConnectionPoolManagerImpl

    private val context: Context = mock()

    private val driverFactory: KLTBDriverFactory = mock()

    private val toothbrushScanner: ToothbrushScanner = mock()

    private val bluetoothUtils: IBluetoothUtils = mock()

    private val toothbrushRepository: ToothbrushRepository = mock()

    private val doctorFactory: KLTBConnectionDoctorFactory = mock()

    private val establishConnectionFilter: EstablishConnectionFilter = mock()

    private val connectionScannedTracker: ConnectionScannedTracker = mock()

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)

        poolManager = spy(createPoolManager())

        doReturn(bluetoothUtils).whenever(poolManager).bluetoothUtils()
        doReturn(toothbrushScanner).whenever(poolManager).toothbrushScanner(any())
    }

    private fun createPoolManager(): KLTBConnectionPoolManagerImpl {
        return KLTBConnectionPoolManagerImpl(
            context,
            driverFactory,
            toothbrushRepository,
            doctorFactory,
            establishConnectionFilter,
            connectionScannedTracker
        )
    }

    /*
    Instance init
     */
    @Test
    fun `instance init attempts to register broadcast receiver listening to bluetooth state changed`() {
        val broadcastMock = mock<BluetoothSessionResetterRegisterer>()

        KLTBConnectionPoolManagerImpl(
            context,
            driverFactory,
            toothbrushRepository,
            doctorFactory,
            establishConnectionFilter,
            connectionScannedTracker,
            broadcastMock
        )

        verify(broadcastMock).register(context)
    }

    /*
    initOnlyPreviouslyScanned
     */
    @Test
    fun `initOnlyPreviouslyScanned invoke disableScanBeforeConnect before reading toothbrushes`() {
        val listAllSubject = SingleSubject.create<List<AccountToothbrush>>()
        prepareEmptyToothbrushes(listAllSubject)

        var disableScanBeforeConnectInoked = false
        doAnswer {
            assertFalse(listAllSubject.hasObservers())

            disableScanBeforeConnectInoked = true
        }
            .whenever(establishConnectionFilter)
            .disableScanBeforeConnect()

        val observer = poolManager.initOnlyPreviouslyScanned().test().assertNotComplete()

        verify(establishConnectionFilter).disableScanBeforeConnect()

        assertTrue(disableScanBeforeConnectInoked)

        listAllSubject.onSuccess(listOf())

        observer.assertComplete()

        verify(establishConnectionFilter, never()).enableScanBeforeConnect()
        verify(connectionScannedTracker, never()).clear()
    }

    @Test
    fun `initOnlyPreviouslyScanned never invokes connectionScannedTracker clear if pool is already initialized`() {
        prepareEmptyToothbrushes()

        poolManager.init().test()

        verify(connectionScannedTracker, times(1)).clear()

        assertTrue(poolManager.initialized.get())

        poolManager.initOnlyPreviouslyScanned().test()

        verify(connectionScannedTracker, times(1)).clear()
    }

    @Test
    fun `initOnlyPreviouslyScanned never invokes disableScanBeforeConnect if pool is already initialized`() {
        prepareEmptyToothbrushes()

        poolManager.init().test()

        assertTrue(poolManager.initialized.get())

        poolManager.initOnlyPreviouslyScanned().test()

        verify(establishConnectionFilter, never()).disableScanBeforeConnect()
    }

    @Test
    fun `initOnlyPreviouslyScanned invokes disableScanBeforeConnect only once, even on multiple subscriptions`() {
        prepareEmptyToothbrushes()

        poolManager.initOnlyPreviouslyScanned().test()
        poolManager.initOnlyPreviouslyScanned().test()
        poolManager.initOnlyPreviouslyScanned().test()

        verify(establishConnectionFilter).disableScanBeforeConnect()
    }

    /*
    INIT
     */
    @Test
    fun `init invokes enableScanBeforeConnect before reading toothbrushes`() {
        val listAllSubject = SingleSubject.create<List<AccountToothbrush>>()
        prepareEmptyToothbrushes(listAllSubject)

        var enableScanBeforeConnectInvoked = false
        doAnswer {
            assertFalse(listAllSubject.hasObservers())

            enableScanBeforeConnectInvoked = true
        }
            .whenever(establishConnectionFilter)
            .enableScanBeforeConnect()

        val observer = poolManager.init().test().assertNotComplete()

        verify(establishConnectionFilter).enableScanBeforeConnect()

        assertTrue(enableScanBeforeConnectInvoked)

        listAllSubject.onSuccess(listOf())

        observer.assertComplete()

        verify(establishConnectionFilter, never()).disableScanBeforeConnect()
    }

    @Test
    fun `init invokes connectionScannedTracker clear before reading toothbrushes`() {
        val listAllSubject = SingleSubject.create<List<AccountToothbrush>>()
        prepareEmptyToothbrushes(listAllSubject)

        var clearInvoked = false
        doAnswer {
            assertFalse(listAllSubject.hasObservers())

            clearInvoked = true
        }
            .whenever(connectionScannedTracker)
            .clear()

        val observer = poolManager.init().test().assertNotComplete()

        verify(connectionScannedTracker).clear()

        assertTrue(clearInvoked)

        listAllSubject.onSuccess(listOf())

        observer.assertComplete()
    }

    @Test
    fun `init never invokes enableScanBeforeConnect if pool is already initialized`() {
        prepareEmptyToothbrushes()

        poolManager.initOnlyPreviouslyScanned().test()

        assertTrue(poolManager.initialized.get())

        poolManager.init().test()

        verify(establishConnectionFilter, never()).enableScanBeforeConnect()
    }

    @Test
    fun `init invokes enableScanBeforeConnect only once, even on multiple subscriptions`() {
        prepareEmptyToothbrushes()

        poolManager.init().test()
        poolManager.init().test()
        poolManager.init().test()

        verify(establishConnectionFilter).enableScanBeforeConnect()
    }

    /*
    internalInit
     */
    @Test
    fun `internalInit never invokes enableScanBeforeConnect or disableScanBeforeConnect`() {
        prepareEmptyToothbrushes()

        assertFalse(poolManager.initialized.get())

        invokeInternalInit().test().assertComplete()

        verify(establishConnectionFilter, never()).disableScanBeforeConnect()
        verify(establishConnectionFilter, never()).enableScanBeforeConnect()
    }

    @Test
    fun `internalInit flags the instance as initialized only after subscription`() {
        prepareEmptyToothbrushes()

        assertFalse(poolManager.initialized.get())

        val invocation1 = invokeInternalInit()

        assertFalse(poolManager.initialized.get())

        invocation1.test().assertComplete()

        assertTrue(poolManager.initialized.get())
    }

    @Test
    fun `internalInit never subscribes to onInitSuccessCompletable or establishConnectionsSubject if already initialized`() {
        poolManager.initialized.set(true)

        val establishConnectionsSubject = CompletableSubject.create()
        doReturn(establishConnectionsSubject)
            .whenever(poolManager)
            .establishConnectionsCompletable()

        val onSuccessSubject = CompletableSubject.create()
        invokeInternalInit(completable = onSuccessSubject).test().assertComplete()

        assertFalse(onSuccessSubject.hasObservers())
        assertFalse(establishConnectionsSubject.hasObservers())
    }

    @Test
    fun `internalInit only subscribes to establishConnectionsCompletable after onInitSuccessCompletable completes`() {
        val establishConnectionsSubject = CompletableSubject.create()
        doReturn(establishConnectionsSubject)
            .whenever(poolManager)
            .establishConnectionsCompletable()

        val onSuccessSubject = CompletableSubject.create()
        val observer = invokeInternalInit(completable = onSuccessSubject).test()

        assertTrue(onSuccessSubject.hasObservers())
        assertFalse(establishConnectionsSubject.hasObservers())

        onSuccessSubject.onComplete()
        assertTrue(establishConnectionsSubject.hasObservers())

        observer.assertNotComplete()

        establishConnectionsSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `internalInit multiple subscriptions only invoke establishConnectionsCompletable once`() {
        doReturn(Completable.complete())
            .whenever(poolManager)
            .establishConnectionsCompletable()

        val invocation1 = invokeInternalInit()
        val invocation2 = invokeInternalInit()
        val invocation3 = invokeInternalInit()

        verify(poolManager, never()).establishConnectionsCompletable()

        invocation3.test().assertComplete()
        invocation1.test().assertComplete()
        invocation2.test().assertComplete()

        verify(poolManager, times(1)).establishConnectionsCompletable()
    }

    @Test
    fun `internalInit multiple subscriptions only run completable parameter of the first subscription`() {
        prepareEmptyToothbrushes()

        val subject1 = CompletableSubject.create()
        val subject2 = CompletableSubject.create()
        val subject3 = CompletableSubject.create()

        val invocation1 = invokeInternalInit(subject1)
        val invocation2 = invokeInternalInit(subject2)
        val invocation3 = invokeInternalInit(subject3)

        invocation3.test()
        invocation1.test()
        invocation2.test()

        assertTrue(subject3.hasObservers())
        assertFalse(subject2.hasObservers())
        assertFalse(subject1.hasObservers())
    }

    /*
    establishConnectionsCompletable
     */

    @Test
    fun establishConnectionsCompletable_emptyToothbrushRepository_invokesOnConnectionsReadWithEmptyList() {
        prepareEmptyToothbrushes()

        poolManager.establishConnectionsCompletable().test()

        verify(poolManager).onConnectionsRead(listOf())
    }

    @Test
    fun establishConnectionsCompletable_2ConnectionRepository_invokesOnConnectionsReadWithConnectionsNotFilteredOutByEstablishConnectionFilter() {
        val mac2 = "mac2"
        val toothbrush1 = AccountToothbrush(DEFAULT_MAC, DEFAULT_NAME, DEFAULT_TOOTHBRUSH_MODEL, 1)
        val toothbrush2 = AccountToothbrush(mac2, DEFAULT_NAME, ToothbrushModel.ARA, 1)
        val accountToothbrushList = listOf(toothbrush1, toothbrush2)
        whenever(toothbrushRepository.listAll()).thenReturn(Single.just(accountToothbrushList))

        whenever(establishConnectionFilter.canAttemptConnection(toothbrush1.mac)).thenReturn(false)
        whenever(establishConnectionFilter.canAttemptConnection(toothbrush2.mac)).thenReturn(true)

        doReturn(Completable.complete()).whenever(poolManager).onConnectionsRead(any())

        poolManager.establishConnectionsCompletable().test()

        verify(poolManager).onConnectionsRead(listOf(toothbrush2))
    }

    /*
    onConnectionsRead
     */

    @Test
    fun onConnectionsRead_emptyList_doesNothing() {
        prepareEmptyToothbrushes()

        poolManager.onConnectionsRead(listOf()).test()

        assertTrue(poolManager.connectionList.isEmpty())
    }

    @Test
    fun onConnectionsRead_singleAccountToothbrush_createsOneConnectionAndAttemptsToEstablishConnection() {
        val accountToothbrushList =
            listOf(AccountToothbrush(DEFAULT_MAC, DEFAULT_NAME, DEFAULT_TOOTHBRUSH_MODEL, 1))

        doNothing().whenever(poolManager).validateMac(any())
        val doctorList = mockInstanceCreation()

        assertTrue(poolManager.connectionList.isEmpty())

        poolManager.onConnectionsRead(accountToothbrushList).test()

        assertEquals(1, poolManager.connectionList.size)

        assertNotNull(poolManager.get(DEFAULT_MAC))

        doctorList.forEach { verify(it).init() }
    }

    @Test
    fun onConnectionsRead_2AccountToothbrushes_creates2ConnectionsAndAttemptsToEstablishConnection() {
        val mac2 = "mac2"
        val accountToothbrushList = listOf(
            AccountToothbrush(DEFAULT_MAC, DEFAULT_NAME, DEFAULT_TOOTHBRUSH_MODEL, 1),
            AccountToothbrush(mac2, DEFAULT_NAME, ToothbrushModel.ARA, 1)
        )

        doNothing().whenever(poolManager).validateMac(any())
        val expectedConnection1 =
            KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().build()
        val expectedConnection2 =
            KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().withMac(mac2).build()
        val doctorList = mockInstanceCreation(listOf(expectedConnection1, expectedConnection2))

        assertTrue(poolManager.connectionList.isEmpty())

        poolManager.onConnectionsRead(accountToothbrushList).test()

        assertEquals(2, poolManager.connectionList.size)

        assertNotNull(poolManager.get(DEFAULT_MAC))
        assertNotNull(poolManager.get(mac2))

        doctorList.forEach { verify(it).init() }
    }

    /*
    CREATE AND ESTABLISH
     */
    @Test
    fun createAndEstablish_firstInvocationWithMac_invokesCreateAndEstablish() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        val mac = DEFAULT_MAC
        val name = ""
        val model = DEFAULT_TOOTHBRUSH_MODEL

        mockInstanceCreation(listOf(connection))

        val doctor = mockDoctorInstanceCreation(connection)

        poolManager.createAndEstablish(mac, name, model)

        verify(doctor).init()
    }

    @Test
    fun createAndEstablish_firstInvocationWithMac_returnsConnection() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        val mac = DEFAULT_MAC
        val name = ""
        val model = DEFAULT_TOOTHBRUSH_MODEL

        mockInstanceCreation(listOf(connection))

        mockAnyDoctorInstanceCreation()

        assertEquals(connection, poolManager.createAndEstablish(mac, name, model))
    }

    @Test
    fun createAndEstablish_secondInvocationWithMac_returnsConnectionAndInvokesEstablishConnection() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        val mac = DEFAULT_MAC
        val name = ""
        val model = DEFAULT_TOOTHBRUSH_MODEL

        mockInstanceCreation(listOf(connection))

        val doctor = mockDoctorInstanceCreation(connection)

        val returnedConnection = poolManager.createAndEstablish(mac, name, model)

        assertTrue(connection === returnedConnection)
        assertTrue(connection === poolManager.get(mac))

        verify(doctor, times(1)).init()

        poolManager.createAndEstablish(mac, name, model)

        assertTrue(connection === returnedConnection)
        assertTrue(connection === poolManager.get(mac))

        verify(doctor, times(2)).init()
    }

    /*
    CREATE
     */
    @Test(expected = IllegalArgumentException::class)
    fun create_validateMacThrowsException_throwsIllegalArgumentException() {
        doThrow(IllegalArgumentException()).whenever(poolManager).validateMac(any())

        poolManager.create("", "", DEFAULT_TOOTHBRUSH_MODEL)
    }

    @Test
    fun create_firstInvocationWithMac_addsKLTBConnectionToLocalList() {
        doNothing().whenever(poolManager).validateMac(any())

        assertTrue(poolManager.connectionList.isEmpty())

        mockInstanceCreation(listOf(KLTBConnectionBuilder.createAndroidLess().build()))

        poolManager.create(DEFAULT_MAC, "", DEFAULT_TOOTHBRUSH_MODEL)

        assertNotNull(poolManager.get(DEFAULT_MAC))
    }

    @Test
    fun create_firstInvocationWithMac_invokesRefreshKnownConnectionsStream() {
        doNothing().whenever(poolManager).validateMac(any())

        assertTrue(poolManager.connectionList.isEmpty())

        mockInstanceCreation(listOf(KLTBConnectionBuilder.createAndroidLess().build()))

        poolManager.create(DEFAULT_MAC, "", DEFAULT_TOOTHBRUSH_MODEL)

        verify(poolManager).refreshKnownConnectionsStream()
    }

    @Test
    fun create_connectionAlreadyPresent_returnsSameInstance() {
        doNothing().whenever(poolManager).validateMac(any())

        val expectedConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(expectedConnection)

        val returnedConnection =
            poolManager.create(
                DEFAULT_MAC,
                "",
                DEFAULT_TOOTHBRUSH_MODEL
            )

        assertTrue(expectedConnection === returnedConnection)
    }

    @Test
    fun create_connectionAlreadyPresent_neverInvokesRefreshKnownConnectionsStream() {
        doNothing().whenever(poolManager).validateMac(any())

        val expectedConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(expectedConnection)

        poolManager.create(
            DEFAULT_MAC,
            "",
            DEFAULT_TOOTHBRUSH_MODEL
        )

        verify(poolManager, never()).refreshKnownConnectionsStream()
    }

    /*
    FORGET MAC
     */
    @Test
    fun forgetMac_emptyList_emitsUnknownToothbrushException() {
        poolManager.forget("dada").test()
            .assertError(UnknownToothbrushException::class.java)
    }

    @Test
    fun forgetMac_macNotPresent_emitsUnknownToothbrushException() {
        val previousConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(previousConnection)

        poolManager.forget("dada").test()
            .assertError(UnknownToothbrushException::class.java)
    }

    @Test
    fun forgetMac_macPresent_invokesDoctorClose() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        val doctor = addConnectionToInternalList(connection)

        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(Completable.complete())

        poolManager.forget(DEFAULT_MAC).test()

        verify(doctor).close()
    }

    @Test
    fun forgetMac_macPresent_removesConnectionFromListAfterRemovingFromRepository() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(connection)

        assertNotNull(poolManager.get(DEFAULT_MAC))

        val removeSubject = CompletableSubject.create()
        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(removeSubject)

        poolManager.forget(DEFAULT_MAC).test()

        assertNotNull(poolManager.get(DEFAULT_MAC))

        removeSubject.onComplete()

        assertNull(poolManager.get(DEFAULT_MAC))
    }

    @Test
    fun forgetMac_macPresent_invokesRefreshKnownConnectionsStream() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(connection)

        assertNotNull(poolManager.get(DEFAULT_MAC))

        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(Completable.complete())

        doNothing().whenever(poolManager).refreshKnownConnectionsStream()

        poolManager.forget(DEFAULT_MAC).test()

        verify(poolManager).refreshKnownConnectionsStream()
    }

    @Test
    fun forgetMac_macPresent_removesConnectionFromListEvenIfRemovingFromRepositoryFails() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(connection)

        assertNotNull(poolManager.get(DEFAULT_MAC))

        val removeSubject = CompletableSubject.create()
        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(removeSubject)

        poolManager.forget(DEFAULT_MAC).test()

        assertNotNull(poolManager.get(DEFAULT_MAC))

        removeSubject.onError(TestForcedException())

        assertNull(poolManager.get(DEFAULT_MAC))
    }

    /*
    FORGET KLTBCONNECTION
     */
    @Test
    fun forgetKLTBConnection_emptyList_emitsUnknownToothbrushException() {
        poolManager.forget(KLTBConnectionBuilder.createAndroidLess().build()).test()
            .assertError(UnknownToothbrushException::class.java)
    }

    @Test
    fun forgetKLTBConnection_notPresent_emitsUnknownToothbrushException() {
        val previousConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        val other = KLTBConnectionBuilder.createAndroidLess()
            .withMac("random")
            .build()

        addConnectionToInternalList(previousConnection)
        assertEquals(1, poolManager.connectionList.size)

        poolManager.forget(other).test()
            .assertError(UnknownToothbrushException::class.java)
    }

    @Test
    fun forgetKLTBConnection_present_invokesDoctorClose() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        val doctor = addConnectionToInternalList(connection)

        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(Completable.complete())

        poolManager.forget(connection).test()

        verify(doctor).close()
    }

    @Test
    fun forgetKLTBConnection_present_removesConnectionFromListAfterRemovingFromRepository() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(connection)

        assertNotNull(poolManager.get(DEFAULT_MAC))

        val removeSubject = CompletableSubject.create()
        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(removeSubject)

        poolManager.forget(connection).test()

        assertNotNull(poolManager.get(DEFAULT_MAC))

        removeSubject.onComplete()

        assertNull(poolManager.get(DEFAULT_MAC))
    }

    @Test
    fun forgetKLTBConnection_present_invokesRefreshKnownConnectionsStream() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        addConnectionToInternalList(connection)

        assertNotNull(poolManager.get(DEFAULT_MAC))

        whenever(toothbrushRepository.remove(DEFAULT_MAC)).thenReturn(Completable.complete())

        doNothing().whenever(poolManager).refreshKnownConnectionsStream()

        poolManager.forget(connection).test()

        verify(poolManager).refreshKnownConnectionsStream()
    }

    /*
    CLOSE
     */
    @Test
    fun close_notInitialized_doesNotCrash() {
        assertFalse(poolManager.initialized.get())

        poolManager.close()
    }

    @Test
    fun close_flagsAsNotInitialized() {
        poolManager.initialized.set(true)

        poolManager.close()

        assertFalse(poolManager.initialized.get())
    }

    @Test
    fun close_with1Connection_invokesCloseOnDoctor() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        val doctor = addConnectionToInternalList(connection)

        poolManager.close()

        verify(doctor).close()
    }

    @Test
    fun close_with2Connections_invokesCloseOnDoctor() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac(DEFAULT_MAC)
            .build()

        val doctor1 = addConnectionToInternalList(connection1)
        val doctor2 = addConnectionToInternalList(connection2)

        poolManager.close()

        verify(doctor1).close()
        verify(doctor2).close()
    }

    /*
    getKnownConnectionsOnceAndStream
     */
    @Test
    fun `getKnownConnectionsOnceAndStream emits empty list when connectionList is empty`() {
        assertTrue(poolManager.connectionList.isEmpty())

        poolManager.getKnownConnectionsOnceAndStream().test()
            .assertValue { it == listOf<KLTBConnection>() }
    }

    @Test
    fun `getKnownConnectionsOnceAndStream emits stored connections`() {
        poolManager = createPoolManager()

        val expectedConnection = KLTBConnectionBuilder.createAndroidLess().build()
        storeConnection(expectedConnection)

        poolManager.getKnownConnectionsOnceAndStream().test()
            .assertValue { it == listOf<KLTBConnection>(expectedConnection) }
    }

    @Test
    fun `getKnownConnectionsOnceAndStream emits the latest stored connections`() {
        poolManager = createPoolManager()

        val observable = poolManager.getKnownConnectionsOnceAndStream()

        val expectedConnection = KLTBConnectionBuilder.createAndroidLess().build()
        storeConnection(expectedConnection)

        observable.test()
            .assertValue { it == listOf<KLTBConnection>(expectedConnection) }
    }

    /*
    UTILS
     */
    private fun mockInstanceCreation(instancesToBeReturned: List<InternalKLTBConnection> = listOf()): List<KLTBConnectionDoctor> {
        val doctorList = mutableListOf<KLTBConnectionDoctor>()

        when (instancesToBeReturned.size) {
            0 -> {
                val connection =
                    KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().build()
                doReturn(connection).whenever(poolManager)
                    .newKLTBConnectionInstance(any(), any(), any())

                doctorList.add(mockDoctorInstanceCreation(connection))
            }
            1 -> {
                val connection = instancesToBeReturned.first()
                doReturn(connection).whenever(poolManager)
                    .newKLTBConnectionInstance(any(), any(), any())

                doctorList.add(mockDoctorInstanceCreation(connection))
            }
            else -> {
                val firstConnection = instancesToBeReturned.first()
                val otherConnections =
                    instancesToBeReturned.subList(1, instancesToBeReturned.size).toTypedArray()
                doReturn(firstConnection, *otherConnections).whenever(poolManager)
                    .newKLTBConnectionInstance(any(), any(), any())

                doctorList.add(mockDoctorInstanceCreation(firstConnection))
                otherConnections.forEach { doctorList.add(mockDoctorInstanceCreation(it)) }
            }
        }

        doNothing().whenever(poolManager).refreshKnownConnectionsStream()

        return doctorList
    }

    private fun mockDoctorInstanceCreation(connection: InternalKLTBConnection): KLTBConnectionDoctor {
        val doctor: KLTBConnectionDoctor = mock()
        whenever(doctor.connection).thenReturn(connection)
        val mac = connection.toothbrush().mac
        whenever(doctor.mac()).thenReturn(mac)
        doReturn(doctor).whenever(poolManager).newConnectionDoctorInstance(connection)

        return doctor
    }

    private fun storeConnection(connection: InternalKLTBConnection) {
        val connectionDoctor = createDoctor(connection)
        poolManager.connectionList.add(connectionDoctor)
    }

    private fun addConnectionToInternalList(connection: InternalKLTBConnection): KLTBConnectionDoctor {
        val connectionDoctor = mockDoctorInstanceCreation(connection)
        poolManager.connectionList.add(connectionDoctor)

        return connectionDoctor
    }

    private fun mockAnyDoctorInstanceCreation() {
        val doctor: KLTBConnectionDoctor = mock()
        doAnswer {
            whenever(doctor.connection).thenReturn(it.getArgument(0))
            val mac = (it.getArgument(0) as KLTBConnection).toothbrush().mac
            whenever(doctor.mac()).thenReturn(mac)

            doctor
        }.whenever(poolManager).newConnectionDoctorInstance(any())
    }

    private fun prepareEmptyToothbrushes(
        listAllSingle: Single<List<AccountToothbrush>> = Single.just(listOf())
    ) {
        whenever(toothbrushRepository.listAll()).thenReturn(listAllSingle)
    }

    private fun invokeInternalInit(completable: Completable = Completable.complete()): Completable {
        return poolManager.internalInit(completable)
    }
}
