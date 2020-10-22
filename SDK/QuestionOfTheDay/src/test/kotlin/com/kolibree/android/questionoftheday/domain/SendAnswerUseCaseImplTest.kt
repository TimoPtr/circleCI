/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.threeten.bp.OffsetDateTime

class SendAnswerUseCaseImplTest : BaseUnitTest() {

    private val repository: QuestionOfTheDayRepository = mock()

    private val testScheduler = TestScheduler()
    private val markAsAnsweredUseCase: MarkAsAnsweredUseCase = mock()

    private lateinit var useCase: SendAnswerUseCase

    override fun setup() {
        super.setup()
        useCase = SendAnswerUseCaseImpl(testScheduler, repository, markAsAnsweredUseCase)
    }

    @Test
    fun `when sendAnswer request complete, observer should be completed after a delay`() {
        val question: QuestionOfTheDay = mock()
        val answer: QuestionOfTheDay.Answer = mock()
        val answerTime: OffsetDateTime = mock()

        whenever(repository.sendAnswer(question, answer, answerTime))
            .thenReturn(Completable.complete())
        whenever(markAsAnsweredUseCase.markAsAnswered(question))
            .thenReturn(Completable.complete())

        val observer = useCase.sendAnswer(question, answer, answerTime).test()

        observer.assertNotComplete()

        testScheduler.advanceTimeBy(SendAnswerUseCaseImpl.MIN_REQUEST_TIME, TimeUnit.SECONDS)

        observer.assertComplete()
    }

    @Test
    fun `when sendAnswer request fails, observer should have the error after a delay`() {
        val question: QuestionOfTheDay = mock()
        val answer: QuestionOfTheDay.Answer = mock()
        val answerTime: OffsetDateTime = mock()
        val mockError = IllegalStateException("Mock error")

        whenever(repository.sendAnswer(question, answer, answerTime))
            .thenReturn(Completable.error(mockError))

        val observer = useCase.sendAnswer(question, answer, answerTime).test()

        testScheduler.advanceTimeBy(SendAnswerUseCaseImpl.MIN_REQUEST_TIME, TimeUnit.SECONDS)

        observer.assertError(mockError)
    }

    @Test
    fun `when sendAnswer request complete and it is not delayed, the observer should be empty`() {
        val question: QuestionOfTheDay = mock()
        val answer: QuestionOfTheDay.Answer = mock()
        val answerTime: OffsetDateTime = mock()

        whenever(repository.sendAnswer(question, answer, answerTime))
            .thenReturn(Completable.complete())

        val observer = useCase.sendAnswer(question, answer, answerTime).test()

        observer
            .assertNotComplete()
            .assertNoValues()
    }

    @Test
    fun `when sendAnswer request fails and it is not delayed, the observer should be empty`() {
        val question: QuestionOfTheDay = mock()
        val answer: QuestionOfTheDay.Answer = mock()
        val answerTime: OffsetDateTime = mock()
        val mockError = IllegalStateException("Mock error")

        whenever(repository.sendAnswer(question, answer, answerTime))
            .thenReturn(Completable.error(mockError))

        val observer = useCase.sendAnswer(question, answer, answerTime).test()

        observer
            .assertNotComplete()
            .assertNoValues()
    }

    @Test
    fun `when sendAnswer request fails with unknown error, it should not mark the question as answered`() {
        val question: QuestionOfTheDay = mock()
        val answer: QuestionOfTheDay.Answer = mock()
        val answerTime: OffsetDateTime = mock()
        val error = IllegalStateException("Mock error")

        whenever(repository.sendAnswer(question, answer, answerTime))
            .thenReturn(Completable.error(error))

        useCase.sendAnswer(question, answer, answerTime).test()

        testScheduler.advanceTimeBy(SendAnswerUseCaseImpl.MIN_REQUEST_TIME, TimeUnit.SECONDS)

        verify(markAsAnsweredUseCase, never()).markAsAnswered(question)
    }

    @Test
    fun `when sendAnswer request fails with an api error where code = 456, it should mark the question as answered and dispatch an AlreadyAnsweredException`() {
        val question: QuestionOfTheDay = mock()
        val answer: QuestionOfTheDay.Answer = mock()
        val answerTime: OffsetDateTime = mock()
        val alreadyAnsweredError = mock<ApiError>()
        val errorMessage = "apiErrorMessage"

        whenever(alreadyAnsweredError.internalErrorCode).thenReturn(ApiErrorCode.QUESTION_ALREADY_ANSWERED)
        whenever(alreadyAnsweredError.displayableMessage).thenReturn(errorMessage)

        whenever(repository.sendAnswer(question, answer, answerTime))
            .thenReturn(Completable.error(alreadyAnsweredError))
        whenever(markAsAnsweredUseCase.markAsAnswered(question))
            .thenReturn(Completable.complete())

        val observer = useCase.sendAnswer(question, answer, answerTime).test()

        testScheduler.advanceTimeBy(SendAnswerUseCaseImpl.MIN_REQUEST_TIME, TimeUnit.SECONDS)

        verify(markAsAnsweredUseCase).markAsAnswered(question)
        observer
            .assertError(AlreadyAnsweredException::class.java)
            .assertErrorMessage(errorMessage)
    }
}
