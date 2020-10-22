package com.kolibree.android.sdk.core

import android.content.Context
import android.os.Handler
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.brushing.BrushingSessionMonitor
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeManager
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeManagerFactory
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeObserver
import com.kolibree.android.sdk.connection.detectors.DetectorsManager
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.connection.detectors.data.WeightedMouthZone
import com.kolibree.android.sdk.connection.parameters.Parameters
import com.kolibree.android.sdk.connection.root.Root
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATING
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.user.User
import com.kolibree.android.sdk.connection.user.UserImpl
import com.kolibree.android.sdk.connection.user.UserInternal
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.core.di.DaggerConnectionComponent
import com.kolibree.android.sdk.core.driver.DeviceDriver
import com.kolibree.android.sdk.core.driver.KLTBDriver
import com.kolibree.android.sdk.core.driver.KLTBDriverFactory
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.toothbrush.ToothbrushImplementation
import com.kolibree.android.sdk.core.toothbrush.ToothbrushImplementationFactory
import com.kolibree.android.sdk.error.ConnectionEstablishException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.kml.MouthZone16
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Created by aurelien on 25/08/16.
 *
 *
 * [KLTBConnection] implementation
 */
@Suppress("LargeClass", "TooManyFunctions")
internal class KLTBConnectionImpl : InternalKLTBConnection, KLTBDriverListener {

    @VisibleForTesting
    val tagRelay = BehaviorRelay.create<Boolean>()

    /** Bluetooth device driver  */
    private val deviceDriver: DeviceDriver

    /** Vibrator interface  */
    private val vibrator: VibratorImpl

    /** Brushing session monitor */
    private val sessionMonitor: BrushingSessionMonitorImpl

    /** Parameters interface  */
    private val parameters: ParametersImpl

    /** Detectors interface  */
    private val detectors: DetectorsManagerImpl

    /** Toothbrush interface  */
    private val toothbrush: ToothbrushImplementation

    /** Connection state interface  */
    private val state: ConnectionStateImpl

    /** Brushing interface  */
    private val brushing: BrushingImpl

    /** Toothbrush user interface  */
    private val user: UserInternal

    /** Root settings interface  */
    private val root: Root

    /** Brushing manager interface */
    private val brushingModeManager: BrushingModeManager

    @VisibleForTesting
    var establishCompletableEmitter: CompletableEmitter? = null

    @VisibleForTesting
    @Volatile
    var establishCompletable: Completable? = null

    /** Can be useful to add some custom member to a connection (use it as Android's View's one)  */
    override var tag: Any? = null
        set(value) {
            field = value
            tagRelay.accept(tag != null)
        }

    override val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Constructor to be used by KolibreeService.
     *
     * @param context any Context. Not stored locally, only used to create the KLTBDriver
     * @param mac Adress of the toothbrush to pair with
     * @param name Name of the toothbrush
     * @param model ToothbrushModel
     * @param driverFactory Factory to be used to create the KLTBDriver
     */
    constructor(
        context: Context,
        mac: String,
        name: String,
        model: ToothbrushModel,
        driverFactory: KLTBDriverFactory
    ) {
        val driver = driverFactory.create(context, mac, model, this)
        deviceDriver = driver
        state = ConnectionStateImpl(this)
        parameters = ParametersImpl(driver)
        toothbrush = ToothbrushImplementationFactory.createToothbrushImplementation(
            this, context, driver, mac, model, name
        )
        vibrator = VibratorImpl(this, driver)
        brushing = BrushingImpl(this, driver)
        detectors = DetectorsManagerImpl(model, driver)
        root = RootCommonBleImpl(driver as BleDriver)
        user = UserImpl(model, driver as BleDriver)
        brushingModeManager = createBrushingModeManager(model, driver)
        sessionMonitor = BrushingSessionMonitorImpl()
    }

    private fun createBrushingModeManager(
        model: ToothbrushModel,
        driver: KLTBDriver
    ) = BrushingModeManagerFactory.createBrushingModeManager(model, driver, state)

    @VisibleForTesting
    constructor(
        driver: KLTBDriver,
        toothbrushImplementation: ToothbrushImplementation,
        detectorsManager: DetectorsManagerImpl,
        mainHandler: Handler
    ) {
        deviceDriver = driver
        detectors = detectorsManager
        toothbrush = toothbrushImplementation

        state = ConnectionStateImpl(this, mainHandler = mainHandler)
        parameters = ParametersImpl(driver)
        vibrator = VibratorImpl(this, driver, mainHandler = mainHandler)
        brushing = BrushingImpl(this, driver)
        root = RootCommonBleImpl(driver as BleDriver)
        user = UserImpl(toothbrushImplementation.model, driver as BleDriver)
        brushingModeManager = createBrushingModeManager(toothbrushImplementation.model, driver)
        sessionMonitor = BrushingSessionMonitorImpl(mainHandler)
    }

    @Inject
    lateinit var brushingModeObserver: BrushingModeObserver

    override fun toothbrush(): Toothbrush = toothbrush

    override fun vibrator(): Vibrator = vibrator

    override fun brushingSessionMonitor(): BrushingSessionMonitor = sessionMonitor

    override fun state(): ConnectionState = state

    override fun detectors(): DetectorsManager = detectors

    override fun brushing(): Brushing = brushing

    override fun parameters(): Parameters = parameters

    override fun root(): Root = root

    override fun userMode(): User = user

    override fun brushingMode() = brushingModeManager

    override fun supportsGRUUpdates(): Boolean =
        toothbrush.model.supportsGRUDataUpdate() && driver().supportsGRUData()

    override fun onDisconnected() {
        disposables.clear()

        brushing.clearCache()
        state.clearCache()
        detectors.clearCache()
        parameters.clearCache()
        user.clearCache()
        vibrator.clearCache()

        tag = null

        if (state.current !== TERMINATED) {
            Timber.tag(TAG).d("KLTB set terminated")
            state.set(TERMINATED)
        } else {
            Timber.tag(TAG).w("Not notifying disconnected, state is %s", state.current)
        }
    }

    override fun onSensorRawData(rawData: RawSensorState) {
        detectors.onRawData(this, rawData)
    }

    override fun onVibratorStateChanged(on: Boolean) {
        vibrator.onVibratorStateChanged(on)
    }

    override fun onSVMDetection(detection: List<MouthZone16>) {
        detectors.onSVMData(this, detection)
    }

    override fun onBrushingSessionStateChanged(started: Boolean) {
        sessionMonitor.onBrushingSessionStateChanged(started)
    }

    override fun onRNNDetection(detection: List<WeightedMouthZone>) {
        detectors.onRNNData(this, detection)
    }

    override fun onConnectionEstablishing() {
        synchronized(this) {
            if (state().current !== OTA) {
                state.set(ESTABLISHING)
            }
        }
    }

    override fun hasOTAObservable(): Observable<Boolean> {
        var observable = tagRelay.hide()

        if (!tagRelay.hasValue()) {
            observable = observable.startWith(false)
        }

        return observable
    }

    @VisibleForTesting
    @Suppress("TooGenericExceptionCaught")
    fun establish(runningBootloader: Boolean) {
        synchronized(this) {
            if (!(state.current !== ESTABLISHING &&
                    state.current !== ACTIVE &&
                    state.current !== TERMINATING)
            ) { // Prevent SDK misuse
                Timber.tag(TAG)
                    .w("Attempted to establish connection, but state is %s", state.current.name)
                return
            }
        }

        disposables += Completable.create { emitter ->
            Timber.tag(TAG)
                .d(
                    "Invoking driver connect $this  with emitter  $establishCompletableEmitter"
                )
            try {
                if (runningBootloader) {
                    deviceDriver.connectDfuBootloader()
                } else {
                    deviceDriver.connect()
                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).d("Connect exception in %s", this@KLTBConnectionImpl)
                emitter.tryOnError(e)
            }
        }
            .doOnSubscribe { onConnectionEstablishing() }
            .subscribeOn(Schedulers.io())
            .onErrorComplete { throwable ->
                onConnectionError(throwable)
                false
            }
            .onTerminateDetach()
            .subscribe(
                {
                    Timber.tag(TAG)
                        .d("establish innerComplete completed, state is ${state().current}")
                },
                { e -> Timber.e(e, "establish encountered an error") })
    }

    @Synchronized
    override fun establishCompletable(): Completable {
        Timber.tag(TAG).d("Invoked establishCompletable with state %s", state.current)
        return establishCompletable(false)
    }

    @Synchronized
    override fun establishDfuBootloaderCompletable(): Completable {
        Timber.tag(TAG).d("Invoked establishDfuBootloaderCompletable")
        return establishCompletable(true)
    }

    @VisibleForTesting
    @Synchronized
    fun establishCompletable(runningBootloader: Boolean): Completable {
        establishCompletableEmitter?.apply {
            if (establishCompletable == null) {
                tryOnError(
                    ConnectionEstablishException("Completable was null. Unexpected")
                )
            }
        }

        createEstablishCompletable(runningBootloader)

        return establishCompletable!!
    }

    private fun createEstablishCompletable(runningBootloader: Boolean) {
        Timber.tag(TAG)
            .d(
                "Create establish completable, current state is %s. Running bootloader %s",
                state().current, runningBootloader
            )
        if (establishCompletable == null) {
            synchronized(this) {
                if (establishCompletable == null) {
                    establishCompletable = Completable.create { emitter ->
                        establishCompletableEmitter = emitter

                        Timber.tag(TAG)
                            .d("creating completable, emitter is %s", establishCompletableEmitter)
                    }
                        .doOnSubscribe {
                            Timber.tag(TAG)
                                .d(
                                    "Subscribed to establish completable, emitter is %s",
                                    establishCompletableEmitter
                                )
                        }
                        .doOnSubscribe { establish(runningBootloader) }
                        .doOnDispose { setState(KLTBConnectionState.NEW) }
                        .doFinally {
                            synchronized(this@KLTBConnectionImpl) {
                                Timber.tag(TAG).d("nullifying establishCompletableEmitter")
                                establishCompletableEmitter = null
                                establishCompletable = null
                            }
                        }
                }
            }
        }

        Timber.tag(TAG).d("Exit createEstablishCompletable is %s", establishCompletableEmitter)
    }

    @VisibleForTesting
    fun onConnectionError(throwable: Throwable) {
        Timber.tag(TAG).d("On connection error, current state is %s in %s", state().current, this)
        if (state().current !== OTA) {
            innerState().set(KLTBConnectionState.NEW)
        }

        maybeNotifyErrorThroughCompletable(throwable)
    }

    @Throws(FailureReason::class)
    override fun onConnectionEstablished() {
        Timber.tag(TAG).w("onConnectionEstablished with state %s on %s", state().current, this)
        if (!innerToothbrush().isRunningBootloader) {
            deviceDriver.setTime()
            deviceDriver.disableMultiUserMode()
            toothbrush.setSerialNumber(deviceDriver.getSerialNumber())
            detectors.setGruInfo(deviceDriver.hasValidGruData(), deviceDriver.gruDataVersion)

            if (toothbrush.model.useNickName() && deviceDriver is BleDriver) {
                toothbrush.cacheName(deviceDriver.queryRealName())
            }
        }

        if (state().current !== OTA) {
            innerState().set(ACTIVE)
        }

        maybeNotifyThroughCompletable()

        injectDependencies()
    }

    /**
     * Inject dependencies on first invocation
     *
     * Subsequent invocations will be ignored
     */
    @VisibleForTesting
    fun injectDependencies() {
        if (::brushingModeObserver.isInitialized.not()) {
            DaggerConnectionComponent.factory()
                .create(
                    context = KolibreeAndroidSdk.getSdkComponent().applicationContext(),
                    connection = this
                )
                .inject(this)
        }
    }

    @Synchronized
    override fun setState(state: KLTBConnectionState) {
        this.state.set(state)
    }

    override fun emitsVibrationStateAfterLostConnection(): Boolean =
        deviceDriver.supportsBrushingEventsPolling()

    fun driver(): DeviceDriver = deviceDriver

    @VisibleForTesting
    fun innerToothbrush(): ToothbrushImplementation = toothbrush

    @VisibleForTesting
    fun innerState(): ConnectionStateImpl = state

    @VisibleForTesting
    @Synchronized
    fun maybeNotifyThroughCompletable() {
        Timber.tag(TAG)
            .w("maybeNotifyThroughCompletable is $establishCompletableEmitter in $this")
        establishCompletableEmitter?.apply {
            Timber.tag(TAG)
                .w(
                    "maybeNotifyThroughCompletable isDisposed? $isDisposed"
                )
            if (!isDisposed) {
                onComplete()
            } else {
                establishCompletableEmitter = null
            }
        }
    }

    @VisibleForTesting
    @Synchronized
    fun maybeNotifyErrorThroughCompletable(throwable: Throwable) {
        establishCompletableEmitter?.apply {
            tryOnError(ConnectionEstablishException(throwable))
        }
    }

    override fun disconnect() {
        Timber.tag(TAG).d("KLTB disconnect")
        maybeEmitConnectionException()

        maybeSetTerminatingState()

        try {
            Timber.tag(TAG).d("KLTB invoke disconnect")
            deviceDriver.disconnect()
        } catch (failureReason: FailureReason) {
            Timber.tag(TAG).w(failureReason)
        }

        Timber.tag(TAG).d("KLTB post invoke disconnect")
    }

    private fun maybeSetTerminatingState() {
        synchronized(this) {
            state().current.let { currentState ->
                if (currentState !== TERMINATED &&
                    currentState !== TERMINATING &&
                    currentState !== OTA
                ) {
                    state.set(TERMINATING)
                }
            }
        }
    }

    private fun maybeEmitConnectionException() {
        val emitter = synchronized(this) { establishCompletableEmitter }

        emitter?.apply {
            if (!isDisposed)
                tryOnError(ConnectionEstablishException("Disconnect invoked on KLTBConnection"))
        }
    }

    override fun reconnectCompletable(): Completable = deviceDriver.reconnect()

    override fun setGruDataUpdatedVersion(gruDataUpdatedVersion: SoftwareVersion) {
        detectors.setGruInfo(true, gruDataUpdatedVersion)
    }

    override fun deviceParametersCharacteristicChangedStream(): Flowable<ByteArray> {
        return when (deviceDriver) {
            is BleDriver -> deviceDriver.deviceParametersCharacteristicChangedStream()
            else -> Flowable.empty()
        }
    }

    override fun isConnectionAllowed(): Boolean {
        return toothbrush.isConnectionAllowed()
    }

    companion object {
        private val TAG = bluetoothTagFor(KLTBConnectionImpl::class.java)
    }
}
