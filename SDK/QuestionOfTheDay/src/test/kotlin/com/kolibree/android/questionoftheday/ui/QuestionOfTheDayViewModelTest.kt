/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.ui

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.questionoftheday.domain.AlreadyAnsweredException
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.SendAnswerUseCase
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class QuestionOfTheDayViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: QuestionOfTheDayViewModel

    private val navigator: QuestionOfTheDayNavigator = mock()
    private val sendAnswerUseCase: SendAnswerUseCase = mock()

    private val mockAnswers = listOf(
        QuestionOfTheDay.Answer(1, "Answer 1", correct = false),
        QuestionOfTheDay.Answer(2, "Answer 2", correct = true),
        QuestionOfTheDay.Answer(3, "Answer 3", correct = false)
    )

    private val mockQuestion = QuestionOfTheDay(
        id = 1,
        question = "Mock question",
        points = 10,
        answers = mockAnswers
    )

    private val viewState: QuestionOfTheDayViewState
        get() = viewModel.getViewState()!!

    override fun setup() {
        super.setup()

        whenever(
            sendAnswerUseCase.sendAnswer(
                any(),
                any(),
                any()
            )
        ).thenReturn(Completable.complete())
    }

    @Test
    fun `marks answer as selected`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        assertFalse(viewState.answered)

        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers[0])
        viewModel.onAnswerClick(selectedAnswer)

        assertTrue(viewState.answered)
        assertTrue(viewState.answers[0].selected)
    }

    @Test
    fun `allows to select only one answer`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        var selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers[0])
        viewModel.onAnswerClick(selectedAnswer)

        assertTrue(viewState.answers[0].selected)

        selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers[1])
        viewModel.onAnswerClick(selectedAnswer)

        assertTrue(viewState.answers[0].selected)
        assertFalse(viewState.answers[1].selected)
    }

    @Test
    fun `saves answer time`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertNull(viewState.answerTime)

        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers[0])
        viewModel.onAnswerClick(selectedAnswer)

        assertNotNull(viewState.answerTime)
    }

    @Test
    fun `finishes with success if correct answer`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers.first { it.correct })
        viewModel.onAnswerClick(selectedAnswer)
        viewModel.onButtonClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("DailyQuestion_Collect"))
        verify(navigator).finishWithSuccess(viewState.answerTime!!)
    }

    @Test
    fun `finishes without success if incorrect answer`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers.first { !it.correct })
        viewModel.onAnswerClick(selectedAnswer)
        viewModel.onButtonClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("DailyQuestion_Ok"))
        verify(navigator).finish()
    }

    @Test
    fun `sends answer after selection`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers.first())
        viewModel.onAnswerClick(selectedAnswer)

        verify(sendAnswerUseCase).sendAnswer(
            eq(mockQuestion),
            eq(selectedAnswer.answer),
            any()
        )
    }

    @Test
    fun `shows error if unable to send answer`() {
        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers.first())

        whenever(
            sendAnswerUseCase.sendAnswer(
                eq(mockQuestion),
                eq(selectedAnswer.answer),
                any()
            )
        ).thenReturn(Completable.error(IllegalStateException()))

        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val actions = viewModel.actionsObservable.test()
        viewModel.onAnswerClick(selectedAnswer)

        actions.assertValue { it is QuestionOfTheDayActions.ShowUnknownError }
    }

    @Test
    fun `already answered error show an error message and finish the screen`() {
        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers.first())

        whenever(
            sendAnswerUseCase.sendAnswer(
                eq(mockQuestion),
                eq(selectedAnswer.answer),
                any()
            )
        ).thenReturn(Completable.error(AlreadyAnsweredException("")))

        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val actions = viewModel.actionsObservable.test()
        viewModel.onAnswerClick(selectedAnswer)

        actions.assertValue { it is QuestionOfTheDayActions.ShowAlreadyAnsweredError }
        verify(navigator).finish()
    }

    @Test
    fun `resets question if unable to send answer`() {
        val selectedAnswer = QuestionOfTheDayViewState.Answer(mockAnswers.first())

        whenever(
            sendAnswerUseCase.sendAnswer(
                eq(mockQuestion),
                eq(selectedAnswer.answer),
                any()
            )
        ).thenReturn(Completable.error(IllegalStateException()))

        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        viewModel.actionsObservable.test()
        viewModel.onAnswerClick(selectedAnswer)

        assertInitialState()
    }

    @Test
    fun `shows question and answers`() {
        viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertInitialState()
    }

    private fun assertInitialState() {
        assertFalse(viewState.answered)
        assertFalse(viewState.isLoading)
        assertFalse(viewState.isSuccess)
        assertFalse(viewState.bodyVisible)
        assertFalse(viewState.buttonVisible)
        assertFalse(viewState.answers.any { it.confirmed || it.selected })
        assertEquals(viewState.answers.map { it.answer }, mockAnswers)
        assertNull(viewState.answerTime)
    }

    private fun createViewModel(): QuestionOfTheDayViewModel {
        return QuestionOfTheDayViewModel(
            null,
            mockQuestion,
            navigator,
            sendAnswerUseCase
        )
    }
}
