/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.ShowErrorToothbrushNotPaired
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.ShowErrorUnknown
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationUseCaseTest.Companion.mockToAvoidCrash
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class QuizConfirmationViewModelTest : BaseUnitTest() {

    private val brushingQuizAnalyticsHelper = mock<BrushingQuizAnalyticsHelper>()

    private val logoProvider = mock<QuizConfirmationLogoProvider>()

    private lateinit var viewModel: QuizConfirmationViewModel

    @Test
    fun `show loading if state asks for it`() {
        initViewModel(brushingMode = BrushingMode.Regular)
        val showLoading = viewModel.isLoading.test()

        viewModel.updateViewState { copy(showProgressBar = true) }
        viewModel.updateViewState { copy(showProgressBar = false) }

        showLoading.assertValueHistory(false, true, false)
    }

    @Test
    fun `display logo associated with brushing mode`() {
        BrushingMode.values().forEach { brushingMode ->
            initViewModel(brushingMode)
            viewModel.logoRes.test()
            verify(logoProvider).provide(brushingMode)
        }
    }

    /*
    brushingProgram
     */

    @Test
    fun `brushingProgram LiveData emits brushingProgram parameter`() {
        val expectedBrushingMode = BrushingMode.Regular
        initViewModel(brushingMode = expectedBrushingMode)

        assertEquals(expectedBrushingMode, viewModel.brushingProgram.value)
    }

    /*
    onUserClickRevert
     */

    @Test
    fun `userClickRevert calls maybeRevertOldBrushingModeCompletable then emits FinishCancelAction action`() {
        val subject = CompletableSubject.create()
        val confirmationUseCase = mockToAvoidCrash(confirmCompletable = subject)
        confirmationUseCase.oldBrushingMode.set(BrushingMode.Strong)
        initViewModel(quizConfirmationUseCase = confirmationUseCase)

        val actionObservable = viewModel.actionsObservable.test()
            .assertEmpty()

        viewModel.onUserClickRevert()

        assertTrue(subject.hasObservers())

        actionObservable.assertEmpty()

        subject.onComplete()

        actionObservable.assertValue(QuizConfirmationAction.FinishCancelAction)
    }

    @Test
    fun `userClickRevert reports to analytics`() {
        val subject = CompletableSubject.create()
        val confirmationUseCase = mockToAvoidCrash(confirmCompletable = subject)
        confirmationUseCase.oldBrushingMode.set(BrushingMode.Strong)
        initViewModel(quizConfirmationUseCase = confirmationUseCase)

        viewModel.onUserClickRevert()
        verify(brushingQuizAnalyticsHelper).onRevertButtonClick()
    }

    /*
    onUserClickConfirm
     */

    @Test
    fun `userClickConfirm emits FinishSuccess action on success`() {
        initViewModel()

        val actionObservable = viewModel.actionsObservable.test()
            .assertEmpty()

        viewModel.onUserClickConfirm()

        actionObservable.assertValue(QuizConfirmationAction.FinishSuccessAction)
    }

    @Test
    fun `userClickConfirm emits ShowErrorUnknown action on failure`() {
        initViewModel(
            quizConfirmationUseCase = mockToAvoidCrash(
                Completable.error(
                    TestForcedException()
                )
            )
        )

        val actionObservable = viewModel.actionsObservable.test()
            .assertEmpty()

        viewModel.onUserClickConfirm()

        actionObservable.assertValue(ShowErrorUnknown)
    }

    @Test
    fun `userClickConfirm reports to analytics`() {
        initViewModel(
            quizConfirmationUseCase = mockToAvoidCrash(
                Completable.error(
                    TestForcedException()
                )
            )
        )

        viewModel.actionsObservable.test().assertEmpty()

        viewModel.onUserClickConfirm()
        verify(brushingQuizAnalyticsHelper).onConfirmButtonClick()
    }

    /*
    onTryOutNowError
     */

    @Test
    fun `onTryOutNowError emits ShowErrorToothbrushNotPaired if throwable is NoToothbrushWithBrushingProgramException`() {
        initViewModel()

        val actionObservable = viewModel.actionsObservable.test()
            .assertEmpty()

        viewModel.onTryOutNowError(NoToothbrushWithBrushingProgramException)

        actionObservable.assertValue(ShowErrorToothbrushNotPaired)
    }

    @Test
    fun `onTryOutNowError emits ShowErrorUnknown if throwable is not NoToothbrushWithBrushingProgramException`() {
        initViewModel()

        val actionObservable = viewModel.actionsObservable.test()
            .assertEmpty()

        viewModel.onTryOutNowError(TestForcedException())

        actionObservable.assertValue(ShowErrorUnknown)
    }

    /*
    onUserClickTryItNow

    Since tryOutNow is an inline class, the invocation is replaced by code by the compiler, thus
    we can't mock that
     */

    @Test
    fun `userClickTryItNow doOnSubscribeBlock emits ViewState showProgressBar true followed by false`() {
        spyViewModel()

        val viewState = QuizConfirmationViewState.initial()

        // We don't care about the error we only want to assert doOnSubscribe
        doNothing().whenever(viewModel).onTryOutNowError(any())

        viewModel.onUserClickTryItNow()

        argumentCaptor<QuizConfirmationViewState.() -> QuizConfirmationViewState> {
            verify(viewModel, times(2)).updateViewState(capture())

            val firstExpectedViewState = viewState.withShowProgressBar(true)
            assertEquals(firstExpectedViewState, firstValue.invoke(viewState))
            assertEquals(
                viewState.withShowProgressBar(false),
                secondValue.invoke(firstExpectedViewState)
            )
        }
    }

    @Test
    fun `userClickTryItNow() reports to analytics`() {
        spyViewModel()

        // We don't care about the error we only want to assert onTryButtonClick
        doNothing().whenever(viewModel).onTryOutNowError(any())

        viewModel.onUserClickTryItNow()
        verify(brushingQuizAnalyticsHelper).onTryButtonClick()
    }

    /*
    UTILS
     */

    private lateinit var quizConfirmationUseCase: QuizConfirmationUseCase

    private fun initViewModel(
        brushingMode: BrushingMode = defaultBrushingMode,
        quizConfirmationUseCase: QuizConfirmationUseCase = mockToAvoidCrash()
    ) {
        this.quizConfirmationUseCase = quizConfirmationUseCase

        viewModel =
            QuizConfirmationViewModel(
                initialViewState = null,
                selectedBrushingMode = brushingMode,
                quizConfirmationUseCase = quizConfirmationUseCase,
                analyticsHelper = brushingQuizAnalyticsHelper,
                logoProvider = logoProvider
            )
    }

    private fun spyViewModel(
        brushingMode: BrushingMode = defaultBrushingMode,
        quizConfirmationUseCase: QuizConfirmationUseCase = mockToAvoidCrash()
    ) {
        initViewModel(brushingMode, quizConfirmationUseCase)

        viewModel = spy(viewModel)
    }
}
