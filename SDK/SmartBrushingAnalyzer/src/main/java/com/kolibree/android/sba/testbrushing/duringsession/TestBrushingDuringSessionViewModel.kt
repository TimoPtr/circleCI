package com.kolibree.android.sba.testbrushing.duringsession

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.mouthmap.widget.timer.MouthMapTimer
import com.kolibree.android.processedbrushings.exception.ProcessedBrushingNotAvailableException
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.HideFinishBrushingDialog
import com.kolibree.android.sba.testbrushing.base.LostConnectionStateChanged
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.ShowFinishBrushingDialog
import com.kolibree.android.sba.testbrushing.base.UpdateTimer
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyTestBrushingVibratorViewModel
import com.kolibree.android.sba.testbrushing.brushing.TestBrushingResultsProvider
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreator
import com.kolibree.android.sba.testbrushing.duringsession.timer.CarouselTimer
import com.kolibree.android.sba.testbrushing.ui.TestBrushingResourceProvider
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class TestBrushingDuringSessionViewModel(
    override val serviceProvider: ServiceProvider,
    override val toothbrushMac: String,
    private val navigator: TestBrushingNavigator,
    private val toothbrushModel: ToothbrushModel,
    private val carouselTimer: CarouselTimer,
    private val brushingCreator: TestBrushingCreator,
    private val brushingResultsProvider: TestBrushingResultsProvider,
    private val mouthMapTimer: MouthMapTimer,
    private val lostConnectionHandler: LostConnectionHandler
) : LegacyTestBrushingVibratorViewModel<TestBrushingDuringSessionViewState>(
    toothbrushMac,
    serviceProvider,
    TestBrushingDuringSessionViewState()
) {

    private val resourceProvider = TestBrushingResourceProvider()

    @VisibleForTesting
    var currentStep = SessionStep.START_STEP

    override fun onCleared() {
        super.onCleared()

        maybeDisableDetectionNotificationsAndStopVibration()
    }

    private fun maybeDisableDetectionNotificationsAndStopVibration() {
        currentConnection?.apply {
            detectors().disableDetectionNotifications()

            if (vibrator().isOn)
                vibrator().off().subscribeOn(Schedulers.io()).onTerminateDetach().blockingAwait()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposables.addSafely(carouselTimer.observable().subscribe(this::nextItem, Timber::e))
        disposables.addSafely(mouthMapTimer.observable().subscribe(this::updateTimer, Timber::e))
        disposables.addSafely(
            lostConnectionHandler.connectionObservable(toothbrushMac)
                .subscribe(this::connectionChanged, Timber::e)
        )
    }

    @VisibleForTesting
    fun connectionChanged(state: State) {
        when (state) {
            State.CONNECTION_LOST, State.CONNECTING -> pauseTimers()
            State.CONNECTION_ACTIVE -> {
                resumeTimers()
                brushingCreator.notifyReconnection()
            }
        }
        emitState(viewState.copy(action = LostConnectionStateChanged(state)))
    }

    fun pauseTimers() {
        carouselTimer.pause()
        mouthMapTimer.pause()
    }

    fun resumeTimers() {
        carouselTimer.resume()
        mouthMapTimer.resume()
    }

    override fun initViewState() = currentViewState(false).copy(withTimerVisible = toothbrushModel.isPlaqless)

    override fun onVibratorStateChanged(isVibratorOn: Boolean) {
        if (isVibratorOn) {
            emitState(viewState.copy(action = HideFinishBrushingDialog))
            resumeTimers()
            currentConnection?.let {
                brushingCreator.resume(it)
            }
        } else {
            emitState(viewState.copy(action = ShowFinishBrushingDialog))
            pauseTimers()
            currentConnection?.let {
                brushingCreator.pause(it)
            }
        }
    }

    fun nextItem(ignore: Int) {
        currentStep = currentStep.next()
        emitState(currentViewState())
    }

    fun updateTimer(value: Long) {
        emitState(viewState.copy(action = UpdateTimer(value)))
    }

    private fun currentViewState(animation: Boolean = true) = viewState.copy(
        descriptionId = resourceProvider.provideDuringSessionDescription(toothbrushModel, currentStep),
        highlightedId = resourceProvider.provideDuringSessionHighlighted(toothbrushModel, currentStep),
        indicatorStep = currentStep.ordinal,
        withIndicatorAnimation = animation,
        backgroundColorRes = resourceProvider.provideBackgroundColor(toothbrushModel, currentStep),
        animationId = resourceProvider.provideDuringSessionVideo(toothbrushModel, currentStep)
    )

    override fun onResume(owner: LifecycleOwner) {
        resumeTimers()
    }

    override fun onPause(owner: LifecycleOwner) {
        pauseTimers()
    }

    fun userFinishedBrushing() {
        try {
            currentConnection?.let {
                val rawData = brushingCreator.create(it)
                brushingResultsProvider.init(rawData)
            }
            navigator.navigateToOptimizeAnalysisScreen()
        } catch (e: ProcessedBrushingNotAvailableException) {
            Timber.e(e)
            navigator.finishScreen()
        }
    }

    fun userResumedBrushing() {
        if (!toothbrushModel.isManual) {
            currentConnection?.let {
                disposables.addSafely(
                    it.vibrator()
                        .on()
                        .subscribe(this::turnVibratorOnSuccess, this::handleException)
                )
            }
        }
    }

    @VisibleForTesting
    fun turnVibratorOnSuccess() {
        resumeTimers()
        currentConnection?.let {
            brushingCreator.resume(it)
        }
    }

    override fun resetActionViewState() = viewState.copy(action = NoneAction)

    fun onUserDismissedLostConnectionDialog() = navigator.finishScreen()

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val serviceProvider: ServiceProvider,
        private val navigator: TestBrushingNavigator,
        private val mac: String,
        private val model: ToothbrushModel,
        private val carouselTimer: CarouselTimer,
        private val brushingCreator: TestBrushingCreator,
        private val brushingResultsProvider: TestBrushingResultsProvider,
        private val mouthMapTimer: MouthMapTimer,
        private val lostConnectionHandler1: LostConnectionHandler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TestBrushingDuringSessionViewModel(
                serviceProvider,
                mac,
                navigator,
                model,
                carouselTimer,
                brushingCreator,
                brushingResultsProvider,
                mouthMapTimer,
                lostConnectionHandler1
            ) as T
        }
    }
}
