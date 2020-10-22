/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.logic.models.Quiz
import com.kolibree.android.brushingquiz.logic.models.QuizAnswer
import com.kolibree.android.brushingquiz.logic.models.QuizScreen
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramToothbrushesUseCase
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class QuizViewModel(
    initialViewState: QuizViewState?,
    private val analyticsHelper: BrushingQuizAnalyticsHelper,
    private val currentProfileProvider: CurrentProfileProvider,
    private val brushingProgramToothbrushes: BrushingProgramToothbrushesUseCase
) : BaseViewModel<QuizViewState, QuizActions>(
        initialViewState ?: QuizViewState.initial()
    ), QuizAnswerClickListener {

    @VisibleForTesting
    var quiz = Quiz()

    val quizScreens: LiveData<List<QuizScreen>> = MutableLiveData<List<QuizScreen>>().apply {
        value = quiz.sortedScreens
    }

    val currentQuestionIndex: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.currentQuizPosition ?: 0
    }

    override fun onAnswerSelected(answer: QuizAnswer) {
        getViewState()?.let {
            val screenIndex = it.currentQuizPosition
            quiz = quiz.withAnswer(screenIndex, answer)
            analyticsHelper
                .onQuestionAnswered(screenIndex, quiz.getAnswerIndex(screenIndex, answer))
        }

        updateViewStateFromQuiz()

        maybeNavigateToConfirmationScreen()
    }

    /*
    This method will ask if at least one device is compatible with the selected brushing mode.
    - If no one is compatible, we fall back to Regular which is the default one.
    - If at least one device is compatible, we go for the selected mode, non compatible devices will
      fall back to the Regular mode as specified in:
      https://kolibree.atlassian.net/wiki/spaces/PROD/pages/31686662/Brushing+program+vibration+speed)
      Please note that this fallback behaviour is directly managed by the device firmware itself.
     */
    @VisibleForTesting
    fun maybeNavigateToConfirmationScreen() {
        quiz.selectedBrushingMode()?.let { selectedBrushingMode ->
            disposeOnCleared {
                currentProfileOwnsAtLeastOneCompatibleDevice(selectedBrushingMode)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { isCompatible ->
                            if (isCompatible) {
                                navigateToConfirmationScreen(selectedBrushingMode)
                            } else {
                                navigateToConfirmationScreen(BrushingMode.defaultMode())
                            }
                        },
                        Timber::e
                    )
            }
        }
    }

    @VisibleForTesting
    fun currentProfileOwnsAtLeastOneCompatibleDevice(brushingMode: BrushingMode) =
        currentProfileProvider
            .currentProfileSingle()
            .flatMap { brushingProgramToothbrushes.toothbrushesWithBrushingProgramSupport(it.id) }
            .flattenAsFlowable { it }
            .flatMap { connection ->
                connection.brushingMode().availableBrushingModes().flattenAsFlowable { it }
            }
            .contains(brushingMode)

    @VisibleForTesting
    fun navigateToConfirmationScreen(selectedBrushingMode: BrushingMode) =
        pushAction(
            NavigateToConfirmBrushingProgramAction(
                selectedBrushingMode = selectedBrushingMode
            )
        )

    /**
     * Navigates to previous Quiz screen
     *
     * @return true if we are displaying the first QuizScreen. False otherwise
     */
    fun onBackPressed(): Boolean {
        sendCurrentProgramEvent()

        if (getViewState()?.currentQuizPosition == 0) {
            return false
        }

        quiz = quiz.withLastAnswerCleared()

        updateViewStateFromQuiz()

        return true
    }

    fun sendCurrentProgramEvent() {
        getViewState()?.currentQuizPosition?.let { programIndex ->
            analyticsHelper.onGoBackFromProgram(programIndex)
        }
    }

    private fun updateViewStateFromQuiz() =
        updateViewState {
            withCurrentQuizPosition(quiz.nextScreenIndex)
        }

    internal class Factory @Inject constructor(
        private val analyticsHelper: BrushingQuizAnalyticsHelper,
        private val currentProfileProvider: CurrentProfileProvider,
        private val brushingProgramToothbrushes: BrushingProgramToothbrushesUseCase
    ) : BaseViewModel.Factory<QuizViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            QuizViewModel(
                viewState,
                analyticsHelper,
                currentProfileProvider,
                brushingProgramToothbrushes
            ) as T
    }
}

internal interface QuizAnswerClickListener {
    fun onAnswerSelected(answer: QuizAnswer)
}
