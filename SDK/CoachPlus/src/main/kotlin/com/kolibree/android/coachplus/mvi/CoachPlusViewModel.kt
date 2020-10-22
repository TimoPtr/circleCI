/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.coachplus.CoachPlusAnalytics
import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.coachplus.controller.CoachPlusControllerResult
import com.kolibree.android.coachplus.controller.kml.CoachPlusKmlControllerImpl
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.coachplus.logic.R
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.coachplus.ui.colors.CurrentZoneColorProvider
import com.kolibree.android.coachplus.utils.RingLedColorUseCase
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.commons.MIN_BRUSHING_DURATION_SECONDS
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.feature.CoachPlusPlaqlessSupervisionFeature
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.bi.Contract
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.mvi.BaseGameViewModel
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeUseCase
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTING
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_LOST
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.kml.BrushingSession
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import timber.log.Timber

@Keep
class CoachPlusViewModel(
    initialViewState: CoachPlusViewState?,
    macAddress: Optional<String>,
    toothbrushModel: ToothbrushModel?,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    lostConnectionHandler: LostConnectionHandler,
    private val soundInteractor: SoundInteractor,
    colorSet: CoachPlusColorSet,
    private val connector: IKolibreeConnector,
    private val coachPlusController: CoachPlusController,
    private val coachSettingsRepository: CoachSettingsRepository,
    private val zoneHintProvider: ZoneHintProvider,
    tickPeriod: Duration,
    private val confirmBrushingModeUseCase: ConfirmBrushingModeUseCase,
    private val ringLedColorUseCase: RingLedColorUseCase,
    private val brushingCreator: BrushingCreator,
    private val kmlAvroCreator: KmlAvroCreator,
    keepScreenOnController: KeepScreenOnController,
    private val currentZoneColorProvider: CurrentZoneColorProvider,
    private val featuresToggle: FeatureToggleSet,
    private val analytics: CoachPlusAnalytics
) :
    BaseGameViewModel<CoachPlusViewState>(
        initiateViewState(initialViewState, macAddress),
        macAddress,
        gameInteractor,
        facade,
        lostConnectionHandler,
        keepScreenOnController
    ),
    BrushingCreator.Listener {

    @VisibleForTesting
    val disposables = CompositeDisposable()

    @VisibleForTesting
    val isManual = macAddress.isPresent.not()

    @ColorInt
    val coachPlusTitleColor = colorSet.titleColor

    @ColorInt
    val coachPlusViewBackgroundColor = colorSet.backgroundColor

    val brushingProgramAvailable = toothbrushModel?.supportsVibrationSpeedUpdate() == true

    /*
     * Tells if the activity has been started
     */
    val isInit: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isInit ?: false
    }

    /*
     * Tells if the activity is currently playing
     */
    val isPlaying: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isPlaying == true
    }

    /*
     * Tells if the toothbrush head should be display or not
     */
    val shouldShowToothbrushHead: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.shouldShowToothbrushHead == true
    }

    /*
     * Tells the color of the ring of the toothbrush head
     */
    val ringLedColor: LiveData<Int?> = map(viewStateLiveData) { viewState ->
        viewState?.ringLedColor
    }

    /*
     * Tells the currentZone brushing
     */
    val currentZone: LiveData<MouthZone16?> = map(viewStateLiveData) { viewState ->
        viewState?.currentZone
    }

    /*
     * Tells the color of the current zone
     */
    val currentZoneColor: LiveData<Int?> = map(viewStateLiveData) { viewState ->
        viewState?.let {
            currentZoneColorProvider.provideCurrentZoneColor(
                it.currentZoneProgress
            )
        }
    }

    /*
     * Tells the progress of the current zone
     */
    val progressPercentage: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.let {
            it.currentZoneProgress
        }
    }

    /*
     * Tells the color of border of the progress view
     */
    val borderProgressColor: LiveData<Int> = map(viewStateLiveData) { viewState ->
        if (viewState?.isBrushingGoodZone == true) Color.WHITE else Color.RED
    }

    /*
     * Tells the string resources to use to display hint
     */
    val hint: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.getHint(coachPlusController, zoneHintProvider) ?: R.string.empty
    }

    /*
     * Tells which feedback should be display
     */
    val feedback: LiveData<FeedBackMessage> = map(viewStateLiveData) { viewState ->
        viewState?.optionalFeedback ?: FeedBackMessage.EmptyFeedback
    }

    /*
     * Tells if the play button should be display on the screen (manual mode)
     */
    val shouldShowPlay: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isInit?.not() == true && isManual
    }

    /*
     * Tells if the pause screen should be display
     */
    val shouldShowPause: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.shouldShowPause
    }

    val zoneData: LiveData<ZoneProgressData> = map(viewStateLiveData) { viewState ->
        viewState?.zoneProgressData ?: guidedBrushingZones()
    }

    /*
     * This relay contain the latest version of the settings for Coach+
     */
    @VisibleForTesting
    val settingsRelay: Relay<CoachSettings> = BehaviorRelay.create()

    @VisibleForTesting
    var ringLedColorUseCaseDisposable: Disposable? = null

    @VisibleForTesting
    var supervisionDisposable: Disposable? = null

    /*
     * Observable that emit at tickPeriod interval, which will be used to update the result on
     * screen
     */
    @VisibleForTesting
    val tickerObservable: Observable<Long> = Observable.interval(
        tickPeriod.toMillis(),
        TimeUnit.MILLISECONDS,
        Schedulers.single()
    )

    /**
     * Flowable that keep track if the current activity is pause or not
     */
    val isPlayingStream = viewStateFlowable.map { it.isPlaying }

    /**
     * Backed Observable that notify if the user have restarted the brushing session
     */
    private val restartPublisher = BehaviorRelay.create<Unit>()
    val restartStream: Observable<Unit> get() = restartPublisher

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadSettings()
        initTicker()
        initTransitionDetection()
        soundInteractor.setLifecycleOwner(owner)
        brushingCreator.setLifecycleOwner(owner)
        brushingCreator.addListener(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        initAmbientSound()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        stopCoachPlus()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        disposables.clear()
        brushingCreator.removeListener(this)
        super.onDestroy(owner)
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
        brushingCreator.removeListener(this)
    }

    override fun onConnectionEstablished(connection: KLTBConnection) {
        val model = connection.toothbrush().model

        if (model == ToothbrushModel.PLAQLESS) {
            listenToRingLedChange(connection)
            sendSupervisionInfo(connection)
        }

        if (connection.vibrator().isOn) {
            startCoachPlus()
        }
    }

    override fun onLostConnectionHandleStateChanged(
        connection: KLTBConnection,
        state: LostConnectionHandler.State
    ) {
        when (state) {
            CONNECTION_LOST, CONNECTING -> stopCoachPlus()
            CONNECTION_ACTIVE -> coachPlusController.notifyReconnection()
        }
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        super.onVibratorOn(connection)
        getViewState()?.let {
            if (it.shouldShowPause) Analytics.send(analytics.resume())
        }
        startCoachPlus()
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        super.onVibratorOff(connection)
        pauseCoachPlus()
    }

    override fun onSuccessfullySentData() {
        pushAction(CoachPlusActions.DataSaved)
    }

    override fun somethingWrong(error: Throwable) {
        pushAction(CoachPlusActions.SomethingWrong(error))
    }

    @VisibleForTesting
    fun pauseCoachPlus() {
        getViewState()?.let {
            if (!it.shouldShowPause) Analytics.send(analytics.pause())
        }
        stopCoachPlus()
    }

    fun onQuitButtonClick() {
        Analytics.send(analytics.quit())
        onGameFinishing()
    }

    fun onRestartButtonClick() {
        Analytics.send(analytics.restart())

        restartPublisher.accept(Unit)

        if (!isManual) {
            publishAvro()
        }

        coachPlusController.reset()

        nullifyCurrentZone()
        resetBrushingZones()

        pushAction(CoachPlusActions.Restarted)

        resumeCoachPlus()
    }

    fun onResumeButtonClick() {
        Analytics.send(analytics.resume())
        // previously we were checking connection and if manual and we might send and error
        // when not manual and not connected
        resumeCoachPlus()
    }

    fun onSettingsButtonClick() {
        pushAction(CoachPlusActions.OpenSettings(gameInteractor.toothbrushMac))
    }

    fun onManualPause() {
        if (isManual) {
            pauseCoachPlus()
        }
    }

    fun onManualStart() {
        if (isManual) {
            startCoachPlus()
        }
    }

    fun onBrushingProgramButtonClick() {
        stopCoachPlus()
        gameInteractor.connection?.let { connection ->
            disposables += Single.zip(
                connection.brushingMode().availableBrushingModes(),
                connection.brushingMode().getCurrent(),
                BiFunction<List<BrushingMode>,
                    BrushingMode,
                    CoachPlusActions> { availableModes, currentMode ->
                    CoachPlusActions.ShowBrushingModeDialog(availableModes, currentMode)
                }
            ).subscribeOn(Schedulers.io())
                .subscribe({
                    pushAction(it)
                }, Timber::e)
        }
    }

    fun onBrushingModeSelected(brushingMode: BrushingMode) {
        disposables += Single.fromCallable { connector.currentProfile!!.id }
            .flatMapCompletable { profileId ->
                confirmBrushingModeUseCase.confirmBrushingModeCompletable(profileId, brushingMode)
            }.subscribeOn(Schedulers.io())
            .subscribe({ }, Timber::e)
    }

    fun onBackPressed() = when {
        getViewState()?.isInit == false -> pushAction(CoachPlusActions.Cancel)
        getViewState()?.isPlaying == false -> onGameFinishing()
        else -> pauseCoachPlus()
    }

    @VisibleForTesting
    internal fun loadSettings() {
        disposables += Flowable.fromCallable { connector.currentProfile!!.id }
            .flatMap { profileId ->
                coachSettingsRepository.getSettingsByProfileId(profileId)
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ settings ->
                settingsRelay.accept(settings)
                updateViewState {
                    copy(
                        isBrushingMovementEnabled = settings.enableBrushingMovement,
                        isHelpTextEnabled = settings.enableHelpText
                    )
                }
            }, Timber::e)
    }

    @VisibleForTesting
    internal fun initTicker() {
        disposables += isPlayingStream.toObservable()
            .switchMap { isPlaying ->
                if (isPlaying) {
                    tickerObservable
                } else {
                    Observable.empty()
                }
            }.subscribeOn(Schedulers.io()).subscribe(::onTick, Timber::e)
    }

    @VisibleForTesting
    internal fun initTransitionDetection() {
        disposables += settingsRelay.filter { settings -> settings.enableTransitionSounds }
            .flatMap { coachPlusController.zoneChangeObservable }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .subscribe({
                soundInteractor.playTransitionSound()
            }, Timber::e)
    }

    @VisibleForTesting
    internal fun startCoachPlus() {
        if (!isManual && gameInteractor.connection != null) {
            startToothbrushVibration()
        } else if (!isManual) {
            pushAction(CoachPlusActions.SomethingWrong(IllegalStateException("no connection and not manual")))
            return
        }

        updateViewState { copy(isPlaying = true, isInit = true) }
    }

    @VisibleForTesting
    internal fun resumeCoachPlus() {
        startCoachPlus()
    }

    @VisibleForTesting
    internal fun stopCoachPlus(permanently: Boolean = false) {
        stopToothbrushVibration()
        coachPlusController.onPause()
        updateViewState {
            copy(
                isPlaying = false,
                isEnd = permanently || isEnd, // If the game ended already, we don't want to change that
                // We clear the feedback since we will reset the feedbackMapper (it's a hack to avoid some blink)
                feedBackMessage = FeedBackMessage.EmptyFeedback,
                outOfMouth = false // reset this error otherwise pause won't show up
            )
        }
    }

    @VisibleForTesting
    internal fun stopToothbrushVibration() {
        gameInteractor.connection?.let { connection ->
            if (connection.vibrator().isOn) {
                disposables += connection
                    .vibrator()
                    .off()
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, Timber::e)
            }
        }
    }

    @VisibleForTesting
    internal fun startToothbrushVibration() {
        gameInteractor.connection?.let { connection ->
            if (!connection.vibrator().isOn) {
                disposables += connection
                    .vibrator()
                    .on()
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, Timber::e)
            }
        }
    }

    @VisibleForTesting
    internal fun onTick(tick: Long) {
        val result = coachPlusController.onTick()

        if (result.sequenceFinished) {
            onGameFinished(true)
        } else {
            onGameRunning(result)
        }
    }

    @VisibleForTesting
    internal fun onGameRunning(result: CoachPlusControllerResult) {
        updateViewState {
            updateWith(result)
        }
    }

    @VisibleForTesting
    internal fun onGameFinishing() {
        onGameFinished(coachPlusController.computeBrushingDuration() >= MIN_BRUSHING_DURATION_SECONDS)
    }

    @VisibleForTesting
    internal fun onGameFinished(finishedWithSuccess: Boolean) {
        stopCoachPlus(permanently = true)

        if (!isManual) {
            publishAvro()
        }

        if (finishedWithSuccess) {
            onBrushingCompleted()
        } else {
            pushAction(CoachPlusActions.Cancel)
        }
    }

    @VisibleForTesting
    internal fun publishAvro() {
        disposables += publishKmlAvroDataCompletable()
            .subscribeOn(Schedulers.io())
            .subscribe({ Timber.d("KML AVRO data submitted") }, Timber::e)
    }

    @VisibleForTesting
    internal fun shouldSendPlaqlessSupervision() =
        featuresToggle.toggleForFeature(CoachPlusPlaqlessSupervisionFeature).value

    @VisibleForTesting
    internal fun publishKmlAvroDataCompletable() =
        createAvroBrushingSessionSingle()
            .flatMap { generateAvroDataSingle(it) }
            .flatMapCompletable { kmlAvroCreator.submitAvroData(it) }

    @VisibleForTesting
    internal fun createAvroBrushingSessionSingle() =
        gameInteractor
            .connection
            ?.let {
                kmlAvroCreator.createBrushingSession(
                    activityName = Contract.ActivityName.COACH_PLUS,
                    avroTransitionsTable = coachPlusController.getAvroTransitionsTable(),
                    connection = it,
                    isPlaqlessSupervised = shouldSendPlaqlessSupervision()
                )
            }
            ?: Single.error(IllegalStateException("Connection is null"))

    @VisibleForTesting
    internal fun generateAvroDataSingle(brushingSession: BrushingSession) =
        if (coachPlusController is CoachPlusKmlControllerImpl) {
            Single.fromCallable { coachPlusController.kmlAvroData(brushingSession) }
        } else {
            Single.error(IllegalStateException("Not a KML controller"))
        }

    @VisibleForTesting
    internal fun onBrushingCompleted() {
        brushingCreator.onBrushingCompleted(
            isManual,
            if (isManual) null else gameInteractor.connection,
            coachPlusController.createBrushingData()
        )

        updateViewState { copy(zoneProgressData = zoneProgressData.brushingFinished()) }
    }

    @VisibleForTesting
    internal fun initAmbientSound() {
        disposables += Observable.combineLatest(settingsRelay, isPlayingStream.toObservable(),
            BiFunction<CoachSettings, Boolean, Pair<CoachSettings, Boolean>> { settings, isPlaying ->
                Pair(settings, isPlaying)
            }).subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .subscribe({ (settings, isPlaying) ->
                if (!isPlaying) {
                    soundInteractor.pauseAmbientSound()
                } else if (settings.enableMusic) {
                    soundInteractor.prepare(settings.getUriOfMusic())
                    soundInteractor.playAmbientSound()
                }
            }, Timber::e)
    }

    @VisibleForTesting
    internal fun sendSupervisionInfo(connection: KLTBConnection) {
        if (shouldSendPlaqlessSupervision()) {
            supervisionDisposable.forceDispose()
            disposables += coachPlusController.zoneChangeObservable
                .flatMapSingle { supervision ->
                    connection.toothbrush()
                        .setSupervisedMouthZone(supervision.zone, supervision.sequenceId)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::w)
                .apply { supervisionDisposable = this }
        }
    }

    @VisibleForTesting
    internal fun listenToRingLedChange(connection: KLTBConnection) {
        ringLedColorUseCaseDisposable.forceDispose()
        disposables += ringLedColorUseCase.getRingLedColor(connection)
            .subscribeOn(Schedulers.io())
            .subscribe(
                this::onRingLedColorChanged,
                Timber::e
            ).apply { ringLedColorUseCaseDisposable = this }
    }

    @VisibleForTesting
    internal fun onRingLedColorChanged(@ColorInt color: Int) {
        updateViewState {
            copy(ringLedColor = color)
        }
    }

    @VisibleForTesting
    internal fun nullifyCurrentZone() {
        updateViewState { copy(currentZone = null) }
    }

    private fun resetBrushingZones() {
        updateViewState { copy(zoneProgressData = guidedBrushingZones()) }
    }

    @Keep
    class Factory @Inject constructor(
        private val macAddress: Optional<String>,
        private val toothbrushModel: ToothbrushModel?,
        private val gameInteractor: GameInteractor,
        private val facade: GameToothbrushInteractorFacade,
        private val lostConnectionHandler: LostConnectionHandler,
        private val soundTransitionInteractor: SoundInteractor,
        private val colorSet: CoachPlusColorSet,
        private val connector: IKolibreeConnector,
        private val coachPlusController: CoachPlusController,
        private val coachSettingsRepository: CoachSettingsRepository,
        private val zoneHintProvider: ZoneHintProvider,
        private val tickPeriod: Duration,
        private val confirmBrushingModeUseCase: ConfirmBrushingModeUseCase,
        private val ringLedColorUseCase: RingLedColorUseCase,
        private val brushingCreator: BrushingCreator,
        private val kmlAvroCreator: KmlAvroCreator,
        private val keepScreenOnController: KeepScreenOnController,
        private val currentZoneColorProvider: CurrentZoneColorProvider,
        private val featuresToggle: FeatureToggleSet,
        private val analytics: CoachPlusAnalytics
    ) : BaseViewModel.Factory<CoachPlusViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CoachPlusViewModel(
                viewState,
                macAddress,
                toothbrushModel,
                gameInteractor,
                facade,
                lostConnectionHandler,
                soundTransitionInteractor,
                colorSet,
                connector,
                coachPlusController,
                coachSettingsRepository,
                zoneHintProvider,
                tickPeriod,
                confirmBrushingModeUseCase,
                ringLedColorUseCase,
                brushingCreator,
                kmlAvroCreator,
                keepScreenOnController,
                currentZoneColorProvider,
                featuresToggle,
                analytics
            ) as T
    }
}

@VisibleForTesting
internal fun initiateViewState(
    initialViewState: CoachPlusViewState?,
    macAddress: Optional<String>
): CoachPlusViewState =
    initialViewState ?: CoachPlusViewState.initial(isManual = macAddress.isPresent.not())
