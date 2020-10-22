/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATING
import com.kolibree.android.sdk.scan.ScanBeforeConnectFilter
import com.kolibree.android.sdk.scan.SpecificToothbrushScanCallback
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.android.sdk.scan.isDfu
import com.kolibree.android.sdk.usecases.OnConnectionActiveUseCase
import com.kolibree.android.sdk.util.IBluetoothUtils
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import timber.log.Timber

/**
 * Doctor responsible to keep an InternalKLTBConnection in the best possible connection shape.
 *
 * - Before attempting to establish a connection, scan for the connection. If it's unreachable, we'll
 * never succeed in connecting
 * - If there's an exception while attempting to establish the connection, attempt to scan + reconnect
 * - If BT is turned off, disconnect
 * - If BT is turned on, attempt to reconnect
 *
 * This behavior is not applied to V1 toothbrushes since they don't broadcast beacons if not in
 * pairing mode
 */
internal class KLTBConnectionDoctor(
    val connection: InternalKLTBConnection,
    private val toothbrushScanner: ToothbrushScanner,
    private val connectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase,
    private val bluetoothUtils: IBluetoothUtils,
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter(),
    private val scanBeforeConnectFilter: ScanBeforeConnectFilter,
    private val onConnectionActiveUseCase: OnConnectionActiveUseCase
) : AutoCloseable, SpecificToothbrushScanCallback, ConnectionStateListener {

    companion object {
        private val TAG =
            bluetoothTagFor(KLTBConnectionDoctor::class)
    }

    @VisibleForTesting
    var connectionAttemptDisposable: Disposable? = null

    @VisibleForTesting
    var listenToBluetoothStateDisposable: Disposable? = null

    @VisibleForTesting
    var listenToLocationStatusDisposable: Disposable? = null

    @VisibleForTesting
    val initialized = AtomicBoolean()

    @VisibleForTesting
    val disposables = CompositeDisposable()

    /*
    Invoked on main thread, so no need for synchronization
     */
    override fun onToothbrushFound(result: ToothbrushScanResult) {
        Timber.tag(TAG).v("onToothbrushFound %s, %s", result.name, result.mac)
        stopScan()

        if (!shouldAttemptConnection()) {
            reportConnectionNotAttemptedState("shouldAttemptConnection returned false in onToothbrushFound")
            return
        }

        Timber.tag(TAG).v(
            "onToothbrushFound pre establish is dfu %s\nState is %s for %s on %s",
            result.isDfu(),
            connection.state().current,
            connection,
            this
        )

        listenToStateAndAttemptConnection(establishCompletable(result))
    }

    @VisibleForTesting
    fun listenToStateAndAttemptConnection(establishCompletable: Completable) {
        Timber.tag(TAG).v("Doctor attempting connection $connectionAttemptDisposable")
        connectionAttemptDisposable = establishCompletable
            .subscribeOn(Schedulers.io())
            .doOnTerminate { connectionAttemptDisposable = null }
            .subscribe(
                {
                    Timber.tag(TAG).v(
                        "Doctor connected to toothbrush with MAC address %s, %s. State is %s",
                        connection.toothbrush().mac, connection, connection.state().current
                    )
                },
                this::onErrorEstablishingConnection
            )

        disposables.addSafely(connectionAttemptDisposable!!)

        listenToConnectionState()
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun reportConnectionNotAttemptedState(reason: String) {
        Timber.tag(TAG)
            .w(
                "Doctor not attempting to connect after %s. \nDisposable is %s and isDisposed? %s. " +
                    "\nState is %s for %s\nIs closed %s\nIs connection allowed %s on %s",
                reason,
                connectionAttemptDisposable,
                connectionAttemptDisposable?.isDisposed ?: "null",
                connection.state().current,
                connection,
                isClosed(),
                connection.toothbrush().isConnectionAllowed(),
                this
            )
    }

    @VisibleForTesting
    fun establishCompletable(result: ToothbrushScanResult): Completable {
        return establishCompletable(isBootloader = result.isDfu())
    }

    private fun establishCompletable(isBootloader: Boolean): Completable {
        return if (isBootloader) {
            connection.establishDfuBootloaderCompletable()
        } else {
            connection.establishCompletable()
        }
    }

    @VisibleForTesting
    fun listenToConnectionState() {
        Timber.tag(TAG).i("Doctor register to %s", connection)
        connection.state().register(this)
    }

    /**
     * Listens to state changes events and reacts accordingly
     */
    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        Timber.tag(TAG).v("Doctor onConnectionStateChanged to $newState")
        when (newState) {
            ACTIVE -> runOnConnectionActiveHooks(connection)
            TERMINATED -> onStateTerminated()
            NEW, ESTABLISHING, TERMINATING, OTA -> {
                // no-op
            }
        }
    }

    private fun onStateTerminated() {
        if (!isClosed()) {
            establishConnection()
        } else {
            reportConnectionNotAttemptedState("Doctor is closed after state TERMINATED")
        }
    }

    @VisibleForTesting
    fun runOnConnectionActiveHooks(connection: KLTBConnection) =
        disposables.addSafely(
            onConnectionActiveUseCase.apply(connection)
                .subscribe(
                    {
                        Timber.d("Successfully setup toothbrush with mac %s", connection.mac())
                    },
                    Timber::e
                )
        )

    @VisibleForTesting
    fun shouldAttemptConnection(): Boolean {
        Timber.tag("isConnectionAllowed")
            .v("shouldAttemptConnection isConnectionAllowed: ${connection.isConnectionAllowed()} on ${connection.mac()}")
        if (isClosed() || !connection.isConnectionAllowed()) return false

        if (connectionAttemptDisposable != null && !connectionAttemptDisposable!!.isDisposed) return false

        val state = connection.state().current

        return state == NEW || state == TERMINATED
    }

    override fun onError(errorCode: Int) {
        // no-op
    }

    @Synchronized
    override fun close() {
        if (!isClosed()) {
            Timber.tag(TAG).v("Doctor closing %s", this)
            stopScan()

            unregisterAsConnectionStateListener()

            disposables.clear()

            /*
            We shouldn't invoke alien methods inside a synchronized block, but we really need to
            lock here :-/
             */
            connection.disconnect()

            initialized.set(false)
        }
    }

    fun init() {
        var shouldEstablishConnection: Boolean
        synchronized(this) {
            shouldEstablishConnection = initialized.compareAndSet(false, true)
        }

        if (shouldEstablishConnection) {
            establishConnection()
        }
    }

    @VisibleForTesting
    fun establishConnection() {
        if (shouldAttemptConnection()) {
            Timber.tag(TAG).v(
                "Doctor establishconnection to %s. Is bootloader? %s",
                connection.toothbrush().mac,
                connection.toothbrush().isRunningBootloader
            )
            listenToBluetoothState()

            if (bluetoothUtils.isBluetoothEnabled) {
                connection.setState(NEW)
                Timber.tag(TAG)
                    .v("Doctor establishconnection shouldScanForToothbrush ${shouldScanForToothbrush()}")
                if (shouldScanForToothbrush()) {
                    scanForToothbrush()
                } else { // No need to scan
                    listenToStateAndAttemptConnection(establishCompletable(connection.toothbrush().isRunningBootloader))
                }
            } else {
                Timber.tag(TAG).w("Bluetooth is off, Not attempting connection")
            }
        } else {
            reportConnectionNotAttemptedState("shouldAttemptConnection returned false in establishConnection")
        }
    }

    @VisibleForTesting
    fun isDeviceLocationReady() =
        connectionPrerequisitesUseCase.checkConnectionPrerequisites() == ConnectionPrerequisitesState.ConnectionAllowed

    @VisibleForTesting
    fun listenToLocationStatus() {
        with(listenToLocationStatusDisposable) {
            if (this == null || isDisposed) {
                listenToLocationStatusDisposable =
                    connectionPrerequisitesUseCase.checkOnceAndStream()
                        .doOnSubscribe {
                            Timber.tag(TAG)
                                .d("Checking prerequisites for scan")
                        }
                        .doOnNext {
                            Timber.tag(TAG)
                                .d("Prerequisite state $it")
                        }
                        .filter { it == ConnectionPrerequisitesState.ConnectionAllowed }
                        .take(1)
                        .onTerminateDetach()
                        .subscribe(
                            { onDeviceLocationReady() },
                            Timber::e
                        )

                disposables.addSafely(listenToLocationStatusDisposable)
            }
        }
    }

    @VisibleForTesting
    fun onDeviceLocationReady() {
        Timber.tag(TAG).i("Device location ready, invoking establishConnection")
        establishConnection()
    }

    /**
     * Always returns true when it's the first time we are connecting to a toothbrush after BT was available
     *
     * On subsequent reconnections, it delegates the decision to scanBeforeReconnectStrategy
     */
    @VisibleForTesting
    fun shouldScanForToothbrush() = scanBeforeConnectFilter.scanBeforeConnect(connection)

    @VisibleForTesting
    fun listenToBluetoothState() {
        Timber.tag(TAG).v(
            "Doctor %s listenToBluetoothState disposable is disposed %s",
            this,
            listenToBluetoothStateDisposable?.isDisposed
        )
        if (listenToBluetoothStateDisposable != null && !listenToBluetoothStateDisposable!!.isDisposed) return

        listenToBluetoothStateDisposable = bluetoothUtils.bluetoothStateObservable()
            .distinctUntilChanged()
            .doOnSubscribe { Timber.tag(TAG).v("Doctor subscribed to bluetooth") }
            .doFinally {
                Timber.tag(TAG)
                    .v(
                        "Doctor bluetooth doFinally, disposables disposed %s",
                        disposables.isDisposed
                    )
            }
            .subscribe(
                { onNewBluetoothState(it) },
                Timber::e
            )

        disposables.addSafely(listenToBluetoothStateDisposable!!)
    }

    @VisibleForTesting
    fun onNewBluetoothState(newBluetoothState: Boolean) {
        Timber.tag(TAG).v("Doctor onNewBluetoothState %s", newBluetoothState)
        when (newBluetoothState) {
            false -> {
                stopScan()

                abortConnectionAttempt()
            }
            true -> {
                establishConnection()
            }
        }
    }

    private fun abortConnectionAttempt() {
        connectionAttemptDisposable?.dispose()
    }

    @VisibleForTesting
    fun scanForToothbrush() {
        if (isDeviceLocationReady()) {
            Timber.tag(TAG).v("Doctor toothbrushScanner scan for %s", connection.toothbrush().mac)
            toothbrushScanner.scanFor(this)
        } else {
            listenToLocationStatus()
        }
    }

    override fun bluetoothDevice(): BluetoothDevice =
        bluetoothAdapter.getRemoteDevice(connection.toothbrush().mac)

    @VisibleForTesting
    fun onErrorEstablishingConnection(throwable: Throwable) {
        Timber.tag(TAG).e(throwable, "Error connecting to %s", mac())
        unregisterAsConnectionStateListener()

        if (!isClosed()) {
            establishConnection()
        } else {
            Timber.tag(TAG).i("Doctor not attempting reconnection, it's closed")
        }
    }

    private fun stopScan() {
        toothbrushScanner.stopScan(this)
    }

    @VisibleForTesting
    fun unregisterAsConnectionStateListener() {
        connection.state().unregister(this)
    }

    @VisibleForTesting
    @Synchronized
    fun isClosed() = !initialized.get()

    fun mac() = connection.toothbrush().mac
}

internal class KLTBConnectionDoctorFactory
@Inject constructor(
    context: Context,
    private val toothbrushScannerFactory: ToothbrushScannerFactory,
    private val connectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase,
    private val bluetoothUtils: IBluetoothUtils,
    private val scanBeforeConnectFilter: ScanBeforeConnectFilter,
    private val onConnectionActiveUseCase: OnConnectionActiveUseCase
) {

    private val appContext = context.applicationContext

    fun createDoctor(connection: InternalKLTBConnection): KLTBConnectionDoctor {
        return KLTBConnectionDoctor(
            connection = connection,
            toothbrushScanner = toothbrushScanner(connection),
            bluetoothUtils = bluetoothUtils,
            connectionPrerequisitesUseCase = connectionPrerequisitesUseCase,
            scanBeforeConnectFilter = scanBeforeConnectFilter,
            onConnectionActiveUseCase = onConnectionActiveUseCase
        )
    }

    private fun toothbrushScanner(connection: InternalKLTBConnection): ToothbrushScanner {
        return checkNotNull(
            toothbrushScannerFactory.getScanner(appContext, connection.toothbrush().model)
        )
    }
}
