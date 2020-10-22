/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import com.jraska.livedata.test
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.logic.models.Quiz
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramToothbrushesUseCase
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

class QuizViewModelTest : BaseUnitTest() {

    private val brushingQuizAnalyticsHelper = mock<BrushingQuizAnalyticsHelper>()

    private val currentProfileProvider = mock<CurrentProfileProvider>()

    private val brushingProgramToothbrushesUseCase = mock<BrushingProgramToothbrushesUseCase>()

    private lateinit var viewModel: QuizViewModel

    /*
    quizScreens
     */
    @Test
    fun `quizScreens returns screens from Quiz`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)

        viewModel.quizScreens.test().assertValue(Quiz().sortedScreens)
    }

    /*
    currentQuestionIndex
     */
    @Test
    fun `currentQuestionIndex returns index from ViewState`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)

        viewModel.currentQuestionIndex.test().assertValue(viewState.currentQuizPosition)
    }

    @Test
    fun `currentQuestionIndex updates screens if ViewState changes`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)

        val observer = viewModel.currentQuestionIndex.test()

        val expectedPosition = 2
        viewModel.updateViewState {
            viewState.copy(currentQuizPosition = expectedPosition)
        }

        observer.assertValue(expectedPosition)
    }

    /*
    onAnswerSelected
     */
    @Test
    fun `onAnswerSelected updates current QuizScreen with Answer`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)

        val index = 0
        val answer = viewModel.quiz.sortedScreens[index].sortedQuestions.random()

        assertNull(viewModel.quiz.sortedScreens[index].answer)

        viewModel.onAnswerSelected(answer)

        assertEquals(viewModel.quiz.withAnswer(index, answer), viewModel.quiz)
    }

    @Test
    fun `onAnswerSelected updates position in ViewState to last QuizScreen without answer`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)

        assertEquals(0, viewModel.getViewState()!!.currentQuizPosition)

        viewModel.onAnswerSelected(viewModel.quiz.sortedScreens[0].sortedQuestions.random())

        assertEquals(1, viewModel.getViewState()!!.currentQuizPosition)
    }

    @Test
    fun `onAnswerSelected invokes maybeNavigateToConfirmationScreen`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)
        spyViewModel()

        doNothing().whenever(viewModel).maybeNavigateToConfirmationScreen()

        viewModel.onAnswerSelected(viewModel.quiz.sortedScreens[0].sortedQuestions.random())

        verify(viewModel).maybeNavigateToConfirmationScreen()
    }

    @Test
    fun `onAnswerSelected reports to analytics`() {
        val viewState = QuizViewState.initial()
        initViewModel(viewState)
        spyViewModel()

        doNothing().whenever(viewModel).maybeNavigateToConfirmationScreen()

        viewModel.onAnswerSelected(viewModel.quiz.sortedScreens[0].sortedQuestions[0])
        verify(brushingQuizAnalyticsHelper).onQuestionAnswered(0, 0)
    }

    /*
    maybeNavigateToConfirmationScreen
     */
    @Test
    fun `maybeNavigateToConfirmationScreen does nothing if Quiz returns BrushingMode null`() {
        initViewModel()

        viewModel.quiz = mock()
        whenever(viewModel.quiz.selectedBrushingMode()).thenReturn(null)

        val observer = viewModel.actionsObservable.test().assertEmpty()

        viewModel.maybeNavigateToConfirmationScreen()

        observer.assertEmpty()
    }

    @Test
    fun `maybeNavigateToConfirmationScreen invokes isDeviceCompatible with quiz selected BrushingMode`() {
        initViewModel()
        spyViewModel()

        doReturn(Single.just(true)).whenever(viewModel).currentProfileOwnsAtLeastOneCompatibleDevice(any())

        viewModel.actionsObservable.test()

        val expectedBrushingMode = BrushingMode.Strong
        viewModel.quiz = mock()
        whenever(viewModel.quiz.selectedBrushingMode()).thenReturn(expectedBrushingMode)

        viewModel.maybeNavigateToConfirmationScreen()

        verify(viewModel).currentProfileOwnsAtLeastOneCompatibleDevice(expectedBrushingMode)
    }

    @Test
    fun `maybeNavigateToConfirmationScreen invokes navigateToConfirmationScreen with selected mode when compatible`() {
        initViewModel()
        spyViewModel()

        doReturn(Single.just(true)).whenever(viewModel).currentProfileOwnsAtLeastOneCompatibleDevice(any())

        viewModel.actionsObservable.test()

        val expectedBrushingMode = BrushingMode.Strong
        viewModel.quiz = mock()
        whenever(viewModel.quiz.selectedBrushingMode()).thenReturn(expectedBrushingMode)

        viewModel.maybeNavigateToConfirmationScreen()

        verify(viewModel).navigateToConfirmationScreen(expectedBrushingMode)
    }

    @Test
    fun `maybeNavigateToConfirmationScreen invokes navigateToConfirmationScreen with default mode when not compatible`() {
        initViewModel()
        spyViewModel()

        doReturn(Single.just(false)).whenever(viewModel).currentProfileOwnsAtLeastOneCompatibleDevice(any())

        viewModel.actionsObservable.test()

        val expectedBrushingMode = BrushingMode.Strong
        viewModel.quiz = mock()
        whenever(viewModel.quiz.selectedBrushingMode()).thenReturn(expectedBrushingMode)

        viewModel.maybeNavigateToConfirmationScreen()

        assertNotEquals(
            "This test is useless since $expectedBrushingMode is the default one",
            BrushingMode.defaultMode(),
            expectedBrushingMode
        )
        verify(viewModel).navigateToConfirmationScreen(BrushingMode.defaultMode())
    }

    /*
    onBackPressed
     */
    @Test
    fun `onBackPressed returns false if currentPosition is 0`() {
        initViewModel(QuizViewState.initial())

        assertEquals(0, viewModel.currentQuestionIndex.value)

        assertFalse(viewModel.onBackPressed())
    }

    @Test
    fun `onBackPressed returns true if currentPosition is over 0`() {
        initViewModel(QuizViewState.initial().copy(currentQuizPosition = 1))

        assertTrue(viewModel.onBackPressed())
    }

    @Test
    fun `onAnswerSelected clears QuizAnswer from current QuizScreen`() {
        val currentQuizPosition = 1
        val viewState = QuizViewState.initial().copy(currentQuizPosition = currentQuizPosition)
        initViewModel(viewState)

        viewModel.quiz = viewModel.quiz.withAnswer(
            currentQuizPosition,
            viewModel.quiz.sortedScreens[currentQuizPosition].sortedQuestions.random()
        )

        assertNotNull(viewModel.quiz.sortedScreens[currentQuizPosition].answer)

        viewModel.onBackPressed()

        assertNull(viewModel.quiz.sortedScreens[currentQuizPosition].answer)
    }

    @Test
    fun `onBackPressed updates viewState to previousScreen`() {
        val currentQuizPosition = 1
        val viewState = QuizViewState.initial().copy(currentQuizPosition = currentQuizPosition)
        initViewModel(viewState)

        viewModel.quiz = viewModel.quiz.withAnswer(
            currentQuizPosition,
            viewModel.quiz.sortedScreens[currentQuizPosition].sortedQuestions.random()
        )

        assertEquals(viewState, viewModel.getViewState())

        viewModel.onBackPressed()

        assertEquals(
            viewState.withCurrentQuizPosition(screenIndex = 0),
            viewModel.getViewState()
        )
    }

    /*
    navigateToConfirmationScreen
     */

    @Test
    fun `navigateToConfirmationScreen invokes pushAction with selected brushing mode`() {
        initViewModel()
        val expectedMode = BrushingMode.Slow

        val testObserver = viewModel.actionsObservable.test()

        viewModel.navigateToConfirmationScreen(expectedMode)

        testObserver
            .assertNoErrors()
            .assertLastValue(NavigateToConfirmBrushingProgramAction(expectedMode))
    }

    /*
    currentProfileOwnsAtLeastOneCompatibleDevice

    in the following tests we use 'real' mode compatibility (see helper methods at the end)
     */

    @Test
    fun `currentProfileOwnsAtLeastOneCompatibleDevice uses current profile ID`() {
        val expectedProfileId = 1986L
        val currentProfile = ProfileBuilder.create().withId(expectedProfileId).build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(currentProfile))
        whenever(brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(any()))
            .thenReturn(Single.just(emptyList()))

        initViewModel()
        viewModel.currentProfileOwnsAtLeastOneCompatibleDevice(BrushingMode.Slow).test()

        verify(brushingProgramToothbrushesUseCase)
            .toothbrushesWithBrushingProgramSupport(expectedProfileId)
    }

    @Test
    fun `currentProfileOwnsAtLeastOneCompatibleDevice emits false when no device is compatible`() {
        val currentProfile = mock<Profile>()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(currentProfile))

        val connection1 = createB1()
        val connection2 = createB1()
        val connection3 = createB1()

        whenever(brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(any()))
            .thenReturn(Single.just(listOf(connection1, connection2, connection3)))

        initViewModel()
        viewModel
            .currentProfileOwnsAtLeastOneCompatibleDevice(BrushingMode.Strong)
            .test()
            .assertNoErrors()
            .assertValue(false)
    }

    @Test
    fun `currentProfileOwnsAtLeastOneCompatibleDevice emits true when all devices are compatible`() {
        val currentProfile = mock<Profile>()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(currentProfile))

        val connection1 = createB1()
        val connection2 = createB1()
        val connection3 = createB1()

        whenever(brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(any()))
            .thenReturn(Single.just(listOf(connection1, connection2, connection3)))

        initViewModel()
        viewModel
            .currentProfileOwnsAtLeastOneCompatibleDevice(BrushingMode.Slow)
            .test()
            .assertNoErrors()
            .assertValue(true)
    }

    @Test
    fun `currentProfileOwnsAtLeastOneCompatibleDevice emits true when at least one device is compatible`() {
        val currentProfile = mock<Profile>()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(currentProfile))

        val connection1 = createB1()
        val connection2 = createE2()

        whenever(brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(any()))
            .thenReturn(Single.just(listOf(connection1, connection2)))

        initViewModel()
        viewModel
            .currentProfileOwnsAtLeastOneCompatibleDevice(BrushingMode.Strong)
            .test()
            .assertNoErrors()
            .assertValue(true)
    }

    /*
    sendCurrentProgramEvent
     */
    @Test
    fun `sendCurrentProgramEvent send Analytics event`() {
        initViewModel(QuizViewState.initial().copy(currentQuizPosition = 1))

        viewModel.sendCurrentProgramEvent()

        verify(brushingQuizAnalyticsHelper).onGoBackFromProgram(1)
    }

    /*
    UTILS
     */

    private fun initViewModel(viewState: QuizViewState = QuizViewState.initial()) {
        viewModel = QuizViewModel(
            viewState,
            brushingQuizAnalyticsHelper,
            currentProfileProvider,
            brushingProgramToothbrushesUseCase
        )
    }

    private fun spyViewModel() {
        viewModel = spy(viewModel)
    }

    private fun createB1() =
        KLTBConnectionBuilder
            .createAndroidLess()
            .withBrushingMode(availableModes = listOf(BrushingMode.Slow, BrushingMode.Regular))
            .withModel(ToothbrushModel.CONNECT_B1)
            .build()

    private fun createE2() =
        KLTBConnectionBuilder
            .createAndroidLess()
            .withBrushingMode()
            .withModel(ToothbrushModel.CONNECT_E2)
            .build()
}
