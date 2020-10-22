/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.FinishCancelAction
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.FinishSuccessAction
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.ShowErrorToothbrushNotPaired
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.ShowErrorUnknown
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import timber.log.Timber

internal class QuizConfirmationViewModel(
    initialViewState: QuizConfirmationViewState?,
    private val selectedBrushingMode: BrushingMode,
    private val quizConfirmationUseCase: QuizConfirmationUseCase,
    private val analyticsHelper: BrushingQuizAnalyticsHelper,
    @VisibleForTesting val logoProvider: QuizConfirmationLogoProvider
) : BaseViewModel<QuizConfirmationViewState, QuizConfirmationAction>(
    initialViewState ?: QuizConfirmationViewState.initial()
) {

    private val disposables = CompositeDisposable()

    val brushingProgram: LiveData<BrushingMode> = MutableLiveData<BrushingMode>().apply {
        value = selectedBrushingMode
    }

    val isLoading = map(viewStateLiveData) { viewState ->
        viewState?.showProgressBar ?: false
    }

    val logoRes: LiveData<Int> = MutableLiveData<Int>().apply {
        value = logoProvider.provide(selectedBrushingMode)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun onUserClickTryItNow() {
        disposables.addSafely(quizConfirmationUseCase.tryOutBrushingModeCompletable(
            { showProgressBar(show = true) },
            { showProgressBar(show = false) }
        )
            .subscribe(
                { },
                { onTryOutNowError(it) }
            )
        )
        analyticsHelper.onTryButtonClick()
    }

    private fun showProgressBar(show: Boolean) {
        updateViewState { withShowProgressBar(show) }
    }

    @VisibleForTesting
    fun onTryOutNowError(throwable: Throwable) {
        pushAction(
            if (throwable is NoToothbrushWithBrushingProgramException) {
                ShowErrorToothbrushNotPaired
            } else {
                Timber.w(throwable)

                ShowErrorUnknown
            }
        )
    }

    fun onUserClickConfirm() {
        disposables.addSafely(quizConfirmationUseCase.confirmBrushingModeCompletable(
            { showProgressBar(show = true) },
            { showProgressBar(show = false) }
        )
            .subscribe(
                { pushAction(FinishSuccessAction) },
                { pushAction(ShowErrorUnknown) }
            )
        )
        analyticsHelper.onConfirmButtonClick()
    }

    fun onUserClickRevert() {
        disposables.addSafely(quizConfirmationUseCase.maybeRevertBrushingModeCompletable(
            { showProgressBar(show = true) },
            { showProgressBar(show = false) }
        )
            .subscribe(
                { pushAction(FinishCancelAction) },
                { pushAction(ShowErrorUnknown) }
            )
        )
        analyticsHelper.onRevertButtonClick()
    }

    class Factory @Inject constructor(
        private val selectedBrushingMode: BrushingMode,
        private val quizConfirmationUseCase: QuizConfirmationUseCase,
        private val analyticsHelper: BrushingQuizAnalyticsHelper,
        private val logoProvider: Optional<QuizConfirmationLogoProvider>
    ) :
        BaseViewModel.Factory<QuizConfirmationViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            QuizConfirmationViewModel(
                viewState,
                selectedBrushingMode,
                quizConfirmationUseCase,
                analyticsHelper,
                logoProvider.or(DefaultQuizConfirmationLogoProvider)
            ) as T
    }
}

@BindingAdapter(value = ["brushingProgram"])
internal fun TextView.setBrushingProgram(brushingMode: BrushingMode) {
    val brushingProgram = resources.getString(
        when (brushingMode) {
            BrushingMode.Regular -> R.string.quiz_program_everyday_care
            BrushingMode.Slow -> R.string.quiz_program_sensitive
            BrushingMode.Strong -> R.string.quiz_program_whitening
            // TODO to be defined for CC 3.0
            BrushingMode.Polishing -> R.string.quiz_program_whitening
            BrushingMode.UserDefined -> R.string.quiz_program_whitening
        }
    )
    text = brushingProgram
}
