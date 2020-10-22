package com.kolibree.android.sdk.core.ota.kltb003

import android.content.Context
import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_DSP
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_REBOOTING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.MAIN
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.NOT_FOUND
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.kolibree.android.test.extensions.postDelayedImmediateRun
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.ReflectionUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ToothbrushDfuUpdaterTest : BaseUnitTest() {

    private val DEFAULT_MAC = "AA:BB:CC:DD:EE:FF"

    private val toothbrush: Toothbrush = mock()

    private val connection: InternalKLTBConnection = mock()

    private val bleDriver: BleDriver = mock()

    private val context: Context = mock()

    private val toothbrushRepository: ToothbrushRepository = mock()

    private val activeConnectionUseCase: ActiveConnectionUseCase = mock()
    private val toothbrushAdvertisingAppUseCase: ToothbrushAdvertisingAppUseCase = mock()

    private val timeoutScheduler = TestScheduler()

    private lateinit var updater: ToothbrushDfuUpdater

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(toothbrush.mac).thenReturn(DEFAULT_MAC)
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        val state = mock<ConnectionState>()
        whenever(state.current).thenReturn(OTA)
        whenever(connection.state()).thenReturn(state)

        updater = spy(
            ToothbrushDfuUpdater(
                connection = connection,
                toothbrushRepository = toothbrushRepository,
                bleDriver = bleDriver,
                activeConnectionUseCase = activeConnectionUseCase,
                toothbrushAdvertisingAppUseCase = toothbrushAdvertisingAppUseCase,
                timeoutScheduler = timeoutScheduler,
                context = context
            )
        )
    }

    @Test
    fun getRealMac_bootloaderReturnsDfuMac() {
        whenever(toothbrush.isRunningBootloader).thenReturn(true)
        assertEquals("AA:BB:CC:DD:EE:00", updater.realMac())
    }

    @Test
    fun getRealMac_mainAppReturnsRegularMac() {
        whenever(toothbrush.isRunningBootloader).thenReturn(false)
        assertEquals(DEFAULT_MAC, updater.realMac())
    }

    @Test
    fun valueOf_DFU_CHARACTERISTICS_NOT_FOUND() {
        assertEquals(4102, ToothbrushDfuUpdater.DFU_CHARACTERISTICS_NOT_FOUND)
    }

    @Test
    fun isRecoverableError_DFU_CHARACTERISTICS_NOT_FOUND_returnsTrue() {
        assertTrue(updater.isRecoverableError(ToothbrushDfuUpdater.DFU_CHARACTERISTICS_NOT_FOUND))
    }

    @Test
    fun isRecoverableError_other_returnsFalse() {
        assertFalse(updater.isRecoverableError(4201))
    }

    @Test
    fun valueOf_NAME_DFU() {
        assertEquals("Dfu", ToothbrushDfuUpdater.NAME_DFU)
    }

    @Test
    fun valueOf_PACKETS_RECEIPT_NOTIFICATIONS_VALUE() {
        assertEquals(6, ToothbrushDfuUpdater.PACKETS_RECEIPT_NOTIFICATIONS_VALUE)
    }

    /*
    renameIfNecessary
     */
    @Test
    fun renameIfNecessary_isRunningBootloaderTrue_doesNotInvokeRename() {
        whenever(bleDriver.isRunningBootloader).thenReturn(true)

        updater.renameIfNecessary().test().assertComplete()

        verify(updater, never()).renameToothbrushCompletable(any())
    }

    @Test
    fun renameIfNecessary_needsRenamingFalse_doesNotInvokeRename() {
        doReturn(false).whenever(updater).nameIsDfu()

        updater.renameIfNecessary().test().assertComplete()

        verify(updater, never()).renameToothbrushCompletable(any())
    }

    @Test
    fun renameIfNecessary_needsRenamingTrue_invokesRename() {
        doReturn(true).whenever(updater).nameIsDfu()
        doReturn(Completable.complete()).whenever(updater).renameToothbrushCompletable(any())

        val expectedName = "real name"
        doReturn(expectedName).whenever(bleDriver).queryRealName()

        updater.renameIfNecessary().test().assertComplete()

        verify(updater).renameToothbrushCompletable(expectedName)
    }

    /*
    nameIsDfu
     */
    @Test
    fun nameIsDfu_nameStartsWithDfu_true() {
        whenever(toothbrush.getName()).thenReturn("DfuBlabla")

        assertTrue(updater.nameIsDfu())
    }

    @Test
    fun nameIsDfu_nameStartsWithDFU_true() {
        whenever(toothbrush.getName()).thenReturn("DFUtrue")

        assertTrue(updater.nameIsDfu())
    }

    @Test
    fun nameIsDfu_nameContainsDfu_false() {
        whenever(toothbrush.getName()).thenReturn("Mr DFU")

        assertFalse(updater.nameIsDfu())
    }

    @Test
    fun nameIsDfu_nameNotContainsDfu_false() {
        whenever(toothbrush.getName()).thenReturn("Mr T")

        assertFalse(updater.nameIsDfu())
    }

    /*
    RENAME TOOTHBRUSH COMPLETABLE
     */
    @Test
    fun renameToothbrushCompletable_invokesToothbrushRepository_andThenConnectionToothbrush() {
        whenever(toothbrush.mac).thenReturn(DEFAULT_MAC)

        val expectedName = "rerere"
        val repositorySubject = CompletableSubject.create()
        whenever(toothbrushRepository.rename(DEFAULT_MAC, expectedName)).thenReturn(
            repositorySubject
        )

        val observer = updater.renameToothbrushCompletable(expectedName).test()

        observer.assertNotComplete()

        verify(toothbrushRepository).rename(DEFAULT_MAC, expectedName)

        observer.assertNotComplete()
        repositorySubject.onComplete()
        observer.assertComplete()

        verify(toothbrush).cacheName(expectedName)
    }

    /*
    waitUntilConnectionEstablished
     */
    @Test
    fun `when connection is ACTIVE, waitUntilConnectionEstablished completes immediately and does not change state`() {
        val innerConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(ACTIVE)
            .build()

        spyWithConnection(innerConnection)

        updater.waitUntilConnectionEstablished(AvailableUpdate.empty(TYPE_DSP)).test()
            .assertComplete()

        verify(innerConnection, never()).setState(any())
    }

    @Test
    fun `when connection is ESTABLISHING, waitUntilConnectionEstablished subscribes to connectionActiveCompletable`() {
        val innerConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(ESTABLISHING)
            .build()

        spyWithConnection(innerConnection)

        val connectSubject = CompletableSubject.create()
        doReturn(connectSubject).whenever(updater).connectionActiveCompletable()

        val observer =
            updater.waitUntilConnectionEstablished(AvailableUpdate.empty(TYPE_DSP)).test()
                .assertNotComplete()

        connectSubject.assertHasObserversAndComplete()

        observer.assertComplete()

        verify(innerConnection, never()).setState(any())
    }

    @Test
    fun `when connection is not ACTIVE or ESTABLISHING, waitUntilConnectionEstablished subscribes to forceReconnectCompletable`() {
        val availableUpdate = AvailableUpdate.empty(TYPE_DSP)

        KLTBConnectionState.values()
            .filterNot { it == ACTIVE || it == ESTABLISHING }
            .forEach { connectionState ->
                val innerConnection = KLTBConnectionBuilder.createAndroidLess()
                    .withState(connectionState)
                    .build()

                spyWithConnection(innerConnection)

                val forceReconnectSubject = CompletableSubject.create()
                doReturn(forceReconnectSubject).whenever(updater)
                    .forceReconnectCompletable(availableUpdate)

                val observer = updater.waitUntilConnectionEstablished(availableUpdate).test()
                    .assertNotComplete()

                forceReconnectSubject.assertHasObserversAndComplete()

                observer.assertComplete()

                verify(innerConnection, never()).setState(any())
            }
    }

    @Test
    fun `when connection is not ACTIVE or ESTABLISHING and forceReconnectCompletable emits an error, waitUntilConnectionEstablished sets connection state to TERMINATED`() {
        val availableUpdate = AvailableUpdate.empty(TYPE_DSP)

        KLTBConnectionState.values()
            .filterNot { it == ACTIVE || it == ESTABLISHING }
            .forEach { connectionState ->
                val innerConnection = KLTBConnectionBuilder.createAndroidLess()
                    .withState(connectionState)
                    .build()

                spyWithConnection(innerConnection)

                val forceReconnectSubject = CompletableSubject.create()
                doReturn(forceReconnectSubject).whenever(updater)
                    .forceReconnectCompletable(availableUpdate)

                val observer = updater.waitUntilConnectionEstablished(availableUpdate).test()
                    .assertNotComplete()

                assertTrue(forceReconnectSubject.hasObservers())
                forceReconnectSubject.onError(TestForcedException())

                observer.assertError(TestForcedException::class.java)

                verify(innerConnection).setState(TERMINATED)
            }
    }

    @Test
    fun `when connection is ESTABLISHING and connectionActiveCompletable emits error, waitUntilConnectionEstablished sets connection state to TERMINATED`() {
        val innerConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(ESTABLISHING)
            .build()

        spyWithConnection(innerConnection)

        val connectSubject = CompletableSubject.create()
        doReturn(connectSubject).whenever(updater).connectionActiveCompletable()

        val observer =
            updater.waitUntilConnectionEstablished(AvailableUpdate.empty(TYPE_DSP)).test()
                .assertNotComplete()

        assertTrue(connectSubject.hasObservers())
        connectSubject.onError(TestForcedException())

        observer.assertError(TestForcedException::class.java)

        verify(innerConnection).setState(TERMINATED)
    }

    @Test
    fun `when waitUntilConnectionEstablished doesn't complete after 65 seconds, it times out`() {
        val innerConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(TERMINATED)
            .build()

        spyWithConnection(innerConnection)

        val availableUpdate = AvailableUpdate.empty(TYPE_DSP)

        doReturn(Completable.never()).whenever(updater)
            .forceReconnectCompletable(availableUpdate)

        val observer = updater.waitUntilConnectionEstablished(availableUpdate).test()
            .assertNotComplete()

        timeoutScheduler.advanceTimeBy(30L, TimeUnit.SECONDS)

        observer.assertNotComplete()

        timeoutScheduler.advanceTimeBy(30L, TimeUnit.SECONDS)

        observer.assertNotComplete()

        timeoutScheduler.advanceTimeBy(5L, TimeUnit.SECONDS)

        observer.assertError(TimeoutException::class.java)
    }

    /*
    forceReconnectCompletable
     */

    @Test
    fun `forceReconnectCompletable invokes setIsRunningBootloader = true if toothbrushAdvertisingAppUseCase returns DFU_BOOTLOADER, independently of if firmware completed without errors`() {
        doReturn(Completable.complete()).whenever(updater).connectionActiveCompletable()

        val update = AvailableUpdate.empty(TYPE_BOOTLOADER)

        mockAdvertisingState(DFU_BOOTLOADER)

        listOf(true, false).forEachIndexed { index, hasErrors ->
            updater.errorUpdatingFirmware.set(hasErrors)

            updater.forceReconnectCompletable(update).test()

            verify(
                bleDriver,
                times(index + 1).description("hasErrors = $hasErrors")
            ).isRunningBootloader = true
        }
    }

    @Test
    fun `forceReconnectCompletable invokes setIsRunningBootloader = false if toothbrushAdvertisingAppUseCase returns MAIN, independently of if OTA completed without errors`() {
        doReturn(Completable.complete()).whenever(updater).connectionActiveCompletable()

        val update = AvailableUpdate.empty(TYPE_BOOTLOADER)

        mockAdvertisingState(MAIN)

        listOf(true, false).forEachIndexed { index, hasErrors ->
            updater.errorUpdatingFirmware.set(hasErrors)

            updater.forceReconnectCompletable(update).test()

            verify(
                bleDriver,
                times(index + 1).description("hasErrors = $hasErrors")
            ).isRunningBootloader = false
        }
    }

    @Test
    fun `forceReconnectCompletable invokes setIsRunningBootloader = false if toothbrushAdvertisingAppUseCase returns NOT_FOUND and OTA completed without errors`() {
        doReturn(Completable.complete()).whenever(updater).connectionActiveCompletable()

        val update = AvailableUpdate.empty(TYPE_BOOTLOADER)

        mockAdvertisingState(NOT_FOUND)

        updater.errorUpdatingFirmware.set(false)

        updater.forceReconnectCompletable(update).test()

        verify(bleDriver).isRunningBootloader = false
    }

    @Test
    fun `forceReconnectCompletable invokes setIsRunningBootloader = true if toothbrushAdvertisingAppUseCase returns NOT_FOUND and firmware completed with errors`() {
        doReturn(Completable.complete()).whenever(updater).connectionActiveCompletable()

        val update = AvailableUpdate.empty(TYPE_BOOTLOADER)

        mockAdvertisingState(NOT_FOUND)

        updater.errorUpdatingFirmware.set(true)

        updater.forceReconnectCompletable(update).test()

        verify(bleDriver).isRunningBootloader = true
    }

    @Test
    fun `forceReconnectCompletable sets state to NEW on subscription`() {
        val connectSubject = CompletableSubject.create()
        doReturn(connectSubject).whenever(updater).connectionActiveCompletable()

        mockRunAfterDisconnect()
        mockAdvertisingState()

        verify(connection, never()).setState(NEW)

        updater.forceReconnectCompletable(AvailableUpdate.empty(TYPE_BOOTLOADER)).test()

        verify(connection).setState(NEW)
    }

    @Test
    fun `forceReconnectCompletable waits For Disconnection and subscribes to connectionActiveCompletable`() {
        val connectSubject = CompletableSubject.create()
        doReturn(connectSubject).whenever(updater).connectionActiveCompletable()

        mockAdvertisingState()

        var verificationsExecuted = false
        mockRunAfterDisconnect(
            preDisconnectVerifications = { assertFalse(connectSubject.hasObservers()) },
            postDisconnectVerifications = {
                verificationsExecuted = true

                assertTrue(connectSubject.hasObservers())
            }
        )

        val observer =
            updater.forceReconnectCompletable(AvailableUpdate.empty(TYPE_BOOTLOADER)).test()
                .assertNotComplete()

        assertTrue(verificationsExecuted)

        connectSubject.onComplete()

        observer.assertComplete()
    }

    /*
    runAfterDisconnect
     */
    @Test
    fun `runAfterDisconnect sets isUpdateInProgress=false before invoking disconnect`() {
        val connectSubject = CompletableSubject.create()
        doReturn(connectSubject).whenever(updater).connectionActiveCompletable()

        var verificationsExecuted = false
        whenever(connection.disconnect())
            .thenAnswer {
                assertFalse(updater.isUpdateInProgress())

                verificationsExecuted = true

                Unit
            }

        updater.runAfterDisconnect {}

        assertTrue(verificationsExecuted)
    }

    /*
    connectionActiveCompletable
     */

    @Test
    fun `connectionActiveCompletable completes when activeConnectionUseCase emits connection with expected connection`() {
        val connectionSubject = PublishProcessor.create<KLTBConnection>()
        whenever(activeConnectionUseCase.onConnectionsUpdatedStream())
            .thenReturn(connectionSubject)

        val observer = updater.connectionActiveCompletable().test().assertNotComplete()

        connectionSubject.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("aa").build())

        observer.assertNotComplete()

        connectionSubject.onNext(connection)

        observer.assertComplete()
    }

    @Test
    fun `connectionActiveCompletable never invokes establish`() {
        val connectionIsActiveSubject = PublishProcessor.create<KLTBConnection>()
        whenever(activeConnectionUseCase.onConnectionsUpdatedStream())
            .thenReturn(connectionIsActiveSubject)

        updater.connectionActiveCompletable().test()

        verify(connection, never()).establishCompletable()
        verify(connection, never()).establishDfuBootloaderCompletable()
    }

    /*
    runAfterDisconnect
     */
    @Test
    fun `runAfterDisconnect registers listener before invoking disconnect`() {
        val innerConnection = spyWithConnection()

        updater.runAfterDisconnect { }

        inOrder(innerConnection.state(), innerConnection) {
            verify(innerConnection.state()).register(any())

            verify(innerConnection).disconnect()
        }
    }

    @Test
    fun `runAfterDisconnect never invokes runnable if state is not TERMINATED`() {
        val stateSubject = PublishSubject.create<KLTBConnectionState>()
        val innerConnection = KLTBConnectionBuilder.createAndroidLess()
            .withStateListener(stateSubject)
            .build()

        spyWithConnection(innerConnection)

        val runnable = mock<Runnable>()
        updater.runAfterDisconnect(runnable)

        KLTBConnectionState.values()
            .filterNot { it == TERMINATED }
            .forEach {
                stateSubject.onNext(it)
            }

        verify(updater, never()).mainThreadHandler()
        verify(runnable, never()).run()
        verify(connection.state(), never()).unregister(any())
    }

    @Test
    fun `runAfterDisconnect invokes runnable on main thread handler if state is TERMINATED`() {
        val stateSubject = PublishSubject.create<KLTBConnectionState>()
        val innerConnection = KLTBConnectionBuilder.createAndroidLess()
            .withStateListener(stateSubject)
            .build()

        spyWithConnection(innerConnection)

        val runnable = mock<Runnable>()
        updater.runAfterDisconnect(runnable)

        verify(runnable, never()).run()

        val handler = mock<Handler>()
        doReturn(handler).whenever(updater).mainThreadHandler()

        stateSubject.onNext(TERMINATED)

        verify(handler).post(runnable)
    }

    /*
    UPDATE
     */

    @Test
    fun update_updateObservableEmitsError_waitUntilConnectionIsExecuted_renameIsNot() {
        val update = AvailableUpdate.empty(TYPE_FIRMWARE)

        val (updateSubject, waitUntilConnectionSubject, renameSubject) = setupUpdate(update)

        val observer = updater.update(update).test()

        observer.assertNotComplete()

        assertFalse(waitUntilConnectionSubject.hasObservers())

        val expectedException = TestForcedException()
        updateSubject.onError(expectedException)
        observer.assertNotComplete()

        waitUntilConnectionSubject.assertHasObserversAndComplete()

        assertFalse(renameSubject.hasObservers())

        observer.assertError(expectedException)
    }

    /*
    updateObservable
     */
    @Test
    fun `dfuProgressListener invokes bleDriver setIsRunningBootloader = true when onEnablingDfuMode is invoked`() {
        verify(bleDriver, never()).isRunningBootloader = any()

        updater.dfuProgressListener(mock()).onEnablingDfuMode("")

        verify(bleDriver).isRunningBootloader = true
    }

    @Test
    fun `dfuProgressListener onProgressChanged emits OtaUpdateEvent with expected progress`() {
        val expectedProgress = 654

        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        updater.dfuProgressListener(emitter).onProgressChanged("", expectedProgress, 0f, 0f, 0, 0)

        verify(emitter)
            .onNext(OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress))
    }

    @Test
    fun `dfuProgressListener onDeviceRebooting emits OtaUpdateEvent with OTA_UPDATE_REBOOTING`() {
        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        val listener = updater.dfuProgressListener(emitter)

        ReflectionUtils.invokeProtectedVoidMethod(listener, "onDeviceRebooting")

        verify(emitter).onNext(OtaUpdateEvent.fromAction(OTA_UPDATE_REBOOTING))
    }

    @Test
    fun `dfuProgressListener finishes when error`() {
        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        val listener = updater.dfuProgressListener(emitter)

        listener.onError("mock address", 1, 1, "mock message")

        verify(emitter).tryOnError(any())
    }

    @Test
    fun `dfuProgressListener finishes when complete`() {
        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        val listener = updater.dfuProgressListener(emitter)

        // Dirty solution but works
        // better tests would require some refactoring of updater
        val handler = mock<Handler>()
        handler.postDelayedImmediateRun()

        doReturn(handler).whenever(updater).mainThreadHandler()

        listener.onDfuCompleted("mock address")

        verify(emitter).onComplete()
    }

    @Test
    fun `dfuProgressListener finishes when aborted`() {
        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        val listener = updater.dfuProgressListener(emitter)

        listener.onDfuAborted("mock address")

        verify(emitter).tryOnError(any())
    }

    @Test
    fun `errorUpdatingFirmware is set after error`() {
        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        val listener = updater.dfuProgressListener(emitter)

        assertFalse(updater.errorUpdatingFirmware.get())

        listener.onError("mock address", 1, 1, "mock message")

        assertTrue(updater.errorUpdatingFirmware.get())
    }

    @Test
    fun `errorUpdatingFirmware is set after abort`() {
        val emitter: ObservableEmitter<OtaUpdateEvent> = mock()
        val listener = updater.dfuProgressListener(emitter)

        assertFalse(updater.errorUpdatingFirmware.get())

        listener.onDfuAborted("mock address")

        assertTrue(updater.errorUpdatingFirmware.get())
    }

    /*
    Utils
     */

    private fun spyWithConnection(
        innerConnection: InternalKLTBConnection = KLTBConnectionBuilder.createAndroidLess().build()
    ): InternalKLTBConnection {
        updater = spy(
            ToothbrushDfuUpdater(
                connection = innerConnection,
                toothbrushRepository = toothbrushRepository,
                bleDriver = bleDriver,
                activeConnectionUseCase = activeConnectionUseCase,
                toothbrushAdvertisingAppUseCase = toothbrushAdvertisingAppUseCase,
                timeoutScheduler = timeoutScheduler,
                context = context
            )
        )

        return innerConnection
    }

    private fun setupUpdate(
        availableUpdate: AvailableUpdate,
        updateSubject: PublishSubject<OtaUpdateEvent> = PublishSubject.create(),
        waitUntilConnectionSubject: PublishSubject<OtaUpdateEvent> = PublishSubject.create(),
        renameSubject: PublishSubject<OtaUpdateEvent> = PublishSubject.create()
    ): Triple<PublishSubject<OtaUpdateEvent>, PublishSubject<OtaUpdateEvent>, PublishSubject<OtaUpdateEvent>> {
        doReturn(updateSubject).whenever(updater).updateObservable(any())
        doReturn(waitUntilConnectionSubject)
            .whenever(updater).waitUntilConnectionEstablished(availableUpdate)
        doReturn(updateSubject).whenever(updater).renameIfNecessary()

        return Triple(updateSubject, waitUntilConnectionSubject, renameSubject)
    }

    private fun mockRunAfterDisconnect(
        preDisconnectVerifications: () -> Unit = {},
        postDisconnectVerifications: () -> Unit = {}
    ) {
        doAnswer {
            verify(connection).setState(NEW)

            preDisconnectVerifications.invoke()

            (it.getArgument(0) as Runnable).run()

            postDisconnectVerifications.invoke()
        }
            .whenever(updater).runAfterDisconnect(any())
    }

    private fun mockAdvertisingState(advertisingApp: ToothbrushAdvertisingApp = MAIN) {
        whenever(toothbrushAdvertisingAppUseCase.advertisingStateSingle(any()))
            .thenReturn(Single.just(advertisingApp))
    }
}
