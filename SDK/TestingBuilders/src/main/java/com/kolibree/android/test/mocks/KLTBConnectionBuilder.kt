package com.kolibree.android.test.mocks

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.Relay
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.processedbrushings.RecordedSession
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.brushing.BrushingSessionMonitor
import com.kolibree.android.sdk.connection.brushing.OfflineBrushingConsumer
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeManager
import com.kolibree.android.sdk.connection.detectors.DetectorsManager
import com.kolibree.android.sdk.connection.detectors.RNNDetector
import com.kolibree.android.sdk.connection.detectors.RawDetector
import com.kolibree.android.sdk.connection.detectors.SVMDetector
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.battery.Battery
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.connection.user.ToothbrushInSharedModeException
import com.kolibree.android.sdk.connection.user.User
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy.toOfflineBrushing
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.mocks.BrushingBuilder.DEFAULT_GOAL_DURATION
import com.kolibree.sdkws.data.model.GruwareData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BooleanSupplier
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.invocation.InvocationOnMock
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

/** Created by miguelaragues on 16/11/17.  */
@SuppressLint("RxSubscribeOnError", "RxLeakedSubscription")
@Keep
class KLTBConnectionBuilder private constructor(androidLess: Boolean) {

    private val mainThreadHandler: Handler? =
        if (androidLess) null else Handler(Looper.getMainLooper())

    private var connection: InternalKLTBConnection? = null

    private var connectionState: ConnectionState? = null
    private var toothbrush: Toothbrush? = null
    private var userMode: User? = null
    private var detectors: DetectorsManager? = null
    private var vibrator: Vibrator? = null
    private var brushing: Brushing? = null
    private var calibrationData: FloatArray? = null
    private var rnnDetector: RNNDetector? = null
    private var offlineBrushings: MutableList<OfflineBrushing> = mutableListOf()
    private var otaAvailabe: Boolean = false
    private var otaAvailableObservable: Observable<Boolean>? = null
    private var availableUpdate: GruwareData? = null
    private var establishCompletable: Completable? = null
    private var establishDFUCompletable: Completable? = null
    private var battery: Battery? = null
    private var brushingModeManager: BrushingModeManager? = null
    private var emitsVibrationAfterConnectionActive: Boolean = true
    private var brushingSessionMonitor: BrushingSessionMonitor? = null

    private fun withDefaultState(): KLTBConnectionBuilder {
        return withModel(DEFAULT_MODEL)
            .withMockedBrushingSupport()
            .withDefaultMac()
            .withHardwareVersion(DEFAULT_HW_VERSION)
            .withFirmwareVersion(DEFAULT_FW_VERSION)
            .withGruDataVersion(DEFAULT_GRU_VERSION)
            .withBootloaderVersion(DEFAULT_BOOTLOADER_VERSION)
            .withDspVersion(DEFAULT_DSP_VERSION)
            .withSerialNumber(DEFAULT_SERIAL)
            .withName(DEFAULT_NAME)
            .withState(KLTBConnectionState.ACTIVE)
            .withRecordedSessions()
            .withBattery(DEFAULT_BATTERY_LEVEL)
            .withVibration(DEFAULT_VIBRATION_STATE)
            .withBootloader(false)
            .withOfflineBrushings(0)
            .withOwnerId(DEFAULT_OWNER_ID)
            .withPlaqlessRingLedState(DEFAULT_PLAQLESS_RING_LED_STATE)
            .withoutDsp()
            .withOverpressureSensor()
    }

    fun withBrushingInterface(): KLTBConnectionBuilder {
        createBrushing()

        return this
    }

    @JvmOverloads
    fun withSupportEstablish(
        completable: Completable = Completable.complete(),
        dfuCompletable: Completable = Completable.complete()
    ): KLTBConnectionBuilder {
        establishCompletable = completable
        establishDFUCompletable = dfuCompletable

        return this
    }

    fun withOfflineBrushings(nbOfOfflineBrushings: Int): KLTBConnectionBuilder {
        createBrushing()
        whenever(brushing!!.recordCount).thenReturn(Single.just(nbOfOfflineBrushings))

        return this
    }

    @Deprecated(
        replaceWith = ReplaceWith("withMultiListenerInterception"),
        message = "This method does not support multiple listeners or unregister"
    )
    fun withListenerInterception(
        listenerSubject: BehaviorSubject<VibratorListener>
    ): KLTBConnectionBuilder {
        createVibrator().let {
            doAnswer { invocation ->
                listenerSubject.onNext(invocation.getArgument(0))
                null
            }
                .whenever(it)
                .register(any())
        }

        return this
    }

    fun withMultiListenerInterception(
        listenerSubject: BehaviorSubject<Set<VibratorListener>>
    ): KLTBConnectionBuilder {
        fun BehaviorSubject<Set<VibratorListener>>.previousListeners(): MutableSet<VibratorListener> =
            value?.toMutableSet() ?: mutableSetOf()

        createVibrator().let {
            doAnswer { invocation ->
                val previousListeners = listenerSubject.previousListeners()
                listenerSubject.onNext(
                    previousListeners.apply { add(invocation.getArgument(0)) }
                )
                null
            }
                .whenever(it)
                .register(any())

            doAnswer { invocation ->
                val previousListeners = listenerSubject.previousListeners()
                listenerSubject.onNext(
                    previousListeners.apply { remove(invocation.getArgument(0)) }
                )
                null
            }
                .whenever(it)
                .unregister(any())
        }

        return this
    }

    fun withBrushingSessionMonitor(sessionMonitor: BrushingSessionMonitor): KLTBConnectionBuilder {
        this.brushingSessionMonitor = sessionMonitor

        return this
    }

    fun withVibration(enabled: Boolean): KLTBConnectionBuilder {
        createVibrator()

        whenever(vibrator!!.isOn).thenReturn(enabled)

        whenever(vibrator!!.vibratorStream).thenReturn(Flowable.just(enabled))

        return this
    }

    fun withSupportVibrationCommands(): KLTBConnectionBuilder {
        createVibrator()

        whenever(vibrator!!.on()).thenReturn(Completable.complete())
        whenever(vibrator!!.off()).thenReturn(Completable.complete())
        whenever(vibrator!!.offAndStopRecording()).thenReturn(Completable.complete())

        return this
    }

    fun withMockedBrushingSupport(): KLTBConnectionBuilder {
        return withDetectorsSupport()
            .withSupportVibrationCommands()
            .withSupportMonitorCurrent()
            .withDetectorCalibrationData(DEFAULT_CALIBRATION_DATA)
    }

    fun withDetectorsSupport(): KLTBConnectionBuilder {
        createDetectors()

        val rawDetector = mock<RawDetector>()
        whenever(detectors!!.rawData()).thenReturn(rawDetector)

        val svmDetector = mock<SVMDetector>()
        whenever(detectors!!.probableMouthZones()).thenReturn(svmDetector)

        return this
    }

    @JvmOverloads
    fun withPlaqlessRawDetectorSupport(
        plaqlessRawSensorStateFlowable: Flowable<PlaqlessRawSensorState> = Flowable.empty()
    ): KLTBConnectionBuilder {
        createDetectors()

        whenever(detectors!!.plaqlessRawDataNotifications()).thenReturn(
            plaqlessRawSensorStateFlowable
        )

        return this
    }

    @JvmOverloads
    fun withPlaqlessSupport(
        plaqlessSensorStateFlowable: Flowable<PlaqlessSensorState> = Flowable.empty()
    ): KLTBConnectionBuilder {
        createDetectors()

        whenever(detectors!!.plaqlessNotifications()).thenReturn(plaqlessSensorStateFlowable)

        return this
    }

    fun withPingSupport(): KLTBConnectionBuilder {
        createToothbrush().let {
            whenever(it.ping()).thenReturn(Completable.complete())
        }

        return this
    }

    @JvmOverloads
    fun withSupportMonitorCurrent(completable: Completable = Completable.complete()): KLTBConnectionBuilder {
        createBrushing()

        whenever(brushing!!.monitorCurrent()).thenReturn(completable)

        return this
    }

    fun withDetectorCalibrationData(calibrationData: FloatArray): KLTBConnectionBuilder {
        createDetectors()

        this.calibrationData = calibrationData

        return this
    }

    fun withOverpressureSensor(
        flowable: Flowable<OverpressureState> = Flowable.never()
    ): KLTBConnectionBuilder {
        createDetectors()

        whenever(detectors!!.overpressureStateFlowable()).thenReturn(flowable)

        return this
    }

    fun withState(state: KLTBConnectionState): KLTBConnectionBuilder {
        createConnectionState()

        whenever(connectionState!!.current).thenReturn(state)

        whenever(connectionState!!.stateStream).thenReturn(Flowable.just(state))

        return this
    }

    val listeners = HashSet<ConnectionStateListener>()

    fun withStateListener(stateObservable: Observable<KLTBConnectionState>): KLTBConnectionBuilder {
        createConnectionState().let {
            stateObservable
                .subscribe(
                    { newState ->
                        whenever(connection!!.state().current).thenReturn(newState)

                        val runnables = mutableListOf<Runnable>()
                        val iterator = listeners.iterator()
                        while (iterator.hasNext()) {
                            val next = iterator.next()
                            runnables.add(Runnable {
                                next.onConnectionStateChanged(connection!!, newState)
                            })
                        }

                        runnables.forEach {
                            mainThreadHandler?.post(it) ?: it.run()
                        }
                    },
                    Timber::e
                )
        }

        return this
    }

    fun withOTAAvailable(): KLTBConnectionBuilder {
        otaAvailabe = true

        return this
    }

    fun withOTAAvailable(otaAvailableObservable: Observable<Boolean>): KLTBConnectionBuilder {
        this.otaAvailableObservable = otaAvailableObservable

        return this
    }

    fun withOTAAvailable(gruwareData: GruwareData): KLTBConnectionBuilder {
        this.availableUpdate = gruwareData

        return withOTAAvailable()
    }

    fun withOTAUpdateSupport(
        eventObservable: Observable<OtaUpdateEvent>,
        update: AvailableUpdate
    ): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.update(eq(update))).thenReturn(eventObservable)

        return this
    }

    fun withModel(model: ToothbrushModel): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.model).thenReturn(model)

        return this
    }

    fun withDefaultMac(): KLTBConnectionBuilder {
        return withMac(DEFAULT_MAC)
    }

    fun withMac(mac: String): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.mac).thenReturn(mac)

        return this
    }

    fun withName(name: String): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.getName()).thenReturn(name)

        return this
    }

    fun withSetNameSupport(): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.setAndCacheName(anyString()))
            .thenAnswer { invocation ->
                // from now on, return the new name
                whenever(toothbrush!!.getName()).thenReturn(invocation.getArgument(0))

                Completable.complete()
            }

        return this
    }

    fun withSerialNumber(serialNumber: String): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.serialNumber).thenReturn(serialNumber)

        return this
    }

    @JvmOverloads
    fun withBattery(
        level: Int,
        isCharging: Boolean = false,
        isChargingStream: Flowable<Boolean> = Flowable.never()
    ): KLTBConnectionBuilder {
        createToothbrush()
        createBattery()

        whenever(battery!!.batteryLevel).thenReturn(Single.just(level))
        whenever(battery!!.discreteBatteryLevel)
            .thenReturn(Single.error(CommandNotSupportedException("")))
        whenever(battery!!.usesDiscreteLevels).thenReturn(false)
        whenever(battery!!.isCharging).thenReturn(Single.just(isCharging))
        whenever(battery!!.isChargingFlowable()).thenReturn(isChargingStream)
        whenever(toothbrush!!.battery()).thenReturn(battery)

        return this
    }

    fun withBattery(level: DiscreteBatteryLevel): KLTBConnectionBuilder {
        createToothbrush()
        createBattery()

        whenever(battery!!.discreteBatteryLevel).thenReturn(Single.just(level))
        whenever(battery!!.batteryLevel).thenReturn(Single.error(CommandNotSupportedException("")))
        whenever(battery!!.usesDiscreteLevels).thenReturn(true)
        whenever(battery!!.isCharging).thenReturn(Single.just(false))
        whenever(battery!!.isChargingFlowable()).thenReturn(Flowable.never())
        whenever(toothbrush!!.battery()).thenReturn(battery)

        return this
    }

    fun withHardwareVersion(binary: Long): KLTBConnectionBuilder {
        return withHardwareVersion(HardwareVersion(binary))
    }

    fun withHardwareVersion(major: Int, minor: Int): KLTBConnectionBuilder {
        return withHardwareVersion(HardwareVersion(major, minor))
    }

    fun withHardwareVersion(hardwareVersion: HardwareVersion): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.hardwareVersion).thenReturn(hardwareVersion)

        return this
    }

    fun withFirmwareVersion(softwareVersion: String): KLTBConnectionBuilder {
        return withFirmwareVersion(SoftwareVersion(softwareVersion))
    }

    fun withFirmwareVersion(major: Int, minor: Int, revision: Int): KLTBConnectionBuilder {
        return withFirmwareVersion(SoftwareVersion(major, minor, revision))
    }

    fun withFirmwareVersion(softwareVersion: SoftwareVersion): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.firmwareVersion).thenReturn(softwareVersion)

        return this
    }

    fun withDspVersion(dspVersion: DspVersion): KLTBConnectionBuilder {
        createToothbrush().apply {
            whenever(this.dspVersion).thenReturn(dspVersion)
        }

        return this
    }

    fun withBootloaderVersion(softwareVersion: SoftwareVersion): KLTBConnectionBuilder {
        createToothbrush().apply {
            whenever(bootloaderVersion).thenReturn(softwareVersion)
        }

        return this
    }

    fun withBootloaderVersion(softwareVersion: String): KLTBConnectionBuilder =
        withBootloaderVersion(SoftwareVersion(softwareVersion))

    fun withBootloader(isRunningBootloader: Boolean): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.isRunningBootloader).thenReturn(isRunningBootloader)

        return this
    }

    fun withBootloader(booleanSupplier: BooleanSupplier): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.isRunningBootloader)
            .thenAnswer { booleanSupplier.asBoolean }

        return this
    }

    fun withValidGruData(isValidGruData: Boolean): KLTBConnectionBuilder {
        createRnnDetectors().apply {
            whenever(hasValidGruData()).thenReturn(isValidGruData)
        }

        return this
    }

    fun withGruDataBinaryVersion(gruDataBinaryVersion: Long): KLTBConnectionBuilder {
        return withGruDataVersion(SoftwareVersion(gruDataBinaryVersion))
    }

    fun withGruDataVersion(gruDataVersion: String?): KLTBConnectionBuilder {
        var softwareVersion: SoftwareVersion? = null
        if (gruDataVersion != null) {
            softwareVersion = SoftwareVersion(gruDataVersion)
        }

        return withGruDataVersion(softwareVersion)
    }

    fun withGruDataVersion(gruDataVersion: SoftwareVersion?): KLTBConnectionBuilder {
        createRnnDetectors().apply {
            whenever(this.gruDataVersion).thenReturn(gruDataVersion)
        }

        return withValidGruData(gruDataVersion != null && gruDataVersion != SoftwareVersion.NULL)
    }

    fun withSupportGruUpdates(supportsGruUpdates: Boolean): KLTBConnectionBuilder {
        createConnection()?.let {
            whenever(it.supportsGRUUpdates()).thenReturn(supportsGruUpdates)
        }

        return this
    }

    fun withNullRNN(): KLTBConnectionBuilder {
        rnnDetector = null

        return this
    }

    fun withOwnerId(ownerId: Long): KLTBConnectionBuilder {
        createUserMode().apply {
            whenever(profileId()).thenReturn(Single.just(ownerId))
            whenever(isSharedModeEnabled()).thenReturn(Single.just(false))
            whenever(profileOrSharedModeId()).thenReturn(Single.just(ownerId))
        }

        return this
    }

    fun withSharedMode(): KLTBConnectionBuilder {
        createUserMode()

        whenever(userMode!!.profileId()).thenReturn(
            Single.error(ToothbrushInSharedModeException())
        )
        whenever(userMode!!.isSharedModeEnabled()).thenReturn(Single.just(true))
        whenever(userMode!!.profileOrSharedModeId()).thenReturn(Single.just(SHARED_MODE_PROFILE_ID))

        return this
    }

    fun withSupportForSetOperationsOnUserMode(): KLTBConnectionBuilder {
        createUserMode()

        whenever(userMode!!.setProfileId(anyLong())).thenReturn(Completable.complete())
        whenever(userMode!!.enableSharedMode()).thenReturn(Completable.complete())

        return this
    }

    @SuppressLint("VisibleForTests")
    @Deprecated("see {@link KLTBConnectionBuilder#withOfflineBrushings} ")
    fun withRecordedSessions(vararg sessions: RecordedSession): KLTBConnectionBuilder {
        val localOfflineBrushings = arrayOf<OfflineBrushing>()
        for (i in sessions.indices) {
            localOfflineBrushings[i] = sessions[i].toOfflineBrushing()
        }

        return withOfflineBrushings(*localOfflineBrushings)
    }

    fun withOfflineBrushings(vararg offlineBrushings: OfflineBrushing): KLTBConnectionBuilder {
        createBrushing().let {
            whenever(it.recordCount).thenReturn(Single.just(offlineBrushings.size))
        }

        offlineBrushings.forEach {
            this.offlineBrushings.add(it)
        }

        return this
    }

    @JvmOverloads
    fun withBrushingMode(
        availableModes: List<BrushingMode> = listOf(*BrushingMode.values()),
        currentMode: BrushingMode = BrushingMode.defaultMode(),
        lastSync: OffsetDateTime = TrustedClock.getNowOffsetDateTime()
    ): KLTBConnectionBuilder {
        brushingModeManager = mock()
        whenever(brushingModeManager!!.availableBrushingModes()).thenReturn(
            Single.just(
                availableModes
            )
        )
        whenever(brushingModeManager!!.getCurrent()).thenReturn(Single.just(currentMode))
        whenever(brushingModeManager!!.isAvailable()).thenReturn(true)
        whenever(brushingModeManager!!.lastUpdateDate()).thenReturn(Single.just(lastSync))
        whenever(brushingModeManager!!.set(any())).thenReturn(Completable.complete())

        return this
    }

    fun withoutBrushingMode(): KLTBConnectionBuilder {
        brushingModeManager = mock()
        whenever(brushingModeManager!!.availableBrushingModes())
            .thenReturn(Single.error(CommandNotSupportedException()))
        whenever(brushingModeManager!!.getCurrent())
            .thenReturn(Single.error(CommandNotSupportedException()))
        whenever(brushingModeManager!!.isAvailable()).thenReturn(false)
        whenever(brushingModeManager!!.lastUpdateDate())
            .thenReturn(Single.error(CommandNotSupportedException()))
        whenever(brushingModeManager!!.set(any()))
            .thenReturn(Completable.error(CommandNotSupportedException()))

        return this
    }

    fun withPlaqlessRingLedState(state: PlaqlessRingLedState): KLTBConnectionBuilder {
        whenever(detectors!!.plaqlessRingLedState()).thenReturn(Flowable.just(state))
        return this
    }

    fun withPingSupport(completable: Completable = Completable.complete()): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.ping()).thenReturn(completable)

        return this
    }

    fun withDsp(dspState: DspState): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.dspState()).thenReturn(Single.just(dspState))

        return this
    }

    fun withoutDsp(): KLTBConnectionBuilder {
        createToothbrush()

        whenever(toothbrush!!.dspState()).thenReturn(Single.error(CommandNotSupportedException()))

        return this
    }

    fun withEmitsVibrationAfterConectionActive(emitsVibrationAfterConectionActive: Boolean): KLTBConnectionBuilder {
        this.emitsVibrationAfterConnectionActive = emitsVibrationAfterConectionActive

        return this
    }

    fun build(): InternalKLTBConnection {
        createConnection()

        if (connectionState != null) {
            whenever(connection!!.state()).thenReturn(connectionState)
        }

        if (toothbrush != null) {
            whenever(connection!!.toothbrush()).thenReturn(toothbrush)
        }

        if (userMode != null) {
            whenever(connection!!.userMode()).thenReturn(userMode)
        }

        if (detectors != null) {
            whenever(connection!!.detectors()).thenReturn(detectors)

            if (calibrationData != null) {
                whenever(detectors!!.calibrationData).thenReturn(calibrationData)
            }

            if (rnnDetector != null) {
                whenever(detectors!!.mostProbableMouthZones()).thenReturn(rnnDetector)
            }
        }

        if (vibrator != null) {
            whenever(connection!!.vibrator()).thenReturn(vibrator)
        }

        if (brushingModeManager == null) {
            brushingModeManager = createBrushingModeManager()
        }

        if (brushingSessionMonitor != null) {
            whenever(connection!!.brushingSessionMonitor()).thenReturn(brushingSessionMonitor)
        } else {
            val brushingSessionMonitor = mock<BrushingSessionMonitor>()
            whenever(brushingSessionMonitor.sessionMonitorStream).thenReturn(Flowable.never())
            whenever(connection!!.brushingSessionMonitor()).thenReturn(brushingSessionMonitor)
        }

        whenever(connection!!.brushingMode()).thenReturn(brushingModeManager)

        brushing?.let {
            whenever(connection!!.brushing()).thenReturn(brushing)

            userMode?.let { userMode ->
                doAnswer { invocation ->
                    userMode
                        .profileOrSharedModeId()
                        .onTerminateDetach()
                        .subscribe { _ ->
                            simulateOfflineExtraction(invocation)
                        }
                    null
                }
                    .whenever(it)
                    .pullRecords(any())
            }

            whenever(
                connection!!.brushing().setDefaultDuration(anyInt())
            ).thenReturn(Completable.complete())
            whenever(connection!!.brushing().defaultDuration)
                .thenReturn(Single.just(DEFAULT_DURATION))
        }

        if (brushingModeManager != null) {
            whenever(connection!!.brushingMode()).thenReturn(brushingModeManager)
        }

        setupOTAAvailability(connection)

        if (establishCompletable != null) {
            whenever(connection!!.establishCompletable()).thenReturn(establishCompletable)
        }

        if (establishDFUCompletable != null) {
            whenever(connection!!.establishDfuBootloaderCompletable()).thenReturn(
                establishDFUCompletable
            )
        }

        whenever(connection!!.emitsVibrationStateAfterLostConnection())
            .thenReturn(emitsVibrationAfterConnectionActive)

        return connection!!
    }

    private fun simulateOfflineExtraction(invocation: InvocationOnMock) {
        val consumer =
            (invocation.getArgument<Any>(0) as OfflineBrushingConsumer)

        connection?.let { _connection ->
            consumer.onSyncStart(_connection)

            try {
                for (offlineBrushing in offlineBrushings) {
                    consumer
                        .onNewOfflineBrushing(_connection, offlineBrushing, 0)
                }

                consumer.onSuccess(_connection, offlineBrushings.size)
            } catch (throwable: Throwable) {
                consumer.onFailure(_connection, FailureReason(throwable))
            } finally {
                consumer.onSyncEnd(_connection)
            }
        }
    }

    private fun createConnection(): KLTBConnection {
        if (connection == null) {
            connection = mock()
        }

        return connection!!
    }

    @SuppressLint("CheckResult")
    private fun setupOTAAvailability(connection: KLTBConnection?) {
        connection?.let {
            if (otaAvailableObservable != null) {
                whenever(it.hasOTAObservable()).thenReturn(otaAvailableObservable)

                otaAvailableObservable!!.subscribe { hasOTA ->
                    whenever(it.tag).thenReturn(
                        hasOTA
                    )
                }

                propagateChangesIfRelayOrSubject(it)
            } else {
                whenever(it.hasOTAObservable()).thenReturn(Observable.just(otaAvailabe))

                whenever(it.tag).thenReturn(if (otaAvailabe) availableUpdate() else null)
            }
        }
    }

    private fun availableUpdate(): GruwareData {
        return availableUpdate?.let { it } ?: GruwareData(
            AvailableUpdate.empty(UpdateType.TYPE_FIRMWARE),
            AvailableUpdate.empty(UpdateType.TYPE_GRU),
            AvailableUpdate.empty(UpdateType.TYPE_BOOTLOADER),
            AvailableUpdate.empty(UpdateType.TYPE_DSP)
        )
    }

    private fun propagateChangesIfRelayOrSubject(connection: KLTBConnection) {
        if (otaAvailableObservable is Relay<Boolean>) {
            doAnswer { invocation ->
                (otaAvailableObservable as Relay<Boolean>).accept(invocation.getArgument<Any>(0) != null)
                null
            }
                .whenever(connection)
                .tag = any()
        } else if (otaAvailableObservable is Subject<Boolean>) {
            doAnswer { invocation ->
                (otaAvailableObservable as Subject<Boolean>).onNext(invocation.getArgument<Any>(0) != null)
                null
            }
                .whenever(connection)
                .tag = any()
        }
    }

    private fun createRnnDetectors(): RNNDetector {
        createDetectors()

        if (rnnDetector == null) {
            rnnDetector = mock()
        }

        return rnnDetector!!
    }

    private fun createUserMode(): User {
        if (userMode == null) {
            userMode = mock()
        }

        return userMode!!
    }

    private fun createToothbrush(): Toothbrush {
        if (toothbrush == null) {
            toothbrush = mock()
        }

        return toothbrush!!
    }

    private fun createBattery(): Battery {
        if (battery == null) {
            battery = mock()
        }

        return battery!!
    }

    private fun createDetectors(): DetectorsManager {
        if (detectors == null) {
            detectors = mock()
        }

        return detectors!!
    }

    private fun createVibrator(): Vibrator {
        if (vibrator == null) {
            vibrator = mock()
        }

        return vibrator!!
    }

    private fun createBrushing(): Brushing {
        if (brushing == null) {
            brushing = mock()
        }

        return brushing!!
    }

    private fun createConnectionState(): ConnectionState {
        if (connectionState == null) {
            connectionState = mock<ConnectionState>().apply {
                doAnswer { invocation ->
                    val newListener = invocation.getArgument(0) as ConnectionStateListener
                    if (!listeners.contains(newListener)) {
                        listeners.add(newListener)

                        // comply with the register interface and notify the listener immediately
                        mainThreadHandler?.post {
                            newListener.onConnectionStateChanged(createConnection(), current)
                        } ?: newListener.onConnectionStateChanged(createConnection(), current)
                    } else {
                        Timber.d(" listener $newListener already in Set")
                    }
                    null
                }
                    .whenever(this)
                    .register(any())

                doAnswer { invocation ->
                    val listener = invocation.getArgument(0) as ConnectionStateListener
                    listeners.remove(listener)
                    null
                }
                    .whenever(this)
                    .unregister(any())
            }
        }

        return connectionState!!
    }

    private fun createBrushingModeManager(): BrushingModeManager {
        if (brushingModeManager == null) {
            brushingModeManager = mock()
        }

        return brushingModeManager!!
    }

    companion object {

        const val DEFAULT_DURATION = DEFAULT_GOAL_DURATION
        const val DEFAULT_MAC = "00:D0:56:F2:B5:12"
        const val DEFAULT_SERIAL = "ktb02eb00947-999999"
        const val DEFAULT_FW_VERSION_MAJOR = 0
        const val DEFAULT_FW_VERSION_MINOR = 0
        const val DEFAULT_FW_VERSION_REVISION = 1
        const val DEFAULT_NAME = "YOUR TEST toothbrush"
        const val DEFAULT_HW_VERSION_MAJOR = 2
        const val DEFAULT_HW_VERSION_MINOR = 2
        const val DEFAULT_GRU_VERSION_MAJOR = 1
        const val DEFAULT_GRU_VERSION_MINOR = 1
        const val DEFAULT_GRU_VERSION_REVISION = 2
        const val DEFAULT_OWNER_ID = ProfileBuilder.DEFAULT_ID
        const val DEFAULT_BOOTLOADER_VERSION_MAJOR = 1
        const val DEFAULT_BOOTLOADER_VERSION_MINOR = 2
        const val DEFAULT_BOOTLOADER_VERSION_REVISION = 3
        const val DEFAULT_BATTERY_LEVEL = 100
        const val DEFAULT_VIBRATION_STATE = false

        @JvmField
        val DEFAULT_MODEL = ToothbrushModel.ARA

        @JvmField
        val DEFAULT_FW_VERSION = SoftwareVersion(
            DEFAULT_FW_VERSION_MAJOR, DEFAULT_FW_VERSION_MINOR, DEFAULT_FW_VERSION_REVISION
        )

        @JvmField
        val DEFAULT_GRU_VERSION = SoftwareVersion(
            DEFAULT_GRU_VERSION_MAJOR, DEFAULT_GRU_VERSION_MINOR, DEFAULT_GRU_VERSION_REVISION
        )

        @JvmField
        val DEFAULT_HW_VERSION = HardwareVersion(DEFAULT_HW_VERSION_MAJOR, DEFAULT_HW_VERSION_MINOR)

        @JvmField
        val DEFAULT_BOOTLOADER_VERSION = SoftwareVersion(
            DEFAULT_BOOTLOADER_VERSION_MAJOR,
            DEFAULT_BOOTLOADER_VERSION_MINOR,
            DEFAULT_BOOTLOADER_VERSION_REVISION
        )

        @JvmStatic
        val DEFAULT_DSP_VERSION = DspVersion.NULL

        var DEFAULT_CALIBRATION_DATA = floatArrayOf(2f, 7f, 9f)

        @VisibleForTesting
        @JvmField
        val DEFAULT_PLAQLESS_RING_LED_STATE =
            PlaqlessRingLedState(0.toShort(), 0.toShort(), 0.toShort(), 0.toShort())

        @JvmStatic
        @JvmOverloads
        fun createWithDefaultState(androidLess: Boolean = false): KLTBConnectionBuilder {
            return KLTBConnectionBuilder(androidLess).withDefaultState()
        }

        /*
    Intended mainly for unit tests, since the default state includes invocations to real Android stack
    */
        @JvmStatic
        fun createAndroidLess(): KLTBConnectionBuilder {
            return createWithDefaultState(androidLess = true)
        }
    }
}
