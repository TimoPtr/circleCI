/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.checkup.base.BaseCheckupViewModel
import com.kolibree.android.app.ui.checkup.base.CheckupActions
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import timber.log.Timber

/** Checkup [BaseCheckupViewModel] implementation */
@VisibleForApp
class CheckupResultsViewModel(
    initialViewState: CheckupResultsViewState,
    private val currentProfileProvider: CurrentProfileProvider,
    private val brushingFacade: BrushingFacade,
    private val checkupCalculator: CheckupCalculator,
    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase
) : BaseCheckupViewModel<CheckupResultsViewState>(
    initialViewState = initialViewState,
    brushingFacade = brushingFacade
) {

    @VisibleForTesting
    val currentBrushing = AtomicReference<IBrushing>(null)

    val checkupDataLiveData: LiveData<Map<MouthZone16, Float>> =
        map(viewStateLiveData) { viewState ->
            viewState?.checkupData ?: mapOf()
        }

    val isManualBrushingLiveData: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isManualBrushing ?: false
    }

    val title: LiveData<Int> = map(viewStateLiveData) { viewState ->
        when (viewState?.checkupOrigin) {
            CheckupOrigin.HOME -> R.string.checkup_your_results
            CheckupOrigin.TEST_BRUSHING -> R.string.checkup_see_how_you_did
            CheckupOrigin.GUIDED_BRUSHING -> R.string.checkup_your_results
            null -> R.string.checkup_your_results
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop {
            currentProfileLastBrushingSessionSingle()
                .subscribeOn(Schedulers.io())
                .subscribe(this::onLastBrushingSession, Timber::e)
        }
    }

    override fun onBrushingDeleted(brushing: IBrushing) {
        CheckupResultsAnalytics.delete(getViewState()?.checkupOrigin)

        pushAction(CheckupActions.FinishOk)
    }

    override fun currentBrushingSession(): IBrushing = currentBrushing.get()

    override fun onBackButtonClick() {
        CheckupResultsAnalytics.close(getViewState()?.checkupOrigin)
        super.onBackButtonClick()
    }

    fun onFinishClick() {
        if (getViewState()?.isManualBrushing == false) {
            CheckupResultsAnalytics.collect(getViewState()?.checkupOrigin)
        }
        userExpectsSmilesUseCase.onUserExpectsPoints(currentBrushing.get().dateTime.toInstant())

        pushAction(CheckupActions.FinishOk)
    }

    @VisibleForTesting
    fun onLastBrushingSession(brushingSession: IBrushing) {
        currentBrushing.set(brushingSession)
        brushingSession
            .let { checkupCalculator.calculateCheckup(it) }
            .let { checkupData ->
                updateViewState {
                    copy(
                        isManualBrushing = checkupData.isManual,
                        coverage = checkupData.coverage,
                        durationPercentage = checkupData.duration.seconds / brushingSession.goalDuration.toFloat(),
                        durationSeconds = checkupData.duration.seconds,
                        date = brushingSession.dateTime,
                        game = brushingSession.game,
                        checkupData = checkupData.zoneSurfaceMap
                    )
                }
            }
    }

    @VisibleForTesting
    fun currentProfileLastBrushingSessionSingle(): Single<IBrushing> =
        currentProfileProvider.currentProfileSingle()
            .flatMap { currentProfile ->
                brushingFacade.getLastBrushingSession(currentProfile.id)
            }

    @VisibleForApp
    class Factory @Inject constructor(
        private val currentProfileProvider: CurrentProfileProvider,
        private val brushingFacade: BrushingFacade,
        private val checkupCalculator: CheckupCalculator,
        private val checkupOrigin: CheckupOrigin,
        private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase
    ) : BaseViewModel.Factory<CheckupResultsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CheckupResultsViewModel(
                viewState
                    ?: CheckupResultsViewState.initial(
                        checkupOrigin
                    ),
                currentProfileProvider,
                brushingFacade,
                checkupCalculator,
                userExpectsSmilesUseCase
            ) as T
    }
}
