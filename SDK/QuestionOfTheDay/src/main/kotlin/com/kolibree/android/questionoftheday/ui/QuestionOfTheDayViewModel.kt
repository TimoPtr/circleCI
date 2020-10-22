package com.kolibree.android.questionoftheday.ui

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.hum.questionoftheday.R
import com.kolibree.android.questionoftheday.domain.AlreadyAnsweredException
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.SendAnswerUseCase
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayActions.ShowAlreadyAnsweredError
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayActions.ShowUnknownError
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayViewState.Answer
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

internal class QuestionOfTheDayViewModel(
    initialViewState: QuestionOfTheDayViewState?,
    private val questionOfTheDay: QuestionOfTheDay,
    private val navigator: QuestionOfTheDayNavigator,
    private val sendAnswerUseCase: SendAnswerUseCase
) : BaseViewModel<QuestionOfTheDayViewState, QuestionOfTheDayActions>(
    initialViewState ?: QuestionOfTheDayViewState(questionOfTheDay)
), QuestionOfTheDayInteraction {

    val isLoading = mapNonNull(viewStateLiveData, false) { viewState ->
        viewState.isLoading
    }

    val iconRes = mapNonNull(viewStateLiveData, R.drawable.question_icon_small) { viewState ->
        viewState.iconRes
    }

    val texts = mapNonNull(viewStateLiveData, null) { viewState ->
        viewState as QuestionOfTheDayText
    }

    val buttonVisible = mapNonNull(viewStateLiveData, false) { viewState ->
        viewState.buttonVisible
    }

    val answers = mapNonNull(viewStateLiveData, emptyList()) { viewState ->
        viewState.answers
    }

    val answered = mapNonNull(viewStateLiveData, false) { viewState ->
        viewState.answered
    }

    init {
        QuestionOfTheDayAnalytics.accept()
    }

    override fun onAnswerClick(answer: Answer) {
        val viewState = getViewState() ?: return
        if (viewState.answered) {
            return
        }

        val answerTime = TrustedClock.getNowOffsetDateTime()

        updateViewState { selectAnswer(answer, answerTime) }
        disposeOnDestroy { sendAnswer(answer, answerTime) }
        QuestionOfTheDayAnalytics.submit()
    }

    override fun onButtonClick() {
        val viewState = getViewState() ?: return

        if (viewState.isSuccess && viewState.answerTime != null) {
            navigator.finishWithSuccess(viewState.answerTime)
            QuestionOfTheDayAnalytics.collect()
        } else {
            navigator.finish()
            QuestionOfTheDayAnalytics.ok()
        }
    }

    override fun close() {
        navigator.finish()
        QuestionOfTheDayAnalytics.close()
    }

    private fun sendAnswer(
        answer: Answer,
        answerTime: OffsetDateTime
    ): Disposable {
        return sendAnswerUseCase.sendAnswer(
            questionOfTheDay,
            answer.answer,
            answerTime
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onAnswerSent, ::onAnswerError)
    }

    private fun onAnswerSent() {
        val selectedAnswer = getViewState()?.selectedAnswer() ?: return
        updateViewState { complete(selectedAnswer.isCorrect) }
    }

    private fun onAnswerError(throwable: Throwable) {
        Timber.e(throwable)
        updateViewState { reset() }

        if (throwable is AlreadyAnsweredException) {
            pushAction(ShowAlreadyAnsweredError(throwable))
            navigator.finish()
        } else {
            val error = Error.from(
                R.string.question_of_the_day_error_message,
                Error.ErrorStyle.AutoDismiss
            )
            pushAction(ShowUnknownError(error))
        }
    }

    class Factory @Inject constructor(
        private val questionOfTheDay: QuestionOfTheDay,
        private val navigator: QuestionOfTheDayNavigator,
        private val sendAnswerUseCase: SendAnswerUseCase
    ) : BaseViewModel.Factory<QuestionOfTheDayViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            QuestionOfTheDayViewModel(
                viewState,
                questionOfTheDay,
                navigator,
                sendAnswerUseCase
            ) as T
    }
}
