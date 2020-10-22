/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.question

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.AlreadyAnswered
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.NotAvailable
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayUseCase
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.verify

class QuestionCardViewModelTest : BaseUnitTest() {

    private val questionOfTheDayUseCase: QuestionOfTheDayUseCase = mock()
    private val humHomeNavigator: HumHomeNavigator = mock()

    private lateinit var viewModel: QuestionCardViewModel

    override fun setup() {
        super.setup()

        viewModel = QuestionCardViewModel(
            QuestionCardViewState.initial(DynamicCardPosition.ZERO),
            questionOfTheDayUseCase,
            humHomeNavigator
        )
    }

    @Test
    fun `card is hidden by default`() {
        val viewState = viewModel.getViewState()!!
        assertFalse(viewState.visible)
    }

    @Test
    fun `show card when question has not been answered`() {
        val mockQuestion = QuestionOfTheDay(
            id = 1,
            question = "This is mock question",
            points = 12,
            answers = listOf(QuestionOfTheDay.Answer(1, "Answer", correct = true))
        )
        val status = Available(mockQuestion)

        whenever(questionOfTheDayUseCase.questionStatusStream())
            .thenReturn(Flowable.just(status))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val viewState = viewModel.getViewState()!!
        assertEquals(mockQuestion, viewState.questionOfTheDay)
    }

    @Test
    fun `show card when question has been answered`() {
        whenever(questionOfTheDayUseCase.questionStatusStream())
            .thenReturn(Flowable.just(AlreadyAnswered))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val viewState = viewModel.getViewState()!!
        assertTrue(viewState.visible)
    }

    @Test
    fun `hide card when question not available`() {
        whenever(questionOfTheDayUseCase.questionStatusStream())
            .thenReturn(Flowable.just(NotAvailable))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val viewState = viewModel.getViewState()!!
        assertFalse(viewState.visible)
    }

    @Test
    fun `show question screen when question has not been answered`() {
        val mockQuestion = QuestionOfTheDay(
            id = 1,
            question = "This is mock question",
            points = 12,
            answers = listOf(QuestionOfTheDay.Answer(1, "Answer", correct = true))
        )
        val status = Available(mockQuestion)

        whenever(questionOfTheDayUseCase.questionStatusStream()).thenReturn(Flowable.just(status))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onClick()

        verify(humHomeNavigator).showQuestionOfTheDay(mockQuestion)
    }

    @Test
    fun `do not show question screen when question has been answered`() {
        whenever(questionOfTheDayUseCase.questionStatusStream())
            .thenReturn(Flowable.just(AlreadyAnswered))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onClick()

        verifyNoMoreInteractions(humHomeNavigator)
    }
}
