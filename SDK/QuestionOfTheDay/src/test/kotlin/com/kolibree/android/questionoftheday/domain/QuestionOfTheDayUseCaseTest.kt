/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Expired
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test

class QuestionOfTheDayUseCaseTest : BaseUnitTest() {

    private val accountDatastore: AccountDatastore = mock()
    private val profileProvider: CurrentProfileProvider = mock()
    private val repository: QuestionOfTheDayRepository = mock()

    private lateinit var questionOfTheDayUseCase: QuestionOfTheDayUseCase

    private val testProfileId = 1L
    private val testProfile = ProfileBuilder.create()
        .withId(testProfileId)
        .build()

    private val testProfileInternal = mockProfileInternal(testProfileId)

    private val testAccount = mock<AccountInternal>().apply {
        whenever(internalProfiles).thenReturn(listOf(testProfileInternal))
    }

    override fun setup() {
        super.setup()

        questionOfTheDayUseCase =
            QuestionOfTheDayUseCaseImpl(accountDatastore, profileProvider, repository)
        whenever(profileProvider.currentProfileFlowable()).thenReturn(Flowable.just(testProfile))
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(testAccount))
    }

    @Test
    fun `questionStatusStream fetch the repository if the status is Expired and deliver the repository response`() {
        val expectedStatus: QuestionOfTheDayStatus = mock()
        val questionOfTheDay: Flowable<QuestionOfTheDayStatus> = Flowable.just(expectedStatus)

        whenever(repository.fetchQuestion(testProfileId)).thenReturn(Completable.complete())
        whenever(repository.getQuestionStatus(testProfileId)).thenReturn(Single.just(Expired))
        whenever(repository.getQuestionFlowable(testProfileId)).thenReturn(questionOfTheDay)

        val testFlowable = questionOfTheDayUseCase.questionStatusStream().test()

        verify(repository).fetchQuestion(testProfileId)

        testFlowable
            .assertValueCount(1)
            .assertValue(expectedStatus)
    }

    @Test
    fun `questionStatusStream deliver the repository response`() {
        val expectedStatus: QuestionOfTheDayStatus = Available(mock())
        val questionOfTheDay: Flowable<QuestionOfTheDayStatus> = Flowable.just(expectedStatus)

        whenever(repository.getQuestionStatus(testProfileId)).thenReturn(Single.just(expectedStatus))
        whenever(repository.getQuestionFlowable(testProfileId)).thenReturn(questionOfTheDay)

        val testFlowable = questionOfTheDayUseCase.questionStatusStream().test()

        testFlowable
            .assertValueCount(1)
            .assertValue(expectedStatus)
    }

    @Test
    fun `if questions are expired refreshQuestions call the repository to update new one`() {
        whenever(repository.fetchQuestion(testProfileId)).thenReturn(Completable.complete())
        whenever(repository.getQuestionStatus(testProfileId)).thenReturn(Single.just(Expired))

        val testCompletable = questionOfTheDayUseCase.refreshQuestions().test()

        verify(repository).fetchQuestion(testProfileId)

        testCompletable.assertComplete()
    }

    @Test
    fun `if a question is available refreshQuestions does not fetch the repository`() {
        whenever(repository.getQuestionStatus(testProfileId)).thenReturn(
            Single.just(Available(mock()))
        )

        val testCompletable = questionOfTheDayUseCase.refreshQuestions().test()

        verify(repository, never()).fetchQuestion(testProfileId)

        testCompletable.assertComplete()
    }

    @Test
    fun `refreshQuestions downloads questions for all profiles`() {
        val testProfiles = listOf(
            mockProfileInternal(1),
            mockProfileInternal(2),
            mockProfileInternal(3)
        )

        whenever(testAccount.internalProfiles)
            .thenReturn(testProfiles)

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(testAccount))

        whenever(repository.fetchQuestion(any()))
            .thenReturn(Completable.complete())

        whenever(repository.getQuestionStatus(any()))
            .thenReturn(Single.just(QuestionOfTheDayStatus.NotAvailable))

        val testCompletable = questionOfTheDayUseCase
            .refreshQuestions()
            .test()

        for (profile in testProfiles) {
            verify(repository).fetchQuestion(profile.id)
        }

        testCompletable.assertComplete()
    }

    private fun mockProfileInternal(id: Long): ProfileInternal {
        return mock<ProfileInternal>().apply {
            whenever(this.id).thenReturn(id)
        }
    }
}
