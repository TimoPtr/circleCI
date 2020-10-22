/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz.question

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.brushingquiz.BR
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.logic.models.QuizAnswer
import com.kolibree.android.brushingquiz.logic.models.QuizScreen
import com.kolibree.android.brushingquiz.presentation.quiz.QuizAnswerClickListener
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding

internal class QuizScreenViewModel(
    initialViewState: QuizScreenViewState,
    private val quizScreen: QuizScreen,
    private val totalScreens: Int,
    private val currentScreenIndex: Int,
    private val quizAnswerListener: QuizAnswerClickListener
) : BaseViewModel<QuizScreenViewState, QuizScreenAction>(initialViewState),
    QuizAnswerClickListener {

    override fun onAnswerSelected(answer: QuizAnswer) {
        quizAnswerListener.onAnswerSelected(answer)

        updateViewState { copy(selectedAnswer = answer) }
    }

    val quizAnswersBinding =
        ItemBinding.of<QuizAnswer>(BR.answer, R.layout.quiz_answer)
            .bindExtra(BR.itemClickListener, this)

    val totalScreensLiveData: LiveData<Int> = MutableLiveData<Int>().apply {
        value = totalScreens
    }

    val currentScreenIndexLiveData: LiveData<Int> = MutableLiveData<Int>().apply {
        value = currentScreenIndex
    }

    val quizScreenLiveData: LiveData<QuizScreen> = MutableLiveData<QuizScreen>().apply {
        value = quizScreen
    }

    val answers = map(viewStateLiveData) { viewState ->
        val quiz =
            viewState?.selectedAnswer?.let { selectedAnswer -> quizScreen.withAnswer(selectedAnswer) }
                ?: quizScreen

        quiz.sortedQuestions
    }

    internal class Factory @Inject constructor(
        private val quizScreen: QuizScreen,
        private val quizAnswerListener: QuizAnswerClickListener,
        @CurrentScreenIndex private val quizScreenIndex: Int,
        @TotalScreens private val totalScreens: Int
    ) : BaseViewModel.Factory<QuizScreenViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            QuizScreenViewModel(
                initialViewState = viewState ?: QuizScreenViewState(),
                quizScreen = quizScreen,
                quizAnswerListener = quizAnswerListener,
                totalScreens = totalScreens,
                currentScreenIndex = quizScreenIndex
            ) as T
    }
}

private const val QUESTION_ORDER_TEXT_PROPORTION = 0.6f

@Parcelize
internal data class QuizScreenViewState(val selectedAnswer: QuizAnswer? = null) : BaseViewState

/**
 * Quiz screen has no actions
 */
internal object QuizScreenAction : BaseAction

@BindingAdapter("totalScreens", "screenIndex", requireAll = true)
internal fun TextView.setQuizStep(totalScreens: Int, screenIndex: Int) {
    text = context.getString(
        R.string.brushing_quiz_steps_counter,
        (screenIndex + 1).toString(),
        totalScreens.toString()
    )
}
