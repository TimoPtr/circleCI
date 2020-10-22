package com.kolibree.android.sdk.core.ota.kltb003

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_REBOOTING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionDoctor
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.nordic.DfuUtils
import com.kolibree.android.sdk.core.driver.ble.nordic.KLManagerCallbacks
import com.kolibree.android.sdk.core.ota.ToothbrushUpdater
import com.kolibree.android.sdk.core.ota.kltb002.updater.OtaUpdater
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.MAIN
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.NOT_FOUND
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import no.nordicsemi.android.dfu.DfuLogListener
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import org.threeten.bp.Duration
import timber.log.Timber

internal interface KLTB003ToothbrushUpdater : ToothbrushUpdater

/**
 * [OtaUpdater] implementation for 3rd generation toothbrushes (M1, CE2, B1, Glimmer, Plaqless)
 *
 * An update consists of 3 steps, each of them wrapped in an Observable
 * 1. Send the file to the toothbrush. This step is delegated to DFU library
 * 2. Reconnect to the toothbrush. This step is delegated to [KLTBConnectionDoctor]
 * 3. Rename toothbrush if necessary. We don't want to leave the toothbrush with name "DfuTarg"
 *
 * How this works as of July 9th 2020
 *
 * - Send file -
 * 1. External class calls update
 * 2. Flag isUpdateInProgress = true. This'll prevent [KLTBConnectionDoctor] from trying to
 * reconnect in the middle of an update
 * 3. Invoke disconnect. This'll prevent [KolibreeBleDriver] from receiving [KLManagerCallbacks],
 * which might mess with reconnection step
 * 4. Once an update finishes (onDfuCompleted), complete update observable
 * (If update fails, set isUpdateInProgress = false)
 *
 * - Reconnect -
 * Then it's turn for reconnection. We delegate this to [KLTBConnectionDoctor], but the [KLTBConnection]
 * needs some massaging
 * 1. Set connection state to New
 * 2. [KLTBConnection] might have an outdated value. Set isRunningBootloader value according to
 * value emitted by [ToothbrushAdvertisingAppUseCase]
 * 3. Flag isUpdateInProgress = false. Otherwise [KLTBConnectionDoctor] will never attempt to
 * reconnect
 * 4. Disconnect
 * 5. [KLTBConnectionDoctor] eventually notices connection is [TERMINATED] and attempts to reconnect
 * 6. Wait for connection to become [ACTIVE]
 * 7. Complete once connection is [ACTIVE]. [KLTBConnectionDoctor] retries if connection fails
 *
 * ### Known glitches
 * I experienced twice BT stack going nuts and reporting 0 gatt services or persistent 133 errors.
 * Progress was stuck at 50%.
 *
 * Resetting bluetooth manually fixed the issue. Doctor was able to reconnect and progress completed.
 * We didn't find a proper way to automatically reset toothbrush
 */
@SuppressWarnings("TooManyFunctions")
internal class ToothbrushDfuUpdater @Inject constructor(
    private val connection: InternalKLTBConnection,
    private val toothbrushRepository: ToothbrushRepository,
    private val bleDriver: BleDriver,
    private val activeConnectionUseCase: ActiveConnectionUseCase,
    private val toothbrushAdvertisingAppUseCase: ToothbrushAdvertisingAppUseCase,
    @SingleThreadScheduler val timeoutScheduler: Scheduler,
    context: Context
) : KLTB003ToothbrushUpdater, DfuLogListener {

    @VisibleForTesting
    val errorUpdatingFirmware = AtomicBoolean(false)
    private val isUpdateInProgress = AtomicBoolean(false)

    private val appContext = context.applicationContext

    override fun isUpdateInProgress(): Boolean {
        return isUpdateInProgress.get()
    }

    /**
     * When the update gets disposed, many bad things can happen.
     * Depending on when it gets disposed, we may expect different race conditions
     * which can lead to serious problems with connecting the toothbrush.
     *
     * This all is caused by [KLTB003ToothbrushUpdaterService] which doesn't
     * get along well with the update stream. It lives its own life
     * and doesn't really care what's happening in RX. Because of that we
     * could observe memory leaks of [SimpleDfuProgressHelper].
     *
     * If we make sure that the update won't be disposed until the end,
     * then everything works great. We can achieve that by transforming
     * our stream into [ConnectableObservable]. With autoConnect() operator
     * it will never get disposed. It will either finish with success or error.
     *
     * There are few FailEarly added to ensure that stream never gets disposed.
     * @see [dfuProgressListener]
     */
    override fun update(availableUpdate: AvailableUpdate): Observable<OtaUpdateEvent> {
        val update = updateObservable(availableUpdate)
        val reconnect = waitUntilConnectionEstablished(availableUpdate)
        val renameIfNecessary = renameIfNecessary()

        return Observable
            .concatDelayError(listOf(update, reconnect))
            .concatWith(renameIfNecessary)
            .doOnSubscribe {
                Timber.tag(TAG).d("isUpdateInProgress true")

                isUpdateInProgress.set(true)
            }
            .doFinally {
                Timber.tag(TAG).d("isUpdateInProgress false")

                isUpdateInProgress.set(false)
            }
            .doOnDispose { FailEarly.fail("This stream shouldn't be disposed") }
            .publish()
            .autoConnect()
    }

    /**
     * In DFU mode the M1 has +1 on the last MAC digit
     *
     * @return the MAC address to be used to connect to the brush
     *
     *
     */
    @VisibleForTesting
    fun realMac(): String {
        val mac = connection.toothbrush().mac

        return if (connection.toothbrush().isRunningBootloader) DfuUtils.getDFUMac(mac) else mac
    }

    /**
     * If we pair to a toothbrush in bootloader, we store something similar to "DfuT" as toothbrush
     * name.
     *
     *
     * Once we've updated the FW, we can query M1 for its real name, but we also need to update the
     * database
     *
     * @return an Observable that will complete once it has checked if the M1 needs renaming. It
     * doesn't emit any event.
     */
    @VisibleForTesting
    fun renameIfNecessary(): Observable<OtaUpdateEvent> {
        return Observable.defer {
            Observable.create<OtaUpdateEvent> { emitter ->
                try {
                    if (!bleDriver.isRunningBootloader && nameIsDfu()) {
                        val realName = bleDriver.queryRealName()

                        renameToothbrushCompletable(realName).blockingAwait()
                    }
                } catch (e: RuntimeException) {
                    emitter.tryOnError(e)
                }

                if (!emitter.isDisposed) emitter.onComplete()
            }
                .subscribeOn(Schedulers.io())
        }
    }

    /**
     * Updates toothbrush's name in data layer a refreshes the cache in Toothbrush
     *
     * @param realName name to set
     * @return Completable
     */
    @WorkerThread
    @VisibleForTesting
    fun renameToothbrushCompletable(realName: String): Completable {
        return toothbrushRepository
            .rename(connection.toothbrush().mac, realName)
            .andThen(Completable.fromAction { connection.toothbrush().cacheName(realName) })
    }

    @VisibleForTesting
    fun nameIsDfu(): Boolean {
        val defaultLocale = Locale.getDefault()
        return connection.toothbrush().getName().toLowerCase(defaultLocale)
            .startsWith(NAME_DFU.toLowerCase(defaultLocale))
    }

    @SuppressWarnings("ComplexMethod")
    @VisibleForTesting
    fun updateObservable(availableUpdate: AvailableUpdate): Observable<OtaUpdateEvent> {
        return Observable.create<OtaUpdateEvent> { emitter ->
            if (!connection.toothbrush().isRunningBootloader) {
                emitter.onNext(OtaUpdateEvent.fromAction(OTA_UPDATE_REBOOTING))
            }

            val updateFile = File(availableUpdate.updateFilePath)

            val starter = createDfuServiceInitiator(updateFile.absolutePath)

            registerDfuListeners(emitter)

            errorUpdatingFirmware.set(false)
            connection.setState(OTA)

            Timber.tag(TAG).d("Starting DFU update")
            // no need to disconnect
            starter.start(appContext, KLTB003ToothbrushUpdaterService::class.java)
        }
            .scan { previousEvent, newEvent -> previousEvent.updateWithEvent(newEvent) }
    }

    private fun registerDfuListeners(emitter: ObservableEmitter<OtaUpdateEvent>) {
        val listener = dfuProgressListener(emitter)
        DfuServiceListenerHelper.registerProgressListener(appContext, listener)
        DfuServiceListenerHelper.registerLogListener(appContext, this)
    }

    @VisibleForTesting
    fun dfuProgressListener(progressEmitter: ObservableEmitter<OtaUpdateEvent>): SimpleDfuProgressHelper {

        return object : SimpleDfuProgressHelper() {
            private val emitter = progressEmitter
                get() {
                    FailEarly.failInConditionMet(
                        condition = field.isDisposed,
                        message = "Unable to complete the update because emitter is disposed!"
                    )
                    return field
                }

            override fun onDeviceConnecting(deviceAddress: String) {
                Timber.tag(TAG).d("DFU connecting to %s...", deviceAddress)
            }

            override fun onDeviceConnected(deviceAddress: String) {
                Timber.tag(TAG).d("DFU connected to %s", deviceAddress)
                /*
                BleManagerCallbacks were interfering with the State of the connection, which resulted
                in Doctor thinking it didn't need to connect

                For example, KolibreeBleDriver received onDeviceConnecting thus state was set to
                ESTABLISHING, but we never received onDeviceConnected

                By forcing a disconnect here, we remove the callbacks in order to keep the
                connection state agnostic of OTA progress
                 */
                connection.disconnect()
            }

            override fun onDeviceDisconnected(deviceAddress: String) {
                Timber.tag(TAG).d("DFU disconnected from %s", deviceAddress)
            }

            override fun onEnablingDfuMode(deviceAddress: String) {
                super.onEnablingDfuMode(deviceAddress)

                Timber.tag(TAG).d("DFU onEnablingDfuMode from %s", deviceAddress)

                bleDriver.isRunningBootloader = true
            }

            override fun onDfuProcessStarted(deviceAddress: String) {
                Timber.tag(TAG).d("DFU updating %s...", deviceAddress)
            }

            override fun onError(
                deviceAddress: String,
                error: Int,
                errorType: Int,
                message: String?
            ) {
                errorUpdatingFirmware.set(true)
                Timber.tag(TAG).e(
                    "DFU error: %s (error=%s; type=%s). Is recoverable %s",
                    message,
                    error,
                    errorType,
                    isRecoverableError(error)
                )
                unregisterDfuListeners()

                if (isRecoverableError(error)) {
                    emitter.tryOnError(RecoverableDfuException(message ?: "", error))
                } else {
                    emitter.onNext(OtaUpdateEvent.fromAction(OTA_UPDATE_REBOOTING))

                    emitter.tryOnError(Exception("Fatal DFU error: $message ($error)"))
                }
            }

            override fun onDfuCompleted(deviceAddress: String) {
                super.onDfuCompleted(deviceAddress)
                unregisterDfuListeners()

                Timber.tag(TAG).d("DFU completed %s...", deviceAddress)

                mainThreadHandler().postDelayed(
                    { emitter.onComplete() },
                    DELAY_AFTER_DFU.toMillis()
                )
            }

            override fun onDfuAborted(deviceAddress: String) {
                super.onDfuAborted(deviceAddress)
                Timber.tag(TAG).d("DFU aborted $deviceAddress")

                errorUpdatingFirmware.set(true)
                emitter.tryOnError(IllegalStateException("DFU aborted"))
            }

            override fun onProgress(percent: Int) {
                emitter.onNext(
                    OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, percent)
                )
            }

            override fun onDeviceRebooting() {
                Timber.tag(TAG).d("DFU rebooting...")
                emitter.onNext(OtaUpdateEvent.fromAction(OTA_UPDATE_REBOOTING))
            }

            private fun unregisterDfuListeners() {
                DfuServiceListenerHelper.unregisterProgressListener(appContext, this)
                DfuServiceListenerHelper.unregisterLogListener(
                    appContext,
                    this@ToothbrushDfuUpdater
                )
            }
        }
    }

    /**
     * Create a DFU service start with best parameters for M1 toothbrushes
     *
     * @param filePath non null firmware file path
     * @return non null DfuServiceInitiator
     */
    private fun createDfuServiceInitiator(filePath: String): DfuServiceInitiator {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(appContext)
        }

        return DfuServiceInitiator(realMac())
            .setKeepBond(false)
            .setForceDfu(true)
            .setDisableNotification(true)
            .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
            /*
        We were experiencing "Verification failed" error, same as
        https://github.com/NordicSemiconductor/Android-DFU-Library/issues/111

        Enabling PRN slows down the process, but seems to fix issues on some devices such as Huawei
         */
            .setPacketsReceiptNotificationsEnabled(true)
            .setPacketsReceiptNotificationsValue(PACKETS_RECEIPT_NOTIFICATIONS_VALUE)
            .setZip(filePath)
            .setNumberOfRetries(MAX_RETRIES)
    }

    /** Wait until the connection is established.  */
    @VisibleForTesting
    fun waitUntilConnectionEstablished(availableUpdate: AvailableUpdate): Observable<OtaUpdateEvent> {
        return reconnectUnlessActiveOrEstablishing(availableUpdate)
            .doOnError { connection.setState(TERMINATED) }
            .toObservable<OtaUpdateEvent>()
            .timeout(RECONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS, timeoutScheduler)
    }

    private fun reconnectUnlessActiveOrEstablishing(availableUpdate: AvailableUpdate): Completable {
        return Completable.defer {
            when (connection.state().current) {
                ACTIVE -> Completable.complete()
                ESTABLISHING -> connectionActiveCompletable()
                else -> forceReconnectCompletable(availableUpdate)
            }
        }
            .doOnSubscribe {
                Timber.tag(TAG)
                    .d("reconnectUnlessActiveOrEstablishing, state is ${connection.state().current}")
            }
    }

    @VisibleForTesting
    fun forceReconnectCompletable(availableUpdate: AvailableUpdate): Completable {
        return isToothbrushAdvertisingInBootloaderSingle(availableUpdate)
            .flatMapCompletable { isToothbrushInBootloader ->
                Completable.create {
                    Timber.tag(TAG).d("Refreshing isRunningBootloader to $isToothbrushInBootloader")
                    bleDriver.isRunningBootloader = isToothbrushInBootloader

                    // If we leave state == OTA, we never receive disconnect event
                    connection.setState(NEW)

                    runAfterDisconnect { it.onComplete() }
                }
            }
            .andThen(connectionActiveCompletable())
    }

    /**
     * Detects if toothbrush is advertising as [DFU_BOOTLOADER] or as [MAIN]. If it's advertising
     * as DFU, the [Single] will emit true. Otherwise, it will emit false unless there was an error
     * while applying the OTA.
     *
     * If we didn't detect the toothbrush advertising, the value returned will be the one stored in
     * [errorUpdatingFirmware]
     *
     * @return [Single]<[Boolean]> that will emit true if toothbrush is on DFU after an OTA or if
     * there was an error while applying OTA. Otherwise, it'll emit false
     * @see [ToothbrushAdvertisingAppUseCase]
     */
    private fun isToothbrushAdvertisingInBootloaderSingle(availableUpdate: AvailableUpdate): Single<Boolean> {
        return toothbrushAdvertisingAppUseCase.advertisingStateSingle(availableUpdate.type)
            .map { advertisingState ->
                when (advertisingState) {
                    MAIN -> false
                    DFU_BOOTLOADER -> true
                    NOT_FOUND -> errorUpdatingFirmware.get()
                }
            }
    }

    @VisibleForTesting
    fun connectionActiveCompletable(): Completable {
        return activeConnectionUseCase
            .onConnectionsUpdatedStream()
            .doOnSubscribe { Timber.tag(TAG).d("waiting for active connection") }
            .doOnNext { Timber.tag(TAG).d("active connection: $it") }
            .filter { it.toothbrush().mac == connection.toothbrush().mac }
            .take(1)
            .ignoreElements()
    }

    @VisibleForTesting
    fun runAfterDisconnect(runnableAfterDisconnect: Runnable) {
        val listener = object : ConnectionStateListener {
            override fun onConnectionStateChanged(
                connection: KLTBConnection,
                newState: KLTBConnectionState
            ) {
                if (newState === TERMINATED) {
                    connection.state().unregister(this)

                    Timber.tag(TAG).d("runAfterDisconnect scheduling run")
                    mainThreadHandler().post(runnableAfterDisconnect)
                }
            }
        }

        connection.state().register(listener)

        Timber.tag(TAG).d("runAfterDisconnect disconnect, state is %s", connection.state().current)

        try {
            // Let the doctor step in
            isUpdateInProgress.set(false)

            connection.disconnect() // ignore exception on disconnect
        } catch (reason: Exception) {
            Timber.tag(TAG).w(reason)

            listener.onConnectionStateChanged(connection, TERMINATED)
        }
    }

    @VisibleForTesting
    fun mainThreadHandler() = Handler(Looper.getMainLooper())

    @VisibleForTesting
    fun isRecoverableError(errorCode: Int): Boolean {
        return errorCode == DFU_CHARACTERISTICS_NOT_FOUND
    }

    companion object {

        @VisibleForTesting
        internal const val DFU_CHARACTERISTICS_NOT_FOUND = 4102

        @VisibleForTesting
        internal const val NAME_DFU = "Dfu"

        @VisibleForTesting
        internal const val PACKETS_RECEIPT_NOTIFICATIONS_VALUE = 6

        private const val MAX_RETRIES = 5

        /**
         * 65 seconds in theory gives us time for 2 full cycle reconnection attempts
         *
         * BLE Library times out after 30 seconds and retries
         */
        private const val RECONNECT_TIMEOUT_SECONDS = 65L

        private val DELAY_AFTER_DFU = Duration.ofMillis(2000)

        private val TAG = otaTagFor(ToothbrushDfuUpdater::class)
    }

    override fun onLogEvent(p0: String?, p1: Int, message: String?) {
        Timber.tag(TAG).d("DFU log: $message")
    }
}
